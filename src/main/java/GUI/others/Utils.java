package GUI.others;

import GUI.ControlPanel;
import GUI.dialogs.ChatBidirectional;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class Utils {

    public void initMenu(CardLayout cardLayout, Container container, ControlPanel controlPanel) {
        JMenuBar jMenuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu reportMenu = new JMenu("Segnalazioni");

        JMenuItem profileItem = new JMenuItem(new AbstractAction("Profile") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "0");
                controlPanel.setCardShowed(0);
            }
        });

        JMenuItem handlingReports = new JMenuItem(new AbstractAction("Pending List") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "1");
                controlPanel.setCardShowed(1);
            }
        });

        JMenuItem openReports = new JMenuItem(new AbstractAction("Aperte") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "2");
                controlPanel.setCardShowed(2);
            }
        });

        JMenuItem closedReports = new JMenuItem(new AbstractAction("Chiuse") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "3");
                controlPanel.setCardShowed(3);
            }
        });

        JMenu statsMenu = new JMenu("Statistiche");

        JMenuItem statReviewsStream = new JMenuItem(new AbstractAction("Recensioni in streaming") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "4");
                controlPanel.setCardShowed(4);
            }
        });

        JMenuItem statReports = new JMenuItem(new AbstractAction("Segnalazioni") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "5");
                controlPanel.setCardShowed(5);
            }
        });

        JMenuItem sendReport = new JMenuItem(new AbstractAction("Invia segnalazione") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(container, "6");
                controlPanel.setCardShowed(6);
            }
        });

        JMenuItem exitItem = new JMenuItem(new AbstractAction("Esci") {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlPanel.dispose();
                System.exit(1);
            }
        });

        statsMenu.add(statReviewsStream);
        statsMenu.add(statReports);

        reportMenu.add(handlingReports);
        reportMenu.add(openReports);
        reportMenu.add(closedReports);
        reportMenu.add(statsMenu);
        reportMenu.add(sendReport);

        fileMenu.add(profileItem);
        fileMenu.add(exitItem);

        jMenuBar.add(fileMenu);
        jMenuBar.add(reportMenu);

        controlPanel.setJMenuBar(jMenuBar);

    }

    public void initialize(ControlPanel controlPanel) {
        controlPanel.setTitle("CONTROL PANEL v0.0.2");
        controlPanel.setSize(800, 600);
        controlPanel.setLocationRelativeTo(null);
        controlPanel.setVisible(true);
        controlPanel.setResizable(false);
        controlPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void retrieveReportsIDs(Vector<String> pendingReportIDs,
                                   Vector<String> openReportIDs,
                                   Vector<String> closedReportIDs,
                                   DatabaseReference databaseReference,
                                   Semaphore semaphore,
                                   Vector<ChatBidirectional> chatInstances,
                                   ControlPanel controlPanel) {
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
                    controlPanel.requestFocus();
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

    public void setLayoutManager(Container container, CardLayout cardLayout, JPanel profilePanel, JPanel pendingReportPanel,
                                 JPanel openReportPanel,
                                 JPanel closedReportPanel,
                                 JPanel statsReviewsPanel,
                                 JPanel statsReportsPanel,
                                 JPanel createReportPanel,
                                 ControlPanel controlPanel) {
        container.setLayout(cardLayout);
        container.add(profilePanel, "0");
        container.add(pendingReportPanel, "1");
        container.add(openReportPanel, "2");
        container.add(closedReportPanel, "3");
        container.add(statsReviewsPanel, "4");
        container.add(statsReportsPanel, "5");
        container.add(createReportPanel, "6");
        cardLayout.show(container, "0");
        controlPanel.setCardShowed(0);
    }

}