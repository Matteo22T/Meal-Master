package com.matteotocci.app.controller;


import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import java.io.IOException;

public class login {
    @FXML
    private VBox loginBox;

    @FXML
    private VBox registerBox;

    @FXML
    private Button btnAccedi;

    @FXML
    private Button btnRegistrati;

    @FXML
    private void switchToLogin() {
        if (!loginBox.isVisible()) {
            fade(registerBox, false);
            fade(loginBox, true);
            highlightButton(btnAccedi, btnRegistrati);
        }
    }

    @FXML
    private void switchToRegister() {
        if (!registerBox.isVisible()) {
            fade(loginBox, false);
            fade(registerBox, true);
            highlightButton(btnRegistrati, btnAccedi);
        }
    }

    private void fade(VBox box, boolean show) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), box);
        if (show) {
            box.setVisible(true);
            box.setOpacity(0);
            ft.setFromValue(0);
            ft.setToValue(1);
        } else {
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(e -> box.setVisible(false));
        }
        ft.play();
    }
    private void highlightButton(Button active, Button inactive) {
        // Rimuove classe da entrambi
        active.getStyleClass().remove("bottoneSpento");
        inactive.getStyleClass().remove("bottoneAttivo");

        active.getStyleClass().add("bottoneAttivo");

        inactive.getStyleClass().add("bottoneSpento");
    }

    @FXML
    private Button BottoneAccedi;
    @FXML
    private Button BottoneRegistrati;


    @FXML
    private void Registrato(ActionEvent event) {
        try {
            // Carica il file login.fxml
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PrimaPagina.fxml"));
            Parent loginRoot = fxmlLoader.load();

            // Crea un nuovo stage (nuova finestra)
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();

            // SE vuoi chiudere la finestra attuale, togli il commento qui sotto:
            ((Stage) BottoneRegistrati.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void AccessoHomePage(ActionEvent event) {
        try {
            // Carica il file login.fxml
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePage.fxml"));
            Parent loginRoot = fxmlLoader.load();

            // Crea un nuovo stage (nuova finestra)
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();

            // SE vuoi chiudere la finestra attuale, togli il commento qui sotto:
            ((Stage) BottoneAccedi.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}