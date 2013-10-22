package cz.muni.fi.closuresim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author Tom
 */
class AlgorithmLoadResults implements Algorithm {

    private Net net;
    private DisconnectionCollector disconnectionCollector;
    private String fileName;

    public AlgorithmLoadResults(Net net, DisconnectionCollector disconnectionCollector, String fileName) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
        this.fileName = fileName;
    }

    @Override
    public void start(final int number) {

        if (number < 99) {
            loadExactNumberOfRoads(number);
        } else {
            loadVariableNumberOfRoads();
        }


    }

    private void loadExactNumberOfRoads(final int number) {

        try {
            InputStream fis = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                // skip prefix
                if (lineNumber < Integer.parseInt(ExperimentSetup.properties.getProperty("startOnCombinationsNo"))) {
                    continue;
                }

                // skip postfix
                if (lineNumber > Integer.parseInt(ExperimentSetup.properties.getProperty("stopOnCombinationsNo"))) {
                    break;
                }
                lineNumber++;

                String[] elements = line.split(";");

                Set<Road> setRoad = new HashSet<>();
                for (int i = 0; i < number; i++) {
                    Road r = this.net.getRoad(elements[i]);

                    if (r == null) {
                        throw new IllegalArgumentException("Net doesn't contain the road which should be in disconnection.");
                    }

                    setRoad.add(r);
                }

                Disconnection dis = new Disconnection(setRoad);
                if (dis.getNumClosedRoads() != number) {
                    throw new IllegalStateException("Wrong number of closed roads.");
                }
                this.disconnectionCollector.addDisconnection(dis);

            }
        } catch (FileNotFoundException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, null, ex);
            System.err.println("File with results not found.");
            System.exit(1);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, null, ex);
        }

    }

    private void loadVariableNumberOfRoads() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
