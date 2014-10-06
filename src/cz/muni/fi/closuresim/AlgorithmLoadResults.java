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
 * This algorithm loads disconnections from existing result file in output
 * format of the application.
 *
 * @author Tom
 */
class AlgorithmLoadResults implements Algorithm {

    private final Net net;
    private final DisconnectionCollector disconnectionCollector;
    private final String fileName;
    private final int startResultNo;
    private final int stopResultNo;

    /**
     * Constructor initializing all necessary variables.
     *
     * @param net network
     * @param disconnectionCollector
     * @param fileName name of file with result in specified format (every
     * disconnection is on single line)
     * @param startResultNo first line (disconnection) to load
     * @param stopResultNo last line (disconnection) to load
     */
    public AlgorithmLoadResults(Net net, DisconnectionCollector disconnectionCollector, String fileName, int startResultNo, int stopResultNo) {
        this.net = net;
        this.disconnectionCollector = disconnectionCollector;
        this.fileName = fileName;
        this.startResultNo = startResultNo;
        this.stopResultNo = stopResultNo;
    }

    @Override
    public void start(final int number) {

        loadExactNumberOfRoads(number);

    }

    /**
     * Load disconnection
     * 
     * @param number of closed roads in the disconnection (on the line)
     */
    private void loadExactNumberOfRoads(final int number) {

        try {
            InputStream fis = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                // skip prefix
                if (lineNumber < startResultNo) {
                    lineNumber++;
                    continue;
                }

                // skip postfix
                if (lineNumber > stopResultNo) {
                    lineNumber++;
                    break;
                }
                lineNumber++;

                String[] elements = line.split(";");

                Set<Road> setRoad = new HashSet<>();
                for (int i = 0; i < number; i++) {
                    Road r = this.net.getRoad(elements[i]);

                    if (r == null) {
                        throw new IllegalArgumentException("Net doesn't contain the road (" + elements[i] + ") on the line " + (lineNumber - 1) + " which should be in disconnection.");
                    }
                    
                    setRoad.add(r);
                }

                Disconnection dis = new Disconnection(setRoad);
                
                // shouldn't occur
                if (dis.getNumClosedRoads() != number) {
                    System.out.print(setRoad);
                    System.out.println(dis.getNumClosedRoads() + " " + number);
                    throw new IllegalStateException("Wrong number of closed roads on line " + lineNumber + " - " + line);
                }
                
                this.disconnectionCollector.addDisconnection(dis);

            }
        } catch (FileNotFoundException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "File with results wasn't found.", ex);
            
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "IOException occur during loading results.", ex);
        }
    }

}
