package GUI.panels;

import com.google.firebase.database.*;
import utils.Employee;
import utils.Report;

import javax.swing.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Objects;

public class CreateReport extends JPanel {
    private final JLabel oggettoLabel = new JLabel("Oggetto");
    private final JTextField oggettoReport = new JTextField();
    private final JLabel dataLabel = new JLabel("Data");
    private final JLabel coordLabel = new JLabel("Posizione");
    private final JTextField coordReport = new JTextField("coord_x, coord_y");
    private final SpinnerDateModel timeModel = new SpinnerDateModel(new Date(), null, null, Calendar.HOUR_OF_DAY);
    private JSpinner timeReport = new JSpinner(timeModel);

    private final JLabel priorityLabel = new JLabel("Priorità");
    private final JRadioButton lowPriority = new JRadioButton("Bassa");
    private final JRadioButton mediumPriority = new JRadioButton("Media");
    private final JRadioButton highPriority = new JRadioButton("Alta");
    private final ButtonGroup priorities = new ButtonGroup();
    private final JLabel statusLabel = new JLabel("Status");
    private final String[] statusOptions = {"Aperta", "Chiusa", "Pending"};
    private final JComboBox<String> statusBox = new JComboBox<>(statusOptions);
    private final JLabel issuesLabel = new JLabel("Tipologia problematica");
    private final String[] issuesOptions = {"Problematica di origine naturale", "Problematica stradale", "Attività sospette", "Altro"};
    private final JComboBox<String> issuesBox = new JComboBox<>(issuesOptions);

    private final JLabel socialLabel = new JLabel("Social");
    private final JCheckBoxMenuItem socialReport = new JCheckBoxMenuItem();
    private final JTextArea descReport = new JTextArea();
    private final JLabel descLabel = new JLabel("Descrizione");
    private final JButton sendBtn = new JButton("Invia segnalazione");
    private final JButton resetBtn = new JButton("Reset");
    private String time;
    private String date;

    private final DatabaseReference databaseReference = FirebaseDatabase
            .getInstance()
            .getReference("reports");

    private Employee loggedEmploye;

    public void loadCreateReportPanel() {
        loadItems();

        sendBtn.addActionListener(e -> {
            if (checkCampi()) {
                String repId = databaseReference.push().getKey();
                Report report = new Report(repId,
                        loggedEmploye.getUID(),
                        oggettoReport.getText(),
                        descReport.getText(),
                        date,
                        time,
                        Objects.requireNonNull(issuesBox.getSelectedItem()).toString(),
                        coordReport.getText().replace(",", " "),
                        parsePriority(Objects.requireNonNull(getSelectedPriority())),
                        Objects.requireNonNull(statusBox.getSelectedItem()).toString() + "_" + loggedEmploye.getUID(),
                        socialReport.getState()
                );
                databaseReference.child(repId).setValueAsync(report);
                sendBtn.setEnabled(false);
            }
        });

        resetBtn.addActionListener(e -> {
            oggettoReport.setText("");
            descReport.setText("");
            timeReport = new JSpinner(timeModel);
            issuesBox.setSelectedIndex(0);
            coordReport.setText("coord_x, coord_y");
            priorities.clearSelection();
            statusBox.setSelectedIndex(0);
            socialReport.setState(false);
            sendBtn.setEnabled(true);
        });
    }

    private String parsePriority(String selectedPriority) {
        switch (selectedPriority) {
            case "Alta":
                return "2";
            case "Media":
                return "1";
            case "Bassa":
                return "0";
        }
        return selectedPriority;
    }

    private String getSelectedPriority() {
        for (Enumeration<AbstractButton> buttons = priorities.getElements();
             buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }

    private void parseTimeAndDate(JSpinner dateAndTime) {
        final String toParse = dateAndTime.getValue().toString();
        final int indexColumns = toParse.indexOf(":");
        final int indexDate = toParse.lastIndexOf(" ");
        date = "";
        time = "";
        time = toParse.substring(indexColumns - 2, indexColumns + 6);
        final String year = toParse.substring(indexDate + 1);
        final String day = toParse.substring(indexColumns - 5, indexColumns - 3);
        final String monthToParse = toParse.substring(toParse.indexOf(" ") + 1, toParse.indexOf(" ") + 4);
        date += day + "/";

        switch (monthToParse) {
            case "Jan":
                date += "01";
                break;
            case "Feb":
                date += "02";
                break;
            case "Mar":
                date += "03";
                break;
            case "Apr":
                date += "04";
                break;
            case "May":
                date += "05";
                break;
            case "Jun":
                date += "06";
                break;
            case "Jul":
                date += "07";
                break;
            case "Aug":
                date += "08";
                break;
            case "Sep":
                date += "09";
                break;
            case "Oct":
                date += "10";
                break;
            case "Nov":
                date += "11";
                break;
            case "Dec":
                date += "12";
                break;
        }
        date += "/";
        date += year;
    }

    private boolean checkCampi() {
        parseTimeAndDate(timeReport);
        if (!time.isEmpty() && !date.isEmpty()) {
            if (!oggettoReport.getText().isEmpty()) {
                if (!descReport.getText().isEmpty()) {
                    if (!coordReport.getText().isEmpty() &&
                            coordReport.getText().matches("[- 0-9*]*,[-0-9*]*")) {
                        if (getSelectedPriority() != null) {
                            return true;
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Non è stata selezionata la priorità della segnalazione.",
                                    "Alert", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Non sono state inserite le coordinate dove è avvenuta la segnalazione.",
                                "Alert", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Non è stata inserita alcuna descrizione riguardo la segnalazione.",
                            "Alert", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Non è stata inserito alcuno oggetto per la segnalazione.",
                        "Alert", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Non è stata inserita la date e l'orda di quando è avvenuta la segnalazione.",
                    "Alert", JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    private void loadItems() {
        this.setLayout(null);
        oggettoLabel.setBounds(30, 10, 60, 25);
        oggettoReport.setBounds(100, 10, 500, 25);
        this.add(oggettoLabel);
        this.add(oggettoReport);

        dataLabel.setBounds(30, 40, 80, 25);
        timeReport.setBounds(100, 40, 130, 25);
        this.add(dataLabel);
        this.add(timeReport);

        coordLabel.setBounds(30, 70, 60, 25);
        coordReport.setBounds(100, 70, 130, 25);
        this.add(coordLabel);
        this.add(coordReport);

        statusLabel.setBounds(30, 100, 60, 25);
        statusBox.setBounds(100, 100, 100, 25);
        this.add(statusLabel);
        this.add(statusBox);

        priorityLabel.setBounds(30, 130, 60, 25);
        lowPriority.setBounds(90, 130, 80, 25);
        mediumPriority.setBounds(170, 130, 80, 25);
        highPriority.setBounds(250, 130, 80, 25);

        priorities.add(lowPriority);
        priorities.add(mediumPriority);
        priorities.add(highPriority);

        this.add(priorityLabel);
        this.add(lowPriority);
        this.add(mediumPriority);
        this.add(highPriority);

        issuesLabel.setBounds(30, 160, 150, 25);
        issuesBox.setBounds(180, 160, 250, 25);
        this.add(issuesLabel);
        this.add(issuesBox);

        socialLabel.setBounds(30, 190, 60, 25);
        socialReport.setBounds(100, 190, 20, 25);

        this.add(socialLabel);
        this.add(socialReport);
        descLabel.setBounds(30, 220, 120, 25);
        descReport.setBounds(30, 250, 725, 200);
        this.add(descLabel);
        this.add(descReport);

        sendBtn.setBounds(70, 460, 300, 50);
        resetBtn.setBounds(390, 460, 300, 50);
        this.add(sendBtn);
        this.add(resetBtn);
    }

    public void setEmployee(Employee loggedEmployee) {
        this.loggedEmploye = loggedEmployee;
    }
}