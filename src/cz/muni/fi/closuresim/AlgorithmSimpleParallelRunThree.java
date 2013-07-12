package cz.muni.fi.closuresim;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Tom
 */
public class AlgorithmSimpleParallelRunThree implements Runnable {

    private Net net;
    protected static DisconnectionCollector disconnectionCollector;
    private static Set<Set<Road>> doneThreeRoads = Collections.synchronizedSet(new HashSet<Set<Road>>());
    private static Set<Road> workedFirstRoad = Collections.synchronizedSet(new HashSet<Road>());

    public AlgorithmSimpleParallelRunThree(Net net) {
        this.net = net.clone();
    }

    @Override
    public void run() {
        //testCloseThreeRoads();
        testCloseThreeRoads2();
    }

    private void testCloseThreeRoads() {

        for (Iterator<Road> it1 = this.net.getRoads().iterator(); it1.hasNext();) {
            Road r1 = it1.next();
            if (AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r1)) {
                continue;
            }

            if (!workedFirstRoad.add(r1)) {
                continue;
            }

            r1.close();

            for (Iterator<Road> it2 = this.net.getRoads().iterator(); it2.hasNext();) {
                Road r2 = it2.next();
                Set<Road> toCheck = new HashSet<Road>();
                toCheck.add(r1);
                toCheck.add(r2);
                if (!r1.equals(r2) && !AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r2) && !AlgorithmSimpleParallel.setRoadsToDisconnect.contains(toCheck)) {
                    r2.close();
                    for (Iterator<Road> it3 = this.net.getRoads().iterator(); it3.hasNext();) {
                        Road r3 = it3.next();
                        Set<Road> toCheck2 = new HashSet<Road>();
                        toCheck2.add(r1);
                        toCheck2.add(r3);
                        Set<Road> toCheck3 = new HashSet<Road>();
                        toCheck3.add(r2);
                        toCheck3.add(r3);
                        if (!r1.equals(r3) && !r2.equals(r3) && !AlgorithmSimpleParallel.oneRoadToDisconnect.contains(r3) && !AlgorithmSimpleParallel.setRoadsToDisconnect.contains(toCheck2) && !AlgorithmSimpleParallel.setRoadsToDisconnect.contains(toCheck3)) {
                            Set<Road> setRoads = new HashSet<Road>();
                            setRoads.add(r1);
                            setRoads.add(r2);
                            setRoads.add(r3);
                            if (doneThreeRoads.add(setRoads)) {
                                r3.close();
                                int numOfComp = net.getNumOfComponents();
                                if (numOfComp > 1) {
                                    double variance = net.getValueOfBadness(numOfComp);
                                    System.out.println(Thread.currentThread().getName() + ": Disconnected after close roads " + r1.getId() + ", " + r2.getId() + " and " + r3.getId() + " (" + variance + "). ");

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
        Set<RoadsSelection> setRs = Collections.synchronizedSet(new HashSet<RoadsSelection>());

        for (Iterator<Road> it1 = this.net.getRoads().iterator(); it1.hasNext();) {
            Road r1 = it1.next();

            for (Iterator<Road> it2 = this.net.getRoads().iterator(); it2.hasNext();) {
                Road r2 = it2.next();

                if (r2.equals(r1)) {
                    continue;
                }

                for (Iterator<Road> it3 = this.net.getRoads().iterator(); it3.hasNext();) {
                    Road r3 = it3.next();

                    if (r3.equals(r1) || r3.equals(r2)) {
                        continue;
                    }

                    RoadsSelection rs = new RoadsSelection(3);
                    rs.addRoads(r1, r2, r3);
                    if (setRs.add(rs)) {
                        r1.close();
                        r2.close();
                        r3.close();
                        
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
}
