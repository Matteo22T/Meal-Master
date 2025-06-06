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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class AggiungiPasto {

    @FXML
    private Button ButtonCercaAlimento;

    @FXML
    private Button ButtonCercaRicetta;

    @FXML
    private CheckBox CheckBoxAlimenti;

    @FXML
    private CheckBox CheckBoxRicette;

    @FXML
    private ComboBox<String> ComboBoxAlimento;

    @FXML
    private ComboBox<String> ComboBoxRicetta;

    @FXML
    private TableColumn<?, ?> brandCol;

    @FXML
    private TableColumn<?, ?> calorieCol;

    @FXML
    private TableColumn<?, ?> carboidratiCol;

    @FXML
    private TableColumn<?, ?> categoriaColRic;

    @FXML
    private Button confermaPastoButton;

    @FXML
    private Button confermaPasto2Button;

    @FXML
    private VBox contenitoreAlimentiDieta;

    @FXML
    private VBox contenitoreRicetteDieta;

    @FXML
    private TableColumn<?, ?> descrizioneColRic;

    @FXML
    private TableColumn<?, ?> fibreCol;

    @FXML
    private TableColumn<?, ?> grassiCol;

    @FXML
    private TableColumn<?, ?> grassiSatCol;

    @FXML
    private TableColumn<?, ?> immagineCol;

    @FXML
    private TableColumn<?, ?> nomeCol;

    @FXML
    private TableColumn<?, ?> nomeColRic;

    @FXML
    private TableColumn<?, ?> proteineCol;

    @FXML
    private Spinner<Integer> quantitaSpinner;

    @FXML
    private TableColumn<?, ?> saleCol;

    @FXML
    private TableView<Alimento> tableViewAlimenti;

    @FXML
    private TableView<Ricetta> tableViewRicette;

    @FXML
    private TextField textCercaAlimento;

    @FXML
    private TextField textCercaRicetta;

    @FXML
    private TableColumn<?, ?> zuccheriCol;

    private int offset = 0;
    private final int LIMIT = 50;
    private String pastoCorrente;

    private int idGiornoDieta;
    private String tipoPasto; // e.g., "Colazione", "Pranzo"
    private int idCliente; // The logged-in client's ID (Session.getUserId())
    private String data= LocalDate.now().toString();

    HomePage HomePageController;


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
        cercaRicette("", false); // Carica anche le ricette all'avvio
    }

    public void setHomePageController(HomePage HomePageController) {
        this.HomePageController = HomePageController;
    }

    public void setPastoData(int idGiornoDieta, String tipoPasto, int idCliente) {
        this.idGiornoDieta = idGiornoDieta;
        this.tipoPasto = tipoPasto;
        this.idCliente = idCliente;
        System.out.println("AggiungiPastoController - GiornoDieta ID: " + idGiornoDieta + ", Tipo Pasto: " + tipoPasto + ", Cliente ID: " + idCliente);
    }

    @FXML
    void confermaPasto(ActionEvent event) {
        Alimento alimentoSelezionato = tableViewAlimenti.getSelectionModel().getSelectedItem();
        Integer quantita = quantitaSpinner.getValue(); // Recupera la quantità dallo Spinner
        int idPastiGiornaliero;

        if (alimentoSelezionato != null && quantita > 0) {

            double kcalPerQuantita = (alimentoSelezionato.getKcal() / 100.0) * quantita;
            double proteinePerQuantita = (alimentoSelezionato.getProteine() / 100.0) * quantita;
            double carboidratiPerQuantita = (alimentoSelezionato.getCarboidrati() / 100.0) * quantita;
            double grassiPerQuantita = (alimentoSelezionato.getGrassi() / 100.0) * quantita;


            String query="SELECT id_pasti_giornaliero FROM PastiGiornalieri WHERE id_cliente = ? AND data = ? AND pasto = ?";
            try (Connection conn = SQLiteConnessione.connector();
                 PreparedStatement pstmt = conn.prepareStatement(query)){
                pstmt.setInt(1, idCliente);
                pstmt.setString(2, data);
                pstmt.setString(3, tipoPasto);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    idPastiGiornaliero = rs.getInt("id_pasti_giornaliero");
                    // Aggiorna i valori nutrizionali del pasto giornaliero esistente
                    String updatePastiGiornalieriQuery = "UPDATE PastiGiornalieri SET kcal = kcal + ?, proteine = proteine + ?, carboidrati = carboidrati + ?, grassi = grassi + ? WHERE id_pasti_giornaliero = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updatePastiGiornalieriQuery)) {
                        updateStmt.setDouble(1, kcalPerQuantita);
                        updateStmt.setDouble(2, proteinePerQuantita);
                        updateStmt.setDouble(3, carboidratiPerQuantita);
                        updateStmt.setDouble(4, grassiPerQuantita);
                        updateStmt.setInt(5, idPastiGiornaliero);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Inserisce nuovo pasto giornaliero con i valori dell'alimento
                    String insertQuery = "INSERT INTO PastiGiornalieri (id_cliente, id_giorno_dieta, data, pasto, kcal, proteine, carboidrati, grassi) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setInt(1, idCliente);
                        insertStmt.setInt(2, idGiornoDieta);
                        insertStmt.setString(3, data);
                        insertStmt.setString(4, tipoPasto);
                        insertStmt.setDouble(5, kcalPerQuantita);
                        insertStmt.setDouble(6, proteinePerQuantita);
                        insertStmt.setDouble(7, carboidratiPerQuantita);
                        insertStmt.setDouble(8, grassiPerQuantita);
                        insertStmt.executeUpdate();
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            idPastiGiornaliero = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creazione pasto giornaliero fallita, nessun ID ottenuto.");
                        }
                    }
                }
                String insertPasto = "INSERT INTO Pasto (id_pasti_giornaliero, tipo, id_elemento, quantita_grammi) VALUES (?, 'alimento', ?, ?)";
                try (PreparedStatement insertAlimentoStmt = conn.prepareStatement(insertPasto)) {
                    insertAlimentoStmt.setInt(1, idPastiGiornaliero);
                    insertAlimentoStmt.setInt(2, alimentoSelezionato.getId());
                    insertAlimentoStmt.setDouble(3, quantita);
                    insertAlimentoStmt.executeUpdate();
                }
                HomePageController.aggiornaLabelKcalPerPasto();
                System.out.println("Alimento aggiunto correttamente al pasto!");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        else {
            System.out.println("Seleziona un alimento e specifica una quantità valida!"); // Gestione degli errori
        }

        }



    @FXML
    void confermaPasto2(ActionEvent event) {
        Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem();
        Integer quantita = quantitaSpinner.getValue(); // Recupera la quantità dallo Spinner
        int idPastiGiornaliero;

        if (ricettaSelezionata != null && quantita > 0) {

            double kcalPerQuantita = (ricettaSelezionata.getKcal() / 100.0) * quantita;
            double proteinePerQuantita = (ricettaSelezionata.getProteine() / 100.0) * quantita;
            double carboidratiPerQuantita = (ricettaSelezionata.getCarboidrati() / 100.0) * quantita;
            double grassiPerQuantita = (ricettaSelezionata.getGrassi() / 100.0) * quantita;

            String query="SELECT id_pasti_giornaliero FROM PastiGiornalieri WHERE id_cliente = ? AND data = ? AND pasto = ?";
            try (Connection conn = SQLiteConnessione.connector();
                 PreparedStatement pstmt = conn.prepareStatement(query)){
                pstmt.setInt(1, idCliente);
                pstmt.setString(2, data);
                pstmt.setString(3, tipoPasto);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    // Pasto già esistente
                    idPastiGiornaliero = rs.getInt("id_pasti_giornaliero");
                    String updatePastiGiornalieriQuery = "UPDATE PastiGiornalieri SET kcal = kcal + ?, proteine = proteine + ?, carboidrati = carboidrati + ?, grassi = grassi + ? WHERE id_pasti_giornaliero = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updatePastiGiornalieriQuery)) {
                        updateStmt.setDouble(1, kcalPerQuantita);
                        updateStmt.setDouble(2, proteinePerQuantita);
                        updateStmt.setDouble(3, carboidratiPerQuantita);
                        updateStmt.setDouble(4, grassiPerQuantita);
                        updateStmt.setInt(5, idPastiGiornaliero);
                        updateStmt.executeUpdate();
                    }
                }
                else {
                    String insertQuery = "INSERT INTO PastiGiornalieri (id_cliente, id_giorno_dieta, data, pasto, kcal, proteine, carboidrati, grassi) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setInt(1, idCliente);
                        insertStmt.setInt(2, idGiornoDieta);
                        insertStmt.setString(3, data);
                        insertStmt.setString(4, tipoPasto);
                        insertStmt.setDouble(5, kcalPerQuantita);
                        insertStmt.setDouble(6, proteinePerQuantita);
                        insertStmt.setDouble(7, carboidratiPerQuantita);
                        insertStmt.setDouble(8, grassiPerQuantita);
                        insertStmt.executeUpdate();
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            idPastiGiornaliero = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creazione pasto giornaliero fallita, nessun ID ottenuto.");
                        }
                    }
                }
                String insertPasto = "INSERT INTO Pasto (id_pasti_giornaliero, tipo, id_elemento, quantita_grammi) VALUES (?, 'ricetta', ?, ?)";
                try (PreparedStatement insertAlimentoStmt = conn.prepareStatement(insertPasto)) {
                    insertAlimentoStmt.setInt(1, idPastiGiornaliero);
                    insertAlimentoStmt.setInt(2, ricettaSelezionata.getId());
                    insertAlimentoStmt.setDouble(3, quantita);
                    insertAlimentoStmt.executeUpdate();
                }

                System.out.println("Ricetta aggiunta correttamente al pasto!");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        else {
            System.out.println("Seleziona una ricetta e specifica una quantità valida!"); // Gestione degli errori
        }

    }

    @FXML
    void handleCercaAlimento(ActionEvent event) {
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

    private boolean isLoading = false;
    private void caricaAltriAlimenti() {
        if (isLoading) return;
        isLoading = true;
        String filtro = textCercaAlimento.getText();
        cercaAlimenti(filtro, true);
        isLoading = false;
    }

    @FXML
    void handleCercaRicetta(ActionEvent event) {
        offset = 0;
        tableViewRicette.getItems().clear();
        cercaRicette(textCercaRicetta.getText(), false);
    }
    public void cercaRicette(String filtro, boolean append) {
        ObservableList<Ricetta> ricette = append ? tableViewRicette.getItems() : FXCollections.observableArrayList();

        String categoria = ComboBoxRicetta.getSelectionModel().getSelectedItem();
        boolean soloMiei = CheckBoxRicette.isSelected();
        StringBuilder query = new StringBuilder("SELECT * FROM Ricette WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?");
        }
        if (soloMiei && Session.getUserId() != null) {
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

    private boolean isLoading2 = false;
    private void caricaAltreRicette() {
        if (isLoading2) return;
        isLoading2 = true;
        String filtro = textCercaRicetta.getText();
        cercaRicette(filtro, true);
        isLoading2 = false;
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
            ComboBoxAlimento.setItems(categorie);
            ComboBoxAlimento.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ObservableList<String> categoriePrefissate = FXCollections.observableArrayList(
                "Colazione", "Spuntino", "Pranzo", "Merenda", "Cena"
        );
        ComboBoxRicetta.setItems(categoriePrefissate);

    }

    @FXML
    void mostraTabellaAlimenti(ActionEvent event) {
        tableViewAlimenti.setVisible(true);
        tableViewRicette.setVisible(false);
        confermaPastoButton.setVisible(true);
        confermaPasto2Button.setVisible(false);
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
    void mostraTabellaRicette(ActionEvent event) {
        tableViewAlimenti.setVisible(false);
        tableViewRicette.setVisible(true);
        confermaPastoButton.setVisible(false);
        confermaPasto2Button.setVisible(true);
        textCercaAlimento.setVisible(false);
        textCercaRicetta.setVisible(true);
        ButtonCercaAlimento.setVisible(false);
        ButtonCercaRicetta.setVisible(true);
        ComboBoxAlimento.setVisible(false);
        ComboBoxRicetta.setVisible(true);
        CheckBoxAlimenti.setVisible(false);
        CheckBoxRicette.setVisible(true);
    }

}
