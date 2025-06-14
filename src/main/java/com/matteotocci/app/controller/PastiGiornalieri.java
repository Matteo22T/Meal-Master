package com.matteotocci.app.controller; // Dichiarazione del package in cui si trova la classe del controller.

// Importazioni delle classi e interfacce necessarie da altri package.
import com.matteotocci.app.model.*; // Importa tutte le classi dal package model (Session, Dieta, SQLiteConnessione, PastoSpecifico, Pasto, PastoGiornaliero).
import javafx.beans.property.SimpleStringProperty; // Classe per la gestione di proprietà di stringa semplici, usate per la TableView.
import javafx.collections.FXCollections; // Utility per creare ObservableList.
import javafx.collections.ObservableList; // Interfaccia per liste che possono essere osservate da JavaFX per aggiornamenti UI.
import javafx.event.ActionEvent; // Classe per la gestione degli eventi di azione (es. click su un bottone).
import javafx.fxml.FXML; // Annotazione per iniettare componenti FXML nel controller.
import javafx.fxml.Initializable; // Interfaccia che i controller devono implementare per l'inizializzazione dopo il caricamento FXML.
import javafx.geometry.Pos; // Enum per specificare la posizione di un nodo all'interno di un layout.
import javafx.scene.Node; // Classe base per tutti i nodi nel grafo di scena (es. controlli UI).
import javafx.scene.control.*; // Importa tutti i controlli UI di JavaFX (Button, Label, DatePicker, TableView, TableColumn, Alert, TextInputDialog, TableCell).
import javafx.scene.layout.HBox; // Layout container che organizza i suoi figli in una singola riga orizzontale.
import javafx.stage.Stage; // Finestra principale dell'applicazione.
import javafx.util.Callback; // Interfaccia generica di callback, usata per le cell factory delle TableColumn.
import javafx.util.StringConverter; // Interfaccia per convertire oggetti da e verso stringhe, usata per il DatePicker.

import java.net.URL; // Classe per rappresentare un URL (usata per caricare risorse come i CSS).
import java.sql.Connection; // Interfaccia per la connessione al database.
import java.sql.PreparedStatement; // Interfaccia per l'esecuzione di query SQL precompilate.
import java.sql.ResultSet; // Interfaccia per la gestione dei risultati di una query SQL.
import java.time.LocalDate; // Classe per rappresentare una data.
import java.time.format.DateTimeFormatter; // Classe per formattare e parsare date.
import java.util.HashMap; // Implementazione della mappa basata su hash table.
import java.util.Map; // Interfaccia per una mappa chiave-valore.
import java.util.Optional; // Classe contenitore che può o non può contenere un valore non nullo.
import java.sql.SQLException; // Classe per la gestione delle eccezioni SQL.
import java.util.ResourceBundle; // Utilizzato per la localizzazione (non strettamente usato in questo codice, ma richiesto da Initializable).


// Dichiarazione della classe PastiGiornalieri, che implementa Initializable per l'inizializzazione dei componenti.
public class PastiGiornalieri implements Initializable {
    // Campi annotati con @FXML per l'iniezione dei componenti UI definiti nel file FXML.
    @FXML private Button btnPrecedente; // Bottone per navigare al giorno precedente.
    @FXML private Button btnSuccessivo; // Bottone per navigare al giorno successivo.
    @FXML private Label lblDataCorrente; // Label per visualizzare la data corrente (es. "Oggi - 13/06/2025").
    @FXML private DatePicker datePicker; // Componente per selezionare una data.
    @FXML private Label lblCalorieTotali; // Label per visualizzare le calorie totali del giorno.
    @FXML private Label lblProteineTotali; // Label per visualizzare le proteine totali del giorno.
    @FXML private Label lblCarboidratiTotali; // Label per visualizzare i carboidrati totali del giorno.
    @FXML private Label lblGrassiTotali; // Label per visualizzare i grassi totali del giorno.

    // Campi FXML per le tabelle e le label delle calorie per ogni pasto (Colazione, Spuntino, Pranzo, Merenda, Cena).
    @FXML private Label lblKcalColazione;
    @FXML private TableView<PastoSpecifico> tableColazione;
    @FXML private TableColumn<PastoSpecifico, String> colColazioneAlimento;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneQuantita;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneKcal;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneProteine;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneCarboidrati;
    @FXML private TableColumn<PastoSpecifico, Double> colColazioneGrassi;
    @FXML private TableColumn<PastoSpecifico, Void> colColazioneAzioni; // Colonna per i bottoni di azione (elimina, modifica).

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
        alert.setTitle("Conferma Eliminazione");
        alert.setHeaderText("Eliminare l'alimento/ricetta?");
        alert.setContentText("Sei sicuro di voler eliminare '" + entry.getName() + "' da " + mealType + "?");

        Optional<ButtonType> result = alert.showAndWait(); // Mostra l'alert e attende la risposta.
        if (result.isPresent() && result.get() == ButtonType.OK) { // Se l'utente conferma l'eliminazione.

            // Tenta di eliminare il pasto dal database.
            try (Connection conn = SQLiteConnessione.connector();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Pasto WHERE id_pasto = ?")) {
                // Prima di eliminare il pasto, aggiorna i totali del PastoGiornaliero sottraendo le sue macro.
                updatePastiGiornalieriTotals(entry.getPastoId(), -entry.getKcal(), -entry.getProtein(), -entry.getCarb(), -entry.getFat());
                pstmt.setInt(1, entry.getPastoId()); // Imposta l'ID del pasto da eliminare.
                int affectedRows = pstmt.executeUpdate(); // Esegue l'eliminazione.

                if (affectedRows > 0) { // Se la riga è stata eliminata con successo.
                    System.out.println("Record eliminato con successo dal DB.");
                    caricaPasti(dataCorrente); // Ricarica tutti i pasti per aggiornare l'interfaccia utente.
                    homePageController.aggiornaLabelKcalPerPasto(); // Aggiorna le label nella HomePage.
                    showAlert(Alert.AlertType.INFORMATION,"Eliminato!", "'" + entry.getName() + "' eliminato con successo.");
                } else {
                    showAlert(Alert.AlertType.ERROR,"Errore Eliminazione", "Impossibile eliminare '" + entry.getName() + "'. Record non trovato nel database.");
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Stampa l'errore SQL.
                showAlert(Alert.AlertType.ERROR,"Errore Database", "Si è verificato un errore durante l'eliminazione: " + e.getMessage());
            }
        }
    }

    // Metodo per aggiornare i totali di calorie, proteine, carboidrati e grassi nella tabella PastiGiornalieri.
    private void updatePastiGiornalieriTotals(int pastoId, double deltaKcal, double deltaProtein, double deltaCarb, double deltaFat) {
        int idPastiGiornaliero = -1;

        // Trova l'id_pasti_giornaliero associato all'id_pasto.
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

        if (idPastiGiornaliero != -1) { // Se l'id_pasti_giornaliero è stato trovato.
            // Aggiorna i totali nella tabella PastiGiornalieri sommando i delta (che possono essere positivi o negativi).
            String sqlUpdatePg = "UPDATE PastiGiornalieri SET kcal = kcal + ?, proteine = proteine + ?, carboidrati = carboidrati + ?, grassi = grassi + ? WHERE id_pasti_giornaliero = ?";
            try (Connection conn = SQLiteConnessione.connector();
                 PreparedStatement pstmt = conn.prepareStatement(sqlUpdatePg)) {
                pstmt.setDouble(1, deltaKcal);
                pstmt.setDouble(2, deltaProtein);
                pstmt.setDouble(3, deltaCarb);
                pstmt.setDouble(4, deltaFat);
                pstmt.setInt(5, idPastiGiornaliero);
                pstmt.executeUpdate(); // Esegue l'aggiornamento.
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

    // Metodo per gestire la modifica della quantità di un PastoSpecifico.
    private void handleEditPastoSpecifico(PastoSpecifico entry, String mealType) {
        System.out.println("Modifica " + entry.getName() + " in " + mealType);
        // Crea un dialog per chiedere la nuova quantità, pre-compilando con la quantità attuale.
        TextInputDialog dialog = new TextInputDialog(String.valueOf(entry.getQuantity()));
        dialog.setTitle("Modifica Quantità");
        dialog.setHeaderText("Modifica la quantità per " + entry.getName() + " nel pasto " + mealType);
        dialog.setContentText("Nuova quantità (grammi/porzioni):");

        // Applica gli stili CSS al dialog.
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        // Mostra il dialog e attende la risposta dell'utente.
        Optional<String> result = dialog.showAndWait();

        // Elabora la risposta dell'utente.
        result.ifPresent(newQuantityString -> {
            try {
                double newQuantity = Double.parseDouble(newQuantityString); // Tenta di convertire l'input in double.

                if (newQuantity <= 0) { // Validazione: la quantità deve essere positiva.
                    showAlert(Alert.AlertType.ERROR,"Quantità non valida", "La quantità deve essere un numero positivo.");
                    return;
                }

                // Confronta con la vecchia quantità per evitare aggiornamenti inutili.
                if (newQuantity == entry.getQuantity()) {
                    showAlert(Alert.AlertType.ERROR,"Nessuna modifica", "La quantità non è cambiata.");
                    return;
                }

                // Chiama il metodo per aggiornare il database con la nuova quantità.
                updatePastoQuantity(entry, newQuantity);

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR,"Input non valido", "Per favore, inserisci un numero valido per la quantità.");
            }
        });
    }

    // Metodo per aggiornare la quantità di un pasto nel database e ricalcolare le macro.
    private void updatePastoQuantity(PastoSpecifico entry, double newQuantity) {
        // Memorizza le vecchie macro e kcal prima dell'aggiornamento.
        double oldKcal = entry.getKcal();
        double oldProtein = entry.getProtein();
        double oldCarb = entry.getCarb();
        double oldFat = entry.getFat();

        double baseKcal = 0, baseProtein = 0, baseCarb = 0, baseFat = 0; // Valori nutrizionali per 100g/porzione.

        try (Connection conn = SQLiteConnessione.connector()) {
            // Prima, recupera il tipo (alimento/ricetta) e l'id_elemento dal database per il pasto specifico.
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

            // Ora ottieni i valori nutrizionali base (per 100g per alimenti, per porzione per ricette).
            if ("alimento".equals(tipo)) {
                String sqlAlimento = "SELECT kcal, proteine, carboidrati, grassi FROM foods WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlAlimento)) {
                    pstmt.setInt(1, idElemento);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        baseKcal = rs.getDouble("kcal");
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
                        baseKcal = rs.getDouble("kcal");
                        baseProtein = rs.getDouble("proteine");
                        baseCarb = rs.getDouble("carboidrati");
                        baseFat = rs.getDouble("grassi");
                    }
                }
            }

            // Calcola i nuovi valori nutrizionali basati sulla `newQuantity`.
            double newCalculatedKcal, newCalculatedProtein, newCalculatedCarb, newCalculatedFat;

            if ("alimento".equals(tipo)) {
                // Per alimenti: i valori base sono per 100g, quindi si scala in base alla nuova quantità.
                newCalculatedKcal = baseKcal * (newQuantity / 100.0);
                newCalculatedProtein = baseProtein * (newQuantity / 100.0);
                newCalculatedCarb = baseCarb * (newQuantity / 100.0);
                newCalculatedFat = baseFat * (newQuantity / 100.0);
            } else {
                // Per ricette: i valori base sono per porzione, quindi si moltiplica per la nuova quantità.
                newCalculatedKcal = baseKcal * newQuantity;
                newCalculatedProtein = baseProtein * newQuantity;
                newCalculatedCarb = baseCarb * newQuantity;
                newCalculatedFat = baseFat * newQuantity;
            }

            // Calcola la differenza (delta) tra i nuovi e i vecchi valori nutrizionali.
            double deltaKcal = newCalculatedKcal - oldKcal;
            double deltaProtein = newCalculatedProtein - oldProtein;
            double deltaCarb = newCalculatedCarb - oldCarb;
            double deltaFat = newCalculatedFat - oldFat;

            // Aggiorna la quantità del pasto specifico nella tabella 'Pasto' nel database.
            String sqlUpdatePasto = "UPDATE Pasto SET quantita_grammi = ? WHERE id_pasto = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdatePasto)) {
                pstmt.setDouble(1, newQuantity);
                pstmt.setInt(2, entry.getPastoId());
                int affectedRows = pstmt.executeUpdate(); // Esegue l'aggiornamento.

                if (affectedRows > 0) { // Se l'aggiornamento ha avuto successo.
                    System.out.println("Quantità aggiornata nel DB per Pasto ID: " + entry.getPastoId());

                    // Aggiorna i totali nella tabella PastiGiornalieri usando i delta calcolati.
                    updatePastiGiornalieriTotals(entry.getPastoId(), deltaKcal, deltaProtein, deltaCarb, deltaFat);

                    // Ricarica tutti i pasti per il giorno corrente per aggiornare l'interfaccia utente.
                    caricaPasti(dataCorrente);

                    // Aggiorna anche la HomePage se il riferimento al controller è disponibile.
                    if (homePageController != null) {
                        homePageController.aggiornaLabelKcalPerPasto();
                    }

                    showAlert(Alert.AlertType.INFORMATION,"Successo", "Quantità per '" + entry.getName() + "' aggiornata a " + String.format("%.1f", newQuantity) + ".");
                } else {
                    showAlert(Alert.AlertType.ERROR,"Errore", "Impossibile aggiornare la quantità. Record non trovato.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL.
            showAlert(Alert.AlertType.ERROR,"Errore Database", "Si è verificato un errore durante l'aggiornamento: " + e.getMessage());
        }
    }

    // Metodo FXML chiamato quando si clicca il bottone "Precedente".
    @FXML
    private void onPrecedenteClick() {
        dataCorrente = dataCorrente.minusDays(1); // Decrementa la data di un giorno.
        datePicker.setValue(dataCorrente); // Aggiorna il DatePicker.
        aggiornaDateLabel(dataCorrente); // Aggiorna la label testuale della data.
        caricaPasti(dataCorrente); // Ricarica i pasti per la nuova data.
    }

    // Metodo FXML chiamato quando si clicca il bottone "Successivo".
    @FXML
    private void onSuccessivoClick() {
        dataCorrente = dataCorrente.plusDays(1); // Incrementa la data di un giorno.
        datePicker.setValue(dataCorrente); // Aggiorna il DatePicker.
        aggiornaDateLabel(dataCorrente); // Aggiorna la label testuale della data.
        caricaPasti(dataCorrente); // Ricarica i pasti per la nuova data.
    }

    // Metodo FXML chiamato quando l'utente seleziona una data dal DatePicker.
    @FXML
    private void onDataSelezionata() {
        LocalDate selectedDate = datePicker.getValue(); // Ottiene la data selezionata.
        if (selectedDate != null) { // Se una data è stata effettivamente selezionata.
            dataCorrente = selectedDate; // Imposta la data corrente alla data selezionata.
            aggiornaDateLabel(dataCorrente); // Aggiorna la label testuale della data.
            caricaPasti(dataCorrente); // Ricarica i pasti per la data selezionata.
        }
    }

    // Metodo privato per aggiornare la label che mostra la data, con etichette speciali per "Oggi", "Ieri", "Domani".
    private void aggiornaDateLabel(LocalDate date) {
        String labelText = "";
        if (date.isEqual(LocalDate.now())) {
            labelText = "Oggi - " + DATE_FORMATTER.format(date);
        } else if (date.isEqual(LocalDate.now().minusDays(1))) {
            labelText = "Ieri - " + DATE_FORMATTER.format(date);
        } else if (date.isEqual(LocalDate.now().plusDays(1))) {
            labelText = "Domani - " + DATE_FORMATTER.format(date);
        } else {
            labelText = DATE_FORMATTER.format(date); // Formato standard per altre date.
        }
        lblDataCorrente.setText(labelText); // Imposta il testo della label.
    }

    // Metodo principale per caricare tutti i pasti per una data specifica.
    private void caricaPasti(LocalDate date) {
        // Cancella i dati precedenti dalle tabelle e resetta le label delle calorie.
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

        // Inizializza i totali giornalieri a zero.
        double totalKcal = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFats = 0;

        String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE); // Formatta la data per la query SQL.
        // Query per recuperare i record dalla tabella PastiGiornalieri per l'utente e la data specifici.
        String sqlPastiGiornalieri = "SELECT * FROM PastiGiornalieri WHERE id_cliente = ? AND data = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sqlPastiGiornalieri)) {

            pstmt.setInt(1, Session.getUserId()); // Imposta l'ID del cliente.
            pstmt.setString(2, dateString); // Imposta la data.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query.

            // Mappe per memorizzare i dati dei pasti e i totali di calorie per ciascun tipo di pasto.
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

            // Itera sui risultati dalla tabella PastiGiornalieri (che contiene i riepiloghi per tipo di pasto).
            while (rs.next()) {
                PastoGiornaliero pg = new PastoGiornaliero(
                        rs.getInt("id_pasti_giornaliero"),
                        rs.getInt("id_cliente"),
                        rs.getInt("id_giorno_dieta"),
                        LocalDate.parse(rs.getString("data")),
                        rs.getString("pasto"), // Tipo di pasto (es. "Colazione").
                        rs.getDouble("kcal"),
                        rs.getDouble("proteine"),
                        rs.getDouble("carboidrati"),
                        rs.getDouble("grassi")
                );

                // Aggiorna i totali individuali delle calorie per tipo di pasto basandosi sulla tabella PastiGiornalieri.
                mealKcalTotals.merge(pg.getPasto(), pg.getKcal(), Double::sum);


                // Recupera le singole voci di pasto associate a questo PastoGiornaliero.
                // È fondamentale recuperare `id_pasto` qui per abilitare la cancellazione/modifica.
                String sqlPasto = "SELECT id_pasto, tipo, id_elemento, quantita_grammi FROM Pasto WHERE id_pasti_giornaliero = ?";
                try (PreparedStatement pstmtPasto = conn.prepareStatement(sqlPasto)) {
                    pstmtPasto.setInt(1, pg.getIdPastiGiornaliero());
                    ResultSet rsPasto = pstmtPasto.executeQuery();

                    while (rsPasto.next()) { // Itera sulle singole voci di pasto.
                        Pasto pasto = new Pasto(
                                rsPasto.getInt("id_pasto"), // Importante: ottieni id_pasto qui.
                                pg.getIdPastiGiornaliero(),
                                rsPasto.getString("tipo"),
                                rsPasto.getInt("id_elemento"),
                                rsPasto.getDouble("quantita_grammi")
                        );

                        PastoSpecifico entry = getPastoSpecificoDetails(conn, pasto); // Ottiene i dettagli nutrizionali dell'alimento/ricetta.
                        if (entry != null) {
                            entry.setPastoId(pasto.getIdPasto()); // Assegna l'ID del Pasto all'oggetto PastoSpecifico.
                            mealData.get(pg.getPasto()).add(entry); // Aggiunge l'elemento alla lista del pasto corrispondente.
                        }
                    }
                }
            }

            // Popola le TableView con i dati raccolti.
            tableColazione.setItems(mealData.get("Colazione"));
            tableSpuntino.setItems(mealData.get("Spuntino"));
            tablePranzo.setItems(mealData.get("Pranzo"));
            tableMerenda.setItems(mealData.get("Merenda"));
            tableCena.setItems(mealData.get("Cena"));

            // Aggiorna le label delle calorie per ogni pasto.
            lblKcalColazione.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Colazione"))));
            lblKcalSpuntino.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Spuntino"))));
            lblKcalPranzo.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Pranzo"))));
            lblKcalMerenda.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Merenda"))));
            lblKcalCena.setText(String.format("%.0f kcal", fixNegativeZero(mealKcalTotals.get("Cena"))));


            // Calcola i totali giornalieri complessivi sommando i valori delle singole voci dei pasti.
            // Questo fornisce una somma più accurata basata sulle quantità reali.
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
            e.printStackTrace(); // Stampa l'errore SQL.
            showAlert(Alert.AlertType.ERROR,"Errore", "Caricamento fallito" + e.getMessage());
        }

        // Aggiorna il riepilogo nutrizionale giornaliero complessivo.
        lblCalorieTotali.setText(String.format("%.0f kcal", fixNegativeZero(totalKcal)));
        lblProteineTotali.setText(String.format("%.1fg", fixNegativeZero(totalProtein)));
        lblCarboidratiTotali.setText(String.format("%.1fg", fixNegativeZero(totalCarbs)));
        lblGrassiTotali.setText(String.format("%.1fg", fixNegativeZero(totalFats)));
    }

    // Metodo privato per ottenere i dettagli di un PastoSpecifico (nome, macro) dal database.
    private PastoSpecifico getPastoSpecificoDetails(Connection conn, Pasto pasto) throws SQLException {
        String name = "";
        double baseKcal = 0, baseProtein = 0, baseCarb = 0, baseFat = 0;

        if ("alimento".equals(pasto.getTipo())) { // Se il pasto è un alimento.
            String sqlAlimento = "SELECT nome, kcal, proteine, carboidrati, grassi FROM foods WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlAlimento)) {
                pstmt.setInt(1, pasto.getIdElemento());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    name = rs.getString("nome");
                    baseKcal = rs.getDouble("kcal");        // Valori per 100g.
                    baseProtein = rs.getDouble("proteine");
                    baseCarb = rs.getDouble("carboidrati");
                    baseFat = rs.getDouble("grassi");
                }
            }

            // Calcola i valori nutrizionali in base alla quantità in grammi per gli alimenti (valori base sono per 100g).
            if (!name.isEmpty()) {
                double calculatedKcal = baseKcal * (pasto.getQuantitaGrammi() / 100.0);
                double calculatedProtein = baseProtein * (pasto.getQuantitaGrammi() / 100.0);
                double calculatedCarb = baseCarb * (pasto.getQuantitaGrammi() / 100.0);
                double calculatedFat = baseFat * (pasto.getQuantitaGrammi() / 100.0);

                // Restituisce un nuovo oggetto PastoSpecifico con i valori calcolati.
                return new PastoSpecifico(name, pasto.getQuantitaGrammi(), calculatedKcal, calculatedProtein, calculatedCarb, calculatedFat);
            }

        } else if ("ricetta".equals(pasto.getTipo())) { // Se il pasto è una ricetta.
            String sqlRicetta = "SELECT nome, kcal, proteine, carboidrati, grassi FROM Ricette WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlRicetta)) {
                pstmt.setInt(1, pasto.getIdElemento());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    name = rs.getString("nome");
                    baseKcal = rs.getDouble("kcal");        // Valori per porzione.
                    baseProtein = rs.getDouble("proteine");
                    baseCarb = rs.getDouble("carboidrati");
                    baseFat = rs.getDouble("grassi");
                }
            }

            // Calcola i valori nutrizionali in base alla quantità (numero di porzioni) per le ricette.
            if (!name.isEmpty()) {
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
        return (value > -0.0001 && value < 0.0) ? 0.0 : value;
    }

    // Metodo privato per visualizzare gli alert.
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Carica e applica il foglio di stile CSS personalizzato per gli alert.
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");
            // Aggiungi classi di stile specifiche in base al tipo di Alert per personalizzare ulteriormente l'aspetto.
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css");
        }

        alert.showAndWait();
    }
}