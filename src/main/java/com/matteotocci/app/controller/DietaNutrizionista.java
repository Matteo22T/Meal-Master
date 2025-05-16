package com.matteotocci.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DietaNutrizionista {

    @FXML
    private Button bottoneClienti;
    @FXML
    private Label nomeUtenteLabelDieta;
    @FXML
    private Label ruoloUtenteLabelDieta;

    private String loggedInUserId;

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
        setNomeUtenteLabel(); // Chiama setNomeUtenteLabel() SOLO dopo aver ricevuto l'ID
    }

    @FXML
    private void vaiAllaPaginaClienti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml"));
            Parent clientiRoot = fxmlLoader.load();
            HomePageNutrizionista controller = fxmlLoader.getController();
            controller.setLoggedInUserId(loggedInUserId); // Imposta nuovamente l'ID utente

            Stage clientiStage = new Stage();
            clientiStage.setScene(new Scene(clientiRoot));
            clientiStage.setTitle("Clienti");
            clientiStage.show();

            if (bottoneClienti != null && bottoneClienti.getScene() != null && bottoneClienti.getScene().getWindow() != null) {
                ((Stage) bottoneClienti.getScene().getWindow()).close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Non chiamare setNomeUtenteLabel() qui
    }

    private void setNomeUtenteLabel() {
        String nomeUtente = getNomeUtenteDalDatabase(loggedInUserId);
        ruoloUtenteLabelDieta.setText(
                (nomeUtente != null && !nomeUtente.isEmpty()) ? nomeUtente : "Nome e Cognome"
        );
        nomeUtenteLabelDieta.setText("Benvenuto");
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String url = "jdbc:sqlite:database.db";
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome");
            }
        } catch (SQLException e) {
            System.err.println("Errore DB (nome utente): " + e.getMessage());
        }
        return nomeUtente;
    }
}