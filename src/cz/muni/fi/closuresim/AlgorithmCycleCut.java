package cz.muni.fi.closuresim;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 *
 * @author Tom
 */
public class AlgorithmCycleCut implements Algorithm {

    private final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs - 1; // one CPU for main thread
    
    private final Net net;
    private final DisconnectionCollector disconnectionCollector;
    
    private final int maxNumOfComponents;
    private final boolean findOnlyAccurateDisconnection;
    
    protected static final Queue<Road> queueOfRoads = new ConcurrentLinkedQueue<>();

    public AlgorithmCycleCut(Net net, DisconnectionCollector disconnectionCollector, final int maxNumOfComponents, final boolean findOnlyAccurateDisconnection) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
        this.maxNumOfComponents = maxNumOfComponents;
        this.findOnlyAccurateDisconnection = findOnlyAccurateDisconnection;
    }

    @Override
    public void start(final int maxClosedRoads) {

        // add all roads to the queue, threads are going to run over all roads in the queue
        queueOfRoads.addAll(net.getRoads());

        // inicialize runnables and threads
        AlgCycleCutRunnable[] runnables = new AlgCycleCutRunnable[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            // inicialize runnable by specific algorithm modification
            runnables[i] = new AlgCycleCutRunnable(net, disconnectionCollector, maxClosedRoads, maxNumOfComponents, findOnlyAccurateDisconnection);
            // set thread to its runnable and name it
            threads[i] = new Thread(runnables[i]);
            threads[i].setName(Integer.toString(i));
        }

        // start threads
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads[i].start();
        }

        // wait for end of all threads
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ExperimentSetup.LOGGER.log(Level.SEVERE, "Exception during waiting to end of all threads in the algorithm.", ex);
            }
        }

        System.out.println("Algorithm done.");
    }
}
