package com.matteotocci.app.controller; // Dichiara il package della classe.
import com.matteotocci.app.model.Dieta; // Importa la classe Dieta dal modello.
import com.matteotocci.app.model.SQLiteConnessione; // Importa la classe per la connessione SQLite.
import com.matteotocci.app.model.Alimento; // Importa la classe Alimento dal modello.
import com.matteotocci.app.model.Ricetta; // Importa la classe Ricetta dal modello.
import com.matteotocci.app.model.Session; // Importa la classe Session per la gestione dell'utente.
import javafx.collections.FXCollections; // Importa la classe per utility di collezioni osservabili.
import javafx.collections.ObservableList; // Importa l'interfaccia per liste osservabili.
import javafx.event.ActionEvent; // Importa la classe per gli eventi di azione.
import javafx.fxml.FXML; // Importa l'annotazione FXML per collegare elementi UI.
import javafx.fxml.FXMLLoader; // Importa la classe per caricare file FXML.
import javafx.fxml.Initializable; // Importa l'interfaccia Initializable.
import javafx.scene.Node; // Importa la classe base per i nodi del grafo della scena.
import javafx.scene.Parent; // Importa la classe Parent per nodi contenitori.
import javafx.scene.Scene; // Importa la classe Scene per il contenuto di una finestra.
import javafx.scene.control.*; // Importa tutti i controlli UI standard di JavaFX.
import javafx.stage.Stage; // Importa la classe Stage per le finestre.
import javafx.application.Platform; // Importa Platform per eseguire codice sul thread UI di JavaFX.

import java.io.IOException; // Importa l'eccezione per errori di input/output.
import java.net.URL; // Importa la classe URL (necessaria per Initializable).
import java.sql.*; // Importa tutte le classi SQL (Connection, PreparedStatement, ResultSet, SQLException, Statement).
import java.util.HashMap; // Importa la classe HashMap per implementazioni di mappe.
import java.util.Map; // Importa l'interfaccia Map.
import java.util.ResourceBundle; // Importa la classe ResourceBundle (necessaria per Initializable).

public class AggiungiGiornoDieta implements Initializable { // Dichiara la classe controller e implementa Initializable.

    private String titoloPiano; // Variabile per memorizzare il titolo del piano dieta.
    private int numeroGiorni; // Variabile per memorizzare il numero totale di giorni nel piano.
    private int giornoCorrente = 1; // Variabile per tracciare il giorno corrente visualizzato/modificato, inizializzato a 1.
    private int idDieta; // Variabile per memorizzare l'ID della dieta nel database.


    @FXML
    private Label giornoCorrenteLabel; // Campo FXML per la label che mostra il giorno corrente.
    @FXML
    private ListView<String> colazioneListView; // Campo FXML per la ListView della colazione.
    @FXML
    private ListView<String> spuntinoMattinaListView; // Campo FXML per la ListView dello spuntino del mattino.
    @FXML
    private ListView<String> pranzoListView; // Campo FXML per la ListView del pranzo.
    @FXML
    private ListView<String> spuntinoPomeriggioListView; // Campo FXML per la ListView dello spuntino del pomeriggio.
    @FXML
    private ListView<String> cenaListView; // Campo FXML per la ListView della cena.
    @FXML
    private Label kcalTotaliLabel; // Campo FXML per la label delle calorie totali.
    @FXML
    private Label carboidratiLabel; // Campo FXML per la label dei carboidrati totali.
    @FXML
    private Label proteineLabel; // Campo FXML per la label delle proteine totali.
    @FXML
    private Label grassiLabel; // Campo FXML per la label dei grassi totali.
    @FXML
    private TextField nomeGiornoTextField; // Campo FXML per il campo di testo del nome del giorno.

    private String pastoSelezionato; // Variabile per memorizzare il pasto attualmente selezionato per l'aggiunta.

    private Map<Integer, String> nomiGiorni = new HashMap<>(); // Mappa che associa l'indice del giorno al suo nome.
    private Map<Integer, Integer> idGiornoDietaMap = new HashMap<>(); // Mappa che associa l'indice del giorno al suo ID nel database.

    // Mappa per gli alimenti (ora rinominata per chiarezza)
    private Map<Integer, Map<String, ObservableList<AlimentoQuantificato>>> giorniPastiAlimenti = new HashMap<>(); // Mappa nidificata per memorizzare alimenti quantificati per ogni pasto e giorno.
    // NUOVA Mappa per le ricette
    private Map<Integer, Map<String, ObservableList<RicettaQuantificata>>> giorniPastiRicette = new HashMap<>(); // Nuova mappa nidificata per memorizzare ricette quantificate per ogni pasto e giorno.


    public static class AlimentoQuantificato { // Classe statica interna per rappresentare un alimento con la sua quantità.
        private Alimento alimento; // L'oggetto Alimento.
        private int quantita; // La quantità in grammi dell'alimento.

        public AlimentoQuantificato(Alimento alimento, int quantita) { // Costruttore per AlimentoQuantificato.
            this.alimento = alimento; // Inizializza l'alimento.
            this.quantita = quantita; // Inizializza la quantità.
        }

        public Alimento getAlimento() { // Metodo getter per l'alimento.
            return alimento; // Restituisce l'oggetto Alimento.
        }

        public int getQuantita() { // Metodo getter per la quantità.
            return quantita; // Restituisce la quantità.
        }

        @Override
        public String toString() { // Sovrascrive il metodo toString per una rappresentazione testuale.
            return alimento.getNome() + " (" + quantita + " g)"; // Restituisce il nome dell'alimento e la sua quantità.
        }
    }

    // NUOVA classe per le ricette quantificate
    public static class RicettaQuantificata { // Nuova classe statica interna per rappresentare una ricetta con la sua quantità.
        private Ricetta ricetta; // L'oggetto Ricetta.
        private int quantita; // La quantità in grammi della ricetta.

        public RicettaQuantificata(Ricetta ricetta, int quantita) { // Costruttore per RicettaQuantificata.
            this.ricetta = ricetta; // Inizializza la ricetta.
            this.quantita = quantita; // Inizializza la quantità.
        }

        public Ricetta getRicetta() { // Metodo getter per la ricetta.
            return ricetta; // Restituisce l'oggetto Ricetta.
        }

        public int getQuantita() { // Metodo getter per la quantità.
            return quantita; // Restituisce la quantità.
        }

        @Override
        public String toString() { // Sovrascrive il metodo toString per una rappresentazione testuale.
            return ricetta.getNome() + " (Ricetta - " + quantita + " g)"; // Restituisce il nome della ricetta e la sua quantità.
        }
    }

    public void setIdDieta(int idDieta) { // Metodo setter per l'ID della dieta.
        this.idDieta = idDieta; // Imposta l'ID della dieta.
    }

    // AGGIUNTO: Metodo per impostare l'ID del giorno dieta per un indice specifico.
    // Sarà chiamato dal controller che crea/carica i giorni della dieta.
    public void setIdGiornoDietaForCurrentDay(int giornoIndex, int idGenerated) { // Metodo per associare un ID di giorno dieta a un indice di giorno.
        idGiornoDietaMap.put(giornoIndex, idGenerated); // Aggiunge la mappatura alla mappa.
        System.out.println("ID giorno dieta " + idGenerated + " mappato al giorno " + giornoIndex); // Stampa a console per debug.
    }


    public void setTitoloPiano(String titolo) { // Metodo setter per il titolo del piano dieta.
        this.titoloPiano = titolo; // Imposta il titolo del piano.
    }

    public void setNumeroGiorni(int numero) { // Metodo setter per il numero totale di giorni.
        this.numeroGiorni = numero; // Imposta il numero di giorni.
        aggiornaIndicatoreGiorno(); // Aggiorna la label che indica il giorno corrente.
    }

    private void aggiornaIndicatoreGiorno() { // Metodo privato per aggiornare la label del giorno corrente e il campo nome giorno.
        giornoCorrenteLabel.setText("Giorno corrente: " + giornoCorrente + "/" + numeroGiorni); // Imposta il testo della label del giorno corrente.
        String nomeGiorno = nomiGiorni.getOrDefault(giornoCorrente, ""); // Ottiene il nome del giorno dalla mappa, o una stringa vuota se non presente.
        nomeGiornoTextField.setText(nomeGiorno); // Imposta il testo del campo nome giorno.
    }


    @FXML
    public void initialize(URL url, ResourceBundle resources) { // Metodo di inizializzazione del controller, chiamato dopo il caricamento dell'FXML.
        // Inizializza le mappe dei pasti per il giorno corrente all'avvio
        giorniPastiAlimenti.put(giornoCorrente, creaMappaPastiAlimentiVuota()); // Inizializza la mappa dei pasti per gli alimenti per il giorno corrente.
        giorniPastiRicette.put(giornoCorrente, creaMappaPastiRicetteVuota()); // AGGIUNTO: Inizializza la mappa dei pasti per le ricette per il giorno corrente.
        aggiornaIndicatoreGiorno(); // Aggiorna la label del giorno corrente.
        aggiornaListView(); // Aggiorna le ListView dei pasti.
        aggiornaTotali(); // Aggiorna i totali nutrizionali.
    }

    // Rinominato da creaMappaPastiVuota per chiarezza
    private Map<String, ObservableList<AlimentoQuantificato>> creaMappaPastiAlimentiVuota() { // Metodo privato per creare una mappa vuota di pasti per alimenti.
        Map<String, ObservableList<AlimentoQuantificato>> pasti = new HashMap<>(); // Crea una nuova HashMap per i pasti.
        pasti.put("colazione", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per la colazione.
        pasti.put("spuntinoMattina", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per lo spuntino del mattino.
        pasti.put("pranzo", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per il pranzo.
        pasti.put("spuntinoPomeriggio", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per lo spuntino del pomeriggio.
        pasti.put("cena", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per la cena.
        return pasti; // Restituisce la mappa dei pasti.
    }

    // NUOVO metodo per creare una mappa vuota per le ricette
    private Map<String, ObservableList<RicettaQuantificata>> creaMappaPastiRicetteVuota() { // Nuovo metodo privato per creare una mappa vuota di pasti per ricette.
        Map<String, ObservableList<RicettaQuantificata>> pasti = new HashMap<>(); // Crea una nuova HashMap per i pasti.
        pasti.put("colazione", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per la colazione.
        pasti.put("spuntinoMattina", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per lo spuntino del mattino.
        pasti.put("pranzo", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per il pranzo.
        pasti.put("spuntinoPomeriggio", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per lo spuntino del pomeriggio.
        pasti.put("cena", FXCollections.observableArrayList()); // Aggiunge una lista osservabile vuota per la cena.
        return pasti; // Restituisce la mappa dei pasti.
    }

    private Stage aggiungiAlimentoStage = null; // Variabile per mantenere il riferimento allo stage "Aggiungi Alimento/Ricetta".
    private AggiungiAlimentoDieta controllerAggiungi = null; // Variabile per mantenere il riferimento al controller "Aggiungi Alimento/Ricetta".

    @FXML
    public void openAggiungiAlimentoDieta(ActionEvent event) { // Metodo FXML per aprire la finestra "Aggiungi Alimento/Ricetta".
        pastoSelezionato = null; // Resetta il pasto selezionato.
        String buttonId = ((Node) event.getSource()).getId(); // Ottiene l'ID del bottone che ha generato l'evento.

        switch (buttonId) { // Switch per determinare il pasto selezionato in base all'ID del bottone.
            case "aggiungiColazioneButton":
                pastoSelezionato = "colazione";
                break; // Se il bottone è "aggiungiColazioneButton", imposta "colazione".
            case "aggiungiSpuntinoMattinaButton":
                pastoSelezionato = "spuntinoMattina";
                break; // Se il bottone è "aggiungiSpuntinoMattinaButton", imposta "spuntinoMattina".
            case "aggiungiPranzoButton":
                pastoSelezionato = "pranzo";
                break; // Se il bottone è "aggiungiPranzoButton", imposta "pranzo".
            case "aggiungiSpuntinoPomeriggioButton":
                pastoSelezionato = "spuntinoPomeriggio";
                break; // Se il bottone è "aggiungiSpuntinoPomeriggioButton", imposta "spuntinoPomeriggio".
            case "aggiungiCenaButton":
                pastoSelezionato = "cena";
                break; // Se il bottone è "aggiungiCenaButton", imposta "cena".
        }

        if (pastoSelezionato != null) { // Se un pasto è stato selezionato.
            try { // Inizia un blocco try-catch per gestire IOException.
                if (aggiungiAlimentoStage == null || !aggiungiAlimentoStage.isShowing()) { // Se lo stage non è stato creato o non è visibile.
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiAlimentoDieta.fxml")); // Crea un FXMLLoader per caricare l'FXML.
                    Parent aggiungiAlimentoRoot = fxmlLoader.load(); // Carica il root node dall'FXML.

                    controllerAggiungi = fxmlLoader.getController(); // Ottiene il controller della finestra appena caricata.
                    controllerAggiungi.setGiornoDietaController(this); // Imposta un riferimento a questo controller nel controller della nuova finestra.

                    aggiungiAlimentoStage = new Stage(); // Crea un nuovo Stage.
                    aggiungiAlimentoStage.setTitle("Aggiungi Alimento/Ricetta"); // Imposta il titolo dello stage.
                    aggiungiAlimentoStage.setScene(new Scene(aggiungiAlimentoRoot)); // Imposta la scena dello stage.

                    aggiungiAlimentoStage.setResizable(false); // Rende lo stage non ridimensionabile.
                    aggiungiAlimentoStage.setFullScreen(false); // Disabilita la modalità a schermo intero.

                    // Chiude la finestra se viene chiusa la principale
                    ((Stage) ((Node) event.getSource()).getScene().getWindow()).setOnCloseRequest(e -> { // Aggiunge un gestore per la chiusura della finestra principale.
                        if (aggiungiAlimentoStage != null) { // Se lo stage di aggiunta alimento/ricetta esiste.
                            aggiungiAlimentoStage.close(); // Lo chiude.
                        }
                    });

                    aggiungiAlimentoStage.show(); // Mostra lo stage.
                }

                // Aggiorna ogni volta il pasto selezionato
                if (controllerAggiungi != null) { // Se il controller della finestra di aggiunta esiste.
                    controllerAggiungi.setPastoCorrente(pastoSelezionato); // Aggiorna il pasto corrente nel controller della finestra di aggiunta.
                }

                // Porta in primo piano
                aggiungiAlimentoStage.toFront(); // Porta la finestra di aggiunta in primo piano.

            } catch (IOException e) { // Cattura l'eccezione IOException.
                e.printStackTrace(); // Stampa lo stack trace dell'errore.
            }
        }
    }


    public void aggiungiAlimentoAllaLista(String pasto, Alimento alimento, int quantita) { // Metodo per aggiungere un alimento alla lista del pasto.
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoCorrente); // Ottiene la mappa dei pasti per gli alimenti del giorno corrente.
        if (pastiAlimenti == null) { // Se la mappa dei pasti non esiste per il giorno corrente.
            pastiAlimenti = creaMappaPastiAlimentiVuota(); // La crea.
            giorniPastiAlimenti.put(giornoCorrente, pastiAlimenti); // La aggiunge alla mappa dei giorni.
        }
        pastiAlimenti.get(pasto).add(new AlimentoQuantificato(alimento, quantita)); // Aggiunge l'alimento quantificato alla lista del pasto specifico.
        aggiornaListView(); // Aggiorna le ListView per riflettere i cambiamenti.
        aggiornaTotali(); // Aggiorna i totali nutrizionali.
    }

    // NUOVO metodo: aggiunge una ricetta alla lista interna (UI e memoria, NESSUN DB QUI)
    public void aggiungiRicettaAllaLista(Ricetta ricetta, int quantita, String pasto) { // Nuovo metodo per aggiungere una ricetta alla lista del pasto.
        // 1. Aggiungi la ricetta alla struttura dati in memoria per la UI e i calcoli
        Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoCorrente); // Ottiene la mappa dei pasti per le ricette del giorno corrente.
        if (pastiRicette == null) { // Se la mappa dei pasti non esiste per il giorno corrente.
            pastiRicette = creaMappaPastiRicetteVuota(); // La crea.
            giorniPastiRicette.put(giornoCorrente, pastiRicette); // La aggiunge alla mappa dei giorni.
        }
        pastiRicette.get(pasto).add(new RicettaQuantificata(ricetta, quantita)); // Aggiunge la ricetta quantificata alla lista del pasto specifico.

        // 2. Aggiorna la UI (ListView e Totali)
        aggiornaListView(); // Aggiorna le ListView per riflettere i cambiamenti.
        aggiornaTotali(); // Aggiorna i totali nutrizionali.

        // N.B.: Il salvataggio nel database di questa ricetta NON avviene qui.
        // Avverrà nel metodo salvaPianoButtonAction() // Nota che il salvataggio nel DB avverrà altrove.
    }

    private void aggiornaListView() { // Metodo privato per aggiornare tutte le ListView dei pasti.
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoCorrente); // Ottiene la mappa dei pasti per gli alimenti.
        Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoCorrente); // AGGIUNTO: Ottiene la mappa dei pasti per le ricette.

        // Assicurati che le mappe per il giorno corrente esistano
        if (pastiAlimenti == null) { // Controllo per assicurare che la mappa degli alimenti esista.
            pastiAlimenti = creaMappaPastiAlimentiVuota(); // Crea la mappa se non esiste.
            giorniPastiAlimenti.put(giornoCorrente, pastiAlimenti); // La aggiunge.
        }
        if (pastiRicette == null) { // Controllo per assicurare che la mappa delle ricette esista.
            pastiRicette = creaMappaPastiRicetteVuota(); // Crea la mappa se non esiste.
            giorniPastiRicette.put(giornoCorrente, pastiRicette); // La aggiunge.
        }


        colazioneListView.setItems(FXCollections.observableArrayList()); // Resetta la ListView della colazione.
        spuntinoMattinaListView.setItems(FXCollections.observableArrayList()); // Resetta la ListView dello spuntino del mattino.
        pranzoListView.setItems(FXCollections.observableArrayList()); // Resetta la ListView del pranzo.
        spuntinoPomeriggioListView.setItems(FXCollections.observableArrayList()); // Resetta la ListView dello spuntino del pomeriggio.
        cenaListView.setItems(FXCollections.observableArrayList()); // Resetta la ListView della cena.

        // Aggiungi alimenti
        for (AlimentoQuantificato a : pastiAlimenti.get("colazione"))
            colazioneListView.getItems().add(a.toString()); // Aggiunge gli alimenti della colazione.
        for (AlimentoQuantificato a : pastiAlimenti.get("spuntinoMattina"))
            spuntinoMattinaListView.getItems().add(a.toString()); // Aggiunge gli alimenti dello spuntino mattino.
        for (AlimentoQuantificato a : pastiAlimenti.get("pranzo"))
            pranzoListView.getItems().add(a.toString()); // Aggiunge gli alimenti del pranzo.
        for (AlimentoQuantificato a : pastiAlimenti.get("spuntinoPomeriggio"))
            spuntinoPomeriggioListView.getItems().add(a.toString()); // Aggiunge gli alimenti dello spuntino pomeriggio.
        for (AlimentoQuantificato a : pastiAlimenti.get("cena"))
            cenaListView.getItems().add(a.toString()); // Aggiunge gli alimenti della cena.

        // AGGIUNTO: Aggiungi ricette alle rispettive liste
        for (RicettaQuantificata r : pastiRicette.get("colazione"))
            colazioneListView.getItems().add(r.toString()); // Aggiunge le ricette della colazione.
        for (RicettaQuantificata r : pastiRicette.get("spuntinoMattina"))
            spuntinoMattinaListView.getItems().add(r.toString()); // Aggiunge le ricette dello spuntino mattino.
        for (RicettaQuantificata r : pastiRicette.get("pranzo"))
            pranzoListView.getItems().add(r.toString()); // Aggiunge le ricette del pranzo.
        for (RicettaQuantificata r : pastiRicette.get("spuntinoPomeriggio"))
            spuntinoPomeriggioListView.getItems().add(r.toString()); // Aggiunge le ricette dello spuntino pomeriggio.
        for (RicettaQuantificata r : pastiRicette.get("cena"))
            cenaListView.getItems().add(r.toString()); // Aggiunge le ricette della cena.
    }

    private void aggiornaTotali() { // Metodo privato per aggiornare i totali nutrizionali.
        double kcalTotali = 0, carboidratiTotali = 0, proteineTotali = 0, grassiTotali = 0; // Inizializza i totali nutrizionali.
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoCorrente); // Ottiene la mappa dei pasti per gli alimenti.
        Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoCorrente); // AGGIUNTO: Ottiene la mappa dei pasti per le ricette.

        // Calcola totali dagli alimenti
        if (pastiAlimenti != null) { // Controllo per assicurare che la mappa degli alimenti esista.
            for (ObservableList<AlimentoQuantificato> listaAlimenti : pastiAlimenti.values()) { // Itera sulle liste di alimenti di ogni pasto.
                for (AlimentoQuantificato aq : listaAlimenti) { // Itera su ogni alimento quantificato.
                    Alimento alimento = aq.getAlimento(); // Ottiene l'oggetto Alimento.
                    int quantita = aq.getQuantita(); // Ottiene la quantità.
                    kcalTotali += (alimento.getKcal() / 100.0) * quantita; // Calcola e aggiunge le calorie.
                    carboidratiTotali += (alimento.getCarboidrati() / 100.0) * quantita; // Calcola e aggiunge i carboidrati.
                    proteineTotali += (alimento.getProteine() / 100.0) * quantita; // Calcola e aggiunge le proteine.
                    grassiTotali += (alimento.getGrassi() / 100.0) * quantita; // Calcola e aggiunge i grassi.
                }
            }
        }

        // AGGIUNTO: Calcola totali dalle ricette
        if (pastiRicette != null) { // Controllo per assicurare che la mappa delle ricette esista.
            for (ObservableList<RicettaQuantificata> listaRicette : pastiRicette.values()) { // Itera sulle liste di ricette di ogni pasto.
                for (RicettaQuantificata rq : listaRicette) { // Itera su ogni ricetta quantificata.
                    Ricetta ricetta = rq.getRicetta(); // Ottiene l'oggetto Ricetta.
                    int quantita = rq.getQuantita(); // Ottiene la quantità.
                    // Assumiamo che i valori nutrizionali delle ricette siano per 100g come gli alimenti
                    kcalTotali += (ricetta.getKcal() / 100.0) * quantita; // Calcola e aggiunge le calorie.
                    carboidratiTotali += (ricetta.getCarboidrati() / 100.0) * quantita; // Calcola e aggiunge i carboidrati.
                    proteineTotali += (ricetta.getProteine() / 100.0) * quantita; // Calcola e aggiunge le proteine.
                    grassiTotali += (ricetta.getGrassi() / 100.0) * quantita; // Calcola e aggiunge i grassi.
                }
            }
        }


        kcalTotaliLabel.setText(String.format("%.2f", kcalTotali)); // Imposta il testo della label delle calorie totali formattato.
        carboidratiLabel.setText(String.format("%.2f g", carboidratiTotali)); // Imposta il testo della label dei carboidrati totali formattato.
        proteineLabel.setText(String.format("%.2f g", proteineTotali)); // Imposta il testo della label delle proteine totali formattato.
        grassiLabel.setText(String.format("%.2f g", grassiTotali)); // Imposta il testo della label dei grassi totali formattato.
    }


    public void salvaPianoButtonAction(ActionEvent event) { // Metodo pubblico chiamato quando si clicca il bottone "Salva Piano".
        Connection conn = null; // Dichiarazione della variabile per la connessione al database, inizializzata a null.
        PreparedStatement psGetDietaId = null; // Dichiarazione dello statement preparato per ottenere l'ID della dieta, inizializzato a null.
        PreparedStatement psGetGiorni = null; // Dichiarazione dello statement preparato per ottenere i giorni della dieta, inizializzato a null.
        PreparedStatement psDeleteAlimenti = null; // Dichiarazione dello statement preparato per cancellare gli alimenti esistenti, inizializzato a null.
        PreparedStatement psInsertAlimenti = null; // Dichiarazione dello statement preparato per inserire nuovi alimenti, inizializzato a null.
        PreparedStatement psDeleteRicette = null; // AGGIUNTO: Dichiarazione dello statement preparato per cancellare le ricette esistenti, inizializzato a null.
        PreparedStatement psInsertRicette = null; // AGGIUNTO: Dichiarazione dello statement preparato per inserire nuove ricette, inizializzato a null.
        ResultSet rs = null; // Dichiarazione della variabile per il set di risultati delle query, inizializzato a null.

        salvaNomeGiornoCorrente(); // Chiama un metodo per salvare il nome del giorno corrente prima di procedere con il salvataggio principale nel database.

        try { // Inizio del blocco try per la gestione delle eccezioni SQL che possono derivare dalle operazioni sul database.
            conn = SQLiteConnessione.connector(); // Ottiene una connessione al database SQLite tramite il metodo statico 'connector()'.
            conn.setAutoCommit(false); // Disabilita l'autocommit per iniziare una transazione manuale, permettendo di annullare tutte le modifiche in caso di errore.

            System.out.println("titoloPiano = '" + titoloPiano + "'"); // Stampa a console il valore di 'titoloPiano' per scopi di debug.
            System.out.println("UserID = " + Session.getUserId()); // Stampa a console l'ID dell'utente loggato (recuperato dalla Sessione) per debug.
            // 1. Recupera l'ID della dieta (usando l'idDieta già impostato, altrimenti cerca)
            int currentIdDieta = this.idDieta; // Assegna il valore dell'idDieta corrente della classe a una variabile locale.
            if (currentIdDieta == 0) { // Controlla se l'ID della dieta non è ancora stato impostato (il valore iniziale è 0).
                String sqlGetDieta = "SELECT id FROM Diete WHERE nome_dieta = ? AND id_nutrizionista = ?"; // Definizione della query SQL per cercare l'ID della dieta in base al nome e all'ID del nutrizionista.
                psGetDietaId = conn.prepareStatement(sqlGetDieta); // Prepara lo statement SQL per l'esecuzione.
                psGetDietaId.setString(1, titoloPiano); // Imposta il primo parametro della query (nome_dieta) con il titolo del piano.
                psGetDietaId.setInt(2, Session.getUserId()); // Imposta il secondo parametro (id_nutrizionista) con l'ID dell'utente loggato.
                rs = psGetDietaId.executeQuery(); // Esegue la query e memorizza il risultato nel ResultSet.

                if (!rs.next()) { // Controlla se il ResultSet non contiene alcuna riga, indicando che la dieta non è stata trovata.
                    System.err.println("Dieta non trovata. Impossibile salvare il piano."); // Stampa un messaggio di errore sulla console degli errori.

                    showAlert(Alert.AlertType.ERROR, "Errore di Salvataggio", "Impossibile trovare la dieta. Assicurati che sia stata creata correttamente."); // Mostra un popup di avviso all'utente.

                    return; // Esce dal metodo, interrompendo il processo di salvataggio.
                }
                currentIdDieta = rs.getInt("id"); // Ottiene l'ID della dieta dalla colonna "id" del ResultSet.
                this.idDieta = currentIdDieta; // Aggiorna l'idDieta della classe con l'ID recuperato per future operazioni.
            }

            // 2. Recupera gli ID dei giorni
            String sqlGetGiorni = "SELECT id_giorno_dieta FROM Giorno_dieta WHERE id_dieta = ? ORDER BY id_giorno_dieta ASC"; // Query SQL per ottenere gli ID di tutti i giorni associati a questa dieta, ordinati in modo crescente.
            psGetGiorni = conn.prepareStatement(sqlGetGiorni); // Prepara lo statement SQL.
            psGetGiorni.setInt(1, currentIdDieta); // Imposta l'ID della dieta come parametro.
            rs = psGetGiorni.executeQuery(); // Esegue la query e ottiene il ResultSet.

            // Popola idGiornoDietaMap se non già popolata (per i giorni esistenti)
            if (idGiornoDietaMap.isEmpty()) { // Controlla se la mappa che memorizza gli ID dei giorni dieta è vuota (indicando che non è stata popolata in precedenza).
                int tempGiornoIndex = 1; // Inizializza un indice temporaneo per i giorni a 1.
                while (rs.next()) { // Itera su ogni riga del ResultSet dei giorni dieta.
                    idGiornoDietaMap.put(tempGiornoIndex++, rs.getInt("id_giorno_dieta")); // Mappa l'ID del giorno dieta con l'indice temporaneo (che viene poi incrementato).
                }
                rs.close(); // Chiude il ResultSet corrente per liberare le risorse.
                psGetGiorni.close(); // Chiude il PreparedStatement corrente.
                psGetGiorni = conn.prepareStatement(sqlGetGiorni); // Riapre e riprepara lo statement per i giorni (necessario perché il ResultSet precedente è stato chiuso).
                psGetGiorni.setInt(1, currentIdDieta); // Reimposta l'ID della dieta come parametro.
                rs = psGetGiorni.executeQuery(); // Riesegue la query per ripopolare il ResultSet.
            }


            int giornoIndex = 1; // Inizializza l'indice del giorno corrente per il ciclo di elaborazione a 1.
            while (rs.next() && giornoIndex <= numeroGiorni) { // Itera sui risultati del ResultSet finché ci sono righe e l'indice del giorno non supera il numero totale di giorni.
                int idGiornoDieta = rs.getInt("id_giorno_dieta"); // Ottiene l'ID del giorno dieta dalla riga corrente del ResultSet.
                // Aggiorna la mappa con l'ID del giorno dieta (utile per giorni nuovi o modificati)
                idGiornoDietaMap.put(giornoIndex, idGiornoDieta); // Assicura che la mappa contenga l'ID corretto per il giorno attuale.


                // UPDATE nome giorno
                String nomeGiorno = nomiGiorni.get(giornoIndex); // Ottiene il nome personalizzato del giorno dall'HashMap 'nomiGiorni'.
                if (nomeGiorno != null) { // Controlla se è stato impostato un nome per questo giorno.
                    String sqlUpdateNomeGiorno = "UPDATE Giorno_dieta SET nome_giorno = ? WHERE id_giorno_dieta = ?"; // Query SQL per aggiornare il nome del giorno nel database.
                    try (PreparedStatement psUpdateNomeGiorno = conn.prepareStatement(sqlUpdateNomeGiorno)) { // Prepara lo statement per l'aggiornamento.
                        psUpdateNomeGiorno.setString(1, nomeGiorno.isEmpty() ? null : nomeGiorno); // Imposta il nome del giorno; se è una stringa vuota, imposta null nel DB.
                        psUpdateNomeGiorno.setInt(2, idGiornoDieta); // Imposta l'ID del giorno dieta per la clausola WHERE.
                        psUpdateNomeGiorno.executeUpdate(); // Esegue l'aggiornamento nel database.
                    } // Il PreparedStatement viene automaticamente chiuso qui grazie al try-with-resources.
                }

                // --- GESTIONE ALIMENTI ---
                // 3a. Cancella gli alimenti esistenti per questo giorno
                String sqlDeleteAlimenti = "DELETE FROM DietaAlimenti WHERE id_giorno_dieta = ?"; // Query SQL per eliminare tutti gli alimenti associati a questo specifico giorno della dieta.
                psDeleteAlimenti = conn.prepareStatement(sqlDeleteAlimenti); // Prepara lo statement per la cancellazione.
                psDeleteAlimenti.setInt(1, idGiornoDieta); // Imposta l'ID del giorno dieta come parametro per la cancellazione.
                psDeleteAlimenti.executeUpdate(); // Esegue la query di cancellazione.

                // 3b. Inserisci i nuovi alimenti per questo giorno
                String sqlInsertAlimenti = "INSERT INTO DietaAlimenti (id_giorno_dieta,id_alimento, quantita_grammi, pasto) VALUES (?, ?, ?, ?)"; // Query SQL per inserire nuovi alimenti nella tabella DietaAlimenti.
                psInsertAlimenti = conn.prepareStatement(sqlInsertAlimenti); // Prepara lo statement per l'inserimento.

                Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoIndex); // Recupera la mappa dei pasti con gli alimenti quantificati per il giorno corrente dal campo 'giorniPastiAlimenti'.
                if (pastiAlimenti == null) { // Se per qualche motivo la mappa dei pasti per questo giorno è nulla.
                    pastiAlimenti = creaMappaPastiAlimentiVuota(); // Crea una nuova mappa vuota per i pasti degli alimenti.
                    giorniPastiAlimenti.put(giornoIndex, pastiAlimenti); // Aggiunge la mappa appena creata alla collezione principale 'giorniPastiAlimenti'.
                }

                for (Map.Entry<String, ObservableList<AlimentoQuantificato>> entry : pastiAlimenti.entrySet()) { // Itera su ogni entry della mappa dei pasti (es. "colazione", "pranzo").
                    String pasto = entry.getKey(); // Ottiene il nome del pasto (la chiave dell'entry).
                    ObservableList<AlimentoQuantificato> lista = entry.getValue(); // Ottiene la lista di AlimentoQuantificato per quel pasto.

                    for (AlimentoQuantificato aq : lista) { // Itera su ogni AlimentoQuantificato all'interno della lista del pasto.
                        psInsertAlimenti.setInt(1, idGiornoDieta); // Imposta l'ID del giorno dieta come primo parametro.
                        psInsertAlimenti.setInt(2, aq.getAlimento().getId()); // Imposta l'ID dell'alimento come secondo parametro.
                        psInsertAlimenti.setDouble(3, aq.getQuantita()); // Imposta la quantità dell'alimento come terzo parametro.
                        psInsertAlimenti.setString(4, pasto); // Imposta il nome del pasto come quarto parametro.
                        psInsertAlimenti.addBatch(); // Aggiunge questa operazione di inserimento al batch, per eseguire più inserimenti in modo efficiente.
                    }
                }
                psInsertAlimenti.executeBatch(); // Esegue tutte le operazioni di inserimento degli alimenti in batch.
                // --- FINE GESTIONE ALIMENTI ---


                // --- GESTIONE RICETTE ---
                // AGGIUNTO: 4a. Cancella le ricette esistenti per questo giorno
                String sqlDeleteRicette = "DELETE FROM DietaRicette WHERE id_giorno_dieta = ?"; // Query SQL per eliminare tutte le ricette associate a questo specifico giorno della dieta.
                psDeleteRicette = conn.prepareStatement(sqlDeleteRicette); // Prepara lo statement per la cancellazione delle ricette.
                psDeleteRicette.setInt(1, idGiornoDieta); // Imposta l'ID del giorno dieta come parametro.
                psDeleteRicette.executeUpdate(); // Esegue la query di cancellazione.

                // AGGIUNTO: 4b. Inserisci le nuove ricette per questo giorno
                // AGGIORNATO: Query include la colonna 'pasto'
                String sqlInsertRicette = "INSERT INTO DietaRicette (id_giorno_dieta, id_ricetta, quantita_grammi, pasto) VALUES (?, ?, ?, ?)"; // Query SQL per inserire nuove ricette nella tabella DietaRicette.
                psInsertRicette = conn.prepareStatement(sqlInsertRicette); // Prepara lo statement per l'inserimento delle ricette.

                Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoIndex); // Recupera la mappa dei pasti con le ricette quantificate per il giorno corrente dal campo 'giorniPastiRicette'.
                if (pastiRicette == null) { // Se la mappa dei pasti per questo giorno è nulla.
                    pastiRicette = creaMappaPastiRicetteVuota(); // Crea una nuova mappa vuota per i pasti delle ricette.
                    giorniPastiRicette.put(giornoIndex, pastiRicette); // La aggiunge alla collezione principale 'giorniPastiRicette'.
                }

                for (Map.Entry<String, ObservableList<RicettaQuantificata>> entry : pastiRicette.entrySet()) { // Itera su ogni entry della mappa dei pasti per le ricette.
                    String pastoRicetta = entry.getKey(); // Recupera il nome del pasto dalla chiave della mappa.
                    ObservableList<RicettaQuantificata> lista = entry.getValue(); // Ottiene la lista di RicettaQuantificata per quel pasto.

                    for (RicettaQuantificata rq : lista) { // Itera su ogni RicettaQuantificata all'interno della lista del pasto.
                        psInsertRicette.setInt(1, idGiornoDieta); // Imposta l'ID del giorno dieta come primo parametro.
                        psInsertRicette.setInt(2, rq.getRicetta().getId()); // Imposta l'ID della ricetta come secondo parametro.
                        psInsertRicette.setDouble(3, rq.getQuantita()); // Imposta la quantità della ricetta come terzo parametro.
                        psInsertRicette.setString(4, pastoRicetta); // Imposta il nome del pasto come quarto parametro.
                        psInsertRicette.addBatch(); // Aggiunge questa operazione di inserimento al batch.
                    }
                }
                psInsertRicette.executeBatch(); // Esegue tutte le operazioni di inserimento delle ricette in batch.
                // --- FINE GESTIONE RICETTE ---


                // Aggiorna i totali nutrizionali per il giorno corrente nel DB
                // AGGIUNTO: Passa entrambe le mappe per il calcolo completo
                aggiornaTotaliDBPerGiorno(conn, idGiornoDieta, giorniPastiAlimenti.get(giornoIndex), giorniPastiRicette.get(giornoIndex)); // Chiama un metodo ausiliario per aggiornare i totali nutrizionali del giorno nel database.


                giornoIndex++; // Incrementa l'indice del giorno per passare all'elaborazione del giorno successivo.
            }

            conn.commit(); // Conferma la transazione, rendendo permanenti tutte le modifiche al database.
            System.out.println("Piano salvato correttamente."); // Stampa un messaggio di successo sulla console.
            showAlert(Alert.AlertType.INFORMATION, "Salvataggio Completato", "Dieta salvata con successo!"); // Mostra un alert di informazione all'utente che il salvataggio è completato.

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene il riferimento allo Stage (finestra) da cui è stato generato l'evento.
            stage.close(); // Chiude la finestra corrente.

        } catch (
                SQLException e) { // Cattura qualsiasi eccezione di tipo SQLException che può verificarsi nel blocco try.
            e.printStackTrace(); // Stampa lo stack trace dell'eccezione, utile per il debugging.
            try { // Inizia un blocco try-catch interno per tentare il rollback della transazione.
                if (conn != null)
                    conn.rollback(); // Se la connessione al database non è nulla, esegue il rollback per annullare le modifiche.
            } catch (SQLException ex) { // Cattura eventuali eccezioni che si verificano durante il rollback.
                ex.printStackTrace(); // Stampa lo stack trace di eventuali errori durante il rollback.
            }
            // Mostra un alert all'utente in caso di errore generale di salvataggio
            Platform.runLater(() -> { // Esegue il seguente codice sul thread dell'interfaccia utente (necessario per aggiornare la UI).
                showAlert(Alert.AlertType.ERROR, "Errore di Salvataggio", "Dettagli: " + e.getMessage()); // Mostra un alert di errore all'utente con i dettagli dell'eccezione SQL.

            });
        } finally { // Blocco finally, che viene sempre eseguito, indipendentemente dal fatto che si sia verificata un'eccezione o meno.
            try { // Inizia un blocco try-catch per chiudere tutte le risorse del database in modo sicuro.
                if (rs != null) rs.close(); // Chiude il ResultSet se non è nullo, rilasciando le risorse.
                if (psGetDietaId != null) psGetDietaId.close(); // Chiude lo statement psGetDietaId se non è nullo.
                if (psGetGiorni != null) psGetGiorni.close(); // Chiude lo statement psGetGiorni se non è nullo.
                if (psDeleteAlimenti != null)
                    psDeleteAlimenti.close(); // Chiude lo statement psDeleteAlimenti se non è nullo.
                if (psInsertAlimenti != null)
                    psInsertAlimenti.close(); // Chiude lo statement psInsertAlimenti se non è nullo.
                if (psDeleteRicette != null)
                    psDeleteRicette.close(); // AGGIUNTO: Chiude lo statement psDeleteRicette se non è nullo.
                if (psInsertRicette != null)
                    psInsertRicette.close(); // AGGIUNTO: Chiude lo statement psInsertRicette se non è nullo.
                if (conn != null) conn.close(); // Chiude la connessione al database se non è nulla.
            } catch (
                    SQLException e) { // Cattura eventuali eccezioni che si verificano durante la chiusura delle risorse.
                e.printStackTrace(); // Stampa lo stack trace dell'errore di chiusura delle risorse.
            }
        }
    }


    @FXML
    private void rimuoviAlimento(ActionEvent event) { // Metodo FXML per rimuovere un alimento o una ricetta da una ListView.
        // Ottieni il bottone che ha generato l'evento
        String buttonId = ((Node) event.getSource()).getId(); // Ottiene l'ID del bottone cliccato.

        // Mappa per associare il buttonId al nome del pasto e alla ListView corrispondente
        String pasto = null; // Variabile per memorizzare il nome del pasto.
        ListView<String> listView = null; // Variabile per memorizzare la ListView associata.

        switch (buttonId) { // Switch per determinare il pasto e la ListView in base all'ID del bottone.
            case "rimuoviColazioneButton": // Se il bottone è per rimuovere dalla colazione.
                pasto = "colazione"; // Imposta il pasto a "colazione".
                listView = colazioneListView; // Assegna la ListView della colazione.
                break; // Esce dallo switch.
            case "rimuoviSpuntinoMattinaButton": // Se il bottone è per rimuovere dallo spuntino del mattino.
                pasto = "spuntinoMattina"; // Imposta il pasto a "spuntinoMattina".
                listView = spuntinoMattinaListView; // Assegna la ListView dello spuntino del mattino.
                break; // Esce dallo switch.
            case "rimuoviPranzoButton": // Se il bottone è per rimuovere dal pranzo.
                pasto = "pranzo"; // Imposta il pasto a "pranzo".
                listView = pranzoListView; // Assegna la ListView del pranzo.
                break; // Esce dallo switch.
            case "rimuoviSpuntinoPomeriggioButton": // Se il bottone è per rimuovere dallo spuntino del pomeriggio.
                pasto = "spuntinoPomeriggio"; // Imposta il pasto a "spuntinoPomeriggio".
                listView = spuntinoPomeriggioListView; // Assegna la ListView dello spuntino del pomeriggio.
                break; // Esce dallo switch.
            case "rimuoviCenaButton": // Se il bottone è per rimuovere dalla cena.
                pasto = "cena"; // Imposta il pasto a "cena".
                listView = cenaListView; // Assegna la ListView della cena.
                break; // Esce dallo switch.
        }

        if (pasto == null || listView == null) { // Controlla se il pasto o la ListView non sono stati riconosciuti.
            System.out.println("Errore: bottone non riconosciuto."); // Stampa un messaggio di errore.
            return; // Esce dal metodo.
        }

        String selectedString = listView.getSelectionModel().getSelectedItem(); // Ottiene l'elemento selezionato nella ListView.

        if (selectedString == null) { // Controlla se nessun elemento è stato selezionato.
            System.out.println("Seleziona un alimento o una ricetta da rimuovere!"); // Stampa un messaggio all'utente.
            return; // Esce dal metodo.
        }

        // Prova a rimuovere come Alimento
        Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = giorniPastiAlimenti.get(giornoCorrente); // Ottiene la mappa dei pasti con gli alimenti per il giorno corrente.
        AlimentoQuantificato daRimuovereAlimento = null; // Variabile per memorizzare l'alimento quantificato da rimuovere.
        if (pastiAlimenti != null) { // Aggiunto controllo null: verifica che la mappa dei pasti alimenti non sia nulla.
            ObservableList<AlimentoQuantificato> listaPasto = pastiAlimenti.get(pasto); // Ottiene la lista di alimenti per il pasto selezionato.
            if (listaPasto != null) { // Aggiunto controllo null: verifica che la lista del pasto non sia nulla.
                for (AlimentoQuantificato aq : listaPasto) { // Itera su ogni alimento quantificato nella lista del pasto.
                    if (aq.toString().equals(selectedString)) { // Controlla se la rappresentazione stringa dell'alimento corrisponde a quella selezionata.
                        daRimuovereAlimento = aq; // Se corrisponde, assegna l'alimento a 'daRimuovereAlimento'.
                        break; // Esce dal ciclo for.
                    }
                }
            }
        }

        if (daRimuovereAlimento != null) { // Se un alimento da rimuovere è stato trovato.
            pastiAlimenti.get(pasto).remove(daRimuovereAlimento); // Rimuove l'alimento dalla lista in memoria.
            // Ora, chiama un metodo che gestisce la rimozione dal DB per entrambi i tipi
            // La rimozione dal DB avverrà solo al Salva Piano, qui rimuovi solo dalla memoria
            eliminaItemDalDB(giornoCorrente, daRimuovereAlimento, null); // Chiama il metodo per "segnare" l'elemento per la rimozione dal DB (non lo rimuove immediatamente).
        } else { // Se non è stato trovato un alimento corrispondente.
            // Se non è un alimento, prova a rimuovere come Ricetta
            Map<String, ObservableList<RicettaQuantificata>> pastiRicette = giorniPastiRicette.get(giornoCorrente); // Ottiene la mappa dei pasti con le ricette per il giorno corrente.
            RicettaQuantificata daRimuovereRicetta = null; // Variabile per memorizzare la ricetta quantificata da rimuovere.
            if (pastiRicette != null) { // Aggiunto controllo null: verifica che la mappa dei pasti ricette non sia nulla.
                ObservableList<RicettaQuantificata> listaPasto = pastiRicette.get(pasto); // Ottiene la lista di ricette per il pasto selezionato.
                if (listaPasto != null) { // Aggiunto controllo null: verifica che la lista del pasto non sia nulla.
                    for (RicettaQuantificata rq : listaPasto) { // Itera su ogni ricetta quantificata nella lista del pasto.
                        if (rq.toString().equals(selectedString)) { // Controlla se la rappresentazione stringa della ricetta corrisponde a quella selezionata.
                            daRimuovereRicetta = rq; // Se corrisponde, assegna la ricetta a 'daRimuovereRicetta'.
                            break; // Esce dal ciclo for.
                        }
                    }
                }
            }

            if (daRimuovereRicetta != null) { // Se una ricetta da rimuovere è stata trovata.
                pastiRicette.get(pasto).remove(daRimuovereRicetta); // Rimuove la ricetta dalla lista in memoria.
                // Chiama lo stesso metodo di eliminazione, passando la ricetta e null per l'alimento
                // La rimozione dal DB avverrà solo al Salva Piano, qui rimuovi solo dalla memoria
                eliminaItemDalDB(giornoCorrente, null, daRimuovereRicetta); // Chiama il metodo per "segnare" l'elemento per la rimozione dal DB.
            } else { // Se né un alimento né una ricetta corrispondente sono stati trovati.
                System.out.println("Errore: elemento non trovato nella lista interna. Nessun alimento o ricetta selezionata corrisponde."); // Stampa un messaggio di errore.
                return; // Nessun elemento rimosso, esce dal metodo.
            }
        }

        // Aggiorna la ListView e i totali dopo la rimozione
        aggiornaListView(); // Aggiorna le ListView per mostrare la rimozione.

        // Aggiorna i totali nutrizionali in UI
        aggiornaTotali(); // Aggiorna i totali nutrizionali visualizzati nell'interfaccia utente.

        // Rimuovi l'alimento dal database
        // eliminaAlimentoDalDB(giornoCorrente, daRimuovere); // Rimosso: la logica DB avviene solo al Salva Piano // Questo commento indica che la rimozione dal DB non avviene qui.
    }


    // Nuovo metodo generalizzato per eliminare sia alimenti che ricette dal DB (la rimozione dal DB avverrà solo al 'Salva Piano')
    private void eliminaItemDalDB(int giorno, AlimentoQuantificato aq, RicettaQuantificata rq) { // Metodo privato per "gestire" la rimozione di un elemento (alimento o ricetta).
        // NOTA: In questa implementazione, l'eliminazione dal DB non avviene qui.
        // Avverrà in blocco nel metodo salvaPianoButtonAction() tramite DELETE e re-INSERT.
        // Questo metodo serve solo a notificare che un elemento è stato "segnato" per la rimozione,
        // o per debug, ma l'azione sul DB è ritardata.
        System.out.println("Elemento rimosso dalla memoria per il giorno " + giorno + ". Salvataggio nel DB al click su 'Salva Piano'."); // Stampa un messaggio informativo.

        // Qui ho rimosso tutta la logica di connessione e PreparedStatement per eliminare direttamente dal DB
        // come richiesto, dato che il salvataggio è centralizzato su "Salva Piano".
        // Se in futuro volessi una rimozione immediata, la logica andrebbe reinserita qui.
    }


    // Metodo di supporto per aggiornare i totali nel DB per un giorno specifico
    private void aggiornaTotaliDBPerGiorno(Connection conn, int idGiornoDieta, // Metodo privato per aggiornare i totali nutrizionali di un giorno specifico nel database.
                                           Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti, // AGGIUNTO: Mappa dei pasti con alimenti quantificati.
                                           Map<String, ObservableList<RicettaQuantificata>> pastiRicette) throws SQLException { // AGGIUNTO: Mappa dei pasti con ricette quantificate, lancia SQLException.
        double kcalTotali = 0, carboidratiTotali = 0, proteineTotali = 0, grassiTotali = 0; // Inizializza i totali nutrizionali.

        // Calcola da alimenti
        if (pastiAlimenti != null) { // Controlla se la mappa degli alimenti è valida.
            for (ObservableList<AlimentoQuantificato> listaAlimenti : pastiAlimenti.values()) { // Itera sulle liste di alimenti di ogni pasto.
                for (AlimentoQuantificato aq : listaAlimenti) { // Itera su ogni alimento quantificato.
                    Alimento alimento = aq.getAlimento(); // Ottiene l'oggetto Alimento.
                    double quantita = aq.getQuantita(); // Ottiene la quantità.
                    kcalTotali += (alimento.getKcal() / 100.0) * quantita; // Calcola e aggiunge le calorie.
                    carboidratiTotali += (alimento.getCarboidrati() / 100.0) * quantita; // Calcola e aggiunge i carboidrati.
                    proteineTotali += (alimento.getProteine() / 100.0) * quantita; // Calcola e aggiunge le proteine.
                    grassiTotali += (alimento.getGrassi() / 100.0) * quantita; // Calcola e aggiunge i grassi.
                }
            }
        }

        // AGGIUNTO: Calcola da ricette
        if (pastiRicette != null) { // Controlla se la mappa delle ricette è valida.
            for (ObservableList<RicettaQuantificata> listaRicette : pastiRicette.values()) { // Itera sulle liste di ricette di ogni pasto.
                for (RicettaQuantificata rq : listaRicette) { // Itera su ogni ricetta quantificata.
                    Ricetta ricetta = rq.getRicetta(); // Ottiene l'oggetto Ricetta.
                    double quantita = rq.getQuantita(); // Ottiene la quantità.
                    // Assumiamo che i valori nutrizionali delle ricette siano per 100g come gli alimenti
                    kcalTotali += (ricetta.getKcal() / 100.0) * quantita; // Calcola e aggiunge le calorie.
                    carboidratiTotali += (ricetta.getCarboidrati() / 100.0) * quantita; // Calcola e aggiunge i carboidrati.
                    proteineTotali += (ricetta.getProteine() / 100.0) * quantita; // Calcola e aggiunge le proteine.
                    grassiTotali += (ricetta.getGrassi() / 100.0) * quantita; // Calcola e aggiunge i grassi.
                }
            }
        }


        String sqlUpdate = "UPDATE Giorno_dieta SET calorie_giorno = ?, proteine_giorno = ?, carboidrati_giorno = ?, grassi_giorno = ? WHERE id_giorno_dieta = ?"; // Query SQL per aggiornare i totali nutrizionali nel database.
        try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) { // Prepara lo statement.
            psUpdate.setDouble(1, kcalTotali); // Imposta le calorie totali.
            psUpdate.setDouble(2, proteineTotali); // Imposta le proteine totali.
            psUpdate.setDouble(3, carboidratiTotali); // Imposta i carboidrati totali.
            psUpdate.setDouble(4, grassiTotali); // Imposta i grassi totali.
            psUpdate.setInt(5, idGiornoDieta); // Imposta l'ID del giorno dieta.
            psUpdate.executeUpdate(); // Esegue l'aggiornamento.
        } // Il PreparedStatement viene automaticamente chiuso.
    }


    @FXML
    private void avantiGiornoButtonAction(ActionEvent event) { // Metodo FXML per avanzare al giorno successivo nel piano dieta.

        salvaNomeGiornoCorrente(); // Salva il nome del giorno corrente prima di cambiare giorno.

        if (giornoCorrente < numeroGiorni) { // Controlla se non si è ancora all'ultimo giorno.
            giornoCorrente++; // Incrementa il giorno corrente.
            // Assicurati che le mappe per il nuovo giorno esistano
            if (!giorniPastiAlimenti.containsKey(giornoCorrente)) { // Controlla se la mappa degli alimenti non contiene il giorno corrente.
                giorniPastiAlimenti.put(giornoCorrente, creaMappaPastiAlimentiVuota()); // Se non esiste, la crea e la aggiunge.
            }
            if (!giorniPastiRicette.containsKey(giornoCorrente)) { // AGGIUNTO: Controlla se la mappa delle ricette non contiene il giorno corrente.
                giorniPastiRicette.put(giornoCorrente, creaMappaPastiRicetteVuota()); // Se non esiste, la crea e la aggiunge.
            }
            aggiornaIndicatoreGiorno(); // Aggiorna la label del giorno corrente.
            aggiornaListView(); // Aggiorna le ListView dei pasti.
            aggiornaTotali(); // Aggiorna i totali nutrizionali.
        } else { // Se si è già all'ultimo giorno.
            System.out.println("Ultimo giorno raggiunto."); // Stampa un messaggio sulla console.
        }
    }

    @FXML
    private void indietroGiornoButtonAction(ActionEvent event) { // Metodo FXML per tornare al giorno precedente nel piano dieta.

        salvaNomeGiornoCorrente(); // Salva il nome del giorno corrente prima di cambiare giorno.

        if (giornoCorrente > 1) { // Controlla se non si è già al primo giorno.
            giornoCorrente--; // Decrementa il giorno corrente.
            aggiornaIndicatoreGiorno(); // Aggiorna la label del giorno corrente.
            aggiornaListView(); // Aggiorna le ListView dei pasti.
            aggiornaTotali(); // Aggiorna i totali nutrizionali.
        } else { // Se si è già al primo giorno.
            System.out.println("Primo giorno raggiunto."); // Stampa un messaggio sulla console.
        }
    }

    private void salvaNomeGiornoCorrente() { // Metodo privato per salvare il nome del giorno corrente.
        String nomeInserito = nomeGiornoTextField.getText(); // Ottiene il testo dal campo nome giorno.
        if (nomeInserito != null && !nomeInserito.trim().isEmpty()) { // Controlla se il nome inserito non è nullo e non è vuoto dopo aver rimosso gli spazi.
            nomiGiorni.put(giornoCorrente, nomeInserito.trim()); // Salva il nome del giorno nella mappa 'nomiGiorni'.
        } else { // Se il nome inserito è nullo o vuoto.
            // Se l'utente cancella il nome, puoi scegliere di rimuoverlo dalla mappa
            // o lasciare che il database lo salvi come null/vuoto.
            // Per ora, lo rimuoviamo se è vuoto
            nomiGiorni.remove(giornoCorrente); // Rimuove l'entry corrispondente al giorno corrente dalla mappa.
        }
        System.out.println("Salvato in nomiGiorni per giorno " + giornoCorrente + ": " + nomiGiorni.get(giornoCorrente)); // Stampa a console per debug.
    }

    private Dieta dietaCorrente; // <-- Modificato // Variabile per memorizzare l'oggetto Dieta corrente da modificare.

    public void impostaDietaDaModificare(Dieta dieta) { // <-- Modificato // Metodo per impostare la dieta da modificare.
        this.dietaCorrente = dieta; // Assegna la dieta passata come parametro.
        // AGGIUNTO: Imposta l'ID della dieta e il titolo del piano
        this.idDieta = dieta.getId(); // Imposta l'ID della dieta.
        this.titoloPiano = dieta.getNome(); // Imposta il titolo del piano.
        this.numeroGiorni = dieta.getNumeroGiorni(); // Assicurati che Dieta abbia getNumeroGiorni() // Imposta il numero di giorni della dieta.

        caricaDatiDieta(); // Chiama il metodo per caricare i dati della dieta dal database.
    }

    private void caricaDatiDieta() { // Metodo privato per caricare i dati di una dieta esistente dal database.
        if (dietaCorrente == null) return; // Se la dieta corrente è nulla, esce dal metodo.

        int dietaId = dietaCorrente.getId(); // Ottiene l'ID della dieta corrente.

        Connection conn = null; // Variabile per la connessione al database.
        PreparedStatement psGiorni = null; // PreparedStatement per i giorni.
        PreparedStatement psAlimenti = null; // PreparedStatement per gli alimenti.
        PreparedStatement psRicette = null;  // AGGIUNTO: PreparedStatement per le ricette.
        ResultSet rsGiorni = null; // ResultSet per i giorni.
        ResultSet rsAlimenti = null; // ResultSet per gli alimenti.
        ResultSet rsRicette = null;  // AGGIUNTO: ResultSet per le ricette.

        try { // Inizia un blocco try-catch per la gestione delle eccezioni SQL.
            conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.

            // 1. Recupera i giorni associati alla dieta
            String sqlGiorni = "SELECT id_giorno_dieta, nome_giorno FROM Giorno_dieta WHERE id_dieta = ? ORDER BY id_giorno_dieta ASC"; // Query SQL per selezionare i giorni della dieta.
            psGiorni = conn.prepareStatement(sqlGiorni); // Prepara lo statement.
            psGiorni.setInt(1, dietaId); // Imposta l'ID della dieta.
            rsGiorni = psGiorni.executeQuery(); // Esegue la query.

            int giornoIndex = 1; // Inizializza l'indice del giorno.
            giorniPastiAlimenti.clear(); // Rinominato e pulito: Pulisce la mappa degli alimenti per tutti i giorni.
            giorniPastiRicette.clear(); // AGGIUNTO e pulito: Pulisce la mappa delle ricette per tutti i giorni.
            nomiGiorni.clear(); // Pulisce la mappa dei nomi dei giorni.
            idGiornoDietaMap.clear(); // Pulisce la mappa degli ID dei giorni dieta.

            while (rsGiorni.next()) { // Itera su ogni giorno della dieta.
                int idGiornoDieta = rsGiorni.getInt("id_giorno_dieta"); // Ottiene l'ID del giorno dieta.
                String nomeGiorno = rsGiorni.getString("nome_giorno"); // Ottiene il nome del giorno.

                nomiGiorni.put(giornoIndex, nomeGiorno); // Mappa il nome del giorno con l'indice.
                idGiornoDietaMap.put(giornoIndex, idGiornoDieta); // Mappa l'ID del giorno dieta con l'indice.

                // Crea le mappe vuote per questo giorno (rinominato)
                Map<String, ObservableList<AlimentoQuantificato>> pastiAlimenti = creaMappaPastiAlimentiVuota(); // Crea una mappa vuota per gli alimenti del giorno corrente.
                Map<String, ObservableList<RicettaQuantificata>> pastiRicette = creaMappaPastiRicetteVuota(); // AGGIUNTO: Crea una mappa vuota per le ricette del giorno corrente.

                // Associa le mappe 'pasti' al giornoIndex corrente
                giorniPastiAlimenti.put(giornoIndex, pastiAlimenti); // Rinominato: Aggiunge la mappa dei pasti alimenti al giorno corrente.
                giorniPastiRicette.put(giornoIndex, pastiRicette); // AGGIUNTO: Aggiunge la mappa dei pasti ricette al giorno corrente.

                // 2. Recupera gli alimenti associati a questo giorno, includendo la colonna pasto
                String sqlAlimenti = "SELECT da.id_alimento, da.quantita_grammi, da.pasto, a.nome, a.brand, a.kcal, a.proteine, a.carboidrati, a.grassi, a.grassiSaturi, a.sale, a.fibre, a.zuccheri, a.immaginePiccola, a.immagineGrande, a.user_id " +
                        "FROM DietaAlimenti da " +
                        "JOIN foods a ON da.id_alimento = a.id " +
                        "WHERE da.id_giorno_dieta = ?"; // Query SQL per recuperare gli alimenti del giorno con i loro dettagli.
                psAlimenti = conn.prepareStatement(sqlAlimenti); // Prepara lo statement.
                psAlimenti.setInt(1, idGiornoDieta); // Imposta l'ID del giorno dieta.
                rsAlimenti = psAlimenti.executeQuery(); // Esegue la query.

                while (rsAlimenti.next()) { // Itera su ogni alimento trovato.
                    // Crea l'oggetto Alimento completo (potrebbe mancare qualche campo se la tua classe Alimento ha più campi)
                    Alimento alimento = new Alimento( // Crea un nuovo oggetto Alimento.
                            rsAlimenti.getString("nome"), // Ottiene il nome.
                            rsAlimenti.getString("brand"), // Ottiene il brand.
                            rsAlimenti.getDouble("kcal"), // Ottiene le calorie.
                            rsAlimenti.getDouble("proteine"), // Ottiene le proteine.
                            rsAlimenti.getDouble("carboidrati"), // Ottiene i carboidrati.
                            rsAlimenti.getDouble("grassi"), // Ottiene i grassi.
                            rsAlimenti.getDouble("grassiSaturi"), // Ottiene i grassi saturi.
                            rsAlimenti.getDouble("sale"), // Ottiene il sale.
                            rsAlimenti.getDouble("fibre"), // Ottiene le fibre.
                            rsAlimenti.getDouble("zuccheri"), // Ottiene gli zuccheri.
                            rsAlimenti.getString("immaginePiccola"), // Ottiene l'immagine piccola.
                            rsAlimenti.getString("immagineGrande"), // Ottiene l'immagine grande.
                            rsAlimenti.getInt("user_id"), // Ottiene l'ID utente.
                            rsAlimenti.getInt("id_alimento") // ID dell'alimento.
                    );
                    int quantita = rsAlimenti.getInt("quantita_grammi"); // Ottiene la quantità.
                    String pasto = rsAlimenti.getString("pasto"); // Ottiene il pasto.

                    if (pasto == null || !pastiAlimenti.containsKey(pasto)) { // Se il pasto è nullo o non è una chiave valida.
                        pasto = "pranzo"; // default fallback: Imposta un pasto di default.
                    }
                    pastiAlimenti.get(pasto).add(new AlimentoQuantificato(alimento, quantita)); // Aggiunge l'alimento alla lista del pasto.
                }
                // Assicurati di chiudere sempre ResultSet e PreparedStatement
                if (rsAlimenti != null) rsAlimenti.close(); // Chiude il ResultSet degli alimenti.
                if (psAlimenti != null) psAlimenti.close(); // Chiude il PreparedStatement degli alimenti.


                // AGGIUNTO: 3. Recupera le ricette associate a questo giorno, includendo la colonna 'pasto'
                String sqlRicette = "SELECT dr.id_ricetta, dr.quantita_grammi, dr.pasto, r.nome, r.descrizione, r.categoria, r.id_utente, r.kcal, r.proteine, r.carboidrati, r.grassi, r.grassi_saturi, r.zuccheri, r.fibre, r.sale " +
                        "FROM DietaRicette dr " +
                        "JOIN Ricette r ON dr.id_ricetta = r.id " +
                        "WHERE dr.id_giorno_dieta = ?"; // Query SQL per recuperare le ricette del giorno con i loro dettagli.
                psRicette = conn.prepareStatement(sqlRicette); // Prepara lo statement.
                psRicette.setInt(1, idGiornoDieta); // Imposta l'ID del giorno dieta.
                rsRicette = psRicette.executeQuery(); // Esegue la query.

                while (rsRicette.next()) { // Itera su ogni ricetta trovata.
                    // Crea l'oggetto Ricetta completo
                    Ricetta ricetta = new Ricetta( // Crea un nuovo oggetto Ricetta.
                            rsRicette.getInt("id_ricetta"), // Ottiene l'ID della ricetta.
                            rsRicette.getString("nome"), // Ottiene il nome.
                            rsRicette.getString("descrizione"), // Ottiene la descrizione.
                            rsRicette.getString("categoria"), // Ottiene la categoria.
                            rsRicette.getInt("id_utente"), // Ottiene l'ID utente.
                            rsRicette.getDouble("kcal"), // Ottiene le calorie.
                            rsRicette.getDouble("proteine"), // Ottiene le proteine.
                            rsRicette.getDouble("carboidrati"), // Ottiene i carboidrati.
                            rsRicette.getDouble("grassi"), // Ottiene i grassi.
                            rsRicette.getDouble("grassi_saturi"), // Ottiene i grassi saturi.
                            rsRicette.getDouble("zuccheri"), // Ottiene gli zuccheri.
                            rsRicette.getDouble("fibre"), // Ottiene le fibre.
                            rsRicette.getDouble("sale") // Ottiene il sale.
                    );
                    int quantita = rsRicette.getInt("quantita_grammi"); // Ottiene la quantità.
                    String pastoRicetta = rsRicette.getString("pasto"); // Recupera il pasto dal DB.

                    if (pastoRicetta == null || !pastiRicette.containsKey(pastoRicetta)) { // Se il pasto è nullo o non è una chiave valida.
                        pastoRicetta = "pranzo"; // Fallback se il pasto non è specificato: Imposta un pasto di default.
                    }
                    pastiRicette.get(pastoRicetta).add(new RicettaQuantificata(ricetta, quantita)); // Aggiunge la ricetta alla lista del pasto.
                }
                // Assicurati di chiudere sempre ResultSet e PreparedStatement
                if (rsRicette != null) rsRicette.close(); // Chiude il ResultSet delle ricette.
                if (psRicette != null) psRicette.close(); // Chiude il PreparedStatement delle ricette.


                giornoIndex++; // Incrementa l'indice del giorno solo una volta dopo aver processato alimenti e ricette.
            }
            numeroGiorni = giornoIndex - 1; // Aggiorna il numero totale di giorni con il conteggio effettivo.
            aggiornaListView(); // Aggiorna le ListView dell'interfaccia utente.
            aggiornaTotali(); // Aggiorna i totali nutrizionali dell'interfaccia utente.
            aggiornaIndicatoreGiorno(); // Aggiorna la label del giorno corrente.


        } catch (SQLException e) { // Cattura le eccezioni SQL.
            e.printStackTrace(); // Stampa lo stack trace dell'errore.
        } finally { // Blocco finally per chiudere le risorse.
            try { // Inizia un blocco try-catch per la chiusura delle risorse.
                if (rsGiorni != null) rsGiorni.close(); // Chiude il ResultSet dei giorni.
                if (psGiorni != null) psGiorni.close(); // Chiude il PreparedStatement dei giorni.
                if (psAlimenti != null) psAlimenti.close(); // Chiude il PreparedStatement degli alimenti.
                if (rsAlimenti != null) rsAlimenti.close(); // Chiude il ResultSet degli alimenti.
                if (psRicette != null) psRicette.close();   // AGGIUNTO: Chiude il PreparedStatement delle ricette.
                if (rsRicette != null) rsRicette.close();   // AGGIUNTO: Chiude il ResultSet delle ricette.
                if (conn != null) conn.close(); // Chiude la connessione al database.
            } catch (SQLException e) { // Cattura eventuali eccezioni durante la chiusura.
                e.printStackTrace(); // Stampa lo stack trace dell'errore.
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) { // Metodo privato per mostrare un Alert all'utente.
        Alert alert = new Alert(alertType); // Crea una nuova istanza di Alert con il tipo specificato.
        alert.setTitle(title); // Imposta il titolo dell'alert.
        alert.setHeaderText(null); // Imposta l'header text a null (non visualizzato).
        alert.setContentText(message); // Imposta il messaggio di contenuto dell'alert.
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css"); // Ottiene l'URL del file CSS per lo stile dell'alert.
        if (cssUrl != null) { // Se l'URL del CSS non è nullo.
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // Aggiunge il CSS al dialog pane dell'alert.
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base "dialog-pane".
            // Add specific style class based on AlertType for custom styling
            if (alertType == Alert.AlertType.INFORMATION) { // Se il tipo di alert è INFORMATION.
                alert.getDialogPane().getStyleClass().add("alert-information"); // Aggiunge la classe di stile specifica.
            } else if (alertType == Alert.AlertType.WARNING) { // Se il tipo di alert è WARNING.
                alert.getDialogPane().getStyleClass().add("alert-warning"); // Aggiunge la classe di stile specifica.
            } else if (alertType == Alert.AlertType.ERROR) { // Se il tipo di alert è ERROR.
                alert.getDialogPane().getStyleClass().add("alert-error"); // Aggiunge la classe di stile specifica.
            } else if (alertType == Alert.AlertType.CONFIRMATION) { // Se il tipo di alert è CONFIRMATION.
                alert.getDialogPane().getStyleClass().add("alert-confirmation"); // Aggiunge la classe di stile specifica.
            }
        } else { // Se l'URL del CSS è nullo (file non trovato).
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Stampa un messaggio di errore sulla console degli errori.
        }

        alert.showAndWait(); // Mostra l'alert e attende che l'utente lo chiuda.
    }
}