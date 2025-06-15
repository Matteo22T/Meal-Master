package com.matteotocci.app.controller; // Dichiara il package della classe.

import javafx.collections.FXCollections; // Importa le utility per creare ObservableList.
import javafx.collections.ObservableList; // Importa l'interfaccia ObservableList.
import javafx.event.ActionEvent; // Importa ActionEvent per la gestione degli eventi.
import javafx.fxml.FXML; // Importa l'annotazione FXML.
import javafx.fxml.FXMLLoader; // Importa FXMLLoader per caricare file FXML.
import javafx.fxml.Initializable; // Importa l'interfaccia Initializable per l'inizializzazione del controller.
import javafx.geometry.Rectangle2D; // Importa Rectangle2D per gestire le dimensioni dello schermo.
import javafx.scene.Node; // Importa Node, la classe base per gli elementi del grafo della scena.
import javafx.scene.Parent; // Importa Parent, la classe base per i nodi contenitori.
import javafx.scene.Scene; // Importa Scene per la gestione della scena.
import javafx.scene.control.*; // Importa tutti i controlli UI standard (Button, Label, ListView, TextField, TableView, TableColumn, etc.).
import javafx.scene.control.cell.PropertyValueFactory; // Importa PropertyValueFactory per collegare le colonne della TableView alle proprietà degli oggetti.
import javafx.scene.input.MouseEvent; // Importa MouseEvent per la gestione degli eventi del mouse.
import javafx.stage.Screen; // Importa Screen per ottenere informazioni sullo schermo.
import javafx.stage.Stage; // Importa Stage per la gestione delle finestre.
import javafx.util.Callback; // Importa Callback, un'interfaccia funzionale generica.
import javafx.scene.layout.HBox; // Importa HBox per layout orizzontali.

import java.io.IOException; // Importa IOException per la gestione degli errori I/O.
import java.net.URL; // Importa URL (necessario per Initializable).
import java.sql.Connection; // Importa Connection per la connessione al database.
import java.sql.DriverManager; // Mantenuto se usato da getNomeUtenteDalDatabase: Importa DriverManager per la connessione al database (potrebbe essere sostituito da SQLiteConnessione).
import java.sql.PreparedStatement; // Importa PreparedStatement per l'esecuzione di query SQL precompilate.
import java.sql.ResultSet; // Importa ResultSet per leggere i risultati delle query SQL.
import java.sql.SQLException; // Importa SQLException per gestire errori di database.
import java.util.ResourceBundle; // Importa ResourceBundle (necessario per Initializable).

import com.matteotocci.app.model.Dieta; // Importa la classe Dieta dal modello.
import com.matteotocci.app.model.SQLiteConnessione; // Importa la classe per la connessione al database SQLite.
import com.matteotocci.app.model.Session; // Importa la classe Session per gestire l'ID utente loggato.

public class HomePageNutrizionista implements Initializable { // Dichiara la classe HomePageNutrizionista e implementa Initializable.

    @FXML
    private Button BottoneDieta; // Bottone FXML per navigare alla sezione Diete.
    @FXML
    private Button BottoneAlimenti; // Bottone FXML per navigare alla sezione Alimenti.
    @FXML
    private Button BottoneRicette; // FXML ID per il bottone Ricette (generico, per i clienti).
    @FXML
    private Button BottoneRicetteNutrizionista; // Nuovo FXML ID se hai un bottone separato per le ricette del nutrizionista: Bottone FXML per navigare alla sezione Ricette del Nutrizionista.
    @FXML
    private Label nomeUtenteLabelHomePage; // Label FXML per visualizzare il nome dell'utente loggato.
    @FXML
    private TextField ricercaClienteTextField; // TextField FXML per la ricerca dei clienti.
    @FXML
    private TableView<Cliente> tabellaClienti; // TableView FXML per mostrare la lista dei clienti.
    @FXML
    private TableColumn<Cliente, String> nomeColonna; // Colonna della TableView per il nome del cliente.
    @FXML
    private TableColumn<Cliente, String> azioniColonna; // Colonna della TableView per le azioni (es. bottoni).

    private ObservableList<Cliente> listaClienti = FXCollections.observableArrayList(); // ObservableList per memorizzare i dati dei clienti.

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // Metodo di inizializzazione del controller, chiamato all'avvio.
        nomeColonna.setCellValueFactory(new PropertyValueFactory<>("nome")); // Imposta la cell factory per la colonna 'nome' per mostrare la proprietà 'nome' dell'oggetto Cliente.

        azioniColonna.setCellFactory(new Callback<TableColumn<Cliente, String>, TableCell<Cliente, String>>() { // Imposta una cell factory personalizzata per la colonna 'azioni'.
            @Override
            public TableCell<Cliente, String> call(TableColumn<Cliente, String> param) { // Metodo chiamato per creare ogni cella della colonna 'azioni'.
                return new TableCell<>() { // Restituisce una nuova istanza di TableCell.
                    final Button visualizzaButton = new Button("Visualizza Dieta"); // Crea un bottone "Visualizza Dieta".
                    final HBox container = new HBox(visualizzaButton); // Crea un HBox per contenere il bottone.
                    { // Blocco di inizializzazione della cella.
                        container.setSpacing(5); // Imposta la spaziatura tra gli elementi nell'HBox.

                        visualizzaButton.getStyleClass().add("visualizza-dieta-button"); // Aggiunge una classe di stile CSS al bottone.
                        container.getStyleClass().add("visualizza-dieta-container"); // Aggiunge una classe di stile CSS al contenitore.

                        visualizzaButton.setOnAction(event -> { // Imposta l'azione per il click del bottone "Visualizza Dieta".
                            Cliente cliente = getTableView().getItems().get(getIndex()); // Ottiene l'oggetto Cliente dalla riga corrente della tabella.
                            System.out.println("DEBUG (HomePageNutrizionista): Click su Visualizza Dieta per cliente: " + cliente.getNome() + " (ID: " + cliente.getId() + ")"); // Stampa un messaggio di debug.

                            Dieta dietaAssegnata = recuperaDietaAssegnataACliente(cliente.getId()); // Recupera la dieta assegnata a questo cliente dal database.

                            if (dietaAssegnata != null) { // Se è stata trovata una dieta assegnata.
                                try { // Inizia un blocco try-catch per la gestione degli errori durante il caricamento della nuova finestra.
                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml")); // Crea un FXMLLoader per caricare il file FXML della vista "VisualizzaDieta".
                                    Parent visualizzaDietaRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.

                                    VisualizzaDieta visualizzaDietaController = fxmlLoader.getController(); // Ottiene il controller della nuova finestra.
                                    visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata); // Imposta la dieta da visualizzare nel controller della nuova finestra.
                                    System.out.println("DEBUG (HomePageNutrizionista): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta."); // Stampa un messaggio di debug.

                                    Stage visualizzaDietaStage = new Stage(); // Crea un nuovo Stage (finestra).
                                    visualizzaDietaStage.setScene(new Scene(visualizzaDietaRoot)); // Imposta la scena per la nuova finestra.
                                    visualizzaDietaStage.setTitle("Dieta di " + cliente.getNome()); // Imposta il titolo della finestra.
                                    visualizzaDietaStage.show(); // Mostra la nuova finestra.

                                } catch (IOException e) { // Cattura l'eccezione IOException se il caricamento del file FXML fallisce.
                                    System.err.println("ERRORE (HomePageNutrizionista): Errore caricamento FXML VisualizzaDieta: " + e.getMessage()); // Stampa un messaggio di errore.
                                    e.printStackTrace(); // Stampa lo stack trace.
                                    showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta."); // Mostra un avviso di errore all'utente.
                                } catch (Exception e) { // Cattura qualsiasi altra eccezione generica.
                                    System.err.println("ERRORE (HomePageNutrizionista): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage()); // Stampa un messaggio di errore.
                                    e.printStackTrace(); // Stampa lo stack trace.
                                    showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso."); // Mostra un avviso di errore generico.
                                }
                            } else { // Se nessuna dieta è stata trovata per il cliente.
                                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente " + cliente.getNome() + " (ID: " + cliente.getId() + ")."); // Stampa un messaggio di debug.
                                showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta", "Nessuna dieta assegnata"); // Mostra un avviso all'utente.
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) { // Metodo chiamato per aggiornare il contenuto della cella.
                        super.updateItem(item, empty); // Chiama il metodo super.
                        if (empty) { // Se la cella è vuota.
                            setGraphic(null); // Rimuove il graphic.
                        } else { // Se la cella contiene un elemento.
                            setGraphic(container); // Imposta l'HBox come graphic della cella.
                        }
                    }
                };
            }
        });

        ricercaClienteTextField.textProperty().addListener((obs, oldVal, newVal) -> { // Aggiunge un listener al campo di testo di ricerca.
            filtraClienti(newVal); // Chiama il metodo per filtrare i clienti quando il testo cambia.
        });

        setNomeUtenteLabel(); // Chiama il metodo per impostare il nome dell'utente.
        caricaClientiDelNutrizionista(); // Chiama il metodo per caricare la lista dei clienti del nutrizionista.
    }

    private void setNomeUtenteLabel() { // Metodo privato per impostare il nome dell'utente loggato nella label.
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID dell'utente dalla Sessione.
        if (userIdFromSession != null) { // Se l'ID utente non è nullo.
            String nomeUtente = getNomeUtenteDalDatabase(userIdFromSession.toString()); // Recupera il nome dell'utente dal database.
            nomeUtenteLabelHomePage.setText( // Imposta il testo della label.
                    (nomeUtente != null && !nomeUtente.isEmpty()) ? nomeUtente : "Nome e Cognome" // Se il nome è valido, lo usa, altrimenti usa un fallback.
            );
        } else { // Se l'ID utente è nullo.
            nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Imposta il testo di fallback.
            System.err.println("[ERROR - HomePageNutrizionista] ID utente non disponibile dalla Sessione per impostare il nome."); // Stampa un messaggio di errore.
        }
    }

    private String getNomeUtenteDalDatabase(String userId) { // Metodo privato per recuperare il nome completo dell'utente dal database.
        String nomeUtente = null; // Variabile per il nome utente, inizializzata a null.
        String url = "jdbc:sqlite:database.db"; // Percorso del database SQLite.
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?"; // Query SQL per selezionare nome e cognome.

        // Utilizza un blocco try-with-resources per gestire la connessione e lo statement
        try (Connection conn = DriverManager.getConnection(url); // Ottiene una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement.
            pstmt.setString(1, userId); // Imposta l'ID utente come parametro della query.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query.

            if (rs.next()) { // Se c'è un risultato.
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena nome e cognome.
            }
        } catch (SQLException e) { // Cattura eccezioni SQL.
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage()); // Stampa messaggio di errore.
            e.printStackTrace(); // Stampa la traccia dell'errore.
        }
        return nomeUtente; // Restituisce il nome utente.
    }
    private Dieta recuperaDietaAssegnataACliente(int idCliente) { // Metodo privato per recuperare la dieta assegnata a un cliente specifico.
        Dieta dieta = null; // Variabile per l'oggetto Dieta, inizializzata a null.
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " + // Query SQL per selezionare i dettagli della dieta.
                "FROM Diete WHERE id_cliente = ?"; // Filtra per l'ID del cliente.

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement.
            pstmt.setInt(1, idCliente); // Imposta l'ID del cliente come parametro.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query.

            if (rs.next()) { // Se viene trovata una dieta.
                dieta = new Dieta( // Crea un nuovo oggetto Dieta.
                        rs.getInt("id"), // ID della dieta.
                        rs.getString("nome_dieta"), // Nome della dieta.
                        rs.getString("data_inizio"), // Data di inizio.
                        rs.getString("data_fine"), // Data di fine.
                        rs.getInt("id_nutrizionista"), // ID del nutrizionista.
                        rs.getInt("id_cliente") // ID del cliente.
                );
                System.out.println("DEBUG (HomePageNutrizionista): Recuperata dieta '" + dieta.getNome() + "' (ID: " + dieta.getId() + ") per cliente ID: " + idCliente); // Stampa messaggio di debug.
            } else { // Se nessuna dieta è stata trovata.
                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente ID: " + idCliente); // Stampa messaggio di debug.
            }
        } catch (SQLException e) { // Cattura eccezioni SQL.
            System.err.println("ERRORE SQL (HomePageNutrizionista): Errore durante il recupero della dieta per il cliente: " + e.getMessage()); // Stampa messaggio di errore.
            e.printStackTrace(); // Stampa stack trace.
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile recuperare la dieta."); // Mostra un avviso di errore.
        }
        return dieta; // Restituisce la dieta (o null).
    }

    private void caricaClientiDelNutrizionista() { // Metodo privato per caricare i clienti associati al nutrizionista loggato.
        listaClienti.clear(); // Pulisce la lista dei clienti.
        Integer currentNutrizionistaId = Session.getUserId(); // Ottiene l'ID del nutrizionista dalla Sessione.

        if (currentNutrizionistaId == null) { // Se l'ID del nutrizionista non è disponibile.
            System.err.println("[ERROR - HomePageNutrizionista] ID nutrizionista non disponibile dalla Sessione. Impossibile caricare i clienti."); // Stampa messaggio di errore.
            showAlert(Alert.AlertType.WARNING, "Utente non loggato", "ID Nutrizionista non disponibile"); // Mostra un avviso.
            return; // Esce dal metodo.
        }

        String query = "SELECT u.id, u.Nome, u.Cognome FROM Utente u " + // Query SQL per selezionare ID, Nome e Cognome degli utenti.
                "JOIN Clienti c ON u.id = c.id_cliente " + // Esegue un JOIN con la tabella Clienti.
                "WHERE c.id_nutrizionista = ?"; // Filtra per l'ID del nutrizionista.
        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement.
            pstmt.setInt(1, currentNutrizionistaId); // Imposta l'ID del nutrizionista come parametro.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query.
            while (rs.next()) { // Itera sui risultati.
                int clienteId = rs.getInt("id"); // Ottiene l'ID del cliente.
                String nome = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena nome e cognome.
                listaClienti.add(new Cliente(clienteId, nome)); // Aggiunge un nuovo oggetto Cliente alla lista.
            }
            tabellaClienti.setItems(listaClienti); // Imposta la lista dei clienti come elementi della TableView.
        } catch (SQLException e) { // Cattura eccezioni SQL.
            System.err.println("Errore DB (caricaClienti): " + e.getMessage()); // Stampa messaggio di errore.
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare i clienti."); // Mostra un avviso di errore.
        }
    }

    private void filtraClienti(String filtro) { // Metodo privato per filtrare la lista dei clienti in base a un testo.
        ObservableList<Cliente> filtrati = FXCollections.observableArrayList(); // Crea una nuova ObservableList per i clienti filtrati.
        if (filtro == null || filtro.isEmpty()) { // Se il filtro è nullo o vuoto.
            filtrati.addAll(listaClienti); // Aggiunge tutti i clienti non filtrati alla lista filtrata.
        } else { // Se il filtro non è vuoto.
            String lower = filtro.toLowerCase(); // Converte il filtro in minuscolo per una ricerca case-insensitive.
            for (Cliente c : listaClienti) { // Itera su tutti i clienti.
                if (c.getNome().toLowerCase().contains(lower)) { // Se il nome del cliente (in minuscolo) contiene il filtro.
                    filtrati.add(c); // Aggiunge il cliente alla lista filtrata.
                }
            }
        }
        tabellaClienti.setItems(filtrati); // Aggiorna la TableView con la lista dei clienti filtrati.
    }

    public static class Cliente { // Classe interna statica per rappresentare un cliente.
        private String nome; // Nome del cliente.
        private int id; // ID del cliente.

        public Cliente(int id, String nome) { // Costruttore della classe Cliente.
            this.id = id; // Inizializza l'ID.
            this.nome = nome; // Inizializza il nome.
        }

        public int getId() { // Metodo getter per l'ID.
            return id; // Restituisce l'ID.
        }

        public String getNome() { // Metodo getter per il nome.
            return nome; // Restituisce il nome.
        }

        public void setNome(String nome) { // Metodo setter per il nome.
            this.nome = nome; // Imposta il nome.
        }
    }

    // --- Metodi di Navigazione ---

    @FXML // Annotazione FXML per collegare questo metodo all'azione di un elemento FXML (es. onAction di BottoneDieta).
    private void AccessoDieta(ActionEvent event) { // Metodo per navigare alla vista DietaNutrizionista.
        try { // Inizia un blocco try-catch.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml")); // Carica l'FXML DietaNutrizionista.
            Parent dietaRoot = fxmlLoader.load(); // Ottiene il nodo radice.
            Stage dietaStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente (stage).
            boolean isMaximized = dietaStage.isMaximized(); // Controlla se la finestra è massimizzata.
            dietaStage.setScene(new Scene(dietaRoot)); // Imposta la nuova scena.
            dietaStage.setTitle("Diete Nutrizionista"); // Imposta il titolo della finestra.
            if (isMaximized){ // Se la finestra era massimizzata, la mantiene massimizzata.
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds(); // Ottiene le dimensioni dello schermo.
                dietaStage.setX(screenBounds.getMinX()); // Imposta la posizione X.
                dietaStage.setY(screenBounds.getMinY()); // Imposta la posizione Y.
                dietaStage.setWidth(screenBounds.getWidth()); // Imposta la larghezza.
                dietaStage.setHeight(screenBounds.getHeight()); // Imposta l'altezza.
            }
            dietaStage.show(); // Visualizza la nuova finestra.
        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Diete Nutrizionista'."); // Mostra un avviso di errore.
        }
    }

    @FXML // Annotazione FXML.
    private void AccessoAlimenti(ActionEvent event) { // Metodo per navigare alla vista AlimentiNutrizionista.
        try { // Inizia un blocco try-catch.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AlimentiNutrizionista.fxml")); // Carica l'FXML AlimentiNutrizionista.
            Parent alimentiRoot = fxmlLoader.load(); // Ottiene il nodo radice.
            Stage alimentiStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            boolean isMaximized = alimentiStage.isMaximized(); // Controlla se la finestra è massimizzata.
            alimentiStage.setScene(new Scene(alimentiRoot)); // Imposta la nuova scena.
            alimentiStage.setTitle("Alimenti"); // Imposta il titolo della finestra.
            if (isMaximized){ // Se la finestra era massimizzata, la mantiene massimizzata.
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds(); // Ottiene le dimensioni dello schermo.
                alimentiStage.setX(screenBounds.getMinX()); // Imposta la posizione X.
                alimentiStage.setY(screenBounds.getMinY()); // Imposta la posizione Y.
                alimentiStage.setWidth(screenBounds.getWidth()); // Imposta la larghezza.
                alimentiStage.setHeight(screenBounds.getHeight()); // Imposta l'altezza.
            }
            alimentiStage.show(); // Visualizza la nuova finestra.
        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Alimenti'."); // Mostra un avviso di errore.
        }
    }




    @FXML // Annotazione FXML.
    private void AccessoRicetteNutrizionista(ActionEvent event) { // Metodo per navigare alla vista RicetteNutrizionista.
        try { // Inizia un blocco try-catch.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/RicetteNutrizionista.fxml")); // Carica l'FXML RicetteNutrizionista.
            Parent ricetteNutrizionistaRoot = fxmlLoader.load(); // Ottiene il nodo radice.
            Stage ricetteNutrizionistaStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            boolean isMaximized = ricetteNutrizionistaStage.isMaximized(); // Controlla se la finestra è massimizzata.
            ricetteNutrizionistaStage.setScene(new Scene(ricetteNutrizionistaRoot)); // Imposta la nuova scena.
            ricetteNutrizionistaStage.setTitle("Le Mie Ricette (Nutrizionista)"); // Imposta il titolo della finestra.
            if (isMaximized){ // Se la finestra era massimizzata, la mantiene massimizzata.
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds(); // Ottiene le dimensioni dello schermo.
                ricetteNutrizionistaStage.setX(screenBounds.getMinX()); // Imposta la posizione X.
                ricetteNutrizionistaStage.setY(screenBounds.getMinY()); // Imposta la posizione Y.
                ricetteNutrizionistaStage.setWidth(screenBounds.getWidth()); // Imposta la larghezza.
                ricetteNutrizionistaStage.setHeight(screenBounds.getHeight()); // Imposta l'altezza.
            }
            ricetteNutrizionistaStage.show(); // Visualizza la nuova finestra.
        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Le Mie Ricette (Nutrizionista)'."); // Mostra un avviso di errore.
        }
    }


    @FXML // Annotazione FXML.
    private void openProfiloNutrizionista(MouseEvent event) { // Metodo per navigare alla vista ProfiloNutrizionista (attivato da un click del mouse).
        try { // Inizia un blocco try-catch.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ProfiloNutrizionista.fxml")); // Carica l'FXML ProfiloNutrizionista.
            Parent profileRoot = fxmlLoader.load(); // Ottiene il nodo radice.
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra corrente.
            boolean isMaximized = profileStage.isMaximized(); // Controlla se la finestra è massimizzata.
            profileStage.setScene(new Scene(profileRoot)); // Imposta la nuova scena.
            profileStage.setTitle("Profilo Nutrizionista"); // Imposta il titolo della finestra.
            if (isMaximized){ // Se la finestra era massimizzata, la mantiene massimizzata.
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds(); // Ottiene le dimensioni dello schermo.
                profileStage.setX(screenBounds.getMinX()); // Imposta la posizione X.
                profileStage.setY(screenBounds.getMinY()); // Imposta la posizione Y.
                profileStage.setWidth(screenBounds.getWidth()); // Imposta la larghezza.
                profileStage.setHeight(screenBounds.getHeight()); // Imposta l'altezza.
            }
            profileStage.show(); // Visualizza la nuova finestra.
        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Profilo Nutrizionista'."); // Mostra un avviso di errore.
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) { // Metodo privato per mostrare un Alert all'utente.
        Alert alert = new Alert(alertType); // Crea una nuova istanza di Alert.
        alert.setTitle(title); // Imposta il titolo.
        alert.setHeaderText(null); // Non mostra un header text.
        alert.setContentText(message); // Imposta il contenuto.

        // Cerca il file CSS per lo stile personalizzato degli alert
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css"); // Ottiene l'URL del file CSS per lo stile dell'alert.
        if (cssUrl != null) { // Se il CSS viene trovato.
            // Se il CSS viene trovato, lo aggiunge al DialogPane dell'alert
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // Aggiunge il CSS al DialogPane.
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base.
            // Aggiunge una classe di stile specifica in base al tipo di alert per una maggiore personalizzazione
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
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Messaggio di errore se il CSS non è trovato.
        }

        alert.showAndWait(); // Mostra l'avviso e attende che l'utente lo chiuda.
    }
}