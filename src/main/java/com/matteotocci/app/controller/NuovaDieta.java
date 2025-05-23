package com.matteotocci.app.controller;

import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class NuovaDieta {

    @FXML
    private TextField titoloPianoTextField;

    @FXML
    private Spinner<Integer> numeroGiorniSpinner;

    @FXML
    private Button avantiButton;

    @FXML
    private Label messaggioLabel;

    @FXML
    private void switchToAggiungiAlimenti(ActionEvent event) {
        String titoloPiano = titoloPianoTextField.getText();
        Integer numeroGiorni = numeroGiorniSpinner.getValue(); // Ottieni il valore come Integer

        if (titoloPiano == null || titoloPiano.trim().isEmpty() || numeroGiorni == null) { // Controlla anche se numeroGiorni è null
            messaggioLabel.setText("Inserisci un titolo e un numero di giorni validi.");
            return;
        }
        System.out.println("---- Inizio salvaPianoButtonAction ----");
        Connection conn = null;
        PreparedStatement psDieta = null;
        PreparedStatement psGiorno= null;


        try {
            conn = SQLiteConnessione.connector();
            System.out.println("Connessione stabilita: " + (conn != null));

            // DEBUG: Lista tabelle esistenti
            conn.setAutoCommit(false);

            // DEBUG: Inserimento Dieta
            String insertDietaSql = "INSERT INTO Diete (id_cliente,nome_dieta, data_inizio, data_fine,id_nutrizionista) VALUES (NULL,?, NULL, NULL,?)";
            psDieta = conn.prepareStatement(insertDietaSql, Statement.RETURN_GENERATED_KEYS); //si ferma qua
            psDieta.setString(1, titoloPianoTextField.getText());
            psDieta.setInt(2, Session.getUserId());
            psDieta.executeUpdate();


            ResultSet generatedKeys = psDieta.getGeneratedKeys();
            int idDieta;
            if (generatedKeys.next()) {
                idDieta = generatedKeys.getInt(1);
                System.out.println("ID Dieta generato: " + idDieta);
            } else {
                throw new SQLException("Errore nella creazione della dieta: nessun ID ottenuto.");
            }


            String insertGiornoSql = "INSERT INTO Giorno_dieta (id_dieta, calorie_giorno, proteine_giorno, carboidrati_giorno, grassi_giorno) VALUES (?,0,0,0,0)";

            psGiorno = conn.prepareStatement(insertGiornoSql);
            for (int i = 1; i <= numeroGiorniSpinner.getValue(); i++) {
                psGiorno.setInt(1, idDieta);
                psGiorno.addBatch();
            }

            psGiorno.executeBatch();
            conn.commit();






            System.out.println("Salvataggio piano dieta completato con successo.");
        } catch (SQLException e) {
            System.err.println("Errore durante il salvataggio del piano dieta: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (psDieta != null) psDieta.close();
                if (conn != null) conn.close();
                System.out.println("Risorse chiuse correttamente.");
            } catch (SQLException ex) {
                System.err.println("Errore chiusura risorse: " + ex.getMessage());
                ex.printStackTrace();
            }
        }


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiGiornoDieta.fxml"));
            Parent aggiungiAlimentiRoot = loader.load();

            AggiungiGiornoDieta aggiungiAlimentiController = loader.getController();
            aggiungiAlimentiController.setTitoloPiano(titoloPiano);
            aggiungiAlimentiController.setNumeroGiorni(numeroGiorni);


            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(aggiungiAlimentiRoot));
            stage.setTitle("Aggiungi Alimenti al Piano");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            messaggioLabel.setText("Errore nel caricamento della schermata successiva.");
        }
    }
}
