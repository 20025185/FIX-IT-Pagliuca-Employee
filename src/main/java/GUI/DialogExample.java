package GUI;

import com.google.firebase.database.*;
import utils.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogExample {
    private static JDialog d;

    private static JLabel priorityLabel = new JLabel("Priorità");
    private static JLabel objectLabel = new JLabel("Oggetto");
    private static JLabel statusLabel = new JLabel("Status");

    private static String[] priorities = {"0", "1", "2"};
    private static String[] possibleStatus = {"Aperta", "Pending", "Chiusa"};

    private static JComboBox priority = new JComboBox(priorities);
    private static JTextField object = new JTextField(10);
    private static JComboBox status = new JComboBox(possibleStatus);

    DialogExample(Report report) {
        JFrame f = new JFrame();
        d = new JDialog(f, "Dialog Example", true);
        d.setLayout(new FlowLayout());

        object.setText(report.getObject());
        JButton edit = new JButton("Modifica");

        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                report.setPriority(priority.getSelectedItem().toString());
                report.setStatus(status.getSelectedItem().toString() + "_" + report.getId());
                report.setObject(object.getText());

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("reports").child(report.getId());

                databaseReference.setValueAsync(report);
                d.dispose();
            }
        });

        d.add(priorityLabel);
        d.add(priority);
        d.add(objectLabel);
        d.add(object);
        d.add(statusLabel);
        d.add(status);
        d.add(edit);

        d.setLocationRelativeTo(null);
        d.setSize(300, 120);
        d.setVisible(true);
        d.setResizable(false);
        d.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

}
