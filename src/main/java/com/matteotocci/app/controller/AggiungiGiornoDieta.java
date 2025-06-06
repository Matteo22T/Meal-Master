package com.matteotocci.app.controller;
import com.matteotocci.app.model.Dieta; // Questo import è corretto e necessario
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Alimento;
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

    public void setIdDieta(int idDieta){
        this.idDieta=idDieta;
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
        nomeGiornoTextField.setText(nomeGiorno); // <-- MOSTRA il nome nel TextFiel
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

        for (AlimentoQuantificato a : pastiAlimenti.get("colazione")) colazioneListView.getItems().add(a.toString());
        for (AlimentoQuantificato a : pastiAlimenti.get("spuntinoMattina")) spuntinoMattinaListView.getItems().add(a.toString());
        for (AlimentoQuantificato a : pastiAlimenti.get("pranzo")) pranzoListView.getItems().add(a.toString());
        for (AlimentoQuantificato a : pastiAlimenti.get("spuntinoPomeriggio")) spuntinoPomeriggioListView.getItems().add(a.toString());
        for (AlimentoQuantificato a : pastiAlimenti.get("cena")) cenaListView.getItems().add(a.toString());
    }

    private void aggiornaTotali() {
        double kcalTotali = 0, carboidratiTotali = 0, proteineTotali = 0, grassiTotali = 0;
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPasti.get(giornoCorrente);

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
        ResultSet rs = null;

        salvaNomeGiornoCorrente(); // Salva il nome del giorno corrente prima di iniziare il salvataggio generale

        try {
            conn = SQLiteConnessione.connector();
            conn.setAutoCommit(false);
            System.out.println("titoloPiano = '" + titoloPiano + "'");
            System.out.println("UserID = " + Session.getUserId());
            // 1. Recupera l'ID della dieta
            String sqlGetDieta = "SELECT id FROM Diete WHERE nome_dieta = ? AND id_nutrizionista = ?";
            psGetDietaId = conn.prepareStatement(sqlGetDieta);
            psGetDietaId.setString(1, titoloPiano);
            psGetDietaId.setInt(2, Session.getUserId());
            rs = psGetDietaId.executeQuery();

            if (!rs.next()) {
                System.err.println("Dieta non trovata.");
                return;
            }

            int idDieta = rs.getInt("id");

            // 2. Recupera gli ID dei giorni
            String sqlGetGiorni = "SELECT id_giorno_dieta FROM Giorno_dieta WHERE id_dieta = ? ORDER BY id_giorno_dieta ASC";
            psGetGiorni = conn.prepareStatement(sqlGetGiorni);
            psGetGiorni.setInt(1, idDieta);
            rs = psGetGiorni.executeQuery();

            int giornoIndex = 1;

            while (rs.next() && giornoIndex <= numeroGiorni) {
                int idGiornoDieta = rs.getInt("id_giorno_dieta");

                // UPDATE nome giorno
                String nomeGiorno = nomiGiorni.get(giornoIndex); // devi avere questa mappa o altra struttura
                System.out.println("nome giorno = " + nomeGiorno);
                if (nomeGiorno != null && !nomeGiorno.isEmpty()) {
                    String sqlUpdateNomeGiorno = "UPDATE Giorno_dieta SET nome_giorno = ? WHERE id_giorno_dieta = ?";
                    try (PreparedStatement psUpdateNomeGiorno = conn.prepareStatement(sqlUpdateNomeGiorno)) {
                        psUpdateNomeGiorno.setString(1, nomeGiorno);
                        psUpdateNomeGiorno.setInt(2, idGiornoDieta);
                        psUpdateNomeGiorno.executeUpdate();
                    }
                }

                // 3a. Cancella gli alimenti esistenti per questo giorno
                String sqlDelete = "DELETE FROM DietaAlimenti WHERE id_giorno_dieta = ?";
                psDeleteAlimenti = conn.prepareStatement(sqlDelete);
                psDeleteAlimenti.setInt(1, idGiornoDieta);
                psDeleteAlimenti.executeUpdate();

                // 3b. Inserisci i nuovi alimenti per questo giorno
                String sqlInsert = "INSERT INTO DietaAlimenti (id_giorno_dieta,id_alimento, quantita_grammi, pasto) VALUES (?, ?, ?, ?)";
                psInsertAlimenti = conn.prepareStatement(sqlInsert);

                Map<String, ObservableList<AlimentoQuantificato>> pasti = giorniPasti.get(giornoIndex);
                if (pasti == null) {
                    pasti = creaMappaPastiVuota();
                    giorniPasti.put(giornoIndex, pasti);
                }

                for (Map.Entry<String, ObservableList<AlimentoQuantificato>> entry : pasti.entrySet()) {
                    String pasto = entry.getKey();
                    ObservableList<AlimentoQuantificato> lista = entry.getValue();

                    for (AlimentoQuantificato aq : lista) {
                        psInsertAlimenti.setInt(1, idGiornoDieta);
                        psInsertAlimenti.setInt(2, aq.getAlimento().getId());
                        psInsertAlimenti.setDouble(3, aq.getQuantita());
                        psInsertAlimenti.setString(4, pasto);  // <-- aggiunta colonna pasto
                        psInsertAlimenti.addBatch();
                    }
                }

                psInsertAlimenti.executeBatch();

                // Aggiorna i totali nutrizionali per il giorno corrente nel DB
                aggiornaTotaliDBPerGiorno(conn, idGiornoDieta, pasti);

                giornoIndex++;
            }

            conn.commit();
            System.out.println("Piano salvato correttamente.");
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Salvataggio Completato");
            successAlert.setHeaderText(null); // Non mostrare un header text
            successAlert.setContentText("Dieta salvata con successo!");
            successAlert.showAndWait();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (psGetDietaId != null) psGetDietaId.close();
                if (psGetGiorni != null) psGetGiorni.close();
                if (psDeleteAlimenti != null) psDeleteAlimenti.close();
                if (psInsertAlimenti != null) psInsertAlimenti.close();
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
            System.out.println("Seleziona un alimento da rimuovere!");
            return;
        }

        // Trova l'elemento AlimentoQuantificato corrispondente (per confrontare con toString)
        ObservableList<AlimentoQuantificato> listaPasto = giorniPasti.get(giornoCorrente).get(pasto);
        AlimentoQuantificato daRimuovere = null;
        for (AlimentoQuantificato aq : listaPasto) {
            if (aq.toString().equals(selectedString)) {
                daRimuovere = aq;
                break;
            }
        }

        if (daRimuovere == null) {
            System.out.println("Errore: elemento non trovato nella lista interna.");
            return;
        }

        // Rimuovi dalla lista interna
        listaPasto.remove(daRimuovere);

        // Aggiorna la ListView
        aggiornaListView();

        // Aggiorna i totali nutrizionali in UI
        aggiornaTotali();

        // Rimuovi l'alimento dal database
        eliminaAlimentoDalDB(giornoCorrente, daRimuovere);
    }

    private void eliminaAlimentoDalDB(int giorno, AlimentoQuantificato aq) {
        Connection conn = null;
        PreparedStatement psGetDietaId = null;
        PreparedStatement psGetGiornoId = null;
        PreparedStatement psDelete = null;
        ResultSet rs = null;

        try {
            conn = SQLiteConnessione.connector();

            // 1. Recupera l'ID della dieta
            String sqlGetDieta = "SELECT id FROM Diete WHERE nome_dieta = ? AND id_nutrizionista = ?";
            psGetDietaId = conn.prepareStatement(sqlGetDieta);
            psGetDietaId.setString(1, titoloPiano);
            psGetDietaId.setInt(2, Session.getUserId());
            rs = psGetDietaId.executeQuery();

            if (!rs.next()) {
                System.err.println("Dieta non trovata.");
                return;
            }
            int idDieta = rs.getInt("id");
            rs.close();
            psGetDietaId.close();

            // 2. Recupera l'ID del giorno dieta
            String sqlGetGiorno = "SELECT id_giorno_dieta FROM Giorno_dieta WHERE id_dieta = ? ORDER BY id_giorno_dieta ASC LIMIT 1 OFFSET ?";
            psGetGiornoId = conn.prepareStatement(sqlGetGiorno);
            psGetGiornoId.setInt(1, idDieta);
            psGetGiornoId.setInt(2, giorno - 1);  // offset zero-based
            rs = psGetGiornoId.executeQuery();

            if (!rs.next()) {
                System.err.println("Giorno dieta non trovato.");
                return;
            }
            int idGiornoDieta = rs.getInt("id_giorno_dieta");
            rs.close();
            psGetGiornoId.close();

            // 3. Elimina l'alimento dalla tabella DietaAlimenti per questo giorno
            String sqlDelete = "DELETE FROM DietaAlimenti WHERE id_giorno_dieta = ? AND id_alimento = ? AND quantita_grammi = ?";
            psDelete = conn.prepareStatement(sqlDelete);
            psDelete.setInt(1, idGiornoDieta);
            psDelete.setInt(2, aq.getAlimento().getId());
            psDelete.setInt(3, aq.getQuantita());
            int rows = psDelete.executeUpdate();

            if (rows > 0) {
                System.out.println("Alimento rimosso dal database con successo.");
            } else {
                System.out.println("Alimento non trovato nel database per la rimozione.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (psGetDietaId != null) psGetDietaId.close();
                if (psGetGiornoId != null) psGetGiornoId.close();
                if (psDelete != null) psDelete.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Metodo di supporto per aggiornare i totali nel DB per un giorno specifico
    private void aggiornaTotaliDBPerGiorno(Connection conn, int idGiornoDieta, Map<String, ObservableList<AlimentoQuantificato>> pasti) throws SQLException {
        double kcalTotali = 0, carboidratiTotali = 0, proteineTotali = 0, grassiTotali = 0;

        for (ObservableList<AlimentoQuantificato> listaAlimenti : pasti.values()) {
            for (AlimentoQuantificato aq : listaAlimenti) {
                Alimento alimento = aq.getAlimento();
                double quantita = aq.getQuantita();
                kcalTotali += (alimento.getKcal() / 100.0) * quantita;
                carboidratiTotali += (alimento.getCarboidrati() / 100.0) * quantita;
                proteineTotali += (alimento.getProteine() / 100.0) * quantita;
                grassiTotali += (alimento.getGrassi() / 100.0) * quantita;
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
        caricaDatiDieta();
    }

    private void caricaDatiDieta() {
        if (dietaCorrente == null) return;

        int dietaId = dietaCorrente.getId();

        Connection conn = null;
        PreparedStatement psGiorni = null;
        PreparedStatement psAlimenti = null;
        ResultSet rsGiorni = null;
        ResultSet rsAlimenti = null;

        try {
            conn = SQLiteConnessione.connector();

            // 1. Recupera i giorni associati alla dieta
            String sqlGiorni = "SELECT id_giorno_dieta, nome_giorno FROM Giorno_dieta WHERE id_dieta = ? ORDER BY id_giorno_dieta ASC";
            psGiorni = conn.prepareStatement(sqlGiorni);
            psGiorni.setInt(1, dietaId);
            rsGiorni = psGiorni.executeQuery();

            int giornoIndex = 1;
            giorniPasti.clear();
            nomiGiorni.clear();
            idGiornoDietaMap.clear();

            while (rsGiorni.next()) {
                int idGiornoDieta = rsGiorni.getInt("id_giorno_dieta");
                String nomeGiorno = rsGiorni.getString("nome_giorno");

                nomiGiorni.put(giornoIndex, nomeGiorno);
                idGiornoDietaMap.put(giornoIndex, idGiornoDieta);

                // Crea la mappa vuota per questo giorno
                Map<String, ObservableList<AlimentoQuantificato>> pasti = creaMappaPastiVuota();

                // Associa questa mappa 'pasti' al giornoIndex corrente *una sola volta*
                giorniPasti.put(giornoIndex, pasti); // <-- Questa riga è corretta qui

                // 2. Recupera gli alimenti associati a questo giorno, includendo la colonna pasto
                String sqlAlimenti = "SELECT da.id_alimento, da.quantita_grammi, da.pasto, a.nome, a.kcal, a.carboidrati, a.proteine, a.grassi " +
                        "FROM DietaAlimenti da " +
                        "JOIN foods a ON da.id_alimento = a.id " +
                        "WHERE da.id_giorno_dieta = ?";
                psAlimenti = conn.prepareStatement(sqlAlimenti);
                psAlimenti.setInt(1, idGiornoDieta);
                rsAlimenti = psAlimenti.executeQuery();

                while (rsAlimenti.next()) {
                    int idAlimento = rsAlimenti.getInt("id_alimento");
                    String nome = rsAlimenti.getString("nome");
                    double kcal = rsAlimenti.getDouble("kcal");
                    double carboidrati = rsAlimenti.getDouble("carboidrati");
                    double proteine = rsAlimenti.getDouble("proteine");
                    double grassi = rsAlimenti.getDouble("grassi");
                    int quantita = rsAlimenti.getInt("quantita_grammi");

                    String pasto = rsAlimenti.getString("pasto");

                    Alimento alimento = new Alimento(nome, null, kcal, proteine, carboidrati, grassi, 0, 0, 0, 0, null, null, null, idAlimento);

                    if (pasto == null || !pasti.containsKey(pasto)) {
                        pasto = "pranzo"; // default fallback
                    }
                    pasti.get(pasto).add(new AlimentoQuantificato(alimento, quantita));
                }
                rsAlimenti.close();
                psAlimenti.close();

                // Incrementa l'indice del giorno solo una volta, alla fine del ciclo per il giorno corrente
                giornoIndex++; // <-- Questa riga è corretta qui e non deve essere duplicata
            }
            numeroGiorni=giornoIndex-1; // Questo sarà ora corretto
            aggiornaListView();
            aggiornaTotali();
            aggiornaIndicatoreGiorno();


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rsGiorni != null) rsGiorni.close();
                if (psGiorni != null) psGiorni.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}