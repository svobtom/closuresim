package cz.muni.fi.closuresim;

/**
 * Interface of algorithm which find discconection.
 *
 * @author Tom
 */
public interface Algorithm {

    /**
     * Start the algorithm where maximum number of closed roads is determined.
     */
    public void start(int maxClosedRoads);
}
