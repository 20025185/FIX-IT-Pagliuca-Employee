package GUI.panels;

import kafka.members.Consumer;
import kafka.streams.FixItStream;

import javax.swing.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatsReviewsPanel extends JPanel {
    private HashMap<String, String> stringStringHashMap = new HashMap<>();

    public void loadStatsRecensioniStream() {
        FixItStream favReviewStream = new FixItStream("input-ratings");
        favReviewStream.execute();

        Consumer consumerFavReview = new Consumer("count-fav-issues");
        consumerFavReview.run();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel labelNaturali = new JLabel("Problematiche di origine naturale : ");
        JLabel labelAltro = new JLabel("Altro : ");
        JLabel labelSospette = new JLabel("Attività sospette : ");
        JLabel labelStradali = new JLabel("Problematiche stradali : ");

        this.add(labelNaturali);
        this.add(labelAltro);
        this.add(labelSospette);
        this.add(labelStradali);

        Runnable r = () -> {
            stringStringHashMap = consumerFavReview.getRecordKeysAndValues();

            labelNaturali.setText("Problematiche di origine naturale : " + stringStringHashMap.get("naturale"));
            labelAltro.setText("Altro : " + stringStringHashMap.get("altro"));
            labelSospette.setText("Attività sospette : " + stringStringHashMap.get("sospette"));
            labelStradali.setText("Problematiche stradali : " + stringStringHashMap.get("stradale"));

            this.removeAll();
            this.repaint();
            this.revalidate();

            this.add(labelNaturali);
            this.add(labelAltro);
            this.add(labelSospette);
            this.add(labelStradali);
        };

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(r, 1500, 100, TimeUnit.MILLISECONDS);

    }

}
