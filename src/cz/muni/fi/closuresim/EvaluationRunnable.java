package cz.muni.fi.closuresim;

import java.util.Iterator;

/**
 *
 * @author Tom
 */
public class EvaluationRunnable implements Runnable {

    private Net net;
    protected DisconnectionCollector subCollector;

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
            int numOfComp = net.getNumOfComponents();
            disconnection.setEvaluation(Valuation.COMPONENTS, numOfComp);
            double variance = net.getValueOfBadness(numOfComp);
            disconnection.setEvaluation(Valuation.VARIANCE, variance);
            //System.out.println(Thread.currentThread().getName() + ": Disconnection (" + disconnection.getRoadsNames() +") evaluated.");

        }
    }
}
