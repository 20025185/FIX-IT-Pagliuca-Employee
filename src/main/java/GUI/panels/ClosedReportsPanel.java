package GUI.panels;

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

public class ClosedReportsPanel extends JPanel {
    //  SplitPane, nella parte sinistra è presente una JList contenente gli id dei record, nella parte destrea le loro informazioni
    private JSplitPane splitPane = new JSplitPane();
    private static JList<String> jListClosedReports;        //  SX
    private final JPanel rightComponentPane = new JPanel(); //  DX

    //  Label che vengono aggiornate sulla parte destra dello split pane.
    private final JLabel closedReportlabelInfo = new JLabel();
    private final JLabel attachmentImgLink = new JLabel();

    //  Bottone che serve a riaprire i record che hanno fatto una richiesta di riapertura
    private final JButton reopenReportBtn = new JButton("Riapri segnalazione");

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    private static Vector<String> closedReportsIDs;
    private static Report singleClosedReport;
    private int lastSelectedIndex;
    private int lastIndexPriorityColour;

    public ClosedReportsPanel() {
        rightComponentPane.setLayout(new BoxLayout(rightComponentPane, BoxLayout.Y_AXIS));
        handleReopenButton();
    }

    public void updateClosedReportsPanel(Vector<String> newClosedReportsIDs) {
        jListClosedReports = new JList<>(newClosedReportsIDs);
        jListClosedReports.setSelectedIndex(lastSelectedIndex);

        setLastIndexItemBackground();

        this.remove(splitPane.getLeftComponent());
        this.remove(rightComponentPane);

        mySelection();

        setSplitPane();

        this.revalidate();
        this.repaint();
    }


    public void loadClosedReportsPanel(Vector<String> t_closedReportsIDs) {
        closedReportsIDs = t_closedReportsIDs;
        jListClosedReports = new JList<>(t_closedReportsIDs);
        jListClosedReports.setAlignmentX(Component.LEFT_ALIGNMENT);

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

    public void mySelection() {
        jListClosedReports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                final String reportID = jListClosedReports.getSelectedValue();

                Semaphore semaphore = new Semaphore(1);
                singleClosedReport = null;

                clearAndRepaintAllComponents();
                lastSelectedIndex = jListClosedReports.getSelectedIndex();

                databaseReference.child(reportID).addValueEventListener(new ValueEventListener() {
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
                        final String attachImg = dataSnapshot.child("attachmentPath").getValue().toString();

                        singleClosedReport = new Report(reportID, uid, object, description, date, time, type, position, priority, "Chiusa_" + reportID, Boolean.parseBoolean(social));
                        saveIndexAndSetColor(priority);

                        String textOfTheLabel = "<html>" +
                                "Oggetto : " + object + "<br>" +
                                "Data : " + date + "<br>" +
                                "Ora : " + time + "<br>" +
                                "UID : " + uid + "<br>" +
                                "Tipologia : " + type + "<br>" +
                                "Descrizione : " + description + "<br>" +
                                "Coordinate : " + position + "<br>" +
                                "Priorità : " + priority + "<br>" +
                                "Diffusione su social : " + social + "<br>" +
                                "Status : " + status + "<br>";

                        if (dataSnapshot.child("request").getValue() != null) {
                            textOfTheLabel += "Request : " + dataSnapshot.child("request").getValue().toString() + "<br>";
                            reopenReportBtn.setVisible(true);
                            rightComponentPane.add(reopenReportBtn);
                        }

                        setImgLabel(attachImg);
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

                rightComponentPane.add(closedReportlabelInfo);
                rightComponentPane.revalidate();
                rightComponentPane.repaint();

                rightComponentPane.add(attachmentImgLink);
                rightComponentPane.revalidate();
                rightComponentPane.repaint();
                semaphore.release();
            }
        });
    }

    private void clearAndRepaintAllComponents() {
        closedReportlabelInfo.setText("");
        rightComponentPane.remove(closedReportlabelInfo);

        attachmentImgLink.setText("");
        rightComponentPane.remove(attachmentImgLink);

        rightComponentPane.revalidate();
        rightComponentPane.repaint();

        reopenReportBtn.setVisible(false);
        rightComponentPane.remove(reopenReportBtn);

        rightComponentPane.revalidate();
        rightComponentPane.repaint();
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

    private void saveIndexAndSetColor(String priority) {
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
    }

    private void handleReopenButton() {
        reopenReportBtn.addActionListener(e1 -> {
            clearAndRepaintAllComponents();
            reopenReport();
            System.out.println(closedReportsIDs);
            System.out.println(singleClosedReport.getId());
            closedReportsIDs.remove(singleClosedReport.getId());
            singleClosedReport = null;
        });
    }

    private void setLastIndexItemBackground() {
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
    }

    private void reopenReport() {
        databaseReference.child(singleClosedReport.getId()).child("status").setValueAsync("Aperta_" + singleClosedReport.getUid());
        databaseReference.child(singleClosedReport.getId()).child("request").setValueAsync(null);
    }

    private void setSplitPane() {
        splitPane.setRightComponent(rightComponentPane);
        splitPane.setLeftComponent(jListClosedReports);
        splitPane.setDividerLocation(180);
    }
}