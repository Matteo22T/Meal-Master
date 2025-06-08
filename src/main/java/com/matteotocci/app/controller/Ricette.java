package com.matteotocci.app.controller;

import com.matteotocci.app.model.Ricetta;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import com.matteotocci.app.model.Dieta; // Importa la classe Dieta

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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional; // Necessario per showAlert

public class Ricette {

    @FXML private Button BottoneAlimenti;
    @FXML private Button BottoneHome;
    @FXML private Button BottoneRicette;
    @FXML private Button BottonePiano; // Aggiungi il riferimento al bottone del piano alimentare nel FXML
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

    // Dichiarazione della variabile per la dieta assegnata
    private Dieta dietaAssegnata;

    // Rimosso il campo loggedInUserId in quanto useremo sempre Session.getUserId()

    private void setNomeUtenteLabel() {
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID direttamente dalla Session
        if (userIdFromSession != null) {
            String nomeUtente = getNomeUtenteDalDatabase(userIdFromSession.toString());
            if (nomeUtente != null && !nomeUtente.isEmpty()) {
                nomeUtenteLabelHomePage.setText(nomeUtente);
            } else {
                nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Testo di fallback
            }
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Testo di fallback se l'ID non è disponibile
            System.err.println("[ERROR - Ricette] ID utente non disponibile dalla Sessione per impostare il nome.");
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

    @FXML
    public void initialize() {
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        descrizioneCol.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        categoriaCol.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        // L'ID utente viene recuperato e usato direttamente qui
        System.out.println("[DEBUG - Ricette] ID utente da Sessione: " + Session.getUserId());
        setNomeUtenteLabel(); // Imposta il nome utente all'inizializzazione

        cercaRicette("", false); // Carica le ricette iniziali
        popolaCategorie(); // Popola le categorie

        // Listener per i filtri di ricerca
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

        // Listener per lo scroll della tabella
        ScrollBar scrollBar = getVerticalScrollbar(tableViewRicette);
        if (scrollBar != null) {
            scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == scrollBar.getMax()) {
                    caricaAltri();
                }
            });
        }

        // Recupera la dieta assegnata all'avvio della pagina
        recuperaEImpostaDietaAssegnata();
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
        // Usa direttamente Session.getUserId()
        Integer currentUserId = Session.getUserId();
        if (soloMiei && currentUserId != null && currentUserId != 0) { // Assicurati che l'ID non sia 0 se 0 significa non valido
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
            if (soloMiei && currentUserId != null && currentUserId != 0) {
                stmt.setInt(paramIndex++, currentUserId);
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
                            rs.getInt("id_utente"), // Assicurati che 'id_utente' esista nella tua tabella Ricette
                            rs.getDouble("kcal"),
                            rs.getDouble("proteine"),
                            rs.getDouble("carboidrati"),
                            rs.getDouble("grassi"),
                            rs.getDouble("grassi_saturi"),
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

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent root = loader.load();
            // Non passiamo più l'ID via setLoggedInUserId, la classe Alimenti lo prenderà dalla Session
            // Alimenti controller = loader.getController();
            // controller.setLoggedInUserId(Session.getUserId().toString());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePage.fxml"));
            Parent root = loader.load();
            // Non passiamo più l'ID via setLoggedInUserId, la classe HomePage lo prenderà dalla Session
            // HomePage homePageController = loader.getController();
            // homePageController.setLoggedInUserId(Session.getUserId().toString());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent root = loader.load();
            // Non passiamo più l'ID via setUtenteCorrenteId, la classe PaginaProfilo lo prenderà dalla Session
            // PaginaProfilo controller = loader.getController();
            // controller.setUtenteCorrenteId(Session.getUserId().toString());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApriAggiuntaRicetta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiRicetta.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Aggiungi Ricetta");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo per recuperare la dieta assegnata
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
            System.err.println("ERRORE SQL (Ricette): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta;
    }

    // Metodo per recuperare e impostare la dieta all'inizializzazione
    private void recuperaEImpostaDietaAssegnata() {
        Integer userIdFromSession = Session.getUserId();
        if (userIdFromSession != null) {
            this.dietaAssegnata = recuperaDietaAssegnataACliente(userIdFromSession.intValue());
            if (this.dietaAssegnata != null) {
                System.out.println("DEBUG (Ricette): Dieta '" + dietaAssegnata.getNome() + "' (ID: " + dietaAssegnata.getId() + ") recuperata per utente ID: " + userIdFromSession);
            } else {
                System.out.println("DEBUG (Ricette): Nessuna dieta trovata per l'utente ID: " + userIdFromSession);
            }
        } else {
            System.err.println("[ERROR - Ricette] ID utente non disponibile dalla Sessione per recupero dieta.");
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

    @FXML
    private void AccessoPianoAlimentare(ActionEvent event) {
        if (dietaAssegnata != null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                Parent visualizzaDietaRoot = fxmlLoader.load();

                VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();

                visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                System.out.println("DEBUG (Ricette): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                Stage dietaStage = new Stage();
                dietaStage.setScene(new Scene(visualizzaDietaRoot));
                dietaStage.show();

            } catch (IOException e) {
                System.err.println("ERRORE (Ricette): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.", "Verificare il percorso del file FXML.");
            } catch (Exception e) {
                System.err.println("ERRORE (Ricette): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.", "Dettagli: " + e.getMessage());
            }
        } else {
            System.out.println("DEBUG (Ricette): Nessuna dieta trovata per il cliente (ID: " + Session.getUserId() + ").");
            showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta", "Nessuna dieta assegnata",
                    "Il cliente non ha diete assegnate o non è stato possibile recuperarle.");
        }
    }
}

