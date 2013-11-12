/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.closuresim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyledEditorKit;

/**
 *
 * @author Tom
 */
public class AlgorithmCycle implements Algorithm {

    private Net net;
    private DisconnectionCollector disconnectionCollector;

    public AlgorithmCycle(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
    }

    @Override
    public void start(int maxClosedRoads) {

        // create array of runnables and threads
        AlgCycleRunnable runnable = new AlgCycleRunnable(net, disconnectionCollector);
        Thread thread = new Thread(runnable);

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
