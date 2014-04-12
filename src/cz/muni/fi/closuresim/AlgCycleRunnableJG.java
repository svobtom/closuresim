package cz.muni.fi.closuresim;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import org.jgrapht.Graph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.alg.DijkstraShortestPath;

/**
 * Body of the cycle algorithm. It use jGraphT library.
 *
 * @author Tom
 */
public class AlgCycleRunnableJG implements Runnable {

    private final Net net;
    private final Set<Disconnection> disconnections;
    private final DisconnectionCollector disconnectionCollector;
    private final int maxNumberOfRoadsToClose;
    private final int maxNumberOfComponents;
    private final boolean findOnlyAccurateDisconnection;
    private final ResultWriter resultWriter;

    private final Graph<Node, Road> graph = new Multigraph(Road.class);

    public AlgCycleRunnableJG(Net net, DisconnectionCollector dc, final int roads, final int comp, final boolean foad) {
        this.net = net.clone();
        this.disconnections = new HashSet<>();
        this.disconnectionCollector = dc;
        this.maxNumberOfRoadsToClose = roads;
        this.maxNumberOfComponents = comp;
        this.findOnlyAccurateDisconnection = foad;
        File partRes = new File(ExperimentSetup.outputDirectory, "partial-results");
        this.resultWriter = new ResultWriter(partRes);

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

                // create the R set - bannedRoads
                final Set<Road> bannedRoads = new HashSet<>();
                bannedRoads.add(cRoadToStart);

                // 1. choosing edge e
                theFindCyclesAlgorithm(bannedRoads, cRoadToStart, 1, true);

                // add found disconnections by one run of the algorithm to the disconnection collector
                final int numFoundDis = this.disconnections.size();
                
                // save partial results
                resultWriter.storeDisconnection(cRoadToStart.getName(), this.disconnections);

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
    private void theFindCyclesAlgorithm(final Set<Road> bannedRoads, final Road road, final int components, boolean recComp) {

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

        for (Road roadToAdd : bannedRoads) {
            graph.addEdge(roadToAdd.getFirst_node(), roadToAdd.getSecond_node(), roadToAdd);
        }

        // Does the path exist?
        if (path == null) {
            // The path doesn't exist. We have cut. Put it down
            // if we don't want disconnection by fewer roads, skip putting down 
            if (!findOnlyAccurateDisconnection || bannedRoads.size() >= maxNumberOfRoadsToClose) {
                Disconnection dis = new Disconnection(bannedRoads);
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
                    // create new banned roads (add roads on the found cycle to recently banned roads)
                    Set<Road> newBannedRoads = new HashSet<>(bannedRoads);
                    newBannedRoads.add(roadInPath);

                    //  run the algorithm recursively
                    theFindCyclesAlgorithm(newBannedRoads, roadInPath, components, false);
                }
            }
        }
    }

}
