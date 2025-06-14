package com.matteotocci.app.controller; // Dichiarazione del package in cui si trova la classe del controller.

// Importazioni delle classi e interfacce necessarie dai package model e JavaFX.
import com.matteotocci.app.model.Dieta; // Classe per la gestione delle diete.
import com.matteotocci.app.model.Alimento; // Classe per la gestione degli alimenti.
import com.matteotocci.app.model.Ricetta; // Classe per la gestione delle ricette.
import com.matteotocci.app.model.SQLiteConnessione; // Classe per la gestione della connessione al database SQLite.

import javafx.event.ActionEvent; // Classe per la gestione degli eventi di azione (es. click su un bottone).
import javafx.fxml.FXML; // Annotazione per iniettare componenti FXML nel controller.
import javafx.geometry.Insets; // Classe per definire i margini e i padding.
import javafx.scene.control.Alert; // Classe per visualizzare finestre di avviso.
import javafx.scene.control.Label; // Componente UI per visualizzare testo.
import javafx.scene.control.Separator; // Componente UI per una linea di separazione.
import javafx.scene.image.ImageView; // Componente UI per visualizzare immagini.
import javafx.scene.layout.HBox; // Layout container che organizza i figli in orizzontale.
import javafx.scene.layout.VBox; // Layout container che organizza i figli in verticale.
import javafx.stage.FileChooser; // Classe per aprire una finestra di dialogo per la selezione/salvataggio di file.
import javafx.stage.Stage; // Finestra principale dell'applicazione.

// Importazioni specifiche per OpenPDF (una libreria per la creazione di PDF).
import com.lowagie.text.Document; // Rappresenta il documento PDF.
import com.lowagie.text.DocumentException; // Eccezione per errori di documenti PDF.
import com.lowagie.text.Paragraph; // Elemento di testo che rappresenta un paragrafo.
import com.lowagie.text.pdf.PdfWriter; // Scrive il documento PDF in un OutputStream.
import com.lowagie.text.Font; // Classe per la gestione dei font nel PDF.
import com.lowagie.text.Element; // Interfaccia per gli elementi del documento PDF (per allineamento).
import com.lowagie.text.Phrase; // Combina Chunk con stili diversi.
import com.lowagie.text.Chunk; // Piccolo frammento di testo con un proprio stile.
import com.lowagie.text.pdf.PdfPTable; // Tabella nel documento PDF.
import com.lowagie.text.pdf.PdfPCell; // Cella di una tabella PDF.

import java.awt.Color; // Utilizzo di java.awt.Color per i colori in OpenPDF.

import java.io.File; // Classe per la gestione dei file.
import java.io.FileOutputStream; // Output stream per scrivere su un file.
import java.io.IOException; // Eccezione per errori di I/O.
import java.net.URL; // Classe per rappresentare un URL (usata per caricare risorse come i CSS).
import java.sql.Connection; // Interfaccia per una connessione al database.
import java.sql.PreparedStatement; // Interfaccia per query SQL precompilate.
import java.sql.ResultSet; // Interfaccia per i risultati di una query SQL.
import java.sql.SQLException; // Eccezione per errori SQL.
import java.util.ArrayList; // Implementazione di List basata su array ridimensionabile.
import java.util.Comparator; // Interfaccia per il confronto di oggetti.
import java.util.HashMap; // Implementazione di Map basata su hash table.
import java.util.List; // Interfaccia per una collezione ordinata.
import java.util.Map; // Interfaccia per una collezione di coppie chiave-valore.
import java.util.TreeMap; // Implementazione di Map che ordina le sue chiavi.

// Dichiarazione della classe VisualizzaDieta.
public class VisualizzaDieta {

    // Campi annotati con @FXML per l'iniezione dei componenti UI definiti nel file FXML.
    @FXML private Label labelNomeDieta; // Label per il nome della dieta.
    @FXML private Label labelDataInizio; // Label per la data di inizio della dieta.
    @FXML private Label labelDataFine; // Label per la data di fine della dieta.
    @FXML private VBox contenitoreGiorni; // Contenitore per i VBox di ogni giorno della dieta.

    private Dieta dietaCorrente; // Oggetto Dieta attualmente visualizzato.

    // Interfaccia funzionale per rappresentare un elemento visualizzabile della dieta (alimento o ricetta).
    public interface DietaItem {
        String getDisplayName(); // Ottiene il nome da visualizzare.
        String getDisplayQuantity(); // Ottiene la quantità da visualizzare (con unità di misura).
        ImageView getDisplayImage(); // Ottiene l'immagine da visualizzare (può essere null per le ricette).
    }

    // Classe interna che implementa DietaItem per gli alimenti.
    private static class AlimentoQuantita implements DietaItem {
        private Alimento alimento; // L'oggetto Alimento.
        private double quantita; // La quantità in grammi dell'alimento.

        // Costruttore.
        public AlimentoQuantita(Alimento alimento, double quantita) {
            this.alimento = alimento;
            this.quantita = quantita;
        }

        public Alimento getAlimento() { return alimento; } // Getter per l'alimento.
        public double getQuantita() { return quantita; } // Getter per la quantità.

        @Override
        public String getDisplayName() {
            return alimento.getNome(); // Restituisce il nome dell'alimento.
        }

        @Override
        public String getDisplayQuantity() {
            // Formatta la quantità in grammi, gestendo interi e decimali.
            if (quantita == (long) quantita) {
                return "(" + (long) quantita + "g)";
            }
            return "(" + String.format("%.2f", quantita) + "g)";
        }

        @Override
        public ImageView getDisplayImage() {
            // Crea un'immagine clonata dell'immagine dell'alimento con dimensioni specifiche.
            ImageView originalImageView = alimento.getImmagine();
            if (originalImageView != null && originalImageView.getImage() != null) {
                ImageView clonedImageView = new ImageView(originalImageView.getImage());
                clonedImageView.setFitWidth(35);
                clonedImageView.setFitHeight(35);
                clonedImageView.setPreserveRatio(true); // Mantiene le proporzioni.
                return clonedImageView;
            }
            return null; // Nessuna immagine se non disponibile.
        }
    }

    // Classe interna che implementa DietaItem per le ricette.
    private static class RicettaQuantita implements DietaItem {
        private Ricetta ricetta; // L'oggetto Ricetta.
        private double quantita; // La quantità in grammi della ricetta.

        // Costruttore.
        public RicettaQuantita(Ricetta ricetta, double quantita) {
            this.ricetta = ricetta;
            this.quantita = quantita;
        }

        public Ricetta getRicetta() { return ricetta; } // Getter per la ricetta.
        public double getQuantita() { return quantita; } // Getter per la quantità.

        @Override
        public String getDisplayName() {
            return ricetta.getNome(); // Restituisce il nome della ricetta.
        }

        @Override
        public String getDisplayQuantity() {
            // Formatta la quantità in grammi per la ricetta.
            if (quantita == (long) quantita) {
                return "(Ricetta - " + (long) quantita + "g)";
            }
            return "(Ricetta - " + String.format("%.2f", quantita) + "g)";
        }

        @Override
        public ImageView getDisplayImage() {
            return null; // Le ricette non hanno un'immagine associata in questo contesto.
        }
    }

    // Metodo pubblico per impostare la dieta da visualizzare. Chiamato dal controller che apre questa vista.
    public void impostaDietaDaVisualizzare(Dieta dieta) {
        this.dietaCorrente = dieta; // Assegna la dieta ricevuta.
        if (dieta == null) {
            System.err.println("ERRORE (VisualizzaDieta): impostaDietaDaVisualizzare - Dieta ricevuta è NULL. Impossibile caricare i dettagli.");
        }
        caricaEVisualizzaDettagliDieta(); // Avvia il caricamento e la visualizzazione dei dettagli.
    }

    // Metodo privato per caricare e visualizzare i dettagli della dieta nell'interfaccia utente.
    private void caricaEVisualizzaDettagliDieta() {
        if (dietaCorrente == null) {
            System.err.println("ERRORE (VisualizzaDieta): caricaEVisualizzaDettagliDieta chiamato con dietaCorrente NULL. Uscita.");
            return;
        }

        // Imposta i testi delle label con le informazioni della dieta.
        labelNomeDieta.setText(dietaCorrente.getNome());
        labelDataInizio.setText("Data Inizio: " + (dietaCorrente.getDataInizio() != null ? dietaCorrente.getDataInizio() : "N/D"));
        labelDataFine.setText("Data Fine: " + (dietaCorrente.getDataFine() != null ? dietaCorrente.getDataFine() : "N/D"));

        // Recupera i dettagli completi della dieta dal database.
        Map<Integer, Map<String, Object>> giorniDietaConNomi = recuperaDettagliCompletiDieta(dietaCorrente.getId());

        if (giorniDietaConNomi.isEmpty()) {
            System.out.println("Nessun alimento o ricetta trovata per questa dieta. Potrebbe essere vuota.");
        }

        popolaContenitoreGiorni(giorniDietaConNomi); // Popola il contenitore UI con i dati recuperati.
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
        Map<Integer, Map<String, Object>> giorniDietaMap = new TreeMap<>(); // Usa TreeMap per mantenere i giorni ordinati per ID.
        Connection conn = null;

        try {
            conn = SQLiteConnessione.connector(); // Ottiene la connessione al database.

            // Query SQL per recuperare gli ALIMENTI associati alla dieta.
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
                    "ORDER BY gd.id_giorno_dieta, " + // Ordina per giorno della dieta
                    "       CASE da.pasto " + // Ordina i pasti in un ordine specifico (colazione, spuntino, pranzo, ecc.)
                    "         WHEN 'colazione' THEN 1 " +
                    "         WHEN 'spuntinoMattina' THEN 2 " +
                    "         WHEN 'pranzo' THEN 3 " +
                    "         WHEN 'spuntinoPomeriggio' THEN 4 " +
                    "         WHEN 'cena' THEN 5 " +
                    "         ELSE 6 " +
                    "       END";

            // Esegue la query per gli alimenti.
            try (PreparedStatement pstmtAlimenti = conn.prepareStatement(sqlAlimenti)) {
                pstmtAlimenti.setInt(1, idDieta); // Imposta l'ID della dieta.
                ResultSet rsAlimenti = pstmtAlimenti.executeQuery(); // Esegue la query.

                while (rsAlimenti.next()) { // Itera sui risultati degli alimenti.
                    int idGiornoDieta = rsAlimenti.getInt("id_giorno_dieta");
                    String nomeGiorno = rsAlimenti.getString("nome_giorno");
                    String nomePasto = rsAlimenti.getString("pasto");
                    double quantita = rsAlimenti.getDouble("quantita_grammi");

                    // Crea un oggetto Alimento con i dati dal ResultSet.
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

                    // Aggiunge i dati alla mappa giorniDietaMap.
                    giorniDietaMap.putIfAbsent(idGiornoDieta, new HashMap<>()); // Se il giorno non esiste, lo aggiunge.
                    Map<String, Object> giornoData = giorniDietaMap.get(idGiornoDieta); // Ottiene la mappa per il giorno corrente.

                    giornoData.putIfAbsent("nomeGiorno", nomeGiorno); // Memorizza il nome del giorno.
                    giornoData.putIfAbsent("pasti", new HashMap<String, List<DietaItem>>()); // Se i pasti non esistono, li aggiunge.

                    // Cast sicuro per recuperare la mappa dei pasti.
                    @SuppressWarnings("unchecked")
                    Map<String, List<DietaItem>> pastiDelGiorno = (Map<String, List<DietaItem>>) giornoData.get("pasti");

                    pastiDelGiorno.putIfAbsent(nomePasto, new ArrayList<>()); // Se il pasto non esiste, lo aggiunge.
                    pastiDelGiorno.get(nomePasto).add(new AlimentoQuantita(alimento, quantita)); // Aggiunge l'alimento al pasto.
                }
            }

            // Query SQL per recuperare le RICETTE associate alla dieta.
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
                    "ORDER BY gd.id_giorno_dieta, " + // Ordina per giorno della dieta
                    "       CASE dr.pasto " + // Ordina i pasti in un ordine specifico.
                    "         WHEN 'colazione' THEN 1 " +
                    "         WHEN 'spuntinoMattina' THEN 2 " +
                    "         WHEN 'pranzo' THEN 3 " +
                    "         WHEN 'spuntinoPomeriggio' THEN 4 " +
                    "         WHEN 'cena' THEN 5 " +
                    "         ELSE 6 " +
                    "       END";

            // Esegue la query per le ricette.
            try (PreparedStatement pstmtRicette = conn.prepareStatement(sqlRicette)) {
                pstmtRicette.setInt(1, idDieta); // Imposta l'ID della dieta.
                ResultSet rsRicette = pstmtRicette.executeQuery(); // Esegue la query.

                while (rsRicette.next()) { // Itera sui risultati delle ricette.
                    int idGiornoDieta = rsRicette.getInt("id_giorno_dieta");
                    String nomeGiorno = rsRicette.getString("nome_giorno");
                    String nomePasto = rsRicette.getString("pasto");
                    double quantita = rsRicette.getDouble("quantita_grammi");

                    // Crea un oggetto Ricetta con i dati dal ResultSet.
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

                    // Aggiunge i dati alla mappa giorniDietaMap, in modo simile agli alimenti.
                    giorniDietaMap.putIfAbsent(idGiornoDieta, new HashMap<>());
                    Map<String, Object> giornoData = giorniDietaMap.get(idGiornoDieta);

                    giornoData.putIfAbsent("nomeGiorno", nomeGiorno);
                    giornoData.putIfAbsent("pasti", new HashMap<String, List<DietaItem>>());

                    @SuppressWarnings("unchecked")
                    Map<String, List<DietaItem>> pastiDelGiorno = (Map<String, List<DietaItem>>) giornoData.get("pasti");

                    pastiDelGiorno.putIfAbsent(nomePasto, new ArrayList<>());
                    pastiDelGiorno.get(nomePasto).add(new RicettaQuantita(ricetta, quantita)); // Aggiunge la ricetta al pasto.
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
                if (conn != null) conn.close(); // Chiude la connessione al database.
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return giorniDietaMap; // Restituisce la mappa completa dei dettagli della dieta.
    }

    /**
     * Popola il VBox 'contenitoreGiorni' dinamicamente con i blocchi UI per ogni giorno e pasto.
     * Ora usa il 'nome_giorno' effettivo recuperato dal database, memorizzato nella mappa.
     * @param giorniDietaMap La mappa dei dati recuperati dal database, contenente sia alimenti che ricette,
     * con l'aggiunta dell'informazione del nome del giorno.
     */
    private void popolaContenitoreGiorni(Map<Integer, Map<String, Object>> giorniDietaMap) {
        contenitoreGiorni.getChildren().clear(); // Pulisce i contenuti precedenti del VBox.

        if (giorniDietaMap.isEmpty()) { // Se non ci sono dati, mostra un messaggio.
            Label noDataLabel = new Label("Nessun piano alimentare dettagliato trovato per questa dieta.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888; -fx-padding: 20px;");
            contenitoreGiorni.getChildren().add(noDataLabel);
            return;
        }

        // Ordina gli ID dei giorni per visualizzare i giorni in ordine corretto.
        List<Integer> idGiorniOrdinati = new ArrayList<>(giorniDietaMap.keySet());
        idGiorniOrdinati.sort(Integer::compareTo);

        for (Integer idGiorno : idGiorniOrdinati) { // Itera su ogni giorno della dieta.
            Map<String, Object> giornoData = giorniDietaMap.get(idGiorno);
            String nomeGiorno = (String) giornoData.get("nomeGiorno"); // Recupera il nome del giorno.

            @SuppressWarnings("unchecked")
            Map<String, List<DietaItem>> pastiDelGiorno = (Map<String, List<DietaItem>>) giornoData.get("pasti");

            // Crea un VBox per il giorno corrente e applica stili.
            VBox giornoBox = new VBox(10);
            giornoBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 15px; -fx-background-color: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            VBox.setMargin(giornoBox, new Insets(0, 0, 20, 0));

            // Label per il nome del giorno.
            Label giornoLabel = new Label(nomeGiorno);
            giornoLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            giornoBox.getChildren().add(giornoLabel);

            giornoBox.getChildren().add(new Separator()); // Aggiunge un separatore.

            // Definisce l'ordine dei pasti per la visualizzazione.
            List<String> ordinePasti = List.of(
                    "colazione",
                    "spuntinoMattina",
                    "pranzo",
                    "spuntinoPomeriggio",
                    "cena"
            );

            for (String nomePastoDalDB : ordinePasti) { // Itera su ogni pasto nell'ordine definito.
                if (pastiDelGiorno.containsKey(nomePastoDalDB)) { // Se il pasto esiste per questo giorno.
                    List<DietaItem> itemsDelPasto = pastiDelGiorno.get(nomePastoDalDB);

                    itemsDelPasto.sort(Comparator.comparing(DietaItem::getDisplayName)); // Ordina gli item del pasto alfabeticamente.

                    VBox pastoBox = new VBox(5);
                    pastoBox.setPadding(new Insets(5, 0, 5, 10));

                    // Traduce il nome del pasto dal DB a un nome visualizzabile.
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

                    // Label per il nome del pasto.
                    Label pastoLabel = new Label(nomePastoVisualizzato);
                    pastoLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
                    pastoBox.getChildren().add(pastoLabel);

                    for (DietaItem item : itemsDelPasto) { // Itera su ogni alimento/ricetta del pasto.
                        HBox itemHBox = new HBox(10);
                        itemHBox.setPadding(new Insets(0, 0, 0, 15));

                        ImageView displayImage = item.getDisplayImage(); // Ottiene l'immagine dell'item.
                        if (displayImage != null) {
                            itemHBox.getChildren().add(displayImage); // Aggiunge l'immagine se disponibile.
                        } else {
                            // Se non c'è immagine (es. per le ricette), usa un placeholder grafico.
                            Label placeholder = new Label("🍽");
                            placeholder.setStyle("-fx-font-size: 20px; -fx-alignment: center; -fx-pref-width: 35px; -fx-pref-height: 35px;");
                            itemHBox.getChildren().add(placeholder);
                        }

                        // Label per il nome e la quantità dell'item.
                        Label itemLabel = new Label("- " + item.getDisplayName() + " " + item.getDisplayQuantity());
                        itemLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
                        itemHBox.getChildren().add(itemLabel); // Aggiunge la label all'HBox.

                        pastoBox.getChildren().add(itemHBox); // Aggiunge l'HBox dell'item al VBox del pasto.
                    }
                    giornoBox.getChildren().add(pastoBox); // Aggiunge il VBox del pasto al VBox del giorno.
                }
            }
            contenitoreGiorni.getChildren().add(giornoBox); // Aggiunge il VBox del giorno al contenitore principale.
        }
    }

    // --- NUOVO METODO PER SCARICARE IL PDF ---
    @FXML
    private void handleScaricaPdf(ActionEvent event) {
        if (dietaCorrente == null) { // Controlla se una dieta è stata caricata.
            showAlert(Alert.AlertType.ERROR ,"Errore", "Nessuna dieta caricata da scaricare.");
            return;
        }

        // Recupera di nuovo i dettagli completi della dieta per il PDF.
        Map<Integer, Map<String, Object>> giorniDietaConNomi = recuperaDettagliCompletiDieta(dietaCorrente.getId());

        // Configura il FileChooser per il salvataggio del file PDF.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Dieta PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (.pdf)", ".pdf"));
        fileChooser.setInitialFileName("Dieta_" + dietaCorrente.getNome().replaceAll("\\s+", "_") + ".pdf"); // Nome file suggerito.

        // Mostra la finestra di dialogo per il salvataggio.
        File file = fileChooser.showSaveDialog(((javafx.scene.Node) event.getSource()).getScene().getWindow());

        if (file != null) { // Se l'utente ha scelto un file.
            try {
                // Definizioni di font e colori per il PDF utilizzando OpenPDF.
                Font fontTitle = new Font(Font.HELVETICA, 24, Font.BOLD, new Color(44, 62, 80)); // Titolo dieta.
                Font fontSubtitle = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(52, 73, 94)); // Sottotitoli (es. date).
                Font fontSection = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(192, 57, 43)); // Titolo sezioni (es. Piani Giornalieri).
                Font fontNormal = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK); // Testo normale.
                Font fontItem = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(85, 85, 85)); // Dettagli item.
                Font fontPasto = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(70, 96, 117)); // Nomi pasti.

                Document document = new Document(); // Crea un nuovo documento PDF.
                PdfWriter.getInstance(document, new FileOutputStream(file)); // Collega il writer al file di output.
                document.open(); // Apre il documento per la scrittura.

                // Aggiunge l'intestazione generale della dieta al PDF.
                Paragraph pTitle = new Paragraph(dietaCorrente.getNome(), fontTitle);
                pTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(pTitle);
                document.add(new Paragraph("\n"));

                // Aggiunge le date di inizio e fine dieta.
                Paragraph pDates = new Paragraph();
                pDates.setAlignment(Element.ALIGN_CENTER);
                pDates.add(new Chunk("Data Inizio: ", fontSubtitle));
                pDates.add(new Chunk(dietaCorrente.getDataInizio() != null ? dietaCorrente.getDataInizio() : "N/D", fontNormal));
                pDates.add(new Chunk("    Data Fine: ", fontSubtitle));
                pDates.add(new Chunk(dietaCorrente.getDataFine() != null ? dietaCorrente.getDataFine() : "N/D", fontNormal));
                document.add(pDates);
                document.add(new Paragraph("\n\n")); // Spazio extra.

                // Aggiunge la sezione "Piani Giornalieri".
                Paragraph pSectionTitle = new Paragraph("Piani Giornalieri", fontSection);
                pSectionTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(pSectionTitle);
                document.add(new Paragraph("\n"));

                // Ordina gli ID dei giorni per la visualizzazione nel PDF.
                List<Integer> idGiorniOrdinati = new ArrayList<>(giorniDietaConNomi.keySet());
                idGiorniOrdinati.sort(Integer::compareTo);

                for (Integer idGiorno : idGiorniOrdinati) { // Itera su ogni giorno.
                    Map<String, Object> giornoData = giorniDietaConNomi.get(idGiorno);
                    String nomeGiorno = (String) giornoData.get("nomeGiorno");

                    @SuppressWarnings("unchecked")
                    Map<String, List<DietaItem>> pastiDelGiorno = (Map<String, List<DietaItem>>) giornoData.get("pasti");

                    // Titolo del giorno nel PDF.
                    Paragraph pGiorno = new Paragraph(nomeGiorno.toUpperCase(), fontSubtitle);
                    pGiorno.setAlignment(Element.ALIGN_LEFT);
                    pGiorno.setSpacingAfter(5);
                    document.add(pGiorno);
                    // Separatore sotto il titolo del giorno.
                    document.add(new Paragraph("--------------------------------------------------------------------------------------------------------------------", fontNormal));
                    document.add(new Paragraph("\n"));

                    // Definisce l'ordine dei pasti anche per il PDF.
                    List<String> ordinePasti = List.of(
                            "colazione", "spuntinoMattina", "pranzo", "spuntinoPomeriggio", "cena"
                    );

                    // Crea una tabella per contenere i pasti e i loro item.
                    PdfPTable tableGiorno = new PdfPTable(1); // Una colonna.
                    tableGiorno.setWidthPercentage(100); // Occupa il 100% della larghezza.
                    tableGiorno.setSpacingAfter(15); // Spazio dopo la tabella.

                    for (String nomePastoDalDB : ordinePasti) { // Itera sui pasti.
                        if (pastiDelGiorno.containsKey(nomePastoDalDB)) {
                            List<DietaItem> itemsDelPasto = pastiDelGiorno.get(nomePastoDalDB);
                            itemsDelPasto.sort(Comparator.comparing(DietaItem::getDisplayName)); // Ordina gli item.

                            // Traduce il nome del pasto per la visualizzazione nel PDF.
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

                            // Cella per il nome del pasto nella tabella.
                            PdfPCell cellPastoTitle = new PdfPCell(new Phrase(nomePastoVisualizzato + ":", fontPasto));
                            cellPastoTitle.setBorder(0); // Nessun bordo.
                            cellPastoTitle.setPaddingBottom(5);
                            tableGiorno.addCell(cellPastoTitle);

                            for (DietaItem item : itemsDelPasto) { // Itera sugli item del pasto.
                                // Cella per l'item (alimento o ricetta).
                                PdfPCell cellItem = new PdfPCell(new Phrase("  - " + item.getDisplayName() + " " + item.getDisplayQuantity(), fontItem));
                                cellItem.setBorder(0); // Nessun bordo.
                                cellItem.setPaddingLeft(20); // Indentazione.
                                tableGiorno.addCell(cellItem);
                            }
                            // Aggiunge un po' di spazio dopo ogni pasto.
                            PdfPCell spacerCell = new PdfPCell(new Phrase("\n"));
                            spacerCell.setBorder(0);
                            spacerCell.setPaddingBottom(5);
                            tableGiorno.addCell(spacerCell);
                        }
                    }
                    document.add(tableGiorno); // Aggiunge la tabella del giorno al documento.
                }
                document.close(); // Chiude il documento PDF.
                showAlert(Alert.AlertType.INFORMATION,"Successo", "Dieta salvata come PDF con successo!");
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR,"Errore", "Errore durante la generazione o il salvataggio del PDF: " + e.getMessage());
            }
        }
    }

    // Metodo privato per visualizzare gli alert (finestre di avviso).
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType); // Crea una nuova istanza di Alert.
        alert.setTitle(title); // Imposta il titolo.
        alert.setHeaderText(null); // Non mostra un header text.
        alert.setContentText(message); // Imposta il contenuto.

        // Cerca il file CSS per lo stile personalizzato degli alert.
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            // Se il CSS viene trovato, lo aggiunge al DialogPane dell'alert.
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base.
            // Aggiunge una classe di stile specifica in base al tipo di alert per una maggiore personalizzazione.
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Messaggio di errore se il CSS non è trovato.
        }

        alert.showAndWait(); // Mostra l'avviso e attende che l'utente lo chiuda.
    }

}