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
    private int startIndex;
    private int stopIndex;
     private static int minDistanceOfClosedRoads;

    public AlgCombRunnable(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net.clone();
        this.disconnectionCollector = disconnectionCollector;
        this.disconnections = new HashSet<>();
    }

    void prepare(Generator<Road> gen, final int startIndex, final int stopIndex) {
        this.generator = gen;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    @Override
    public void run() {
        // assign workout to this thread
        this.fronta.addAll(this.generator.generateObjectsRange(startIndex, stopIndex));

        // start testing
        //testCombinations();
        
        // store disconnections
        this.disconnectionCollector.addDisconnections(disconnections);
    }

    public void testCombinations() {

        while (!this.fronta.isEmpty()) {
            ICombinatoricsVector<Road> iCombinatoricsVector = this.fronta.poll();
            if (iCombinatoricsVector != null) {

                // filtering
                // if (disconnectionCollector.make1RDisconnection(listOfroadsSourceNet)) { // || disconnectionCollector.make2RDisconnection(listOfroadsSourceNet) || !net.distanceBetweenRoadsIsAtLeast(2, listOfroadsSourceNet)
                // if (minDistanceOfClosedRoads != 0 && !net.distanceBetweenRoadsIsAtLeast(minDistanceOfClosedRoads, listOfroadsSourceNet)) {                
                // continue;
                List<Road> listOfroadsSourceNet = iCombinatoricsVector.getVector();
                List<Road> listOfroads = new ArrayList<>(listOfroadsSourceNet.size());

                // get roads from net of this thread
                for (Road road : listOfroadsSourceNet) {
                    listOfroads.add(this.net.getRoad(road.getId()));
                }

                // close all roads
                for (Road road : listOfroads) {
                    road.close();
                }

                if (!net.isInOneComponentFaster()) {
                    Disconnection dis = new Disconnection(listOfroads);
                    disconnections.add(dis);
                }

                // open all roads
                for (Road road : listOfroads) {
                    road.open();
                }
            }
        }
    }

    /**
     * Set minimal distance of two closed roads. The algorithm doesn't try close
     * roads, which are closer than this distance.
     *
     * @param num minimal distance
     */
    public static void setMinDistanceOfClosedRoads(final int num) {
        minDistanceOfClosedRoads = num;
    }

}
