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

    private final OpenReportsPanel openReportPanel = new OpenReportsPanel();
    private final PendingReportsPanel pendingReportPanel;

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");
    private final Semaphore semaphore = new Semaphore(0);

    private int cardShowed;
    private final Employee employee;

    public Board(Employee t_employee) {
        employee = t_employee;

        requestFocus(true);
        addKeyListener(this);
        Utils utils = new Utils();

        pendingReportPanel = new PendingReportsPanel(databaseReference);
        pendingReportPanel.loadPendingReportsPanel(pendingReportIDs);

        ProfilePanel profilePanel = new ProfilePanel();
        profilePanel.loadProfilePanel(employee);
        profilePanel.setLocationAndSize();

        StatsReviewsPanel statsReviewsPanel = new StatsReviewsPanel();
        statsReviewsPanel.loadStatsRecensioniStream();

        StatsReportsPanel statsReportsPanel = new StatsReportsPanel();
        statsReportsPanel.loadStatsEmployee();

        openReportPanel.loadOpenReportsPanel(openReportIDs, chatInstances, employee);

        ClosedReportsPanel closedReportPanel = new ClosedReportsPanel();
        closedReportPanel.loadClosedReportPanel();

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
                statsReviewsPanel,
                statsReportsPanel,
                this);

        Runnable r = () -> {
            utils.retrieveReportsIDs(
                    pendingReportIDs,
                    openReportIDs,
                    closedReportIDs,
                    databaseReference,
                    semaphore,
                    chatInstances,
                    this);

            if (cardShowed == 1) {
                pendingReportPanel.updatePendingReportsPanel(pendingReportIDs);
            } else if (cardShowed == 2) {
                openReportPanel.updateOpenReportsPanel(openReportIDs, chatInstances);
            }
        };

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(r, 0, 600, TimeUnit.MILLISECONDS);
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