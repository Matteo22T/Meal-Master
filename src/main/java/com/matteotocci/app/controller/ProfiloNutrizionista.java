package com.matteotocci.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // Importa Initializable
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox; // Non usato, si può rimuovere se non serve
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

import com.matteotocci.app.model.Session; // Importa la classe Session
import com.matteotocci.app.model.SQLiteConnessione; // Aggiungi questo import se SQLiteConnessione.connector() è usato

public class ProfiloNutrizionista implements Initializable { // Implementa Initializable

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

    // Rimuovi loggedInUserId e nomeUtenteCompleto se non strettamente necessari al di fuori del metodo initialize
    // private String loggedInUserId;
    // private String nomeUtenteCompleto;

    // Rimuovi setLoggedInUserId, non è più necessario in questo approccio
    // public void setLoggedInUserId(String userId) {
    //     this.loggedInUserId = userId;
    //     inizializzaProfilo();
    // }

    // Rimuovi l'initialize() senza parametri, causa problemi con Initializable
    // @FXML
    // public void initialize() {
    //     ruoloUtenteLabel.setText("Nutrizionista");
    // }

    @Override // Questo è il metodo initialize corretto dell'interfaccia Initializable
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ruoloUtenteLabel.setText("Nutrizionista"); // Inizializzazione del ruolo
        inizializzaProfilo(); // Chiama il metodo per caricare i dati del profilo
    }

    private void inizializzaProfilo() {
        Integer userIdFromSession = Session.getUserId(); // Recupera l'ID utente dalla Sessione

        if (userIdFromSession != null) {
            String nome = getDatoUtenteDalDatabase("Utente", userIdFromSession, "Nome"); // Passa Integer
            String cognome = getDatoUtenteDalDatabase("Utente", userIdFromSession, "Cognome");
            String sesso = getDatoUtenteDalDatabase("Utente", userIdFromSession, "Sesso"); // Assumi che ci sia un campo Sesso
            String dataNascita = getDatoUtenteDalDatabase("Utente", userIdFromSession, "DataNascita"); // Assumi che ci sia un campo DataNascita

            String nomeCompleto = (nome != null ? nome : "") + " " + (cognome != null ? cognome : "");

            nomeUtenteSidebarLabel.setText(nomeCompleto.trim());
            benvenutoLabel.setText("Bentornato " + (nome != null ? nome : "Utente"));
            nomeTextField.setText(nome);
            cognomeTextField.setText(cognome);
            sessoTextField.setText(sesso); // Imposta il valore del sesso
            dataNascitaTextField.setText(dataNascita); // Imposta il valore della data di nascita

        } else {
            System.err.println("[ERROR - ProfiloNutrizionista] ID utente non disponibile dalla Sessione. Impossibile caricare il profilo.");
            showAlert(Alert.AlertType.ERROR, "Errore Utente", "Impossibile caricare i dati del profilo.", "L'ID utente non è disponibile. Riprovare il login.");
            // Potresti voler disabilitare i campi o reindirizzare l'utente
        }
    }

    // Modifica il tipo del parametro userId da String a Integer
    private String getDatoUtenteDalDatabase(String tabella, Integer userId, String campo) {
        String valore = null;
        // String url = "jdbc:sqlite:database.db"; // Usa SQLiteConnessione.connector()
        String query = "SELECT " + campo + " FROM " + tabella + " WHERE id = ?";

        // Utilizza SQLiteConnessione.connector() per ottenere la connessione
        try (Connection conn = SQLiteConnessione.connector(); // Assicurati che SQLiteConnessione.connector() sia accessibile e corretto
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId); // Usa setInt per un parametro Integer
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                valore = rs.getString(campo);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile recuperare il dato " + campo + ".", "Dettagli: " + e.getMessage());
        }
        return valore;
    }

    // --- Metodi di Navigazione (lasciati invariati, ma rivedi i close() dei vecchi stage) ---

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
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Home Page Nutrizionista'.", "Verificare il percorso del file FXML.");
        }
    }

    @FXML
    private void AccessoDieta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml"));
            Parent dietaRoot = fxmlLoader.load();

            Stage dietaStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottieni lo stage corrente
            dietaStage.setScene(new Scene(dietaRoot));
            dietaStage.setTitle("Diete Nutrizionista");
            dietaStage.show();
            // Rimuovi il close() se vuoi riutilizzare lo stesso stage, altrimenti chiudilo solo dopo aver mostrato il nuovo.
            // ((Stage) BottoneDieta.getScene().getWindow()).close(); // Questo chiude lo stage corrente
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Diete Nutrizionista'.", "Verificare il percorso del file FXML.");
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
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Ricette Nutrizionista'.", "Verificare il percorso del file FXML.");
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AlimentiNutrizionista.fxml"));
            Parent alimentiRoot = fxmlLoader.load();
            Stage alimentiStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottieni lo stage corrente
            alimentiStage.setScene(new Scene(alimentiRoot));
            alimentiStage.setTitle("Alimenti Nutrizionista");
            alimentiStage.show();
            // Rimuovi il close() se vuoi riutilizzare lo stesso stage, altrimenti chiudilo solo dopo aver mostrato il nuovo.
            // ((Stage) BottoneAlimenti.getScene().getWindow()).close(); // Questo chiude lo stage corrente
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Alimenti Nutrizionista'.", "Verificare il percorso del file FXML.");
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
            showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata di modifica password.", "Verificare il percorso del file FXML.");
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
                showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile effettuare il logout.", "Verificare il percorso del file FXML.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}