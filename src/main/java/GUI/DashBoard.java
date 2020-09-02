package GUI;

import com.google.firebase.database.*;
import utils.Employee;
import utils.Report;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class DashBoard extends JFrame {
    private Container container = getContentPane();

    //  Labels
    private static JLabel uidLabel;
    private static JLabel emailLabel;
    private static JLabel tokenLabel;

    //  Menu Bar
    private static JMenuBar jMenuBar;
    private static JMenu fileMenu, reportMenu;
    private static JMenuItem profileItem, exitItem, handlingReports, openReports, closedReports, statsReports;

    //  JPanels
    private JPanel profilePanel = new JPanel();
    private JPanel reportPanel = new JPanel();
    private JPanel statsPanel = new JPanel();
    private JPanel openReportPanel = new JPanel();
    private JPanel closedReportPanel = new JPanel();
    private JButton handleReport = new JButton("Edit");
    private JButton openReport = new JButton("Apri Segnalazione");

    //  Layout
    private CardLayout cardLayout = new CardLayout();

    //  Utils
    static String[] pendingReportIDs;
    static String[] openReportIDs;
    static String[] closedReportIDs;

    private Report singleReport;

    private String id;
    private String priority;
    private String object;
    private String date;
    private String time;
    private String uid;
    private String type;
    private String description;
    private String position;
    private String social;

    private static Vector<ChatBidirectional> chatInstances = new Vector<ChatBidirectional>();


    //  External
    private static Employee employee;
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    public DashBoard(Employee t_employee) {
        employee = t_employee;

        //  Labels
        uidLabel = new JLabel("UID: " + employee.getUID());
        emailLabel = new JLabel("e-Mail: " + employee.getEmail());
        tokenLabel = new JLabel("TokenID: " + employee.getTokenID());

        //  Menu Bar
        initMenu();
        initialize();
        setLayoutManager();
        setLocationAndSize();
    }

    public void setLayoutManager() {
        container.setLayout(cardLayout);

        loadProfilePanel();
        loadReportPanel();
        loadStatsReportPanel();
        loadOpenReportsPanel();
        loadClosedReportPanel();

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
        profilePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    private void loadOpenReportsPanel() {
        JLabel title = new JLabel("Segnalazioni Aperte");
        title.setSize(20, 20);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JList<String> jList = new JList<String>(openReportIDs);
        jList.setAlignmentX(Component.LEFT_ALIGNMENT);


        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final String reportId = jList.getSelectedValue();

                databaseReference.child(reportId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        priority = dataSnapshot.child("priority").getValue().toString();
                        object = dataSnapshot.child("object").getValue().toString();
                        date = dataSnapshot.child("date").getValue().toString();
                        time = dataSnapshot.child("time").getValue().toString();
                        uid = dataSnapshot.child("uid").getValue().toString();
                        type = dataSnapshot.child("type").getValue().toString();
                        description = dataSnapshot.child("description").getValue().toString();
                        position = dataSnapshot.child("position").getValue().toString();
                        social = dataSnapshot.child("social").getValue().toString();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                JList tmp = (JList) e.getSource();

                if (e.getClickCount() == 2) {
                    int index = jList.locationToIndex(e.getPoint());
                    final Report report = new Report(reportId, uid, object, description, date, time, type, position, priority, "Open_" + reportId, social);

                    if (chatInstances.isEmpty()) {
                        ChatBidirectional chatBidirectional = new ChatBidirectional(report, employee);
                        chatInstances.add(chatBidirectional);
                    } else {
                        boolean isClean = true;

                        for (ChatBidirectional elem : chatInstances) {
                            if (elem.getReportID().equals(reportId)) {
                                isClean = false;
                            }
                        }

                        if (isClean) {
                            ChatBidirectional chatBidirectional = new ChatBidirectional(report, employee);
                            chatInstances.add(chatBidirectional);
                        }
                    }
                }
                super.mouseClicked(e);
            }
        });

        openReportPanel.add(title);
        openReportPanel.add(jList);


    }

    private void loadClosedReportPanel() {
        closedReportPanel.setBackground(Color.YELLOW);
    }

    private void loadStatsReportPanel() {
        statsPanel.setBackground(Color.RED);
    }

    private void loadReportPanel() {
        reportPanel.setPreferredSize(new Dimension(8000, 600));

        final Semaphore semaphore = new Semaphore(0);
        retrieveReportsIDs(semaphore);

        JLabel labelReportInfo = new JLabel();
        JPanel rightPane = new JPanel();

        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));


        JList<String> stringJList = new JList<String>(pendingReportIDs);

        labelReportInfo.setHorizontalAlignment(SwingConstants.LEFT);
        labelReportInfo.setVerticalAlignment(SwingConstants.TOP);

        Semaphore semaphore1 = new Semaphore(0);

        stringJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("reports").child(stringJList.getSelectedValue());

                dbr.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        priority = dataSnapshot.child("priority").getValue().toString();
                        object = dataSnapshot.child("object").getValue().toString();
                        date = dataSnapshot.child("date").getValue().toString();
                        time = dataSnapshot.child("time").getValue().toString();
                        uid = dataSnapshot.child("uid").getValue().toString();
                        type = dataSnapshot.child("type").getValue().toString();
                        description = dataSnapshot.child("description").getValue().toString();
                        position = dataSnapshot.child("position").getValue().toString();
                        social = dataSnapshot.child("social").getValue().toString();
                        id = stringJList.getSelectedValue();

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
                        semaphore1.release();
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
                try {
                    semaphore1.acquire();
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                Report report = new Report(id, uid, object, description, date, time, type, position, priority, "Pending", social);
                DialogExample dialogExample = new DialogExample(report);
                System.out.println(report.toString());
            }
        });

        openReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Report report = new Report(id, uid, object, description, date, time, type, position, priority, "Open_" + id, social);
                databaseReference.child(id).setValueAsync(report);

            }
        });

        reportPanel.add(stringJList);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, new JScrollPane(stringJList), rightPane);
        splitPane.setDividerLocation(180);
        splitPane.setOneTouchExpandable(true);
        splitPane.setPreferredSize(new Dimension(750, 520));

        reportPanel.add(splitPane);
    }

    //  Utils
    private void retrieveReportsIDs(Semaphore semaphore) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pendingReportIDs = new String[(int) dataSnapshot.getChildrenCount()];
                openReportIDs = new String[(int) dataSnapshot.getChildrenCount()];
                closedReportIDs = new String[(int) dataSnapshot.getChildrenCount()];

                int i = 0, j = 0, k = 0;
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (d.child("status").getValue().toString().split("_")[0].equals("Pending")) {
                        pendingReportIDs[i] = d.getKey();
                        i++;
                    } else if (d.child("status").getValue().toString().split("_")[0].equals("Aperta")) {
                        openReportIDs[j] = d.getKey();
                        j++;
                    } else if (d.child("status").getValue().toString().split("_")[0].equals("Chiusa")) {
                        closedReportIDs[k] = d.getKey();
                        k++;
                    }
                }

                semaphore.release();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
}


