package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AggiungiGiornoDieta {

    private String titoloPiano;
    private int numeroGiorni;
    private int giornoCorrente = 1;
    @FXML
    private Label giornoCorrenteLabel;
    @FXML
    private ListView<String> colazioneListView;
    @FXML
    private ListView<String> spuntinoMattinaListView;
    @FXML
    private ListView<String> pranzoListView;
    @FXML
    private ListView<String> spuntinoPomeriggioListView;
    @FXML
    private ListView<String> cenaListView;
    @FXML
    private Label kcalTotaliLabel;
    @FXML
    private Label carboidratiLabel;
    @FXML
    private Label proteineLabel;
    @FXML
    private Label grassiLabel;

    private String pastoSelezionato;
    private Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = new HashMap<>();

    // Nuova classe per tenere traccia dell'alimento e della sua quantità
    public static class AlimentoQuantificato {
        private Alimento alimento;
        private int quantita;

        public AlimentoQuantificato(Alimento alimento, int quantita) {
            this.alimento = alimento;
            this.quantita = quantita;
        }

        public Alimento getAlimento() {
            return alimento;
        }

        public int getQuantita() {
            return quantita;
        }

        @Override
        public String toString() {
            return alimento.getNome() + " (" + quantita + " g)"; // Modifica l'unità se necessario
        }
    }

    public void setTitoloPiano(String titolo) {
        this.titoloPiano = titolo;
    }

    public void setNumeroGiorni(int numero) {
        this.numeroGiorni = numero;
        aggiornaIndicatoreGiorno();
    }

    private void aggiornaIndicatoreGiorno() {
        giornoCorrenteLabel.setText("Giorno corrente: " + giornoCorrente + "/" + numeroGiorni);
    }

    @FXML
    private void initialize() {
        pastiAlimenti.put("colazione", FXCollections.observableArrayList());
        pastiAlimenti.put("spuntinoMattina", FXCollections.observableArrayList());
        pastiAlimenti.put("pranzo", FXCollections.observableArrayList());
        pastiAlimenti.put("spuntinoPomeriggio", FXCollections.observableArrayList());
        pastiAlimenti.put("cena", FXCollections.observableArrayList());

        // Inizializza le ListView
        colazioneListView.setItems(FXCollections.observableArrayList());
        spuntinoMattinaListView.setItems(FXCollections.observableArrayList());
        pranzoListView.setItems(FXCollections.observableArrayList());
        spuntinoPomeriggioListView.setItems(FXCollections.observableArrayList());
        cenaListView.setItems(FXCollections.observableArrayList());

        aggiornaTotali();
        aggiornaIndicatoreGiorno();
    }

    @FXML
    private void openAggiungiAlimentoDieta(ActionEvent event) {
        pastoSelezionato = null;
        String buttonId = ((Node) event.getSource()).getId();

        if ("aggiungiColazioneButton".equals(buttonId)) {
            pastoSelezionato = "colazione";
        } else if ("aggiungiSpuntinoMattinaButton".equals(buttonId)) {
            pastoSelezionato = "spuntinoMattina";
        } else if ("aggiungiPranzoButton".equals(buttonId)) {
            pastoSelezionato = "pranzo";
        } else if ("aggiungiSpuntinoPomeriggioButton".equals(buttonId)) {
            pastoSelezionato = "spuntinoPomeriggio";
        } else if ("aggiungiCenaButton".equals(buttonId)) {
            pastoSelezionato = "cena";
        }

        if (pastoSelezionato != null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiAlimentoDieta.fxml"));
                Parent aggiungiAlimentoRoot = fxmlLoader.load();

                AggiungiAlimentoDieta controller = fxmlLoader.getController();
                controller.setGiornoDietaController(this);
                controller.setPastoCorrente(pastoSelezionato);

                Stage aggiungiAlimentoStage = new Stage();
                aggiungiAlimentoStage.setTitle("Aggiungi Alimento/Ricetta");
                aggiungiAlimentoStage.setScene(new Scene(aggiungiAlimentoRoot));
                aggiungiAlimentoStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void aggiungiAlimentoAllaLista(String pasto, Alimento alimento, int quantita) {
        pastiAlimenti.get(pasto).add(new AlimentoQuantificato(alimento, quantita));
        switch (pasto) {
            case "colazione":
                colazioneListView.getItems().add(new AlimentoQuantificato(alimento, quantita).toString());
                break;
            case "spuntinoMattina":
                spuntinoMattinaListView.getItems().add(new AlimentoQuantificato(alimento, quantita).toString());
                break;
            case "pranzo":
                pranzoListView.getItems().add(new AlimentoQuantificato(alimento, quantita).toString());
                break;
            case "spuntinoPomeriggio":
                spuntinoPomeriggioListView.getItems().add(new AlimentoQuantificato(alimento, quantita).toString());
                break;
            case "cena":
                cenaListView.getItems().add(new AlimentoQuantificato(alimento, quantita).toString());
                break;
        }
        aggiornaTotali();
    }

    private void aggiornaTotali() {
        double kcalTotali = 0;
        double carboidratiTotali = 0;
        double proteineTotali = 0;
        double grassiTotali = 0;

        for (ObservableList<AlimentoQuantificato> listaAlimenti : pastiAlimenti.values()) {
            for (AlimentoQuantificato alimentoQuantificato : listaAlimenti) {
                Alimento alimento = alimentoQuantificato.getAlimento();
                int quantita = alimentoQuantificato.getQuantita();

                // Calcola i valori proporzionalmente alla quantità (assumendo valori per 100g nel DB)
                kcalTotali += (alimento.getKcal() / 100.0) * quantita;
                carboidratiTotali += (alimento.getCarboidrati() / 100.0) * quantita;
                proteineTotali += (alimento.getProteine() / 100.0) * quantita;
                grassiTotali += (alimento.getGrassi() / 100.0) * quantita;
            }
        }

        kcalTotaliLabel.setText(String.format("%.2f", kcalTotali));
        carboidratiLabel.setText(String.format("%.2f g", carboidratiTotali));
        proteineLabel.setText(String.format("%.2f g", proteineTotali));
        grassiLabel.setText(String.format("%.2f g", grassiTotali));
    }

    @FXML
    private void salvaPianoButtonAction(ActionEvent event) {
        // Implementa la logica per salvare il piano dieta
        System.out.println("Piano dieta salvato!");
    }

    @FXML
    private void avantiGiornoButtonAction(ActionEvent event) {
        if (giornoCorrente < numeroGiorni) {
            giornoCorrente++;
            aggiornaIndicatoreGiorno();
            // Implementa la logica per caricare i dati del giorno successivo se necessario
            // Potrebbe essere necessario pulire le liste dei pasti per il nuovo giorno
            pastiAlimenti.values().forEach(ObservableList::clear);
            colazioneListView.getItems().clear();
            spuntinoMattinaListView.getItems().clear();
            pranzoListView.getItems().clear();
            spuntinoPomeriggioListView.getItems().clear();
            cenaListView.getItems().clear();
            aggiornaTotali();
        } else {
            System.out.println("Ultimo giorno raggiunto.");
        }
    }

    @FXML
    private void indietroGiornoButtonAction(ActionEvent event) {
        if (giornoCorrente > 1) {
            giornoCorrente--;
            aggiornaIndicatoreGiorno();
            // Implementa la logica per caricare i dati del giorno precedente se necessario
            // Potrebbe essere necessario pulire le liste dei pasti per il nuovo giorno
            pastiAlimenti.values().forEach(ObservableList::clear);
            colazioneListView.getItems().clear();
            spuntinoMattinaListView.getItems().clear();
            pranzoListView.getItems().clear();
            spuntinoPomeriggioListView.getItems().clear();
            cenaListView.getItems().clear();
            aggiornaTotali();
        } else {
            System.out.println("Primo giorno raggiunto.");
        }
    }
}
