package cz.muni.fi.closuresim;

import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

/**
 * Algorithm combinatoric runnable.
 *
 * @author Tom
 */
public class AlgCombRunnable implements Runnable {

    private final Net net;
    private final DisconnectionCollector disconnectionCollector;
    private final Set<Disconnection> disconnections;
    private final Queue<ICombinatoricsVector<Road>> fronta = new LinkedList<>();
    private Generator<Road> generator;
    private long startIndex;
    private long stopIndex;
    private static int numberOfFoundDisconnection;

    private static final int GENERATED_AT_ONCE = 1000000;
    
    
    private long generatedEnd = Long.MAX_VALUE;

    private static int runCounter = 0;
    private int runCounterThread = 0;
    private static final long startTime = System.currentTimeMillis();
    private static long lastTime = (System.currentTimeMillis() - startTime) / 1000;
    private static final Object LOCKER = new Object();

    public AlgCombRunnable(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net.clone();
        this.disconnectionCollector = disconnectionCollector;
        this.disconnections = new HashSet<>();
    }

    void prepare(Generator<Road> gen, final long startIndex, final long stopIndex) {
        this.generator = gen;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    @Override
    public void run() {
        // assign workout to this thread
        
        if (stopIndex - startIndex < GENERATED_AT_ONCE) {
            this.fronta.addAll(this.generator.generateObjectsRange((int) startIndex, (int) stopIndex));
        } else {
            generatedEnd = startIndex + GENERATED_AT_ONCE;
            this.fronta.addAll(this.generator.generateObjectsRange((int) startIndex, (int) generatedEnd));
        }
        System.out.println("Combinatins generated");
        
        // start testing
        testCombinations();

        // store disconnections
        this.disconnectionCollector.addDisconnections(disconnections);
    }

    public void testCombinations() {

        while (!this.fronta.isEmpty()) {
            ICombinatoricsVector<Road> iCombinatoricsVector = this.fronta.poll();
            if (iCombinatoricsVector != null) {

                List<Road> listOfroadsSourceNet = iCombinatoricsVector.getVector();

                // get roads from net of this thread
                List<Road> listOfroads = new ArrayList<>(listOfroadsSourceNet.size());
                for (Road road : listOfroadsSourceNet) {
                    listOfroads.add(this.net.getRoad(road.getId()));
                }

                // close all roads
                for (Road road : listOfroads) {
                    road.close();
                }

                // if net is not in one component and we save disconnections, the order is IMPORTANT
                if (!net.isInOneComponentFaster() && ExperimentSetup.saveDisconnections) {
                    Disconnection dis = new Disconnection(listOfroads);
                    disconnections.add(dis);
                }

                // open all roads
                for (Road road : listOfroads) {
                    road.open();
                }

                synchronized (LOCKER) {
                    runCounter++;
                    runCounterThread++;
                    long thisTime = (System.currentTimeMillis() - startTime) / 1000;
                    if (runCounter % 1000000 == 0) {
                        System.out.println("After " + ((System.currentTimeMillis() - startTime) / 1000) + " (" + (thisTime - lastTime) + ") seconds processed " + runCounter + " combinations");
                        lastTime = thisTime;
                    }
                }

                if (fronta.isEmpty() && (generatedEnd < stopIndex)) {

                    long newGeneratedEnd = Math.min(generatedEnd + GENERATED_AT_ONCE, stopIndex);
                    this.fronta.addAll(this.generator.generateObjectsRange((int)generatedEnd, (int) newGeneratedEnd));
                    generatedEnd = newGeneratedEnd;
                }
                
                listOfroads.clear();
                listOfroadsSourceNet.clear();
            }
        }

        synchronized (LOCKER) {
            System.out.println("Tested combinations by thread " + Thread.currentThread().getName() + ": " + runCounterThread + ", over all " + runCounter);
        }
    }

    /**
     * Set minimal distance of two closed roads. The algorithm doesn't try close
     * roads, which are closer than this distance.
     *
     * @param num minimal distance
     */
    public static void setMinDistanceOfClosedRoads(final int num) {
        numberOfFoundDisconnection = num;
    }

}
