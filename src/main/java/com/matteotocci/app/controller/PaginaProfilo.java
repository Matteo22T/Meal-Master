package com.matteotocci.app.controller; // Dichiarazione del package in cui si trova la classe del controller.

// Importazioni delle classi e interfacce necessarie da altri package.
import com.matteotocci.app.model.Session; // Importa la classe Session per gestire l'ID dell'utente loggato.
import com.matteotocci.app.model.Dieta; // Importa la classe Dieta, che rappresenta un oggetto dieta.
import com.matteotocci.app.model.SQLiteConnessione; // Importa la classe SQLiteConnessione per stabilire la connessione al database SQLite.

import javafx.collections.FXCollections; // Utility per creare ObservableList.
import javafx.collections.ObservableList; // Interfaccia per liste che possono essere osservate da JavaFX per aggiornamenti UI.
import javafx.event.ActionEvent; // Classe per la gestione degli eventi di azione (es. click su un bottone).
import javafx.fxml.FXML; // Annotazione per iniettare componenti FXML nel controller.
import javafx.fxml.FXMLLoader; // Classe per caricare file FXML.
import javafx.fxml.Initializable; // Interfaccia che i controller devono implementare per l'inizializzazione dopo il caricamento FXML.
import javafx.scene.Node; // Classe base per tutti i nodi nel grafo di scena (es. controlli UI).
import javafx.scene.Parent; // Classe base per i nodi che hanno dei figli (es. layout).
import javafx.scene.Scene; // Contenitore per tutto il contenuto del grafo di scena.
import javafx.scene.control.*; // Importa tutti i controlli UI di JavaFX (Label, TextField, ComboBox, Alert, Button).
import javafx.scene.input.MouseEvent; // Classe per la gestione degli eventi del mouse.
import javafx.stage.Modality;
import javafx.stage.Stage; // Finestra principale dell'applicazione.

import java.io.IOException; // Eccezione per errori di I/O.
import java.net.URL; // Classe per rappresentare un URL (usata per caricare risorse come i CSS).
import java.sql.Connection; // Interfaccia per la connessione al database.
import java.sql.DriverManager; // Classe per la gestione dei driver JDBC (utilizzata per la connessione al database).
import java.sql.PreparedStatement; // Interfaccia per l'esecuzione di query SQL precompilate.
import java.sql.ResultSet; // Interfaccia per la gestione dei risultati di una query SQL.
import java.sql.SQLException; // Classe per la gestione delle eccezioni SQL.
import java.time.LocalDate; // Classe per rappresentare una data.
import java.time.format.DateTimeFormatter; // Classe per formattare e parsare date.
import java.time.format.DateTimeParseException; // Eccezione per errori di parsing della data.
import java.util.*; // Importa utility generiche come HashMap, List, Optional.

// Dichiarazione della classe PaginaProfilo, che implementa Initializable per l'inizializzazione dei componenti.
public class PaginaProfilo implements Initializable{


    // Campi annotati con @FXML per l'iniezione dei componenti UI definiti nel file FXML della Pagina Profilo.
    @FXML
    private Label nomeUtenteSidebarLabel; // Label per visualizzare il nome dell'utente nella sidebar.


    @FXML
    private TextField nomeTextField; // Campo di testo per il nome dell'utente.

    @FXML
    private TextField cognomeTextField; // Campo di testo per il cognome dell'utente.

    @FXML
    private ComboBox<String> sessoComboBox; // ComboBox per selezionare il sesso dell'utente.

    @FXML
    private TextField dataNascitaTextField; // Campo di testo per la data di nascita dell'utente.

    @FXML
    private TextField altezzaTextField; // Campo di testo per l'altezza dell'utente.

    @FXML
    private TextField pesoAttualeTextField; // Campo di testo per il peso attuale dell'utente.

    private Dieta dietaAssegnata; // Oggetto Dieta che rappresenta la dieta attualmente assegnata all'utente.

    @FXML
    private ComboBox<String> nutrizionistaComboBox; // ComboBox per selezionare il nutrizionista associato all'utente.


    @FXML
    private ComboBox<String> livelloAttivitaComboBox; // ComboBox per selezionare il livello di attività fisica dell'utente.


    private String nutrizionistaPrecedente; // Variabile per memorizzare la selezione precedente del nutrizionista, utile per rilevare cambiamenti.


    // Metodo di inizializzazione, chiamato automaticamente da JavaFX dopo che il file FXML è stato caricato.
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Crea una lista osservabile di stringhe per i livelli di attività fisica.
        ObservableList<String> livelliAttivita = FXCollections.observableArrayList(
                "Sedentario",
                "Leggermente Attivo",
                "Moderatamente Attivo",
                "Molto Attivo",
                "Estremamente Attivo"
        );
        livelloAttivitaComboBox.setItems(livelliAttivita); // Imposta gli elementi nella ComboBox del livello di attività.

        // Crea una lista osservabile di stringhe per le opzioni di sesso.
        ObservableList<String> sessi = FXCollections.observableArrayList(
                "Maschio",
                "Femmina",
                "Altro" // Opzione aggiuntiva se desiderata
        );
        sessoComboBox.setItems(sessi); // Imposta gli elementi nella ComboBox del sesso.

        ObservableList<String> nutrizionisti = getNutrizionisti(); // Ottiene la lista dei nutrizionisti dal database.
        nutrizionistaComboBox.setItems(nutrizionisti); // Imposta gli elementi nella ComboBox dei nutrizionisti.


        // Chiama il metodo per caricare i dati utente non appena il controller è pronto.
        inizializzaDatiUtente();
        // Recupera la dieta assegnata all'avvio della pagina per l'utente corrente.
        dietaAssegnata=recuperaDietaAssegnataACliente(Session.getUserId());

        // Memorizza la selezione corrente del nutrizionista come "precedente".
        nutrizionistaPrecedente = nutrizionistaComboBox.getSelectionModel().getSelectedItem();
        System.out.println(nutrizionistaPrecedente); // Stampa a scopo di debug il nutrizionista precedente.
    }

    private Map<String, Integer> mappaNutrizionisti = new HashMap<>(); // Mappa per associare il nome completo del nutrizionista al suo ID.


    // Metodo privato per recuperare la lista dei nutrizionisti dal database.
    private ObservableList<String> getNutrizionisti() {
        ObservableList<String> nutrizionisti = FXCollections.observableArrayList(); // Lista osservabile per i nomi dei nutrizionisti.
        String query = "SELECT id,Nome, Cognome FROM Utente WHERE ruolo = 'nutrizionista'"; // Query SQL per selezionare ID, Nome e Cognome degli utenti con ruolo 'nutrizionista'.

        // Blocco try-with-resources per gestire automaticamente la chiusura di Connection, PreparedStatement e ResultSet.
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database SQLite.
             PreparedStatement stmt = conn.prepareStatement(query); // Prepara lo statement SQL.
             ResultSet rs = stmt.executeQuery()) { // Esegue la query e ottiene il ResultSet.

            while (rs.next()) { // Itera su ogni riga del ResultSet.
                int id = rs.getInt("id"); // Ottiene l'ID del nutrizionista.
                String nome = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena nome e cognome.
                nutrizionisti.add(nome); // Aggiunge il nome completo alla lista dei nutrizionisti.
                mappaNutrizionisti.put(nome, id); // Salva l'associazione nome completo -> ID nella mappa.
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa la traccia dello stack in caso di errore SQL.
        }
        return nutrizionisti; // Restituisce la lista dei nutrizionisti.
    }

    // Metodo privato per inizializzare e popolare i campi dell'interfaccia utente con i dati dell'utente corrente.
    private void inizializzaDatiUtente() {
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID dell'utente corrente dalla Sessione.

        if (userIdFromSession != null) { // Se l'ID utente è disponibile.

            // Recupera nome e cognome dell'utente dalla tabella Utente.
            String nome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Nome");
            String cognome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Cognome");

            // Imposta il nome completo nella sidebar.
            String nomeCompleto = (nome != null ? nome : "") + " " + (cognome != null ? cognome : "");
            nomeUtenteSidebarLabel.setText(nomeCompleto.trim()); // Rimuove spazi bianchi iniziali/finali.

            // Imposta i campi TextField del nome e cognome.
            nomeTextField.setText(nome != null ? nome : "");
            cognomeTextField.setText(cognome != null ? cognome : "");

            // Recupera i dati specifici del cliente dalla tabella Clienti.
            String altezza = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "altezza_cm");
            String peso = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "peso_kg");
            String livelloAttivita = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "livello_attivita");
            String dataNascita = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "data_di_nascita");
            String sesso = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "sesso");
            if (sesso != null && !sesso.isEmpty()) {
                sesso = sesso.substring(0, 1).toUpperCase() + sesso.substring(1).toLowerCase(); // Capitalizza la prima lettera del sesso.
            }
            String idNutrizionista = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "id_nutrizionista");

            // Imposta i valori nei TextField, controllando che non siano nulli.
            if (altezzaTextField != null) {
                altezzaTextField.setText(altezza != null ? altezza : "");
            }
            if (pesoAttualeTextField != null) {
                pesoAttualeTextField.setText(peso != null ? peso : "");
            }
            if (dataNascitaTextField != null) {
                dataNascitaTextField.setText(dataNascita != null ? dataNascita : "");
            }
            if (sessoComboBox != null) {
                if (sesso != null && !sesso.isEmpty()) {
                    sessoComboBox.getSelectionModel().select(sesso); // Seleziona il sesso nella ComboBox.
                } else {
                    sessoComboBox.getSelectionModel().clearSelection(); // Cancella la selezione se non ci sono dati.
                    System.out.println("[DEBUG] Nessun sesso trovato per l'utente.");
                }
            }

            // Imposta la selezione nella ComboBox del livello di attività.
            if (livelloAttivita != null && !livelloAttivita.isEmpty()) {
                livelloAttivitaComboBox.getSelectionModel().select(livelloAttivita);
            } else {
                livelloAttivitaComboBox.getSelectionModel().clearSelection(); // Cancella la selezione se non ci sono dati.
                System.out.println("[DEBUG] Nessun livello di attività trovato per l'utente.");
            }

            // Imposta la selezione nella ComboBox del nutrizionista.
            if (idNutrizionista != null && !idNutrizionista.isEmpty()) {
                // Recupera il nome e cognome del nutrizionista dall'ID.
                String nomeNutrizionista = getDatoUtenteDalDatabase("Utente", idNutrizionista, "Nome");
                String cognomeNutrizionista = getDatoUtenteDalDatabase("Utente", idNutrizionista, "Cognome");

                if (nomeNutrizionista != null && cognomeNutrizionista != null) {
                    String nomeCompletoNutrizionista = nomeNutrizionista + " " + cognomeNutrizionista;
                    // Seleziona il nutrizionista nella ComboBox usando il nome completo.
                    nutrizionistaComboBox.getSelectionModel().select(nomeCompletoNutrizionista);
                } else {
                    nutrizionistaComboBox.getSelectionModel().clearSelection(); // Cancella la selezione se il nome non è trovato.
                    System.out.println("[DEBUG] Nutrizionista con ID " + idNutrizionista + " non trovato o dati incompleti.");
                }
            } else {
                nutrizionistaComboBox.getSelectionModel().clearSelection(); // Cancella la selezione se nessun nutrizionista è assegnato.
                System.out.println("[DEBUG] Nessun nutrizionista assegnato all'utente.");
            }

        } else {
            // Se l'ID utente non è disponibile dalla Sessione, svuota tutti i campi e mostra un messaggio di errore.
            System.err.println("[ERROR] ID utente non disponibile dalla Sessione. Impossibile recuperare i dati del profilo.");
            nomeUtenteSidebarLabel.setText("Utente Sconosciuto");
            nomeTextField.setText("");
            cognomeTextField.setText("");
            if (sessoComboBox != null) sessoComboBox.getSelectionModel().clearSelection();
            if (dataNascitaTextField != null) dataNascitaTextField.setText("");
            if (altezzaTextField != null) altezzaTextField.setText("");
            if (pesoAttualeTextField != null) pesoAttualeTextField.setText("");
            // Svuota le selezioni delle ComboBox.
            if (livelloAttivitaComboBox != null) livelloAttivitaComboBox.getSelectionModel().clearSelection();
            if (nutrizionistaComboBox != null) nutrizionistaComboBox.getSelectionModel().clearSelection();
        }
    }

    // Metodo privato per recuperare un singolo dato dell'utente dal database, specificando tabella, ID utente e campo.
    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) {
        String valore = null; // Variabile per immagazzinare il valore recuperato.
        String url = "jdbc:sqlite:database.db"; // URL di connessione al database SQLite.
        String query; // Stringa per la query SQL.
        String idColumn = "id"; // Nome della colonna ID predefinito per la tabella Utente.
        if (tabella.equals("Clienti")) {
            idColumn = "id_cliente"; // Se la tabella è "Clienti", la colonna ID è "id_cliente".
        }
        query = "SELECT " + campo + " FROM " + tabella + " WHERE " + idColumn + " = ?"; // Costruisce la query SQL.
        System.out.println("[DEBUG] Query per " + tabella + " eseguita: " + query + " con ID: " + userId + ", Campo: " + campo); // Stampa la query per debug.

        // Blocco try-with-resources per gestire automaticamente la chiusura di Connection e PreparedStatement.
        try (Connection conn = DriverManager.getConnection(url); // Ottiene una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement SQL.
            pstmt.setString(1, userId); // Imposta il parametro ID nella query.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query e ottiene il ResultSet.

            if (rs.next()) { // Se c'è un risultato.
                valore = rs.getString(campo); // Ottiene il valore del campo specificato.
            } else {
                System.out.println("[DEBUG] Nessun dato trovato con ID: " + userId + " nella tabella " + tabella + " per il campo " + campo); // Messaggio di debug se nessun dato è trovato.
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage()); // Stampa un errore in caso di SQL Exception.
        }
        return valore; // Restituisce il valore recuperato.
    }

    // --- Metodi di Navigazione ---

    // Metodo FXML chiamato quando si tenta di accedere alla pagina del profilo (probabilmente dal sidebar).
    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            // Carica il file FXML della PaginaProfilo.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load(); // Carica il root del FXML.
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo stage corrente.
            profileStage.setScene(new Scene(profileRoot)); // Imposta la nuova scena.
            profileStage.show(); // Mostra lo stage.
        } catch (IOException e) {
            e.printStackTrace(); // Stampa la traccia dello stack in caso di errore di I/O.
        }
    }

    // Metodo FXML chiamato quando si tenta di accedere alla pagina degli alimenti.
    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            // Carica il file FXML della pagina Alimenti.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent root = fxmlLoader.load(); // Carica il root del FXML.
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo stage corrente.
            stage.setScene(new Scene(root)); // Imposta la nuova scena.
            stage.show(); // Mostra lo stage.

        } catch (IOException e) {
            e.printStackTrace(); // Stampa la traccia dello stack in caso di errore di I/O.
        }
    }

    // Metodo FXML chiamato quando si tenta di accedere alla pagina delle ricette.
    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            // Carica il file FXML della pagina Ricette.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Ricette.fxml"));
            Parent root = fxmlLoader.load(); // Carica il root del FXML.
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo stage corrente.
            stage.setScene(new Scene(root)); // Imposta la nuova scena.
            stage.show(); // Mostra lo stage.

        } catch (IOException e) {
            e.printStackTrace(); // Stampa la traccia dello stack in caso di errore di I/O.
        }
    }

    // Metodo FXML chiamato per mostrare la schermata di modifica password.
    @FXML
    private void mostraSchermataModificaPassword(MouseEvent event) {
        try {
            // Carica il file FXML della schermata ModificaPassword.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ModificaPassword.fxml"));
            Parent modificaPasswordRoot = fxmlLoader.load(); // Carica il root del FXML.

            Stage modificaPasswordStage = new Stage(); // Crea un nuovo Stage per la finestra di modifica password.
            modificaPasswordStage.setTitle("Modifica Password"); // Imposta il titolo della finestra.
            modificaPasswordStage.setScene(new Scene(modificaPasswordRoot)); // Imposta la scena.
            modificaPasswordStage.setResizable(false); // Impedisce il ridimensionamento della finestra.
            modificaPasswordStage.setFullScreen(false); // Impedisce la modalità a schermo intero.
            modificaPasswordStage.show(); // Mostra la finestra.
        } catch (IOException e) {
            e.printStackTrace(); // Stampa la traccia dello stack in caso di errore di I/O.
            showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata di modifica password."); // Mostra un alert di errore.
        }
    }

    // Metodo FXML chiamato per navigare alla HomePage.
    @FXML
    private void vaiAllaHomePage(ActionEvent event) {
        try {
            // Carica il file FXML della HomePage.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePage.fxml"));
            Parent root = loader.load(); // Carica il root del FXML.

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo stage corrente.
            Scene scene = new Scene(root); // Crea una nuova scena.
            stage.setScene(scene); // Imposta la nuova scena.
            stage.show(); // Mostra lo stage.
        } catch (IOException e) {
            e.printStackTrace(); // Stampa la traccia dello stack in caso di errore di I/O.
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la HomePage. Contattare l'amministratore."); // Mostra un alert di errore.
        }
    }



    // Metodo FXML chiamato per eseguire il logout dall'applicazione.
    @FXML
    private void eseguiLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert-confirmation");
        alert.setTitle("Conferma Logout");
        alert.setHeaderText("Sei sicuro di voler uscire?");
        alert.setContentText("Clicca OK per confermare o Annulla per rimanere.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PrimaPagina.fxml")); // Carica il file FXML della PrimaPagina.
                Parent loginRoot = fxmlLoader.load(); // Ottiene il nodo radice.

                // Crea un nuovo Stage (nuova finestra)
                Stage newStage = new Stage();
                Scene loginScene = new Scene(loginRoot); // Crea una nuova scena con la PrimaPagina.

                newStage.setScene(loginScene); // Imposta la scena sul nuovo stage.
                newStage.setTitle("Benvenuto"); // Imposta un titolo a piacere.
                newStage.setResizable(false); // Finestra non ridimensionabile.
                newStage.setFullScreen(false); // Non a schermo intero.
                newStage.show(); // Mostra la nuova finestra.

                // (Facoltativo) Chiudi la finestra corrente, se vuoi
                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Metodo FXML chiamato per mostrare la schermata del calcolo BMI.
    @FXML
    private void mostraBMI(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/BMI.fxml"));
            Parent bmiRoot = fxmlLoader.load();

            // PASSO FONDAMENTALE: Ottieni il controller DOPO il caricamento del FXML
            BMI bmiController = fxmlLoader.getController();

            // PASSO FONDAMENTALE: Passa l'ID dell'utente al controller BMI
            // Assumiamo che Session.getUserId() ritorni un Integer
            Integer currentUserId = Session.getUserId();
            if (currentUserId != null) {
                bmiController.setUtenteCorrenteId(String.valueOf(currentUserId));
            } else {
                // Gestisci il caso in cui l'utente non sia loggato
                showAlert(Alert.AlertType.ERROR, "Errore Utente", "ID utente non disponibile. Impossibile caricare il BMI.");
                return; // Ferma l'esecuzione se l'ID non è disponibile
            }


            Stage bmiStage = new Stage();
            bmiStage.setTitle("Calcolo BMI");
            bmiStage.setScene(new Scene(bmiRoot));

            bmiStage.initModality(Modality.WINDOW_MODAL);
            // 2. Specifica la finestra padre. Questo è fondamentale per la modalità WINDOW_MODAL.
            //    Si ottiene la finestra dell'elemento che ha generato l'evento (es. il pulsante).
            bmiStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            bmiStage.setResizable(false); // Impedisce il ridimensionamento.
            bmiStage.setFullScreen(false); // Impedisce la modalità a schermo intero.
            bmiStage.show(); // Mostra la finestra.

            bmiStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata di calcolo BMI.");
        }
    }


    // Metodo per recuperare la dieta assegnata a un cliente specifico dal database.
    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null; // Inizializza l'oggetto Dieta a null.
        // Query SQL per selezionare i dettagli della dieta basandosi sull'ID del cliente.
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        // Blocco try-with-resources per gestire automaticamente la chiusura di Connection e PreparedStatement.
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente); // Imposta l'ID del cliente come parametro nella query.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query e ottiene il ResultSet.

            if (rs.next()) { // Se viene trovato un risultato.
                // Crea un nuovo oggetto Dieta con i dati recuperati dal ResultSet.
                dieta = new Dieta(
                        rs.getInt("id"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine"),
                        rs.getInt("id_nutrizionista"),
                        rs.getInt("id_cliente")
                );
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (PaginaProfilo): Errore durante il recupero della dieta per il cliente: " + e.getMessage()); // Stampa un messaggio di errore.
            e.printStackTrace(); // Stampa la traccia dello stack.
        }
        return dieta; // Restituisce l'oggetto Dieta (o null se non trovata).
    }


    // Metodo privato per mostrare un Alert (finestra di messaggio) all'utente.
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType); // Crea un nuovo Alert del tipo specificato (es. ERROR, INFORMATION).
        alert.setTitle(title); // Imposta il titolo della finestra di alert.
        alert.setHeaderText(null); // Rimuove l'header text.
        alert.setContentText(message); // Imposta il contenuto del messaggio.
        // Tenta di caricare un foglio di stile CSS personalizzato per l'alert.
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) { // Se il CSS è stato trovato.
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // Aggiunge il CSS ai fogli di stile del dialog pane.
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base.
            // Aggiunge classi di stile specifiche in base al tipo di Alert per personalizzare ulteriormente l'aspetto.
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Messaggio di errore se il CSS non viene trovato.
        }

        alert.showAndWait(); // Mostra l'alert e attende che l'utente lo chiuda.
    }


    // Metodo FXML chiamato per accedere al piano alimentare dell'utente.
    @FXML
    private void AccessoPianoAlimentare(ActionEvent event) {
        if (dietaAssegnata != null) { // Se è stata assegnata una dieta all'utente.
            try {
                // Carica il file FXML della schermata VisualizzaDieta.
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                Parent visualizzaDietaRoot = fxmlLoader.load(); // Carica il root del FXML.

                VisualizzaDieta visualizzaDietaController = fxmlLoader.getController(); // Ottiene il controller della nuova schermata.

                // Passa l'oggetto Dieta recuperato al controller della pagina di visualizzazione.
                visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                System.out.println("DEBUG (PaginaProfilo): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta."); // Debugging.

                Stage dietaStage = new Stage(); // Crea un nuovo Stage per la finestra della dieta.
                dietaStage.setScene(new Scene(visualizzaDietaRoot)); // Imposta la scena.
                dietaStage.show(); // Mostra la finestra.

            } catch (IOException e) {
                System.err.println("ERRORE (PaginaProfilo): Errore caricamento FXML VisualizzaDieta: " + e.getMessage()); // Stampa un messaggio di errore di I/O.
                e.printStackTrace(); // Stampa la traccia dello stack.
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta."); // Mostra un alert di errore.
            } catch (Exception e) {
                System.err.println("ERRORE (PaginaProfilo): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage()); // Stampa un errore generico.
                e.printStackTrace(); // Stampa la traccia dello stack.
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso."); // Mostra un alert di errore generico.
            }
        } else {
            System.out.println("DEBUG (PaginaProfilo): Nessuna dieta trovata per il cliente (ID: " + Session.getUserId() + ")."); // Debugging.
            showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta",
                    "Il cliente non ha diete assegnate o non è stato possibile recuperarle."); // Mostra un alert informativo.
        }
    }

    // Metodo FXML chiamato per salvare le modifiche al profilo dell'utente.
    @FXML
    private void salvaProfilo(ActionEvent event) {
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID dell'utente corrente dalla sessione.

        if (userIdFromSession == null) { // Se l'ID utente non è valido.
            showAlert(Alert.AlertType.ERROR, "Errore di salvataggio",  "Impossibile salvare il profilo senza un ID utente valido."); // Mostra un alert di errore.
            return; // Esce dal metodo.
        }

        String altezzaStr = altezzaTextField.getText().trim(); // Ottiene l'altezza dal campo di testo e rimuove spazi.
        String pesoStr = pesoAttualeTextField.getText().trim(); // Ottiene il peso dal campo di testo e rimuove spazi.


        String sessoSelezionato = sessoComboBox.getSelectionModel().getSelectedItem(); // Ottiene il sesso selezionato.
        String livelloAttivita = livelloAttivitaComboBox.getSelectionModel().getSelectedItem(); // Ottiene il livello di attività selezionato.
        String nutrizionistaSelezionato = nutrizionistaComboBox.getSelectionModel().getSelectedItem(); // Ottiene il nutrizionista selezionato.

        String dataNascitaText = dataNascitaTextField.getText().trim(); // Ottiene la data di nascita.
        String dataNascitaFormattedForDb = null; // Variabile per la data formattata per il database.

        if (!dataNascitaText.isEmpty()) { // Se la data di nascita non è vuota.
            try {
                // Tenta di parsare la data nel formato yyyy-MM-dd.
                LocalDate parsedDate = LocalDate.parse(dataNascitaText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                // Formatta la data per il database nel formato ISO (yyyy-MM-DD).
                dataNascitaFormattedForDb = parsedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                // Errore di formato della data.
                showAlert(Alert.AlertType.ERROR, "Errore Data", "Formato data non valido o data inesistente. La data di nascita deve essere nel formato AAAA-MM-GG (es. 1990-01-01).");
                return; // Ferma il salvataggio se la data non è valida.
            }
        }

        // Determina se il nutrizionista è cambiato.
        boolean nutrizionistaCambiato = false;
        System.out.println(nutrizionistaSelezionato+" "+nutrizionistaPrecedente); // Debugging.
        if (nutrizionistaPrecedente == null && nutrizionistaSelezionato != null) {
            nutrizionistaCambiato = true; // Se prima non c'era e ora c'è.
        } else if (nutrizionistaPrecedente != null && nutrizionistaSelezionato == null) {
            nutrizionistaCambiato = true; // Se prima c'era e ora non c'è.
        } else if (nutrizionistaPrecedente != null && nutrizionistaSelezionato != null && !nutrizionistaSelezionato.equals(nutrizionistaPrecedente)) {
            System.out.println("cambiato true"); // Debugging.
            nutrizionistaCambiato = true; // Se c'erano entrambi ma sono diversi.
        }


        Double altezza = null; // Variabile per l'altezza in Double.
        Double peso = null; // Variabile per il peso in Double.
        if (!altezzaStr.isEmpty()) { // Se l'altezza non è vuota.
            try {
                altezza = Double.parseDouble(altezzaStr); // Converte l'altezza in Double.
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Errore di formato", "Altezza non valida."); // Mostra un alert di errore.
                return; // Esce dal metodo.
            }
        }

        if (!pesoStr.isEmpty()) { // Se il peso non è vuoto.
            try {
                peso = Double.parseDouble(pesoStr); // Converte il peso in Double.
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Errore di formato", "Peso non valido."); // Mostra un alert di errore.
                return; // Esce dal metodo.
            }
        }

        Integer idNutrizionista = null; // Variabile per l'ID del nutrizionista.
        if (nutrizionistaSelezionato != null && !nutrizionistaSelezionato.isEmpty()) { // Se un nutrizionista è stato selezionato.
            idNutrizionista = mappaNutrizionisti.get(nutrizionistaSelezionato); // Recupera l'ID dalla mappa.
            if (idNutrizionista == null) { // Se l'ID non è stato trovato (errore nella selezione).
                System.err.println("[ERROR] ID nutrizionista non trovato per: " + nutrizionistaSelezionato); // Debugging.
                showAlert(Alert.AlertType.ERROR, "Errore Nutrizionista",  "Si prega di selezionare un nutrizionista valido dall'elenco."); // Mostra un alert di errore.
                return; // Esce dal metodo.
            }
        }

        // Blocco try-with-resources per gestire la connessione al database e le transazioni.
        try (Connection conn = SQLiteConnessione.connector()) {
            conn.setAutoCommit(false); // Disabilita l'auto-commit per gestire manualmente la transazione.

            // --- PASSO 1: Controlla se il cliente esiste già nella tabella Clienti ---
            boolean clienteEsiste = false;
            String checkClientQuery = "SELECT COUNT(*) FROM Clienti WHERE id_cliente = ?"; // Query per contare le righe con l'ID cliente.
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkClientQuery)) {
                pstmtCheck.setInt(1, userIdFromSession); // Imposta l'ID del cliente.
                ResultSet rs = pstmtCheck.executeQuery(); // Esegue la query.
                if (rs.next() && rs.getInt(1) > 0) { // Se il conteggio è maggiore di 0, il cliente esiste.
                    clienteEsiste = true;
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] Errore durante il controllo esistenza cliente: " + e.getMessage()); // Debugging.
                throw e; // Rilancia l'eccezione per far scattare il rollback.
            }

            if (clienteEsiste) {
                // --- PASSO 2a: Se il cliente esiste, esegui l'UPDATE dei suoi dati ---
                String updateClientiQuery = "UPDATE Clienti SET altezza_cm = ?, peso_kg = ?, livello_attivita = ?, id_nutrizionista = ?, sesso = ?, data_di_nascita = ? WHERE id_cliente = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateClientiQuery)) {
                    // Imposta i valori per l'update, gestendo i casi nulli.
                    if (altezza != null) {
                        pstmt.setDouble(1, altezza);
                    } else {
                        pstmt.setNull(1, java.sql.Types.DOUBLE);
                    }

                    if (peso != null) {
                        pstmt.setDouble(2, peso);
                    } else {
                        pstmt.setNull(2, java.sql.Types.DOUBLE);
                    }

                    pstmt.setString(3, livelloAttivita);

                    if (idNutrizionista != null) {
                        pstmt.setInt(4, idNutrizionista);
                    } else {
                        pstmt.setNull(4, java.sql.Types.INTEGER);
                    }

                    pstmt.setString(5, sessoSelezionato.toLowerCase()); // Salva il sesso in minuscolo.
                    pstmt.setString(6, dataNascitaFormattedForDb); // Salva la data di nascita formattata.

                    pstmt.setInt(7, userIdFromSession); // Imposta l'ID del cliente per la clausola WHERE.
                    pstmt.executeUpdate(); // Esegue l'update.
                    System.out.println("[DEBUG] Profilo cliente aggiornato con successo."); // Debugging.
                }
            } else {
                // --- PASSO 2b: Se il cliente NON esiste, esegui l'INSERT di un nuovo cliente ---
                String insertClientiQuery = "INSERT INTO Clienti (id_cliente, altezza_cm, peso_kg, livello_attivita, id_nutrizionista, sesso, data_di_nascita) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertClientiQuery)) {
                    pstmt.setInt(1, userIdFromSession); // id_cliente
                    // Imposta i valori per l'insert, gestendo i casi nulli.
                    if (altezza != null) {
                        pstmt.setDouble(2, altezza);
                    } else {
                        pstmt.setNull(2, java.sql.Types.DOUBLE);
                    }

                    if (peso != null) {
                        pstmt.setDouble(3, peso);
                    } else {
                        pstmt.setNull(3, java.sql.Types.DOUBLE);
                    }

                    pstmt.setString(4, livelloAttivita);

                    if (idNutrizionista != null) {
                        pstmt.setInt(5, idNutrizionista);
                    } else {
                        pstmt.setNull(5, java.sql.Types.INTEGER);
                    }

                    pstmt.setString(6, sessoSelezionato.toLowerCase());
                    pstmt.setString(7, dataNascitaFormattedForDb);

                    pstmt.executeUpdate(); // Esegue l'insert.
                    System.out.println("[DEBUG] Nuovo profilo cliente creato con successo."); // Debugging.
                }
            }

            // --- Logica aggiunta per la gestione della dieta ---
            System.out.println(dietaAssegnata); // Debugging.
            // Se il nutrizionista è cambiato E c'è una dieta assegnata.
            if (nutrizionistaCambiato && dietaAssegnata != null) {
                System.out.println("[DEBUG] Nutrizionista cambiato e dieta assegnata. Eseguo pulizia pasti giornalieri e dissociazione dieta."); // Debugging.

                // 1. Recupera tutti gli id_giorno_dieta associati alla dieta del cliente.
                String selectGiornoDietaIdsQuery = "SELECT id_giorno_dieta FROM Giorno_dieta WHERE id_dieta = ?";
                List<Integer> giornoDietaIds = new ArrayList<>();
                try (PreparedStatement pstmtSelectGiorno = conn.prepareStatement(selectGiornoDietaIdsQuery)) {
                    pstmtSelectGiorno.setInt(1, dietaAssegnata.getId()); // Imposta l'ID della dieta.
                    ResultSet rs = pstmtSelectGiorno.executeQuery(); // Esegue la query.
                    while (rs.next()) {
                        giornoDietaIds.add(rs.getInt("id_giorno_dieta")); // Aggiunge gli ID dei giorni dieta alla lista.
                    }
                }

                // 2. Se ci sono id_giorno_dieta validi, elimina i pasti giornalieri associati.
                if (!giornoDietaIds.isEmpty()) {
                    // Crea una stringa di placeholder per la clausola IN (es. ?, ?, ?).
                    String placeholders = String.join(",", Collections.nCopies(giornoDietaIds.size(), "?"));
                    String deletePastiQuery = "DELETE FROM PastiGiornalieri WHERE id_giorno_dieta IN (" + placeholders + ")";
                    try (PreparedStatement pstmtPasti = conn.prepareStatement(deletePastiQuery)) {
                        for (int i = 0; i < giornoDietaIds.size(); i++) {
                            pstmtPasti.setInt(i + 1, giornoDietaIds.get(i)); // Imposta i parametri per la clausola IN.
                        }
                        int affectedRows = pstmtPasti.executeUpdate(); // Esegue l'eliminazione.
                        System.out.println("[DEBUG] Eliminati " + affectedRows + " pasti giornalieri associati alla dieta ID: " + dietaAssegnata.getId()); // Debugging.
                    }
                } else {
                    System.out.println("[DEBUG] Nessun Giorno_dieta trovato per la dieta ID: " + dietaAssegnata.getId() + ". Nessun pasto giornaliero da eliminare."); // Debugging.
                }

                // 3. Setta id_cliente a NULL nella tabella Diete per dissociare la dieta dal cliente.
                String updateDietaQuery = "UPDATE Diete SET id_cliente = NULL WHERE id = ?";
                try (PreparedStatement pstmtDieta = conn.prepareStatement(updateDietaQuery)) {
                    pstmtDieta.setInt(1, dietaAssegnata.getId()); // Imposta l'ID della dieta.
                    pstmtDieta.executeUpdate(); // Esegue l'update.
                    System.out.println("[DEBUG] id_cliente settato a NULL per la dieta ID: " + dietaAssegnata.getId()); // Debugging.
                }
                dietaAssegnata = null; // Resetta dietaAssegnata a null dopo averla dissociata con successo.
            }

            conn.commit(); // Conferma la transazione: tutte le operazioni SQL vengono applicate permanentemente.
            showAlert(Alert.AlertType.INFORMATION, "Salvataggio completato", "Profilo aggiornato con successo!"); // Mostra un messaggio di successo.
            inizializzaDatiUtente(); // Re-inizializza i dati dell'interfaccia utente per riflettere i cambiamenti.
            // Aggiorna la variabile `nutrizionistaPrecedente` con la nuova selezione o null.
            if (nutrizionistaComboBox.getSelectionModel().getSelectedItem() != null) {
                nutrizionistaPrecedente = nutrizionistaComboBox.getSelectionModel().getSelectedItem();
            } else {
                nutrizionistaPrecedente = null;
            }
            dietaAssegnata = recuperaDietaAssegnataACliente(userIdFromSession); // Aggiorna la variabile `dietaAssegnata` per riflettere eventuali modifiche o dissociazioni.
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Errore di database", "Impossibile salvare il profilo."); // Mostra un alert di errore del database.
            e.printStackTrace(); // Stampa la traccia dello stack per debug.
            try (Connection conn = SQLiteConnessione.connector()) { // Tenta di ottenere una nuova connessione per il rollback.
                if (conn != null) {
                    conn.rollback(); // Annulla la transazione in caso di errore, ripristinando lo stato precedente del database.
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Errore durante il rollback: " + rollbackEx.getMessage()); // Stampa un errore se il rollback fallisce.
                showAlert(Alert.AlertType.ERROR, "Errore Database", "Errore durante il rollback della transazione."); // Mostra un alert per l'errore di rollback.
            }
        }
    }
}