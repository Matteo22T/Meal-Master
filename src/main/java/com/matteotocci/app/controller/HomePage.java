package com.matteotocci.app.controller;

import com.matteotocci.app.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Arc;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class HomePage {

    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Button BottoneRicette;
    @FXML
    private Button BottonePiano;
    @FXML
    private Label nomeUtenteLabelHomePage;
    @FXML private ComboBox<GiornoDieta> comboGiorniDieta;
    @FXML private Label labelKcal;
    @FXML private Label labelProteine;
    @FXML private Label labelCarboidrati;
    @FXML private Label labelGrassi;

    @FXML private Label labelKcalCorrenti;
    @FXML private Label labelProteineCorrenti;
    @FXML private Label labelCarboidratiCorrenti;
    @FXML private Label labelGrassiCorrenti;

    private String loggedInUserId; // Variabile per memorizzare l'ID dell'utente loggato

    private Dieta dietaAssegnata;

    private GiornoDieta previousDaySelection;

    @FXML private Label colazioneKcalLabel;
    @FXML private Label spuntinoKcalLabel;
    @FXML private Label pranzoKcalLabel;
    @FXML private Label merendaKcalLabel;
    @FXML private Label cenaKcalLabel;

    @FXML private Arc proteineProgressArc;
    @FXML private Arc carboidratiProgressArc;
    @FXML private Arc grassiProgressArc;

    private int targetProteine = 0; // grammi
    private int targetCarboidrati = 0; // grammi
    private int targetGrassi = 0; // grammi

    private boolean isProgrammaticChange = false;



    @FXML
    private void initialize() {
        // Recupera l'ID utente dalla sessione e inizializza la dieta e la ComboBox
        Integer userIdFromSession = Session.getUserId();

        if (userIdFromSession != null) {
            System.out.println("[DEBUG - HomePage] ID utente da Sessione: " + userIdFromSession);
            setNomeUtenteLabel();

            try {
                this.dietaAssegnata = recuperaDietaAssegnataACliente(userIdFromSession.intValue());

                if (this.dietaAssegnata != null) {
                    popolaComboGiorniDieta(this.dietaAssegnata.getId());
                    System.out.println("DEBUG (HomePage): ComboBox giorni dieta popolata per utente ID: " + userIdFromSession);
                    // Inizializza previousDaySelection con la selezione iniziale
                    comboGiorniDieta.getSelectionModel().clearSelection();
                    previousDaySelection = null;
                    if (!selectCurrentDayIfExists()) { // restituisce true se ha selezionato qualcosa
                        restoreDaySelectionFromSession();
                    }
                    javafx.application.Platform.runLater(() -> {
                        GiornoDieta giornoSelezionato = comboGiorniDieta.getSelectionModel().getSelectedItem();
                        if (giornoSelezionato != null) {
                            SessionGiornoDieta.setGiornoDietaSelezionato(giornoSelezionato);
                            targetProteine = (int) Math.round(giornoSelezionato.getProteine());
                            targetGrassi = (int) Math.round(giornoSelezionato.getGrassi());
                            targetCarboidrati = (int) Math.round(giornoSelezionato.getCarboidrati());

                            labelKcal.setText(Math.round(giornoSelezionato.getKcal()) + " kcal");
                            labelProteine.setText(targetProteine + " g");
                            labelCarboidrati.setText(targetCarboidrati + " g");
                            labelGrassi.setText(targetGrassi + " g");

                            aggiornaLabelKcalPerPasto();

                            previousDaySelection = giornoSelezionato;
                        } else {
                            resetAllLabelsAndProgress();
                        }
                    });
                } else {
                    System.out.println("DEBUG (HomePage): Nessuna dieta trovata per l'utente ID: " + userIdFromSession + ". ComboBox non popolata.");
                    if (comboGiorniDieta != null) {
                        comboGiorniDieta.getItems().clear();
                    }
                    labelKcal.setText("0 kcal");
                    labelProteine.setText("0 g");
                    labelCarboidrati.setText("0 g");
                    labelGrassi.setText("0 g");
                }
            } catch (Exception e) {
                System.err.println("ERRORE: Errore durante il recupero dieta o popolamento ComboBox: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore Caricamento Dati", "Impossibile caricare i dati della dieta.", "Dettagli: " + e.getMessage());
            }
        } else {
            System.out.println("[DEBUG - HomePage] Session.getUserId() è null. Utente non loggato o sessione non impostata.");
            showAlert(Alert.AlertType.WARNING, "Accesso Negato", "Utente non loggato",
                    "Per accedere, è necessario effettuare il login.");
            labelKcal.setText("0 kcal");
            labelProteine.setText("0 g");
            labelCarboidrati.setText("0 g");
            labelGrassi.setText("0 g");
        }

        // Aggiungi un listener alla ComboBox per aggiornare i totali quando la selezione cambia
        if (comboGiorniDieta != null) {
            comboGiorniDieta.valueProperty().addListener((obs, oldVal, newVal) -> {
                System.out.println(isProgrammaticChange);
                if (isProgrammaticChange) {
                    isProgrammaticChange = false; // resetta il flag
                    System.out.println("nooooo");
                    return;
                }
                // Evita di mostrare l'alert all'inizializzazione o se la selezione non cambia
                if (oldVal == null || oldVal.equals(newVal)) {
                    previousDaySelection = newVal; // Aggiorna la selezione precedente
                    if (newVal != null) {
                        targetProteine = (int) Math.round(newVal.getProteine());
                        targetGrassi = (int) Math.round(newVal.getGrassi());
                        targetCarboidrati = (int) Math.round(newVal.getCarboidrati());

                        labelKcal.setText(Math.round(newVal.getKcal()) + " kcal");
                        labelProteine.setText(targetProteine + " g");
                        labelCarboidrati.setText(targetCarboidrati + " g");
                        labelGrassi.setText(targetGrassi + " g");

                        // CORREZIONE: Aggiorna i dati quando cambia la selezione
                        aggiornaLabelKcalPerPasto();
                    } else {
                        labelKcal.setText("0 kcal");
                        labelProteine.setText("0 g");
                        labelCarboidrati.setText("0 g");
                        labelGrassi.setText("0 g");
                        resetAllLabelsAndProgress();
                    }
                    return;
                }

                // Se la selezione è cambiata, mostra l'alert di conferma
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Conferma Cambio Giorno");
                alert.setHeaderText("Sei sicuro di voler cambiare giorno?");
                alert.setContentText("Perderai i pasti mangiati finora per il giorno corrente.");

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Prima elimina i pasti del giorno precedente
                    if (oldVal != null) {
                        eliminaPastiSpecificiGiorno(oldVal.getIdGiornoDieta());
                    }

                    // Aggiorna i target per il nuovo giorno
                    if (newVal != null) {
                        SessionGiornoDieta.setGiornoDietaSelezionato(newVal);
                        targetProteine = (int) Math.round(newVal.getProteine());
                        targetGrassi = (int) Math.round(newVal.getGrassi());
                        targetCarboidrati = (int) Math.round(newVal.getCarboidrati());

                        labelKcal.setText(Math.round(newVal.getKcal()) + " kcal");
                        labelProteine.setText(targetProteine + " g");
                        labelCarboidrati.setText(targetCarboidrati + " g");
                        labelGrassi.setText(targetGrassi + " g");

                        // Aggiorna i dati per il nuovo giorno
                        aggiornaLabelKcalPerPasto();
                    } else {
                        labelKcal.setText("0 kcal");
                        labelProteine.setText("0 g");
                        labelCarboidrati.setText("0 g");
                        labelGrassi.setText("0 g");
                        resetAllLabelsAndProgress();
                    }
                    previousDaySelection = newVal;
                } else {
                    // L'utente ha annullato, ripristina la selezione precedente
                    isProgrammaticChange = true;
                    javafx.application.Platform.runLater(() -> {
                        comboGiorniDieta.getSelectionModel().select(oldVal);
                    });
                }
            });
        }


    }



    private void setNomeUtenteLabel() {
        String nomeUtente = getNomeUtenteDalDatabase(Session.getUserId().toString());
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            nomeUtenteLabelHomePage.setText(nomeUtente);
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Fallback text
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String url = "jdbc:sqlite:database.db"; // Assicurati che sia il percorso corretto
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
        }
        return nomeUtente;
    }



    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent root = fxmlLoader.load();

            Alimenti controller = fxmlLoader.getController();
            controller.setLoggedInUserId(this.loggedInUserId);

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
            Parent loginRoot = fxmlLoader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();
            ((Stage) BottoneRicette.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();
            PaginaProfilo profileController = fxmlLoader.getController();

            if (loggedInUserId != null) {
                System.out.println("[DEBUG - HomePage] ID utente da passare a Profilo: " + loggedInUserId);
                profileController.setUtenteCorrenteId(loggedInUserId);
            } else {
                System.out.println("[DEBUG - HomePage] ID utente non ancora disponibile per il Profilo.");
            }

            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stage dietaStage; // Per gestire apertura/chiusura della finestra dieta

    @FXML
    private void AccessoPianoAlimentare(ActionEvent event) {
        if (dietaAssegnata != null) {
            try {
                // Chiudi la finestra precedente se è già aperta
                if (dietaStage != null && dietaStage.isShowing()) {
                    dietaStage.close();
                }


                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                Parent visualizzaDietaRoot = fxmlLoader.load();

                // PASSO 2: Ottieni il controller della nuova finestra
                VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();

                // PASSO 3: Passa l'oggetto Dieta al controller della nuova finestra
                visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                System.out.println("DEBUG (HomePage): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                dietaStage = new Stage();
                dietaStage.setScene(new Scene(visualizzaDietaRoot));
                dietaStage.show();

            } catch (IOException e) {
                System.err.println("ERRORE (HomePage): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.", "Verificare il percorso del file FXML.");
            } catch (Exception e) {
                System.err.println("ERRORE (HomePage): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.", "Dettagli: " + e.getMessage());
            }
        } else {
            System.out.println("DEBUG (HomePage): Nessuna dieta trovata per il cliente  (ID: " + Session.getUserId() + ").");
            showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta", "Nessuna dieta assegnata",
                    "Il cliente non ha diete assegnate o non è stato possibile recuperarle.");
        }
    }

    private void popolaComboGiorniDieta(int idDieta) {
        ObservableList<GiornoDieta> giorniList = FXCollections.observableArrayList();
        String query = "SELECT id_giorno_dieta, nome_giorno, calorie_giorno, proteine_giorno, carboidrati_giorno, grassi_giorno " +
                "FROM Giorno_dieta WHERE id_dieta = ? ORDER BY id_giorno_dieta ASC";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idDieta);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int idGiorno = rs.getInt("id_giorno_dieta");
                String nome = rs.getString("nome_giorno");
                double kcal = rs.getDouble("calorie_giorno");
                double proteine = rs.getDouble("proteine_giorno");
                double carboidrati = rs.getDouble("carboidrati_giorno");
                double grassi = rs.getDouble("grassi_giorno");

                giorniList.add(new GiornoDieta(idGiorno, nome, kcal, proteine, carboidrati, grassi));
            }
            comboGiorniDieta.setItems(giorniList);
            if (!giorniList.isEmpty()) {
                comboGiorniDieta.getSelectionModel().selectFirst(); // Seleziona il primo elemento di default
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL: Errore durante il popolamento della ComboBox dei giorni dieta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String url = "jdbc:sqlite:database.db";
        // Query per recuperare la dieta con l'ID del cliente
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        try (Connection conn = SQLiteConnessione.connector(); // Usa SQLiteConnessione.connector()
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                dieta = new Dieta(
                        rs.getInt("id"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine"),
                        rs.getInt("id_nutrizionista"), // Assicurati che il costruttore di Dieta supporti questi campi
                        rs.getInt("id_cliente")
                );
                System.out.println("DEBUG (HomePageNutrizionista): Recuperata dieta '" + dieta.getNome() + "' (ID: " + dieta.getId() + ") per cliente ID: " + idCliente);
            } else {
                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (HomePageNutrizionista): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta;
    }


    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @FXML
    private void handleColazioneAdd(ActionEvent event) {
        aggiungiPasto("Colazione", colazioneKcalLabel);
    }

    @FXML
    private void handleSpuntinoAdd(ActionEvent event) {
        aggiungiPasto("Spuntino", spuntinoKcalLabel);
    }

    @FXML
    private void handlePranzoAdd(ActionEvent event) {
        aggiungiPasto("Pranzo", pranzoKcalLabel);
    }

    @FXML
    private void handleMerendaAdd(ActionEvent event) {
        aggiungiPasto("Merenda", merendaKcalLabel);
    }

    @FXML
    private void handleCenaAdd(ActionEvent event) {
        aggiungiPasto("Cena", cenaKcalLabel);
    }
    private void aggiungiPasto(String tipoPasto, Label mealCaloriesLabel) {
        GiornoDieta giornoSelezionato = comboGiorniDieta.getSelectionModel().getSelectedItem();
        if (giornoSelezionato == null) {
            showAlert(Alert.AlertType.WARNING, "Selezione Giorno", "Nessun giorno selezionato", "Seleziona un giorno della dieta prima di aggiungere un pasto.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiPasto.fxml"));
            Parent root = loader.load();

            AggiungiPasto aggiungiPastoController = loader.getController();
            aggiungiPastoController.setPastoData(giornoSelezionato.getIdGiornoDieta(), tipoPasto, Session.getUserId()); // Pass the day ID and meal type
            aggiungiPastoController.setHomePageController(this); // Passaggio del controller

            Stage stage = new Stage();
            stage.setTitle("Aggiungi " + tipoPasto);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Makes it a modal window
            stage.initOwner(((Node) mealCaloriesLabel).getScene().getWindow()); // Set owner to current window
            stage.showAndWait();

            // After the AggiungiPasto window is closed, refresh the daily totals and meal totals
            // You might want to get the updated GiornoDieta object or re-calculate totals
            // For simplicity, let's assume AggiungiPastoController will update the DB.
            // Then, we just need to re-fetch/update the GiornoDieta object.
            //refreshCurrentDayTotals();

        } catch (IOException e) {
            System.err.println("ERRORE (HomePage): Errore caricamento FXML AggiungiPasto: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Caricamento", "Impossibile aprire la finestra di aggiunta pasto.", "Dettagli: " + e.getMessage());
        }
    }

    public void eliminaPastiGiornalieriOggi() {
        // Ottieni il giorno attualmente selezionato
        GiornoDieta giornoSelezionato = comboGiorniDieta.getSelectionModel().getSelectedItem();
        if (giornoSelezionato == null) return;

        // CORREZIONE: Elimina solo i pasti del giorno specifico selezionato
        String sql = "DELETE FROM PastiGiornalieri WHERE id_cliente = ? AND data = ? AND id_giorno_dieta = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Session.getUserId());
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.setInt(3, giornoSelezionato.getIdGiornoDieta()); // CORREZIONE: Aggiungi il parametro

            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("DEBUG: Eliminati " + rowsDeleted + " pasti per il giorno " + giornoSelezionato.getNomeGiorno());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void eliminaPastiSpecificiGiorno(int idGiornoDieta) {
        String sql = "DELETE FROM PastiGiornalieri WHERE id_cliente = ? AND data = ? AND id_giorno_dieta = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Session.getUserId());
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.setInt(3, idGiornoDieta);

            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("DEBUG: Eliminati " + rowsDeleted + " pasti per il giorno con ID " + idGiornoDieta);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void aggiornaLabelKcalPerPasto() {
        Integer userId = Session.getUserId();
        if (userId == null) return;

        // Ottieni il giorno attualmente selezionato nella ComboBox
        GiornoDieta giornoSelezionato = comboGiorniDieta.getSelectionModel().getSelectedItem();
        if (giornoSelezionato == null) {
            // Se non c'è nessun giorno selezionato, resetta tutto a 0
            resetAllLabelsAndProgress();
            return;
        }

        String dataCorrente = LocalDate.now().toString();

        String query = """
        SELECT pasto, kcal, proteine, carboidrati, grassi
        FROM PastiGiornalieri
        WHERE id_cliente = ? AND data = ? AND id_giorno_dieta = ?
    """;

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setString(2, dataCorrente);
            stmt.setInt(3, giornoSelezionato.getIdGiornoDieta()); // CORREZIONE: Aggiungi il parametro

            ResultSet rs = stmt.executeQuery();

            // Reset iniziale a 0
            resetAllLabelsAndProgress();

            int totaleKcal = 0;
            int totaleProteine = 0;
            int totaleCarboidrati = 0;
            int totaleGrassi = 0;

            while (rs.next()) {
                String pasto = rs.getString("pasto");
                int kcal = (int) Math.round(rs.getDouble("kcal"));
                int proteine = (int) Math.round(rs.getDouble("proteine"));
                int carboidrati = (int) Math.round(rs.getDouble("carboidrati"));
                int grassi = (int) Math.round(rs.getDouble("grassi"));

                totaleKcal += kcal;
                totaleProteine += proteine;
                totaleCarboidrati += carboidrati;
                totaleGrassi += grassi;

                // Aggiorna le label per ogni pasto
                switch (pasto) {
                    case "Colazione" -> colazioneKcalLabel.setText(kcal + " kcal");
                    case "Spuntino"  -> spuntinoKcalLabel.setText(kcal + " kcal");
                    case "Pranzo"    -> pranzoKcalLabel.setText(kcal + " kcal");
                    case "Merenda"   -> merendaKcalLabel.setText(kcal + " kcal");
                    case "Cena"      -> cenaKcalLabel.setText(kcal + " kcal");
                }
            }

            // Aggiorna i totali correnti
            labelKcalCorrenti.setText(totaleKcal + " /");
            labelProteineCorrenti.setText(totaleProteine + " /");
            labelCarboidratiCorrenti.setText(totaleCarboidrati + " /");
            labelGrassiCorrenti.setText(totaleGrassi + " /");

            // Aggiorna il progresso degli archi con i totali
            updateProgressArc(proteineProgressArc, totaleProteine, targetProteine);
            updateProgressArc(carboidratiProgressArc, totaleCarboidrati, targetCarboidrati);
            updateProgressArc(grassiProgressArc, totaleGrassi, targetGrassi);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore DB", "Impossibile caricare le kcal per pasto.", "Dettagli: " + e.getMessage());
        }
    }

    private void resetAllLabelsAndProgress() {
        // Reset label pasti
        colazioneKcalLabel.setText("0 kcal");
        spuntinoKcalLabel.setText("0 kcal");
        pranzoKcalLabel.setText("0 kcal");
        merendaKcalLabel.setText("0 kcal");
        cenaKcalLabel.setText("0 kcal");

        // Reset label correnti
        labelKcalCorrenti.setText("0 /");
        labelProteineCorrenti.setText("0 /");
        labelCarboidratiCorrenti.setText("0 /");
        labelGrassiCorrenti.setText("0 /");

        // Reset archi di progresso
        resetProgressArcs();
    }

    /**
     * Aggiorna un singolo arco di progresso con animazione
     */
    private void updateProgressArc(Arc arc, int current, double target) {
        // Calcola la percentuale (da 0 a 1)
        double percentage = Math.min(current / target, 1.0);

        // Converte in gradi (360° = cerchio completo)
        double targetAngle = -360.0 * percentage; // Negativo per andare in senso orario

        // Aggiorna le classi CSS in base al progresso
        updateProgressStyles(arc, percentage);

        // Animazione fluida
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(500),
                        new KeyValue(arc.lengthProperty(), targetAngle)
                )
        );
        timeline.play();
    }

    /**
     * Aggiorna gli stili CSS dell'arco in base alla percentuale
     */
    private void updateProgressStyles(Arc arc, double percentage) {
        // Rimuovi tutte le classi di progresso precedenti
        arc.getStyleClass().removeAll("progress-low", "progress-medium", "progress-high", "progress-complete");

        // Aggiungi la classe appropriata in base alla percentuale
        if (percentage >= 1.0) {
            arc.getStyleClass().add("progress-complete");
        } else if (percentage >= 0.7) {
            arc.getStyleClass().add("progress-high");
        } else if (percentage >= 0.3) {
            arc.getStyleClass().add("progress-medium");
        } else {
            arc.getStyleClass().add("progress-low");
        }
    }

    /**
     * Resetta tutti gli archi di progresso a zero con animazione
     */
    private void resetProgressArcs() {
        resetSingleProgressArc(proteineProgressArc);
        resetSingleProgressArc(carboidratiProgressArc);
        resetSingleProgressArc(grassiProgressArc);
    }

    /**
     * Resetta un singolo arco di progresso a zero
     */
    private void resetSingleProgressArc(Arc arc) {
        // Rimuovi tutte le classi di progresso
        arc.getStyleClass().removeAll("progress-low", "progress-medium", "progress-high", "progress-complete");

        // Animazione per tornare a 0
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(arc.lengthProperty(), 0.0)
                )
        );
        timeline.play();
    }

    private boolean selectCurrentDayIfExists() {
        Integer userId = Session.getUserId();
        if (userId == null) return false;

        String dataCorrente = LocalDate.now().toString();

        String query = """
        SELECT gd.id_giorno_dieta
        FROM Giorno_dieta gd
        INNER JOIN PastiGiornalieri pg ON gd.id_giorno_dieta = pg.id_giorno_dieta
        WHERE pg.id_cliente = ? AND pg.data = ?
        GROUP BY gd.id_giorno_dieta
    """;

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, dataCorrente);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int idGiorno = rs.getInt("id_giorno_dieta");
                for (GiornoDieta giorno : comboGiorniDieta.getItems()) {
                    if (giorno.getIdGiornoDieta() == idGiorno) {
                        isProgrammaticChange = false;
                        previousDaySelection = giorno;
                        comboGiorniDieta.getSelectionModel().select(giorno);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore DB", "Impossibile caricare il giorno corrente.", "Dettagli: " + e.getMessage());
        }

        return false;
    }


    private void restoreDaySelectionFromSession() {
        GiornoDieta giorno = SessionGiornoDieta.getGiornoDietaSelezionato();
        if (giorno != null && comboGiorniDieta.getItems().contains(giorno)) {
            isProgrammaticChange = true;
            comboGiorniDieta.getSelectionModel().select(giorno);
            previousDaySelection = giorno;
        }
    }

}

