package GUI;

import GUI.dialogs.ChatBidirectional;
import GUI.others.Utils;
import GUI.panels.*;
import com.google.firebase.database.*;
import utils.Employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.concurrent.*;

public class Board extends JFrame implements KeyListener {
    private final Vector<String> pendingReportIDs = new Vector<>();
    private final Vector<String> openReportIDs = new Vector<>();
    private final Vector<String> closedReportIDs = new Vector<>();
    private final Vector<ChatBidirectional> chatInstances = new Vector<>();

    private final OpenReports openReportPanel = new OpenReports();
    private final PendingReports pendingReportPanel;

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");
    private final Semaphore semaphore = new Semaphore(0);

    private int cardShowed;

    public Board(Employee loggedEmployee) {

        requestFocus(true);
        addKeyListener(this);
        Utils utils = new Utils();

        pendingReportPanel = new PendingReports(databaseReference);
        pendingReportPanel.loadPendingReportsPanel(pendingReportIDs);

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
            } else if (cardShowed == 4) {
                //
            } else if (cardShowed == 5) {
                //
            } else if (cardShowed == 6) {

            }

        };

        ScheduledExecutorService dynamicPanelsExecutor = Executors.newSingleThreadScheduledExecutor();
        dynamicPanelsExecutor.scheduleAtFixedRate(dynamicPanels, 0, 600, TimeUnit.MILLISECONDS);

        Profile profile = new Profile();
        profile.loadProfilePanel(loggedEmployee);
        profile.setLocationAndSize();

        StreamingStatsReviews streamingStatsReviews = new StreamingStatsReviews();
        streamingStatsReviews.loadStatsRecensioniStream();

        StatsReports statsReports = new StatsReports();
        statsReports.loadStatsEmployee();

        openReportPanel.loadOpenReportsPanel(openReportIDs, chatInstances, loggedEmployee);

        CreateReport createReportPanel = new CreateReport(loggedEmployee);
        createReportPanel.loadCreateReportPanel();

        ClosedReports closedReportPanel = new ClosedReports();
        closedReportPanel.loadClosedReportPanel();

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
                streamingStatsReviews,
                statsReports,
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
        //  Just for Debug
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