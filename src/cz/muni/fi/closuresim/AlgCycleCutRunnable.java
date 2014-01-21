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
import org.jgrapht.graph.UndirectedSubgraph;

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
                theFindCyclesCutAlgorithm(bannedRoads, cRoadToStart, 1);

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
    private void theFindCyclesCutAlgorithm(final Set<Road> bannedRoads, final Road road, final int components) {

        // najde nejkratsi cestu z a do b, bez pouziti cest z bannedRoads
        // a, b jsou vrcholy vybrane cesty, samotna cesta jiz patri do banned roads, takze nenalezne trivialni cestu
        // obsahujici pouze prave tuto cestu z a do b
        final List<Road> path = findShortestPath(road.getFirst_node(), road.getSecond_node(), bannedRoads);

        // existuje cesta?
        if (path.isEmpty()) {
            // neexistuje

            /*
             // register disconnection
             Disconnection dis = new Disconnection(bannedRoads);
             disconnections.add(dis);
             */
            final Graph g = new Multigraph(Road.class);

            // add vertices
            for (Node n : this.net.getNodes()) {
                g.addVertex(n);
            }

            // add roads
            for (Road r : this.net.getRoads()) {
                g.addEdge(r.getFirst_node(), r.getSecond_node(), r);
            }

            // delete F roads - tedka v grafu nejsou zadne F hrany
            for (Road r : bannedRoads) {
                g.removeEdge(r);
            }

            // for each road in F - pridame jednu F hranu pro kazdy beh
            for (Road r : bannedRoads) {
                g.addEdge(r.getFirst_node(), r.getSecond_node(), r);

                KruskalMinimumSpanningTree<Node, Road> st = new KruskalMinimumSpanningTree<>(g);

                Set<Road> kostra = st.getEdgeSet();
                
                Net tempNet = new Net();
                tempNet = this.net.clone();
                tempNet.clearRoads();
                
                System.out.println(kostra.toString());
                
                // roads from spnning tree are given back
                for (Road road1 : kostra) {
                    tempNet.addRoad(road1);
                    
                    tempNet.getNode(road1.getFirst_node().getId()).addRoad(road1);
                    tempNet.getNode(road1.getSecond_node().getId()).addRoad(road1);
                }
                                
                
                if (!tempNet.isInOneComponent()) {
                    System.out.println("neeeenaslo");
                    Disconnection dis = new Disconnection(bannedRoads);
                    disconnections.add(dis);
                } else {
                    System.out.println("naslo");

                    for (Road road_Tf : st.getEdgeSet()) {

                        Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                        newBannedRoads.add(road_Tf);

                        theFindCyclesCutAlgorithm(bannedRoads, road_Tf, components);

                    }

                }
                System.out.println("Road: " + road + ", banned: " + bannedRoads);
                System.out.println("Kostra: " + st.getEdgeSet());
                System.out.println();

                g.removeEdge(r);
                
            } // end of for each road in F

        } else {

            // cesta existuje, rez zatim nemame
            // pro kazdou cestu na nejkratsi kruznici z a do b
            for (final Road roadInPath : path) {
                // vytvorime nove zakazane cesty, tak ze k jiz soucasnym zakazanym pridame cesty, ktere jsou na prave nalezene kruznici
                Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                newBannedRoads.add(roadInPath);

                // omezeni maximalniho poctu uzaviranych silnic
                if ((newBannedRoads.size()) <= maxNumberOfRoadsToClose) {
                    // spustime algoritmus rekurzivne
                    theFindCyclesCutAlgorithm(newBannedRoads, roadInPath, components);
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

