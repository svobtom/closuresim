package cz.muni.fi.closuresim;

import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

/**
 *
 * @author Tom
 */
public class AlgCombRunnable implements Runnable {

    private static int minDistanceOfClosedRoads;
    private Net net;
    private DisconnectionCollector disconnectionCollector;
    private Queue<ICombinatoricsVector<Road>> fronta = new LinkedList<>();
    private Generator<Road> generator;
    private int startIndex;
    private int stopIndex;
    private static final Object LOCKER = new Object();

    public AlgCombRunnable(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net.clone();
        this.disconnectionCollector = disconnectionCollector;
    }

    public void prepare(List<ICombinatoricsVector<Road>> workout) {
        //this.fronta = new LinkedList<>(workout);
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
        test();
    }

    public void test() {

        //while (!fronta.isEmpty()) {
        ICombinatoricsVector<Road> iCombinatoricsVector;
        while ((iCombinatoricsVector = fronta.poll()) != null) {
            //ICombinatoricsVector<Road> iCombinatoricsVector = fronta.poll();

            /*
             if (iCombinatoricsVector == null) {
             continue;
             }
             */

            List<Road> listOfroadsSourceNet = iCombinatoricsVector.getVector();


            if (disconnectionCollector.make1RDisconnection(listOfroadsSourceNet)) { // || disconnectionCollector.make2RDisconnection(listOfroadsSourceNet) || !net.distanceBetweenRoadsIsAtLeast(2, listOfroadsSourceNet)
                continue;
            }

            if (minDistanceOfClosedRoads != 0 && !net.distanceBetweenRoadsIsAtLeast(minDistanceOfClosedRoads, listOfroadsSourceNet)) {
                continue;
            }


            List<Road> listOfroads = new ArrayList<>(listOfroadsSourceNet.size());

            for (Road road : listOfroadsSourceNet) {
                // pro kazdou silnici v pridelenych silnicich pro toto vlakno
                // ziskej jeji id a podle id vyhledej silnici v klonovane siti a tu umisti do seznamu vlakna
                listOfroads.add(this.net.getRoad(road.getId()));
            }

            //RoadsSelection rs = new RoadsSelection();
            //rs.addRoads(listOfroads);

            //System.out.println(Thread.currentThread().getName() + ": closing road " + rs + ".");
            //rs.closeAllRoads();

            // close all roads
            for (Iterator<Road> it = listOfroads.iterator(); it.hasNext();) {
                Road road = it.next();
                road.close();
            }

            //System.out.println(Thread.currentThread().getName() + ": closed road " + rs + ".");
            if (!net.isInOneComponentFaster()) {
                //System.out.println(Thread.currentThread().getName() + ": Disconnected after close road " + rs + ".");
                //System.out.print(".");
                Disconnection dis = new Disconnection(listOfroads);
                disconnectionCollector.addDisconnection(dis);
            }
            //System.out.println();
            //rs.openAllRoads();

            // open all roads
            for (Iterator<Road> it = listOfroads.iterator(); it.hasNext();) {
                Road road = it.next();
                road.open();
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
