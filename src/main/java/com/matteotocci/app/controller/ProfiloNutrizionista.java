package com.matteotocci.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ProfiloNutrizionista {

    @FXML
    private ImageView profileImage;
    @FXML
    private Label nomeUtenteSidebarLabel;
    @FXML
    private Button homePageButton;
    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Label benvenutoLabel;
    @FXML
    private TextField nomeTextField;
    @FXML
    private TextField cognomeTextField;
    @FXML
    private TextField sessoTextField;
    @FXML
    private TextField dataNascitaTextField;
    @FXML
    private Label modificaPasswordLabel;
    @FXML
    private Button LogoutButton;

    private String loggedInUserId;

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
        // Inizializza i dati del profilo quando si riceve l'ID utente
        inizializzaProfilo();
    }

    @FXML
    public void initialize() {
        // Inizializza i componenti dell'interfaccia utente
        // Questo metodo viene chiamato automaticamente da FXMLLoader
    }

    private void inizializzaProfilo() {
        // Carica i dati del profilo dal database e popola i campi
        // Usa il loggedInUserId per recuperare le informazioni dell'utente
        String nome = "Nome Di Esempio";  // Sostituisci con la query al database
        String cognome = "Cognome Di Esempio"; // Sostituisci
        String sesso = "Sesso Di Esempio";     // Sostituisci
        String dataNascita = "01/01/2000"; // Sostituisci

        nomeUtenteSidebarLabel.setText(nome + " " + cognome);
        benvenutoLabel.setText("Bentornato " + nome);
        nomeTextField.setText(nome);
        cognomeTextField.setText(cognome);
        sessoTextField.setText(sesso);
        dataNascitaTextField.setText(dataNascita);
    }

    @FXML
    private void vaiAiClienti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml"));
            Parent homePageRoot = fxmlLoader.load();
            HomePageNutrizionista homePageController = fxmlLoader.getController();

            // Passa l'ID utente alla HomePageNutrizionista
            homePageController.setLoggedInUserId(loggedInUserId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homePageRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        // accesso alla schermata alimenti
    }

    @FXML
    private void mostraSchermataModificaPassword(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ModificaPassword.fxml"));
            Parent modificaPasswordRoot = fxmlLoader.load();

            // **Ottieni il controller di ModificaPassword**
            ModificaPassword modificaPasswordController = fxmlLoader.getController();

            Stage modificaPasswordStage = new Stage();
            modificaPasswordStage.setTitle("Modifica Password");
            modificaPasswordStage.setScene(new Scene(modificaPasswordRoot));
            modificaPasswordStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void eseguiLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Logout");
        alert.setHeaderText("Sei sicuro di voler uscire?");
        alert.setContentText("Clicca OK per confermare o Annulla per rimanere.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // L'utente ha cliccato OK, procedi con il logout
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PrimaPagina.fxml"));
                Parent loginRoot = fxmlLoader.load();
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene loginScene = new Scene(loginRoot);
                currentStage.setScene(loginScene);
                currentStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // L'utente ha cliccato Annulla o ha chiuso la finestra, non fare nulla
        }
    }
}

