package com.matteotocci.app.controller;

import com.matteotocci.app.model.Dieta;
import com.matteotocci.app.model.Session; // Importa la classe Session!
import com.matteotocci.app.model.SQLiteConnessione;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // Importa questa interfaccia!
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL; // Necessario per Initializable
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

// La classe deve implementare Initializable
public class DietaNutrizionista implements Initializable {

    @FXML
    private Button BottoneClienti;
    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Button BottoneDiete;
    @FXML
    private Button BottoneRicette; // Aggiunto per coerenza con le altre pagine

    @FXML
    private Label nomeUtenteLabelDieta;

    @FXML
    private ListView<Dieta> listaDieteAssegnate;
    @FXML
    private ListView<Dieta> listaDieteDaAssegnare;

    @FXML
    private TextField filtroNomeDietaTextField;

    @FXML
    private VBox contenitorePrincipale;


    private ObservableList<Dieta> observableListaDieteAssegnate = FXCollections.observableArrayList();
    private ObservableList<Dieta> observableListaDieteDaAssegnare = FXCollections.observableArrayList();

    private Map<String, Integer> clientiMap = new HashMap<>();
    private VBox sezioneClientiAssegnazione = null;

    // Questo è il metodo initialize corretto per l'interfaccia Initializable
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listaDieteAssegnate.setItems(observableListaDieteAssegnate);
        listaDieteDaAssegnare.setItems(observableListaDieteDaAssegnare);

        ConfigurazioneCelle(listaDieteAssegnate, true);
        ConfigurazioneCelle(listaDieteDaAssegnare, false);

        filtroNomeDietaTextField.textProperty().addListener((observable, oldValue, newValue) -> filtraDiete(newValue));

        listaDieteAssegnate.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                listaDieteDaAssegnare.getSelectionModel().clearSelection();
                rimuoviSezioneClientiAssegnazione();
            }
        });

        listaDieteDaAssegnare.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null && oldSelection != null) {
                rimuoviSezioneClientiAssegnazione();
            }
        });

        // CHIAMATE ALL'INIZIALIZZAZIONE ALL'AVVIO DEL CONTROLLER
        // Ora il controller è pronto e recupera l'ID e il ruolo dalla Session
        setNomeUtenteLabel();
        caricaListaDiete();
    }

    private void setNomeUtenteLabel() {
        Integer userIdFromSession = Session.getUserId(); // Prende l'ID direttamente dalla Session

        if (nomeUtenteLabelDieta != null  && userIdFromSession != null) {
            String nomeUtenteCompleto = getNomeUtenteDalDatabase(userIdFromSession.toString());
            nomeUtenteLabelDieta.setText((nomeUtenteCompleto != null && !nomeUtenteCompleto.isEmpty()) ? nomeUtenteCompleto : "Nome e Cognome");
        } else {
            nomeUtenteLabelDieta.setText("Nome e Cognome"); // Fallback
            System.err.println("[ERROR - DietaNutrizionista] Impossibile impostare il nome/ruolo utente. Componenti UI o ID/ruolo dalla Sessione sono null.");
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtenteCompleto = null;
        String url = "jdbc:sqlite:database.db"; // Assicurati che il percorso del database sia corretto
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nomeUtenteCompleto = rs.getString("Nome") + " " + rs.getString("Cognome");
            }
        } catch (SQLException e) {
            System.err.println("Errore DB (nome utente): " + e.getMessage());
        }
        return nomeUtenteCompleto;
    }

    private void caricaListaDiete() {
        observableListaDieteAssegnate.clear();
        observableListaDieteDaAssegnare.clear();

        Integer currentNutrizionistaId = Session.getUserId(); // Ottieni l'ID del nutrizionista dalla Session

        if (currentNutrizionistaId == null) {
            System.err.println("[ERROR - DietaNutrizionista] ID nutrizionista non disponibile. Impossibile caricare le diete.");
            return;
        }

        String url = "jdbc:sqlite:database.db";
        String query = "SELECT d.id, d.nome_dieta, d.data_inizio, d.data_fine, d.id_cliente, COUNT(gd.id_giorno_dieta) AS numero_giorni " +
                "FROM Diete d " +
                "LEFT JOIN Giorno_dieta gd ON d.id = gd.id_dieta " +
                "WHERE d.id_nutrizionista = ? " +
                "GROUP BY d.id, d.nome_dieta, d.data_inizio, d.data_fine, d.id_cliente";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, currentNutrizionistaId); // Usa l'ID Integer direttamente

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int idDieta = rs.getInt("id");
                String nomeDieta = rs.getString("nome_dieta");
                String dataInizio = rs.getString("data_inizio");
                String dataFine = rs.getString("data_fine");
                Object idClienteObj = rs.getObject("id_cliente");
                int numeroGiorni = rs.getInt("numero_giorni");

                Dieta dieta = new Dieta(idDieta, nomeDieta, dataInizio, dataFine);
                dieta.setNumeroGiorni(numeroGiorni);
                if (idClienteObj != null) {
                    dieta.setIdCliente((Integer) idClienteObj);
                } else {
                    dieta.setIdCliente(0); // Nessun cliente assegnato
                }

                if (dieta.getIdCliente() == 0) {
                    observableListaDieteDaAssegnare.add(dieta);
                } else {
                    observableListaDieteAssegnate.add(dieta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore DB (carica diete): " + e.getMessage());
        }
    }

    private void ConfigurazioneCelle(ListView<Dieta> listView, boolean ListaAssegnata) {
        listView.setCellFactory(lv -> new ListCell<Dieta>() {
            private final HBox hbox = new HBox(10);
            private final Label nomeLabel = new Label();
            private final Button btnModifica = new Button("Modifica Piano");
            private final Button btnAssegna = new Button("Assegna Piano");
            private final Button btnAnnullaAssegnazione = new Button("Rimuovi");

            {
                hbox.getChildren().addAll(nomeLabel, btnModifica);
                if (!ListaAssegnata) { // Aggiungi il bottone Assegna solo per la lista "Da Assegnare"
                    hbox.getChildren().add(btnAssegna);
                } else { // Aggiungi il bottone Annulla Assegnazione solo per la lista "Assegnate"
                    hbox.getChildren().add(btnAnnullaAssegnazione);
                }

                // Stile dei bottoni
                btnModifica.getStyleClass().add("button-primary");
                btnAssegna.getStyleClass().add("button-primary");
                btnAnnullaAssegnazione.getStyleClass().add("button-danger");

                btnModifica.setOnAction(event -> {
                    Dieta dieta = getItem();
                    if (dieta != null) {
                        apriFinestraModificaGiornoDieta(dieta, ((Node) event.getSource()).getScene().getWindow());
                    }
                });

                btnAssegna.setOnAction(event -> {
                    Dieta dieta = getItem();
                    if (dieta != null) {
                        listaDieteDaAssegnare.getSelectionModel().select(dieta);
                        mostraSelezioneClienti(dieta);
                    }
                });

                btnAnnullaAssegnazione.setOnAction(event -> {
                    Dieta dieta = getItem();
                    if (dieta != null) {
                        annullaAssegnazioneDieta(dieta);
                    }
                });
            }

            @Override
            protected void updateItem(Dieta dieta, boolean empty) {
                super.updateItem(dieta, empty);

                if (empty || dieta == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    nomeLabel.setText(dieta.getNome());
                    setGraphic(hbox);
                }
            }
        });
    }

    private void rimuoviSezioneClientiAssegnazione() {
        if (sezioneClientiAssegnazione != null && contenitorePrincipale.getChildren().contains(sezioneClientiAssegnazione)) {
            contenitorePrincipale.getChildren().remove(sezioneClientiAssegnazione);
            sezioneClientiAssegnazione = null;
        }
    }

    private void mostraSelezioneClienti(Dieta dieta) {
        rimuoviSezioneClientiAssegnazione();

        // Blocco 1: Se la dieta selezionata è già assegnata, non mostrare la selezione del cliente.
        if (dieta.getIdCliente() != 0) { // Un id_cliente diverso da 0 significa che è assegnata
            showAlert(Alert.AlertType.WARNING,"Attenzione", "Questa dieta è già assegnata. Annulla prima l'assegnazione corrente se vuoi cambiarla.");
            return;
        }

        // Carica i clienti disponibili (cioè quelli senza una dieta assegnata)
        List<String> clientiDisponibili = caricaClienti(); // Questo metodo ora filtra già i clienti con dieta

        // Blocco 2: Se non ci sono clienti disponibili per l'assegnazione
        if (clientiDisponibili.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,"Attenzione", "Nessun cliente disponibile per l'assegnazione di una nuova dieta. Tutti i clienti hanno già una dieta assegnata.");
            return;
        }

        // Se ci sono clienti disponibili e la dieta non è già assegnata, procedi con la creazione dell'interfaccia
        sezioneClientiAssegnazione = new VBox(5);
        sezioneClientiAssegnazione.setPadding(new Insets(15));
        sezioneClientiAssegnazione.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-radius: 5px;");

        ToggleGroup toggleGroup = new ToggleGroup();
        // clientiMap viene riempito da caricaClienti()

        sezioneClientiAssegnazione.getChildren().add(new Label("Seleziona un cliente:"));
        sezioneClientiAssegnazione.getChildren().add(new Separator());

        for (Map.Entry<String, Integer> entry : clientiMap.entrySet()) {
            RadioButton rb = new RadioButton(entry.getKey());
            rb.setToggleGroup(toggleGroup);
            sezioneClientiAssegnazione.getChildren().add(rb);
        }

        Button btnConferma = new Button("Conferma Assegnazione");
        btnConferma.setDisable(true);
        btnConferma.getStyleClass().add("button-positive");

        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            btnConferma.setDisable(newVal == null);
        });

        btnConferma.setOnAction(event -> {
            RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
            if (selected != null) {
                String selectedCliente = selected.getText().trim();
                assegnaDietaACliente(dieta, selectedCliente); // Questo metodo ora aggiorna la dieta nel DB
                rimuoviSezioneClientiAssegnazione(); // Rimuovi la sezione dopo l'assegnazione
            }
        });

        sezioneClientiAssegnazione.getChildren().add(btnConferma);
        contenitorePrincipale.getChildren().add(sezioneClientiAssegnazione);
    }

    private void assegnaDietaACliente(Dieta dieta, String nomeCliente) {
        Integer idCliente = clientiMap.get(nomeCliente);

        if (idCliente == null) {
            showAlert(Alert.AlertType.ERROR,"Errore", "Cliente non valido.");
            return;
        }

        try (Connection conn = SQLiteConnessione.connector()) {
            String sql = "UPDATE Diete SET id_cliente = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idCliente);
            ps.setInt(2, dieta.getId());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                showAlert(Alert.AlertType.INFORMATION,"Successo", "Dieta assegnata correttamente.");
                caricaListaDiete(); // Ricarica le liste dopo l'assegnazione
            } else {
                showAlert(Alert.AlertType.ERROR,"Errore", "Nessuna dieta aggiornata.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Errore", "Errore durante l'assegnazione: " + e.getMessage());
        }
    }

    private void annullaAssegnazioneDieta(Dieta dieta) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Annullamento Assegnazione");
        alert.setHeaderText("Sei sicuro di voler annullare l'assegnazione della dieta \"" + dieta.getNome() + "\"?");
        alert.setContentText("Questa azione renderà la dieta nuovamente disponibile per l'assegnazione.");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.getDialogPane().getStyleClass().add("alert-confirmation");


        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String url = "jdbc:sqlite:database.db"; // Assicurati che il percorso del database sia corretto
            String sql = "UPDATE Diete SET id_cliente = NULL WHERE id = ?";

            try (Connection conn = SQLiteConnessione.connector();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, dieta.getId());

                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION,"Successo", "Assegnazione della dieta annullata correttamente.");
                    caricaListaDiete(); // Ricarica le liste dopo l'annullamento
                } else {
                    showAlert(Alert.AlertType.ERROR,"Errore", "Nessuna dieta aggiornata. L'annullamento dell'assegnazione potrebbe non essere avvenuto.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR,"Errore DB", "Errore durante l'annullamento dell'assegnazione: " + e.getMessage());
            }
        }
    }

    private List<String> caricaClienti() {
        clientiMap.clear();
        List<String> clienti = new ArrayList<>();
        String sql = "SELECT u.id, u.nome, u.cognome " +
                "FROM Utente u " +
                "JOIN Clienti c ON u.id = c.id_cliente " +
                "WHERE c.id_nutrizionista = ? " +
                "AND u.id NOT IN (SELECT id_cliente FROM Diete WHERE id_cliente IS NOT NULL)"; // Esclude clienti che hanno già una dieta assegnata

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Integer idNutrizionista = Session.getUserId(); // Ottieni l'ID del nutrizionista dalla Session
            if (idNutrizionista == null) {
                System.err.println("[ERROR - DietaNutrizionista] ID nutrizionista non disponibile dalla Sessione. Impossibile caricare i clienti.");
                return clienti;
            }
            pstmt.setInt(1, idNutrizionista);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String nomeCompleto = (rs.getString("nome") + " " + rs.getString("cognome")).trim().replaceAll("\\s+", " ");
                int idCliente = rs.getInt("id");

                clienti.add(nomeCompleto);
                clientiMap.put(nomeCompleto, idCliente);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Errore DB", "Errore durante il caricamento dei clienti disponibili: " + e.getMessage());
        }
        return clienti;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Apply the base style class
            // Add specific style class based on AlertType for custom styling
            if (alertType == Alert.AlertType.INFORMATION) {
                alert.getDialogPane().getStyleClass().add("alert-information");
            } else if (alertType == Alert.AlertType.WARNING) {
                alert.getDialogPane().getStyleClass().add("alert-warning");
            } else if (alertType == Alert.AlertType.ERROR) {
                alert.getDialogPane().getStyleClass().add("alert-error");
            } else if (alertType == Alert.AlertType.CONFIRMATION) {
                alert.getDialogPane().getStyleClass().add("alert-confirmation");
            }
        } else {
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Corrected error message
        }

        alert.showAndWait();
    }



    private void apriFinestraModificaGiornoDieta(Dieta dieta, Window ownerWindow) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiGiornoDieta.fxml"));
            Parent root = loader.load();

            AggiungiGiornoDieta controller = loader.getController();
            controller.impostaDietaDaModificare(dieta);
            controller.setTitoloPiano(dieta.getNome());
            controller.setNumeroGiorni(dieta.getNumeroGiorni());

            Stage stage = new Stage();
            stage.setTitle("Modifica Giorno Dieta - " + dieta.getNome());
            stage.setScene(new Scene(root));
            stage.initOwner(ownerWindow);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Ricarica le diete dopo che la finestra di modifica si è chiusa
            caricaListaDiete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filtraDiete(String searchText) {
        // Ricarica le diete complete prima di filtrare per assicurarsi di avere tutti i dati
        caricaListaDiete();

        String lowerCaseFilter = searchText.toLowerCase();

        ObservableList<Dieta> filteredAssegnate = observableListaDieteAssegnate.stream()
                .filter(dieta -> dieta.getNome().toLowerCase().contains(lowerCaseFilter))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        ObservableList<Dieta> filteredDaAssegnare = observableListaDieteDaAssegnare.stream()
                .filter(dieta -> dieta.getNome().toLowerCase().contains(lowerCaseFilter))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        // Aggiorna le ObservableList con i risultati filtrati
        observableListaDieteAssegnate.setAll(filteredAssegnate);
        observableListaDieteDaAssegnare.setAll(filteredDaAssegnare);
    }

    @FXML
    private void vaiAggiungiNuovaDieta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/NuovaDieta.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nuova Dieta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            Window owner = ((Node) event.getSource()).getScene().getWindow();
            stage.initOwner(owner);

            // Quando la finestra di NuovaDieta si chiude, ricarica le liste
            stage.setOnHidden(e -> caricaListaDiete());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminaDietaSelezionata(ActionEvent event) {
        Dieta dietaSelezionata = null;

        if (listaDieteDaAssegnare.getSelectionModel().getSelectedItem() != null) {
            dietaSelezionata = listaDieteDaAssegnare.getSelectionModel().getSelectedItem();
        }

        if (dietaSelezionata == null) {
            showAlert(Alert.AlertType.ERROR,"Attenzione", "Seleziona una dieta non assegnata da eliminare.");
            return;
        }


        boolean conferma = confermaEliminazione(dietaSelezionata.getNome());
        if (!conferma) {
            return; // L'utente ha annullato
        }


        try (Connection conn = SQLiteConnessione.connector()) {
            conn.setAutoCommit(false); // Inizia una transazione

            try {
                // 1. Recupera gli ID dei giorni di dieta associati a questa dieta
                String queryGiorni = "SELECT id_giorno_dieta FROM Giorno_dieta WHERE id_dieta = ?";
                PreparedStatement psGiorni = conn.prepareStatement(queryGiorni);
                psGiorni.setInt(1, dietaSelezionata.getId());
                ResultSet rs = psGiorni.executeQuery();

                java.util.List<Integer> listaIdGiorni = new java.util.ArrayList<>();
                while (rs.next()) {
                    listaIdGiorni.add(rs.getInt("id_giorno_dieta"));
                }
                rs.close();
                psGiorni.close();

                // 2. Elimina gli alimenti associati a questi giorni di dieta (se ce ne sono)
                if (!listaIdGiorni.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("DELETE FROM DietaAlimenti WHERE id_giorno_dieta IN (");
                    for (int i = 0; i < listaIdGiorni.size(); i++) {
                        sb.append("?");
                        if (i < listaIdGiorni.size() - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");

                    PreparedStatement psEliminaAlimenti = conn.prepareStatement(sb.toString());
                    for (int i = 0; i < listaIdGiorni.size(); i++) {
                        psEliminaAlimenti.setInt(i + 1, listaIdGiorni.get(i));
                    }
                    psEliminaAlimenti.executeUpdate();
                    psEliminaAlimenti.close();
                }

                // 3. Elimina i giorni di dieta
                String eliminaGiorni = "DELETE FROM Giorno_dieta WHERE id_dieta = ?";
                try (PreparedStatement ps = conn.prepareStatement(eliminaGiorni)) {
                    ps.setInt(1, dietaSelezionata.getId());
                    ps.executeUpdate();
                }

                // 4. Elimina la dieta stessa
                String eliminaDieta = "DELETE FROM Diete WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(eliminaDieta)) {
                    ps.setInt(1, dietaSelezionata.getId());
                    ps.executeUpdate();
                }

                conn.commit(); // Conferma la transazione

                // Rimuovi la dieta dalla ObservableList corrispondente nell'UI

                observableListaDieteDaAssegnare.remove(dietaSelezionata);

                showAlert(Alert.AlertType.INFORMATION,"Successo", "Dieta eliminata correttamente.");

            } catch (SQLException e) {
                conn.rollback(); // Esegui il rollback in caso di errore
                System.err.println("Errore durante l'eliminazione della dieta (rollback): " + e.getMessage());
                showAlert(Alert.AlertType.ERROR,"Errore Eliminazione", "Si è verificato un errore durante l'eliminazione della dieta: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Errore di connessione DB: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR,"Errore Connessione", "Impossibile connettersi al database: " + e.getMessage());
        }
    }

    private boolean confermaEliminazione(String nomeDieta) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Eliminazione");
        alert.setHeaderText("Sei sicuro di voler eliminare la dieta \"" + nomeDieta + "\"?");
        alert.setContentText("La dieta non potrà essere recuperata.");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.getDialogPane().getStyleClass().add("alert-confirmation");


        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // --- Metodi di Navigazione ---
    // Questi metodi ora NON passano più l'ID utente esplicitamente.
    // I controller delle pagine di destinazione dovranno recuperare l'ID dalla Session.

    @FXML
    private void vaiAiClienti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml"));
            Parent homePageRoot = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homePageRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openProfiloNutrizionista(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ProfiloNutrizionista.fxml"));
            Parent profileRoot = fxmlLoader.load();
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent alimentiRoot = fxmlLoader.load();
            Stage alimentiStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            alimentiStage.setScene(new Scene(alimentiRoot));
            alimentiStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/RicetteNutrizionista.fxml"));
            Parent ricetteRoot = fxmlLoader.load();
            Stage ricetteStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ricetteStage.setScene(new Scene(ricetteRoot));
            ricetteStage.setTitle("Ricette");
            ricetteStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}