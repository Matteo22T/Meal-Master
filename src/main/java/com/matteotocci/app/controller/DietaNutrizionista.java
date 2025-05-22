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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
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
    private Button BottoneAlimenti; //  ID corretto dal tuo FXML
    @FXML
    private Button BottoneAlimenti1;
    @FXML
    private Button BottoneAlimenti2;
    @FXML
    private Label nomeUtenteLabelDieta;
    @FXML
    private Label ruoloUtenteLabelDieta;
    @FXML
    private ListView<Dieta> listaDiete; //  ID corretto dal tuo FXML
    @FXML
    private TextField filtroNomeDietaTextField; //  ID corretto dal tuo FXML
    @FXML
    private ImageView profileImage;

    private String loggedInUserId;
    private ObservableList<Dieta> observableListaDiete = FXCollections.observableArrayList();

    // Classe interna per rappresentare una Dieta
    public static class Dieta {
        private int id;
        private String nome;
        private String dataInizio;
        private String dataFine;

        public Dieta(int id, String nome, String dataInizio, String dataFine) {
            this.id = id;
            this.nome = nome;
            this.dataInizio = dataInizio;
            this.dataFine = dataFine;
        }

        public int getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public String getDataInizio() {
            return dataInizio;
        }

        public String getDataFine() {
            return dataFine;
        }

        @Override
        public String toString() {
            return nome + " (Inizio: " + dataInizio + ", Fine: " + dataFine + ")";
        }
    }

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
        setNomeUtenteLabel();
        caricaListaDiete();
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
            ProfiloNutrizionista profileController = fxmlLoader.getController();
            profileController.setLoggedInUserId(loggedInUserId);
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Inizializza la ListView
        listaDiete.setItems(observableListaDiete);
    }

    private void setNomeUtenteLabel() {
        if (ruoloUtenteLabelDieta != null && nomeUtenteLabelDieta != null && loggedInUserId != null) {
            String nomeUtenteCompleto = getNomeUtenteDalDatabase(loggedInUserId);
            nomeUtenteLabelDieta.setText((nomeUtenteCompleto != null && !nomeUtenteCompleto.isEmpty()) ? nomeUtenteCompleto : "Nome e Cognome");
        } else {
            System.err.println("Errore: ruoloUtenteLabelDieta o nomeUtenteLabelDieta o loggedInUserId sono null.");
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

    private void caricaListaDiete() {
        observableListaDiete.clear(); // Pulisce la lista prima di ricaricare
        String url = "jdbc:sqlite:database.db";
        String query = "SELECT id_dieta, nome_dieta, data_inizio, data_fine FROM Dieta";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                observableListaDiete.add(new Dieta(
                        rs.getInt("id_dieta"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore DB (carica diete): " + e.getMessage());
        }
    }

    @FXML
    private void vaiAggiungiNuovaDieta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/NuovaDieta.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nuova Dieta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            Window owner = ((Node) event.getSource()).getScene().getWindow();
            stage.initOwner(owner);

            stage.setOnHidden(e -> caricaListaDiete()); // Ricarica la lista quando la finestra "Nuova Dieta" Ã¨ chiusa

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
