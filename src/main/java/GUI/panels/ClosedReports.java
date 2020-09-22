package GUI.panels;

import com.google.firebase.database.*;
import utils.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class ClosedReports extends JPanel {
    private JSplitPane splitPane = new JSplitPane();
    private final JPanel rightComponentPane = new JPanel();
    private final JLabel closedReportlabelInfo = new JLabel();
    private static JList<String> jListClosedReports;
    private static Report singleClosedReport;
    private static Vector<String> closedReportsIDs;

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    private final JButton reopenReportBtn = new JButton("Riapri segnalazione");

    private int lastSelectedIndex;
    private int lastIndexPriorityColour;

    public ClosedReports() {
        
    }

    public void updateClosedReportsPanel(Vector<String> newClosedReportsIDs) {
        jListClosedReports = new JList<>(newClosedReportsIDs);
        jListClosedReports.setSelectedIndex(lastSelectedIndex);

        switch (lastIndexPriorityColour) {
            case 0:
                jListClosedReports.setSelectionBackground(Color.GREEN);
                break;
            case 1:
                jListClosedReports.setSelectionBackground(Color.YELLOW);
                break;
            case 2:
                jListClosedReports.setSelectionBackground(Color.RED);
                break;
            default:
                jListClosedReports.setSelectionBackground(Color.GRAY);
        }

        this.remove(splitPane.getLeftComponent());
        this.remove(rightComponentPane);

        mySelection();

        rightComponentPane.setLayout(new FlowLayout());
        splitPane.setRightComponent(rightComponentPane);
        splitPane.setLeftComponent(jListClosedReports);
        splitPane.setDividerLocation(180);

        handleReopenButton();
        this.revalidate();
        this.repaint();
    }

    public void loadClosedReportsPanel(Vector<String> t_closedReportsIDs) {
        closedReportsIDs = t_closedReportsIDs;

        jListClosedReports = new JList<>(t_closedReportsIDs);
        jListClosedReports.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightComponentPane.setLayout(new BoxLayout(rightComponentPane, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(jListClosedReports);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setContinuousLayout(false);
        splitPane.setLeftComponent(jScrollPane);
        splitPane.setRightComponent(rightComponentPane);
        splitPane.setPreferredSize(new Dimension(750, 520));
        splitPane.setDividerLocation(180);
        splitPane.setOneTouchExpandable(true);

        this.setPreferredSize(new Dimension(800, 600));
        this.add(splitPane);
    }

    public void mySelection( ) {

        jListClosedReports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                final String reportID = jListClosedReports.getSelectedValue();

                Semaphore semaphore = new Semaphore(1);
                closedReportlabelInfo.setText("");
                singleClosedReport = null;
                rightComponentPane.remove(reopenReportBtn);
                rightComponentPane.remove(closedReportlabelInfo);
                rightComponentPane.revalidate();
                rightComponentPane.repaint();
                lastSelectedIndex = jListClosedReports.getSelectedIndex();

                databaseReference
                        .child(reportID)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    semaphore.acquire();
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
                                final String status = dataSnapshot.child("status").getValue().toString().split("_")[0];
                                final String social = dataSnapshot.child("social").getValue().toString();

                                singleClosedReport = new Report(
                                        reportID,
                                        uid,
                                        object,
                                        description,
                                        date,
                                        time,
                                        type,
                                        position,
                                        priority,
                                        "Chiusa_" + reportID,
                                        Boolean.parseBoolean(social));

                                switch (priority) {
                                    case "0":
                                        jListClosedReports.setSelectionBackground(Color.GREEN);
                                        lastIndexPriorityColour = 0;
                                        break;
                                    case "1":
                                        jListClosedReports.setSelectionBackground(Color.YELLOW);
                                        lastIndexPriorityColour = 1;
                                        break;
                                    case "2":
                                        jListClosedReports.setSelectionBackground(Color.RED);
                                        lastIndexPriorityColour = 2;
                                        break;
                                    default:
                                        jListClosedReports.setSelectionBackground(Color.GRAY);
                                        break;
                                }

                                String textOfTheLabel = "<html>" +
                                        "Oggetto : " + object + "<br><br>" +
                                        "Data : " + date + "<br><br>" +
                                        "Ora : " + time + "<br><br>" +
                                        "UID : " + uid + "<br><br>" +
                                        "Tipologia : " + type + "<br><br>" +
                                        "Descrizione : " + description + "<br><br>" +
                                        "Coordinate : " + position + "<br><br>" +
                                        "Priorità : " + priority + "<br><br>" +
                                        "Diffusione su social : " + social + "<br><br>" +
                                        "Status : " + status + "<br><br>";

                                if (dataSnapshot.child("request").getValue() != null) {
                                    textOfTheLabel += "Request : " + dataSnapshot.child("request").getValue().toString() + "<br><br>";
                                    reopenReportBtn.setLayout(new FlowLayout());
                                    rightComponentPane.add(reopenReportBtn);
                                }

                                textOfTheLabel += "</html>";
                                closedReportlabelInfo.setText(textOfTheLabel);
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
                handleReopenButton();

                rightComponentPane.add(closedReportlabelInfo);
                rightComponentPane.revalidate();
                rightComponentPane.repaint();

                semaphore.release();
            }
        });

    }

    public void handleReopenButton() {
        reopenReportBtn.addActionListener(e1 -> {
            if (singleClosedReport != null) {
                reopenReport();
                closedReportsIDs.remove(singleClosedReport.getId());
                singleClosedReport = null;

                rightComponentPane.remove(reopenReportBtn);
                rightComponentPane.remove(closedReportlabelInfo);
                closedReportlabelInfo.setText("");
                rightComponentPane.removeAll();
                rightComponentPane.revalidate();
                rightComponentPane.repaint();
            }
        });
    }

    private void reopenReport() {
        Semaphore semaphore = new Semaphore(1);
        {
            try {
                semaphore.acquire(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            databaseReference
                    .child(singleClosedReport.getId())
                    .child("status")
                    .setValueAsync("Aperta_" + singleClosedReport.getUid());

            databaseReference
                    .child(singleClosedReport.getId())
                    .child("request")
                    .setValueAsync(null);
            semaphore.release(1);
        }

        try {
            semaphore.acquire(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release(1);
        }

    }

}