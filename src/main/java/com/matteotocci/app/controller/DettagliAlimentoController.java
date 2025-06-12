package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DettagliAlimentoController {
    @FXML private ImageView immagineGrande;
    @FXML private Label nomeLabel, kcalLabel, proteineLabel, carboidratiLabel, grassiLabel,
            grassiSatLabel, saleLabel, fibreLabel, zuccheriLabel;
    @FXML private Button BottoneElimina;

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

    private String origineFXML;

    public void setOrigineFXML(String origineFXML) {
        this.origineFXML = origineFXML;
        aggiornaVisibilitaBottone();
    }
    private void aggiornaVisibilitaBottone() {
        BottoneElimina.setVisible("Alimenti.fxml".equals(origineFXML) && Session.getUserId().equals(alimento.getUserId()));
    }

    private Alimenti alimentiController;

    public void setAlimentiController(Alimenti   controller) {
        this.alimentiController = controller;
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
            showAlert(Alert.AlertType.ERROR,"Errore nell'eliminazione", "Puoi eliminare solo gli alimenti che hai aggiunto.");
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
                if (alimentiController != null) {
                    System.out.println("filtro: "+alimentiController.getFiltro());
                    alimentiController.resetRicerca();
                    alimentiController.cercaAlimenti(alimentiController.getFiltro(),false);
                }
                // Chiudi la finestra se l'eliminazione è andata a buon fine
                Stage stage = (Stage) immagineGrande.getScene().getWindow();
                stage.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Apply the base style class
            // Add specific style class based on AlertType for custom styling
            if (alertType == Alert.AlertType.INFORMATION) {
                alert.getDialogPane().getStyleClass().add("alert-information");
            } else if (alertType == Alert.AlertType.WARNING) {
                alert.getDialogPane().getStyleClass().add("alert-warning");
            } else if (alertType == Alert.AlertType.ERROR) {
                alert.getDialogPane().getStyleClass().add("alert-error");
            } else if (alertType == Alert.AlertType.CONFIRMATION) {
                alert.getDialogPane().getStyleClass().add("alert-confirmation");
            }
        } else {
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Corrected error message
        }

        alert.showAndWait();
    }



}
