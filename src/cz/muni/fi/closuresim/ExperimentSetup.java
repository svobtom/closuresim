package cz.muni.fi.closuresim;

/**
 *
 * @author Tom
 */
public class ExperimentSetup {

    private static final int AVAILABLE_CPUs = Runtime.getRuntime().availableProcessors();
    protected static int USE_CPUs;

    public static void main(String[] args) {
        final long startExecutionTime = System.currentTimeMillis();

        // implicite files (used when no arguments found)
        String FILE_NODES = "obce_plus.csv";
        String FILE_ROADS = "silnice_plus.csv";
        
        // handle command line arguments
        if (args.length > 0 && args[0].length() > 0) {
            FILE_NODES = args[0];
        }
        if (args.length > 1 && args[1].length() > 0) {
            FILE_ROADS = args[1];
        }
        if (args.length > 2 && args[2].length() > 0) {
            FILE_ROADS = args[1];
            USE_CPUs = Integer.parseInt(args[2]);
        } else {
            USE_CPUs = Math.min(AVAILABLE_CPUs, 20);
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

        // create result collector
        ResultCollector resultCollector = new ResultCollector();

        // do the algorithm        
        Algorithm alg = new AlgorithmSimpleParallel(net, resultCollector);
        System.out.println();
        System.out.println("Start of the algorithm: " + alg.getClass().getSimpleName());
        System.out.println("------------------------------------------------------------------");
        alg.start();
        System.out.println("------------------------------------------------------------------");

        // display and store results
        resultCollector.displayStatistics();
        System.out.println("Total number of disconnection " + resultCollector.getNumberOfDisconnection());
        resultCollector.storeResultsToFile();
        System.out.println("Result was stored to files results-n.csv, where n is number of closed roads. ");
        
        // display time of execution
        final long endExecutionTime = System.currentTimeMillis();
        System.out.println("Execution time is " + (endExecutionTime - startExecutionTime) / 1000.0 + " seconds. ");
        System.out.println("==================================================================");
        
    } // end method main
} // end class
