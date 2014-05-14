package cz.muni.fi.closuresim;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Descendant of Java Logger. Added special behavior.
 *
 * @author Tom
 */
public class MyLogger extends Logger {

    private final File outputDirectory;
    private final String filename = "experiment.log";
    private final Map<String, Long> times = new HashMap<>();

    /**
     * Create specific logger.
     *
     * @param outputDirectory name of output directory
     */
    public MyLogger(File outputDirectory) {
        super("cz.muni.fi.closuresim", null);

        this.outputDirectory = outputDirectory;

        try {
            this.setUseParentHandlers(false); // don't log to console
            Handler handler = new FileHandler(filename); // log to the file
            //Handler handlerSimple = new FileHandler(fileName + "-simple.log"); // log to the file
            //handlerSimple.setFormatter(new SimpleFormatter());
            this.addHandler(handler);
            //this.addHandler(handlerSimple);
        } catch (IOException ex) {
            this.setUseParentHandlers(true);
            super.log(Level.SEVERE, "Can't open file to logging", ex);
        }
    }

    @Override
    public void log(Level level, String msg) {
        // in case of this levels print message
        if (level.equals(Level.SEVERE) || level.equals(Level.WARNING)) {
            System.err.println(level.toString() + ": " + msg);
        }

        super.log(level, msg);

        // severe error occured
        if (level.equals(Level.SEVERE)) {
            System.exit(1);
        }
    }

    @Override
    public void log(Level level, String msg, Throwable thrown) {
        // in case of this levels print message
        if (level.equals(Level.SEVERE) || level.equals(Level.WARNING)) {
            System.err.println(level.toString() + ": " + msg);
        }

        super.log(level, msg, thrown);

        // severe error occured
        if (level.equals(Level.SEVERE)) {
            System.exit(1);
        }
    }

    /**
     * Start experiment. Set up start time.
     */
    protected void startExperiment() {
        addTime("startTime");
        this.info("Start of experiment");
    }

    /**
     * Close all handlers (files) attached to the logger.
     */
    protected void endExperiment() {
        // display time of execution
        addTime("endTime");
        System.out.println("Finding time is " + (getTime("endOfAlgorithm") - getTime("startOfAlgorithm")) / 1000.0 + " seconds. ");
        System.out.println("Evaluation time is " + (getTime("endOfEvaluation") - getTime("startOfEvaluation")) / 1000.0 + " seconds. ");
        System.out.println("Sorting time is " + (getTime("endOfSorting") - getTime("startOfSorting")) / 1000.0 + " seconds. ");
        System.out.println("Analysis time is " + (getTime("endOfAnalysis") - getTime("startOfAnalysis")) / 1000.0 + " seconds. ");
        System.out.println("Total time is " + (getTime("endTime") - getTime("startTime")) / 1000.0 + " seconds. ");
        System.out.println("==================================================================");
        this.info("End of experiment");

        // closing handlers (files)
        for (Handler h : this.getHandlers()) {
            h.close();
        }

        try {
            final File fileFilename = new File(filename);
            FileUtils.copyFileToDirectory(fileFilename, outputDirectory);
            FileUtils.deleteQuietly(fileFilename);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "Can't copy log file, IO exception occur.", ex);
        }

    }

    /**
     * Add time shot.
     *
     * @param name the name of the point.
     */
    protected void addTime(String name) {
        this.times.put(name, System.currentTimeMillis());
        super.log(Level.INFO, name);
    }

    /**
     * Get stored time by logger.
     *
     * @param name name of time point
     * @return stored time in milliseconds if it exists, 0 otherwise
     */
    private long getTime(String name) {
        Long result = this.times.get(name);
        if (result != null) {
            return result;
        } else {
            return 0;
        }
    }
}
