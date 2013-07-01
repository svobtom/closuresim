package cz.muni.fi.closuresim;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tom
 */
public class AlgorithmSimpleParallel implements Algorithm {

    private static final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs;
    private Net net;
    private ResultCollector resultCollector;
    protected static Set<Road> oneRoadToDisconnect = Collections.synchronizedSet(new HashSet());
    protected static Set<Set<Road>> setRoadsToDisconnect = Collections.synchronizedSet(new HashSet());

    public AlgorithmSimpleParallel(Net net, ResultCollector rc) {
        this.net = net;
        this.resultCollector = rc;
        AlgorithmSimpleParallelRunOne.resultCollector = resultCollector;
        AlgorithmSimpleParallelRunTwo.resultCollector = resultCollector;
        AlgorithmSimpleParallelRunThree.resultCollector = resultCollector;
    }

    @Override
    public void start() {

        Runnable[] runnables = new Runnable[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        // check one road
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            runnables[i] = new AlgorithmSimpleParallelRunOne(net);
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
                Logger.getLogger(AlgorithmSimpleParallel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // check two roads
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            runnables[i] = new AlgorithmSimpleParallelRunTwo(net);
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
                Logger.getLogger(AlgorithmSimpleParallel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //System.out.println("Pocet dvoucest k rozpojeni: " + AlgorithmSimpleParallelRunTwo.count); // vypis kolik vratil algoritmus RunTwo vysledku

        /*System.out.println(setRoadsToDisconnect.toString());
        
        * /*
        // check three roads
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            runnables[i] = new AlgorithmSimpleParallelRunThree(net);
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
                Logger.getLogger(AlgorithmSimpleParallel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        */
    }
}
