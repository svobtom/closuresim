package cz.muni.fi.closuresim;

import java.util.logging.Level;
import org.paukov.combinatorics.*;

/**
 *
 * @author Tom
 */
public class AlgorithmCombinatoric implements Algorithm {

    private static final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs;
    
    private Net net;
    private DisconnectionCollector disconnectionCollector;
    private int minDistanceOfClosedRoads = 0;

    public AlgorithmCombinatoric(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
    }

    public AlgorithmCombinatoric(Net net, DisconnectionCollector disconnectionCollector, final int minDistanceOfClosedRoads) {
        this.minDistanceOfClosedRoads = minDistanceOfClosedRoads;
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
    }

    @Override
    public void start(final int maxClosedRoads) {
        start1(maxClosedRoads);

    }

    public void start1(final int maxClosedRoads) {
        for (int nClosedRoads = 1; nClosedRoads <= maxClosedRoads; nClosedRoads++) {
            System.out.println("Finding combinations for " + nClosedRoads + " road(s).");
            // Create new initial vector
            ICombinatoricsVector<Road> initialVector = Factory.createVector(this.net.getRoads());
            final int vectorSize = this.net.getRoads().size();

            // Create a generator of simple combination to generate x-combinations of the vector
            Generator<Road> gen = Factory.createSimpleCombinationGenerator(initialVector, nClosedRoads);

            //IFilterImplDisconnection filterDisconnection = new IFilterImplDisconnection();
            //IFilterImplDisconnection<initialVector> filterDisconnection;
            //gen.generateFilteredObjects(IFilter<ICombinatoricsVector<Road>> filterDisconnection);

            // create array of runnables and threads
            AlgCombRunnable[] runnables = new AlgCombRunnable[NUMBER_OF_THREADS];
            Thread[] threads = new Thread[NUMBER_OF_THREADS];

            // set additional common atributes of runnable
            AlgCombRunnable.setMinDistanceOfClosedRoads(minDistanceOfClosedRoads);
            
            // inicialize runnebles and threas
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                // inicialize runnable by specific algorithm modification
                runnables[i] = new AlgCombRunnable(net, disconnectionCollector);
                // set thread to its runnable and name it
                threads[i] = new Thread(runnables[i]);
                threads[i].setName(Integer.toString(i));
            }

            /* check count of combination to one thread */

            // get interval from properties
            String setStart = null;
            String setStop = null;
            if (nClosedRoads > 1) {
                setStart = ExperimentSetup.properties.getProperty("startOnCombinationsNo");
                setStop = ExperimentSetup.properties.getProperty("stopOnCombinationsNo");
            }

            int stopOnCombinationNo;
            if (setStop == null) {
                stopOnCombinationNo = combinatoricNumber(vectorSize, nClosedRoads);
            } else {
                stopOnCombinationNo = Integer.parseInt(setStop);
            }

            int startOnCombinationsNo;
            if (setStart == null) {
                startOnCombinationsNo = 0;
            } else {
                startOnCombinationsNo = Integer.parseInt(setStart);
            }

            final int numberOfCombinations = stopOnCombinationNo - startOnCombinationsNo;

            final int forOneThread = numberOfCombinations / NUMBER_OF_THREADS;
            int nextStart = startOnCombinationsNo;
            int nextStop = nextStart + forOneThread;
            //System.out.println("Number of combinations = " + numberOfCombinations + ", combinations for one thread = " + forOneThread);

            /*
             System.out.println(vectorSize + " = " + numberOfCombinations);
             System.out.println(gen.generateObjectsRange(0, 1).size());
             System.exit(1);
             */

            //List<ICombinatoricsVector<Road>>[] workout = new List[NUMBER_OF_THREADS];
            //System.out.println(nextStart + " - " + nextStop + " / " + vectorSize + "(" + forOneThread + ")");
            // for all threads - give to the threads their combinations (workout) and start the thread
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                //workout[i] = gen.generateObjectsRange(nextStart, nextStop);

                runnables[i].prepare(gen, nextStart, nextStop);
                //System.out.println("Thread " + i + ": range " + nextStart + " - " + (nextStop - 1));

                // test na predposledni iteraci cyklu
                if (i != NUMBER_OF_THREADS - 2) {
                    nextStart = nextStop;
                    nextStop = nextStart + forOneThread;
                } else {
                    // predposledni iterace cyklu (pristi pruchod je posledni)
                    nextStart = nextStop;
                    nextStop = numberOfCombinations + startOnCombinationsNo + 1;
                }


            }

            System.out.println("Workout divided, threads starting.");

            /*
             for (int i = 0; i < NUMBER_OF_THREADS; i++) {
             //System.out.println(workout[i].size());
             runnables[i].prepare(workout[i]);
             }
             */

            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                threads[i].start();
            }

            // wait until all threads end 
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException ex) {
                    ExperimentSetup.LOGGER.log(Level.SEVERE, "error while wating to threads to end", ex);
                }
            }

            System.out.println();
        }
    }

    /**
     * pouziva frontu v alg runnable
     *
     */
    public void start2(final int maxClosedRoads) {
        for (int nClosedRoads = 1; nClosedRoads <= maxClosedRoads; nClosedRoads++) {
            System.out.println("Finding all combinations for " + nClosedRoads + " road(s).");
            // Create new initial vector
            ICombinatoricsVector<Road> initialVector = Factory.createVector(this.net.getRoads());
            //final int vectorSize = this.net.getRoads().size();
            // Create a generator of simple combination to generate x-combinations of the vector
            Generator<Road> gen = Factory.createSimpleCombinationGenerator(initialVector, nClosedRoads);

            // create array of runnables and threads
            AlgCombRunnable[] runnables = new AlgCombRunnable[NUMBER_OF_THREADS];
            Thread[] threads = new Thread[NUMBER_OF_THREADS];

            // inicialize tunnebles and threas
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                // inicialize runnable by specific algorithm modification
                runnables[i] = new AlgCombRunnable(net, disconnectionCollector);
                // set thread to its runnable and name it
                threads[i] = new Thread(runnables[i]);
                threads[i].setName(Integer.toString(i));
            }

            //AlgCombRunnable.fronta.addAll(gen.generateAllObjects());

            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                //System.out.println(workout[i].size());
                //runnables[i].prepare(gen);
            }

            //System.out.println(AlgCombRunnable.fronta.size());
            ExperimentSetup.LOGGER.info("Threads starting");
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                threads[i].start();
            }

            // wait until all threads end 
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException ex) {
                    ExperimentSetup.LOGGER.log(Level.SEVERE, "error while wating to threads to end", ex);
                }
            }
            ExperimentSetup.LOGGER.info("Threads ended");
        }
    }

    /**
     * Count binomical coeficient.
     *
     * @param n number of elements in set
     * @param k element in subset
     * @return binomical coeficient
     */
    public static int combinatoricNumber(final int n, final int k) {
        int numerator = 1;
        for (int i = 0; i < k; i++) {
            numerator = numerator * (n - i);
        }

        return numerator / fact(k);
    }

    /**
     * Count factorial.
     *
     * @param x integer from 0 to 10
     * @return factorial
     */
    private static int fact(final int x) {
        switch (x) {
            case 0:
                return 1;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 6;
            case 4:
                return 24;
            case 5:
                return 120;
            case 6:
                return 720;
            case 7:
                return 5040;
            case 8:
                return 40320;
            case 9:
                return 362880;
            case 10:
                return 3628800;
            default:
                throw new IllegalArgumentException("Illegal argument passed to the method.");
        }
    }
}
