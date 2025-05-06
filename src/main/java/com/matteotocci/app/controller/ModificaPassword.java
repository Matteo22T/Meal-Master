package com.matteotocci.app.controller;

import com.matteotocci.app.model.UtenteModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane; // Import AnchorPane
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ModificaPassword {

    @FXML
    private AnchorPane modificaPasswordRoot; // Riferimento al layout principale
    @FXML
    private ImageView ImmagineOmino;
    @FXML
    private PasswordField vecchiaPasswordField;
    @FXML
    private PasswordField nuovaPasswordField;
    @FXML
    private PasswordField confermaPasswordField;
    @FXML
    private Button salvaPasswordButton;
    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Label nomeUtenteSidebarLabelLeft;
    @FXML
    private ImageView profileImageLeft;
    @FXML
    private Button BottonePianoAlimentareLeft;
    @FXML
    private Button BottoneAlimentiLeft;

    private String utenteCorrenteId;
    private UtenteModel utenteModel;

    public void setUtenteCorrenteId(String userId) {
        this.utenteCorrenteId = userId;
        this.utenteModel = new UtenteModel(); // Inizializza il modello quando hai l'ID
        setNomeUtenteSidebar();
    }

    private void setNomeUtenteSidebar() {
        String nomeUtente = getNomeUtenteDalDatabase(utenteCorrenteId);
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            nomeUtenteSidebarLabelLeft.setText(nomeUtente);
        } else {
            nomeUtenteSidebarLabelLeft.setText("Utente Sconosciuto");
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nome = null;
        String url = "jdbc:sqlite:database.db"; // Assicurati che sia il percorso corretto
        String query = "SELECT Nome FROM Utente WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nome = rs.getString("Nome");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
        }
        return nome;
    }

    @FXML
    private void initialize() {
        // Aggiungi un listener di eventi all'AnchorPane principale per intercettare il tasto "Invio"
        if (modificaPasswordRoot != null) {
            modificaPasswordRoot.setOnKeyPressed(this::handleEnterKeyPressed);
        } else {
            System.err.println("Errore: modificaPasswordRoot non è stato iniettato!");
        }
    }

    @FXML
    private void handleEnterKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            salvaNuovaPasswordAction(new ActionEvent()); // Simula un click sul pulsante
            event.consume(); // Impedisce ad altri elementi di rispondere allo stesso evento
        }
    }

    @FXML
    private void AccessoProfilo(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();
            Stage profileStage = (Stage) ImmagineOmino.getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
    private void salvaNuovaPassword(MouseEvent event) {
        salvaNuovaPasswordAction(new ActionEvent()); // Chiama il metodo con ActionEvent
    }

    @FXML
    private void salvaNuovaPasswordAction(ActionEvent event) {
        System.out.println("Tentativo di salvataggio password");
        if (utenteCorrenteId == null || utenteCorrenteId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "ID utente non valido.");
            return;
        }

        String vecchiaPassword = vecchiaPasswordField.getText();
        String nuovaPassword = nuovaPasswordField.getText();
        String confermaPassword = confermaPasswordField.getText();

        if (vecchiaPassword.isEmpty() || nuovaPassword.isEmpty() || confermaPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Tutti i campi password devono essere compilati.");
            return;
        }

        if (!nuovaPassword.equals(confermaPassword)) {
            showAlert(Alert.AlertType.ERROR, "Errore", "La nuova password e la conferma non corrispondono.");
            return;
        }

        if (nuovaPassword.length() < 8 || !nuovaPassword.matches("^(?=.*[A-Z])(?=.*\\d).*$")) {
            showAlert(Alert.AlertType.WARNING, "Avviso", "La nuova password deve essere di almeno 8 caratteri e contenere almeno una lettera maiuscola e un numero.");
            return;
        }

        // Verifica la vecchia password
        if (!utenteModel.verificaVecchiaPassword(utenteCorrenteId, vecchiaPassword)) {
            showAlert(Alert.AlertType.ERROR, "Errore", "La vecchia password inserita non è corretta.");
            return;
        }

        // Aggiorna la password nel database
        try {
            if (utenteModel.aggiornaPassword(utenteCorrenteId, nuovaPassword)) {
                showAlert(Alert.AlertType.INFORMATION, "Successo", "Password correttamente modificata!");
                // Chiudi la finestra
                Stage stage = (Stage) modificaPasswordRoot.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile aggiornare la password.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Errore del Database", "Si è verificato un errore durante l'aggiornamento della password.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}