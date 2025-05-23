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
        // MODIFICA QUI: La query deve selezionare "id" (il nome reale della colonna ID)
        String query = "SELECT id, nome_dieta, data_inizio, data_fine FROM Diete";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                observableListaDiete.add(new Dieta(
                        // MODIFICA QUI: Recupera il valore usando il nome della colonna reale "id"
                        rs.getInt("id"),
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

            stage.setOnHidden(e -> caricaListaDiete()); // Ricarica la lista quando la finestra "Nuova Dieta" è chiusa

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void eliminaDietaSelezionata(ActionEvent event) {
        Dieta dietaSelezionata = listaDiete.getSelectionModel().getSelectedItem();

        if (dietaSelezionata == null) {
            System.err.println("Nessuna dieta selezionata da eliminare.");
            return;
        }

        boolean conferma = confermaEliminazione(dietaSelezionata.getNome());
        if (!conferma) {
            return;
        }

        String url = "jdbc:sqlite:database.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);

            try {
                // 1. Recupera tutti gli id_giorno_dieta associati alla dieta
                String queryGiorni = "SELECT id_giorno_dieta FROM Giorno_dieta WHERE id_dieta = ?";
                PreparedStatement psGiorni = conn.prepareStatement(queryGiorni);
                psGiorni.setInt(1, dietaSelezionata.getId());
                ResultSet rs = psGiorni.executeQuery();

                // Costruiamo una lista di id_giorno_dieta
                java.util.List<Integer> listaIdGiorni = new java.util.ArrayList<>();
                while (rs.next()) {
                    listaIdGiorni.add(rs.getInt("id_giorno_dieta"));
                }
                rs.close();
                psGiorni.close();

                if (!listaIdGiorni.isEmpty()) {
                    // 2. Elimina da DietaAlimenti tutti i record con id_giorno_dieta trovati
                    StringBuilder sb = new StringBuilder();
                    sb.append("DELETE FROM DietaAlimenti WHERE id_giorno_dieta IN (");
                    for (int i = 0; i < listaIdGiorni.size(); i++) {
                        sb.append("?");
                        if (i < listaIdGiorni.size() - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");

                    PreparedStatement psEliminaAlimenti = conn.prepareStatement(sb.toString());
                    for (int i = 0; i < listaIdGiorni.size(); i++) {
                        psEliminaAlimenti.setInt(i + 1, listaIdGiorni.get(i));
                    }
                    psEliminaAlimenti.executeUpdate();
                    psEliminaAlimenti.close();
                }

                // 3. Elimina i giorni associati alla dieta
                String eliminaGiorni = "DELETE FROM Giorno_dieta WHERE id_dieta = ?";
                try (PreparedStatement ps = conn.prepareStatement(eliminaGiorni)) {
                    ps.setInt(1, dietaSelezionata.getId());
                    ps.executeUpdate();
                }

                // 4. Elimina la dieta
                String eliminaDieta = "DELETE FROM Diete WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(eliminaDieta)) {
                    ps.setInt(1, dietaSelezionata.getId());
                    ps.executeUpdate();
                }

                conn.commit();

                // Rimuove dalla ListView
                observableListaDiete.remove(dietaSelezionata);

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Errore durante l'eliminazione: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Errore di connessione DB: " + e.getMessage());
        }
    }
    private boolean confermaEliminazione(String nomeDieta) {
        // Implementa una finestra di dialogo di conferma
        // Per esempio con Alert di JavaFX:
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Eliminazione");
        alert.setHeaderText("Sei sicuro di voler eliminare la dieta \"" + nomeDieta + "\"?");
        alert.setContentText("Questa operazione non può essere annullata.");

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK;
    }

}
