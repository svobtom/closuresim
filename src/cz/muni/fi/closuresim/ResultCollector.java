package cz.muni.fi.closuresim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collect results of the simulation. After end of the algorithm ResultCollector
 * can display and store results to the file.
 *
 * @author Tom
 */
public class ResultCollector {

    /**
     * List of all results
     */
    private List<Result> results;

    public ResultCollector() {
        this.results = new LinkedList<Result>();
    }

    /**
     * Add result
     *
     * @param rID - id of closed road
     * @param var - variance of the net
     * @return true - if succes
     */
    public boolean addResultOne(int rID, double var) {
        Result rs = new Result(rID, var);

        if (results.contains(rs)) {
            return false;
        } else {
            return results.add(rs);
        }
    }

    /**
     * Add result
     *
     * @param frID - id of first closed road
     * @param srID - if of second closed road
     * @param var - variance
     * @return true - if succes
     */
    public boolean addResultTwo(int frID, int srID, double var) {
        Result rs = new Result(frID, srID, var);

        if (results.contains(rs)) {
            return false;
        } else {
            return results.add(rs);
        }
    }

    /**
     * Add result
     *
     * @param frID
     * @param srID
     * @param trID
     * @param var
     * @return
     */
    public boolean addResultThree(int frID, int srID, int trID, double var) {
        Result rs = new Result(frID, srID, trID, var);

        if (results.contains(rs)) {
            return false;
        } else {
            return results.add(rs);
        }
    }

    /**
     * Display results to System.out
     */
    public void displayResults() {
        for (Iterator<Result> it = results.iterator(); it.hasNext();) {
            Result result = it.next();
            for (Iterator<Integer> it1 = result.getRoadsID().iterator(); it1.hasNext();) {
                Integer id = it1.next();
                System.out.print(id.toString() + "; ");
            }
            System.out.println(result.getVariance());
        }
    }

    /**
     * Store results to files. Result was stored to files results-n.csv, where n
     * is number of closed roads.
     */
    public void storeResultsToFile() {
        // check maximum num of closed roads
        int maxClosedRoads = 0;
        for (Iterator<Result> it = results.iterator(); it.hasNext();) {
            Result result = it.next();
            maxClosedRoads = Math.max(maxClosedRoads, result.getNumClosedRoads());
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
            for (Iterator<Result> it = results.iterator(); it.hasNext();) {
                Result result = it.next();
                
                outAll.write("[ ");
                // iterate over all closed roads in the result
                for (Iterator<Integer> it1 = result.getRoadsID().iterator(); it1.hasNext();) {
                    Integer id = it1.next();
                    // write to right result-n.csv file
                    out[result.getNumClosedRoads() - 1].write(id.toString() + ";");
                    // write to all result file
                    outAll.write(id.toString() + " ");
                }
                outAll.write("];");
                
                out[result.getNumClosedRoads() - 1].write(Double.toString(result.getVariance()));
                out[result.getNumClosedRoads() - 1].newLine();
                outAll.write(Double.toString(result.getVariance()));
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

    /**
     * Get sum of all combination found in a algorithm.
     *
     * @return sum of all results
     */
    public int getNumberOfDisconnection() {
        return results.size();
    }

    public void displayStatistics() {
        int maxClosedRoads = 0;
        for (Iterator<Result> it = results.iterator(); it.hasNext();) {
            Result result = it.next();
            maxClosedRoads = Math.max(maxClosedRoads, result.getNumClosedRoads());
        }

        int[] numOfDisconnection;
        numOfDisconnection = new int[maxClosedRoads];

        for (Iterator<Result> it = results.iterator(); it.hasNext();) {
            Result result = it.next();
            numOfDisconnection[result.getNumClosedRoads() - 1]++;
        }

        for (int i = 0; i < maxClosedRoads; i++) {
            int cr = i + 1;
            System.out.println("Disconnection closing " + cr + " roads: " + numOfDisconnection[i]);
        }
    }
}
