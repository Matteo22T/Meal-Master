package com.matteotocci.app.controller;

import com.matteotocci.app.model.*;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


// Dichiarazione della classe DettaglioRicettaController. Questo controller gestisce la vista di dettaglio di una ricetta.
public class DettaglioRicettaController {

    // Campi annotati con @FXML per l'iniezione dei componenti UI definiti nel file FXML.
    @FXML private Label nomeRicettaLabel; // Label per visualizzare il nome della ricetta.
    @FXML private TextArea descrizioneArea; // Area di testo per visualizzare la descrizione della ricetta.
    @FXML private Label categoriaLabel; // Label per visualizzare la categoria della ricetta.
    @FXML private Label autoreLabel; // Label per visualizzare il nome dell'autore della ricetta.

    @FXML private Label kcalLabel; // Label per visualizzare il totale delle calorie.
    @FXML private Label proteineLabel; // Label per visualizzare il totale delle proteine.
    @FXML private Label carboidratiLabel; // Label per visualizzare il totale dei carboidrati.
    @FXML private Label grassiLabel; // Label per visualizzare il totale dei grassi.
    @FXML private Label grassiSaturiLabel; // Label per visualizzare il totale dei grassi saturi.
    @FXML private Label zuccheriLabel; // Label per visualizzare il totale degli zuccheri.
    @FXML private Label fibreLabel; // Label per visualizzare il totale delle fibre.
    @FXML private Label saleLabel; // Label per visualizzare il totale del sale.

    @FXML private Button bottoneElimina; // Bottone per eliminare la ricetta.

    @FXML private TableView<IngredienteVisuale> ingredientiTable; // TableView per mostrare gli ingredienti della ricetta.
    @FXML private TableColumn<IngredienteVisuale, String> nomeCol; // Colonna della tabella per il nome dell'ingrediente.
    @FXML private TableColumn<IngredienteVisuale, Double> quantitaCol; // Colonna della tabella per la quantità dell'ingrediente.

    private Ricetta ricetta; // Oggetto Ricetta che rappresenta la ricetta visualizzata in dettaglio.

    // Metodo per impostare l'oggetto Ricetta in questo controller.
    public void setRicetta(Ricetta ricetta) {
        this.ricetta = ricetta; // Inizializza la variabile d'istanza `ricetta` con l'oggetto passato.
        caricaDatiRicetta(); // Chiama il metodo per caricare i dati della ricetta nell'UI.
    }

    // Metodo privato per caricare i dati della ricetta dal database e visualizzarli nell'interfaccia utente.
    private void caricaDatiRicetta() {
        // Query SQL per recuperare i dettagli della ricetta e le informazioni sull'autore.
        String queryRicetta = "SELECT r.nome, r.descrizione, r.id_utente,r.categoria, u.nome AS nome_autore, u.cognome AS cognome_autore, " +
                "r.kcal, r.proteine, r.carboidrati, r.zuccheri, r.grassi, r.grassi_saturi, r.fibre, r.sale " +
                "FROM Ricette r JOIN Utente u ON r.id_utente = u.id WHERE r.id = ?";

        // Query SQL per recuperare gli ingredienti associati alla ricetta.
        String queryIngredienti = "SELECT f.nome, ir.quantita_grammi FROM ingredienti_ricette ir " +
                "JOIN foods f ON ir.id_alimento = f.id WHERE ir.id_ricetta = ?";

        // Blocco try-with-resources per gestire automaticamente la chiusura di Connection e PreparedStatement.
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database SQLite.
             PreparedStatement stmtRicetta = conn.prepareStatement(queryRicetta); // Prepara lo statement per la query della ricetta.
             PreparedStatement stmtIngredienti = conn.prepareStatement(queryIngredienti)) { // Prepara lo statement per la query degli ingredienti.

            // Imposta il parametro ID nella query della ricetta.
            stmtRicetta.setInt(1, ricetta.getId());
            ResultSet rs = stmtRicetta.executeQuery(); // Esegue la query e ottiene il ResultSet.

            // Se c'è un risultato (la ricetta è stata trovata).
            if (rs.next()) {
                // Imposta i valori delle Label con i dati recuperati dal ResultSet.
                nomeRicettaLabel.setText(rs.getString("nome"));
                descrizioneArea.setText(rs.getString("descrizione"));
                categoriaLabel.setText(rs.getString("categoria"));

                // Imposta i valori nutrizionali, formattandoli a una cifra decimale.
                kcalLabel.setText(String.format("%.1f", rs.getDouble("kcal")));
                proteineLabel.setText(String.format("%.1f", rs.getDouble("proteine")));
                carboidratiLabel.setText(String.format("%.1f", rs.getDouble("carboidrati")));
                zuccheriLabel.setText(String.format("%.1f", rs.getDouble("zuccheri")));
                grassiLabel.setText(String.format("%.1f", rs.getDouble("grassi")));
                grassiSaturiLabel.setText(String.format("%.1f", rs.getDouble("grassi_saturi")));
                fibreLabel.setText(String.format("%.1f", rs.getDouble("fibre")));
                saleLabel.setText(String.format("%.1f", rs.getDouble("sale")));

                int idUtente = rs.getInt("id_utente"); // Ottiene l'ID dell'utente che ha creato la ricetta.
                caricaAutore(idUtente, conn); // Chiama il metodo per caricare il nome dell'autore.
            }

            // Imposta il parametro ID nella query degli ingredienti.
            stmtIngredienti.setInt(1, ricetta.getId());
            ResultSet rs2 =stmtIngredienti.executeQuery(); // Esegue la query e ottiene il ResultSet degli ingredienti.

            ObservableList<IngredienteVisuale> lista = FXCollections.observableArrayList(); // Crea una lista osservabile per gli ingredienti.

            // Itera sui risultati della query degli ingredienti.
            while(rs2.next()) {
                String nomeAlimento = rs2.getString("nome"); // Ottiene il nome dell'ingrediente.
                int quantita = rs2.getInt("quantita_grammi"); // Ottiene la quantità dell'ingrediente.
                lista.add(new IngredienteVisuale(nomeAlimento, quantita)); // Aggiunge un nuovo oggetto IngredienteVisuale alla lista.
            }
            // Collega le proprietà "nome" e "quantita" dell'oggetto IngredienteVisuale alle rispettive colonne della TableView.
            nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
            quantitaCol.setCellValueFactory(new PropertyValueFactory<>("quantita"));
            ingredientiTable.setItems(lista); // Imposta la lista di ingredienti nella TableView.


        } catch (SQLException e) {
            e.printStackTrace(); // Stampa la traccia dello stack in caso di errore SQL.
        }
    }

    // Metodo privato per caricare il nome e cognome dell'autore di una ricetta dal database.
    private void caricaAutore(int idUtente, Connection conn) {
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?"; // Query SQL per selezionare nome e cognome dell'utente.
        try (PreparedStatement stmt = conn.prepareStatement(query)) { // Prepara lo statement SQL.
            stmt.setInt(1, idUtente); // Imposta l'ID dell'utente come parametro.
            ResultSet rs = stmt.executeQuery(); // Esegue la query.

            if (rs.next()) { // Se l'utente è stato trovato.
                String nomeCompleto = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena nome e cognome.
                autoreLabel.setText(nomeCompleto); // Imposta il nome completo nella Label dell'autore.
            } else {
                autoreLabel.setText("Sconosciuto"); // Se l'utente non è trovato, imposta "Sconosciuto".
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Stampa la traccia dello stack in caso di errore SQL.
        }
    }

    // Classe interna statica per rappresentare un ingrediente in modo adatto alla visualizzazione in una TableView.
    public static class IngredienteVisuale {
        private final SimpleStringProperty nome; // Proprietà osservabile per il nome dell'ingrediente.
        private final SimpleDoubleProperty quantita; // Proprietà osservabile per la quantità dell'ingrediente.

        // Costruttore della classe IngredienteVisuale.
        public IngredienteVisuale(String nome, double quantita) {
            this.nome = new SimpleStringProperty(nome); // Inizializza la proprietà nome.
            this.quantita = new SimpleDoubleProperty(quantita); // Inizializza la proprietà quantità.
        }

        // Getter per il nome dell'ingrediente.
        public String getNome() {
            return nome.get();
        }

        // Getter per la quantità dell'ingrediente.
        public double getQuantita() {
            return quantita.get();
        }

    }

    private Ricette ricettaController; // Riferimento al controller `Ricette` (probabilmente il controller della lista di ricette).

    // Metodo per impostare il riferimento al controller `Ricette`.
    public void setRicettaController(Ricette controller) {
        this.ricettaController = controller; // Inizializza il riferimento al controller esterno.
    }

    // Metodo FXML chiamato quando viene premuto il bottone "Elimina Ricetta".
    @FXML
    private void handleEliminaRicetta(ActionEvent event) {
        System.out.println("User corrente: " + Session.getUserId()); // Stampa l'ID dell'utente attualmente loggato (per debug).
        System.out.println("User dell'alimento: " + ricetta.getUserId()); // Stampa l'ID dell'utente che ha creato la ricetta (per debug).

        // Verifica se l'ID dell'utente loggato corrisponde all'ID dell'utente che ha aggiunto la ricetta.
        if (ricetta.getUserId()==(Session.getUserId())) {
            eliminaRicetta(ricetta); // Chiama il metodo per eliminare la ricetta se l'utente è autorizzato.
        } else {
            // Mostra un messaggio di errore se l'utente non è autorizzato a eliminare la ricetta.
            showAlert(Alert.AlertType.ERROR,"Errore nell'eliminazione", "Puoi eliminare solo le ricette che hai aggiunto.");
        }
    }

    // Metodo privato per eliminare una ricetta dal database.
    private void eliminaRicetta(Ricetta ricetta) {
        System.out.println("Alimento eliminato: " + ricetta.getNome()); // Debug: nome della ricetta da eliminare.
        String query = "DELETE FROM Ricette WHERE id = ?"; // Query SQL per eliminare la ricetta tramite ID.
        // Blocco try-with-resources per gestire automaticamente la chiusura di Connection e PreparedStatement.
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            System.out.println("ID eliminato: " + ricetta.getId()); // Debug: ID della ricetta da eliminare.
            stmt.setInt(1, ricetta.getId()); // Imposta l'ID della ricetta nel prepared statement.
            int affected = stmt.executeUpdate(); // Esegue l'eliminazione e ottiene il numero di righe affette.
            System.out.println("Righe eliminate: " + affected); // Debug: numero di righe eliminate.

            if (affected > 0) { // Se almeno una riga è stata eliminata (eliminazione riuscita).
                if (ricettaController != null) { // Se esiste un riferimento al controller delle ricette.
                    System.out.println("filtro: "+ricettaController.getFiltro()); // Debug: filtro corrente del controller ricette.
                    ricettaController.resetRicerca(); // Resetta la ricerca nel controller delle ricette.
                    ricettaController.cercaRicette(ricettaController.getFiltro(),false); // Ricarica le ricette nel controller principale con il filtro corrente.
                }
                // Chiude la finestra corrente dopo che l'eliminazione è andata a buon fine.
                chiudiFinestra();
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa la traccia dello stack in caso di errore SQL.
        }
    }


    // Metodo privato per mostrare un Alert (finestra di messaggio) all'utente.
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType); // Crea un nuovo Alert del tipo specificato (es. ERROR, INFORMATION).
        alert.setTitle(title); // Imposta il titolo della finestra di alert.
        alert.setHeaderText(null); // Rimuove l'header text.
        alert.setContentText(message); // Imposta il contenuto del messaggio.
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css"); // Tenta di caricare il CSS per lo stile dell'alert.
        if (cssUrl != null) { // Se il CSS è stato trovato.
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // Aggiunge il CSS ai fogli di stile del dialog pane.
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base.
            // Aggiunge classi di stile specifiche in base al tipo di Alert per personalizzare ulteriormente l'aspetto.
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Messaggio di errore se il CSS non viene trovato.
        }

        alert.showAndWait(); // Mostra l'alert e attende che l'utente lo chiuda.
    }


    // Metodo FXML per chiudere la finestra corrente.
    @FXML
    private void chiudiFinestra() {
        // Ottiene il riferimento alla Stage (finestra) tramite un componente UI e la chiude.
        ((Stage) nomeRicettaLabel.getScene().getWindow()).close();
    }

    private String origineFXML; // Variabile per memorizzare il nome del file FXML da cui è stata aperta questa finestra.

    // Metodo per impostare la stringa `origineFXML`.
    public void setOrigineFXML(String origineFXML) {
        this.origineFXML = origineFXML; // Inizializza la variabile `origineFXML`.
        aggiornaVisibilitaBottone(); // Chiama il metodo per aggiornare la visibilità del bottone di eliminazione.
    }

    // Metodo privato per aggiornare la visibilità del bottone di eliminazione.
    private void aggiornaVisibilitaBottone() {
        // Il bottone "Elimina" è visibile solo se la finestra è stata aperta da "Ricette.fxml"
        // E l'utente loggato è lo stesso che ha creato la ricetta.
        bottoneElimina.setVisible("Ricette.fxml".equals(origineFXML) && Session.getUserId().equals(ricetta.getUserId()));
    }
}