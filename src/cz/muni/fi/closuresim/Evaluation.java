package cz.muni.fi.closuresim;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;

/**
 * Evaluate found disconnections.
 *
 * @author Tom
 */
public class Evaluation {

    private final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs - 1; // one CPU for main thread
    private final Net net;
    private final DisconnectionCollector disconnectionCollector;
    private final DisconnectionCollector[] subCollectors;

    public Evaluation(Net net, DisconnectionCollector dc, boolean storeByRoad) {
        this.net = net;
        this.disconnectionCollector = dc;

        // load partial results
        if (storeByRoad) {
            loadPartialResults();
        }

        // create new subcollectors
        subCollectors = new DisconnectionCollector[NUMBER_OF_THREADS];
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            subCollectors[i] = new DisconnectionCollector();
        }

        // divide disconnections
        int num = 1;
        for (Disconnection dis : this.disconnectionCollector.getDisconnections()) {
            subCollectors[num % NUMBER_OF_THREADS].addDisconnection(dis);
            num++;
        }
    }

    public void start() {

        // if no disconnection was in the collector don't start the threads
        if (this.disconnectionCollector.getNumberOfDisconnections() == 0) {
            ExperimentSetup.LOGGER.warning("There is no disconnection to evaluate");
            return;
        }

        Runnable[] runnables = new Runnable[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            runnables[i] = new EvaluationRunnable(net, subCollectors[i]);
            threads[i] = new Thread(runnables[i]);
            threads[i].setName(Integer.toString(i));
        }

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads[i].start();
        }

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ExperimentSetup.LOGGER.log(Level.SEVERE, "Error during waiting.", ex);
            }
        }

        System.out.println("Done");
    }

    private void loadPartialResults() {

        // load disconnections from partial results
        File directory = new File(ExperimentSetup.outputDirectory, "partial-results");
        final String[] extensions = {"csv"}; // file extension of loaded files
        Collection<File> files = FileUtils.listFiles(directory, extensions, false);
       
        int processed = 0;
        for (File oneFile : files) {
            
            loadPartialFile(oneFile);
            processed++;
            
            if (processed % 100 == 0) {
                ExperimentSetup.LOGGER.info("Loading partial results. Processed " + processed);
            }
        }
    }

    private void loadPartialFile(File oneFile) {
        
        try {
            List<String> lines = Files.readLines(oneFile, Charset.forName("UTF-8"));

            SortedSet<Disconnection> disconnections = new TreeSet<>();
            for (String line : lines) {
                Disconnection dis = loadOneLine(line);
                disconnections.add(dis);
            }
            
            this.disconnectionCollector.addDisconnections(disconnections);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "Partial result (" + oneFile + ") can't be read", ex);
        }
    }

    private Disconnection loadOneLine(String line) {

        String[] roadNames = line.split(";");
        Set<Road> roads = new HashSet<>();
        for (String roadName : roadNames) {
            Road r = this.net.getRoad(roadName);
            roads.add(r);
        }

        return new Disconnection(roads);
    }
}
