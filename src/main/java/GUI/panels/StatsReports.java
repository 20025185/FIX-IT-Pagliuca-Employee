package GUI.panels;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

public class StatsReports extends JPanel {

    public void loadStatsEmployee() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(80, "Marks", "Student1");
        dataset.setValue(50, "Marks", "Student2");
        dataset.setValue(70, "Marks", "Student3");
        dataset.setValue(10, "Marks", "Student4");
        dataset.setValue(20, "Marks", "Student5");
        dataset.setValue(90, "Marks", "Student6");

        JFreeChart chart = ChartFactory.createLineChart("Statistiche segnalazioni", "Ascisse", "Ordinate", dataset);

        CategoryPlot p = chart.getCategoryPlot();
        p.setRangeGridlinePaint(Color.BLACK);
        String[] options = {"Tutte le segnalazioni", "Bassa priorità", "Media priorità", "Alta priorità", "Budget", "Costo segnalazioni", "Utenti attivi"};

        JComboBox<String> comboBox = new JComboBox<>(options);
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.add(comboBox);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setSize(300, 200);
        chartPanel.setMaximumSize(new Dimension(300, 200));
        chartPanel.setPreferredSize(new Dimension(500, 500));
        chartPanel.setVisible(true);
        chartPanel.setEnabled(false);

        this.setBackground(Color.WHITE);
        this.add(chartPanel);
    }
}