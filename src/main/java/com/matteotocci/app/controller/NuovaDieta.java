package com.matteotocci.app.controller;

import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DateCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class NuovaDieta implements Initializable {

    @FXML
    private TextField titoloPianoTextField;

    @FXML
    private DatePicker dataInizioDatePicker;
    @FXML
    private DatePicker dataFineDatePicker;

    @FXML
    private Spinner<Integer> numeroGiorniSpinner;
    @FXML
    private Label erroreNumeroGiorniLabel;

    @FXML
    private Button avantiButton;

    @FXML
    private Label messaggioLabel;

    private String titoloPiano;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private int numeroGiorniCalcolato; // Maximum days allowed by date range
    private int numeroGiorniEffettivo; // Actual days from spinner, used for DB insertion

    @FXML
    public void initialize(URL url, ResourceBundle resources) {
        messaggioLabel.setText("");
        erroreNumeroGiorniLabel.setText("");

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

        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1);
        numeroGiorniSpinner.setValueFactory(valueFactory);
        numeroGiorniSpinner.setEditable(true);

        numeroGiorniSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                numeroGiorniEffettivo = newValue;
                validateNumeroGiorniInput();
            }
        });

        numeroGiorniSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                Platform.runLater(() -> numeroGiorniSpinner.getEditor().setText(oldValue));
            }
        });

        dataInizioDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            calcolaEImpostaNumeroGiorni();
        });
        dataFineDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            calcolaEImpostaNumeroGiorni();
        });

        calcolaEImpostaNumeroGiorni();

        numeroGiorniCalcolato = 0;
        numeroGiorniEffettivo = 0;
    }

    private void calcolaEImpostaNumeroGiorni() {
        LocalDate start = dataInizioDatePicker.getValue();
        LocalDate end = dataFineDatePicker.getValue();

        if (start != null && end != null && !end.isBefore(start)) {
            long days = ChronoUnit.DAYS.between(start, end) + 1;
            numeroGiorniCalcolato = (int) days;

            SpinnerValueFactory<Integer> valueFactory = numeroGiorniSpinner.getValueFactory();
            if (valueFactory instanceof SpinnerValueFactory.IntegerSpinnerValueFactory) {
                SpinnerValueFactory.IntegerSpinnerValueFactory intValueFactory =
                        (SpinnerValueFactory.IntegerSpinnerValueFactory) valueFactory;

                intValueFactory.setMax(numeroGiorniCalcolato);

                if (numeroGiorniSpinner.getValue() == null || numeroGiorniSpinner.getValue() <= 0 || numeroGiorniSpinner.getValue() > numeroGiorniCalcolato) {
                    intValueFactory.setValue(Math.max(1, numeroGiorniCalcolato));
                }
            }
            erroreNumeroGiorniLabel.setText("");
        } else {
            numeroGiorniCalcolato = 0;
            if (numeroGiorniSpinner.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory) {
                SpinnerValueFactory.IntegerSpinnerValueFactory intValueFactory =
                        (SpinnerValueFactory.IntegerSpinnerValueFactory) numeroGiorniSpinner.getValueFactory();
                intValueFactory.setMax(1);
            }
            numeroGiorniSpinner.getValueFactory().setValue(1);
            erroreNumeroGiorniLabel.setText("");
        }
        numeroGiorniEffettivo = numeroGiorniSpinner.getValue();
    }

    private boolean validateNumeroGiorniInput() {
        erroreNumeroGiorniLabel.setText("");

        Integer inputGiorni = numeroGiorniSpinner.getValue();

        if (inputGiorni == null) {
            erroreNumeroGiorniLabel.setText("Inserisci un numero di giorni valido.");
            return false;
        }

        if (inputGiorni <= 0) {
            erroreNumeroGiorniLabel.setText("Il numero di giorni deve essere maggiore di 0.");
            return false;
        }
        if (numeroGiorniCalcolato > 0 && inputGiorni > numeroGiorniCalcolato) {
            erroreNumeroGiorniLabel.setText("Non puoi inserire più giorni del range di date selezionato (" + numeroGiorniCalcolato + ").");
            return false;
        }

        numeroGiorniEffettivo = inputGiorni;
        return true;
    }


    @FXML
    private void switchToAggiungiAlimenti(ActionEvent event) {
        titoloPiano = titoloPianoTextField.getText();
        dataInizio = dataInizioDatePicker.getValue();
        dataFine = dataFineDatePicker.getValue();

        String errorMessage = null;

        if (titoloPiano == null || titoloPiano.trim().isEmpty()) {
            errorMessage = "Il titolo del piano non può essere vuoto.";
        } else if (dataInizio == null || dataFine == null) {
            errorMessage = "Devi selezionare sia la Data Inizio che la Data Fine.";
        } else {
            LocalDate start = dataInizioDatePicker.getValue();
            LocalDate end = dataFineDatePicker.getValue();
            if (start != null && end != null && !end.isBefore(start)) {
                numeroGiorniCalcolato = (int) (ChronoUnit.DAYS.between(start, end) + 1);
                if (!validateNumeroGiorniInput()) {
                    errorMessage = erroreNumeroGiorniLabel.getText();
                }
            } else {
                errorMessage = "La Data Fine non può essere precedente alla Data Inizio o le date non sono valide.";
            }
        }

        if (errorMessage != null) {
            showAlert(AlertType.WARNING, "Attenzione!", errorMessage);
            return;
        }

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

            // Use numeroGiorniEffettivo for database insertion
            String insertGiornoSql = "INSERT INTO Giorno_dieta (id_dieta, nome_giorno, calorie_giorno, proteine_giorno, carboidrati_giorno, grassi_giorno) VALUES (?, ?,0,0,0,0)";
            psGiorno = conn.prepareStatement(insertGiornoSql);

            for (int i = 0; i < numeroGiorniEffettivo; i++) {
                psGiorno.setInt(1, idDieta);
                psGiorno.setString(2, "Giorno " + (i + 1));
                psGiorno.addBatch();
            }

            psGiorno.executeBatch();
            conn.commit();

            System.out.println("Salvataggio piano dieta completato con successo nel DB.");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiGiornoDieta.fxml"));
            Parent aggiungiAlimentiRoot = loader.load();

            AggiungiGiornoDieta aggiungiAlimentiController = loader.getController();
            aggiungiAlimentiController.setTitoloPiano(titoloPiano);
            aggiungiAlimentiController.setNumeroGiorni(numeroGiorniEffettivo); // Pass the actual days from spinner
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
            showAlert(AlertType.ERROR,"Errore","Errore durante il salvataggio della dieta.");
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della schermata successiva: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR,"Errore","Errore nel caricamento della schermata successiva.");

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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css");
        }

        alert.showAndWait();
    }
}