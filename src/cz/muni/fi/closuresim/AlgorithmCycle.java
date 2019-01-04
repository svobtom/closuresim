package cz.muni.fi.closuresim;

import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import org.jgrapht.Graph;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;
import org.jgrapht.graph.Multigraph;

/**
 * Main class of the Algorithm Cycle. It do necessary setting and start threads.
 *
 * @author Tom
 */
public class AlgorithmCycle implements Algorithm {

    private final Net net;
    private final DisconnectionCollector disconnectionCollector;
    /* Maximum number of components of disconnections to be found */
    private final int maxNumOfComponents;
    /* Determine if disconnection containing less roads than specified will be stored */
    private final boolean findOnlyAccurateDisconnection;
    /* alwaysOpenRoads */
    private final Set<Road> alwaysOpenRoads;

    /**
     * Use jGrapht library
     */
    private final boolean withJG;
    /**
     * Road witch will be skiped
     */
    private final Set<Road> roadsToSkip;
    /* */
    private final boolean onlyStoreResultByRoads;
    /* Number of threads to create (one core is left for the main thread) */
    private final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs - 1;
    /* Queue of unprocessed roads */
    protected static final Queue<Road> queue = new ConcurrentLinkedQueue<>();

    protected static final Graph<Node, Road> graph = new Multigraph(Road.class);
    protected static Set<Road> spanningTree;

    public AlgorithmCycle(
            Net net,
            DisconnectionCollector disconnectionCollector,
            final int maxNumOfComponents,
            final boolean findOnlyAccurateDisconnection,
            boolean withJG,
            Set<Road> roadsToSkip,
            boolean onlyStoreResultByRoads,
            Set<Road> alwaysOpenRoads
    ) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
        this.maxNumOfComponents = maxNumOfComponents;
        this.findOnlyAccurateDisconnection = findOnlyAccurateDisconnection;
        this.withJG = withJG;
        this.roadsToSkip = roadsToSkip;
        this.onlyStoreResultByRoads = onlyStoreResultByRoads;
        this.alwaysOpenRoads = alwaysOpenRoads;

        // add vertices
        for (Node n : this.net.getNodes()) {
            graph.addVertex(n);
        }
        // add roads
        for (Road r : this.net.getRoads()) {
            graph.addEdge(r.getFirst_node(), r.getSecond_node(), r);
        }
        MinimumSpanningTree<Node, Road> minSpanningTree = new KruskalMinimumSpanningTree<>(graph);
        spanningTree = minSpanningTree.getMinimumSpanningTreeEdgeSet();
    }

    public AlgorithmCycle(
            Net net,
            DisconnectionCollector disconnectionCollector,
            final int maxNumOfComponents,
            final boolean findOnlyAccurateDisconnection,
            boolean withJG,
            boolean onlyStoreResultByRoads,
            Set<Road> alwaysOpenRoads
    ) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
        this.maxNumOfComponents = maxNumOfComponents;
        this.findOnlyAccurateDisconnection = findOnlyAccurateDisconnection;
        this.withJG = withJG;
        this.roadsToSkip = new HashSet<>(0);
        this.onlyStoreResultByRoads = onlyStoreResultByRoads;
        this.alwaysOpenRoads = alwaysOpenRoads;

        // add vertices
        for (Node n : this.net.getNodes()) {
            graph.addVertex(n);
        }

        // add roads
        for (Road r : this.net.getRoads()) {
            graph.addEdge(r.getFirst_node(), r.getSecond_node(), r);
        }
        MinimumSpanningTree<Node, Road> minSpanningTree = new KruskalMinimumSpanningTree<>(graph);
        spanningTree = minSpanningTree.getMinimumSpanningTreeEdgeSet();
    }

    @Override
    public void start(final int maxClosedRoads) {

        // add roads from spanning tree to the queue, threads are going to run over all roads in the queue
        queue.addAll(spanningTree);

        //System.out.println("Roads = " + net.getRoads().size() + ", Spanning-tree roads = " + spanningTree.size());
        
        // skip road processed before
        queue.removeAll(roadsToSkip);

        // validate number of threads
        if (NUMBER_OF_THREADS < 1) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "Number of threads must be at least 1.");
        }

        // inicialize runnables and threads
        Runnable[] runnables = new Runnable[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            // inicialize runnable by specific algorithm modification
            if (withJG) {
                runnables[i] = new AlgCycleRunnableJG(net, disconnectionCollector, maxClosedRoads,
                        maxNumOfComponents, findOnlyAccurateDisconnection, onlyStoreResultByRoads,
                        alwaysOpenRoads
                );
            } else {
                runnables[i] = new AlgCycleRunnable(net, disconnectionCollector, maxClosedRoads, maxNumOfComponents, findOnlyAccurateDisconnection);
            }

            // set thread to its runnable and name it
            threads[i] = new Thread(runnables[i]);
            threads[i].setName(Integer.toString(i));
        }

        // start threads - searching
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads[i].start();
        }

        // wait for end of all threads
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ExperimentSetup.LOGGER.log(Level.SEVERE, "Exception during waiting to end of all threads in the algorithm.", ex);
            }
        }

        if (ExperimentSetup.DEBUG) {
            System.out.println("*** Cycles length ***");
            for (Map.Entry<Integer, Integer> entrySet : AlgCycleRunnableJG.cycleLength.entrySet()) {
                Integer key = entrySet.getKey();
                Integer value = entrySet.getValue();

                System.out.println(key + "\t" + value);
            }
            System.out.println("***");
        }

        System.out.println("Algorithm done.");
    }
}
