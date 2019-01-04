package cz.muni.fi.closuresim;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import org.jgrapht.Graph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Body of the cycle algorithm. It use jGraphT library.
 *
 * @author Tom
 */
public class AlgCycleRunnableJG implements Runnable {

    private final Net net;
    private final SortedSet<Disconnection> disconnections;
    private final DisconnectionCollector disconnectionCollector;
    private final ResultWriter resultWriter;

    private final int maxNumberOfRoadsToClose;
    private final int maxNumberOfComponents;
    private final boolean findOnlyAccurateDisconnection;
    private int disconnectionsFromOneRoad = 0;
    private final boolean onlyStoreResultByRoads;

    private final Set<Road> alwaysOpenRoads;
    private final boolean alwaysOpenRoadsOccur; // optimalization

    private final Graph<Node, Road> graph = new Multigraph(Road.class);

    protected final static Map<Integer, Integer> cycleLength = new HashMap<>();
    private final static Object LOCKER = new Object();

    public AlgCycleRunnableJG(Net net, DisconnectionCollector dc, int roads, int maxComponents, boolean accurate, boolean onlyStoreResultByRoads, Set<Road> alwaysOpenRoads) {
        this.net = net.clone();
        this.disconnections = new TreeSet<>();
        this.disconnectionCollector = dc;
        this.maxNumberOfRoadsToClose = roads;
        this.maxNumberOfComponents = maxComponents;
        this.findOnlyAccurateDisconnection = accurate;
        File partRes = new File(ExperimentSetup.outputDirectory, "partial-results");
        this.resultWriter = new ResultWriter(partRes);
        this.onlyStoreResultByRoads = onlyStoreResultByRoads;

        // if there are some alwaysOpenRoads
        if (alwaysOpenRoads != null && alwaysOpenRoads.size() > 0) {
            this.alwaysOpenRoads = alwaysOpenRoads;
            this.alwaysOpenRoadsOccur = true;
        } else {
            this.alwaysOpenRoads = null;
            this.alwaysOpenRoadsOccur = false;
        }

        // add vertices
        for (Node n : this.net.getNodes()) {
            this.graph.addVertex(n);
        }

        // add roads
        for (Road r : this.net.getRoads()) {
            this.graph.addEdge(r.getFirst_node(), r.getSecond_node(), r);
        }
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

                // 1. choosing edge e
                findCutSets(1, new TreeSet<>(), cRoadToStart);

                ExperimentSetup.LOGGER.log(Level.INFO,
                        "Road " + cRoadToStart.getName() + " was processed by thread {0}. Found " + this.disconnectionsFromOneRoad + " disconnections.",
                        Thread.currentThread().getName());
                this.disconnectionsFromOneRoad = 0;

                if (onlyStoreResultByRoads) {
                    resultWriter.storeDisconnection(cRoadToStart.getName(), disconnections);
                    disconnections.clear();
                }
            }
        }
        ExperimentSetup.LOGGER.log(Level.INFO, "Thread {0} finished searching.", Thread.currentThread().getName());

        // store all disconnection to the main collector
        if (!onlyStoreResultByRoads) {
            this.disconnectionCollector.addDisconnections(this.disconnections);
        }

        ExperimentSetup.LOGGER.log(Level.INFO, "Thread {0} end.", Thread.currentThread().getName());
    }

    /**
     * The algorithm itself.
     *
     * @param restrictedEdges set of banned roads
     * @param road road which was chosen in the cycle
     */
    private void findCutSets(final int level, final Set<Road> restrictedEdges, final Road road) {

        // add chosen edge to bannedEdges
        restrictedEdges.add(road);

        // remove banned roads from graph
        for (Road roadTORemove : restrictedEdges) {
            graph.removeEdge(roadTORemove);
        }

        List<Road> path = findShortestPathByBFS(graph, road.getFirst_node(), road.getSecond_node());

        if (ExperimentSetup.DEBUG) {
            //System.out.println(road + " - " + path + " banned " + bannedRoads);

            synchronized (LOCKER) {
                // store cycle length
                int pathLength = path == null ? 0 : path.size() + 1;
                if (cycleLength.get(pathLength) == null) {
                    // new cycle length
                    cycleLength.put(pathLength, 1);
                } else {
                    cycleLength.put(pathLength, cycleLength.get(pathLength) + 1);
                }
            }
        }

        // add edges to graph
        for (Road roadToAdd : restrictedEdges) {
            graph.addEdge(roadToAdd.getFirst_node(), roadToAdd.getSecond_node(), roadToAdd);
        }

        // Does the path exist?
        if (path != null) {
            // The path exists. We haven't got the cut.
            // limit maximal number of closed roads
            if ((restrictedEdges.size()) < maxNumberOfRoadsToClose) {

                // for every road on the shortest cycle (from A to B)
                for (Road roadInPath : path) {
                    // create new banned roads (add roads on the found cycle to recently banned roads)

                    //  run the algorithm recursively
                    findCutSets(level, new TreeSet<>(restrictedEdges), roadInPath);
                }
            } else {
                //System.out.println("cond 1 - cycle road limit");
            }

        } else {
            // The path doesn't exist. We have cut. Put it down
            // if we don't want disconnection by fewer roads, skip putting down 
            if (!findOnlyAccurateDisconnection || restrictedEdges.size() >= maxNumberOfRoadsToClose) {

                // only store the new disconnection when alwaysOpenRoads isn't set or banned roads doesn't contain any road from alwaysOpenRoads
                if (!alwaysOpenRoadsOccur || (alwaysOpenRoadsOccur && Net.roadIntersection(restrictedEdges, alwaysOpenRoads).isEmpty())) {

                    Disconnection dis = new Disconnection(restrictedEdges);
                    this.disconnections.add(dis);
                    this.disconnectionsFromOneRoad++;
                }
            }

            // recursive finding disconnections to more components
            if ((level + 1) < maxNumberOfComponents) {
                if (restrictedEdges.size() < maxNumberOfRoadsToClose) {

                    Set<Road> allowedRoads = new HashSet<>(AlgorithmCycle.spanningTree);
                    allowedRoads.removeAll(restrictedEdges);

                    // for every recently not banned road
                    for (Iterator<Road> it = allowedRoads.iterator(); it.hasNext();) {
                        final Road allowedRoad = it.next();

                        findCutSets(level + 1, new TreeSet<>(restrictedEdges), allowedRoad);
                    }
                } else {
                    //System.out.println("cond 2 - roads limit");
                }
            } else {
                //System.out.println("cond 2 - components limit");
            }
        }
    }

    /**
     * Find the shortest path from one node to another.
     *
     * @param graph
     * @param startNode
     * @param endNode
     * @return list of roads on the shortest path, null if the path doesn't
     * exist
     */
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
