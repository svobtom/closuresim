package cz.muni.fi.closuresim;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * Class which leads finding disconnections.
 *
 * @author Tom
 */
public class AlgorithmSimpleParallel implements Algorithm {

    private static final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs;
    private Net net;
    protected DisconnectionCollector disconnectionCollector;
    protected static Set<Road> oneRoadToDisconnect = Collections.synchronizedSet(new HashSet());
    protected static Set<Set<Road>> setRoadsToDisconnect = Collections.synchronizedSet(new HashSet());

    public AlgorithmSimpleParallel(Net net, DisconnectionCollector dl) {
        this.net = net;
        this.disconnectionCollector = dl;

        AlgorithmSimpleParallelRunOne.disconnectionCollector = disconnectionCollector;
        AlgorithmSimpleParallelRunTwo.disconnectionCollector = disconnectionCollector;
        AlgorithmSimpleParallelRunThree.disconnectionCollector = disconnectionCollector;

        AlgorithmSimpleParallelRunnable.disconnectionCollector = disconnectionCollector;
    }

    @Override
    public void start(final int maxNumClosedRoads) {

        // set bounds of closed roads
        if (!(maxNumClosedRoads > 0 && maxNumClosedRoads <= 10)) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "Entered number of roads to close out of bounds");
            return;
        }

        // doing algorithm for one road, two roads, three roads ...
        for (int numOfRoads = 1; numOfRoads <= maxNumClosedRoads; numOfRoads++) {
            System.out.print("Discovering " + numOfRoads + " road(s) disconnections ... ");

            // choose procedure to cope with the number of recently closed roads
            switch (numOfRoads) {
                case 1:
                    findNewCombinationDisconnection(1);
                    break;

                case 2:
                    find1Rdisconnections(2);
                    //find1R1Rdisconnection(); // old
                    //find1RnRdisconnection(); // not desired
                    findNewCombinationDisconnection(2);
                    break;

                case 3:
                    find1Rdisconnections(3);
                    find2R1Rdisconnection(2);
                    //find1R1R1Rdisconnection(); // old
                    //find2R1Rdisconnecton(); // old
                    findNewCombinationDisconnection(3);
                    break;

                case 4:
                    find1Rdisconnections(4);
                    find2R1Rdisconnection(3);
                    find2Rdisconnection(2);
                    find3R1Rdisconnection(2);
                    //findNewCombinationDisconnection(4);
                    break;

                case 5:
                    find1Rdisconnections(5);
                    findNewCombinationDisconnection(5);
                    break;
            }

            ExperimentSetup.LOGGER.info("All " + numOfRoads + " road(s) disconnection found");
            System.out.println("Done (found " + disconnectionCollector.getNumberOfDisconnections(numOfRoads) + ")");
        }
    }

    private void findNewCombinationDisconnection(final int numOfClosedRoads) {

        // set bounds of closed roads for this method, must respond to switch cases
        if (!(numOfClosedRoads > 0 && numOfClosedRoads <= 4)) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "Desired number of close roads out of bounds");
            return;
        }

        // create array of runnable and thread
        AlgorithmSimpleParallelRunnable[] runnables = new AlgorithmSimpleParallelRunnable[NUMBER_OF_THREADS];
        //Runnable[] runnables = new Runnable[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {

            // inicialize runnable by specific algorithm modification
            runnables[i] = new AlgorithmSimpleParallelRunnable(net);


            // set thread to its runnable and name it
            threads[i] = new Thread(runnables[i]);
            threads[i].setName(Integer.toString(i));
        }

        // prepare runnables
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            runnables[i].prepare(numOfClosedRoads);
        }
        
        /*
        List<RoadsSelection> roadsSelection = getNCombination(numOfClosedRoads);


        // prideleni vlaknu mnoziny silnic, kterou bude pocitat
        List<RoadsSelection> loadToThread = new LinkedList<>();

        
        int ij = 0;
        for (Iterator<RoadsSelection> it = roadsSelection.iterator(); it.hasNext();) {
            RoadsSelection rs = it.next();

            runnables[ij % NUMBER_OF_THREADS].setLoad3(rs);
            ij++;
        }
*/
/*
        if (numOfClosedRoads != 1) {
            int roadsToOneThread = roadsSelection.size() / NUMBER_OF_THREADS;
            int lastStart = 0;
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                List<RoadsSelection> templist = new LinkedList<>();
                templist.addAll(roadsSelection.subList(lastStart, lastStart + roadsToOneThread));

                runnables[i].setLoad(roadsSelection.subList(lastStart, lastStart + roadsToOneThread));
                lastStart = roadsToOneThread;
            }
        } else {
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                runnables[i].setLoad2();

            }
        }
*/
        
        // start the threads
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads[i].start();
        }

        // wait until all threads end 
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ExperimentSetup.LOGGER.log(Level.SEVERE, "error while wating to threads to end", ex);
            }
        }

    }

    /*
    private List<RoadsSelection> getNCombination(int numOfClosedRoads) {
        List<RoadsSelection> roadsSelection = new LinkedList<>();
        switch (numOfClosedRoads) {
            case 1:
                for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
                    Road road = it.next();
                    RoadsSelection rs = new RoadsSelection(1);
                    rs.addRoad(road);
                    roadsSelection.add(rs);
                }
                break;

            case 2:
                for (Iterator<Road> it1 = net.getRoads().iterator(); it1.hasNext();) {
                    Road road1 = it1.next();
                    RoadsSelection rs = new RoadsSelection(1);
                    rs.addRoad(road1);

                    // if road1 disconnect net
                    if (disconnectionCollector.containDisconnection(rs)) {
                        continue;
                    }
                    for (Iterator<Road> it2 = net.getRoads().iterator(); it2.hasNext();) {
                        Road road2 = it2.next();

                        // if road2 disconnect net
                        RoadsSelection rs2 = new RoadsSelection(2);
                        rs2.addRoad(road2);
                        if (road2.equals(road1) || disconnectionCollector.containDisconnection(rs2)) {
                            continue;
                        }

                        rs2.addRoad(road1);
                        roadsSelection.add(rs2);
                    }
                }

            case 3:
                for (Iterator<Road> it1 = net.getRoads().iterator(); it1.hasNext();) {
                    Road road1 = it1.next();
                    RoadsSelection rs = new RoadsSelection(1);
                    rs.addRoad(road1);

                    // if road1 disconnect net
                    if (disconnectionCollector.containDisconnection(rs)) {
                        continue;
                    }
                    for (Iterator<Road> it2 = net.getRoads().iterator(); it2.hasNext();) {
                        Road road2 = it2.next();

                        // if road2 disconnect net
                        RoadsSelection rs2 = new RoadsSelection(2);
                        rs2.addRoad(road2);
                        if (road2.equals(road1) || disconnectionCollector.containDisconnection(rs2)) {
                            continue;
                        }

                        // if road1 and road2 disconenct
                        rs2.addRoad(road1);
                        if (disconnectionCollector.containDisconnection(rs2)) {
                            continue;
                        }


                        for (Iterator<Road> it3 = net.getRoads().iterator(); it3.hasNext();) {
                            Road road3 = it3.next();

                            // if road3 disconnect net
                            RoadsSelection rs3 = new RoadsSelection(3);
                            rs3.addRoad(road3);
                            if (road3.equals(road1) || road3.equals(road2) || disconnectionCollector.containDisconnection(rs3)) {
                                continue;
                            }

                            RoadsSelection rs3_1 = new RoadsSelection(2);
                            rs3_1.addRoads(road3, road1);
                            RoadsSelection rs3_2 = new RoadsSelection(2);
                            rs3_1.addRoads(road3, road2);
                            if (disconnectionCollector.containDisconnection(rs3_1) || disconnectionCollector.containDisconnection(rs3_2)) {
                                continue;
                            }
                            rs3.addRoad(road1);
                            rs3.addRoad(road2);
                            roadsSelection.add(rs3);
                        }
                    }
                }

        }
        return roadsSelection;
    }
    */

    /**
     * Find all disconnection. Find all disconnection which are made by
     * gradually adding one-disconnection roads.
     *
     * @param count number of one-disconnection roads to combine
     */
    private void find1Rdisconnections(final int count) {
        // get all roads which make one-road disconnection
        Set<Road> oneRoadDisSet = new HashSet<>();
        for (Iterator<Disconnection> it = disconnectionCollector.getDisconnections(1).iterator(); it.hasNext();) {
            Disconnection dis = it.next();
            oneRoadDisSet.addAll(dis.getRoads());
        }

        // call auxiliary method
        find1RdisconnectionsRec(count, disconnectionCollector.getDisconnections(1), oneRoadDisSet);
    }

    /**
     * Auxiliary recursion method. It can combine one-disconnection roads to
     * known disconnections.
     *
     * @param count number of one-disconnection roads to combine
     * @param recent yet found one road combination disconnection
     * @param disconnection1R all roads which make one-disconnection
     */
    private void find1RdisconnectionsRec(final int count, Set<Disconnection> recent, Set<Road> disconnection1R) {
        // if we want 1 x 1R disconnection, no need to combine anything 
        if (count == 1) {
            return;
        }

        // here will by new found disconnection stored
        Set<Disconnection> newSetOfDis = new HashSet<>();

        // for every recent known disconnection
        for (Iterator<Disconnection> it = recent.iterator(); it.hasNext();) {
            Disconnection dis = it.next();

            // add to it (recent known disconnection) another one-disconnection road
            for (Iterator<Road> it1 = disconnection1R.iterator(); it1.hasNext();) {
                Road road = it1.next();

                // create new set of closed roads
                Set<Road> sr = new HashSet<>();
                sr.addAll(dis.getRoads());
                sr.add(road);

                // register new disconnection
                Disconnection newDis = new Disconnection(sr);
                newSetOfDis.add(newDis);
                disconnectionCollector.addDisconnection(newDis);
            }
        }

        // call this method again to add next one-disconnection road to all yet found disconnection
        find1RdisconnectionsRec(count - 1, newSetOfDis, disconnection1R);
    }

    /**
     * Find one two-road and more one-road disconnection. Find one two-road
     * disconnection and combine it with specific number of one-road
     * disconnection.
     *
     * @param count specific how many disconnection would combine
     */
    private void find2R1Rdisconnection(final int count) {
        // get all roads which make one-road disconnection
        Set<Road> oneRoadDisSet = new HashSet<>();
        for (Iterator<Disconnection> it = disconnectionCollector.getDisconnections(1).iterator(); it.hasNext();) {
            Disconnection dis = it.next();
            oneRoadDisSet.addAll(dis.getRoads());
        }

        // call auxiliary method
        find1RdisconnectionsRec(count, disconnectionCollector.getDisconnections(2), oneRoadDisSet);
    }

    /**
     * Find one three-road and more one-road disconnection. Find one three-road
     * disconnection and combine it with specific number of one-road
     * disconnection.
     *
     * @param count specific how many disconnection would combine
     */
    private void find3R1Rdisconnection(final int count) {
        // get all roads which make one-road disconnection
        Set<Road> oneRoadDisSet = new HashSet<>();
        for (Iterator<Disconnection> it = disconnectionCollector.getDisconnections(1).iterator(); it.hasNext();) {
            Disconnection dis = it.next();
            oneRoadDisSet.addAll(dis.getRoads());
        }

        // call auxiliary method
        find1RdisconnectionsRec(count, disconnectionCollector.getDisconnections(3), oneRoadDisSet);
    }

    /**
     * Find more two-road disconnection. Find chain of two-road disconnections.
     *
     * @param count number of desired two-road disconnection
     */
    private void find2Rdisconnection(final int count) {
        // get all two-road disconnection
        Set<Disconnection> twoRoadDis = new HashSet<>();
        for (Iterator<Disconnection> it = disconnectionCollector.getDisconnections(2).iterator(); it.hasNext();) {
            Disconnection dis = it.next();
            twoRoadDis.add(dis);
        }

        // call auxiliary recursion method
        find2RdisconnectionsRec(count, disconnectionCollector.getDisconnections(1), twoRoadDis);
    }

    /**
     * Auxiliary recursive method.
     *
     * @param count
     * @param recent
     * @param disconnection2R
     */
    private void find2RdisconnectionsRec(final int count, Set<Disconnection> recent, Set<Disconnection> disconnection2R) {
        // if we want 1 x 2R disconnection, no need to combine anything 
        if (count == 1) {
            return;
        }

        // here will by new found disconnection stored
        Set<Disconnection> newSetOfDis = new HashSet<>();

        // for every recent known disconnection
        for (Iterator<Disconnection> it = recent.iterator(); it.hasNext();) {
            Disconnection dis = it.next();

            // add to it another two-road disconnection
            for (Iterator<Disconnection> it1 = disconnection2R.iterator(); it1.hasNext();) {
                Disconnection dis1 = it1.next();

                // create new set of closed roads
                Set<Road> sr = new HashSet<>();
                sr.addAll(dis.getRoads());
                sr.addAll(dis1.getRoads());

                // create and register new disconnection
                Disconnection newDis = new Disconnection(sr);
                newSetOfDis.add(newDis);
                disconnectionCollector.addDisconnection(newDis);
            }
        }

        // call this method again to add next two-road disconnection to all yet found disconnection
        find2RdisconnectionsRec(count - 1, newSetOfDis, disconnection2R);
    }

    /*  *********** Old unused methods ***********  */
    private void find2R1Rdisconnecton() {
        Set<Disconnection> temp1Rdisconnection = disconnectionCollector.getDisconnections(1);
        Set<Disconnection> temp2Rdisconnection = disconnectionCollector.getDisconnections(2);

        for (Iterator<Disconnection> it = temp1Rdisconnection.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            for (Iterator<Disconnection> it1 = temp2Rdisconnection.iterator(); it1.hasNext();) {
                Disconnection disconnection1 = it1.next();
                if (!disconnection1.equals(disconnection)) {
                    Set<Road> setRoads = new HashSet<>();
                    setRoads.addAll(disconnection.getRoads());
                    setRoads.addAll(disconnection1.getRoads());
                    Disconnection newDis = new Disconnection(setRoads);
                    this.disconnectionCollector.addDisconnection(newDis);
                }
            }
        }
    }

    /**
     * Find 2R disconnection by combine two 1R disconnection.
     */
    private void find1R1Rdisconnection() {
        Set<Disconnection> temp1Rdisconnection = disconnectionCollector.getDisconnections(1);

        for (Iterator<Disconnection> it = temp1Rdisconnection.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            for (Iterator<Disconnection> it1 = temp1Rdisconnection.iterator(); it1.hasNext();) {
                Disconnection disconnection1 = it1.next();
                if (!disconnection1.equals(disconnection)) {
                    Set<Road> setRoads = new HashSet<>();
                    setRoads.addAll(disconnection.getRoads());
                    setRoads.addAll(disconnection1.getRoads());
                    Disconnection newDis = new Disconnection(setRoads);
                    this.disconnectionCollector.addDisconnection(newDis);
                }
            }
        }
    }

    /**
     * Find 2R disconnection by combine all 1R disconnection with all roads
     * which don't make disconnection.
     */
    private void find1RnRdisconnection() {
        for (Iterator<Disconnection> it = disconnectionCollector.getDisconnections(1).iterator(); it.hasNext();) {
            Disconnection dis = it.next();
            for (Iterator<Road> it2 = net.getRoads().iterator(); it2.hasNext();) {
                Road road = it2.next();
                // register disconnection
                RoadsSelection rs = new RoadsSelection(2);
                rs.addRoads(dis.getRoads());
                rs.addRoad(road);
                Disconnection newDis = new Disconnection(rs.getRoads());
                this.disconnectionCollector.addDisconnection(newDis);
            }
        }
    }

    /**
     * Find 3R disconnection by combine three 1R disconnection.
     */
    private void find1R1R1Rdisconnection() {
        Set<Disconnection> temp1Rdisconnection = disconnectionCollector.getDisconnections(1);

        for (Iterator<Disconnection> it = temp1Rdisconnection.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            for (Iterator<Disconnection> it1 = temp1Rdisconnection.iterator(); it1.hasNext();) {
                Disconnection disconnection1 = it1.next();
                for (Iterator<Disconnection> it2 = temp1Rdisconnection.iterator(); it2.hasNext();) {
                    Disconnection disconnection2 = it2.next();

                    if (!disconnection1.equals(disconnection)
                            && !disconnection2.equals(disconnection)
                            && !disconnection1.equals(disconnection2)) {
                        Set<Road> setRoads = new HashSet<>();
                        setRoads.addAll(disconnection.getRoads());
                        setRoads.addAll(disconnection1.getRoads());
                        setRoads.addAll(disconnection2.getRoads());
                        Disconnection newDis = new Disconnection(setRoads);
                        this.disconnectionCollector.addDisconnection(newDis);
                    }

                }
            }
        }
    }
}
