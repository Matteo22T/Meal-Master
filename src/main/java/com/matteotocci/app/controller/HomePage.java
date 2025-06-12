package com.matteotocci.app.controller;

// Importa le classi necessarie per l'applicazione JavaFX e le operazioni del database
import com.matteotocci.app.model.Dieta; // Modello per l'oggetto Dieta
import com.matteotocci.app.model.GiornoDieta; // Modello per l'oggetto GiornoDieta
import com.matteotocci.app.model.Session; // Classe per la gestione della sessione utente (es. ID utente loggato)
import com.matteotocci.app.model.SessionGiornoDieta; // Classe per la gestione della sessione del giorno dieta selezionato
import com.matteotocci.app.model.SQLiteConnessione; // Classe per gestire la connessione al database SQLite
import javafx.animation.KeyFrame; // Per definire un fotogramma chiave in un'animazione
import javafx.animation.KeyValue; // Per definire il valore di una proprietà in un KeyFrame
import javafx.animation.Timeline; // Per creare animazioni basate su una linea temporale
import javafx.application.Platform; // Per eseguire codice sul thread di UI di JavaFX
import javafx.collections.FXCollections; // Utility per creare collezioni osservabili
import javafx.collections.ObservableList; // Lista che notifica i "listener" quando avvengono dei cambiamenti
import javafx.event.ActionEvent; // Tipo di evento generato dalle azioni dell'utente (es. click su un bottone)
import javafx.fxml.FXML; // Annotazione per collegare elementi dell'interfaccia utente definiti in FXML al codice Java
import javafx.fxml.FXMLLoader; // Carica file FXML (layout dell'interfaccia utente)
import javafx.fxml.Initializable; // AGGIUNTA: Interfaccia per i controller che devono essere inizializzati dopo il caricamento dell'FXML
import javafx.scene.Node; // Classe base per tutti i nodi nel grafo della scena (elementi UI)
import javafx.scene.Parent; // Nodo base per la gerarchia della scena (container di tutti gli elementi UI)
import javafx.scene.Scene; // Contenitore per tutti i contenuti di una scena
import javafx.scene.control.*; // Controlli UI standard di JavaFX (Button, Label, ComboBox, Alert)
import javafx.scene.input.MouseEvent; // Tipo di evento generato da interazioni del mouse
import javafx.scene.shape.Circle; // Elemento grafico circolare (usato per i cerchi di progresso)
import javafx.scene.text.Font; // Per la gestione dei font
import javafx.stage.Modality; // Per definire la modalità di una finestra (es. modale)
import javafx.stage.Stage; // La finestra principale dell'applicazione
import javafx.util.Duration; // Per specificare la durata delle animazioni

import java.io.IOException; // Eccezione per errori di input/output (es. caricamento file FXML)
import java.sql.Connection; // Interfaccia per la connessione al database
import java.sql.DriverManager; // Per ottenere una connessione al database
import java.sql.PreparedStatement; // Per eseguire query SQL precompilate
import java.sql.ResultSet; // Per leggere i risultati delle query SQL
import java.sql.SQLException; // Eccezione per errori di database
import java.time.LocalDate; // Per gestire le date
import java.util.Optional; // Contenitore che può o meno contenere un valore non nullo
import java.net.URL; // Necessario per Initializable
import java.util.ResourceBundle; // Necessario per Initializable

/**
 * Controller per la schermata principale (Home Page) dell'applicazione per i clienti.
 * Gestisce la visualizzazione dei dati della dieta assegnata, l'andamento dei macronutrienti
 * e la navigazione ad altre sezioni dell'app.
 */
public class HomePage implements Initializable { // MODIFICA: Ora implementa Initializable

    // --- Elementi dell'interfaccia utente (collegati tramite @FXML) ---
    @FXML private Button BottoneAlimenti; // Bottone per accedere alla sezione Alimenti
    @FXML private Button BottoneRicette; // Bottone per accedere alla sezione Ricette
    @FXML private Button BottonePiano; // Bottone per accedere alla sezione Piano Alimentare
    @FXML private Label nomeUtenteLabelHomePage; // Etichetta per mostrare il nome dell'utente loggato
    @FXML private ComboBox<GiornoDieta> comboGiorniDieta; // ComboBox per selezionare il giorno della dieta

    // Etichette per i valori target (obiettivi) di macronutrienti e calorie del giorno selezionato
    @FXML private Label labelKcal;
    @FXML private Label labelProteine;
    @FXML private Label labelCarboidrati;
    @FXML private Label labelGrassi;

    // Etichette per i valori correnti (consumati) di macronutrienti e calorie del giorno selezionato
    @FXML private Label labelKcalCorrenti;
    @FXML private Label labelProteineCorrenti;
    @FXML private Label labelCarboidratiCorrenti;
    @FXML private Label labelGrassiCorrenti;

    // Etichette per le calorie per ogni pasto (colazione, spuntino, ecc.)
    @FXML private Label colazioneKcalLabel;
    @FXML private Label spuntinoKcalLabel;
    @FXML private Label pranzoKcalLabel;
    @FXML private Label merendaKcalLabel;
    @FXML private Label cenaKcalLabel;

    // Cerchi di progresso per visualizzare l'andamento dei macronutrienti e delle calorie
    @FXML private Circle proteineProgressCircle;
    @FXML private Circle carboidratiProgressCircle;
    @FXML private Circle grassiProgressCircle;
    @FXML private Circle kcalProgressCircle;

    // --- Variabili di stato interne ---
    private String loggedInUserId; // Variabile per memorizzare l'ID dell'utente loggato (potrebbe essere redondante con Session.getUserId())
    private Dieta dietaAssegnata; // Oggetto Dieta che rappresenta la dieta assegnata al cliente
    private GiornoDieta previousDaySelection; // Mantiene traccia del giorno selezionato in precedenza nella ComboBox
    private int targetProteine = 0; // Obiettivo di proteine in grammi per il giorno selezionato
    private int targetCarboidrati = 0; // Obiettivo di carboidrati in grammi per il giorno selezionato
    private int targetGrassi = 0; // Obiettivo di grassi in grammi per il giorno selezionato
    private int targetKcal = 0; // Obiettivo di calorie in kcal per il giorno selezionato
    private boolean isProgrammaticChange = false; // Flag per distinguere i cambiamenti programmatici della ComboBox da quelli dell'utente

    /**
     * Metodo di inizializzazione chiamato automaticamente da FXMLLoader dopo che
     * tutti gli elementi FXML sono stati caricati e iniettati nel controller.
     * Questo è il punto in cui viene eseguita la logica di setup iniziale della pagina.
     *
     * @param location L'URL del documento FXML che ha dato origine a questo controller.
     * @param resources Le risorse utilizzate per localizzare gli oggetti radice, o null se la radice non è stata localizzata.
     */
    @Override // ANNOTAZIONE AGGIUNTA: Indica che questo metodo fa l'override di un metodo dell'interfaccia Initializable
    public void initialize(URL location, ResourceBundle resources) { // MODIFICA: La firma del metodo ora corrisponde a quella di Initializable
        // Recupera l'ID dell'utente loggato dalla classe Session
        Integer userIdFromSession = Session.getUserId();

        // Verifica se l'ID utente è valido (cioè, se un utente è effettivamente loggato)
        if (userIdFromSession != null) {
            System.out.println("[DEBUG - HomePage] ID utente da Sessione: " + userIdFromSession);
            setNomeUtenteLabel(); // Imposta il nome dell'utente nella label in alto

            try {
                // Recupera la dieta assegnata al cliente dal database
                this.dietaAssegnata = recuperaDietaAssegnataACliente(userIdFromSession.intValue());

                // Se una dieta è stata trovata
                if (this.dietaAssegnata != null) {
                    // Popola la ComboBox dei giorni dieta con i giorni della dieta recuperata
                    popolaComboGiorniDieta(this.dietaAssegnata.getId());
                    System.out.println("DEBUG (HomePage): ComboBox giorni dieta popolata per utente ID: " + userIdFromSession);

                    // Prepara la selezione iniziale del giorno:
                    // 1. Pulisce la selezione attuale e resetta la selezione precedente
                    comboGiorniDieta.getSelectionModel().clearSelection();
                    previousDaySelection = null;
                    // 2. Tenta di selezionare il giorno corrente se esiste nel piano dieta
                    // 3. Se il giorno corrente non è presente, prova a ripristinare la selezione dalla sessione (se presente)
                    if (!selectCurrentDayIfExists()) { // Restituisce true se ha selezionato qualcosa
                        restoreDaySelectionFromSession();
                    }

                    // Esegue il codice sul thread di JavaFX UI dopo che l'interfaccia è pronta
                    Platform.runLater(() -> {
                        GiornoDieta giornoSelezionato = comboGiorniDieta.getSelectionModel().getSelectedItem();
                        if (giornoSelezionato != null) {
                            // Imposta il giorno dieta selezionato nella SessionGiornoDieta
                            SessionGiornoDieta.setGiornoDietaSelezionato(giornoSelezionato);
                            // Imposta i valori target (obiettivi) in base al giorno selezionato
                            targetProteine = (int) Math.round(giornoSelezionato.getProteine());
                            targetGrassi = (int) Math.round(giornoSelezionato.getGrassi());
                            targetCarboidrati = (int) Math.round(giornoSelezionato.getCarboidrati());
                            targetKcal = (int) Math.round(giornoSelezionato.getKcal());

                            // Aggiorna le etichette con i valori target
                            String kcalText= targetKcal+" kcal";
                            labelKcal.setText(kcalText);
                            AggiustaFontSize(labelKcal, kcalText); // Adatta la dimensione del font
                            labelProteine.setText(targetProteine + " g");
                            labelCarboidrati.setText(targetCarboidrati + " g");
                            labelGrassi.setText(targetGrassi + " g");

                            // Aggiorna le etichette delle calorie per ogni pasto e i totali correnti
                            aggiornaLabelKcalPerPasto();

                            // Memorizza il giorno selezionato come precedente per futuri controlli
                            previousDaySelection = giornoSelezionato;
                        } else {
                            // Se nessun giorno è selezionato, resetta tutte le etichette e i cerchi di progresso
                            resetAllLabelsAndProgress();
                        }
                    });
                } else {
                    // Se nessuna dieta è stata trovata per l'utente
                    System.out.println("DEBUG (HomePage): Nessuna dieta trovata per l'utente ID: " + userIdFromSession + ". ComboBox non popolata.");
                    if (comboGiorniDieta != null) {
                        comboGiorniDieta.getItems().clear(); // Pulisce la ComboBox
                    }
                    // Resetta le etichette dei target a 0
                    String kcalText = "0 kcal";
                    labelKcal.setText(kcalText);
                    AggiustaFontSize(labelKcal, kcalText);
                    labelProteine.setText("0 g");
                    labelCarboidrati.setText("0 g");
                    labelGrassi.setText("0 g");
                    resetAllLabelsAndProgress(); // Resetta anche i totali correnti e il progresso
                }
            } catch (Exception e) {
                // Gestione degli errori durante il recupero della dieta o il popolamento della ComboBox
                System.err.println("ERRORE: Errore durante il recupero dieta o popolamento ComboBox: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore Caricamento Dati", "Impossibile caricare i dati della dieta.");
            }
        } else {
            // Se l'ID utente dalla sessione è null (utente non loggato)
            System.out.println("[DEBUG - HomePage] Session.getUserId() è null. Utente non loggato o sessione non impostata.");
            showAlert(Alert.AlertType.WARNING, "Accesso Negato", "Utente non loggato");
            // Resetta le etichette dei target a 0
            String kcalText = "0 kcal";
            labelKcal.setText(kcalText);
            AggiustaFontSize(labelKcal, kcalText);
            labelProteine.setText("0 g");
            labelCarboidrati.setText("0 g");
            labelGrassi.setText("0 g");
            resetAllLabelsAndProgress(); // Resetta anche i totali correnti e il progresso
        }

        // --- Listener per la ComboBox dei giorni dieta ---
        // Questo listener viene attivato ogni volta che la selezione nella ComboBox cambia.
        if (comboGiorniDieta != null) {
            comboGiorniDieta.valueProperty().addListener((obs, oldVal, newVal) -> {
                System.out.println(isProgrammaticChange); // Stampa lo stato del flag programmatico
                if (isProgrammaticChange) {
                    isProgrammaticChange = false; // Resetta il flag se il cambiamento è programmatico
                    return; // Esce dal listener per evitare l'alert di conferma
                }
                // Evita di mostrare l'alert all'inizializzazione o se la selezione non è effettivamente cambiata
                if (oldVal == null || oldVal.equals(newVal)) {
                    previousDaySelection = newVal; // Aggiorna la selezione precedente
                    if (newVal != null) {
                        // Se è una selezione iniziale o la stessa selezione, aggiorna i target
                        targetProteine = (int) Math.round(newVal.getProteine());
                        targetGrassi = (int) Math.round(newVal.getGrassi());
                        targetCarboidrati = (int) Math.round(newVal.getCarboidrati());
                        targetKcal = (int) Math.round(newVal.getKcal()); // CORREZIONE: Math.round()

                        String kcalText = targetKcal + " kcal";
                        labelKcal.setText(kcalText);
                        AggiustaFontSize(labelKcal, kcalText);
                        labelProteine.setText(targetProteine + " g");
                        labelCarboidrati.setText(targetCarboidrati + " g");
                        labelGrassi.setText(targetGrassi + " g");

                        // CORREZIONE: Aggiorna i dati quando cambia la selezione
                        aggiornaLabelKcalPerPasto();
                    } else {
                        // Se la nuova selezione è null, resetta tutto
                        String kcalText = "0 kcal";
                        labelKcal.setText(kcalText);
                        AggiustaFontSize(labelKcal, kcalText);
                        labelProteine.setText("0 g");
                        labelCarboidrati.setText("0 g");
                        labelGrassi.setText("0 g");
                        resetAllLabelsAndProgress();
                    }
                    return; // Esce
                }

                // Se la selezione è cambiata da parte dell'utente, mostra un alert di conferma
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Conferma Cambio Giorno");
                alert.setHeaderText("Sei sicuro di voler cambiare giorno?");
                alert.setContentText("Perderai i pasti mangiati finora per il giorno corrente.");
                alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm());
                alert.getDialogPane().getStyleClass().add("dialog-pane");
                alert.getDialogPane().getStyleClass().add("alert-confirmation");


                Optional<ButtonType> result = alert.showAndWait(); // Mostra l'alert e attende la risposta dell'utente

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Se l'utente conferma il cambio di giorno:
                    // Prima elimina i pasti registrati per il giorno precedentemente selezionato
                    if (oldVal != null) {
                        eliminaPastiSpecificiGiorno(oldVal.getIdGiornoDieta());
                    }

                    // Aggiorna i target di macronutrienti e calorie per il nuovo giorno selezionato
                    if (newVal != null) {
                        SessionGiornoDieta.setGiornoDietaSelezionato(newVal); // Imposta il nuovo giorno nella sessione
                        targetProteine = (int) Math.round(newVal.getProteine());
                        targetGrassi = (int) Math.round(newVal.getGrassi());
                        targetCarboidrati = (int) Math.round(newVal.getCarboidrati());
                        targetKcal = (int) Math.round(newVal.getKcal()); // CORREZIONE: Math.round()

                        String kcalText = targetKcal + " kcal";
                        labelKcal.setText(kcalText);
                        AggiustaFontSize(labelKcal, kcalText);
                        labelProteine.setText(targetProteine + " g");
                        labelCarboidrati.setText(targetCarboidrati + " g");
                        labelGrassi.setText(targetGrassi + " g");

                        // Aggiorna le etichette dei pasti e i totali correnti per il nuovo giorno
                        aggiornaLabelKcalPerPasto();
                    } else {
                        // Se il nuovo valore è null, resetta tutto
                        String kcalText = "0 kcal";
                        labelKcal.setText(kcalText);
                        AggiustaFontSize(labelKcal, kcalText);
                        labelProteine.setText("0 g");
                        labelCarboidrati.setText("0 g");
                        labelGrassi.setText("0 g");
                        resetAllLabelsAndProgress();
                    }
                    previousDaySelection = newVal; // Aggiorna la selezione precedente con il nuovo giorno
                } else {
                    // Se l'utente ha annullato il cambio di giorno, ripristina la selezione precedente
                    isProgrammaticChange = true; // Imposta il flag per evitare che il listener si attivi di nuovo
                    Platform.runLater(() -> {
                        comboGiorniDieta.getSelectionModel().select(oldVal); // Seleziona nuovamente il vecchio valore
                    });
                }
            });
        }
    }

    /**
     * Imposta la label del nome utente nella Home Page.
     * Recupera il nome e cognome dell'utente dal database usando l'ID dalla sessione.
     */
    private void setNomeUtenteLabel() {
        String nomeUtente = getNomeUtenteDalDatabase(Session.getUserId().toString());
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            nomeUtenteLabelHomePage.setText(nomeUtente);
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Testo di fallback se il nome non è disponibile
        }
    }

    /**
     * Recupera il nome e cognome di un utente dal database dato il suo ID.
     * @param userId L'ID dell'utente.
     * @return Il nome completo dell'utente (Nome Cognome) o null se non trovato.
     */
    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String url = "jdbc:sqlite:database.db"; // Percorso del database SQLite
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?"; // Query SQL per selezionare nome e cognome

        // Utilizza un blocco try-with-resources per gestire la connessione e lo statement
        try (Connection conn = DriverManager.getConnection(url); // Ottiene una connessione al database
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement
            pstmt.setString(1, userId); // Imposta l'ID utente come parametro della query
            ResultSet rs = pstmt.executeQuery(); // Esegue la query

            if (rs.next()) { // Se c'è un risultato
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena nome e cognome
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
            e.printStackTrace(); // Stampa la traccia dell'errore
        }
        return nomeUtente;
    }

    /**
     * Metodo per la navigazione alla schermata "Alimenti".
     * @param event L'evento di azione (click sul bottone).
     */
    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            // Carica il file FXML della schermata Alimenti
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent root = fxmlLoader.load();

            // Ottiene lo Stage corrente (la finestra attuale) e imposta la nuova scena
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show(); // Mostra la nuova finestra
        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore se il caricamento fallisce
        }
    }

    /**
     * Metodo per la navigazione alla schermata "Ricette".
     * @param event L'evento di azione (click sul bottone).
     */
    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            // Carica il file FXML della schermata Ricette
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Ricette.fxml"));
            Parent root = fxmlLoader.load();

            // Ottiene lo Stage corrente e imposta la nuova scena
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show(); // Mostra la nuova finestra
        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore se il caricamento fallisce
        }
    }

    /**
     * Metodo per la navigazione alla schermata "PaginaProfilo".
     * @param event L'evento del mouse (click).
     */
    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            // Carica il file FXML della schermata PaginaProfilo
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();

            // Ottiene lo Stage corrente e imposta la nuova scena
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show(); // Mostra la nuova finestra
        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore se il caricamento fallisce
        }
    }

    /**
     * Metodo per l'accesso alla schermata "Pasti Giornalieri".
     * Questa schermata viene aperta come finestra modale, bloccando l'interazione
     * con la Home Page sottostante finché non viene chiusa.
     * @param event L'evento del mouse (click).
     */
    @FXML
    private void AccessoPastiGiornalieri(MouseEvent event) {
        try {
            // Carica il file FXML per PastiGiornalieri
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PastiGiornalieri.fxml"));
            Parent pastiGiornalieriRoot = fxmlLoader.load();
            // Ottiene il controller della finestra PastiGiornalieri
            PastiGiornalieri pastiGiornalieriController = fxmlLoader.getController();
            // Passa un riferimento a questo controller (HomePage) al controller PastiGiornalieri.
            // Questo permette a PastiGiornalieri di chiamare metodi di HomePage (es. per aggiornare i dati dopo l'aggiunta di un pasto).
            pastiGiornalieriController.setHomePageController(this);

            // Crea un NUOVO Stage (finestra) per PastiGiornalieri
            Stage pastiGiornalieriStage = new Stage();

            // Ottieni lo Stage corrente (la tua HomePage)
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Imposta il proprietario del nuovo Stage. Questo lo rende "figlio" della HomePage.
            pastiGiornalieriStage.initOwner(currentStage);

            // Imposta la modalità modale.
            // APPLICATION_MODAL blocca l'interazione con TUTTE le altre finestre dell'applicazione.
            // WINDOW_MODAL blocca solo l'interazione con la finestra proprietaria (HomePage).
            // Per l'esigenza "senza poter interagire con quella sotto", APPLICATION_MODAL è più robusto.
            pastiGiornalieriStage.initModality(Modality.APPLICATION_MODAL);

            pastiGiornalieriStage.setTitle("Pasti Giornalieri"); // Imposta un titolo per la nuova finestra
            pastiGiornalieriStage.setScene(new Scene(pastiGiornalieriRoot)); // Imposta la scena

            // Mostra il nuovo Stage e attendi che venga chiuso.
            // Questo blocca l'interazione con la finestra proprietaria (HomePage)
            // finché PastiGiornalieriStage non viene chiuso.
            pastiGiornalieriStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore
            // Mostra un messaggio di errore se il caricamento fallisce
            showAlert(Alert.AlertType.ERROR,"Errore di caricamento","Impossibile caricare la pagina Pasti Giornalieri");
        }
    }

    private Stage dietaStage; // Variabile per mantenere il riferimento allo Stage della schermata Dieta

    /**
     * Metodo per l'accesso alla schermata "Piano Alimentare".
     * Apre la schermata `VisualizzaDieta.fxml` e le passa l'oggetto `Dieta` assegnato al cliente.
     * @param event L'evento di azione (click sul bottone).
     */
    @FXML
    private void AccessoPianoAlimentare(ActionEvent event) {
        // Verifica se una dieta è stata assegnata al cliente
        if (dietaAssegnata != null) {
            try {
                // Chiudi la finestra precedente della dieta se è già aperta
                if (dietaStage != null && dietaStage.isShowing()) {
                    dietaStage.close();
                }

                // Carica il file FXML per la visualizzazione della dieta
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                Parent visualizzaDietaRoot = fxmlLoader.load();

                // PASSO 2: Ottieni il controller della nuova finestra
                VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();

                // PASSO 3: Passa l'oggetto Dieta al controller della nuova finestra
                visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                System.out.println("DEBUG (HomePage): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                // Crea e mostra il nuovo Stage per la visualizzazione della dieta
                dietaStage = new Stage();
                dietaStage.setScene(new Scene(visualizzaDietaRoot));
                dietaStage.show();

            } catch (IOException e) {
                System.err.println("ERRORE (HomePage): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.");
            } catch (Exception e) {
                System.err.println("ERRORE (HomePage): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.");
            }
        } else {
            // Se nessuna dieta è stata trovata
            System.out.println("DEBUG (HomePage): Nessuna dieta trovata per il cliente  (ID: " + Session.getUserId() + ").");
            showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta",
                    "Il cliente non ha diete assegnate o non è stato possibile recuperarle.");
        }
    }

    /**
     * Popola la ComboBox dei giorni dieta con i giorni associati a una specifica dieta.
     * @param idDieta L'ID della dieta da cui recuperare i giorni.
     */
    private void popolaComboGiorniDieta(int idDieta) {
        ObservableList<GiornoDieta> giorniList = FXCollections.observableArrayList(); // Lista per i giorni della dieta
        // Query SQL per selezionare i dati dei giorni dieta per una specifica dieta
        String query = "SELECT id_giorno_dieta, nome_giorno, calorie_giorno, proteine_giorno, carboidrati_giorno, grassi_giorno " +
                "FROM Giorno_dieta WHERE id_dieta = ? ORDER BY id_giorno_dieta ASC";

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione al database
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement
            pstmt.setInt(1, idDieta); // Imposta l'ID della dieta come parametro
            ResultSet rs = pstmt.executeQuery(); // Esegue la query

            while (rs.next()) { // Itera su ogni riga del ResultSet
                int idGiorno = rs.getInt("id_giorno_dieta");
                String nome = rs.getString("nome_giorno");
                double kcal = rs.getDouble("calorie_giorno");
                double proteine = rs.getDouble("proteine_giorno");
                double carboidrati = rs.getDouble("carboidrati_giorno");
                double grassi = rs.getDouble("grassi_giorno"); // CORREZIONE: C'era un typo "grasi_giorno" qui, corretto in "grassi_giorno"

                // Aggiunge un nuovo oggetto GiornoDieta alla lista
                giorniList.add(new GiornoDieta(idGiorno, nome, kcal, proteine, carboidrati, grassi));
            }
            comboGiorniDieta.setItems(giorniList); // Imposta gli elementi della ComboBox
            if (!giorniList.isEmpty()) {
                comboGiorniDieta.getSelectionModel().selectFirst(); // Seleziona il primo elemento di default
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL: Errore durante il popolamento della ComboBox dei giorni dieta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recupera la dieta assegnata a un cliente specifico dal database.
     * @param idCliente L'ID del cliente.
     * @return L'oggetto Dieta assegnata al cliente o null se non trovata.
     */
    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String url = "jdbc:sqlite:database.db"; // Percorso del database
        // Query per recuperare la dieta con l'ID del cliente
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        try (Connection conn = SQLiteConnessione.connector(); // Usa la connessione centralizzata
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente); // Imposta l'ID del cliente come parametro
            ResultSet rs = pstmt.executeQuery(); // Esegue la query

            if (rs.next()) { // Se c'è un risultato
                // Crea un nuovo oggetto Dieta con i dati recuperati
                dieta = new Dieta(
                        rs.getInt("id"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine"),
                        rs.getInt("id_nutrizionista"),
                        rs.getInt("id_cliente")
                );
                System.out.println("DEBUG (HomePageNutrizionista): Recuperata dieta '" + dieta.getNome() + "' (ID: " + dieta.getId() + ") per cliente ID: " + idCliente);
            } else {
                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (HomePageNutrizionista): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta;
    }

    /**
     * Metodo helper per mostrare messaggi di avviso all'utente.
     */
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


    // --- Metodi per l'aggiunta di pasti ---

    @FXML
    private void handleColazioneAdd(ActionEvent event) {
        aggiungiPasto("Colazione", colazioneKcalLabel);
    }

    @FXML
    private void handleSpuntinoAdd(ActionEvent event) {
        aggiungiPasto("Spuntino", spuntinoKcalLabel);
    }

    @FXML
    private void handlePranzoAdd(ActionEvent event) {
        aggiungiPasto("Pranzo", pranzoKcalLabel);
    }

    @FXML
    private void handleMerendaAdd(ActionEvent event) {
        aggiungiPasto("Merenda", merendaKcalLabel);
    }

    @FXML
    private void handleCenaAdd(ActionEvent event) {
        aggiungiPasto("Cena", cenaKcalLabel);
    }

    /**
     * Apre la finestra per aggiungere un pasto.
     * Passa l'ID del giorno dieta selezionato, il tipo di pasto e l'ID dell'utente al controller AggiungiPasto.
     * @param tipoPasto Il tipo di pasto da aggiungere (es. "Colazione", "Pranzo").
     * @param mealCaloriesLabel La label delle calorie del pasto specifico (per riferimento all'owner della finestra).
     */
    private void aggiungiPasto(String tipoPasto, Label mealCaloriesLabel) {
        GiornoDieta giornoSelezionato = comboGiorniDieta.getSelectionModel().getSelectedItem();
        if (giornoSelezionato == null) {
            // Se nessun giorno è selezionato, mostra un avviso
            showAlert(Alert.AlertType.WARNING, "Selezione Giorno", "Nessun giorno selezionato, seleziona un giorno della dieta prima di aggiungere un pasto.");
            return;
        }

        try {
            // Carica il file FXML per la schermata AggiungiPasto
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiPasto.fxml"));
            Parent root = loader.load();

            // Ottiene il controller della schermata AggiungiPasto
            AggiungiPasto aggiungiPastoController = loader.getController();
            // Imposta i dati necessari nel controller AggiungiPasto
            aggiungiPastoController.setPastoData(giornoSelezionato.getIdGiornoDieta(), tipoPasto, Session.getUserId());
            // Passa un riferimento a questo controller (HomePage) al controller AggiungiPasto
            aggiungiPastoController.setHomePageController(this);

            Stage stage = new Stage(); // Crea un nuovo Stage per AggiungiPasto
            stage.setTitle("Aggiungi " + tipoPasto); // Imposta il titolo
            stage.setScene(new Scene(root)); // Imposta la scena
            stage.initModality(Modality.APPLICATION_MODAL); // Rende la finestra modale (blocca interazione con altre finestre)
            // Imposta la finestra corrente (quella del pasto) come proprietaria della nuova finestra modale
            stage.initOwner(((Node) mealCaloriesLabel).getScene().getWindow());
            stage.showAndWait(); // Mostra la finestra e attende che venga chiusa

            // Dopo la chiusura della finestra AggiungiPasto, aggiorna i totali giornalieri e dei pasti.
            // Si assume che AggiungiPastoController abbia aggiornato il DB, quindi basta ri-calcolare/aggiornare i dati.
            // refreshCurrentDayTotals(); // Questo metodo non è presente, ma 'aggiornaLabelKcalPerPasto()' farà il lavoro.

        } catch (IOException e) {
            System.err.println("ERRORE (HomePage): Errore caricamento FXML AggiungiPasto: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire la finestra di aggiunta pasto.");
        }
    }

    /**
     * Elimina tutti i pasti registrati per il giorno corrente e l'utente loggato.
     * Questa implementazione sembra specifica per il giorno attuale.
     */
    public void eliminaPastiGiornalieriOggi() {
        // Ottieni il giorno attualmente selezionato nella ComboBox
        GiornoDieta giornoSelezionato = comboGiorniDieta.getSelectionModel().getSelectedItem();
        if (giornoSelezionato == null) return; // Se nessun giorno è selezionato, non fare nulla

        // Query SQL per eliminare i pasti del giorno specifico, per un dato cliente e data
        String sql = "DELETE FROM PastiGiornalieri WHERE id_cliente = ? AND data = ? AND id_giorno_dieta = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Session.getUserId()); // ID del cliente dalla sessione
            pstmt.setString(2, LocalDate.now().toString()); // Data corrente
            pstmt.setInt(3, giornoSelezionato.getIdGiornoDieta()); // CORREZIONE: ID del giorno dieta specifico

            int rowsDeleted = pstmt.executeUpdate(); // Esegue l'eliminazione e ottiene il numero di righe eliminate
            System.out.println("DEBUG: Eliminati " + rowsDeleted + " pasti per il giorno " + giornoSelezionato.getNomeGiorno());

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
        }
    }

    /**
     * Elimina i pasti registrati per un giorno dieta specifico e l'utente loggato.
     * Utilizzata quando l'utente cambia giorno nella ComboBox e i pasti del giorno precedente vengono "resettati".
     * @param idGiornoDieta L'ID del giorno dieta i cui pasti devono essere eliminati.
     */
    private void eliminaPastiSpecificiGiorno(int idGiornoDieta) {
        // Query SQL per eliminare i pasti associati a un ID giorno dieta, cliente e data specifici
        String sql = "DELETE FROM PastiGiornalieri WHERE id_cliente = ? AND data = ? AND id_giorno_dieta = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Session.getUserId()); // ID del cliente
            pstmt.setString(2, LocalDate.now().toString()); // Data corrente
            pstmt.setInt(3, idGiornoDieta); // ID del giorno dieta da eliminare

            int rowsDeleted = pstmt.executeUpdate(); // Esegue l'eliminazione
            System.out.println("DEBUG: Eliminati " + rowsDeleted + " pasti per il giorno con ID " + idGiornoDieta);

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
        }
    }

    /**
     * Aggiorna le etichette delle calorie e dei macronutrienti per ogni pasto
     * e i totali giornalieri (correnti) recuperando i dati dal database.
     * Aggiorna anche i cerchi di progresso.
     */
    public void aggiornaLabelKcalPerPasto() {
        Integer userId = Session.getUserId();
        if (userId == null) return; // Se l'utente non è loggato, esci

        // Ottieni il giorno attualmente selezionato nella ComboBox
        GiornoDieta giornoSelezionato = comboGiorniDieta.getSelectionModel().getSelectedItem();
        if (giornoSelezionato == null) {
            // Se non c'è nessun giorno selezionato, resetta tutte le etichette e il progresso a zero
            resetAllLabelsAndProgress();
            return;
        }

        String dataCorrente = LocalDate.now().toString(); // Ottiene la data corrente

        // Query SQL per recuperare i dettagli dei pasti giornalieri per il cliente, la data e il giorno dieta selezionato
        String query = """
        SELECT pasto, kcal, proteine, carboidrati, grassi
        FROM PastiGiornalieri
        WHERE id_cliente = ? AND data = ? AND id_giorno_dieta = ?
    """;

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione
             PreparedStatement stmt = conn.prepareStatement(query)) { // Prepara lo statement

            stmt.setInt(1, userId); // Imposta l'ID utente
            stmt.setString(2, dataCorrente); // Imposta la data corrente
            stmt.setInt(3, giornoSelezionato.getIdGiornoDieta()); // Imposta l'ID del giorno dieta

            ResultSet rs = stmt.executeQuery(); // Esegue la query

            // Reset iniziale di tutte le etichette dei pasti e dei cerchi di progresso a 0
            resetAllLabelsAndProgress();

            int totaleKcal = 0;
            int totaleProteine = 0;
            int totaleCarboidrati = 0;
            int totaleGrassi = 0;

            while (rs.next()) { // Itera sui risultati della query
                String pasto = rs.getString("pasto");
                int kcal = (int) Math.round(rs.getDouble("kcal"));
                int proteine = (int) Math.round(rs.getDouble("proteine")); // CORREZIONE: Math.round()
                int carboidrati = (int) Math.round(rs.getDouble("carboidrati")); // CORREZIONE: Math.round()
                int grassi = (int) Math.round(rs.getDouble("grassi")); // CORREZIONE: Math.round()

                // Aggiorna i totali complessivi
                totaleKcal += kcal;
                totaleProteine += proteine;
                totaleCarboidrati += carboidrati;
                totaleGrassi += grassi;

                // Aggiorna le label specifiche per ogni pasto
                switch (pasto) {
                    case "Colazione" -> colazioneKcalLabel.setText(kcal + " kcal");
                    case "Spuntino"  -> spuntinoKcalLabel.setText(kcal + " kcal");
                    case "Pranzo"    -> pranzoKcalLabel.setText(kcal + " kcal");
                    case "Merenda"   -> merendaKcalLabel.setText(kcal + " kcal");
                    case "Cena"      -> cenaKcalLabel.setText(kcal + " kcal");
                }
            }

            // Aggiorna le etichette dei totali correnti
            String kcalCorrentiText = totaleKcal + " /";
            labelKcalCorrenti.setText(kcalCorrentiText);
            AggiustaFontSize(labelKcalCorrenti, kcalCorrentiText); // Adatta la dimensione del font
            labelProteineCorrenti.setText(totaleProteine + " /");
            labelCarboidratiCorrenti.setText(totaleCarboidrati + " /");
            labelGrassiCorrenti.setText(totaleGrassi + " /");

            // Aggiorna il progresso dei cerchi visivi con i totali calcolati rispetto agli obiettivi
            updateProgressCircle(proteineProgressCircle, totaleProteine, targetProteine);
            updateProgressCircle(carboidratiProgressCircle, totaleCarboidrati, targetCarboidrati);
            updateProgressCircle(grassiProgressCircle, totaleGrassi, targetGrassi);
            updateProgressCircle(kcalProgressCircle, totaleKcal, targetKcal);


        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore DB", "Impossibile caricare le kcal per pasto.");
        }
    }

    /**
     * Resetta tutte le etichette delle calorie per pasto a "0 kcal" e i totali correnti a "0 /".
     * Resetta anche lo stile dei cerchi di progresso e il loro riempimento.
     */
    private void resetAllLabelsAndProgress() {
        colazioneKcalLabel.setText("0 kcal");
        spuntinoKcalLabel.setText("0 kcal");
        pranzoKcalLabel.setText("0 kcal");
        merendaKcalLabel.setText("0 kcal");
        cenaKcalLabel.setText("0 kcal");

        labelKcalCorrenti.setText("0 /");
        AggiustaFontSize(labelKcalCorrenti, "0 /");
        labelProteineCorrenti.setText("0 /");
        labelCarboidratiCorrenti.setText("0 /");
        labelGrassiCorrenti.setText("0 /");

        resetProgressCircles(); // Resetta i cerchi di progresso
    }

    /**
     * Aggiorna un singolo cerchio di progresso con animazione e stile CSS in base alla percentuale raggiunta.
     * @param circle Il Circle JavaFX da aggiornare.
     * @param current Il valore corrente (quanto è stato consumato/raggiunto).
     * @param target L'obiettivo (il valore massimo da raggiungere).
     */
    private void updateProgressCircle(Circle circle, int current, double target) {
        // Evita divisione per zero se il target è 0 (e imposta a 1.0 per non avere errori di visualizzazione)
        double percentage = (target == 0) ? 1.0 : Math.min(current / target, 1.0);
        // Se target è 0 e current è > 0, si può considerare un superamento infinito o errore.
        if (target == 0 && current > 0) percentage = 1.1; // Forzare un superamento se non c'è target ma ci sono valori

        // Calcola la circonferenza del cerchio (2 * π * r)
        double radius = circle.getRadius();
        double circumference = 2 * Math.PI * radius;

        // Calcola il nuovo stroke-dash-offset per il riempimento del cerchio
        // strokeDashOffset determina l'offset iniziale di un pattern di trattini.
        // Impostandolo, si controlla quanto del bordo del cerchio è visibile.
        double newOffset = circumference * (1 - percentage);

        // Aggiorna le classi CSS del cerchio in base al progresso
        updateCircleProgressStyles(circle, percentage);

        // Animazione fluida del riempimento del cerchio (cambio di strokeDashOffset)
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(800), // Durata dell'animazione: 800 millisecondi
                        new KeyValue(circle.strokeDashOffsetProperty(), newOffset) // Anima la proprietà strokeDashOffset al newOffset
                )
        );
        timeline.play(); // Avvia l'animazione
    }

    /**
     * Aggiorna le classi CSS di un Circle per riflettere visivamente il progresso.
     * Vengono rimosse le classi precedenti e aggiunta una nuova classe in base alla percentuale.
     * @param circle Il Circle JavaFX su cui applicare gli stili.
     * @param percentage La percentuale di progresso (da 0.0 a >1.0).
     */
    private void updateCircleProgressStyles(Circle circle, double percentage) {
        // Rimuovi tutte le classi di progresso precedenti per evitare sovrapposizioni
        circle.getStyleClass().removeAll("progress-low", "progress-medium", "progress-high", "progress-complete", "progress-over");

        // Aggiungi la classe appropriata in base alla percentuale di progresso
        if (percentage > 1.0) {
            circle.getStyleClass().add("progress-over"); // Superato l'obiettivo
        } else if (percentage >= 1.0) {
            circle.getStyleClass().add("progress-complete"); // Obiettivo raggiunto (o leggermente superato ma entro tolleranza)
        } else if (percentage >= 0.8) {
            circle.getStyleClass().add("progress-high"); // Quasi completato (80% o più)
        } else if (percentage >= 0.5) {
            circle.getStyleClass().add("progress-medium"); // A metà strada (50% o più)
        } else if (percentage > 0) {
            circle.getStyleClass().add("progress-low"); // Iniziato (maggiore di 0%)
        }
    }

    /**
     * Resetta tutti i cerchi di progresso a zero (riporto visuale a 0%) con animazione.
     * Questo metodo chiama `resetSingleProgressCircle` per ogni cerchio.
     */
    private void resetProgressCircles() {
        resetSingleProgressCircle(proteineProgressCircle);
        resetSingleProgressCircle(carboidratiProgressCircle);
        resetSingleProgressCircle(grassiProgressCircle);
        resetSingleProgressCircle(kcalProgressCircle); // AGGIUNTA: Resetta anche il cerchio delle kcal
    }

    /**
     * Resetta un singolo cerchio di progresso, riportando il suo riempimento a zero e rimuovendo gli stili.
     * @param circle Il Circle JavaFX da resettare.
     */
    private void resetSingleProgressCircle(Circle circle) {
        if (circle == null) return; // Controllo null
        double radius = circle.getRadius();
        double circumference = 2 * Math.PI * radius;

        // Imposta il riempimento a 0 (offset uguale alla circonferenza)
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(300), // Animazione più breve per il reset
                        new KeyValue(circle.strokeDashOffsetProperty(), circumference)
                )
        );
        timeline.play();

        // Rimuovi le classi di stile subito (o dopo l'animazione se si vuole un effetto più graduale)
        circle.getStyleClass().removeAll("progress-low", "progress-medium", "progress-high", "progress-complete", "progress-over");
    }

    /**
     * Seleziona automaticamente il giorno corrente (es. Lunedì, Martedì) nella ComboBox dei giorni dieta,
     * se questo giorno è presente nella lista dei giorni disponibili per la dieta.
     * @return true se il giorno corrente è stato selezionato, false altrimenti.
     */
    private boolean selectCurrentDayIfExists() {
        // Ottiene il nome del giorno corrente (es. "LUNEDI", "MARTEDI") in maiuscolo
        String currentDayName = LocalDate.now().getDayOfWeek().name(); // E.g., MONDAY
        // Lo converte in italiano (es. "Lunedì") per confrontarlo con i nomi dei giorni nella ComboBox
        String dayNameToMatch = switch (currentDayName) {
            case "MONDAY" -> "Lunedì";
            case "TUESDAY" -> "Martedì";
            case "WEDNESDAY" -> "Mercoledì";
            case "THURSDAY" -> "Giovedì";
            case "FRIDAY" -> "Venerdì";
            case "SATURDAY" -> "Sabato";
            case "SUNDAY" -> "Domenica";
            default -> "";
        };

        if (comboGiorniDieta == null || comboGiorniDieta.getItems().isEmpty()) {
            return false; // Se la ComboBox non è pronta o vuota, non può selezionare
        }

        // Itera sugli elementi della ComboBox per trovare il giorno corrispondente
        for (GiornoDieta giorno : comboGiorniDieta.getItems()) {
            if (giorno.getNomeGiorno().equalsIgnoreCase(dayNameToMatch)) { // Confronta ignorando maiuscole/minuscole
                isProgrammaticChange = true; // Imposta il flag per evitare l'alert di conferma
                comboGiorniDieta.getSelectionModel().select(giorno); // Seleziona il giorno
                System.out.println("DEBUG: Giorno corrente selezionato: " + giorno.getNomeGiorno());
                return true; // Ritorna true, selezione avvenuta
            }
        }
        System.out.println("DEBUG: Giorno corrente (" + dayNameToMatch + ") non trovato nel piano dieta.");
        return false; // Giorno corrente non trovato
    }

    /**
     * Ripristina la selezione del giorno dalla sessione utente (se precedentemente salvata).
     * Questo è utile per mantenere lo stato della ComboBox se l'utente torna alla HomePage.
     */
    private void restoreDaySelectionFromSession() {
        GiornoDieta sessionDay = SessionGiornoDieta.getGiornoDietaSelezionato();
        if (sessionDay != null && comboGiorniDieta != null && !comboGiorniDieta.getItems().isEmpty()) {
            // Cerca nella lista della ComboBox un GiornoDieta con lo stesso ID
            for (GiornoDieta giorno : comboGiorniDieta.getItems()) {
                if (giorno.getIdGiornoDieta() == sessionDay.getIdGiornoDieta()) {
                    isProgrammaticChange = true; // Imposta il flag per evitare l'alert di conferma
                    comboGiorniDieta.getSelectionModel().select(giorno); // Seleziona il giorno dalla sessione
                    System.out.println("DEBUG: Giorno ripristinato dalla sessione: " + giorno.getNomeGiorno());
                    return; // Esce una volta trovato e selezionato
                }
            }
        }
        System.out.println("DEBUG: Nessun giorno da ripristinare dalla sessione o non trovato nella ComboBox.");
    }

    /**
     * Adatta la dimensione del font di una Label in base alla lunghezza del testo,
     * per evitare che il testo vada a capo o fuoriesca dai bordi.
     * Questo è un esempio semplificato e potrebbe richiedere un calcolo più preciso
     * basato sulla larghezza effettiva della label e sul font utilizzato.
     * @param label La Label su cui adattare il font.
     * @param text Il testo da visualizzare.
     */
    private void AggiustaFontSize(Label label, String text) {
        // Implementazione semplificata: riduce la dimensione del font se il testo è lungo
        // Per una soluzione più robusta, si dovrebbe calcolare la larghezza del testo con il font corrente
        // e confrontarla con la larghezza disponibile della label.

        if (text.length() > 6 && text.length() <= 8) { // Esempio: se il testo è tra 7 e 8 caratteri
            label.setFont(new Font(20)); // Dimensione font più piccola
        } else if (text.length() > 8) { // Se il testo è molto lungo
            label.setFont(new Font(18)); // Dimensione font ancora più piccola
        } else {
            label.setFont(new Font(24)); // Dimensione font predefinita
        }
        // Nota: Un'implementazione reale dovrebbe considerare la larghezza della label
        // e l'oggetto FontMetrics per un calcolo accurato.
    }
}
