package com.matteotocci.app.controller; // Dichiara il package della classe.

import com.matteotocci.app.model.SQLiteConnessione; // Importa la classe per la connessione al database SQLite.
import com.matteotocci.app.model.Session; // Importa la classe Session per gestire l'ID utente loggato.
import javafx.application.Platform; // Importa Platform per eseguire codice sul thread UI di JavaFX.
import javafx.event.ActionEvent; // Importa ActionEvent per la gestione degli eventi.
import javafx.fxml.FXML; // Importa l'annotazione FXML.
import javafx.fxml.FXMLLoader; // Importa FXMLLoader per caricare file FXML.
import javafx.fxml.Initializable; // Importa l'interfaccia Initializable per l'inizializzazione del controller.
import javafx.scene.Node; // Importa Node, la classe base per gli elementi del grafo della scena.
import javafx.scene.Parent; // Importa Parent, la classe base per i nodi contenitori.
import javafx.scene.Scene; // Importa Scene per la gestione della scena.
import javafx.scene.control.Button; // Importa la classe Button.
import javafx.scene.control.Label; // Importa la classe Label.
import javafx.scene.control.TextField; // Importa la classe TextField.
import javafx.scene.control.DatePicker; // Importa la classe DatePicker per la selezione delle date.
import javafx.scene.control.DateCell; // Importa DateCell per personalizzare le celle del DatePicker.
import javafx.scene.control.Spinner; // Importa la classe Spinner per selezionare valori numerici.
import javafx.scene.control.SpinnerValueFactory; // Importa SpinnerValueFactory per definire i valori dello Spinner.
import javafx.stage.Stage; // Importa Stage per la gestione delle finestre.

import java.io.IOException; // Importa IOException per la gestione degli errori I/O.
import java.net.URL; // Importa URL (necessario per Initializable).
import java.sql.*; // Importa tutte le classi SQL (Connection, PreparedStatement, ResultSet, SQLException, Statement).
import java.time.LocalDate; // Importa LocalDate per lavorare con le date.
import java.time.temporal.ChronoUnit; // Importa ChronoUnit per calcolare unità di tempo (es. giorni).
import java.util.ResourceBundle; // Importa ResourceBundle (necessario per Initializable).

import javafx.scene.control.Alert; // Importa la classe Alert per mostrare messaggi all'utente.
import javafx.scene.control.Alert.AlertType; // Importa AlertType per definire il tipo di alert.

public class NuovaDieta implements Initializable { // Dichiara la classe NuovaDieta e implementa Initializable.

    @FXML
    private TextField titoloPianoTextField; // Campo FXML per il titolo del piano dieta.

    @FXML
    private DatePicker dataInizioDatePicker; // Campo FXML per la selezione della data di inizio.
    @FXML
    private DatePicker dataFineDatePicker; // Campo FXML per la selezione della data di fine.

    @FXML
    private Spinner<Integer> numeroGiorniSpinner; // Campo FXML per il selettore del numero di giorni.
    @FXML
    private Label erroreNumeroGiorniLabel; // Campo FXML per visualizzare messaggi di errore relativi al numero di giorni.

    @FXML
    private Button avantiButton; // Campo FXML per il bottone "Avanti".


    private String titoloPiano; // Variabile per memorizzare il titolo del piano.
    private LocalDate dataInizio; // Variabile per memorizzare la data di inizio.
    private LocalDate dataFine; // Variabile per memorizzare la data di fine.
    private int numeroGiorniCalcolato; // Maximum days allowed by date range: Numero massimo di giorni calcolabile in base al range di date.
    private int numeroGiorniEffettivo; // Actual days from spinner, used for DB insertion: Numero di giorni effettivo selezionato dallo spinner, usato per l'inserimento nel DB.

    @FXML
    public void initialize(URL url, ResourceBundle resources) { // Metodo di inizializzazione del controller, chiamato all'avvio.
        erroreNumeroGiorniLabel.setText(""); // Inizialmente, la label di errore è vuota.

        dataInizioDatePicker.setDayCellFactory(picker -> new DateCell() { // Imposta una cell factory personalizzata per il DatePicker della data di inizio.
            @Override
            public void updateItem(LocalDate date, boolean empty) { // Metodo chiamato per aggiornare ogni cella del calendario.
                super.updateItem(date, empty); // Chiama il metodo super.
                setDisable(empty || date.isBefore(LocalDate.now())); // Disabilita le celle vuote o quelle con date precedenti a oggi.
            }
        });

        dataFineDatePicker.setDayCellFactory(picker -> new DateCell() { // Imposta una cell factory personalizzata per il DatePicker della data di fine.
            @Override
            public void updateItem(LocalDate date, boolean empty) { // Metodo chiamato per aggiornare ogni cella del calendario.
                super.updateItem(date, empty); // Chiama il metodo super.
                LocalDate minDate = (dataInizioDatePicker.getValue() != null) ? dataInizioDatePicker.getValue() : LocalDate.now(); // La data minima selezionabile è la data di inizio o oggi.
                setDisable(empty || date.isBefore(minDate)); // Disabilita le celle vuote o quelle con date precedenti alla data minima.
            }
        });

        SpinnerValueFactory<Integer> valueFactory = // Crea una factory di valori per lo Spinner.
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1); // Inizializza lo Spinner con valori interi, minimo 1, massimo 1, valore iniziale 1.
        numeroGiorniSpinner.setValueFactory(valueFactory); // Imposta la factory di valori per lo Spinner.
        numeroGiorniSpinner.setEditable(true); // Rende lo Spinner modificabile tramite digitazione.

        numeroGiorniSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { // Aggiunge un listener al cambio di valore dello Spinner.
            if (newValue != null) { // Se il nuovo valore non è nullo.
                numeroGiorniEffettivo = newValue; // Aggiorna il numero di giorni effettivo.
                validateNumeroGiorniInput(); // Valida l'input del numero di giorni.
            }
        });

        numeroGiorniSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> { // Aggiunge un listener al cambio di testo nell'editor dello Spinner.
            if (!newValue.matches("\\d*")) { // Se il nuovo testo non contiene solo cifre.
                Platform.runLater(() -> numeroGiorniSpinner.getEditor().setText(oldValue)); // Ripristina il valore precedente (per evitare input non numerici).
            }
        });

        dataInizioDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> { // Aggiunge un listener al cambio di valore del DatePicker della data di inizio.
            calcolaEImpostaNumeroGiorni(); // Chiama il metodo per ricalcolare e impostare il numero di giorni.
        });
        dataFineDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> { // Aggiunge un listener al cambio di valore del DatePicker della data di fine.
            calcolaEImpostaNumeroGiorni(); // Chiama il metodo per ricalcolare e impostare il numero di giorni.
        });

        calcolaEImpostaNumeroGiorni(); // Chiama il metodo all'inizializzazione per impostare i valori iniziali.

        numeroGiorniCalcolato = 0; // Inizializza il numero di giorni calcolato a 0.
        numeroGiorniEffettivo = 0; // Inizializza il numero di giorni effettivo a 0.
    }

    private void calcolaEImpostaNumeroGiorni() { // Metodo privato per calcolare e impostare il numero di giorni tra le date selezionate.
        LocalDate start = dataInizioDatePicker.getValue(); // Ottiene la data di inizio.
        LocalDate end = dataFineDatePicker.getValue(); // Ottiene la data di fine.

        if (start != null && end != null && !end.isBefore(start)) { // Se entrambe le date sono selezionate e la data di fine non è precedente alla data di inizio.
            long days = ChronoUnit.DAYS.between(start, end) + 1; // Calcola la differenza in giorni (+1 per includere la data di inizio).
            numeroGiorniCalcolato = (int) days; // Converte in int e lo assegna al numero di giorni calcolato.

            SpinnerValueFactory<Integer> valueFactory = numeroGiorniSpinner.getValueFactory(); // Ottiene la factory di valori dello Spinner.
            if (valueFactory instanceof SpinnerValueFactory.IntegerSpinnerValueFactory) { // Se la factory è di tipo IntegerSpinnerValueFactory.
                SpinnerValueFactory.IntegerSpinnerValueFactory intValueFactory = // Effettua il cast.
                        (SpinnerValueFactory.IntegerSpinnerValueFactory) valueFactory;

                intValueFactory.setMax(numeroGiorniCalcolato); // Imposta il valore massimo dello Spinner al numero di giorni calcolato.

                if (numeroGiorniSpinner.getValue() == null || numeroGiorniSpinner.getValue() <= 0 || numeroGiorniSpinner.getValue() > numeroGiorniCalcolato) { // Se il valore attuale dello Spinner è nullo, <=0, o maggiore del massimo calcolato.
                    intValueFactory.setValue(Math.max(1, numeroGiorniCalcolato)); // Imposta il valore dello Spinner a 1 o al numero di giorni calcolato, a seconda di quale sia maggiore.
                }
            }
            erroreNumeroGiorniLabel.setText(""); // Pulisce la label di errore.
        } else { // Se le date non sono valide o non sono entrambe selezionate.
            numeroGiorniCalcolato = 0; // Reset del numero di giorni calcolato.
            if (numeroGiorniSpinner.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory) { // Se la factory è di tipo IntegerSpinnerValueFactory.
                SpinnerValueFactory.IntegerSpinnerValueFactory intValueFactory = // Effettua il cast.
                        (SpinnerValueFactory.IntegerSpinnerValueFactory) numeroGiorniSpinner.getValueFactory();
                intValueFactory.setMax(1); // Imposta il massimo a 1.
            }
            numeroGiorniSpinner.getValueFactory().setValue(1); // Imposta il valore dello Spinner a 1.
            erroreNumeroGiorniLabel.setText(""); // Pulisce la label di errore.
        }
        numeroGiorniEffettivo = numeroGiorniSpinner.getValue(); // Aggiorna il numero di giorni effettivo con il valore attuale dello Spinner.
    }

    private boolean validateNumeroGiorniInput() { // Metodo privato per validare l'input dello Spinner del numero di giorni.
        erroreNumeroGiorniLabel.setText(""); // Pulisce la label di errore.

        Integer inputGiorni = numeroGiorniSpinner.getValue(); // Ottiene il valore dallo Spinner.

        if (inputGiorni == null) { // Se l'input è nullo.
            erroreNumeroGiorniLabel.setText("Inserisci un numero di giorni valido."); // Imposta il messaggio di errore.
            return false; // Restituisce false (validazione fallita).
        }

        if (inputGiorni <= 0) { // Se l'input è minore o uguale a 0.
            erroreNumeroGiorniLabel.setText("Il numero di giorni deve essere maggiore di 0."); // Imposta il messaggio di errore.
            return false; // Restituisce false.
        }
        if (numeroGiorniCalcolato > 0 && inputGiorni > numeroGiorniCalcolato) { // Se il numero di giorni calcolato è positivo e l'input è maggiore del calcolato.
            erroreNumeroGiorniLabel.setText("Non puoi inserire più giorni del range di date selezionato (" + numeroGiorniCalcolato + ")."); // Imposta il messaggio di errore.
            return false; // Restituisce false.
        }

        numeroGiorniEffettivo = inputGiorni; // Se la validazione ha successo, aggiorna il numero di giorni effettivo.
        return true; // Restituisce true (validazione riuscita).
    }


    @FXML
    private void switchToAggiungiAlimenti(ActionEvent event) { // Metodo FXML per passare alla schermata di aggiunta alimenti, dopo aver salvato la nuova dieta.
        titoloPiano = titoloPianoTextField.getText(); // Ottiene il titolo del piano dal campo di testo.
        dataInizio = dataInizioDatePicker.getValue(); // Ottiene la data di inizio dal DatePicker.
        dataFine = dataFineDatePicker.getValue(); // Ottiene la data di fine dal DatePicker.

        String errorMessage = null; // Variabile per memorizzare un eventuale messaggio di errore.

        if (titoloPiano == null || titoloPiano.trim().isEmpty()) { // Se il titolo è nullo o vuoto.
            errorMessage = "Il titolo del piano non può essere vuoto."; // Imposta il messaggio di errore.
        } else if (dataInizio == null || dataFine == null) { // Se una delle date non è selezionata.
            errorMessage = "Devi selezionare sia la Data Inizio che la Data Fine."; // Imposta il messaggio di errore.
        } else { // Se titolo e date sono presenti.
            LocalDate start = dataInizioDatePicker.getValue(); // Ottiene la data di inizio.
            LocalDate end = dataFineDatePicker.getValue(); // Ottiene la data di fine.
            if (start != null && end != null && !end.isBefore(start)) { // Se le date sono valide e la data di fine non è prima della data di inizio.
                numeroGiorniCalcolato = (int) (ChronoUnit.DAYS.between(start, end) + 1); // Ricalcola il numero di giorni.
                if (!validateNumeroGiorniInput()) { // Se la validazione dello spinner fallisce.
                    errorMessage = erroreNumeroGiorniLabel.getText(); // Ottiene il messaggio di errore dallo spinner.
                }
            } else { // Se le date non sono valide.
                errorMessage = "La Data Fine non può essere precedente alla Data Inizio o le date non sono valide."; // Imposta il messaggio di errore.
            }
        }

        if (errorMessage != null) { // Se è presente un messaggio di errore.
            showAlert(AlertType.WARNING, "Attenzione!", errorMessage); // Mostra un Alert con l'errore.
            return; // Esce dal metodo.
        }

        System.out.println("---- Inizio salvataggio piano nel DB ----"); // Messaggio di debug.
        Connection conn = null; // Dichiarazione della connessione al database.
        PreparedStatement psDieta = null; // Dichiarazione dello statement preparato per la dieta.
        PreparedStatement psGiorno = null; // Dichiarazione dello statement preparato per il giorno.
        ResultSet generatedKeys = null; // Dichiarazione del ResultSet per le chiavi generate.

        try { // Inizia un blocco try-catch per la gestione degli errori SQL e I/O.
            conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
            System.out.println("Connessione stabilita: " + (conn != null)); // Messaggio di debug.

            conn.setAutoCommit(false); // Disabilita l'autocommit per gestire la transazione manualmente.

            String insertDietaSql = "INSERT INTO Diete (id_cliente, nome_dieta, data_inizio, data_fine, id_nutrizionista) VALUES (NULL, ?, ?, ?, ?)"; // Query SQL per inserire una nuova dieta.
            psDieta = conn.prepareStatement(insertDietaSql, Statement.RETURN_GENERATED_KEYS); // Prepara lo statement, chiedendo di ritornare le chiavi generate.
            psDieta.setString(1, titoloPiano); // Imposta il titolo del piano.
            psDieta.setString(2, dataInizio.toString()); // Imposta la data di inizio.
            psDieta.setString(3, dataFine.toString()); // Imposta la data di fine.
            psDieta.setInt(4, Session.getUserId()); // Imposta l'ID del nutrizionista dalla Sessione.
            psDieta.executeUpdate(); // Esegue l'inserimento.

            generatedKeys = psDieta.getGeneratedKeys(); // Ottiene le chiavi generate dal database.
            int idDieta; // Variabile per l'ID della dieta generato.
            if (generatedKeys.next()) { // Se ci sono chiavi generate.
                idDieta = generatedKeys.getInt(1); // Ottiene la prima chiave generata (l'ID della dieta).
                System.out.println("ID Dieta generato: " + idDieta); // Messaggio di debug.
            } else { // Se nessun ID è stato generato.
                throw new SQLException("Errore nella creazione della dieta: nessun ID ottenuto."); // Lancia un'eccezione SQL.
            }

            // Use numeroGiorniEffettivo for database insertion
            String insertGiornoSql = "INSERT INTO Giorno_dieta (id_dieta, nome_giorno, calorie_giorno, proteine_giorno, carboidrati_giorno, grassi_giorno) VALUES (?, ?,0,0,0,0)"; // Query SQL per inserire i giorni della dieta.
            psGiorno = conn.prepareStatement(insertGiornoSql); // Prepara lo statement.

            for (int i = 0; i < numeroGiorniEffettivo; i++) { // Itera per il numero di giorni effettivo.
                psGiorno.setInt(1, idDieta); // Imposta l'ID della dieta.
                psGiorno.setString(2, "Giorno " + (i + 1)); // Imposta il nome del giorno (es. "Giorno 1").
                psGiorno.addBatch(); // Aggiunge l'operazione al batch.
            }

            psGiorno.executeBatch(); // Esegue tutte le operazioni di inserimento dei giorni in batch.
            conn.commit(); // Esegue il commit della transazione, rendendo permanenti le modifiche.

            System.out.println("Salvataggio piano dieta completato con successo nel DB."); // Messaggio di successo.

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiGiornoDieta.fxml")); // Crea un FXMLLoader per la schermata successiva (AggiungiGiornoDieta).
            Parent aggiungiAlimentiRoot = loader.load(); // Carica il root node della nuova schermata.

            AggiungiGiornoDieta aggiungiAlimentiController = loader.getController(); // Ottiene il controller della nuova schermata.
            aggiungiAlimentiController.setTitoloPiano(titoloPiano); // Imposta il titolo del piano nel nuovo controller.
            aggiungiAlimentiController.setNumeroGiorni(numeroGiorniEffettivo); // Pass the actual days from spinner: Passa il numero effettivo di giorni.
            aggiungiAlimentiController.setIdDieta(idDieta); // Imposta l'ID della dieta nel nuovo controller.

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage corrente.
            stage.setScene(new Scene(aggiungiAlimentiRoot)); // Imposta la nuova scena.
            stage.setTitle("Aggiungi Alimenti al Piano"); // Imposta il titolo della finestra.
            stage.show(); // Mostra la nuova finestra.

        } catch (SQLException e) { // Cattura le eccezioni SQL.
            System.err.println("Errore SQL durante il salvataggio del piano dieta: " + e.getMessage()); // Messaggio di errore.
            e.printStackTrace(); // Stampa lo stack trace.
            try { // Tenta un rollback in caso di errore.
                if (conn != null) conn.rollback(); // Se la connessione esiste, esegue il rollback.
            } catch (SQLException ex) { // Cattura eventuali errori durante il rollback.
                System.err.println("Errore durante il rollback: " + ex.getMessage()); // Messaggio di errore.
                ex.printStackTrace(); // Stampa lo stack trace.
            }
            showAlert(AlertType.ERROR,"Errore","Errore durante il salvataggio della dieta."); // Mostra un Alert di errore.
        } catch (IOException e) { // Cattura le eccezioni I/O.
            System.err.println("Errore nel caricamento della schermata successiva: " + e.getMessage()); // Messaggio di errore.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(AlertType.ERROR,"Errore","Errore nel caricamento della schermata successiva."); // Mostra un Alert di errore.

        } finally { // Blocco finally, eseguito sempre.
            try { // Tenta di chiudere tutte le risorse del database.
                if (generatedKeys != null) generatedKeys.close(); // Chiude il ResultSet.
                if (psDieta != null) psDieta.close(); // Chiude lo statement della dieta.
                if (psGiorno != null) psGiorno.close(); // Chiude lo statement del giorno.
                if (conn != null) conn.close(); // Chiude la connessione.
                System.out.println("Risorse del DB chiuse correttamente."); // Messaggio di debug.
            } catch (SQLException ex) { // Cattura eventuali errori durante la chiusura delle risorse.
                System.err.println("Errore chiusura risorse DB: " + ex.getMessage()); // Messaggio di errore.
                ex.printStackTrace(); // Stampa lo stack trace.
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) { // Metodo privato per mostrare un Alert all'utente.
        Alert alert = new Alert(alertType); // Crea una nuova istanza di Alert.
        alert.setTitle(title); // Imposta il titolo.
        alert.setHeaderText(null); // Non mostra un header text.
        alert.setContentText(message); // Imposta il contenuto.

        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css"); // Ottiene l'URL del file CSS per lo stile dell'alert.
        if (cssUrl != null) { // Se il CSS viene trovato.
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // Aggiunge il CSS al DialogPane.
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base.
            if (alertType == Alert.AlertType.INFORMATION) { // Se il tipo di alert è INFORMATION.
                alert.getDialogPane().getStyleClass().add("alert-information"); // Aggiunge la classe di stile specifica.
            } else if (alertType == Alert.AlertType.WARNING) { // Se il tipo di alert è WARNING.
                alert.getDialogPane().getStyleClass().add("alert-warning"); // Aggiunge la classe di stile specifica.
            } else if (alertType == Alert.AlertType.ERROR) { // Se il tipo di alert è ERROR.
                alert.getDialogPane().getStyleClass().add("alert-error"); // Aggiunge la classe di stile specifica.
            } else if (alertType == Alert.AlertType.CONFIRMATION) { // Se il tipo di alert è CONFIRMATION.
                alert.getDialogPane().getStyleClass().add("alert-confirmation"); // Aggiunge la classe di stile specifica.
            }
        } else { // Se il CSS non viene trovato.
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Messaggio di errore.
        }

        alert.showAndWait(); // Mostra l'avviso e attende che l'utente lo chiuda.
    }
}