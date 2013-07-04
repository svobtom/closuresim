package cz.muni.fi.closuresim;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Find disconnection of the net by closing one road.
 * @author Tom
 */
class AlgorithmSimpleParallelRunOne implements Runnable {

    private Net net;
    //public static ResultCollector resultCollector;
    protected static DisconnectionCollector disconnectionList;
    private static Set<Road> workedRoads = Collections.synchronizedSet(new HashSet());
    private static final Object LOCKER = new Object();

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

            //synchronized (LOCKER) {
                if (!workedRoads.add(r)) {
                    // if the road was closed by another thread
                    continue;
                }
            //}

            r.close();
            
            // TODO ? - nebylo by rychlejsi, prvni sit otestovat na dve komponenty a nepocitat vsechny? if (!net.isInOneComponent()) {
            //int numOfComp = net.getNumOfComponents();
            if (!net.isInOneComponent()) {
                System.out.println(Thread.currentThread().getName() + ": Disconnected after close road " + r.getId() + ".");
                AlgorithmSimpleParallel.oneRoadToDisconnect.add(r);
                synchronized (LOCKER) {
                    //resultCollector.addResultOne(r.getId(), 0.0);
                    
                    Disconnection dis = new Disconnection(r);
                    disconnectionList.addDisconnection(dis);
                }
            }
            r.open();
        }
    }
}
