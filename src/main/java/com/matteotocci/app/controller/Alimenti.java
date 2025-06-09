package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session; // Importa la classe Session
import com.matteotocci.app.model.Dieta; // Necessario per recuperare la dieta in Alimenti
import javafx.application.Platform;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional; // Necessario per showAlert

public class Alimenti {

    @FXML private Button BottoneAlimenti;
    @FXML private ComboBox<String> categoriaComboBox;
    @FXML private CheckBox mieiAlimentiCheckBox;
    @FXML private Button BottoneHome;
    @FXML private Label nomeUtenteLabelHomePage;
    @FXML private Button BottoneRicette;
    @FXML private TextField cercaAlimento;
    @FXML private TableView<Alimento> tableView;
    @FXML private Button bottoneCerca;

    @FXML private TableColumn<Alimento, ImageView> immagineCol;
    @FXML private TableColumn<Alimento, String> nomeCol, brandCol;
    @FXML private TableColumn<Alimento, Double> calorieCol, proteineCol, carboidratiCol, grassiCol;
    @FXML private TableColumn<Alimento, Double> grassiSatCol, saleCol, fibreCol, zuccheriCol;

    private int offset = 0;
    private final int LIMIT = 50;
    private boolean isLoading = false;

    // Aggiungo la variabile per la dieta in Alimenti, necessaria per la gestione dell'alert
    private Dieta dietaAssegnata;

    private void setNomeUtenteLabel() {
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID direttamente dalla Sessione

        if (userIdFromSession != null) {
            String nomeUtente = getNomeUtenteDalDatabase(userIdFromSession.toString());
            if (nomeUtente != null && !nomeUtente.isEmpty()) {
                nomeUtenteLabelHomePage.setText(nomeUtente);
            } else {
                nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Testo di fallback
            }
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Testo di fallback se l'ID non è disponibile
            System.err.println("[ERROR - Alimenti] ID utente non disponibile dalla Sessione per impostare il nome.");
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = SQLiteConnessione.connector();
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

    @FXML
    public void initialize() {
        immagineCol.setCellValueFactory(new PropertyValueFactory<>("immagine"));
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        calorieCol.setCellValueFactory(new PropertyValueFactory<>("kcal"));
        proteineCol.setCellValueFactory(new PropertyValueFactory<>("proteine"));
        carboidratiCol.setCellValueFactory(new PropertyValueFactory<>("carboidrati"));
        grassiCol.setCellValueFactory(new PropertyValueFactory<>("grassi"));
        grassiSatCol.setCellValueFactory(new PropertyValueFactory<>("grassiSaturi"));
        saleCol.setCellValueFactory(new PropertyValueFactory<>("sale"));
        fibreCol.setCellValueFactory(new PropertyValueFactory<>("fibre"));
        zuccheriCol.setCellValueFactory(new PropertyValueFactory<>("zuccheri"));

        tableView.setRowFactory(tv -> {
            TableRow<Alimento> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    apriDettaglio(row.getItem());
                }
            });
            return row;
        });

        popolaCategorie();

        categoriaComboBox.setOnAction(e -> {
            resetRicerca();
            cercaAlimenti(cercaAlimento.getText(), false);
        });

        mieiAlimentiCheckBox.setOnAction(e -> {
            resetRicerca();
            cercaAlimenti(cercaAlimento.getText(), false);
        });

        Platform.runLater(() -> {
            ScrollBar scrollBar = getVerticalScrollbar(tableView);
            if (scrollBar != null) {
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == scrollBar.getMax()) {
                        caricaAltri();
                    }
                });
            }
        });

        // Chiama setNomeUtenteLabel qui per impostare il nome utente all'avvio
        setNomeUtenteLabel();
        // Carica gli alimenti iniziali
        cercaAlimenti("", false);
        // Recupera la dieta assegnata all'avvio della pagina
        recuperaEImpostaDietaAssegnata(); // Aggiungi questa chiamata
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
        if (isLoading) return;
        isLoading = true;
        cercaAlimenti(cercaAlimento.getText(), true);
        isLoading = false;
    }

    @FXML
    public void resetRicerca() {
        offset = 0;
        tableView.getItems().clear();
    }

    @FXML
    public String getFiltro() {
        return cercaAlimento != null ? cercaAlimento.getText() : "";
    }

    @FXML
    private void handleCercaAlimento(ActionEvent event) {
        resetRicerca();
        cercaAlimenti(cercaAlimento.getText(), false);
    }

    public void cercaAlimenti(String filtro, boolean append) {
        ObservableList<Alimento> alimenti = append ? tableView.getItems() : FXCollections.observableArrayList();

        String categoria = categoriaComboBox.getSelectionModel().getSelectedItem();
        boolean soloMiei = mieiAlimentiCheckBox.isSelected();

        StringBuilder query = new StringBuilder("SELECT * FROM foods WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?");
        }
        Integer currentUserId = Session.getUserId();
        if (soloMiei && currentUserId != null) {
            query.append(" AND user_id = ?");
        }
        query.append(" LIMIT ? OFFSET ?");

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%");
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria);
            }
            if (soloMiei && currentUserId != null) {
                stmt.setInt(paramIndex++, currentUserId);
            }
            stmt.setInt(paramIndex++, LIMIT);
            stmt.setInt(paramIndex++, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alimenti.add(new Alimento(
                            rs.getString("nome"),
                            rs.getString("brand"),
                            rs.getDouble("kcal"),
                            rs.getDouble("proteine"),
                            rs.getDouble("carboidrati"),
                            rs.getDouble("grassi"),
                            rs.getDouble("grassiSaturi"),
                            rs.getDouble("sale"),
                            rs.getDouble("fibre"),
                            rs.getDouble("zuccheri"),
                            rs.getString("immaginePiccola"),
                            rs.getString("immagineGrande"),
                            rs.getInt("user_id"),
                            rs.getInt("id")
                    ));
                }
                tableView.setItems(alimenti);
                offset += LIMIT;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare gli alimenti", "Dettagli: " + e.getMessage());
        }
    }

    private void apriDettaglio(Alimento alimento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettagliAlimento.fxml"));
            Parent root = loader.load();

            DettagliAlimentoController controller = loader.getController();
            controller.setAlimento(alimento);
            controller.setAlimentiController(this);
            controller.setOrigineFXML("Alimenti.fxml");

            Stage stage = new Stage();
            stage.setTitle("Dettaglio Alimento");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/DettaglioAlimento-Style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire il dettaglio alimento", "Verificare il file FXML.");
        }
    }

    @FXML
    private void handleApriAggiunta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiAlimento.fxml"));
            Parent root = loader.load();

            AggiungiAlimentoController controller = loader.getController();
            controller.setAlimentiController(this);

            Stage stage = new Stage();
            stage.setTitle("Aggiungi Alimento");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire la schermata di aggiunta alimento", "Verificare il file FXML.");
        }
    }

    private void popolaCategorie() {
        String query = "SELECT DISTINCT categoria FROM foods WHERE categoria IS NOT NULL";
        try (Connection conn = SQLiteConnessione.connector();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ObservableList<String> categorie = FXCollections.observableArrayList();
            categorie.add("Tutte");
            while (rs.next()) {
                categorie.add(rs.getString("categoria"));
            }
            categoriaComboBox.setItems(categorie);
            categoriaComboBox.getSelectionModel().selectFirst();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile popolare le categorie", "Dettagli: " + e.getMessage());
        }
    }

    // Metodo per visualizzare gli alert
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Metodo per recuperare la dieta assegnata al cliente (copiato da PaginaProfilo/Ricette)
    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String url = "jdbc:sqlite:database.db"; // Assicurati che sia il percorso corretto
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
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (Alimenti): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta;
    }

    // Metodo per recuperare e impostare la dieta all'inizializzazione (copiato da PaginaProfilo/Ricette)
    private void recuperaEImpostaDietaAssegnata() {
        Integer userIdFromSession = Session.getUserId();
        if (userIdFromSession != null) {
            this.dietaAssegnata = recuperaDietaAssegnataACliente(userIdFromSession.intValue());
            if (this.dietaAssegnata != null) {
                System.out.println("DEBUG (Alimenti): Dieta '" + dietaAssegnata.getNome() + "' (ID: " + dietaAssegnata.getId() + ") recuperata per utente ID: " + userIdFromSession);
            } else {
                System.out.println("DEBUG (Alimenti): Nessuna dieta trovata per l'utente ID: " + userIdFromSession);
            }
        } else {
            System.err.println("[ERROR - Alimenti] ID utente non disponibile dalla Sessione per recupero dieta.");
        }
    }


    // --- Metodi di Navigazione (aggiornati per non passare l'ID esplicitamente) ---

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent root = fxmlLoader.load();
            // Alimenti si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la pagina Alimenti", "Verificare il percorso FXML.");
        }
    }

    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Ricette.fxml"));
            Parent root = loader.load();
            // Ricette si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la pagina Ricette", "Verificare il percorso FXML.");
        }
    }

    @FXML
    private void AccessoHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePage.fxml"));
            Parent root = loader.load();
            // HomePage si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la HomePage", "Verificare il percorso FXML.");
        }
    }

    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent root = fxmlLoader.load();
            // PaginaProfilo si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la Pagina Profilo", "Verificare il percorso FXML.");
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
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.", "Verificare il percorso del file FXML.");
            } catch (Exception e) {
                System.err.println("ERRORE (HomePage): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.", "Dettagli: " + e.getMessage());
            }
        } else {
            System.out.println("DEBUG (HomePage): Nessuna dieta trovata per il cliente  (ID: " + Session.getUserId() + ").");
            showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta", "Nessuna dieta assegnata",
                    "Il cliente non ha diete assegnate o non è stato possibile recuperarle.");
        }
    }
}