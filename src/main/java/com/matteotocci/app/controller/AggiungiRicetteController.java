package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.IngredienteRicetta;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class AggiungiRicetteController {

    @FXML private Button aggiungiAlimento;
    @FXML private Button annulla;
    @FXML private TextField cercaAlimento;
    @FXML private TextField nomeRicetta;
    @FXML private TextField quantitaField;
    @FXML private Button salvaRicetta;
    @FXML private ComboBox<String> categoriaComboBox;
    @FXML private CheckBox mieiAlimentiCheckBox;

    @FXML private TableView<Alimento> tableView;
    @FXML private TableColumn<Alimento, ImageView> immagineCol;
    @FXML private TableColumn<Alimento, String> nomeCol, brandCol;
    @FXML private TableColumn<Alimento, Double> calorieCol, proteineCol, carboidratiCol, grassiCol;
    @FXML private TableColumn<Alimento, Double> grassiSatCol, saleCol, fibreCol, zuccheriCol;


    @FXML private Label calorieTotaliLabel;
    @FXML private Label carboidratiTotaliLabel;
    @FXML private Label fibreTotaliLabel;
    @FXML private Label grassiSaturiTotaliLabel;
    @FXML private Label grassiTotaliLabel;


    @FXML private TableView<IngredienteRicetta> ingredientiTable;
    @FXML private TableColumn<IngredienteRicetta, ?> ingredienteNomeCol;
    @FXML private TableColumn<IngredienteRicetta, ?> modificaCol;
    @FXML private TableColumn<IngredienteRicetta, ?> eliminaCol;
    @FXML private TableColumn<IngredienteRicetta, ?> quantitaCol;
    @FXML private Label proteineTotaliLabel;
    @FXML private Label saleTotaliLabel;
    @FXML private Label zuccheriTotaliLabel;

    private int offset = 0;
    private final int LIMIT = 50;

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
                    Alimento alimento = row.getItem();
                    apriDettaglio(alimento);
                }
            });
            return row;
        });

        popolaCategorie();

        categoriaComboBox.setOnAction(e -> {
            offset = 0;
            tableView.getItems().clear();
            cercaAlimenti(cercaAlimento.getText(), false);
        });

        mieiAlimentiCheckBox.setOnAction(e -> {
            offset = 0;
            tableView.getItems().clear();
            cercaAlimenti(cercaAlimento.getText(), false);
        });




        Platform.runLater(() -> {
            ScrollBar scrollBar = getVerticalScrollbar(tableView);
            if (scrollBar != null) {
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == scrollBar.getMax()) {
                        // Se raggiunge il fondo, carica altri 50
                        caricaAltri();
                    }
                });
            }
        });

        cercaAlimenti("", false);
    }


    @FXML
    private void handleCercaAlimento(ActionEvent event) {
        offset = 0;
        tableView.getItems().clear();
        String filtro = cercaAlimento.getText();
        cercaAlimenti(filtro, false);
    }
    private void cercaAlimenti(String filtro, boolean append) {
        ObservableList<Alimento> alimenti = append ? tableView.getItems() : FXCollections.observableArrayList();

        String categoria = categoriaComboBox.getSelectionModel().getSelectedItem();
        boolean soloMiei = mieiAlimentiCheckBox.isSelected();

        StringBuilder query = new StringBuilder("SELECT * FROM foods WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?");
        }
        if (soloMiei && Session.getUserId() != null) {
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
            if (soloMiei && Session.getUserId() != null) {
                stmt.setInt(paramIndex++, Session.getUserId());
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
    private ScrollBar getVerticalScrollbar(TableView<?> table) {
        for (Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) {
                ScrollBar sb = (ScrollBar) node;
                if (sb.getOrientation() == Orientation.VERTICAL) {
                    return sb;
                }
            }
        }
        return null;
    }
    private boolean isLoading = false;
    private void caricaAltri() {
        if (isLoading) return;
        isLoading = true;
        String filtro = cercaAlimento.getText();
        cercaAlimenti(filtro, true);
        isLoading = false;
    }
    private void popolaCategorie() {
        String query = "SELECT DISTINCT categoria FROM foods WHERE categoria IS NOT NULL";
        try (Connection conn = SQLiteConnessione.connector();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ObservableList<String> categorie = FXCollections.observableArrayList();
            categorie.add("Tutte"); // opzione iniziale
            while (rs.next()) {
                categorie.add(rs.getString("categoria"));
            }
            categoriaComboBox.setItems(categorie);
            categoriaComboBox.getSelectionModel().selectFirst();
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


            Stage stage = new Stage();
            stage.setTitle("Dettaglio Alimento");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<IngredienteRicetta> ingredientiRicettaList = new ArrayList<>();
    @FXML
    private void handleAggiungiAlimento(ActionEvent event) {
        Alimento alimentoSelezionato = tableView.getSelectionModel().getSelectedItem();

        if (alimentoSelezionato != null && !quantitaField.getText().isEmpty()) {
            try {
                double quantita = Double.parseDouble(quantitaField.getText());
                IngredienteRicetta ingrediente = new IngredienteRicetta(alimentoSelezionato, quantita);
                ingredientiRicettaList.add(ingrediente);

                // Aggiungi i valori nutrizionali
                aggiornaValoriNutrizionali();
            } catch (NumberFormatException e) {
                // Gestire l'errore se la quantità non è valida
                showAlert("Errore", "Inserisci una quantità valida.");
            }
        } else {
            showAlert("Errore", "Seleziona un alimento e inserisci una quantità.");
        }
    }
    private void aggiornaValoriNutrizionali() {
        double calorieTotali = 0, carboidratiTotali = 0, grassiTotali = 0;
        double proteineTotali = 0, zuccheriTotali = 0, saleTotali = 0, fibreTotali = 0;

        for (IngredienteRicetta ingrediente : ingredientiRicettaList) {
            Alimento alimento = ingrediente.getAlimento();
            double quantita = ingrediente.getQuantita();

            // Calcolare i valori nutrizionali in base alla quantità
            calorieTotali += alimento.getKcal() * quantita / 100;
            carboidratiTotali += alimento.getCarboidrati() * quantita / 100;
            grassiTotali += alimento.getGrassi() * quantita / 100;
            proteineTotali += alimento.getProteine() * quantita / 100;
            zuccheriTotali += alimento.getZuccheri() * quantita / 100;
            saleTotali += alimento.getSale() * quantita / 100;
            fibreTotali += alimento.getFibre() * quantita / 100;
        }

        // Impostare i valori nelle etichette
        calorieTotaliLabel.setText(String.format("%.2f", calorieTotali));
        carboidratiTotaliLabel.setText(String.format("%.2f", carboidratiTotali));
        grassiTotaliLabel.setText(String.format("%.2f", grassiTotali));
        proteineTotaliLabel.setText(String.format("%.2f", proteineTotali));
        zuccheriTotaliLabel.setText(String.format("%.2f", zuccheriTotali));
        saleTotaliLabel.setText(String.format("%.2f", saleTotali));
        fibreTotaliLabel.setText(String.format("%.2f", fibreTotali));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleAnnulla(ActionEvent event) {

    }


    @FXML
    void handleSalvaRicetta(ActionEvent event) {

    }


}
