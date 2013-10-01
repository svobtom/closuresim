package cz.muni.fi.closuresim;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Tom
 */
public class RoadsSelection {

    private Set<Road> roads;

    public RoadsSelection() {
        this.roads = new HashSet<>();
    }

    public RoadsSelection(int initialCapacity) {
        this.roads = new HashSet<>(initialCapacity);
    }

    /**
     * All methods addRoads return true if and only if the set roads doesn't
     * contain any of the input roads. In this case all input roads are added.
     *
     * @param r1
     * @return
     */
    public boolean addRoad(Road r1) {
        return roads.add(r1);
    }

    public boolean addRoads(Road r1, Road r2) {
        if (roads.add(r1)) {
            return roads.add(r2);
        }
        return false;
    }

    public boolean addRoads(Road r1, Road r2, Road r3) {
        if (roads.add(r1)) {
            if (roads.add(r2)) {
                return roads.add(r3);
            }
            return false;
        }
        return false;
    }

    public boolean addRoads(Road r1, Road r2, Road r3, Road r4) {
        if (roads.add(r1)) {
            if (roads.add(r2)) {
                if (roads.add(r3)) {
                    return roads.add(r4);
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public boolean addRoads(Collection<Road> sr) {
        return roads.addAll(sr);
    }

    public void closeAllRoads() {
        for (Iterator<Road> it = roads.iterator(); it.hasNext();) {
            Road road = it.next();
            road.close();
        }
    }

    public void openAllRoads() {
        for (Iterator<Road> it = roads.iterator(); it.hasNext();) {
            Road road = it.next();
            road.open();
        }
    }

    public Set<Road> getRoads() {
        return roads;
    }
    
    public int getNumOfRoads() {
        return roads.size();
    }

    @Override
    public String toString() {
        return roads.toString();
    }
    
}
