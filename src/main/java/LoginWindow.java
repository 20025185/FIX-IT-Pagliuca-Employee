import com.google.firebase.database.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoginWindow extends JFrame implements ActionListener, KeyListener {
    private Container container = getContentPane();
    private JTextField userTextField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JButton loginButton = new JButton("LOGIN");

    private static FirebaseAuthAPI firebaseAuthAPI;
    private static DatabaseReference dbr;
    private String uid;
    private boolean isEmployee = false;
    private static Employee employee;

    private static DashBoard dashBoard;

    LoginWindow() {
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
        userTextField.setBounds(110, 150, 150, 30);
        passwordField.setBounds(110, 220, 150, 30);
        loginButton.setBounds(135, 300, 100, 30);
    }

    public void addComponents() {
        container.add(userTextField);
        container.add(passwordField);
        container.add(loginButton);
    }

    private void initialize() {
        this.setTitle("[FIX-IT] - LOGIN EMPLOYEE");
        this.setSize(400, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().add(new JLabel("Label"), BorderLayout.CENTER);
    }

    private void addActionEvent() {
        loginButton.addActionListener(this);
    }

    private String getEmail() {
        return userTextField.getText();
    }

    private String getPsw() {
        return passwordField.getText();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            final String email = getEmail();
            final String psw = getPsw();

            firebaseAuthAPI = new FirebaseAuthAPI();

            try {
                employee = firebaseAuthAPI.auth(email, psw);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
