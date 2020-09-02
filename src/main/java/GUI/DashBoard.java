package GUI;

import com.google.firebase.database.*;
import utils.Employee;
import utils.Report;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
    private final JPanel reportPanel = new JPanel();
    private final JLabel labelReportInfo = new JLabel();
    private final JPanel rightPane = new JPanel();
    private JSplitPane splitPane = new JSplitPane();
    private final JButton handleReport = new JButton("Edit");
    private final JButton openReport = new JButton("Apri Segnalazione");

    //  Open reports Panel
    private final JPanel openReportPanel = new JPanel();

    //  Closed reports Panel
    private final JPanel closedReportPanel = new JPanel();

    //  Stats Panel
    private final JPanel statsPanel = new JPanel();

    //  Thread
    private final Semaphore semaphore = new Semaphore(0);

    //  External
    private final Vector<ChatBidirectional> chatInstances = new Vector<>();
    private final Employee employee;
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    public DashBoard(Employee t_employee) {
        employee = t_employee;
        addKeyListener(this);

        //  Loading static stuff
        loadProfilePanel();
        loadStatsReportPanel();
        loadOpenReportsPanel();
        loadClosedReportPanel();
        handlePendingReportsButtons();

        Runnable r = this::retrieveReportsIDs;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(r, 0, 100, TimeUnit.MILLISECONDS);

        initMenu();
        initialize();
        setLayoutManager();
        setLocationAndSize();
    }

    public void setLayoutManager() {
        container.setLayout(cardLayout);
        container.add(profilePanel, "0");
        container.add(reportPanel, "1");
        container.add(statsPanel, "2");
        container.add(openReportPanel, "3");
        container.add(closedReportPanel, "4");
        cardLayout.show(container, "0");
    }

    //  Panels
    private void loadProfilePanel() {
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
        String istructions = "\n\nISTRUZIONI PER l'USO\n1. Premere F5 per aggiornare le entries \n2. Non arrabbiarsi è una pre-alpha\n3. Per eventuali suggerimenti pagliuca.manuel@gmail.com\n";
        istructionText.setText(istructions);

        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.add(uidLabel);
        profilePanel.add(emailLabel);
        profilePanel.add(tokenLabel);
        profilePanel.add(istructionText);
        profilePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    private void loadOpenReportsPanel() {
        openReportTitle.setSize(20, 20);
        openReportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        jListOpenReports = new JList<>(openReportIDs);
        jListOpenReports.setAlignmentX(Component.LEFT_ALIGNMENT);

        jListOpenReports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final String reportId = jListOpenReports.getSelectedValue();

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
                        String social = dataSnapshot.child("social").getValue().toString();

                        singleOpenReport = new Report(reportId, uid, object, description, date, time, type, position, priority, "Open_" + reportId, social);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if (e.getClickCount() == 2) {
                    if (chatInstances.isEmpty()) {
                        ChatBidirectional chatBidirectional = new ChatBidirectional(singleOpenReport, employee);
                        chatInstances.add(chatBidirectional);
                    } else {
                        boolean isClean = true;

                        for (ChatBidirectional elem : chatInstances) {
                            if (elem.getReportID().equals(reportId)) {
                                isClean = false;
                            }
                        }

                        if (isClean) {
                            ChatBidirectional chatBidirectional = new ChatBidirectional(singleOpenReport, employee);
                            chatInstances.add(chatBidirectional);
                        }
                    }
                }
                super.mouseClicked(e);
            }
        });

        openReportPanel.add(openReportTitle);
        openReportPanel.add(jListOpenReports);
    }

    private void loadClosedReportPanel() {
        closedReportPanel.setBackground(Color.YELLOW);
    }

    private void loadStatsReportPanel() {
        statsPanel.setBackground(Color.RED);
    }

    //  Utils
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

                boolean isThere = false;

                for (String str : pendingReportIDs) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (d.child("status").getValue().toString().split("_")[0].equals("Pending")) {
                            if (d.getKey().equals(str)) {
                                isThere = true;
                            }
                        }
                    }

                    if (!isThere) {
                        pendingReportIDs.remove(str);
                    }
                }

                semaphore.release();
                requestFocus(true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

        JMenuItem handlingReports = new JMenuItem(new AbstractAction("Pending List") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "1");
            }
        });

        JMenuItem openReports = new JMenuItem(new AbstractAction("Aperte") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "3");
            }
        });
        JMenuItem closedReports = new JMenuItem(new AbstractAction("Chiuse") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "4");
            }
        });

        JMenuItem statsReports = new JMenuItem(new AbstractAction("Statistiche") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "2");
            }
        });

        JMenuItem profileItem = new JMenuItem(new AbstractAction("Profile") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "0");
            }
        });

        JMenuItem exitItem = new JMenuItem(new AbstractAction("Esci") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DashBoard.this.dispose();
                System.exit(1);
            }
        });

        this.setJMenuBar(jMenuBar);
        jMenuBar.add(fileMenu);
        jMenuBar.add(reportMenu);
        fileMenu.add(profileItem);
        fileMenu.add(exitItem);
        reportMenu.add(handlingReports);
        reportMenu.add(openReports);
        reportMenu.add(closedReports);
        reportMenu.add(statsReports);
    }

    private void initialize() {
        this.setTitle("[FIX-IT] - DASHBOARD");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        openReportPanel.setLayout(new BoxLayout(openReportPanel, BoxLayout.Y_AXIS));
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

            System.out.println("odsf pressed");

            System.out.println(pendingReportIDs);

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

                        rightPane.add(labelReportInfo);
                        rightPane.add(handleReport);
                        rightPane.add(openReport);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            });

            reportPanel.setPreferredSize(new Dimension(800, 600));

            labelReportInfo.setHorizontalAlignment(SwingConstants.LEFT);
            labelReportInfo.setVerticalAlignment(SwingConstants.TOP);

            rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));

            JScrollPane jScrollPane = new JScrollPane(stringJList);

            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, jScrollPane, rightPane);

            splitPane.setDividerLocation(180);

            splitPane.setOneTouchExpandable(true);
            splitPane.setPreferredSize(new Dimension(750, 520));

            reportPanel.removeAll();
            reportPanel.add(splitPane);
            reportPanel.revalidate();
            reportPanel.repaint();
        }
    }

    private void handlePendingReportsButtons() {
        handleReport.addActionListener(e -> {
            DialogExample dialogExample = new DialogExample(singlePendingReport);

            if (singlePendingReport.getStatus().equals("Aperta") || singlePendingReport.getStatus().equals("Chiusa")) {
                pendingReportIDs.remove(singlePendingReport.getId());
            }

            requestFocus(true);
            reportPanel.removeAll();
            reportPanel.add(splitPane);
            reportPanel.revalidate();
            reportPanel.repaint();
        });

        openReport.addActionListener(e -> {
            databaseReference.child(singlePendingReport.getId()).child("status").setValueAsync("Aperta_" + singlePendingReport.getId());
            pendingReportIDs.remove(singlePendingReport.getId());

            requestFocus(true);
            reportPanel.removeAll();
            reportPanel.add(splitPane);
            reportPanel.revalidate();
            reportPanel.repaint();
        });
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}