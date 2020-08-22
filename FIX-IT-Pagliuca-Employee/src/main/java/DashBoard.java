import com.google.firebase.database.DatabaseReference;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class DashBoard extends JFrame implements ActionListener, KeyListener {
    private Container container = getContentPane();

    //  Labels
    private static JLabel uidLabel;
    private static JLabel emailLabel;
    private static JLabel tokenLabel;

    //  Menu Bar
    private static JMenuBar jMenuBar;
    private static JMenu fileMenu, reportMenu;
    private static JMenuItem profileItem, exitItem;

    //  Employee logged
    private static Employee employee;

    DashBoard(Employee t_employee) {
        employee = t_employee;

        //  Labels
        uidLabel = new JLabel(employee.getUID());
        emailLabel = new JLabel(employee.getEmail());
        tokenLabel = new JLabel(employee.getTokenID());

        //  Menu Bar
        initMenu();
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
        uidLabel.setBounds(110, 150, 250, 30);
        emailLabel.setBounds(110, 250, 150, 30);
        tokenLabel.setBounds(110, 350, 250, 30);
    }

    private void initMenu() {
        jMenuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        profileItem = new JMenuItem(new AbstractAction("Profile") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Clicked on profile");
            }
        });
        exitItem = new JMenuItem(new AbstractAction("Esci") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DashBoard.this.dispose();
                System.exit(1);
            }
        });

        this.setJMenuBar(jMenuBar);
        jMenuBar.add(fileMenu);
        fileMenu.add(profileItem);
        fileMenu.add(exitItem);
    }


    public void addComponents() {
        container.add(uidLabel);
        container.add(emailLabel);
        container.add(tokenLabel);
    }

    private void initialize() {
        this.setTitle("[FIX-IT] - DASHBOARD");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void addActionEvent() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

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
