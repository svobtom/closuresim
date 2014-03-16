package cz.muni.fi.closuresim;

/**
 * Algorithm interface. All others algorithms must implement this interface.
 *
 * @author Tom
 */
public interface Algorithm {

    /**
     * Start the algorithm.
     * 
     * @param maxClosedRoads maximum number of roads which will be closed in the algorithm
     */
    public void start(int maxClosedRoads);
    
}
