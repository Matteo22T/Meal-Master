package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.matteotocci.app.model.SQLiteConnessione;
import javafx.stage.Stage;

public class AggiungiAlimentoController {


    @FXML
    private TextField nomeField, brandField, kcalField, proteineField, carboidratiField, grassiField,
            grassiSatField, saleField, fibreField, zuccheriField,
            immaginePiccolaField, immagineGrandeField;

    public void setupFocusTraversal() {
        List<TextField> textFields = Arrays.asList(nomeField, brandField, kcalField, proteineField, carboidratiField, grassiField,
                grassiSatField, saleField, fibreField, zuccheriField, immaginePiccolaField, immagineGrandeField);

        for (int i = 0; i < textFields.size(); i++) {
            final int index = i;
            TextField tf = textFields.get(i);
            tf.setFocusTraversable(true);
            tf.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case DOWN:
                        if (index + 1 < textFields.size()) {
                            textFields.get(index + 1).requestFocus();
                        }
                        break;
                    case UP:
                        if (index - 1 >= 0) {
                            textFields.get(index - 1).requestFocus();
                        }
                        break;
                }
            });
        }
    }

    public void initialize() {
        setupFocusTraversal();
    }




    private Alimenti alimentiController;
    public void setAlimentiController(Alimenti controller) {
        this.alimentiController = controller;
    }

    @FXML
    private void handleSalva(ActionEvent event) {
        // Validazione: controlla che tutti i campi obbligatori siano pieni
        if (isEmpty(nomeField) || isEmpty(brandField) || isEmpty(kcalField) || isEmpty(proteineField) ||
                isEmpty(carboidratiField) || isEmpty(grassiField) || isEmpty(grassiSatField) ||
                isEmpty(saleField) || isEmpty(fibreField) || isEmpty(zuccheriField)) {

            showAlert(Alert.AlertType.ERROR, "Errore", "Tutti i campi obbligatori devono essere compilati.");
            return;
        }

        try {
            // Esegui il parsing dei valori numerici
            double kcal = Double.parseDouble(kcalField.getText());
            double proteine = Double.parseDouble(proteineField.getText());
            double carboidrati = Double.parseDouble(carboidratiField.getText());
            double grassi = Double.parseDouble(grassiField.getText());
            double grassiSat = Double.parseDouble(grassiSatField.getText());
            double sale = Double.parseDouble(saleField.getText());
            double fibre = Double.parseDouble(fibreField.getText());
            double zuccheri = Double.parseDouble(zuccheriField.getText());

            // Connessione al DB e inserimento
            Connection conn = SQLiteConnessione.connector();

            Integer userId = Session.getUserId();
            System.out.println("User ID attuale: " + Session.getUserId());

            String query = "INSERT INTO foods (nome, brand, kcal, proteine, carboidrati, grassi, grassiSaturi, sale, fibre, zuccheri, immaginePiccola, immagineGrande, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, nomeField.getText());
            stmt.setString(2, brandField.getText());
            stmt.setDouble(3, kcal);
            stmt.setDouble(4, proteine);
            stmt.setDouble(5, carboidrati);
            stmt.setDouble(6, grassi);
            stmt.setDouble(7, grassiSat);
            stmt.setDouble(8, sale);
            stmt.setDouble(9, fibre);
            stmt.setDouble(10, zuccheri);
            stmt.setString(11, immaginePiccolaField.getText().isEmpty() ? null : immaginePiccolaField.getText());
            stmt.setString(12, immagineGrandeField.getText().isEmpty() ? null : immagineGrandeField.getText());
            stmt.setInt(13, userId);  // Aggiungi l'ID dell'utente


            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Successo", "Alimento aggiunto con successo!");
            if (alimentiController != null) {
                System.out.println("filtro: "+alimentiController.getFiltro());
                alimentiController.resetRicerca();
                alimentiController.cercaAlimenti(alimentiController.getFiltro(),false);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Assicurati che tutti i valori nutrizionali siano numeri validi.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Errore", "Errore durante il salvataggio nel database: " + e.getMessage());
        }
    }

    private boolean isEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
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

    private void pulisciCampi() {
        nomeField.clear();
        brandField.clear();
        kcalField.clear();
        proteineField.clear();
        carboidratiField.clear();
        grassiField.clear();
        grassiSatField.clear();
        saleField.clear();
        fibreField.clear();
        zuccheriField.clear();
        immaginePiccolaField.clear();
        immagineGrandeField.clear();
    }

}