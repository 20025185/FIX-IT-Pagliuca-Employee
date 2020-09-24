package GUI.panels;

import com.google.firebase.database.*;
import firebase.Employee;
import firebase.Report;

import javax.swing.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Objects;

@SuppressWarnings("JavaDoc")
public class CreateReportPanel extends JPanel {
    //  JTextField per impostare l'oggetto della segnalazione
    private final JLabel oggettoLabel = new JLabel("Oggetto");
    private final JTextField oggettoReport = new JTextField();

    //  JSpinner per selezionare la data e ora della segnalazione
    private final JLabel dataLabel = new JLabel("Data");
    private final SpinnerDateModel timeModel = new SpinnerDateModel(new Date(), null, null, Calendar.HOUR_OF_DAY);
    private JSpinner timeReport = new JSpinner(timeModel);

    //  JTextField per impostare le coordinate della segnalazione.
    private final JLabel coordLabel = new JLabel("Posizione");
    private final JTextField coordReport = new JTextField("coord_x, coord_y");

    //  ButtonGroup per selezionare la priorità della segnalazione
    private final JLabel priorityLabel = new JLabel("Priorità");
    private final ButtonGroup priorities = new ButtonGroup();
    private final JRadioButton lowPriority = new JRadioButton("Bassa");
    private final JRadioButton mediumPriority = new JRadioButton("Media");
    private final JRadioButton highPriority = new JRadioButton("Alta");

    //  JComboBox per selezionare lo status della segnalazione
    private final JLabel statusLabel = new JLabel("Status");
    private final String[] statusOptions = {"Aperta", "Chiusa", "Pending"};
    private final JComboBox<String> statusBox = new JComboBox<>(statusOptions);

    //  JComboBox per selezionare la tipologia della segnalazione
    private final JLabel issuesLabel = new JLabel("Tipologia problematica");
    private final String[] issuesOptions = {"Problematica di origine naturale", "Problematica stradale", "Attività sospette", "Altro"};
    private final JComboBox<String> issuesBox = new JComboBox<>(issuesOptions);

    //  JCheckBoxMenuItem che rappresenta il booleano per la diffusione su social
    private final JLabel socialLabel = new JLabel("Social");
    private final JCheckBoxMenuItem socialReport = new JCheckBoxMenuItem();

    //  JTextArea compilabile nella descrizione del report.
    private final JTextArea descReport = new JTextArea();

    //  JLabel che indica la descrizione del report.
    private final JLabel descLabel = new JLabel("Descrizione");

    //  JButton per inviare la segnalazione sul RTD.
    private final JButton sendBtn = new JButton("Invia segnalazione");

    //  JButton per resettare i campi.
    private final JButton resetBtn = new JButton("Reset");

    private String time;
    private String date;

    //  Riferimento al database partendo dal nodo "reports"
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reports");

    //  Impiegato loggato, viene passato come parametro nella funzione di loading
    private Employee loggedEmployee;

    /***
     * Metodo che chiamando un altro metodo loadItems() carica i componenti all'interno del JPanel, successivamente vengono anche definiti i listeners
     * per il JButton "sendBtn" che invierà e creerà il metodo sul Real Time Database di Firebase, e per il JButton "resetBtn" che si occuperà del reset dei campi.
     */
    public void loadCreateReportPanel(Employee loggedEmployee) {
        loadItems();
        this.loggedEmployee = loggedEmployee;
        sendBtn.addActionListener(e -> {
            if (checkCampi()) {
                String repId = databaseReference.push().getKey();
                Report report = new Report(repId,
                        this.loggedEmployee.getUid(),
                        oggettoReport.getText(),
                        descReport.getText(),
                        date,
                        time,
                        Objects.requireNonNull(issuesBox.getSelectedItem()).toString(),
                        coordReport.getText().replace(",", " "),
                        parsePriority(Objects.requireNonNull(getSelectedPriority())),
                        Objects.requireNonNull(statusBox.getSelectedItem()).toString() + "_" + this.loggedEmployee.getUid(),
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

    /***
     * Metodo che restituisce la priorità selezionata selezionata sui RadioButtons
     * @return
     */
    private String getSelectedPriority() {
        for (Enumeration<AbstractButton> buttons = priorities.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }

    /***
     * Metodo che effetua il parsing della data e tempo per ottenere la formattazione desiderata.
     * @param dateAndTime
     */
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

    /***
     * Metodo ausiliario che effettua il controllo e la verifica chet tutti i campi compilabili per la segnalazioni rispettino
     * le regole di formattazione per mandare una segnalazione "sensata".
     * @return
     */
    private boolean checkCampi() {
        parseTimeAndDate(timeReport);
        if (!time.isEmpty() && !date.isEmpty()) {
            if (!oggettoReport.getText().isEmpty()) {
                if (!descReport.getText().isEmpty()) {
                    //noinspection RegExpDuplicateCharacterInClass
                    if (!coordReport.getText().isEmpty() &&
                            coordReport.getText().matches("[- 0-9*.0-9*],[- 0-9*.0-9*]")) {
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

    /***
     * Metodo ausiliario che carica gli oggetti sul pannello
     */

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

    /***
     * Metodo ausiliario che effettua il parsing della priorità restituendo una stringa che corrisponde al codice della priorità letta.
     * @param selectedPriority
     * @return
     */
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

}