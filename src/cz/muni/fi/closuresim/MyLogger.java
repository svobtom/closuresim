package cz.muni.fi.closuresim;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Descendant of Java Logger. Special behavior added. 
 * 
 * @author Tom
 */
public class MyLogger extends Logger {

    /**
     * Create specific logger. 
     * @param fileName name of file to logging
     */
    public MyLogger(String fileName) {
        super("cz.muni.fi.closuresim", null);

        try {
            this.setUseParentHandlers(false); // don't log to console
            Handler handler = new FileHandler(fileName); // log to the file
            this.addHandler(handler);
        } catch (IOException ex) {
            this.setUseParentHandlers(true);
            this.log(Level.SEVERE, "Can't open file to logging", ex);
        }
    }
    
    /**
     * Close all handlers (files) attached to the logger. 
     */
    protected void closeLogger() {
        for (Handler h : this.getHandlers()) {
            h.close();
        }
    }
    
}
