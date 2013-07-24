package cz.muni.fi.closuresim;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Descendant of Java Logger. Special behavior added. 
 * 
 * @author Tom
 */
public class MyLogger extends Logger {

    private Map<String, Long> times = new HashMap<>();
    
    /**
     * Create specific logger. 
     * 
     * @param fileName name of file to logging
     */
    public MyLogger(String fileName) {
        super("cz.muni.fi.closuresim", null);

        try {
            this.setUseParentHandlers(false); // don't log to console
            Handler handler = new FileHandler(fileName + ".log"); // log to the file
            //Handler handlerSimple = new FileHandler(fileName + "-simple.log"); // log to the file
            this.addHandler(handler);
            //handlerSimple.setFormatter(new SimpleFormatter());
            //this.addHandler(handlerSimple);
        } catch (IOException ex) {
            this.setUseParentHandlers(true);
            this.log(Level.SEVERE, "Can't open file to logging", ex);
        }
    }

    @Override
    public void log(Level level, String msg) {
        // in case of this levels print message
        if (level.equals(Level.SEVERE) || level.equals(Level.WARNING)) {
            System.out.println(msg);
        }
        
        super.log(level, msg); 
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
        System.out.println("Finding time is " + (getTime("endOfAlgorithm") - getTime("startTime")) / 1000.0 + " seconds. ");
        System.out.println("Evaluation time is " + (getTime("endOfEvaluation") - getTime("endOfAlgorithm")) / 1000.0 + " seconds. ");
        System.out.println("Sorting time is " + (getTime("endTime") - getTime("endOfEvaluation")) / 1000.0 + " seconds. ");
        System.out.println("Total time is " + (getTime("endTime") - getTime("startTime")) / 1000.0 + " seconds. ");
        System.out.println("==================================================================");
        this.info("End of experiment");
        
        // closing handlers (files)
        for (Handler h : this.getHandlers()) {
            h.close();
        }
    }
    
    /**
     * Add time spot.
     * @param name name of the point. 
     * @param value 
     */
    protected void addTime(String name) {
        this.times.put(name, System.currentTimeMillis());
    }
    
    private long getTime(String name) {
        return this.times.get(name);
    }
    
}
