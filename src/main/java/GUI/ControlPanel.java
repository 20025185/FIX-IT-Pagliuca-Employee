package GUI;

import GUI.dialogs.ChatBidirectional;
import GUI.others.Utils;
import GUI.panels.*;
import com.google.firebase.database.*;
import utils.Employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.*;

public class ControlPanel extends JFrame implements KeyListener {
    //  I record del DB smistati nei vettori, per poi venire distribuiti nei panels rispettivi
    private final Vector<String> pendingReportIDs = new Vector<>();
    private final Vector<String> openReportIDs = new Vector<>();
    private final Vector<String> closedReportIDs = new Vector<>();

    //  Instanza delle chat con gli impiegati aperte
    private final Vector<ChatBidirectional> chatInstances = new Vector<>();

    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    private final static OpenReports openReportPanel = new OpenReports();
    private final static PendingReports pendingReportPanel = new PendingReports(databaseReference);
    private final static ClosedReports closedReportPanel = new ClosedReports() ;
    private final static StreamingStatsReviews streamingStatsReviewsPanel = new StreamingStatsReviews();
    private final static StatsReports statsReportsPanel = new StatsReports();
    private final static CreateReport createReportPanel = new CreateReport();

    //  Semaforo per la distribuzione dei record nei vari panels
    private final Semaphore semaphore = new Semaphore(0);
    private int cardShowed;

    public ControlPanel(Employee loggedEmployee) throws IOException {
        ImageIcon favicon = new ImageIcon("src\\icon.png");
        this.setIconImage(favicon.getImage());
        requestFocus(true);
        addKeyListener(this);
        Utils utils = new Utils();

        Profile profile = new Profile();
        profile.loadProfilePanel(loggedEmployee);
        profile.setLocationAndSize();

        pendingReportPanel.loadPendingReportsPanel(pendingReportIDs);
        streamingStatsReviewsPanel.loadStatsRecensioniStream();
        statsReportsPanel.loadStatsEmployee();
        openReportPanel.loadOpenReportsPanel(openReportIDs, chatInstances, loggedEmployee);
        closedReportPanel.loadClosedReportsPanel(closedReportIDs);

        createReportPanel.setEmployee(loggedEmployee);
        createReportPanel.loadCreateReportPanel();

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
            } else if (cardShowed == 4) {
                //
            } else if (cardShowed == 5) {
                //
            } else if (cardShowed == 6) {

            }

        };

        ScheduledExecutorService dynamicPanelsExecutor = Executors.newSingleThreadScheduledExecutor();
        dynamicPanelsExecutor.scheduleAtFixedRate(
                dynamicPanels,
                0,
                800,
                TimeUnit.MILLISECONDS);

        CardLayout cardLayout = new CardLayout();
        Container container = getContentPane();
        utils.initMenu(cardLayout, container, this);
        utils.initialize(this);
        utils.setLayoutManager(container,
                cardLayout,
                profile,
                pendingReportPanel,
                openReportPanel,
                closedReportPanel,
                streamingStatsReviewsPanel,
                statsReportsPanel,
                createReportPanel,
                this);
    }

    public void setCardShowed(int val) {
        cardShowed = val;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

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
    public void keyReleased(KeyEvent e) {

    }
}