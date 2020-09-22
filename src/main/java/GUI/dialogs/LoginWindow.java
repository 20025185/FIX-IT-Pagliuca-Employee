package GUI.dialogs;

import GUI.ControlPanel;
import com.google.firebase.database.*;
import utils.Employee;
import utils.FirebaseAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class LoginWindow extends JFrame implements ActionListener {
    private final Container container = getContentPane();
    private final JLabel emailLabel = new JLabel("e-mail : ");
    private final JLabel pswLabel = new JLabel("Password : ");
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("LOGIN");
    private boolean isEmployee = false;
    private Employee employee;

    public LoginWindow() {
        initialize();
        setLayoutManager();
        setLocationAndSize();
        addComponents();
        addActionEvent();
    }

    private void initialize() {
        this.setTitle("LOGIN");
        this.setIconImage(new ImageIcon("src\\lemon-icon.png").getImage());
        this.setSize(300, 180);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        emailField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                final int key = e.getKeyCode();

                if (key == KeyEvent.VK_ENTER) {
                    try {
                        login();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        passwordField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                final int key = e.getKeyCode();

                if (key == KeyEvent.VK_ENTER) {
                    try {
                        login();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    public void login() throws IOException {
        final String email = "pagliuca.manuel@gmail.com";// getEmail();
        final String psw = "pirletto22";// getPsw();
        FirebaseAPI firebaseAPI = new FirebaseAPI();

        if (!email.isEmpty() && !psw.isEmpty()) {
            try {
                employee = firebaseAPI.signInWithPassword(email, psw);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            CountDownLatch countDownLatch = new CountDownLatch(1);

            if (!employee.getUID().isEmpty()) {
                DatabaseReference dbr = FirebaseDatabase
                        .getInstance()
                        .getReference("employee");

                dbr.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        isEmployee = dataSnapshot
                                .child(employee.getUID())
                                .getValue() != null;
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (isEmployee) {
                    new ControlPanel(employee);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "Non sono state inserite le credenziali corrette.",
                            "Credenziali non valide",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }

    public void setLayoutManager() {
        container.setLayout(null);
    }

    public void setLocationAndSize() {
        emailLabel.setBounds(30, 10, 50, 30);
        emailField.setBounds(90, 10, 150, 30);
        pswLabel.setBounds(20, 50, 100, 30);
        passwordField.setBounds(90, 50, 150, 30);
        loginButton.setBounds(105, 100, 100, 30);
    }

    public void addComponents() {
        container.add(emailLabel);
        container.add(emailField);
        container.add(pswLabel);
        container.add(passwordField);
        container.add(loginButton);
    }

    private void addActionEvent() {
        loginButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            try {
                login();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private String getEmail() {
        return emailField.getText();
    }

    private String getPsw() {
        return String.valueOf(passwordField.getPassword());
    }

}