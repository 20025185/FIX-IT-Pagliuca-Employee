package GUI.panels;

import GUI.dialogs.EditReportFrame;
import com.google.firebase.database.*;
import utils.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class PendingReports extends JPanel {
    private final DatabaseReference databaseReference;
    private JSplitPane pendingSplitPane = new JSplitPane();
    private final JButton editBtn = new JButton("Edit");
    private final JButton openStatusBtn = new JButton("Apri Segnalazione");
    private final JLabel labelReportInfo = new JLabel();
    private final JPanel pendingRightPane = new JPanel();
    private static Report singlePendingReport;
    private static Vector<String> pendingReportIDs;
    private static JList<String> stringJList;
    private int lastSelectedIndex;
    private int lastIndexPriorityColour;

    public PendingReports(DatabaseReference ref) {
        this.databaseReference = ref;

        labelReportInfo.setHorizontalAlignment(SwingConstants.LEFT);
        labelReportInfo.setVerticalAlignment(SwingConstants.TOP);
        pendingRightPane.setLayout(new BoxLayout(pendingRightPane, BoxLayout.Y_AXIS));
        this.setPreferredSize(new Dimension(800, 600));

        handlePendingReportsButtons();
    }

    public void updatePendingReportsPanel(Vector<String> t_newReports) {
        stringJList = new JList<>(t_newReports);
        stringJList.setSelectedIndex(lastSelectedIndex);

        switch (lastIndexPriorityColour) {
            case 0:
                stringJList.setSelectionBackground(Color.GREEN);
                break;
            case 1:
                stringJList.setSelectionBackground(Color.YELLOW);
                break;
            case 2:
                stringJList.setSelectionBackground(Color.RED);
                break;
            default:
                stringJList.setSelectionBackground(Color.GRAY);
        }

        this.remove(pendingSplitPane.getLeftComponent());
        pendingSplitPane.setLeftComponent(stringJList);
        pendingSplitPane.setDividerLocation(180);

        this.revalidate();
        this.repaint();

        mySelection();
    }

    public void loadPendingReportsPanel(Vector<String> t_pendingReportIDs) {
        pendingReportIDs = t_pendingReportIDs;
        stringJList = new JList<>(pendingReportIDs);

        mySelection();


        JScrollPane jScrollPane = new JScrollPane(stringJList);
        pendingSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pendingSplitPane.setContinuousLayout(false);
        pendingSplitPane.setLeftComponent(jScrollPane);
        pendingSplitPane.setRightComponent(pendingRightPane);
        pendingSplitPane.setDividerLocation(180);
        pendingSplitPane.setOneTouchExpandable(true);
        pendingSplitPane.setPreferredSize(new Dimension(750, 520));

        this.add(pendingSplitPane);
    }

    private void handlePendingReportsButtons() {
        editBtn.addActionListener(e -> {
            new EditReportFrame(singlePendingReport);

            if (singlePendingReport.getStatus().equals("Aperta")
                    || singlePendingReport.getStatus().equals("Chiusa")) {
                pendingReportIDs.remove(singlePendingReport.getId());
            }

            pendingRightPane.removeAll();
            labelReportInfo.setText("");
            this.removeAll();
            this.add(pendingSplitPane);
            this.revalidate();
            this.repaint();
        });

        openStatusBtn.addActionListener(e -> {
            databaseReference.child(singlePendingReport.getId()).child("status").setValueAsync("Aperta_" + singlePendingReport.getUid());
            pendingReportIDs.remove(singlePendingReport.getId());

            this.removeAll();
            this.add(pendingSplitPane);
            this.revalidate();
            this.repaint();
        });
    }

    public void mySelection() {

        stringJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (stringJList.getSelectedValue() != null) {
                    final String reportID = stringJList.getSelectedValue();
                    final DatabaseReference dbr =
                            FirebaseDatabase
                                    .getInstance()
                                    .getReference("reports")
                                    .child(stringJList.getSelectedValue());
                    Semaphore semaphore = new Semaphore(1);

                    dbr.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                semaphore.acquire(1);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }

                            final String priority = dataSnapshot.child("priority").getValue().toString();
                            final String object = dataSnapshot.child("object").getValue().toString();
                            final String date = dataSnapshot.child("date").getValue().toString();
                            final String time = dataSnapshot.child("time").getValue().toString();
                            final String uid = dataSnapshot.child("uid").getValue().toString();
                            final String type = dataSnapshot.child("type").getValue().toString();
                            final String description = dataSnapshot.child("description").getValue().toString();
                            final String position = dataSnapshot.child("position").getValue().toString();
                            final String status = dataSnapshot.child("status").getValue().toString();
                            final String social = dataSnapshot.child("social").getValue().toString();

                            singlePendingReport = new Report(
                                    reportID,
                                    uid,
                                    object,
                                    description,
                                    date,
                                    time,
                                    type,
                                    position,
                                    priority,
                                    status,
                                    Boolean.parseBoolean(social));

                            switch (priority) {
                                case "0":
                                    stringJList.setSelectionBackground(Color.GREEN);
                                    lastIndexPriorityColour = 0;
                                    break;
                                case "1":
                                    stringJList.setSelectionBackground(Color.YELLOW);
                                    lastIndexPriorityColour = 1;
                                    break;
                                case "2":
                                    stringJList.setSelectionBackground(Color.RED);
                                    lastIndexPriorityColour = 2;
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

                            lastSelectedIndex = stringJList.getSelectedIndex();

                            semaphore.release(1);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    try {
                        semaphore.acquire(1);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    } finally {
                        semaphore.release();
                    }
                }
            }
        });
    }
}