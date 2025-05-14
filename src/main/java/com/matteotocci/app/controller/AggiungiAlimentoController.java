package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.matteotocci.app.model.SQLiteConnessione;
import javafx.stage.Stage;

public class AggiungiAlimentoController {


    @FXML
    private TextField nomeField, brandField, kcalField, proteineField, carboidratiField, grassiField,
            grassiSatField, saleField, fibreField, zuccheriField,
            immaginePiccolaField, immagineGrandeField;

    @FXML
    private void handleSalva(ActionEvent event) {
        // Validazione: controlla che tutti i campi obbligatori siano pieni
        if (isEmpty(nomeField) || isEmpty(brandField) || isEmpty(kcalField) || isEmpty(proteineField) ||
                isEmpty(carboidratiField) || isEmpty(grassiField) || isEmpty(grassiSatField) ||
                isEmpty(saleField) || isEmpty(fibreField) || isEmpty(zuccheriField)) {

            mostraErrore("Tutti i campi obbligatori devono essere compilati.");
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

            mostraInfo("Alimento aggiunto con successo!");
            // (Facoltativo) pulisci i campi dopo il salvataggio
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            mostraErrore("Assicurati che tutti i valori nutrizionali siano numeri validi.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            mostraErrore("Errore durante il salvataggio nel database: " + e.getMessage());
        }
    }

    private boolean isEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private void mostraInfo(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
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
