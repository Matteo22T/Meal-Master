package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class DettagliAlimentoController {
    @FXML private ImageView immagineGrande;
    @FXML private Label nomeLabel, kcalLabel, proteineLabel, carboidratiLabel, grassiLabel,
            grassiSatLabel, saleLabel, fibreLabel, zuccheriLabel;

    public void setAlimento(Alimento alimento) {
        immagineGrande.setImage(new Image(alimento.getImmagineGrande(), 200, 200, true, true));
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

}
