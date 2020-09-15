package GUI;

import GUI.panels.*;
import com.google.firebase.database.*;
import utils.Employee;
import utils.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.concurrent.*;

public class Board extends JFrame implements KeyListener {
    private final Container container = getContentPane();
    private final CardLayout cardLayout = new CardLayout();

    private final Vector<String> pendingReportIDs = new Vector<>();
    private final Vector<String> openReportIDs = new Vector<>();
    private final Vector<String> closedReportIDs = new Vector<>();
    private static Report singlePendingReport;

    //  Pending Report Panel
    private final JLabel labelReportInfo = new JLabel();
    private JSplitPane pendingSplitPane = new JSplitPane();
    private final JButton editBtn = new JButton("Edit");
    private final JButton openStatusBtn = new JButton("Apri Segnalazione");

    private final JPanel pendingReportPanel = new JPanel();
    private final JPanel pendingRightPane = new JPanel();
    private final ProfilePanel profilePanel = new ProfilePanel();
    private final OpenReportsPanel openReportPanel = new OpenReportsPanel();
    private final ClosedReportsPanel closedReportPanel = new ClosedReportsPanel();
    private final StatsReviewsPanel statsReviewsPanel = new StatsReviewsPanel();
    private final StatsReportsPanel statsReportsPanel = new StatsReportsPanel();

    private final Semaphore semaphore = new Semaphore(0);
    private int cardShowed;

    //  External
    private final Vector<ChatBidirectional> chatInstances = new Vector<>();
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");
    private Employee employee = null;

    public Board(Employee t_employee) {
        employee = t_employee;

        requestFocus(true);
        addKeyListener(this);
        Utils utils = new Utils();
        Runnable r = () -> utils.retrieveReportsIDs(
                pendingReportIDs,
                openReportIDs,
                closedReportIDs,
                databaseReference,
                semaphore,
                chatInstances,
                this);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(r, 0, 100, TimeUnit.MILLISECONDS);

        loadPendingReportsPanel();
        handlePendingReportsButtons();
        profilePanel.loadProfilePanel(employee);
        profilePanel.setLocationAndSize();
        statsReviewsPanel.loadStatsRecensioniStream();
        statsReportsPanel.loadStatsEmployee();
        openReportPanel.loadOpenReportsPanel(openReportIDs, databaseReference, chatInstances, employee);
        closedReportPanel.loadClosedReportPanel();


        utils.initMenu(cardLayout, container, this);
        utils.initialize(this);
        setLayoutManager();
    }


    //  Dynamic functions (depends from keyListeners and thread function)
    private void loadPendingReportsPanel() {
        JList<String> stringJList = new JList<>(pendingReportIDs);

        stringJList.addListSelectionListener(e1 -> {
            final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("reports").child(stringJList.getSelectedValue());

            dbr.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String priority = dataSnapshot.child("priority").getValue().toString();
                    String object = dataSnapshot.child("object").getValue().toString();
                    String date = dataSnapshot.child("date").getValue().toString();
                    String time = dataSnapshot.child("time").getValue().toString();
                    String uid = dataSnapshot.child("uid").getValue().toString();
                    String type = dataSnapshot.child("type").getValue().toString();
                    String description = dataSnapshot.child("description").getValue().toString();
                    String position = dataSnapshot.child("position").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String social = dataSnapshot.child("social").getValue().toString();
                    String id = stringJList.getSelectedValue();

                    singlePendingReport = new Report(id, uid, object, description, date, time, type, position, priority, status, social);

                    switch (priority) {
                        case "0":
                            stringJList.setSelectionBackground(Color.GREEN);
                            break;
                        case "1":
                            stringJList.setSelectionBackground(Color.YELLOW);
                            break;
                        case "2":
                            stringJList.setSelectionBackground(Color.RED);
                            break;
                        default:
                            stringJList.setSelectionBackground(Color.GRAY);
                            break;
                    }

                    labelReportInfo.setText("<html>" +
                            "Oggetto : " + object + "<br><br>" +
                            "Data : " + date + "<br><br>" +
                            "Ora : " + time + "<br><br>" +
                            "UID : " + uid + "<br><br>" +
                            "Tipologia : " + type + "<br><br>" +
                            "Descrizione : " + description + "<br><br>" +
                            "Coordinate : " + position + "<br><br>" +
                            "Priorità : " + priority + "<br><br>" +
                            "Diffusione su social : " + social + "<br><br>" +
                            "Status : Pending <br><br>" + "</html>");

                    pendingRightPane.add(labelReportInfo);
                    pendingRightPane.add(editBtn);
                    pendingRightPane.add(openStatusBtn);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        });

        pendingReportPanel.setPreferredSize(new Dimension(800, 600));

        labelReportInfo.setHorizontalAlignment(SwingConstants.LEFT);
        labelReportInfo.setVerticalAlignment(SwingConstants.TOP);

        pendingRightPane.setLayout(new BoxLayout(pendingRightPane, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(stringJList);

        pendingSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, jScrollPane, pendingRightPane);
        pendingSplitPane.setDividerLocation(180);
        pendingSplitPane.setOneTouchExpandable(true);
        pendingSplitPane.setPreferredSize(new Dimension(750, 520));

        pendingReportPanel.removeAll();
        pendingReportPanel.add(pendingSplitPane);
        pendingReportPanel.revalidate();
        pendingReportPanel.repaint();
    }

    public void setLayoutManager() {
        container.setLayout(cardLayout);
        container.add(profilePanel, "0");
        container.add(pendingReportPanel, "1");
        container.add(openReportPanel, "2");
        container.add(closedReportPanel, "3");
        container.add(statsReviewsPanel, "4");
        container.add(statsReportsPanel, "5");
        cardLayout.show(container, "0");
        cardShowed = 0;
    }

    private void handlePendingReportsButtons() {
        editBtn.addActionListener(e -> {
            new EditReportFrame(singlePendingReport);

            if (singlePendingReport.getStatus().equals("Aperta") || singlePendingReport.getStatus().equals("Chiusa")) {
                pendingReportIDs.remove(singlePendingReport.getId());
            }

            requestFocus(true);
            pendingReportPanel.removeAll();
            pendingReportPanel.add(pendingSplitPane);
            pendingReportPanel.revalidate();
            pendingReportPanel.repaint();
        });

        openStatusBtn.addActionListener(e -> {
            databaseReference.child(singlePendingReport.getId()).child("status").setValueAsync("Aperta_" + singlePendingReport.getUid());
            pendingReportIDs.remove(singlePendingReport.getId());

            requestFocus(true);
            pendingReportPanel.removeAll();
            pendingReportPanel.add(pendingSplitPane);
            pendingReportPanel.revalidate();
            pendingReportPanel.repaint();
        });
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

            if (cardShowed == 1) {
                loadPendingReportsPanel();

            } else if (cardShowed == 2) {
                openReportPanel.loadOpenReportsPanel(openReportIDs, databaseReference, chatInstances, employee);
            }
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}