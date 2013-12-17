package cz.muni.fi.closuresim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import de.normalisiert.utils.graphs.ElementaryCyclesSearch;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Tom
 */
public class AlgorithmCycle2 implements Algorithm {

    private Net net;
    private DisconnectionCollector disconnectionCollector;
    private final File outputDirectory;
    private int maxNumOfComponents;
    private static final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs;
    protected static Queue<Road> queue = new ConcurrentLinkedQueue<>();
    //
    //
    private UndirectedGraph<Node, DefaultEdge> graph;
    private List<Node> vertexList;
    private boolean adjMatrix[][];

    public AlgorithmCycle2(Net net, DisconnectionCollector disconnectionCollector, final File outputDirectory) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
        this.outputDirectory = outputDirectory;

        this.graph = new SimpleGraph<Node, DefaultEdge>(DefaultEdge.class);
        this.vertexList = new ArrayList<Node>();
    }

    @Override
    public void start(int maxClosedRoads) {

        johnsonAlgStart();
        //floydWarshallAlgStart();
        
    }

    private void floydWarshallAlgStart() {
        // matice
        int[][] graphMatrix;

        final int numberOfNodes = this.net.getNodes().size();
        graphMatrix = new int[numberOfNodes][numberOfNodes];

        Map<Integer, Node> mapOfNodes = new HashMap<>(numberOfNodes);
        int num = 0;
        for (Node n : this.net.getNodes()) {
            mapOfNodes.put(num++, n);
        }

        for (Map.Entry<Integer, Node> entry : mapOfNodes.entrySet()) {
            Integer i = entry.getKey();
            Node node = entry.getValue();

            for (Map.Entry<Integer, Node> entry2 : mapOfNodes.entrySet()) {
                Integer j = entry2.getKey();
                Node node2 = entry2.getValue();

                if (node.equals(node2)) {
                    graphMatrix[i][j] = 0;
                } else {
                    if (node.getNeighbours().contains(node2)) {
                        graphMatrix[i][j] = 1;
                    } else {
                        graphMatrix[i][j] = Integer.MAX_VALUE;
                    }
                }
            }

        }

        System.out.println(graphMatrix);
        int[][] floydWarshall = floydWarshall(graphMatrix);

        System.out.println(floydWarshall);

        Node[][] floydWarshallNode;
        floydWarshallNode = new Node[mapOfNodes.size()][mapOfNodes.size()];

        for (int i = 0; i < floydWarshall.length; i++) {
            int[] nodes1 = floydWarshall[i];
            for (int j = 0; j < floydWarshall.length; j++) {
                int nodes2 = nodes1[j];

                floydWarshallNode[i][j] = mapOfNodes.get(nodes2);
            }
        }

        System.out.println(floydWarshallNode);

        for (Map.Entry<Integer, Node> entry : mapOfNodes.entrySet()) {
            Node node = entry.getValue();
            System.out.print(node.getName() + "\t");
        }
        System.out.println();
        
        for (int i = 0; i < floydWarshallNode.length; i++) {
            for (int j = 0; j < floydWarshallNode.length; j++) {
                Node node = floydWarshallNode[i][j];

                if (node == null) {
                    System.out.print("(tam)\t");
                } else {
                    System.out.print(node.getName() + "\t");
                }
            }
            System.out.println();
        }
    }

    private void johnsonAlgStart() {
        // pridame uzly
        for (Node node : net.getNodes()) {
            addVertex(node);
        }

        // pridame hrany
        for (Road road : net.getRoads()) {
            addEdge(road.getFirst_node(), road.getSecond_node());
        }

        // System.out.println(getGraph());
        // System.out.println(this.vertexList);

        // ziskaji se vsechny cykly
        List<List<Node>> listOfCycles = getAllCycles();

        // cykly uzlu se prevedou na cykly silnic
        List<List<Road>> listOfCyclesRoad = new LinkedList<>();
        for (List<Node> cycleNode : listOfCycles) {
            List<Road> cycleRoad = new LinkedList<>();

            Iterator<Node> it = cycleNode.iterator();
            Node node1 = it.next();
            Node nodeFirst = node1;
            for (; it.hasNext();) {
                Node node2 = it.next();

                for (Road r : node1.getRoads()) {
                    if (r.getOppositeNode(node1).equals(node2)) {
                        cycleRoad.add(r);
                        break; // TODO - ale co kdyz jsou mezi dvema uzly dve a vice hran
                    }
                }

                node1 = node2;
            }
            // pridame hranu od posledniho uzlu k prvnimu
            for (Road r : nodeFirst.getRoads()) {
                if (r.getOppositeNode(nodeFirst).equals(node1)) {
                    cycleRoad.add(r);
                    break; // TODO - ale co kdyz jsou mezi dvema uzly dve a vice hran
                }
            }

            listOfCyclesRoad.add(cycleRoad);
        }


        // vytvorime mnozinu kruznic
        Set<Circle> setOfCycles = new HashSet<>();
        for (List<Road> list : listOfCyclesRoad) {
            Circle c = new Circle(list);
            setOfCycles.add(c);
        }

        System.out.println("Pocet kruznic seznam " + listOfCyclesRoad.size());
        System.out.println("Pocet kruznic " + setOfCycles.size());

        storeCyclesToFile(setOfCycles);

    }

    public void addVertex(Node vertex) {
        this.graph.addVertex(vertex);
        this.vertexList.add(vertex);
    }

    public void addEdge(Node vertex1, Node vertex2) {
        this.graph.addEdge(vertex1, vertex2);
    }

    public UndirectedGraph<Node, DefaultEdge> getGraph() {
        return graph;
    }

    public List<List<Node>> getAllCycles() {
        this.buildAdjancyMatrix();

        @SuppressWarnings("unchecked")
        Object[] vertexArray = this.vertexList.toArray();
        ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(this.adjMatrix, vertexArray);

        @SuppressWarnings("unchecked")
        List<List<Node>> cycles0 = ecs.getElementaryCycles();

        // remove cycles of size 2
        Iterator<List<Node>> listIt = cycles0.iterator();
        while (listIt.hasNext()) {
            List<Node> cycle = listIt.next();

            if (cycle.size() == 2) {
                listIt.remove();
            }
        }

        // remove repeated cycles (two cycles are repeated if they have the same vertex (no matter the order)
        List<List<Node>> cycles1 = removeRepeatedLists(cycles0);

        /*
         for (List<Node> cycle : cycles1) {
         System.out.println(cycle);
         }
         */

        return cycles1;
    }

    private void buildAdjancyMatrix() {
        Set<DefaultEdge> edges = this.graph.edgeSet();
        Integer nVertex = this.vertexList.size();
        this.adjMatrix = new boolean[nVertex][nVertex];

        for (DefaultEdge edge : edges) {
            Node v1 = this.graph.getEdgeSource(edge);
            Node v2 = this.graph.getEdgeTarget(edge);

            int i = this.vertexList.indexOf(v1);
            int j = this.vertexList.indexOf(v2);

            this.adjMatrix[i][j] = true;
            this.adjMatrix[j][i] = true;
        }
    }
    /* Here repeated lists are those with the same elements, no matter the order, 
     * and it is assumed that there are no repeated elements on any of the lists*/

    private List<List<Node>> removeRepeatedLists(List<List<Node>> listOfLists) {
        List<List<Node>> inputListOfLists = new ArrayList<List<Node>>(listOfLists);
        List<List<Node>> outputListOfLists = new ArrayList<List<Node>>();

        while (!inputListOfLists.isEmpty()) {
            // get the first element
            List<Node> thisList = inputListOfLists.get(0);
            // remove it
            inputListOfLists.remove(0);
            outputListOfLists.add(thisList);
            // look for duplicates
            Integer nEl = thisList.size();
            Iterator<List<Node>> listIt = inputListOfLists.iterator();
            while (listIt.hasNext()) {
                List<Node> remainingList = listIt.next();

                if (remainingList.size() == nEl) {
                    if (remainingList.containsAll(thisList)) {
                        listIt.remove();
                    }
                }
            }

        }

        return outputListOfLists;
    }

    private void storeCyclesToFile(Collection<Circle> sc) {
        try {

            FileWriter writer = new FileWriter(new File(outputDirectory, "cycles.csv"));

            for (Iterator<Circle> it = sc.iterator(); it.hasNext();) {
                Circle c = it.next();

                writer.append(Integer.toString(c.getLength()));
                writer.append(";");

                boolean isFirst = true;
                for (Road r : c.getRoads()) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        writer.append(";");
                    }
                    writer.append(r.getName());
                }
                writer.append("\n");
            }

            writer.flush();
            writer.close();

        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "IO exception occur when writting cycles to file.", ex);
        }
    }

    private Map<Integer, Set<Circle>> createMapOfCycleSets(Set<Circle> setOfCycles) {

        Map<Integer, Set<Circle>> mapOfCycles = new HashMap<>();
        for (Circle c : setOfCycles) {
            final int length = c.getLength();
            if (!mapOfCycles.containsKey(length)) {
                Set<Circle> sc = new HashSet<>();
                sc.add(c);
                mapOfCycles.put(length, sc);
            } else {
                mapOfCycles.get(length).add(c);
            }
        }

        return mapOfCycles;

    }

    /**
     * Floyd-Warshall algorithm. Finds all shortest paths among all pairs of
     * nodes
     *
     * @param d matrix of distances (Integer.MAX_VALUE represents positive
     * infinity)
     * @return matrix of predecessors
     */
    public static int[][] floydWarshall(int[][] d) {
        int[][] p = constructInitialMatixOfPredecessors(d);
        for (int k = 0; k < d.length; k++) {
            for (int i = 0; i < d.length; i++) {
                for (int j = 0; j < d.length; j++) {
                    if (d[i][k] == Integer.MAX_VALUE || d[k][j] == Integer.MAX_VALUE) {
                        continue;
                    }

                    if (d[i][j] > d[i][k] + d[k][j]) {
                        d[i][j] = d[i][k] + d[k][j];
                        p[i][j] = p[k][j];
                    }

                }
            }
        }
        return p;
    }

    /**
     * Constructs matrix P0
     *
     * @param d matrix of lengths
     * @return P0
     */
    private static int[][] constructInitialMatixOfPredecessors(int[][] d) {
        int[][] p = new int[d.length][d.length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d.length; j++) {
                if (d[i][j] != 0 && d[i][j] != Integer.MAX_VALUE) {
                    p[i][j] = i;
                } else {
                    p[i][j] = -1;
                }
            }
        }
        return p;
    }
} // end class
