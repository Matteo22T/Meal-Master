package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
import com.matteotocci.app.model.Dieta; // Importa la classe Dieta
import com.matteotocci.app.model.SQLiteConnessione; // Importa la classe SQLiteConnessione

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class PaginaProfilo implements Initializable{


    @FXML
    private Label nomeUtenteSidebarLabel;


    @FXML
    private TextField nomeTextField;

    @FXML
    private TextField cognomeTextField;

    @FXML
    private ComboBox<String> sessoComboBox;

    @FXML
    private TextField dataNascitaTextField;

    @FXML
    private TextField altezzaTextField;

    @FXML
    private TextField pesoAttualeTextField;

    private Dieta dietaAssegnata;

    @FXML
    private ComboBox<String> nutrizionistaComboBox;

    @FXML
    private GridPane gridPane;




    @FXML
    private ComboBox<String> livelloAttivitaComboBox;


    private String nutrizionistaPrecedente; // Variabile per memorizzare la selezione precedente


    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ObservableList<String> livelliAttivita = FXCollections.observableArrayList(
                "Sedentario",
                "Leggermente Attivo",
                "Moderatamente Attivo",
                "Molto Attivo",
                "Estremamente Attivo"
        );
        livelloAttivitaComboBox.setItems(livelliAttivita);

        ObservableList<String> sessi = FXCollections.observableArrayList(
                "Maschio",
                "Femmina",
                "Altro" // Opzione aggiuntiva se desiderata
        );
        sessoComboBox.setItems(sessi);

        ObservableList<String> nutrizionisti = getNutrizionisti();
        nutrizionistaComboBox.setItems(nutrizionisti);



        // Chiama il metodo per caricare i dati utente non appena il controller è pronto
        inizializzaDatiUtente();
        // Recupera la dieta assegnata all'avvio della pagina
        dietaAssegnata=recuperaDietaAssegnataACliente(Session.getUserId());


        nutrizionistaPrecedente = nutrizionistaComboBox.getSelectionModel().getSelectedItem();
        System.out.println(nutrizionistaPrecedente);
    }

    private Map<String, Integer> mappaNutrizionisti = new HashMap<>();


    private ObservableList<String> getNutrizionisti() {
        ObservableList<String> nutrizionisti = FXCollections.observableArrayList();
        String query = "SELECT id,Nome, Cognome FROM Utente WHERE ruolo = 'nutrizionista'"; // Query per selezionare i nutrizionisti

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("Nome") + " " + rs.getString("Cognome");
                nutrizionisti.add(nome); // Aggiungi il nome alla lista
                mappaNutrizionisti.put(nome, id); // Salva il nome associato all'ID
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nutrizionisti;
    }

    private void inizializzaDatiUtente() {
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID direttamente dalla Sessione

        if (userIdFromSession != null) {

            // Recupera nome e cognome dell'utente dalla tabella Utente
            String nome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Nome");
            String cognome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Cognome");

            // Imposta il nome completo nella sidebar
            String nomeCompleto = (nome != null ? nome : "") + " " + (cognome != null ? cognome : "");
            nomeUtenteSidebarLabel.setText(nomeCompleto.trim());

            // Imposta i campi TextField del nome e cognome
            nomeTextField.setText(nome != null ? nome : "");
            cognomeTextField.setText(cognome != null ? cognome : "");

            // Recupera i dati specifici del cliente dalla tabella Clienti
            String altezza = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "altezza_cm");
            String peso = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "peso_kg");
            String livelloAttivita = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "livello_attivita");
            String dataNascita = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "data_di_nascita");
            String sesso = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "sesso");
            if (sesso != null && !sesso.isEmpty()) {
                sesso = sesso.substring(0, 1).toUpperCase() + sesso.substring(1).toLowerCase();
            }
            String idNutrizionista = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "id_nutrizionista");

            // Imposta i valori nei TextField
            if (altezzaTextField != null) {
                altezzaTextField.setText(altezza != null ? altezza : "");
            }
            if (pesoAttualeTextField != null) {
                pesoAttualeTextField.setText(peso != null ? peso : "");
            }
            if (dataNascitaTextField != null) {
                dataNascitaTextField.setText(dataNascita != null ? dataNascita : "");
            }
            if (sessoComboBox != null) {
                if (sesso != null && !sesso.isEmpty()) {
                    sessoComboBox.getSelectionModel().select(sesso);
                } else {
                    sessoComboBox.getSelectionModel().clearSelection();
                    System.out.println("[DEBUG] Nessun sesso trovato per l'utente.");
                }
            }

            // --- Aggiornato per le ComboBox ---
            // Imposta la selezione nella ComboBox del livello di attività
            if (livelloAttivita != null && !livelloAttivita.isEmpty()) {
                livelloAttivitaComboBox.getSelectionModel().select(livelloAttivita);
            } else {
                livelloAttivitaComboBox.getSelectionModel().clearSelection(); // Cancella la selezione se non ci sono dati
                System.out.println("[DEBUG] Nessun livello di attività trovato per l'utente.");
            }

            // Imposta la selezione nella ComboBox del nutrizionista
            if (idNutrizionista != null && !idNutrizionista.isEmpty()) {
                // Recupera il nome e cognome del nutrizionista dall'ID
                String nomeNutrizionista = getDatoUtenteDalDatabase("Utente", idNutrizionista, "Nome");
                String cognomeNutrizionista = getDatoUtenteDalDatabase("Utente", idNutrizionista, "Cognome");

                if (nomeNutrizionista != null && cognomeNutrizionista != null) {
                    String nomeCompletoNutrizionista = nomeNutrizionista + " " + cognomeNutrizionista;
                    // Seleziona il nutrizionista nella ComboBox usando il nome completo
                    nutrizionistaComboBox.getSelectionModel().select(nomeCompletoNutrizionista);
                } else {
                    nutrizionistaComboBox.getSelectionModel().clearSelection(); // Cancella la selezione se il nome non è trovato
                    System.out.println("[DEBUG] Nutrizionista con ID " + idNutrizionista + " non trovato o dati incompleti.");
                }
            } else {
                nutrizionistaComboBox.getSelectionModel().clearSelection(); // Cancella la selezione se nessun nutrizionista è assegnato
                System.out.println("[DEBUG] Nessun nutrizionista assegnato all'utente.");
            }

        } else {
            // Se l'ID utente non è disponibile dalla Sessione, svuota tutti i campi
            System.err.println("[ERROR] ID utente non disponibile dalla Sessione. Impossibile recuperare i dati del profilo.");
            nomeUtenteSidebarLabel.setText("Utente Sconosciuto");
            nomeTextField.setText("");
            cognomeTextField.setText("");
            if (sessoComboBox != null) sessoComboBox.getSelectionModel().clearSelection();
            if (dataNascitaTextField != null) dataNascitaTextField.setText("");
            if (altezzaTextField != null) altezzaTextField.setText("");
            if (pesoAttualeTextField != null) pesoAttualeTextField.setText("");
            // Svuota le selezioni delle ComboBox
            if (livelloAttivitaComboBox != null) livelloAttivitaComboBox.getSelectionModel().clearSelection();
            if (nutrizionistaComboBox != null) nutrizionistaComboBox.getSelectionModel().clearSelection();
        }
    }

    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) {
        String valore = null;
        String url = "jdbc:sqlite:database.db";
        String query;
        String idColumn = "id";
        if (tabella.equals("Clienti")) {
            idColumn = "id_cliente";
        }
        query = "SELECT " + campo + " FROM " + tabella + " WHERE " + idColumn + " = ?";
        System.out.println("[DEBUG] Query per " + tabella + " eseguita: " + query + " con ID: " + userId + ", Campo: " + campo);

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                valore = rs.getString(campo);
            } else {
                System.out.println("[DEBUG] Nessun dato trovato con ID: " + userId + " nella tabella " + tabella + " per il campo " + campo);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage());
        }
        return valore;
    }

    // --- Metodi di Navigazione ---

    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Ricette.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostraSchermataModificaPassword(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ModificaPassword.fxml"));
            Parent modificaPasswordRoot = fxmlLoader.load();

            Stage modificaPasswordStage = new Stage();
            modificaPasswordStage.setTitle("Modifica Password");
            modificaPasswordStage.setScene(new Scene(modificaPasswordRoot));
            modificaPasswordStage.setResizable(false);
            modificaPasswordStage.setFullScreen(false);
            modificaPasswordStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata di modifica password.");
        }
    }

    @FXML
    private void vaiAllaHomePage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la HomePage. Contattare l'amministratore.");
        }
    }

    @FXML
    private void eseguiLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert-confirmation");
        alert.setTitle("Conferma Logout");
        alert.setHeaderText("Sei sicuro di voler uscire?");
        alert.setContentText("Clicca OK per confermare o Annulla per rimanere.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PrimaPagina.fxml"));
                Parent loginRoot = fxmlLoader.load();
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene loginScene = new Scene(loginRoot);
                currentStage.setResizable(false);
                currentStage.setFullScreen(false);
                currentStage.setScene(loginScene);
                currentStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Logout", "Impossibile effettuare il logout. Contattare l'amministratore.");
            }
        }
    }
    @FXML
    private void mostraBMI(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/BMI.fxml"));
            Parent bmiRoot = fxmlLoader.load();

            Stage bmiStage = new Stage();
            bmiStage.setTitle("Calcolo BMI");
            bmiStage.setScene(new Scene(bmiRoot));
            bmiStage.setResizable(false);
            bmiStage.setFullScreen(false);
            bmiStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata di calcolo BMI.");
        }
    }

    // Metodo per recuperare la dieta assegnata al cliente (copiato da Ricette)
    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
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
            System.err.println("ERRORE SQL (PaginaProfilo): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta;
    }



    // Metodo per visualizzare gli alert
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Apply the base style class
            // Add specific style class based on AlertType for custom styling
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Corrected error message
        }

        alert.showAndWait();
    }


    @FXML
    private void AccessoPianoAlimentare(ActionEvent event) {
        if (dietaAssegnata != null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                Parent visualizzaDietaRoot = fxmlLoader.load();

                VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();

                // Passa l'oggetto Dieta recuperato al controller della pagina di visualizzazione
                visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                System.out.println("DEBUG (PaginaProfilo): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                Stage dietaStage = new Stage();
                dietaStage.setScene(new Scene(visualizzaDietaRoot));
                dietaStage.show();

            } catch (IOException e) {
                System.err.println("ERRORE (PaginaProfilo): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.");
            } catch (Exception e) {
                System.err.println("ERRORE (PaginaProfilo): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.");
            }
        } else {
            System.out.println("DEBUG (PaginaProfilo): Nessuna dieta trovata per il cliente (ID: " + Session.getUserId() + ").");
            showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta",
                    "Il cliente non ha diete assegnate o non è stato possibile recuperarle.");
        }
    }

    @FXML
    private void salvaProfilo(ActionEvent event) {
        Integer userIdFromSession = Session.getUserId();

        if (userIdFromSession == null) {
            showAlert(Alert.AlertType.ERROR, "Errore di salvataggio",  "Impossibile salvare il profilo senza un ID utente valido.");
            return;
        }

        String altezzaStr = altezzaTextField.getText().trim();
        String pesoStr = pesoAttualeTextField.getText().trim();


        String sessoSelezionato = sessoComboBox.getSelectionModel().getSelectedItem();
        String livelloAttivita = livelloAttivitaComboBox.getSelectionModel().getSelectedItem();
        String nutrizionistaSelezionato = nutrizionistaComboBox.getSelectionModel().getSelectedItem();

        String dataNascitaText = dataNascitaTextField.getText().trim();
        String dataNascitaFormattedForDb = null;

        if (!dataNascitaText.isEmpty()) {
            try {
                // Tenta di parsare la data nel formato dd-MM-yyyy
                LocalDate parsedDate = LocalDate.parse(dataNascitaText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                // Formatta per il database: YYYY-MM-DD
                dataNascitaFormattedForDb = parsedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                // Errore di formato della data
                showAlert(Alert.AlertType.ERROR, "Errore Data", "Formato data non valido o data inesistente. La data di nascita deve essere nel formato GG-MM-AAAA (es. 01-01-1990).");
                return; // Ferma il salvataggio se la data non è valida
            }
        }

        // Determina se il nutrizionista è cambiato
        boolean nutrizionistaCambiato = false;
        System.out.println(nutrizionistaSelezionato+" "+nutrizionistaPrecedente);
        if (nutrizionistaPrecedente == null && nutrizionistaSelezionato != null) {
            nutrizionistaCambiato = true;
        } else if (nutrizionistaPrecedente != null && nutrizionistaSelezionato == null) {
            nutrizionistaCambiato = true;
        } else if (nutrizionistaPrecedente != null && nutrizionistaSelezionato != null && !nutrizionistaSelezionato.equals(nutrizionistaPrecedente)) {
            System.out.println("cambiato true");
            nutrizionistaCambiato = true;
        }


        Double altezza = null;
        Double peso = null;
        if (!altezzaStr.isEmpty()) {
            try {
                altezza = Double.parseDouble(altezzaStr);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Errore di formato", "Altezza non valida.");
                return;
            }
        }

        if (!pesoStr.isEmpty()) {
            try {
                peso = Double.parseDouble(pesoStr);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Errore di formato", "Peso non valido.");
                return;
            }
        }

        Integer idNutrizionista = null;
        if (nutrizionistaSelezionato != null && !nutrizionistaSelezionato.isEmpty()) {
            idNutrizionista = mappaNutrizionisti.get(nutrizionistaSelezionato);
            if (idNutrizionista == null) {
                System.err.println("[ERROR] ID nutrizionista non trovato per: " + nutrizionistaSelezionato);
                showAlert(Alert.AlertType.ERROR, "Errore Nutrizionista",  "Si prega di selezionare un nutrizionista valido dall'elenco.");
                return;
            }
        }

        try (Connection conn = SQLiteConnessione.connector()) {
            conn.setAutoCommit(false); // Inizia una transazione

            // --- PASSO 1: Controlla se il cliente esiste già ---
            boolean clienteEsiste = false;
            String checkClientQuery = "SELECT COUNT(*) FROM Clienti WHERE id_cliente = ?";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkClientQuery)) {
                pstmtCheck.setInt(1, userIdFromSession);
                ResultSet rs = pstmtCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    clienteEsiste = true;
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] Errore durante il controllo esistenza cliente: " + e.getMessage());
                throw e; // Rilancia l'eccezione per far scattare il rollback
            }

            if (clienteEsiste) {
                // --- PASSO 2a: Se il cliente esiste, esegui l'UPDATE ---
                String updateClientiQuery = "UPDATE Clienti SET altezza_cm = ?, peso_kg = ?, livello_attivita = ?, id_nutrizionista = ?, sesso = ?, data_di_nascita = ? WHERE id_cliente = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateClientiQuery)) {
                    if (altezza != null) {
                        pstmt.setDouble(1, altezza);
                    } else {
                        pstmt.setNull(1, java.sql.Types.DOUBLE);
                    }

                    if (peso != null) {
                        pstmt.setDouble(2, peso);
                    } else {
                        pstmt.setNull(2, java.sql.Types.DOUBLE);
                    }

                    pstmt.setString(3, livelloAttivita);

                    if (idNutrizionista != null) {
                        pstmt.setInt(4, idNutrizionista);
                    } else {
                        pstmt.setNull(4, java.sql.Types.INTEGER);
                    }

                    pstmt.setString(5, sessoSelezionato.toLowerCase());
                    pstmt.setString(6, dataNascitaFormattedForDb);

                    pstmt.setInt(7, userIdFromSession);
                    pstmt.executeUpdate();
                    System.out.println("[DEBUG] Profilo cliente aggiornato con successo.");
                }
            } else {
                // --- PASSO 2b: Se il cliente NON esiste, esegui l'INSERT ---
                String insertClientiQuery = "INSERT INTO Clienti (id_cliente, altezza_cm, peso_kg, livello_attivita, id_nutrizionista, sesso, data_di_nascita) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertClientiQuery)) {
                    pstmt.setInt(1, userIdFromSession); // id_cliente
                    if (altezza != null) {
                        pstmt.setDouble(2, altezza);
                    } else {
                        pstmt.setNull(2, java.sql.Types.DOUBLE);
                    }

                    if (peso != null) {
                        pstmt.setDouble(3, peso);
                    } else {
                        pstmt.setNull(3, java.sql.Types.DOUBLE);
                    }

                    pstmt.setString(4, livelloAttivita);

                    if (idNutrizionista != null) {
                        pstmt.setInt(5, idNutrizionista);
                    } else {
                        pstmt.setNull(5, java.sql.Types.INTEGER);
                    }

                    pstmt.setString(6, sessoSelezionato.toLowerCase());
                    pstmt.setString(7, dataNascitaFormattedForDb);

                    pstmt.executeUpdate();
                    System.out.println("[DEBUG] Nuovo profilo cliente creato con successo.");
                }
            }

            // --- Logica aggiunta per la gestione della dieta ---
            // ... (Questa parte rimane invariata, si applica sia dopo un UPDATE che un INSERT)
            System.out.println(dietaAssegnata);
            if (nutrizionistaCambiato && dietaAssegnata != null) {
                System.out.println("[DEBUG] Nutrizionista cambiato e dieta assegnata. Eseguo pulizia pasti giornalieri e dissociazione dieta.");

                // 1. Recupera tutti gli id_giorno_dieta associati alla dieta del cliente
                String selectGiornoDietaIdsQuery = "SELECT id_giorno_dieta FROM Giorno_dieta WHERE id_dieta = ?";
                List<Integer> giornoDietaIds = new ArrayList<>();
                try (PreparedStatement pstmtSelectGiorno = conn.prepareStatement(selectGiornoDietaIdsQuery)) {
                    pstmtSelectGiorno.setInt(1, dietaAssegnata.getId());
                    ResultSet rs = pstmtSelectGiorno.executeQuery();
                    while (rs.next()) {
                        giornoDietaIds.add(rs.getInt("id_giorno_dieta"));
                    }
                }

                // 2. Se ci sono id_giorno_dieta validi, elimina i pasti giornalieri associati
                if (!giornoDietaIds.isEmpty()) {
                    String placeholders = String.join(",", Collections.nCopies(giornoDietaIds.size(), "?"));
                    String deletePastiQuery = "DELETE FROM PastiGiornalieri WHERE id_giorno_dieta IN (" + placeholders + ")";
                    try (PreparedStatement pstmtPasti = conn.prepareStatement(deletePastiQuery)) {
                        for (int i = 0; i < giornoDietaIds.size(); i++) {
                            pstmtPasti.setInt(i + 1, giornoDietaIds.get(i));
                        }
                        int affectedRows = pstmtPasti.executeUpdate();
                        System.out.println("[DEBUG] Eliminati " + affectedRows + " pasti giornalieri associati alla dieta ID: " + dietaAssegnata.getId());
                    }
                } else {
                    System.out.println("[DEBUG] Nessun Giorno_dieta trovato per la dieta ID: " + dietaAssegnata.getId() + ". Nessun pasto giornaliero da eliminare.");
                }

                // 3. Setta id_cliente a NULL nella tabella Diete
                String updateDietaQuery = "UPDATE Diete SET id_cliente = NULL WHERE id = ?";
                try (PreparedStatement pstmtDieta = conn.prepareStatement(updateDietaQuery)) {
                    pstmtDieta.setInt(1, dietaAssegnata.getId());
                    pstmtDieta.executeUpdate();
                    System.out.println("[DEBUG] id_cliente settato a NULL per la dieta ID: " + dietaAssegnata.getId());
                }
                dietaAssegnata = null; // Resetta dietaAssegnata a null dopo averla dissociata con successo
            }

            conn.commit(); // Conferma la transazione
            showAlert(Alert.AlertType.INFORMATION, "Salvataggio completato", "Profilo aggiornato con successo!");
            inizializzaDatiUtente(); // Re-inizializza i dati per riflettere i cambiamenti
            if (nutrizionistaComboBox.getSelectionModel().getSelectedItem() != null) {
                nutrizionistaPrecedente = nutrizionistaComboBox.getSelectionModel().getSelectedItem();
            } else {
                nutrizionistaPrecedente = null;
            }
            dietaAssegnata = recuperaDietaAssegnataACliente(userIdFromSession); // Aggiorna la variabile dietaAssegnata
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Errore di database", "Impossibile salvare il profilo.");
            e.printStackTrace();
            try (Connection conn = SQLiteConnessione.connector()) {
                if (conn != null) {
                    conn.rollback(); // Annulla la transazione in caso di errore
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Errore durante il rollback: " + rollbackEx.getMessage());
                showAlert(Alert.AlertType.ERROR, "Errore Database", "Errore durante il rollback della transazione.");
            }
        }
    }



}

