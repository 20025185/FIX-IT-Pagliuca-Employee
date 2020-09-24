package GUI;

import GUI.dialogs.ChatBidirectional;
import GUI.panels.*;
import com.google.firebase.database.*;
import firebase.Employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.*;

@SuppressWarnings("JavaDoc")
public class ControlPanel extends JFrame implements KeyListener {
    //  I record del DB smistati nei vettori, per poi venire distribuiti nei panels rispettivi
    private final Vector<String> pendingReportIDs = new Vector<>();
    private final Vector<String> openReportIDs = new Vector<>();
    private final Vector<String> closedReportIDs = new Vector<>();

    //  Vettore contenente le istanze delle chat bidirezionali aperte.
    private final Vector<ChatBidirectional> chatInstances = new Vector<>();

    //  Il nodo riferimento da cui vengono effettuate la maggior parte delle query sul RTD.
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    //  I pannelli che vengono utilizzati all'interno dell'applicazione.
    private final static OpenReportsPanel openReportPanel = new OpenReportsPanel();
    private final static PendingReportsPanel pendingReportPanel = new PendingReportsPanel();
    private final static ClosedReportsPanel closedReportPanel = new ClosedReportsPanel();
    private final static StreamingStatsReviewsPanel streamingStatsReviewsPanel = new StreamingStatsReviewsPanel();
    private final static StatsReportsPanel statsReportsPanel = new StatsReportsPanel();
    private final static CreateReportPanel createReportPanel = new CreateReportPanel();

    //  Semaforo per la distribuzione dei record nei vari panels
    private final Semaphore semaphore = new Semaphore(0);

    //  Intero rappresentante il pannello mostrato nel CardLayout
    private int cardShowed;

    /***
     *  Il costruttore di questa classe costituisce il tronco centrale dell'applicazione da esso vengono caricati i pannelli, e
     *  con caricati si intende effettuare un primo singolo avvio, e successivamente vengono continuamente aggiornati (chi ne richiede) con il Runnable
     *  dynamicPanels.
     *
     *  Sono presenti due tipi di panels:
     *      #Statici    :  Sono dei pannelli che non richiedono aggiornamenti continui, come per esempio il pannell per il profilo dell'impiegato.
     *      #Dinamici   :  Sono i pannell che necessitano di un costante aggiornamento dei reports per effettuare le operazioni su di essi, per esempio
     *                  il pannello dei record in attesa.
     *
     *  dynamicPanels -    Si occupa di fornire alle funzioni di aggiornamento dei vari panels dinamici, il rispettivo vettore dei record che è stato aggiornato
     *                      precedentemente con il metodo retrieveReportIDs().
     *
     *  Per la gestione di tutti i pannelli si utilizza un CardLayout, la gestione di questo layout è stata lasciata alla classe Utils.java per non rendere ulteriormente
     *  troppo denso il codice di questa classe.
     *
     * @param loggedEmployee
     * @throws IOException
     */

    public ControlPanel(Employee loggedEmployee) throws IOException {
        requestFocus(true);
        addKeyListener(this);
        Utils utils = new Utils();

        ProfilePanel profilePanel = new ProfilePanel();
        profilePanel.loadProfilePanel(loggedEmployee);

        pendingReportPanel.loadPendingReportsPanel(pendingReportIDs);
        streamingStatsReviewsPanel.loadStatsRecensioniStream();
        statsReportsPanel.loadStatsEmployee();
        openReportPanel.loadOpenReportsPanel(openReportIDs, chatInstances, loggedEmployee);
        closedReportPanel.loadClosedReportsPanel(closedReportIDs);
        createReportPanel.loadCreateReportPanel(loggedEmployee);

        Runnable dynamicPanels = () -> {
            if (cardShowed == 1) {
                utils.retrieveReportsIDs(
                        pendingReportIDs,
                        openReportIDs,
                        closedReportIDs,
                        databaseReference,
                        semaphore,
                        chatInstances,
                        this);
                pendingReportPanel.updatePendingReportsPanel(pendingReportIDs);
            } else if (cardShowed == 2) {
                utils.retrieveReportsIDs(
                        pendingReportIDs,
                        openReportIDs,
                        closedReportIDs,
                        databaseReference,
                        semaphore,
                        chatInstances,
                        this);
                openReportPanel.updateOpenReportsPanel(openReportIDs, chatInstances);
            } else if (cardShowed == 3) {
                utils.retrieveReportsIDs(
                        pendingReportIDs,
                        openReportIDs,
                        closedReportIDs,
                        databaseReference,
                        semaphore,
                        chatInstances,
                        this);
                closedReportPanel.updateClosedReportsPanel(closedReportIDs);
            } else //noinspection StatementWithEmptyBody
                if (cardShowed == 4) {

                } else //noinspection StatementWithEmptyBody
                    if (cardShowed == 5) {

                    } else //noinspection StatementWithEmptyBody
                        if (cardShowed == 6) {

                        }
        };

        ScheduledExecutorService dynamicPanelsExecutor = Executors.newSingleThreadScheduledExecutor();
        dynamicPanelsExecutor.scheduleAtFixedRate(
                dynamicPanels,
                0,
                500,
                TimeUnit.MILLISECONDS);

        CardLayout cardLayout = new CardLayout();
        Container container = getContentPane();
        utils.initMenu(cardLayout, container, this);
        utils.initialize(this);
        utils.setLayoutManager(container,
                cardLayout,
                profilePanel,
                pendingReportPanel,
                openReportPanel,
                closedReportPanel,
                streamingStatsReviewsPanel,
                statsReportsPanel,
                createReportPanel,
                this);
    }

    /***
     * Metodo che aggiorna l'indice relativa al pannello mostrato con il valore passato.
     * @param val
     */
    public void setCardShowed(int val) {
        cardShowed = val;
    }

    /***
     * Alla pressione del tasto F5 verranno mostrati sulla console lo stato dei vettori contenenti le segnalazioni smistate, questo serve
     * per capire la posizione dei record all'interno dell'applicazione e che percorso stanno effettuando.
     * È una piccola funzione di stampa utile per il debugging.
     * @param e
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_F5) {
            try {
                semaphore.acquire();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }

            System.out.println("{Refreshed}\n"
                    + "[PendingReports]:\n" + pendingReportIDs
                    + "\n\n[OpenReports]:\n" + openReportIDs
                    + "\n\n[ClosedReports]:\n" + closedReportIDs);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }


}