package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
import com.matteotocci.app.model.UtenteModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

// Dichiarazione della classe ModificaPassword, che implementa Initializable per l'inizializzazione dei componenti.
public class ModificaPassword implements Initializable {

    // Campi annotati con @FXML per l'iniezione dei componenti UI definiti nel file FXML.
    @FXML
    private PasswordField vecchiaPasswordField; // Campo di testo per la vecchia password (nascosta).
    @FXML
    private PasswordField nuovaPasswordField; // Campo di testo per la nuova password (nascosta).
    @FXML
    private PasswordField confermaPasswordField; // Campo di testo per la conferma della nuova password (nascosta).
    @FXML
    private CheckBox mostraVecchiaPasswordCheckBox; // CheckBox per mostrare/nascondere la vecchia password.
    @FXML
    private CheckBox mostraNuovaPasswordCheckBox; // CheckBox per mostrare/nascondere la nuova password.
    @FXML
    private CheckBox mostraConfermaPasswordCheckBox; // CheckBox per mostrare/nascondere la conferma della password.
    @FXML
    private TextField vecchiaPasswordFieldVisible; // Campo di testo per la vecchia password (visibile).
    @FXML
    private TextField nuovaPasswordFieldVisible; // Campo di testo per la nuova password (visibile).
    @FXML
    private TextField confermaPasswordFieldVisible; // Campo di testo per la conferma della password (visibile).

    // Variabili d'istanza private.
    private String utenteCorrenteId= Session.getUserId().toString(); // Ottiene l'ID dell'utente corrente dalla sessione e lo converte in stringa.
    private UtenteModel utenteModel=new UtenteModel(); // Crea un'istanza di UtenteModel per interagire con i dati dell'utente.


    // Metodo di inizializzazione, chiamato automaticamente da JavaFX dopo che il file FXML è stato caricato.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Collega la proprietà 'managed' e 'visible' dei TextField visibili alla proprietà 'selected' delle rispettive CheckBox.
        // Questo fa sì che i TextField visibili appaiano e scompaiano quando le CheckBox sono selezionate/deselezionate.
        vecchiaPasswordFieldVisible.managedProperty().bind(mostraVecchiaPasswordCheckBox.selectedProperty());
        vecchiaPasswordFieldVisible.visibleProperty().bind(mostraVecchiaPasswordCheckBox.selectedProperty());

        nuovaPasswordFieldVisible.managedProperty().bind(mostraNuovaPasswordCheckBox.selectedProperty());
        nuovaPasswordFieldVisible.visibleProperty().bind(mostraNuovaPasswordCheckBox.selectedProperty());

        confermaPasswordFieldVisible.managedProperty().bind(mostraConfermaPasswordCheckBox.selectedProperty());
        confermaPasswordFieldVisible.visibleProperty().bind(mostraConfermaPasswordCheckBox.selectedProperty());

        // Binda bidirezionalmente le proprietà 'text' dei PasswordField e dei TextField visibili.
        // Questo significa che se il testo cambia in uno, cambia automaticamente anche nell'altro, e viceversa.
        vecchiaPasswordFieldVisible.textProperty().bindBidirectional(vecchiaPasswordField.textProperty());
        nuovaPasswordFieldVisible.textProperty().bindBidirectional(nuovaPasswordField.textProperty());
        confermaPasswordFieldVisible.textProperty().bindBidirectional(confermaPasswordField.textProperty());

        // Imposta l'azione da eseguire quando viene premuto il tasto Invio su uno qualsiasi dei campi password.
        vecchiaPasswordField.setOnAction(this::handleInvio);
        nuovaPasswordField.setOnAction(this::handleInvio);
        confermaPasswordField.setOnAction(this::handleInvio);
        vecchiaPasswordFieldVisible.setOnAction(this::handleInvio);
        nuovaPasswordFieldVisible.setOnAction(this::handleInvio);
        confermaPasswordFieldVisible.setOnAction(this::handleInvio);

        setupFocusTraversal(); // Chiama il metodo per configurare la navigazione del focus.
    }

    // Metodo FXML chiamato quando viene premuto Invio in un campo password.
    @FXML
    private void handleInvio(ActionEvent event) {
        salvaNuovaPassword((MouseEvent) null); // Chiama il metodo per salvare la password, passando null per l'evento MouseEvent.
    }


    // Metodo FXML chiamato quando viene cliccato il bottone per salvare la nuova password.
    @FXML
    private void salvaNuovaPassword(MouseEvent event) {
        System.out.println("Cliccato"); // Stampa un messaggio di debug.
        // Controlla se l'ID utente corrente è nullo o vuoto.
        if (utenteCorrenteId == null || utenteCorrenteId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "ID utente non valido."); // Mostra un messaggio di errore.
            return; // Esce dal metodo.
        }

        // Recupera il testo dai campi password.
        String vecchiaPassword = vecchiaPasswordField.getText();
        String nuovaPassword = nuovaPasswordField.getText();
        String confermaPassword = confermaPasswordField.getText();

        // Controlla se uno qualsiasi dei campi password è vuoto.
        if (vecchiaPassword.isEmpty() || nuovaPassword.isEmpty() || confermaPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Tutti i campi password devono essere compilati."); // Mostra un messaggio di errore.
            return; // Esce dal metodo.
        }

        // Controlla se la nuova password e la conferma non corrispondono.
        if (!nuovaPassword.equals(confermaPassword)) {
            showAlert(Alert.AlertType.ERROR, "Errore", "La nuova password e la conferma non corrispondono."); // Mostra un messaggio di errore.
            return; // Esce dal metodo.
        }

        // Controlla i requisiti di robustezza della nuova password (lunghezza minima, almeno una maiuscola e un numero).
        if (nuovaPassword.length() < 8 || !nuovaPassword.matches("^(?=.*[A-Z])(?=.*\\d).*$")) {
            showAlert(Alert.AlertType.WARNING, "Avviso", "La nuova password deve essere di almeno 8 caratteri e contenere almeno una lettera maiuscola e un numero."); // Mostra un messaggio di avviso.
            return; // Esce dal metodo.
        }

        // Verifica che la vecchia password inserita sia corretta chiamando il metodo `verificaVecchiaPassword` di UtenteModel.
        if (!utenteModel.verificaVecchiaPassword(utenteCorrenteId, vecchiaPassword)) {
            showAlert(Alert.AlertType.ERROR, "Errore", "La vecchia password inserita non è corretta."); // Mostra un messaggio di errore.
            return; // Esce dal metodo.
        }

        // Controlla se la nuova password è uguale alla vecchia.
        if(vecchiaPassword.equals(nuovaPassword)) {
            showAlert(Alert.AlertType.ERROR,"Errore","La nuova password corrisponde alla vecchia"); // Mostra un messaggio di errore.
            return; // Esce dal metodo.
        }

        // Tenta di aggiornare la password nel database.
        try {
            if (utenteModel.aggiornaPassword(utenteCorrenteId, nuovaPassword)) { // Se l'aggiornamento ha successo.
                showAlert(Alert.AlertType.INFORMATION, "Successo", "Password correttamente modificata!"); // Mostra un messaggio di successo.
                // Chiude la finestra corrente.
                Stage stage = (Stage) vecchiaPasswordField.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile aggiornare la password."); // Mostra un messaggio di errore generico.
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Errore del Database", "Si è verificato un errore durante l'aggiornamento della password."); // Mostra un messaggio di errore del database.
            e.printStackTrace(); // Stampa la traccia dello stack dell'eccezione SQL per debug.
        }
    }

    // Metodo per configurare la navigazione del focus tra i campi di testo usando i tasti freccia UP/DOWN.
    public void setupFocusTraversal() {
        // Crea una lista ordinata di tutti i campi di testo che partecipano alla navigazione del focus.
        List<TextField> textFields = Arrays.asList(vecchiaPasswordField, nuovaPasswordField, confermaPasswordField);

        // Itera su ogni campo di testo nella lista.
        for (int i = 0; i < textFields.size(); i++) {
            final int index = i; // Dichiarazione di una variabile finale per l'indice, necessaria per l'uso nella lambda expression.
            TextField tf = textFields.get(i); // Ottiene il TextField corrente.
            tf.setFocusTraversable(true); // Assicura che il campo possa ricevere il focus tramite la navigazione della tastiera.
            tf.setOnKeyPressed(event -> { // Imposta un gestore di eventi per la pressione dei tasti.
                switch (event.getCode()) { // Valuta il codice del tasto premuto.
                    case DOWN: // Se il tasto premuto è FRECCIA GIÙ.
                        if (index + 1 < textFields.size()) { // Controlla se esiste un campo successivo nella lista.
                            textFields.get(index + 1).requestFocus(); // Sposta il focus al campo successivo.
                        }
                        break; // Esce dallo switch.
                    case UP: // Se il tasto premuto è FRECCIA SU.
                        if (index - 1 >= 0) { // Controlla se esiste un campo precedente nella lista.
                            textFields.get(index - 1).requestFocus(); // Sposta il focus al campo precedente.
                        }
                        break; // Esce dallo switch.
                }
            });
        }
    }


    // Metodo privato per mostrare un Alert (finestra di messaggio) all'utente.
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType); // Crea un nuovo Alert del tipo specificato (es. ERROR, INFORMATION).
        alert.setTitle(title); // Imposta il titolo della finestra di alert.
        alert.setHeaderText(null); // Rimuove l'header text per un layout più pulito.
        alert.setContentText(message); // Imposta il messaggio principale dell'alert.
        // Tenta di caricare un foglio di stile CSS personalizzato per l'alert.
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) { // Se il file CSS è stato trovato.
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // Aggiunge il CSS al pannello del dialogo dell'alert.
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica una classe di stile base al pannello del dialogo.
            // Aggiunge classi di stile specifiche in base al tipo di Alert per una personalizzazione visiva più dettagliata.
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Stampa un errore se il CSS non viene trovato.
        }

        alert.showAndWait(); // Mostra l'alert e attende che l'utente lo chiuda prima di procedere.
    }
}