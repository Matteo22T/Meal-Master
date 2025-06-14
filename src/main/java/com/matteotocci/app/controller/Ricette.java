package com.matteotocci.app.controller; // Dichiarazione del package in cui si trova la classe del controller.

// Importazioni delle classi e interfacce necessarie.
import com.matteotocci.app.model.*; // Importa tutte le classi dal package model (es. Session, Dieta, LoginModel, Ricetta, SQLiteConnessione).
import javafx.collections.FXCollections; // Utility per creare ObservableList.
import javafx.collections.ObservableList; // Interfaccia per liste che possono essere osservate da JavaFX per aggiornamenti UI.
import javafx.event.ActionEvent; // Classe per la gestione degli eventi di azione (es. click su un bottone).
import javafx.fxml.FXML; // Annotazione per iniettare componenti FXML nel controller.
import javafx.fxml.FXMLLoader; // Classe per caricare file FXML.
import javafx.fxml.Initializable; // Interfaccia che i controller devono implementare per l'inizializzazione dopo il caricamento FXML.
import javafx.geometry.Orientation; // Enum per specificare l'orientamento (es. per le scrollbar).
import javafx.scene.Node; // Classe base per tutti i nodi nel grafo di scena (es. controlli UI).
import javafx.scene.Parent; // Classe base per i nodi che hanno figli (usata per la root di una scena FXML).
import javafx.scene.Scene; // Contenitore per tutti i contenuti di una scena.
import javafx.scene.control.*; // Importa tutti i controlli UI di JavaFX (Label, ComboBox, CheckBox, TextField, TableView, TableColumn, Alert, ScrollBar).
import javafx.scene.control.cell.PropertyValueFactory; // Factory per la colonna della tabella che estrae valori da una proprietà.
import javafx.scene.input.MouseEvent; // Classe per la gestione degli eventi del mouse.
import javafx.stage.Stage; // Finestra principale dell'applicazione.

import java.io.IOException; // Eccezione per errori di I/O.
import java.net.URL; // Classe per rappresentare un URL (usata per caricare risorse come i CSS).
import java.sql.*; // Importa tutte le classi SQL (Connection, DriverManager, PreparedStatement, ResultSet, SQLException).
import java.util.ResourceBundle; // Utilizzato per la localizzazione (non strettamente usato in questo codice, ma richiesto da Initializable).

// Dichiarazione della classe Ricette, che implementa Initializable per l'inizializzazione dei componenti.
public class Ricette implements Initializable {

    // Campi annotati con @FXML per l'iniezione dei componenti UI definiti nel file FXML.
    @FXML private Label nomeUtenteLabelHomePage; // Label per visualizzare il nome dell'utente nella homepage.
    @FXML private ComboBox<String> categorieRicette; // ComboBox per selezionare le categorie di ricette.
    @FXML private CheckBox mieiAlimentiCheckBox; // CheckBox per filtrare le ricette create dall'utente.
    @FXML private TextField cercaRicetta; // Campo di testo per la ricerca di ricette.
    @FXML private TableView<Ricetta> tableViewRicette; // Tabella per visualizzare le ricette.
    @FXML private TableColumn<Ricetta, String> nomeCol; // Colonna per il nome della ricetta.
    @FXML private TableColumn<Ricetta, String> descrizioneCol; // Colonna per la descrizione della ricetta.
    @FXML private TableColumn<Ricetta, String> categoriaCol; // Colonna per la categoria della ricetta.

    private int offset = 0; // Offset per la paginazione dei risultati (caricamento incrementale).
    private final int LIMIT = 50; // Numero massimo di risultati da caricare per volta.
    private boolean isLoading = false; // Flag per prevenire il caricamento multiplo durante lo scrolling.

    private Dieta dietaAssegnata; // Oggetto Dieta assegnato all'utente corrente.

    public LoginModel loginModel = new LoginModel(); // Istanza di LoginModel per recuperare informazioni sull'utente.
    String email = loginModel.getEmail(Session.getUserId()); // Recupera l'email dell'utente dalla sessione.
    String ruolo = loginModel.getRuoloUtente(email); // Recupera il ruolo dell'utente (nutrizionista/cliente).

    // Metodo privato per impostare il testo della label del nome utente.
    private void setNomeUtenteLabel() {
        String nomeUtente = getNomeUtenteDalDatabase(Session.getUserId().toString()); // Ottiene nome e cognome dal DB.
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            nomeUtenteLabelHomePage.setText(nomeUtente); // Imposta la label se il nome è disponibile.
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Valore di default se non trovato.
        }
    }

    // Metodo privato per recuperare il nome e cognome dell'utente dal database.
    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?"; // Query SQL per il nome e cognome.
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db"); // Connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId); // Imposta l'ID utente nella query.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query.
            if (rs.next()) {
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena nome e cognome.
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
        }
        return nomeUtente; // Restituisce il nome completo.
    }

    // Metodo privato per recuperare la dieta assegnata a un cliente specifico.
    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        // Query per recuperare la dieta con l'ID del cliente.
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        try (Connection conn = SQLiteConnessione.connector(); // Usa il connettore personalizzato per SQLite.
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente); // Imposta l'ID del cliente.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query.

            if (rs.next()) { // Se viene trovata una dieta.
                dieta = new Dieta( // Crea un nuovo oggetto Dieta.
                        rs.getInt("id"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine"),
                        rs.getInt("id_nutrizionista"),
                        rs.getInt("id_cliente")
                );
                System.out.println("DEBUG (Ricette): Recuperata dieta '" + dieta.getNome() + "' (ID: " + dieta.getId() + ") per cliente ID: " + idCliente);
            } else {
                System.out.println("DEBUG (Ricette): Nessuna dieta trovata per il cliente ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (Ricette): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta; // Restituisce l'oggetto Dieta.
    }

    // Metodo di inizializzazione, chiamato automaticamente da JavaFX dopo che il file FXML è stato caricato.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Collega le colonne della TableView alle proprietà dell'oggetto Ricetta.
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        descrizioneCol.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        categoriaCol.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        Integer userIdFromSession = Session.getUserId(); // Recupera l'ID utente dalla sessione.

        if (userIdFromSession != null) {
            System.out.println("[DEBUG - Ricette] ID utente da Sessione: " + userIdFromSession);
            this.dietaAssegnata = recuperaDietaAssegnataACliente(userIdFromSession); // Recupera la dieta per l'utente.
            setNomeUtenteLabel(); // Imposta la label del nome utente.
        }

        cercaRicette("", false); // Carica le ricette iniziali (senza filtro).
        popolaCategorie(); // Popola la ComboBox delle categorie.

        // Listener per la selezione della categoria: resetta l'offset e ricarica le ricette.
        categorieRicette.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(cercaRicetta.getText(), false);
        });

        // Listener per la checkbox "Miei Alimenti": resetta l'offset e ricarica le ricette.
        mieiAlimentiCheckBox.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(cercaRicetta.getText(), false);
        });

        // Ottiene la scrollbar verticale della tabella e aggiunge un listener.
        ScrollBar scrollBar = getVerticalScrollbar(tableViewRicette);
        if (scrollBar != null) {
            scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                // Se lo scroll raggiunge la fine, carica altri elementi.
                if (newVal.doubleValue() == scrollBar.getMax()) {
                    caricaAltri();
                }
            });
        }
    }

    // Metodo per ottenere la scrollbar verticale di una TableView.
    private ScrollBar getVerticalScrollbar(TableView<?> table) {
        // Cerca tra tutti i nodi della tabella quelli che sono scrollbar verticali.
        for (Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                return sb;
            }
        }
        return null; // Restituisce null se non trovata.
    }

    // Metodo per caricare altri elementi quando lo scroll raggiunge la fine.
    private void caricaAltri() {
        if (!isLoading) { // Previene caricamenti multipli simultanei.
            isLoading = true; // Imposta il flag di caricamento.
            cercaRicette(cercaRicetta.getText(), true); // Carica altre ricette, appendendole.
            isLoading = false; // Resetta il flag di caricamento.
        }
    }

    // Metodo FXML chiamato quando si preme il bottone "Cerca".
    @FXML
    private void handleCercaRicetta(ActionEvent event) {
        offset = 0; // Reset dell'offset per una nuova ricerca.
        tableViewRicette.getItems().clear(); // Pulisce la tabella.
        cercaRicette(cercaRicetta.getText(), false); // Esegue la ricerca.
    }

    // Metodo principale per cercare e caricare le ricette dal database.
    public void cercaRicette(String filtro, boolean append) {
        // Se `append` è true, aggiunge gli elementi alla lista esistente; altrimenti crea una nuova lista.
        ObservableList<Ricetta> ricette = append ? tableViewRicette.getItems() : FXCollections.observableArrayList();

        String categoria = categorieRicette.getSelectionModel().getSelectedItem(); // Ottiene la categoria selezionata.
        boolean soloMiei = mieiAlimentiCheckBox.isSelected(); // Controlla se la checkbox "Miei Alimenti" è selezionata.

        // Costruisce la query SQL dinamicamente in base ai filtri.
        StringBuilder query = new StringBuilder("SELECT * FROM Ricette WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?"); // Aggiunge filtro per categoria.
        }
        if (soloMiei && Session.getUserId() != 0) {
            query.append(" AND id_utente = ?"); // Aggiunge filtro per ricette dell'utente.
        }
        query.append(" LIMIT ? OFFSET ?"); // Aggiunge clausole LIMIT e OFFSET per la paginazione.

        try (Connection conn = SQLiteConnessione.connector(); // Connessione al database.
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1; // Indice per i parametri della PreparedStatement.
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%"); // Imposta il filtro di ricerca (case-insensitive).
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria); // Imposta il parametro categoria.
            }
            if (soloMiei && Session.getUserId() != 0) {
                stmt.setInt(paramIndex++, Session.getUserId()); // Imposta l'ID utente.
            }
            stmt.setInt(paramIndex++, LIMIT); // Imposta il limite di risultati.
            stmt.setInt(paramIndex++, offset); // Imposta l'offset.

            try (ResultSet rs = stmt.executeQuery()) { // Esegue la query.
                while (rs.next()) { // Itera sui risultati.
                    Ricetta ricetta = new Ricetta( // Crea un nuovo oggetto Ricetta con i dati dal ResultSet.
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
                    ricette.add(ricetta); // Aggiunge la ricetta alla lista.
                }

                tableViewRicette.setItems(ricette); // Imposta la lista delle ricette nella TableView.
                offset += LIMIT; // Incrementa l'offset per il prossimo caricamento.
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL.
        }
    }

    // Metodo privato per popolare la ComboBox delle categorie.
    private void popolaCategorie() {
        ObservableList<String> categoriePrefissate = FXCollections.observableArrayList(
                "Tutte", "Colazione", "Spuntino", "Pranzo", "Merenda", "Cena" // Categorie predefinite.
        );
        categorieRicette.setItems(categoriePrefissate); // Imposta le categorie nella ComboBox.
        categorieRicette.getSelectionModel().selectFirst(); // Seleziona "Tutte" come predefinito.
    }

    // Metodo FXML per resettare la ricerca (non completamente implementato qui per svuotare il campo testo).
    @FXML
    public void resetRicerca() {
        offset = 0; // Resetta l'offset.
        tableViewRicette.getItems().clear(); // Pulisce la tabella.
        // Mancano istruzioni per pulire il campo 'cercaRicetta' e la ComboBox 'categorieRicette'.
        // Esempio: cercaRicetta.clear(); categorieRicette.getSelectionModel().selectFirst();
    }

    // Metodo FXML per ottenere il testo del campo di ricerca.
    @FXML
    public String getFiltro() {
        return cercaRicetta != null ? cercaRicetta.getText() : ""; // Restituisce il testo del TextField o una stringa vuota.
    }

    // Metodo FXML chiamato quando si fa doppio click su una ricetta nella tabella.
    @FXML
    private void apriDettaglioRicetta(MouseEvent event) {
        // Controlla se è stato un doppio click e se una ricetta è selezionata.
        if (event.getClickCount() == 2 && !tableViewRicette.getSelectionModel().isEmpty()) {
            Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem(); // Ottiene la ricetta selezionata.
            try {
                // Carica il file FXML per la finestra di dettaglio della ricetta.
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettaglioRicetta.fxml"));
                Parent root = loader.load();

                // Ottiene il controller della finestra di dettaglio e gli passa la ricetta e un riferimento a questo controller.
                DettaglioRicettaController controller = loader.getController();
                controller.setRicetta(ricettaSelezionata);
                controller.setRicettaController(this); // Passa 'this' per consentire aggiornamenti dopo modifiche.
                controller.setOrigineFXML("Ricette.fxml"); // Indica da quale FXML è stata aperta.

                Stage stage = new Stage(); // Crea una nuova finestra.
                stage.setTitle("Dettaglio Ricetta");
                stage.setScene(new Scene(root));
                stage.setResizable(false); // Impedisce il ridimensionamento.
                stage.setFullScreen(false); // Non apre a schermo intero.
                stage.show(); // Mostra la finestra.
            } catch (IOException e) {
                e.printStackTrace(); // Stampa l'errore di I/O.
            }
        }
    }

    private Stage dietaStage; // Variabile per gestire l'apertura/chiusura della finestra della dieta.

    // Metodo FXML per l'accesso al piano alimentare.
    @FXML
    private void AccessoPianoAlimentare(ActionEvent event) {
        if (dietaAssegnata != null) { // Controlla se una dieta è stata assegnata.
            try {
                // Chiude la finestra precedente della dieta se è già aperta.
                if (dietaStage != null && dietaStage.isShowing()) {
                    dietaStage.close();
                }

                // Carica il FXML della vista della dieta.
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                Parent visualizzaDietaRoot = fxmlLoader.load();

                // Ottiene il controller della nuova finestra e gli passa l'oggetto Dieta.
                VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();
                visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                System.out.println("DEBUG (Ricette): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                dietaStage = new Stage(); // Crea una nuova finestra per la dieta.
                dietaStage.setScene(new Scene(visualizzaDietaRoot));
                dietaStage.show(); // Mostra la finestra.

            } catch (IOException e) {
                System.err.println("ERRORE (Ricette): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.");
            } catch (Exception e) {
                System.err.println("ERRORE (Ricette): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.");
            }
        } else {
            System.out.println("DEBUG (Ricette): Nessuna dieta trovata per il cliente  (ID: " + Session.getUserId() + ").");
            showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta", "Nessuna dieta assegnata, il cliente non ha diete assegnate o non è stato possibile recuperarle.");
        }
    }

    // Metodo FXML per l'accesso alla pagina Alimenti, differenziando per ruolo utente.
    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/AlimentiNutrizionista.fxml"; // Path per nutrizionisti.
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/Alimenti.fxml"; // Path per clienti.
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            // Il controller degli Alimenti si inizializza autonomamente dalla Sessione.
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            stage.setScene(new Scene(root)); // Imposta la nuova scena.
            stage.show(); // Mostra la finestra.
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la pagina Alimenti");
        }
    }

    // Metodo FXML per l'accesso alla HomePage, differenziando per ruolo utente.
    @FXML
    private void AccessoHome(ActionEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/HomePageNutrizionista.fxml"; // Path per nutrizionisti.
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/HomePage.fxml"; // Path per clienti.
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            stage.setScene(new Scene(root)); // Imposta la nuova scena.
            stage.show(); // Mostra la finestra.
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la HomePage");
        }
    }

    // Metodo FXML per l'accesso alla Pagina Profilo, differenziando per ruolo utente.
    @FXML
    private void AccessoProfilo(MouseEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/ProfiloNutrizionista.fxml"; // Path per nutrizionisti.
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/PaginaProfilo.fxml"; // Path per clienti.
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            // Il controller PaginaProfilo si inizializza autonomamente dalla Sessione.
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            stage.setScene(new Scene(root)); // Imposta la nuova scena.
            stage.show(); // Mostra la finestra.
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la Pagina Profilo");
        }
    }

    // Metodo FXML per aprire la finestra di aggiunta di una nuova ricetta.
    @FXML
    private void handleApriAggiuntaRicetta(ActionEvent event) {
        try {
            // Carica il FXML per la finestra di aggiunta ricetta.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiRicetta.fxml"));
            Parent root = loader.load();

            // Ottiene il controller della nuova finestra e gli passa un riferimento a questo controller.
            AggiungiRicetteController controller = loader.getController();
            controller.setRicettaController(this); // Passa 'this' per consentire l'aggiornamento della tabella dopo l'aggiunta.

            Stage stage = new Stage(); // Crea una nuova finestra.
            stage.setTitle("Aggiungi Ricetta");
            stage.setScene(new Scene(root));
            stage.show(); // Mostra la finestra.

        } catch (IOException e) {
            e.printStackTrace(); // Stampa l'errore di I/O.
        }
    }

    // Metodo FXML per l'accesso alla gestione delle diete per il nutrizionista.
    @FXML
    private void AccessoDieta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml"));
            Parent dietaRoot = fxmlLoader.load();
            Stage dietaStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            dietaStage.setScene(new Scene(dietaRoot)); // Imposta la nuova scena.
            dietaStage.setTitle("Diete Nutrizionista"); // Titolo più specifico.
            dietaStage.show(); // Mostra la finestra.
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Diete Nutrizionista'.");
        }
    }

    // Metodo FXML per l'accesso alla pagina Ricette, differenziando per ruolo utente.
    @FXML
    private void AccessoRicette(ActionEvent event) {
        String fxmlPath;
        if (ruolo.equalsIgnoreCase("nutrizionista")) {
            fxmlPath = "/com/matteotocci/app/RicetteNutrizionista.fxml"; // Path per nutrizionisti.
        } else if (ruolo.equalsIgnoreCase("cliente")) {
            fxmlPath = "/com/matteotocci/app/Ricette.fxml"; // Path per clienti.
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            // Il controller delle Ricette si inizializza autonomamente dalla Sessione.
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            stage.setScene(new Scene(root)); // Imposta la nuova scena.
            stage.show(); // Mostra la finestra.
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Navigazione", "Impossibile caricare la pagina Ricette");
        }
    }

    // Metodo privato per visualizzare gli alert.
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