package GUI;

import com.google.firebase.database.*;
import utils.Report;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class EditReportFrame {

    private final String[] priorities = {"0", "1", "2"};
    private final String[] possibleStatus = {"Pending", "Aperta", "Chiusa"};

    private final JComboBox<String> priority = new JComboBox<>(priorities);
    private final JTextField object = new JTextField(10);
    private final JComboBox<String> status = new JComboBox<>(possibleStatus);

    private final DatabaseReference databaseReference;

    EditReportFrame(Report report) {
        JFrame f = new JFrame();
        JDialog d = new JDialog(f, "Modifica", true);
        d.setLayout(new FlowLayout());
        object.setText(report.getObject());
        JButton edit = new JButton("Modifica");

        object.setEnabled(true);
        priority.setEnabled(true);
        priority.setSelectedItem(report.getPriority());
        status.setEnabled(true);
        edit.setEnabled(true);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("reports").child(report.getId());

        edit.addActionListener(e -> {
            report.setPriority(Objects.requireNonNull(priority.getSelectedItem()).toString());
            report.setStatus(Objects.requireNonNull(status.getSelectedItem()).toString() + "_" + report.getUid());
            report.setObject(object.getText());

            databaseReference.child("priority").setValueAsync(priority.getSelectedItem().toString());
            databaseReference.child("status").setValueAsync(status.getSelectedItem().toString() + "_" + report.getUid());
            databaseReference.child("object").setValueAsync(object.getText());
            if (report.getStatus().contains("Chiusa")) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                String dateFormatted = formatter.format(date);
                databaseReference.child("data_chiusura").setValueAsync(dateFormatted);
            }
            
            object.setEnabled(false);
            priority.setEnabled(false);
            status.setEnabled(false);
            edit.setEnabled(false);
        });

        JLabel priorityLabel = new JLabel("Priorità");
        d.add(priorityLabel);
        d.add(priority);
        JLabel objectLabel = new JLabel("Oggetto");
        d.add(objectLabel);
        d.add(object);
        JLabel statusLabel = new JLabel("Status");
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
