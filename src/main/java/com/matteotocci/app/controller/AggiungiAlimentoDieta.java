package com.matteotocci.app.controller; // Dichiara il package a cui appartiene questa classe.

// Importa le classi necessarie per l'applicazione JavaFX e le operazioni del database
import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.Ricetta;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation; // Importa la classe Orientation per specificare l'orientamento di elementi come le ScrollBar.
import javafx.scene.Node; // Importa la classe base per tutti i nodi nel grafo della scena (elementi UI).
import javafx.scene.Parent; // Importa il nodo base per la gerarchia della scena (container di tutti gli elementi UI).
import javafx.scene.Scene;
import javafx.scene.control.*; // Importa tutti i controlli UI standard di JavaFX (Button, ComboBox, CheckBox, TableView, TextField, Spinner, etc.).
import javafx.scene.control.cell.PropertyValueFactory; // Importa la classe per collegare le proprietà degli oggetti alle colonne di una TableView.
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException; // Importa l'eccezione per errori di input/output (es. caricamento file FXML).
import java.net.URL; // Importa la classe URL, necessaria per Initializable.
import java.sql.Connection; // Importa l'interfaccia per la connessione al database.
import java.sql.PreparedStatement; // Importa la classe per eseguire query SQL precompilate.
import java.sql.ResultSet; // Importa la classe per leggere i risultati delle query SQL.
import java.sql.SQLException;
import java.sql.Statement; // Importa la classe per eseguire query SQL semplici.
import java.util.ResourceBundle; // Importa la classe ResourceBundle, necessaria per Initializable.

public class AggiungiAlimentoDieta implements Initializable {

    // Colonne della TableView per gli Alimenti
    @FXML private TableColumn<Alimento, ImageView> immagineCol;
    @FXML private TableColumn<Alimento, String> nomeCol, brandCol;
    @FXML private TableColumn<Alimento, Double> calorieCol, proteineCol, carboidratiCol, grassiCol;
    @FXML private TableColumn<Alimento, Double> grassiSatCol, saleCol, fibreCol, zuccheriCol;

    // Bottoni per la ricerca
    @FXML private Button ButtonCercaAlimento;
    @FXML private Button ButtonCercaRicetta;

    // ComboBox per la selezione della categoria (Alimenti e Ricette)
    @FXML private ComboBox<String> ComboBoxAlimento;
    @FXML private ComboBox<String> ComboBoxRicetta;

    // CheckBox per filtrare "Solo i miei" alimenti/ricette
    @FXML private CheckBox CheckBoxAlimenti;
    @FXML private CheckBox CheckBoxRicette;

    // Bottoni di conferma per aggiungere alla dieta
    @FXML private Button confermaAlimentiButton;
    @FXML private Button confermaRicetteButton;

    // TableView per visualizzare alimenti e ricette
    @FXML private TableView<Alimento> tableViewAlimenti;
    @FXML private TableView<Ricetta> tableViewRicette;

    // Campi di testo per la ricerca per nome
    @FXML private TextField textCercaAlimento;
    @FXML private TextField textCercaRicetta;

    // Colonne della TableView per le Ricette
    @FXML private TableColumn<Ricetta, String> nomeColRic;
    @FXML private TableColumn<Ricetta, String> descrizioneColRic;
    @FXML private TableColumn<Ricetta, String> categoriaColRic;

    // Spinner per la selezione della quantità
    @FXML private Spinner<Integer> quantitaSpinner;

    // --- Variabili di stato interne ---
    private int offset = 0; // Inizializza l'offset per la paginazione delle ricerche (quanti elementi saltare).
    private final int LIMIT = 50; // Definisce il numero massimo di elementi da caricare per volta (per paginazione).

    // Riferimenti ad altri controller per la comunicazione
    private AggiungiGiornoDieta giornoDietaController;
    private String pastoCorrente; // Dichiara una variabile per memorizzare il tipo di pasto corrente (es. "Colazione").

    @FXML private Button btnRicette;
    @FXML private Button btnAlimenti;


    // --- Metodi di comunicazione tra controller ---

    public void setGiornoDietaController(AggiungiGiornoDieta controller) {
        this.giornoDietaController = controller;
    }

    public void setPastoCorrente(String pasto) {
        this.pastoCorrente = pasto;
    }

    @FXML
    private void mostraTabellaAlimenti(ActionEvent event) { // Metodo per mostrare la tabella degli alimenti, collegato ad un'azione FXML.
        highlightButton(btnAlimenti, btnRicette); // Chiama un metodo per evidenziare il bottone "Alimenti" e disattivare "Ricette".

        tableViewAlimenti.setVisible(true);
        tableViewRicette.setVisible(false);
        confermaAlimentiButton.setVisible(true);
        confermaRicetteButton.setVisible(false);
        textCercaAlimento.setVisible(true);
        textCercaRicetta.setVisible(false);
        ButtonCercaAlimento.setVisible(true);
        ButtonCercaRicetta.setVisible(false);
        ComboBoxAlimento.setVisible(true);
        ComboBoxRicetta.setVisible(false);
        CheckBoxAlimenti.setVisible(true);
        CheckBoxRicette.setVisible(false);
    }
    @FXML
    private void mostraTabellaRicette(ActionEvent event) { // Metodo per mostrare la tabella delle ricette, collegato ad un'azione FXML.
        highlightButton(btnRicette, btnAlimenti); // Chiama un metodo per evidenziare il bottone "Ricette" e disattivare "Alimenti".


        tableViewAlimenti.setVisible(false);
        tableViewRicette.setVisible(true);
        confermaAlimentiButton.setVisible(false);
        confermaRicetteButton.setVisible(true);
        textCercaAlimento.setVisible(false);
        textCercaRicetta.setVisible(true);
        ButtonCercaAlimento.setVisible(false);
        ButtonCercaRicetta.setVisible(true);
        ComboBoxAlimento.setVisible(false);
        ComboBoxRicetta.setVisible(true);
        CheckBoxAlimenti.setVisible(false);
        CheckBoxRicette.setVisible(true);

        // Quando si passa alla visualizzazione delle ricette, ricaricale
        offset = 0; // Reset dell'offset per iniziare una nuova ricerca dall'inizio.
        tableViewRicette.getItems().clear(); // Pulisci la tabella delle ricette.
        // Forza la selezione per mostrare le proprie ricette se l'utente è loggato
        if (Session.getUserId() != null) { // Controlla se l'ID utente della sessione non è nullo (utente loggato).
            CheckBoxRicette.setSelected(true); // Seleziona la checkbox "Solo le mie ricette".
        } else { // Altrimenti (utente non loggato).
            CheckBoxRicette.setSelected(false); // Deseleziona la checkbox.
        }
        cercaRicette(textCercaRicetta.getText(), false); // Avvia la ricerca delle ricette (non in append mode).
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) { // Implementazione del metodo initialize dell'interfaccia Initializable.
        // Associa le proprietà dell'oggetto Alimento alle colonne della TableView degli Alimenti
        immagineCol.setCellValueFactory(new PropertyValueFactory<>("immagine"));
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        calorieCol.setCellValueFactory(new PropertyValueFactory<>("kcal"));
        proteineCol.setCellValueFactory(new PropertyValueFactory<>("proteine"));
        carboidratiCol.setCellValueFactory(new PropertyValueFactory<>("carboidrati"));
        grassiCol.setCellValueFactory(new PropertyValueFactory<>("grassi"));
        grassiSatCol.setCellValueFactory(new PropertyValueFactory<>("grassiSaturi"));
        saleCol.setCellValueFactory(new PropertyValueFactory<>("sale"));
        fibreCol.setCellValueFactory(new PropertyValueFactory<>("fibre"));
        zuccheriCol.setCellValueFactory(new PropertyValueFactory<>("zuccheri"));

        // Associa le proprietà dell'oggetto Ricetta alle colonne della TableView delle Ricette
        nomeColRic.setCellValueFactory(new PropertyValueFactory<>("nome"));
        descrizioneColRic.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        categoriaColRic.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        // Configura il comportamento di double-click sulle righe della tabella degli Alimenti
        tableViewAlimenti.setRowFactory(tv -> { // Imposta una factory per creare le righe della tabella degli alimenti.
            TableRow<Alimento> row = new TableRow<>(); // Crea una nuova riga della tabella.
            row.setOnMouseClicked(event -> { // Imposta un gestore di eventi per il click del mouse sulla riga.
                if (event.getClickCount() == 2 && !row.isEmpty()) { // Controlla se è un doppio click e se la riga non è vuota.
                    Alimento alimento = row.getItem(); // Ottiene l'oggetto Alimento associato alla riga.
                    apriDettaglio(alimento); // Chiama il metodo per aprire la finestra di dettaglio per l'alimento.
                }
            });
            return row;
        });

        // Configura il comportamento di double-click sulla tabella delle Ricette (gestito da un metodo separato)
        tableViewRicette.setOnMouseClicked(this::apriDettaglioRicetta); // Imposta un gestore di eventi per il click del mouse sulla tabella ricette.

        popolaCategorie(); // Chiama il metodo per popolare le ComboBox delle categorie per alimenti e ricette.

        // Inizializza lo Spinner per la selezione della quantità

        quantitaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100)); // Imposta la factory di valori per lo Spinner.
        quantitaSpinner.setEditable(true); // Rende lo Spinner modificabile dall'utente.

        // --- Listener per la ricerca e il filtro ---
        // Ogni volta che la selezione nella ComboBox Alimento cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca degli alimenti.
        ComboBoxAlimento.setOnAction(e -> { // Imposta un gestore di eventi quando la selezione della ComboBox Alimento cambia.
            offset = 0; // Resetta l'offset di paginazione.
            tableViewAlimenti.getItems().clear(); // Pulisce gli elementi della tabella alimenti.
            cercaAlimenti(textCercaAlimento.getText(), false); // Esegue la ricerca degli alimenti (non in append mode).
        });

        // Ogni volta che lo stato della CheckBox Alimenti cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca degli alimenti.
        CheckBoxAlimenti.setOnAction(e -> { // Imposta un gestore di eventi quando lo stato della CheckBox Alimenti cambia.
            offset = 0; // Resetta l'offset di paginazione.
            tableViewAlimenti.getItems().clear(); // Pulisce gli elementi della tabella alimenti.
            cercaAlimenti(textCercaAlimento.getText(), false); // Esegue la ricerca degli alimenti (non in append mode).
        });

        // Ogni volta che la selezione nella ComboBox Ricetta cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca delle ricette.
        ComboBoxRicetta.setOnAction(e -> { // Imposta un gestore di eventi quando la selezione della ComboBox Ricetta cambia.
            offset = 0;
            tableViewRicette.getItems().clear(); // Pulisce gli elementi della tabella ricette.
            cercaRicette(textCercaRicetta.getText(), false); // Esegue la ricerca delle ricette (non in append mode).
        });

        // Ogni volta che lo stato della CheckBox Ricette cambia, resetta l'offset,
        // pulisce la tabella e riesegue la ricerca delle ricette.
        CheckBoxRicette.setOnAction(e -> { // Imposta un gestore di eventi quando lo stato della CheckBox Ricette cambia.
            offset = 0;
            tableViewRicette.getItems().clear(); // Pulisce gli elementi della tabella ricette.
            cercaRicette(textCercaRicetta.getText(), false); // Esegue la ricerca delle ricette (non in append mode).
        });

        // --- Gestione dello scroll per la paginazione infinita ---
        // Questo codice viene eseguito sul thread di JavaFX UI dopo l'inizializzazione completa
        Platform.runLater(() -> {
            // Ottiene la ScrollBar verticale della tabella alimenti
            ScrollBar scrollBar = getVerticalScrollbar(tableViewAlimenti); // Ottiene la scrollbar verticale della tabella alimenti.
            if (scrollBar != null) {
                // Aggiunge un listener alla proprietà 'value' della scrollbar
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> { // Aggiunge un listener al valore della scrollbar.
                    // Se il valore della scrollbar raggiunge il massimo (cioè, l'utente ha scrollato fino in fondo)
                    if (newVal.doubleValue() == scrollBar.getMax()) {
                        caricaAltriAlimenti();
                    }
                });
            }
            // Ripete la stessa logica per la tabella ricette
            ScrollBar scrollBar2 = getVerticalScrollbar(tableViewRicette); // Ottiene la scrollbar verticale della tabella ricette.
            if (scrollBar2 != null) {
                scrollBar2.valueProperty().addListener((obs, oldVal, newVal) -> { // Aggiunge un listener al valore della scrollbar.
                    if (newVal.doubleValue() == scrollBar2.getMax()) {
                        caricaAltreRicette();
                    }
                });
            }
        });

        // Carica inizialmente gli alimenti (senza filtro di ricerca e senza append)
        cercaAlimenti("", false); // Carica inizialmente gli alimenti senza filtro e non in append mode.

        if (Session.getUserId() != null) {
            CheckBoxRicette.setSelected(true); // Seleziona la checkbox "Solo le mie ricette".
        }
        // Carica inizialmente le ricette (senza filtro di ricerca e senza append)
        // La condizione della checkbox "Solo le mie ricette" verrà applicata da cercaRicette()
        cercaRicette("", false); // Carica inizialmente le ricette senza filtro e non in append mode.

        highlightButton(btnAlimenti, btnRicette); // Chiama il metodo per evidenziare il bottone degli alimenti all'avvio.

    }

    // --- Metodi di ricerca ---

    /**
     * Gestisce l'evento di click sul bottone "Cerca" per gli alimenti.
     * Resetta l'offset e la tabella, poi esegue una nuova ricerca.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleCercaAlimento(ActionEvent event) { // Metodo per gestire il click sul bottone di ricerca alimenti.
        offset = 0;
        tableViewAlimenti.getItems().clear(); // Pulisce tutti gli elementi dalla tabella degli alimenti.
        String filtro = textCercaAlimento.getText(); // Ottiene il testo inserito nel campo di ricerca alimenti.
        cercaAlimenti(filtro, false); // Chiama il metodo per cercare gli alimenti con il filtro specificato e non in modalità append.
    }

    /**
     * Esegue la ricerca degli alimenti nel database.
     * @param filtro Il testo da cercare nel nome dell'alimento.
     * @param append Se true, aggiunge i nuovi risultati alla lista esistente; altrimenti, crea una nuova lista.
     */
    private void cercaAlimenti(String filtro, boolean append) {
        ObservableList<Alimento> alimenti = append ? tableViewAlimenti.getItems() : FXCollections.observableArrayList();

        String categoria = ComboBoxAlimento.getSelectionModel().getSelectedItem();
        boolean soloMiei = CheckBoxAlimenti.isSelected();

        StringBuilder query = new StringBuilder("SELECT * FROM foods WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?");
        }
        if (soloMiei && Session.getUserId() != null) {
            query.append(" AND user_id = ?");
        }
        query.append(" LIMIT ? OFFSET ?");

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%");
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria);
            }
            if (soloMiei && Session.getUserId() != null) {
                stmt.setInt(paramIndex++, Session.getUserId());
            }
            stmt.setInt(paramIndex++, LIMIT);
            stmt.setInt(paramIndex++, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alimenti.add(new Alimento(
                            rs.getString("nome"),
                            rs.getString("brand"),
                            rs.getDouble("kcal"),
                            rs.getDouble("proteine"),
                            rs.getDouble("carboidrati"),
                            rs.getDouble("grassi"),
                            rs.getDouble("grassiSaturi"),
                            rs.getDouble("sale"),
                            rs.getDouble("fibre"),
                            rs.getDouble("zuccheri"),
                            rs.getString("immaginePiccola"),
                            rs.getString("immagineGrande"),
                            rs.getInt("user_id"),
                            rs.getInt("id")
                    ));
                }

                tableViewAlimenti.setItems(alimenti);
                offset += LIMIT;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isLoading = false; // Flag per prevenire carichi multipli durante lo scroll.

    /**
     * Carica altri alimenti quando l'utente raggiunge il fondo della scrollbar.
     * Utilizza il flag `isLoading` per evitare richieste duplicate.
     */
    private void caricaAltriAlimenti() { // Metodo per caricare più alimenti durante lo scroll.
        if (isLoading) return; // Se un caricamento è già in corso, esce dal metodo per evitare duplicati.
        isLoading = true;
        String filtro = textCercaAlimento.getText();
        cercaAlimenti(filtro, true);
        isLoading = false; // Resetta il flag una volta che il caricamento è completato.
    }

    private boolean isLoading2 = false; // Flag per prevenire carichi multipli per le ricette.

    /**
     * Carica altre ricette quando l'utente raggiunge il fondo della scrollbar.
     * @see #caricaAltriAlimenti()
     */
    private void caricaAltreRicette() { // Metodo per caricare più ricette durante lo scroll.
        if (isLoading2) return; // Se un caricamento è già in corso, esce dal metodo.
        isLoading2 = true;
        String filtro = textCercaRicetta.getText();
        cercaRicette(filtro, true);
        isLoading2 = false; // Resetta il flag una volta che il caricamento è completato.
    }

    /**
     * Gestisce l'evento di click sul bottone "Cerca" per le ricette.
     * Resetta l'offset e la tabella, poi esegue una nuova ricerca.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleCercaRicetta(ActionEvent event) {
        offset = 0;
        tableViewRicette.getItems().clear();
        cercaRicette(textCercaRicetta.getText(), false);
    }

    public void cercaRicette(String filtro, boolean append) {
        ObservableList<Ricetta> ricette = append ? tableViewRicette.getItems() : FXCollections.observableArrayList();

        String categoria = ComboBoxRicetta.getSelectionModel().getSelectedItem();
        boolean soloMiei = CheckBoxRicette.isSelected();

        StringBuilder query = new StringBuilder("SELECT * FROM Ricette WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?");
        }
        if (soloMiei && Session.getUserId() != null) {
            query.append(" AND id_utente = ?");
        }
        query.append(" LIMIT ? OFFSET ?");

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%");
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria);
            }
            if (soloMiei && Session.getUserId() != null) {
                stmt.setInt(paramIndex++, Session.getUserId());
            }
            stmt.setInt(paramIndex++, LIMIT);
            stmt.setInt(paramIndex++, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ricetta ricetta = new Ricetta(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descrizione"),
                            rs.getString("categoria"),
                            rs.getInt("id_utente"),
                            rs.getDouble("kcal"),
                            rs.getDouble("proteine"),
                            rs.getDouble("carboidrati"),
                            rs.getDouble("grassi"),
                            rs.getDouble("grassi_saturi"),
                            rs.getDouble("zuccheri"),
                            rs.getDouble("fibre"),
                            rs.getDouble("sale")
                    );
                    ricette.add(ricetta);
                }

                tableViewRicette.setItems(ricette);
                offset += LIMIT;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void apriDettaglioRicetta(MouseEvent event) {
        if (event.getClickCount() == 2 && !tableViewRicette.getSelectionModel().isEmpty()) {
            Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettaglioRicetta.fxml"));
                Parent root = loader.load();

                DettaglioRicettaController controller = loader.getController();
                controller.setRicetta(ricettaSelezionata);
                controller.setOrigineFXML("AggiungiAlimentoDieta.fxml");

                Stage stage = new Stage();
                stage.setTitle("Dettaglio Ricetta");
                stage.setScene(new Scene(root));
                stage.setFullScreen(false);
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ScrollBar getVerticalScrollbar(TableView<?> table) { // Metodo per ottenere la scrollbar verticale di una TableView.
        // Cerca tra tutti i nodi con la classe CSS ".scroll-bar"
        for (Node node : table.lookupAll(".scroll-bar")) { // Itera su tutti i nodi figli della tabella che hanno la classe CSS ".scroll-bar".
            if (node instanceof ScrollBar) { // Controlla se il nodo corrente è un'istanza di ScrollBar.
                ScrollBar sb = (ScrollBar) node; // Effettua il casting del nodo a ScrollBar.
                if (sb.getOrientation() == Orientation.VERTICAL) { // Controlla se l'orientamento della scrollbar è verticale.
                    return sb; // Restituisce la scrollbar verticale trovata.
                }
            }
        }
        return null; // Restituisce null se nessuna scrollbar verticale è stata trovata.
    }

    private void highlightButton(Button active, Button inactive) { // Metodo per evidenziare un bottone e disattivare un altro.
        // Se il bottone attivo ha già lo stile "bottoneAttivo", non fare nulla per evitare refresh inutili
        if(active.getStyleClass().contains("bottoneAttivo")) { // Controlla se il bottone attivo ha già lo stile "bottoneAttivo".
            return;
        }
        // Rimuove gli stili "bottoneSpento" dal bottone attivo e "bottoneAttivo" dal bottone inattivo
        active.getStyleClass().remove("bottoneSpento");
        inactive.getStyleClass().remove("bottoneAttivo");

        // Aggiunge gli stili "bottoneAttivo" al bottone attivo e "bottoneSpento" al bottone inattivo
        active.getStyleClass().add("bottoneAttivo");
        inactive.getStyleClass().add("bottoneSpento");
    }

    private void apriDettaglio(Alimento alimento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettagliAlimento.fxml"));
            Parent root = loader.load();

            DettagliAlimentoController controller = loader.getController();
            controller.setAlimento(alimento);
            controller.setOrigineFXML("AggiungAlimentoDieta.fxml");

            Stage stage = new Stage();
            stage.setTitle("Dettaglio Alimento");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setFullScreen(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void popolaCategorie() { // Metodo per popolare le ComboBox delle categorie.
        // Query per le categorie degli alimenti
        String queryAlimenti = "SELECT DISTINCT categoria FROM foods WHERE categoria IS NOT NULL";
        try (Connection conn = SQLiteConnessione.connector();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryAlimenti)) {

            ObservableList<String> categorieAlimenti = FXCollections.observableArrayList(); // Crea una nuova lista osservabile per le categorie degli alimenti.
            categorieAlimenti.add("Tutte"); // Aggiunge l'opzione "Tutte" come prima voce.
            while (rs.next()) {
                categorieAlimenti.add(rs.getString("categoria")); // Aggiunge la categoria corrente alla lista.
            }
            ComboBoxAlimento.setItems(categorieAlimenti); // Imposta gli elementi nella ComboBox degli alimenti.
            ComboBoxAlimento.getSelectionModel().selectFirst(); // Seleziona il primo elemento ("Tutte") di default.
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Popola ComboBoxRicetta con categorie dalla tabella Ricette
        ObservableList<String> categorieRicetteList = FXCollections.observableArrayList(); // Crea una nuova lista osservabile per le categorie delle ricette.
        categorieRicetteList.add("Tutte"); // Aggiunge "Tutte" come prima opzione.

        String queryRicette = "SELECT DISTINCT categoria FROM Ricette WHERE categoria IS NOT NULL"; // Definisce la query SQL per ottenere le categorie distinte dalle ricette.
        try (Connection conn = SQLiteConnessione.connector();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryRicette)) {
            while (rs.next()) {
                categorieRicetteList.add(rs.getString("categoria")); // Aggiunge la categoria corrente alla lista.
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ComboBoxRicetta.setItems(categorieRicetteList); // Imposta gli elementi nella ComboBox delle ricette.
        ComboBoxRicetta.getSelectionModel().selectFirst(); // Seleziona "Tutte" di default.
    }

    @FXML
    private void confermaAlimenti(ActionEvent event) {
        Alimento alimentoSelezionato = tableViewAlimenti.getSelectionModel().getSelectedItem();
        Integer quantita = quantitaSpinner.getValue();

        if (alimentoSelezionato != null && giornoDietaController != null && pastoCorrente != null && quantita != null && quantita > 0) {
            giornoDietaController.aggiungiAlimentoAllaLista(pastoCorrente, alimentoSelezionato, quantita);
        } else {
            System.out.println("Seleziona un alimento e specifica una quantità valida!");
        }
    }

    @FXML
    private void confermaRicette(ActionEvent event) {
        Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem();
        Integer quantita = quantitaSpinner.getValue();

        if (ricettaSelezionata != null && giornoDietaController != null && pastoCorrente != null && quantita != null && quantita > 0) {
            giornoDietaController.aggiungiRicettaAllaLista(ricettaSelezionata, quantita, pastoCorrente);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } else {
            System.out.println("Seleziona una ricetta e specifica una quantità valida!");
        }
    }


    private Alimento getAlimentoFromRicetta(Ricetta ricetta) { // Metodo per ottenere un oggetto Alimento da un oggetto Ricetta.
        // Query per recuperare i dettagli di un alimento con lo stesso nome della ricetta
        String query = "SELECT * FROM foods WHERE nome = ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ricetta.getNome());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Alimento(
                        rs.getString("nome"),
                        rs.getString("brand"),
                        rs.getDouble("kcal"),
                        rs.getDouble("proteine"),
                        rs.getDouble("carboidrati"),
                        rs.getDouble("grassi"),
                        rs.getDouble("grassiSaturi"),
                        rs.getDouble("sale"),
                        rs.getDouble("fibre"),
                        rs.getDouble("zuccheri"),
                        rs.getString("immaginePiccola"),
                        rs.getString("immagineGrande"),
                        rs.getInt("user_id"),
                        rs.getInt("id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
        }
        return null;
    }
}
