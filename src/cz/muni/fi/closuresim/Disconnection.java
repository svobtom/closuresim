package cz.muni.fi.closuresim;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author Tom
 */
public class Disconnection {

    /**
     * Roads which was closed in the disconnection.
     */
    private final Set<Road> roads;

    /**
     * Map of valuations which can be assigned to the disconnection.
     */
    private final Map<Valuation, Number> valuation = new HashMap<>();

    public Disconnection(Set<Road> closedRoads) {
        roads = new HashSet<>();
        roads.addAll(closedRoads);
    }

    public Disconnection(Collection<Road> closedRoads) {
        roads = new HashSet<>();
        roads.addAll(closedRoads);
    }

    public Disconnection(Road r1) {
        roads = new HashSet<>();
        roads.add(r1);
    }

    public Disconnection(Road r1, Road r2) {
        roads = new HashSet<>();
        roads.add(r1);
        roads.add(r2);
    }

    public Disconnection(Road r1, Road r2, Road r3) {
        roads = new HashSet<>();
        roads.add(r1);
        roads.add(r2);
        roads.add(r3);
    }

    public Disconnection(Road r1, Road r2, Road r3, Road r4) {
        roads = new HashSet<>();
        roads.add(r1);
        roads.add(r2);
        roads.add(r3);
        roads.add(r4);
    }

    /**
     * Return names of roads in disconnection separated by semicolon
     *
     * @return
     */
    public String getRoadsNames() {
        String result = "";

        for (Iterator<Road> it = roads.iterator(); it.hasNext();) {
            Road road = it.next();
            result = result + road.getName() + ";";
        }

        // return names of roads, remove last semicolon
        return result.substring(0, result.length() - 1);
    }

    public Set<Road> getRoads() {
        return roads;
    }

    public int getNumClosedRoads() {
        return roads.size();
    }

    /**
     * Get precalculated number of components of this disconnection.
     *
     * @return number of components, 0 if number wasn't be computed before
     */
    public int getNumOfComponents() {
        Number result = getEvaluation(Valuation.COMPONENTS);
        if (result != null) {
            return (int) result;
        } else {
            return 0;
        }
    }

    /**
     * Get precalculated variance of this disconnection.
     *
     * @return variance, 0 if variance wasn't be computed before
     */
    public double getVariance() {
        Number result = getEvaluation(Valuation.VARIANCE);
        if (result != null) {
            return (double) result;
        } else {
            return 0;
        }
    }

    /**
     * Get precalculated remoteness components index.
     *
     * @return remoteness components index, 0 if variance wasn't be computed before
     */
    public int getRCI() {
        Number result = getEvaluation(Valuation.REMOTENESS_COMPONENTS);
        if (result != null) {
            return (int) result;
        } else {
            return 0;
        }
    }

    /**
     * Get precalculated number of inhabitants of smaller component.
     *
     * @return umber of inhabitants, 0 if variance wasn't be computed before
     */
    public int getSmallerComponentInhabitants() {
        Number result = getEvaluation(Valuation.THE_MOST_INHABITANTS_IN_THE_SMALLEST_COMPONENT);
        if (result != null) {
            return (int) result;
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (this.roads != null ? this.roads.hashCode() : 0);
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
        final Disconnection other = (Disconnection) obj;
        // if sets of roads doesn't contains exactlly same roads, it isn't equals
        if (!this.roads.equals(other.roads)) {
            return false;
        }
        return true;
    }

    /*
     public Map<Integer, Number> getValuation() {
     return valuation;
     }
     */
    public void setEvaluation(Valuation v, Number num) {
        this.valuation.put(v, num);
    }

    /**
     * Return numeric valuation.
     *
     * @param v numeric value, null if doesn't exist
     * @return
     */
    public Number getEvaluation(Valuation v) {
        return this.valuation.get(v);
    }

    @Override
    public String toString() {
        return "Disconnection{" + "roads=" + roads + "}";
    }

}
