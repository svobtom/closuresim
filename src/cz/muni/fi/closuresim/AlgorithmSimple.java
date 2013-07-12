package cz.muni.fi.closuresim;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author Tom
 */
public class AlgorithmSimple implements Algorithm {

    private Net net;
    /**
     * Set of sets of roads, which has been closed and make net disconnection.
     */
    //private Set<Road> knownRoadToDisconnect;
    /**
     * Set of roads which faulture disconnecting net
     */
    private Set<Road> oneRoadToDisconnect;

    public AlgorithmSimple(Net net) {
        this.net = net;
        oneRoadToDisconnect = new HashSet();
    }

    @Override
    public void start(int maxNumOfClosedRoads) {
        testCloseOneRoad();
        System.out.println();

        testCloseTwoRoads();
        System.out.println();
    }

    public void testCloseOneRoad() {
        // testing closing one road
        for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
            Road r = it.next();
            // System.out.print("Connected after close " + r.getId() + "? ");
            r.close();
            int numOfComp = net.getNumOfComponents();
            if (numOfComp > 1) {
                System.out.println("Disconnected after close road " + r.getId() + " (" + net.getValueOfBadness(numOfComp) + ").");
                oneRoadToDisconnect.add(r);
            }
            r.open();
        }
    }

    public void testCloseTwoRoads() {
        // testing closing two roads
        Set<Road> twoRoadCheck = new HashSet();
        for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
            Road r = it.next();
            if (!oneRoadToDisconnect.contains(r)) {
                r.close();

                for (Iterator<Road> it2 = net.getRoads().iterator(); it2.hasNext();) {
                    Road r2 = it2.next();
                    if (!oneRoadToDisconnect.contains(r2) && !r.equals(r2) && !twoRoadCheck.contains(r2)) {
                        r2.close();
                        int numOfComp = net.getNumOfComponents();
                        if (numOfComp > 1) {
                            System.out.println("Graph isn't connected when roads " + r.getId() + " and " + r2.getId() + " are closed (" + net.getValueOfBadness(numOfComp) + "). ");

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
