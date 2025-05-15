package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.IngredienteRicetta;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.*;



public class AggiungiRicetteController {

    @FXML private Button aggiungiAlimento;
    @FXML private Button annulla;
    @FXML private TextField cercaAlimento;
    @FXML private TextField nomeRicetta;
    @FXML private TextField quantitaField;
    @FXML private Button salvaRicetta;
    @FXML private ComboBox<String> categoriaComboBox;
    @FXML private CheckBox mieiAlimentiCheckBox;
    @FXML private ComboBox<String> categoriaRicetta;
    @FXML private TextArea descrizioneRicetta;

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
    @FXML private TableColumn<IngredienteRicetta, String> ingredienteNomeCol;
    @FXML private TableColumn<IngredienteRicetta, Void> azioniCol;
    @FXML private TableColumn<IngredienteRicetta, Number> quantitaCol;
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

        // Configura tableViewIngredienti
        ingredienteNomeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlimento().getNome()));
        quantitaCol.setCellValueFactory(data -> data.getValue().quantitaProperty());
        ingredientiTable.setItems(ingredienti);

        // Colonna per azioni (modifica/elimina)
        azioniCol.setCellFactory(getAzioneCellFactory());




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
        ObservableList<String> categoriePrefissate = FXCollections.observableArrayList(
                "Colazione", "Spuntino", "Pranzo", "Merenda", "Cena"
        );
        categoriaRicetta.setItems(categoriePrefissate);

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


    private ObservableList<IngredienteRicetta> ingredienti = FXCollections.observableArrayList();
    private double totKcal = 0, totProteine = 0, totCarboidrati = 0, totGrassi = 0,
            totGrassiSaturi = 0, totSale = 0, totFibre = 0, totZuccheri = 0;
    @FXML
    private void handleAggiungiAlimento(ActionEvent event) {
        Alimento alimentoSelezionato = tableView.getSelectionModel().getSelectedItem();

        if (alimentoSelezionato != null && !quantitaField.getText().isEmpty()) {
            try {
                double quantita = Double.parseDouble(quantitaField.getText());
                IngredienteRicetta ingrediente = new IngredienteRicetta(alimentoSelezionato, quantita);
                ingredienti.add(ingrediente);

                // Aggiungi i valori nutrizionali
                aggiornaValoriNutrizionali(alimentoSelezionato,quantita);
            } catch (NumberFormatException e) {
                // Gestire l'errore se la quantità non è valida
                showAlert("Errore", "Inserisci una quantità valida.");
            }
        } else {
            showAlert("Errore", "Seleziona un alimento e inserisci una quantità.");
        }
    }
    private void aggiornaValoriNutrizionali(Alimento alimento, double quantita) {
        double fattore = quantita / 100.0;

        totKcal += alimento.getKcal() * fattore;
        totProteine += alimento.getProteine() * fattore;
        totCarboidrati += alimento.getCarboidrati() * fattore;
        totGrassi += alimento.getGrassi() * fattore;
        totGrassiSaturi += alimento.getGrassiSaturi() * fattore;
        totSale += alimento.getSale() * fattore;
        totFibre += alimento.getFibre() * fattore;
        totZuccheri += alimento.getZuccheri() * fattore;

        aggiornaLabel();
    }
    private void aggiornaLabel() {
        calorieTotaliLabel.setText(String.format("%.2f", totKcal));
        proteineTotaliLabel.setText(String.format("%.2f", totProteine));
        carboidratiTotaliLabel.setText(String.format("%.2f", totCarboidrati));
        grassiTotaliLabel.setText(String.format("%.2f", totGrassi));
        grassiSaturiTotaliLabel.setText(String.format("%.2f", totGrassiSaturi));
        saleTotaliLabel.setText(String.format("%.2f", totSale));
        fibreTotaliLabel.setText(String.format("%.2f", totFibre));
        zuccheriTotaliLabel.setText(String.format("%.2f", totZuccheri));
    }
    private void sottraiValoriNutrizionali(Alimento alimento, double quantita) {
        double fattore = quantita / 100.0;

        totKcal -= alimento.getKcal() * fattore;
        totProteine -= alimento.getProteine() * fattore;
        totCarboidrati -= alimento.getCarboidrati() * fattore;
        totGrassi -= alimento.getGrassi() * fattore;
        totGrassiSaturi -= alimento.getGrassiSaturi() * fattore;
        totSale -= alimento.getSale() * fattore;
        totFibre -= alimento.getFibre() * fattore;
        totZuccheri -= alimento.getZuccheri() * fattore;

        aggiornaLabel();
    }
    private Callback<TableColumn<IngredienteRicetta, Void>, TableCell<IngredienteRicetta, Void>> getAzioneCellFactory() {
        return param -> new TableCell<>() {
            private final Button btnModifica = new Button("mod");
            private final Button btnElimina = new Button("-");
            private final HBox pane = new HBox(5, btnModifica, btnElimina);

            {
                btnModifica.setOnAction(e -> {
                    IngredienteRicetta ing = getTableView().getItems().get(getIndex());
                    TextInputDialog dialog = new TextInputDialog(String.valueOf(ing.getQuantita()));
                    dialog.setHeaderText("Modifica Quantità");
                    dialog.setContentText("Inserisci nuova quantità:");
                    dialog.showAndWait().ifPresent(nuova -> {
                        try {
                            double nuovaQuantita = Double.parseDouble(nuova);
                            sottraiValoriNutrizionali(ing.getAlimento(), ing.getQuantita());
                            ing.setQuantita(nuovaQuantita);
                            aggiornaValoriNutrizionali(ing.getAlimento(), nuovaQuantita);
                        } catch (NumberFormatException ignored) {}
                    });
                });

                btnElimina.setOnAction(e -> {
                    IngredienteRicetta ing = getTableView().getItems().get(getIndex());
                    ingredienti.remove(ing);
                    sottraiValoriNutrizionali(ing.getAlimento(), ing.getQuantita());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
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
        ((Stage) nomeRicetta.getScene().getWindow()).close();
    }


    @FXML
    private void handleSalvaRicetta(ActionEvent event) {
        // Validazione dei campi obbligatori
        if (isEmpty(nomeRicetta) || isEmpty(descrizioneRicetta) || categoriaRicetta.getValue() == null || ingredienti.isEmpty()) {
            mostraErrore("Tutti i campi devono essere compilati e devi aggiungere almeno un ingrediente.");
            return;
        }

        try {
            // Recupera dati dalla UI
            String nome = nomeRicetta.getText();
            String descrizione = descrizioneRicetta.getText();
            String categoria = categoriaRicetta.getValue();
            Integer userId = Session.getUserId(); // recupera l'utente loggato

            // Connessione DB
            Connection conn = SQLiteConnessione.connector();

            // 1. Inserimento nella tabella Ricette
            String insertRicettaSQL = "INSERT INTO Ricette (nome, descrizione, id_utente, categoria, kcal, proteine, carboidrati, grassi, grassi_saturi, sale, fibre, zuccheri) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertRicettaStmt = conn.prepareStatement(insertRicettaSQL, Statement.RETURN_GENERATED_KEYS);
            insertRicettaStmt.setString(1, nome);
            insertRicettaStmt.setString(2, descrizione);
            insertRicettaStmt.setInt(3, userId);
            insertRicettaStmt.setString(4, categoria);
            insertRicettaStmt.setDouble(5, totKcal);
            insertRicettaStmt.setDouble(6, totProteine);
            insertRicettaStmt.setDouble(7, totCarboidrati);
            insertRicettaStmt.setDouble(8, totGrassi);
            insertRicettaStmt.setDouble(9, totGrassiSaturi);
            insertRicettaStmt.setDouble(10, totSale);
            insertRicettaStmt.setDouble(11, totFibre);
            insertRicettaStmt.setDouble(12, totZuccheri);
            insertRicettaStmt.executeUpdate();

            // Recupera ID generato
            ResultSet generatedKeys = insertRicettaStmt.getGeneratedKeys();
            int idRicetta = -1;
            if (generatedKeys.next()) {
                idRicetta = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Errore nel recupero dell'ID della ricetta appena inserita.");
            }

            // 2. Inserisci gli ingredienti nella tabella ingredienti_ricette
            String insertIngredienteSQL = "INSERT INTO Ingredienti_ricette (id_ricetta, id_alimento, quantita_grammi) VALUES (?, ?, ?)";
            PreparedStatement insertIngredienteStmt = conn.prepareStatement(insertIngredienteSQL);

            for (IngredienteRicetta ingrediente : ingredienti) {
                insertIngredienteStmt.setInt(1, idRicetta);
                insertIngredienteStmt.setInt(2, ingrediente.getAlimento().getId());
                insertIngredienteStmt.setDouble(3, ingrediente.getQuantita());
                insertIngredienteStmt.addBatch();
            }

            insertIngredienteStmt.executeBatch();

            mostraInfo("Ricetta salvata con successo!");

            // Chiudi finestra
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            mostraErrore("Errore durante il salvataggio: " + e.getMessage());
        }
    }

    private boolean isEmpty(TextInputControl campo) {
        return campo.getText() == null || campo.getText().trim().isEmpty();
    }
    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    private void mostraInfo(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }




}
