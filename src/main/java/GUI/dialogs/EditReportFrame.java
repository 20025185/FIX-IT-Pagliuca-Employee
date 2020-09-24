package GUI.dialogs;

import com.google.firebase.database.*;
import firebase.Report;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@SuppressWarnings("JavaDoc")
public class EditReportFrame {
    //  JFrame
    private final JFrame f = new JFrame();

    //  JDialog
    private final JDialog d = new JDialog(f, "Modifica", true);

    //  JTextField per la modifica dell'oggetto del report
    private final JTextField object = new JTextField(10);

    //  JComboBox contenente le priorità
    private final String[] priorities = {"0", "1", "2"};
    private final JComboBox<String> priority = new JComboBox<>(priorities);

    //  JComboBox per i possibili status
    private final String[] possibleStatus = {"Pending", "Aperta", "Chiusa"};
    private final JComboBox<String> status = new JComboBox<>(possibleStatus);

    //  JButton che permette la modifica della segnalazione.
    private final JButton editBtn = new JButton("Modifica");

    //  Riferimento al Real Time Database
    private final DatabaseReference databaseReference;

    //  Rappresenta il report che verrà passato per parametro nel costruttore.
    private final Report report;

    /***
     * Costruttore che abilità le principali configurazioni del JDialog e ne carica i componenti.
     * Questo avviene attraverso il metodo "editBtnListener()" e "addComponents()"
     * @param report
     */
    public EditReportFrame(Report report) {
        d.setLayout(new FlowLayout());
        this.report = report;
        object.setText(report.getObject());

        object.setEnabled(true);
        priority.setEnabled(true);
        priority.setSelectedItem(report.getPriority());
        status.setEnabled(true);
        editBtn.setEnabled(true);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("reports").child(report.getId());

        editBtnListener();
        addComponents();
    }

    /***
     * Metodo che gestisce il JButton per la modifica "editBtn", il bottene quando viene cliccato effettua le modifiche sulla segnalazione,
     * caso particolare quando lo status viene spostato su "Chiusa" alla segnalazione viene aggiunto un campo contenente
     * il tempo effettivo di quando è stata chiusa.
     */
    private void editBtnListener() {
        editBtn.addActionListener(e -> {
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
            editBtn.setEnabled(false);
        });
    }

    /***
     * Metodo ausiliario che carica i componenti al di sopra del JDialog
     */
    private void addComponents() {
        JLabel priorityLabel = new JLabel("Priorità");
        d.add(priorityLabel);
        d.add(priority);
        JLabel objectLabel = new JLabel("Oggetto");
        d.add(objectLabel);
        d.add(object);
        JLabel statusLabel = new JLabel("Status");
        d.add(statusLabel);
        d.add(status);
        d.add(editBtn);
        d.setLocationRelativeTo(null);
        d.setSize(300, 120);
        d.setVisible(true);
        d.setResizable(false);
        d.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
