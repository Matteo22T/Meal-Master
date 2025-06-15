package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Controller per la schermata "Dettagli Alimento".
 * Questa classe gestisce la visualizzazione dettagliata di un singolo alimento,
 * inclusi i suoi valori nutrizionali e la possibilità di eliminarlo (se l'utente è autorizzato).
 */
public class DettagliAlimentoController {
    // --- Elementi dell'interfaccia utente (collegati tramite @FXML) ---
    @FXML private ImageView immagineGrande; // ImageView per visualizzare l'immagine grande dell'alimento
    @FXML private Label nomeLabel, kcalLabel, proteineLabel, carboidratiLabel, grassiLabel,
            grassiSatLabel, saleLabel, fibreLabel, zuccheriLabel; // Etichette per i valori nutrizionali e il nome
    @FXML private Button BottoneElimina; // Bottone per eliminare l'alimento

    private Alimento alimento; // L'oggetto Alimento i cui dettagli sono visualizzati

    /**
     * Imposta l'alimento i cui dettagli devono essere visualizzati nella schermata.
     * Questo metodo popola tutte le etichette con i dati dell'alimento e carica la sua immagine.
     * @param alimento L'oggetto Alimento da visualizzare.
     */
    public void setAlimento(Alimento alimento) {
        this.alimento = alimento; // Memorizza l'alimento corrente
        String url = alimento.getImmagineGrande(); // Ottiene l'URL dell'immagine grande

        // Carica l'immagine dell'alimento o un'immagine di default se l'URL è nullo/vuoto
        if (url != null && !url.trim().isEmpty()) {
            immagineGrande.setImage(new Image(url, 200, 200, true, true));
        } else {
            // Carica immagine di default dalle risorse dell'applicazione
            // Questo percorso è relativo alla cartella delle risorse del progetto JavaFX
            Image defaultImage = new Image("com/matteotocci/app/immagini/png-clipart-computer-icons-encapsulated-postscript-dish-dish-love-food-thumbnail.png", 200, 200, true, true);
            immagineGrande.setImage(defaultImage);
        }

        // Popola le etichette con i valori dell'alimento
        nomeLabel.setText("Nome: " + alimento.getNome());
        kcalLabel.setText("Kcal: " + alimento.getKcal());
        proteineLabel.setText("Proteine: " + alimento.getProteine());
        carboidratiLabel.setText("Carboidrati: " + alimento.getCarboidrati());
        grassiLabel.setText("Grassi: " + alimento.getGrassi());
        grassiSatLabel.setText("Grassi Saturi: " + alimento.getGrassiSaturi());
        saleLabel.setText("Sale: " + alimento.getSale());
        fibreLabel.setText("Fibre: " + alimento.getFibre());
        zuccheriLabel.setText("Zuccheri: " + alimento.getZuccheri());
    }

    private String origineFXML; // Variabile per memorizzare la schermata da cui è stato aperto questo dettaglio

    /**
     * Imposta la schermata di origine da cui è stato aperto il dettaglio dell'alimento.
     * Questo è utile per determinare la visibilità di alcuni elementi (es. bottone "Elimina").
     * @param origineFXML Il nome del file FXML di origine (es. "Alimenti.fxml").
     */
    public void setOrigineFXML(String origineFXML) {
        this.origineFXML = origineFXML;
        aggiornaVisibilitaBottone(); // Aggiorna la visibilità del bottone "Elimina"
    }

    /**
     * Aggiorna la visibilità del bottone "Elimina".
     * Il bottone è visibile solo se la schermata di origine è "Alimenti.fxml"
     * e l'utente loggato è lo stesso che ha aggiunto l'alimento.
     */
    private void aggiornaVisibilitaBottone() {
        // Il bottone elimina è visibile solo se:
        // 1. La schermata di origine è "Alimenti.fxml" (il che implica che l'utente sta gestendo i propri alimenti)
        // 2. L'ID dell'utente loggato (Session.getUserId()) corrisponde all'ID dell'utente che ha inserito l'alimento (alimento.getUserId())
        if (Session.getUserId() != null && alimento != null && alimento.getUserId() != null) {
            BottoneElimina.setVisible("Alimenti.fxml".equals(origineFXML) && Session.getUserId().equals(alimento.getUserId()));
        } else {
            BottoneElimina.setVisible(false); // Nascondi il bottone se mancano le informazioni necessarie
        }
    }

    private Alimenti alimentiController; // Riferimento al controller della schermata "Alimenti"

    /**
     * Imposta il riferimento al controller della schermata "Alimenti".
     * Questo permette a questo controller di richiamare metodi sul controller genitore
     * (es. per aggiornare la lista degli alimenti dopo un'eliminazione).
     * @param controller L'istanza del controller Alimenti.
     */
    public void setAlimentiController(Alimenti controller) {
        this.alimentiController = controller;
    }

    /**
     * Gestisce l'azione per chiudere la finestra corrente.
     * @param event L'evento di azione (se associato a un bottone).
     */
    @FXML
    private void chiudiFinestra(ActionEvent event) { // Ho aggiunto ActionEvent event per coerenza con altri metodi FXML
        ((Stage) immagineGrande.getScene().getWindow()).close(); // Chiude lo Stage (la finestra)
    }

    // Metodo che viene chiamato quando si clicca sul pulsante "Elimina"
    @FXML
    private void handleEliminaAlimento(ActionEvent event) {
        System.out.println("User corrente: " + Session.getUserId()); // Debug: stampa l'ID utente corrente
        System.out.println("User dell'alimento: " + alimento.getUserId()); // Debug: stampa l'ID utente che ha aggiunto l'alimento

        // Verifica se l'utente loggato è lo stesso che ha aggiunto l'alimento
        // Questo è un controllo di sicurezza fondamentale per l'eliminazione
        if (alimento != null && Session.getUserId() != null && alimento.getUserId().equals(Session.getUserId())) {
            // Chiede conferma all'utente prima di eliminare
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Conferma Eliminazione");
            confirmationAlert.setHeaderText("Sei sicuro di voler eliminare questo alimento?");
            confirmationAlert.setContentText("Questa azione non può essere annullata.");
            // Applica stili CSS all'alert di conferma
            URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css");
            if (cssUrl != null) {
                confirmationAlert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                confirmationAlert.getDialogPane().getStyleClass().add("dialog-pane");
                confirmationAlert.getDialogPane().getStyleClass().add("alert-confirmation");
            } else {
                System.err.println("CSS file not found: Alert-Dialog-Style.css");
            }

            // Mostra l'alert e attende la risposta dell'utente
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    eliminaAlimento(alimento); // Se l'utente conferma, procede con l'eliminazione
                }
            });
        } else {
            // Se l'utente non è autorizzato, mostra un messaggio di errore
            showAlert(Alert.AlertType.ERROR,"Errore nell'eliminazione", "Puoi eliminare solo gli alimenti che hai aggiunto.");
        }
    }

    /**
     * Elimina un alimento dal database.
     * Dopo l'eliminazione, aggiorna la schermata `Alimenti` (se il controller è stato impostato)
     * e chiude la finestra di dettaglio.
     * @param alimento L'oggetto Alimento da eliminare.
     */
    private void eliminaAlimento(Alimento alimento) {
        System.out.println("Alimento da eliminare: " + alimento.getNome() + " (ID: " + alimento.getId() + ")");
        String query = "DELETE FROM foods WHERE id = ?"; // Query SQL per eliminare l'alimento per ID

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene la connessione
             PreparedStatement stmt = conn.prepareStatement(query)) { // Prepara lo statement
            stmt.setInt(1, alimento.getId()); // Imposta l'ID dell'alimento come parametro
            int affected = stmt.executeUpdate(); // Esegue la query di eliminazione e ottiene il numero di righe influenzate
            System.out.println("Righe eliminate: " + affected); // Debug: stampa il numero di righe eliminate

            if (affected > 0) { // Se l'eliminazione ha avuto successo (almeno una riga è stata eliminata)
                showAlert(Alert.AlertType.INFORMATION, "Successo", "Alimento eliminato con successo!");
                if (alimentiController != null) {
                    System.out.println("Filtro Alimenti: " + alimentiController.getFiltro()); // Debug: stampa il filtro corrente
                    alimentiController.resetRicerca(); // Resetta la ricerca nella schermata Alimenti
                    alimentiController.cercaAlimenti(alimentiController.getFiltro(), false); // Aggiorna la lista degli alimenti
                }
                // Chiudi la finestra di dettaglio se l'eliminazione è andata a buon fine
                Stage stage = (Stage) immagineGrande.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.WARNING, "Attenzione", "Nessun alimento eliminato. Potrebbe non esistere o non essere accessibile.");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore SQL
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Errore durante l'eliminazione dell'alimento: " + e.getMessage());
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
}
