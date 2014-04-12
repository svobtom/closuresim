package cz.muni.fi.closuresim;

import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Tom
 */
public class EvaluationRunnable implements Runnable {

    private final Net net;
    private final DisconnectionCollector subCollector;

    public EvaluationRunnable(Net net, DisconnectionCollector subCollector) {
        this.net = net.clone();
        this.subCollector = subCollector;
    }

    @Override
    public void run() {

        for (Disconnection disconnection : subCollector.getDisconnections()) {

            evaluateDisconnection(disconnection);

        }

    }

    /**
     * Evaluate given disconnection.
     *
     * @param disconnection disconenction to evaluate
     */
    private void evaluateDisconnection(Disconnection disconnection) {

        // open all roads except the roads from disconnection set of roads
        for (Road road : this.net.getRoads()) {
            if (disconnection.getRoads().contains(road)) {
                road.close();
            } else {
                road.open();
            }
        }

        /* Evaluating */
        // components
        final int numOfComp = this.net.getNumOfComponents();
        if (numOfComp < 2) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "In Disconnection collector was found not-disconnection.");
            return;
        }
        disconnection.setEvaluation(Valuation.COMPONENTS, numOfComp);

        // variance
        final double variance = this.net.getValueOfBadness(numOfComp);
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

}
