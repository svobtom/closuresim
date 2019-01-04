package cz.muni.fi.closuresim;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.jgrapht.Graph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 *
 * @author Tom
 */
class AlgorithmTest implements Algorithm {

    private final Net net;
    private final Graph<Node, Road> graph = new Multigraph(Road.class);

    public AlgorithmTest(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net.clone();

        // add vertices
        for (Node n : this.net.getNodes()) {
            this.graph.addVertex(n);
        }

        // add roads
        for (Road r : this.net.getRoads()) {
            this.graph.addEdge(r.getFirst_node(), r.getSecond_node(), r);
        }
    }

    public void start(int maxClosedRoads) {
        SortedSet<Node> uzly = new TreeSet<>();

        // bez krizovatek
        for (Node n : net.getNodes()) {
            if (n.getNumOfInhabitant() > 0) {
                uzly.add(n);
            }
        }

        for (Node mujNode : uzly) {

            // odstraneni diakritiky
            String string = Normalizer.normalize(mujNode.getName(), Normalizer.Form.NFD);
            string = string.replaceAll("[^\\p{ASCII}]", "");
            string = string.replaceAll(" ", "");
            mujNode.setName(string);
        }

        SortedSet<Node> skladky = new TreeSet();

        for (Node n : uzly) {
            if (n.getId() == 1 || n.getId() == 5 || n.getId() == 20) {
                skladky.add(n);
            }
        }
        uzly.removeAll(skladky);

        /*
         set obec uzly produkujici odpad
         /Hovorany, Dubnany, Hodonin/;
         */
        System.out.println("set obec uzly produkujici odpad");
        System.out.print("/ ");
        for (Node n : uzly) {
            System.out.print(n.getName() + ", ");
        }
        System.out.println("/;");
        System.out.println();

        /*
         set skladka uzly ukladajici odpad
         /Mut-sk, Rat-sk/;
         */
        System.out.println("set skladka uzly ukladajici odpad");
        System.out.print("/ ");
        for (Node n : skladky) {
            System.out.print(n.getName() + ", ");
        }
        System.out.println("/;");
        System.out.println();
        /*
         parameter pocetObyvatel(obec) pocet obyvatel v obcich
         / Hovorany 70
         Dubnany 40
         Hodonin 20 /;
         */
        System.out.println("parameter pocetObyvatel(obec) pocet obyvatel v obcich");
        System.out.print("/");
        for (Node n : uzly) {
            System.out.println("  " + n.getName() + " " + n.getNumOfInhabitant());
        }
        System.out.println("/;");
        System.out.println();
        /*
         parameter kapacita(skladka) kapacita skladek
         / Mut-sk 80
         Rat-sk 300 /;
         */
        System.out.println("parameter kapacita(skladka) kapacita skladek");
        System.out.print("/");
        for (Node n : skladky) {
            System.out.println("  " + n.getName() + " 1000");
        }
        System.out.println("/;");
        System.out.println();

        /*
         parameter otevrena(skladka) definice jiz otevrenych skladek (nevznikaji naklady na jejich otevreni)
         / Mut-sk 0
         Rat-sk 1
         /;
         */
        System.out.println("parameter otevrena(skladka) definice jiz otevrenych skladek (nevznikaji naklady na jejich otevreni)");
        System.out.print("/");
        for (Node n : skladky) {
            if (n.getId() % 10 == 0) {
                System.out.println("  " + n.getName() + " 1");
            } else {
                System.out.println("  " + n.getName() + " 0");
            }
        }
        System.out.println("/;");
        System.out.println();
        /*
         parameter skladkovaciPoplatek(skladka) poplatek za ulozeni 1 tuny odpadu na skladce
         / Mut-sk 800
         Rat-sk 1000 /;
         */
        System.out.println("parameter skladkovaciPoplatek(skladka) poplatek za ulozeni 1 tuny odpadu na skladce");
        System.out.print("/");
        for (Node n : skladky) {
            System.out.println("  " + n.getName() + " 100");
        }
        System.out.println("/;");
        System.out.println();
        /*
         parameter vzdalenost(obec, skladka) matice vzdalenosti mezi obcemi a skladkami v km
         / Hovorany.Mut-sk 2
         Hovorany.Rat-sk 10
         Dubnany.Mut-sk 4
         Dubnany.Rat-sk 5
         Hodonin.Mut-sk 6
         Hodonin.Rat-sk 7
         /;
         */
        
        
        System.out.println("parameter vzdalenost(obec, skladka) matice vzdalenosti mezi obcemi a skladkami v km");
        System.out.print("/");

        for (Node n : uzly) {
            for (Node skladka : skladky) {
                List<Road> rs = this.net.findShortestPath(n, skladka);
                int soucet = 0;
                for (Road r : rs) {
                    soucet += r.getLength();
                }
                soucet = soucet / 1000;

                System.out.println("  " + n.getName() + "." + skladka.getName() + " " + soucet);

            }
            System.out.println();

        }
        System.out.println("/;");
        System.out.println();

        System.exit(0);
    }

    /*
     public void start(int maxClosedRoads) {

     SortedSet<Node> seznam = new TreeSet<>();

     seznam.addAll(net.getNodes());

     for (Node mujNode : seznam) {

     // odstraneni diakritiky
     String string = Normalizer.normalize(mujNode.getName(), Normalizer.Form.NFD);
     string = string.replaceAll("[^\\p{ASCII}]", "");
     string = string.replaceAll(" ", "");
     System.out.print(string + ", ");
     }
     System.out.println("");

     int i = 1;
     for (Road r : net.getRoads()) {
     System.out.print(i++ + ", ");
     }
     System.out.println();
        
     i = 1;
     Random randomGenerator = new Random();
     for (Road r : net.getRoads()) {
     System.out.print(i++ + " " + randomGenerator.nextInt(17) + ", ");
     }
     System.out.println();
        
     System.out.println("**********************");
        
     System.out.print("             \t");
     i = 1;
     for (Road r : net.getRoads()) {

     System.out.print(i++ + "\t");

     }
     System.out.println();

     for (Node mujNode : seznam) {

     // odstraneni diakritiky
     String string = Normalizer.normalize(mujNode.getName(), Normalizer.Form.NFD);
     string = string.replaceAll("[^\\p{ASCII}]", "");

     while (string.length() < 13) {
     string = string + " ";
     }

     System.out.print(string + "\t");

     int j = 1;
     for (Road r : net.getRoads()) {

     if (r.getFirst_node().equals(mujNode)) {
     System.out.print("1\t");
     } else if (r.getSecond_node().equals(mujNode)) {
     System.out.print("-1\t");
     } else {
     System.out.print("\t");
     }

     }
     System.out.println();

     }

     System.exit(0);
     }
     */

    /*
     @Override
     public void start(int maxClosedRoads) {
     int pocet = 0;

     System.out.println("--- Stejne ---");
     for (Road r1 : net.getRoads()) {
     for (Road r2 : net.getRoads()) {

     if (!r1.equals(r2)) {

     if (r1.getFirst_node().equals(r2.getFirst_node()) && r1.getSecond_node().equals(r2.getSecond_node())
     || r1.getFirst_node().equals(r2.getSecond_node()) && r1.getSecond_node().equals(r2.getFirst_node())) {

     System.out.println(r1.getName() + " === " + r2.getName());
     pocet++;
     }
     }
     }
     }
     System.out.println("Pocet stejnych je " + pocet);
     //allCycleEnumeration();
     //varianta1();
     //varianta2();
     }

     */
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

    private void allCycleEnumeration() {

        // net to jGrapht
        DirectedMultigraph<Node, Road> graph = new DirectedMultigraph<>(Road.class);

        // add vertices
        for (Node n : this.net.getNodes()) {
            graph.addVertex(n);
        }

        // add roads
        for (Road r : this.net.getRoads()) {
            graph.addEdge(r.getFirst_node(), r.getSecond_node(), r);
            graph.addEdge(r.getSecond_node(), r.getFirst_node(), r);
        }

        graph.removeVertex(net.getNode(24));

        CycleDetector<Node, Road> cycleDetector = new CycleDetector<>(graph);

        for (Node s : cycleDetector.findCycles()) {
            System.out.println(s);
        }

        System.out.println("Number of cycles " + cycleDetector.findCycles().size());

    }

    private static List<Road> findShortestPathByBFS(Graph<Node, Road> graph, Node startNode, Node endNode) {

        MyBreadthFirstIterator bfi = new MyBreadthFirstIterator(graph, startNode);

        while (bfi.hasNext()) {
            Node nextNode = bfi.next();

            if (nextNode.equals(endNode)) {
                return createPath(bfi, endNode);
            }
        }

        return null;
    }

    private static List<Road> createPath(MyBreadthFirstIterator iter, Node endVertex) {
        List<Road> path = new ArrayList<>();

        while (true) {
            Road edge = iter.getSpanningTreeEdge(endVertex);

            if (edge == null) {
                break;
            } else {
                path.add(edge);
                endVertex = edge.getOppositeNode(endVertex);
            }
        }

        //Collections.reverse(path); // no need, doesn't depend on the order
        return path;
    }

    private static class MyBreadthFirstIterator extends BreadthFirstIterator<Node, Road> {

        public MyBreadthFirstIterator(Graph g, Node startVertex) {
            super(g, startVertex);
        }

        @Override
        protected void encounterVertex(Node vertex, Road edge) {
            super.encounterVertex(vertex, edge);
            putSeenData(vertex, edge);
        }

        public Road getSpanningTreeEdge(Node vertex) {
            Road r = (Road) getSeenData(vertex);
            return r;
        }
    }

}
