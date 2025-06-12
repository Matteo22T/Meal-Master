package com.matteotocci.app.controller;

// Importa le classi necessarie per l'applicazione JavaFX e le operazioni del database
import com.matteotocci.app.model.Alimento; // Modello per l'oggetto Alimento
import com.matteotocci.app.model.IngredienteRicetta; // Modello per l'oggetto IngredienteRicetta
import com.matteotocci.app.model.SQLiteConnessione; // Classe per gestire la connessione al database SQLite
import com.matteotocci.app.model.Session; // Classe per la gestione della sessione utente
import javafx.application.Platform; // Per eseguire codice sul thread di UI di JavaFX
import javafx.beans.property.SimpleStringProperty; // Utilizzato per PropertyValueFactory di String
import javafx.collections.FXCollections; // Utility per creare collezioni osservabili
import javafx.collections.ObservableList; // Lista che notifica i "listener" quando avvengono dei cambiamenti
import javafx.event.ActionEvent; // Tipo di evento generato dalle azioni dell'utente (es. click su un bottone)
import javafx.fxml.FXML; // Annotazione per collegare elementi dell'interfaccia utente definiti in FXML al codice Java
import javafx.fxml.FXMLLoader; // Carica file FXML (layout dell'interfaccia utente)
import javafx.fxml.Initializable; // Interfaccia per i controller che devono essere inizializzati dopo il caricamento dell'FXML
import javafx.geometry.Orientation; // Per specificare l'orientamento di elementi come le ScrollBar
import javafx.scene.Node; // Classe base per tutti i nodi nel grafo della scena (elementi UI)
import javafx.scene.Parent; // Nodo base per la gerarchia della scena (container di tutti gli elementi UI)
import javafx.scene.Scene; // Contenitore per tutti i contenuti di una scena
import javafx.scene.control.*; // Controlli UI standard di JavaFX (Button, TextField, ComboBox, TableView, Label, TextArea, CheckBox, Alert)
import javafx.scene.control.cell.PropertyValueFactory; // Per collegare le proprietà degli oggetti alle colonne di una TableView
import javafx.scene.image.ImageView; // Per visualizzare immagini
import javafx.scene.layout.HBox; // Layout container orizzontale
import javafx.stage.Stage; // La finestra dell'applicazione
import javafx.util.Callback; // Utilizzato per le CellFactory delle TableColumn

import java.io.IOException; // Eccezione per errori di input/output (es. caricamento file FXML)
import java.net.URL; // Necessario per Initializable
import java.sql.*; // Classi per l'interazione con il database
import java.util.ResourceBundle; // Necessario per Initializable

/**
 * Controller per la schermata "Aggiungi Ricetta".
 * Questa classe gestisce l'interfaccia utente per la creazione di nuove ricette,
 * permettendo l'aggiunta di alimenti come ingredienti, il calcolo automatico dei
 * valori nutrizionali totali e il salvataggio della ricetta nel database.
 */
public class AggiungiRicetteController implements Initializable { // Implementa l'interfaccia Initializable

    // --- Elementi dell'interfaccia utente (collegati tramite @FXML) ---
    @FXML private Button aggiungiAlimento; // Bottone per aggiungere l'alimento selezionato alla ricetta
    @FXML private Button annulla; // Bottone per annullare l'operazione e chiudere la finestra
    @FXML private TextField cercaAlimento; // Campo di testo per la ricerca di alimenti nella tabella
    @FXML private TextField nomeRicetta; // Campo di testo per il nome della ricetta
    @FXML private TextField quantitaField; // Campo di testo per la quantità dell'ingrediente da aggiungere
    @FXML private Button salvaRicetta; // Bottone per salvare la ricetta nel database
    @FXML private ComboBox<String> categoriaComboBox; // ComboBox per la selezione della categoria degli alimenti
    @FXML private CheckBox mieiAlimentiCheckBox; // CheckBox per filtrare gli alimenti "Solo i miei"
    @FXML private ComboBox<String> categoriaRicetta; // ComboBox per la selezione della categoria della ricetta
    @FXML private TextArea descrizioneRicetta; // Area di testo per la descrizione della ricetta

    @FXML private TableView<Alimento> tableView; // TableView per visualizzare la lista degli alimenti disponibili
    @FXML private TableColumn<Alimento, ImageView> immagineCol; // Colonna immagine per la TableView alimenti
    @FXML private TableColumn<Alimento, String> nomeCol, brandCol; // Colonne nome e brand per la TableView alimenti
    @FXML private TableColumn<Alimento, Double> calorieCol, proteineCol, carboidratiCol, grassiCol; // Colonne macronutrienti per la TableView alimenti
    @FXML private TableColumn<Alimento, Double> grassiSatCol, saleCol, fibreCol, zuccheriCol; // Colonne altri valori nutrizionali per la TableView alimenti

    @FXML private Label calorieTotaliLabel; // Etichetta per le calorie totali della ricetta
    @FXML private Label carboidratiTotaliLabel; // Etichetta per i carboidrati totali della ricetta
    @FXML private Label fibreTotaliLabel; // Etichetta per le fibre totali della ricetta
    @FXML private Label grassiSaturiTotaliLabel; // Etichetta per i grassi saturi totali della ricetta
    @FXML private Label grassiTotaliLabel; // Etichetta per i grassi totali della ricetta
    @FXML private Label proteineTotaliLabel; // Etichetta per le proteine totali della ricetta
    @FXML private Label saleTotaliLabel; // Etichetta per il sale totale della ricetta
    @FXML private Label zuccheriTotaliLabel; // Etichetta per gli zuccheri totali della ricetta

    @FXML private TableView<IngredienteRicetta> ingredientiTable; // TableView per visualizzare gli ingredienti aggiunti alla ricetta
    @FXML private TableColumn<IngredienteRicetta, String> ingredienteNomeCol; // Colonna nome ingrediente per la TableView ingredienti
    @FXML private TableColumn<IngredienteRicetta, Void> azioniCol; // Colonna per le azioni (modifica/elimina) degli ingredienti
    @FXML private TableColumn<IngredienteRicetta, Number> quantitaCol; // Colonna quantità per la TableView ingredienti

    // --- Variabili di stato per la paginazione e i totali nutrizionali ---
    private int offset = 0; // Offset per la paginazione delle ricerche nella tabella alimenti
    private final int LIMIT = 50; // Numero massimo di alimenti da caricare per volta

    // Liste osservabili per la gestione degli ingredienti e valori nutrizionali
    private ObservableList<IngredienteRicetta> ingredienti = FXCollections.observableArrayList(); // Lista degli ingredienti della ricetta corrente
    private double totKcal = 0, totProteine = 0, totCarboidrati = 0, totGrassi = 0,
            totGrassiSaturi = 0, totSale = 0, totFibre = 0, totZuccheri = 0; // Variabili per i totali nutrizionali

    // --- Metodo di inizializzazione della UI ---

    /**
     * Metodo di inizializzazione chiamato automaticamente da FXMLLoader dopo che
     * tutti gli elementi FXML sono stati caricati e iniettati nel controller.
     * Qui vengono configurate le colonne delle TableView, i listener, e la logica
     * iniziale per il caricamento dei dati e la gestione dello scroll.
     *
     * @param location L'URL del documento FXML che ha dato origine a questo controller.
     * @param resources Le risorse utilizzate per localizzare gli oggetti radice, o null se la radice non è stata localizzata.
     */
    @Override // Indica che questo metodo fa l'override di un metodo dell'interfaccia Initializable
    public void initialize(URL location, ResourceBundle resources) {
        // Associa le proprietà dell'oggetto Alimento alle colonne della TableView degli alimenti
        immagineCol.setCellValueFactory(new PropertyValueFactory<>("immagine"));
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        calorieCol.setCellValueFactory(new PropertyValueFactory<>("kcal"));
        proteineCol.setCellValueFactory(new PropertyValueFactory<>("proteine"));
        carboidratiCol.setCellValueFactory(new PropertyValueFactory<>("carboidrati"));
        grassiCol.setCellValueFactory(new PropertyValueFactory<>("grasi")); // ATTENZIONE: C'è un typo qui, dovrebbe essere "grassi"
        grassiSatCol.setCellValueFactory(new PropertyValueFactory<>("grassiSaturi"));
        saleCol.setCellValueFactory(new PropertyValueFactory<>("sale"));
        fibreCol.setCellValueFactory(new PropertyValueFactory<>("fibre"));
        zuccheriCol.setCellValueFactory(new PropertyValueFactory<>("zuccheri"));

        // Configura il comportamento di double-click sulle righe della tabella degli alimenti
        tableView.setRowFactory(tv -> {
            TableRow<Alimento> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) { // Se c'è un double-click e la riga non è vuota
                    Alimento alimento = row.getItem(); // Ottieni l'alimento della riga
                    apriDettaglio(alimento); // Apri la finestra di dettaglio per l'alimento
                }
            });
            return row;
        });

        popolaCategorie(); // Popola le ComboBox delle categorie

        // Listener per la selezione della categoria degli alimenti: resetta offset, pulisce tabella, cerca alimenti
        categoriaComboBox.setOnAction(e -> {
            offset = 0;
            tableView.getItems().clear();
            cercaAlimenti(cercaAlimento.getText(), false);
        });

        // Listener per la checkbox "Solo i miei alimenti": resetta offset, pulisce tabella, cerca alimenti
        mieiAlimentiCheckBox.setOnAction(e -> {
            offset = 0;
            tableView.getItems().clear();
            cercaAlimenti(cercaAlimento.getText(), false);
        });

        // Configura la tabella degli ingredienti della ricetta
        ingredienteNomeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlimento().getNome()));
        quantitaCol.setCellValueFactory(data -> data.getValue().quantitaProperty());
        ingredientiTable.setItems(ingredienti); // Collega la lista degli ingredienti alla tabella

        // Configura la colonna delle azioni (modifica/elimina)
        azioniCol.setCellFactory(getAzioneCellFactory());

        // Gestione dello scroll per la paginazione infinita della tabella degli alimenti
        Platform.runLater(() -> {
            ScrollBar scrollBar = getVerticalScrollbar(tableView);
            if (scrollBar != null) {
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == scrollBar.getMax()) {
                        // Se raggiunge il fondo, carica altri 50 alimenti
                        caricaAltri();
                    }
                });
            }
        });

        cercaAlimenti("", false); // Carica gli alimenti iniziali all'avvio della schermata
    }

    // --- Metodi di ricerca e caricamento dati ---

    /**
     * Gestisce l'azione del bottone "Cerca Alimento".
     * Resetta l'offset e la tabella, poi esegue una nuova ricerca di alimenti.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleCercaAlimento(ActionEvent event) {
        offset = 0; // Resetta l'offset per iniziare la ricerca dall'inizio
        tableView.getItems().clear(); // Pulisce la tabella degli alimenti
        String filtro = cercaAlimento.getText(); // Ottiene il testo del filtro di ricerca
        cercaAlimenti(filtro, false); // Esegue la ricerca (non in append mode)
    }

    /**
     * Esegue la ricerca degli alimenti nel database.
     * @param filtro Il testo da cercare nel nome dell'alimento.
     * @param append Se true, aggiunge i nuovi risultati alla lista esistente; altrimenti, crea una nuova lista.
     */
    private void cercaAlimenti(String filtro, boolean append) {
        // Se `append` è true, usa la lista esistente della tabella; altrimenti, crea una nuova lista
        ObservableList<Alimento> alimenti = append ? tableView.getItems() : FXCollections.observableArrayList();

        String categoria = categoriaComboBox.getSelectionModel().getSelectedItem(); // Ottiene la categoria selezionata
        boolean soloMiei = mieiAlimentiCheckBox.isSelected(); // Controlla lo stato della checkbox "Solo i miei"

        // Costruisce dinamicamente la query SQL
        StringBuilder query = new StringBuilder("SELECT * FROM foods WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?"); // Aggiunge la condizione per la categoria se selezionata
        }
        if (soloMiei && Session.getUserId() != null) {
            query.append(" AND user_id = ?"); // Aggiunge la condizione per l'ID utente se la checkbox è selezionata
        }
        query.append(" LIMIT ? OFFSET ?"); // Aggiunge LIMIT e OFFSET per la paginazione

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione al database
             PreparedStatement stmt = conn.prepareStatement(query.toString())) { // Prepara lo statement

            int paramIndex = 1; // Indice per i parametri della query
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%"); // Imposta il filtro di ricerca (case-insensitive)
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria); // Imposta il parametro categoria
            }
            if (soloMiei && Session.getUserId() != null) {
                stmt.setInt(paramIndex++, Session.getUserId()); // Imposta il parametro user_id
            }
            stmt.setInt(paramIndex++, LIMIT); // Imposta il limite di risultati
            stmt.setInt(paramIndex++, offset); // Imposta l'offset

            try (ResultSet rs = stmt.executeQuery()) { // Esegue la query e ottiene i risultati
                while (rs.next()) { // Itera su ogni riga del ResultSet
                    // Crea un nuovo oggetto Alimento con i dati dal database e lo aggiunge alla lista
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

                tableView.setItems(alimenti); // Aggiorna gli elementi della TableView
                offset += LIMIT; // Incrementa l'offset per la prossima paginazione
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare gli alimenti dal database.");
        }
    }

    /**
     * Metodo helper per ottenere la ScrollBar verticale di una TableView.
     * Utilizzato per implementare la paginazione basata sullo scroll.
     * @param table La TableView da cui ottenere la scrollbar.
     * @return La ScrollBar verticale o null se non trovata.
     */
    private ScrollBar getVerticalScrollbar(TableView<?> table) {
        // Cerca tra tutti i nodi con la classe CSS ".scroll-bar"
        for (Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) { // Se il nodo è una ScrollBar
                ScrollBar sb = (ScrollBar) node;
                if (sb.getOrientation() == Orientation.VERTICAL) { // E se è verticale
                    return sb; // La restituisce
                }
            }
        }
        return null; // Nessuna scrollbar verticale trovata
    }

    private boolean isLoading = false; // Flag per prevenire carichi multipli durante lo scroll

    /**
     * Carica altri alimenti quando l'utente raggiunge il fondo della scrollbar.
     * Utilizza il flag `isLoading` per evitare richieste duplicate.
     */
    private void caricaAltri() {
        if (isLoading) return; // Se è già in corso un caricamento, esci
        isLoading = true; // Imposta il flag per indicare che un caricamento è in corso
        String filtro = cercaAlimento.getText(); // Ottiene il filtro corrente
        cercaAlimenti(filtro, true); // Esegue la ricerca in modalità append
        isLoading = false; // Resetta il flag al termine del caricamento
    }

    /**
     * Popola le ComboBox delle categorie degli alimenti e delle categorie per le ricette.
     */
    private void popolaCategorie() {
        // Popola la ComboBox delle categorie alimenti
        String query = "SELECT DISTINCT categoria FROM foods WHERE categoria IS NOT NULL";
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione
             Statement stmt = conn.createStatement(); // Crea uno statement
             ResultSet rs = stmt.executeQuery(query)) { // Esegue la query

            ObservableList<String> categorie = FXCollections.observableArrayList();
            categorie.add("Tutte"); // Aggiunge l'opzione "Tutte"
            while (rs.next()) {
                categorie.add(rs.getString("categoria")); // Aggiunge le categorie dal database
            }
            categoriaComboBox.setItems(categorie); // Imposta gli elementi nella ComboBox
            categoriaComboBox.getSelectionModel().selectFirst(); // Seleziona il primo elemento ("Tutte")
        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare le categorie alimenti.");
        }

        // Popola la ComboBox delle categorie ricetta (con categorie prefissate o dal DB se esistenti)
        ObservableList<String> categoriePrefissate = FXCollections.observableArrayList(
                "Colazione", "Spuntino", "Pranzo", "Merenda", "Cena", "Dessert", "Antipasto", "Piatto Unico", "Contorno"
        );
        // È possibile estendere questo per caricare categorie ricette dal DB se presenti
        categoriaRicetta.setItems(categoriePrefissate); // Imposta le categorie prefissate
        categoriaRicetta.getSelectionModel().selectFirst(); // Seleziona il primo elemento
    }

    /**
     * Apre la finestra di dettaglio per un alimento selezionato.
     * @param alimento L'oggetto Alimento da visualizzare in dettaglio.
     */
    private void apriDettaglio(Alimento alimento) {
        try {
            // Carica il file FXML della schermata DettagliAlimento
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettagliAlimento.fxml"));
            Parent root = loader.load();

            DettagliAlimentoController controller = loader.getController(); // Ottiene il controller
            controller.setAlimento(alimento); // Passa l'alimento selezionato
            controller.setOrigineFXML("AggiungiRicettaController.fxml");

            Stage stage = new Stage(); // Crea un nuovo Stage (finestra)
            stage.setTitle("Dettaglio Alimento"); // Imposta il titolo
            stage.setScene(new Scene(root)); // Imposta la scena
            stage.setResizable(false);
            stage.setFullScreen(false);
            stage.show(); // Mostra la finestra
        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire la schermata di dettaglio alimento.");
        }
    }

    /**
     * Gestisce l'azione del bottone "Aggiungi Alimento" per aggiungere un ingrediente alla ricetta.
     * Recupera l'alimento selezionato e la quantità, crea un `IngredienteRicetta` e lo aggiunge alla lista.
     * Aggiorna anche i totali nutrizionali della ricetta.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleAggiungiAlimento(ActionEvent event) {
        Alimento alimentoSelezionato = tableView.getSelectionModel().getSelectedItem(); // Ottiene l'alimento selezionato
        // Verifica che un alimento sia selezionato e che il campo quantità non sia vuoto
        if (alimentoSelezionato != null && !quantitaField.getText().isEmpty()) {
            try {
                double quantita = Double.parseDouble(quantitaField.getText()); // Parsifica la quantità
                // Validazione: assicura che la quantità sia positiva
                if (quantita <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Errore Quantità", "La quantità deve essere un numero positivo.");
                    return;
                }
                IngredienteRicetta ingrediente = new IngredienteRicetta(alimentoSelezionato, quantita); // Crea un nuovo ingrediente
                ingredienti.add(ingrediente); // Aggiunge l'ingrediente alla lista osservabile

                aggiornaValoriNutrizionali(alimentoSelezionato, quantita); // Aggiorna i totali nutrizionali della ricetta

                quantitaField.clear(); // Pulisce il campo quantità
                tableView.getSelectionModel().clearSelection(); // Deseleziona l'alimento nella tabella
            } catch (NumberFormatException e) {
                // Gestisce l'errore se la quantità non è un numero valido
                showAlert(Alert.AlertType.ERROR,"Errore nei dati", "Inserisci una quantità numerica valida.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR,"Errore nei dati", "Seleziona un alimento e inserisci una quantità.");
        }
    }

    /**
     * Aggiorna i totali nutrizionali della ricetta aggiungendo i valori dell'alimento specificato.
     * @param alimento L'alimento i cui valori nutrizionali devono essere aggiunti.
     * @param quantita La quantità dell'alimento in grammi.
     */
    private void aggiornaValoriNutrizionali(Alimento alimento, double quantita) {
        double fattore = quantita / 100.0; // Calcola il fattore per proporzionare i valori a 100g

        totKcal += alimento.getKcal() * fattore;
        totProteine += alimento.getProteine() * fattore;
        totCarboidrati += alimento.getCarboidrati() * fattore;
        totGrassi += alimento.getGrassi() * fattore;
        totGrassiSaturi += alimento.getGrassiSaturi() * fattore;
        totSale += alimento.getSale() * fattore;
        totFibre += alimento.getFibre() * fattore;
        totZuccheri += alimento.getZuccheri() * fattore;

        aggiornaLabel(); // Aggiorna le etichette di visualizzazione dei totali
    }

    /**
     * Aggiorna le etichette nella UI che mostrano i totali nutrizionali della ricetta.
     */
    private void aggiornaLabel() {
        calorieTotaliLabel.setText(String.format("%.2f", totKcal));
        proteineTotaliLabel.setText(String.format("%.2f", totProteine));
        carboidratiTotaliLabel.setText(String.format("%.2f", totCarboidrati));
        grassiTotaliLabel.setText(String.format("%.2f", totGrassi));
        grassiSaturiTotaliLabel.setText(String.format("%.2f", totGrassiSaturi));
        saleTotaliLabel.setText(String.format("%.2f", totSale));
        fibreTotaliLabel.setText(String.format("%.2f", totFibre));
        zuccheriTotaliLabel.setText(String.format("%.2f", totZuccheri));
    }

    /**
     * Sottrae i valori nutrizionali dell'alimento specificato dai totali della ricetta.
     * Utilizzato quando un ingrediente viene rimosso o la sua quantità modificata.
     * @param alimento L'alimento i cui valori nutrizionali devono essere sottratti.
     * @param quantita La quantità dell'alimento in grammi.
     */
    private void sottraiValoriNutrizionali(Alimento alimento, double quantita) {
        double fattore = quantita / 100.0;

        totKcal -= alimento.getKcal() * fattore;
        totProteine -= alimento.getProteine() * fattore;
        totCarboidrati -= alimento.getCarboidrati() * fattore;
        totGrassi -= alimento.getGrassi() * fattore;
        totGrassiSaturi -= alimento.getGrassiSaturi() * fattore;
        totSale -= alimento.getSale() * fattore;
        totFibre -= alimento.getFibre() * fattore;
        totZuccheri -= alimento.getZuccheri() * fattore;

        aggiornaLabel(); // Aggiorna le etichette di visualizzazione dei totali
    }

    /**
     * Fornisce una `CellFactory` per la colonna delle azioni (`azioniCol`) nella tabella degli ingredienti.
     * Ogni cella conterrà un bottone "Modifica" e un bottone "Elimina" per l'ingrediente corrispondente.
     * @return Una Callback che produce `TableCell` personalizzate.
     */
    private Callback<TableColumn<IngredienteRicetta, Void>, TableCell<IngredienteRicetta, Void>> getAzioneCellFactory() {
        return param -> new TableCell<>() {
            private final Button btnModifica = new Button("mod"); // Bottone per modificare la quantità
            private final Button btnElimina = new Button("-"); // Bottone per eliminare l'ingrediente
            private final HBox pane = new HBox(5, btnModifica, btnElimina); // Contenitore per i bottoni

            { // Blocco di inizializzazione dell'istanza
                btnModifica.setOnAction(e -> {
                    IngredienteRicetta ing = getTableView().getItems().get(getIndex()); // Ottiene l'ingrediente della riga
                    TextInputDialog dialog = new TextInputDialog(String.valueOf(ing.getQuantita())); // Crea un dialog per l'input
                    dialog.setHeaderText("Modifica Quantità");
                    dialog.setContentText("Inserisci nuova quantità:");
                    dialog.showAndWait().ifPresent(nuova -> { // Mostra il dialog e attende l'input
                        try {
                            double nuovaQuantita = Double.parseDouble(nuova); // Parsifica la nuova quantità
                            // Validazione: la nuova quantità deve essere positiva
                            if (nuovaQuantita <= 0) {
                                showAlert(Alert.AlertType.ERROR, "Errore Quantità", "La quantità deve essere un numero positivo.");
                                return;
                            }
                            sottraiValoriNutrizionali(ing.getAlimento(), ing.getQuantita()); // Sottrae i vecchi valori
                            ing.setQuantita(nuovaQuantita); // Imposta la nuova quantità
                            aggiornaValoriNutrizionali(ing.getAlimento(), nuovaQuantita); // Aggiunge i nuovi valori
                            ingredientiTable.refresh(); // Aggiorna la tabella per riflettere i cambiamenti
                        } catch (NumberFormatException ignored) {
                            showAlert(Alert.AlertType.ERROR, "Input non valido", "Inserisci un numero valido per la quantità.");
                        }
                    });
                });

                btnElimina.setOnAction(e -> {
                    IngredienteRicetta ing = getTableView().getItems().get(getIndex()); // Ottiene l'ingrediente da eliminare
                    ingredienti.remove(ing); // Rimuove l'ingrediente dalla lista osservabile
                    sottraiValoriNutrizionali(ing.getAlimento(), ing.getQuantita()); // Sottrae i valori nutrizionali
                    ingredientiTable.refresh(); // Aggiorna la tabella
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane); // Imposta il grafico della cella (i bottoni)
            }
        };
    }

    // --- Metodi di gestione dei bottoni di azione (Annulla, Salva) ---

    /**
     * Gestisce l'azione del bottone "Annulla".
     * Chiude la finestra corrente senza salvare le modifiche.
     * @param event L'evento di azione.
     */
    @FXML
    void handleAnnulla(ActionEvent event) {
        ((Stage) nomeRicetta.getScene().getWindow()).close(); // Chiude lo Stage corrente
    }

    private Ricette ricettaController; // Riferimento al controller della schermata "Ricette"

    /**
     * Imposta il riferimento al controller della schermata "Ricette".
     * Questo permette di aggiornare la lista delle ricette nella schermata principale
     * dopo averne salvata una nuova.
     * @param controller L'istanza del controller Ricette.
     */
    public void setRicettaController(Ricette controller) {
        this.ricettaController = controller;
    }

    /**
     * Gestisce l'azione del bottone "Salva Ricetta".
     * Valida i campi, salva la ricetta e i suoi ingredienti nel database,
     * e aggiorna la schermata delle ricette.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleSalvaRicetta(ActionEvent event) {
        // Validazione dei campi obbligatori
        if (isEmpty(nomeRicetta) || isEmpty(descrizioneRicetta) || categoriaRicetta.getValue() == null || ingredienti.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,"Errore nei dati","Tutti i campi devono essere compilati e devi aggiungere almeno un ingrediente.");
            return;
        }

        try (Connection conn = SQLiteConnessione.connector()) { // Ottiene la connessione al database
            conn.setAutoCommit(false); // Disabilita l'auto-commit per gestire le transazioni

            // Recupera dati dalla UI
            String nome = nomeRicetta.getText();
            String descrizione = descrizioneRicetta.getText();
            String categoria = categoriaRicetta.getValue();
            Integer userId = Session.getUserId(); // Recupera l'ID dell'utente loggato

            // 1. Inserimento nella tabella Ricette
            String insertRicettaSQL = "INSERT INTO Ricette (nome, descrizione, id_utente, categoria, kcal, proteine, carboidrati, grassi, grassi_saturi, sale, fibre, zuccheri) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // Prepara lo statement, specificando di voler recuperare le chiavi generate automaticamente (ID ricetta)
            PreparedStatement insertRicettaStmt = conn.prepareStatement(insertRicettaSQL, Statement.RETURN_GENERATED_KEYS);
            insertRicettaStmt.setString(1, nome);
            insertRicettaStmt.setString(2, descrizione);
            insertRicettaStmt.setInt(3, userId);
            insertRicettaStmt.setString(4, categoria);
            insertRicettaStmt.setDouble(5, totKcal);
            insertRicettaStmt.setDouble(6, totProteine);
            insertRicettaStmt.setDouble(7, totCarboidrati);
            insertRicettaStmt.setDouble(8, totGrassi);
            insertRicettaStmt.setDouble(9, totGrassiSaturi);
            insertRicettaStmt.setDouble(10, totSale);
            insertRicettaStmt.setDouble(11, totFibre);
            insertRicettaStmt.setDouble(12, totZuccheri);
            insertRicettaStmt.executeUpdate(); // Esegue l'inserimento

            // Recupera l'ID generato per la nuova ricetta
            ResultSet generatedKeys = insertRicettaStmt.getGeneratedKeys();
            int idRicetta = -1;
            if (generatedKeys.next()) {
                idRicetta = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Errore nel recupero dell'ID della ricetta appena inserita.");
            }
            generatedKeys.close();
            insertRicettaStmt.close();

            // 2. Inserisci gli ingredienti nella tabella ingredienti_ricette
            String insertIngredienteSQL = "INSERT INTO Ingredienti_ricette (id_ricetta, id_alimento, quantita_grammi) VALUES (?, ?, ?)";
            PreparedStatement insertIngredienteStmt = conn.prepareStatement(insertIngredienteSQL);

            for (IngredienteRicetta ingrediente : ingredienti) {
                insertIngredienteStmt.setInt(1, idRicetta);
                insertIngredienteStmt.setInt(2, ingrediente.getAlimento().getId());
                insertIngredienteStmt.setDouble(3, ingrediente.getQuantita());
                insertIngredienteStmt.addBatch(); // Aggiunge l'operazione al batch per l'esecuzione efficiente
            }

            insertIngredienteStmt.executeBatch(); // Esegue tutte le operazioni di inserimento in batch
            insertIngredienteStmt.close();

            conn.commit(); // Conferma la transazione: tutte le operazioni sono state un successo

            showAlert(Alert.AlertType.INFORMATION,"Salvataggio dati","Ricetta salvata con successo!");
            if (ricettaController != null) {
                System.out.println("filtro: "+ricettaController.getFiltro()); // Debug
                ricettaController.resetRicerca(); // Resetta la ricerca nella schermata Ricette principale
                ricettaController.cercaRicette(ricettaController.getFiltro(),false); // Aggiorna la lista delle ricette
            }
            // Chiudi finestra corrente
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR,"Errore database","Errore durante il salvataggio: " + e.getMessage());
        }
    }

    /**
     * Metodo helper per controllare se un campo di input testuale è vuoto o contiene solo spazi bianchi.
     * @param campo Il TextInputControl (TextField o TextArea) da controllare.
     * @return true se il campo è vuoto o contiene solo spazi, false altrimenti.
     */
    private boolean isEmpty(TextInputControl campo) {
        return campo.getText() == null || campo.getText().trim().isEmpty();
    }

    /**
     * Metodo helper per mostrare messaggi di avviso all'utente.
     * Applica stili CSS personalizzati in base al tipo di avviso.
     * @param alertType Il tipo di avviso (ERROR, INFORMATION, WARNING, CONFIRMATION).
     * @param title Il titolo della finestra di avviso.
     * @param message Il messaggio da visualizzare.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType); // Crea una nuova istanza di Alert
        alert.setTitle(title); // Imposta il titolo
        alert.setHeaderText(null); // Non mostra un header text
        alert.setContentText(message); // Imposta il contenuto

        // Cerca il file CSS per lo stile personalizzato degli alert
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            // Se il CSS viene trovato, lo aggiunge al DialogPane dell'alert
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base
            // Aggiunge una classe di stile specifica in base al tipo di alert per una maggiore personalizzazione
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Messaggio di errore se il CSS non è trovato
        }

        alert.showAndWait(); // Mostra l'avviso e attende che l'utente lo chiuda
    }
}
