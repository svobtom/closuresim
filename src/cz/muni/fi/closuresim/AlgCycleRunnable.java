package cz.muni.fi.closuresim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Body of the cycle algorithm.
 *
 * @author Tom
 */
public class AlgCycleRunnable implements Runnable {

    private final Net net;
    private final Set<Disconnection> disconnections;
    private final DisconnectionCollector disconnectionCollector;
    private final int maxNumberOfRoadsToClose;
    private final int maxNumberOfComponents;
    private final boolean findOnlyAccurateDisconnection;

    public AlgCycleRunnable(Net net, DisconnectionCollector dc, final int roads, final int comp, final boolean findOnlyAccurateDisconnection) {
        this.net = net.clone();
        this.disconnections = new HashSet<>();
        this.disconnectionCollector = dc;
        this.maxNumberOfRoadsToClose = roads;
        this.maxNumberOfComponents = comp;
        this.findOnlyAccurateDisconnection = findOnlyAccurateDisconnection;
    }

    @Override
    public void run() {
        ExperimentSetup.LOGGER.log(Level.INFO, "Thread {0} started.", Thread.currentThread().getName());

        // threads do the algorithm parallel starting from each road
        while (!AlgorithmCycle.queue.isEmpty()) {
            // get the next road from the queue
            final Road roadToStart = AlgorithmCycle.queue.poll();
            if (roadToStart != null) {
                // get the road in the cloned net
                final Road cRoadToStart = this.net.getRoad(roadToStart.getId());

                // create the R set - bannedRoads
                final Set<Road> bannedRoads = new HashSet<>();
                bannedRoads.add(cRoadToStart);

                // 1. choosing edge e
                theFindCyclesAlgorithm(bannedRoads, cRoadToStart, 1, false);

                // add found disconnections by one run of the algorithm to the disconnection collector
                final int numFoundDis = this.disconnections.size();
                this.disconnectionCollector.addDisconnections(this.disconnections);
                this.disconnections.clear();
                ExperimentSetup.LOGGER.log(Level.INFO, "Road " + cRoadToStart.getName() + " was processed by thread {0}. Found " + numFoundDis + " disconnections.", Thread.currentThread().getName());
            }
        }

        ExperimentSetup.LOGGER.log(Level.INFO, "Thread {0} end.", Thread.currentThread().getName());
    }

    /**
     * The algorithm itself.
     *
     * @param bannedRoads set of banned roads
     * @param road road which was chosen in the cycle
     */
    private void theFindCyclesAlgorithm(final Set<Road> bannedRoads, final Road road, final int components, boolean recComp) {

        /*
         // find the shortest path from A to B without use banned roads
         // A, B are verticles of chosen road, this road belongs to banned roads
         // trivial path (A, B) isn't found        
         //List<Road> path = findShortestPath(road.getFirst_node(), road.getSecond_node(), bannedRoads); // earlier solution
         // zamenit za for pres vsechny bannedRoads
         // nove najit nejkratsi cestu z banndroad, to se hodi na vystup, budu pridavat ke stavajici mnozine, pres vsechny cesty jednu hranu z nich
         */
        // find the shortest cycle passing thru just one road from banned roads and avoid the others 
        List<Road> path = new LinkedList<>();

        // if not go recursively to more components
        if (!recComp) {
            int minLength = Integer.MAX_VALUE;
            for (Road chosenRoad : bannedRoads) {
                final List<Road> tempPath;
                tempPath = findShortestPath(chosenRoad.getFirst_node(), chosenRoad.getSecond_node(), bannedRoads);

                // set the shortest cycle
                if (!tempPath.isEmpty() && tempPath.size() <= minLength) {
                    path = tempPath;
                    minLength = tempPath.size();

                }
            }
        } else {
            path = findShortestPath(road.getFirst_node(), road.getSecond_node(), bannedRoads);
        }

        // Does the path exist?
        if (path.isEmpty()) {
            // The path doesn't exist. We have cut. Put it down
            // if we don't want disconnection by fewer roads, skip putting down 
            if (!findOnlyAccurateDisconnection || bannedRoads.size() >= maxNumberOfRoadsToClose) {
                Disconnection dis = new Disconnection(bannedRoads);

                /*
                 // only for testing
                 Set<Road> testSet = new HashSet<>();
                 testSet.add(this.net.getRoad(24));
                 testSet.add(this.net.getRoad(26));
                 testSet.add(this.net.getRoad(25));
                 Disconnection testDis = new Disconnection(testSet);
                 if (dis.equals(testDis)) {
                 System.out.println("For debug");
                 }
                 */
                //disconnectionCollector.addDisconnection(dis);
                disconnections.add(dis);
            }

            // recursive finding disconnections to more components
            if ((components + 1) < maxNumberOfComponents) {
                final Set<Road> allowedRoads = new HashSet<>(net.getRoads());
                allowedRoads.removeAll(bannedRoads);

                // for every recently not banned road
                for (Iterator<Road> it = allowedRoads.iterator(); it.hasNext();) {
                    final Road allowedRoad = it.next();
                    theFindCyclesAlgorithm(bannedRoads, allowedRoad, components + 1, true);
                }
            }

        } else {
            // The path exists. We haven't got the cut.

            // limit maximal number of closed roads
            if ((bannedRoads.size()) < maxNumberOfRoadsToClose) {

                // for every road on the shortest cycle (from A to B)
                for (final Road roadInPath : path) {
                    // vytvorime nove zakazane cesty, tak ze k jiz soucasnym zakazanym pridame cesty, ktere jsou na prave nalezene kruznici
                    // create new banned roads (add roads on the found cycle to recently banned roads)
                    Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                    newBannedRoads.add(roadInPath);

                    /*
                     // only for testing
                     if (bannedRoads.contains(this.net.getRoad(25)) && bannedRoads.contains(this.net.getRoad(24)) && roadInPath.equals(this.net.getRoad(26))) {
                     System.out.println("For debug, road");
                     }
                     */
                    //  run the algorithm recursively
                    theFindCyclesAlgorithm(newBannedRoads, roadInPath, components, false);
                }
            }
        }
    }

    /**
     * Find shortest path between specified nodes avoiding banned roads.
     *
     * @param source source node
     * @param target target node
     * @param bannedRoads set of roads which are banned
     * @return List<Road> list of roads on the shortest path, if the path
     * doesn't exist the empty list is returned
     */
    private List<Road> findShortestPath(final Node source, final Node target, final Set<Road> bannedRoads) {

        // path to return, empty yet
        final List<Road> listOfRoadsOnThePath = new LinkedList<>();

        // get map of ancestors for reconstruction path from source to target 
        final Map<Node, NodeAndRoad> mapOfAncestors = dijkstra(source, target, bannedRoads);

        // road doesn't exist
        if (!mapOfAncestors.containsKey(target)) {
            return listOfRoadsOnThePath;
        }

        // reconstruction path from source to target from map
        Node recent = target;
        NodeAndRoad previous;
        while (!recent.equals(source)) {
            previous = mapOfAncestors.get(recent);

            listOfRoadsOnThePath.add(previous.getRoad());
            recent = previous.getNode();
        }

        return listOfRoadsOnThePath;
    }

    /**
     * Dijkstra algorithm modified to avoid banned roads.
     *
     * @param source start node
     * @param target target node
     * @param bannedRoads set of roads which are banned on the found path
     * @return map of nodes and its ancestors on the optimal path
     */
    private Map<Node, NodeAndRoad> dijkstra(final Node source, final Node target, final Set<Road> bannedRoads) {
        final Map<Node, Integer> distance = new HashMap<>();
        final Map<Node, Boolean> visited = new HashMap<>();
        final Map<Node, NodeAndRoad> previousRoad = new HashMap<>(); // previous node and road in the best path

        // init nodes
        for (final Node node : net.getNodes()) {
            distance.put(node, Integer.MAX_VALUE);
            visited.put(node, Boolean.FALSE);
        }

        // start with the source node
        distance.put(source, 0);

        final Set<Node> queue = new HashSet<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            Node u = null; // node with smallest distance, not visited
            int smallestDistance = Integer.MAX_VALUE;

            // find smallest distance
            for (Iterator<Node> it = queue.iterator(); it.hasNext();) {
                final Node node = it.next();

                if (!visited.get(node) && distance.get(node) < smallestDistance) {
                    smallestDistance = distance.get(node);
                    u = node;
                }
            }

            // if we closed target node
            if (target.equals(u)) {
                return previousRoad;
            }

            queue.remove(u);
            visited.put(u, Boolean.TRUE);

            for (final Road r : u.getRoads()) {

                // skip banned roads
                if (bannedRoads.contains(r)) {
                    continue;
                }

                final Node v = r.getOppositeNode(u);
                final int alt = distance.get(u) + 1; // accumulate shortest distance, dist[u] + dist_between(u, v)
                if (alt < distance.get(v) && !visited.get(v)) {
                    distance.put(v, alt);
                    //previous.put(v, u);
                    previousRoad.put(v, new NodeAndRoad(u, r));
                    queue.add(v);
                }
            } // end for
        } // end while
        return previousRoad;

    }
}
