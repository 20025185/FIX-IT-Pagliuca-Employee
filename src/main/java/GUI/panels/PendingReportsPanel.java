package GUI.panels;

import GUI.dialogs.EditReportFrame;
import com.google.firebase.database.*;
import firebase.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.concurrent.Semaphore;

@SuppressWarnings("Javadoc")
public class PendingReportsPanel extends JPanel {
    //  SplitPane, nella parte sinistra è presente una JList contenente gli id dei record, nella parte destrea le loro informazioni
    private static JSplitPane pendingSplitPane = new JSplitPane();
    private static JList<String> jListPendingReports;           //  SX
    private final JPanel rightComponentPane = new JPanel();     //  DX

    //  Bottoni che permetteno di effettuare la modifica della segnalazione (aprendo un dialog) o di aprire direttamente la segnalazione
    private final JButton editBtn = new JButton("Edit");
    private final JButton openStatusBtn = new JButton("Apri Segnalazione");

    //  Label che vengono aggiornate sulla parte destra dello split pane.
    private final JLabel attachmentImgLink = new JLabel();
    private final JLabel labelReportInfo = new JLabel();

    //  Riferimento al nodo del database "reports"
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    //  Singolo report che viene compilato per effettuare le operazioni
    private static Report singlePendingReport;

    //  Vettore contenente gli ID dei report in attesa
    private static Vector<String> pendingReportIDs;

    //  Ultimo indice selezionato della JList
    private int lastSelectedIndex;

    //  Ultimo colore relativo all'ID dell'ultimo report selzionato nella JList.
    private int lastIndexPriorityColour;

    /***
     * Costruttore che genera il layout e chiama un metodo che gestisce i bottoni del pannello.
     */
    public PendingReportsPanel() {
        labelReportInfo.setHorizontalAlignment(SwingConstants.LEFT);
        labelReportInfo.setVerticalAlignment(SwingConstants.TOP);
        rightComponentPane.setLayout(new BoxLayout(rightComponentPane, BoxLayout.Y_AXIS));
        this.setPreferredSize(new Dimension(800, 600));
        handlePendingReportsButtons();
    }

    /***
     * Metodo che si occupa di aggiornare il JSplitPane nella sua parte destra e sinistra, in ingresso viene fornito il
     * vettore contenente i report con status "Aperta", questa funzione viene continuamente chiamata nel Runnable-> dynamicPanels
     * del ControlPanel.java.
     * @param t_newReports
     */
    public void updatePendingReportsPanel(Vector<String> t_newReports) {
        jListPendingReports = new JList<>(t_newReports);
        jListPendingReports.setSelectedIndex(lastSelectedIndex);

        setLastIndexItemBackground();

        this.remove(pendingSplitPane.getLeftComponent());

        pendingSplitPane.setLeftComponent(jListPendingReports);
        pendingSplitPane.setDividerLocation(180);

        this.revalidate();
        this.repaint();

        mySelection();
    }

    /***
     * Metodo che da un primo avvio (manuale) al pannello.
     * @param t_pendingReportIDs
     */
    public void loadPendingReportsPanel(Vector<String> t_pendingReportIDs) {
        pendingReportIDs = t_pendingReportIDs;
        jListPendingReports = new JList<>(pendingReportIDs);

        mySelection();

        JScrollPane jScrollPane = new JScrollPane(jListPendingReports);
        pendingSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pendingSplitPane.setContinuousLayout(false);
        pendingSplitPane.setLeftComponent(jScrollPane);
        pendingSplitPane.setRightComponent(rightComponentPane);
        pendingSplitPane.setDividerLocation(180);
        pendingSplitPane.setOneTouchExpandable(true);
        pendingSplitPane.setPreferredSize(new Dimension(750, 520));

        this.add(pendingSplitPane);
    }

    /***
     * Questo metodo si occupa sia di scaricare tutte le informazioni del report in attesa, e di distribuirle sulla parte destra del
     * JSplitPane e fornire la possibilità di svolgere delle operazioni di amministrazione sui report (tramite i bottoni).
     */
    private void mySelection() {
        jListPendingReports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (jListPendingReports.getSelectedValue() != null) {
                    final String reportID = jListPendingReports.getSelectedValue();
                    final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("reports").child(jListPendingReports.getSelectedValue());
                    Semaphore semaphore = new Semaphore(1);
                    lastSelectedIndex = jListPendingReports.getSelectedIndex();

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

                    rightComponentPane.add(labelReportInfo);
                    rightComponentPane.add(attachmentImgLink);
                    rightComponentPane.add(editBtn);
                    rightComponentPane.add(openStatusBtn);

                    semaphore.release();
                }
            }
        });
    }

    /***
     * Metodo contenente i listener dei bottoni presenti all'interno del programma.
     *
     * Edit button -> Apre un nuovo frame che permette la modifica di diversi campi importanti (spiegata nel dettagliio nella relativa classe).
     * Se questa modifica cambia lo status, l'ID del relativo report viene rimosso dal vettore dei report in attesa.
     * Alla fine si effettua una pulizia delle componenti disposte sullo SplitPane.
     */
    private void handlePendingReportsButtons() {
        editBtn.addActionListener(e -> {
            new EditReportFrame(singlePendingReport);

            if (singlePendingReport.getStatus().equals("Aperta") || singlePendingReport.getStatus().equals("Chiusa")) {
                pendingReportIDs.remove(singlePendingReport.getId());
            }

            rightComponentPane.removeAll();
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

    /***
     * Metodo utilitario, imposta il background dell'item selezionato in base alla priorità della relativa segnalazione e ne
     * effettua il salvataggio dell'ultimo colore selezionato sulla variabile "lastIndexPriorityColour".
     * @param priority
     */
    private void saveIndexAndSetColor(String priority) {
        switch (priority) {
            case "0":
                jListPendingReports.setSelectionBackground(Color.GREEN);
                lastIndexPriorityColour = 0;
                break;
            case "1":
                jListPendingReports.setSelectionBackground(Color.YELLOW);
                lastIndexPriorityColour = 1;
                break;
            case "2":
                jListPendingReports.setSelectionBackground(Color.RED);
                lastIndexPriorityColour = 2;
                break;
            default:
                jListPendingReports.setSelectionBackground(Color.GRAY);
                break;
        }
    }

    /***
     * Metodo utilitario, lo si utilizza nella funzione di aggiornamento per mantenere il colore dell'elemento selezionato
     * (precedentemente, perchè la funzione continua a riaggiornare).
     */
    private void clearAndRepaintAllComponents() {
        labelReportInfo.setText("");
        rightComponentPane.remove(labelReportInfo);

        attachmentImgLink.setText("");
        rightComponentPane.remove(attachmentImgLink);

        rightComponentPane.revalidate();
        rightComponentPane.repaint();
    }

    /***
     * Metodo utilitario che imposta, nel caso fosse presente l'URL, la label-link cliccabile con l'URL passato come parametro.
     * In questa maniera sarà possibile visualizzare gli allegati relativi al report selezionato in una finestra del browser predefinito.
     * @param attachImg
     */
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

    /***
     * Metodo utilitario, lo si utilizza nella funzione di aggiornamento per mantenere il colore dell'elemento selezionato
     * (precedentemente, perchè la funzione continua a riaggiornare).
     */
    private void setLastIndexItemBackground() {
        switch (lastIndexPriorityColour) {
            case 0:
                jListPendingReports.setSelectionBackground(Color.GREEN);
                break;
            case 1:
                jListPendingReports.setSelectionBackground(Color.YELLOW);
                break;
            case 2:
                jListPendingReports.setSelectionBackground(Color.RED);
                break;
            default:
                jListPendingReports.setSelectionBackground(Color.GRAY);
        }
    }
}