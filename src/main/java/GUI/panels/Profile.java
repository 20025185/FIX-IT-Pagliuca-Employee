package GUI.panels;

import utils.Employee;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Profile extends JPanel {
    private Image profileImg = null;
    private Employee employee;

    public void loadProfilePanel(Employee employee) throws IOException {
        this.employee = employee;
        retrieveProfileImg();
        loadItemsOnPanel();
    }

    private void loadItemsOnPanel() {
        JLabel employeeInfo = new JLabel(
                   "<html><center><br><br><br><br><br><center><h1><u>Profilo impiegato</u></h1>"    +
                        "<br>Nome: " + employee.getFullname()  + "" +
                        "<br>Cognome: "  + employee.getSurname()   + "" +
                        "<br>Data di nascita: "  + employee.getBirthday()   + "" +
                        "<br>Codice fiscale: "  + employee.getFiscalCode()   + "" +
                        "<br>e-mail: "  + employee.getEmail()     + "" +
                        "<br><u>UID: "  + employee.getUID()    + "</u></center></html>");
        //employeeInfo.setOpaque(true);
        Border border = BorderFactory.createLineBorder(Color.PINK, 3);
        employeeInfo.setBorder(border);
        //employeeInfo.setBackground(Color.PINK);
        this.setLayout(new BorderLayout());
        employeeInfo.setHorizontalAlignment(JLabel.CENTER);
        employeeInfo.setVerticalAlignment(JLabel.CENTER);
        this.add(employeeInfo, BorderLayout.CENTER);
    }

    private void retrieveProfileImg() throws IOException {
        try {
            URL url = new URL(employee.getImageURL());
            profileImg = ImageIO.read(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        profileImg = profileImg.getScaledInstance(200, 200, Image.SCALE_DEFAULT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(profileImg, 290, 0, this);
    }

    public void setLocationAndSize() {

    }
}
