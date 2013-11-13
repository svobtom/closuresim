package cz.muni.fi.closuresim;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 *
 * @author Tom
 */
public class AlgorithmCycle implements Algorithm {

    private Net net;
    private DisconnectionCollector disconnectionCollector;
    private static final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs;
    protected static Queue<Road> queue = new ConcurrentLinkedQueue<>();

    public AlgorithmCycle(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
    }

    @Override
    public void start(int maxClosedRoads) {

        // add all roads to the queue, threads are going to run over all roads in the queue
        for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
            Road road = it.next();
            queue.add(road);
        }

        // inicialize runnebles and threads
        AlgCycleRunnable[] runnables = new AlgCycleRunnable[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            // inicialize runnable by specific algorithm modification
            runnables[i] = new AlgCycleRunnable(net, disconnectionCollector);
            // set thread to its runnable and name it
            threads[i] = new Thread(runnables[i]);
            threads[i].setName(Integer.toString(i));
        }

        // start threads
        for (int i = 0; i < ExperimentSetup.USE_CPUs; i++) {
            threads[i].start();
        }

        // wait for end of all threads
        for (int i = 0; i < ExperimentSetup.USE_CPUs; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ExperimentSetup.LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("Done");
    }
}
