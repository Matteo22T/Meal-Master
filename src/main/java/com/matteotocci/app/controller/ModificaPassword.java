package com.matteotocci.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ModificaPassword{
    @FXML
    private ImageView ImmagineOmino;

    @FXML
    private void AccessoProfilo(ActionEvent event) {
        try {
            // Carica di nuovo il file FXML della stessa pagina
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();

            // Ottieni la finestra corrente (la finestra in cui si trova l'immagine)
            Stage profileStage = (Stage) ImmagineOmino.getScene().getWindow();

            // Ricarica la scena con la nuova root
            profileStage.setScene(new Scene(profileRoot));  // Ricarica la scena
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private Button BottoneAlimenti;
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

}

