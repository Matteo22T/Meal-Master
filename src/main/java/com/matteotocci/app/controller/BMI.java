package com.matteotocci.app.controller;

// import com.matteotocci.app.model.UtenteModel; // Rimosso se non usato direttamente

import javafx.event.ActionEvent; // Importa ActionEvent per il metodo del bottone
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert; // Per i messaggi di errore
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane; // O il tuo layout container per il gauge

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;

public class BMI implements Initializable {
    private String utenteCorrenteId;
    // private UtenteModel utenteModel; // Rimosso se non usato direttamente

    @FXML private TextField altezzaTextField;
    @FXML private TextField pesoTextField;
    @FXML private ComboBox<String> altezzaUnitComboBox; // Per cm/m
    @FXML private ComboBox<String> pesoUnitComboBox;     // Per kg/lbs
    @FXML private ComboBox<Integer> ageComboBox;         // Per l'età
    @FXML private Label bmiValueDisplayLabel;
    @FXML private Label bmiClassificationDisplayLabel;
    @FXML private Label bmiFeedbackLabel;
    @FXML private ImageView gaugeNeedle;
    @FXML private Pane bmiGaugeContainer; // Il contenitore del tuo quadrante

    // Aggiungi qui i ToggleButton per il sesso, se li hai nel FXML
    // @FXML private ToggleButton maleToggle;
    // @FXML private ToggleButton femaleToggle;


    public void setUtenteCorrenteId(String userId) {
        this.utenteCorrenteId = userId;
        // Ora che abbiamo l'ID, possiamo inizializzare i dati
        inizializzaDatiUtente();
    }

    private void inizializzaDatiUtente() {
        System.out.println("[DEBUG] inizializzaDatiUtente chiamato con ID: " + utenteCorrenteId);
        if (utenteCorrenteId != null) {
            System.out.println("[DEBUG] Tentativo di recupero dati per l'utente con ID: " + utenteCorrenteId);

            // Recupera i dati del cliente dalla tabella Clienti
            String altezza = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "altezza_cm");
            String peso = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "peso_kg");
            String dataNascita = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "data_di_nascita");

            // Imposta i valori nei rispettivi TextField
            if (altezzaTextField != null && altezza != null) {
                altezzaTextField.setText(altezza);
            }
            if (pesoTextField != null && peso != null) {
                pesoTextField.setText(peso);
            }
            // Pre-popola l'età se disponibile
            if (ageComboBox != null && dataNascita != null && !dataNascita.isEmpty()) {
                try {
                    // Assumiamo dataNascita sia nel formato YYYY-MM-DD
                    LocalDate birthDate = LocalDate.parse(dataNascita);
                    int age = Period.between(birthDate, LocalDate.now()).getYears();
                    ageComboBox.setValue(age);
                } catch (Exception e) {
                    System.err.println("Errore nel parsing della data di nascita: " + e.getMessage());
                }
            }
            // TODO: Pre-popolare sesso se hai i ToggleButton
        } else {
            System.out.println("[DEBUG] ID utente non valido (null). Impossibile recuperare i dati.");
        }
    }

    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) {
        String valore = null;
        String url = "jdbc:sqlite:database.db";
        String query;
        String idColumn = "id"; // Default per la tabella Utente
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
                System.out.println("[DEBUG] Valore recuperato per " + campo + " da " + tabella + ": " + valore);
            } else {
                System.out.println("[DEBUG] Nessun utente trovato con ID: " + userId + " nella tabella " + tabella + " per il campo " + campo);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage());
        }
        return valore;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Popola le ComboBox per le unità di misura e l'età
        if (altezzaUnitComboBox != null) {
            altezzaUnitComboBox.getItems().addAll("cm", "m");
            altezzaUnitComboBox.setValue("cm"); // Valore di default
        }
        if (pesoUnitComboBox != null) {
            pesoUnitComboBox.getItems().addAll("kg", "lbs");
            pesoUnitComboBox.setValue("kg"); // Valore di default
        }
        if (ageComboBox != null) {
            for (int i = 1; i <= 100; i++) { // Età da 1 a 100
                ageComboBox.getItems().add(i);
            }
            ageComboBox.setValue(25); // Età di default
        }
        // L'inizializzazione dei dati utente avviene in setUtenteCorrenteId
    }

    @FXML
    private void handleCalcolaBMI(ActionEvent event) {
        try {
            double altezzaCm;
            double pesoKg;

            // Validazione Altezza
            if (altezzaTextField.getText().isEmpty()) {
                mostraAvviso("Input Mancante", "Inserisci la tua altezza.");
                return;
            }
            try {
                altezzaCm = Double.parseDouble(altezzaTextField.getText());
                if (altezzaCm <= 0) {
                    mostraAvviso("Input Non Valido", "L'altezza deve essere un valore positivo.");
                    return;
                }
                // Converti in cm se l'unità è metri
                if (altezzaUnitComboBox != null && "m".equals(altezzaUnitComboBox.getValue())) {
                    altezzaCm *= 100;
                }
            } catch (NumberFormatException e) {
                mostraAvviso("Input Non Valido", "Inserisci un numero valido per l'altezza.");
                return;
            }

            // Validazione Peso
            if (pesoTextField.getText().isEmpty()) {
                mostraAvviso("Input Mancante", "Inserisci il tuo peso.");
                return;
            }
            try {
                pesoKg = Double.parseDouble(pesoTextField.getText());
                if (pesoKg <= 0) {
                    mostraAvviso("Input Non Valido", "Il peso deve essere un valore positivo.");
                    return;
                }
                // Converti in kg se l'unità è lbs
                if (pesoUnitComboBox != null && "lbs".equals(pesoUnitComboBox.getValue())) {
                    pesoKg *= 0.453592; // 1 libbra = 0.453592 kg
                }
            } catch (NumberFormatException e) {
                mostraAvviso("Input Non Valido", "Inserisci un numero valido per il peso.");
                return;
            }

            // Calcolo BMI
            double altezzaMetri = altezzaCm / 100.0;
            double bmi = pesoKg / (altezzaMetri * altezzaMetri);

            // Aggiorna le etichette con il valore del BMI
            bmiValueDisplayLabel.setText(String.format("%.1f", bmi));

            // Classificazione BMI e aggiornamento colore
            String classification;
            String feedback;
            String colorStyle; // Stile CSS per il colore del testo

            if (bmi < 16.0) {
                classification = "Gravemente Sottopeso";
                feedback = "È importante consultare un medico.";
                colorStyle = "-fx-text-fill: #0000FF;"; // Blu scuro
            } else if (bmi >= 16.0 && bmi < 18.5) {
                classification = "Sottopeso";
                feedback = "Considera di consultare un medico.";
                colorStyle = "-fx-text-fill: #4169E1;"; // Blu reale
            } else if (bmi >= 18.5 && bmi < 24.9) {
                classification = "Normopeso";
                feedback = "Ottimo! Mantieni uno stile di vita sano.";
                colorStyle = "-fx-text-fill: #228B22;"; // Verde foresta
            } else if (bmi >= 24.9 && bmi < 29.9) {
                classification = "Sovrappeso";
                feedback = "Potrebbe essere utile un piano alimentare.";
                colorStyle = "-fx-text-fill: #FFA500;"; // Arancione
            } else if (bmi >= 29.9 && bmi < 34.9) {
                classification = "Obeso Classe I";
                feedback = "È consigliato consultare un professionista.";
                colorStyle = "-fx-text-fill: #FF4500;"; // Rosso Arancione
            } else if (bmi >= 34.9 && bmi < 39.9) {
                classification = "Obeso Classe II";
                feedback = "È fondamentale consultare un medico.";
                colorStyle = "-fx-text-fill: #B22222;"; // Rosso mattone
            } else { // bmi >= 39.9
                classification = "Obeso Classe III";
                feedback = "Richiede attenzione medica immediata.";
                colorStyle = "-fx-text-fill: #8B0000;"; // Rosso scuro
            }

            bmiClassificationDisplayLabel.setText(classification);
            bmiFeedbackLabel.setText(feedback);
            bmiClassificationDisplayLabel.setStyle(colorStyle); // Applica il colore al testo

            // TODO: Aggiorna la rotazione della lancetta del gauge qui
            // updateGauge(bmi); // Chiamata a un metodo per aggiornare il gauge visivo
            // Questo metodo 'updateGauge' dovrebbe essere implementato separatamente
            // come discusso in precedenza, usando gaugeNeedle e i limiti del tuo quadrante.

        } catch (Exception e) {
            e.printStackTrace();
            mostraAvviso("Errore", "Si è verificato un errore durante il calcolo del BMI.");
        }
    }

    private void mostraAvviso(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}