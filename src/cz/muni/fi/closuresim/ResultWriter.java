package cz.muni.fi.closuresim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;

/**
 * Manage storing results to the specified directory.
 * 
 * @author Tom
 */
public class ResultWriter {

    private DisconnectionCollector disconnectionCollector;
    private final File outputDirectory;

    public ResultWriter(DisconnectionCollector dc, final File od) {
        this.disconnectionCollector = dc;
        this.outputDirectory = od;
    }

    public ResultWriter(final File od) {
        this.outputDirectory = od;
        od.mkdir();
    }

    /**
     * Prepare directory to store the results. If it already exists,
     * it is added by suffix "-old" at first. 
     * 
     * @param outputDirectory
     * @param configFile 
     */
    public static void prepareOutputDirectory(File outputDirectory, String configFile) {
        // prepareDirectory
        if (outputDirectory.exists()) {
            FileUtils.deleteQuietly(new File(outputDirectory + "-old"));
            outputDirectory.renameTo(new File(outputDirectory + "-old"));
        }
        outputDirectory.mkdir();

        try {
            FileUtils.copyFileToDirectory(new File(configFile), outputDirectory);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "Can't save properties to result folder.", ex);
        }
    }

    /**
     * Store results to files. Result was stored to files results-n.csv, where n
     * is number of closed roads.
     */
    public void storeResultsToFiles() {

        Set<Disconnection> disconnections = disconnectionCollector.getDisconnections();

        // check maximum num of closed roads
        int maxClosedRoads = 0;
        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            maxClosedRoads = Math.max(maxClosedRoads, disconnection.getNumClosedRoads());
        }

        // create File Writers
        try {
            FileWriter fileWriterAll = new FileWriter(new File(outputDirectory, "results-all.csv"));
            BufferedWriter outAll = new BufferedWriter(fileWriterAll);
            FileWriter[] fileWriter;
            fileWriter = new FileWriter[maxClosedRoads];
            BufferedWriter[] out;
            out = new BufferedWriter[maxClosedRoads];

            for (int i = 0; i < maxClosedRoads; i++) {
                fileWriter[i] = new FileWriter(new File(outputDirectory, "results-" + (i + 1) + ".csv"));
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
                outAll.write("VAL;");

                final double roundedVariance = (double) Math.round(disconnection.getVariance() * 100) / 100;
                
                out[disconnection.getNumClosedRoads() - 1].write(Integer.toString(disconnection.getNumOfComponents()) + ";");
                out[disconnection.getNumClosedRoads() - 1].write(Double.toString(roundedVariance) + ";");
                out[disconnection.getNumClosedRoads() - 1].write(Integer.toString(disconnection.getSmallerComponentInhabitants()) + ";");
                out[disconnection.getNumClosedRoads() - 1].write(Integer.toString(disconnection.getRCI()));
                out[disconnection.getNumClosedRoads() - 1].newLine();

                outAll.write(Integer.toString(disconnection.getNumOfComponents()) + ";");
                outAll.write(Double.toString(roundedVariance) + ";");
                outAll.write(Integer.toString(disconnection.getSmallerComponentInhabitants()) + ";");
                outAll.write(Integer.toString(disconnection.getRCI()));
                outAll.newLine();
            }

            // closing files
            for (int i = 0; i < maxClosedRoads; i++) {
                out[i].close();
            }
            outAll.close();

            System.out.println("Result was stored to files results-n.csv, where n is number of closed roads. ");

        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Write partial result to file.
     *
     * @param name name of the file
     * @param disconnections collection to write
     */
    public void storeDisconnection(String name, Collection<Disconnection> disconnections) {

        // create File Writers
        try {
            FileWriter fileWriterAll = new FileWriter(new File(outputDirectory, name + ".csv"));
            BufferedWriter outAll = new BufferedWriter(fileWriterAll);

            if (disconnections.isEmpty()) {
                outAll.write("None_disconnection_found.");
                outAll.close();
                return;
            }

            for (Disconnection disconnection : disconnections) {

                boolean first = true;
                // iterate over all closed roads in the disconnection
                for (Road r : disconnection.getRoads()) {
                    if (!first) {
                        outAll.write(";");
                    } else {
                        first = false;
                    }

                    outAll.write(r.getName());
                }
                outAll.newLine();
            }

            outAll.close();
            fileWriterAll.close();

            ExperimentSetup.LOGGER.log(Level.INFO, "Partial result " + name + ".csv was stored.");

        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "Error writing partial result to file", ex);
            System.exit(1);
        }

    }

}
