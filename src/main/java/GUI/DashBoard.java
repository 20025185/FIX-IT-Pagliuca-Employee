package GUI;

import com.google.firebase.database.*;
import kafka.members.Consumer;
import kafka.streams.FixItStream;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import utils.Employee;
import utils.Report;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.*;


public class DashBoard extends JFrame implements KeyListener {
    //  Elements of JFrame
    private JLabel uidLabel;
    private JLabel emailLabel;
    private JLabel tokenLabel;
    private final Container container = getContentPane();
    private final CardLayout cardLayout = new CardLayout();

    private final Vector<String> pendingReportIDs = new Vector<>();
    private final Vector<String> openReportIDs = new Vector<>();
    private final Vector<String> closedReportIDs = new Vector<>();
    private static Report singlePendingReport, singleOpenReport;
    private static JList<String> jListOpenReports;
    private final JLabel openReportTitle = new JLabel("Segnalazioni Aperte");

    //  Profile Panel
    private final JPanel profilePanel = new JPanel();

    //  Pending Report Panel
    private final JPanel pendingReportPanel = new JPanel();
    private final JLabel labelReportInfo = new JLabel();
    private final JPanel pendingRightPane = new JPanel();
    private JSplitPane pendingSplitPane = new JSplitPane();
    private final JButton editBtn = new JButton("Edit");
    private final JButton openStatusBtn = new JButton("Apri Segnalazione");

    //  Open reports Panel
    private final JPanel openReportPanel = new JPanel();
    private JSplitPane openSplitPane = new JSplitPane();
    private final JLabel openReportlabelInfo = new JLabel();
    private final JPanel openRightPanel = new JPanel();
    private final JRadioButton pendingRadioBtn = new JRadioButton("Pending");
    private final JRadioButton closedRadioBtn = new JRadioButton("Chiusa");
    private final ButtonGroup statusButtons = new ButtonGroup();
    private final JButton submitStatusBtn = new JButton("Imposta");

    //  Closed reports Panel
    private final JPanel closedReportPanel = new JPanel();

    //  Stats Panel
    private final JPanel statsPanel = new JPanel();

    //  Thread
    private final Semaphore semaphore = new Semaphore(0);

    //  Utils
    private int cardShowed;

    public enum ISSUE_TYPE {
        PROBLEMATICA_STRADALE,
        PROBLEMATICA_ORIGINE_NATURALE,
        ATTIVITA_SOSPETTE,
        ALTRO
    }


    //  External
    private final Vector<ChatBidirectional> chatInstances = new Vector<>();
    private final Employee employee;
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    public DashBoard(Employee t_employee) {
        employee = t_employee;
        requestFocus(true);
        addKeyListener(this);

        Runnable r = this::retrieveReportsIDs;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(r, 0, 100, TimeUnit.MILLISECONDS);

        //  First call for initialise GUI, next will need refresh.
        loadPendingReportsPanel();
        loadProfilePanel();
        loadStatsReportPanel();
        loadOpenReportsPanel();
        loadClosedReportPanel();
        handlePendingReportsButtons();
        initMenu();
        initialize();
        setLayoutManager();
        setLocationAndSize();
    }

    //  Thread function, for download data in to vectors
    private void retrieveReportsIDs() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    switch (d.child("status").getValue().toString().split("_")[0]) {
                        case "Pending":
                            if (!pendingReportIDs.contains(d.getKey())) {
                                pendingReportIDs.add(d.getKey());
                            }
                            break;
                        case "Aperta":
                            if (!openReportIDs.contains(d.getKey())) {
                                openReportIDs.add(d.getKey());
                            }
                            break;
                        case "Chiusa":
                            if (!closedReportIDs.contains(d.getKey())) {
                                closedReportIDs.add(d.getKey());
                            }
                            break;
                    }
                }

                checkIntegrity(pendingReportIDs, "Pending", dataSnapshot);
                checkIntegrity(openReportIDs, "Aperta", dataSnapshot);
                checkIntegrity(closedReportIDs, "Chiusa", dataSnapshot);

                semaphore.release();

                if (chatInstances.isEmpty()) {
                    requestFocus(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkIntegrity(Vector<String> vectorIDs, String status, DataSnapshot dataSnapshot) {
        boolean isThere = false;

        for (String str : vectorIDs) {
            for (DataSnapshot d : dataSnapshot.getChildren()) {
                if (d.child("status").getValue().toString().split("_")[0].equals(status)) {
                    if (d.getKey().equals(str)) {
                        isThere = true;
                    }
                }
            }

            if (!isThere) {
                vectorIDs.remove(str);
            }
        }
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

    private void loadOpenReportsPanel() {
        jListOpenReports = new JList<>(openReportIDs);
        jListOpenReports.setAlignmentX(Component.LEFT_ALIGNMENT);

        //  Static
        openReportTitle.setSize(20, 20);
        openReportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        //  Dynamic
        openReportPanel.remove(openSplitPane);
        openReportPanel.remove(openRightPanel);
        openReportPanel.remove(openReportlabelInfo);

        jListOpenReports = new JList<>(openReportIDs);
        jListOpenReports.setAlignmentX(Component.LEFT_ALIGNMENT);

        jListOpenReports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final String reportId = jListOpenReports.getSelectedValue();

                if (e.getClickCount() == 1) {
                    Semaphore semaphore = new Semaphore(0);

                    openReportlabelInfo.setText("");
                    openRightPanel.remove(openReportlabelInfo);
                    openRightPanel.remove(pendingRadioBtn);
                    openRightPanel.remove(closedRadioBtn);
                    openRightPanel.remove(submitStatusBtn);

                    openRightPanel.revalidate();
                    openRightPanel.repaint();

                    databaseReference.child(reportId).addValueEventListener(new ValueEventListener() {
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
                            String status = dataSnapshot.child("status").getValue().toString().split("_")[0];
                            String social = dataSnapshot.child("social").getValue().toString();

                            singleOpenReport = new Report(reportId, uid, object, description, date, time, type, position, priority, "Aperta_" + reportId, social);

                            openReportlabelInfo.setText("<html>" +
                                    "Oggetto : " + object + "<br><br>" +
                                    "Data : " + date + "<br><br>" +
                                    "Ora : " + time + "<br><br>" +
                                    "UID : " + uid + "<br><br>" +
                                    "Tipologia : " + type + "<br><br>" +
                                    "Descrizione : " + description + "<br><br>" +
                                    "Coordinate : " + position + "<br><br>" +
                                    "Priorità : " + priority + "<br><br>" +
                                    "Diffusione su social : " + social + "<br><br>" +
                                    "Status : " + status + "<br><br></html>");

                            semaphore.release();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    try {
                        semaphore.acquire();
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }

                    openRightPanel.add(openReportlabelInfo);
                    openRightPanel.add(pendingRadioBtn);
                    openRightPanel.add(closedRadioBtn);

                    submitStatusBtn.addActionListener(e1 -> {
                        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("reports");

                        if (pendingRadioBtn.isSelected()) {
                            singleOpenReport.setStatus("Pending_" + singleOpenReport.getUid());
                            openReportIDs.remove(singleOpenReport.getId());
                            dbr.child(singleOpenReport.getId()).child("status").setValueAsync(singleOpenReport.getStatus());

                        } else if (closedRadioBtn.isSelected()) {
                            singleOpenReport.setStatus("Chiusa_" + singleOpenReport.getUid());
                            openReportIDs.remove(singleOpenReport.getId());
                            dbr.child(singleOpenReport.getId()).child("status").setValueAsync(singleOpenReport.getStatus());
                        }

                        loadOpenReportsPanel();
                    });

                    openRightPanel.add(submitStatusBtn);
                }

                if (e.getClickCount() == 2) {
                    if (chatInstances.isEmpty()) {
                        ChatBidirectional chatBidirectional = new ChatBidirectional(singleOpenReport, employee);
                        chatInstances.add(chatBidirectional);
                    } else {
                        boolean isAlreadyOpened = false;

                        for (ChatBidirectional elem : chatInstances) {
                            if (elem.getReportID().equals(reportId)) {
                                isAlreadyOpened = true;
                            }
                        }

                        if (!isAlreadyOpened) {
                            ChatBidirectional chatBidirectional = new ChatBidirectional(singleOpenReport, employee);
                            chatInstances.add(chatBidirectional);
                        }
                    }
                }
                super.mouseClicked(e);
            }
        });

        if (jListOpenReports.isSelectionEmpty()) {
            openReportlabelInfo.setText("");
            openRightPanel.remove(openReportlabelInfo);
            openRightPanel.remove(pendingRadioBtn);
            openRightPanel.remove(closedRadioBtn);
            openRightPanel.remove(submitStatusBtn);
        }

        openRightPanel.setLayout(new BoxLayout(openRightPanel, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(jListOpenReports);
        openSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, jScrollPane, openRightPanel);
        openSplitPane.setPreferredSize(new Dimension(750, 520));
        openSplitPane.setDividerLocation(180);
        openSplitPane.setOneTouchExpandable(true);

        openReportPanel.revalidate();
        openReportPanel.repaint();
        openReportPanel.setPreferredSize(new Dimension(800, 600));
        openReportPanel.add(openSplitPane);
    }

    private void loadClosedReportPanel() {

        closedReportPanel.setBackground(Color.YELLOW);
    }

    private void loadStatsReportPanel() {

        FixItStream favReviewStream = new FixItStream("input-ratings");
        favReviewStream.execute();

        Consumer consumerFavReview = new Consumer("count-fav-issues");
        consumerFavReview.run();
    }


    //  Static functions
    public void setLayoutManager() {
        container.setLayout(cardLayout);
        container.add(profilePanel, "0");
        container.add(pendingReportPanel, "1");
        container.add(openReportPanel, "2");
        container.add(closedReportPanel, "3");
        container.add(statsPanel, "4");
        cardLayout.show(container, "0");
        cardShowed = 0;
    }

    private void handlePendingReportsButtons() {
        editBtn.addActionListener(e -> {
            DialogExample dialogExample = new DialogExample(singlePendingReport);

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

    private void loadProfilePanel() {   //  For the moment is dynamic, but should be static.
        uidLabel = new JLabel("UID: " + employee.getUID(), SwingConstants.LEFT);
        emailLabel = new JLabel("e-Mail: " + employee.getEmail());
        tokenLabel = new JLabel("TokenID: " + employee.getTokenID());

        uidLabel.setAlignmentX(JLabel.CENTER);
        emailLabel.setAlignmentX(JLabel.CENTER);
        tokenLabel.setAlignmentX(JLabel.CENTER);

        JTextPane istructionText = new JTextPane();
        istructionText.setEditable(false);

        SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(simpleAttributeSet, StyleConstants.ALIGN_CENTER);
        istructionText.setParagraphAttributes(simpleAttributeSet, false);
        //  Utils
        String istructions = "\n\nISTRUZIONI PER l'USO" +
                "\n1. Premere F5 per aggiornare le entries \n" +
                "2. Nel gestore delle segnalazioni aperte, con un doppio click sul codice della segnalazione sarà possibile aprire la chat con l'utente segnalatore.\n" +
                "3. Per eventuali suggerimenti pagliuca.manuel@gmail.com\n";
        istructionText.setText(istructions);

        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.add(uidLabel);
        profilePanel.add(emailLabel);
        profilePanel.add(tokenLabel);
        profilePanel.add(istructionText);
        profilePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    public void setLocationAndSize() {
        uidLabel.setBounds(110, 150, 250, 30);
        emailLabel.setBounds(110, 250, 150, 30);
        tokenLabel.setBounds(110, 350, 250, 30);
    }

    private void initMenu() {
        JMenuBar jMenuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu reportMenu = new JMenu("Segnalazioni");

        JMenuItem profileItem = new JMenuItem(new AbstractAction("Profile") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "0");
                cardShowed = 0;
            }
        });

        JMenuItem handlingReports = new JMenuItem(new AbstractAction("Pending List") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "1");
                cardShowed = 1;
            }
        });

        JMenuItem openReports = new JMenuItem(new AbstractAction("Aperte") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "2");
                cardShowed = 2;
            }
        });

        JMenuItem closedReports = new JMenuItem(new AbstractAction("Chiuse") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "3");
                cardShowed = 3;
            }
        });

        JMenu statsMenu = new JMenu("Statistiche");

        JMenuItem statReviewReports = new JMenuItem(new AbstractAction("Recensioni") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "4");
                cardShowed = 4;
            }
        });

        JMenuItem statReports = new JMenuItem(new AbstractAction("Segnalazioni") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        JMenuItem exitItem = new JMenuItem(new AbstractAction("Esci") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DashBoard.this.dispose();
                System.exit(1);
            }
        });

        statsMenu.add(statReviewReports);
        statsMenu.add(statReports);

        reportMenu.add(handlingReports);
        reportMenu.add(openReports);
        reportMenu.add(closedReports);
        reportMenu.add(statsMenu);

        fileMenu.add(profileItem);
        fileMenu.add(exitItem);

        jMenuBar.add(fileMenu);
        jMenuBar.add(reportMenu);

        this.setJMenuBar(jMenuBar);

    }

    private void initialize() {
        this.setTitle("[FIX-IT] - DASHBOARD");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        statusButtons.add(pendingRadioBtn);
        statusButtons.add(closedRadioBtn);
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
                loadOpenReportsPanel();
            }
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

}