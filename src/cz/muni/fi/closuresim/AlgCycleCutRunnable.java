package cz.muni.fi.closuresim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.jgrapht.Graph;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.Multigraph;

/**
 *
 * @author Tom
 */
public class AlgCycleCutRunnable implements Runnable {

    private final Net net;
    private final List<Disconnection> disconnections;
    private final DisconnectionCollector disconnectionCollector;
    private final int maxNumberOfRoadsToClose;
    private final int maxNumberOfComponents;
    private final boolean findOnlyAccurateDisconnection;

    public AlgCycleCutRunnable(Net net, DisconnectionCollector dc, final int roads, final int comp, final boolean findOnlyAccurateDisconnection) {
        this.net = net.clone();
        this.disconnections = new LinkedList<>();
        this.disconnectionCollector = dc;
        this.maxNumberOfRoadsToClose = roads;
        this.maxNumberOfComponents = comp;
        this.findOnlyAccurateDisconnection = findOnlyAccurateDisconnection;
    }

    @Override
    public void run() {
        ExperimentSetup.LOGGER.log(Level.INFO, "Thread {0} started.", Thread.currentThread().getName());

        // vlakna paralelne provedou algoritmus pocinaje od kazde cesty
        while (!AlgorithmCycleCut.queueOfRoads.isEmpty()) {
            final Road roadToStart = AlgorithmCycleCut.queueOfRoads.poll();
            if (roadToStart != null) {
                // get the road in the cloned net
                final Road cRoadToStart = this.net.getRoad(roadToStart.getId());

                final Set<Road> bannedRoads = new HashSet<>();
                bannedRoads.add(cRoadToStart);

                // 1. Zvolim hranu e (jednu vybranou cestu)
                theFindCyclesCutAlgorithm(bannedRoads, cRoadToStart, 1, true, false);

                // add found disconnections by one run of the algorithm
                final int numFoundDis = this.disconnections.size();
                this.disconnectionCollector.addDisconnections(this.disconnections);
                this.disconnections.clear();
                ExperimentSetup.LOGGER.log(Level.INFO, "Road " + cRoadToStart.getName() + " was processed by thread {0}. Found " + numFoundDis + " disconnections.", Thread.currentThread().getName());
            }
        }

        ExperimentSetup.LOGGER.log(Level.INFO, "Thread {0} end.", Thread.currentThread().getName());
    }

    /**
     * Do the algorithm.
     *
     * @param bannedRoads set of banned roads
     * @param road road which was chosen in the cycle
     */
    private void theFindCyclesCutAlgorithm(final Set<Road> bannedRoads, final Road road, final int components, boolean recComp, boolean skip) {

        List<Road> path = new LinkedList<>();

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

        // Does path exist?
        if (path.isEmpty()) {
            // path doesn't exist

            final Graph g = new Multigraph(Road.class);

            // add vertices
            for (Node n : this.net.getNodes()) {
                g.addVertex(n);
            }

            // add roads except F roads / no F roads are int the graph
            for (Road r : this.net.getRoads()) {
                if (!bannedRoads.contains(r)) {
                    g.addEdge(r.getFirst_node(), r.getSecond_node(), r);
                }
            }

            // for each road in F - add one F edge in every run of the cycle
            for (Road r : bannedRoads) {
                g.addEdge(r.getFirst_node(), r.getSecond_node(), r);

                //System.out.println("z " + bannedRoads);
                //System.out.println("g " + g.edgeSet());
                //System.out.println("");
                KruskalMinimumSpanningTree<Node, Road> st = new KruskalMinimumSpanningTree<>(g);

                // get road on the spanning tree/forest
                Set<Road> kostra = st.getEdgeSet();
                //System.out.println("ko " + kostra.toString());

                // check if edges of the spanning tree make tree and all verticles are involve               
                Net kostraNet = this.net.clone();
                kostraNet.clearRoads();

                // roads from spanning tree are given back
                for (Road road1 : kostra) {
                    // set road
                    Road nR = new Road();
                    nR.setId(road1.getId());
                    nR.setName(road1.getName());

                    nR.setFirst_node(kostraNet.getNode(road1.getFirst_node().getId()));
                    nR.setSecond_node(kostraNet.getNode(road1.getSecond_node().getId()));
                    kostraNet.addRoad(nR);

                    // pridani do seznamu na uzlech
                    kostraNet.getNode(road1.getFirst_node().getId()).addRoad(nR);
                    kostraNet.getNode(road1.getSecond_node().getId()).addRoad(nR);

                }

                if (!kostraNet.isInOneComponent()) {
                    //System.out.println("kostra NEtvori strom ale les");
                    Disconnection dis = new Disconnection(bannedRoads);
                    disconnections.add(dis);

                    // recursive finding disconnections to more components
                    if ((components + 2) < maxNumberOfComponents) {
                        final Set<Road> allowedRoads = new HashSet<>(net.getRoads());
                        allowedRoads.removeAll(bannedRoads);

                        // for every recently not banned road
                        for (Iterator<Road> it = allowedRoads.iterator(); it.hasNext();) {
                            final Road allowedRoad = it.next();
                            theFindCyclesCutAlgorithm(bannedRoads, allowedRoad, components + 1, true, true);
                        }
                    }

                } else {
                    //System.out.println("kostra tvori strom");

                    for (Road road_Tf : kostra) {

                        // T-f therefore skip
                        if (road_Tf.equals(r)) {
                            continue;
                        }

                        Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                        newBannedRoads.add(road_Tf);

                        theFindCyclesCutAlgorithm(bannedRoads, road_Tf, components, true, true);

                    }
                }

                //System.out.println("Road: " + road + ", banned: " + bannedRoads);
                //System.out.println("Kostra: " + st.getEdgeSet());
                //System.out.println();
                g.removeEdge(r);

            } // end of for each road in F

        } else {

            // cesta existuje
            // pro kazdou cestu na nejkratsi kruznici z a do b
            for (final Road roadInPath : path) {
                // vytvorime nove zakazane cesty, tak ze k jiz soucasnym zakazanym pridame cesty, ktere jsou na prave nalezene kruznici
                Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                newBannedRoads.add(roadInPath);

                // omezeni maximalniho poctu uzaviranych silnic
                if ((newBannedRoads.size()) <= maxNumberOfRoadsToClose) {
                    // spustime algoritmus rekurzivne
                    theFindCyclesCutAlgorithm(newBannedRoads, roadInPath, components, false, false);
                }
            }

        }
    }

    /**
     * Find shortest path using dijkstra algorithm.
     *
     * @param source source node
     * @param target target node
     * @param bannedRoads set of roads which are banned/closed
     * @return List<Road> list of roads on the shortest path
     */
    private List<Road> findShortestPath(final Node source, final Node target, final Set<Road> bannedRoads) {

        final List<Road> listOfRoadsOnThePath = new LinkedList<>();

        // nalezeni nejkratsi cestu z a do b bez pouziti cest v bannedRoads
        final Map<Node, NodeAndRoad> mapOfAncestors = dijkstra(source, target, bannedRoads);

        // cesta neexistuje
        if (!mapOfAncestors.containsKey(target)) {
            return listOfRoadsOnThePath;
        }

        // rekonstruuj nalezenou nejkratsi cestu z mapy predchudcu
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
     * Dijkstra algorithm.
     *
     * @param source start node
     * @param target target node
     * @param bannedRoads set of roads which are banned on the returned path
     * @return map of nodes and its ancestors on the optimal path
     */
    private Map<Node, NodeAndRoad> dijkstra(final Node source, final Node target, final Set<Road> bannedRoads) {
        final Map<Node, Integer> distance = new HashMap<>();
        final Map<Node, Boolean> visited = new HashMap<>();
        final Map<Node, NodeAndRoad> previousRoad = new HashMap<>(); // previous node and road in the best path
        //Map<Node, Node> previous = new HashMap<>(); // previous node in the best path

        // init nodes
        for (final Node node : net.getNodes()) {
            distance.put(node, Integer.MAX_VALUE);
            visited.put(node, Boolean.FALSE);
        }

        // start with the source node
        distance.put(source, 0);

        final Set<Node> queue = new HashSet<>(); // final List<Node> queue = new LinkedList<>();
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

            for (final Road r : u.getRoads()) { // for (final Road r : roadToFor) { // 

                // skip closed and banned roads
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
