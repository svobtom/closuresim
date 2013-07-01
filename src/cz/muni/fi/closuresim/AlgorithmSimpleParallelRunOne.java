package cz.muni.fi.closuresim;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Tom
 */
class AlgorithmSimpleParallelRunOne implements Runnable {

    private Net net;
    public static ResultCollector resultCollector;
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

            synchronized (LOCKER) {
                if (!workedRoads.contains(r)) {
                    // zde mesmi pristoupit dalsi vlakno
                    workedRoads.add(r);

                } else {
                    continue;
                }
            }

            r.close();
            
            // TODO ? - nebylo by rychlejsi, prvni sit otestovat na dve komponenty a nepocitat vsechny? if (!net.isInOneComponent()) {
            int numOfComp = net.getNumOfComponents();
            if (numOfComp > 1) {
                double variance = net.getValueOfBadness(numOfComp);
                System.out.println(Thread.currentThread().getName() + ": Disconnected after close road " + r.getId() + " (" + variance + ").");
                AlgorithmSimpleParallel.oneRoadToDisconnect.add(r);
                synchronized (LOCKER) {
                    resultCollector.addResultOne(r.getId(), variance);
                }
            }
            r.open();
        }
    }
}
