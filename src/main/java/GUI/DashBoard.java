package GUI;

import utils.Employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DashBoard extends JFrame {
    private Container container = getContentPane();

    //  Labels
    private static JLabel uidLabel;
    private static JLabel emailLabel;
    private static JLabel tokenLabel;

    //  Menu Bar
    private static JMenuBar jMenuBar;
    private static JMenu fileMenu, reportMenu;
    private static JMenuItem profileItem, exitItem, handlingReports;

    //  utils.Employee logged
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
        //addActionEvent();
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
        reportMenu = new JMenu("Segnalazioni");
        handlingReports = new JMenuItem(new AbstractAction("Gestione segnalazioni") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

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
        jMenuBar.add(reportMenu);
        fileMenu.add(profileItem);
        fileMenu.add(exitItem);
        reportMenu.add(handlingReports);
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
}
