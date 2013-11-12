package cz.muni.fi.closuresim;

//import cz.muni.fi.closuresim.tools.NetReducer;
import java.util.Properties;

/**
 *
 * @author Tom
 */
public class ExperimentSetup {

    /**
     * Number CPU cores on the machine.
     */
    private static final int AVAILABLE_CPUs = Runtime.getRuntime().availableProcessors();
    /**
     * Maximal number of CPU cores (Threads) which application can use.
     */
    protected static int USE_CPUs;
    /**
     * Properties loaded from configuration file. Properties are availible by
     * all class int the package.
     */
    protected static Properties properties;
    /**
     * Logger which log all events in application.
     */
    protected static final MyLogger LOGGER = new MyLogger("experiment");

    public static void main(String[] args) {
        LOGGER.startExperiment();

        // load command line arguments
        String configFile;
        if (args.length > 0 && args[0].length() > 0) {
            configFile = args[0];
        } else {
            configFile = "config.properties";
        }
        if (args.length > 1 && args[1].length() > 0) {
            USE_CPUs = Integer.parseInt(args[1]);
        } else {
            // use implicit value of used CPU
            USE_CPUs = Math.min(AVAILABLE_CPUs, 8);
        }

        // load configuration file
        ConfigFileReader cfr = new ConfigFileReader(configFile);
        properties = cfr.loadConfiguration();

        System.out.println();
        System.out.println("========================== ClosureSim ===========================");
        System.out.println("Program use " + USE_CPUs + "/" + AVAILABLE_CPUs + " CPUs installed in this system. ");

        // Create new network, load nodes and roads from file
        NetLoader loader = new NetLoader();
        Net net;

        // choose load function according to input file(s)
        if (properties.getProperty("fileEdges") == null) {
            net = loader.load(properties.getProperty("fileNodes"));
            properties.setProperty("fileEdges", properties.getProperty("fileNodes"));
        } else {
            net = loader.load(properties.getProperty("fileNodes"), properties.getProperty("fileEdges"));
        }

        net.setName("Silniční síť");
        System.out.print("From files \"" + properties.getProperty("fileNodes") + "\" ");
        if (properties.getProperty("fileEdges") != null) {
            System.out.print("and " + properties.getProperty("fileEdges") + " ");
        }
        System.out.println("was loaded " + loader.getNumOfLoadedNodes() + " nodes and " + loader.getNumOfLoadedRoads() + " roads.");

        //NetReducer nr = new NetReducer(net);
        //nr.reduce(8);

        // test if the net is connected at start
        if (net.isInOneComponent()) {
            System.out.println("The net is connected at the beginning.");
        } else {
            System.out.println("WARNING: The net is NOT connected at the beginning.");
            System.out.println("The net contains " + net.getNumOfComponents() + " components!");
            System.exit(1);
        }

        // create collector of disconnection
        DisconnectionCollector disconnectionCollector = new DisconnectionCollector();

        // do the algorithm
        Algorithm alg;
        switch (properties.getProperty("algorithm")) {
            case "simpa":
                alg = new AlgorithmSimpleParallel(net, disconnectionCollector);
                break;
            case "comb":
                alg = new AlgorithmCombinatoric(net, disconnectionCollector, Integer.parseInt(properties.getProperty("minDistanceOfClosedRoads", "1")));
                break;
            case "cycle":
                alg = new AlgorithmCycle(net, disconnectionCollector);
                break;
            case "load":
                alg = new AlgorithmLoadResults(net, disconnectionCollector, properties.getProperty("resultFile"));
                break;
            default:
                throw new IllegalArgumentException("No such algorithm.");
        }

        System.out.println();
        System.out.println("Finding disconnection algorithm: " + alg.getClass().getSimpleName());
        System.out.println("------------------------------------------------------------------");
        alg.start(Integer.parseInt(properties.getProperty("numberOfRoads")));
        System.out.println("------------------------------------------------------------------");
        LOGGER.addTime("endOfAlgorithm");

        // evaluation of the disconnection
        Evaluation evaluation = new Evaluation(net, disconnectionCollector);
        System.out.println();
        System.out.println("Evaluaton started (" + evaluation.getClass().getSimpleName() + ")");
        System.out.println("------------------------------------------------------------------");
        evaluation.start();
        System.out.println("------------------------------------------------------------------");
        System.out.println();
        LOGGER.addTime("endOfEvaluation");

        // sorting of the evaluation
        // 0 - number of components, 1 - variance
        disconnectionCollector.sort(1);
        LOGGER.addTime("endOfSorting");

        // display and store disconnections to a file
        disconnectionCollector.displayStatistics();
        disconnectionCollector.storeResultsToFile();
        System.out.println("Result was stored to files results-n.csv, where n is number of closed roads. ");
        System.out.println();

        // let only specified number of the worst disconnections
        disconnectionCollector.letOnlyFirst(Integer.parseInt(properties.getProperty("numberToAnalyze", "100")));

        // do detail analyze
        GraphExport ge = new GraphExport(); // vizualize
        ge.export(net);
        ge.exportDisconnections(net, disconnectionCollector);
        System.out.println();

        LOGGER.endExperiment();
    } // end method main
} // end class
