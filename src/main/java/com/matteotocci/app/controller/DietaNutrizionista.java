package com.matteotocci.app.controller;

import com.matteotocci.app.model.Dieta;
import com.matteotocci.app.model.Session;
import com.matteotocci.app.model.SQLiteConnessione;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DietaNutrizionista {

    @FXML
    private Button BottoneClienti;
    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Button BottoneDiete;

    @FXML
    private Label nomeUtenteLabelDieta;
    @FXML
    private Label ruoloUtenteLabelDieta;

    @FXML
    private ListView<Dieta> listaDieteAssegnate;
    @FXML
    private ListView<Dieta> listaDieteDaAssegnare;

    @FXML
    private TextField filtroNomeDietaTextField;
    @FXML
    private ImageView profileImage;
    @FXML
    private VBox contenitorePrincipale;

    private String loggedInUserId;
    private ObservableList<Dieta> observableListaDieteAssegnate = FXCollections.observableArrayList();  //le observable notificano i listener ogni volta che il loro contenuto cambia
    private ObservableList<Dieta> observableListaDieteDaAssegnare = FXCollections.observableArrayList();

    private Map<String, Integer> clientiMap = new HashMap<>();
    private VBox sezioneClientiAssegnazione = null;

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
        setNomeUtenteLabel();
        caricaListaDiete();
    }

    @FXML
    private void vaiAiClienti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml"));
            Parent homePageRoot = fxmlLoader.load();
            HomePageNutrizionista homePageController = fxmlLoader.getController();
            homePageController.setLoggedInUserId(loggedInUserId);
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
            ProfiloNutrizionista profileController = fxmlLoader.getController();
            profileController.setLoggedInUserId(loggedInUserId);
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        listaDieteAssegnate.setItems(observableListaDieteAssegnate); // la ListView "osserverà" (observe) la ObservableList.
        // Ogni volta che gli elementi vengono aggiunti, rimossi o modificati in observableListaDieteAssegnate,
        // la listaDieteAssegnate si aggiornerà automaticamente nell'interfaccia utente
        listaDieteDaAssegnare.setItems(observableListaDieteDaAssegnare);

        ConfigurazioneCelle(listaDieteAssegnate, true); //personalizzare l'aspetto e il comportamento di ogni singola
        // voce all'interno della listaDieteAssegnate.
        ConfigurazioneCelle(listaDieteDaAssegnare, false);

        filtroNomeDietaTextField.textProperty().addListener((observable, oldValue, newValue) -> filtraDiete(newValue));
         //Quando l'applicazione si avvia, il metodo initialize() viene chiamato.
        //All'interno di initialize(), viene impostato un listener sul testo del filtroNomeDietaTextField.
        //L'utente inizia a digitare nel filtroNomeDietaTextField (ad esempio, digita "p").
        //La textProperty() del TextField rileva che il suo valore è cambiato (da "" a "p").
        //La textProperty() notifica tutti i suoi listener, inclusa la lambda che hai registrato.
        //La lambda viene eseguita, e chiama filtraDiete("p").
        //Il metodo filtraDiete() aggiorna le ListView mostrando solo le diete che contengono "p" nel loro nome.
        //L'utente digita un'altra lettera (ad esempio, "a", ora il testo è "pa").
        //Il processo si ripete: textProperty() rileva il cambiamento, notifica il listener, la lambda esegue filtraDiete("pa"), e le liste si aggiornano di conseguenza.
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
    }

    private void ConfigurazioneCelle(ListView<Dieta> listView, boolean ListaAssegnata) {
        listView.setCellFactory(lv -> new ListCell<Dieta>() {
            private final HBox hbox = new HBox(10);
            private final Label nomeLabel = new Label();
            private final Button btnModifica = new Button("Modifica Piano");
            private final Button btnAssegna = new Button("Assegna Piano");
            private final Button btnAnnullaAssegnazione = new Button("Annulla Assegnazione");

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
            mostraAlert("Attenzione", "Questa dieta è già assegnata. Annulla prima l'assegnazione corrente se vuoi cambiarla.");
            return;
        }

        // Carica i clienti disponibili (cioè quelli senza una dieta assegnata)
        List<String> clientiDisponibili = caricaClienti(); // Ora questo metodo filtra già i clienti con dieta

        // Blocco 2: Se non ci sono clienti disponibili per l'assegnazione
        if (clientiDisponibili.isEmpty()) {
            mostraAlert("Attenzione", "Nessun cliente disponibile per l'assegnazione di una nuova dieta. Tutti i clienti hanno già una dieta assegnata.");
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
            mostraAlert("Errore", "Cliente non valido.");
            return;
        }

        try (Connection conn = SQLiteConnessione.connector()) {
            String sql = "UPDATE Diete SET id_cliente = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idCliente);
            ps.setInt(2, dieta.getId());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                mostraAlert("Successo", "Dieta assegnata correttamente.");
                caricaListaDiete();
            } else {
                mostraAlert("Errore", "Nessuna dieta aggiornata.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostraAlert("Errore", "Errore durante l'assegnazione: " + e.getMessage());
        }
    }

    private void annullaAssegnazioneDieta(Dieta dieta) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Annullamento Assegnazione");
        alert.setHeaderText("Sei sicuro di voler annullare l'assegnazione della dieta \"" + dieta.getNome() + "\"?");
        alert.setContentText("Questa azione renderà la dieta nuovamente disponibile per l'assegnazione.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String url = "jdbc:sqlite:database.db";
            String sql = "UPDATE Diete SET id_cliente = NULL WHERE id = ?";

            try (Connection conn = SQLiteConnessione.connector();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, dieta.getId());

                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    mostraAlert("Successo", "Assegnazione della dieta annullata correttamente.");
                    caricaListaDiete();
                } else {
                    mostraAlert("Errore", "Nessuna dieta aggiornata. L'annullamento dell'assegnazione potrebbe non essere avvenuto.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                mostraAlert("Errore DB", "Errore durante l'annullamento dell'assegnazione: " + e.getMessage());
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
                "AND u.id NOT IN (SELECT id_cliente FROM Diete WHERE id_cliente IS NOT NULL)"; // AGGIUNTA CHIAVE: esclude clienti che hanno già una dieta assegnata

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int idNutrizionista = Session.getUserId(); // Assicurati che Session.getUserId() sia corretto
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
            mostraAlert("Errore DB", "Errore durante il caricamento dei clienti disponibili: " + e.getMessage());
        }
        return clienti;
    }

    private void mostraAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
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

            caricaListaDiete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setNomeUtenteLabel() {
        if (ruoloUtenteLabelDieta != null && nomeUtenteLabelDieta != null && loggedInUserId != null) {
            String nomeUtenteCompleto = getNomeUtenteDalDatabase(loggedInUserId);
            nomeUtenteLabelDieta.setText((nomeUtenteCompleto != null && !nomeUtenteCompleto.isEmpty()) ? nomeUtenteCompleto : "Nome e Cognome");
        } else {
            System.err.println("Errore: ruoloUtenteLabelDieta o nomeUtenteLabelDieta o loggedInUserId sono null.");
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtenteCompleto = null;
        String url = "jdbc:sqlite:database.db";
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

        // RIMOSSO: boolean soloDieteDaAssegnare = (mostraSoloDieteDaAssegnareCheckBox != null && mostraSoloDieteDaAssegnareCheckBox.isSelected());

        String url = "jdbc:sqlite:database.db";
        String query = "SELECT d.id, d.nome_dieta, d.data_inizio, d.data_fine, d.id_cliente, COUNT(gd.id_giorno_dieta) AS numero_giorni " +
                "FROM Diete d " +
                "LEFT JOIN Giorno_dieta gd ON d.id = gd.id_dieta " +
                "WHERE d.id_nutrizionista = ? " +
                "GROUP BY d.id, d.nome_dieta, d.data_inizio, d.data_fine, d.id_cliente";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Session.getUserId());

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
                    dieta.setIdCliente(0);
                }

                if (dieta.getIdCliente() == 0) {
                    observableListaDieteDaAssegnare.add(dieta);
                } else {
                    // RIMOSSO: if (!soloDieteDaAssegnare) {
                    observableListaDieteAssegnate.add(dieta);
                    // }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore DB (carica diete): " + e.getMessage());
        }
    }

    private void filtraDiete(String searchText) {
        // La logica di filtro qui è rimasta la stessa, ma ora caricaListaDiete() non considera la checkbox
        caricaListaDiete();

        String lowerCaseFilter = searchText.toLowerCase();

        ObservableList<Dieta> filteredAssegnate = observableListaDieteAssegnate.stream()
                .filter(dieta -> dieta.getNome().toLowerCase().contains(lowerCaseFilter))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        ObservableList<Dieta> filteredDaAssegnare = observableListaDieteDaAssegnare.stream()
                .filter(dieta -> dieta.getNome().toLowerCase().contains(lowerCaseFilter))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

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

            stage.setOnHidden(e -> caricaListaDiete());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminaDietaSelezionata(ActionEvent event) {
        Dieta dietaSelezionata = null;

        if (listaDieteAssegnate.getSelectionModel().getSelectedItem() != null) {
            dietaSelezionata = listaDieteAssegnate.getSelectionModel().getSelectedItem();
        } else if (listaDieteDaAssegnare.getSelectionModel().getSelectedItem() != null) {
            dietaSelezionata = listaDieteDaAssegnare.getSelectionModel().getSelectedItem();
        }

        if (dietaSelezionata == null) {
            System.err.println("Nessuna dieta selezionata da eliminare.");
            mostraAlert("Attenzione", "Seleziona una dieta da eliminare.");
            return;
        }

        if (dietaSelezionata.getIdCliente() != 0) {
            mostraAlert("Errore", "Impossibile eliminare una dieta assegnata a un cliente. Rimuovi prima l'assegnazione.");
            return;
        }

        boolean conferma = confermaEliminazione(dietaSelezionata.getNome());
        if (!conferma) {
            return;
        }

        String url = "jdbc:sqlite:database.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);

            try {
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

                String eliminaGiorni = "DELETE FROM Giorno_dieta WHERE id_dieta = ?";
                try (PreparedStatement ps = conn.prepareStatement(eliminaGiorni)) {
                    ps.setInt(1, dietaSelezionata.getId());
                    ps.executeUpdate();
                }

                String eliminaDieta = "DELETE FROM Diete WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(eliminaDieta)) {
                    ps.setInt(1, dietaSelezionata.getId());
                    ps.executeUpdate();
                }

                conn.commit();

                if (dietaSelezionata.getIdCliente() != 0) {
                    observableListaDieteAssegnate.remove(dietaSelezionata);
                } else {
                    observableListaDieteDaAssegnare.remove(dietaSelezionata);
                }

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Errore durante l'eliminazione: " + e.getMessage());
                mostraAlert("Errore Eliminazione", "Si è verificato un errore durante l'eliminazione della dieta: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Errore di connessione DB: " + e.getMessage());
            mostraAlert("Errore Connessione", "Impossibile connettersi al database: " + e.getMessage());
        }
    }

    private boolean confermaEliminazione(String nomeDieta) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Eliminazione");
        alert.setHeaderText("Sei sicuro di voler eliminare la dieta \"" + nomeDieta + "\"?");
        alert.setContentText("Questa operazione non può essere annullata.");

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK;
    }
}