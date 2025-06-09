package com.matteotocci.app.controller;

import com.matteotocci.app.model.Dieta;
import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.Ricetta; // Importa la classe Ricetta
import com.matteotocci.app.model.SQLiteConnessione;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator; // Import per ordinamento
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualizzaDieta {

    @FXML
    private Label labelNomeDieta;
    @FXML
    private Label labelDataInizio;
    @FXML
    private Label labelDataFine;
    @FXML
    private VBox contenitoreGiorni;

    private Dieta dietaCorrente;

    /**
     * Interfaccia comune per AlimentoQuantita e RicettaQuantita per la visualizzazione.
     */
    public interface DietaItem {
        String getDisplayName();
        String getDisplayQuantity();
        ImageView getDisplayImage(); // Può restituire null se non c'è immagine o un placeholder
    }

    /**
     * Classe interna per rappresentare un Alimento con la sua quantità, implementa DietaItem.
     */
    private static class AlimentoQuantita implements DietaItem {
        private Alimento alimento;
        private double quantita;

        public AlimentoQuantita(Alimento alimento, double quantita) {
            this.alimento = alimento;
            this.quantita = quantita;
        }

        public Alimento getAlimento() { return alimento; }
        public double getQuantita() { return quantita; }

        @Override
        public String getDisplayName() {
            return alimento.getNome();
        }

        @Override
        public String getDisplayQuantity() {
            // Formatta la quantità in grammi, senza decimali se è un intero
            if (quantita == (long) quantita) {
                return "(" + (long) quantita + "g)";
            }
            return "(" + String.format("%.2f", quantita) + "g)";
        }

        @Override
        public ImageView getDisplayImage() {
            ImageView originalImageView = alimento.getImmagine();
            if (originalImageView != null && originalImageView.getImage() != null) {
                // Clona l'ImageView per evitare "javafx.scene.image.ImageView is not a child of..."
                ImageView clonedImageView = new ImageView(originalImageView.getImage());
                clonedImageView.setFitWidth(35);
                clonedImageView.setFitHeight(35);
                clonedImageView.setPreserveRatio(true);
                return clonedImageView;
            }
            return null; // O un placeholder generico se l'immagine non è disponibile
        }
    }

    /**
     * Classe interna per rappresentare una Ricetta con la sua quantità, implementa DietaItem.
     */
    private static class RicettaQuantita implements DietaItem {
        private Ricetta ricetta;
        private double quantita;

        public RicettaQuantita(Ricetta ricetta, double quantita) {
            this.ricetta = ricetta;
            this.quantita = quantita;
        }

        public Ricetta getRicetta() { return ricetta; }
        public double getQuantita() { return quantita; }

        @Override
        public String getDisplayName() {
            return ricetta.getNome();
        }

        @Override
        public String getDisplayQuantity() {
            // Formatta la quantità in grammi, senza decimali se è un intero
            if (quantita == (long) quantita) {
                return "(Ricetta - " + (long) quantita + "g)";
            }
            return "(Ricetta - " + String.format("%.2f", quantita) + "g)";
        }

        @Override
        public ImageView getDisplayImage() {
            // Le ricette nel tuo modello non hanno un'immagine diretta.
            // Restituisci null, il metodo popolaContenitoreGiorni aggiungerà un placeholder.
            return null;
        }
    }


    /**
     * Metodo chiamato dal controller precedente per passare l'oggetto Dieta da visualizzare.
     * Questo è il punto d'ingresso per popolare la finestra.
     * @param dieta L'oggetto Dieta da mostrare.
     */
    public void impostaDietaDaVisualizzare(Dieta dieta) {
        this.dietaCorrente = dieta;
        if (dieta == null) {
            System.err.println("ERRORE (VisualizzaDieta): impostaDietaDaVisualizzare - Dieta ricevuta è NULL. Impossibile caricare i dettagli.");
            // Potresti mostrare un messaggio all'utente qui
        }
        caricaEVisualizzaDettagliDieta();
    }

    @FXML
    public void initialize() {
        // L'inizializzazione specifica della UI viene fatta in caricaEVisualizzaDettagliDieta
        // dopo che la dietaCorrente è stata impostata.
    }

    private void caricaEVisualizzaDettagliDieta() {
        if (dietaCorrente == null) {
            System.err.println("ERRORE (VisualizzaDieta): caricaEVisualizzaDettagliDieta chiamato con dietaCorrente NULL. Uscita.");
            return;
        }

        // Popola le Label dell'intestazione della dieta
        labelNomeDieta.setText(dietaCorrente.getNome());
        labelDataInizio.setText("Data Inizio: " + (dietaCorrente.getDataInizio() != null ? dietaCorrente.getDataInizio() : "N/D"));
        labelDataFine.setText("Data Fine: " + (dietaCorrente.getDataFine() != null ? dietaCorrente.getDataFine() : "N/D"));

        // Recupera i dettagli di tutti i giorni, pasti, alimenti e ricette dal database
        // Questo metodo ora è rinominato e restituisce una mappa di DietaItem
        Map<Integer, Map<String, List<DietaItem>>> giorniDieta = recuperaDettagliCompletiDieta(dietaCorrente.getId());

        if (giorniDieta.isEmpty()) {
            System.out.println("Nessun alimento o ricetta trovata per questa dieta. Potrebbe essere vuota.");
            // Potresti aggiungere un messaggio visibile all'utente.
        }

        // Popola il contenitoreGiorni (VBox) dinamicamente con gli elementi UI
        popolaContenitoreGiorni(giorniDieta);
    }

    /**
     * Recupera tutti i dettagli completi di una dieta dal database: giorni, pasti, alimenti e ricette correlate.
     * Questo metodo sostituisce il precedente 'recuperaDettagliGiorniAlimenti'.
     * @param idDieta L'ID della dieta per cui recuperare i dettagli.
     * @return Una mappa complessa contenente tutti i dettagli strutturati della dieta (alimenti e ricette).
     */
    private Map<Integer, Map<String, List<DietaItem>>> recuperaDettagliCompletiDieta(int idDieta) {
        Map<Integer, Map<String, List<DietaItem>>> giorniDieta = new HashMap<>();
        Connection conn = null;

        try {
            conn = SQLiteConnessione.connector();

            // Query per gli ALIMENTI
            String sqlAlimenti = "SELECT " +
                    "  gd.id_giorno_dieta, " +
                    "  da.pasto, " +
                    "  f.id AS alimento_id, " +
                    "  f.nome AS alimento_nome, " +
                    "  f.brand, " +
                    "  f.kcal, " +
                    "  f.proteine, " +
                    "  f.carboidrati, " +
                    "  f.grassi, " +
                    "  f.grassiSaturi, " +
                    "  f.sale, " +
                    "  f.fibre, " +
                    "  f.zuccheri, " +
                    "  f.immaginePiccola, " +
                    "  f.immagineGrande, " +
                    "  f.user_id, " +
                    "  da.quantita_grammi " +
                    "FROM Giorno_dieta gd " +
                    "JOIN DietaAlimenti da ON gd.id_giorno_dieta = da.id_giorno_dieta " +
                    "JOIN foods f ON da.id_alimento = f.id " +
                    "WHERE gd.id_dieta = ? " +
                    "ORDER BY gd.id_giorno_dieta, " +
                    "       CASE da.pasto " +
                    "         WHEN 'colazione' THEN 1 " +
                    "         WHEN 'spuntinoMattina' THEN 2 " +
                    "         WHEN 'pranzo' THEN 3 " +
                    "         WHEN 'spuntinoPomeriggio' THEN 4 " +
                    "         WHEN 'cena' THEN 5 " +
                    "         ELSE 6 " +
                    "       END"; // Ordina i pasti per la visualizzazione

            try (PreparedStatement pstmtAlimenti = conn.prepareStatement(sqlAlimenti)) {
                pstmtAlimenti.setInt(1, idDieta);
                ResultSet rsAlimenti = pstmtAlimenti.executeQuery();

                while (rsAlimenti.next()) {
                    int idGiornoDieta = rsAlimenti.getInt("id_giorno_dieta");
                    String nomePasto = rsAlimenti.getString("pasto");
                    double quantita = rsAlimenti.getDouble("quantita_grammi");

                    Alimento alimento = new Alimento(
                            rsAlimenti.getString("alimento_nome"),
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
                            rsAlimenti.getInt("alimento_id")
                    );

                    giorniDieta.putIfAbsent(idGiornoDieta, new HashMap<>());
                    Map<String, List<DietaItem>> pastiDelGiorno = giorniDieta.get(idGiornoDieta);

                    pastiDelGiorno.putIfAbsent(nomePasto, new ArrayList<>());
                    pastiDelGiorno.get(nomePasto).add(new AlimentoQuantita(alimento, quantita));
                }
            }

            // Query per le RICETTE (include la colonna 'pasto' dalla tabella DietaRicette)
            String sqlRicette = "SELECT " +
                    "  gd.id_giorno_dieta, " +
                    "  dr.pasto, " + // Recupera la colonna 'pasto'
                    "  r.id AS ricetta_id, " +
                    "  r.nome AS ricetta_nome, " +
                    "  r.descrizione, " +
                    "  r.categoria, " +
                    "  r.id_utente, " +
                    "  r.kcal, " +
                    "  r.proteine, " +
                    "  r.carboidrati, " +
                    "  r.grassi, " +
                    "  r.grassi_saturi, " +
                    "  r.zuccheri, " +
                    "  r.fibre, " +
                    "  r.sale, " +
                    "  dr.quantita_grammi " +
                    "FROM Giorno_dieta gd " +
                    "JOIN DietaRicette dr ON gd.id_giorno_dieta = dr.id_giorno_dieta " +
                    "JOIN Ricette r ON dr.id_ricetta = r.id " +
                    "WHERE gd.id_dieta = ? " +
                    "ORDER BY gd.id_giorno_dieta, " +
                    "       CASE dr.pasto " + // Ordina anche le ricette per pasto
                    "         WHEN 'colazione' THEN 1 " +
                    "         WHEN 'spuntinoMattina' THEN 2 " +
                    "         WHEN 'pranzo' THEN 3 " +
                    "         WHEN 'spuntinoPomeriggio' THEN 4 " +
                    "         WHEN 'cena' THEN 5 " +
                    "         ELSE 6 " +
                    "       END";

            try (PreparedStatement pstmtRicette = conn.prepareStatement(sqlRicette)) {
                pstmtRicette.setInt(1, idDieta);
                ResultSet rsRicette = pstmtRicette.executeQuery();

                while (rsRicette.next()) {
                    int idGiornoDieta = rsRicette.getInt("id_giorno_dieta");
                    String nomePasto = rsRicette.getString("pasto"); // Recupera il pasto dalla ricetta
                    double quantita = rsRicette.getDouble("quantita_grammi");

                    Ricetta ricetta = new Ricetta(
                            rsRicette.getInt("ricetta_id"),
                            rsRicette.getString("ricetta_nome"),
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

                    giorniDieta.putIfAbsent(idGiornoDieta, new HashMap<>());
                    Map<String, List<DietaItem>> pastiDelGiorno = giorniDieta.get(idGiornoDieta);

                    pastiDelGiorno.putIfAbsent(nomePasto, new ArrayList<>());
                    pastiDelGiorno.get(nomePasto).add(new RicettaQuantita(ricetta, quantita));
                }
            }

        } catch (SQLException e) {
            System.err.println("ERRORE SQL (VisualizzaDieta): Durante il recupero dei dettagli della dieta: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRORE GENERICO (VisualizzaDieta): Durante il recupero dei dettagli della dieta: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return giorniDieta;
    }


    /**
     * Popola il VBox 'contenitoreGiorni' dinamicamente con i blocchi UI per ogni giorno e pasto.
     * I giorni sono ordinati per il loro 'id_giorno_dieta' e visualizzati come "Giorno 1", "Giorno 2", ecc.
     * @param giorniDieta La mappa dei dati recuperati dal database, contenente sia alimenti che ricette.
     */
    private void popolaContenitoreGiorni(Map<Integer, Map<String, List<DietaItem>>> giorniDieta) {
        contenitoreGiorni.getChildren().clear();

        if (giorniDieta.isEmpty()) {
            Label noDataLabel = new Label("Nessun piano alimentare dettagliato trovato per questa dieta.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888; -fx-padding: 20px;");
            contenitoreGiorni.getChildren().add(noDataLabel);
            return;
        }

        List<Integer> idGiorniOrdinati = new ArrayList<>(giorniDieta.keySet());
        idGiorniOrdinati.sort(Integer::compareTo); // Ordina gli ID dei giorni

        int contatoreGiorno = 1;

        for (Integer idGiorno : idGiorniOrdinati) {
            Map<String, List<DietaItem>> pastiDelGiorno = giorniDieta.get(idGiorno);

            VBox giornoBox = new VBox(10);
            giornoBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 15px; -fx-background-color: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            VBox.setMargin(giornoBox, new Insets(0, 0, 20, 0));

            Label giornoLabel = new Label("Giorno " + contatoreGiorno);
            giornoLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            giornoBox.getChildren().add(giornoLabel);

            giornoBox.getChildren().add(new Separator());

            // Ordine desiderato per la visualizzazione dei pasti
            List<String> ordinePasti = List.of(
                    "colazione",
                    "spuntinoMattina",
                    "pranzo",
                    "spuntinoPomeriggio",
                    "cena"
            );

            for (String nomePastoDalDB : ordinePasti) {
                if (pastiDelGiorno.containsKey(nomePastoDalDB)) {
                    List<DietaItem> itemsDelPasto = pastiDelGiorno.get(nomePastoDalDB);

                    // Ordina gli elementi all'interno di ogni pasto per nome
                    itemsDelPasto.sort(Comparator.comparing(DietaItem::getDisplayName));

                    VBox pastoBox = new VBox(5);
                    pastoBox.setPadding(new Insets(5, 0, 5, 10));

                    String nomePastoVisualizzato;
                    switch (nomePastoDalDB) {
                        case "spuntinoMattina":
                            nomePastoVisualizzato = "Spuntino Mattina";
                            break;
                        case "spuntinoPomeriggio":
                            nomePastoVisualizzato = "Spuntino Pomeriggio";
                            break;
                        default:
                            // Capitalizza la prima lettera per gli altri pasti (colazione, pranzo, cena)
                            nomePastoVisualizzato = nomePastoDalDB.substring(0, 1).toUpperCase() + nomePastoDalDB.substring(1);
                            break;
                    }

                    Label pastoLabel = new Label(nomePastoVisualizzato);
                    pastoLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
                    pastoBox.getChildren().add(pastoLabel);

                    for (DietaItem item : itemsDelPasto) {
                        HBox itemHBox = new HBox(10);
                        itemHBox.setPadding(new Insets(0, 0, 0, 15));

                        ImageView displayImage = item.getDisplayImage();
                        if (displayImage != null) {
                            itemHBox.getChildren().add(displayImage);
                        } else {
                            // Se l'immagine non è disponibile (es. per le ricette), puoi aggiungere un placeholder testuale o un'icona generica
                            Label placeholder = new Label("🍽️"); // Esempio di placeholder con emoji
                            placeholder.setStyle("-fx-font-size: 20px; -fx-alignment: center; -fx-pref-width: 35px; -fx-pref-height: 35px;");
                            itemHBox.getChildren().add(placeholder);
                        }

                        Label itemLabel = new Label("- " + item.getDisplayName() + " " + item.getDisplayQuantity());
                        itemLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
                        itemHBox.getChildren().add(itemLabel);

                        pastoBox.getChildren().add(itemHBox);
                    }
                    giornoBox.getChildren().add(pastoBox);
                }
            }
            contenitoreGiorni.getChildren().add(giornoBox);
            contatoreGiorno++;
        }
    }
}