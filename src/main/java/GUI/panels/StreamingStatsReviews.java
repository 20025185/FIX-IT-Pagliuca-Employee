package GUI.panels;

import kafka.members.Consumer;
import kafka.streams.FixItStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

import java.util.HashMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StreamingStatsReviews extends JPanel {
    private HashMap<String, String> stringStringHashMap = new HashMap<>();
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private CategoryPlot plot;
    private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private int naturalVal = 0, stradaleVal = 0, sospetteVal = 0, altroVal = 0;

    public void loadStatsRecensioniStream() {
        FixItStream favReviewStream = new FixItStream("input-ratings");
        favReviewStream.execute();

        Consumer consumerFavReview = new Consumer("count-fav-issues");
        consumerFavReview.run();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initializeChartBar();

        Runnable r = () -> {
            stringStringHashMap = consumerFavReview.getRecordKeysAndValues();

            parseValueAndKeys(stringStringHashMap);

            dataset.setValue(naturalVal,
                    "Problematica di origine naturale",
                    "Problematica di origine naturale");
            dataset.setValue(stradaleVal,
                    "Problematica stradale",
                    "Problematica stradale");
            dataset.setValue(sospetteVal,
                    "Attività sospette",
                    "Attività sospette");
            dataset.setValue(altroVal,
                    "Altro",
                    "Altro");

            chart = ChartFactory.createBarChart(
                    "Statistiche in tempo reale delle recensioni favorevoli (rating >= 4.0)",
                    "Tipologia delle segnalazioni",
                    "Numero di segnalazioni",
                    dataset);

            plot = chart.getCategoryPlot();
            plot.getRenderer().setSeriesPaint(1, Color.red);
            plot.getRenderer().setSeriesPaint(2, Color.yellow);
            plot.getRenderer().setSeriesPaint(3, Color.green);
            //plot.setRangeGridlinePaint(Color.WHITE);
            chartPanel = new ChartPanel(chart);

            this.removeAll();
            this.revalidate();
            this.repaint();
            this.add(chartPanel);
        };

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(r, 0, 500, TimeUnit.MILLISECONDS);

        this.setBackground(Color.WHITE);
    }

    private void parseValueAndKeys(HashMap<String, String> hm) {
        if (hm.get("naturale") != null) {
            naturalVal = Integer.parseInt(hm.get("naturale"));
        }

        if (hm.get("stradale") != null) {
            stradaleVal = Integer.parseInt(hm.get("stradale"));
        }

        if (hm.get("sospette") != null) {
            sospetteVal = Integer.parseInt(hm.get("sospette"));
        }

        if (hm.get("altro") != null) {
            altroVal = Integer.parseInt(hm.get("altro"));
        }
    }

    private void initializeChartBar() {
        dataset.setValue(0, "", "Problematica di origine naturale");
        dataset.setValue(0, "", "Problematica stradale");
        dataset.setValue(0, "", "Attività sospette");
        dataset.setValue(0, "", "Altro");

        chart = ChartFactory.createBarChart("Statistiche segnalazioni",
                "Tipologia",
                "Numero di segnalazioni",
                dataset);

        plot = chart.getCategoryPlot();
        plot.setRangeGridlinePaint(Color.WHITE);

        chartPanel = new ChartPanel(chart);
        chartPanel.setSize(500, 400);
        chartPanel.setMaximumSize(new Dimension(500, 400));
        chartPanel.setPreferredSize(new Dimension(500, 400));
        chartPanel.setVisible(true);
        chartPanel.setEnabled(false);

        this.add(chartPanel);
    }
}
