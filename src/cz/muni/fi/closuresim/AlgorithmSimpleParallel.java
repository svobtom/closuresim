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
    protected DisconnectionCollector disconnectionList;
    protected static Set<Road> oneRoadToDisconnect = Collections.synchronizedSet(new HashSet());
    protected static Set<Set<Road>> setRoadsToDisconnect = Collections.synchronizedSet(new HashSet());

    public AlgorithmSimpleParallel(Net net, DisconnectionCollector dl) {
        this.net = net;
        this.disconnectionList = dl;
        
        AlgorithmSimpleParallelRunOne.disconnectionCollector = disconnectionList;
        AlgorithmSimpleParallelRunTwo.disconnectionCollector = disconnectionList;
        AlgorithmSimpleParallelRunThree.disconnectionCollector = disconnectionList;
    }

    @Override
    public void start(final int maxNumClosedRoads) {

        // create array of runnable and thread
        Runnable[] runnables = new Runnable[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        // doing algorithm for one road, two roads, ...
        for (int numOfRoads = 1; numOfRoads <= maxNumClosedRoads; numOfRoads++) {
            System.out.print("Discovering " + numOfRoads + " road(s) disconnections ... ");
            
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {

                // inicialize runnable by specific algorithm modification
                switch (numOfRoads) {
                    case 0:
                        break;
                    case 1:
                        runnables[i] = new AlgorithmSimpleParallelRunOne(net);
                        break;
                    case 2:
                        runnables[i] = new AlgorithmSimpleParallelRunTwo(net);
                        break;
                    case 3:
                        runnables[i] = new AlgorithmSimpleParallelRunThree(net);
                        break;
                }

                // set thread to its runnable and name it
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
            
            System.out.println("Done");
        }
    }
}
