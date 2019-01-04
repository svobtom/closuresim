package cz.muni.fi.closuresim;

import java.util.logging.Level;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.paukov.combinatorics.*;

/**
 * Find disconnections by brutal force (generate all possible combinations of
 * roads).
 *
 * @author Tom
 */
public class AlgorithmCombinatoric implements Algorithm {

    private static final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs; // - 1;

    private final Net net;
    private final DisconnectionCollector disconnectionCollector;
    private int minDistanceOfClosedRoads = 0;

    public AlgorithmCombinatoric(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
    }

    public AlgorithmCombinatoric(Net net, DisconnectionCollector disconnectionCollector, final int minDistanceOfClosedRoads) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
        this.minDistanceOfClosedRoads = minDistanceOfClosedRoads;
    }

    @Override
    public void start(final int maxClosedRoads) {

        //for (int nClosedRoads = 1; nClosedRoads <= maxClosedRoads; nClosedRoads++) {
        int nClosedRoads = maxClosedRoads;

        System.out.println("Finding combinations for " + nClosedRoads + " road(s).");

        // Create new initial vector
        ICombinatoricsVector<Road> initialVector = Factory.createVector(this.net.getRoads());
        final int vectorSize = this.net.getRoads().size();

        // Create a generator of simple combination to generate x-combinations of the vector
        Generator<Road> gen = Factory.createSimpleCombinationGenerator(initialVector, nClosedRoads);

        // create array of runnables and threads
        AlgCombRunnable[] runnables = new AlgCombRunnable[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        // inicialize runnables and threas
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            // inicialize runnable by specific algorithm modification
            runnables[i] = new AlgCombRunnable(net, disconnectionCollector);
            // set thread to its runnable and name it
            threads[i] = new Thread(runnables[i]);
            threads[i].setName(Integer.toString(i));
        }

        // check count of combination to one thread
        final int startOnCombinationsNo = 0;
        final long stopOnCombinationNo = combinatoricNumber(vectorSize, nClosedRoads);
        final long numberOfCombinations = stopOnCombinationNo - startOnCombinationsNo;

        final long forOneThread = numberOfCombinations / NUMBER_OF_THREADS;
        long nextStart = startOnCombinationsNo;
        long nextStop = nextStart + forOneThread;

        // for all threads - give to the threads their combinations (workout) and start the thread
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {

            runnables[i].prepare(gen, nextStart, nextStop);
            System.out.println("For thread " + i + " start is " + nextStart + ", stop is " + nextStop + ", so there is " + (nextStop - nextStart) + " combinations");

            // test pre-last iteration
            if (i != NUMBER_OF_THREADS - 2) {
                nextStart = nextStop;
                nextStop = nextStart + forOneThread;
            } else {
                // next run of cycle is the last
                nextStart = nextStop;
                nextStop = stopOnCombinationNo; // + 1 one no need may be
            }

        }

        System.out.println("Number of combinations " + numberOfCombinations + ", for one thread (" + forOneThread + ")");
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

    /**
     * Count binomical coeficient.
     *
     * @param n number of elements in set
     * @param k element in subset
     * @return binomical coeficient
     */
    public static long combinatoricNumber(final int n, int k) {
        /*
         int numerator = 1;
         for (int i = 0; i < k; i++) {
         numerator = numerator * (n - i);
         }

         return numerator / fact(k);
         */
        
        return CombinatoricsUtils.binomialCoefficient(n, k);
        
    }

    /**
     * Count factorial.
     *
     * @param x integer from 0 to 10
     * @return factorial
     */
    private static int fact(int x) {
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
