package com.matteotocci.app.controller;

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
import javafx.stage.Stage;
import javafx.scene.layout.GridPane; // Import aggiunto

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

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

    @FXML
    private TextField sessoTextField;

    @FXML
    private TextField dataNascitaTextField;

    @FXML
    private TextField altezzaTextField;

    @FXML
    private TextField pesoAttualeTextField;

    @FXML
    private TextField pesoIdealeTextField;

    @FXML
    private TextField dietaTextField;

    @FXML
    private TextField livelloAttivitaTextField;

    @FXML
    private TextField nutrizionistaTextField;

    @FXML
    private GridPane gridPane;

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
            System.out.println("[DEBUG] Tentativo di recupero dati per l'utente con ID: " + utenteCorrenteId);

            // Recupera il nome utente per la sidebar dalla tabella Utente
            String nomeUtenteSidebar = getDatoUtenteDalDatabase("Utente", utenteCorrenteId, "Nome");
            System.out.println("[DEBUG] Nome utente sidebar recuperato: " + nomeUtenteSidebar);
            if (nomeUtenteSidebar != null && !nomeUtenteSidebar.isEmpty()) {
                nomeUtenteSidebarLabel.setText(nomeUtenteSidebar);
                benvenutoLabel.setText("Benvenuto " + nomeUtenteSidebar);
            } else {
                nomeUtenteSidebarLabel.setText("Utente Sconosciuto");
                benvenutoLabel.setText("Benvenuto Utente");
            }

            // Recupera nome e cognome dalla tabella Utente
            String nome = getDatoUtenteDalDatabase("Utente", utenteCorrenteId, "Nome");
            String cognome = getDatoUtenteDalDatabase("Utente", utenteCorrenteId, "Cognome");
            if (nome != null) {
                nomeTextField.setText(nome);
            }
            if (cognome != null) {
                cognomeTextField.setText(cognome);
            }

            // Recupera i dati del cliente dalla tabella Clienti
            String altezza = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "altezza_cm");
            String peso = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "peso_kg");
            String livelloAttivita = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "livello_attivita");
            String dataNascita = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "data_di_nascita");
            String sesso = getDatoUtenteDalDatabase("Clienti",utenteCorrenteId,"sesso");

            // Imposta i valori nei rispettivi TextField
            if (altezzaTextField != null) {
                altezzaTextField.setText(altezza);
            }
            if (pesoAttualeTextField != null) {
                pesoAttualeTextField.setText(peso);
            }
            if (livelloAttivitaTextField != null) {
                livelloAttivitaTextField.setText(livelloAttivita);
            }
            if (dataNascitaTextField != null) {
                dataNascitaTextField.setText(dataNascita);
            }
            if (sessoTextField != null){
                sessoTextField.setText(sesso);
            }
            if (nutrizionistaTextField != null) {
                String idNutrizionista = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "id_nutrizionista");

                if (idNutrizionista != null && !idNutrizionista.isEmpty()) {
                    String nomeNutrizionista = getDatoUtenteDalDatabase("Utente", idNutrizionista, "Nome");
                    String cognomeNutrizionista = getDatoUtenteDalDatabase("Utente", idNutrizionista, "Cognome");

                    if (nomeNutrizionista != null && cognomeNutrizionista != null) {
                        nutrizionistaTextField.setText(nomeNutrizionista + " " + cognomeNutrizionista);
                    }
                }
            }

            // Imposta anche gli altri campi se hai i riferimenti FXML
            // Esempio per gli altri (decommenta e usa se necessario)
            // if (sessoTextField != null) {
            //     sessoTextField.setText(getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "sesso")); // Assumi che ci sia una colonna 'sesso'
            // }
            // if (pesoIdealeTextField != null) {
            //     pesoIdealeTextField.setText(getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "peso_ideale_kg")); // Assumi una colonna 'peso_ideale_kg'
            // }
            // if (dietaTextField != null) {
            //     dietaTextField.setText(getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "dieta")); // Assumi una colonna 'dieta'
            // }
            // if (nutrizionistaTextField != null) {
            //     nutrizionistaTextField.setText(getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "id_nutrizionista")); // Recupera l'ID del nutrizionista
            // }
        } else {
            System.out.println("[DEBUG] ID utente non valido (null). Impossibile recuperare i dati.");
        }
    }

    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) {
        String valore = null;
        String url = "jdbc:sqlite:database.db";
        String query;
        String idColumn = "id"; // Default per la tabella Utente
        if (tabella.equals("Clienti")) {
            idColumn = "id_cliente";
        }
        query = "SELECT " + campo + " FROM " + tabella + " WHERE " + idColumn + " = ?";
        System.out.println("[DEBUG] Query per " + tabella + " eseguita: " + query + " con ID: " + userId + ", Campo: " + campo);

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                valore = rs.getString(campo);
                System.out.println("[DEBUG] Valore recuperato per " + campo + " da " + tabella + ": " + valore);
            } else {
                System.out.println("[DEBUG] Nessun utente trovato con ID: " + userId + " nella tabella " + tabella + " per il campo " + campo);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage());
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

            // Ottieni il controller della pagina del profilo appena caricata
            PaginaProfilo profileController = fxmlLoader.getController();

            // Imposta l'ID dell'utente corrente nel controller
            profileController.setUtenteCorrenteId(utenteCorrenteId);

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

    @FXML
    private Button homePageButton;

    public void vaiAllaHomePage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePage.fxml"));
        Parent root = loader.load();

        // Ottieni il controller della HomePage
        HomePage homePageController = loader.getController();

        // Passa l'ID utente al controller della HomePage
        homePageController.setLoggedInUserId(this.utenteCorrenteId);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private Button LogoutButton;

    public void eseguiLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Logout");
        alert.setHeaderText("Sei sicuro di voler uscire?");
        alert.setContentText("Clicca OK per confermare o Annulla per rimanere.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // L'utente ha cliccato OK, procedi con il logout
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
        } else {
            // L'utente ha cliccato Annulla o ha chiuso la finestra, non fare nulla
        }
    }
}