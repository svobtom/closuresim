package cz.muni.fi.closuresim;

/**
 * Various valuations which can be assigned to Disconnection.
 *
 * @author Tom
 */
public enum Valuation {

    /**
     * Variance according to sum of number of inhabitants of the netowork nodes.
     */
    VARIANCE,
    /**
     * Number of components of the network.
     */
    COMPONENTS,
    /**
     * Number of roads to repair to connect network again. Number of steps.
     */
    REMOTENESS_COMPONENTS,
    /**
     * Number of inhabitants of the smallest component.
     */
    THE_MOST_INHABITANTS_IN_THE_SMALLEST_COMPONENT,
    /**
     * Value which is true (1) if disconnection is minimal cut-set, false (0)
     * otherwise
     */
    IS_MINIMAL_CUT_SET
}
