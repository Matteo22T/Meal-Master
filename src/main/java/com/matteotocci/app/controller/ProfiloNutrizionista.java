package com.matteotocci.app.controller;

colimport com.matteotocci.app.model.Session; // Importa la classe Session!
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
import javafx.scene.layout.VBox; // Mantenuto se usato in FXML, altrimenti rimuovibile
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
    private Button BottoneRicette; // Aggiunto per coerenza con le altre pagine
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
    private Label modificaPasswordLabel; // Presumo sia una Label cliccabile
    @FXML
    private Button LogoutButton;
    @FXML
    private Label ruoloUtenteLabel;

    // Rimosso: private String loggedInUserId;
    // Rimosso: public void setLoggedInUserId(String userId) { ... }
    // Rimosso: private String nomeUtenteCompleto; (nomeUtenteCompleto è locale a inizializzaProfilo() o viene generato al volo)


    // Questo è il metodo initialize corretto per l'interfaccia Initializable
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // L'inizializzazione statica del ruolo avviene qui
        ruoloUtenteLabel.setText("Nutrizionista");
        // Chiamata per inizializzare il profilo non appena il controller è pronto
        inizializzaProfilo();
    }

    private void inizializzaProfilo() {
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID direttamente dalla Session
        if (userIdFromSession != null) {
            String nome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Nome");
            String cognome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Cognome");

            String nomeCompleto = (nome != null ? nome : "") + " " + (cognome != null ? cognome : "");
            nomeUtenteSidebarLabel.setText(nomeCompleto.trim());
            benvenutoLabel.setText("Bentornato " + (nome != null ? nome : "Utente"));
            nomeTextField.setText(nome != null ? nome : ""); // Imposta anche i TextField specifici
            cognomeTextField.setText(cognome != null ? cognome : "");

            // Se hai questi campi nel DB per il nutrizionista (e sono presenti nel FXML)
            String sesso = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Sesso"); // Esempio: assumendo colonna 'Sesso'
            String dataNascita = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "DataNascita"); // Esempio: assumendo colonna 'DataNascita'

            if (sessoTextField != null) {
                sessoTextField.setText(sesso != null ? sesso : "");
            }
            if (dataNascitaTextField != null) {
                dataNascitaTextField.setText(dataNascita != null ? dataNascita : "");
            }

        } else {
            System.out.println("[DEBUG] ID utente non valido (null) dalla Sessione in inizializzaProfilo. Non è possibile caricare i dati del profilo.");
            nomeUtenteSidebarLabel.setText("Nome e Cognome"); // Fallback
            benvenutoLabel.setText("Bentornato Utente"); // Fallback
            nomeTextField.setText("");
            cognomeTextField.setText("");
            if (sessoTextField != null) sessoTextField.setText("");
            if (dataNascitaTextField != null) dataNascitaTextField.setText("");
        }
    }

    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) {
        String valore = null;
        String url = "jdbc:sqlite:database.db"; // Assicurati che il percorso del database sia corretto
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

    // --- Metodi di Navigazione ---
    // Questi metodi ora NON passano più l'ID utente esplicitamente.
    // I controller delle pagine di destinazione dovranno recuperare l'ID dalla Session.

    @FXML
    private void vaiAiClienti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml"));
            Parent homePageRoot = fxmlLoader.load();
            // Non è necessario passare l'ID qui, HomePageNutrizionista lo prenderà dalla Session
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
            // Non è necessario passare l'ID qui, DietaNutrizionista lo prenderà dalla Session
            Stage dietaStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            dietaStage.setScene(new Scene(dietaRoot));
            dietaStage.setTitle("Diete");
            dietaStage.show();
            // Considera se chiudere la finestra precedente o solo nasconderla:
            // ((Stage) BottoneDieta.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent alimentiRoot = fxmlLoader.load();
            // Non è necessario passare l'ID qui, Alimenti lo prenderà dalla Session
            Stage alimentiStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            alimentiStage.setScene(new Scene(alimentiRoot));
            alimentiStage.show();
            // Considera se chiudere la finestra precedente o solo nasconderla:
            // ((Stage) BottoneAlimenti.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/RicetteNutrizionista.fxml"));
            Parent ricetteRoot = fxmlLoader.load();
            // Non è necessario passare l'ID qui, Ricette lo prenderà dalla Session
            Stage ricetteStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ricetteStage.setScene(new Scene(ricetteRoot));
            ricetteStage.setTitle("Ricette");
            ricetteStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostraSchermataModificaPassword(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ModificaPassword.fxml"));
            Parent modificaPasswordRoot = fxmlLoader.load();
            ModificaPassword modificaPasswordController = fxmlLoader.getController();

            // Passa l'ID utente corrente direttamente dalla Session
            Integer userId = Session.getUserId();
            if (userId != null) {
                modificaPasswordController.setUtenteCorrenteId(userId.toString());
            } else {
                System.err.println("[ERROR - ProfiloNutrizionista] ID utente non disponibile dalla Sessione per ModificaPassword.");
            }

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

    // Metodo helper per mostrare alert
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}