package GUI.panels;

import GUI.ChatBidirectional;
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
    private final JLabel openReportTitle = new JLabel("Segnalazioni Aperte");
    private final JPanel openRightPanel = new JPanel();
    private JSplitPane openSplitPane = new JSplitPane();
    private final JLabel openReportlabelInfo = new JLabel();

    private static JList<String> jListOpenReports;
    private static Report singleOpenReport;
    private final JRadioButton pendingRadioBtn = new JRadioButton("Pending");
    private final JRadioButton closedRadioBtn = new JRadioButton("Chiusa");
    private final ButtonGroup statusButtons = new ButtonGroup();
    private final JButton submitStatusBtn = new JButton("Imposta");

    public void loadOpenReportsPanel(Vector<String> openReportIDs,
                                     DatabaseReference databaseReference,
                                     Vector<ChatBidirectional> chatInstances, Employee employee) {
        jListOpenReports = new JList<>(openReportIDs);
        jListOpenReports.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusButtons.add(pendingRadioBtn);
        statusButtons.add(closedRadioBtn);

        //  Static
        openReportTitle.setSize(20, 20);
        openReportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        //  Dynamic
        this.remove(openSplitPane);
        this.remove(openRightPanel);
        this.remove(openReportlabelInfo);

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

                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            String dateFormatted = formatter.format(date);
                            dbr.child(singleOpenReport.getId()).child("data_chiusura").setValueAsync(dateFormatted);
                        }

                        loadOpenReportsPanel(openReportIDs, databaseReference, chatInstances, employee);
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

        this.revalidate();
        this.repaint();
        this.setPreferredSize(new Dimension(800, 600));
        this.add(openSplitPane);
    }
}
