package cz.muni.fi.closuresim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Tom
 */
public class AlgCycleRunnable implements Runnable {

    private Net net;
    private DisconnectionCollector disconnectionCollector;

    public AlgCycleRunnable(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net.clone();
        this.disconnectionCollector = disconnectionCollector;
    }

    @Override
    public void run() {

        // vlakna paralelne provedou algoritmus pocinaje od kazde cesty
        while (!AlgorithmCycle.queue.isEmpty()) {
            Road roadToStart = AlgorithmCycle.queue.poll();
            if (roadToStart != null) {
                Set<Road> bannedRoads = new HashSet<>();
                bannedRoads.add(roadToStart);
                // 1. Zvolim hranu e (jednu vybranou cestu)
                theFindCyclesAlgorithm(bannedRoads, roadToStart);
            }
        }
        
    }

    /**
     * Do the algorithm.
     *
     * @param bannedRoads set of banned roads
     * @param road road which was chosen in the cycle
     */
    private void theFindCyclesAlgorithm(Set<Road> bannedRoads, Road road) {

        // najde nejkratsi cestu z a do b, bez pouziti cest z bannedRoads
        // a, b jsou vrcholy vybrane cesty, samotna cesta jiz patri do banned roads, takze nenalezne trivialni cestu
        // obsahujici pouze prave tuto cestu
        List<Road> path = findShortestPath(road.getFirst_node(), road.getSecond_node(), bannedRoads);

        /*
         System.out.println("Roads: ");
         for (Road road1 : path) {
         System.out.print(road1 + " ");
         }
         System.out.println();
         */

        // existuje cesta?
        if (path.isEmpty()) {
            // cesta neexistuje, mame rez, poznacime si ho
            Disconnection dis = new Disconnection(bannedRoads);
            disconnectionCollector.addDisconnection(dis);
        } else {
            // cesta existuje, rez zatim nemame

            // pro kazdou cestu na nejkratsi kruznici
            for (Road roadInPath : path) {
                // vytvorime nove zakazane cesty, tak ze k jiz soucasnym zakazanym pridame cesty, ktere jsou na prave nalezene kruznici
                Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                newBannedRoads.add(roadInPath); //newBannedRoads.addAll(path);

                // spustime algoritmus rekurzivne
                theFindCyclesAlgorithm(newBannedRoads, roadInPath);
            }
        }
    }

    /**
     * Find shortest path using dijkstra algorithm.
     *
     * @param source source node
     * @param target target node
     * @param bannedRoads set of banned roads
     * @return
     */
    private List<Road> findShortestPath(Node source, Node target, Set<Road> bannedRoads) {

        //System.out.println(source + " => " + target);

        // nalezeni nejkratsi cestu z a do b bez pouziti cest v bannedRoads
        Map<Node, NodeAndRoad> mapOfAncestors = dijkstra(source, target, bannedRoads);

        // rekonstruuj nalezenou nejkratsi cestu z mapy predchudcu
        List<Road> listOfRoadsOnThePath = new LinkedList<>();
        Node recent = target;
        while (!recent.equals(source)) {
            NodeAndRoad previous = mapOfAncestors.get(recent);

            if (previous == null) {
                return listOfRoadsOnThePath;
            }

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
    private Map<Node, NodeAndRoad> dijkstra(Node source, Node target, Set<Road> bannedRoads) {
        Map<Node, Integer> distance = new HashMap<>();
        Map<Node, Boolean> visited = new HashMap<>();
        //Map<Node, Node> previous = new HashMap<>(); // previous node in the best path
        Map<Node, NodeAndRoad> previousRoad = new HashMap<>(); // previous node and road in the best path

        // init nodes
        for (Node node : net.getNodes()) {
            distance.put(node, Integer.MAX_VALUE);
            visited.put(node, Boolean.FALSE);
        }

        // start with the source node
        distance.put(source, 0);

        Set<Node> queue = new HashSet<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            Node u = null; // node with smallest distance, not visited
            int smallestDistance = Integer.MAX_VALUE;

            // find smallest distance
            for (Iterator<Node> it = queue.iterator(); it.hasNext();) {
                Node node = it.next();

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

            for (Road r : u.getRoads()) {

                // skip closed and banned roads
                if (bannedRoads.contains(r) || r.isClosed()) {
                    continue;
                }

                Node v = r.getOppositeNode(u);
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
