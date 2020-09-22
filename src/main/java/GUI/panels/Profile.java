package GUI.panels;

import utils.Employee;

import javax.imageio.ImageIO;
import javax.swing.*;
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
        JLabel employeeInfo = new JLabel("<html>Fullname: " + employee.getFullname() +
                "<br>" + "Surname: " + employee.getSurname()
                + "<br> e-Mail: " + employee.getEmail() +
                "<br> UID: " + employee.getUID() + "</html>");
        this.setLayout(new BorderLayout());
        employeeInfo.setHorizontalAlignment(JLabel.CENTER);
        employeeInfo.setVerticalAlignment(JLabel.CENTER);
        this.add(employeeInfo,BorderLayout.CENTER);
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
        g.drawImage(profileImg, 280, 0, this);
    }

    public void setLocationAndSize() {

    }
}
