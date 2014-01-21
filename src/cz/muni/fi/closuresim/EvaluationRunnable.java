package cz.muni.fi.closuresim;

import java.util.Iterator;
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
        
        for (Iterator<Disconnection> it = subCollector.getDisconnections().iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            
            // open all roads except the roads from disconnection set of roads
            for (Iterator<Road> itRoad = net.getRoads().iterator(); itRoad.hasNext();) {
                Road road = itRoad.next();
                if (disconnection.getRoads().contains(road)) {
                    road.close();
                } else {
                    road.open();
                }
            }

            // evaluating
            final int numOfComp = net.getNumOfComponents();
            if (numOfComp < 2) {
                ExperimentSetup.LOGGER.log(Level.SEVERE, "In Disconnection collector was not disconnection.");
            }
            
            disconnection.setEvaluation(Valuation.COMPONENTS, numOfComp);
            final double variance = net.getValueOfBadness(numOfComp);
            disconnection.setEvaluation(Valuation.VARIANCE, variance);
            //System.out.println(Thread.currentThread().getName() + ": Disconnection (" + disconnection.getRoadsNames() +") evaluated.");

        }
    }
}
