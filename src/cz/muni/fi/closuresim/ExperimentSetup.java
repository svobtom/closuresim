package cz.muni.fi.closuresim;

//import cz.muni.fi.closuresim.tools.NetReducer;

/**
 *
 * @author Tom
 */
public class ExperimentSetup {

    private static final int AVAILABLE_CPUs = Runtime.getRuntime().availableProcessors();
    protected static int USE_CPUs;
    protected static final MyLogger LOGGER = new MyLogger("experiment");

    public static void main(String[] args) {
        LOGGER.startExperiment();
        
        // implicite files (used when no arguments found)
        String FILE_NODES = "obce.csv";
        String FILE_ROADS = "silnice.csv";
        // implicite number of closed roads
        int MAX_CLOSED_ROADS = 1;
        
        // handle command line arguments
        if (args.length > 0 && args[0].length() > 0) {
            FILE_NODES = args[0];
        }
        if (args.length > 1 && args[1].length() > 0) {
            FILE_ROADS = args[1];
        }
        if (args.length > 2 && args[2].length() > 0) {
            MAX_CLOSED_ROADS = Integer.parseInt(args[2]);
        }
        if (args.length > 3 && args[3].length() > 0) {
            //FILE_ROADS = args[1]; // TODO - proc sem to sem dal?
            USE_CPUs = Integer.parseInt(args[3]);
        } else {
            // second prameter is implicit value of used CPU
            USE_CPUs = Math.min(AVAILABLE_CPUs, 8);
        }
        
        System.out.println();
        System.out.println("========================== ClosureSim ===========================");
        System.out.println("Program use " + USE_CPUs + "/" + AVAILABLE_CPUs + " CPUs installed in this system. ");

        // Create new network, load nodes and roads from file
        NetLoader loader = new NetLoader();
        Net net;
        
        // choose load function according to input file(s)
        if (args.length > 0 && "-txt".equals(args[0])) {
            net = loader.load(args[1]);
        } else {
            net = loader.load(FILE_NODES, FILE_ROADS);
        }
        
        net.setName("Silniční síť");
        System.out.println("From file \"" + FILE_NODES + "\" was loaded " + loader.getNumOfLoadedNodes() + " nodes.");
        System.out.println("From file \"" + FILE_ROADS + "\" was loaded " + loader.getNumOfLoadedRoads()+ " roads.");
        
        //NetReducer nr = new NetReducer(net);
        //nr.reduce(8);

        // display network
        //System.out.println(net.toString());

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
        Algorithm alg = new AlgorithmSimpleParallel(net, disconnectionCollector);
        System.out.println();
        System.out.println("Finding disconnection algorithm: " + alg.getClass().getSimpleName());
        System.out.println("------------------------------------------------------------------");
        alg.start(MAX_CLOSED_ROADS);
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
        // 0 - number of conponent, 1 - variance
        disconnectionCollector.sort(1);
        LOGGER.addTime("endOfSorting");
        
        // display and store disconnections to a file
        disconnectionCollector.displayStatistics();
        disconnectionCollector.storeResultsToFile();
        System.out.println("Result was stored to files results-n.csv, where n is number of closed roads. ");
        System.out.println();
        
        LOGGER.endExperiment();
    } // end method main
    
} // end class
