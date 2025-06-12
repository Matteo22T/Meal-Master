package com.matteotocci.app.controller;

// Importa le classi necessarie per l'applicazione JavaFX e le operazioni del database
import com.matteotocci.app.model.Alimento; // Modello per l'oggetto Alimento
import com.matteotocci.app.model.Ricetta; // Modello per l'oggetto Ricetta
import com.matteotocci.app.model.SQLiteConnessione; // Classe per gestire la connessione al database SQLite
import com.matteotocci.app.model.Session; // Classe per la gestione della sessione utente (es. ID utente loggato)
import javafx.application.Platform; // Per eseguire codice sul thread di JavaFX UI
import javafx.collections.FXCollections; // Utility per creare collezioni osservabili
import javafx.collections.ObservableList; // Lista che notifica i "listener" quando avvengono dei cambiamenti
import javafx.event.ActionEvent; // Tipo di evento generato dalle azioni dell'utente (es. click su un bottone)
import javafx.fxml.FXML; // Annotazione per collegare elementi dell'interfaccia utente definiti in FXML al codice Java
import javafx.fxml.FXMLLoader; // Carica file FXML (layout dell'interfaccia utente)
import javafx.fxml.Initializable; // AGGIUNTA: Interfaccia per i controller che devono essere inizializzati dopo il caricamento dell'FXML
import javafx.geometry.Orientation; // Per specificare l'orientamento di elementi come le ScrollBar
import javafx.scene.Node; // Classe base per tutti i nodi nel grafo della scena (elementi UI)
import javafx.scene.Parent; // Nodo base per la gerarchia della scena (container di tutti gli elementi UI)
import javafx.scene.Scene; // Contenitore per tutti i contenuti di una scena
import javafx.scene.control.*; // Controlli UI standard di JavaFX (Button, ComboBox, CheckBox, TableView, TextField, Spinner, etc.)
import javafx.scene.control.cell.PropertyValueFactory; // Per collegare le proprietà degli oggetti alle colonne di una TableView
import javafx.scene.image.ImageView; // Per visualizzare immagini
import javafx.scene.input.MouseEvent; // Tipo di evento generato da interazioni del mouse
import javafx.stage.Stage; // La finestra dell'applicazione

import java.io.IOException; // Eccezione per errori di input/output (es. caricamento file FXML)
import java.net.URL; // Necessario per Initializable
import java.sql.Connection; // Interfaccia per la connessione al database
import java.sql.PreparedStatement; // Per eseguire query SQL precompilate
import java.sql.ResultSet; // Per leggere i risultati delle query SQL
import java.sql.SQLException; // Eccezione per errori di database
import java.sql.Statement; // Per eseguire query SQL semplici
import java.util.ResourceBundle; // Necessario per Initializable

/**
 * Controller per la schermata "Aggiungi Alimento/Ricetta alla Dieta".
 * Questa classe permette all'utente di cercare e selezionare alimenti o ricette
 * da aggiungere a un giorno specifico di una dieta.
 * Implementa Initializable per gestire l'inizializzazione dei componenti UI.
 */
public class AggiungiAlimentoDieta implements Initializable { // AGGIUNTA: Implementa l'interfaccia Initializable

    // --- Elementi dell'interfaccia utente (collegati tramite @FXML) ---
    // Colonne della TableView per gli Alimenti
    @FXML private TableColumn<Alimento, ImageView> immagineCol;
    @FXML private TableColumn<Alimento, String> nomeCol, brandCol;
    @FXML private TableColumn<Alimento, Double> calorieCol, proteineCol, carboidratiCol, grassiCol;
    @FXML private TableColumn<Alimento, Double> grassiSatCol, saleCol, fibreCol, zuccheriCol;

    // Bottoni per la ricerca
    @FXML private Button ButtonCercaAlimento;
    @FXML private Button ButtonCercaRicetta;

    // ComboBox per la selezione della categoria (Alimenti e Ricette)
    @FXML private ComboBox<String> ComboBoxAlimento;
    @FXML private ComboBox<String> ComboBoxRicetta;

    // CheckBox per filtrare "Solo i miei" alimenti/ricette
    @FXML private CheckBox CheckBoxAlimenti;
    @FXML private CheckBox CheckBoxRicette;

    // Bottoni di conferma per aggiungere alla dieta
    @FXML private Button confermaAlimentiButton;
    @FXML private Button confermaRicetteButton;

    // TableView per visualizzare alimenti e ricette
    @FXML private TableView<Alimento> tableViewAlimenti;
    @FXML private TableView<Ricetta> tableViewRicette;

    // Campi di testo per la ricerca per nome
    @FXML private TextField textCercaAlimento;
    @FXML private TextField textCercaRicetta;

    // Colonne della TableView per le Ricette
    @FXML private TableColumn<Ricetta, String> nomeColRic;
    @FXML private TableColumn<Ricetta, String> descrizioneColRic;
    @FXML private TableColumn<Ricetta, String> categoriaColRic;

    // Spinner per la selezione della quantità
    @FXML private Spinner<Integer> quantitaSpinner;

    // --- Variabili di stato interne ---
    private int offset = 0; // Offset per la paginazione delle ricerche (quanti elementi saltare)
    private final int LIMIT = 50; // Numero massimo di elementi da caricare per volta (per paginazione)

    // Riferimenti ad altri controller per la comunicazione
    private AggiungiGiornoDieta giornoDietaController; // Controller della schermata "Aggiungi Giorno Dieta"
    private String pastoCorrente; // Il tipo di pasto a cui aggiungere l'alimento/ricetta (es. "Colazione")

    @FXML private Button btnRicette; // Bottone per passare alla visualizzazione Ricette
    @FXML private Button btnAlimenti; // Bottone per passare alla visualizzazione Alimenti


    // --- Metodi di comunicazione tra controller ---

    /**
     * Imposta il controller della schermata "Aggiungi Giorno Dieta".
     * Questo permette a questo controller di passare le selezioni al controller genitore.
     * @param controller L'istanza di AggiungiGiornoDieta.
     */
    public void setGiornoDietaController(AggiungiGiornoDieta controller) {
        this.giornoDietaController = controller;
    }

    /**
     * Imposta il tipo di pasto corrente (es. "Colazione", "Pranzo") per cui l'alimento/ricetta
     * viene aggiunto.
     * @param pasto Il nome del pasto.
     */
    public void setPastoCorrente(String pasto) {
        this.pastoCorrente = pasto;
    }

    // --- Metodi di gestione della visibilità delle tabelle ---

    /**
     * Mostra la tabella e i controlli relativi agli Alimenti e nasconde quelli delle Ricette.
     * Viene chiamato quando l'utente vuole aggiungere un alimento.
     * @param event L'evento di azione (es. click del mouse).
     */
    @FXML
    private void mostraTabellaAlimenti(ActionEvent event) {
        highlightButton(btnAlimenti, btnRicette);

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

    /**
     * Mostra la tabella e i controlli relativi alle Ricette e nasconde quelli degli Alimenti.
     * Viene chiamato quando l'utente vuole aggiungere una ricetta.
     * @param event L'evento di azione (es. click del mouse).
     */
    @FXML
    private void mostraTabellaRicette(ActionEvent event) {
        highlightButton(btnRicette, btnAlimenti); // Evidenzia il bottone "Ricette"


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
        offset = 0; // Reset dell'offset per iniziare una nuova ricerca dall'inizio
        tableViewRicette.getItems().clear(); // Pulisci la tabella delle ricette
        // Forza la selezione per mostrare le proprie ricette se l'utente è loggato
        if (Session.getUserId() != null) {
            CheckBoxRicette.setSelected(true); // Seleziona la checkbox "Solo le mie ricette"
        } else {
            CheckBoxRicette.setSelected(false); // Deseleziona altrimenti
        }
        cercaRicette(textCercaRicetta.getText(), false); // Avvia la ricerca delle ricette (non in append mode)
    }

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

        // Inizializza lo Spinner per la selezione della quantità
        // Valori da 1 a 1000, valore iniziale 100, editabile dall'utente
        quantitaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100));
        quantitaSpinner.setEditable(true); // Permette all'utente di digitare un valore

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

        // Carica inizialmente gli alimenti (senza filtro di ricerca e senza append)
        cercaAlimenti("", false);
        // All'inizializzazione, se un utente è loggato, forza la selezione della checkbox "Solo le mie ricette"
        if (Session.getUserId() != null) {
            CheckBoxRicette.setSelected(true);
        }
        // Carica inizialmente le ricette (senza filtro di ricerca e senza append)
        // La condizione della checkbox "Solo le mie ricette" verrà applicata da cercaRicette()
        cercaRicette("", false);

        highlightButton(btnAlimenti, btnRicette);

    }

    // --- Metodi di ricerca ---

    /**
     * Gestisce l'evento di click sul bottone "Cerca" per gli alimenti.
     * Resetta l'offset e la tabella, poi esegue una nuova ricerca.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleCercaAlimento(ActionEvent event) {
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
        }
    }

    private boolean isLoading = false; // Flag per prevenire carichi multipli durante lo scroll

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

    private boolean isLoading2 = false; // Flag per prevenire carichi multipli per le ricette

    /**
     * Carica altre ricette quando l'utente raggiunge il fondo della scrollbar.
     * @see #caricaAltriAlimenti()
     */
    private void caricaAltreRicette() {
        if (isLoading2) return;
        isLoading2 = true;
        String filtro = textCercaRicetta.getText();
        cercaRicette(filtro, true);
        isLoading2 = false;
    }

    /**
     * Gestisce l'evento di click sul bottone "Cerca" per le ricette.
     * Resetta l'offset e la tabella, poi esegue una nuova ricerca.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleCercaRicetta(ActionEvent event) {
        offset = 0; // Resetta l'offset
        tableViewRicette.getItems().clear(); // Pulisce la tabella
        cercaRicette(textCercaRicetta.getText(), false); // Esegue la ricerca
    }

    /**
     * Esegue la ricerca delle ricette nel database.
     * La logica è simile a `cercaAlimenti`, ma specifica per le ricette.
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
        }
    }

    /**
     * Apre la finestra di dettaglio per una ricetta selezionata tramite double-click.
     * @param event L'evento del mouse (double-click).
     */
    @FXML
    private void apriDettaglioRicetta(MouseEvent event) {
        // Controlla se è un double-click e se una riga è selezionata
        if (event.getClickCount() == 2 && !tableViewRicette.getSelectionModel().isEmpty()) {
            Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem(); // Ottiene la ricetta selezionata
            try {
                // Carica il file FXML della schermata DettaglioRicetta
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettaglioRicetta.fxml"));
                Parent root = loader.load();

                DettaglioRicettaController controller = loader.getController(); // Ottiene il controller
                controller.setRicetta(ricettaSelezionata); // Passa la ricetta selezionata
                controller.setOrigineFXML("AggiungiAlimentoDieta.fxml"); // Indica la schermata di origine

                Stage stage = new Stage(); // Crea un nuovo Stage
                stage.setTitle("Dettaglio Ricetta"); // Imposta il titolo
                stage.setScene(new Scene(root)); // Imposta la scena
                stage.setFullScreen(false);
                stage.setResizable(false);
                stage.show(); // Mostra la finestra
            } catch (IOException e) {
                e.printStackTrace(); // Stampa l'errore
            }
        }
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
     * Apre la finestra di dettaglio per un alimento selezionato tramite double-click.
     * @param alimento L'oggetto Alimento da visualizzare in dettaglio.
     */
    private void apriDettaglio(Alimento alimento) {
        try {
            // Carica il file FXML della schermata DettagliAlimento
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettagliAlimento.fxml"));
            Parent root = loader.load();

            DettagliAlimentoController controller = loader.getController(); // Ottiene il controller
            controller.setAlimento(alimento); // Passa l'alimento selezionato
            controller.setOrigineFXML("AggiungAlimentoDieta.fxml");

            Stage stage = new Stage(); // Crea un nuovo Stage
            stage.setTitle("Dettaglio Alimento"); // Imposta il titolo
            stage.setScene(new Scene(root)); // Imposta la scena
            stage.setResizable(false);
            stage.setFullScreen(false);
            stage.show(); // Mostra la finestra
        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore
        }
    }

    /**
     * Popola le ComboBox delle categorie per gli alimenti e le ricette,
     * recuperando le categorie distinte dal database.
     */
    private void popolaCategorie() {
        // Query per le categorie degli alimenti
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
        }

        // Popola ComboBoxRicetta con categorie dalla tabella Ricette
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
        }
        ComboBoxRicetta.setItems(categorieRicetteList); // Imposta gli elementi nella ComboBox Ricette
        ComboBoxRicetta.getSelectionModel().selectFirst(); // Seleziona "Tutte" di default
    }

    // --- Metodi di conferma e aggiunta alla dieta ---

    /**
     * Gestisce la conferma e l'aggiunta di un alimento selezionato alla dieta.
     * Passa l'alimento e la quantità al controller del giorno della dieta.
     * @param event L'evento di azione.
     */
    @FXML
    private void confermaAlimenti(ActionEvent event) {
        Alimento alimentoSelezionato = tableViewAlimenti.getSelectionModel().getSelectedItem(); // Ottiene l'alimento selezionato
        Integer quantita = quantitaSpinner.getValue(); // Recupera la quantità dallo Spinner

        // Verifica che un alimento sia selezionato, che il controller genitore sia disponibile,
        // che il pasto corrente sia definito e che la quantità sia valida.
        if (alimentoSelezionato != null && giornoDietaController != null && pastoCorrente != null && quantita != null && quantita > 0) {
            // Chiama un metodo nel controller AggiungiGiornoDieta per aggiungere l'alimento
            giornoDietaController.aggiungiAlimentoAllaLista(pastoCorrente, alimentoSelezionato, quantita);
            // La finestra rimane aperta ora, come da richiesta precedente (implicita, dato che non c'è chiusura)
        } else {
            System.out.println("Seleziona un alimento e specifica una quantità valida!"); // Messaggio di errore per il debug
            // Si potrebbe aggiungere una showAlert qui per l'utente
        }
    }

    /**
     * Gestisce la conferma e l'aggiunta di una ricetta selezionata alla dieta.
     * Passa la ricetta e la quantità al controller del giorno della dieta.
     * Dopo l'aggiunta, chiude la finestra corrente.
     * @param event L'evento di azione.
     */
    @FXML
    private void confermaRicette(ActionEvent event) {
        Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem(); // Ottiene la ricetta selezionata
        Integer quantita = quantitaSpinner.getValue(); // Recupera la quantità dallo Spinner

        // Verifica che una ricetta sia selezionata, che il controller genitore sia disponibile,
        // che il pasto corrente sia definito e che la quantità sia valida.
        if (ricettaSelezionata != null && giornoDietaController != null && pastoCorrente != null && quantita != null && quantita > 0) {
            // Chiama un metodo specifico nel controller AggiungiGiornoDieta per aggiungere la ricetta
            // (solo alla UI e alla memoria in questo momento, non al DB)
            giornoDietaController.aggiungiRicettaAllaLista(ricettaSelezionata, quantita, pastoCorrente);

            // Chiude la finestra corrente dopo la conferma
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } else {
            System.out.println("Seleziona una ricetta e specifica una quantità valida!"); // Messaggio di errore per il debug
            // Si potrebbe aggiungere una showAlert qui per l'utente
        }
    }

    /**
     * Metodo per convertire una Ricetta in un Alimento equivalente.
     * Questo metodo sembra essere un residuo o un'alternativa non più utilizzata
     * direttamente nella logica di aggiunta ricetta, ma potrebbe essere utile altrove.
     * @param ricetta La ricetta da convertire.
     * @return Un oggetto Alimento basato sulla ricetta, o null se non trovato/errore.
     */
    private Alimento getAlimentoFromRicetta(Ricetta ricetta) {
        // Query per recuperare i dettagli di un alimento con lo stesso nome della ricetta
        String query = "SELECT * FROM foods WHERE nome = ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ricetta.getNome()); // Imposta il nome della ricetta come filtro
            ResultSet rs = stmt.executeQuery(); // Esegue la query
            if (rs.next()) { // Se trova un risultato
                // Crea e restituisce un nuovo oggetto Alimento con i dati recuperati
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
            e.printStackTrace(); // Stampa l'errore SQL
        }
        return null; // Restituisce null se non trova l'alimento o in caso di errore
    }
}
