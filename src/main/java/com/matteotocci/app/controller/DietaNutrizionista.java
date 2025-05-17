package com.matteotocci.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

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
    private void vaiAiClienti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml"));
            Parent homePageRoot = fxmlLoader.load();
            HomePageNutrizionista homePageController = fxmlLoader.getController();
            homePageController.setLoggedInUserId(loggedInUserId);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homePageRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void openProfiloNutrizionista(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ProfiloNutrizionista.fxml"));
            Parent profileRoot = fxmlLoader.load();

            // Ottieni il controller della pagina del profilo appena caricata
            ProfiloNutrizionista profileController = fxmlLoader.getController();
            profileController.setLoggedInUserId(loggedInUserId); // Passa l'ID utente

            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void initialize() {
        // Non chiamare setNomeUtenteLabel() qui
    }

    private void setNomeUtenteLabel() {
        if (ruoloUtenteLabelDieta != null && nomeUtenteLabelDieta != null && loggedInUserId != null) {
            String nomeUtenteCompleto = getNomeUtenteDalDatabase(loggedInUserId);
            nomeUtenteLabelDieta.setText(
                    (nomeUtenteCompleto != null && !nomeUtenteCompleto.isEmpty()) ? nomeUtenteCompleto : "Nome e Cognome"
            );
            // La Label ruoloUtenteLabelDieta ora conterrà "Nutrizionista" direttamente dall'FXML
        } else {
            System.err.println("Errore: ruoloUtenteLabelDieta o nomeUtenteLabelDieta o loggedInUserId sono null.");
            if (ruoloUtenteLabelDieta == null) System.err.println("ruoloUtenteLabelDieta è null");
            if (nomeUtenteLabelDieta == null) System.err.println("nomeUtenteLabelDieta è null");
            if (loggedInUserId == null) System.err.println("loggedInUserId è null");
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtenteCompleto = null;
        String url = "jdbc:sqlite:database.db";
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nomeUtenteCompleto = rs.getString("Nome") + " " + rs.getString("Cognome");
            }
        } catch (SQLException e) {
            System.err.println("Errore DB (nome utente): " + e.getMessage());
        }
        return nomeUtenteCompleto;
    }
    @FXML
    private void vaiAggiungiNuovaDieta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/NuovaDieta.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nuova Dieta");
            stage.setScene(new Scene(root));

            // Imposta la modalità della finestra per farla apparire sopra quella corrente
            stage.initModality(Modality.APPLICATION_MODAL); // Or Modality.WINDOW_MODAL
            //Se si volesse far apparire la nuova finestra sopra quella attuale
            Window owner = ((Node) event.getSource()).getScene().getWindow();
            stage.initOwner(owner);

            stage.show();
            // Non chiudere la finestra precedente!
            // ((Stage) bottoneNuovaDieta.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            // Gestisci l'errore di caricamento della pagina
        }
    }
}