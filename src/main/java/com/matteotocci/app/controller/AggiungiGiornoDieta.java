package com.matteotocci.app.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;

public class AggiungiGiornoDieta {


    private String titoloPiano;
    private int numeroGiorni;
    @FXML
    private Label giornoCorrenteLabel;

    public void setTitoloPiano(String titolo) {
        this.titoloPiano = titolo;
    }

    public void setNumeroGiorni(int numero) {
        this.numeroGiorni = numero;
    }
    @FXML
    private void openAggiungiAlimentoDieta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiAlimentoDieta.fxml"));
            Parent profileRoot = fxmlLoader.load();

            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
