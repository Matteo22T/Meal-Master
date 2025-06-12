package com.matteotocci.app.controller;

import com.matteotocci.app.model.Dieta;
import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.Ricetta;
import com.matteotocci.app.model.SQLiteConnessione;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Importazioni per OpenPDF per uno stile migliore
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font; // Importa Font
import com.lowagie.text.Element; // Per allineamento
import com.lowagie.text.Phrase; // Per combinare testo con stili diversi
import com.lowagie.text.Chunk; // Per piccoli frammenti di testo con stili diversi
import com.lowagie.text.pdf.PdfPTable; // Per tabelle
import com.lowagie.text.pdf.PdfPCell; // Per celle della tabella

import java.awt.Color; // Ora useremo solo java.awt.Color

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    public interface DietaItem {
        String getDisplayName();
        String getDisplayQuantity();
        ImageView getDisplayImage();
    }

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
            if (quantita == (long) quantita) {
                return "(" + (long) quantita + "g)";
            }
            return "(" + String.format("%.2f", quantita) + "g)";
        }

        @Override
        public ImageView getDisplayImage() {
            ImageView originalImageView = alimento.getImmagine();
            if (originalImageView != null && originalImageView.getImage() != null) {
                ImageView clonedImageView = new ImageView(originalImageView.getImage());
                clonedImageView.setFitWidth(35);
                clonedImageView.setFitHeight(35);
                clonedImageView.setPreserveRatio(true);
                return clonedImageView;
            }
            return null;
        }
    }

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
            if (quantita == (long) quantita) {
                return "(Ricetta - " + (long) quantita + "g)";
            }
            return "(Ricetta - " + String.format("%.2f", quantita) + "g)";
        }

        @Override
        public ImageView getDisplayImage() {
            return null;
        }
    }


    public void impostaDietaDaVisualizzare(Dieta dieta) {
        this.dietaCorrente = dieta;
        if (dieta == null) {
            System.err.println("ERRORE (VisualizzaDieta): impostaDietaDaVisualizzare - Dieta ricevuta è NULL. Impossibile caricare i dettagli.");
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

        labelNomeDieta.setText(dietaCorrente.getNome());
        labelDataInizio.setText("Data Inizio: " + (dietaCorrente.getDataInizio() != null ? dietaCorrente.getDataInizio() : "N/D"));
        labelDataFine.setText("Data Fine: " + (dietaCorrente.getDataFine() != null ? dietaCorrente.getDataFine() : "N/D"));

        Map<Integer, Map<String, Object>> giorniDietaConNomi = recuperaDettagliCompletiDieta(dietaCorrente.getId());

        if (giorniDietaConNomi.isEmpty()) {
            System.out.println("Nessun alimento o ricetta trovata per questa dieta. Potrebbe essere vuota.");
        }

        popolaContenitoreGiorni(giorniDietaConNomi);
    }

    /**
     * Recupera tutti i dettagli completi di una dieta dal database: giorni, pasti, alimenti e ricette correlate.
     * Ora include il 'nome_giorno' dalla tabella Giorno_dieta e lo memorizza nella mappa.
     * @param idDieta L'ID della dieta per cui recuperare i dettagli.
     * @return Una mappa complessa in cui la chiave è id_giorno_dieta, e il valore è un'altra mappa contenente:
     * - "nomeGiorno": Stringa con il nome del giorno
     * - "pasti": Map<String, List<DietaItem>> con i pasti e gli items
     */
    private Map<Integer, Map<String, Object>> recuperaDettagliCompletiDieta(int idDieta) {
        Map<Integer, Map<String, Object>> giorniDietaMap = new TreeMap<>();
        Connection conn = null;

        try {
            conn = SQLiteConnessione.connector();

            // Query per gli ALIMENTI
            String sqlAlimenti = "SELECT " +
                    "  gd.id_giorno_dieta, " +
                    "  gd.nome_giorno, " +
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
                    "       END";

            try (PreparedStatement pstmtAlimenti = conn.prepareStatement(sqlAlimenti)) {
                pstmtAlimenti.setInt(1, idDieta);
                ResultSet rsAlimenti = pstmtAlimenti.executeQuery();

                while (rsAlimenti.next()) {
                    int idGiornoDieta = rsAlimenti.getInt("id_giorno_dieta");
                    String nomeGiorno = rsAlimenti.getString("nome_giorno");
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

                    giorniDietaMap.putIfAbsent(idGiornoDieta, new HashMap<>());
                    Map<String, Object> giornoData = giorniDietaMap.get(idGiornoDieta);

                    giornoData.putIfAbsent("nomeGiorno", nomeGiorno);
                    giornoData.putIfAbsent("pasti", new HashMap<String, List<DietaItem>>());

                    @SuppressWarnings("unchecked")
                    Map<String, List<DietaItem>> pastiDelGiorno = (Map<String, List<DietaItem>>) giornoData.get("pasti");

                    pastiDelGiorno.putIfAbsent(nomePasto, new ArrayList<>());
                    pastiDelGiorno.get(nomePasto).add(new AlimentoQuantita(alimento, quantita));
                }
            }

            // Query per le RICETTE
            String sqlRicette = "SELECT " +
                    "  gd.id_giorno_dieta, " +
                    "  gd.nome_giorno, " +
                    "  dr.pasto, " +
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
                    "       CASE dr.pasto " +
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
                    String nomeGiorno = rsRicette.getString("nome_giorno");
                    String nomePasto = rsRicette.getString("pasto");
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

                    giorniDietaMap.putIfAbsent(idGiornoDieta, new HashMap<>());
                    Map<String, Object> giornoData = giorniDietaMap.get(idGiornoDieta);

                    giornoData.putIfAbsent("nomeGiorno", nomeGiorno);
                    giornoData.putIfAbsent("pasti", new HashMap<String, List<DietaItem>>());


                    @SuppressWarnings("unchecked")
                    Map<String, List<DietaItem>> pastiDelGiorno = (Map<String, List<DietaItem>>) giornoData.get("pasti");

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
        return giorniDietaMap;
    }


    /**
     * Popola il VBox 'contenitoreGiorni' dinamicamente con i blocchi UI per ogni giorno e pasto.
     * Ora usa il 'nome_giorno' effettivo recuperato dal database, memorizzato nella mappa.
     * @param giorniDietaMap La mappa dei dati recuperati dal database, contenente sia alimenti che ricette,
     * con l'aggiunta dell'informazione del nome del giorno.
     */
    private void popolaContenitoreGiorni(Map<Integer, Map<String, Object>> giorniDietaMap) {
        contenitoreGiorni.getChildren().clear();

        if (giorniDietaMap.isEmpty()) {
            Label noDataLabel = new Label("Nessun piano alimentare dettagliato trovato per questa dieta.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888; -fx-padding: 20px;");
            contenitoreGiorni.getChildren().add(noDataLabel);
            return;
        }

        List<Integer> idGiorniOrdinati = new ArrayList<>(giorniDietaMap.keySet());
        idGiorniOrdinati.sort(Integer::compareTo);

        for (Integer idGiorno : idGiorniOrdinati) {
            Map<String, Object> giornoData = giorniDietaMap.get(idGiorno);
            String nomeGiorno = (String) giornoData.get("nomeGiorno");

            @SuppressWarnings("unchecked")
            Map<String, List<DietaItem>> pastiDelGiorno = (Map<String, List<DietaItem>>) giornoData.get("pasti");


            VBox giornoBox = new VBox(10);
            giornoBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 15px; -fx-background-color: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            VBox.setMargin(giornoBox, new Insets(0, 0, 20, 0));

            Label giornoLabel = new Label(nomeGiorno);
            giornoLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            giornoBox.getChildren().add(giornoLabel);

            giornoBox.getChildren().add(new Separator());

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
                            Label placeholder = new Label("🍽");
                            placeholder.setStyle("-fx-font-size: 20px; -fx-alignment: center; -fx-pref-width: 35px; -fx-pref-height: 35px;");
                            itemHBox.getChildren().add(placeholder);
                        }

                        Label itemLabel = new Label("- " + item.getDisplayName() + " " + item.getDisplayQuantity());
                        itemLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
                        itemHBox.getChildren().add(itemLabel); // <-- CORREZIONELOGICA QUI

                        pastoBox.getChildren().add(itemHBox);
                    }
                    giornoBox.getChildren().add(pastoBox);
                }
            }
            contenitoreGiorni.getChildren().add(giornoBox);
        }
    }

    // --- NUOVO METODO PER SCARICARE IL PDF ---
    @FXML
    private void handleScaricaPdf(ActionEvent event) {
        if (dietaCorrente == null) {
            showAlert("Errore", "Nessuna dieta caricata da scaricare.");
            return;
        }

        Map<Integer, Map<String, Object>> giorniDietaConNomi = recuperaDettagliCompletiDieta(dietaCorrente.getId());

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Dieta PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (.pdf)", ".pdf"));
        fileChooser.setInitialFileName("Dieta_" + dietaCorrente.getNome().replaceAll("\\s+", "_") + ".pdf");

        File file = fileChooser.showSaveDialog(((javafx.scene.Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                // Definizioni di font e colori
                Font fontTitle = new Font(Font.HELVETICA, 24, Font.BOLD, new Color(44, 62, 80)); // Grigio scuro per i titoli
                Font fontSubtitle = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(52, 73, 94)); // Blu scuro
                Font fontSection = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(192, 57, 43)); // Rosso per sezioni
                Font fontNormal = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK);
                Font fontItem = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(85, 85, 85));
                Font fontPasto = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(70, 96, 117)); // Verde scuro per i pasti

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Intestazione generale della dieta
                Paragraph pTitle = new Paragraph(dietaCorrente.getNome(), fontTitle);
                pTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(pTitle);
                document.add(new Paragraph("\n"));

                // Date
                Paragraph pDates = new Paragraph();
                pDates.setAlignment(Element.ALIGN_CENTER);
                pDates.add(new Chunk("Data Inizio: ", fontSubtitle));
                pDates.add(new Chunk(dietaCorrente.getDataInizio() != null ? dietaCorrente.getDataInizio() : "N/D", fontNormal));
                pDates.add(new Chunk("    Data Fine: ", fontSubtitle));
                pDates.add(new Chunk(dietaCorrente.getDataFine() != null ? dietaCorrente.getDataFine() : "N/D", fontNormal));
                document.add(pDates);
                document.add(new Paragraph("\n\n")); // Spazio extra

                // Sezione "Piani Giornalieri"
                Paragraph pSectionTitle = new Paragraph("Piani Giornalieri", fontSection);
                pSectionTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(pSectionTitle);
                document.add(new Paragraph("\n"));

                List<Integer> idGiorniOrdinati = new ArrayList<>(giorniDietaConNomi.keySet());
                idGiorniOrdinati.sort(Integer::compareTo);

                for (Integer idGiorno : idGiorniOrdinati) {
                    Map<String, Object> giornoData = giorniDietaConNomi.get(idGiorno);
                    String nomeGiorno = (String) giornoData.get("nomeGiorno");


                    Map<String, List<DietaItem>> pastiDelGiorno = (Map<String, List<DietaItem>>) giornoData.get("pasti");

                    // Titolo del giorno
                    Paragraph pGiorno = new Paragraph(nomeGiorno.toUpperCase(), fontSubtitle);
                    pGiorno.setAlignment(Element.ALIGN_LEFT);
                    pGiorno.setSpacingAfter(5);
                    document.add(pGiorno);
                    document.add(new Paragraph("--------------------------------------------------------------------------------------------------------------------", fontNormal)); // Separatore
                    document.add(new Paragraph("\n"));


                    List<String> ordinePasti = List.of(
                            "colazione", "spuntinoMattina", "pranzo", "spuntinoPomeriggio", "cena"
                    );


                    PdfPTable tableGiorno = new PdfPTable(1);
                    tableGiorno.setWidthPercentage(100);
                    tableGiorno.setSpacingAfter(15);


                    for (String nomePastoDalDB : ordinePasti) {
                        if (pastiDelGiorno.containsKey(nomePastoDalDB)) {
                            List<DietaItem> itemsDelPasto = pastiDelGiorno.get(nomePastoDalDB);
                            itemsDelPasto.sort(Comparator.comparing(DietaItem::getDisplayName));

                            String nomePastoVisualizzato;
                            switch (nomePastoDalDB) {
                                case "spuntinoMattina":
                                    nomePastoVisualizzato = "Spuntino Mattina";
                                    break;
                                case "spuntinoPomeriggio":
                                    nomePastoVisualizzato = "Spuntino Pomeriggio";
                                    break;
                                default:
                                    nomePastoVisualizzato = nomePastoDalDB.substring(0, 1).toUpperCase() + nomePastoDalDB.substring(1);
                                    break;
                            }

                            // Cella per il nome del pasto
                            PdfPCell cellPastoTitle = new PdfPCell(new Phrase(nomePastoVisualizzato + ":", fontPasto));
                            cellPastoTitle.setBorder(0);
                            cellPastoTitle.setPaddingBottom(5);
                            tableGiorno.addCell(cellPastoTitle);

                            for (DietaItem item : itemsDelPasto) {
                                PdfPCell cellItem = new PdfPCell(new Phrase("  - " + item.getDisplayName() + " " + item.getDisplayQuantity(), fontItem));
                                cellItem.setBorder(0);
                                cellItem.setPaddingLeft(20); // Indenta gli item
                                tableGiorno.addCell(cellItem);
                            }
                            // Aggiungi un po' di spazio dopo ogni pasto
                            PdfPCell spacerCell = new PdfPCell(new Phrase("\n"));
                            spacerCell.setBorder(0);
                            spacerCell.setPaddingBottom(5);
                            tableGiorno.addCell(spacerCell);
                        }
                    }
                    document.add(tableGiorno);
                }
                document.close();
                showAlert("Successo", "Dieta salvata come PDF con successo!");
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
                showAlert("Errore", "Errore durante la generazione o il salvataggio del PDF: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

