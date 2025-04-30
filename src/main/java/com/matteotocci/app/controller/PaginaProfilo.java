package com.matteotocci.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.URL;
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

    // Dovresti avere un modo per identificare l'utente corrente.
    // Potrebbe essere una variabile di istanza impostata al login o passata.
    private String utenteCorrenteId = "user123"; // Sostituisci con l'ID utente effettivo

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Recupera il nome utente dal database e lo imposta nella label
        String nomeUtente = getNomeUtenteDalDatabase(utenteCorrenteId);
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            nomeUtenteSidebarLabel.setText(nomeUtente);
            benvenutoLabel.setText("Benvenuto " + nomeUtente); // Aggiorna anche la label di benvenuto
        } else {
            nomeUtenteSidebarLabel.setText("Utente Sconosciuto"); // Gestisci il caso in cui non trovi il nome
            benvenutoLabel.setText("Benvenuto Utente");
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String url = "jdbc:sqlite:database.db"; // Sostituisci con il percorso del tuo database
        String query = "SELECT nome FROM utenti WHERE id_utente = ?"; // Adatta la query e i nomi delle colonne

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nomeUtente = rs.getString("nome"); // Recupera il nome dalla colonna "nome"
            }

        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
        }
        return nomeUtente;
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
            Stage modificaPasswordStage = new Stage();
            modificaPasswordStage.setTitle("Modifica Password");
            modificaPasswordStage.setScene(new Scene(modificaPasswordRoot));
            modificaPasswordStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

