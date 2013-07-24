package cz.muni.fi.closuresim;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tom
 */
public class AlgorithmSimpleParallelRunnable implements Runnable {

    private Net net;
    protected static DisconnectionCollector disconnectionCollector;
    //
    // pro reseni zpusobem synchronizace pri behu vlaken
    private int numOfRoads;
    private static Set<Road> workedRoads = Collections.synchronizedSet(new HashSet());
    private static Set<Road> twoRoadCheck = Collections.synchronizedSet(new HashSet());
    private static Set<Set<Road>> doneThreeRoads = Collections.synchronizedSet(new HashSet<Set<Road>>());
    private static Set<RoadsSelection> setRs = Collections.synchronizedSet(new HashSet<RoadsSelection>());
    //
    // pro reseni pri behu bez synchronizace
    private List<RoadsSelection> roadsSelection = new LinkedList<>();

    public AlgorithmSimpleParallelRunnable(Net net) {
        this.net = net.clone();
    }

    public void prepare(final int numOfRoads) {
        this.numOfRoads = numOfRoads;
        workedRoads.clear();
        twoRoadCheck.clear();
    }

    @Override
    public void run() {

        switch (numOfRoads) {
            case 1:
                testCloseOneRoad();
                break;
            case 2:
                testCloseTwoRoads();
                break;
            case 3:
                testCloseThreeRoads2();
                break;
        }

        // obecna metoda, ktera umi overovat rozpad bez zavislosti na poctu cest
        //examineDisconnections();

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

                    // if the road2 closing alone don't lead to disconnection, not be same, not be ?
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

    private void testCloseThreeRoads() {

        for (Iterator<Road> it1 = this.net.getRoads().iterator(); it1.hasNext();) {
            Road r1 = it1.next();
            if (AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r1)) {
                continue;
            }

            if (!workedRoads.add(r1)) {
                continue;
            }

            r1.close();

            for (Iterator<Road> it2 = this.net.getRoads().iterator(); it2.hasNext();) {
                Road r2 = it2.next();
                Set<Road> toCheck = new HashSet<>();
                toCheck.add(r1);
                toCheck.add(r2);
                if (!r1.equals(r2) && !AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r2) && !AlgorithmSimpleParallel.setRoadsToDisconnect.contains(toCheck)) {
                    r2.close();
                    for (Iterator<Road> it3 = this.net.getRoads().iterator(); it3.hasNext();) {
                        Road r3 = it3.next();
                        Set<Road> toCheck2 = new HashSet<>();
                        toCheck2.add(r1);
                        toCheck2.add(r3);
                        Set<Road> toCheck3 = new HashSet<>();
                        toCheck3.add(r2);
                        toCheck3.add(r3);
                        if (!r1.equals(r3) && !r2.equals(r3) && !AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r3) && !AlgorithmSimpleParallel.setRoadsToDisconnect.contains(toCheck2) && !AlgorithmSimpleParallel.setRoadsToDisconnect.contains(toCheck3)) {
                            Set<Road> setRoads = new HashSet<>();
                            setRoads.add(r1);
                            setRoads.add(r2);
                            setRoads.add(r3);
                            if (doneThreeRoads.add(setRoads)) {
                                r3.close();
                                int numOfComp = net.getNumOfComponents();
                                if (numOfComp > 1) {
                                    double variance = net.getValueOfBadness(numOfComp);
                                    System.out.println(Thread.currentThread().getName() + ": Disconnected after close roads " + r1.getName() + ", " + r2.getName() + " and " + r3.getName() + " (" + variance + "). ");

                                    Disconnection dis = new Disconnection(r1, r2, r3);
                                    disconnectionCollector.addDisconnection(dis);

                                }
                                r3.open();

                            }



                        }
                    }
                }
                r2.open();
            }
            r1.open();
        }

    }

    private void testCloseThreeRoads2() {

        for (Iterator<Road> it1 = this.net.getRoads().iterator(); it1.hasNext();) {
            Road r1 = it1.next();

            // if this roads isn't been processing by someone else
            if (!workedRoads.add(r1)) {
                continue;
            }

            RoadsSelection rs = new RoadsSelection(3);
            rs.addRoad(r1);
            if (disconnectionCollector.containDisconnection(rs)) {
                continue;
            }

            for (Iterator<Road> it2 = this.net.getRoads().iterator(); it2.hasNext();) {
                Road r2 = it2.next();

                RoadsSelection rs2 = new RoadsSelection(2);
                rs2.addRoad(r2);
                if (r2.equals(r1) || disconnectionCollector.containDisconnection(rs2)) {
                    continue;
                }

                rs.addRoad(r2);
                if (disconnectionCollector.containDisconnection(rs)) {
                    continue;
                }

                for (Iterator<Road> it3 = this.net.getRoads().iterator(); it3.hasNext();) {
                    Road r3 = it3.next();

                    RoadsSelection rs3_2 = new RoadsSelection(2);
                    rs3_2.addRoad(r3);
                    if (r3.equals(r1) || r3.equals(r2) || disconnectionCollector.containDisconnection(rs3_2)) {
                        continue;
                    }
                    rs3_2.addRoad(r2);

                    RoadsSelection rs3_1 = new RoadsSelection(2);
                    rs3_1.addRoad(r1);
                    rs3_1.addRoad(r3);

                    rs2.addRoad(r3);
                    if (disconnectionCollector.containDisconnection(rs3_1) || disconnectionCollector.containDisconnection(rs3_2)) {
                        continue;
                    }


                    rs.addRoad(r3);
                    if (!disconnectionCollector.containDisconnection(rs)) {
                        //setRs.add(rs);
                        r1.close();
                        r2.close();
                        r3.close();
                        //System.out.println(Thread.currentThread().getName() + ": Candidate after close roads " + r1.getName() + ", " + r2.getName() + " and " + r3.getName() + ".");
                        if (!net.isInOneComponent()) {
                            //System.out.println(Thread.currentThread().getName() + ": Disconnected after close roads " + r1.getName() + ", " + r2.getName() + " and " + r3.getName() + ".");
                            Disconnection dis = new Disconnection(r1, r2, r3);
                            disconnectionCollector.addDisconnection(dis);
                        }

                        r1.open();
                        r2.open();
                        r3.open();
                    }

                }

            }

        }
    }

    /*
     public void setLoad2() {
     for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
     Road road = it.next();

     RoadsSelection rs = new RoadsSelection(1);
     rs.addRoad(road);
     this.roadsSelection.add(rs);

     }
     }
     * */
    public void setLoad3(RoadsSelection foreignRoadsSelection) {
        //this.roadsSelection.add(rs);

        RoadsSelection rs = new RoadsSelection();
        for (Iterator<Road> it1 = foreignRoadsSelection.getRoads().iterator(); it1.hasNext();) {
            Road road1 = it1.next();
            int id = road1.getId();
            Road temp = this.net.getRoad(id);
            rs.addRoad(temp);
        }
        this.roadsSelection.add(rs);
    }

    public void setLoad(List<RoadsSelection> lrs) {

        // for every given set of roads
        for (Iterator<RoadsSelection> it = lrs.iterator(); it.hasNext();) {
            RoadsSelection foreignRoadsSelection = it.next();

            // create new RoadSelection and clone to this roads which are in this instance of class
            RoadsSelection rs = new RoadsSelection();
            for (Iterator<Road> it1 = foreignRoadsSelection.getRoads().iterator(); it1.hasNext();) {
                Road road1 = it1.next();
                int id = road1.getId();
                Road temp = this.net.getRoad(id);
                rs.addRoad(temp);
            }

            this.roadsSelection.add(rs);
        }
    }

    /**
     * Run over all registered set of roads and do the check if its closing
     * leads to net disconnection.
     *
     */
    private void examineDisconnections() {
        // obecna metoda, ktera umi overovat rozpad bez zavislosti na poctu cest
        for (Iterator<RoadsSelection> it = roadsSelection.iterator(); it.hasNext();) {
            RoadsSelection rs = it.next();

            rs.closeAllRoads();

            // test if disconnection occur
            if (!net.isInOneComponent()) {
                //System.out.println(Thread.currentThread().getName() + ": Disconnected after close road " + rs.toString() + ".");
                Disconnection dis = new Disconnection(rs.getRoads());
                disconnectionCollector.addDisconnection(dis);
            }

            rs.openAllRoads();

        }
    }
}
