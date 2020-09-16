package GUI.panels;

import GUI.dialogs.ChatBidirectional;
import com.google.firebase.database.*;
import utils.Employee;
import utils.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class OpenReportsPanel extends JPanel {
    private JSplitPane openSplitPane = new JSplitPane();
    private final JPanel rightComponentPane = new JPanel();
    private final JLabel openReportTitle = new JLabel("Segnalazioni Aperte");
    private final JLabel openReportlabelInfo = new JLabel();
    private static JList<String> jListOpenReports;
    private static Report singleOpenReport;

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    private final ButtonGroup statusButtons = new ButtonGroup();
    private final JRadioButton pendingRadioBtn = new JRadioButton("Pending");
    private final JRadioButton closedRadioBtn = new JRadioButton("Chiusa");
    private final JButton submitStatusBtn = new JButton("Imposta");

    private Employee employee;
    private int lastSelectedIndex;
    private int lastIndexPriorityColour;

    public void updateOpenReportsPanel(Vector<String> new_openReports,
                                       Vector<ChatBidirectional> chatInstances) {
        jListOpenReports = new JList<>(new_openReports);
        jListOpenReports.setSelectedIndex(lastSelectedIndex);

        switch (lastIndexPriorityColour) {
            case 0:
                jListOpenReports.setSelectionBackground(Color.GREEN);
                break;
            case 1:
                jListOpenReports.setSelectionBackground(Color.YELLOW);
                break;
            case 2:
                jListOpenReports.setSelectionBackground(Color.RED);
                break;
            default:
                jListOpenReports.setSelectionBackground(Color.GRAY);
        }

        this.remove(openSplitPane.getLeftComponent());
        this.remove(rightComponentPane);

        rightComponentPane.setLayout(new BoxLayout(rightComponentPane, BoxLayout.Y_AXIS));

        openSplitPane.setRightComponent(rightComponentPane);
        openSplitPane.setLeftComponent(jListOpenReports);
        openSplitPane.setDividerLocation(180);


        this.revalidate();
        this.repaint();

        mySelection(new_openReports, chatInstances, employee);
    }

    public void loadOpenReportsPanel(Vector<String> openReportIDs,
                                     Vector<ChatBidirectional> chatInstances, Employee employee) {
        this.employee = employee;
        jListOpenReports = new JList<>(openReportIDs);
        jListOpenReports.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusButtons.add(pendingRadioBtn);
        statusButtons.add(closedRadioBtn);

        openReportTitle.setSize(20, 20);
        openReportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);


        rightComponentPane.setLayout(new BoxLayout(rightComponentPane, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(jListOpenReports);
        openSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        openSplitPane.setContinuousLayout(false);
        openSplitPane.setLeftComponent(jScrollPane);
        openSplitPane.setRightComponent(rightComponentPane);
        openSplitPane.setPreferredSize(new Dimension(750, 520));
        openSplitPane.setDividerLocation(180);
        openSplitPane.setOneTouchExpandable(true);

        this.setPreferredSize(new Dimension(800, 600));
        this.add(openSplitPane);
    }

    public void mySelection(Vector<String> openReportIDs,
                            Vector<ChatBidirectional> chatInstances, Employee employee) {
        jListOpenReports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final String reportId = jListOpenReports.getSelectedValue();

                if (e.getClickCount() == 1) {
                    Semaphore semaphore = new Semaphore(0);

                    openReportlabelInfo.setText("");
                    rightComponentPane.remove(openReportlabelInfo);
                    rightComponentPane.remove(pendingRadioBtn);
                    rightComponentPane.remove(closedRadioBtn);
                    rightComponentPane.remove(submitStatusBtn);
                    rightComponentPane.revalidate();
                    rightComponentPane.repaint();
                    lastSelectedIndex = jListOpenReports.getSelectedIndex();

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

                            switch (priority) {
                                case "0":
                                    jListOpenReports.setSelectionBackground(Color.GREEN);
                                    lastIndexPriorityColour = 0;
                                    break;
                                case "1":
                                    jListOpenReports.setSelectionBackground(Color.YELLOW);
                                    lastIndexPriorityColour = 1;
                                    break;
                                case "2":
                                    jListOpenReports.setSelectionBackground(Color.RED);
                                    lastIndexPriorityColour = 2;
                                    break;
                                default:
                                    jListOpenReports.setSelectionBackground(Color.GRAY);
                                    break;
                            }

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

                    rightComponentPane.add(openReportlabelInfo);
                    rightComponentPane.add(pendingRadioBtn);
                    rightComponentPane.add(closedRadioBtn);

                    submitStatusBtn.addActionListener(e1 -> {

                        if (pendingRadioBtn.isSelected()) {
                            singleOpenReport.setStatus("Pending_" + singleOpenReport.getUid());
                            openReportIDs.remove(singleOpenReport.getId());
                            jListOpenReports.remove(lastSelectedIndex);
                            databaseReference.child(singleOpenReport.getId()).child("status").setValueAsync(singleOpenReport.getStatus());
                        } else if (closedRadioBtn.isSelected()) {
                            singleOpenReport.setStatus("Chiusa_" + singleOpenReport.getUid());
                            openReportIDs.remove(singleOpenReport.getId());
                            databaseReference.child(singleOpenReport.getId()).child("status").setValueAsync(singleOpenReport.getStatus());
                            jListOpenReports.remove(lastSelectedIndex);

                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            String dateFormatted = formatter.format(date);
                            databaseReference.child(singleOpenReport.getId()).child("data_chiusura").setValueAsync(dateFormatted);

                        }

                        openReportlabelInfo.setText("");
                        rightComponentPane.remove(openReportlabelInfo);
                        rightComponentPane.remove(pendingRadioBtn);
                        rightComponentPane.remove(closedRadioBtn);
                        rightComponentPane.remove(submitStatusBtn);
                        rightComponentPane.revalidate();
                        rightComponentPane.repaint();

                    });
                    rightComponentPane.add(submitStatusBtn);
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
    }
}
