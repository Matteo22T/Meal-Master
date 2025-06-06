package com.matteotocci.app.controller;

import com.matteotocci.app.model.Dieta;
import com.matteotocci.app.model.Alimento;
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
     * Metodo chiamato dal controller precedente per passare l'oggetto Dieta da visualizzare.
     * Questo è il punto d'ingresso per popolare la finestra.
     * @param dieta L'oggetto Dieta da mostrare.
     */
    public void impostaDietaDaVisualizzare(Dieta dieta) {
        this.dietaCorrente = dieta;
        if (dieta == null) {
            System.err.println("ERRORE (VisualizzaDieta): impostaDietaDaVisualizzare - Dieta ricevuta è NULL. Impossibile caricare i dettagli.");
        }
        caricaEVisualizzaDettagliDieta();
    }

    @FXML
    public void initialize() {
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

        // Recupera i dettagli di tutti i giorni, pasti e alimenti dal database
        Map<Integer, Map<String, List<AlimentoQuantita>>> giorniDieta = recuperaDettagliGiorniAlimenti(dietaCorrente.getId());

        if (giorniDieta.isEmpty()) {
            // Nessun alimento trovato per questa dieta. Potresti aggiungere un messaggio visibile all'utente.
        }

        // Popola il contenitoreGiorni (VBox) dinamicamente con gli elementi UI
        popolaContenitoreGiorni(giorniDieta);
    }

    /**
     * Recupera tutti i dettagli di una dieta dal database: giorni, pasti e alimenti correlati.
     * Utilizza id_giorno_dieta per l'ordinamento e il raggruppamento dei giorni.
     * @param idDieta L'ID della dieta per cui recuperare i dettagli.
     * @return Una mappa complessa contenente tutti i dettagli strutturati della dieta.
     */
    private Map<Integer, Map<String, List<AlimentoQuantita>>> recuperaDettagliGiorniAlimenti(int idDieta) {
        Map<Integer, Map<String, List<AlimentoQuantita>>> giorniDieta = new HashMap<>();

        String sql = "SELECT " +
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
                "         WHEN 'spuntino' THEN 2 " +
                "         WHEN 'pranzo' THEN 3 " +
                "         WHEN 'merenda' THEN 4 " +
                "         WHEN 'cena' THEN 5 " +
                "         ELSE 6 " +
                "       END";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idDieta);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int idGiornoDieta = rs.getInt("id_giorno_dieta");
                String nomePasto = rs.getString("pasto");
                double quantita = rs.getDouble("quantita_grammi");

                Integer alimentoId = rs.getInt("alimento_id");
                String alimentoNome = rs.getString("alimento_nome");
                String brand = rs.getString("brand");
                double kcal = rs.getDouble("kcal");
                double proteine = rs.getDouble("proteine");
                double carboidrati = rs.getDouble("carboidrati");
                double grassi = rs.getDouble("grassi");
                double grassiSaturi = rs.getDouble("grassiSaturi");
                double sale = rs.getDouble("sale");
                double fibre = rs.getDouble("fibre");
                double zuccheri = rs.getDouble("zuccheri");
                String immaginePiccolaPath = rs.getString("immaginePiccola");
                String immagineGrandePath = rs.getString("immagineGrande");
                Integer userId = rs.getInt("user_id");

                Alimento alimento = new Alimento(alimentoNome, brand, kcal, proteine, carboidrati, grassi,
                        grassiSaturi, sale, fibre, zuccheri, immaginePiccolaPath, immagineGrandePath, userId, alimentoId);

                giorniDieta.putIfAbsent(idGiornoDieta, new HashMap<>());
                Map<String, List<AlimentoQuantita>> pastiDelGiorno = giorniDieta.get(idGiornoDieta);

                pastiDelGiorno.putIfAbsent(nomePasto, new ArrayList<>());
                List<AlimentoQuantita> alimentiDelPasto = pastiDelGiorno.get(nomePasto);

                alimentiDelPasto.add(new AlimentoQuantita(alimento, quantita));
            }

        } catch (SQLException e) {
            System.err.println("ERRORE SQL (VisualizzaDieta): Durante il recupero dei dettagli della dieta: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRORE GENERICO (VisualizzaDieta): Durante il recupero dei dettagli della dieta: " + e.getMessage());
            e.printStackTrace();
        }
        return giorniDieta;
    }


    /**
     * Popola il VBox 'contenitoreGiorni' dinamicamente con i blocchi UI per ogni giorno e pasto.
     * I giorni sono ordinati per il loro 'id_giorno_dieta' e visualizzati come "Giorno 1", "Giorno 2", ecc.
     * @param giorniDieta La mappa dei dati recuperati dal database.
     */
    private void popolaContenitoreGiorni(Map<Integer, Map<String, List<AlimentoQuantita>>> giorniDieta) {
        contenitoreGiorni.getChildren().clear();

        if (giorniDieta.isEmpty()) {
            Label noDataLabel = new Label("Nessun piano alimentare dettagliato trovato per questa dieta.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888; -fx-padding: 20px;");
            contenitoreGiorni.getChildren().add(noDataLabel);
            return;
        }

        List<Integer> idGiorniOrdinati = new ArrayList<>(giorniDieta.keySet());
        idGiorniOrdinati.sort(Integer::compareTo);

        int contatoreGiorno = 1;

        for (Integer idGiorno : idGiorniOrdinati) {
            Map<String, List<AlimentoQuantita>> pastiDelGiorno = giorniDieta.get(idGiorno);

            VBox giornoBox = new VBox(10);
            giornoBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 15px; -fx-background-color: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            VBox.setMargin(giornoBox, new Insets(0, 0, 20, 0));

            Label giornoLabel = new Label("Giorno " + contatoreGiorno);
            giornoLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            giornoBox.getChildren().add(giornoLabel);

            giornoBox.getChildren().add(new Separator());

            // *** MODIFICA QUI ***
            // Includi i nomi esatti dei pasti come sono nel tuo DB
            // e definisci l'ordine desiderato per la visualizzazione
            List<String> ordinePasti = List.of(
                    "colazione",
                    "spuntinoMattina",    // Il nome esatto dal DB
                    "pranzo",
                    "spuntinoPomeriggio", // Il nome esatto dal DB
                    "cena"
            );

            for (String nomePastoDalDB : ordinePasti) { // Cambiato nome variabile per chiarezza
                if (pastiDelGiorno.containsKey(nomePastoDalDB)) {
                    List<AlimentoQuantita> alimentiDelPasto = pastiDelGiorno.get(nomePastoDalDB);

                    VBox pastoBox = new VBox(5);
                    pastoBox.setPadding(new Insets(5, 0, 5, 10));

                    // *** MODIFICA QUI PER LA VISUALIZZAZIONE ***
                    // Questa logica serve a visualizzare il nome del pasto in un formato più leggibile
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

                    for (AlimentoQuantita aq : alimentiDelPasto) {
                        HBox alimentoHBox = new HBox(10);
                        alimentoHBox.setPadding(new Insets(0, 0, 0, 15));

                        ImageView originalImageView = aq.getAlimento().getImmagine();

                        if (originalImageView != null && originalImageView.getImage() != null) {
                            ImageView clonedImageView = new ImageView(originalImageView.getImage());
                            clonedImageView.setFitWidth(35);
                            clonedImageView.setFitHeight(35);
                            clonedImageView.setPreserveRatio(true);
                            alimentoHBox.getChildren().add(clonedImageView);
                        } else {
                            // Immagine non disponibile. Potresti aggiungere un placeholder.
                        }

                        Label alimentoLabel = new Label("- " + aq.getAlimento().getNome() + " (" + aq.getQuantita() + "g)");
                        alimentoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
                        alimentoHBox.getChildren().add(alimentoLabel);

                        pastoBox.getChildren().add(alimentoHBox);
                    }
                    giornoBox.getChildren().add(pastoBox);
                }
            }
            contenitoreGiorni.getChildren().add(giornoBox);
            contatoreGiorno++;
        }
    }

    private static class AlimentoQuantita {
        private Alimento alimento;
        private double quantita;

        public AlimentoQuantita(Alimento alimento, double quantita) {
            this.alimento = alimento;
            this.quantita = quantita;
        }

        public Alimento getAlimento() { return alimento; }
        public double getQuantita() { return quantita; }
    }
}