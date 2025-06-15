package com.matteotocci.app.controller; // Dichiara il package a cui appartiene questa classe.

// import com.matteotocci.app.model.UtenteModel; // Rimosso se non usato direttamente: Commento che indica una riga potenzialmente rimossa perché non utilizzata.

import javafx.event.ActionEvent; // Importa la classe ActionEvent, usata per gestire gli eventi generati dai controlli UI (es. click su un bottone).
import javafx.fxml.FXML; // Importa l'annotazione FXML per collegare gli elementi dell'interfaccia utente definiti in FXML al codice Java.
import javafx.fxml.Initializable; // Importa l'interfaccia Initializable, per i controller che devono essere inizializzati dopo il caricamento dell'FXML.
import javafx.scene.control.Alert; // Importa la classe Alert per visualizzare messaggi di avviso o errore.
import javafx.scene.control.ComboBox; // Importa la classe ComboBox per menu a tendina.
import javafx.scene.control.Label; // Importa la classe Label per visualizzare testo non modificabile.
import javafx.scene.control.TextField; // Importa la classe TextField per campi di input testuali.
import javafx.scene.image.ImageView; // Importa la classe ImageView per visualizzare immagini.
import javafx.scene.layout.Pane; // Importa la classe Pane o il tuo layout container specifico per elementi UI.

import java.net.URL; // Importa la classe URL, necessaria per l'interfaccia Initializable.
import java.sql.Connection; // Importa l'interfaccia Connection per la connessione al database.
import java.sql.DriverManager; // Importa la classe DriverManager per gestire un set di driver JDBC.
import java.sql.PreparedStatement; // Importa la classe PreparedStatement per eseguire query SQL precompilate.
import java.sql.ResultSet; // Importa la classe ResultSet per leggere i risultati delle query SQL.
import java.sql.SQLException; // Importa la classe SQLException per gestire errori di database.
import java.time.LocalDate; // Importa la classe LocalDate per lavorare con le date (senza orario).
import java.time.Period; // Importa la classe Period per calcolare intervalli tra date.
import java.util.ResourceBundle; // Importa la classe ResourceBundle, necessaria per l'interfaccia Initializable.

public class BMI implements Initializable { // Dichiara la classe BMI e implementa l'interfaccia Initializable.
    private String utenteCorrenteId; // Variabile per memorizzare l'ID dell'utente corrente.
    // private UtenteModel utenteModel; // Rimosso se non usato direttamente: Commento che indica una riga potenzialmente rimossa.

    @FXML private TextField altezzaTextField; // Campo FXML per il campo di testo dell'altezza.
    @FXML private TextField pesoTextField; // Campo FXML per il campo di testo del peso.
    @FXML private ComboBox<String> altezzaUnitComboBox; // Campo FXML per la ComboBox delle unità di misura dell'altezza (cm/m).
    @FXML private ComboBox<String> pesoUnitComboBox;     // Campo FXML per la ComboBox delle unità di misura del peso (kg/lbs).
    @FXML private ComboBox<Integer> ageComboBox;         // Campo FXML per la ComboBox dell'età.
    @FXML private Label bmiValueDisplayLabel; // Campo FXML per la label che mostra il valore del BMI.
    @FXML private Label bmiClassificationDisplayLabel; // Campo FXML per la label che mostra la classificazione del BMI.
    @FXML private Label bmiFeedbackLabel; // Campo FXML per la label che mostra un feedback sul BMI.
    @FXML private ImageView gaugeNeedle; // Campo FXML per l'ImageView che rappresenta l'ago del quadrante (gauge).
    @FXML private Pane bmiGaugeContainer; // Campo FXML per il contenitore del quadrante del BMI.

    // Aggiungi qui i ToggleButton per il sesso, se li hai nel FXML
    // @FXML private ToggleButton maleToggle; // Commento che suggerisce l'aggiunta di ToggleButton per il sesso.
    // @FXML private ToggleButton femaleToggle; // Commento che suggerisce l'aggiunta di ToggleButton per il sesso.


    public void setUtenteCorrenteId(String userId) { // Metodo per impostare l'ID dell'utente corrente.
        this.utenteCorrenteId = userId; // Assegna l'ID utente.
        // Ora che abbiamo l'ID, possiamo inizializzare i dati
        inizializzaDatiUtente(); // Chiama il metodo per inizializzare i dati dell'utente.
    }

    private void inizializzaDatiUtente() { // Metodo privato per inizializzare i dati dell'utente dal database.
        System.out.println("[DEBUG] inizializzaDatiUtente chiamato con ID: " + utenteCorrenteId); // Stampa a console per debug.
        if (utenteCorrenteId != null) { // Controlla se l'ID utente non è nullo.
            System.out.println("[DEBUG] Tentativo di recupero dati per l'utente con ID: " + utenteCorrenteId); // Stampa a console per debug.

            // Recupera i dati del cliente dalla tabella Clienti
            String altezza = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "altezza_cm"); // Recupera l'altezza dal DB.
            String peso = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "peso_kg"); // Recupera il peso dal DB.
            String dataNascita = getDatoUtenteDalDatabase("Clienti", utenteCorrenteId, "data_di_nascita"); // Recupera la data di nascita dal DB.

            // Imposta i valori nei rispettivi TextField
            if (altezzaTextField != null && altezza != null) { // Se il campo altezza e il valore recuperato non sono nulli.
                altezzaTextField.setText(altezza); // Imposta il testo del campo altezza.
            }
            if (pesoTextField != null && peso != null) { // Se il campo peso e il valore recuperato non sono nulli.
                pesoTextField.setText(peso); // Imposta il testo del campo peso.
            }
            // Pre-popola l'età se disponibile
            if (ageComboBox != null && dataNascita != null && !dataNascita.isEmpty()) { // Se la ComboBox dell'età, la data di nascita e la data di nascita non sono vuote.
                try { // Inizia un blocco try-catch per il parsing della data.
                    // Assumiamo dataNascita sia nel formato YYYY-MM-DD
                    LocalDate birthDate = LocalDate.parse(dataNascita); // Parsa la stringa della data di nascita in un oggetto LocalDate.
                    int age = Period.between(birthDate, LocalDate.now()).getYears(); // Calcola l'età in anni.
                    ageComboBox.setValue(age); // Imposta il valore dell'età nella ComboBox.
                } catch (Exception e) { // Cattura qualsiasi eccezione durante il parsing.
                    System.err.println("Errore nel parsing della data di nascita: " + e.getMessage()); // Stampa un messaggio di errore.
                }
            }
            // TODO: Pre-popolare sesso se hai i ToggleButton // Commento che indica una funzionalità da implementare.
        } else { // Se l'ID utente è nullo.
            System.out.println("[DEBUG] ID utente non valido (null). Impossibile recuperare i dati."); // Stampa un messaggio di debug.
        }
    }

    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) { // Metodo privato per recuperare un singolo dato utente dal database.
        String valore = null; // Variabile per memorizzare il valore recuperato, inizializzata a null.
        String url = "jdbc:sqlite:database.db"; // URL del database SQLite.
        String query; // Variabile per la query SQL.
        String idColumn = "id"; // Default per la colonna ID nella tabella Utente.
        if (tabella.equals("Clienti")) { // Se la tabella è "Clienti".
            idColumn = "id_cliente"; // Imposta la colonna ID a "id_cliente".
        }
        query = "SELECT " + campo + " FROM " + tabella + " WHERE " + idColumn + " = ?"; // Costruisce la query SQL dinamicamente.
        System.out.println("[DEBUG] Query per " + tabella + " eseguita: " + query + " con ID: " + userId + ", Campo: " + campo); // Stampa la query per debug.

        try (Connection conn = DriverManager.getConnection(url); // Ottiene una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement SQL.
            pstmt.setString(1, userId); // Imposta l'ID utente come parametro della query.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query e ottiene il ResultSet.

            if (rs.next()) { // Se il ResultSet contiene una riga.
                valore = rs.getString(campo); // Ottiene il valore del campo specificato.
                System.out.println("[DEBUG] Valore recuperato per " + campo + " da " + tabella + ": " + valore); // Stampa il valore recuperato per debug.
            } else { // Se il ResultSet è vuoto.
                System.out.println("[DEBUG] Nessun utente trovato con ID: " + userId + " nella tabella " + tabella + " per il campo " + campo); // Stampa un messaggio di debug.
            }

        } catch (SQLException e) { // Cattura le eccezioni SQL.
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage()); // Stampa un messaggio di errore.
        }
        return valore; // Restituisce il valore recuperato.
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // Metodo di inizializzazione del controller.
        // Popola le ComboBox per le unità di misura e l'età
        if (altezzaUnitComboBox != null) { // Se la ComboBox delle unità di altezza esiste.
            altezzaUnitComboBox.getItems().addAll("cm", "m"); // Aggiunge le opzioni "cm" e "m".
            altezzaUnitComboBox.setValue("cm"); // Imposta "cm" come valore di default.
        }
        if (pesoUnitComboBox != null) { // Se la ComboBox delle unità di peso esiste.
            pesoUnitComboBox.getItems().addAll("kg", "lbs"); // Aggiunge le opzioni "kg" e "lbs".
            pesoUnitComboBox.setValue("kg"); // Imposta "kg" come valore di default.
        }
        if (ageComboBox != null) { // Se la ComboBox dell'età esiste.
            for (int i = 1; i <= 100; i++) { // Itera da 1 a 100.
                ageComboBox.getItems().add(i); // Aggiunge ogni numero come opzione di età.
            }
            ageComboBox.setValue(25); // Imposta 25 come età di default.
        }
        // L'inizializzazione dei dati utente avviene in setUtenteCorrenteId // Commento che spiega dove avviene un'altra inizializzazione.
    }

    @FXML
    private void handleCalcolaBMI(ActionEvent event) { // Metodo FXML per gestire il calcolo del BMI quando il bottone viene cliccato.
        try { // Inizia un blocco try-catch per gestire potenziali errori di input o calcolo.
            double altezzaCm; // Variabile per l'altezza in centimetri.
            double pesoKg; // Variabile per il peso in chilogrammi.

            // Validazione Altezza
            if (altezzaTextField.getText().isEmpty()) { // Controlla se il campo altezza è vuoto.
                mostraAvviso("Input Mancante", "Inserisci la tua altezza."); // Mostra un avviso all'utente.
                return; // Esce dal metodo.
            }
            try { // Inizia un blocco try-catch per il parsing dell'altezza.
                altezzaCm = Double.parseDouble(altezzaTextField.getText()); // Tenta di convertire il testo dell'altezza in un double.
                if (altezzaCm <= 0) { // Controlla se l'altezza è minore o uguale a zero.
                    mostraAvviso("Input Non Valido", "L'altezza deve essere un valore positivo."); // Mostra un avviso.
                    return; // Esce dal metodo.
                }
                // Converti in cm se l'unità è metri
                if (altezzaUnitComboBox != null && "m".equals(altezzaUnitComboBox.getValue())) { // Se l'unità selezionata è metri.
                    altezzaCm *= 100; // Converte l'altezza da metri a centimetri.
                }
            } catch (NumberFormatException e) { // Cattura l'eccezione se il testo non è un numero valido.
                mostraAvviso("Input Non Valido", "Inserisci un numero valido per l'altezza."); // Mostra un avviso.
                return; // Esce dal metodo.
            }

            // Validazione Peso
            if (pesoTextField.getText().isEmpty()) { // Controlla se il campo peso è vuoto.
                mostraAvviso("Input Mancante", "Inserisci il tuo peso."); // Mostra un avviso.
                return; // Esce dal metodo.
            }
            try { // Inizia un blocco try-catch per il parsing del peso.
                pesoKg = Double.parseDouble(pesoTextField.getText()); // Tenta di convertire il testo del peso in un double.
                if (pesoKg <= 0) { // Controlla se il peso è minore o uguale a zero.
                    mostraAvviso("Input Non Valido", "Il peso deve essere un valore positivo."); // Mostra un avviso.
                    return; // Esce dal metodo.
                }
                // Converti in kg se l'unità è lbs
                if (pesoUnitComboBox != null && "lbs".equals(pesoUnitComboBox.getValue())) { // Se l'unità selezionata è libbre.
                    pesoKg *= 0.453592; // Converte il peso da libbre a chilogrammi.
                }
            } catch (NumberFormatException e) { // Cattura l'eccezione se il testo non è un numero valido.
                mostraAvviso("Input Non Valido", "Inserisci un numero valido per il peso."); // Mostra un avviso.
                return; // Esce dal metodo.
            }

            // Calcolo BMI
            double altezzaMetri = altezzaCm / 100.0; // Converte l'altezza da centimetri a metri.
            double bmi = pesoKg / (altezzaMetri * altezzaMetri); // Calcola il BMI.

            // Aggiorna le etichette con il valore del BMI
            bmiValueDisplayLabel.setText(String.format("%.1f", bmi)); // Imposta il testo della label del valore BMI formattato a una cifra decimale.

            // Classificazione BMI e aggiornamento colore
            String classification; // Variabile per la classificazione del BMI.
            String feedback; // Variabile per il feedback sul BMI.
            String colorStyle; // Stile CSS per il colore del testo della classificazione.

            if (bmi < 16.0) { // Se il BMI è inferiore a 16.0.
                classification = "Gravemente Sottopeso"; // Classificazione.
                feedback = "È importante consultare un medico."; // Feedback.
                colorStyle = "-fx-text-fill: #0000FF;"; // Blu scuro: Stile colore.
            } else if (bmi >= 16.0 && bmi < 18.5) { // Se il BMI è tra 16.0 e 18.5 (escluso).
                classification = "Sottopeso"; // Classificazione.
                feedback = "Considera di consultare un medico."; // Feedback.
                colorStyle = "-fx-text-fill: #4169E1;"; // Blu reale: Stile colore.
            } else if (bmi >= 18.5 && bmi < 24.9) { // Se il BMI è tra 18.5 e 24.9 (escluso).
                classification = "Normopeso"; // Classificazione.
                feedback = "Ottimo! Mantieni uno stile di vita sano."; // Feedback.
                colorStyle = "-fx-text-fill: #228B22;"; // Verde foresta: Stile colore.
            } else if (bmi >= 24.9 && bmi < 29.9) { // Se il BMI è tra 24.9 e 29.9 (escluso).
                classification = "Sovrappeso"; // Classificazione.
                feedback = "Potrebbe essere utile un piano alimentare."; // Feedback.
                colorStyle = "-fx-text-fill: #FFA500;"; // Arancione: Stile colore.
            } else if (bmi >= 29.9 && bmi < 34.9) { // Se il BMI è tra 29.9 e 34.9 (escluso).
                classification = "Obeso Classe I"; // Classificazione.
                feedback = "È consigliato consultare un professionista."; // Feedback.
                colorStyle = "-fx-text-fill: #FF4500;"; // Rosso Arancione: Stile colore.
            } else if (bmi >= 34.9 && bmi < 39.9) { // Se il BMI è tra 34.9 e 39.9 (escluso).
                classification = "Obeso Classe II"; // Classificazione.
                feedback = "È fondamentale consultare un medico."; // Feedback.
                colorStyle = "-fx-text-fill: #B22222;"; // Rosso mattone: Stile colore.
            } else { // Se il BMI è maggiore o uguale a 39.9.
                classification = "Obeso Classe III"; // Classificazione.
                feedback = "Richiede attenzione medica immediata."; // Feedback.
                colorStyle = "-fx-text-fill: #8B0000;"; // Rosso scuro: Stile colore.
            }

            bmiClassificationDisplayLabel.setText(classification); // Imposta il testo della label di classificazione.
            bmiFeedbackLabel.setText(feedback); // Imposta il testo della label di feedback.
            bmiClassificationDisplayLabel.setStyle(colorStyle); // Applica lo stile del colore alla label di classificazione.

            // TODO: Aggiorna la rotazione della lancetta del gauge qui // Commento che indica una funzionalità da implementare.
            // updateGauge(bmi); // Chiamata a un metodo per aggiornare il gauge visivo // Esempio di chiamata a un metodo per aggiornare l'indicatore.
            // Questo metodo 'updateGauge' dovrebbe essere implementato separatamente // Nota che il metodo 'updateGauge' deve essere implementato altrove.
            // come discusso in precedenza, usando gaugeNeedle e i limiti del tuo quadrante. // Suggerimento su come implementare 'updateGauge'.

        } catch (Exception e) { // Cattura qualsiasi eccezione generica.
            e.printStackTrace(); // Stampa lo stack trace dell'errore.
            mostraAvviso("Errore", "Si è verificato un errore durante il calcolo del BMI."); // Mostra un avviso generico di errore.
        }
    }

    private void mostraAvviso(String titolo, String messaggio) { // Metodo privato per mostrare un Alert di avviso.
        Alert alert = new Alert(Alert.AlertType.WARNING); // Crea un nuovo Alert di tipo WARNING.
        alert.setTitle(titolo); // Imposta il titolo dell'alert.
        alert.setHeaderText(null); // Imposta l'header text a null.
        alert.setContentText(messaggio); // Imposta il messaggio di contenuto.
        alert.showAndWait(); // Mostra l'alert e attende che l'utente lo chiuda.
    }
}