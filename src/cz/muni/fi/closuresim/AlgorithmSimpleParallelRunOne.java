package cz.muni.fi.closuresim;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Find disconnection of the net by closing one road.
 *
 * @author Tom
 */
class AlgorithmSimpleParallelRunOne implements Runnable {

    private Net net;
    protected static DisconnectionCollector disconnectionCollector;
    private static Set<Road> workedRoads = Collections.synchronizedSet(new HashSet());
    //private static final Object LOCKER = new Object();

    public AlgorithmSimpleParallelRunOne(Net net) {
        this.net = net.clone();
    }

    @Override
    public void run() {
        testCloseOneRoad();
    }

    private void testCloseOneRoad() {

        // testing closing one road
        for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
            Road r = it.next();

            if (!workedRoads.add(r)) {
                // if the road was closed (is in processing) by another thread
                continue;
            }

            r.close();

            if (!net.isInOneComponent()) {
                //System.out.println(Thread.currentThread().getName() + ": Disconnected after close road " + r.getName() + ".");
                AlgorithmSimpleParallel.oneRoadToDisconnect.add(r);
                Disconnection dis = new Disconnection(r);
                //synchronized (LOCKER) {
                disconnectionCollector.addDisconnection(dis);
                //}

            }

            r.open();
        }
    }
}
