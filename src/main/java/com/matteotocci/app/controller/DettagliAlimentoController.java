package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DettagliAlimentoController {
    @FXML private ImageView immagineGrande;
    @FXML private Label nomeLabel, kcalLabel, proteineLabel, carboidratiLabel, grassiLabel,
            grassiSatLabel, saleLabel, fibreLabel, zuccheriLabel;

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
        String url = alimento.getImmagineGrande();
        if (url != null && !url.trim().isEmpty()) {
            immagineGrande.setImage(new Image(url, 200, 200, true, true));
        } else {
            // Carica immagine di default dalle risorse
            Image defaultImage = new Image("com/matteotocci/app/immagini/png-clipart-computer-icons-encapsulated-postscript-dish-dish-love-food-thumbnail.png", 200, 200, true, true);
            immagineGrande.setImage(defaultImage);
        }
        nomeLabel.setText("Nome: " + alimento.getNome());
        kcalLabel.setText("Kcal: " + alimento.getKcal());
        proteineLabel.setText("Proteine: " + alimento.getProteine());
        carboidratiLabel.setText("Carboidrati: " + alimento.getCarboidrati());
        grassiLabel.setText("Grassi: " + alimento.getGrassi());
        grassiSatLabel.setText("Grassi Saturi: " + alimento.getGrassiSaturi());
        saleLabel.setText("Sale: " + alimento.getSale());
        fibreLabel.setText("Fibre: " + alimento.getFibre());
        zuccheriLabel.setText("Zuccheri: " + alimento.getZuccheri());
    }

    @FXML
    private void chiudiFinestra() {
        ((Stage) immagineGrande.getScene().getWindow()).close();
    }

    private Alimento alimento;

    // Metodo che viene chiamato quando si clicca sul pulsante "Elimina"
    @FXML
    private void handleEliminaAlimento(ActionEvent event) {
        System.out.println("User corrente: " + Session.getUserId());
        System.out.println("User dell'alimento: " + alimento.getUserId());

        // Verifica se l'utente loggato è lo stesso che ha aggiunto l'alimento
        if (alimento.getUserId().equals(Session.getUserId())) {
            eliminaAlimento(alimento); // Elimina l'alimento
        } else {
            mostraMessaggio("Puoi eliminare solo gli alimenti che hai aggiunto.");
        }
    }

    private void eliminaAlimento(Alimento alimento) {
        System.out.println("Alimento eliminato: " + alimento.getNome());
        String query = "DELETE FROM foods WHERE id = ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            System.out.println("ID eliminato: " + alimento.getId());
            stmt.setInt(1, alimento.getId());
            int affected = stmt.executeUpdate();
            System.out.println("Righe eliminate: " + affected);

            if (affected > 0) {
                // Chiudi la finestra se l'eliminazione è andata a buon fine
                Stage stage = (Stage) immagineGrande.getScene().getWindow();
                stage.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void mostraMessaggio(String messaggio) {
        // Logica per mostrare un messaggio (ad esempio usando un `Alert`)
        System.out.println(messaggio);  // Qui puoi sostituire con un'alert di JavaFX
    }


}
