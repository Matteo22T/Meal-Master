package com.matteotocci.app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager; // Mantenuto se usato da getNomeUtenteDalDatabase
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.matteotocci.app.model.Dieta;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;

public class HomePageNutrizionista implements Initializable {

    @FXML
    private Button BottoneDieta;
    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Button BottoneRicette; // FXML ID per il bottone Ricette (generico, per i clienti)
    @FXML
    private Button BottoneRicetteNutrizionista; // Nuovo FXML ID se hai un bottone separato per le ricette del nutrizionista
    @FXML
    private Label nomeUtenteLabelHomePage;
    @FXML
    private TextField ricercaClienteTextField;
    @FXML
    private TableView<Cliente> tabellaClienti;
    @FXML
    private TableColumn<Cliente, String> nomeColonna;
    @FXML
    private TableColumn<Cliente, String> azioniColonna;

    private ObservableList<Cliente> listaClienti = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nomeColonna.setCellValueFactory(new PropertyValueFactory<>("nome"));

        azioniColonna.setCellFactory(new Callback<TableColumn<Cliente, String>, TableCell<Cliente, String>>() {
            @Override
            public TableCell<Cliente, String> call(TableColumn<Cliente, String> param) {
                return new TableCell<>() {
                    final Button visualizzaButton = new Button("Visualizza Dieta");
                    final HBox container = new HBox(visualizzaButton);
                    {
                        container.setSpacing(5);

                        visualizzaButton.getStyleClass().add("visualizza-dieta-button");
                        container.getStyleClass().add("visualizza-dieta-container");

                        visualizzaButton.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            System.out.println("DEBUG (HomePageNutrizionista): Click su Visualizza Dieta per cliente: " + cliente.getNome() + " (ID: " + cliente.getId() + ")");

                            Dieta dietaAssegnata = recuperaDietaAssegnataACliente(cliente.getId());

                            if (dietaAssegnata != null) {
                                try {
                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                                    Parent visualizzaDietaRoot = fxmlLoader.load();

                                    VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();
                                    visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                                    System.out.println("DEBUG (HomePageNutrizionista): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                                    Stage visualizzaDietaStage = new Stage();
                                    visualizzaDietaStage.setScene(new Scene(visualizzaDietaRoot));
                                    visualizzaDietaStage.setTitle("Dieta di " + cliente.getNome());
                                    visualizzaDietaStage.show();

                                } catch (IOException e) {
                                    System.err.println("ERRORE (HomePageNutrizionista): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                                    e.printStackTrace();
                                    showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.");
                                } catch (Exception e) {
                                    System.err.println("ERRORE (HomePageNutrizionista): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                                    e.printStackTrace();
                                    showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.");
                                }
                            } else {
                                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente " + cliente.getNome() + " (ID: " + cliente.getId() + ").");
                                showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta", "Nessuna dieta assegnata");
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(container);
                        }
                    }
                };
            }
        });

        ricercaClienteTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            filtraClienti(newVal);
        });

        setNomeUtenteLabel();
        caricaClientiDelNutrizionista();
    }

    private void setNomeUtenteLabel() {
        Integer userIdFromSession = Session.getUserId();
        if (userIdFromSession != null) {
            String nomeUtente = getNomeUtenteDalDatabase(userIdFromSession.toString());
            nomeUtenteLabelHomePage.setText(
                    (nomeUtente != null && !nomeUtente.isEmpty()) ? nomeUtente : "Nome e Cognome"
            );
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome");
            System.err.println("[ERROR - HomePageNutrizionista] ID utente non disponibile dalla Sessione per impostare il nome.");
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String url = "jdbc:sqlite:database.db"; // Percorso del database SQLite
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?"; // Query SQL per selezionare nome e cognome

        // Utilizza un blocco try-with-resources per gestire la connessione e lo statement
        try (Connection conn = DriverManager.getConnection(url); // Ottiene una connessione al database
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement
            pstmt.setString(1, userId); // Imposta l'ID utente come parametro della query
            ResultSet rs = pstmt.executeQuery(); // Esegue la query

            if (rs.next()) { // Se c'è un risultato
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena nome e cognome
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
            e.printStackTrace(); // Stampa la traccia dell'errore
        }
        return nomeUtente;
    }
    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                dieta = new Dieta(
                        rs.getInt("id"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine"),
                        rs.getInt("id_nutrizionista"),
                        rs.getInt("id_cliente")
                );
                System.out.println("DEBUG (HomePageNutrizionista): Recuperata dieta '" + dieta.getNome() + "' (ID: " + dieta.getId() + ") per cliente ID: " + idCliente);
            } else {
                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (HomePageNutrizionista): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile recuperare la dieta.");
        }
        return dieta;
    }

    private void caricaClientiDelNutrizionista() {
        listaClienti.clear();
        Integer currentNutrizionistaId = Session.getUserId();

        if (currentNutrizionistaId == null) {
            System.err.println("[ERROR - HomePageNutrizionista] ID nutrizionista non disponibile dalla Sessione. Impossibile caricare i clienti.");
            showAlert(Alert.AlertType.WARNING, "Utente non loggato", "ID Nutrizionista non disponibile");
            return;
        }

        String query = "SELECT u.id, u.Nome, u.Cognome FROM Utente u " +
                "JOIN Clienti c ON u.id = c.id_cliente " +
                "WHERE c.id_nutrizionista = ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, currentNutrizionistaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int clienteId = rs.getInt("id");
                String nome = rs.getString("Nome") + " " + rs.getString("Cognome");
                listaClienti.add(new Cliente(clienteId, nome));
            }
            tabellaClienti.setItems(listaClienti);
        } catch (SQLException e) {
            System.err.println("Errore DB (caricaClienti): " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare i clienti.");
        }
    }

    private void filtraClienti(String filtro) {
        ObservableList<Cliente> filtrati = FXCollections.observableArrayList();
        if (filtro == null || filtro.isEmpty()) {
            filtrati.addAll(listaClienti);
        } else {
            String lower = filtro.toLowerCase();
            for (Cliente c : listaClienti) {
                if (c.getNome().toLowerCase().contains(lower)) {
                    filtrati.add(c);
                }
            }
        }
        tabellaClienti.setItems(filtrati);
    }

    public static class Cliente {
        private String nome;
        private int id;

        public Cliente(int id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        public int getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }

    // --- Metodi di Navigazione ---

    @FXML // Annotazione FXML per collegare questo metodo all'azione di un elemento FXML (es. onAction di BottoneDieta).
    private void AccessoDieta(ActionEvent event) { // Metodo per navigare alla vista DietaNutrizionista.
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml")); // Carica l'FXML DietaNutrizionista.
            Parent dietaRoot = fxmlLoader.load(); // Ottiene il nodo radice.
            Stage dietaStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente (stage).
            boolean isMaximized = dietaStage.isMaximized();
            dietaStage.setScene(new Scene(dietaRoot)); // Imposta la nuova scena.
            dietaStage.setTitle("Diete Nutrizionista"); // Imposta il titolo della finestra
            if (isMaximized){
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                dietaStage.setX(screenBounds.getMinX());
                dietaStage.setY(screenBounds.getMinY());
                dietaStage.setWidth(screenBounds.getWidth());
                dietaStage.setHeight(screenBounds.getHeight());}
            dietaStage.show(); // Visualizza la nuova finestra.
        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Diete Nutrizionista'."); // Mostra un avviso di errore.
        }
    }

    @FXML // Annotazione FXML.
    private void AccessoAlimenti(ActionEvent event) { // Metodo per navigare alla vista AlimentiNutrizionista.
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AlimentiNutrizionista.fxml")); // Carica l'FXML AlimentiNutrizionista.
            Parent alimentiRoot = fxmlLoader.load(); // Ottiene il nodo radice.
            Stage alimentiStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            boolean isMaximized = alimentiStage.isMaximized();
            alimentiStage.setScene(new Scene(alimentiRoot)); // Imposta la nuova scena.
            alimentiStage.setTitle("Alimenti"); // Imposta il titolo della finestra.
            if (isMaximized){
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                alimentiStage.setX(screenBounds.getMinX());
                alimentiStage.setY(screenBounds.getMinY());
                alimentiStage.setWidth(screenBounds.getWidth());
                alimentiStage.setHeight(screenBounds.getHeight());
            }
            alimentiStage.show(); // Visualizza la nuova finestra.
        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Alimenti'."); // Mostra un avviso di errore.
        }
    }




    @FXML // Annotazione FXML.
    private void AccessoRicetteNutrizionista(ActionEvent event) { // Metodo per navigare alla vista RicetteNutrizionista.
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/RicetteNutrizionista.fxml")); // Carica l'FXML RicetteNutrizionista.
            Parent ricetteNutrizionistaRoot = fxmlLoader.load(); // Ottiene il nodo radice.
            Stage ricetteNutrizionistaStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            boolean isMaximized = ricetteNutrizionistaStage.isMaximized();
            ricetteNutrizionistaStage.setScene(new Scene(ricetteNutrizionistaRoot)); // Imposta la nuova scena.
            ricetteNutrizionistaStage.setTitle("Le Mie Ricette (Nutrizionista)"); // Imposta il titolo della finestra.
            if (isMaximized){
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                ricetteNutrizionistaStage.setX(screenBounds.getMinX());
                ricetteNutrizionistaStage.setY(screenBounds.getMinY());
                ricetteNutrizionistaStage.setWidth(screenBounds.getWidth());
                ricetteNutrizionistaStage.setHeight(screenBounds.getHeight());
            }
            ricetteNutrizionistaStage.show(); // Visualizza la nuova finestra.
        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Le Mie Ricette (Nutrizionista)'."); // Mostra un avviso di errore.
        }
    }


    @FXML // Annotazione FXML.
    private void openProfiloNutrizionista(MouseEvent event) { // Metodo per navigare alla vista ProfiloNutrizionista (attivato da un click del mouse).
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ProfiloNutrizionista.fxml")); // Carica l'FXML ProfiloNutrizionista.
            Parent profileRoot = fxmlLoader.load(); // Ottiene il nodo radice.
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            boolean isMaximized = profileStage.isMaximized();
            profileStage.setScene(new Scene(profileRoot)); // Imposta la nuova scena.
            profileStage.setTitle("Profilo Nutrizionista"); // Imposta il titolo della finestra.
            if (isMaximized){
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                profileStage.setX(screenBounds.getMinX());
                profileStage.setY(screenBounds.getMinY());
                profileStage.setWidth(screenBounds.getWidth());
                profileStage.setHeight(screenBounds.getHeight());
            }
            profileStage.show(); // Visualizza la nuova finestra.
        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Profilo Nutrizionista'."); // Mostra un avviso di errore.
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType); // Crea una nuova istanza di Alert
        alert.setTitle(title); // Imposta il titolo
        alert.setHeaderText(null); // Non mostra un header text
        alert.setContentText(message); // Imposta il contenuto

        // Cerca il file CSS per lo stile personalizzato degli alert
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            // Se il CSS viene trovato, lo aggiunge al DialogPane dell'alert
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base
            // Aggiunge una classe di stile specifica in base al tipo di alert per una maggiore personalizzazione
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Messaggio di errore se il CSS non è trovato
        }

        alert.showAndWait(); // Mostra l'avviso e attende che l'utente lo chiuda
    }
}