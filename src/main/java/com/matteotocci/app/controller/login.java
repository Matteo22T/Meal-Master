package com.matteotocci.app.controller;

import com.matteotocci.app.model.LoginModel; // Modello per la gestione delle operazioni di login e registrazione (interazione con il database)
import com.matteotocci.app.model.Session; // Classe per la gestione della sessione utente (es. ID utente loggato)
import javafx.animation.FadeTransition; // Per creare animazioni di dissolvenza (fade-in/fade-out)
import javafx.event.ActionEvent; // Tipo di evento generato dalle azioni dell'utente (es. click su un bottone)
import javafx.fxml.FXML; // Annotazione per collegare elementi dell'interfaccia utente definiti in FXML al codice Java
import javafx.fxml.FXMLLoader; // Carica file FXML (layout dell'interfaccia utente)
import javafx.fxml.Initializable; // Interfaccia per i controller che devono essere inizializzati dopo il caricamento dell'FXML
import javafx.scene.Parent; // Nodo base per la gerarchia della scena (container di tutti gli elementi UI)
import javafx.scene.Scene; // Contenitore per tutti i contenuti di una scena
import javafx.scene.control.*; // Controlli UI standard di JavaFX (Button, TextField, PasswordField, RadioButton, ToggleGroup, Alert)
import javafx.scene.input.KeyCode; // Codici dei tasti della tastiera
import javafx.scene.input.KeyEvent; // Tipo di evento generato dalla pressione di un tasto
import javafx.scene.layout.VBox; // Layout container che organizza i suoi figli in una singola colonna verticale
import javafx.stage.Stage; // La finestra principale dell'applicazione
import javafx.util.Duration; // Per specificare la durata delle animazioni

import java.io.IOException; // Eccezione per errori di input/output (es. caricamento file FXML)
import java.net.URL; // Classe che rappresenta un Uniform Resource Locator (per trovare risorse come file FXML o CSS)
import java.util.ResourceBundle; // Utilizzato per la localizzazione (non usato esplicitamente qui, ma richiesto da Initializable)

/**
 * Controller per la schermata di login e registrazione.
 * Implementa l'interfaccia Initializable per eseguire operazioni
 * di inizializzazione dopo che l'interfaccia utente è stata caricata.
 */
public class login implements Initializable {

    // --- Elementi dell'interfaccia utente (collegati tramite @FXML) ---

    @FXML private VBox loginBox; // Contenitore VBox per i campi di login
    @FXML private VBox registerBox; // Contenitore VBox per i campi di registrazione

    @FXML private Button btnAccedi; // Bottone "Accedi" (per switchare alla vista login)
    @FXML private Button btnRegistrati; // Bottone "Registrati" (per switchare alla vista registrazione)

    @FXML private Button BottoneAccedi; // Bottone per effettuare il login vero e proprio
    @FXML private Button BottoneRegistrati; // Bottone per effettuare la registrazione vera e propria

    // Campi di testo per la registrazione
    @FXML private TextField nomeField; // Campo per il nome
    @FXML private TextField cognomeField; // Campo per il cognome
    @FXML private TextField emailField; // Campo per l'email di registrazione
    @FXML private PasswordField passwordField; // Campo per la password di registrazione

    // Campi di testo per il login
    @FXML private TextField loginEmailField; // Campo per l'email di login
    @FXML private PasswordField loginPasswordField; // Campo per la password di login

    // RadioButton per la selezione del ruolo (Cliente)
    @FXML private RadioButton Cliente;
    // RadioButton per la selezione del ruolo (Nutrizionista)
    @FXML private RadioButton Nutrizionista;
    // Gruppo di Toggle per i RadioButton, assicura che solo uno possa essere selezionato
    @FXML private ToggleGroup Ruolo;

    // Istanza del modello LoginModel per gestire la logica di business e l'interazione con il database.
    public LoginModel loginModel = new LoginModel();

    // --- Metodi per lo switch tra le schermate di Login e Registrazione ---

    /**
     * Metodo chiamato quando si vuole passare alla schermata di login.
     * Controlla se la loginBox non è già visibile per evitare animazioni superflue.
     * Effettua una dissolvenza della registerBox (nascondendola) e della loginBox (mostrandola).
     * Evidenzia il bottone "Accedi" e disattiva il bottone "Registrati".
     */
    @FXML
    private void switchToLogin() {
        if (!loginBox.isVisible()) {
            fade(registerBox, false); // Nasconde la scatola di registrazione con dissolvenza
            fade(loginBox, true); // Mostra la scatola di login con dissolvenza
            highlightButton(btnAccedi, btnRegistrati); // Evidenzia il bottone 'Accedi'
        }
    }

    /**
     * Metodo chiamato quando si vuole passare alla schermata di registrazione.
     * Controlla se la registerBox non è già visibile per evitare animazioni superflue.
     * Effettua una dissolvenza della loginBox (nascondendola) e della registerBox (mostrandola).
     * Evidenzia il bottone "Registrati" e disattiva il bottone "Accedi".
     */
    @FXML
    private void switchToRegister() {
        if (!registerBox.isVisible()) {
            fade(loginBox, false); // Nasconde la scatola di login con dissolvenza
            fade(registerBox, true); // Mostra la scatola di registrazione con dissolvenza
            highlightButton(btnRegistrati, btnAccedi); // Evidenzia il bottone 'Registrati'
        }
    }

    /**
     * Esegue un'animazione di dissolvenza (fade-in o fade-out) su un elemento VBox.
     * @param box Il VBox su cui applicare l'animazione.
     * @param show Booleano che indica se mostrare (true) o nascondere (false) il box.
     */
    private void fade(VBox box, boolean show) {
        // Crea una transizione di dissolvenza con una durata di 300 millisecondi per il box specificato
        FadeTransition ft = new FadeTransition(Duration.millis(300), box);
        if (show) {
            box.setVisible(true); // Rende il box visibile prima dell'animazione
            box.setOpacity(0); // Imposta l'opacità iniziale a 0 (completamente trasparente)
            ft.setFromValue(0); // Inizia l'animazione da opacità 0
            ft.setToValue(1); // Termina l'animazione a opacità 1 (completamente opaco)
        } else {
            ft.setFromValue(1); // Inizia l'animazione da opacità 1
            ft.setToValue(0); // Termina l'animazione a opacità 0
            // Imposta un'azione da eseguire alla fine dell'animazione: nascondere il box
            ft.setOnFinished(e -> box.setVisible(false));
        }
        ft.play(); // Avvia l'animazione
    }

    /**
     * Applica stili CSS ai bottoni per evidenziare quello attivo e disattivare l'altro.
     * @param active Il bottone da evidenziare.
     * @param inactive Il bottone da disattivare.
     */
    private void highlightButton(Button active, Button inactive) {
        // Rimuove la classe di stile "bottoneSpento" (se presente) dal bottone attivo
        active.getStyleClass().remove("bottoneSpento");
        // Rimuove la classe di stile "bottoneAttivo" (se presente) dal bottone inattivo
        inactive.getStyleClass().remove("bottoneAttivo");

        // Aggiunge la classe di stile "bottoneAttivo" al bottone attivo
        active.getStyleClass().add("bottoneAttivo");
        // Aggiunge la classe di stile "bottoneSpento" al bottone inattivo
        inactive.getStyleClass().add("bottoneSpento");
    }

    // --- Metodi per la gestione del Login ---

    /**
     * Metodo chiamato quando l'utente clicca il bottone "Accedi" (BottoneAccedi).
     * Avvia la procedura di login.
     * @param event L'evento di azione.
     */
    @FXML
    private void AccessoHomePage(ActionEvent event) {
        effettuaLogin(); // Chiama il metodo che contiene la logica di login
    }

    /**
     * Metodo chiamato quando un tasto viene premuto nei campi di login.
     * Se il tasto premuto è INVIO (ENTER), avvia la procedura di login.
     * @param event L'evento di pressione del tasto.
     */
    @FXML
    private void handleLoginEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) { // Controlla se il tasto premuto è INVIO
            effettuaLogin(); // Chiama il metodo che contiene la logica di login
        }
    }

    /**
     * Implementa la logica per l'effettivo processo di login.
     * Recupera email e password dai campi di input, valida i campi,
     * verifica le credenziali con il database e, in caso di successo,
     * carica la Home Page appropriata per il ruolo dell'utente.
     */
    private void effettuaLogin() {
        String email = loginEmailField.getText(); // Recupera l'email dal campo di login
        String password = loginPasswordField.getText(); // Recupera la password dal campo di login

        // Controllo campi vuoti
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci email e password."); // Mostra un avviso di errore
            return; // Termina l'esecuzione del metodo
        }

        // Verifica credenziali nel database utilizzando il modello LoginModel
        boolean loginRiuscito = loginModel.verificaCredenziali(email, password);

        if (loginRiuscito) {
            // Se il login è riuscito, recupera il ruolo dell'utente
            String ruolo = loginModel.getRuoloUtente(email);

            if (ruolo == null) {
                // Se il ruolo non è stato trovato (situazione anomala), mostra un errore
                showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo non trovato per questo utente.");
                return;
            }
            String fxmlPath; // Variabile per memorizzare il percorso del file FXML della Home Page
            // Determina quale Home Page caricare in base al ruolo dell'utente
            if (ruolo.equalsIgnoreCase("nutrizionista")) {
                fxmlPath = "/com/matteotocci/app/HomePageNutrizionista.fxml"; // Home Page per nutrizionisti
            } else if (ruolo.equalsIgnoreCase("cliente")) {
                fxmlPath = "/com/matteotocci/app/HomePage.fxml"; // Home Page per clienti
            } else {
                // Se il ruolo non è riconosciuto, mostra un errore
                showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
                return;
            }

            // Mostra un messaggio di successo all'utente
            showAlert(Alert.AlertType.INFORMATION, "Accesso riuscito", "Benvenuto!");

            try {
                // Recupera l'ID dell'utente loggato e lo imposta nella sessione
                int loggedInUserId = loginModel.getIdUtente(email);
                Session.setUserId(loggedInUserId); // Memorizza l'ID utente per l'uso in altre schermate

                // Carica il file FXML della Home Page
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent homePageRoot = fxmlLoader.load(); // Carica la gerarchia dei nodi dell'interfaccia utente

                // Crea una nuova finestra (Stage) per la Home Page
                Stage homePageStage = new Stage();
                homePageStage.setScene(new Scene(homePageRoot)); // Imposta la scena nella nuova finestra
                homePageStage.show(); // Mostra la nuova finestra

                // Chiude la finestra corrente (quella di login)
                // Questo cast è necessario per ottenere lo Stage dal bottone "Accedi"
                ((Stage) BottoneAccedi.getScene().getWindow()).close();

            } catch (IOException e) {
                e.printStackTrace(); // Stampa la traccia dello stack dell'errore (utile per il debug)
                showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile caricare la Home Page."); // Mostra un avviso di errore
            }
        } else {
            // Se il login è fallito, mostra un messaggio di errore
            showAlert(Alert.AlertType.ERROR, "Accesso fallito", "Email o password errati.");
        }
    }

    // --- Metodi per la gestione della Registrazione ---

    /**
     * Metodo chiamato quando l'utente clicca il bottone "Registrati" (BottoneRegistrati).
     * Avvia la procedura di registrazione.
     * @param event L'evento di azione.
     */
    @FXML
    private void Registrato(ActionEvent event) {
        effettuaRegistrazione(); // Chiama il metodo che contiene la logica di registrazione
    }

    /**
     * Metodo chiamato quando un tasto viene premuto nei campi di registrazione.
     * Se il tasto premuto è INVIO (ENTER), avvia la procedura di registrazione.
     * @param event L'evento di pressione del tasto.
     */
    @FXML
    private void handleRegisterEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) { // Controlla se il tasto premuto è INVIO
            effettuaRegistrazione(); // Chiama il metodo che contiene la logica di registrazione
        }
    }

    /**
     * Implementa la logica per l'effettivo processo di registrazione.
     * Recupera i dati dai campi di input, esegue una validazione rigorosa,
     * registra l'utente nel database e, in caso di successo,
     * carica la pagina successiva appropriata (ConfermaRegistrazione o DatiCliente).
     */
    private void effettuaRegistrazione() {
        // Controllo iniziale per assicurarsi che i campi FXML siano stati correttamente inizializzati.
        // Questo evita NullPointerException se per qualche motivo il caricamento FXML non è completo.
        if (nomeField == null || cognomeField == null || emailField == null || passwordField == null || Ruolo == null) {
            System.err.println("Errore: Campi FXML non inizializzati nel controller!");
            showAlert(Alert.AlertType.ERROR, "Errore Interno", "Errore nell'interfaccia utente.");
            return;
        }

        // Recupera i valori dai campi di input della registrazione
        String nome = nomeField.getText();
        String cognome = cognomeField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        boolean campiValidi = true; // Flag per tenere traccia della validità dei campi
        String messaggioErrore = ""; // Stringa per accumulare i messaggi di errore

        // Validazione campi vuoti e formato
        if (nome.isEmpty()) {
            messaggioErrore += "Il campo Nome è obbligatorio.\n";
            campiValidi = false;
        }
        if (cognome.isEmpty()) {
            messaggioErrore += "Il campo Cognome è obbligatorio.\n";
            campiValidi = false;
        }
        if (email.isEmpty()) {
            messaggioErrore += "Il campo Email è obbligatorio.\n";
            campiValidi = false;
        } else if (!email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            // Validazione del formato email: deve essere un indirizzo @gmail.com
            messaggioErrore += "L'email deve essere un indirizzo @gmail.com valido.\n";
            campiValidi = false;
        }
        if (password.isEmpty()) {
            messaggioErrore += "Il campo Password è obbligatorio.\n";
            campiValidi = false;
        } else if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            // Validazione della password: almeno 8 caratteri, una maiuscola e un numero (regex)
            messaggioErrore += "La password deve contenere almeno 8 caratteri, una lettera maiuscola e un numero.\n";
            campiValidi = false;
        }

        // Recupera il RadioButton selezionato dal ToggleGroup
        Toggle selectedToggle = Ruolo.getSelectedToggle();
        if (selectedToggle == null) {
            // Se nessun RadioButton è selezionato, aggiunge un errore
            messaggioErrore += "Seleziona un ruolo (Cliente o Nutrizionista).\n";
            campiValidi = false;
        }

        // Se ci sono campi non validi, mostra l'avviso con tutti gli errori raccolti
        if (!campiValidi) {
            showAlert(Alert.AlertType.ERROR, "Registrazione Incompleta", messaggioErrore);
            return; // Termina l'esecuzione del metodo
        }

        // Ottiene il testo del RadioButton selezionato e lo converte in minuscolo per la registrazione
        RadioButton selectedRadioButton = (RadioButton) selectedToggle;
        String ruolo = selectedRadioButton.getText().toLowerCase();

        // Tenta di registrare l'utente nel database tramite il modello LoginModel
        boolean successo = loginModel.registraUtente(nome, cognome, email, password, ruolo);

        if (successo) {
            // Se la registrazione è avvenuta con successo, recupera l'ID dell'utente appena registrato
            int idUtente = loginModel.getIdUtente(email);
            if (idUtente == -1) {
                // Se l'ID non può essere recuperato (situazione anomala), mostra un errore
                showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile recuperare l'ID dell'utente.");
                return;
            }

            try {
                FXMLLoader fxmlLoader;
                Parent root;
                // Carica la schermata successiva in base al ruolo dell'utente
                if (ruolo.equals("nutrizionista")) {
                    // Per i nutrizionisti, carica la pagina di conferma registrazione
                    fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ConfermaRegistrazione.fxml"));
                    root = fxmlLoader.load();
                } else {
                    // Per i clienti, carica la pagina per inserire i dati aggiuntivi del cliente
                    fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DatiCliente.fxml"));
                    root = fxmlLoader.load();
                    // Ottiene il controller della pagina DatiCliente e passa l'ID dell'utente
                    DatiCliente datiClienteController = fxmlLoader.getController();
                    if (datiClienteController != null) {
                        datiClienteController.setIdUtente(idUtente); // Imposta l'ID utente nel controller DatiCliente
                    } else {
                        // Se il controller non viene caricato, stampa un errore e mostra un avviso
                        System.err.println("Errore: Controller non caricato correttamente.");
                        showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile caricare il controller.");
                        return;
                    }
                }
                // Crea e mostra la nuova finestra
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Benvenuto!");
                stage.show();

                // Chiude la finestra corrente (quella di registrazione)
                // Controllo per evitare NullPointerException se il bottone o la scena non sono disponibili
                if (BottoneRegistrati != null && BottoneRegistrati.getScene() != null && BottoneRegistrati.getScene().getWindow() != null) {
                    ((Stage) BottoneRegistrati.getScene().getWindow()).close();
                } else {
                    System.err.println("Impossibile ottenere la finestra corrente per chiuderla.");
                }

            } catch (IOException e) {
                e.printStackTrace(); // Stampa la traccia dell'errore
                showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina successiva dopo la registrazione.");
            }

        } else {
            // Se la registrazione è fallita, mostra un messaggio di errore generico
            showAlert(Alert.AlertType.ERROR, "Errore di registrazione", "Impossibile registrare l'utente. L'email potrebbe essere già in uso o si è verificato un problema.");
        }
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

    /**
     * Metodo di inizializzazione chiamato automaticamente da FXMLLoader
     * dopo che tutti gli elementi FXML sono stati caricati e iniettati.
     * Qui vengono configurati i listener per la navigazione da tastiera.
     * @param url La posizione del file FXML che ha caricato questo controller.
     * @param resourceBundle Le risorse utilizzate per la localizzazione (non usate direttamente qui).
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // --- Gestione della navigazione con frecce direzionali nei campi di Login ---

        // Listener per il campo email di login: quando si preme FRECCIA GIÙ, sposta il focus al campo password
        loginEmailField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DOWN) {
                loginPasswordField.requestFocus(); // Sposta il focus al campo password
                event.consume(); // Consuma l'evento per evitare che si propaghi ulteriormente
            }
        });

        // Listener per il campo password di login: quando si preme FRECCIA SU, sposta il focus al campo email
        loginPasswordField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.UP) {
                loginEmailField.requestFocus(); // Sposta il focus al campo email
                event.consume(); // Consuma l'evento
            }
        });

        // Associa il metodo handleLoginEnter alla pressione di un tasto nei campi di login,
        // in modo che premendo INVIO venga tentato il login.
        loginPasswordField.setOnKeyPressed(this::handleLoginEnter);
        loginEmailField.setOnKeyPressed(this::handleLoginEnter);


        // --- Gestione della navigazione con frecce direzionali nei campi di Registrazione ---

        // Navigazione tra i RadioButton Cliente e Nutrizionista
        // Listener per il RadioButton Cliente:
        // - FRECCIA DESTRA: sposta il focus a Nutrizionista
        // - FRECCIA GIÙ: sposta il focus al campo Nome
        Cliente.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.RIGHT) {
                Nutrizionista.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                nomeField.requestFocus();
                event.consume();
            }
        });

        // Listener per il RadioButton Nutrizionista:
        // - FRECCIA SINISTRA: sposta il focus a Cliente
        // - FRECCIA GIÙ: sposta il focus al campo Nome
        Nutrizionista.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.LEFT) {
                Cliente.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                nomeField.requestFocus();
                event.consume();
            }
        });

        // Navigazione tra Nome, Cognome, Email, Password di registrazione
        // Listener per il campo Nome:
        // - FRECCIA GIÙ: sposta il focus a Email (saltando Cognome per un percorso più diretto)
        // - FRECCIA DESTRA: sposta il focus a Cognome
        // - FRECCIA SU: torna ai RadioButton (Cliente o Nutrizionista, a seconda dell'ultima selezione o focus)
        nomeField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DOWN) {
                emailField.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                cognomeField.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                Cliente.requestFocus(); // Si assume di tornare al primo RadioButton del gruppo
                event.consume();
            }
        });

        // Listener per il campo Cognome:
        // - FRECCIA GIÙ: sposta il focus a Email
        // - FRECCIA SINISTRA: sposta il focus a Nome
        // - FRECCIA SU: torna ai RadioButton
        cognomeField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DOWN) {
                emailField.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                nomeField.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                Nutrizionista.requestFocus(); // Si assume di tornare al secondo RadioButton del gruppo
                event.consume();
            }
        });

        // Listener per il campo Email:
        // - FRECCIA GIÙ: sposta il focus a Password
        // - FRECCIA SU: sposta il focus a Nome
        emailField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DOWN) {
                passwordField.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                nomeField.requestFocus();
                event.consume();
            }
        });

        // Listener per il campo Password:
        // - FRECCIA SU: sposta il focus a Email
        passwordField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.UP) {
                emailField.requestFocus();
                event.consume();
            }
        });

        // Associa il metodo handleRegisterEnter alla pressione di un tasto nei campi di registrazione,
        // in modo che premendo INVIO venga tentata la registrazione.
        nomeField.setOnKeyPressed(this::handleRegisterEnter);
        cognomeField.setOnKeyPressed(this::handleRegisterEnter);
        emailField.setOnKeyPressed(this::handleRegisterEnter);
        passwordField.setOnKeyPressed(this::handleRegisterEnter);
    }
}