package com.matteotocci.app.controller;

import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label; // Manteniamo Label per 'messaggioLabel'
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DateCell;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class NuovaDieta {

    @FXML
    private TextField titoloPianoTextField;

    @FXML
    private DatePicker dataInizioDatePicker;
    @FXML
    private DatePicker dataFineDatePicker;
    // RIMOSSO: @FXML private Label numeroGiorniCalcolatoLabel; // Questo Label non esiste più nel FXML

    @FXML
    private Button avantiButton;

    @FXML
    private Label messaggioLabel; // Questo Label è ancora presente per i messaggi di errore (Alert)

    private String titoloPiano;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private int numeroGiorni; // Questa variabile continuerà a conservare il numero di giorni calcolato

    @FXML
    public void initialize() {
        // Imposta un CellFactory per DatePicker per disabilitare le date passate
        dataInizioDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        dataFineDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate minDate = (dataInizioDatePicker.getValue() != null) ? dataInizioDatePicker.getValue() : LocalDate.now();
                setDisable(empty || date.isBefore(minDate));
            }
        });

        // Aggiungi listener ai DatePicker per calcolare il numero di giorni internamente
        // Non aggiorneranno più un Label visibile
        dataInizioDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> calcolaNumeroGiorni());
        dataFineDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> calcolaNumeroGiorni());

        // Inizializza il numero di giorni a 0 all'avvio (internamente)
        numeroGiorni = 0;
        messaggioLabel.setText(""); // Assicurati che il messaggio rosso sia vuoto all'inizio
    }

    private void calcolaNumeroGiorni() {
        LocalDate start = dataInizioDatePicker.getValue();
        LocalDate end = dataFineDatePicker.getValue();

        // Pulisci il messaggio rosso esistente (se usato per altri scopi)
        messaggioLabel.setText("");

        if (start != null && end != null) {
            if (end.isBefore(start)) {
                // Il messaggio di errore verrà gestito dall'Alert al click su Avanti
                numeroGiorni = 0; // Imposta a 0 se le date non sono valide
                return;
            }
            long days = ChronoUnit.DAYS.between(start, end) + 1;
            numeroGiorni = (int) days; // Aggiorna la variabile interna
        } else {
            numeroGiorni = 0; // Azzera se le date non sono complete
        }
        // NON c'è più una chiamata a numeroGiorniCalcolatoLabel.setText() qui
    }

    @FXML
    private void switchToAggiungiAlimenti(ActionEvent event) {
        titoloPiano = titoloPianoTextField.getText();
        dataInizio = dataInizioDatePicker.getValue();
        dataFine = dataFineDatePicker.getValue();

        String errorMessage = null;

        // Validazione Input
        if (titoloPiano == null || titoloPiano.trim().isEmpty()) {
            errorMessage = "Il titolo del piano non può essere vuoto.";
        } else if (dataInizio == null || dataFine == null) {
            errorMessage = "Devi selezionare sia la Data Inizio che la Data Fine.";
        } else {
            // Ricalcola numeroGiorni qui per essere sicuro che sia aggiornato al momento del click
            // (Anche se i listener lo fanno, è una buona pratica per le validazioni finali)
            LocalDate start = dataInizioDatePicker.getValue();
            LocalDate end = dataFineDatePicker.getValue();
            if (start != null && end != null) {
                if (end.isBefore(start)) {
                    errorMessage = "La Data Fine non può essere precedente alla Data Inizio.";
                } else {
                    numeroGiorni = (int) (ChronoUnit.DAYS.between(start, end) + 1);
                    if (numeroGiorni <= 0) { // Questo dovrebbe essere già coperto da isBefore, ma è un check di sicurezza
                        errorMessage = "L'intervallo di date non genera un numero di giorni valido. Controlla le date.";
                    }
                }
            } else { // Questo caso dovrebbe essere già coperto da "dataInizio == null || dataFine == null"
                errorMessage = "Errore interno: date non valide per il calcolo dei giorni.";
            }
        }


        if (errorMessage != null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Errore di input");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return;
        }

        // Se non ci sono errori di input, procedi con il salvataggio e la transizione
        System.out.println("---- Inizio salvataggio piano nel DB ----");
        Connection conn = null;
        PreparedStatement psDieta = null;
        PreparedStatement psGiorno = null;
        ResultSet generatedKeys = null;

        try {
            conn = SQLiteConnessione.connector();
            System.out.println("Connessione stabilita: " + (conn != null));

            conn.setAutoCommit(false);

            String insertDietaSql = "INSERT INTO Diete (id_cliente, nome_dieta, data_inizio, data_fine, id_nutrizionista) VALUES (NULL, ?, ?, ?, ?)";
            psDieta = conn.prepareStatement(insertDietaSql, Statement.RETURN_GENERATED_KEYS);
            psDieta.setString(1, titoloPiano);
            psDieta.setString(2, dataInizio.toString());
            psDieta.setString(3, dataFine.toString());
            psDieta.setInt(4, Session.getUserId());
            psDieta.executeUpdate();

            generatedKeys = psDieta.getGeneratedKeys();
            int idDieta;
            if (generatedKeys.next()) {
                idDieta = generatedKeys.getInt(1);
                System.out.println("ID Dieta generato: " + idDieta);
            } else {
                throw new SQLException("Errore nella creazione della dieta: nessun ID ottenuto.");
            }

            String insertGiornoSql = "INSERT INTO Giorno_dieta (id_dieta, calorie_giorno, proteine_giorno, carboidrati_giorno, grassi_giorno) VALUES (?,0,0,0,0)";
            psGiorno = conn.prepareStatement(insertGiornoSql);
            for (int i = 1; i <= numeroGiorni; i++) {
                psGiorno.setInt(1, idDieta);
                psGiorno.addBatch();
            }

            psGiorno.executeBatch();
            conn.commit();

            System.out.println("Salvataggio piano dieta completato con successo nel DB.");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiGiornoDieta.fxml"));
            Parent aggiungiAlimentiRoot = loader.load();

            AggiungiGiornoDieta aggiungiAlimentiController = loader.getController();
            aggiungiAlimentiController.setTitoloPiano(titoloPiano);
            aggiungiAlimentiController.setNumeroGiorni(numeroGiorni);
            aggiungiAlimentiController.setIdDieta(idDieta);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(aggiungiAlimentiRoot));
            stage.setTitle("Aggiungi Alimenti al Piano");
            stage.show();

        } catch (SQLException e) {
            System.err.println("Errore SQL durante il salvataggio del piano dieta: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Errore durante il rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Errore Database");
            alert.setHeaderText("Errore durante il salvataggio della dieta.");
            alert.setContentText("Si è verificato un problema durante la connessione o l'operazione sul database. Riprova più tardi.");
            alert.showAndWait();
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della schermata successiva: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Errore Applicazione");
            alert.setHeaderText("Errore nel caricamento della schermata successiva.");
            alert.setContentText("Impossibile caricare la prossima schermata. Contatta il supporto.");
            alert.showAndWait();
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (psDieta != null) psDieta.close();
                if (psGiorno != null) psGiorno.close();
                if (conn != null) conn.close();
                System.out.println("Risorse del DB chiuse correttamente.");
            } catch (SQLException ex) {
                System.err.println("Errore chiusura risorse DB: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}