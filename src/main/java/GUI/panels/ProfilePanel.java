package GUI.panels;

import utils.Employee;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class ProfilePanel extends JPanel {
    private JLabel uidLabel;
    private JLabel emailLabel;
    private JLabel tokenLabel;

    public void loadProfilePanel(Employee employee) {
        uidLabel = new JLabel("UID: " + employee.getUID(), SwingConstants.LEFT);
        emailLabel = new JLabel("e-Mail: " + employee.getEmail());
        tokenLabel = new JLabel("TokenID: " + employee.getTokenID());

        uidLabel.setAlignmentX(JLabel.CENTER);
        emailLabel.setAlignmentX(JLabel.CENTER);
        tokenLabel.setAlignmentX(JLabel.CENTER);

        JTextPane istructionText = new JTextPane();
        istructionText.setEditable(false);

        SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(simpleAttributeSet, StyleConstants.ALIGN_CENTER);
        istructionText.setParagraphAttributes(simpleAttributeSet, false);

        //  Utils
        String istructions = "\n\nISTRUZIONI PER l'USO" +
                "\n1. Premere F5 per aggiornare le entries \n" +
                "2. Nel gestore delle segnalazioni aperte, con un doppio click sul codice della segnalazione sarà possibile aprire la chat con l'utente segnalatore.\n" +
                "3. Per eventuali suggerimenti pagliuca.manuel@gmail.com\n";
        istructionText.setText(istructions);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(uidLabel);
        this.add(emailLabel);
        this.add(tokenLabel);
        this.add(istructionText);
        this.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    public void setLocationAndSize() {
        uidLabel.setBounds(110, 150, 250, 30);
        emailLabel.setBounds(110, 250, 150, 30);
        tokenLabel.setBounds(110, 350, 250, 30);
    }
}
