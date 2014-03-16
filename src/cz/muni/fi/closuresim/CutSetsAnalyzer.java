package cz.muni.fi.closuresim;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Analyze cut-sets stored in Disconnection collector
 * 
 * @author Tom
 */
class CutSetsAnalyzer {

    final private Net net;
    final private DisconnectionCollector disconnectionCollector;
    final private File outputDirectory;

    public CutSetsAnalyzer(Net net, DisconnectionCollector disconnectionCollector, File outputDirectory) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
        this.outputDirectory = outputDirectory;
    }

    /**
     * 
     */
    public void doRoadsStatisctics() {

        // inicialize
        Map<Road, Integer> roadsStatistics = new HashMap<>(this.net.getNodes().size());
        for (Road road : this.net.getRoads()) {
            roadsStatistics.put(road, 0);
        }

        // counting, for every disconnection
        for (Disconnection disconnection : this.disconnectionCollector.getDisconnections()) {
            // for every road  
            for (Road r : disconnection.getRoads()) {
                // increment counter for road
                roadsStatistics.put(r, roadsStatistics.get(r) + 1);
            }
        }
        
        GraphExport ge = new GraphExport(this.outputDirectory);
        ge.exportColoredRoads(this.net, roadsStatistics);
    }

}
