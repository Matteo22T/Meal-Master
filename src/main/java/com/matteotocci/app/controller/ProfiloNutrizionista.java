package com.matteotocci.app.controller; // Dichiara il package a cui appartiene questa classe.

import com.matteotocci.app.model.Session; // Importa la classe Session, utilizzata per accedere all'ID dell'utente loggato.
import javafx.event.ActionEvent; // Importa ActionEvent, per gestire gli eventi di azione (es. click su un bottone).
import javafx.fxml.FXML; // Importa l'annotazione FXML, per collegare gli elementi dell'interfaccia utente definiti in FXML al codice Java.
import javafx.fxml.FXMLLoader; // Importa FXMLLoader, per caricare file FXML.
import javafx.fxml.Initializable; // Importa l'interfaccia Initializable, per i controller che devono essere inizializzati dopo il caricamento dell'FXML.
import javafx.scene.Node; // Importa Node, la classe base per gli elementi del grafo della scena.
import javafx.scene.Parent; // Importa Parent, la classe base per i nodi contenitori.
import javafx.scene.Scene; // Importa Scene, per gestire il contenuto di una finestra.
import javafx.scene.control.*; // Importa tutti i controlli UI standard di JavaFX (Button, Label, TextField, Alert).
import javafx.scene.image.ImageView; // Importa ImageView, per visualizzare immagini nell'interfaccia.
import javafx.scene.input.MouseEvent; // Importa MouseEvent, per gestire gli eventi del mouse (es. click su un'immagine).
import javafx.stage.Stage; // Importa Stage, per gestire le finestre dell'applicazione.

import java.io.IOException; // Importa IOException, per gestire gli errori di input/output (es. caricamento FXML).
import java.net.URL; // Importa URL, necessario per l'interfaccia Initializable.
import java.sql.Connection; // Importa Connection, per la connessione al database.
import java.sql.DriverManager; // Importa DriverManager, per gestire i driver JDBC e ottenere connessioni.
import java.sql.PreparedStatement; // Importa PreparedStatement, per eseguire query SQL precompilate.
import java.sql.ResultSet; // Importa ResultSet, per leggere i risultati delle query SQL.
import java.sql.SQLException; // Importa SQLException, per gestire errori del database.
import java.util.Optional; // Importa Optional, per gestire valori che potrebbero essere assenti (es. risultato di un Alert).
import java.util.ResourceBundle; // Importa ResourceBundle, necessario per l'interfaccia Initializable.

public class ProfiloNutrizionista implements Initializable { // Dichiara la classe ProfiloNutrizionista e implementa Initializable.

    @FXML
    private ImageView profileImage; // Campo FXML per l'immagine del profilo.
    @FXML
    private Label nomeUtenteSidebarLabel; // Campo FXML per la label del nome utente nella sidebar.
    @FXML
    private Button ClientiButton; // Campo FXML per il bottone "Clienti".
    @FXML
    private Button BottoneAlimenti; // Campo FXML per il bottone "Alimenti".
    @FXML
    private Button BottoneDieta; // Campo FXML per il bottone "Diete".
    @FXML
    private Label benvenutoLabel; // Campo FXML per la label di benvenuto.
    @FXML
    private TextField nomeTextField; // Campo FXML per il campo di testo del nome.
    @FXML
    private TextField cognomeTextField; // Campo FXML per il campo di testo del cognome.
    @FXML
    private TextField sessoTextField; // Campo FXML per il campo di testo del sesso.
    @FXML
    private TextField dataNascitaTextField; // Campo FXML per il campo di testo della data di nascita.
    @FXML
    private Label modificaPasswordLabel; // Campo FXML per la label "Modifica Password".
    @FXML
    private Button LogoutButton; // Campo FXML per il bottone "Logout".
    @FXML
    private Label ruoloUtenteLabel; // Campo FXML per la label del ruolo dell'utente.


    // Questo metodo initialize viene chiamato automaticamente dopo che i campi FXML sono stati iniettati.
    // È presente un errore qui: si chiama `initialize()` senza parametri, ma dovrebbe avere `URL url, ResourceBundle resourceBundle`
    // come definito dall'interfaccia Initializable. La versione corretta è quella più in basso.
    @FXML
    public void initialize() { // Questo metodo initialize è errato per l'interfaccia Initializable, dovrebbe avere due parametri.
        inizializzaProfilo(); // Chiama il metodo per inizializzare i dati del profilo.
        ruoloUtenteLabel.setText("Nutrizionista"); // Imposta il testo del ruolo utente a "Nutrizionista".
    }

    private void inizializzaProfilo() { // Metodo privato per inizializzare i campi del profilo con i dati dell'utente.
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID dell'utente dalla Sessione.

        if (userIdFromSession != null) { // Controlla se l'ID utente è disponibile.

            // Recupera nome e cognome dell'utente dalla tabella Utente
            String nome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Nome"); // Recupera il nome dell'utente dal database.
            String cognome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Cognome"); // Recupera il cognome dell'utente dal database.

            // Imposta il nome completo nella sidebar
            String nomeCompleto = (nome != null ? nome : "") + " " + (cognome != null ? cognome : ""); // Concatena nome e cognome, gestendo i casi null.
            nomeUtenteSidebarLabel.setText(nomeCompleto.trim()); // Imposta il testo della label nella sidebar, rimuovendo spazi extra.

            // Imposta i campi TextField del nome e cognome
            nomeTextField.setText(nome != null ? nome : ""); // Imposta il testo del campo nome.
            cognomeTextField.setText(cognome != null ? cognome : ""); // Imposta il testo del campo cognome.
        }
        else { // Se l'ID utente non è disponibile.
            System.err.println("[ERROR] ID utente non disponibile dalla Sessione. Impossibile recuperare i dati del profilo."); // Stampa un messaggio di errore.
            nomeUtenteSidebarLabel.setText("Utente Sconosciuto"); // Imposta la label della sidebar.
            nomeTextField.setText(""); // Pulisce il campo nome.
            cognomeTextField.setText(""); // Pulisce il campo cognome.
        }
    }

    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) { // Metodo privato per recuperare un singolo dato utente dal database.
        String valore = null; // Variabile per memorizzare il valore recuperato.
        String url = "jdbc:sqlite:database.db"; // URL del database SQLite.
        String query = "SELECT " + campo + " FROM " + tabella + " WHERE id = ?"; // Query SQL per selezionare un campo da una tabella in base all'ID.

        try (Connection conn = DriverManager.getConnection(url); // Tenta di ottenere una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement SQL.
            pstmt.setString(1, userId); // Imposta l'ID utente come parametro della query.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query e ottiene il ResultSet.

            if (rs.next()) { // Se il ResultSet contiene una riga.
                valore = rs.getString(campo); // Ottiene il valore del campo specificato.
            }

        } catch (SQLException e) { // Cattura le eccezioni SQL.
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage()); // Stampa un messaggio di errore.
        }
        return valore; // Restituisce il valore recuperato (o null se non trovato/errore).
    }

    @FXML
    private void vaiAiClienti(ActionEvent event) { // Metodo FXML per navigare alla pagina HomePageNutrizionista (clienti).
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent homePageRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage corrente.
            stage.setScene(new Scene(homePageRoot)); // Imposta la nuova scena.
            stage.show(); // Mostra lo Stage.
        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }
    @FXML
    private void AccessoDieta(ActionEvent event) { // Metodo FXML per navigare alla pagina DietaNutrizionista.
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent dietaRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.

            Stage dietaStage = new Stage(); // Crea un nuovo Stage per la finestra della dieta.
            dietaStage.setScene(new Scene(dietaRoot)); // Imposta la scena per il nuovo Stage.
            dietaStage.setTitle("Diete"); // Imposta il titolo della finestra.
            dietaStage.show(); // Mostra la nuova finestra.
            ((Stage) BottoneDieta.getScene().getWindow()).close(); // Chiude la finestra corrente del profilo.
        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }


    @FXML
    private void AccessoRicette(ActionEvent event) { // Metodo FXML per navigare alla pagina RicetteNutrizionista.
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/RicetteNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent root = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage corrente.
            stage.setScene(new Scene(root)); // Imposta la nuova scena.
            stage.show(); // Mostra lo Stage.

        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) { // Metodo FXML per navigare alla pagina AlimentiNutrizionista.
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AlimentiNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent loginRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.
            Stage loginStage = new Stage(); // Crea un nuovo Stage per la finestra degli alimenti.
            loginStage.setScene(new Scene(loginRoot)); // Imposta la scena per il nuovo Stage.
            loginStage.show(); // Mostra la nuova finestra.
            ((Stage) BottoneAlimenti.getScene().getWindow()).close(); // Chiude la finestra corrente del profilo.
        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    @FXML
    private void mostraSchermataModificaPassword(MouseEvent event) { // Metodo FXML per mostrare la schermata di modifica password (attivato da un click del mouse).
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ModificaPassword.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent modificaPasswordRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.

            Stage modificaPasswordStage = new Stage(); // Crea un nuovo Stage per la finestra di modifica password.
            modificaPasswordStage.setTitle("Modifica Password"); // Imposta il titolo della finestra.
            modificaPasswordStage.setScene(new Scene(modificaPasswordRoot)); // Imposta la scena per il nuovo Stage.
            modificaPasswordStage.setResizable(false); // Rende la finestra non ridimensionabile.
            modificaPasswordStage.setFullScreen(false); // Disabilita la modalità a schermo intero.
            modificaPasswordStage.show(); // Mostra la nuova finestra.
        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    public void eseguiLogout(ActionEvent event) { // Metodo pubblico per gestire l'azione di logout.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Crea un Alert di tipo CONFIRMATION.
        alert.setTitle("Conferma Logout"); // Imposta il titolo dell'alert.
        alert.setHeaderText("Sei sicuro di voler uscire?"); // Imposta il testo dell'intestazione dell'alert.
        alert.setContentText("Clicca OK per confermare o Annulla per rimanere."); // Imposta il testo del contenuto dell'alert.
        // Aggiunge il foglio di stile CSS personalizzato all'alert.
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base al pannello del dialogo.
        alert.getDialogPane().getStyleClass().add("alert-confirmation"); // Applica la classe di stile specifica per gli alert di conferma.

        Optional<ButtonType> result = alert.showAndWait(); // Mostra l'alert e attende la risposta dell'utente (OK o ANNULLA).
        if (result.isPresent() && result.get() == ButtonType.OK) { // Se l'utente ha cliccato OK.
            try { // Inizia un blocco try-catch per gestire l'IOException.
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PrimaPagina.fxml")); // Carica il file FXML della PrimaPagina (schermata di login o benvenuto iniziale).
                Parent loginRoot = fxmlLoader.load(); // Ottiene il nodo radice dal file FXML caricato.

                // Crea un nuovo Stage (nuova finestra)
                Stage newStage = new Stage(); // Crea una nuova istanza di Stage (una nuova finestra).
                Scene loginScene = new Scene(loginRoot); // Crea una nuova scena con la PrimaPagina come contenuto.

                newStage.setScene(loginScene); // Imposta la scena sul nuovo stage.
                newStage.setTitle("Benvenuto"); // Imposta un titolo a piacere per la nuova finestra.
                newStage.setResizable(false); // Rende la nuova finestra non ridimensionabile.
                newStage.setFullScreen(false); // Non visualizza la nuova finestra a schermo intero.
                newStage.show(); // Mostra la nuova finestra.

                // (Facoltativo) Chiudi la finestra corrente, se vuoi
                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close(); // Chiude la finestra da cui è stato attivato il logout.

            } catch (IOException e) { // Cattura l'eccezione IOException se il caricamento del file FXML fallisce.
                e.printStackTrace(); // Stampa lo stack trace dell'eccezione per debug.
            }

        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // Metodo di inizializzazione corretto per l'interfaccia Initializable.
        inizializzaProfilo(); // Chiama il metodo per inizializzare i dati del profilo al caricamento.
        // Nota: La riga 'ruoloUtenteLabel.setText("Nutrizionista");' era duplicata e non era nel initialize corretto.
        // Dovrebbe essere qui o nel CSS se il testo è statico.
    }
}

