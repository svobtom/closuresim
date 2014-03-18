package cz.muni.fi.closuresim;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.alg.PrimMinimumSpanningTree;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;
import org.jgrapht.graph.Multigraph;

/**
 *
 * @author Tom
 */
public class AlgCycleCutRunnableJG implements Runnable {

    private final Net net;
    private final Set<Disconnection> disconnections;
    private final DisconnectionCollector disconnectionCollector;
    private final int maxNumberOfRoadsToClose;
    private final int maxNumberOfComponents;
    private final boolean findOnlyAccurateDisconnection;

    private final Graph<Node, Road> graph = new Multigraph(Road.class);

    public AlgCycleCutRunnableJG(Net net, DisconnectionCollector disconnectionCollector, int maxNumberOfRoadsToClose, int maxNumberOfComponents, boolean findOnlyAccurateDisconnection) {
        this.net = net;
        this.disconnections = new HashSet<>();
        this.disconnectionCollector = disconnectionCollector;
        this.maxNumberOfRoadsToClose = maxNumberOfRoadsToClose;
        this.maxNumberOfComponents = maxNumberOfComponents;
        this.findOnlyAccurateDisconnection = findOnlyAccurateDisconnection;

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
        while (!AlgorithmCycleCut.queueOfRoads.isEmpty()) {
            // get the next road from the queue
            final Road roadToStart = AlgorithmCycleCut.queueOfRoads.poll();
            if (roadToStart != null) {

                // get the road in the cloned net
                final Road cRoadToStart = this.net.getRoad(roadToStart.getId());

                // create the R set - bannedRoads
                final Set<Road> bannedRoads = new HashSet<>();
                bannedRoads.add(cRoadToStart);

                // 1. choosing edge e
                theFindCyclesCutAlgorithm(bannedRoads, cRoadToStart, 1, true);

                // add found disconnections by one run of the algorithm to the disconnection collector
                final int numFoundDis = this.disconnections.size();
                this.disconnectionCollector.addDisconnections(this.disconnections);
                this.disconnections.clear();

                ExperimentSetup.LOGGER.log(Level.INFO,
                        "Road " + cRoadToStart.getName() + " was processed by thread {0}. Found " + numFoundDis + " disconnections.",
                        Thread.currentThread().getName());
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
    private void theFindCyclesCutAlgorithm(final Set<Road> bannedRoads, final Road road, final int components, boolean recComp) {

        // remove banned roads from graph
        for (Road roadTORemove : bannedRoads) {
            graph.removeEdge(roadTORemove);
        }

        // find the shortest cycle passing thru just one road from banned roads and avoid the others 
        // if not go recursively to more components
        List<Road> path = null;
        if (!recComp) {
            int minLength = Integer.MAX_VALUE;

            for (Road chosenRoad : bannedRoads) {

                DijkstraShortestPath<Node, Road> dsp = new DijkstraShortestPath<>(graph, chosenRoad.getFirst_node(), chosenRoad.getSecond_node());
                List<Road> tempPath = dsp.getPathEdgeList();

                // set the shortest cycle
                if (tempPath != null && tempPath.size() <= minLength) {
                    path = tempPath;
                    minLength = tempPath.size();
                }
            }
        } else {
            DijkstraShortestPath<Node, Road> dsp = new DijkstraShortestPath<>(graph, road.getFirst_node(), road.getSecond_node());
            path = dsp.getPathEdgeList();
        }

        // add banned road again
        for (Road roadTtoAdd : bannedRoads) {
            graph.addEdge(roadTtoAdd.getFirst_node(), roadTtoAdd.getSecond_node(), roadTtoAdd);
        }

        // Does the path exist?
        if (path == null) {
            // path doesn't exist

            Graph g = new Multigraph(Road.class);

            // add vertices
            for (Node n : this.graph.vertexSet()) {
                g.addVertex(n);
            }

            // add roads except F roads / no F roads are int the graph
            for (Road rro : this.graph.edgeSet()) {
                if (!bannedRoads.contains(rro)) {
                    g.addEdge(rro.getFirst_node(), rro.getSecond_node(), rro);
                }
            }

            // for each road in F - add one F edge in every run of the cycle
            for (Road r : bannedRoads) {

                g.addEdge(r.getFirst_node(), r.getSecond_node(), r);

                // get road on the spanning tree/forest
                MinimumSpanningTree<Node, Road> st = new KruskalMinimumSpanningTree<>(g);

                // this condition is true if and only if the spanning tree isn't in one component
                if ((g.vertexSet().size() - st.getMinimumSpanningTreeTotalWeight() - 1) <= 1) {

                    Disconnection dis = new Disconnection(bannedRoads);
                    disconnections.add(dis);

                    // recursive finding disconnections to more components
                    if ((components + 1) < maxNumberOfComponents) {
                        Set<Road> allowedRoads = new HashSet<>(net.getRoads());
                        allowedRoads.removeAll(bannedRoads);

                        // for every recently not banned road
                        for (Iterator<Road> it = allowedRoads.iterator(); it.hasNext();) {
                            final Road allowedRoad = it.next();
                            theFindCyclesCutAlgorithm(bannedRoads, allowedRoad, components + 1, true);
                        }
                    }

                } else {

                    Set<Road> roadMST = st.getMinimumSpanningTreeEdgeSet();
                    roadMST.remove(r); // for recursion choose T \ {f}

                    if (bannedRoads.size() < maxNumberOfRoadsToClose) {
                        for (Road roadOnSpanningTree : roadMST) {
                            Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                            newBannedRoads.add(roadOnSpanningTree);

                            theFindCyclesCutAlgorithm(newBannedRoads, roadOnSpanningTree, components, true);
                        }
                    }
                }

                g.removeEdge(r);

            } // end of for each road in F

        } else {
            // path exists
            // omezeni maximalniho poctu uzaviranych silnic
            if (bannedRoads.size() < maxNumberOfRoadsToClose) {
                for (final Road roadInPath : path) {
                    // vytvorime nove zakazane cesty, tak ze k jiz soucasnym zakazanym pridame cesty, ktere jsou na prave nalezene kruznici
                    Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                    newBannedRoads.add(roadInPath);

                    // spustime algoritmus rekurzivne
                    theFindCyclesCutAlgorithm(newBannedRoads, roadInPath, components, false);
                }
            }
        }
    }
}
