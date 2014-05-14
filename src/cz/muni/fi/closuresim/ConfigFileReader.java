package cz.muni.fi.closuresim;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Load and verify values from configuration file. 
 * 
 * @author Tom
 */
public class ConfigFileReader {

    private final String fileName;
    private final Properties properties;

    /**
     * Create instance of ConfigFileReader.
     * 
     * @param fileName name of file with configuration
     */
    public ConfigFileReader(final String fileName) {
        this.fileName = fileName;
        this.properties = new Properties();
    }

    /**
     * Load values from the file.
     * 
     * @return loaded properties
     */
    public Properties loadConfiguration() {
        try {
            this.properties.load(new FileInputStream(this.fileName));
        } catch (IOException ex) {
            String msg = "Configuration file " + fileName + " wasn't found";
            ExperimentSetup.LOGGER.log(Level.SEVERE, msg, ex);
            System.err.println(msg);
            System.exit(1);
        }

        validationOfProperties();

        return this.properties;
    }

    /**
     * Basic verication of the properties. 
     */
    private void validationOfProperties() {

        // file with nodes
        if (properties.getProperty("fileNodes") == null || properties.getProperty("fileNodes").length() < 1) {
            String msg = "Configuration file " + fileName + ": fileNodes isn't defined or in right format.";
            ExperimentSetup.LOGGER.log(Level.SEVERE, msg);
            System.err.println(msg);
            System.exit(1);
        }

        // algorithm name
        if (properties.getProperty("algorithm") == null || properties.getProperty("algorithm").length() < 1) {
            String msg = "Configuration file " + fileName + ": algorithm isn't defined or in right format.";
            ExperimentSetup.LOGGER.log(Level.SEVERE, msg);
            System.err.println(msg);
            System.exit(1);
        }

        // number of roads
        try {
            Integer.parseInt(properties.getProperty("numberOfRoads"));
        } catch (NumberFormatException ex) {
            String msg = "Configuration file " + fileName + ": numberOfRoads is not parsable to integer.";
            ExperimentSetup.LOGGER.log(Level.SEVERE, msg, ex);
            System.err.println(msg);
            System.exit(1);
        }

        // number of components
        if (properties.getProperty("algorithm").equals("cycle")) {
            try {
                Integer.parseInt(properties.getProperty("numberOfComponents"));
            } catch (NumberFormatException ex) {
                String msg = "Configuration file " + fileName + ": numberOfComponents is not parsable to integer.";
                ExperimentSetup.LOGGER.log(Level.SEVERE, msg, ex);
                System.err.println(msg);
                System.exit(1);
            }
        }

        // method of evaluation
         if (properties.getProperty("evaluation") == null || properties.getProperty("evaluation").length() < 1) {
            String msg = "Configuration file " + fileName + ": evaluation isn't defined or it isn't in right format.";
            ExperimentSetup.LOGGER.log(Level.SEVERE, msg);
            System.err.println(msg);
            System.exit(1);
        }

            // number to analyze
            try {
                Integer.parseInt(properties.getProperty("numberToAnalyze"));
            } catch (NumberFormatException ex) {
                String msg = "Configuration file " + fileName + ": numberToAnalyze is not parsable to integer.";
                ExperimentSetup.LOGGER.log(Level.SEVERE, msg, ex);
                System.err.println(msg);
                System.exit(1);
            }

            // number to analyze by road
            try {
                Integer.parseInt(properties.getProperty("numberToAnalyzeByRoad"));
            } catch (NumberFormatException ex) {
                String msg = "Configuration file " + fileName + ": numberToAnalyzeByRoad is not parsable to integer.";
                ExperimentSetup.LOGGER.log(Level.SEVERE, msg, ex);
                System.err.println(msg);
                System.exit(1);
            }
        }
    }
