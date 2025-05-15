package com.matteotocci.app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomePage {

    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Button BottoneRicette;
    @FXML
    private Label nomeUtenteLabelHomePage;
    @FXML
    private TextField ricercaClienteTextField;

    private String loggedInUserId; // Variabile per memorizzare l'ID dell'utente loggato
    private ObservableList<Cliente> listaClienti = FXCollections.observableArrayList();

    public HomePage() {
        // Costruttore predefinito richiesto da JavaFX
    }

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
        System.out.println("[DEBUG - HomePage] ID utente ricevuto: " + this.loggedInUserId);
        setNomeUtenteLabel();
        caricaClientiDelNutrizionista(); //carica i clienti all'avvio della pagina
    }

    private void setNomeUtenteLabel() {
        String nomeUtente = getNomeUtenteDalDatabase(loggedInUserId);
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            nomeUtenteLabelHomePage.setText(nomeUtente);
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Fallback text
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String url = "jdbc:sqlite:database.db"; // Assicurati che sia il percorso corretto
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
        }
        return nomeUtente;
    }

    @FXML
    private void initialize() {
        // Se non usi più la TableView, non è necessario inizializzare nulla per le colonne
    }

    private void caricaClientiDelNutrizionista() {
        listaClienti.clear(); //pulisce la lista prima di caricare nuovi dati
        String url = "jdbc:sqlite:database.db";
        // Query per selezionare i clienti che hanno come id_nutrizionista l'id dell'utente loggato
        String query = "SELECT u.Nome, u.Cognome FROM Utente u " +
                "JOIN Clienti c ON u.id = c.id_cliente " +
                "WHERE c.id_nutrizionista = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, loggedInUserId); // Usa l'id dell'utente loggato
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String nome = rs.getString("Nome") + " " + rs.getString("Cognome");
                listaClienti.add(new Cliente(nome));
            }
            // Puoi fare qualcosa con la listaClienti qui (ma senza la TableView, probabilmente la visualizzerai in un altro modo)
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento dei clienti del nutrizionista: " + e.getMessage());
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
    private void AccessoRicette(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Ricette.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();
            ((Stage) BottoneRicette.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();
            PaginaProfilo profileController = fxmlLoader.getController();

            if (loggedInUserId != null) {
                System.out.println("[DEBUG - HomePage] ID utente da passare a Profilo: " + loggedInUserId);
                profileController.setUtenteCorrenteId(loggedInUserId);
            } else {
                System.out.println("[DEBUG - HomePage] ID utente non ancora disponibile per il Profilo.");
            }

            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe Cliente (assicurati che corrisponda alla struttura dei dati che vuoi visualizzare)
    public static class Cliente {
        private String nome;
        private String azioni; //per visualizzare i bottoni

        public Cliente(String nome) {
            this.nome = nome;
            this.azioni = "Visualizza Dieta/Modifica"; //valore di default
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getAzioni() {
            return azioni;
        }

        public void setAzioni(String azioni) {
            this.azioni = azioni;
        }
    }
}
