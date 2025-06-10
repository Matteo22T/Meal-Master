package com.matteotocci.app.controller;

import com.matteotocci.app.model.DatiClienteModel;
import com.matteotocci.app.model.SQLiteConnessione;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox; // Assicurati che il layout principale sia un VBox o adatta di conseguenza
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DatiCliente {
    @FXML
    private DatePicker datadinascitaPicker;

    @FXML
    private ChoiceBox<String> livelloattivitàBox;

    @FXML
    private ChoiceBox<String> nutrizionistaBox;

    @FXML
    private Label altezzaLabel;

    @FXML
    private Label pesoLabel;

    @FXML
    private Button BottoneConferma;

    @FXML
    private Slider altezzaSlider;

    @FXML
    private Slider pesoSlider;

    @FXML
    private ChoiceBox<String> genereBox;

    @FXML
    private VBox registerBox; // Ottieni il riferimento al VBox principale


    private Map<String, Integer> mappaNutrizionisti = new HashMap<>();
    private int idUtente;
    public DatiClienteModel datiCliente = new DatiClienteModel();

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
        System.out.println("Id utente ricevuto: " + idUtente); // Debug
    }

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

    @FXML
    private void initialize() {
        ObservableList<String> nutrizionisti = getNutrizionisti();
        nutrizionistaBox.setItems(nutrizionisti);

        altezzaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double valore = newValue.doubleValue();
            altezzaLabel.setText(String.format("%.0f cm", valore));
        });

        pesoSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double valore = newValue.doubleValue();
            pesoLabel.setText(String.format("%.0f kg", valore));
        });

        ObservableList<String> opzioniSesso = FXCollections.observableArrayList(
                "Maschio",
                "Femmina",
                "Altro" // O le opzioni che preferisci
        );
        genereBox.setItems(opzioniSesso);

        ObservableList<String> livelliAttivita = FXCollections.observableArrayList(
                "Sedentario",
                "Leggermente Attivo",
                "Moderatamente Attivo",
                "Molto Attivo",
                "Estremamente Attivo"
        );
        livelloattivitàBox.setItems(livelliAttivita);

        // Aggiungi un listener di eventi al VBox principale per intercettare il tasto "Invio"
        if (registerBox != null) {
            registerBox.setOnKeyPressed(this::handleEnterKeyPressed);
        } else {
            System.err.println("Errore: registerBox non è stato iniettato!");
        }
    }

    @FXML
    private void handleEnterKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            confermaDati(new ActionEvent()); // Simula un click sul pulsante
            event.consume(); // Impedisce ad altri elementi di rispondere allo stesso evento
        }
    }

    @FXML
    private void confermaDati(ActionEvent event) {
        // Controlla se i campi FXML sono stati iniettati correttamente
        if (altezzaSlider == null || pesoSlider == null || datadinascitaPicker == null || livelloattivitàBox == null || nutrizionistaBox == null) {
            System.err.println("Errore: Campi FXML non inizializzati nel controller!");
            showAlert(Alert.AlertType.ERROR, "Errore Interno", "Errore nell'interfaccia utente.");
            return;
        }

        if (altezzaSlider.getValue() == 0) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci la tua altezza.");
            return;
        }
        if (pesoSlider.getValue() == 0) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci il tuo peso.");
            return;
        }
        if (livelloattivitàBox.getValue() == null || livelloattivitàBox.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona il tuo livello di attività.");
            return;
        }
        if (datadinascitaPicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci la tua data di nascita.");
            return;
        }
        if (nutrizionistaBox.getValue() == null || nutrizionistaBox.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona un nutrizionista.");
            return;
        }
        if (genereBox.getValue() == null || genereBox.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona il tuo sesso.");
            return;
        }

        double altezza = altezzaSlider.getValue();
        double peso = pesoSlider.getValue();
        LocalDate dataDiNascita = datadinascitaPicker.getValue();
        String livelloAttivita = livelloattivitàBox.getValue();
        String sessoSelezionato = genereBox.getValue().toLowerCase();
        String nutrizionistaSelezionato = nutrizionistaBox.getValue();
        Integer idNutrizionista = mappaNutrizionisti.get(nutrizionistaSelezionato);


        boolean successo = datiCliente.registraCliente(altezza, peso, dataDiNascita, livelloAttivita,sessoSelezionato, idNutrizionista, idUtente);

        if (successo) {
            // Ora esegui l'azione originale di "Registrato": carica la nuova pagina
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ConfermaRegistrazione.fxml"));
                Parent root = fxmlLoader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Benvenuto!"); // Puoi impostare un titolo per la nuova finestra
                stage.show();

                if (BottoneConferma != null && BottoneConferma.getScene() != null && BottoneConferma.getScene().getWindow() != null) {
                    ((Stage) BottoneConferma.getScene().getWindow()).close();
                } else {
                    System.err.println("Impossibile ottenere la finestra corrente per chiuderla.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                // Mostra un errore se il caricamento della nuova pagina fallisce
                showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina successiva dopo la registrazione.");
            }

        } else {
            // Registrazione fallita (es. errore DB)
            showAlert(Alert.AlertType.ERROR, "Errore di registrazione", "Impossibile registrare i dati del cliente. Si è verificato un problema.");
            // In caso di fallimento, l'utente rimane sulla schermata di inserimento dati
        }
    }

    @FXML
    private void Conferma(ActionEvent event) {
        confermaDati(event); // Chiama lo stesso metodo di handleEnterKeyPressed
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}