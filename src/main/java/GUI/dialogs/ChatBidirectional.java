package GUI.dialogs;

import com.google.firebase.database.*;
import firebase.Employee;
import firebase.Report;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Semaphore;

public class ChatBidirectional extends JFrame {
    //  JPanel della chat
    private final JPanel chatPanel = new JPanel();

    //  JTextPane, contiene il testo dove viene letta la chat.
    private final JTextPane chatArea = new JTextPane();

    //  JTextField dove viene immesso il messaggio da spedire al destinatario.
    private final JTextField textToSend = new JTextField();

    //  Bottone per inviare il messaggio composto nella JTextField
    private final JButton sendButton = new JButton("Invia");

    //  Strumenti che permettono di effettuare lo scrolling dei messaggi quando si supera una certa soglia.
    private final JScrollPane scrollPane = new JScrollPane();
    private JScrollBar verticalScrollBar = new JScrollBar();

    //  Componenti del messaggio
    private String fullname;    //  Nome
    private String surname;     //  Cognome
    private StringBuilder chatString = new StringBuilder(); //  Serve per prelevare i messaggi letti dal DataBase

    //  Variabile contenente gli indici del messaggio
    private int msgIndex = 0;

    //  Singolo report
    private final Report report;

    //  Impiegato, verrà utilizzato per comporre il messaggio
    private final Employee employee;

    //  Istanza di FirebaseDatabase
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    //  Riferimento al nodo "users" nel RTD.
    private DatabaseReference usersReference = firebaseDatabase.getReference("users");

    //  Riferimento al nodo "reports" nel RTD.
    private DatabaseReference chatReference = firebaseDatabase.getReference("reports");

    //  Semafori utili per gestire i metodi asincroni.
    private Semaphore semaphore1, chatSemaphore;

    /***
     * Costruttore, preleva le informazioni relative all'utente con il metodo retrieveUserData(), successivamente preleva
     * le informazione per la chat con il metodo retrieveChatData() ed in fine inizializza il frame con la funzione initializeFrame().
     * E si mette la JTextField per la composizione del messaggio in ascolto per la pressione del tasto invio, questo per mettere l'invio del messaggio
     * anche senza il click del bottone invia.
     *
     * @param _report
     * @param _employee
     */
    public ChatBidirectional(Report _report, Employee _employee) {
        this.report = _report;
        this.employee = _employee;

        retrieveUserFullnameAndSurname();
        retrieveChatData();
        initializeFrame();

        textToSend.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    sendingText();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        sendButton.addActionListener(e -> sendingText());
    }

    /***
     * Funzione che effettua l'invio del messaggio sul Real Time Database, questo solo nel caso in cui sia presente del testo
     * nella JTextField e che il semaforo relativo alla chat abbia finito, quindi che i dati testuali da leggere siano
     * stati effettivamente caricati nella chatArea.
     */
    private void sendingText() {
        if (!textToSend.getText().isEmpty()) {
            try {
                chatSemaphore.acquire();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

            final String rawMsg = textToSend.getText();
            msgIndex++;
            final String keyMsg = "msg_" + msgIndex;
            final String msgToSend = employee.getFullname() + " " + employee.getSurname() + " : " + rawMsg;

            DatabaseReference dbrChat = firebaseDatabase.getReference("reports").child(report.getId()).child("discussion").child(keyMsg);
            dbrChat.setValueAsync(msgToSend);
            msgIndex++;

            textToSend.setText("");
        }
    }

    /***
     * Metodo ausiliario che preleva tutti i messaggi della conversione (nel caso fossero avvenuti in qualche conversazione precedente) e li
     * carica nell'oggetto chatArea.
     */
    private void retrieveChatData() {
        chatSemaphore = new Semaphore(1);
        chatReference = chatReference.child(report.getId()).child("discussion");

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatString = new StringBuilder();
                msgIndex = (int) dataSnapshot.getChildrenCount();


                for (int i = 1; i <= msgIndex; i++) {
                    chatString.append(dataSnapshot.child("msg_" + i).getValue().toString()).append("\n");
                }

                chatArea.revalidate();
                chatArea.repaint();
                chatArea.setText(String.valueOf(chatString));

                scrollPane.revalidate();
                verticalScrollBar.revalidate();

                scrollPane.repaint();
                verticalScrollBar.repaint();

                verticalScrollBar = scrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getMaximum());

                chatSemaphore.release();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /***
     * Metodo ausiliario che preleva il nome e cognome dell'utente con cui si comunicherà.
     */
    void retrieveUserFullnameAndSurname() {
        semaphore1 = new Semaphore(0);
        usersReference = usersReference.child(report.getUid());

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fullname = dataSnapshot.child("fullname").getValue().toString();
                surname = dataSnapshot.child("surname").getValue().toString();
                semaphore1.release();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    /***
     * Metodo ausiliario che inizializza il frame dopo che si è atteso il termine delle funzioni ausiliarie per il prelevamento
     * dei dati utente e della chat.
     */
    void initializeFrame() {
        try {
            semaphore1.acquire();
            chatSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loadElementsOnPanel();

        this.setTitle("Report : " + report.getId() + ", from : " + fullname + " " + surname);
        this.setSize(490, 250);
        this.setVisible(true);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /***
     * Funzione ausiliaria che effettua il caricamento degli oggetti necessari (cone le opportune configurazioni) per
     * il funzionamento della chat sul pannello "chatPanel".
     */
    private void loadElementsOnPanel() {
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));

        textToSend.setMaximumSize(new Dimension(400, 1));
        textToSend.setSize(250, 1);
        SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(simpleAttributeSet, Color.BLACK);

        chatArea.setMinimumSize(new Dimension(300, 100));
        chatArea.setEditable(false);
        chatArea.setText(String.valueOf(chatString));

        JScrollPane scrollPane = new JScrollPane(chatArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        verticalScrollBar.setValue(verticalScrollBar.getMaximum());

        scrollPane.setVerticalScrollBar(verticalScrollBar);
        scrollPane.setSize(new Dimension(300, 100));

        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
        textToSend.setAlignmentX(Component.CENTER_ALIGNMENT);
        sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        chatPanel.add(scrollPane);
        chatPanel.add(textToSend);
        chatPanel.add(sendButton);

        getContentPane().add(chatPanel);
    }

    /***
     * Metodo che restituisce la stringa contenente l'ID relativo alla segnalazione per la quale è stata instanziata una chat.
     * @return
     */
    public String getReportID() {
        return report.getId();
    }
}