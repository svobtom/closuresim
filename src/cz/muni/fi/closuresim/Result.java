package cz.muni.fi.closuresim;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Tom
 */
public class Result {

    /**
     * ID of roads, which was closed
     */
    private final Set<Integer> roadsID;
    /**
     * Evaluation of disconnected network
     */
    private final double variance;
    private final int numOfComponents;

    public Result(int id_r, double variance) {
        roadsID = new TreeSet<Integer>();
        this.roadsID.add(id_r);
        this.variance = variance;
        this.numOfComponents = 999;
    }

    public Result(int id_fr, int id_sr, double variance) {
        roadsID = new TreeSet<Integer>();
        this.roadsID.add(id_fr);
        this.roadsID.add(id_sr);
        this.variance = variance;
        this.numOfComponents = 999;
    }

    public Result(int id_fr, int id_sr, int id_tr, double variance) {
        roadsID = new TreeSet<Integer>();
        this.roadsID.add(id_fr);
        this.roadsID.add(id_sr);
        this.roadsID.add(id_tr);
        this.variance = variance;
        this.numOfComponents = 999;
    }

    public Set<Integer> getRoadsID() {
        return Collections.unmodifiableSet(roadsID);
    }

    public int getNumClosedRoads() {
        return roadsID.size();
    }

    public double getVariance() {
        return variance;
    }

    public int getNumOfComponents() {
        return numOfComponents;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Result other = (Result) obj;
        if (this.roadsID != other.roadsID && (this.roadsID == null || !this.roadsID.equals(other.roadsID))) {
            return false;
        }
        return true;
    }
}
