package cz.muni.fi.closuresim;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
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
    private final SortedSet<Disconnection> disconnections;
    private final DisconnectionCollector disconnectionCollector;
    private final int maxNumberOfRoadsToClose;
    private final int maxNumberOfComponents;
    private final boolean findOnlyAccurateDisconnection;
    private final ResultWriter resultWriter;
    /**
     * Number of disconenctions found from one road
     */
    private int disconnectionsFromOneRoad = 0;
    private final boolean onlyStoreResultByRoads;

    private final Graph<Node, Road> graph = new Multigraph(Road.class);

    public AlgCycleRunnableJG(Net net, DisconnectionCollector dc, final int roads, final int comp, final boolean foad, boolean onlyStoreResultByRoads) {
        this.net = net.clone();
        this.disconnections = new TreeSet<>();
        this.disconnectionCollector = dc;
        this.maxNumberOfRoadsToClose = roads;
        this.maxNumberOfComponents = comp;
        this.findOnlyAccurateDisconnection = foad;
        File partRes = new File(ExperimentSetup.outputDirectory, "partial-results");
        this.resultWriter = new ResultWriter(partRes);
        this.onlyStoreResultByRoads = onlyStoreResultByRoads;

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

        if (!onlyStoreResultByRoads) {
            resultWriter.storeDisconnection(Thread.currentThread().getName(), disconnections);
            this.disconnectionCollector.addDisconnections(this.disconnections);
        }

        ExperimentSetup.LOGGER.log(Level.INFO, "Thread {0} end.", Thread.currentThread().getName());
    }

    /**
     * The algorithm itself.
     *
     * @param bannedRoads set of banned roads
     * @param road road which was chosen in the cycle
     */
    private void theFindCyclesAlgorithm(final Set<Road> bannedRoads, final Road road, final int components, boolean recComp) { //////////////////////////////// see, remove last parameter

        // remove banned roads from graph
        for (Road roadTORemove : bannedRoads) {
            graph.removeEdge(roadTORemove);
        }

        DijkstraShortestPath<Node, Road> dsp = new DijkstraShortestPath<>(graph, road.getFirst_node(), road.getSecond_node());
        List<Road> path = dsp.getPathEdgeList();

        for (Road roadToAdd : bannedRoads) {
            graph.addEdge(roadToAdd.getFirst_node(), roadToAdd.getSecond_node(), roadToAdd);
        }

        // Does the path exist?
        if (path == null) {
            // The path doesn't exist. We have cut. Put it down
            // if we don't want disconnection by fewer roads, skip putting down 
            if (!findOnlyAccurateDisconnection || bannedRoads.size() >= maxNumberOfRoadsToClose) {
                Disconnection dis = new Disconnection(bannedRoads);
                this.disconnections.add(dis);
                this.disconnectionsFromOneRoad++;
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
