package GUI.panels;

import GUI.dialogs.ChatBidirectional;
import com.google.firebase.database.*;
import firebase.Employee;
import firebase.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Semaphore;

@SuppressWarnings("JavaDoc")
public class OpenReportsPanel extends JPanel {
    //  SplitPane, nella parte sinistra è presente una JList contenente gli id dei record, nella parte destrea le loro informazioni
    private static JSplitPane openSplitPane = new JSplitPane();
    private static JList<String> jListOpenReports;              //  SX
    private final JPanel rightComponentPane = new JPanel();     //  DX

    //  Label che vengono aggiornate sulla parte destra dello split pane.
    private final JLabel openReportlabelInfo = new JLabel();
    private final JLabel attachmentImgLink = new JLabel();

    //  Radio buttons per scegliere le operazioni da effettuare sulla segnalazione.
    private final JRadioButton pendingRadioBtn = new JRadioButton("Pending");
    private final JRadioButton closedRadioBtn = new JRadioButton("Chiusa");
    private final JButton submitStatusBtn = new JButton("Imposta");

    //  Riferimento al nodo del database "reports"
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("reports");

    //  Singolo report che viene compilato per effettuare le operazioni
    private static Report singleOpenReport;

    //  Istanza dell'impiegato, viene utilizzata per aprire una chat con l'utente.
    private Employee employee;

    //  Ultimo indice selezionato della JList
    private int lastSelectedIndex;

    //  Ultimo colore relativo all'ID dell'ultimo report selzionato nella JList.
    private int lastIndexPriorityColour;

    /***
     * Costruttore che genera il layout, i RadioButtons vengono unificati sotto un univo ButtonGroup.
     */
    public OpenReportsPanel() {
        rightComponentPane.setLayout(new BoxLayout(rightComponentPane, BoxLayout.Y_AXIS));
        ButtonGroup statusButtons = new ButtonGroup();
        statusButtons.add(pendingRadioBtn);
        statusButtons.add(closedRadioBtn);
    }

    /***
     * Metodo che si occupa di aggiornare il JSplitPane nella sua parte destra e sinistra, in ingresso viene fornito il
     * vettore contenente i report con status "Aperta", questa funzione viene continuamente chiamata nel Runnable-> dynamicPanels
     * del ControlPanel.java.
     * @param new_openReports
     * @param chatInstances
     */
    public void updateOpenReportsPanel(Vector<String> new_openReports,
                                       Vector<ChatBidirectional> chatInstances) {
        jListOpenReports = new JList<>(new_openReports);
        jListOpenReports.setSelectedIndex(lastSelectedIndex);

        setLastIndexItemBackground();

        this.remove(openSplitPane.getLeftComponent());
        this.remove(rightComponentPane);

        openSplitPane.setRightComponent(rightComponentPane);
        openSplitPane.setLeftComponent(jListOpenReports);
        openSplitPane.setDividerLocation(180);

        this.revalidate();
        this.repaint();

        mySelection(new_openReports, chatInstances, employee);
    }

    /***
     * Metodo che da un primo avvio (manuale) al pannello.
     * @param openReportIDs
     * @param chatInstances
     * @param employee
     */
    public void loadOpenReportsPanel(Vector<String> openReportIDs,
                                     Vector<ChatBidirectional> chatInstances,
                                     Employee employee) {
        this.employee = employee;
        jListOpenReports = new JList<>(openReportIDs);

        jListOpenReports.setAlignmentX(Component.LEFT_ALIGNMENT);


        mySelection(openReportIDs, chatInstances, employee);
        setSplitPane();

        this.setPreferredSize(new Dimension(800, 600));
        this.add(openSplitPane);
    }

    /***
     * Questo metodo si occupa sia di scaricare tutte le informazioni del report e di distribuirle sulla parte destra del
     * JSplitPane e fornire la possibilità di svolgere delle operazioni di amministrazione sui report (tramite i bottoni).
     * Ma anche di aprire una chat in RT con l'utente che ha effettuato la segnalazione.
     *
     * Le due operazioni sono differenziate dal numero di click, 1 click sull'elemento dell JList mostrerò a video le informazioni
     * relative alla segnalazione, 2-clock sull'elemento della JList permetteranno di aprire una finestra a parte per chattare con l'utente
     * o consegnargli un messaggio.
     * @param openReportIDs
     * @param chatInstances
     * @param employee
     */
    private void mySelection(Vector<String> openReportIDs,
                             Vector<ChatBidirectional> chatInstances,
                             Employee employee) {
        jListOpenReports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final String reportId = jListOpenReports.getSelectedValue();

                if (e.getClickCount() == 1 && reportId != null) {
                    Semaphore semaphore = new Semaphore(1);
                    clearAndRepaintAllComponents();
                    lastSelectedIndex = jListOpenReports.getSelectedIndex();

                    databaseReference.child(reportId).addValueEventListener(new ValueEventListener() {
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

                            singleOpenReport = new Report(reportId, uid, object, description, date, time, type, position, priority, "Aperta_" + reportId, Boolean.parseBoolean(social));
                            saveIndexAndSetColor(priority);

                            openReportlabelInfo.setText("<html>" +
                                    "Oggetto : " + object + "<br>" +
                                    "Data : " + date + "<br>" +
                                    "Ora : " + time + "<br>" +
                                    "UID : " + uid + "<br>" +
                                    "Tipologia : " + type + "<br>" +
                                    "Descrizione : " + description + "<br>" +
                                    "Coordinate : " + position + "<br>" +
                                    "Priorità : " + priority + "<br>" +
                                    "Diffusione su social : " + social + "<br>" +
                                    "Status : " + status + "<br></html>");

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

                    submitButtonListener(openReportIDs);

                    rightComponentPane.add(openReportlabelInfo);
                    rightComponentPane.add(attachmentImgLink);
                    rightComponentPane.add(pendingRadioBtn);
                    rightComponentPane.add(closedRadioBtn);
                    rightComponentPane.add(submitStatusBtn);

                    semaphore.release();
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

    /***
     * Metodo che gestisce il bottone di Submit, questo bottone cambia lo status della segnalazione in base a quale RadioButton è stato
     * selezionato, non sono presenti altre operazioni di default.
     * @param openReportIDs
     */
    private void submitButtonListener(Vector<String> openReportIDs) {
        submitStatusBtn.addActionListener(e1 -> {
            if (pendingRadioBtn.isSelected()) {
                singleOpenReport.setStatus("Pending_" + singleOpenReport.getUid());
                databaseReference.child(singleOpenReport.getId()).child("status").setValueAsync(singleOpenReport.getStatus());
            } else if (closedRadioBtn.isSelected()) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                String dateFormatted = formatter.format(date);

                singleOpenReport.setDataChiusura(dateFormatted);
                singleOpenReport.setStatus("Chiusa_" + singleOpenReport.getUid());
                databaseReference.child(singleOpenReport.getId()).child("status").setValueAsync(singleOpenReport.getStatus());
                databaseReference.child(singleOpenReport.getId()).child("data_chiusura").setValueAsync(singleOpenReport.getDataChiusura());
            }
            openReportIDs.remove(singleOpenReport.getId());

            clearAndRepaintAllComponents();
        });
    }

    /***
     * Metodo utilitario che pulisce le varie JLabels e JButtons della parte destra del JSplitPane, "rightComponentPane".
     */
    private void clearAndRepaintAllComponents() {
        openReportlabelInfo.setText("");
        rightComponentPane.remove(openReportlabelInfo);

        attachmentImgLink.setText("");
        rightComponentPane.remove(attachmentImgLink);
        rightComponentPane.remove(pendingRadioBtn);
        rightComponentPane.remove(closedRadioBtn);
        rightComponentPane.remove(submitStatusBtn);

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
     * Metodo utilitario, imposta il background dell'item selezionato in base alla priorità della relativa segnalazione e ne
     * effettua il salvataggio dell'ultimo colore selezionato sulla variabile "lastIndexPriorityColour".
     * @param priority
     */
    private void saveIndexAndSetColor(String priority) {
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
    }

    /***
     * Metodo utilitario, lo si utilizza nella funzione di aggiornamento per mantenere il colore dell'elemento selezionato
     * (precedentemente, perchè la funzione continua a riaggiornare).
     */
    private void setLastIndexItemBackground() {
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
    }

    /***
     * Metodo utilitario per impostare le configurazioni dello SplitPane.
     */
    private void setSplitPane() {
        JScrollPane jScrollPane = new JScrollPane(jListOpenReports);
        openSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        openSplitPane.setContinuousLayout(false);
        openSplitPane.setLeftComponent(jScrollPane);
        openSplitPane.setRightComponent(rightComponentPane);
        openSplitPane.setPreferredSize(new Dimension(750, 520));
        openSplitPane.setDividerLocation(180);
        openSplitPane.setOneTouchExpandable(true);
    }
}