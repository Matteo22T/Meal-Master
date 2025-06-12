package com.matteotocci.app.controller;

import com.matteotocci.app.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;

public class Ricette {

    @FXML private Button BottoneAlimenti;
    @FXML private Button BottoneHome;
    @FXML private Button BottoneRicette;
    @FXML private Label nomeUtenteLabelHomePage;
    @FXML private ComboBox<String> categorieRicette;
    @FXML private CheckBox mieiAlimentiCheckBox;
    @FXML private TextField cercaRicetta;
    @FXML private TableView<Ricetta> tableViewRicette;
    @FXML private TableColumn<Ricetta, String> nomeCol;
    @FXML private TableColumn<Ricetta, String> descrizioneCol;
    @FXML private TableColumn<Ricetta, String> categoriaCol;


    private int offset = 0;
    private final int LIMIT = 50;
    private boolean isLoading = false;

    private Dieta dietaAssegnata;

    public LoginModel loginModel = new LoginModel();
    String email = loginModel.getEmail(Session.getUserId());
    String ruolo = loginModel.getRuoloUtente(email);




    private void setNomeUtenteLabel() {
        String nomeUtente = getNomeUtenteDalDatabase(Session.getUserId().toString());
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            nomeUtenteLabelHomePage.setText(nomeUtente);
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome");
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
        }
        return nomeUtente;
    }

    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String url = "jdbc:sqlite:database.db";
        // Query per recuperare la dieta con l'ID del cliente
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        try (Connection conn = SQLiteConnessione.connector(); // Usa SQLiteConnessione.connector()
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                dieta = new Dieta(
                        rs.getInt("id"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine"),
                        rs.getInt("id_nutrizionista"), // Assicurati che il costruttore di Dieta supporti questi campi
                        rs.getInt("id_cliente")
                );
                System.out.println("DEBUG (HomePageNutrizionista): Recuperata dieta '" + dieta.getNome() + "' (ID: " + dieta.getId() + ") per cliente ID: " + idCliente);
            } else {
                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (HomePageNutrizionista): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta;
    }

    @FXML
    public void initialize() {
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        descrizioneCol.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        categoriaCol.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        Integer userIdFromSession = Session.getUserId();


        if (userIdFromSession != null) {
            System.out.println("[DEBUG - HomePage] ID utente da Sessione: " + userIdFromSession);
            this.dietaAssegnata=recuperaDietaAssegnataACliente(userIdFromSession);
            setNomeUtenteLabel();}

        cercaRicette("", false);
        popolaCategorie();

        categorieRicette.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(cercaRicetta.getText(), false);
        });

        mieiAlimentiCheckBox.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(cercaRicetta.getText(), false);
        });

        ScrollBar scrollBar = getVerticalScrollbar(tableViewRicette);
        if (scrollBar != null) {
            scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == scrollBar.getMax()) {
                    caricaAltri();
                }
            });
        }
    }

    private ScrollBar getVerticalScrollbar(TableView<?> table) {
        for (Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                return sb;
            }
        }
        return null;
    }

    private void caricaAltri() {
        if (!isLoading) {
            isLoading = true;
            cercaRicette(cercaRicetta.getText(), true);
            isLoading = false;
        }
    }

    @FXML
    private void handleCercaRicetta(ActionEvent event) {
        offset = 0;
        tableViewRicette.getItems().clear();
        cercaRicette(cercaRicetta.getText(), false);
    }

    public void cercaRicette(String filtro, boolean append) {
        ObservableList<Ricetta> ricette = append ? tableViewRicette.getItems() : FXCollections.observableArrayList();

        String categoria = categorieRicette.getSelectionModel().getSelectedItem();
        boolean soloMiei = mieiAlimentiCheckBox.isSelected();
        StringBuilder query = new StringBuilder("SELECT * FROM Ricette WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?");
        }
        if (soloMiei && Session.getUserId() != 0) {
            query.append(" AND id_utente = ?");
        }
        query.append(" LIMIT ? OFFSET ?");

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%");
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria);
            }
            if (soloMiei && Session.getUserId() != 0) {
                stmt.setInt(paramIndex++, Session.getUserId());
            }
            stmt.setInt(paramIndex++, LIMIT);
            stmt.setInt(paramIndex++, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ricetta ricetta = new Ricetta(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descrizione"),
                            rs.getString("categoria"),
                            rs.getInt("id_utente"),
                            rs.getDouble("kcal"),
                            rs.getDouble("proteine"),
                            rs.getDouble("carboidrati"),
                            rs.getDouble("grassi"),
                            rs.getDouble("grassi_saturi"), // Assicurati che il nome della colonna sia esatto
                            rs.getDouble("zuccheri"),
                            rs.getDouble("fibre"),
                            rs.getDouble("sale")
                    );
                    ricette.add(ricetta);
                }

                tableViewRicette.setItems(ricette);
                offset += LIMIT;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void popolaCategorie() {
        ObservableList<String> categoriePrefissate = FXCollections.observableArrayList(
                "Tutte", "Colazione", "Spuntino", "Pranzo", "Merenda", "Cena"
        );
        categorieRicette.setItems(categoriePrefissate);
        categorieRicette.getSelectionModel().selectFirst();
    }

    @FXML
    public void resetRicerca() {
        offset = 0;
        tableViewRicette.getItems().clear();
    }

    @FXML
    public String getFiltro() {
        return cercaRicetta != null ? cercaRicetta.getText() : "";
    }

    @FXML
    private void apriDettaglioRicetta(MouseEvent event) {
        if (event.getClickCount() == 2 && !tableViewRicette.getSelectionModel().isEmpty()) {
            Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettaglioRicetta.fxml"));
                Parent root = loader.load();

                DettaglioRicettaController controller = loader.getController();
                controller.setRicetta(ricettaSelezionata);
                controller.setRicettaController(this);
                controller.setOrigineFXML("Ricette.fxml");

                Stage stage = new Stage();
                stage.setTitle("Dettaglio Ricetta");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Stage dietaStage; // Per gestire apertura/chiusura della finestra dieta

    @FXML
    private void AccessoPianoAlimentare(ActionEvent event) {
        if (dietaAssegnata != null) {
            try {
                // Chiudi la finestra precedente se è già aperta
                if (dietaStage != null && dietaStage.isShowing()) {
                    dietaStage.close();
                }


                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                Parent visualizzaDietaRoot = fxmlLoader.load();

                // PASSO 2: Ottieni il controller della nuova finestra
                VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();

                // PASSO 3: Passa l'oggetto Dieta al controller della nuova finestra
                visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                System.out.println("DEBUG (HomePage): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                dietaStage = new Stage();
                dietaStage.setScene(new Scene(visualizzaDietaRoot));
                dietaStage.show();

            } catch (IOException e) {
                System.err.println("ERRORE (HomePage): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.");
            } catch (Exception e) {
                System.err.println("ERRORE (HomePage): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.");
            }
        } else {
            System.out.println("DEBUG (HomePage): Nessuna dieta trovata per il cliente  (ID: " + Session.getUserId() + ").");
            showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta", "Nessuna dieta assegnata, il cliente non ha diete assegnate o non è stato possibile recuperarle.");
        }
    }

    // Cambia scena a Alimenti, passando l'ID utente
    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/AlimentiNutrizionista.fxml";
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/Alimenti.fxml";
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            // Alimenti si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la pagina Alimenti");
        }
    }

    // Cambia scena a HomePage, passando l'ID utente
    @FXML
    private void AccessoHome(ActionEvent event) {

        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/HomePageNutrizionista.fxml";
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/HomePage.fxml";
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la HomePage");
        }
    }

    // Cambia scena a PaginaProfilo passando l'ID utente
    @FXML
    private void AccessoProfilo(MouseEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/ProfiloNutrizionista.fxml";
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/PaginaProfilo.fxml";
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            // PaginaProfilo si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la Pagina Profilo");
        }
    }
    @FXML
    private void handleApriAggiuntaRicetta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiRicetta.fxml"));
            Parent root = loader.load();

            AggiungiRicetteController controller = loader.getController();
            controller.setRicettaController(this);

            Stage stage = new Stage();
            stage.setTitle("Aggiungi Ricetta");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void AccessoDieta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml"));
            Parent dietaRoot = fxmlLoader.load();
            Stage dietaStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            dietaStage.setScene(new Scene(dietaRoot));
            dietaStage.setTitle("Diete Nutrizionista"); // Titolo più specifico
            dietaStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Diete Nutrizionista'.");
        }
    }

    @FXML
    private void AccessoRicette(ActionEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/RicetteNutrizionista.fxml";
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/Ricette.fxml";
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            // Ricette si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la pagina Ricette");
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

