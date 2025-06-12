package com.matteotocci.app.controller;

import com.matteotocci.app.model.DatiClienteModel; // Modello per la gestione dei dati del cliente (interazione con il database)
import com.matteotocci.app.model.SQLiteConnessione; // Classe per gestire la connessione al database SQLite
import javafx.collections.FXCollections; // Utility per creare collezioni osservabili (liste per ChoiceBox)
import javafx.collections.ObservableList; // Lista che notifica i "listener" quando avvengono dei cambiamenti
import javafx.event.ActionEvent; // Tipo di evento generato dalle azioni dell'utente (es. click su un bottone)
import javafx.fxml.FXML; // Annotazione per collegare elementi dell'interfaccia utente definiti in FXML al codice Java
import javafx.fxml.FXMLLoader; // Carica file FXML (layout dell'interfaccia utente)
import javafx.fxml.Initializable;
import javafx.scene.Parent; // Nodo base per la gerarchia della scena (container di tutti gli elementi UI)
import javafx.scene.Scene; // Contenitore per tutti i contenuti di una scena
import javafx.scene.control.*; // Controlli UI standard di JavaFX (Button, DatePicker, ChoiceBox, Label, Slider, Alert)
import javafx.scene.input.KeyCode; // Codici dei tasti della tastiera
import javafx.scene.input.KeyEvent; // Tipo di evento generato dalla pressione di un tasto
import javafx.scene.layout.VBox; // Layout container che organizza i suoi figli in una singola colonna verticale
import javafx.stage.Stage; // La finestra principale dell'applicazione

import java.io.IOException; // Eccezione per errori di input/output (es. caricamento file FXML)
import java.net.URL; // Classe che rappresenta un Uniform Resource Locator (per trovare risorse come file CSS)
import java.sql.Connection; // Interfaccia per la connessione al database
import java.sql.PreparedStatement; // Per eseguire query SQL precompilate
import java.sql.ResultSet; // Per leggere i risultati delle query SQL
import java.sql.SQLException; // Eccezione per errori di database
import java.time.LocalDate; // Per gestire le date
import java.util.HashMap; // Implementazione di Map per associare nomi a ID
import java.util.Map; // Interfaccia per mappe
import java.util.ResourceBundle;

/**
 * Controller per la schermata di inserimento dei dati aggiuntivi del cliente.
 * Questa schermata appare dopo la registrazione di un nuovo utente di tipo "cliente".
 */
public class DatiCliente implements Initializable {
    // --- Elementi dell'interfaccia utente (collegati tramite @FXML) ---

    @FXML private DatePicker datadinascitaPicker; // Selettore data per la data di nascita
    @FXML private ChoiceBox<String> livelloattivitàBox; // Dropdown per il livello di attività fisica
    @FXML private ChoiceBox<String> nutrizionistaBox; // Dropdown per selezionare un nutrizionista
    @FXML private Label altezzaLabel; // Etichetta che mostra il valore dello slider dell'altezza
    @FXML private Label pesoLabel; // Etichetta che mostra il valore dello slider del peso
    @FXML private Button BottoneConferma; // Bottone per confermare l'inserimento dei dati
    @FXML private Slider altezzaSlider; // Slider per selezionare l'altezza
    @FXML private Slider pesoSlider; // Slider per selezionare il peso
    @FXML private ChoiceBox<String> genereBox; // Dropdown per selezionare il genere
    @FXML private VBox registerBox; // Contenitore VBox principale della schermata, utilizzato per la gestione degli eventi da tastiera

    // --- Variabili di stato e Modelli ---

    // Mappa per associare il nome completo del nutrizionista al suo ID (utilizzato per il salvataggio nel DB)
    private Map<String, Integer> mappaNutrizionisti = new HashMap<>();
    // ID dell'utente attualmente registrato (passato dalla schermata di login/registrazione)
    private int idUtente;
    // Istanza del modello DatiClienteModel per gestire la logica di business e l'interazione con il database
    public DatiClienteModel datiCliente = new DatiClienteModel();

    /**
     * Metodo per impostare l'ID dell'utente.
     * Questo ID viene passato dalla schermata di registrazione per associare
     * i dati del cliente all'utente appena creato.
     * @param idUtente L'ID dell'utente registrato.
     */
    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
        System.out.println("Id utente ricevuto: " + idUtente); // Debug: stampa l'ID ricevuto per verifica
    }

    /**
     * Recupera la lista dei nutrizionisti dal database.
     * Questa lista viene utilizzata per popolare la ChoiceBox 'nutrizionistaBox'.
     * Associa anche il nome del nutrizionista al suo ID in 'mappaNutrizionisti'.
     * @return Una ObservableList di stringhe contenente i nomi dei nutrizionisti.
     */
    private ObservableList<String> getNutrizionisti() {
        ObservableList<String> nutrizionisti = FXCollections.observableArrayList(); // Crea una lista osservabile
        // Query SQL per selezionare ID, Nome e Cognome di tutti gli utenti con ruolo 'nutrizionista'
        String query = "SELECT id,Nome, Cognome FROM Utente WHERE ruolo = 'nutrizionista'";

        // Utilizza un blocco try-with-resources per garantire che le risorse del database siano chiuse automaticamente
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database
             PreparedStatement stmt = conn.prepareStatement(query); // Prepara la statement SQL
             ResultSet rs = stmt.executeQuery()) { // Esegue la query e ottiene i risultati

            while (rs.next()) { // Itera su ogni riga del ResultSet
                int id = rs.getInt("id"); // Recupera l'ID del nutrizionista
                String nomeCompleto = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena Nome e Cognome
                nutrizionisti.add(nomeCompleto); // Aggiunge il nome completo alla lista per la ChoiceBox
                mappaNutrizionisti.put(nomeCompleto, id); // Salva l'associazione nome completo -> ID nella mappa
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore in caso di problemi con il database
        }
        return nutrizionisti; // Restituisce la lista dei nutrizionisti
    }

    /**
     * Metodo di inizializzazione chiamato automaticamente da FXMLLoader.
     * Questo metodo viene eseguito dopo che tutti gli elementi FXML sono stati caricati e iniettati.
     * Qui si popolano i ChoiceBox, si configurano i listener per gli slider e si impostano i listener per la tastiera.
     * Nota: A differenza del controller di login, questa classe non implementa 'Initializable',
     * ma un metodo @FXML initialize() può essere chiamato se presente e non ci sono dipendenze da Initializable
     * per il caricamento iniziale dei dati. Tuttavia, è una pratica più robusta implementare 'Initializable'.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Popola la ChoiceBox dei nutrizionisti
        ObservableList<String> nutrizionisti = getNutrizionisti();
        nutrizionistaBox.setItems(nutrizionisti);

        // Listener per lo slider dell'altezza: aggiorna l'etichetta dell'altezza in tempo reale
        altezzaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double valore = newValue.doubleValue();
            altezzaLabel.setText(String.format("%.0f cm", valore)); // Formatta il valore senza decimali
        });

        // Listener per lo slider del peso: aggiorna l'etichetta del peso in tempo reale
        pesoSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double valore = newValue.doubleValue();
            pesoLabel.setText(String.format("%.0f kg", valore)); // Formatta il valore senza decimali
        });

        // Popola la ChoiceBox del genere
        ObservableList<String> opzioniSesso = FXCollections.observableArrayList(
                "Maschio",
                "Femmina",
                "Altro"
        );
        genereBox.setItems(opzioniSesso);

        // Popola la ChoiceBox del livello di attività
        ObservableList<String> livelliAttivita = FXCollections.observableArrayList(
                "Sedentario",
                "Leggermente Attivo",
                "Moderatamente Attivo",
                "Molto Attivo",
                "Estremamente Attivo"
        );
        livelloattivitàBox.setItems(livelliAttivita);

        // Aggiungi un listener di eventi al VBox principale per intercettare il tasto "Invio".
        // Questo permette di premere Invio per confermare i dati da qualsiasi punto del VBox.
        if (registerBox != null) { // Verifica che il VBox sia stato correttamente iniettato
            registerBox.setOnKeyPressed(this::handleEnterKeyPressed);
        } else {
            System.err.println("Errore: registerBox non è stato iniettato!"); // Stampa un errore se non è iniettato
        }
    }

    /**
     * Gestisce la pressione del tasto INVIO (ENTER) all'interno del VBox principale.
     * Se l'utente preme INVIO, simula un click sul bottone di conferma.
     * @param event L'evento di pressione del tasto.
     */
    @FXML
    private void handleEnterKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) { // Controlla se il tasto premuto è INVIO
            confermaDati(new ActionEvent()); // Chiama il metodo di conferma dati, creando un ActionEvent fittizio
            event.consume(); // Impedisce che l'evento si propaghi ulteriormente e venga gestito da altri elementi
        }
    }

    /**
     * Metodo chiamato quando l'utente clicca il bottone "Conferma".
     * Esegue la validazione dei dati inseriti e, se validi, li registra nel database.
     * In caso di successo, naviga alla pagina di conferma registrazione.
     * @param event L'evento di azione.
     */
    @FXML
    private void confermaDati(ActionEvent event) {
        // Controlla che tutti i campi FXML critici siano stati iniettati correttamente.
        // Questo è un controllo di sicurezza per prevenire NullPointerException.
        if (altezzaSlider == null || pesoSlider == null || datadinascitaPicker == null ||
                livelloattivitàBox == null || nutrizionistaBox == null || genereBox == null) { // Aggiunto genereBox qui
            System.err.println("Errore: Campi FXML non inizializzati nel controller!");
            showAlert(Alert.AlertType.ERROR, "Errore Interno", "Errore nell'interfaccia utente.");
            return;
        }

        // --- Validazione dei campi di input ---
        // Verifica che gli slider non siano ai loro valori minimi predefiniti (spesso 0, indicando non selezionato)
        if (altezzaSlider.getValue() == altezzaSlider.getMin()) { // Controlla se il valore è il minimo dello slider
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci la tua altezza.");
            return;
        }
        if (pesoSlider.getValue() == pesoSlider.getMin()) { // Controlla se il valore è il minimo dello slider
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci il tuo peso.");
            return;
        }
        if (livelloattivitàBox.getValue() == null || livelloattivitàBox.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona il tuo livello di attività.");
            return;
        }
        if (datadinascitaPicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci la tua data di nascita.");
            return;
        }
        if (nutrizionistaBox.getValue() == null || nutrizionistaBox.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona un nutrizionista.");
            return;
        }
        if (genereBox.getValue() == null || genereBox.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona il tuo sesso.");
            return;
        }

        // --- Recupero dei valori dai campi di input ---
        double altezza = altezzaSlider.getValue();
        double peso = pesoSlider.getValue();
        LocalDate dataDiNascita = datadinascitaPicker.getValue();
        String livelloAttivita = livelloattivitàBox.getValue();
        String sessoSelezionato = genereBox.getValue().toLowerCase(); // Converte il sesso in minuscolo
        String nutrizionistaSelezionato = nutrizionistaBox.getValue();
        // Ottiene l'ID del nutrizionista dalla mappa usando il nome selezionato
        Integer idNutrizionista = mappaNutrizionisti.get(nutrizionistaSelezionato);

        // Chiama il metodo del modello per registrare i dati del cliente nel database
        boolean successo = datiCliente.registraCliente(altezza, peso, dataDiNascita, livelloAttivita, sessoSelezionato, idNutrizionista, idUtente);

        if (successo) {
            // Se la registrazione dei dati del cliente è avvenuta con successo, carica la pagina di conferma
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ConfermaRegistrazione.fxml"));
                Parent root = fxmlLoader.load(); // Carica la gerarchia dei nodi dell'interfaccia utente
                Stage stage = new Stage(); // Crea una nuova finestra
                stage.setScene(new Scene(root)); // Imposta la scena nella nuova finestra
                stage.setResizable(false);
                stage.setFullScreen(false);
                stage.setTitle("Benvenuto!"); // Imposta un titolo per la nuova finestra
                stage.show(); // Mostra la nuova finestra

                // Chiude la finestra corrente (quella di inserimento dati cliente)
                // Controllo per evitare NullPointerException se il bottone o la scena non sono disponibili
                if (BottoneConferma != null && BottoneConferma.getScene() != null && BottoneConferma.getScene().getWindow() != null) {
                    ((Stage) BottoneConferma.getScene().getWindow()).close();
                } else {
                    System.err.println("Impossibile ottenere la finestra corrente per chiuderla.");
                }

            } catch (IOException e) {
                e.printStackTrace(); // Stampa la traccia dell'errore (utile per il debug)
                // Mostra un errore se il caricamento della nuova pagina fallisce
                showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina successiva dopo la registrazione.");
            }

        } else {
            // Se la registrazione dei dati del cliente è fallita, mostra un messaggio di errore
            showAlert(Alert.AlertType.ERROR, "Errore di registrazione", "Impossibile registrare i dati del cliente. Si è verificato un problema.");
            // In caso di fallimento, l'utente rimane sulla schermata di inserimento dati per riprovare
        }
    }

    /**
     * Metodo alias per 'confermaDati'.
     * Questo metodo è un @FXML per collegare un bottone specifico (es. BottoneConferma)
     * all'azione di conferma.
     * @param event L'evento di azione.
     */
    @FXML
    private void Conferma(ActionEvent event) {
        confermaDati(event); // Chiama il metodo principale di conferma dati
    }

    /**
     * Metodo helper per mostrare messaggi di avviso all'utente.
     * Applica stili CSS personalizzati in base al tipo di avviso.
     * @param alertType Il tipo di avviso (ERROR, INFORMATION, WARNING, CONFIRMATION).
     * @param title Il titolo della finestra di avviso.
     * @param message Il messaggio da visualizzare.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType); // Crea una nuova istanza di Alert con il tipo specificato
        alert.setTitle(title); // Imposta il titolo dell'avviso
        alert.setHeaderText(null); // Non mostra un header text (solo il contenuto)
        alert.setContentText(message); // Imposta il messaggio principale dell'avviso

        // Cerca il file CSS per lo stile personalizzato degli alert
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            // Se il CSS viene trovato, lo aggiunge al DialogPane dell'alert
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            // Applica la classe di stile base "dialog-pane"
            alert.getDialogPane().getStyleClass().add("dialog-pane");
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
            // Se il file CSS non viene trovato, stampa un messaggio di errore nella console
            System.err.println("CSS file not found: Alert-Dialog-Style.css");
        }

        alert.showAndWait(); // Mostra l'avviso e attende che l'utente lo chiuda
    }
}
