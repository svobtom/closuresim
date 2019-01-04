package cz.muni.fi.closuresim;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Only copy partial-results directory to the right path. 
 * 
 * @author Tom
 */
class AlgorithmCopyPartialResults implements Algorithm {

    private final File partialDir;
    
    public AlgorithmCopyPartialResults(String filePath) {
        this.partialDir = new File(filePath);
    }

    @Override
    public void start(int maxClosedRoads) {
        
        try {
            ExperimentSetup.LOGGER.info("Copying partial-results started");
            FileUtils.copyDirectoryToDirectory(partialDir, ExperimentSetup.outputDirectory);
            System.out.println("Done");
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "Error during copy partial-results directory", ex);
        }
    }
    
}
