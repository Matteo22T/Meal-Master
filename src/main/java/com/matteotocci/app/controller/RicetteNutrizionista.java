package com.matteotocci.app.controller;

import com.matteotocci.app.model.Ricetta;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session; // Importa la classe Session

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional; // Necessario per showAlert

public class RicetteNutrizionista {

    @FXML private Button BottoneAlimenti;
    @FXML private Button BottoneHome;
    @FXML private Button BottoneRicette;
    @FXML private Label nomeUtenteLabelHomePage;
    @FXML private ComboBox<String> categorieRicette;
    @FXML private CheckBox mieiAlimentiCheckBox; // Potrebbe essere rinominato "mieRicetteCheckBox"
    @FXML private TextField cercaRicetta;
    @FXML private TableView<Ricetta> tableViewRicette;
    @FXML private TableColumn<Ricetta, String> nomeCol;
    @FXML private TableColumn<Ricetta, String> descrizioneCol;
    @FXML private TableColumn<Ricetta, String> categoriaCol;

    private int offset = 0;
    private final int LIMIT = 50;
    private boolean isLoading = false;

    private void setNomeNutrizionistaLabel() {
        Integer nutrizionistaIdFromSession = Session.getUserId(); // Assumiamo esista Session.getNutrizionistaId()
        if (nutrizionistaIdFromSession != null) {
            String nomeNutrizionista = getNomeCognomeNutrizionistaDalDatabase(nutrizionistaIdFromSession.toString());
            if (nomeNutrizionista != null && !nomeNutrizionista.isEmpty()) {
                nomeUtenteLabelHomePage.setText(nomeNutrizionista);
            } else {
                nomeUtenteLabelHomePage.setText("Nome e Cognome Nutrizionista"); // Testo di fallback
            }
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome Nutrizionista"); // Testo di fallback se l'ID non è disponibile
            System.err.println("[ERROR - RicetteNutrizionista] ID nutrizionista non disponibile dalla Sessione per impostare il nome.");
        }
    }

    private String getNomeCognomeNutrizionistaDalDatabase(String nutrizionistaId) {
        String nomeNutrizionista = null;
        // Assumiamo che i nutrizionisti siano nella tabella 'Utente' con un ruolo specifico 'nutrizionista'
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ? AND ruolo = 'nutrizionista'";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nutrizionistaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nomeNutrizionista = rs.getString("Nome") + " " + rs.getString("Cognome");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome nutrizionista dal database: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare il nome del nutrizionista.");
        }
        return nomeNutrizionista;
    }

    @FXML
    public void initialize() {
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        descrizioneCol.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        categoriaCol.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        System.out.println("[DEBUG - RicetteNutrizionista] ID nutrizionista da Sessione: " + Session.getUserId());
        setNomeNutrizionistaLabel(); // Imposta il nome del nutrizionista all'inizializzazione

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
        // Usa direttamente Session.getNutrizionistaId() per filtrare le ricette create dal nutrizionista loggato
        Integer currentNutrizionistaId = Session.getUserId();
        if (soloMiei && currentNutrizionistaId != null && currentNutrizionistaId != 0) {
            query.append(" AND id_utente = ?"); // Assumiamo che 'id_utente' sia la colonna che indica il creatore della ricetta
        }
        query.append(" LIMIT ? OFFSET ?");

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%");
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria);
            }
            if (soloMiei && currentNutrizionistaId != null && currentNutrizionistaId != 0) {
                stmt.setInt(paramIndex++, currentNutrizionistaId);
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
                            rs.getInt("id_utente"), // Assicurati che 'id_utente' esista nella tua tabella Ricette e contenga l'ID del creatore (nutrizionista o utente)
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
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare le ricette");
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
    private void AccessoHome(ActionEvent event) {
        try {
            // Reindirizza al controller HomePageNutrizionista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la HomePage per Nutrizionisti");
        }
    }

    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            // Reindirizza al controller ProfiloNutrizionista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ProfiloNutrizionista.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la Pagina Profilo per Nutrizionisti");
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
    private void handleApriAggiuntaRicetta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiRicetta.fxml"));
            Parent root = loader.load();

            // Suggerimento: potresti voler passare l'ID del nutrizionista al controller AggiungiRicetta
            // per associare la ricetta al creatore.
            // AggiungiRicettaController controller = loader.getController();
            // controller.setCreatoreId(Session.getNutrizionistaId()); // Esempio di metodo nel controller AggiungiRicetta

            Stage stage = new Stage();
            stage.setTitle("Aggiungi Ricetta");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire la schermata di aggiunta ricetta");
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