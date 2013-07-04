package cz.muni.fi.closuresim;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tom
 */
public class Evaluation {
    private static final int NUMBER_OF_THREADS = ExperimentSetup.USE_CPUs;
    private Net net;
    private DisconnectionCollector disconnectionCollector;
    private DisconnectionCollector[] subCollectors;

    public Evaluation(Net net, DisconnectionCollector dc) {
        this.net = net;
        this.disconnectionCollector = dc;
        
        //EvaluationRunnable.disconnectionCollector = dc;
        int discForOneThread = this.disconnectionCollector.getNumberOfDisconnections() / NUMBER_OF_THREADS ;
        
        subCollectors = new DisconnectionCollector[NUMBER_OF_THREADS];
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            subCollectors[i] = new DisconnectionCollector();
        }
        
        int num = 1;
        for (Iterator<Disconnection> it = this.disconnectionCollector.getDisconnections().iterator(); it.hasNext();) {
            Disconnection dis = it.next();
            subCollectors[num % NUMBER_OF_THREADS].addDisconnection(dis);
            num++;
    }
    }
    
    public void start() {
        Runnable[] runnables = new Runnable[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            runnables[i] = new EvaluationRunnable(net, subCollectors[i]);
            threads[i] = new Thread(runnables[i]);
            threads[i].setName(Integer.toString(i));
        }

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads[i].start();
        }

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(AlgorithmSimpleParallel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
}
