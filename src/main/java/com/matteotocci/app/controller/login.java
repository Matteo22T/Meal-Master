package com.matteotocci.app.controller;


import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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

}