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
    protected static DisconnectionCollector disconnectionCollector;
    private static Set<Road> twoRoadCheck = Collections.synchronizedSet(new HashSet());
    /**
     * Set of roads, which are processing by some thread.
     */
    private static Set<Road> workedRoads = Collections.synchronizedSet(new HashSet());
    private static Set<Set<Road>> twoWorkedRoads = Collections.synchronizedSet(new HashSet());
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
        for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
            Road r = it.next();

            // if this roads isn't been processing by someone else
            if (!workedRoads.add(r)) {
                continue;
            }

            // if the road closing alone don't lead to disconnection
            if (!AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r)) {

                r.close();

                for (Iterator<Road> it2 = net.getRoads().iterator(); it2.hasNext();) {
                    Road r2 = it2.next();

                    if (!AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r2) && !r.equals(r2) && !twoRoadCheck.contains(r2)) { // && !workedRoads.contains(r2)

                        r2.close();

                        if (!net.isInOneComponent()) {

                            //System.out.println(Thread.currentThread().getName() + ": Disconnected after close roads " + r.getName() + " and " + r2.getName() + ". ");

                            Set<Road> toStore = new HashSet<>();
                            toStore.add(r);
                            toStore.add(r2);
                            AlgorithmSimpleParallel.setRoadsToDisconnect.add(toStore); // todo - zrusit, je nahrazeno disconnectionCollectorem

                            Disconnection dis = new Disconnection(r, r2);
                            disconnectionCollector.addDisconnection(dis);

                        }

                        r2.open();

                    }
                }

                r.open();
                twoRoadCheck.add(r);

            }

        }
    }
}
