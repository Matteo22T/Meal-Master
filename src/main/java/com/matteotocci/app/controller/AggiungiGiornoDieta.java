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

    // Map<giorno, Map<pasto, lista di alimenti>>
    private Map<Integer, Map<String, ObservableList<AlimentoQuantificato>>> giorniPasti = new HashMap<>();

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
            return alimento.getNome() + " (" + quantita + " g)";
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
        giorniPasti.put(giornoCorrente, creaMappaPastiVuota());
        aggiornaIndicatoreGiorno();
        aggiornaListView();
        aggiornaTotali();
    }

    private Map<String, ObservableList<AlimentoQuantificato>> creaMappaPastiVuota() {
        Map<String, ObservableList<AlimentoQuantificato>> pasti = new HashMap<>();
        pasti.put("colazione", FXCollections.observableArrayList());
        pasti.put("spuntinoMattina", FXCollections.observableArrayList());
        pasti.put("pranzo", FXCollections.observableArrayList());
        pasti.put("spuntinoPomeriggio", FXCollections.observableArrayList());
        pasti.put("cena", FXCollections.observableArrayList());
        return pasti;
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
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPasti.get(giornoCorrente);
        pastiAlimenti.get(pasto).add(new AlimentoQuantificato(alimento, quantita));
        aggiornaListView();
        aggiornaTotali();
    }

    private void aggiornaListView() {
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPasti.get(giornoCorrente);

        colazioneListView.setItems(FXCollections.observableArrayList());
        spuntinoMattinaListView.setItems(FXCollections.observableArrayList());
        pranzoListView.setItems(FXCollections.observableArrayList());
        spuntinoPomeriggioListView.setItems(FXCollections.observableArrayList());
        cenaListView.setItems(FXCollections.observableArrayList());

        for (AlimentoQuantificato a : pastiAlimenti.get("colazione")) {
            colazioneListView.getItems().add(a.toString());
        }
        for (AlimentoQuantificato a : pastiAlimenti.get("spuntinoMattina")) {
            spuntinoMattinaListView.getItems().add(a.toString());
        }
        for (AlimentoQuantificato a : pastiAlimenti.get("pranzo")) {
            pranzoListView.getItems().add(a.toString());
        }
        for (AlimentoQuantificato a : pastiAlimenti.get("spuntinoPomeriggio")) {
            spuntinoPomeriggioListView.getItems().add(a.toString());
        }
        for (AlimentoQuantificato a : pastiAlimenti.get("cena")) {
            cenaListView.getItems().add(a.toString());
        }
    }

    private void aggiornaTotali() {
        double kcalTotali = 0;
        double carboidratiTotali = 0;
        double proteineTotali = 0;
        double grassiTotali = 0;

        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPasti.get(giornoCorrente);

        for (ObservableList<AlimentoQuantificato> listaAlimenti : pastiAlimenti.values()) {
            for (AlimentoQuantificato alimentoQuantificato : listaAlimenti) {
                Alimento alimento = alimentoQuantificato.getAlimento();
                int quantita = alimentoQuantificato.getQuantita();

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
        System.out.println("Piano dieta salvato!");
        // Qui potresti serializzare giorniPasti o salvarlo su DB
    }

    @FXML
    private void avantiGiornoButtonAction(ActionEvent event) {
        if (giornoCorrente < numeroGiorni) {
            giornoCorrente++;
            if (!giorniPasti.containsKey(giornoCorrente)) {
                giorniPasti.put(giornoCorrente, creaMappaPastiVuota());
            }
            aggiornaIndicatoreGiorno();
            aggiornaListView();
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
            aggiornaListView();
            aggiornaTotali();
        } else {
            System.out.println("Primo giorno raggiunto.");
        }
    }
}
