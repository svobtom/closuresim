package cz.muni.fi.closuresim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.Multigraph;

/**
 *
 * @author Tom
 */
class AlgorithmTest implements Algorithm {

    private Net net;

    public AlgorithmTest(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net.clone();
    }

    @Override
    public void start(int maxClosedRoads) {

        varianta1();
        //varianta2();

    }

    private void varianta1() {

        Set<Road> noBannedRoads = new HashSet<>();

        for (Node n1 : this.net.getNodes()) {

            for (Node n2 : this.net.getNodes()) {

                if (!n1.equals(n2)) {

                    List<Road> path = findShortestPath(n1, n2, noBannedRoads);

                    System.out.print(path.size() + " ");

                }

            }
            System.out.println();
        }

    }

    private void varianta2() {

        // dat do struktury jGrapht
        Graph<Node, Road> graph = new Multigraph(Road.class);

        // add vertices
        for (Node n : this.net.getNodes()) {
            graph.addVertex(n);
        }

        // add roads
        for (Road r : this.net.getRoads()) {
            graph.addEdge(r.getFirst_node(), r.getSecond_node(), r);
        }

        for (Node n1 : this.net.getNodes()) {

            for (Node n2 : this.net.getNodes()) {

                if (!n1.equals(n2)) {
                    
                    DijkstraShortestPath<Node, Road> dsp = new DijkstraShortestPath<>(graph, n1, n2);

                    System.out.print(dsp.getPathLength() + " ");

                }

            }
            System.out.println();
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
