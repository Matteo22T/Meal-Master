package com.matteotocci.app.controller;

// Importa le classi necessarie per l'applicazione JavaFX e le operazioni del database
import com.matteotocci.app.model.Alimento; // Modello per l'oggetto Alimento
import com.matteotocci.app.model.Ricetta; // Modello per l'oggetto Ricetta
import com.matteotocci.app.model.SQLiteConnessione; // Classe per gestire la connessione al database SQLite
import com.matteotocci.app.model.Session; // Classe per la gestione della sessione utente
import javafx.application.Platform; // Per eseguire codice sul thread di UI di JavaFX
import javafx.collections.FXCollections; // Utility per creare collezioni osservabili
import javafx.collections.ObservableList; // Lista che notifica i "listener" quando avvengono dei cambiamenti
import javafx.event.ActionEvent; // Tipo di evento generato dalle azioni dell'utente (es. click su un bottone)
import javafx.fxml.FXML; // Annotazione per collegare elementi dell'interfaccia utente definiti in FXML al codice Java
import javafx.fxml.FXMLLoader; // Carica file FXML (layout dell'interfaccia utente)
import javafx.fxml.Initializable; // AGGIUNTA: Interfaccia per i controller che devono essere inizializzati dopo il caricamento dell'FXML
import javafx.geometry.Insets; // Per impostare i margini/padding
import javafx.geometry.Orientation; // Per specificare l'orientamento di elementi come le ScrollBar
import javafx.scene.Node; // Classe base per tutti i nodi nel grafo della scena (elementi UI)
import javafx.scene.Parent; // Nodo base per la gerarchia della scena (container di tutti gli elementi UI)
import javafx.scene.Scene; // Contenitore per tutti i contenuti di una scena
import javafx.scene.control.*; // Controlli UI standard di JavaFX (Button, CheckBox, ComboBox, TableView, TextField, Spinner, Label, Alert)
import javafx.scene.control.cell.PropertyValueFactory; // Per collegare le proprietà degli oggetti alle colonne di una TableView
import javafx.scene.input.MouseButton; // Per identificare il bottone del mouse cliccato
import javafx.scene.input.MouseEvent; // Tipo di evento generato da interazioni del mouse
import javafx.scene.layout.HBox; // Layout container orizzontale
import javafx.scene.layout.VBox; // Layout container verticale
import javafx.stage.Stage; // La finestra dell'applicazione

import java.io.IOException; // Eccezione per errori di input/output (es. caricamento file FXML)
import java.net.URL; // Necessario per Initializable
import java.sql.*; // Classi per l'interazione con il database
import java.time.LocalDate; // Per ottenere la data corrente
import java.util.ArrayList; // Implementazione di List
import java.util.List; // Interfaccia per liste
import java.util.ResourceBundle; // Necessario per Initializable

/**
 * Controller per la schermata "Aggiungi Pasto".
 * Questa classe gestisce l'interfaccia utente per permettere all'utente di selezionare
 * alimenti o ricette e aggiungerli a un pasto specifico del giorno corrente,
 * aggiornando i totali nutrizionali e salvando le modifiche nel database.
 */
public class AggiungiPasto implements Initializable { // AGGIUNTA: Implementa l'interfaccia Initializable

    // --- Elementi dell'interfaccia utente (collegati tramite @FXML) ---
    @FXML private Button ButtonCercaAlimento; // Bottone per avviare la ricerca di alimenti
    @FXML private Button ButtonCercaRicetta; // Bottone per avviare la ricerca di ricette
    @FXML private CheckBox CheckBoxAlimenti; // CheckBox per filtrare gli alimenti (es. "Solo i miei")
    @FXML private CheckBox CheckBoxRicette; // CheckBox per filtrare le ricette (es. "Solo le mie")
    @FXML private ComboBox<String> ComboBoxAlimento; // ComboBox per la selezione della categoria degli alimenti
    @FXML private ComboBox<String> ComboBoxRicetta; // ComboBox per la selezione della categoria delle ricette
    @FXML private TableColumn<?, ?> brandCol; // Colonna per il brand dell'alimento
    @FXML private TableColumn<?, ?> calorieCol; // Colonna per le calorie dell'alimento
    @FXML private TableColumn<?, ?> carboidratiCol; // Colonna per i carboidrati dell'alimento
    @FXML private TableColumn<?, ?> categoriaColRic; // Colonna per la categoria della ricetta
    @FXML private Button confermaPastoButton; // Bottone per confermare l'aggiunta di un alimento al pasto
    @FXML private Button confermaPasto2Button; // Bottone per confermare l'aggiunta di una ricetta al pasto
    @FXML private VBox contenitoreAlimentiDieta; // Contenitore VBox per visualizzare gli alimenti già aggiunti al pasto
    @FXML private VBox contenitoreRicetteDieta; // Contenitore VBox per visualizzare le ricette già aggiunte al pasto
    @FXML private TableColumn<?, ?> descrizioneColRic; // Colonna per la descrizione della ricetta
    @FXML private TableColumn<?, ?> fibreCol; // Colonna per le fibre dell'alimento
    @FXML private TableColumn<?, ?> grassiCol; // Colonna per i grassi dell'alimento
    @FXML private TableColumn<?, ?> grassiSatCol; // Colonna per i grassi saturi dell'alimento
    @FXML private TableColumn<?, ?> immagineCol; // Colonna per l'immagine dell'alimento
    @FXML private TableColumn<?, ?> nomeCol; // Colonna per il nome dell'alimento
    @FXML private TableColumn<?, ?> nomeColRic; // Colonna per il nome della ricetta
    @FXML private TableColumn<?, ?> proteineCol; // Colonna per le proteine dell'alimento
    @FXML private Spinner<Integer> quantitaSpinner; // Spinner per selezionare la quantità (in grammi)
    @FXML private TableColumn<?, ?> saleCol; // Colonna per il sale dell'alimento
    @FXML private TableView<Alimento> tableViewAlimenti; // TableView per visualizzare la lista degli alimenti
    @FXML private TableView<Ricetta> tableViewRicette; // TableView per visualizzare la lista delle ricette
    @FXML private TextField textCercaAlimento; // Campo di testo per la ricerca di alimenti
    @FXML private TextField textCercaRicetta; // Campo di testo per la ricerca di ricette
    @FXML private TableColumn<?, ?> zuccheriCol; // Colonna per gli zuccheri dell'alimento

    // --- Variabili di stato interne ---
    private int offset = 0; // Offset per la paginazione delle ricerche (quanti elementi saltare)
    private final int LIMIT = 50; // Numero massimo di elementi da caricare per volta (per paginazione)
    private String pastoCorrente; // Il tipo di pasto (es. "Colazione", "Pranzo") a cui aggiungere l'elemento

    // Dati specifici del pasto e dell'utente
    private int idGiornoDieta; // L'ID del giorno dieta a cui si sta aggiungendo il pasto
    private String tipoPasto; // Il tipo di pasto (es. "Colazione") per cui si sta lavorando
    private int idCliente; // L'ID dell'utente loggato (recuperato da Session.getUserId())
    private String data = LocalDate.now().toString(); // La data corrente per cui si registra il pasto

    // Riferimento al controller HomePage per aggiornare l'interfaccia principale
    private HomePage HomePageController;

    // --- Metodi di inizializzazione e gestione della UI ---

    /**
     * Metodo di inizializzazione chiamato automaticamente da FXMLLoader dopo che
     * tutti gli elementi FXML sono stati caricati e iniettati nel controller.
     * Qui vengono configurate le colonne delle TableView, i listener per i click sulle righe,
     * i listener per le ComboBox e CheckBox, e la logica di caricamento dei dati iniziali e paginazione.
     *
     * @param location L'URL del documento FXML che ha dato origine a questo controller.
     * @param resources Le risorse utilizzate per localizzare gli oggetti radice, o null se la radice non è stata localizzata.
     */
    @Override // ANNOTAZIONE: Indica che questo metodo fa l'override di un metodo dell'interfaccia Initializable
    public void initialize(URL location, ResourceBundle resources) { // MODIFICA: Firma del metodo conforme a Initializable
        // Associa le proprietà dell'oggetto Alimento alle colonne della TableView degli Alimenti
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

        // Associa le proprietà dell'oggetto Ricetta alle colonne della TableView delle Ricette
        nomeColRic.setCellValueFactory(new PropertyValueFactory<>("nome"));
        descrizioneColRic.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        categoriaColRic.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        // Configura il comportamento di double-click sulle righe della tabella degli Alimenti
        tableViewAlimenti.setRowFactory(tv -> {
            TableRow<Alimento> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) { // Se c'è un double-click e la riga non è vuota
                    Alimento alimento = row.getItem(); // Ottieni l'alimento della riga
                    apriDettaglio(alimento); // Apri la finestra di dettaglio per l'alimento
                }
            });
            return row;
        });

        // Configura il comportamento di double-click sulla tabella delle Ricette (gestito da un metodo separato)
        tableViewRicette.setOnMouseClicked(this::apriDettaglioRicetta);

        popolaCategorie(); // Popola le ComboBox delle categorie per alimenti e ricette

        // Configura lo Spinner per la quantità (es. da 1 a 1000, valore iniziale 100)
        quantitaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100));
        quantitaSpinner.setEditable(true); // Permette di editare il valore direttamente

        // --- Listener per la ricerca e il filtro ---
        // Ogni volta che la selezione nella ComboBox Alimento cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca degli alimenti.
        ComboBoxAlimento.setOnAction(e -> {
            offset = 0;
            tableViewAlimenti.getItems().clear();
            cercaAlimenti(textCercaAlimento.getText(), false);
        });

        // Ogni volta che lo stato della CheckBox Alimenti cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca degli alimenti.
        CheckBoxAlimenti.setOnAction(e -> {
            offset = 0;
            tableViewAlimenti.getItems().clear();
            cercaAlimenti(textCercaAlimento.getText(), false);
        });

        // Ogni volta che la selezione nella ComboBox Ricetta cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca delle ricette.
        ComboBoxRicetta.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(textCercaRicetta.getText(), false);
        });

        // Ogni volta che lo stato della CheckBox Ricette cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca delle ricette.
        CheckBoxRicette.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(textCercaRicetta.getText(), false);
        });

        // --- Gestione dello scroll per la paginazione infinita ---
        // Questo codice viene eseguito sul thread di JavaFX UI dopo l'inizializzazione completa
        Platform.runLater(() -> {
            // Ottiene la ScrollBar verticale della tabella alimenti
            ScrollBar scrollBar = getVerticalScrollbar(tableViewAlimenti);
            if (scrollBar != null) {
                // Aggiunge un listener alla proprietà 'value' della scrollbar
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                    // Se il valore della scrollbar raggiunge il massimo (cioè, l'utente ha scrollato fino in fondo)
                    if (newVal.doubleValue() == scrollBar.getMax()) {
                        caricaAltriAlimenti(); // Carica altri alimenti
                    }
                });
            }
            // Ripete la stessa logica per la tabella ricette
            ScrollBar scrollBar2 = getVerticalScrollbar(tableViewRicette);
            if (scrollBar2 != null) {
                scrollBar2.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == scrollBar2.getMax()) {
                        caricaAltreRicette(); // Carica altre ricette
                    }
                });
            }
        });

        // Carica inizialmente gli alimenti e le ricette all'avvio della schermata
        cercaAlimenti("", false);
        cercaRicette("", false);

        // Inizializza i bottoni per lo switch tra alimenti e ricette
        // Applica lo stile di evidenziazione al bottone Alimenti di default
        highlightButton(btnAlimenti, btnRicette);
    }

    /**
     * Imposta il riferimento al controller `HomePage`.
     * Questo permette a `AggiungiPasto` di chiamare metodi su `HomePage`
     * (es. per aggiornare i totali dopo l'aggiunta di un pasto).
     * @param HomePageController L'istanza del controller HomePage.
     */
    public void setHomePageController(HomePage HomePageController) {
        this.HomePageController = HomePageController;
    }

    /**
     * Imposta i dati contestuali per il pasto che si sta configurando.
     * @param idGiornoDieta L'ID del giorno dieta associato.
     * @param tipoPasto Il tipo di pasto (es. "Colazione", "Pranzo").
     * @param idCliente L'ID del cliente loggato.
     */
    public void setPastoData(int idGiornoDieta, String tipoPasto, int idCliente) {
        this.idGiornoDieta = idGiornoDieta;
        this.tipoPasto = tipoPasto;
        this.idCliente = idCliente;
        System.out.println("AggiungiPastoController - GiornoDieta ID: " + idGiornoDieta + ", Tipo Pasto: " + tipoPasto + ", Cliente ID: " + idCliente);
        // Inizializza il contenuto del VBox con gli alimenti/ricette già registrati per questo pasto
        inizializzaContenutoDieta(idGiornoDieta, tipoPasto);
    }

    /**
     * Gestisce l'azione di conferma per aggiungere un **alimento** al pasto.
     * Calcola i valori nutrizionali, aggiorna o inserisce il pasto giornaliero
     * nel database e aggiorna l'interfaccia della HomePage.
     * @param event L'evento di azione (click sul bottone "Conferma Pasto").
     */
    @FXML
    void confermaPasto(ActionEvent event) {
        Alimento alimentoSelezionato = tableViewAlimenti.getSelectionModel().getSelectedItem(); // Ottieni l'alimento selezionato
        Integer quantita = quantitaSpinner.getValue(); // Recupera la quantità dallo Spinner

        // Validazione: assicura che un alimento sia selezionato e la quantità sia valida
        if (alimentoSelezionato != null && quantita != null && quantita > 0) {

            // Calcola i valori nutrizionali proporzionali alla quantità
            double kcalPerQuantita = (alimentoSelezionato.getKcal() / 100.0) * quantita;
            double proteinePerQuantita = (alimentoSelezionato.getProteine() / 100.0) * quantita;
            double carboidratiPerQuantita = (alimentoSelezionato.getCarboidrati() / 100.0) * quantita;
            double grassiPerQuantita = (alimentoSelezionato.getGrassi() / 100.0) * quantita;

            int idPastiGiornaliero; // Variabile per l'ID del pasto giornaliero

            String query = "SELECT id_pasti_giornaliero FROM PastiGiornalieri WHERE id_cliente = ? AND data = ? AND pasto = ?";
            try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione
                 PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement per cercare un pasto esistente
                pstmt.setInt(1, idCliente); // Imposta l'ID del cliente
                pstmt.setString(2, data); // Imposta la data corrente
                pstmt.setString(3, tipoPasto); // Imposta il tipo di pasto
                ResultSet rs = pstmt.executeQuery(); // Esegue la query

                if (rs.next()) {
                    // Se esiste già un pasto giornaliero per questo cliente, data e tipo di pasto
                    idPastiGiornaliero = rs.getInt("id_pasti_giornaliero");
                    // Aggiorna i valori nutrizionali del pasto giornaliero esistente (incrementali)
                    String updatePastiGiornalieriQuery = "UPDATE PastiGiornalieri SET kcal = kcal + ?, proteine = proteine + ?, carboidrati = carboidrati + ?, grassi = grassi + ? WHERE id_pasti_giornaliero = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updatePastiGiornalieriQuery)) {
                        updateStmt.setDouble(1, kcalPerQuantita);
                        updateStmt.setDouble(2, proteinePerQuantita);
                        updateStmt.setDouble(3, carboidratiPerQuantita);
                        updateStmt.setDouble(4, grassiPerQuantita);
                        updateStmt.setInt(5, idPastiGiornaliero);
                        updateStmt.executeUpdate(); // Esegue l'aggiornamento
                    }
                } else {
                    // Se non esiste, inserisce un nuovo pasto giornaliero
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
                        insertStmt.executeUpdate(); // Esegue l'inserimento
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys(); // Ottiene le chiavi generate (l'ID del nuovo pasto)
                        if (generatedKeys.next()) {
                            idPastiGiornaliero = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creazione pasto giornaliero fallita, nessun ID ottenuto.");
                        }
                    }
                }
                // Inserisce l'alimento specifico nella tabella 'Pasto' (che collega a PastiGiornalieri)
                String insertPasto = "INSERT INTO Pasto (id_pasti_giornaliero, tipo, id_elemento, quantita_grammi) VALUES (?, 'alimento', ?, ?)";
                try (PreparedStatement insertAlimentoStmt = conn.prepareStatement(insertPasto)) {
                    insertAlimentoStmt.setInt(1, idPastiGiornaliero);
                    insertAlimentoStmt.setInt(2, alimentoSelezionato.getId());
                    insertAlimentoStmt.setDouble(3, quantita);
                    insertAlimentoStmt.executeUpdate(); // Esegue l'inserimento
                }
                // Aggiorna le etichette delle calorie nella HomePage per riflettere i cambiamenti
                HomePageController.aggiornaLabelKcalPerPasto();
                System.out.println("Alimento aggiunto correttamente al pasto!");
                // Re-inizializza il contenuto del VBox per mostrare l'elemento appena aggiunto
                inizializzaContenutoDieta(idGiornoDieta, tipoPasto);

            } catch (SQLException e) {
                e.printStackTrace(); // Stampa l'errore SQL
                showAlert(Alert.AlertType.ERROR, "Errore Database", "Errore durante il salvataggio dell'alimento nel database.", "Dettagli: " + e.getMessage());
            }

        } else {
            System.out.println("Seleziona un alimento e specifica una quantità valida!"); // Messaggio di debug
            showAlert(Alert.AlertType.ERROR, "Errore di Input", "Seleziona un alimento e specifica una quantità valida!", "");
        }
    }

    /**
     * Gestisce l'azione di conferma per aggiungere una **ricetta** al pasto.
     * Calcola i valori nutrizionali, aggiorna o inserisce il pasto giornaliero
     * nel database e aggiorna l'interfaccia della HomePage.
     * @param event L'evento di azione (click sul bottone "Conferma Pasto 2").
     */
    @FXML
    void confermaPasto2(ActionEvent event) {
        Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem(); // Ottieni la ricetta selezionata
        Integer quantita = quantitaSpinner.getValue(); // Recupera la quantità dallo Spinner

        // Validazione: assicura che una ricetta sia selezionata e la quantità sia valida
        if (ricettaSelezionata != null && quantita != null && quantita > 0) {

            // Calcola i valori nutrizionali proporzionali alla quantità della ricetta
            double kcalPerQuantita = (ricettaSelezionata.getKcal() / 100.0) * quantita;
            double proteinePerQuantita = (ricettaSelezionata.getProteine() / 100.0) * quantita;
            double carboidratiPerQuantita = (ricettaSelezionata.getCarboidrati() / 100.0) * quantita;
            double grassiPerQuantita = (ricettaSelezionata.getGrassi() / 100.0) * quantita;

            int idPastiGiornaliero; // Variabile per l'ID del pasto giornaliero

            String query = "SELECT id_pasti_giornaliero FROM PastiGiornalieri WHERE id_cliente = ? AND data = ? AND pasto = ?";
            try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione
                 PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement per cercare un pasto esistente
                pstmt.setInt(1, idCliente); // Imposta l'ID del cliente
                pstmt.setString(2, data); // Imposta la data corrente
                pstmt.setString(3, tipoPasto); // Imposta il tipo di pasto
                ResultSet rs = pstmt.executeQuery(); // Esegue la query

                if (rs.next()) {
                    // Se esiste già un pasto giornaliero, aggiorna i suoi valori nutrizionali
                    idPastiGiornaliero = rs.getInt("id_pasti_giornaliero");
                    String updatePastiGiornalieriQuery = "UPDATE PastiGiornalieri SET kcal = kcal + ?, proteine = proteine + ?, carboidrati = carboidrati + ?, grassi = grassi + ? WHERE id_pasti_giornaliero = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updatePastiGiornalieriQuery)) {
                        updateStmt.setDouble(1, kcalPerQuantita);
                        updateStmt.setDouble(2, proteinePerQuantita);
                        updateStmt.setDouble(3, carboidratiPerQuantita);
                        updateStmt.setDouble(4, grassiPerQuantita);
                        updateStmt.setInt(5, idPastiGiornaliero);
                        updateStmt.executeUpdate(); // Esegue l'aggiornamento
                    }
                } else {
                    // Se non esiste, inserisce un nuovo pasto giornaliero
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
                        insertStmt.executeUpdate(); // Esegue l'inserimento
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            idPastiGiornaliero = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creazione pasto giornaliero fallita, nessun ID ottenuto.");
                        }
                    }
                }
                // Inserisce la ricetta specifica nella tabella 'Pasto'
                String insertPasto = "INSERT INTO Pasto (id_pasti_giornaliero, tipo, id_elemento, quantita_grammi) VALUES (?, 'ricetta', ?, ?)";
                try (PreparedStatement insertRicettaStmt = conn.prepareStatement(insertPasto)) {
                    insertRicettaStmt.setInt(1, idPastiGiornaliero);
                    insertRicettaStmt.setInt(2, ricettaSelezionata.getId());
                    insertRicettaStmt.setDouble(3, quantita);
                    insertRicettaStmt.executeUpdate(); // Esegue l'inserimento
                }
                // Aggiorna le etichette delle calorie nella HomePage per riflettere i cambiamenti
                HomePageController.aggiornaLabelKcalPerPasto();
                System.out.println("Ricetta aggiunta correttamente al pasto!");
                // Re-inizializza il contenuto del VBox per mostrare l'elemento appena aggiunto
                inizializzaContenutoDieta(idGiornoDieta, tipoPasto);

            } catch (SQLException e) {
                e.printStackTrace(); // Stampa l'errore SQL
                showAlert(Alert.AlertType.ERROR, "Errore Database", "Errore durante il salvataggio della ricetta nel database.", "Dettagli: " + e.getMessage());
            }

        } else {
            System.out.println("Seleziona una ricetta e specifica una quantità valida!"); // Messaggio di debug
            showAlert(Alert.AlertType.ERROR, "Errore di Input", "Seleziona una ricetta e specifica una quantità valida!", "");
        }
    }

    // --- Metodi di ricerca e caricamento dati nelle tabelle ---

    /**
     * Gestisce l'azione del bottone "Cerca" per gli alimenti.
     * Resetta l'offset e la tabella, poi esegue una nuova ricerca.
     * @param event L'evento di azione.
     */
    @FXML
    void handleCercaAlimento(ActionEvent event) {
        offset = 0; // Resetta l'offset per iniziare la ricerca dall'inizio
        tableViewAlimenti.getItems().clear(); // Pulisce la tabella degli alimenti
        String filtro = textCercaAlimento.getText(); // Ottiene il testo del filtro
        cercaAlimenti(filtro, false); // Esegue la ricerca (non in append mode)
    }

    /**
     * Esegue la ricerca degli alimenti nel database.
     * @param filtro Il testo da cercare nel nome dell'alimento.
     * @param append Se true, aggiunge i nuovi risultati alla lista esistente; altrimenti, crea una nuova lista.
     */
    private void cercaAlimenti(String filtro, boolean append) {
        // Se append è true, usa la lista esistente della tabella; altrimenti, crea una nuova lista
        ObservableList<Alimento> alimenti = append ? tableViewAlimenti.getItems() : FXCollections.observableArrayList();

        String categoria = ComboBoxAlimento.getSelectionModel().getSelectedItem(); // Ottiene la categoria selezionata
        boolean soloMiei = CheckBoxAlimenti.isSelected(); // Controlla lo stato della checkbox "Solo i miei"

        // Costruisce dinamicamente la query SQL
        StringBuilder query = new StringBuilder("SELECT * FROM foods WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?"); // Aggiunge la condizione per la categoria se selezionata
        }
        if (soloMiei && Session.getUserId() != null) {
            query.append(" AND user_id = ?"); // Aggiunge la condizione per l'ID utente se la checkbox è selezionata
        }
        query.append(" LIMIT ? OFFSET ?"); // Aggiunge LIMIT e OFFSET per la paginazione

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione al database
             PreparedStatement stmt = conn.prepareStatement(query.toString())) { // Prepara lo statement

            int paramIndex = 1; // Indice per i parametri della query
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%"); // Imposta il filtro di ricerca (case-insensitive)
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria); // Imposta il parametro categoria
            }
            if (soloMiei && Session.getUserId() != null) {
                stmt.setInt(paramIndex++, Session.getUserId()); // Imposta il parametro user_id
            }
            stmt.setInt(paramIndex++, LIMIT); // Imposta il limite di risultati
            stmt.setInt(paramIndex++, offset); // Imposta l'offset

            try (ResultSet rs = stmt.executeQuery()) { // Esegue la query e ottiene i risultati
                while (rs.next()) { // Itera su ogni riga del ResultSet
                    // Crea un nuovo oggetto Alimento con i dati dal database e lo aggiunge alla lista
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

                tableViewAlimenti.setItems(alimenti); // Aggiorna gli elementi della TableView
                offset += LIMIT; // Incrementa l'offset per la prossima paginazione
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare gli alimenti dal database.", "Dettagli: " + e.getMessage());
        }
    }

    /**
     * Apre la finestra di dettaglio per un alimento selezionato (tramite double-click).
     * @param alimento L'oggetto Alimento da visualizzare in dettaglio.
     */
    private void apriDettaglio(Alimento alimento) {
        try {
            // Carica il file FXML della schermata DettagliAlimento
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettagliAlimento.fxml"));
            Parent root = loader.load();

            DettagliAlimentoController controller = loader.getController(); // Ottiene il controller
            controller.setAlimento(alimento); // Passa l'alimento selezionato
            controller.setOrigineFXML("AggiungiPasto.fxml");

            Stage stage = new Stage(); // Crea un nuovo Stage
            stage.setTitle("Dettaglio Alimento"); // Imposta il titolo
            stage.setScene(new Scene(root)); // Imposta la scena
            stage.setResizable(false);
            stage.setFullScreen(false);
            stage.show(); // Mostra la finestra
        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire la schermata di dettaglio alimento.", "Dettagli: " + e.getMessage());
        }
    }

    private boolean isLoading = false; // Flag per prevenire carichi multipli durante lo scroll per gli alimenti

    /**
     * Carica altri alimenti quando l'utente raggiunge il fondo della scrollbar.
     * Utilizza il flag `isLoading` per evitare richieste duplicate.
     */
    private void caricaAltriAlimenti() {
        if (isLoading) return; // Se è già in corso un caricamento, esci
        isLoading = true; // Imposta il flag per indicare che un caricamento è in corso
        String filtro = textCercaAlimento.getText(); // Ottiene il filtro corrente
        cercaAlimenti(filtro, true); // Esegue la ricerca in modalità append
        isLoading = false; // Resetta il flag al termine del caricamento
    }

    /**
     * Gestisce l'azione del bottone "Cerca" per le ricette.
     * Resetta l'offset e la tabella, poi esegue una nuova ricerca.
     * @param event L'evento di azione.
     */
    @FXML
    void handleCercaRicetta(ActionEvent event) {
        offset = 0; // Resetta l'offset
        tableViewRicette.getItems().clear(); // Pulisce la tabella
        cercaRicette(textCercaRicetta.getText(), false); // Esegue la ricerca
    }

    /**
     * Esegue la ricerca delle ricette nel database.
     * @param filtro Il testo da cercare nel nome della ricetta.
     * @param append Se true, aggiunge i nuovi risultati alla lista esistente; altrimenti, crea una nuova lista.
     */
    public void cercaRicette(String filtro, boolean append) {
        // Se append è true, usa la lista esistente; altrimenti, crea una nuova lista
        ObservableList<Ricetta> ricette = append ? tableViewRicette.getItems() : FXCollections.observableArrayList();

        String categoria = ComboBoxRicetta.getSelectionModel().getSelectedItem(); // Ottiene la categoria selezionata
        boolean soloMiei = CheckBoxRicette.isSelected(); // Controlla lo stato della checkbox

        // Costruisce dinamicamente la query SQL per le ricette
        StringBuilder query = new StringBuilder("SELECT * FROM Ricette WHERE LOWER(nome) LIKE ?");
        // Aggiungi una condizione per la categoria solo se non è "Tutte"
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?");
        }
        // Aggiungi una condizione per l'id_utente solo se la checkbox "Solo le mie ricette" è selezionata e l'utente è loggato
        if (soloMiei && Session.getUserId() != null) {
            query.append(" AND id_utente = ?");
        }
        query.append(" LIMIT ? OFFSET ?"); // Aggiunge LIMIT e OFFSET per la paginazione

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione
             PreparedStatement stmt = conn.prepareStatement(query.toString())) { // Prepara lo statement

            int paramIndex = 1; // Indice per i parametri
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%"); // Imposta il filtro
            // Applica il parametro per la categoria solo se la condizione è stata aggiunta
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria);
            }
            // Applica il parametro per l'id_utente solo se la condizione è stata aggiunta
            if (soloMiei && Session.getUserId() != null) {
                stmt.setInt(paramIndex++, Session.getUserId());
            }
            stmt.setInt(paramIndex++, LIMIT); // Imposta il limite
            stmt.setInt(paramIndex++, offset); // Imposta l'offset

            try (ResultSet rs = stmt.executeQuery()) { // Esegue la query
                while (rs.next()) { // Itera sui risultati
                    // Crea un nuovo oggetto Ricetta con i dati dal database e lo aggiunge alla lista
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

                tableViewRicette.setItems(ricette); // Aggiorna gli elementi della TableView
                offset += LIMIT; // Incrementa l'offset
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare le ricette dal database.", "Dettagli: " + e.getMessage());
        }
    }

    /**
     * Apre la finestra di dettaglio per una ricetta selezionata (tramite double-click).
     * @param event L'evento del mouse (double-click).
     */
    @FXML
    private void apriDettaglioRicetta(MouseEvent event) {
        // Controlla se è un double-click e se una riga è selezionata
        if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && !tableViewRicette.getSelectionModel().isEmpty()) {
            Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem(); // Ottiene la ricetta selezionata
            try {
                // Carica il file FXML della schermata DettaglioRicetta
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettaglioRicetta.fxml"));
                Parent root = loader.load();

                DettaglioRicettaController controller = loader.getController(); // Ottiene il controller
                controller.setRicetta(ricettaSelezionata); // Passa la ricetta selezionata
                controller.setOrigineFXML("AggiungiPasto.fxml"); // Indica la schermata di origine

                Stage stage = new Stage(); // Crea un nuovo Stage
                stage.setTitle("Dettaglio Ricetta"); // Imposta il titolo
                stage.setScene(new Scene(root)); // Imposta la scena
                stage.show(); // Mostra la finestra
            } catch (IOException e) {
                e.printStackTrace(); // Stampa l'errore
                showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire la schermata di dettaglio ricetta.", "Dettagli: " + e.getMessage());
            }
        }
    }

    private boolean isLoading2 = false; // Flag per prevenire carichi multipli per le ricette

    /**
     * Carica altre ricette quando l'utente raggiunge il fondo della scrollbar.
     * Utilizza il flag `isLoading2` per evitare richieste duplicate.
     */
    private void caricaAltreRicette() {
        if (isLoading2) return; // Se è già in corso un caricamento, esci
        isLoading2 = true; // Imposta il flag
        String filtro = textCercaRicetta.getText(); // Ottiene il filtro corrente
        cercaRicette(filtro, true); // Esegue la ricerca in modalità append
        isLoading2 = false; // Resetta il flag
    }

    /**
     * Metodo helper per ottenere la ScrollBar verticale di una TableView.
     * Utilizzato per implementare la paginazione basata sullo scroll.
     * @param table La TableView da cui ottenere la scrollbar.
     * @return La ScrollBar verticale o null se non trovata.
     */
    private ScrollBar getVerticalScrollbar(TableView<?> table) {
        // Cerca tra tutti i nodi con la classe CSS ".scroll-bar"
        for (Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) { // Se il nodo è una ScrollBar
                ScrollBar sb = (ScrollBar) node;
                if (sb.getOrientation() == Orientation.VERTICAL) { // E se è verticale
                    return sb; // La restituisce
                }
            }
        }
        return null; // Nessuna scrollbar verticale trovata
    }

    /**
     * Popola le ComboBox delle categorie per gli alimenti e le ricette,
     * recuperando le categorie distinte dal database.
     */
    private void popolaCategorie() {
        // Popola ComboBoxAlimento con categorie dalla tabella 'foods'
        String queryAlimenti = "SELECT DISTINCT categoria FROM foods WHERE categoria IS NOT NULL";
        try (Connection conn = SQLiteConnessione.connector();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryAlimenti)) {

            ObservableList<String> categorieAlimenti = FXCollections.observableArrayList();
            categorieAlimenti.add("Tutte"); // Aggiunge l'opzione "Tutte"
            while (rs.next()) {
                categorieAlimenti.add(rs.getString("categoria")); // Aggiunge le categorie dal DB
            }
            ComboBoxAlimento.setItems(categorieAlimenti); // Imposta gli elementi nella ComboBox Alimenti
            ComboBoxAlimento.getSelectionModel().selectFirst(); // Seleziona il primo elemento ("Tutte")
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare le categorie alimenti.", "Dettagli: " + e.getMessage());
        }

        // Popola ComboBoxRicetta con categorie dalla tabella 'Ricette'
        ObservableList<String> categorieRicetteList = FXCollections.observableArrayList();
        categorieRicetteList.add("Tutte"); // Aggiungi "Tutte" come prima opzione

        String queryRicette = "SELECT DISTINCT categoria FROM Ricette WHERE categoria IS NOT NULL";
        try (Connection conn = SQLiteConnessione.connector();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryRicette)) {
            while (rs.next()) {
                categorieRicetteList.add(rs.getString("categoria")); // Aggiunge le categorie dal DB
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare le categorie ricette.", "Dettagli: " + e.getMessage());
        }
        ComboBoxRicetta.setItems(categorieRicetteList); // Imposta gli elementi nella ComboBox Ricette
        ComboBoxRicetta.getSelectionModel().selectFirst(); // Seleziona "Tutte" di default
    }

    @FXML private Button btnRicette; // Bottone per passare alla visualizzazione Ricette
    @FXML private Button btnAlimenti; // Bottone per passare alla visualizzazione Alimenti

    /**
     * Applica lo stile di evidenziazione al bottone attivo e rimuove lo stile dal bottone inattivo.
     * @param active Il bottone che deve essere evidenziato.
     * @param inactive Il bottone che deve essere "spento".
     */
    private void highlightButton(Button active, Button inactive) {
        // Se il bottone attivo ha già lo stile "bottoneAttivo", non fare nulla per evitare refresh inutili
        if(active.getStyleClass().contains("bottoneAttivo")) {
            return;
        }
        // Rimuove gli stili "bottoneSpento" dal bottone attivo e "bottoneAttivo" dal bottone inattivo
        active.getStyleClass().remove("bottoneSpento");
        inactive.getStyleClass().remove("bottoneAttivo");

        // Aggiunge gli stili "bottoneAttivo" al bottone attivo e "bottoneSpento" al bottone inattivo
        active.getStyleClass().add("bottoneAttivo");
        inactive.getStyleClass().add("bottoneSpento");
    }

    /**
     * Mostra la tabella e i controlli relativi agli Alimenti e nasconde quelli delle Ricette.
     * Viene chiamato quando l'utente vuole aggiungere un alimento.
     * @param event L'evento di azione (es. click del mouse).
     */
    @FXML
    void mostraTabellaAlimenti(ActionEvent event) {
        highlightButton(btnAlimenti, btnRicette); // Evidenzia il bottone "Alimenti"

        tableViewAlimenti.setVisible(true); // Rende visibile la tabella alimenti
        tableViewRicette.setVisible(false); // Nasconde la tabella ricette
        confermaPastoButton.setVisible(true); // Mostra il bottone conferma per alimenti
        confermaPasto2Button.setVisible(false); // Nasconde il bottone conferma per ricette
        textCercaAlimento.setVisible(true); // Mostra campo ricerca alimenti
        textCercaRicetta.setVisible(false); // Nasconde campo ricerca ricette
        ButtonCercaAlimento.setVisible(true); // Mostra bottone cerca alimenti
        ButtonCercaRicetta.setVisible(false); // Nasconde bottone cerca ricette
        ComboBoxAlimento.setVisible(true); // Mostra ComboBox categorie alimenti
        ComboBoxRicetta.setVisible(false); // Nasconde ComboBox categorie ricette
        CheckBoxAlimenti.setVisible(true); // Mostra CheckBox alimenti
        CheckBoxRicette.setVisible(false); // Nasconde CheckBox ricette
    }

    /**
     * Mostra la tabella e i controlli relativi alle Ricette e nasconde quelli degli Alimenti.
     * Viene chiamato quando l'utente vuole aggiungere una ricetta.
     * @param event L'evento di azione (es. click del mouse).
     */
    @FXML
    void mostraTabellaRicette(ActionEvent event) {
        highlightButton(btnRicette, btnAlimenti); // Evidenzia il bottone "Ricette"

        tableViewAlimenti.setVisible(false); // Nasconde la tabella alimenti
        tableViewRicette.setVisible(true); // Rende visibile la tabella ricette
        confermaPastoButton.setVisible(false); // Nasconde il bottone conferma per alimenti
        confermaPasto2Button.setVisible(true); // Mostra il bottone conferma per ricette
        textCercaAlimento.setVisible(false); // Nasconde campo ricerca alimenti
        textCercaRicetta.setVisible(true); // Mostra campo ricerca ricette
        ButtonCercaAlimento.setVisible(false); // Nasconde bottone cerca alimenti
        ButtonCercaRicetta.setVisible(true); // Mostra bottone cerca ricette
        ComboBoxAlimento.setVisible(false); // Nasconde ComboBox categorie alimenti
        ComboBoxRicetta.setVisible(true); // Mostra ComboBox categorie ricette
        CheckBoxAlimenti.setVisible(false); // Nasconde CheckBox alimenti
        CheckBoxRicette.setVisible(true); // Mostra CheckBox ricette
    }

    /**
     * Carica gli alimenti e le ricette già associati a un pasto specifico di un giorno dieta
     * dal database e li restituisce come una lista di Node (HBox).
     * @param idGiornoDieta L'ID del giorno dieta.
     * @param tipoPasto Il tipo di pasto (es. "colazione").
     * @return Una List di Node (HBox) che rappresentano gli alimenti/ricette caricati.
     */
    public List<Node> caricaAlimentiERicette(int idGiornoDieta, String tipoPasto) {
        List<Node> items = new ArrayList<>(); // Lista per memorizzare gli elementi dell'interfaccia

        // Query per recuperare gli alimenti associati a un pasto di un giorno dieta
        String queryAlimenti = """
    SELECT f.nome, da.quantita_grammi, f.id
    FROM DietaAlimenti da
    JOIN foods f ON da.id_alimento = f.id
    WHERE da.id_giorno_dieta = ? AND da.pasto = ?
    """;

        // Query per recuperare le ricette associate a un pasto di un giorno dieta
        String queryRicette = """
    SELECT r.nome, dr.quantita_grammi, r.id
    FROM DietaRicette dr
    JOIN Ricette r ON dr.id_ricetta = r.id
    WHERE dr.id_giorno_dieta = ? AND dr.pasto = ?
    """;

        try (Connection conn = SQLiteConnessione.connector()) { // Ottiene la connessione
            // --- Carica Alimenti ---
            try (PreparedStatement stmt = conn.prepareStatement(queryAlimenti)) {
                stmt.setInt(1, idGiornoDieta); // Imposta l'ID del giorno dieta
                stmt.setString(2, tipoPasto); // Imposta il tipo di pasto
                ResultSet rs = stmt.executeQuery(); // Esegue la query

                while (rs.next()) {
                    String nome = rs.getString("nome");
                    double quantita = rs.getDouble("quantita_grammi");
                    int id = rs.getInt("id"); // Recupera l'ID dell'alimento
                    System.out.println(nome + " " + quantita + " (ID: " + id + ")"); // Debug
                    // Crea un HBox per visualizzare l'alimento e lo aggiunge alla lista
                    items.add(creaBoxElemento(nome, quantita, id, false)); // false indica che è un alimento
                }
            }

            // --- Carica Ricette ---
            try (PreparedStatement stmt = conn.prepareStatement(queryRicette)) {
                stmt.setInt(1, idGiornoDieta); // Imposta l'ID del giorno dieta
                stmt.setString(2, tipoPasto); // Imposta il tipo di pasto
                ResultSet rs = stmt.executeQuery(); // Esegue la query

                while (rs.next()) {
                    String nome = rs.getString("nome");
                    double quantita = rs.getDouble("quantita_grammi");
                    int id = rs.getInt("id"); // Recupera l'ID della ricetta
                    System.out.println(nome + " " + quantita + " (ID: " + id + ")"); // Debug
                    // Crea un HBox per visualizzare la ricetta e lo aggiunge alla lista
                    items.add(creaBoxElemento(nome, quantita, id, true)); // true indica che è una ricetta
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile caricare alimenti e ricette per il pasto.", "Dettagli: " + e.getMessage());
        }

        return items; // Restituisce la lista di elementi dell'interfaccia
    }

    /**
     * Crea un HBox che rappresenta un singolo elemento (alimento o ricetta) con nome e quantità.
     * Questo HBox è cliccabile per aprire i dettagli o per cercare l'elemento nella tabella.
     * @param nome Il nome dell'alimento o della ricetta.
     * @param quantita La quantità in grammi.
     * @param idElemento L'ID dell'alimento o della ricetta nel database.
     * @param isRicetta True se l'elemento è una ricetta, false se è un alimento.
     * @return Un HBox configurato.
     */
    private HBox creaBoxElemento(String nome, double quantita, int idElemento, boolean isRicetta) {
        // Crea le etichette per nome e quantità
        Label nomeLabel = new Label((isRicetta ? "[Ricetta] " : "[Alimento] ") + nome);
        Label quantitaLabel = new Label(quantita + " g");

        // Crea l'HBox contenitore per le etichette
        HBox hbox = new HBox(10, nomeLabel, quantitaLabel); // Spazio di 10 tra le etichette
        hbox.setPadding(new Insets(5)); // Padding interno
        hbox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-radius: 5;"); // Stile CSS

        // Configura il comportamento di double-click sull'HBox
        hbox.setOnMouseClicked(event -> {
            // Se è un double-click con il tasto primario del mouse
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                if (isRicetta) {
                    cercaRicettePerId(idElemento); // Cerca la ricetta per ID nella tabella ricette
                    mostraTabellaRicette(null); // Passa alla visualizzazione della tabella ricette
                } else {
                    cercaAlimentiPerId(idElemento); // Cerca l'alimento per ID nella tabella alimenti
                    mostraTabellaAlimenti(null); // Passa alla visualizzazione della tabella alimenti
                }
            }
        });

        return hbox;
    }

    @FXML private VBox vboxContenuto; // Il VBox nel FXML che conterrà gli HBox degli elementi del pasto

    /**
     * Inizializza il contenuto del VBox `vboxContenuto` con gli alimenti e le ricette
     * già presenti per il pasto specificato in un dato giorno dieta.
     * @param idGiornoDieta L'ID del giorno dieta.
     * @param tipoPasto Il tipo di pasto (es. "colazione").
     */
    public void inizializzaContenutoDieta(int idGiornoDieta, String tipoPasto) {
        System.out.println(idGiornoDieta + " " + tipoPasto); // Debug
        vboxContenuto.getChildren().clear(); // Pulisce il VBox da elementi precedenti
        // Carica gli elementi dal database
        List<Node> elementi = caricaAlimentiERicette(idGiornoDieta, tipoPasto.toLowerCase());
        System.out.println("Numero di elementi caricati: " + elementi.size()); // Debug
        vboxContenuto.getChildren().addAll(elementi); // Aggiunge tutti gli elementi al VBox
    }

    /**
     * Cerca un alimento specifico per ID nel database e lo visualizza nella `tableViewAlimenti`.
     * @param id L'ID dell'alimento da cercare.
     */
    private void cercaAlimentiPerId(int id) {
        ObservableList<Alimento> alimenti = FXCollections.observableArrayList(); // Lista per l'alimento trovato

        String query = "SELECT * FROM foods WHERE id = ?"; // Query per cercare per ID

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione
             PreparedStatement stmt = conn.prepareStatement(query)) { // Prepara lo statement

            stmt.setInt(1, id); // Imposta l'ID come parametro

            try (ResultSet rs = stmt.executeQuery()) { // Esegue la query
                while (rs.next()) { // Se trova l'alimento
                    // Crea un nuovo oggetto Alimento con i dati dal DB
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
                tableViewAlimenti.setItems(alimenti); // Imposta la lista nella TableView

                // Opzionale: Seleziona l'elemento trovato nella tabella e lo scorre in vista
                if (!alimenti.isEmpty()) {
                    tableViewAlimenti.getSelectionModel().selectFirst();
                    tableViewAlimenti.scrollTo(alimenti.get(0));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile trovare l'alimento per ID.", "Dettagli: " + e.getMessage());
        }
    }

    /**
     * Cerca una ricetta specifica per ID nel database e la visualizza nella `tableViewRicette`.
     * @param id L'ID della ricetta da cercare.
     */
    private void cercaRicettePerId(int id) {
        ObservableList<Ricetta> ricette = FXCollections.observableArrayList(); // Lista per la ricetta trovata

        String query = "SELECT * FROM Ricette WHERE id = ?"; // Query per cercare per ID

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione
             PreparedStatement stmt = conn.prepareStatement(query)) { // Prepara lo statement

            stmt.setInt(1, id); // Imposta l'ID come parametro

            try (ResultSet rs = stmt.executeQuery()) { // Esegue la query
                while (rs.next()) { // Se trova la ricetta
                    // Crea un nuovo oggetto Ricetta con i dati dal DB
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
                tableViewRicette.setItems(ricette); // Imposta la lista nella TableView

                // Opzionale: Seleziona l'elemento trovato nella tabella e lo scorre in vista
                if (!ricette.isEmpty()) {
                    tableViewRicette.getSelectionModel().selectFirst();
                    tableViewRicette.scrollTo(ricette.get(0));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile trovare la ricetta per ID.", "Dettagli: " + e.getMessage());
        }
    }

    /**
     * Metodo helper per mostrare messaggi di avviso all'utente.
     * @param alertType Il tipo di avviso (ERROR, INFORMATION, WARNING, CONFIRMATION).
     * @param title Il titolo della finestra di avviso.
     * @param header L'intestazione del messaggio (può essere null).
     * @param content Il contenuto del messaggio.
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
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
        alert.showAndWait();
    }
}
