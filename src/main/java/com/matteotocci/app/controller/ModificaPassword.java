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
    private UtenteModel utenteModel;



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
    }

    @FXML
    private void handleInvio(ActionEvent event) {
        salvaNuovaPassword((MouseEvent) null); // Indica che l'evento non è un MouseEvent
    }

    @FXML
    private void AccessoProfilo(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();
            Stage profileStage = (Stage) ImmagineOmino.getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();
            ((Stage) BottoneAlimenti.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @FXML
    private void mostraNascondiVecchiaPassword(ActionEvent event) {
        // La visibilità è gestita dai binding nell'initialize
    }

    @FXML
    private void mostraNascondiNuovaPassword(ActionEvent event) {
        // La visibilità è gestita dai binding nell'initialize
    }

    @FXML
    private void mostraNascondiConfermaPassword(ActionEvent event) {
        // La visibilità è gestita dai binding nell'initialize
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}