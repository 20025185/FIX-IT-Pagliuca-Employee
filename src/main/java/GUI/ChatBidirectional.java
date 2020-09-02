package GUI;

import com.google.firebase.database.*;
import utils.Employee;
import utils.Report;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Semaphore;

public class ChatBidirectional extends JFrame {
    //  Elements
    private final JPanel chatPanel = new JPanel();
    private final JTextPane chatArea = new JTextPane();
    private final JTextField textToSend = new JTextField();
    private final JButton sendButton = new JButton("Invia");

    //  Message Components
    private String fullname;
    private String surname;
    private final Report report;
    private final Employee employee;
    private final StringBuilder chatString = new StringBuilder();
    //private String chatString = "";
    private int msgIndex = 0;

    //  Firebase
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference usersReference = firebaseDatabase.getReference("users");
    private DatabaseReference chatReference = firebaseDatabase.getReference("reports");

    //  Utils
    private Semaphore semaphore1, semaphore2;

    ChatBidirectional(Report _report, Employee _employee) {
        this.report = _report;
        this.employee = _employee;
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));

        retrieveUserData();
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

    private void sendingText() {
        if (!textToSend.getText().isEmpty()) {
            try {
                semaphore2.acquire();
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

    private void retrieveChatData() {
        semaphore2 = new Semaphore(1);
        chatReference = chatReference.child(report.getId()).child("discussion");

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    chatString.append(d.getValue().toString()).append("\n");
                    //chatString += d.getValue().toString() + "\n";
                }
                msgIndex = (int) dataSnapshot.getChildrenCount();
                semaphore2.release();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void retrieveUserData() {
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

    void initializeFrame() {
        try {
            semaphore1.acquire();
            semaphore2.acquire();
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
        this.add(chatPanel);
    }

    private void loadElementsOnPanel() {
        chatArea.setMinimumSize(new Dimension(300, 100));
        chatArea.setEditable(false);
        chatArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        chatArea.setText(String.valueOf(chatString));

        textToSend.setMaximumSize(new Dimension(400, 1));
        textToSend.setSize(250, 1);
        textToSend.setAlignmentX(Component.CENTER_ALIGNMENT);

        sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(simpleAttributeSet, Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(chatArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setSize(new Dimension(300, 100));

        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(textToSend);
        chatPanel.add(sendButton);
    }

    public String getReportID(){
        return report.getId();
    }
}
