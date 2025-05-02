package com.matteotocci.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class HomePage {
    @FXML
    private Button BottoneAlimenti;

    private String loggedInUserId; // Variabile per memorizzare l'ID dell'utente loggato

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
        System.out.println("[DEBUG - HomePage] ID utente ricevuto: " + this.loggedInUserId);
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
    private void AccessoProfilo(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();

            // Ottieni il controller PaginaProfilo
            PaginaProfilo profileController = fxmlLoader.getController();

            // **Usa l'ID utente memorizzato invece della stringa statica**
            if (loggedInUserId != null) {
                System.out.println("[DEBUG - HomePage] ID utente da passare a Profilo: " + loggedInUserId);
                profileController.setUtenteCorrenteId(loggedInUserId);
            } else {
                System.out.println("[DEBUG - HomePage] ID utente non ancora disponibile per il Profilo.");
                // Potresti voler gestire questo caso mostrando un messaggio o disabilitando l'accesso al profilo
            }

            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}