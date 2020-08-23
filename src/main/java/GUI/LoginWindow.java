package GUI;

import com.google.firebase.database.*;
import utils.Employee;
import utils.FirebaseAuthAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoginWindow extends JFrame implements ActionListener {
    private Container container = getContentPane();
    private JLabel emailLabel = new JLabel("e-mail : ");
    private JLabel pswLabel = new JLabel("Password : ");
    private JTextField emailField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JButton loginButton = new JButton("LOGIN");

    private static FirebaseAuthAPI firebaseAuthAPI;
    private static DatabaseReference dbr;
    private String uid;
    private boolean isEmployee = false;
    private static Employee employee;

    private static DashBoard dashBoard;

    public LoginWindow() {
        initialize();
        setLayoutManager();
        setLocationAndSize();
        addComponents();
        addActionEvent();
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

    private void initialize() {
        this.setTitle("[FIX-IT] - LOGIN EMPLOYEE");
        this.setSize(300, 200);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        passwordField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                final int key = e.getKeyCode();

                if (key == KeyEvent.VK_ENTER) {
                    login();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    private void addActionEvent() {
        loginButton.addActionListener(this);
    }

    private String getEmail() {
        return emailField.getText();
    }

    private String getPsw() {
        return passwordField.getText();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            login();
        }
    }

    public void login(){
        final String email = getEmail();
        final String psw = getPsw();

        firebaseAuthAPI = new FirebaseAuthAPI();

        try {
            employee = firebaseAuthAPI.auth(email, psw);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        System.out.println("Login button clicked, result : \n" + employee.toString());

        if (!employee.getUID().isEmpty()) {
            dbr = FirebaseDatabase.getInstance().getReference("employee");
            dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(employee.getUID()).exists()) {
                        isEmployee = true;
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });

            if (isEmployee) {
                dashBoard = new DashBoard(employee);
                this.dispose();
            }

        }
    }

}
