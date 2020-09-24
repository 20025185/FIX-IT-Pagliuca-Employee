package GUI.dialogs;

import GUI.ControlPanel;
import firebase.Employee;
import firebase.FirebaseAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class LoginWindow extends JFrame implements ActionListener {
    private final Container container = getContentPane();
    private final JLabel emailLabel = new JLabel("e-mail : ");
    private final JLabel pswLabel = new JLabel("Password : ");
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("LOGIN");
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
        this.setIconImage(new ImageIcon("src\\imgs\\icons\\lemon-icon.png").getImage());
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
        final String email = getEmail();
        final String psw = getPsw();
        FirebaseAPI firebaseAPI = new FirebaseAPI();

        if (!email.isEmpty() && !psw.isEmpty()) {
            try {
                employee = firebaseAPI.signInWithPassword(email, psw);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            if (employee != null) {
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