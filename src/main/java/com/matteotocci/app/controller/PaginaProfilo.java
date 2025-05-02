package com.matteotocci.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class PaginaProfilo implements Initializable {

    @FXML
    private ImageView profileImage;

    @FXML
    private Label nomeUtenteSidebarLabel;

    @FXML
    private Label benvenutoLabel;

    @FXML
    private ImageView ImmagineOmino;

    @FXML
    private TextField nomeTextField;

    @FXML
    private TextField cognomeTextField;

    private String utenteCorrenteId; // L'ID utente verrà impostato esternamente

    public void setUtenteCorrenteId(String userId) {
        System.out.println("[DEBUG] setUtenteCorrenteId chiamato con ID: " + userId);
        this.utenteCorrenteId = userId;
        // Ora che abbiamo l'ID, possiamo inizializzare i dati
        inizializzaDatiUtente();
    }

    private void inizializzaDatiUtente() {
        System.out.println("[DEBUG] inizializzaDatiUtente chiamato con ID: " + utenteCorrenteId);
        if (utenteCorrenteId != null) {
            // Recupera il nome utente per la sidebar
            String nomeUtenteSidebar = getDatoUtenteDalDatabase(utenteCorrenteId, "Nome");
            System.out.println("[DEBUG] Nome utente sidebar recuperato: " + nomeUtenteSidebar);
            if (nomeUtenteSidebar != null && !nomeUtenteSidebar.isEmpty()) {
                nomeUtenteSidebarLabel.setText(nomeUtenteSidebar);
                benvenutoLabel.setText("Benvenuto " + nomeUtenteSidebar);
            } else {
                nomeUtenteSidebarLabel.setText("Utente Sconosciuto");
                benvenutoLabel.setText("Benvenuto Utente");
            }

            // Recupera nome e cognome per i campi non modificabili
            String nome = getDatoUtenteDalDatabase(utenteCorrenteId, "Nome");
            System.out.println("[DEBUG] Nome recuperato: " + nome);
            String cognome = getDatoUtenteDalDatabase(utenteCorrenteId, "Cognome");
            System.out.println("[DEBUG] Cognome recuperato: " + cognome);

            if (nome != null) {
                nomeTextField.setText(nome);
            }
            if (cognome != null) {
                cognomeTextField.setText(cognome);
            }
        }
    }

    private String getDatoUtenteDalDatabase(String userId, String campo) {
        String valore = null;
        String url = "jdbc:sqlite:database.db";
        String query = "SELECT " + campo + " FROM Utente WHERE id = ?";
        System.out.println("[DEBUG] Query eseguita: " + query + " con ID: " + userId + ", Campo: " + campo);

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                valore = rs.getString(campo);
                System.out.println("[DEBUG] Valore recuperato per " + campo + ": " + valore);
            } else {
                System.out.println("[DEBUG] Nessun utente trovato con ID: " + userId + " per il campo " + campo);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dal database: " + e.getMessage());
        }
        return valore;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // L'inizializzazione dei dati ora avviene dopo aver ricevuto l'ID
        // tramite il metodo setUtenteCorrenteId
        System.out.println("[DEBUG] initialize chiamato.");
    }

    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
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

    @FXML
    private void mostraSchermataModificaPassword(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ModificaPassword.fxml"));
            Parent modificaPasswordRoot = fxmlLoader.load();

            // **Ottieni il controller di ModificaPassword**
            ModificaPassword modificaPasswordController = fxmlLoader.getController();

            // **Imposta l'ID utente nel controller di ModificaPassword**
            modificaPasswordController.setUtenteCorrenteId(utenteCorrenteId);

            Stage modificaPasswordStage = new Stage();
            modificaPasswordStage.setTitle("Modifica Password");
            modificaPasswordStage.setScene(new Scene(modificaPasswordRoot));
            modificaPasswordStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}