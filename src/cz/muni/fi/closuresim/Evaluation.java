package cz.muni.fi.closuresim;

import java.util.Iterator;
import java.util.logging.Level;

/**
 * Evaluate found disconnection.
 *
 * @author Tom
 */
public class Evaluation {

    private static final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs - 1; // one CPU for main thread
    private Net net;
    private DisconnectionCollector disconnectionCollector;
    private DisconnectionCollector[] subCollectors;

    public Evaluation(Net net, DisconnectionCollector dc) {
        this.net = net;
        this.disconnectionCollector = dc;

        subCollectors = new DisconnectionCollector[NUMBER_OF_THREADS];
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            subCollectors[i] = new DisconnectionCollector();
        }

        int num = 1;
        for (Iterator<Disconnection> it = this.disconnectionCollector.getDisconnections().iterator(); it.hasNext();) {
            Disconnection dis = it.next();
            subCollectors[num % NUMBER_OF_THREADS].addDisconnection(dis);
            num++;
        }
    }

    public void start() {
        
        // if no disconnection was in the collector don't start the threads
        if (this.disconnectionCollector.getNumberOfDisconnections() == 0) {
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
                ExperimentSetup.LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("Done");
    }
}
