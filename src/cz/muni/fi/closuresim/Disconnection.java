package cz.muni.fi.closuresim;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Class representing set of roads in the net which can make disconnection.
 *
 * @author Tom
 */
public class Disconnection implements Comparable<Disconnection> {

    /**
     * Roads which was closed in the disconnection. Can be set only in
     * constructors.
     */
    private final SortedSet<Road> roads;
    /**
     * Sum of roads IDs to hashCode purpose.
     */
    private final int priparedHash;
    /**
     * Shift according to count of roads.
     */
    private static final int SHIFT = 10000;
    /**
     * Map of valuations which can be assigned to the disconnection.
     */
    private final Map<Valuation, Number> valuation = new HashMap<>();

    public Disconnection(Collection<Road> closedRoads) {
        roads = new TreeSet<>();
        roads.addAll(closedRoads);

        int s = 0;
        for (Road road : closedRoads) {
            s += road.getId();
        }
        priparedHash = closedRoads.size() * SHIFT + s;
    }

    public Disconnection(Road r1) {
        roads = new TreeSet<>();
        roads.add(r1);
        priparedHash = SHIFT + r1.getId();
    }

    public Disconnection(Road r1, Road r2) {
        roads = new TreeSet<>();
        roads.add(r1);
        roads.add(r2);
        priparedHash = 2 * SHIFT + (r1.getId() + r2.getId());
    }

    public Disconnection(Road r1, Road r2, Road r3) {
        roads = new TreeSet<>();
        roads.add(r1);
        roads.add(r2);
        roads.add(r3);
        priparedHash = 3 * SHIFT + (r1.getId() + r2.getId() + r3.getId());
    }

    public Disconnection(Road r1, Road r2, Road r3, Road r4) {
        roads = new TreeSet<>();
        roads.add(r1);
        roads.add(r2);
        roads.add(r3);
        roads.add(r4);
        priparedHash = 4 * SHIFT + (r1.getId() + r2.getId() + r3.getId() + r4.getId());
    }

    /**
     * Return names of roads in disconnection separated by semicolon
     *
     * @return
     */
    public String getRoadsNames() {
        String result = "";

        for (Road road : roads) {
            result = result + road.getName() + ";";
        }

        // return names of roads, remove last semicolon
        return result.substring(0, result.length() - 1);
    }

    public Set<Road> getRoads() {
        return roads;
    }

    /**
     * Get sorted roads according its names. 
     * 
     * @return 
     */
    public SortedSet<Road> getSortedRoads() {
        
        SortedSet<Road> r = new TreeSet<>(new RoadComparator());
        
        for (Road road :this.roads) {
            r.add(road);
        }
        
        return r;
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
     * @return remoteness components index, 0 if variance wasn't be computed
     * before
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
     * @return number of inhabitants, 0 if variance wasn't be computed before
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
        return priparedHash;
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
        return this.roads.equals(other.roads);
    }

    @Override
    public int compareTo(Disconnection other) {
        // disconnection with less roads is less, vice versa 
        if (this.roads.size() < other.roads.size()) {
            return -1;
        } else if (this.roads.size() > other.roads.size()) {
            return 1;
        }

        Iterator<Road> itThis = this.roads.iterator();
        Iterator<Road> itOther = other.roads.iterator();

        while (itThis.hasNext()) {
            Road roadThis = itThis.next();
            Road roadOther = itOther.next();

            final int roadComparison = roadThis.compareTo(roadOther);
            if (roadComparison != 0) {
                return roadComparison;
            }
        }
        // disconnections are same
        return 0;
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
