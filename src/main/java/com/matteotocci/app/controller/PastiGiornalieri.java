package com.matteotocci.app.controller;

import com.matteotocci.app.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.sql.SQLException;


public class PastiGiornalieri {
    @FXML private Button btnPrecedente;
    @FXML private Button btnSuccessivo;
    @FXML private Label lblDataCorrente;
    @FXML private DatePicker datePicker;
    @FXML private Label lblCalorieTotali;
    @FXML private Label lblProteineTotali;
    @FXML private Label lblCarboidratiTotali;
    @FXML private Label lblGrassiTotali;

    @FXML private Label lblKcalColazione;
    @FXML private TableView<PastoSpecifico> tableColazione;
    @FXML private TableColumn<PastoSpecifico, String> colColazioneAlimento;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneQuantita;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneKcal;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneProteine;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneCarboidrati;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneGrassi;
    @FXML private TableColumn<PastoSpecifico, Void> colColazioneAzioni;

    @FXML private Label lblKcalSpuntino;
    @FXML private TableView<PastoSpecifico> tableSpuntino;
    @FXML private TableColumn<PastoSpecifico, String> colSpuntinoAlimento;
    @FXML private TableColumn<PastoSpecifico, Double> colSpuntinoQuantita;
    @FXML private TableColumn<PastoSpecifico, Double> colSpuntinoKcal;
    @FXML private TableColumn<PastoSpecifico, Double> colSpuntinoProteine;
    @FXML private TableColumn<PastoSpecifico, Double> colSpuntinoCarboidrati;
    @FXML private TableColumn<PastoSpecifico, Double> colSpuntinoGrassi;
    @FXML private TableColumn<PastoSpecifico, Void> colSpuntinoAzioni;

    @FXML private Label lblKcalPranzo;
    @FXML private TableView<PastoSpecifico> tablePranzo;
    @FXML private TableColumn<PastoSpecifico, String> colPranzoAlimento;
    @FXML private TableColumn<PastoSpecifico, Double> colPranzoQuantita;
    @FXML private TableColumn<PastoSpecifico, Double> colPranzoKcal;
    @FXML private TableColumn<PastoSpecifico, Double> colPranzoProteine;
    @FXML private TableColumn<PastoSpecifico, Double> colPranzoCarboidrati;
    @FXML private TableColumn<PastoSpecifico, Double> colPranzoGrassi;
    @FXML private TableColumn<PastoSpecifico, Void> colPranzoAzioni;

    @FXML private Label lblKcalMerenda;
    @FXML private TableView<PastoSpecifico> tableMerenda;
    @FXML private TableColumn<PastoSpecifico, String> colMerendaAlimento;
    @FXML private TableColumn<PastoSpecifico, Double> colMerendaQuantita;
    @FXML private TableColumn<PastoSpecifico, Double> colMerendaKcal;
    @FXML private TableColumn<PastoSpecifico, Double> colMerendaProteine;
    @FXML private TableColumn<PastoSpecifico, Double> colMerendaCarboidrati;
    @FXML private TableColumn<PastoSpecifico, Double> colMerendaGrassi;
    @FXML private TableColumn<PastoSpecifico, Void> colMerendaAzioni;

    @FXML private Label lblKcalCena;
    @FXML private TableView<PastoSpecifico> tableCena;
    @FXML private TableColumn<PastoSpecifico, String> colCenaAlimento;
    @FXML private TableColumn<PastoSpecifico, Double> colCenaQuantita;
    @FXML private TableColumn<PastoSpecifico, Double> colCenaKcal;
    @FXML private TableColumn<PastoSpecifico, Double> colCenaProteine;
    @FXML private TableColumn<PastoSpecifico, Double> colCenaCarboidrati;
    @FXML private TableColumn<PastoSpecifico, Double> colCenaGrassi;
    @FXML private TableColumn<PastoSpecifico, Void> colCenaAzioni;

    private LocalDate dataCorrente;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private HomePage homePageController;
    public void setHomePageController(HomePage homePage) {
        homePageController = homePage;
    }

    public void initialize() {
        dataCorrente = LocalDate.now();
        AggiornaData();
        inizializzaTabelle();
        caricaPasti(dataCorrente);
    }

    private void AggiornaData() {
        datePicker.setValue(dataCorrente);
        aggiornaDateLabel(dataCorrente);

        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, DATE_FORMATTER) : null;
            }
        });
    }


    private void inizializzaTabelle() {
        // Initialize table columns for all meal types
        setupColonne(tableColazione, colColazioneAlimento, colColazioneQuantita, colColazioneKcal, colColazioneProteine, colColazioneCarboidrati, colColazioneGrassi);
        setupColonne(tableSpuntino, colSpuntinoAlimento, colSpuntinoQuantita, colSpuntinoKcal, colSpuntinoProteine, colSpuntinoCarboidrati, colSpuntinoGrassi);
        setupColonne(tablePranzo, colPranzoAlimento, colPranzoQuantita, colPranzoKcal, colPranzoProteine, colPranzoCarboidrati, colPranzoGrassi);
        setupColonne(tableMerenda, colMerendaAlimento, colMerendaQuantita, colMerendaKcal, colMerendaProteine, colMerendaCarboidrati, colMerendaGrassi);
        setupColonne(tableCena, colCenaAlimento, colCenaQuantita, colCenaKcal, colCenaProteine, colCenaCarboidrati, colCenaGrassi);

        // Add action buttons to the last column for each meal table
        addColonnaAzione(colColazioneAzioni, tableColazione, "Colazione");
        addColonnaAzione(colSpuntinoAzioni, tableSpuntino, "Spuntino");
        addColonnaAzione(colPranzoAzioni, tablePranzo, "Pranzo");
        addColonnaAzione(colMerendaAzioni, tableMerenda, "Merenda");
        addColonnaAzione(colCenaAzioni, tableCena, "Cena");
    }

    private void setupColonne(
            TableView<PastoSpecifico> tableView,
            TableColumn<PastoSpecifico, String> colAlimento,
            TableColumn<PastoSpecifico, Double> colQuantita,
            TableColumn<PastoSpecifico, Double> colKcal,
            TableColumn<PastoSpecifico, Double> colProteine,
            TableColumn<PastoSpecifico, Double> colCarboidrati,
            TableColumn<PastoSpecifico, Double> colGrassi) {

        colAlimento.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colQuantita.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colKcal.setCellValueFactory(cellData -> cellData.getValue().kcalProperty().asObject());
        colProteine.setCellValueFactory(cellData -> cellData.getValue().proteinProperty().asObject());
        colCarboidrati.setCellValueFactory(cellData -> cellData.getValue().carbProperty().asObject());
        colGrassi.setCellValueFactory(cellData -> cellData.getValue().fatProperty().asObject());

        // --- NUOVO: Formattazione per le colonne numeriche ---
        colKcal.setCellFactory(column -> new TableCell<PastoSpecifico, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.0f", item)); // Kcal a 0 decimali
            }
        });

        colProteine.setCellFactory(column -> new TableCell<PastoSpecifico, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item)); // Proteine a 1 decimale
            }
        });

        colCarboidrati.setCellFactory(column -> new TableCell<PastoSpecifico, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item)); // Carboidrati a 1 decimale
            }
        });

        colGrassi.setCellFactory(column -> new TableCell<PastoSpecifico, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item)); // Grassi a 1 decimale
            }
        });


        // Set default sort order for quantity
        colQuantita.setSortType(TableColumn.SortType.ASCENDING);
        tableView.getSortOrder().add(colQuantita); // Apply initial sort
    }


    private void addColonnaAzione(TableColumn<PastoSpecifico, Void> actionColumn, TableView<PastoSpecifico> tableView, String mealType) {
        Callback<TableColumn<PastoSpecifico, Void>, TableCell<PastoSpecifico, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<PastoSpecifico, Void> call(final TableColumn<PastoSpecifico, Void> param) {
                final TableCell<PastoSpecifico, Void> cell = new TableCell<>() {

                    private final Button deleteButton = new Button("X");
                    private final Button editButton = new Button("Mod");
                    private final HBox pane = new HBox(5, deleteButton, editButton);

                    {
                        pane.setAlignment(Pos.CENTER);
                        deleteButton.getStyleClass().add("delete-button"); // Add CSS class for styling
                        editButton.getStyleClass().add("edit-button");     // Add CSS class for styling

                        deleteButton.setOnAction(event -> {
                            PastoSpecifico entry = getTableView().getItems().get(getIndex());
                            handleDeletePastoSpecifico(entry, mealType);
                        });

                        editButton.setOnAction(event -> {
                            PastoSpecifico entry = getTableView().getItems().get(getIndex());
                            handleEditPastoSpecifico(entry, mealType);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
                return cell;
            }
        };

        actionColumn.setCellFactory(cellFactory);
    }

    private void handleDeletePastoSpecifico(PastoSpecifico entry, String mealType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Eliminazione");
        alert.setHeaderText("Eliminare l'alimento/ricetta?");
        alert.setContentText("Sei sicuro di voler eliminare '" + entry.getName() + "' da " + mealType + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            try (Connection conn = SQLiteConnessione.connector();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Pasto WHERE id_pasto = ?")) {
                updatePastiGiornalieriTotals(entry.getPastoId(), -entry.getKcal(), -entry.getProtein(), -entry.getCarb(), -entry.getFat());
                pstmt.setInt(1, entry.getPastoId());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Record eliminato con successo dal DB.");

                    caricaPasti(dataCorrente); // Ricarica i pasti per riflettere i cambiamenti
                    homePageController.aggiornaLabelKcalPerPasto();
                    showAlert(Alert.AlertType.INFORMATION,"Eliminato!", "'" + entry.getName() + "' eliminato con successo.");
                } else {
                    showAlert(Alert.AlertType.ERROR,"Errore Eliminazione", "Impossibile eliminare '" + entry.getName() + "'. Record non trovato nel database.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR,"Errore Database", "Si è verificato un errore durante l'eliminazione: " + e.getMessage());
            }
        }
    }

    private void updatePastiGiornalieriTotals(int pastoId, double deltaKcal, double deltaProtein, double deltaCarb, double deltaFat) {
        int idPastiGiornaliero = -1;

        // Trova l'id_pasti_giornaliero associato a id_pasto
        String sqlSelectPgId = "SELECT id_pasti_giornaliero FROM Pasto WHERE id_pasto = ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sqlSelectPgId)) {
            pstmt.setInt(1, pastoId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                idPastiGiornaliero = rs.getInt("id_pasti_giornaliero");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Errore Database", "Impossibile recuperare l'ID del pasto giornaliero per l'aggiornamento dei totali: " + e.getMessage());
            return;
        }

        if (idPastiGiornaliero != -1) {
            // Aggiorna i totali in PastiGiornalieri
            String sqlUpdatePg = "UPDATE PastiGiornalieri SET kcal = kcal + ?, proteine = proteine + ?, carboidrati = carboidrati + ?, grassi = grassi + ? WHERE id_pasti_giornaliero = ?";
            try (Connection conn = SQLiteConnessione.connector();
                 PreparedStatement pstmt = conn.prepareStatement(sqlUpdatePg)) {
                pstmt.setDouble(1, deltaKcal);
                pstmt.setDouble(2, deltaProtein);
                pstmt.setDouble(3, deltaCarb);
                pstmt.setDouble(4, deltaFat);
                pstmt.setInt(5, idPastiGiornaliero);
                pstmt.executeUpdate();
                System.out.println("DEBUG: Totali PastiGiornalieri aggiornati per id_pasti_giornaliero: " + idPastiGiornaliero +
                        " (Kcal: " + deltaKcal + ", Prot: " + deltaProtein + ", Carb: " + deltaCarb + ", Fat: " + deltaFat + ")");

                System.out.println("Totali PastiGiornalieri aggiornati.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR,"Errore Database", "Si è verificato un errore durante l'aggiornamento dei totali del pasto giornaliero: " + e.getMessage());
            }
        } else {
            System.err.println("Attenzione: ID del pasto giornaliero non trovato per il pasto con ID: " + pastoId);
        }
    }



    private void handleEditPastoSpecifico(PastoSpecifico entry, String mealType) {
        System.out.println("Modifica " + entry.getName() + " in " + mealType);
        TextInputDialog dialog = new TextInputDialog(String.valueOf(entry.getQuantity())); // Pre-compila con la quantità attuale
        dialog.setTitle("Modifica Quantità");
        dialog.setHeaderText("Modifica la quantità per " + entry.getName() + " nel pasto " + mealType);
        dialog.setContentText("Nuova quantità (grammi/porzioni):");

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm());
        // Aggiungi la classe base 'dialog-pane' per applicare gli stili generali
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        // Non è necessario aggiungere classi specifiche come 'alert-information',
        // poiché il tuo CSS usa già il selettore '.text-input-dialog' che JavaFX
        // applica automaticamente a questo tipo di dialogo.

        // 2. Mostra il dialogo e attendi la risposta dell'utente
        Optional<String> result = dialog.showAndWait();

        // 3. Elabora la risposta dell'utente
        result.ifPresent(newQuantityString -> {
            try {
                double newQuantity = Double.parseDouble(newQuantityString);

                if (newQuantity <= 0) {
                    showAlert(Alert.AlertType.ERROR,"Quantità non valida", "La quantità deve essere un numero positivo.");
                    return;
                }

                // Confronta con la vecchia quantità per evitare aggiornamenti inutili
                if (newQuantity == entry.getQuantity()) {
                    showAlert(Alert.AlertType.ERROR,"Nessuna modifica", "La quantità non è cambiata.");
                    return;
                }

                // 4. Chiama un nuovo metodo per aggiornare il database
                updatePastoQuantity(entry, newQuantity);

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR,"Input non valido", "Per favore, inserisci un numero valido per la quantità.");
            }
        });
    }

    private void updatePastoQuantity(PastoSpecifico entry, double newQuantity) {
        // Calcola le vecchie macro e kcal basate sulla vecchia quantità
        double oldQuantity = entry.getQuantity();
        double oldKcal = entry.getKcal();
        double oldProtein = entry.getProtein();
        double oldCarb = entry.getCarb();
        double oldFat = entry.getFat();

        // CORREZIONE: Per ottenere i valori base corretti, devi rifare la query al database
        // per ottenere i valori originali (per 100g o per porzione)
        double baseKcal = 0, baseProtein = 0, baseCarb = 0, baseFat = 0;

        try (Connection conn = SQLiteConnessione.connector()) {
            // Prima ottieni il tipo e id_elemento dal database
            String sqlGetPastoInfo = "SELECT tipo, id_elemento FROM Pasto WHERE id_pasto = ?";
            String tipo = "";
            int idElemento = 0;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlGetPastoInfo)) {
                pstmt.setInt(1, entry.getPastoId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    tipo = rs.getString("tipo");
                    idElemento = rs.getInt("id_elemento");
                }
            }

            // Ora ottieni i valori nutrizionali base
            if ("alimento".equals(tipo)) {
                String sqlAlimento = "SELECT kcal, proteine, carboidrati, grassi FROM foods WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlAlimento)) {
                    pstmt.setInt(1, idElemento);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        baseKcal = rs.getDouble("kcal");      // Questi sono per 100g
                        baseProtein = rs.getDouble("proteine");
                        baseCarb = rs.getDouble("carboidrati");
                        baseFat = rs.getDouble("grassi");
                    }
                }
            } else if ("ricetta".equals(tipo)) {
                String sqlRicetta = "SELECT kcal, proteine, carboidrati, grassi FROM Ricette WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlRicetta)) {
                    pstmt.setInt(1, idElemento);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        baseKcal = rs.getDouble("kcal");      // Questi sono per porzione
                        baseProtein = rs.getDouble("proteine");
                        baseCarb = rs.getDouble("carboidrati");
                        baseFat = rs.getDouble("grassi");
                    }
                }
            }

            // Calcola i nuovi valori
            double newCalculatedKcal, newCalculatedProtein, newCalculatedCarb, newCalculatedFat;

            if ("alimento".equals(tipo)) {
                // Per alimenti: valori base sono per 100g
                newCalculatedKcal = baseKcal * (newQuantity / 100.0);
                newCalculatedProtein = baseProtein * (newQuantity / 100.0);
                newCalculatedCarb = baseCarb * (newQuantity / 100.0);
                newCalculatedFat = baseFat * (newQuantity / 100.0);
            } else {
                // Per ricette: valori base sono per porzione
                newCalculatedKcal = baseKcal * newQuantity;
                newCalculatedProtein = baseProtein * newQuantity;
                newCalculatedCarb = baseCarb * newQuantity;
                newCalculatedFat = baseFat * newQuantity;
            }

            // Calcola la differenza nelle macro e kcal
            double deltaKcal = newCalculatedKcal - oldKcal;
            double deltaProtein = newCalculatedProtein - oldProtein;
            double deltaCarb = newCalculatedCarb - oldCarb;
            double deltaFat = newCalculatedFat - oldFat;

            // Aggiorna la quantità del pasto specifico nella tabella 'Pasto'
            String sqlUpdatePasto = "UPDATE Pasto SET quantita_grammi = ? WHERE id_pasto = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdatePasto)) {
                pstmt.setDouble(1, newQuantity);
                pstmt.setInt(2, entry.getPastoId());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Quantità aggiornata nel DB per Pasto ID: " + entry.getPastoId());

                    // Aggiorna i totali nella tabella PastiGiornalieri
                    updatePastiGiornalieriTotals(entry.getPastoId(), deltaKcal, deltaProtein, deltaCarb, deltaFat);

                    // Ricarica tutti i pasti per il giorno corrente per aggiornare l'interfaccia utente
                    caricaPasti(dataCorrente);

                    // Aggiorna anche la HomePage
                    if (homePageController != null) {
                        homePageController.aggiornaLabelKcalPerPasto();
                    }

                    showAlert(Alert.AlertType.INFORMATION,"Successo", "Quantità per '" + entry.getName() + "' aggiornata a " + String.format("%.1f", newQuantity) + ".");
                } else {
                    showAlert(Alert.AlertType.ERROR,"Errore", "Impossibile aggiornare la quantità. Record non trovato.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Errore Database", "Si è verificato un errore durante l'aggiornamento: " + e.getMessage());
        }
    }

    @FXML
    private void onPrecedenteClick() {
        dataCorrente = dataCorrente.minusDays(1);
        datePicker.setValue(dataCorrente);
        aggiornaDateLabel(dataCorrente);
        caricaPasti(dataCorrente);
    }

    @FXML
    private void onSuccessivoClick() {
        dataCorrente = dataCorrente.plusDays(1);
        datePicker.setValue(dataCorrente);
        aggiornaDateLabel(dataCorrente);
        caricaPasti(dataCorrente);
    }

    @FXML
    private void onDataSelezionata() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            dataCorrente = selectedDate;
            aggiornaDateLabel(dataCorrente);
            caricaPasti(dataCorrente);
        }
    }


    private void aggiornaDateLabel(LocalDate date) {
        String labelText = "";
        if (date.isEqual(LocalDate.now())) {
            labelText = "Oggi - " + DATE_FORMATTER.format(date);
        } else if (date.isEqual(LocalDate.now().minusDays(1))) {
            labelText = "Ieri - " + DATE_FORMATTER.format(date);
        } else if (date.isEqual(LocalDate.now().plusDays(1))) {
            labelText = "Domani - " + DATE_FORMATTER.format(date);
        } else {
            labelText = DATE_FORMATTER.format(date);
        }
        lblDataCorrente.setText(labelText);
    }

    private void caricaPasti(LocalDate date) {
        // Clear previous data
        tableColazione.getItems().clear();
        tableSpuntino.getItems().clear();
        tablePranzo.getItems().clear();
        tableMerenda.getItems().clear();
        tableCena.getItems().clear();

        lblKcalColazione.setText("0 kcal");
        lblKcalSpuntino.setText("0 kcal");
        lblKcalPranzo.setText("0 kcal");
        lblKcalMerenda.setText("0 kcal");
        lblKcalCena.setText("0 kcal");

        double totalKcal = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFats = 0;

        String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String sqlPastiGiornalieri = "SELECT * FROM PastiGiornalieri WHERE id_cliente = ? AND data = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sqlPastiGiornalieri)) {

            pstmt.setInt(1, Session.getUserId());
            pstmt.setString(2, dateString);
            ResultSet rs = pstmt.executeQuery();

            Map<String, ObservableList<PastoSpecifico>> mealData = new HashMap<>();
            mealData.put("Colazione", FXCollections.observableArrayList());
            mealData.put("Spuntino", FXCollections.observableArrayList());
            mealData.put("Pranzo", FXCollections.observableArrayList());
            mealData.put("Merenda", FXCollections.observableArrayList());
            mealData.put("Cena", FXCollections.observableArrayList());

            Map<String, Double> mealKcalTotals = new HashMap<>();
            mealKcalTotals.put("Colazione", 0.0);
            mealKcalTotals.put("Spuntino", 0.0);
            mealKcalTotals.put("Pranzo", 0.0);
            mealKcalTotals.put("Merenda", 0.0);
            mealKcalTotals.put("Cena", 0.0);

            while (rs.next()) {
                PastoGiornaliero pg = new PastoGiornaliero(
                        rs.getInt("id_pasti_giornaliero"),
                        rs.getInt("id_cliente"),
                        rs.getInt("id_giorno_dieta"),
                        LocalDate.parse(rs.getString("data")),
                        rs.getString("pasto"),
                        rs.getDouble("kcal"),
                        rs.getDouble("proteine"),
                        rs.getDouble("carboidrati"),
                        rs.getDouble("grassi")
                );

                // Update individual meal totals based on PastiGiornalieri table
                mealKcalTotals.merge(pg.getPasto(), pg.getKcal(), Double::sum);


                // Fetch individual meal entries
                // We need to fetch Pasto.id_pasto here to enable deletion/editing
                String sqlPasto = "SELECT id_pasto, tipo, id_elemento, quantita_grammi FROM Pasto WHERE id_pasti_giornaliero = ?";
                try (PreparedStatement pstmtPasto = conn.prepareStatement(sqlPasto)) {
                    pstmtPasto.setInt(1, pg.getIdPastiGiornaliero());
                    ResultSet rsPasto = pstmtPasto.executeQuery();

                    while (rsPasto.next()) {
                        Pasto pasto = new Pasto(
                                rsPasto.getInt("id_pasto"), // Important: get id_pasto here
                                pg.getIdPastiGiornaliero(),
                                rsPasto.getString("tipo"),
                                rsPasto.getInt("id_elemento"),
                                rsPasto.getDouble("quantita_grammi")
                        );

                        PastoSpecifico entry = getPastoSpecificoDetails(conn, pasto);
                        if (entry != null) {
                            // Assign the Pasto ID to the PastoSpecifico
                            entry.setPastoId(pasto.getIdPasto());
                            mealData.get(pg.getPasto()).add(entry);
                        }
                    }
                }
            }

            // Populate TableViews
            tableColazione.setItems(mealData.get("Colazione"));
            tableSpuntino.setItems(mealData.get("Spuntino"));
            tablePranzo.setItems(mealData.get("Pranzo"));
            tableMerenda.setItems(mealData.get("Merenda"));
            tableCena.setItems(mealData.get("Cena"));

            // Update meal calorie labels
            lblKcalColazione.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Colazione"))));
            lblKcalSpuntino.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Spuntino"))));
            lblKcalPranzo.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Pranzo"))));
            lblKcalMerenda.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Merenda"))));
            lblKcalCena.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Cena"))));


            // Calculate overall daily totals from individual meal entries
            // This provides a more accurate sum based on actual quantities
            totalKcal = 0;
            totalProtein = 0;
            totalCarbs = 0;
            totalFats = 0;

            for (ObservableList<PastoSpecifico> entries : mealData.values()) {
                for (PastoSpecifico entry : entries) {
                    totalKcal += entry.getKcal();
                    totalProtein += entry.getProtein();
                    totalCarbs += entry.getCarb();
                    totalFats += entry.getFat();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Errore", "Caricamento fallito" + e.getMessage());
        }

        // Update overall daily nutrition summary
        lblCalorieTotali.setText(String.format("%.0f kcal", fixNegativeZero(totalKcal)));
        lblProteineTotali.setText(String.format("%.1fg", fixNegativeZero(totalProtein)));
        lblCarboidratiTotali.setText(String.format("%.1fg", fixNegativeZero(totalCarbs)));
        lblGrassiTotali.setText(String.format("%.1fg", fixNegativeZero(totalFats)));
    }

    private PastoSpecifico getPastoSpecificoDetails(Connection conn, Pasto pasto) throws SQLException {
        String name = "";
        double baseKcal = 0, baseProtein = 0, baseCarb = 0, baseFat = 0;

        if ("alimento".equals(pasto.getTipo())) {
            String sqlAlimento = "SELECT nome, kcal, proteine, carboidrati, grassi FROM foods WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlAlimento)) {
                pstmt.setInt(1, pasto.getIdElemento());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    name = rs.getString("nome");
                    baseKcal = rs.getDouble("kcal");        // Valori per 100g
                    baseProtein = rs.getDouble("proteine");
                    baseCarb = rs.getDouble("carboidrati");
                    baseFat = rs.getDouble("grassi");
                }
            }

            // Calcola i valori nutrizionali in base alla quantità in grammi
            // Per gli alimenti: valori base sono per 100g, quindi dividiamo per 100
            if (!name.isEmpty()) {
                double calculatedKcal = baseKcal * (pasto.getQuantitaGrammi() / 100.0);
                double calculatedProtein = baseProtein * (pasto.getQuantitaGrammi() / 100.0);
                double calculatedCarb = baseCarb * (pasto.getQuantitaGrammi() / 100.0);
                double calculatedFat = baseFat * (pasto.getQuantitaGrammi() / 100.0);

                return new PastoSpecifico(name, pasto.getQuantitaGrammi(),
                        calculatedKcal, calculatedProtein, calculatedCarb, calculatedFat);
            }

        } else if ("ricetta".equals(pasto.getTipo())) {
            String sqlRicetta = "SELECT nome, kcal, proteine, carboidrati, grassi FROM Ricette WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlRicetta)) {
                pstmt.setInt(1, pasto.getIdElemento());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    name = rs.getString("nome");
                    baseKcal = rs.getDouble("kcal");        // Valori per porzione
                    baseProtein = rs.getDouble("proteine");
                    baseCarb = rs.getDouble("carboidrati");
                    baseFat = rs.getDouble("grassi");
                }
            }

            // Calcola i valori nutrizionali in base al numero di porzioni
            // Per le ricette: valori base sono per porzione, quindi moltiplichiamo direttamente
            if (!name.isEmpty()) {
                double calculatedKcal = baseKcal * pasto.getQuantitaGrammi();
                double calculatedProtein = baseProtein * pasto.getQuantitaGrammi();
                double calculatedCarb = baseCarb * pasto.getQuantitaGrammi();
                double calculatedFat = baseFat * pasto.getQuantitaGrammi();

                return new PastoSpecifico(name, pasto.getQuantitaGrammi(),
                        calculatedKcal, calculatedProtein, calculatedCarb, calculatedFat);
            }
        }

        return null;
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

    private double fixNegativeZero(double value) {
        return Math.abs(value) < 0.0001 ? 0.0 : value;
    }

}



