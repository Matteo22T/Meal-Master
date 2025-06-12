package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProfiloNutrizionista implements Initializable {

    @FXML
    private ImageView profileImage;
    @FXML
    private Label nomeUtenteSidebarLabel;
    @FXML
    private Button ClientiButton;
    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Button BottoneDieta;
    @FXML
    private Label benvenutoLabel;
    @FXML
    private TextField nomeTextField;
    @FXML
    private TextField cognomeTextField;
    @FXML
    private TextField sessoTextField;
    @FXML
    private TextField dataNascitaTextField;
    @FXML
    private Label modificaPasswordLabel;
    @FXML
    private Button LogoutButton;
    @FXML
    private Label ruoloUtenteLabel;

    private String loggedInUserId;
    private String nomeUtenteCompleto;

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
        inizializzaProfilo();
    }

    @FXML
    public void initialize() {
        ruoloUtenteLabel.setText("Nutrizionista");
    }

    private void inizializzaProfilo() {
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID direttamente dalla Sessione

        if (userIdFromSession != null) {

            // Recupera nome e cognome dell'utente dalla tabella Utente
            String nome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Nome");
            String cognome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Cognome");

            // Imposta il nome completo nella sidebar
            String nomeCompleto = (nome != null ? nome : "") + " " + (cognome != null ? cognome : "");
            nomeUtenteSidebarLabel.setText(nomeCompleto.trim());

            // Imposta i campi TextField del nome e cognome
            nomeTextField.setText(nome != null ? nome : "");
            cognomeTextField.setText(cognome != null ? cognome : "");
        }
        else {
            System.err.println("[ERROR] ID utente non disponibile dalla Sessione. Impossibile recuperare i dati del profilo.");
            nomeUtenteSidebarLabel.setText("Utente Sconosciuto");
            nomeTextField.setText("");
            cognomeTextField.setText("");
        }
    }

    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) {
        String valore = null;
        String url = "jdbc:sqlite:database.db";
        String query = "SELECT " + campo + " FROM " + tabella + " WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                valore = rs.getString(campo);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage());
        }
        return valore;
    }

    @FXML
    private void vaiAiClienti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml"));
            Parent homePageRoot = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homePageRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void AccessoDieta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml"));
            Parent dietaRoot = fxmlLoader.load();

            Stage dietaStage = new Stage();
            dietaStage.setScene(new Scene(dietaRoot));
            dietaStage.setTitle("Diete");
            dietaStage.show();
            ((Stage) BottoneDieta.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/RicetteNutrizionista.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AlimentiNutrizionista.fxml"));
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

            Stage modificaPasswordStage = new Stage();
            modificaPasswordStage.setTitle("Modifica Password");
            modificaPasswordStage.setScene(new Scene(modificaPasswordRoot));
            modificaPasswordStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void eseguiLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Logout");
        alert.setHeaderText("Sei sicuro di voler uscire?");
        alert.setContentText("Clicca OK per confermare o Annulla per rimanere.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PrimaPagina.fxml"));
                Parent loginRoot = fxmlLoader.load();
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene loginScene = new Scene(loginRoot);
                currentStage.setScene(loginScene);
                currentStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        inizializzaProfilo();
    }
}

