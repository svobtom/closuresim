package cz.muni.fi.closuresim;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author Tom
 */
public class ConfigFileReader {

    private final String fileName;
    private Properties properties;

    ConfigFileReader(String fileName) {
        this.fileName = fileName;
        this.properties = new Properties();
    }

    public Properties loadConfiguration() {
        try {
            this.properties.load(new FileInputStream(this.fileName));
        } catch (IOException ex) {
            String msg = "Configuration file config.prop wasn't found";
            ExperimentSetup.LOGGER.log(Level.SEVERE, msg, ex);
            System.err.println(msg);
            System.exit(1);
        }

        return this.properties;
    }
}
