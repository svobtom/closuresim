package cz.muni.fi.closuresim;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Find all pair of roads, which closing disconnect the network.
 *
 * @author Tom
 */
public class AlgorithmSimpleParallelRunTwo implements Runnable {

    /**
     * Network. Each instance of this class has to have hard copy of the
     * discovering network.
     */
    private Net net;
    /**
     * Reference to ResultCollector.
     */
    public static ResultCollector resultCollector;
    private static Set<Road> twoRoadCheck = Collections.synchronizedSet(new HashSet());
    /**
     * Set of roads, which are processing by some thread.
     */
    private static Set<Road> workedRoads = Collections.synchronizedSet(new HashSet());
    public static int count = 0;
    private static final Object LOCKER = new Object();

    public AlgorithmSimpleParallelRunTwo(Net net) {
        this.net = net.clone();
    }

    @Override
    public void run() {

        testCloseTwoRoads();
    }

    private void testCloseTwoRoads() {
        // testing closing two roads
        //Set<Road> twoRoadCheck = new HashSet();
        for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
            Road r = it.next();

            synchronized (LOCKER) {
                if (!workedRoads.contains(r)) {
                    // zde mesmi pristoupit dalsi vlakno
                    workedRoads.add(r);
                } else {
                    continue;
                }
            }

            if (!AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r)) {

                r.close();

                for (Iterator<Road> it2 = net.getRoads().iterator(); it2.hasNext();) {
                    Road r2 = it2.next();

                    /*synchronized (LOCKER) {
                     if (workedRoads.contains(r2)) {
                     continue;
                     }
                     }*/

                    if (!AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r2) && !r.equals(r2) && !twoRoadCheck.contains(r2)) { // && !workedRoads.contains(r2)

                        r2.close();
                        // TODO ? - nebylo by rychlejsi, prvni sit otestovat na dve komponenty a nepocitat vsechny? boolean connected = net.isInOneComponent(); if (!connected) {
                        int numOfComp = net.getNumOfComponents();
                        if (numOfComp > 1) {
                            double variance = net.getValueOfBadness(numOfComp);
                            System.out.println(Thread.currentThread().getName() + ": Disconnected after close roads " + r.getId() + " and " + r2.getId() + " (" + variance + "). ");
                            Set<Road> toStore = new HashSet<Road>();
                            toStore.add(r);
                            toStore.add(r2);
                            AlgorithmSimpleParallel.setRoadsToDisconnect.add(toStore);
                            synchronized (LOCKER) {
                                resultCollector.addResultTwo(r.getId(), r2.getId(), variance);
                                count++;
                            }
                        }
                        r2.open();

                    }
                }
                r.open();
                twoRoadCheck.add(r);
                //System.out.print(".");
            }

        }
    }
}
