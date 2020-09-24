package GUI.panels;

import firebase.Employee;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("JavaDoc")
public class ProfilePanel extends JPanel {
    //  Immagine del profilo
    private Image profileImg = null;

    //  Oggetto impiegato, contenente tutte le informazioni da dover mostrare
    private Employee employee;

    /***
     *  Metodo che alla creazione scarica l'immagine del profilo (se la trova) attraverso un metodo e poi
     *  mostra le informazioni, anche qui con chiamando un metodo
     * @param employee
     * @throws IOException
     */
    public void loadProfilePanel(Employee employee) throws IOException {
        this.employee = employee;
        retrieveProfileImg();
        loadItemsOnPanel();
    }

    /***
     * Questo metodo si occuppa di comporre la JLabel mostrando tutte le informazioni utili relative all'impiegato,
     * per formattare la JLabel si è integrato parte di codice HTML all'interno del testo.
     */
    private void loadItemsOnPanel() {
        JLabel employeeInfo = new JLabel(
                "<html><center><br><br><br><br><br><center><h1><u>Profilo impiegato</u></h1>" +
                        "<br>Nome: " + employee.getFullname() + "" +
                        "<br>Cognome: " + employee.getSurname() + "" +
                        "<br>Data di nascita: " + employee.getBirthday() + "" +
                        "<br>Codice fiscale: " + employee.getFiscalCode() + "" +
                        "<br>e-mail: " + employee.getEmail() + "" +
                        "<br><u>UID: " + employee.getUid() + "</u></center></html>");
        Border border = BorderFactory.createLineBorder(Color.PINK, 3);
        employeeInfo.setBorder(border);
        this.setLayout(new BorderLayout());
        employeeInfo.setHorizontalAlignment(JLabel.CENTER);
        employeeInfo.setVerticalAlignment(JLabel.CENTER);
        this.add(employeeInfo, BorderLayout.CENTER);
    }

    /***
     * Metodo utilitario che si occupa di cercare all'interno del relativo nodo impiegato la presenza
     * del campo "imageURL", nel caso in cui il campo non sia vuoto si preleva l'URL e si scarica l'immagine
     * all'interno di una variabile Image (che poi verrà mostrata effettuando l'overriding del metodo paintComponent).
     * In caso contrario non si esegue nulla, e quindi non verrà mostrato nulla.
     *
     * @throws IOException
     */
    private void retrieveProfileImg() throws IOException {
        if (!employee.getImageURL().isEmpty()) {
            try {
                URL url = new URL(employee.getImageURL());
                profileImg = ImageIO.read(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            profileImg = profileImg.getScaledInstance(200, 200, Image.SCALE_DEFAULT);
        }
    }

    /***
     * Override della funzione paintComponent(Graphics g), si mostra a video (su questo JPanel) la variabile di tipo Image
     * della classe, nel caso non fosse caricato nulla all'interno di essa non verrà mostrato nulla.
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(profileImg, 290, 0, this);
    }

}
