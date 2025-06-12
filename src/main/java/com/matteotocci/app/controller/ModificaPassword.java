package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
import com.matteotocci.app.model.UtenteModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class ModificaPassword implements Initializable {

    @FXML
    private ImageView ImmagineOmino;
    @FXML
    private PasswordField vecchiaPasswordField;
    @FXML
    private PasswordField nuovaPasswordField;
    @FXML
    private PasswordField confermaPasswordField;
    @FXML
    private Button salvaPasswordButton;
    @FXML
    private Button BottoneAlimenti;
    @FXML
    private CheckBox mostraVecchiaPasswordCheckBox;
    @FXML
    private CheckBox mostraNuovaPasswordCheckBox;
    @FXML
    private CheckBox mostraConfermaPasswordCheckBox;
    @FXML
    private TextField vecchiaPasswordFieldVisible;
    @FXML
    private TextField nuovaPasswordFieldVisible;
    @FXML
    private TextField confermaPasswordFieldVisible;
    @FXML
    private Label nomeUtenteSidebarLabelLeft;
    @FXML
    private Button BottonePianoAlimentareLeft;

    private String utenteCorrenteId= Session.getUserId().toString();
    private UtenteModel utenteModel=new UtenteModel();



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inizializza la visibilità dei TextField per la password visibile
        vecchiaPasswordFieldVisible.managedProperty().bind(mostraVecchiaPasswordCheckBox.selectedProperty());
        vecchiaPasswordFieldVisible.visibleProperty().bind(mostraVecchiaPasswordCheckBox.selectedProperty());

        nuovaPasswordFieldVisible.managedProperty().bind(mostraNuovaPasswordCheckBox.selectedProperty());
        nuovaPasswordFieldVisible.visibleProperty().bind(mostraNuovaPasswordCheckBox.selectedProperty());

        confermaPasswordFieldVisible.managedProperty().bind(mostraConfermaPasswordCheckBox.selectedProperty());
        confermaPasswordFieldVisible.visibleProperty().bind(mostraConfermaPasswordCheckBox.selectedProperty());

        // Binda il testo tra PasswordField e TextField
        vecchiaPasswordFieldVisible.textProperty().bindBidirectional(vecchiaPasswordField.textProperty());
        nuovaPasswordFieldVisible.textProperty().bindBidirectional(nuovaPasswordField.textProperty());
        confermaPasswordFieldVisible.textProperty().bindBidirectional(confermaPasswordField.textProperty());

        // Imposta l'azione per il tasto Invio sui campi password
        vecchiaPasswordField.setOnAction(this::handleInvio);
        nuovaPasswordField.setOnAction(this::handleInvio);
        confermaPasswordField.setOnAction(this::handleInvio);
        vecchiaPasswordFieldVisible.setOnAction(this::handleInvio);
        nuovaPasswordFieldVisible.setOnAction(this::handleInvio);
        confermaPasswordFieldVisible.setOnAction(this::handleInvio);

        setupFocusTraversal();
    }

    @FXML
    private void handleInvio(ActionEvent event) {
        salvaNuovaPassword((MouseEvent) null); // Indica che l'evento non è un MouseEvent
    }



    @FXML
    private void salvaNuovaPassword(MouseEvent event) {
        System.out.println("Cliccato");
        if (utenteCorrenteId == null || utenteCorrenteId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "ID utente non valido.");
            return;
        }

        String vecchiaPassword = vecchiaPasswordField.getText();
        String nuovaPassword = nuovaPasswordField.getText();
        String confermaPassword = confermaPasswordField.getText();

        if (vecchiaPassword.isEmpty() || nuovaPassword.isEmpty() || confermaPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Tutti i campi password devono essere compilati.");
            return;
        }

        if (!nuovaPassword.equals(confermaPassword)) {
            showAlert(Alert.AlertType.ERROR, "Errore", "La nuova password e la conferma non corrispondono.");
            return;
        }

        if (nuovaPassword.length() < 8 || !nuovaPassword.matches("^(?=.*[A-Z])(?=.*\\d).*$")) {
            showAlert(Alert.AlertType.WARNING, "Avviso", "La nuova password deve essere di almeno 8 caratteri e contenere almeno una lettera maiuscola e un numero.");
            return;
        }

        // Verifica la vecchia password
        if (!utenteModel.verificaVecchiaPassword(utenteCorrenteId, vecchiaPassword)) {
            showAlert(Alert.AlertType.ERROR, "Errore", "La vecchia password inserita non è corretta.");
            return;
        }

        if(vecchiaPassword.equals(nuovaPassword)) {
            showAlert(Alert.AlertType.ERROR,"Errore","La nuova password corrisponde alla vecchia");
            return;
        }

        // Aggiorna la password nel database
        try {
            if (utenteModel.aggiornaPassword(utenteCorrenteId, nuovaPassword)) {
                showAlert(Alert.AlertType.INFORMATION, "Successo", "Password correttamente modificata!");
                // Chiudi la finestra
                Stage stage = (Stage) vecchiaPasswordField.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile aggiornare la password.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Errore del Database", "Si è verificato un errore durante l'aggiornamento della password.");
            e.printStackTrace();
        }
    }

    public void setupFocusTraversal() {
        // Crea una lista ordinata di tutti i campi di testo
        List<TextField> textFields = Arrays.asList(vecchiaPasswordField, nuovaPasswordField, confermaPasswordField);

        // Itera su ogni campo di testo per impostare il listener per la pressione dei tasti
        for (int i = 0; i < textFields.size(); i++) {
            final int index = i; // Rende l'indice effettivo finale per l'uso nella lambda expression
            TextField tf = textFields.get(i);
            tf.setFocusTraversable(true); // Assicura che il campo possa ricevere il focus
            tf.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case DOWN: // Se viene premuta la freccia giù
                        if (index + 1 < textFields.size()) { // Controlla se c'è un campo successivo
                            textFields.get(index + 1).requestFocus(); // Sposta il focus al campo successivo
                        }
                        break;
                    case UP: // Se viene premuta la freccia su
                        if (index - 1 >= 0) { // Controlla se c'è un campo precedente
                            textFields.get(index - 1).requestFocus(); // Sposta il focus al campo precedente
                        }
                        break;
                }
            });
        }
    }




    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Apply the base style class
            // Add specific style class based on AlertType for custom styling
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Corrected error message
        }

        alert.showAndWait();
    }
}