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

@SuppressWarnings("JavaDoc")
public class StreamingStatsReviewsPanel extends JPanel {
    //  HashMap contenente i log <Key,Value> tali che contengano la tipologia della recensione nella chiave e il numero di recensioni nel value
    private HashMap<String, String> favReviewsHashMap = new HashMap<>();

    //  Grafo della libreria JFreeChart
    private JFreeChart chart;

    //  ChartPanel per spostare il grafo su un oggetto simile a JPanel
    private ChartPanel chartPanel;

    //  Oggetto per effettuare il plotting
    private CategoryPlot plot;

    //  Dataset per effettuare il plot
    private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    //  Valori per ogni tipo di segnalazione
    private int naturalVal = 0, stradaleVal = 0, sospetteVal = 0, altroVal = 0;

    /***
     * Metodo che crea uno Stream sul topic "input-ratings" e crea un consumer sul topic "count-fav-issues".
     * Nel runnable si continua a ricevere un HashMap aggiornata dei valori dal consumer.
     * successivamente si estraggono i valori e li si mettono all'interno del dataset successivamente vengono elaborati e
     * plottati sul grafo, avendo così un grafo che è aggiornato i log ricevuti dal topic che subisce il processamento dal KafkaStream
     * "favReviewStream".
     */
    public void loadStatsRecensioniStream() {
        FixItStream favReviewStream = new FixItStream("input-ratings");
        favReviewStream.execute();

        Consumer consumerFavReview = new Consumer("count-fav-issues");
        consumerFavReview.run();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initializeChartBar();

        Runnable r = () -> {
            favReviewsHashMap = consumerFavReview.getRecordKeysAndValues();

            parseValueAndKeys(favReviewsHashMap);

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

    /***
     * Metodo che effettua il parsing dei valori contenuti all'interno dell'HashMap passata come parametro,
     * dopo aver effettuato questo parsing essendo entrambi i valori di tipo string si effettua un altro parse ma stavolta con il metodo
     * della classe involucro Integer, ed i valori vengono smistati per tipologia nelle rispettive variabili.
     * @param hm
     */
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

    /***
     * Metodo ausiliari che setta le impostazioni per il DefaultCategoryDataset "dataset" (ovvero l'insieme dei dati che verrà rappresentato)
     * e per il pannello contenente il grafo "chartPanel".
     */
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
