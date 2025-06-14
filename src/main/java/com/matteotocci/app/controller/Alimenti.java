package com.matteotocci.app.controller;

// Importa le classi necessarie per l'applicazione JavaFX e le operazioni del database
import com.matteotocci.app.model.LoginModel; // Modello per la gestione del login (es. recupero ruolo utente)
import com.matteotocci.app.model.Alimento; // Modello per l'oggetto Alimento
import com.matteotocci.app.model.SQLiteConnessione; // Classe per gestire la connessione al database SQLite
import com.matteotocci.app.model.Session; // Classe per la gestione della sessione utente (es. ID utente loggato)
import com.matteotocci.app.model.Dieta; // Modello per l'oggetto Dieta (necessario per recuperare la dieta in Alimenti)
import javafx.application.Platform; // Per eseguire codice sul thread di UI di JavaFX
import javafx.collections.FXCollections; // Utility per creare collezioni osservabili
import javafx.collections.ObservableList; // Lista che notifica i "listener" quando avvengono dei cambiamenti
import javafx.event.ActionEvent; // Tipo di evento generato dalle azioni dell'utente (es. click su un bottone)
import javafx.fxml.FXML; // Annotazione per collegare elementi dell'interfaccia utente definiti in FXML al codice Java
import javafx.fxml.FXMLLoader; // Carica file FXML (layout dell'interfaccia utente)
import javafx.fxml.Initializable; // AGGIUNTA: Interfaccia per i controller che devono essere inizializzati dopo il caricamento dell'FXML
import javafx.geometry.Orientation; // Per specificare l'orientamento di elementi come le ScrollBar
import javafx.scene.Node; // Classe base per tutti i nodi nel grafo della scena (elementi UI)
import javafx.scene.Parent; // Nodo base per la gerarchia della scena (container di tutti gli elementi UI)
import javafx.scene.Scene; // Contenitore per tutti i contenuti di una scena
import javafx.scene.control.*; // Controlli UI standard di JavaFX (Button, ComboBox, CheckBox, TableView, TextField, Label, Alert)
import javafx.scene.control.cell.PropertyValueFactory; // Per collegare le proprietà degli oggetti alle colonne di una TableView
import javafx.scene.image.ImageView; // Per visualizzare immagini
import javafx.scene.input.MouseEvent; // Tipo di evento generato da interazioni del mouse
import javafx.stage.Stage; // La finestra principale dell'applicazione

import java.io.IOException; // Eccezione per errori di input/output (es. caricamento file FXML)
import java.net.URL; // Necessario per Initializable
import java.sql.*; // Classi per l'interazione con il database
import java.util.Optional; // Necessario per showAlert
import java.util.ResourceBundle; // Necessario per Initializable

/**
 * Controller per la schermata "Alimenti".
 * Questa classe gestisce la visualizzazione, la ricerca, il filtro e l'aggiunta di alimenti.
 * Permette anche la navigazione tra le diverse sezioni dell'applicazione.
 */
public class Alimenti implements Initializable { // AGGIUNTA: Implementa l'interfaccia Initializable

    public LoginModel loginModel = new LoginModel(); // Istanza del modello per la gestione del login
    // Recupera l'email e il ruolo dell'utente loggato.
    // Nota: queste chiamate a Session.getUserId() qui potrebbero essere null all'avvio molto precoce.
    // È più sicuro recuperarle dopo l'inizializzazione completa o dove sono effettivamente usate.
    String email = loginModel.getEmail(Session.getUserId());
    String ruolo = loginModel.getRuoloUtente(email);

    // --- Elementi dell'interfaccia utente (collegati tramite @FXML) ---
    @FXML private Button BottoneAlimenti; // Bottone per accedere alla sezione Alimenti (auto-referenziale)
    @FXML private ComboBox<String> categoriaComboBox; // ComboBox per filtrare gli alimenti per categoria
    @FXML private CheckBox mieiAlimentiCheckBox; // CheckBox per filtrare solo gli alimenti creati dall'utente corrente
    @FXML private Button BottoneHome; // Bottone per tornare alla Home Page
    @FXML private Label nomeUtenteLabelHomePage; // Etichetta per mostrare il nome dell'utente loggato
    @FXML private Button BottoneRicette; // Bottone per accedere alla sezione Ricette
    @FXML private TextField cercaAlimento; // Campo di testo per la ricerca di alimenti per nome
    @FXML private TableView<Alimento> tableView; // Tabella per visualizzare la lista degli alimenti
    @FXML private Button bottoneCerca; // Bottone per avviare la ricerca (associato a handleCercaAlimento)

    // Colonne della TableView per gli alimenti
    @FXML private TableColumn<Alimento, ImageView> immagineCol;
    @FXML private TableColumn<Alimento, String> nomeCol, brandCol;
    @FXML private TableColumn<Alimento, Double> calorieCol, proteineCol, carboidratiCol, grassiCol;
    @FXML private TableColumn<Alimento, Double> grassiSatCol, saleCol, fibreCol, zuccheriCol;

    // --- Variabili di stato interne per la paginazione ---
    private int offset = 0; // Offset per la paginazione dei risultati della ricerca
    private final int LIMIT = 50; // Numero massimo di risultati da caricare per volta
    private boolean isLoading = false; // Flag per prevenire carichi multipli durante lo scroll

    // Variabile per memorizzare la dieta assegnata al cliente (utile per la navigazione al piano alimentare)
    private Dieta dietaAssegnata;

    /**
     * Imposta il testo della label del nome utente nella parte superiore della pagina.
     * Recupera il nome e cognome dell'utente dal database usando l'ID dalla sessione.
     */
    private void setNomeUtenteLabel() {
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID direttamente dalla Sessione

        if (userIdFromSession != null) {
            String nomeUtente = getNomeUtenteDalDatabase(userIdFromSession.toString());
            if (nomeUtente != null && !nomeUtente.isEmpty()) {
                nomeUtenteLabelHomePage.setText(nomeUtente);
            } else {
                nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Testo di fallback
            }
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Testo di fallback se l'ID non è disponibile
            System.err.println("[ERROR - Alimenti] ID utente non disponibile dalla Sessione per impostare il nome.");
        }
    }

    /**
     * Recupera il nome e cognome di un utente dal database dato il suo ID.
     * @param userId L'ID dell'utente.
     * @return Il nome completo dell'utente (Nome Cognome) o null se non trovato.
     */
    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement
            pstmt.setString(1, userId); // Imposta l'ID utente come parametro
            ResultSet rs = pstmt.executeQuery(); // Esegue la query
            if (rs.next()) { // Se c'è un risultato
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena nome e cognome
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
            e.printStackTrace(); // Stampa la traccia dell'errore
        }
        return nomeUtente;
    }

    /**
     * Metodo di inizializzazione chiamato automaticamente da FXMLLoader dopo che
     * tutti gli elementi FXML sono stati caricati e iniettati nel controller.
     * Qui vengono configurate le colonne della TableView, i listener, la logica di paginazione,
     * e il caricamento iniziale dei dati.
     *
     * @param location L'URL del documento FXML che ha dato origine a questo controller.
     * @param resources Le risorse utilizzate per localizzare gli oggetti radice, o null se la radice non è stata localizzata.
     */
    @Override // ANNOTAZIONE AGGIUNTA: Indica che questo metodo fa l'override di un metodo dell'interfaccia Initializable
    public void initialize(URL location, ResourceBundle resources) { // MODIFICA: La firma del metodo ora corrisponde a quella di Initializable
        // Associa le proprietà dell'oggetto Alimento alle colonne della TableView
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

        // Configura il comportamento di double-click sulle righe della tabella
        tableView.setRowFactory(tv -> {
            TableRow<Alimento> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) { // Se c'è un double-click e la riga non è vuota
                    apriDettaglio(row.getItem()); // Apre la finestra di dettaglio per l'alimento selezionato
                }
            });
            return row;
        });

        popolaCategorie(); // Popola la ComboBox delle categorie

        // Listener per la selezione della categoria: resetta la ricerca e la riesegue
        categoriaComboBox.setOnAction(e -> {
            resetRicerca();
            cercaAlimenti(cercaAlimento.getText(), false);
        });

        // Listener per la checkbox "Solo i miei alimenti": resetta la ricerca e la riesegue
        mieiAlimentiCheckBox.setOnAction(e -> {
            resetRicerca();
            cercaAlimenti(cercaAlimento.getText(), false);
        });

        // Gestione dello scroll per la paginazione infinita
        Platform.runLater(() -> {
            ScrollBar scrollBar = getVerticalScrollbar(tableView);
            if (scrollBar != null) {
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == scrollBar.getMax()) {
                        caricaAltri(); // Carica altri alimenti quando si raggiunge il fondo della scrollbar
                    }
                });
            }
        });

        setNomeUtenteLabel(); // Imposta il nome utente nella label all'avvio
        cercaAlimenti("", false); // Carica gli alimenti iniziali (senza filtro di ricerca)
        recuperaEImpostaDietaAssegnata(); // Recupera la dieta assegnata all'avvio della pagina
    }

    /**
     * Metodo helper per ottenere la ScrollBar verticale di una TableView.
     * Utilizzato per implementare la paginazione basata sullo scroll.
     * @param table La TableView da cui ottenere la scrollbar.
     * @return La ScrollBar verticale o null se non trovata.
     */
    private ScrollBar getVerticalScrollbar(TableView<?> table) {
        for (Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                return sb;
            }
        }
        return null;
    }

    /**
     * Carica altri alimenti quando l'utente raggiunge il fondo della scrollbar.
     * Utilizza il flag `isLoading` per evitare richieste duplicate.
     */
    private void caricaAltri() {
        if (isLoading) return; // Se è già in corso un caricamento, esci
        isLoading = true; // Imposta il flag per indicare che un caricamento è in corso
        cercaAlimenti(cercaAlimento.getText(), true); // Esegue la ricerca in modalità append
        isLoading = false; // Resetta il flag al termine del caricamento
    }

    /**
     * Resetta l'offset di paginazione e pulisce gli elementi dalla tabella,
     * preparando per una nuova ricerca.
     */
    @FXML
    public void resetRicerca() {
        offset = 0;
        tableView.getItems().clear();
    }

    /**
     * Restituisce il testo attualmente presente nel campo di ricerca alimenti.
     * @return Il testo del filtro di ricerca.
     */
    @FXML
    public String getFiltro() {
        return cercaAlimento != null ? cercaAlimento.getText() : "";
    }

    /**
     * Gestisce l'azione del bottone "Cerca Alimento".
     * Resetta la ricerca e la riesegue con il filtro attuale.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleCercaAlimento(ActionEvent event) {
        resetRicerca(); // Resetta la ricerca
        cercaAlimenti(cercaAlimento.getText(), false); // Esegue la ricerca
    }

    /**
     * Esegue la ricerca degli alimenti nel database.
     * Costruisce dinamicamente la query SQL in base al filtro di ricerca,
     * alla categoria selezionata e allo stato della checkbox "Solo i miei".
     *
     * @param filtro Il testo da cercare nel nome dell'alimento.
     * @param append Se true, aggiunge i nuovi risultati alla lista esistente; altrimenti, crea una nuova lista.
     */
    public void cercaAlimenti(String filtro, boolean append) {
        // Se `append` è true, usa la lista esistente della tabella; altrimenti, crea una nuova lista
        ObservableList<Alimento> alimenti = append ? tableView.getItems() : FXCollections.observableArrayList();

        String categoria = categoriaComboBox.getSelectionModel().getSelectedItem(); // Ottiene la categoria selezionata
        boolean soloMiei = mieiAlimentiCheckBox.isSelected(); // Controlla lo stato della checkbox "Solo i miei"

        // Costruisce dinamicamente la query SQL
        StringBuilder query = new StringBuilder("SELECT * FROM foods WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?"); // Aggiunge la condizione per la categoria se selezionata
        }
        Integer currentUserId = Session.getUserId(); // Ottiene l'ID dell'utente corrente
        if (soloMiei && currentUserId != null) {
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
            if (soloMiei && currentUserId != null) {
                stmt.setInt(paramIndex++, currentUserId); // Imposta il parametro user_id
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
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare gli alimenti.");
        }
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
            controller.setAlimentiController(this); // Passa un riferimento a questo controller
            controller.setOrigineFXML("Alimenti.fxml"); // Imposta la schermata di origine

            Stage stage = new Stage(); // Crea un nuovo Stage (finestra)
            stage.setTitle("Dettaglio Alimento"); // Imposta il titolo
            Scene scene = new Scene(root);
            stage.setResizable(false);
            stage.setFullScreen(false);
            // Carica un CSS specifico per la finestra di dettaglio se necessario
            scene.getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/DettaglioAlimento-Style.css").toExternalForm());
            stage.setScene(scene);
            stage.show(); // Mostra la finestra

        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire il dettaglio alimento.");
        }
    }

    /**
     * Gestisce l'azione per aprire la schermata di aggiunta di un nuovo alimento.
     * @param event L'evento di azione.
     */
    @FXML
    private void handleApriAggiunta(ActionEvent event) {
        try {
            // Carica il file FXML della schermata AggiungiAlimento
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiAlimento.fxml"));
            Parent root = loader.load();

            AggiungiAlimentoController controller = loader.getController(); // Ottiene il controller
            controller.setAlimentiController(this); // Passa un riferimento a questo controller

            Stage stage = new Stage(); // Crea un nuovo Stage
            stage.setTitle("Aggiungi Alimento"); // Imposta il titolo
            stage.setScene(new Scene(root)); // Imposta la scena
            stage.setResizable(false);
            stage.setFullScreen(false);
            stage.show(); // Mostra la finestra

        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire la schermata di aggiunta alimento. ");
        }
    }

    /**
     * Popola la ComboBox delle categorie recuperando le categorie distinte degli alimenti dal database.
     */
    private void popolaCategorie() {
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
            showAlert(Alert.AlertType.ERROR, "Errore Database",  "Errore nel caricamento delle categorie");
        }
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

    /**
     * Recupera la dieta assegnata a un cliente specifico dal database.
     * @param idCliente L'ID del cliente.
     * @return L'oggetto Dieta assegnata al cliente o null se non trovata.
     */
    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String url = "jdbc:sqlite:database.db"; // Percorso del database
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        try (Connection conn = SQLiteConnessione.connector(); // Usa la connessione centralizzata
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente); // Imposta l'ID del cliente come parametro
            ResultSet rs = pstmt.executeQuery(); // Esegue la query

            if (rs.next()) { // Se c'è un risultato
                // Crea un nuovo oggetto Dieta con i dati recuperati
                dieta = new Dieta(
                        rs.getInt("id"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine"),
                        rs.getInt("id_nutrizionista"),
                        rs.getInt("id_cliente")
                );
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (Alimenti): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta;
    }

    /**
     * Recupera e imposta la dieta assegnata al cliente all'inizializzazione della pagina.
     */
    private void recuperaEImpostaDietaAssegnata() {
        Integer userIdFromSession = Session.getUserId();
        if (userIdFromSession != null) {
            this.dietaAssegnata = recuperaDietaAssegnataACliente(userIdFromSession.intValue());
            if (this.dietaAssegnata != null) {
                System.out.println("DEBUG (Alimenti): Dieta '" + dietaAssegnata.getNome() + "' (ID: " + dietaAssegnata.getId() + ") recuperata per utente ID: " + userIdFromSession);
            } else {
                System.out.println("DEBUG (Alimenti): Nessuna dieta trovata per l'utente ID: " + userIdFromSession);
            }
        } else {
            System.err.println("[ERROR - Alimenti] ID utente non disponibile dalla Sessione per recupero dieta.");
        }
    }

    // --- Metodi di Navigazione ---

    /**
     * Metodo per la navigazione alla schermata "Alimenti" (per nutrizionista o cliente).
     * @param event L'evento di azione (click sul bottone).
     */
    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/AlimentiNutrizionista.fxml";
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/Alimenti.fxml";
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            // Alimenti si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la pagina Alimenti.");
        }
    }

    /**
     * Metodo per la navigazione alla schermata "Ricette" (per nutrizionista o cliente).
     * @param event L'evento di azione (click sul bottone).
     */
    @FXML
    private void AccessoRicette(ActionEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/RicetteNutrizionista.fxml";
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/Ricette.fxml";
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            // Ricette si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la pagina Ricette");
        }
    }

    /**
     * Metodo per la navigazione alla "HomePage" (per nutrizionista o cliente).
     * @param event L'evento di azione (click sul bottone).
     */
    @FXML
    private void AccessoHome(ActionEvent event) {

        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/HomePageNutrizionista.fxml";
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/HomePage.fxml";
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la HomePage");
        }
    }

    /**
     * Metodo per la navigazione alla "Pagina Profilo" (per nutrizionista o cliente).
     * @param event L'evento del mouse (click).
     */
    @FXML
    private void AccessoProfilo(MouseEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/ProfiloNutrizionista.fxml";
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/PaginaProfilo.fxml";
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            // PaginaProfilo si inizializza da sola dalla Sessione, non serve passare l'ID
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la Pagina Profilo");
        }
    }

    private Stage dietaStage; // Per gestire apertura/chiusura della finestra dieta

    /**
     * Metodo per la navigazione alla schermata "Visualizza Dieta" (solo per cliente).
     * Apre la schermata `VisualizzaDieta.fxml` e le passa l'oggetto `Dieta` assegnato al cliente.
     * @param event L'evento di azione (click sul bottone).
     */
    @FXML
    private void AccessoPianoAlimentare(ActionEvent event) {
        if (dietaAssegnata != null) { // Controlla se una dieta è stata assegnata
            try {
                // Chiudi la finestra precedente se è già aperta
                if (dietaStage != null && dietaStage.isShowing()) {
                    dietaStage.close();
                }

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                Parent visualizzaDietaRoot = fxmlLoader.load();

                // Ottieni il controller della nuova finestra
                VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();

                // Passa l'oggetto Dieta al controller della nuova finestra
                visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                System.out.println("DEBUG (Alimenti): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                dietaStage = new Stage(); // Crea un nuovo Stage
                dietaStage.setScene(new Scene(visualizzaDietaRoot)); // Imposta la scena
                dietaStage.show(); // Mostra la finestra

            } catch (IOException e) {
                System.err.println("ERRORE (Alimenti): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.");
            } catch (Exception e) {
                System.err.println("ERRORE (Alimenti): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.");
            }
        } else {
            System.out.println("DEBUG (Alimenti): Nessuna dieta trovata per il cliente (ID: " + Session.getUserId() + ").");
            showAlert(Alert.AlertType.INFORMATION, "Nessuna dieta assegnata",
                    "Il cliente non ha diete assegnate o non è stato possibile recuperarle.");
        }
    }

    /**
     * Metodo per la navigazione alla schermata "Diete Nutrizionista" (solo per nutrizionista).
     * @param event L'evento di azione (click sul bottone).
     */
    @FXML
    private void AccessoDieta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml"));
            Parent dietaRoot = fxmlLoader.load();
            Stage dietaStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            dietaStage.setScene(new Scene(dietaRoot));
            dietaStage.setTitle("Diete Nutrizionista"); // Titolo più specifico
            dietaStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Diete Nutrizionista'.");
        }
    }
}
