package GUI;

import com.google.firebase.database.*;
import com.google.firebase.database.core.Repo;
import utils.Employee;
import utils.Report;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DashBoard extends JFrame implements KeyListener {
    private Container container = getContentPane();

    //  Elements of JFrame
    private static JLabel uidLabel;
    private static JLabel emailLabel;
    private static JLabel tokenLabel;
    private static JTextPane istructionText;

    private JPanel profilePanel = new JPanel();
    private JPanel reportPanel = new JPanel();
    private JPanel statsPanel = new JPanel();
    private JPanel openReportPanel = new JPanel();
    private JPanel closedReportPanel = new JPanel();

    private JButton handleReport = new JButton("Edit");
    private JButton openReport = new JButton("Apri Segnalazione");
    private JButton refreshButton = new JButton("Refresh");

    private static JMenuBar jMenuBar;
    private static JMenu fileMenu, reportMenu;
    private static JMenuItem profileItem, exitItem, handlingReports, openReports, closedReports, statsReports;

    private CardLayout cardLayout = new CardLayout();

    //  Utils
    private static Vector<String> pendingReportIDs = new Vector<String>();
    private static Vector<String> openReportIDs = new Vector<String>();
    private static Vector<String> closedReportIDs = new Vector<String>();
    private static final String istructions = "\n\nISTRUZIONI PER l'USO\n1. Premere F5 per aggiornare le entries \n2. Non arrabbiarsi è una pre-alpha\n3. Per eventuali suggerimenti pagliuca.manuel@gmail.com\n";
    private static Report singlePendingReport, singleOpenReport;

    /*private String id;
    private String priority;
    private String object;
    private String date;
    private String time;
    private String uid;
    private String type;
    private String description;
    private String position;
    private String social;*/

    private static JList<String> jListOpenReports;
    private static JLabel openReportTitle = new JLabel("Segnalazioni Aperte");

    //  Pending Report Panel
    private static JLabel labelReportInfo = new JLabel();
    private static JPanel rightPane = new JPanel();
    private static JSplitPane splitPane = new JSplitPane();

    //  Thread
    private static Semaphore semaphore = new Semaphore(0);
    private static Semaphore waitForData = new Semaphore(0);
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private Runnable r;

    //  External
    private static Vector<ChatBidirectional> chatInstances = new Vector<ChatBidirectional>();
    private static Employee employee;
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    public DashBoard(Employee t_employee) {
        addKeyListener(this);
        requestFocus(true);

        employee = t_employee;
        uidLabel = new JLabel("UID: " + employee.getUID(), SwingConstants.LEFT);
        emailLabel = new JLabel("e-Mail: " + employee.getEmail());
        tokenLabel = new JLabel("TokenID: " + employee.getTokenID());

        uidLabel.setAlignmentX(JLabel.CENTER);
        emailLabel.setAlignmentX(JLabel.CENTER);
        tokenLabel.setAlignmentX(JLabel.CENTER);

        istructionText = new JTextPane();
        istructionText.setEditable(false);

        SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(simpleAttributeSet, Color.RED);
        istructionText.setText(istructions);

        //  Update values in JPanels
        r = new Runnable() {
            @Override
            public void run() {
                retrieveReportsIDs();
            }
        };

        //  Load elements in JPanel
        loadOpenReportsPanel();
        loadClosedReportPanel();

        executor.scheduleAtFixedRate(r, 0, 1500, TimeUnit.MILLISECONDS);

        initMenu();
        initialize();
        setLayoutManager();
        setLocationAndSize();
    }

    public void setLayoutManager() {
        container.setLayout(cardLayout);

        loadProfilePanel();
        loadStatsReportPanel();

        container.add(profilePanel, "0");
        container.add(reportPanel, "1");
        container.add(statsPanel, "2");
        container.add(openReportPanel, "3");
        container.add(closedReportPanel, "4");
        cardLayout.show(container, "0");
    }

    //  Panels
    private void loadProfilePanel() {
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.add(uidLabel);
        profilePanel.add(emailLabel);
        profilePanel.add(tokenLabel);
        //profilePanel.add(istructionText);
        profilePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    private void loadOpenReportsPanel() {
        openReportTitle.setSize(20, 20);
        openReportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        jListOpenReports = new JList<String>(openReportIDs);
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
                    if (d.child("status").getValue().toString().split("_")[0].equals("Pending")) {
                        if (!pendingReportIDs.contains(d.getKey())) {
                            pendingReportIDs.add(d.getKey());
                        }
                    } else if (d.child("status").getValue().toString().split("_")[0].equals("Aperta")) {
                        if (!openReportIDs.contains(d.getKey())) {
                            openReportIDs.add(d.getKey());
                        }
                    } else if (d.child("status").getValue().toString().split("_")[0].equals("Chiusa")) {
                        if (!closedReportIDs.contains(d.getKey())) {
                            closedReportIDs.add(d.getKey());
                        }
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
        jMenuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        reportMenu = new JMenu("Segnalazioni");

        handlingReports = new JMenuItem(new AbstractAction("Pending List") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "1");
            }
        });

        openReports = new JMenuItem(new AbstractAction("Aperte") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "3");
            }
        });
        closedReports = new JMenuItem(new AbstractAction("Chiuse") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "4");
            }
        });

        statsReports = new JMenuItem(new AbstractAction("Statistiche") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "2");
            }
        });

        profileItem = new JMenuItem(new AbstractAction("Profile") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "0");
            }
        });

        exitItem = new JMenuItem(new AbstractAction("Esci") {
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

            JList<String> stringJList = new JList<String>(pendingReportIDs);

            stringJList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
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

                }
            });


            handleReport.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DialogExample dialogExample = new DialogExample(singlePendingReport);

                    requestFocus(true);
                    reportPanel.removeAll();
                    reportPanel.add(splitPane);
                    reportPanel.revalidate();
                    reportPanel.repaint();
                }
            });

            openReport.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    databaseReference.child(singlePendingReport.getId()).child("status").setValueAsync("Aperta_"+singlePendingReport.getId());

                    reportPanel.removeAll();
                    reportPanel.add(splitPane);
                    reportPanel.revalidate();
                    reportPanel.repaint();
                }
            });

            refreshButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("refresh");
                }
            });

            reportPanel.setPreferredSize(new Dimension(800, 600));

            labelReportInfo.setHorizontalAlignment(SwingConstants.LEFT);
            labelReportInfo.setVerticalAlignment(SwingConstants.TOP);

            rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));

            JScrollPane jScrollPane = new JScrollPane(stringJList);
            reportPanel.add(refreshButton);

            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, jScrollPane, rightPane);

            splitPane.setDividerLocation(180);

            splitPane.setOneTouchExpandable(true);
            splitPane.setPreferredSize(new Dimension(750, 520));

            reportPanel.removeAll();
            reportPanel.add(splitPane);
            reportPanel.revalidate();
            reportPanel.repaint();
        }
        requestFocus(true);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}


