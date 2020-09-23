package GUI.panels;

import GUI.dialogs.EditReportFrame;
import com.google.firebase.database.*;
import utils.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class PendingReportsPanel extends JPanel {
    private final DatabaseReference databaseReference;
    private JSplitPane pendingSplitPane = new JSplitPane();

    private final JButton editBtn = new JButton("Edit");
    private final JButton openStatusBtn = new JButton("Apri Segnalazione");

    //  Label
    private final JLabel attachmentImgLink = new JLabel();
    private final JLabel labelReportInfo = new JLabel();

    private final JPanel rightPanel = new JPanel();
    private static Report singlePendingReport;
    private static Vector<String> pendingReportIDs;
    private static JList<String> stringJList;
    private int lastSelectedIndex;
    private int lastIndexPriorityColour;

    public PendingReportsPanel(DatabaseReference ref) {
        this.databaseReference = ref;

        labelReportInfo.setHorizontalAlignment(SwingConstants.LEFT);
        labelReportInfo.setVerticalAlignment(SwingConstants.TOP);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        this.setPreferredSize(new Dimension(800, 600));

        handlePendingReportsButtons();
    }

    public void updatePendingReportsPanel(Vector<String> t_newReports) {
        stringJList = new JList<>(t_newReports);
        stringJList.setSelectedIndex(lastSelectedIndex);

        setLastIndexItemBackground();

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
        pendingSplitPane.setRightComponent(rightPanel);
        pendingSplitPane.setDividerLocation(180);
        pendingSplitPane.setOneTouchExpandable(true);
        pendingSplitPane.setPreferredSize(new Dimension(750, 520));

        this.add(pendingSplitPane);
    }

    private void handlePendingReportsButtons() {
        editBtn.addActionListener(e -> {
            new EditReportFrame(singlePendingReport);

            if (singlePendingReport.getStatus().equals("Aperta") || singlePendingReport.getStatus().equals("Chiusa")) {
                pendingReportIDs.remove(singlePendingReport.getId());
            }

            rightPanel.removeAll();
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
                    final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("reports").child(stringJList.getSelectedValue());
                    Semaphore semaphore = new Semaphore(1);
                    lastSelectedIndex = stringJList.getSelectedIndex();

                    clearAndRepaintAllComponents();


                    dbr.addValueEventListener(new ValueEventListener() {
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
                            final String status = dataSnapshot.child("status").getValue().toString();
                            final String social = dataSnapshot.child("social").getValue().toString();
                            final String attachImg = dataSnapshot.child("attachmentPath").getValue().toString();

                            singlePendingReport = new Report(reportID, uid, object, description, date, time, type, position, priority, status, Boolean.parseBoolean(social));
                            saveIndexAndSetColor(priority);

                            labelReportInfo.setText("<html>" +
                                    "Oggetto : " + object + "<br>" +
                                    "Data : " + date + "<br>" +
                                    "Ora : " + time + "<br>" +
                                    "UID : " + uid + "<br>" +
                                    "Tipologia : " + type + "<br>" +
                                    "Descrizione : " + description + "<br>" +
                                    "Coordinate : " + position + "<br>" +
                                    "Priorità : " + priority + "<br>" +
                                    "Diffusione su social : " + social + "<br>" +
                                    "Status : Pending <br>" + "</html>");

                            if (!attachImg.isEmpty())
                                setImgLabel(attachImg);

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

                    rightPanel.add(labelReportInfo);
                    rightPanel.add(attachmentImgLink);
                    rightPanel.add(editBtn);
                    rightPanel.add(openStatusBtn);

                    semaphore.release();
                }
            }
        });
    }

    private void saveIndexAndSetColor(String priority) {
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
    }

    private void clearAndRepaintAllComponents() {
        labelReportInfo.setText("");
        rightPanel.remove(labelReportInfo);

        attachmentImgLink.setText("");
        rightPanel.remove(attachmentImgLink);

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void setImgLabel(String attachImg) {
        if (attachImg != null) {
            attachmentImgLink.setText("<html><a href=\" " + attachImg + "\">Allegato</a><br><br></html>");
            attachmentImgLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
            attachmentImgLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    try {
                        Desktop.getDesktop().browse(new URI(attachImg));
                    } catch (URISyntaxException | IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private void setLastIndexItemBackground() {
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
    }

}