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

        // vlakna paralelne provedou algoritmus pocinaje od kazde cesty
        while (!AlgorithmCycle.queue.isEmpty()) {
            // vezme se dalsi cesta z fronty
            final Road roadToStart = AlgorithmCycle.queue.poll();
            if (roadToStart != null) {
                // get the road in the cloned net
                final Road cRoadToStart = this.net.getRoad(roadToStart.getId());

                // vytvoreni mnoziny R - bannedRoads
                final Set<Road> bannedRoads = new HashSet<>();
                bannedRoads.add(cRoadToStart);

                // 1. Zvolim hranu e (jednu vybranou cestu)
                theFindCyclesAlgorithm(bannedRoads, cRoadToStart, 1);

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
     * Do the algorithm.
     *
     * @param bannedRoads set of banned roads
     * @param road road which was chosen in the cycle
     */
    private void theFindCyclesAlgorithm(final Set<Road> bannedRoads, final Road road, final int components) {

        // najde nejkratsi cestu z a do b, bez pouziti cest z bannedRoads
        // a, b jsou oba vrcholy vybrane cesty, samotna cesta jiz patri do bannedRoads, takze nenalezne trivialni cestu
        // obsahujici pouze prave tuto cestu z a do b
        final List<Road> path = findShortestPath(road.getFirst_node(), road.getSecond_node(), bannedRoads);
        // zamenit za for pres vsechny bannedRoads
        // nove najit nejkratsi cestu z banndroad, to se hodi na vystup, budu pridavat ke stavajici mnozine, pres vsechny cesty jednu hranu z nich
        
        // existuje cesta?
        if (path.isEmpty()) {
            // cesta neexistuje, mame rez, poznacime si ho
            // pokud nas nezajimaji rozpady pomoci mensiho mnozstvi cest, tak se nepoznacuji
            if (!findOnlyAccurateDisconnection || bannedRoads.size() >= maxNumberOfRoadsToClose) {
                Disconnection dis = new Disconnection(bannedRoads);
                
                Set<Road> testSet = new HashSet<>();
                testSet.add(this.net.getRoad(24));
                testSet.add(this.net.getRoad(26));
                testSet.add(this.net.getRoad(25));                
                Disconnection testDis = new Disconnection(testSet);                
                if (dis.equals(testDis)) {
                    System.out.println("For debug");
                }
                
                //disconnectionCollector.addDisconnection(dis);
                disconnections.add(dis);
            }

            // rekurzivni hledani rozpadu na vice komponent
            if ((components + 1) < maxNumberOfComponents) {
                final Set<Road> allowedRoads = new HashSet<>(net.getRoads());
                allowedRoads.removeAll(bannedRoads);

                // pro kazdou zatim jeste povolenou cestu
                for (Iterator<Road> it = allowedRoads.iterator(); it.hasNext();) {
                    final Road allowedRoad = it.next();
                    theFindCyclesAlgorithm(bannedRoads, allowedRoad, components + 1);
                }
            }

        } else {
            // cesta existuje, rez zatim nemame

            // omezeni maximalniho poctu uzaviranych silnic
            if ((bannedRoads.size()) < maxNumberOfRoadsToClose) {

                //path.add(road);
                
                // pro kazdou cestu na nejkratsi kruznici z a do b
                for (final Road roadInPath : path) {
                    // vytvorime nove zakazane cesty, tak ze k jiz soucasnym zakazanym pridame cesty, ktere jsou na prave nalezene kruznici
                    Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                    
                    if (bannedRoads.contains(this.net.getRoad(25)) && bannedRoads.contains(this.net.getRoad(24)) &&  roadInPath.equals(this.net.getRoad(26))) {
                        System.out.println("For debug, road");
                    }
                    
                    newBannedRoads.add(roadInPath);

                    // spustime algoritmus rekurzivne
                    theFindCyclesAlgorithm(newBannedRoads, roadInPath, components);
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

        // vysledna cesta, zatim prazdna
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
