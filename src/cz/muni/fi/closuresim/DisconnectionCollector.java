package cz.muni.fi.closuresim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tom
 */
public class DisconnectionCollector {

    private Set<Disconnection> disconnections;

    public DisconnectionCollector() {
        this.disconnections = new HashSet<Disconnection>();
    }

    /**
     * Add one disconnections to the list.
     *
     * @param dis
     * @return
     */
    public boolean addDisconnection(Disconnection dis) {
        return disconnections.add(dis);
    }

    public Set<Disconnection> getDisconnections() {
        return Collections.unmodifiableSet(disconnections);
    }

    /**
     * Display names of roads in every disconnections.
     */
    public void displayDisconnections() {
        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection dis = it.next();
            System.out.println(dis.getRoadsNames());
        }
    }

    /**
     * Count number of disconnections (doesn't matter on number of roads in
     * disconnections).
     *
     * @return - number of road
     */
    public int getNumberOfDisconnections() {
        return disconnections.size();
    }

    /**
     * Display overall statistics
     */
    public void displayStatistics() {
        int maxClosedRoads = 0;
        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            maxClosedRoads = Math.max(maxClosedRoads, disconnection.getNumClosedRoads());
        }

        int[] numOfDisconnection;
        numOfDisconnection = new int[maxClosedRoads];

        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            numOfDisconnection[disconnection.getNumClosedRoads() - 1]++;
        }

        for (int i = 0; i < maxClosedRoads; i++) {
            int cr = i + 1;
            System.out.println("Disconnection closing " + cr + " roads: " + numOfDisconnection[i]);
        }

        System.out.println("Total number of disconnection " + getNumberOfDisconnections());
    }

    /**
     * Store results to files. Result was stored to files results-n.csv, where n
     * is number of closed roads.
     */
    public void storeResultsToFile() {
        // check maximum num of closed roads
        int maxClosedRoads = 0;
        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            maxClosedRoads = Math.max(maxClosedRoads, disconnection.getNumClosedRoads());
        }

        // create File Writers
        try {
            FileWriter fileWriterAll = new FileWriter("results-all.csv");
            BufferedWriter outAll = new BufferedWriter(fileWriterAll);
            FileWriter[] fileWriter;
            fileWriter = new FileWriter[maxClosedRoads];
            BufferedWriter[] out;
            out = new BufferedWriter[maxClosedRoads];

            for (int i = 0; i < maxClosedRoads; i++) {
                fileWriter[i] = new FileWriter("results-" + (i + 1) + ".csv");
                out[i] = new BufferedWriter(fileWriter[i]);
            }

            // interate over all results
            for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
                Disconnection disconnection = it.next();

                outAll.write("");
                // iterate over all closed roads in the disconnection
                for (Iterator<Road> it1 = disconnection.getRoads().iterator(); it1.hasNext();) {
                    Road r = it1.next();
                    // write to right result-n.csv file
                    out[disconnection.getNumClosedRoads() - 1].write(r.getName() + ";");
                    // write to all result file
                    outAll.write(r.getName() + ";");
                }
                outAll.write("VALUATION;");

                out[disconnection.getNumClosedRoads() - 1].write(Integer.toString((Integer) disconnection.getValuation().get(0)) + ";");
                out[disconnection.getNumClosedRoads() - 1].write(Double.toString((Double) disconnection.getValuation().get(1))); // todo
                out[disconnection.getNumClosedRoads() - 1].newLine();
                
                outAll.write(Integer.toString((Integer) disconnection.getValuation().get(0)) + ";"); // todo
                outAll.write(Double.toString((Double) disconnection.getValuation().get(1)));
                outAll.newLine();
            }

            // closing files
            for (int i = 0; i < maxClosedRoads; i++) {
                out[i].close();
            }
            outAll.close();

        } catch (IOException ex) {
            Logger.getLogger(ResultCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
