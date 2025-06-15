package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
import com.matteotocci.app.model.SQLiteConnessione;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import java.util.ResourceBundle;

import com.matteotocci.app.model.*;


public class PastiGiornalieri implements Initializable {
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
    private LocalDate dataCorrente; // Variabile per tenere traccia della data attualmente visualizzata.
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Formattatore per le date.

    private HomePage homePageController; // Riferimento al controller della HomePage per aggiornare le informazioni lì.

    // Metodo per impostare il controller della HomePage.
    public void setHomePageController(HomePage homePage) {
        homePageController = homePage;
    }

    // Metodo di inizializzazione, chiamato automaticamente da JavaFX dopo che il file FXML è stato caricato.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dataCorrente = LocalDate.now(); // Imposta la data corrente all'avvio.
        AggiornaData(); // Aggiorna l'interfaccia utente con la data corrente.
        inizializzaTabelle(); // Configura le colonne e le factory delle celle per tutte le tabelle.
        caricaPasti(dataCorrente); // Carica i pasti per la data corrente dal database.
    }

    // Metodo per aggiornare il DatePicker e la label della data.
    private void AggiornaData() {
        datePicker.setValue(dataCorrente); // Imposta la data nel DatePicker.
        aggiornaDateLabel(dataCorrente); // Aggiorna la label testuale della data.

        // Imposta un convertitore personalizzato per il DatePicker, per formattare e parsare le date.
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                // Converte LocalDate in una stringa nel formato "dd/MM/yyyy".
                return (date != null) ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                // Converte una stringa nel formato "dd/MM/yyyy" in LocalDate.
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, DATE_FORMATTER) : null;
            }
        });
    }

    // Metodo privato per inizializzare tutte le tabelle dei pasti.
    private void inizializzaTabelle() {
        // Chiama setupColonne per configurare le colonne per ogni tipo di pasto.
        setupColonne(tableColazione, colColazioneAlimento, colColazioneQuantita, colColazioneKcal, colColazioneProteine, colColazioneCarboidrati, colColazioneGrassi);
        setupColonne(tableSpuntino, colSpuntinoAlimento, colSpuntinoQuantita, colSpuntinoKcal, colSpuntinoProteine, colSpuntinoCarboidrati, colSpuntinoGrassi);
        setupColonne(tablePranzo, colPranzoAlimento, colPranzoQuantita, colPranzoKcal, colPranzoProteine, colPranzoCarboidrati, colPranzoGrassi);
        setupColonne(tableMerenda, colMerendaAlimento, colMerendaQuantita, colMerendaKcal, colMerendaProteine, colMerendaCarboidrati, colMerendaGrassi);
        setupColonne(tableCena, colCenaAlimento, colCenaQuantita, colCenaKcal, colCenaProteine, colCenaCarboidrati, colCenaGrassi);

        // Aggiunge i bottoni di azione (elimina/modifica) all'ultima colonna di ogni tabella.
        addColonnaAzione(colColazioneAzioni, tableColazione, "Colazione");
        addColonnaAzione(colSpuntinoAzioni, tableSpuntino, "Spuntino");
        addColonnaAzione(colPranzoAzioni, tablePranzo, "Pranzo");
        addColonnaAzione(colMerendaAzioni, tableMerenda, "Merenda");
        addColonnaAzione(colCenaAzioni, tableCena, "Cena");
    }

    // Metodo generico per configurare le colonne di una TableView.
    private void setupColonne(
            TableView<PastoSpecifico> tableView, // La TableView da configurare.
            TableColumn<PastoSpecifico, String> colAlimento,
            TableColumn<PastoSpecifico, Double> colQuantita,
            TableColumn<PastoSpecifico, Double> colKcal,
            TableColumn<PastoSpecifico, Double> colProteine,
            TableColumn<PastoSpecifico, Double> colCarboidrati,
            TableColumn<PastoSpecifico, Double> colGrassi) {

        // Imposta le cell value factory per mappare le proprietà dell'oggetto PastoSpecifico alle colonne.
        colAlimento.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colQuantita.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colKcal.setCellValueFactory(cellData -> cellData.getValue().kcalProperty().asObject());
        colProteine.setCellValueFactory(cellData -> cellData.getValue().proteinProperty().asObject());
        colCarboidrati.setCellValueFactory(cellData -> cellData.getValue().carbProperty().asObject());
        colGrassi.setCellValueFactory(cellData -> cellData.getValue().fatProperty().asObject());

        // --- NUOVO: Formattazione per le colonne numeriche per visualizzare i decimali in modo specifico ---
        colKcal.setCellFactory(column -> new TableCell<PastoSpecifico, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.0f", item)); // Kcal senza decimali.
            }
        });

        colProteine.setCellFactory(column -> new TableCell<PastoSpecifico, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item)); // Proteine con un decimale.
            }
        });

        colCarboidrati.setCellFactory(column -> new TableCell<PastoSpecifico, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item)); // Carboidrati con un decimale.
            }
        });

        colGrassi.setCellFactory(column -> new TableCell<PastoSpecifico, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item)); // Grassi con un decimale.
            }
        });


        // Imposta l'ordinamento predefinito per la colonna quantità (crescente).
        colQuantita.setSortType(TableColumn.SortType.ASCENDING);
        tableView.getSortOrder().add(colQuantita); // Applica l'ordinamento iniziale.
    }

    // Metodo per aggiungere una colonna con i bottoni di azione (Elimina, Modifica) a una TableView.
    private void addColonnaAzione(TableColumn<PastoSpecifico, Void> actionColumn, TableView<PastoSpecifico> tableView, String mealType) {
        // Crea una cell factory per generare le celle della colonna.
        Callback<TableColumn<PastoSpecifico, Void>, TableCell<PastoSpecifico, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<PastoSpecifico, Void> call(final TableColumn<PastoSpecifico, Void> param) {
                final TableCell<PastoSpecifico, Void> cell = new TableCell<>() {

                    private final Button deleteButton = new Button("X"); // Bottone per eliminare.
                    private final Button editButton = new Button("Mod"); // Bottone per modificare.
                    private final HBox pane = new HBox(5, deleteButton, editButton); // Contenitore per i bottoni.

                    {
                        pane.setAlignment(Pos.CENTER); // Allinea i bottoni al centro.
                        deleteButton.getStyleClass().add("delete-button"); // Aggiunge classi CSS per lo styling.
                        editButton.getStyleClass().add("edit-button");     // Aggiunge classi CSS per lo styling.

                        // Imposta l'azione per il bottone "Elimina".
                        deleteButton.setOnAction(event -> {
                            PastoSpecifico entry = getTableView().getItems().get(getIndex()); // Ottiene l'elemento dalla riga.
                            handleDeletePastoSpecifico(entry, mealType); // Chiama il metodo per gestire l'eliminazione.
                        });

                        // Imposta l'azione per il bottone "Modifica".
                        editButton.setOnAction(event -> {
                            PastoSpecifico entry = getTableView().getItems().get(getIndex()); // Ottiene l'elemento dalla riga.
                            handleEditPastoSpecifico(entry, mealType); // Chiama il metodo per gestire la modifica.
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null); // Se la cella è vuota, non mostrare nulla.
                        } else {
                            setGraphic(pane); // Altrimenti, mostra il contenitore dei bottoni.
                        }
                    }
                };
                return cell;
            }
        };

        actionColumn.setCellFactory(cellFactory); // Applica la cell factory alla colonna.
    }

    // Metodo per gestire l'eliminazione di un PastoSpecifico.
    private void handleDeletePastoSpecifico(PastoSpecifico entry, String mealType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Crea un alert di conferma.
        alert.setTitle("Conferma Eliminazione"); // Imposta il titolo dell'alert.
        alert.setHeaderText("Eliminare l'alimento/ricetta?"); // Imposta l'header text dell'alert.
        alert.setContentText("Sei sicuro di voler eliminare '" + entry.getName() + "' da " + mealType + "?"); // Imposta il contenuto dell'alert.

        Optional<ButtonType> result = alert.showAndWait(); // Mostra l'alert e attende la risposta dell'utente.
        if (result.isPresent() && result.get() == ButtonType.OK) { // Se l'utente conferma l'eliminazione.

            // Tenta di eliminare il pasto dal database.
            try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Pasto WHERE id_pasto = ?")) { // Prepara lo statement SQL per l'eliminazione.
                // Prima di eliminare il pasto, aggiorna i totali del PastoGiornaliero sottraendo le sue macro.
                updatePastiGiornalieriTotals(entry.getPastoId(), -entry.getKcal(), -entry.getProtein(), -entry.getCarb(), -entry.getFat()); // Aggiorna i totali delle macro.
                pstmt.setInt(1, entry.getPastoId()); // Imposta l'ID del pasto da eliminare.
                int affectedRows = pstmt.executeUpdate(); // Esegue l'eliminazione e ottiene il numero di righe influenzate.

                if (affectedRows > 0) { // Se la riga è stata eliminata con successo.
                    System.out.println("Record eliminato con successo dal DB."); // Messaggio di debug.
                    caricaPasti(dataCorrente); // Ricarica tutti i pasti per aggiornare l'interfaccia utente.
                    homePageController.aggiornaLabelKcalPerPasto(); // Aggiorna le label nella HomePage (se il riferimento è disponibile).
                    showAlert(Alert.AlertType.INFORMATION,"Eliminato!", "'" + entry.getName() + "' eliminato con successo."); // Mostra un alert di successo.
                } else {
                    showAlert(Alert.AlertType.ERROR,"Errore Eliminazione", "Impossibile eliminare '" + entry.getName() + "'. Record non trovato nel database."); // Mostra un alert di errore se il record non è stato trovato.
                }
            } catch (SQLException e) { // Cattura le eccezioni SQL.
                e.printStackTrace(); // Stampa l'errore SQL.
                showAlert(Alert.AlertType.ERROR,"Errore Database", "Si è verificato un errore durante l'eliminazione: " + e.getMessage()); // Mostra un alert di errore di database.
            }
        }
    }

    // Metodo per aggiornare i totali di calorie, proteine, carboidrati e grassi nella tabella PastiGiornalieri.
    private void updatePastiGiornalieriTotals(int pastoId, double deltaKcal, double deltaProtein, double deltaCarb, double deltaFat) {
        int idPastiGiornaliero = -1; // Inizializza l'ID del pasto giornaliero a -1.

        // Trova l'id_pasti_giornaliero associato all'id_pasto.
        String sqlSelectPgId = "SELECT id_pasti_giornaliero FROM Pasto WHERE id_pasto = ?"; // Query SQL per selezionare l'ID del pasto giornaliero.
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(sqlSelectPgId)) { // Prepara lo statement SQL.
            pstmt.setInt(1, pastoId); // Imposta l'ID del pasto come parametro.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query.
            if (rs.next()) { // Se viene trovato un risultato.
                idPastiGiornaliero = rs.getInt("id_pasti_giornaliero"); // Ottiene l'ID del pasto giornaliero.
            }
        } catch (SQLException e) { // Cattura le eccezioni SQL.
            e.printStackTrace(); // Stampa l'errore SQL.
            showAlert(Alert.AlertType.ERROR,"Errore Database", "Impossibile recuperare l'ID del pasto giornaliero per l'aggiornamento dei totali: " + e.getMessage()); // Mostra un alert di errore.
            return; // Esce dal metodo.
        }

        if (idPastiGiornaliero != -1) { // Se l'id_pasti_giornaliero è stato trovato.
            // Aggiorna i totali nella tabella PastiGiornalieri sommando i delta (che possono essere positivi o negativi).
            String sqlUpdatePg = "UPDATE PastiGiornalieri SET kcal = kcal + ?, proteine = proteine + ?, carboidrati = carboidrati + ?, grassi = grassi + ? WHERE id_pasti_giornaliero = ?"; // Query SQL per aggiornare i totali.
            try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
                 PreparedStatement pstmt = conn.prepareStatement(sqlUpdatePg)) { // Prepara lo statement SQL.
                pstmt.setDouble(1, deltaKcal); // Imposta il delta delle calorie.
                pstmt.setDouble(2, deltaProtein); // Imposta il delta delle proteine.
                pstmt.setDouble(3, deltaCarb); // Imposta il delta dei carboidrati.
                pstmt.setDouble(4, deltaFat); // Imposta il delta dei grassi.
                pstmt.setInt(5, idPastiGiornaliero); // Imposta l'ID del pasto giornaliero.
                pstmt.executeUpdate(); // Esegue l'aggiornamento.
                System.out.println("DEBUG: Totali PastiGiornalieri aggiornati per id_pasti_giornaliero: " + idPastiGiornaliero + // Messaggio di debug.
                        " (Kcal: " + deltaKcal + ", Prot: " + deltaProtein + ", Carb: " + deltaCarb + ", Fat: " + deltaFat + ")");

                System.out.println("Totali PastiGiornalieri aggiornati."); // Messaggio di debug.
            } catch (SQLException e) { // Cattura le eccezioni SQL.
                e.printStackTrace(); // Stampa l'errore SQL.
                showAlert(Alert.AlertType.ERROR,"Errore Database", "Si è verificato un errore durante l'aggiornamento dei totali del pasto giornaliero: " + e.getMessage()); // Mostra un alert di errore.
            }
        } else {
            System.err.println("Attenzione: ID del pasto giornaliero non trovato per il pasto con ID: " + pastoId); // Messaggio di errore se l'ID non è stato trovato.
        }
    }

    // Metodo per gestire la modifica della quantità di un PastoSpecifico.
    private void handleEditPastoSpecifico(PastoSpecifico entry, String mealType) {
        System.out.println("Modifica " + entry.getName() + " in " + mealType); // Messaggio di debug.
        // Crea un dialog per chiedere la nuova quantità, pre-compilando con la quantità attuale.
        TextInputDialog dialog = new TextInputDialog(String.valueOf(entry.getQuantity())); // Crea un dialog di input testuale con la quantità attuale pre-compilata.
        dialog.setTitle("Modifica Quantità"); // Imposta il titolo del dialog.
        dialog.setHeaderText("Modifica la quantità per " + entry.getName() + " nel pasto " + mealType); // Imposta l'header text.
        dialog.setContentText("Nuova quantità (grammi/porzioni):"); // Imposta il contenuto del dialog.

        // Applica gli stili CSS al dialog.
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm()); // Aggiunge il CSS.
        dialog.getDialogPane().getStyleClass().add("dialog-pane"); // Aggiunge la classe di stile.

        // Mostra il dialog e attende la risposta dell'utente.
        Optional<String> result = dialog.showAndWait(); // Mostra il dialog e attende che l'utente clicchi OK o Annulla.

        // Elabora la risposta dell'utente.
        result.ifPresent(newQuantityString -> { // Se l'utente ha inserito un valore (non ha annullato).
            try {
                double newQuantity = Double.parseDouble(newQuantityString); // Tenta di convertire l'input in double.

                if (newQuantity <= 0) { // Validazione: la quantità deve essere positiva.
                    showAlert(Alert.AlertType.ERROR,"Quantità non valida", "La quantità deve essere un numero positivo."); // Mostra un alert di errore.
                    return; // Esce dal metodo.
                }

                // Confronta con la vecchia quantità per evitare aggiornamenti inutili.
                if (newQuantity == entry.getQuantity()) { // Se la nuova quantità è uguale alla vecchia.
                    showAlert(Alert.AlertType.ERROR,"Nessuna modifica", "La quantità non è cambiata."); // Mostra un alert di errore.
                    return; // Esce dal metodo.
                }

                // Chiama il metodo per aggiornare il database con la nuova quantità.
                updatePastoQuantity(entry, newQuantity); // Chiama il metodo per aggiornare la quantità nel database.

            } catch (NumberFormatException e) { // Cattura l'eccezione se l'input non è un numero valido.
                showAlert(Alert.AlertType.ERROR,"Input non valido", "Per favore, inserisci un numero valido per la quantità."); // Mostra un alert di errore.
            }
        });
    }

    // Metodo per aggiornare la quantità di un pasto nel database e ricalcolare le macro.
    private void updatePastoQuantity(PastoSpecifico entry, double newQuantity) {
        // Memorizza le vecchie macro e kcal prima dell'aggiornamento.
        double oldKcal = entry.getKcal(); // Calorie originali.
        double oldProtein = entry.getProtein(); // Proteine originali.
        double oldCarb = entry.getCarb(); // Carboidrati originali.
        double oldFat = entry.getFat(); // Grassi originali.

        double baseKcal = 0, baseProtein = 0, baseCarb = 0, baseFat = 0; // Valori nutrizionali per 100g/porzione, inizializzati a 0.

        try (Connection conn = SQLiteConnessione.connector()) { // Ottiene una connessione al database.
            // Prima, recupera il tipo (alimento/ricetta) e l'id_elemento dal database per il pasto specifico.
            String sqlGetPastoInfo = "SELECT tipo, id_elemento FROM Pasto WHERE id_pasto = ?"; // Query per recuperare tipo e id_elemento del pasto.
            String tipo = ""; // Tipo dell'elemento (alimento/ricetta).
            int idElemento = 0; // ID dell'alimento o ricetta.

            try (PreparedStatement pstmt = conn.prepareStatement(sqlGetPastoInfo)) { // Prepara lo statement.
                pstmt.setInt(1, entry.getPastoId()); // Imposta l'ID del pasto.
                ResultSet rs = pstmt.executeQuery(); // Esegue la query.
                if (rs.next()) { // Se c'è un risultato.
                    tipo = rs.getString("tipo"); // Ottiene il tipo.
                    idElemento = rs.getInt("id_elemento"); // Ottiene l'ID dell'elemento.
                }
            }

            // Ora ottieni i valori nutrizionali base (per 100g per alimenti, per porzione per ricette).
            if ("alimento".equals(tipo)) { // Se il tipo è "alimento".
                String sqlAlimento = "SELECT kcal, proteine, carboidrati, grassi FROM foods WHERE id = ?"; // Query per recuperare macro dell'alimento.
                try (PreparedStatement pstmt = conn.prepareStatement(sqlAlimento)) { // Prepara lo statement.
                    pstmt.setInt(1, idElemento); // Imposta l'ID dell'alimento.
                    ResultSet rs = pstmt.executeQuery(); // Esegue la query.
                    if (rs.next()) { // Se c'è un risultato.
                        baseKcal = rs.getDouble("kcal"); // Ottiene le calorie base.
                        baseProtein = rs.getDouble("proteine"); // Ottiene le proteine base.
                        baseCarb = rs.getDouble("carboidrati"); // Ottiene i carboidrati base.
                        baseFat = rs.getDouble("grassi"); // Ottiene i grassi base.
                    }
                }
            } else if ("ricetta".equals(tipo)) { // Se il tipo è "ricetta".
                String sqlRicetta = "SELECT kcal, proteine, carboidrati, grassi FROM Ricette WHERE id = ?"; // Query per recuperare macro della ricetta.
                try (PreparedStatement pstmt = conn.prepareStatement(sqlRicetta)) { // Prepara lo statement.
                    pstmt.setInt(1, idElemento); // Imposta l'ID della ricetta.
                    ResultSet rs = pstmt.executeQuery(); // Esegue la query.
                    if (rs.next()) { // Se c'è un risultato.
                        baseKcal = rs.getDouble("kcal"); // Ottiene le calorie base.
                        baseProtein = rs.getDouble("proteine"); // Ottiene le proteine base.
                        baseCarb = rs.getDouble("carboidrati"); // Ottiene i carboidrati base.
                        baseFat = rs.getDouble("grassi"); // Ottiene i grassi base.
                    }
                }
            }

            // Calcola i nuovi valori nutrizionali basati sulla `newQuantity`.
            double newCalculatedKcal, newCalculatedProtein, newCalculatedCarb, newCalculatedFat; // Variabili per le nuove macro calcolate.

            if ("alimento".equals(tipo)) {
                // Per alimenti: i valori base sono per 100g, quindi si scala in base alla nuova quantità.
                newCalculatedKcal = baseKcal * (newQuantity / 100.0); // Calcola le nuove calorie.
                newCalculatedProtein = baseProtein * (newQuantity / 100.0); // Calcola le nuove proteine.
                newCalculatedCarb = baseCarb * (newQuantity / 100.0); // Calcola i nuovi carboidrati.
                newCalculatedFat = baseFat * (newQuantity / 100.0); // Calcola i nuovi grassi.
            } else {
                // Per ricette: i valori base sono per porzione, quindi si moltiplica per la nuova quantità.
                newCalculatedKcal = baseKcal * newQuantity; // Calcola le nuove calorie.
                newCalculatedProtein = baseProtein * newQuantity; // Calcola le nuove proteine.
                newCalculatedCarb = baseCarb * newQuantity; // Calcola i nuovi carboidrati.
                newCalculatedFat = baseFat * newQuantity; // Calcola i nuovi grassi.
            }

            // Calcola la differenza (delta) tra i nuovi e i vecchi valori nutrizionali.
            double deltaKcal = newCalculatedKcal - oldKcal; // Delta calorie.
            double deltaProtein = newCalculatedProtein - oldProtein; // Delta proteine.
            double deltaCarb = newCalculatedCarb - oldCarb; // Delta carboidrati.
            double deltaFat = newCalculatedFat - oldFat; // Delta grassi.

            // Aggiorna la quantità del pasto specifico nella tabella 'Pasto' nel database.
            String sqlUpdatePasto = "UPDATE Pasto SET quantita_grammi = ? WHERE id_pasto = ?"; // Query per aggiornare la quantità del pasto.
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdatePasto)) { // Prepara lo statement.
                pstmt.setDouble(1, newQuantity); // Imposta la nuova quantità.
                pstmt.setInt(2, entry.getPastoId()); // Imposta l'ID del pasto.
                int affectedRows = pstmt.executeUpdate(); // Esegue l'aggiornamento.

                if (affectedRows > 0) { // Se l'aggiornamento ha avuto successo.
                    System.out.println("Quantità aggiornata nel DB per Pasto ID: " + entry.getPastoId()); // Messaggio di debug.

                    // Aggiorna i totali nella tabella PastiGiornalieri usando i delta calcolati.
                    updatePastiGiornalieriTotals(entry.getPastoId(), deltaKcal, deltaProtein, deltaCarb, deltaFat); // Aggiorna i totali del giorno.

                    // Ricarica tutti i pasti per il giorno corrente per aggiornare l'interfaccia utente.
                    caricaPasti(dataCorrente); // Ricarica i pasti del giorno.

                    // Aggiorna anche la HomePage se il riferimento al controller è disponibile.
                    if (homePageController != null) { // Se il controller HomePage è disponibile.
                        homePageController.aggiornaLabelKcalPerPasto(); // Chiama il metodo per aggiornare le calorie nella HomePage.
                    }

                    showAlert(Alert.AlertType.INFORMATION,"Successo", "Quantità per '" + entry.getName() + "' aggiornata a " + String.format("%.1f", newQuantity) + "."); // Mostra un alert di successo.
                } else {
                    showAlert(Alert.AlertType.ERROR,"Errore", "Impossibile aggiornare la quantità. Record non trovato."); // Mostra un alert di errore.
                }
            }

        } catch (SQLException e) { // Cattura le eccezioni SQL.
            e.printStackTrace(); // Stampa l'errore SQL.
            showAlert(Alert.AlertType.ERROR,"Errore Database", "Si è verificato un errore durante l'aggiornamento: " + e.getMessage()); // Mostra un alert di errore di database.
        }
    }

    // Metodo FXML chiamato quando si clicca il bottone "Precedente".
    @FXML
    private void onPrecedenteClick() {
        dataCorrente = dataCorrente.minusDays(1); // Decrementa la data corrente di un giorno.
        datePicker.setValue(dataCorrente); // Aggiorna il DatePicker con la nuova data.
        aggiornaDateLabel(dataCorrente); // Aggiorna la label testuale della data.
        caricaPasti(dataCorrente); // Ricarica i pasti per la nuova data dal database.
    }

    // Metodo FXML chiamato quando si clicca il bottone "Successivo".
    @FXML
    private void onSuccessivoClick() {
        dataCorrente = dataCorrente.plusDays(1); // Incrementa la data corrente di un giorno.
        datePicker.setValue(dataCorrente); // Aggiorna il DatePicker con la nuova data.
        aggiornaDateLabel(dataCorrente); // Aggiorna la label testuale della data.
        caricaPasti(dataCorrente); // Ricarica i pasti per la nuova data dal database.
    }

    // Metodo FXML chiamato quando l'utente seleziona una data dal DatePicker.
    @FXML
    private void onDataSelezionata() {
        LocalDate selectedDate = datePicker.getValue(); // Ottiene la data selezionata dal DatePicker.
        if (selectedDate != null) { // Se una data è stata effettivamente selezionata (non è null).
            dataCorrente = selectedDate; // Imposta la data corrente alla data selezionata.
            aggiornaDateLabel(dataCorrente); // Aggiorna la label testuale della data.
            caricaPasti(dataCorrente); // Ricarica i pasti per la data selezionata dal database.
        }
    }


    // Metodo privato per aggiornare la label che mostra la data, con etichette speciali per "Oggi", "Ieri", "Domani".
    // Metodo per aggiornare la label che visualizza la data corrente, includendo "Oggi", "Ieri" o "Domani".
    private void aggiornaDateLabel(LocalDate date) {
        String labelText = ""; // Inizializza la stringa del testo della label.
        if (date.isEqual(LocalDate.now())) { // Controlla se la data è uguale alla data odierna.
            labelText = "Oggi - " + DATE_FORMATTER.format(date); // Imposta il testo come "Oggi - [Data]".
        } else if (date.isEqual(LocalDate.now().minusDays(1))) { // Controlla se la data è uguale a ieri.
            labelText = "Ieri - " + DATE_FORMATTER.format(date); // Imposta il testo come "Ieri - [Data]".
        } else if (date.isEqual(LocalDate.now().plusDays(1))) { // Controlla se la data è uguale a domani.
            labelText = "Domani - " + DATE_FORMATTER.format(date); // Imposta il testo come "Domani - [Data]".
        } else {
            labelText = DATE_FORMATTER.format(date); // Per tutte le altre date, usa il formato standard.
        }
        lblDataCorrente.setText(labelText); // Imposta il testo finale sulla label UI.
    }

    // Metodo principale per caricare tutti i pasti per una data specifica.
    private void caricaPasti(LocalDate date) {
        // Cancella i dati precedenti dalle tabelle e resetta le label delle calorie.
        tableColazione.getItems().clear(); // Pulisce gli elementi dalla tabella della colazione.
        tableSpuntino.getItems().clear(); // Pulisce gli elementi dalla tabella dello spuntino.
        tablePranzo.getItems().clear(); // Pulisce gli elementi dalla tabella del pranzo.
        tableMerenda.getItems().clear(); // Pulisce gli elementi dalla tabella della merenda.
        tableCena.getItems().clear(); // Pulisce gli elementi dalla tabella della cena.

        lblKcalColazione.setText("0 kcal"); // Reimposta le calorie della colazione a 0.
        lblKcalSpuntino.setText("0 kcal"); // Reimposta le calorie dello spuntino a 0.
        lblKcalPranzo.setText("0 kcal"); // Reimposta le calorie del pranzo a 0.
        lblKcalMerenda.setText("0 kcal"); // Reimposta le calorie della merenda a 0.
        lblKcalCena.setText("0 kcal"); // Reimposta le calorie della cena a 0.

        // Inizializza i totali giornalieri a zero.
        double totalKcal = 0; // Inizializza le calorie totali del giorno.
        double totalProtein = 0; // Inizializza le proteine totali del giorno.
        double totalCarbs = 0; // Inizializza i carboidrati totali del giorno.
        double totalFats = 0; // Inizializza i grassi totali del giorno.

        String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE); // Formatta la data per la query SQL (es. YYYY-MM-DD).
        // Query per recuperare i record dalla tabella PastiGiornalieri per l'utente e la data specifici.
        String sqlPastiGiornalieri = "SELECT * FROM PastiGiornalieri WHERE id_cliente = ? AND data = ?"; // Query SQL.

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(sqlPastiGiornalieri)) { // Prepara lo statement SQL.

            pstmt.setInt(1, Session.getUserId()); // Imposta l'ID del cliente (dalla sessione utente).
            pstmt.setString(2, dateString); // Imposta la data nel formato stringa.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query e ottiene i risultati.

            // Mappe per memorizzare i dati dei pasti e i totali di calorie per ciascun tipo di pasto.
            Map<String, ObservableList<PastoSpecifico>> mealData = new HashMap<>(); // Mappa che conterrà le liste di PastoSpecifico per ogni pasto.
            mealData.put("Colazione", FXCollections.observableArrayList()); // Inizializza lista per la Colazione.
            mealData.put("Spuntino", FXCollections.observableArrayList()); // Inizializza lista per lo Spuntino.
            mealData.put("Pranzo", FXCollections.observableArrayList()); // Inizializza lista per il Pranzo.
            mealData.put("Merenda", FXCollections.observableArrayList()); // Inizializza lista per la Merenda.
            mealData.put("Cena", FXCollections.observableArrayList()); // Inizializza lista per la Cena.

            Map<String, Double> mealKcalTotals = new HashMap<>(); // Mappa per tenere traccia dei totali di calorie per ogni pasto.
            mealKcalTotals.put("Colazione", 0.0); // Inizializza calorie Colazione a 0.0.
            mealKcalTotals.put("Spuntino", 0.0); // Inizializza calorie Spuntino a 0.0.
            mealKcalTotals.put("Pranzo", 0.0); // Inizializza calorie Pranzo a 0.0.
            mealKcalTotals.put("Merenda", 0.0); // Inizializza calorie Merenda a 0.0.
            mealKcalTotals.put("Cena", 0.0); // Inizializza calorie Cena a 0.0.

            // Itera sui risultati dalla tabella PastiGiornalieri (che contiene i riepiloghi per tipo di pasto).
            while (rs.next()) { // Finchè ci sono righe nel ResultSet.
                PastoGiornaliero pg = new PastoGiornaliero( // Crea un oggetto PastoGiornaliero.
                        rs.getInt("id_pasti_giornaliero"), // Ottiene l'ID del pasto giornaliero.
                        rs.getInt("id_cliente"), // Ottiene l'ID del cliente.
                        rs.getInt("id_giorno_dieta"), // Ottiene l'ID del giorno dieta.
                        LocalDate.parse(rs.getString("data")), // Parsa la stringa della data in LocalDate.
                        rs.getString("pasto"), // Ottiene il tipo di pasto (es. "Colazione").
                        rs.getDouble("kcal"), // Ottiene le calorie totali per quel pasto.
                        rs.getDouble("proteine"), // Ottiene le proteine totali per quel pasto.
                        rs.getDouble("carboidrati"), // Ottiene i carboidrati totali per quel pasto.
                        rs.getDouble("grassi") // Ottiene i grassi totali per quel pasto.
                );

                // Aggiorna i totali individuali delle calorie per tipo di pasto basandosi sulla tabella PastiGiornalieri.
                mealKcalTotals.merge(pg.getPasto(), pg.getKcal(), Double::sum); // Somma le kcal per il tipo di pasto.


                // Recupera le singole voci di pasto associate a questo PastoGiornaliero.
                // È fondamentale recuperare `id_pasto` qui per abilitare la cancellazione/modifica.
                String sqlPasto = "SELECT id_pasto, tipo, id_elemento, quantita_grammi FROM Pasto WHERE id_pasti_giornaliero = ?"; // Query per le voci singole del pasto.
                try (PreparedStatement pstmtPasto = conn.prepareStatement(sqlPasto)) { // Prepara lo statement.
                    pstmtPasto.setInt(1, pg.getIdPastiGiornaliero()); // Imposta l'ID del pasto giornaliero.
                    ResultSet rsPasto = pstmtPasto.executeQuery(); // Esegue la query.

                    while (rsPasto.next()) { // Itera sulle singole voci di pasto.
                        Pasto pasto = new Pasto( // Crea un oggetto Pasto.
                                rsPasto.getInt("id_pasto"), // Importante: ottieni id_pasto qui per le operazioni di modifica/eliminazione.
                                pg.getIdPastiGiornaliero(), // ID del PastoGiornaliero a cui appartiene.
                                rsPasto.getString("tipo"), // Tipo dell'elemento (alimento o ricetta).
                                rsPasto.getInt("id_elemento"), // ID dell'alimento o ricetta.
                                rsPasto.getDouble("quantita_grammi") // Quantità in grammi o porzioni.
                        );

                        PastoSpecifico entry = getPastoSpecificoDetails(conn, pasto); // Ottiene i dettagli nutrizionali dell'alimento/ricetta.
                        if (entry != null) { // Se l'entry non è null.
                            entry.setPastoId(pasto.getIdPasto()); // Assegna l'ID del Pasto all'oggetto PastoSpecifico.
                            mealData.get(pg.getPasto()).add(entry); // Aggiunge l'elemento alla lista del pasto corrispondente.
                        }
                    }
                }
            }

            // Popola le TableView con i dati raccolti.
            tableColazione.setItems(mealData.get("Colazione")); // Imposta gli elementi per la tabella della colazione.
            tableSpuntino.setItems(mealData.get("Spuntino")); // Imposta gli elementi per la tabella dello spuntino.
            tablePranzo.setItems(mealData.get("Pranzo")); // Imposta gli elementi per la tabella del pranzo.
            tableMerenda.setItems(mealData.get("Merenda")); // Imposta gli elementi per la tabella della merenda.
            tableCena.setItems(mealData.get("Cena")); // Imposta gli elementi per la tabella della cena.

            // Aggiorna le label delle calorie per ogni pasto.
            lblKcalColazione.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Colazione")))); // Formatta e imposta le calorie della colazione.
            lblKcalSpuntino.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Spuntino")))); // Formatta e imposta le calorie dello spuntino.
            lblKcalPranzo.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Pranzo")))); // Formatta e imposta le calorie del pranzo.
            lblKcalMerenda.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Merenda")))); // Formatta e imposta le calorie della merenda.
            lblKcalCena.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Cena")))); // Formatta e imposta le calorie della cena.


            // Calcola i totali giornalieri complessivi sommando i valori delle singole voci dei pasti.
            // Questo fornisce una somma più accurata basata sulle quantità reali.
            totalKcal = 0; // Reimposta le calorie totali a 0 per un ricalcolo basato sugli elementi.
            totalProtein = 0; // Reimposta le proteine totali a 0.
            totalCarbs = 0; // Reimposta i carboidrati totali a 0.
            totalFats = 0; // Reimposta i grassi totali a 0.

            for (ObservableList<PastoSpecifico> entries : mealData.values()) { // Itera su tutte le liste di PastoSpecifico (tutti i pasti).
                for (PastoSpecifico entry : entries) { // Itera su ogni singolo elemento del pasto.
                    totalKcal += entry.getKcal(); // Aggiunge le calorie dell'elemento al totale.
                    totalProtein += entry.getProtein(); // Aggiunge le proteine dell'elemento al totale.
                    totalCarbs += entry.getCarb(); // Aggiunge i carboidrati dell'elemento al totale.
                    totalFats += entry.getFat(); // Aggiunge i grassi dell'elemento al totale.
                }
            }

        } catch (SQLException e) { // Cattura le eccezioni SQL.
            e.printStackTrace(); // Stampa l'errore SQL.
            showAlert(Alert.AlertType.ERROR,"Errore", "Caricamento fallito" + e.getMessage()); // Mostra un alert di errore.
        }

        // Aggiorna il riepilogo nutrizionale giornaliero complessivo.
        lblCalorieTotali.setText(String.format("%.0f kcal", fixNegativeZero(totalKcal))); // Aggiorna la label delle calorie totali.
        lblProteineTotali.setText(String.format("%.1fg", fixNegativeZero(totalProtein))); // Aggiorna la label delle proteine totali.
        lblCarboidratiTotali.setText(String.format("%.1fg", fixNegativeZero(totalCarbs))); // Aggiorna la label dei carboidrati totali.
        lblGrassiTotali.setText(String.format("%.1fg", fixNegativeZero(totalFats))); // Aggiorna la label dei grassi totali.
    }

    // Metodo privato per ottenere i dettagli di un PastoSpecifico (nome, macro) dal database.
    private PastoSpecifico getPastoSpecificoDetails(Connection conn, Pasto pasto) throws SQLException {
        String name = ""; // Nome dell'alimento/ricetta, inizializzato a stringa vuota.
        double baseKcal = 0, baseProtein = 0, baseCarb = 0, baseFat = 0; // Macro per 100g/porzione, inizializzate a 0.

        if ("alimento".equals(pasto.getTipo())) { // Se il pasto è un alimento.
            String sqlAlimento = "SELECT nome, kcal, proteine, carboidrati, grassi FROM foods WHERE id = ?"; // Query per recuperare dettagli alimento.
            try (PreparedStatement pstmt = conn.prepareStatement(sqlAlimento)) { // Prepara lo statement.
                pstmt.setInt(1, pasto.getIdElemento()); // Imposta l'ID dell'alimento.
                ResultSet rs = pstmt.executeQuery(); // Esegue la query.
                if (rs.next()) { // Se c'è un risultato.
                    name = rs.getString("nome"); // Ottiene il nome.
                    baseKcal = rs.getDouble("kcal");        // Valori per 100g.
                    baseProtein = rs.getDouble("proteine");
                    baseCarb = rs.getDouble("carboidrati");
                    baseFat = rs.getDouble("grassi");
                }
            }

            // Calcola i valori nutrizionali in base alla quantità in grammi per gli alimenti (valori base sono per 100g).
            if (!name.isEmpty()) { // Se il nome non è vuoto.
                double calculatedKcal = baseKcal * (pasto.getQuantitaGrammi() / 100.0); // Calcola le calorie.
                double calculatedProtein = baseProtein * (pasto.getQuantitaGrammi() / 100.0); // Calcola le proteine.
                double calculatedCarb = baseCarb * (pasto.getQuantitaGrammi() / 100.0); // Calcola i carboidrati.
                double calculatedFat = baseFat * (pasto.getQuantitaGrammi() / 100.0); // Calcola i grassi.

                // Restituisce un nuovo oggetto PastoSpecifico con i valori calcolati.
                return new PastoSpecifico(name, pasto.getQuantitaGrammi(), calculatedKcal, calculatedProtein, calculatedCarb, calculatedFat);
            }

        } else if ("ricetta".equals(pasto.getTipo())) { // Se il pasto è una ricetta.
            String sqlRicetta = "SELECT nome, kcal, proteine, carboidrati, grassi FROM Ricette WHERE id = ?"; // Query per recuperare dettagli ricetta.
            try (PreparedStatement pstmt = conn.prepareStatement(sqlRicetta)) { // Prepara lo statement.
                pstmt.setInt(1, pasto.getIdElemento()); // Imposta l'ID della ricetta.
                ResultSet rs = pstmt.executeQuery(); // Esegue la query.
                if (rs.next()) { // Se c'è un risultato.
                    name = rs.getString("nome"); // Ottiene il nome.
                    baseKcal = rs.getDouble("kcal");        // Valori per porzione.
                    baseProtein = rs.getDouble("proteine");
                    baseCarb = rs.getDouble("carboidrati");
                    baseFat = rs.getDouble("grassi");
                }
            }

            // Calcola i valori nutrizionali in base alla quantità (numero di porzioni) per le ricette.
            if (!name.isEmpty()) { // Se il nome non è vuoto.
                double calculatedKcal = baseKcal * pasto.getQuantitaGrammi(); // Qui getQuantitaGrammi() è in realtà il numero di porzioni.
                double calculatedProtein = baseProtein * pasto.getQuantitaGrammi();
                double calculatedCarb = baseCarb * pasto.getQuantitaGrammi();
                double calculatedFat = baseFat * pasto.getQuantitaGrammi();

                // Restituisce un nuovo oggetto PastoSpecifico con i valori calcolati.
                return new PastoSpecifico(name, pasto.getQuantitaGrammi(), calculatedKcal, calculatedProtein, calculatedCarb, calculatedFat);
            }
        }
        return null; // Restituisce null se non è stato possibile recuperare i dettagli.
    }

    // Metodo privato per gestire valori negativi molto piccoli (es. -0.0) e visualizzarli come 0.
    private double fixNegativeZero(double value) {
        return (value > -0.0001 && value < 0.0) ? 0.0 : value; // Se il valore è un "zero negativo" o molto vicino a zero negativo, restituisce 0.0, altrimenti il valore originale.
    }

    // Metodo privato per visualizzare gli alert.
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType); // Crea una nuova istanza di Alert.
        alert.setTitle(title); // Imposta il titolo.
        alert.setHeaderText(null); // Non mostra un header text.
        alert.setContentText(message); // Imposta il contenuto.

        // Carica e applica il foglio di stile CSS personalizzato per gli alert.
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css"); // Ottiene l'URL del file CSS.
        if (cssUrl != null) { // Se l'URL non è nullo (il file CSS è stato trovato).
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // Aggiunge il foglio di stile al dialog.
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base.
            // Aggiungi classi di stile specifiche in base al tipo di Alert per personalizzare ulteriormente l'aspetto.
            if (alertType == Alert.AlertType.INFORMATION) { // Se è un alert di tipo INFORMAZIONE.
                alert.getDialogPane().getStyleClass().add("alert-information"); // Aggiunge la classe specifica.
            } else if (alertType == Alert.AlertType.WARNING) { // Se è un alert di tipo WARNING.
                alert.getDialogPane().getStyleClass().add("alert-warning"); // Aggiunge la classe specifica.
            } else if (alertType == Alert.AlertType.ERROR) { // Se è un alert di tipo ERROR.
                alert.getDialogPane().getStyleClass().add("alert-error"); // Aggiunge la classe specifica.
            } else if (alertType == Alert.AlertType.CONFIRMATION) { // Se è un alert di tipo CONFIRMATION.
                alert.getDialogPane().getStyleClass().add("alert-confirmation"); // Aggiunge la classe specifica.
            }
        } else {
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Messaggio di errore se il CSS non è stato trovato.
        }

        alert.showAndWait(); // Mostra l'alert e attende che l'utente lo chiuda.
    }
}
