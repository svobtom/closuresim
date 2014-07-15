package cz.muni.fi.closuresim;

import java.util.List;
import java.util.logging.Level;
import org.jgrapht.Graph;
import org.jgrapht.graph.Multigraph;

/**
 * Evaluate disconenctions.
 *
 * @author Tom
 */
public class EvaluationRunnable implements Runnable {

    private final Net net;
    private final DisconnectionCollector subCollector;

    private final int ANNOUNCEMENT_TRESHOLD = 1000000;
    private final int ANNOUNCEMENT_EVERY = 1000000;

    private final Graph<Node, Road> graph = new Multigraph(Road.class);

    public EvaluationRunnable(Net net, DisconnectionCollector subCollector) {
        this.net = net.clone();

        // for sure open roads
        for (Road road : this.net.getRoads()) {
            road.open();
        }

        this.subCollector = subCollector;

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

        ExperimentSetup.LOGGER.log(Level.INFO, "Evaluation thread {0} started.", Thread.currentThread().getName());

        int processed = 0;
        final int toProcess = subCollector.getNumberOfDisconnections();
        final boolean logAnnouncement = toProcess >= ANNOUNCEMENT_TRESHOLD;

        for (Disconnection disconnection : subCollector.getDisconnections()) {

            evaluateDisconnection(disconnection);
            processed++;

            if (logAnnouncement && (processed % ANNOUNCEMENT_EVERY) == 0) {

                final int percentage = (processed * 100) / toProcess;

                ExperimentSetup.LOGGER.log(Level.INFO, "Evaluation thread {0} process " + percentage + "%.", Thread.currentThread().getName());
            }

        }

        ExperimentSetup.LOGGER.log(Level.INFO, "Evaluation thread {0} end.", Thread.currentThread().getName());

    }

    /**
     * Evaluate given disconnection.
     *
     * @param disconnection disconenction to evaluate
     */
    private void evaluateDisconnection(Disconnection disconnection) {

        // close roads named in disconnection
        for (Road road : disconnection.getRoads()) {
            this.net.getRoad(road.getId()).close();
        }

        /* Evaluating */
        // components
        final int numOfComp = this.net.getNumOfComponents();
        if (numOfComp < 2) {
            ExperimentSetup.LOGGER.log(Level.WARNING, "In Disconnection collector was found not-disconnection.");
        }
        disconnection.setEvaluation(Valuation.COMPONENTS, numOfComp);

        // check minimal cut-set
        final boolean minimalCutSet = this.net.isClosedRoadsMinimalCutSet();
        disconnection.setEvaluation(Valuation.IS_MINIMAL_CUT_SET, minimalCutSet ? 1 : 0);

        if (minimalCutSet) {
            // variance
            final double variance = this.net.getValueOfBadness(numOfComp, disconnection.getNumClosedRoads());
            disconnection.setEvaluation(Valuation.VARIANCE, variance);

            // remoteness components
            final int maxNumOfRemoteness = this.net.getRemotenessComponentsIndex();
            disconnection.setEvaluation(Valuation.REMOTENESS_COMPONENTS, maxNumOfRemoteness);

            // get the least number of inhabitants in a component
            List<Integer> inhabitants = this.net.getInhabitantsByComponents();
            // get the minimum
            int minInhabitants = Integer.MAX_VALUE;
            for (Integer integer : inhabitants) {
                if (integer < minInhabitants) {
                    minInhabitants = integer;
                }
            }
            disconnection.setEvaluation(Valuation.THE_MOST_INHABITANTS_IN_THE_SMALLEST_COMPONENT, minInhabitants);
        }

        // open roads named in disconnection
        for (Road road : disconnection.getRoads()) {
            this.net.getRoad(road.getId()).open();
        }
    }

    /**
     * Evaluate given disconnection. TODO by jGraphT
     *
     * @param disconnection disconenction to evaluate
     */
    private void evaluateDisconnectionJG(Disconnection disconnection) {

        // remove closed roads from the graph
        for (Road roadTORemove : disconnection.getRoads()) {
            graph.removeEdge(roadTORemove);
        }

        /* Evaluating */
        // components
        final int numOfComp = this.net.getNumOfComponents();
        if (numOfComp < 2) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "In Disconnection collector was found not-disconnection.");
            return;
        }
        disconnection.setEvaluation(Valuation.COMPONENTS, numOfComp);

        // check minimal cut-set
        final boolean cutSet = this.net.isClosedRoadsMinimalCutSet();
        disconnection.setEvaluation(Valuation.IS_MINIMAL_CUT_SET, cutSet ? 1 : 0);

        // variance
        final double variance = this.net.getValueOfBadness(numOfComp, disconnection.getNumClosedRoads());
        disconnection.setEvaluation(Valuation.VARIANCE, variance);

        // remoteness components
        final int maxNumOfRemoteness = this.net.getRemotenessComponentsIndex();
        disconnection.setEvaluation(Valuation.REMOTENESS_COMPONENTS, maxNumOfRemoteness);

        // get the least number of inhabitants in a component
        List<Integer> inhabitants = this.net.getInhabitantsByComponents();
        // get the minimum
        int minInhabitants = Integer.MAX_VALUE;
        for (Integer integer : inhabitants) {
            if (integer < minInhabitants) {
                minInhabitants = integer;
            }
        }
        disconnection.setEvaluation(Valuation.THE_MOST_INHABITANTS_IN_THE_SMALLEST_COMPONENT, minInhabitants);

        // add roads to the net again
        for (Road roadToAdd : disconnection.getRoads()) {
            graph.addEdge(roadToAdd.getFirst_node(), roadToAdd.getSecond_node(), roadToAdd);
        }

    }

}
