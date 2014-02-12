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
        roads = new  HashSet<>();
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
     * Get number of copmponents. 
     * 
     * @return number of components.
     */
    public int getNumOfComponents() {
        return (int) this.valuation.get(Valuation.COMPONENTS);
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

    public Number getEvaluation(Valuation v) {
        return this.valuation.get(v);
    }

    @Override
    public String toString() {
        return "Disconnection{" + "roads=" + roads + "}";
    }

}
