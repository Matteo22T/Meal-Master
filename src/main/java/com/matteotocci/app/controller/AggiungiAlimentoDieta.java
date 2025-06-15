package com.matteotocci.app.controller; // Dichiara il package a cui appartiene questa classe.

// Importa le classi necessarie per l'applicazione JavaFX e le operazioni del database
import com.matteotocci.app.model.Alimento; // Importa la classe modello per l'oggetto Alimento.
import com.matteotocci.app.model.Ricetta; // Importa la classe modello per l'oggetto Ricetta.
import com.matteotocci.app.model.SQLiteConnessione; // Importa la classe per gestire la connessione al database SQLite.
import com.matteotocci.app.model.Session; // Importa la classe per la gestione della sessione utente (es. ID utente loggato).
import javafx.application.Platform; // Importa la classe Platform per eseguire codice sul thread di JavaFX UI.
import javafx.collections.FXCollections; // Importa Utility per creare collezioni osservabili (ObservableList).
import javafx.collections.ObservableList; // Importa la lista che notifica i "listener" quando avvengono dei cambiamenti.
import javafx.event.ActionEvent; // Importa il tipo di evento generato dalle azioni dell'utente (es. click su un bottone).
import javafx.fxml.FXML; // Importa l'annotazione per collegare elementi dell'interfaccia utente definiti in FXML al codice Java.
import javafx.fxml.FXMLLoader; // Importa la classe FXMLLoader per caricare file FXML (layout dell'interfaccia utente).
import javafx.fxml.Initializable; // Importa l'interfaccia Initializable, per i controller che devono essere inizializzati dopo il caricamento dell'FXML.
import javafx.geometry.Orientation; // Importa la classe Orientation per specificare l'orientamento di elementi come le ScrollBar.
import javafx.scene.Node; // Importa la classe base per tutti i nodi nel grafo della scena (elementi UI).
import javafx.scene.Parent; // Importa il nodo base per la gerarchia della scena (container di tutti gli elementi UI).
import javafx.scene.Scene; // Importa il contenitore per tutti i contenuti di una scena.
import javafx.scene.control.*; // Importa tutti i controlli UI standard di JavaFX (Button, ComboBox, CheckBox, TableView, TextField, Spinner, etc.).
import javafx.scene.control.cell.PropertyValueFactory; // Importa la classe per collegare le proprietà degli oggetti alle colonne di una TableView.
import javafx.scene.image.ImageView; // Importa la classe per visualizzare immagini.
import javafx.scene.input.MouseEvent; // Importa il tipo di evento generato da interazioni del mouse.
import javafx.stage.Stage; // Importa la finestra principale dell'applicazione.

import java.io.IOException; // Importa l'eccezione per errori di input/output (es. caricamento file FXML).
import java.net.URL; // Importa la classe URL, necessaria per Initializable.
import java.sql.Connection; // Importa l'interfaccia per la connessione al database.
import java.sql.PreparedStatement; // Importa la classe per eseguire query SQL precompilate.
import java.sql.ResultSet; // Importa la classe per leggere i risultati delle query SQL.
import java.sql.SQLException; // Importa l'eccezione per errori di database.
import java.sql.Statement; // Importa la classe per eseguire query SQL semplici.
import java.util.ResourceBundle; // Importa la classe ResourceBundle, necessaria per Initializable.

public class AggiungiAlimentoDieta implements Initializable { // Dichiara la classe AggiungiAlimentoDieta e implementa l'interfaccia Initializable.

    // --- Elementi dell'interfaccia utente (collegati tramite @FXML) ---
    // Colonne della TableView per gli Alimenti
    @FXML private TableColumn<Alimento, ImageView> immagineCol; // Dichiara la colonna per le immagini degli alimenti.
    @FXML private TableColumn<Alimento, String> nomeCol, brandCol; // Dichiarano le colonne per nome e brand degli alimenti.
    @FXML private TableColumn<Alimento, Double> calorieCol, proteineCol, carboidratiCol, grassiCol; // Dichiarano le colonne per calorie, proteine, carboidrati, grassi.
    @FXML private TableColumn<Alimento, Double> grassiSatCol, saleCol, fibreCol, zuccheriCol; // Dichiarano le colonne per grassi saturi, sale, fibre, zuccheri.

    // Bottoni per la ricerca
    @FXML private Button ButtonCercaAlimento; // Dichiara il bottone per la ricerca di alimenti.
    @FXML private Button ButtonCercaRicetta; // Dichiara il bottone per la ricerca di ricette.

    // ComboBox per la selezione della categoria (Alimenti e Ricette)
    @FXML private ComboBox<String> ComboBoxAlimento; // Dichiara la ComboBox per la categoria alimenti.
    @FXML private ComboBox<String> ComboBoxRicetta; // Dichiara la ComboBox per la categoria ricette.

    // CheckBox per filtrare "Solo i miei" alimenti/ricette
    @FXML private CheckBox CheckBoxAlimenti; // Dichiara la CheckBox per filtrare "Solo i miei" alimenti.
    @FXML private CheckBox CheckBoxRicette; // Dichiara la CheckBox per filtrare "Solo le mie" ricette.

    // Bottoni di conferma per aggiungere alla dieta
    @FXML private Button confermaAlimentiButton; // Dichiara il bottone per confermare l'aggiunta di alimenti.
    @FXML private Button confermaRicetteButton; // Dichiara il bottone per confermare l'aggiunta di ricette.

    // TableView per visualizzare alimenti e ricette
    @FXML private TableView<Alimento> tableViewAlimenti; // Dichiara la TableView per visualizzare gli alimenti.
    @FXML private TableView<Ricetta> tableViewRicette; // Dichiara la TableView per visualizzare le ricette.

    // Campi di testo per la ricerca per nome
    @FXML private TextField textCercaAlimento; // Dichiara il campo di testo per la ricerca di alimenti per nome.
    @FXML private TextField textCercaRicetta; // Dichiara il campo di testo per la ricerca di ricette per nome.

    // Colonne della TableView per le Ricette
    @FXML private TableColumn<Ricetta, String> nomeColRic; // Dichiara la colonna per il nome delle ricette.
    @FXML private TableColumn<Ricetta, String> descrizioneColRic; // Dichiara la colonna per la descrizione delle ricette.
    @FXML private TableColumn<Ricetta, String> categoriaColRic; // Dichiara la colonna per la categoria delle ricette.

    // Spinner per la selezione della quantità
    @FXML private Spinner<Integer> quantitaSpinner; // Dichiara lo Spinner per la selezione della quantità.

    // --- Variabili di stato interne ---
    private int offset = 0; // Inizializza l'offset per la paginazione delle ricerche (quanti elementi saltare).
    private final int LIMIT = 50; // Definisce il numero massimo di elementi da caricare per volta (per paginazione).

    // Riferimenti ad altri controller per la comunicazione
    private AggiungiGiornoDieta giornoDietaController; // Dichiara un riferimento al controller della schermata "Aggiungi Giorno Dieta".
    private String pastoCorrente; // Dichiara una variabile per memorizzare il tipo di pasto corrente (es. "Colazione").

    @FXML private Button btnRicette; // Dichiara il bottone per passare alla visualizzazione Ricette.
    @FXML private Button btnAlimenti; // Dichiara il bottone per passare alla visualizzazione Alimenti.


    // --- Metodi di comunicazione tra controller ---

    public void setGiornoDietaController(AggiungiGiornoDieta controller) { // Metodo per impostare il controller AggiungiGiornoDieta.
        this.giornoDietaController = controller; // Assegna il controller passato come parametro alla variabile di istanza.
    }

    public void setPastoCorrente(String pasto) { // Metodo per impostare il pasto corrente.
        this.pastoCorrente = pasto; // Assegna il pasto passato come parametro alla variabile di istanza.
    }

    @FXML
    private void mostraTabellaAlimenti(ActionEvent event) { // Metodo per mostrare la tabella degli alimenti, collegato ad un'azione FXML.
        highlightButton(btnAlimenti, btnRicette); // Chiama un metodo per evidenziare il bottone "Alimenti" e disattivare "Ricette".

        tableViewAlimenti.setVisible(true); // Rende visibile la tabella degli alimenti.
        tableViewRicette.setVisible(false); // Rende invisibile la tabella delle ricette.
        confermaAlimentiButton.setVisible(true); // Rende visibile il bottone di conferma per gli alimenti.
        confermaRicetteButton.setVisible(false); // Rende invisibile il bottone di conferma per le ricette.
        textCercaAlimento.setVisible(true); // Rende visibile il campo di testo per la ricerca alimenti.
        textCercaRicetta.setVisible(false); // Rende invisibile il campo di testo per la ricerca ricette.
        ButtonCercaAlimento.setVisible(true); // Rende visibile il bottone di ricerca alimenti.
        ButtonCercaRicetta.setVisible(false); // Rende invisibile il bottone di ricerca ricette.
        ComboBoxAlimento.setVisible(true); // Rende visibile la ComboBox alimenti.
        ComboBoxRicetta.setVisible(false); // Rende invisibile la ComboBox ricette.
        CheckBoxAlimenti.setVisible(true); // Rende visibile la CheckBox alimenti.
        CheckBoxRicette.setVisible(false); // Rende invisibile la CheckBox ricette.
    }
    @FXML
    private void mostraTabellaRicette(ActionEvent event) { // Metodo per mostrare la tabella delle ricette, collegato ad un'azione FXML.
        highlightButton(btnRicette, btnAlimenti); // Chiama un metodo per evidenziare il bottone "Ricette" e disattivare "Alimenti".


        tableViewAlimenti.setVisible(false); // Rende invisibile la tabella degli alimenti.
        tableViewRicette.setVisible(true); // Rende visibile la tabella delle ricette.
        confermaAlimentiButton.setVisible(false); // Rende invisibile il bottone di conferma per gli alimenti.
        confermaRicetteButton.setVisible(true); // Rende visibile il bottone di conferma per le ricette.
        textCercaAlimento.setVisible(false); // Rende invisibile il campo di testo per la ricerca alimenti.
        textCercaRicetta.setVisible(true); // Rende visibile il campo di testo per la ricerca ricette.
        ButtonCercaAlimento.setVisible(false); // Rende invisibile il bottone di ricerca alimenti.
        ButtonCercaRicetta.setVisible(true); // Rende visibile il bottone di ricerca ricette.
        ComboBoxAlimento.setVisible(false); // Rende invisibile la ComboBox alimenti.
        ComboBoxRicetta.setVisible(true); // Rende visibile la ComboBox ricette.
        CheckBoxAlimenti.setVisible(false); // Rende invisibile la CheckBox alimenti.
        CheckBoxRicette.setVisible(true); // Rende visibile la CheckBox ricette.

        // Quando si passa alla visualizzazione delle ricette, ricaricale
        offset = 0; // Reset dell'offset per iniziare una nuova ricerca dall'inizio.
        tableViewRicette.getItems().clear(); // Pulisci la tabella delle ricette.
        // Forza la selezione per mostrare le proprie ricette se l'utente è loggato
        if (Session.getUserId() != null) { // Controlla se l'ID utente della sessione non è nullo (utente loggato).
            CheckBoxRicette.setSelected(true); // Seleziona la checkbox "Solo le mie ricette".
        } else { // Altrimenti (utente non loggato).
            CheckBoxRicette.setSelected(false); // Deseleziona la checkbox.
        }
        cercaRicette(textCercaRicetta.getText(), false); // Avvia la ricerca delle ricette (non in append mode).
    }

    @Override // ANNOTAZIONE: Indica che questo metodo fa l'override di un metodo dell'interfaccia Initializable.
    public void initialize(URL location, ResourceBundle resources) { // Implementazione del metodo initialize dell'interfaccia Initializable.
        // Associa le proprietà dell'oggetto Alimento alle colonne della TableView degli Alimenti
        immagineCol.setCellValueFactory(new PropertyValueFactory<>("immagine")); // Collega la colonna 'immagineCol' alla proprietà "immagine" dell'Alimento.
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome")); // Collega la colonna 'nomeCol' alla proprietà "nome" dell'Alimento.
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand")); // Collega la colonna 'brandCol' alla proprietà "brand" dell'Alimento.
        calorieCol.setCellValueFactory(new PropertyValueFactory<>("kcal")); // Collega la colonna 'calorieCol' alla proprietà "kcal" dell'Alimento.
        proteineCol.setCellValueFactory(new PropertyValueFactory<>("proteine")); // Collega la colonna 'proteineCol' alla proprietà "proteine" dell'Alimento.
        carboidratiCol.setCellValueFactory(new PropertyValueFactory<>("carboidrati")); // Collega la colonna 'carboidratiCol' alla proprietà "carboidrati" dell'Alimento.
        grassiCol.setCellValueFactory(new PropertyValueFactory<>("grassi")); // Collega la colonna 'grassiCol' alla proprietà "grassi" dell'Alimento.
        grassiSatCol.setCellValueFactory(new PropertyValueFactory<>("grassiSaturi")); // Collega la colonna 'grassiSatCol' alla proprietà "grassiSaturi" dell'Alimento.
        saleCol.setCellValueFactory(new PropertyValueFactory<>("sale")); // Collega la colonna 'saleCol' alla proprietà "sale" dell'Alimento.
        fibreCol.setCellValueFactory(new PropertyValueFactory<>("fibre")); // Collega la colonna 'fibreCol' alla proprietà "fibre" dell'Alimento.
        zuccheriCol.setCellValueFactory(new PropertyValueFactory<>("zuccheri")); // Collega la colonna 'zuccheriCol' alla proprietà "zuccheri" dell'Alimento.

        // Associa le proprietà dell'oggetto Ricetta alle colonne della TableView delle Ricette
        nomeColRic.setCellValueFactory(new PropertyValueFactory<>("nome")); // Collega la colonna 'nomeColRic' alla proprietà "nome" della Ricetta.
        descrizioneColRic.setCellValueFactory(new PropertyValueFactory<>("descrizione")); // Collega la colonna 'descrizioneColRic' alla proprietà "descrizione" della Ricetta.
        categoriaColRic.setCellValueFactory(new PropertyValueFactory<>("categoria")); // Collega la colonna 'categoriaColRic' alla proprietà "categoria" della Ricetta.

        // Configura il comportamento di double-click sulle righe della tabella degli Alimenti
        tableViewAlimenti.setRowFactory(tv -> { // Imposta una factory per creare le righe della tabella degli alimenti.
            TableRow<Alimento> row = new TableRow<>(); // Crea una nuova riga della tabella.
            row.setOnMouseClicked(event -> { // Imposta un gestore di eventi per il click del mouse sulla riga.
                if (event.getClickCount() == 2 && !row.isEmpty()) { // Controlla se è un doppio click e se la riga non è vuota.
                    Alimento alimento = row.getItem(); // Ottiene l'oggetto Alimento associato alla riga.
                    apriDettaglio(alimento); // Chiama il metodo per aprire la finestra di dettaglio per l'alimento.
                }
            });
            return row; // Restituisce la riga configurata.
        });

        // Configura il comportamento di double-click sulla tabella delle Ricette (gestito da un metodo separato)
        tableViewRicette.setOnMouseClicked(this::apriDettaglioRicetta); // Imposta un gestore di eventi per il click del mouse sulla tabella ricette.

        popolaCategorie(); // Chiama il metodo per popolare le ComboBox delle categorie per alimenti e ricette.

        // Inizializza lo Spinner per la selezione della quantità
        // Valori da 1 a 1000, valore iniziale 100, editabile dall'utente
        quantitaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100)); // Imposta la factory di valori per lo Spinner.
        quantitaSpinner.setEditable(true); // Rende lo Spinner modificabile dall'utente.

        // --- Listener per la ricerca e il filtro ---
        // Ogni volta che la selezione nella ComboBox Alimento cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca degli alimenti.
        ComboBoxAlimento.setOnAction(e -> { // Imposta un gestore di eventi quando la selezione della ComboBox Alimento cambia.
            offset = 0; // Resetta l'offset di paginazione.
            tableViewAlimenti.getItems().clear(); // Pulisce gli elementi della tabella alimenti.
            cercaAlimenti(textCercaAlimento.getText(), false); // Esegue la ricerca degli alimenti (non in append mode).
        });

        // Ogni volta che lo stato della CheckBox Alimenti cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca degli alimenti.
        CheckBoxAlimenti.setOnAction(e -> { // Imposta un gestore di eventi quando lo stato della CheckBox Alimenti cambia.
            offset = 0; // Resetta l'offset di paginazione.
            tableViewAlimenti.getItems().clear(); // Pulisce gli elementi della tabella alimenti.
            cercaAlimenti(textCercaAlimento.getText(), false); // Esegue la ricerca degli alimenti (non in append mode).
        });

        // Ogni volta che la selezione nella ComboBox Ricetta cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca delle ricette.
        ComboBoxRicetta.setOnAction(e -> { // Imposta un gestore di eventi quando la selezione della ComboBox Ricetta cambia.
            offset = 0; // Resetta l'offset di paginazione.
            tableViewRicette.getItems().clear(); // Pulisce gli elementi della tabella ricette.
            cercaRicette(textCercaRicetta.getText(), false); // Esegue la ricerca delle ricette (non in append mode).
        });

        // Ogni volta che lo stato della CheckBox Ricette cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca delle ricette.
        CheckBoxRicette.setOnAction(e -> { // Imposta un gestore di eventi quando lo stato della CheckBox Ricette cambia.
            offset = 0; // Resetta l'offset di paginazione.
            tableViewRicette.getItems().clear(); // Pulisce gli elementi della tabella ricette.
            cercaRicette(textCercaRicetta.getText(), false); // Esegue la ricerca delle ricette (non in append mode).
        });

        // --- Gestione dello scroll per la paginazione infinita ---
        // Questo codice viene eseguito sul thread di JavaFX UI dopo l'inizializzazione completa
        Platform.runLater(() -> { // Esegue il blocco di codice sul thread dell'interfaccia utente di JavaFX.
            // Ottiene la ScrollBar verticale della tabella alimenti
            ScrollBar scrollBar = getVerticalScrollbar(tableViewAlimenti); // Ottiene la scrollbar verticale della tabella alimenti.
            if (scrollBar != null) { // Controlla se la scrollbar è stata trovata.
                // Aggiunge un listener alla proprietà 'value' della scrollbar
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> { // Aggiunge un listener al valore della scrollbar.
                    // Se il valore della scrollbar raggiunge il massimo (cioè, l'utente ha scrollato fino in fondo)
                    if (newVal.doubleValue() == scrollBar.getMax()) { // Controlla se la scrollbar è al suo valore massimo.
                        caricaAltriAlimenti(); // Chiama il metodo per caricare altri alimenti.
                    }
                });
            }
            // Ripete la stessa logica per la tabella ricette
            ScrollBar scrollBar2 = getVerticalScrollbar(tableViewRicette); // Ottiene la scrollbar verticale della tabella ricette.
            if (scrollBar2 != null) { // Controlla se la scrollbar è stata trovata.
                scrollBar2.valueProperty().addListener((obs, oldVal, newVal) -> { // Aggiunge un listener al valore della scrollbar.
                    if (newVal.doubleValue() == scrollBar2.getMax()) { // Controlla se la scrollbar è al suo valore massimo.
                        caricaAltreRicette(); // Chiama il metodo per caricare altre ricette.
                    }
                });
            }
        });

        // Carica inizialmente gli alimenti (senza filtro di ricerca e senza append)
        cercaAlimenti("", false); // Carica inizialmente gli alimenti senza filtro e non in append mode.
        // All'inizializzazione, se un utente è loggato, forza la selezione della checkbox "Solo le mie ricette"
        if (Session.getUserId() != null) { // Controlla se l'utente è loggato.
            CheckBoxRicette.setSelected(true); // Seleziona la checkbox "Solo le mie ricette".
        }
        // Carica inizialmente le ricette (senza filtro di ricerca e senza append)
        // La condizione della checkbox "Solo le mie ricette" verrà applicata da cercaRicette()
        cercaRicette("", false); // Carica inizialmente le ricette senza filtro e non in append mode.

        highlightButton(btnAlimenti, btnRicette); // Chiama il metodo per evidenziare il bottone degli alimenti all'avvio.

    }

    // --- Metodi di ricerca ---

    /**
     * Gestisce l'evento di click sul bottone "Cerca" per gli alimenti.
     * Resetta l'offset e la tabella, poi esegue una nuova ricerca.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleCercaAlimento(ActionEvent event) { // Metodo per gestire il click sul bottone di ricerca alimenti.
        offset = 0; // Resetta l'offset di paginazione a 0.
        tableViewAlimenti.getItems().clear(); // Pulisce tutti gli elementi dalla tabella degli alimenti.
        String filtro = textCercaAlimento.getText(); // Ottiene il testo inserito nel campo di ricerca alimenti.
        cercaAlimenti(filtro, false); // Chiama il metodo per cercare gli alimenti con il filtro specificato e non in modalità append.
    }

    /**
     * Esegue la ricerca degli alimenti nel database.
     * @param filtro Il testo da cercare nel nome dell'alimento.
     * @param append Se true, aggiunge i nuovi risultati alla lista esistente; altrimenti, crea una nuova lista.
     */
    private void cercaAlimenti(String filtro, boolean append) { // Metodo per cercare alimenti nel database.
        // Se append è true, usa la lista esistente della tabella; altrimenti, crea una nuova lista
        ObservableList<Alimento> alimenti = append ? tableViewAlimenti.getItems() : FXCollections.observableArrayList(); // Crea una lista osservabile o usa quella esistente a seconda del parametro append.

        String categoria = ComboBoxAlimento.getSelectionModel().getSelectedItem(); // Ottiene la categoria selezionata dalla ComboBox degli alimenti.
        boolean soloMiei = CheckBoxAlimenti.isSelected(); // Controlla lo stato della checkbox "Solo i miei" alimenti.

        // Costruisce dinamicamente la query SQL
        StringBuilder query = new StringBuilder("SELECT * FROM foods WHERE LOWER(nome) LIKE ?"); // Inizia a costruire la query SQL per selezionare gli alimenti con nome simile al filtro (case-insensitive).
        if (categoria != null && !categoria.equals("Tutte")) { // Se una categoria è selezionata e non è "Tutte".
            query.append(" AND categoria = ?"); // Aggiunge una condizione per la categoria alla query.
        }
        if (soloMiei && Session.getUserId() != null) { // Se la checkbox "Solo i miei" è selezionata e un utente è loggato.
            query.append(" AND user_id = ?"); // Aggiunge una condizione per l'ID utente alla query.
        }
        query.append(" LIMIT ? OFFSET ?"); // Aggiunge le clausole LIMIT e OFFSET per la paginazione.

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database tramite la classe SQLiteConnessione.
             PreparedStatement stmt = conn.prepareStatement(query.toString())) { // Prepara uno statement SQL precompilato con la query costruita.

            int paramIndex = 1; // Inizializza l'indice per i parametri dello statement.
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%"); // Imposta il primo parametro: il filtro di ricerca per il nome (case-insensitive).
            if (categoria != null && !categoria.equals("Tutte")) { // Se la condizione per la categoria è stata aggiunta alla query.
                stmt.setString(paramIndex++, categoria); // Imposta il parametro per la categoria.
            }
            if (soloMiei && Session.getUserId() != null) { // Se la condizione per l'ID utente è stata aggiunta alla query.
                stmt.setInt(paramIndex++, Session.getUserId()); // Imposta il parametro per l'ID utente.
            }
            stmt.setInt(paramIndex++, LIMIT); // Imposta il parametro LIMIT (numero massimo di risultati da restituire).
            stmt.setInt(paramIndex++, offset); // Imposta il parametro OFFSET (numero di risultati da saltare).

            try (ResultSet rs = stmt.executeQuery()) { // Esegue la query e ottiene il ResultSet.
                while (rs.next()) { // Itera su ogni riga del ResultSet.
                    // Crea un nuovo oggetto Alimento con i dati dal database e lo aggiunge alla lista
                    alimenti.add(new Alimento( // Aggiunge un nuovo oggetto Alimento alla lista.
                            rs.getString("nome"), // Ottiene il valore della colonna "nome".
                            rs.getString("brand"), // Ottiene il valore della colonna "brand".
                            rs.getDouble("kcal"), // Ottiene il valore della colonna "kcal".
                            rs.getDouble("proteine"), // Ottiene il valore della colonna "proteine".
                            rs.getDouble("carboidrati"), // Ottiene il valore della colonna "carboidrati".
                            rs.getDouble("grassi"), // Ottiene il valore della colonna "grassi".
                            rs.getDouble("grassiSaturi"), // Ottiene il valore della colonna "grassiSaturi".
                            rs.getDouble("sale"), // Ottiene il valore della colonna "sale".
                            rs.getDouble("fibre"), // Ottiene il valore della colonna "fibre".
                            rs.getDouble("zuccheri"), // Ottiene il valore della colonna "zuccheri".
                            rs.getString("immaginePiccola"), // Ottiene il valore della colonna "immaginePiccola".
                            rs.getString("immagineGrande"), // Ottiene il valore della colonna "immagineGrande".
                            rs.getInt("user_id"), // Ottiene il valore della colonna "user_id".
                            rs.getInt("id") // Ottiene il valore della colonna "id".
                    ));
                }

                tableViewAlimenti.setItems(alimenti); // Aggiorna gli elementi visualizzati nella TableView degli alimenti.
                offset += LIMIT; // Incrementa l'offset per la prossima paginazione.
            }

        } catch (SQLException e) { // Cattura le eccezioni SQL.
            e.printStackTrace(); // Stampa l'errore SQL sulla console.
        }
    }

    private boolean isLoading = false; // Flag per prevenire carichi multipli durante lo scroll.

    /**
     * Carica altri alimenti quando l'utente raggiunge il fondo della scrollbar.
     * Utilizza il flag `isLoading` per evitare richieste duplicate.
     */
    private void caricaAltriAlimenti() { // Metodo per caricare più alimenti durante lo scroll.
        if (isLoading) return; // Se un caricamento è già in corso, esce dal metodo per evitare duplicati.
        isLoading = true; // Imposta il flag per indicare che un caricamento è in corso.
        String filtro = textCercaAlimento.getText(); // Ottiene il testo del filtro di ricerca corrente.
        cercaAlimenti(filtro, true); // Esegue la ricerca degli alimenti in modalità append (aggiungendo ai risultati esistenti).
        isLoading = false; // Resetta il flag una volta che il caricamento è completato.
    }

    private boolean isLoading2 = false; // Flag per prevenire carichi multipli per le ricette.

    /**
     * Carica altre ricette quando l'utente raggiunge il fondo della scrollbar.
     * @see #caricaAltriAlimenti()
     */
    private void caricaAltreRicette() { // Metodo per caricare più ricette durante lo scroll.
        if (isLoading2) return; // Se un caricamento è già in corso, esce dal metodo.
        isLoading2 = true; // Imposta il flag per indicare che un caricamento è in corso.
        String filtro = textCercaRicetta.getText(); // Ottiene il testo del filtro di ricerca corrente per le ricette.
        cercaRicette(filtro, true); // Esegue la ricerca delle ricette in modalità append.
        isLoading2 = false; // Resetta il flag una volta che il caricamento è completato.
    }

    /**
     * Gestisce l'evento di click sul bottone "Cerca" per le ricette.
     * Resetta l'offset e la tabella, poi esegue una nuova ricerca.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleCercaRicetta(ActionEvent event) { // Metodo per gestire il click sul bottone di ricerca ricette.
        offset = 0; // Resetta l'offset di paginazione a 0.
        tableViewRicette.getItems().clear(); // Pulisce tutti gli elementi dalla tabella delle ricette.
        cercaRicette(textCercaRicetta.getText(), false); // Chiama il metodo per cercare le ricette con il filtro specificato e non in modalità append.
    }

    public void cercaRicette(String filtro, boolean append) { // Metodo per cercare ricette nel database.
        // Se append è true, usa la lista esistente; altrimenti, crea una nuova lista
        ObservableList<Ricetta> ricette = append ? tableViewRicette.getItems() : FXCollections.observableArrayList(); // Crea una lista osservabile o usa quella esistente a seconda del parametro append.

        String categoria = ComboBoxRicetta.getSelectionModel().getSelectedItem(); // Ottiene la categoria selezionata dalla ComboBox delle ricette.
        boolean soloMiei = CheckBoxRicette.isSelected(); // Controlla lo stato della checkbox "Solo i miei" ricette.

        // Costruisce dinamicamente la query SQL per le ricette
        StringBuilder query = new StringBuilder("SELECT * FROM Ricette WHERE LOWER(nome) LIKE ?"); // Inizia a costruire la query SQL per selezionare le ricette con nome simile al filtro (case-insensitive).
        // Aggiungi una condizione per la categoria solo se non è "Tutte"
        if (categoria != null && !categoria.equals("Tutte")) { // Se una categoria è selezionata e non è "Tutte".
            query.append(" AND categoria = ?"); // Aggiunge una condizione per la categoria alla query.
        }
        // Aggiungi una condizione per l'id_utente solo se la checkbox "Solo le mie ricette" è selezionata e l'utente è loggato
        if (soloMiei && Session.getUserId() != null) { // Se la checkbox "Solo le mie ricette" è selezionata e un utente è loggato.
            query.append(" AND id_utente = ?"); // Aggiunge una condizione per l'ID utente alla query.
        }
        query.append(" LIMIT ? OFFSET ?"); // Aggiunge le clausole LIMIT e OFFSET per la paginazione.

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
             PreparedStatement stmt = conn.prepareStatement(query.toString())) { // Prepara uno statement SQL precompilato.

            int paramIndex = 1; // Inizializza l'indice per i parametri dello statement.
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%"); // Imposta il primo parametro: il filtro di ricerca per il nome (case-insensitive).
            // Applica il parametro per la categoria solo se la condizione è stata aggiunta
            if (categoria != null && !categoria.equals("Tutte")) { // Se la condizione per la categoria è stata aggiunta alla query.
                stmt.setString(paramIndex++, categoria); // Imposta il parametro per la categoria.
            }
            // Applica il parametro per l'id_utente solo se la condizione è stata aggiunta
            if (soloMiei && Session.getUserId() != null) { // Se la condizione per l'ID utente è stata aggiunta alla query.
                stmt.setInt(paramIndex++, Session.getUserId()); // Imposta il parametro per l'ID utente.
            }
            stmt.setInt(paramIndex++, LIMIT); // Imposta il parametro LIMIT.
            stmt.setInt(paramIndex++, offset); // Imposta il parametro OFFSET.

            try (ResultSet rs = stmt.executeQuery()) { // Esegue la query e ottiene il ResultSet.
                while (rs.next()) { // Itera su ogni riga del ResultSet.
                    // Crea un nuovo oggetto Ricetta con i dati dal database e lo aggiunge alla lista
                    Ricetta ricetta = new Ricetta( // Crea un nuovo oggetto Ricetta.
                            rs.getInt("id"), // Ottiene il valore della colonna "id".
                            rs.getString("nome"), // Ottiene il valore della colonna "nome".
                            rs.getString("descrizione"), // Ottiene il valore della colonna "descrizione".
                            rs.getString("categoria"), // Ottiene il valore della colonna "categoria".
                            rs.getInt("id_utente"), // Ottiene il valore della colonna "id_utente".
                            rs.getDouble("kcal"), // Ottiene il valore della colonna "kcal".
                            rs.getDouble("proteine"), // Ottiene il valore della colonna "proteine".
                            rs.getDouble("carboidrati"), // Ottiene il valore della colonna "carboidrati".
                            rs.getDouble("grassi"), // Ottiene il valore della colonna "grassi".
                            rs.getDouble("grassi_saturi"), // Ottiene il valore della colonna "grassi_saturi".
                            rs.getDouble("zuccheri"), // Ottiene il valore della colonna "zuccheri".
                            rs.getDouble("fibre"), // Ottiene il valore della colonna "fibre".
                            rs.getDouble("sale") // Ottiene il valore della colonna "sale".
                    );
                    ricette.add(ricetta); // Aggiunge la ricetta alla lista.
                }

                tableViewRicette.setItems(ricette); // Aggiorna gli elementi visualizzati nella TableView delle ricette.
                offset += LIMIT; // Incrementa l'offset per la prossima paginazione.
            }

        } catch (SQLException e) { // Cattura le eccezioni SQL.
            e.printStackTrace(); // Stampa l'errore SQL sulla console.
        }
    }

    @FXML
    private void apriDettaglioRicetta(MouseEvent event) { // Metodo per aprire la finestra di dettaglio di una ricetta al double-click.
        // Controlla se è un double-click e se una riga è selezionata
        if (event.getClickCount() == 2 && !tableViewRicette.getSelectionModel().isEmpty()) { // Controlla se è un doppio click e se una ricetta è selezionata nella tabella.
            Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem(); // Ottiene la ricetta selezionata dalla tabella.
            try { // Inizia un blocco try-catch per la gestione di IOException.
                // Carica il file FXML della schermata DettaglioRicetta
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettaglioRicetta.fxml")); // Crea un FXMLLoader per caricare il file FXML di DettaglioRicetta.
                Parent root = loader.load(); // Carica la gerarchia di nodi dall'FXML.

                DettaglioRicettaController controller = loader.getController(); // Ottiene il controller associato al FXML caricato.
                controller.setRicetta(ricettaSelezionata); // Passa la ricetta selezionata al controller della finestra di dettaglio.
                controller.setOrigineFXML("AggiungiAlimentoDieta.fxml"); // Indica la schermata di origine al controller di dettaglio.

                Stage stage = new Stage(); // Crea un nuovo Stage (finestra) per il dettaglio.
                stage.setTitle("Dettaglio Ricetta"); // Imposta il titolo della finestra.
                stage.setScene(new Scene(root)); // Imposta la Scene (contenuto) della finestra.
                stage.setFullScreen(false); // Disabilita la modalità a schermo intero.
                stage.setResizable(false); // Disabilita la possibilità di ridimensionare la finestra.
                stage.show(); // Mostra la finestra di dettaglio.
            } catch (IOException e) { // Cattura le eccezioni di input/output.
                e.printStackTrace(); // Stampa lo stack trace dell'errore.
            }
        }
    }

    private ScrollBar getVerticalScrollbar(TableView<?> table) { // Metodo per ottenere la scrollbar verticale di una TableView.
        // Cerca tra tutti i nodi con la classe CSS ".scroll-bar"
        for (Node node : table.lookupAll(".scroll-bar")) { // Itera su tutti i nodi figli della tabella che hanno la classe CSS ".scroll-bar".
            if (node instanceof ScrollBar) { // Controlla se il nodo corrente è un'istanza di ScrollBar.
                ScrollBar sb = (ScrollBar) node; // Effettua il casting del nodo a ScrollBar.
                if (sb.getOrientation() == Orientation.VERTICAL) { // Controlla se l'orientamento della scrollbar è verticale.
                    return sb; // Restituisce la scrollbar verticale trovata.
                }
            }
        }
        return null; // Restituisce null se nessuna scrollbar verticale è stata trovata.
    }

    private void highlightButton(Button active, Button inactive) { // Metodo per evidenziare un bottone e disattivare un altro.
        // Se il bottone attivo ha già lo stile "bottoneAttivo", non fare nulla per evitare refresh inutili
        if(active.getStyleClass().contains("bottoneAttivo")) { // Controlla se il bottone attivo ha già lo stile "bottoneAttivo".
            return; // Se sì, esce dal metodo.
        }
        // Rimuove gli stili "bottoneSpento" dal bottone attivo e "bottoneAttivo" dal bottone inattivo
        active.getStyleClass().remove("bottoneSpento"); // Rimuove lo stile "bottoneSpento" dal bottone attivo.
        inactive.getStyleClass().remove("bottoneAttivo"); // Rimuove lo stile "bottoneAttivo" dal bottone inattivo.

        // Aggiunge gli stili "bottoneAttivo" al bottone attivo e "bottoneSpento" al bottone inattivo
        active.getStyleClass().add("bottoneAttivo"); // Aggiunge lo stile "bottoneAttivo" al bottone attivo.
        inactive.getStyleClass().add("bottoneSpento"); // Aggiunge lo stile "bottoneSpento" al bottone inattivo.
    }

    private void apriDettaglio(Alimento alimento) { // Metodo per aprire la finestra di dettaglio di un alimento.
        try { // Inizia un blocco try-catch per la gestione di IOException.
            // Carica il file FXML della schermata DettagliAlimento
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettagliAlimento.fxml")); // Crea un FXMLLoader per caricare il file FXML di DettagliAlimento.
            Parent root = loader.load(); // Carica la gerarchia di nodi dall'FXML.

            DettagliAlimentoController controller = loader.getController(); // Ottiene il controller associato al FXML caricato.
            controller.setAlimento(alimento); // Passa l'alimento selezionato al controller della finestra di dettaglio.
            controller.setOrigineFXML("AggiungAlimentoDieta.fxml"); // Indica la schermata di origine al controller di dettaglio.

            Stage stage = new Stage(); // Crea un nuovo Stage (finestra) per il dettaglio.
            stage.setTitle("Dettaglio Alimento"); // Imposta il titolo della finestra.
            stage.setScene(new Scene(root)); // Imposta la Scene (contenuto) della finestra.
            stage.setResizable(false); // Disabilita la possibilità di ridimensionare la finestra.
            stage.setFullScreen(false); // Disabilita la modalità a schermo intero.
            stage.show(); // Mostra la finestra di dettaglio.
        } catch (IOException e) { // Cattura le eccezioni di input/output.
            e.printStackTrace(); // Stampa lo stack trace dell'errore.
        }
    }

    private void popolaCategorie() { // Metodo per popolare le ComboBox delle categorie.
        // Query per le categorie degli alimenti
        String queryAlimenti = "SELECT DISTINCT categoria FROM foods WHERE categoria IS NOT NULL"; // Definisce la query SQL per ottenere le categorie distinte dagli alimenti.
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
             Statement stmt = conn.createStatement(); // Crea uno statement SQL.
             ResultSet rs = stmt.executeQuery(queryAlimenti)) { // Esegue la query e ottiene il ResultSet.

            ObservableList<String> categorieAlimenti = FXCollections.observableArrayList(); // Crea una nuova lista osservabile per le categorie degli alimenti.
            categorieAlimenti.add("Tutte"); // Aggiunge l'opzione "Tutte" come prima voce.
            while (rs.next()) { // Itera su ogni riga del ResultSet.
                categorieAlimenti.add(rs.getString("categoria")); // Aggiunge la categoria corrente alla lista.
            }
            ComboBoxAlimento.setItems(categorieAlimenti); // Imposta gli elementi nella ComboBox degli alimenti.
            ComboBoxAlimento.getSelectionModel().selectFirst(); // Seleziona il primo elemento ("Tutte") di default.
        } catch (SQLException e) { // Cattura le eccezioni SQL.
            e.printStackTrace(); // Stampa lo stack trace dell'errore.
        }

        // Popola ComboBoxRicetta con categorie dalla tabella Ricette
        ObservableList<String> categorieRicetteList = FXCollections.observableArrayList(); // Crea una nuova lista osservabile per le categorie delle ricette.
        categorieRicetteList.add("Tutte"); // Aggiunge "Tutte" come prima opzione.

        String queryRicette = "SELECT DISTINCT categoria FROM Ricette WHERE categoria IS NOT NULL"; // Definisce la query SQL per ottenere le categorie distinte dalle ricette.
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
             Statement stmt = conn.createStatement(); // Crea uno statement SQL.
             ResultSet rs = stmt.executeQuery(queryRicette)) { // Esegue la query e ottiene il ResultSet.
            while (rs.next()) { // Itera su ogni riga del ResultSet.
                categorieRicetteList.add(rs.getString("categoria")); // Aggiunge la categoria corrente alla lista.
            }
        } catch (SQLException e) { // Cattura le eccezioni SQL.
            e.printStackTrace(); // Stampa lo stack trace dell'errore.
        }
        ComboBoxRicetta.setItems(categorieRicetteList); // Imposta gli elementi nella ComboBox delle ricette.
        ComboBoxRicetta.getSelectionModel().selectFirst(); // Seleziona "Tutte" di default.
    }

    @FXML
    private void confermaAlimenti(ActionEvent event) { // Metodo per confermare l'aggiunta di alimenti alla dieta.
        Alimento alimentoSelezionato = tableViewAlimenti.getSelectionModel().getSelectedItem(); // Ottiene l'alimento selezionato dalla tabella.
        Integer quantita = quantitaSpinner.getValue(); // Recupera il valore della quantità dallo Spinner.

        // Verifica che un alimento sia selezionato, che il controller genitore sia disponibile,
        // che il pasto corrente sia definito e che la quantità sia valida.
        if (alimentoSelezionato != null && giornoDietaController != null && pastoCorrente != null && quantita != null && quantita > 0) { // Controlla se le condizioni per l'aggiunta sono soddisfatte.
            // Chiama un metodo nel controller AggiungiGiornoDieta per aggiungere l'alimento
            giornoDietaController.aggiungiAlimentoAllaLista(pastoCorrente, alimentoSelezionato, quantita); // Chiama il metodo per aggiungere l'alimento alla lista nel controller della dieta.
            // La finestra rimane aperta ora, come da richiesta precedente (implicita, dato che non c'è chiusura)
        } else { // Se le condizioni non sono soddisfatte.
            System.out.println("Seleziona un alimento e specifica una quantità valida!"); // Stampa un messaggio di errore sulla console.
            // Si potrebbe aggiungere una showAlert qui per l'utente
        }
    }

    @FXML
    private void confermaRicette(ActionEvent event) { // Metodo per confermare l'aggiunta di ricette alla dieta.
        Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem(); // Ottiene la ricetta selezionata dalla tabella.
        Integer quantita = quantitaSpinner.getValue(); // Recupera il valore della quantità dallo Spinner.

        // Verifica che una ricetta sia selezionata, che il controller genitore sia disponibile,
        // che il pasto corrente sia definito e che la quantità sia valida.
        if (ricettaSelezionata != null && giornoDietaController != null && pastoCorrente != null && quantita != null && quantita > 0) { // Controlla se le condizioni per l'aggiunta sono soddisfatte.
            // Chiama un metodo specifico nel controller AggiungiGiornoDieta per aggiungere la ricetta
            // (solo alla UI e alla memoria in questo momento, non al DB)
            giornoDietaController.aggiungiRicettaAllaLista(ricettaSelezionata, quantita, pastoCorrente); // Chiama il metodo per aggiungere la ricetta alla lista nel controller della dieta.

            // Chiude la finestra corrente dopo la conferma
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage corrente dalla sorgente dell'evento.
            stage.close(); // Chiude la finestra corrente.
        } else { // Se le condizioni non sono soddisfatte.
            System.out.println("Seleziona una ricetta e specifica una quantità valida!"); // Stampa un messaggio di errore sulla console.
            // Si potrebbe aggiungere una showAlert qui per l'utente
        }
    }

    private Alimento getAlimentoFromRicetta(Ricetta ricetta) { // Metodo per ottenere un oggetto Alimento da un oggetto Ricetta.
        // Query per recuperare i dettagli di un alimento con lo stesso nome della ricetta
        String query = "SELECT * FROM foods WHERE nome = ?"; // Definisce la query SQL per selezionare un alimento per nome.
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
             PreparedStatement stmt = conn.prepareStatement(query)) { // Prepara uno statement SQL precompilato.
            stmt.setString(1, ricetta.getNome()); // Imposta il nome della ricetta come parametro per la query.
            ResultSet rs = stmt.executeQuery(); // Esegue la query e ottiene il ResultSet.
            if (rs.next()) { // Controlla se c'è almeno un risultato.
                // Crea e restituisce un nuovo oggetto Alimento con i dati recuperati
                return new Alimento( // Restituisce un nuovo oggetto Alimento.
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
