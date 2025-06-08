package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.Ricetta;
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

public class AggiungiAlimentoDieta {
    @FXML
    private TableColumn<Alimento, ImageView> immagineCol;
    @FXML
    private TableColumn<Alimento, String> nomeCol, brandCol;
    @FXML
    private TableColumn<Alimento, Double> calorieCol, proteineCol, carboidratiCol, grassiCol;
    @FXML
    private TableColumn<Alimento, Double> grassiSatCol, saleCol, fibreCol, zuccheriCol;
    @FXML
    private Button ButtonCercaAlimento;
    @FXML
    private Button ButtonCercaRicetta;
    @FXML
    private ComboBox<String> ComboBoxAlimento;
    @FXML
    private ComboBox<String> ComboBoxRicetta;
    @FXML
    private CheckBox CheckBoxAlimenti;
    @FXML
    private CheckBox CheckBoxRicette;
    @FXML
    private Button confermaAlimentiButton;
    @FXML
    private Button confermaRicetteButton;
    @FXML
    private TableView<Alimento> tableViewAlimenti;
    @FXML
    private TableView<Ricetta> tableViewRicette;
    @FXML
    private TextField textCercaAlimento;
    @FXML
    private TextField textCercaRicetta;
    @FXML
    private TableColumn<Ricetta, String> nomeColRic;
    @FXML
    private TableColumn<Ricetta, String> descrizioneColRic;
    @FXML
    private TableColumn<Ricetta, String> categoriaColRic;
    @FXML
    private Spinner<Integer> quantitaSpinner; // Aggiunto per la quantità

    private int offset = 0;
    private final int LIMIT = 50;
    private AggiungiGiornoDieta giornoDietaController;
    private String pastoCorrente;

    public void setGiornoDietaController(AggiungiGiornoDieta controller) {
        this.giornoDietaController = controller;
    }

    public void setPastoCorrente(String pasto) {
        this.pastoCorrente = pasto;
    }

    @FXML
    private void mostraTabellaAlimenti(ActionEvent event) {
        tableViewAlimenti.setVisible(true);
        tableViewRicette.setVisible(false);
        confermaAlimentiButton.setVisible(true);
        confermaRicetteButton.setVisible(false);
        textCercaAlimento.setVisible(true);
        textCercaRicetta.setVisible(false);
        ButtonCercaAlimento.setVisible(true);
        ButtonCercaRicetta.setVisible(false);
        ComboBoxAlimento.setVisible(true);
        ComboBoxRicetta.setVisible(false);
        CheckBoxAlimenti.setVisible(true);
        CheckBoxRicette.setVisible(false);
    }

    @FXML
    private void mostraTabellaRicette(ActionEvent event) {
        tableViewAlimenti.setVisible(false);
        tableViewRicette.setVisible(true);
        confermaAlimentiButton.setVisible(false);
        confermaRicetteButton.setVisible(true);
        textCercaAlimento.setVisible(false);
        textCercaRicetta.setVisible(true);
        ButtonCercaAlimento.setVisible(false);
        ButtonCercaRicetta.setVisible(true);
        ComboBoxAlimento.setVisible(false);
        ComboBoxRicetta.setVisible(true);
        CheckBoxAlimenti.setVisible(false);
        CheckBoxRicette.setVisible(true);
        // Quando si passa alla visualizzazione delle ricette, ricaricale
        offset = 0; // Reset offset per la nuova ricerca
        tableViewRicette.getItems().clear(); // Pulisci la tabella
        // Forza la selezione per mostrare le proprie ricette se l'utente è loggato
        if (Session.getUserId() != null) {
            CheckBoxRicette.setSelected(true);
        } else {
            CheckBoxRicette.setSelected(false);
        }
        cercaRicette(textCercaRicetta.getText(), false);
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
        nomeColRic.setCellValueFactory(new PropertyValueFactory<>("nome"));
        descrizioneColRic.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        categoriaColRic.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        tableViewAlimenti.setRowFactory(tv -> {
            TableRow<Alimento> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Alimento alimento = row.getItem();
                    apriDettaglio(alimento);
                }
            });
            return row;
        });

        tableViewRicette.setOnMouseClicked(this::apriDettaglioRicetta);

        popolaCategorie();

        // Inizializza lo Spinner per la quantità
        quantitaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100));
        quantitaSpinner.setEditable(true);


        ComboBoxAlimento.setOnAction(e -> {
            offset = 0;
            tableViewAlimenti.getItems().clear();
            cercaAlimenti(textCercaAlimento.getText(), false);
        });

        CheckBoxAlimenti.setOnAction(e -> {
            offset = 0;
            tableViewAlimenti.getItems().clear();
            cercaAlimenti(textCercaAlimento.getText(), false);
        });

        ComboBoxRicetta.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(textCercaRicetta.getText(), false);
        });

        CheckBoxRicette.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(textCercaRicetta.getText(), false);
        });


        Platform.runLater(() -> {
            ScrollBar scrollBar = getVerticalScrollbar(tableViewAlimenti);
            if (scrollBar != null) {
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == scrollBar.getMax()) {
                        // Se raggiunge il fondo, carica altri 50
                        caricaAltriAlimenti();
                    }
                });
            }
            ScrollBar scrollBar2 = getVerticalScrollbar(tableViewRicette);
            if (scrollBar2 != null) {
                scrollBar2.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == scrollBar2.getMax()) {
                        // Se raggiunge il fondo, carica altri 50
                        caricaAltreRicette();
                    }
                });
            }
        });

        cercaAlimenti("", false);
        // All'inizializzazione, se un utente è loggato, forza la selezione della checkbox "Solo le mie ricette"
        if (Session.getUserId() != null) {
            CheckBoxRicette.setSelected(true);
        }
        cercaRicette("", false); // Carica anche le ricette all'avvio, ora con il filtro impostato
    }

    @FXML
    private void handleCercaAlimento(ActionEvent event) {
        offset = 0;
        tableViewAlimenti.getItems().clear();
        String filtro = textCercaAlimento.getText();
        cercaAlimenti(filtro, false);
    }

    private void cercaAlimenti(String filtro, boolean append) {
        ObservableList<Alimento> alimenti = append ? tableViewAlimenti.getItems() : FXCollections.observableArrayList();

        String categoria = ComboBoxAlimento.getSelectionModel().getSelectedItem();
        boolean soloMiei = CheckBoxAlimenti.isSelected();

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

                tableViewAlimenti.setItems(alimenti);
                offset += LIMIT;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isLoading = false;

    private void caricaAltriAlimenti() {
        if (isLoading) return;
        isLoading = true;
        String filtro = textCercaAlimento.getText();
        cercaAlimenti(filtro, true);
        isLoading = false;
    }

    private boolean isLoading2 = false;

    private void caricaAltreRicette() {
        if (isLoading2) return;
        isLoading2 = true;
        String filtro = textCercaRicetta.getText();
        cercaRicette(filtro, true);
        isLoading2 = false;
    }

    @FXML
    private void handleCercaRicetta(ActionEvent event) {
        offset = 0;
        tableViewRicette.getItems().clear();
        cercaRicette(textCercaRicetta.getText(), false);
    }


    public void cercaRicette(String filtro, boolean append) {
        ObservableList<Ricetta> ricette = append ? tableViewRicette.getItems() : FXCollections.observableArrayList();

        String categoria = ComboBoxRicetta.getSelectionModel().getSelectedItem();
        boolean soloMiei = CheckBoxRicette.isSelected();
        StringBuilder query = new StringBuilder("SELECT * FROM Ricette WHERE LOWER(nome) LIKE ?");
        // Aggiungi una condizione per la categoria solo se non è "Tutte"
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?");
        }
        // Aggiungi una condizione per l'id_utente solo se la checkbox "Solo le mie ricette" è selezionata
        if (soloMiei && Session.getUserId() != null) {
            query.append(" AND id_utente = ?");
        }
        query.append(" LIMIT ? OFFSET ?");
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%");
            // Applica il parametro per la categoria solo se la condizione è stata aggiunta
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria);
            }
            // Applica il parametro per l'id_utente solo se la condizione è stata aggiunta
            if (soloMiei && Session.getUserId() != null) {
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

    @FXML
    private void apriDettaglioRicetta(MouseEvent event) {
        if (event.getClickCount() == 2 && !tableViewRicette.getSelectionModel().isEmpty()) {
            Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettaglioRicetta.fxml"));
                Parent root = loader.load();

                DettaglioRicettaController controller = loader.getController();
                controller.setRicetta(ricettaSelezionata);
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

    private void popolaCategorie() {
        String queryAlimenti = "SELECT DISTINCT categoria FROM foods WHERE categoria IS NOT NULL";
        try (Connection conn = SQLiteConnessione.connector();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryAlimenti)) {

            ObservableList<String> categorieAlimenti = FXCollections.observableArrayList();
            categorieAlimenti.add("Tutte"); // opzione iniziale
            while (rs.next()) {
                categorieAlimenti.add(rs.getString("categoria"));
            }
            ComboBoxAlimento.setItems(categorieAlimenti);
            ComboBoxAlimento.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Popola ComboBoxRicetta con categorie dalla tabella Ricette
        ObservableList<String> categorieRicetteList = FXCollections.observableArrayList();
        categorieRicetteList.add("Tutte"); // Aggiungi "Tutte" come prima opzione

        String queryRicette = "SELECT DISTINCT categoria FROM Ricette WHERE categoria IS NOT NULL";
        try (Connection conn = SQLiteConnessione.connector();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryRicette)) {
            while (rs.next()) {
                categorieRicetteList.add(rs.getString("categoria"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ComboBoxRicetta.setItems(categorieRicetteList);
        ComboBoxRicetta.getSelectionModel().selectFirst(); // Seleziona "Tutte" di default
    }

    @FXML
    private void confermaAlimenti(ActionEvent event) {
        Alimento alimentoSelezionato = tableViewAlimenti.getSelectionModel().getSelectedItem();
        Integer quantita = quantitaSpinner.getValue(); // Recupera la quantità dallo Spinner

        if (alimentoSelezionato != null && giornoDietaController != null && pastoCorrente != null && quantita != null && quantita > 0) {
            giornoDietaController.aggiungiAlimentoAllaLista(pastoCorrente, alimentoSelezionato, quantita);
            // La finestra rimane aperta ora (come da richiesta precedente)
        } else {
            System.out.println("Seleziona un alimento e specifica una quantità valida!"); // Gestione degli errori
        }
    }

    @FXML
    private void confermaRicette(ActionEvent event) {
        Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem();
        // Recupera la quantità dallo Spinner
        Integer quantita = quantitaSpinner.getValue();

        if (ricettaSelezionata != null && giornoDietaController != null && pastoCorrente != null && quantita != null && quantita > 0) {
            // Chiamiamo un metodo specifico nel controller AggiungiGiornoDieta
            // per aggiungere la ricetta solo alla UI e alla memoria (non al DB in questo momento)
            giornoDietaController.aggiungiRicettaAllaLista(ricettaSelezionata, quantita, pastoCorrente);

            // Chiudiamo la finestra dopo la conferma
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } else {
            System.out.println("Seleziona una ricetta e specifica una quantità valida!");
        }
    }

    private Alimento getAlimentoFromRicetta(Ricetta ricetta) {
        // Questo metodo non è più rilevante per la logica di aggiunta ricetta
        // ma lo lascio se è usato altrove.
        String query = "SELECT * FROM foods WHERE nome = ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ricetta.getNome());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Alimento(
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
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
