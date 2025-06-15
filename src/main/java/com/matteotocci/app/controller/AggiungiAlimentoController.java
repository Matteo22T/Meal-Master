package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert; // Finestra di dialogo per avvisi o errori

import java.net.URL; // Necessario per Initializable (per trovare risorse come file CSS)
import java.sql.Connection;
import java.sql.PreparedStatement; // Per eseguire query SQL precompilate
import java.sql.SQLException; // Eccezione per errori di database
import java.util.Arrays;
import java.util.List;

import com.matteotocci.app.model.SQLiteConnessione; // Classe per gestire la connessione al database SQLite
import javafx.stage.Stage;

import java.util.ResourceBundle; // Necessario per Initializable (per la localizzazione)

/**
 * Controller per la schermata "Aggiungi Alimento".
 * Questa classe gestisce l'interfaccia utente per l'inserimento di nuovi alimenti
 * e il loro salvataggio nel database. Implementa Initializable per l'inizializzazione
 * dei componenti UI dopo il caricamento dell'FXML.
 */
public class AggiungiAlimentoController implements Initializable { // AGGIUNTA: Implementa l'interfaccia Initializable


    @FXML
    private TextField nomeField, brandField, kcalField, proteineField, carboidratiField, grassiField,
            grassiSatField, saleField, fibreField, zuccheriField,
            immaginePiccolaField, immagineGrandeField;

    // --- Metodi di inizializzazione e gestione focus ---

    /**
     * Configura la navigazione del focus tra i campi di testo utilizzando le frecce direzionali.
     * Permette all'utente di spostarsi tra i campi UP (su) e DOWN (giù).
     */
    public void setupFocusTraversal() {
        // Crea una lista ordinata di tutti i campi di testo
        List<TextField> textFields = Arrays.asList(nomeField, brandField, kcalField, proteineField, carboidratiField, grassiField,
                grassiSatField, saleField, fibreField, zuccheriField, immaginePiccolaField, immagineGrandeField);

        // Itera su ogni campo di testo per impostare il listener per la pressione dei tasti
        for (int i = 0; i < textFields.size(); i++) {
            final int index = i; // Rende l'indice effettivo finale per l'uso nella lambda expression
            TextField tf = textFields.get(i);
            tf.setFocusTraversable(true); // Assicura che il campo possa ricevere il focus
            tf.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case DOWN: // Se viene premuta la freccia giù
                        if (index + 1 < textFields.size()) { // Controlla se c'è un campo successivo
                            textFields.get(index + 1).requestFocus(); // Sposta il focus al campo successivo
                        }
                        break;
                    case UP:
                        if (index - 1 >= 0) { // Controlla se c'è un campo precedente
                            textFields.get(index - 1).requestFocus(); // Sposta il focus al campo precedente
                        }
                        break;
                }
            });
        }
    }

    /**
     * Metodo di inizializzazione chiamato automaticamente da FXMLLoader dopo che
     * tutti gli elementi FXML sono stati caricati e iniettati nel controller.
     * Questo è il punto in cui viene eseguita la logica di setup iniziale della pagina.
     *
     * @param location L'URL del documento FXML che ha dato origine a questo controller.
     * @param resources Le risorse utilizzate per localizzare gli oggetti radice, o null se la radice non è stata localizzata.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) { // MODIFICA: La firma del metodo ora corrisponde a quella di Initializable
        setupFocusTraversal(); // Chiama il metodo per configurare la navigazione del focus
    }

    // --- Variabili per la comunicazione tra controller ---
    private Alimenti alimentiController; // Riferimento al controller della schermata "Alimenti"

    /**
     * Imposta il riferimento al controller della schermata "Alimenti".
     * Questo è un meccanismo per permettere a questo controller (AggiungiAlimentoController)
     * di richiamare metodi sul controller genitore (Alimenti) dopo aver salvato un alimento.
     * @param controller L'istanza del controller Alimenti.
     */
    public void setAlimentiController(Alimenti controller) {
        this.alimentiController = controller;
    }

    // --- Gestione del salvataggio dell'alimento ---

    /**
     * Gestisce l'evento di click sul bottone "Salva".
     * Valida i dati inseriti, li parsifica, e li salva nel database.
     * Successivamente, aggiorna la schermata degli alimenti e chiude la finestra corrente.
     * @param event L'evento di azione (click sul bottone).
     */
    @FXML
    private void handleSalva(ActionEvent event) {
        // Validazione: controlla che tutti i campi obbligatori siano pieni
        if (isEmpty(nomeField) || isEmpty(brandField) || isEmpty(kcalField) || isEmpty(proteineField) ||
                isEmpty(carboidratiField) || isEmpty(grassiField) || isEmpty(grassiSatField) || // CORREZIONE: grasiField dovrebbe essere grassiField
                isEmpty(saleField) || isEmpty(fibreField) || isEmpty(zuccheriField)) {

            showAlert(Alert.AlertType.ERROR, "Errore", "Tutti i campi obbligatori devono essere compilati.");
            return; // Interrompe l'esecuzione se ci sono campi vuoti
        }

        try {
            // Esegui il parsing dei valori numerici dai campi di testo.
            // Se un campo numerico non è un numero valido, viene catturata una NumberFormatException.
            double kcal = Double.parseDouble(kcalField.getText());
            double proteine = Double.parseDouble(proteineField.getText());
            double carboidrati = Double.parseDouble(carboidratiField.getText());
            double grassi = Double.parseDouble(grassiField.getText());
            double grassiSat = Double.parseDouble(grassiSatField.getText());
            double sale = Double.parseDouble(saleField.getText());
            double fibre = Double.parseDouble(fibreField.getText());
            double zuccheri = Double.parseDouble(zuccheriField.getText());

            // Ottiene una connessione al database tramite la classe SQLiteConnessione
            Connection conn = SQLiteConnessione.connector();

            // Recupera l'ID dell'utente loggato dalla classe Session
            Integer userId = Session.getUserId();
            System.out.println("User ID attuale: " + Session.getUserId()); // Debug: stampa l'ID utente

            // Query SQL per inserire un nuovo alimento nella tabella 'foods'
            String query = "INSERT INTO foods (nome, brand, kcal, proteine, carboidrati, grassi, grassiSaturi, sale, fibre, zuccheri, immaginePiccola, immagineGrande, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
            // Prepara lo statement SQL per prevenire SQL Injection e migliorare le prestazioni
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, nomeField.getText()); // Imposta il nome
            stmt.setString(2, brandField.getText()); // Imposta il brand
            stmt.setDouble(3, kcal); // Imposta le kcal
            stmt.setDouble(4, proteine); // Imposta le proteine
            stmt.setDouble(5, carboidrati); // Imposta i carboidrati
            stmt.setDouble(6, grassi); // Imposta i grassi
            stmt.setDouble(7, grassiSat); // Imposta i grassi saturi
            stmt.setDouble(8, sale); // Imposta il sale
            stmt.setDouble(9, fibre); // Imposta le fibre
            stmt.setDouble(10, zuccheri); // Imposta gli zuccheri
            // Imposta l'URL dell'immagine piccola; se il campo è vuoto, salva NULL nel DB
            stmt.setString(11, immaginePiccolaField.getText().isEmpty() ? null : immaginePiccolaField.getText());
            // Imposta l'URL dell'immagine grande; se il campo è vuoto, salva NULL nel DB
            stmt.setString(12, immagineGrandeField.getText().isEmpty() ? null : immagineGrandeField.getText());
            stmt.setInt(13, userId);  // Aggiungi l'ID dell'utente che ha inserito l'alimento


            stmt.executeUpdate(); // Esegue la query di inserimento

            showAlert(Alert.AlertType.INFORMATION, "Successo", "Alimento aggiunto con successo!"); // Mostra messaggio di successo
            if (alimentiController != null) {
                System.out.println("filtro: "+alimentiController.getFiltro()); // Debug: stampa il filtro corrente
                alimentiController.resetRicerca(); // Resetta la ricerca nella schermata Alimenti
                alimentiController.cercaAlimenti(alimentiController.getFiltro(),false); // Aggiorna la lista degli alimenti
            }
            // Chiude la finestra corrente (AggiungiAlimentoController)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            // Cattura l'eccezione se il testo nei campi numerici non può essere convertito in Double
            showAlert(Alert.AlertType.ERROR, "Errore", "Assicurati che tutti i valori nutrizionali siano numeri validi.");
        } catch (SQLException e) {
            // Cattura l'eccezione se si verifica un errore durante l'interazione con il database
            System.out.println(e.getMessage()); // Stampa il messaggio di errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore", "Errore durante il salvataggio nel database: " + e.getMessage());
        }
    }

    /**
     * Metodo helper per controllare se un campo di testo è vuoto o contiene solo spazi bianchi.
     * @param field Il TextField da controllare.
     * @return true se il campo è vuoto o contiene solo spazi, false altrimenti.
     */
    private boolean isEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    /**
     * Metodo helper per mostrare messaggi di avviso all'utente.
     * Applica stili CSS personalizzati in base al tipo di avviso.
     * @param alertType Il tipo di avviso (ERROR, INFORMATION, WARNING, CONFIRMATION).
     * @param title Il titolo della finestra di avviso.
     * @param message Il messaggio da visualizzare.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Cerca il file CSS per lo stile personalizzato degli alert
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
        if (cssUrl != null) {
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css");
        }

        alert.showAndWait();
    }

    /**
     * Pulisce tutti i campi di input nella schermata.
     */
    private void pulisciCampi() {
        nomeField.clear();
        brandField.clear();
        kcalField.clear();
        proteineField.clear();
        carboidratiField.clear();
        grassiField.clear();
        grassiSatField.clear();
        saleField.clear();
        fibreField.clear();
        zuccheriField.clear();
        immaginePiccolaField.clear();
        immagineGrandeField.clear();
    }
}
