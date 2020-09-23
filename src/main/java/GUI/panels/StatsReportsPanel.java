package GUI.panels;

import com.google.firebase.database.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Semaphore;

public class StatsReportsPanel extends JPanel {
    private ChartPanel chartPanel = null;
    private TreeMap<String, Integer> datesAndCount;
    private final ArrayList<String> usersUID = new ArrayList<>();
    private final ArrayList<String> activeUsers = new ArrayList<>();
    private int nonActiveUsers;

    public void loadStatsEmployee() {
        this.setLayout(new BorderLayout());
        String[] options = {"Tutte le segnalazioni", "Bassa priorità", "Media priorità", "Alta priorità", "Budget", "Costo segnalazioni", "Utenti attivi"};

        JComboBox<String> comboBox = new JComboBox<>(options);
        this.add(comboBox, BorderLayout.NORTH);

        comboBox.addActionListener(e -> {
            switch (Objects.requireNonNull(comboBox.getSelectedItem()).toString()) {
                case "Tutte le segnalazioni":
                    retrieveAllReportsData();
                    break;
                case "Bassa priorità":
                    RetrievePriorityReportsData("0");
                    break;
                case "Media priorità":
                    RetrievePriorityReportsData("1");
                    break;
                case "Alta priorità":
                    RetrievePriorityReportsData("2");
                    break;
                case "Budget":
                    retrieveBudgetDataSimulation();
                    break;
                case "Costo segnalazioni":
                    retrievePricesOfReportsSimulation();
                    break;
                case "Utenti attivi":
                    retrieveUsersData();
                    break;
            }
        });

        //  Grafo di default
        retrieveAllReportsData();
    }

    private void retrieveUsersData() {
        if (chartPanel != null)
            this.remove(chartPanel);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        datesAndCount = new TreeMap<>();
        Semaphore semaphore = new Semaphore(0);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.child("users").getChildren()) {
                    usersUID.add(d.getKey());
                }

                for (DataSnapshot d : dataSnapshot.child("reports").getChildren()) {
                    if (usersUID.contains(d.child("uid").getValue().toString())) {
                        int activeUserIndex = usersUID.indexOf(d.child("uid").getValue().toString());
                        activeUsers.add(usersUID.get(activeUserIndex));
                    }
                }

                nonActiveUsers = usersUID.size() - activeUsers.size();
                semaphore.release();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Utenti attivi (che hanno almeno effettuato una segnalazione)", activeUsers.size());
        dataset.setValue("Utenti non attivi", nonActiveUsers);

        JFreeChart chart = ChartFactory.createPieChart("Suddivisione dell'utenza", dataset, true, true, false);

        PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                "{0} ({2})", new DecimalFormat("0"), new DecimalFormat("0%"));
        ((PiePlot) chart.getPlot()).setLabelGenerator(labelGenerator);

        chartPanel = new ChartPanel(chart);
        chartConfig();

        this.repaint();
        this.revalidate();
        this.add(chartPanel);
    }

    private void retrievePricesOfReportsSimulation() {
        if (chartPanel != null)
            this.remove(chartPanel);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(505, "Costo risoluzione della segnalazione", "10/10");
        dataset.setValue(221, "Costo risoluzione della segnalazione", "11/10");
        dataset.setValue(391, "Costo risoluzione della segnalazione", "12/10");
        dataset.setValue(54, "Costo risoluzione della segnalazione", "13/10");
        dataset.setValue(121, "Costo risoluzione della segnalazione", "14/10");

        JFreeChart chart = ChartFactory.createLineChart("Costo riparazioni delle segnalazioni", "Giorni", "Costo per la riparazione (€)", dataset);
        CategoryPlot p = chart.getCategoryPlot();
        p.setRangeGridlinePaint(Color.GREEN);

        chartPanel = new ChartPanel(chart);

        chartConfig();

        this.repaint();
        this.revalidate();
        this.add(chartPanel);
    }

    private void chartConfig() {
        chartPanel.setSize(500, 500);
        chartPanel.setMaximumSize(new Dimension(500, 500));
        chartPanel.setPreferredSize(new Dimension(500, 500));
        chartPanel.setVisible(true);
        chartPanel.setEnabled(false);
    }

    private void RetrievePriorityReportsData(String priority) {
        if (chartPanel != null)
            this.remove(chartPanel);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reports");
        Semaphore semaphore = new Semaphore(0);
        datesAndCount = new TreeMap<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (dataSnapshot1.child("priority").getValue().toString().equals(priority)) {
                        final String date = dataSnapshot1.child("date").getValue().toString();

                        if (datesAndCount.containsKey(date)) {
                            int val = datesAndCount.get(date);
                            ++val;
                            datesAndCount.put(date, val);
                        } else {
                            datesAndCount.put(date, 1);
                        }
                    }
                }
                semaphore.release();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(datesAndCount.toString());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Integer> entry : datesAndCount.entrySet()) {
            dataset.setValue(entry.getValue(), "Affluenza segnalazioni", entry.getKey());
        }

        String title = "";
        switch (priority) {
            case "0":
                title = "Statistiche segnalazioni con bassa priorità";
                break;
            case "1":
                title = "Statistiche segnalazioni con media priorità";
                break;
            case "2":
                title = "Statistiche segnalazioni con alta priorità";
                break;
        }

        JFreeChart chart = ChartFactory.createLineChart(title, "Giorni", "Numero di segnalazioni", dataset);

        CategoryPlot p = chart.getCategoryPlot();
        p.setRangeGridlinePaint(Color.BLUE);

        chartPanel = new ChartPanel(chart);
        chartConfig();

        this.repaint();
        this.revalidate();
        this.add(chartPanel);
    }

    private void retrieveAllReportsData() {
        if (chartPanel != null)
            this.remove(chartPanel);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reports");
        TreeMap<String, Integer> datesAndCount = new TreeMap<>();

        Semaphore semaphore = new Semaphore(0);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    final String date = dataSnapshot1.child("date").getValue().toString();

                    if (datesAndCount.containsKey(date)) {
                        int val = datesAndCount.get(date);
                        ++val;
                        datesAndCount.put(date, val);
                    } else {
                        datesAndCount.put(date, 1);
                    }
                }

                semaphore.release();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Integer> entry : datesAndCount.entrySet()) {
            dataset.setValue(entry.getValue(), "Affluenza segnalazioni", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createLineChart("Comportamento di tutte le segnalazioni sulla piattaforma", "Giorni", "Numero di segnalazioni", dataset);

        CategoryPlot p = chart.getCategoryPlot();
        p.setRangeGridlinePaint(Color.BLACK);

        chartPanel = new ChartPanel(chart);

        chartConfig();

        this.repaint();
        this.revalidate();
        this.add(chartPanel);

    }

    private void retrieveBudgetDataSimulation() {
        if (chartPanel != null)
            this.remove(chartPanel);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(100, "Variazione del budget", "10/10");
        dataset.setValue(-50, "Variazione del budget", "11/10");
        dataset.setValue(200, "Variazione del budget", "12/10");
        dataset.setValue(500, "Variazione del budget", "13/10");
        dataset.setValue(100, "Variazione del budget", "14/10");

        JFreeChart chart = ChartFactory.createLineChart("Transazioni", "Giorni", "Conto (€)", dataset);

        CategoryPlot p = chart.getCategoryPlot();
        p.setRangeGridlinePaint(Color.YELLOW);

        chartPanel = new ChartPanel(chart);

        chartConfig();

        this.repaint();
        this.revalidate();
        this.add(chartPanel);
    }

}