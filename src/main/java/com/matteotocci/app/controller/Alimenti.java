package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
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

    private Integer loggedInUserId=Session.getUserId();
    private int offset = 0;
    private final int LIMIT = 50;
    private boolean isLoading = false;


    private void setNomeUtenteLabel() {
        // Usa l'ID passato o quello in Session se presente
        if (loggedInUserId == null) {
            nomeUtenteLabelHomePage.setText("Nome e Cognome");
            return;
        }

        String nomeUtente = getNomeUtenteDalDatabase(loggedInUserId);
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            nomeUtenteLabelHomePage.setText(nomeUtente);
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome");
        }
    }

    private String getNomeUtenteDalDatabase(Integer userId) {
        String nomeUtente = null;
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
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
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent root = fxmlLoader.load();


            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Ricette.fxml"));
            Parent root = loader.load();


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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent root = fxmlLoader.load();



            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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

        cercaAlimenti("", false);
        setNomeUtenteLabel();
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
        if (soloMiei && loggedInUserId != null) {
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
            if (soloMiei && loggedInUserId != null) {
                stmt.setInt(paramIndex++, loggedInUserId);
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
        }
    }
}
