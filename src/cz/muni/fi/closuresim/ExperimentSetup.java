package cz.muni.fi.closuresim;

import java.util.Properties;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
//import cz.muni.fi.closuresim.tools.NetReducer;

/**
 * ExperimentSetup is main class of this project. There are defined variables
 * which are used in entire application. It contains the main method.
 *
 * @author Tom
 */
public class ExperimentSetup {

    /**
     * Number of CPU cores of the (virtual) machine.
     */
    private static final int AVAILABLE_CPUs = Runtime.getRuntime().availableProcessors();
    /**
     * Maximal number of CPU cores which application can use. Availible by all
     * class of this package.
     */
    protected static int USE_CPUs;
    /**
     * Properties loaded from configuration file.
     */
    private static Properties properties;
    /**
     * Output directory
     */
    protected static final File outputDirectory = new File("results");
    /**
     * Logger which log all events in application.
     */
    protected static final MyLogger LOGGER = new MyLogger(outputDirectory);

    /**
     * The main method
     *
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        LOGGER.startExperiment();

        // read command line arguments
        String configFile; // location of config file
        if (args.length > 0 && args[0].length() > 0) {
            configFile = args[0];
        } else {
            configFile = "config.properties";
        }
        // number of threads
        if (args.length > 1 && args[1].length() > 0) {
            USE_CPUs = Integer.parseInt(args[1]);
        } else {
            // use implicit value of used CPU
            USE_CPUs = Math.min(AVAILABLE_CPUs, 8);
        }

        // load configuration file
        ConfigFileReader cfr = new ConfigFileReader(configFile);
        properties = cfr.loadConfiguration();

        // prepare output directory
        ResultWriter.prepareOutputDirectory(outputDirectory, configFile);

        System.out.println();
        System.out.println("========================== ClosureSim ===========================");
        System.out.println("Program use " + USE_CPUs + "/" + AVAILABLE_CPUs + " CPUs installed in this system. ");

        // Create new network, load nodes and roads from file
        NetLoader loader = new NetLoader(Boolean.parseBoolean(properties.getProperty("ignoreInhabitants")));
        Net net;

        // choose load function according to input file(s)
        if (properties.getProperty("fileEdges") == null) {
            net = loader.load(properties.getProperty("fileNodes"));
            properties.setProperty("fileEdges", properties.getProperty("fileNodes"));
        } else {
            net = loader.load(properties.getProperty("fileNodes"), properties.getProperty("fileEdges"));
        }

        net.setName("Road network");
        System.out.print("From files \"" + properties.getProperty("fileNodes") + "\" ");
        if (properties.getProperty("fileEdges") != null) {
            System.out.print("and " + properties.getProperty("fileEdges") + " ");
        }
        System.out.println("was loaded " + loader.getNumOfLoadedNodes() + " nodes and " + loader.getNumOfLoadedRoads() + " roads.");

        if (properties.getProperty("fileCoordinates") != null) {
            loader.loadCoordinates(properties.getProperty("fileCoordinates"));
        }

        Set<Road> roadsToSkip = new HashSet<>();
        if (properties.getProperty("roadsToSkip") != null) {
            roadsToSkip = loader.loadRoadsToSkip(properties.getProperty("roadsToSkip"));
            System.out.println("Partial results processing: " + roadsToSkip.size() + " road(s) should be skipped.");
        }

        // test if the net is connected at start
        if (net.isInOneComponent()) {
            System.out.println("The net is connected at the beginning.");
        } else {
            System.out.println("WARNING: The net is NOT connected at the beginning.");
            System.out.println("The net contains " + net.getNumOfComponents() + " components!");
        }

        // create collector of disconnection
        DisconnectionCollector disconnectionCollector = new DisconnectionCollector();

        // do the algorithm
        LOGGER.addTime("startOfAlgorithm");
        Algorithm alg;
        switch (properties.getProperty("algorithm")) {
            case "simpa":
                alg = new AlgorithmSimpleParallel(net, disconnectionCollector);
                break;
            case "comb":
                alg = new AlgorithmCombinatoric(
                        net,
                        disconnectionCollector,
                        Integer.parseInt(properties.getProperty("minDistanceOfClosedRoads", "1")));
                break;
            case "cycle":
                alg = new AlgorithmCycle(
                        net,
                        disconnectionCollector,
                        Integer.parseInt(properties.getProperty("numberOfComponents", "2")),
                        Boolean.parseBoolean(properties.getProperty("findOnlyAccurate")),
                        true,
                        roadsToSkip,
                        Boolean.parseBoolean(properties.getProperty("onlyStoreResultByRoads")));
                break;
            case "cycle-my":
                alg = new AlgorithmCycle(
                        net,
                        disconnectionCollector,
                        Integer.parseInt(properties.getProperty("numberOfComponents", "2")),
                        Boolean.parseBoolean(properties.getProperty("findOnlyAccurate")),
                        false,
                        roadsToSkip,
                        Boolean.parseBoolean(properties.getProperty("onlyStoreResultByRoads")));
                break;
            case "cycle-cut":
                alg = new AlgorithmCycleCut(
                        net,
                        disconnectionCollector,
                        Integer.parseInt(properties.getProperty("numberOfComponents", "2")),
                        Boolean.parseBoolean(properties.getProperty("findOnlyAccurate")),
                        true);
                break;
            case "cycle-cut-my":
                alg = new AlgorithmCycleCut(
                        net,
                        disconnectionCollector,
                        Integer.parseInt(properties.getProperty("numberOfComponents", "2")),
                        Boolean.parseBoolean(properties.getProperty("findOnlyAccurate")),
                        false);
                break;
            case "cycle2":
                alg = new AlgorithmCycle2(net, disconnectionCollector, outputDirectory);
                break;
            case "load":
                alg = new AlgorithmLoadResults(
                        net,
                        disconnectionCollector,
                        properties.getProperty("resultFile"),
                        Integer.parseInt(ExperimentSetup.properties.getProperty("startOnLine")),
                        Integer.parseInt(ExperimentSetup.properties.getProperty("stopOnLine")));
                break;
            case "test":
                alg = new AlgorithmTest(
                        net,
                        disconnectionCollector
                );
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

        // test found disconnections        
        //disconnectionCollector.findUnnecessaryDisconnections(false); // only for two components cut-sets
        // evaluation of disconnection
        if (!properties.getProperty("evaluation").equals("none")) {

            // evaluation of the disconnection
            
            LOGGER.addTime("prepareEvaluation");
            Evaluation evaluation = new Evaluation(net, disconnectionCollector, Boolean.parseBoolean(properties.getProperty("onlyStoreResultByRoads")));
            System.out.println();
            System.out.println("Evaluaton started (" + evaluation.getClass().getSimpleName() + ")");
            LOGGER.addTime("endOfprepareEvaluation");
            System.out.println("------------------------------------------------------------------");
            LOGGER.addTime("startOfEvaluation");
            evaluation.start();
            LOGGER.addTime("endOfEvaluation");
            System.out.println("------------------------------------------------------------------");
            System.out.println();

            // filter not minimal cut-sets
            LOGGER.addTime("startOfFiltering");
            final int shouldFiltering = Integer.parseInt(properties.getProperty("filteringNoMinimalCS", "0"));
            if (shouldFiltering == 1 || shouldFiltering == 2) {

                int foundNcs = -1;
                switch (shouldFiltering) {
                    case 1:
                        foundNcs = disconnectionCollector.notMinimalCutSets(false);
                        break;
                    case 2:
                        foundNcs = disconnectionCollector.notMinimalCutSets(true);
                        break;
                }

                System.out.println("No minimal cut-sets = " + foundNcs);
            }
            LOGGER.addTime("endOfFiltering");

            // sorting of the evaluation
            LOGGER.addTime("startOfSorting");
            disconnectionCollector.sort(Valuation.VARIANCE);
            LOGGER.addTime("endOfSorting");

            disconnectionCollector.displayDetailStatistics();
            System.out.println();
        }

        // display disconnections
        disconnectionCollector.displayStatistics();

        LOGGER.addTime("startOfStoringToFiles");
        ResultWriter resultWriter = new ResultWriter(disconnectionCollector, outputDirectory);
        resultWriter.storeResultsToFiles();
        System.out.println();
        LOGGER.addTime("endOfStoringToFiles");

        if (properties.getProperty("evaluation").equals("deep")) {
            LOGGER.addTime("startOfAnalysis");
            // let only specified number of the worst disconnections
            disconnectionCollector.letOnlyFirst(Integer.parseInt(properties.getProperty("numberToAnalyze")));

            // do the detail analyze
            GraphExport ge = new GraphExport(outputDirectory);
            ge.export(net); // vizualize net
            ge.exportDisconnections(net, disconnectionCollector, Integer.parseInt(properties.getProperty("numberToAnalyzeByRoad"))); // vizualize cut-sets

            CutSetsAnalyzer csa = new CutSetsAnalyzer(net, disconnectionCollector, outputDirectory);
            csa.doRoadsStatisctics();
            LOGGER.addTime("endOfAnalysis");
        }

        // delete temporary files and parcial results
        resultWriter.deleteTempFiles();
        System.out.println();
        LOGGER.endExperiment();
    } // end method main
} // end class
