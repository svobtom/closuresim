package cz.muni.fi.closuresim;

import java.util.logging.Level;

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
    
    public Evaluation(Net net, DisconnectionCollector dc) {
        this.net = net;
        this.disconnectionCollector = dc;

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
            System.out.println("There is no disconnection to evaluate");
            ExperimentSetup.LOGGER.info("There is no disconnection to evaluate");
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
}
