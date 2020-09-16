package GUI.dialogs;

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
    private final JScrollPane scrollPane = new JScrollPane();
    private JScrollBar verticalScrollBar = new JScrollBar();

    //  Message Components
    private String fullname;
    private String surname;
    private final Report report;
    private final Employee employee;
    private StringBuilder chatString = new StringBuilder();
    private int msgIndex = 0;

    //  Firebase
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference usersReference = firebaseDatabase.getReference("users");
    private DatabaseReference chatReference = firebaseDatabase.getReference("reports");

    //  Utils
    private Semaphore semaphore1, semaphore2;

    public ChatBidirectional(Report _report, Employee _employee) {
        this.report = _report;
        this.employee = _employee;

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
    }

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

    public String getReportID() {
        return report.getId();
    }
}
