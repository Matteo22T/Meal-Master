package com.matteotocci.app.controller;
import com.matteotocci.app.model.Dieta;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.Ricetta; // Importa la classe Ricetta
import com.matteotocci.app.model.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform; // Necessario per Platform.runLater

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AggiungiGiornoDieta {

    private String titoloPiano;
    private int numeroGiorni;
    private int giornoCorrente = 1;
    private int idDieta;


    @FXML private Label giornoCorrenteLabel;
    @FXML private ListView<String> colazioneListView;
    @FXML private ListView<String> spuntinoMattinaListView;
    @FXML private ListView<String> pranzoListView;
    @FXML private ListView<String> spuntinoPomeriggioListView;
    @FXML private ListView<String> cenaListView;
    @FXML private Label kcalTotaliLabel;
    @FXML private Label carboidratiLabel;
    @FXML private Label proteineLabel;
    @FXML private Label grassiLabel;
    @FXML private TextField nomeGiornoTextField;

    private String pastoSelezionato;

    private Map<Integer, String> nomiGiorni = new HashMap<>();
    private Map<Integer, Integer> idGiornoDietaMap = new HashMap<>();

    // Mappa per gli alimenti (ora rinominata per chiarezza)
    private Map<Integer, Map<String, ObservableList<AlimentoQuantificato>>> giorniPastiAlimenti = new HashMap<>();
    // NUOVA Mappa per le ricette
    private Map<Integer, Map<String, ObservableList<RicettaQuantificata>>> giorniPastiRicette = new HashMap<>();


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

    // NUOVA classe per le ricette quantificate
    public static class RicettaQuantificata {
        private Ricetta ricetta;
        private int quantita; // in grammi

        public RicettaQuantificata(Ricetta ricetta, int quantita) {
            this.ricetta = ricetta;
            this.quantita = quantita;
        }

        public Ricetta getRicetta() {
            return ricetta;
        }

        public int getQuantita() {
            return quantita;
        }

        @Override
        public String toString() {
            return ricetta.getNome() + " (Ricetta - " + quantita + " g)";
        }
    }

    public void setIdDieta(int idDieta){
        this.idDieta=idDieta;
    }

    // AGGIUNTO: Metodo per impostare l'ID del giorno dieta per un indice specifico.
    // Sarà chiamato dal controller che crea/carica i giorni della dieta.
    public void setIdGiornoDietaForCurrentDay(int giornoIndex, int idGenerated) {
        idGiornoDietaMap.put(giornoIndex, idGenerated);
        System.out.println("ID giorno dieta " + idGenerated + " mappato al giorno " + giornoIndex); // Debug
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
        String nomeGiorno = nomiGiorni.getOrDefault(giornoCorrente, "");
        nomeGiornoTextField.setText(nomeGiorno);
    }



    @FXML
    private void initialize() {
        // Inizializza le mappe dei pasti per il giorno corrente all'avvio
        giorniPastiAlimenti.put(giornoCorrente, creaMappaPastiAlimentiVuota()); // Rinominato da giorniPasti
        giorniPastiRicette.put(giornoCorrente, creaMappaPastiRicetteVuota()); // AGGIUNTO: Inizializza anche per le ricette
        aggiornaIndicatoreGiorno();
        aggiornaListView();
        aggiornaTotali();
    }

    // Rinominato da creaMappaPastiVuota per chiarezza
    private Map<String, ObservableList<AlimentoQuantificato>> creaMappaPastiAlimentiVuota() {
        Map<String, ObservableList<AlimentoQuantificato>> pasti = new HashMap<>();
        pasti.put("colazione", FXCollections.observableArrayList());
        pasti.put("spuntinoMattina", FXCollections.observableArrayList());
        pasti.put("pranzo", FXCollections.observableArrayList());
        pasti.put("spuntinoPomeriggio", FXCollections.observableArrayList());
        pasti.put("cena", FXCollections.observableArrayList());
        return pasti;
    }

    // NUOVO metodo per creare una mappa vuota per le ricette
    private Map<String, ObservableList<RicettaQuantificata>> creaMappaPastiRicetteVuota() {
        Map<String, ObservableList<RicettaQuantificata>> pasti = new HashMap<>();
        pasti.put("colazione", FXCollections.observableArrayList());
        pasti.put("spuntinoMattina", FXCollections.observableArrayList());
        pasti.put("pranzo", FXCollections.observableArrayList());
        pasti.put("spuntinoPomeriggio", FXCollections.observableArrayList());
        pasti.put("cena", FXCollections.observableArrayList());
        return pasti;
    }

    private Stage aggiungiAlimentoStage = null;
    private AggiungiAlimentoDieta controllerAggiungi = null;

    @FXML
    public void openAggiungiAlimentoDieta(ActionEvent event) {
        pastoSelezionato = null;
        String buttonId = ((Node) event.getSource()).getId();

        switch (buttonId) {
            case "aggiungiColazioneButton": pastoSelezionato = "colazione"; break;
            case "aggiungiSpuntinoMattinaButton": pastoSelezionato = "spuntinoMattina"; break;
            case "aggiungiPranzoButton": pastoSelezionato = "pranzo"; break;
            case "aggiungiSpuntinoPomeriggioButton": pastoSelezionato = "spuntinoPomeriggio"; break;
            case "aggiungiCenaButton": pastoSelezionato = "cena"; break;
        }

        if (pastoSelezionato != null) {
            try {
                if (aggiungiAlimentoStage == null || !aggiungiAlimentoStage.isShowing()) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiAlimentoDieta.fxml"));
                    Parent aggiungiAlimentoRoot = fxmlLoader.load();

                    controllerAggiungi = fxmlLoader.getController();
                    controllerAggiungi.setGiornoDietaController(this);

                    aggiungiAlimentoStage = new Stage();
                    aggiungiAlimentoStage.setTitle("Aggiungi Alimento/Ricetta");
                    aggiungiAlimentoStage.setScene(new Scene(aggiungiAlimentoRoot));

                    // Chiude la finestra se viene chiusa la principale
                    ((Stage)((Node)event.getSource()).getScene().getWindow()).setOnCloseRequest(e -> {
                        if (aggiungiAlimentoStage != null) {
                            aggiungiAlimentoStage.close();
                        }
                    });

                    aggiungiAlimentoStage.show();
                }

                // Aggiorna ogni volta il pasto selezionato
                if (controllerAggiungi != null) {
                    controllerAggiungi.setPastoCorrente(pastoSelezionato);
                }

                // Porta in primo piano
                aggiungiAlimentoStage.toFront();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void aggiungiAlimentoAllaLista(String pasto, Alimento alimento, int quantita) {
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoCorrente); // Rinominato
        if (pastiAlimenti == null) {
            pastiAlimenti = creaMappaPastiAlimentiVuota();
            giorniPastiAlimenti.put(giornoCorrente, pastiAlimenti);
        }
        pastiAlimenti.get(pasto).add(new AlimentoQuantificato(alimento, quantita));
        aggiornaListView();
        aggiornaTotali();
    }

    // NUOVO metodo: aggiunge una ricetta alla lista interna (UI e memoria, NESSUN DB QUI)
    public void aggiungiRicettaAllaLista(Ricetta ricetta, int quantita, String pasto) {
        // 1. Aggiungi la ricetta alla struttura dati in memoria per la UI e i calcoli
        Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoCorrente);
        if (pastiRicette == null) {
            pastiRicette = creaMappaPastiRicetteVuota();
            giorniPastiRicette.put(giornoCorrente, pastiRicette);
        }
        pastiRicette.get(pasto).add(new RicettaQuantificata(ricetta, quantita));

        // 2. Aggiorna la UI (ListView e Totali)
        aggiornaListView();
        aggiornaTotali();

        // N.B.: Il salvataggio nel database di questa ricetta NON avviene qui.
        // Avverrà nel metodo salvaPianoButtonAction()
    }

    private void aggiornaListView() {
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoCorrente); // Rinominato
        Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoCorrente); // AGGIUNTO

        // Assicurati che le mappe per il giorno corrente esistano
        if (pastiAlimenti == null) { // Controllo aggiunto
            pastiAlimenti = creaMappaPastiAlimentiVuota();
            giorniPastiAlimenti.put(giornoCorrente, pastiAlimenti);
        }
        if (pastiRicette == null) { // Controllo aggiunto
            pastiRicette = creaMappaPastiRicetteVuota();
            giorniPastiRicette.put(giornoCorrente, pastiRicette);
        }


        colazioneListView.setItems(FXCollections.observableArrayList());
        spuntinoMattinaListView.setItems(FXCollections.observableArrayList());
        pranzoListView.setItems(FXCollections.observableArrayList());
        spuntinoPomeriggioListView.setItems(FXCollections.observableArrayList());
        cenaListView.setItems(FXCollections.observableArrayList());

        // Aggiungi alimenti
        for (AlimentoQuantificato a : pastiAlimenti.get("colazione")) colazioneListView.getItems().add(a.toString());
        for (AlimentoQuantificato a : pastiAlimenti.get("spuntinoMattina")) spuntinoMattinaListView.getItems().add(a.toString());
        for (AlimentoQuantificato a : pastiAlimenti.get("pranzo")) pranzoListView.getItems().add(a.toString());
        for (AlimentoQuantificato a : pastiAlimenti.get("spuntinoPomeriggio")) spuntinoPomeriggioListView.getItems().add(a.toString());
        for (AlimentoQuantificato a : pastiAlimenti.get("cena")) cenaListView.getItems().add(a.toString());

        // AGGIUNTO: Aggiungi ricette alle rispettive liste
        for (RicettaQuantificata r : pastiRicette.get("colazione")) colazioneListView.getItems().add(r.toString());
        for (RicettaQuantificata r : pastiRicette.get("spuntinoMattina")) spuntinoMattinaListView.getItems().add(r.toString());
        for (RicettaQuantificata r : pastiRicette.get("pranzo")) pranzoListView.getItems().add(r.toString());
        for (RicettaQuantificata r : pastiRicette.get("spuntinoPomeriggio")) spuntinoPomeriggioListView.getItems().add(r.toString());
        for (RicettaQuantificata r : pastiRicette.get("cena")) cenaListView.getItems().add(r.toString());
    }

    private void aggiornaTotali() {
        double kcalTotali = 0, carboidratiTotali = 0, proteineTotali = 0, grassiTotali = 0;
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoCorrente); // Rinominato
        Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoCorrente); // AGGIUNTO

        // Calcola totali dagli alimenti
        if (pastiAlimenti != null) { // Controllo aggiunto
            for (ObservableList<AlimentoQuantificato> listaAlimenti : pastiAlimenti.values()) {
                for (AlimentoQuantificato aq : listaAlimenti) {
                    Alimento alimento = aq.getAlimento();
                    int quantita = aq.getQuantita();
                    kcalTotali += (alimento.getKcal() / 100.0) * quantita;
                    carboidratiTotali += (alimento.getCarboidrati() / 100.0) * quantita;
                    proteineTotali += (alimento.getProteine() / 100.0) * quantita;
                    grassiTotali += (alimento.getGrassi() / 100.0) * quantita;
                }
            }
        }

        // AGGIUNTO: Calcola totali dalle ricette
        if (pastiRicette != null) { // Controllo aggiunto
            for (ObservableList<RicettaQuantificata> listaRicette : pastiRicette.values()) {
                for (RicettaQuantificata rq : listaRicette) {
                    Ricetta ricetta = rq.getRicetta();
                    int quantita = rq.getQuantita();
                    // Assumiamo che i valori nutrizionali delle ricette siano per 100g come gli alimenti
                    kcalTotali += (ricetta.getKcal() / 100.0) * quantita;
                    carboidratiTotali += (ricetta.getCarboidrati() / 100.0) * quantita;
                    proteineTotali += (ricetta.getProteine() / 100.0) * quantita;
                    grassiTotali += (ricetta.getGrassi() / 100.0) * quantita;
                }
            }
        }


        kcalTotaliLabel.setText(String.format("%.2f", kcalTotali));
        carboidratiLabel.setText(String.format("%.2f g", carboidratiTotali));
        proteineLabel.setText(String.format("%.2f g", proteineTotali));
        grassiLabel.setText(String.format("%.2f g", grassiTotali));
    }


    public void salvaPianoButtonAction(ActionEvent event) {
        Connection conn = null;
        PreparedStatement psGetDietaId = null;
        PreparedStatement psGetGiorni = null;
        PreparedStatement psDeleteAlimenti = null;
        PreparedStatement psInsertAlimenti = null;
        PreparedStatement psDeleteRicette = null; // AGGIUNTO
        PreparedStatement psInsertRicette = null; // AGGIUNTO
        ResultSet rs = null;

        salvaNomeGiornoCorrente(); // Salva il nome del giorno corrente prima di iniziare il salvataggio generale

        try {
            conn = SQLiteConnessione.connector();
            conn.setAutoCommit(false); // Inizia una transazione

            System.out.println("titoloPiano = '" + titoloPiano + "'");
            System.out.println("UserID = " + Session.getUserId());
            // 1. Recupera l'ID della dieta (usando l'idDieta già impostato, altrimenti cerca)
            int currentIdDieta = this.idDieta;
            if (currentIdDieta == 0) { // Se non è stato impostato, cercalo
                String sqlGetDieta = "SELECT id FROM Diete WHERE nome_dieta = ? AND id_nutrizionista = ?";
                psGetDietaId = conn.prepareStatement(sqlGetDieta);
                psGetDietaId.setString(1, titoloPiano);
                psGetDietaId.setInt(2, Session.getUserId());
                rs = psGetDietaId.executeQuery();

                if (!rs.next()) {
                    System.err.println("Dieta non trovata. Impossibile salvare il piano.");
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Errore di Salvataggio");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Impossibile trovare la dieta. Assicurati che sia stata creata correttamente.");
                    errorAlert.showAndWait();
                    return;
                }
                currentIdDieta = rs.getInt("id");
                this.idDieta = currentIdDieta; // Aggiorna l'idDieta per future operazioni
            }

            // 2. Recupera gli ID dei giorni
            String sqlGetGiorni = "SELECT id_giorno_dieta FROM Giorno_dieta WHERE id_dieta = ? ORDER BY id_giorno_dieta ASC";
            psGetGiorni = conn.prepareStatement(sqlGetGiorni);
            psGetGiorni.setInt(1, currentIdDieta);
            rs = psGetGiorni.executeQuery();

            // Popola idGiornoDietaMap se non già popolata (per i giorni esistenti)
            if (idGiornoDietaMap.isEmpty()) {
                int tempGiornoIndex = 1;
                while(rs.next()) {
                    idGiornoDietaMap.put(tempGiornoIndex++, rs.getInt("id_giorno_dieta"));
                }
                rs.close(); // Chiudi il ResultSet precedente prima di riaprirlo
                psGetGiorni.close(); // Chiudi il PreparedStatement precedente
                psGetGiorni = conn.prepareStatement(sqlGetGiorni);
                psGetGiorni.setInt(1, currentIdDieta);
                rs = psGetGiorni.executeQuery();
            }


            int giornoIndex = 1;
            while (rs.next() && giornoIndex <= numeroGiorni) {
                int idGiornoDieta = rs.getInt("id_giorno_dieta");
                // Aggiorna la mappa con l'ID del giorno dieta (utile per giorni nuovi o modificati)
                idGiornoDietaMap.put(giornoIndex, idGiornoDieta);


                // UPDATE nome giorno
                String nomeGiorno = nomiGiorni.get(giornoIndex);
                if (nomeGiorno != null) {
                    String sqlUpdateNomeGiorno = "UPDATE Giorno_dieta SET nome_giorno = ? WHERE id_giorno_dieta = ?";
                    try (PreparedStatement psUpdateNomeGiorno = conn.prepareStatement(sqlUpdateNomeGiorno)) {
                        psUpdateNomeGiorno.setString(1, nomeGiorno.isEmpty() ? null : nomeGiorno);
                        psUpdateNomeGiorno.setInt(2, idGiornoDieta);
                        psUpdateNomeGiorno.executeUpdate();
                    }
                }

                // --- GESTIONE ALIMENTI ---
                // 3a. Cancella gli alimenti esistenti per questo giorno
                String sqlDeleteAlimenti = "DELETE FROM DietaAlimenti WHERE id_giorno_dieta = ?";
                psDeleteAlimenti = conn.prepareStatement(sqlDeleteAlimenti);
                psDeleteAlimenti.setInt(1, idGiornoDieta);
                psDeleteAlimenti.executeUpdate();

                // 3b. Inserisci i nuovi alimenti per questo giorno
                String sqlInsertAlimenti = "INSERT INTO DietaAlimenti (id_giorno_dieta,id_alimento, quantita_grammi, pasto) VALUES (?, ?, ?, ?)";
                psInsertAlimenti = conn.prepareStatement(sqlInsertAlimenti);

                Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoIndex);
                if (pastiAlimenti == null) {
                    pastiAlimenti = creaMappaPastiAlimentiVuota();
                    giorniPastiAlimenti.put(giornoIndex, pastiAlimenti);
                }

                for (Map.Entry<String, ObservableList<AlimentoQuantificato>> entry : pastiAlimenti.entrySet()) {
                    String pasto = entry.getKey();
                    ObservableList<AlimentoQuantificato> lista = entry.getValue();

                    for (AlimentoQuantificato aq : lista) {
                        psInsertAlimenti.setInt(1, idGiornoDieta);
                        psInsertAlimenti.setInt(2, aq.getAlimento().getId());
                        psInsertAlimenti.setDouble(3, aq.getQuantita());
                        psInsertAlimenti.setString(4, pasto);
                        psInsertAlimenti.addBatch();
                    }
                }
                psInsertAlimenti.executeBatch();
                // --- FINE GESTIONE ALIMENTI ---


                // --- GESTIONE RICETTE ---
                // AGGIUNTO: 4a. Cancella le ricette esistenti per questo giorno
                String sqlDeleteRicette = "DELETE FROM DietaRicette WHERE id_giorno_dieta = ?";
                psDeleteRicette = conn.prepareStatement(sqlDeleteRicette);
                psDeleteRicette.setInt(1, idGiornoDieta);
                psDeleteRicette.executeUpdate();

                // AGGIUNTO: 4b. Inserisci le nuove ricette per questo giorno
                // AGGIORNATO: Query include la colonna 'pasto'
                String sqlInsertRicette = "INSERT INTO DietaRicette (id_giorno_dieta, id_ricetta, quantita_grammi, pasto) VALUES (?, ?, ?, ?)";
                psInsertRicette = conn.prepareStatement(sqlInsertRicette);

                Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoIndex);
                if (pastiRicette == null) {
                    pastiRicette = creaMappaPastiRicetteVuota();
                    giorniPastiRicette.put(giornoIndex, pastiRicette);
                }

                for (Map.Entry<String, ObservableList<RicettaQuantificata>> entry : pastiRicette.entrySet()) {
                    String pastoRicetta = entry.getKey(); // Recupera il pasto dalla chiave della mappa
                    ObservableList<RicettaQuantificata> lista = entry.getValue();

                    for (RicettaQuantificata rq : lista) {
                        psInsertRicette.setInt(1, idGiornoDieta);
                        psInsertRicette.setInt(2, rq.getRicetta().getId());
                        psInsertRicette.setDouble(3, rq.getQuantita());
                        psInsertRicette.setString(4, pastoRicetta); // Imposta il pasto
                        psInsertRicette.addBatch();
                    }
                }
                psInsertRicette.executeBatch();
                // --- FINE GESTIONE RICETTE ---


                // Aggiorna i totali nutrizionali per il giorno corrente nel DB
                // AGGIUNTO: Passa entrambe le mappe per il calcolo completo
                aggiornaTotaliDBPerGiorno(conn, idGiornoDieta, giorniPastiAlimenti.get(giornoIndex), giorniPastiRicette.get(giornoIndex));


                giornoIndex++;
            }

            conn.commit(); // Conferma la transazione
            System.out.println("Piano salvato correttamente.");
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Salvataggio Completato");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Dieta salvata con successo!");
            successAlert.showAndWait();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback(); // Rollback in caso di errore
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            // Mostra un alert all'utente in caso di errore generale di salvataggio
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Errore di Salvataggio");
                errorAlert.setHeaderText("Impossibile salvare il piano dieta.");
                errorAlert.setContentText("Dettagli: " + e.getMessage());
                errorAlert.showAndWait();
            });
        } finally {
            try {
                if (rs != null) rs.close();
                if (psGetDietaId != null) psGetDietaId.close();
                if (psGetGiorni != null) psGetGiorni.close();
                if (psDeleteAlimenti != null) psDeleteAlimenti.close();
                if (psInsertAlimenti != null) psInsertAlimenti.close();
                if (psDeleteRicette != null) psDeleteRicette.close(); // AGGIUNTO
                if (psInsertRicette != null) psInsertRicette.close(); // AGGIUNTO
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void rimuoviAlimento(ActionEvent event) {
        // Ottieni il bottone che ha generato l'evento
        String buttonId = ((Node) event.getSource()).getId();

        // Mappa per associare il buttonId al nome del pasto e alla ListView corrispondente
        String pasto = null;
        ListView<String> listView = null;

        switch (buttonId) {
            case "rimuoviColazioneButton":
                pasto = "colazione";
                listView = colazioneListView;
                break;
            case "rimuoviSpuntinoMattinaButton":
                pasto = "spuntinoMattina";
                listView = spuntinoMattinaListView;
                break;
            case "rimuoviPranzoButton":
                pasto = "pranzo";
                listView = pranzoListView;
                break;
            case "rimuoviSpuntinoPomeriggioButton":
                pasto = "spuntinoPomeriggio";
                listView = spuntinoPomeriggioListView;
                break;
            case "rimuoviCenaButton":
                pasto = "cena";
                listView = cenaListView;
                break;
        }

        if (pasto == null || listView == null) {
            System.out.println("Errore: bottone non riconosciuto.");
            return;
        }

        String selectedString = listView.getSelectionModel().getSelectedItem();

        if (selectedString == null) {
            System.out.println("Seleziona un alimento o una ricetta da rimuovere!");
            return;
        }

        // Prova a rimuovere come Alimento
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoCorrente);
        AlimentoQuantificato daRimuovereAlimento = null;
        if (pastiAlimenti != null) { // Aggiunto controllo null
            ObservableList<AlimentoQuantificato> listaPasto = pastiAlimenti.get(pasto);
            if (listaPasto != null) { // Aggiunto controllo null
                for (AlimentoQuantificato aq : listaPasto) {
                    if (aq.toString().equals(selectedString)) {
                        daRimuovereAlimento = aq;
                        break;
                    }
                }
            }
        }

        if (daRimuovereAlimento != null) {
            pastiAlimenti.get(pasto).remove(daRimuovereAlimento);
            // Ora, chiama un metodo che gestisce la rimozione dal DB per entrambi i tipi
            // La rimozione dal DB avverrà solo al Salva Piano, qui rimuovi solo dalla memoria
            eliminaItemDalDB(giornoCorrente, daRimuovereAlimento, null);
        } else {
            // Se non è un alimento, prova a rimuovere come Ricetta
            Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoCorrente);
            RicettaQuantificata daRimuovereRicetta = null;
            if (pastiRicette != null) { // Aggiunto controllo null
                ObservableList<RicettaQuantificata> listaPasto = pastiRicette.get(pasto);
                if (listaPasto != null) { // Aggiunto controllo null
                    for (RicettaQuantificata rq : listaPasto) {
                        if (rq.toString().equals(selectedString)) {
                            daRimuovereRicetta = rq;
                            break;
                        }
                    }
                }
            }

            if (daRimuovereRicetta != null) {
                pastiRicette.get(pasto).remove(daRimuovereRicetta);
                // Chiama lo stesso metodo di eliminazione, passando la ricetta e null per l'alimento
                // La rimozione dal DB avverrà solo al Salva Piano, qui rimuovi solo dalla memoria
                eliminaItemDalDB(giornoCorrente, null, daRimuovereRicetta);
            } else {
                System.out.println("Errore: elemento non trovato nella lista interna. Nessun alimento o ricetta selezionata corrisponde.");
                return; // Nessun elemento rimosso
            }
        }

        // Aggiorna la ListView e i totali dopo la rimozione
        aggiornaListView();

        // Aggiorna i totali nutrizionali in UI
        aggiornaTotali();

        // Rimuovi l'alimento dal database
        // eliminaAlimentoDalDB(giornoCorrente, daRimuovere); // Rimosso: la logica DB avviene solo al Salva Piano
    }


    // Nuovo metodo generalizzato per eliminare sia alimenti che ricette dal DB (la rimozione dal DB avverrà solo al 'Salva Piano')
    private void eliminaItemDalDB(int giorno, AlimentoQuantificato aq, RicettaQuantificata rq) {
        // NOTA: In questa implementazione, l'eliminazione dal DB non avviene qui.
        // Avverrà in blocco nel metodo salvaPianoButtonAction() tramite DELETE e re-INSERT.
        // Questo metodo serve solo a notificare che un elemento è stato "segnato" per la rimozione,
        // o per debug, ma l'azione sul DB è ritardata.
        System.out.println("Elemento rimosso dalla memoria per il giorno " + giorno + ". Salvataggio nel DB al click su 'Salva Piano'.");

        // Qui ho rimosso tutta la logica di connessione e PreparedStatement per eliminare direttamente dal DB
        // come richiesto, dato che il salvataggio è centralizzato su "Salva Piano".
        // Se in futuro volessi una rimozione immediata, la logica andrebbe reinserita qui.
    }


    // Metodo di supporto per aggiornare i totali nel DB per un giorno specifico
    private void aggiornaTotaliDBPerGiorno(Connection conn, int idGiornoDieta,
                                           Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti, // AGGIUNTO
                                           Map<String, ObservableList<RicettaQuantificata>> pastiRicette) throws SQLException { // AGGIUNTO
        double kcalTotali = 0, carboidratiTotali = 0, proteineTotali = 0, grassiTotali = 0;

        // Calcola da alimenti
        if (pastiAlimenti != null) {
            for (ObservableList<AlimentoQuantificato> listaAlimenti : pastiAlimenti.values()) {
                for (AlimentoQuantificato aq : listaAlimenti) {
                    Alimento alimento = aq.getAlimento();
                    double quantita = aq.getQuantita();
                    kcalTotali += (alimento.getKcal() / 100.0) * quantita;
                    carboidratiTotali += (alimento.getCarboidrati() / 100.0) * quantita;
                    proteineTotali += (alimento.getProteine() / 100.0) * quantita;
                    grassiTotali += (alimento.getGrassi() / 100.0) * quantita;
                }
            }
        }

        // AGGIUNTO: Calcola da ricette
        if (pastiRicette != null) {
            for (ObservableList<RicettaQuantificata> listaRicette : pastiRicette.values()) {
                for (RicettaQuantificata rq : listaRicette) {
                    Ricetta ricetta = rq.getRicetta();
                    double quantita = rq.getQuantita();
                    kcalTotali += (ricetta.getKcal() / 100.0) * quantita;
                    carboidratiTotali += (ricetta.getCarboidrati() / 100.0) * quantita;
                    proteineTotali += (ricetta.getProteine() / 100.0) * quantita;
                    grassiTotali += (ricetta.getGrassi() / 100.0) * quantita;
                }
            }
        }


        String sqlUpdate = "UPDATE Giorno_dieta SET calorie_giorno = ?, proteine_giorno = ?, carboidrati_giorno = ?, grassi_giorno = ? WHERE id_giorno_dieta = ?";
        try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
            psUpdate.setDouble(1, kcalTotali);
            psUpdate.setDouble(2, proteineTotali);
            psUpdate.setDouble(3, carboidratiTotali);
            psUpdate.setDouble(4, grassiTotali);
            psUpdate.setInt(5, idGiornoDieta);
            psUpdate.executeUpdate();
        }
    }


    @FXML
    private void avantiGiornoButtonAction(ActionEvent event) {

        salvaNomeGiornoCorrente();

        if (giornoCorrente < numeroGiorni) {
            giornoCorrente++;
            // Assicurati che le mappe per il nuovo giorno esistano
            if (!giorniPastiAlimenti.containsKey(giornoCorrente)) { // Rinominato
                giorniPastiAlimenti.put(giornoCorrente, creaMappaPastiAlimentiVuota());
            }
            if (!giorniPastiRicette.containsKey(giornoCorrente)) { // AGGIUNTO
                giorniPastiRicette.put(giornoCorrente, creaMappaPastiRicetteVuota());
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

        salvaNomeGiornoCorrente();

        if (giornoCorrente > 1) {
            giornoCorrente--;
            aggiornaIndicatoreGiorno();
            aggiornaListView();
            aggiornaTotali();
        } else {
            System.out.println("Primo giorno raggiunto.");
        }
    }

    private void salvaNomeGiornoCorrente() {
        String nomeInserito = nomeGiornoTextField.getText();
        if (nomeInserito != null && !nomeInserito.trim().isEmpty()) {
            nomiGiorni.put(giornoCorrente, nomeInserito.trim());
        } else {
            // Se l'utente cancella il nome, puoi scegliere di rimuoverlo dalla mappa
            // o lasciare che il database lo salvi come null/vuoto.
            // Per ora, lo rimuoviamo se è vuoto
            nomiGiorni.remove(giornoCorrente);
        }
        System.out.println("Salvato in nomiGiorni per giorno " + giornoCorrente + ": " + nomiGiorni.get(giornoCorrente)); // Debug
    }

    private Dieta dietaCorrente; // <-- Modificato

    public void impostaDietaDaModificare(Dieta dieta) { // <-- Modificato
        this.dietaCorrente = dieta;
        // AGGIUNTO: Imposta l'ID della dieta e il titolo del piano
        this.idDieta = dieta.getId();
        this.titoloPiano = dieta.getNome();
        this.numeroGiorni = dieta.getNumeroGiorni(); // Assicurati che Dieta abbia getNumeroGiorni()

        caricaDatiDieta();
    }

    private void caricaDatiDieta() {
        if (dietaCorrente == null) return;

        int dietaId = dietaCorrente.getId();

        Connection conn = null;
        PreparedStatement psGiorni = null;
        PreparedStatement psAlimenti = null;
        PreparedStatement psRicette = null;  // AGGIUNTO
        ResultSet rsGiorni = null;
        ResultSet rsAlimenti = null;
        ResultSet rsRicette = null;  // AGGIUNTO

        try {
            conn = SQLiteConnessione.connector();

            // 1. Recupera i giorni associati alla dieta
            String sqlGiorni = "SELECT id_giorno_dieta, nome_giorno FROM Giorno_dieta WHERE id_dieta = ? ORDER BY id_giorno_dieta ASC";
            psGiorni = conn.prepareStatement(sqlGiorni);
            psGiorni.setInt(1, dietaId);
            rsGiorni = psGiorni.executeQuery();

            int giornoIndex = 1;
            giorniPastiAlimenti.clear(); // Rinominato e pulito
            giorniPastiRicette.clear(); // AGGIUNTO e pulito
            nomiGiorni.clear();
            idGiornoDietaMap.clear();

            while (rsGiorni.next()) {
                int idGiornoDieta = rsGiorni.getInt("id_giorno_dieta");
                String nomeGiorno = rsGiorni.getString("nome_giorno");

                nomiGiorni.put(giornoIndex, nomeGiorno);
                idGiornoDietaMap.put(giornoIndex, idGiornoDieta);

                // Crea le mappe vuote per questo giorno (rinominato)
                Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = creaMappaPastiAlimentiVuota();
                Map<String, ObservableList<RicettaQuantificata>> pastiRicette = creaMappaPastiRicetteVuota(); // AGGIUNTO

                // Associa le mappe 'pasti' al giornoIndex corrente
                giorniPastiAlimenti.put(giornoIndex, pastiAlimenti); // Rinominato
                giorniPastiRicette.put(giornoIndex, pastiRicette); // AGGIUNTO

                // 2. Recupera gli alimenti associati a questo giorno, includendo la colonna pasto
                String sqlAlimenti = "SELECT da.id_alimento, da.quantita_grammi, da.pasto, a.nome, a.brand, a.kcal, a.proteine, a.carboidrati, a.grassi, a.grassiSaturi, a.sale, a.fibre, a.zuccheri, a.immaginePiccola, a.immagineGrande, a.user_id " +
                        "FROM DietaAlimenti da " +
                        "JOIN foods a ON da.id_alimento = a.id " +
                        "WHERE da.id_giorno_dieta = ?";
                psAlimenti = conn.prepareStatement(sqlAlimenti);
                psAlimenti.setInt(1, idGiornoDieta);
                rsAlimenti = psAlimenti.executeQuery();

                while (rsAlimenti.next()) {
                    // Crea l'oggetto Alimento completo (potrebbe mancare qualche campo se la tua classe Alimento ha più campi)
                    Alimento alimento = new Alimento(
                            rsAlimenti.getString("nome"),
                            rsAlimenti.getString("brand"),
                            rsAlimenti.getDouble("kcal"),
                            rsAlimenti.getDouble("proteine"),
                            rsAlimenti.getDouble("carboidrati"),
                            rsAlimenti.getDouble("grassi"),
                            rsAlimenti.getDouble("grassiSaturi"),
                            rsAlimenti.getDouble("sale"),
                            rsAlimenti.getDouble("fibre"),
                            rsAlimenti.getDouble("zuccheri"),
                            rsAlimenti.getString("immaginePiccola"),
                            rsAlimenti.getString("immagineGrande"),
                            rsAlimenti.getInt("user_id"),
                            rsAlimenti.getInt("id_alimento") // ID dell'alimento
                    );
                    int quantita = rsAlimenti.getInt("quantita_grammi");
                    String pasto = rsAlimenti.getString("pasto");

                    if (pasto == null || !pastiAlimenti.containsKey(pasto)) {
                        pasto = "pranzo"; // default fallback
                    }
                    pastiAlimenti.get(pasto).add(new AlimentoQuantificato(alimento, quantita));
                }
                // Assicurati di chiudere sempre ResultSet e PreparedStatement
                if (rsAlimenti != null) rsAlimenti.close();
                if (psAlimenti != null) psAlimenti.close();


                // AGGIUNTO: 3. Recupera le ricette associate a questo giorno, includendo la colonna 'pasto'
                String sqlRicette = "SELECT dr.id_ricetta, dr.quantita_grammi, dr.pasto, r.nome, r.descrizione, r.categoria, r.id_utente, r.kcal, r.proteine, r.carboidrati, r.grassi, r.grassi_saturi, r.zuccheri, r.fibre, r.sale " +
                        "FROM DietaRicette dr " +
                        "JOIN Ricette r ON dr.id_ricetta = r.id " +
                        "WHERE dr.id_giorno_dieta = ?";
                psRicette = conn.prepareStatement(sqlRicette);
                psRicette.setInt(1, idGiornoDieta);
                rsRicette = psRicette.executeQuery();

                while (rsRicette.next()) {
                    // Crea l'oggetto Ricetta completo
                    Ricetta ricetta = new Ricetta(
                            rsRicette.getInt("id_ricetta"),
                            rsRicette.getString("nome"),
                            rsRicette.getString("descrizione"),
                            rsRicette.getString("categoria"),
                            rsRicette.getInt("id_utente"),
                            rsRicette.getDouble("kcal"),
                            rsRicette.getDouble("proteine"),
                            rsRicette.getDouble("carboidrati"),
                            rsRicette.getDouble("grassi"),
                            rsRicette.getDouble("grassi_saturi"),
                            rsRicette.getDouble("zuccheri"),
                            rsRicette.getDouble("fibre"),
                            rsRicette.getDouble("sale")
                    );
                    int quantita = rsRicette.getInt("quantita_grammi");
                    String pastoRicetta = rsRicette.getString("pasto"); // Recupera il pasto dal DB

                    if (pastoRicetta == null || !pastiRicette.containsKey(pastoRicetta)) {
                        pastoRicetta = "pranzo"; // Fallback se il pasto non è specificato
                    }
                    pastiRicette.get(pastoRicetta).add(new RicettaQuantificata(ricetta, quantita));
                }
                // Assicurati di chiudere sempre ResultSet e PreparedStatement
                if (rsRicette != null) rsRicette.close();
                if (psRicette != null) psRicette.close();


                giornoIndex++; // Incrementa l'indice del giorno solo una volta
            }
            numeroGiorni = giornoIndex - 1; // Aggiorna il numero totale di giorni
            aggiornaListView();
            aggiornaTotali();
            aggiornaIndicatoreGiorno();


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rsGiorni != null) rsGiorni.close();
                if (psGiorni != null) psGiorni.close();
                if (psAlimenti != null) psAlimenti.close();
                if (rsAlimenti != null) rsAlimenti.close();
                if (psRicette != null) psRicette.close();   // AGGIUNTO
                if (rsRicette != null) rsRicette.close();   // AGGIUNTO
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}