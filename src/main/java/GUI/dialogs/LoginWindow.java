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
    //  JLabels che stanno ad indicare la JTextField e la JPasswordField
    private final JLabel emailLabel = new JLabel("e-mail : ");
    private final JLabel pswLabel = new JLabel("Password : ");

    //  JTextField e JPasswordField per l'inserimento dei dati all'interno del login.
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    //  Container
    private final Container container = getContentPane();

    //  JButton per il login.
    private final JButton loginButton = new JButton("LOGIN");

    //  Oggetto impiegato che verrà passato al ControlPanel in caso di login riuscito.
    private Employee employee;


    /***
     * Costruttore che inizializza il frame di login, ne imposta il Layout, ne imposta la posizione e la dimensione,
     * aggiunge i componenti ed in fine aggiunge un azione all'evento (tutto realizzato con dei metodi ausiliari).
     */
    public LoginWindow() {
        initialize();
        setComponentsLocation();
        addComponents();
    }

    /***
     * Metodo di inizializzazione, imposta le configurazioni principali e poi chiama le funzioni emailKeyListener() e
     * passwordKeyListener() che si occuperanno di chiamare i relativi ActionListener per i JTextField e JPasswordField.
     */
    private void initialize() {
        this.setTitle("LOGIN");
        this.setIconImage(new ImageIcon("src\\imgs\\icons\\lemon-icon.png").getImage());
        this.setSize(300, 180);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginButton.addActionListener(this);
        container.setLayout(null);

        emailKeyListener();
        passwordKeyListener();
    }

    /***
     * Metodo che si occupa di effettuare il login estrapolando l'email e la password dai campi "emailField" e "passwordField".
     * Successivamente chiama il metodo dell'oggetto FirebaseAPI, "signInWithPassword" che si occuperò dell'effettiva operazione di login,
     * restituendo nell'oggetto employee una vero oggetto employee istanziato (nel caso di login riuscito) o null (nel caso di un login fallito).
     *
     * Nel caso in cui sia riuscito il login sia aprirà il ControlPanel passando l'employee in uscita su di esso, in caso contrario si mostrerà
     * l'errore in un dialog.
     * @throws IOException
     */
    private void login() throws IOException {
        final String email = emailField.getText();
        final String psw = String.valueOf(passwordField.getPassword());
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

    /***
     * Metodo che imposta la posizione dei componenti della finestra di login.
     */
    private void setComponentsLocation() {
        emailLabel.setBounds(30, 10, 50, 30);
        emailField.setBounds(90, 10, 150, 30);
        pswLabel.setBounds(20, 50, 100, 30);
        passwordField.setBounds(90, 50, 150, 30);
        loginButton.setBounds(105, 100, 100, 30);
    }

    /***
     * Metodo che aggiunge i componenti della finestra di login al container.
     */
    private void addComponents() {
        container.add(emailLabel);
        container.add(emailField);
        container.add(pswLabel);
        container.add(passwordField);
        container.add(loginButton);
    }

    /***
     * Metodo che mette il campo "passwordField" in ascolto della pressione del tasto 'Invio',
     * in caso di avvenuta pressione di quel tasto si avvia il metodo di login() per tentare di effettuare il login.
     */
    private void passwordKeyListener() {
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

    /***
     * Metodo che mette il campo "emailField" in ascolto della pressione del tasto 'Invio',
     * in caso di avvenuta pressione di quel tasto si avvia il metodo di login() per tentare di effettuare il login.
     */
    private void emailKeyListener() {
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
    }

    /***
     * Override del metodo actionPerformed(), in caso che il JButton "loginButton" venga cliccato, allora,
     * verrà chiamata la funzione login() per tentare di effettuare il login.
     * @param e
     */
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
}