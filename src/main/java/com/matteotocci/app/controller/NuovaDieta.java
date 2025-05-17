package com.matteotocci.app.controller;

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