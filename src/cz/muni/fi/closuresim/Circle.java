package cz.muni.fi.closuresim;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author Tom
 */
public class Circle {

    private List<Road> listOfRoads;

    public Circle() {
        this.listOfRoads = new LinkedList<>();
    }

    /**
     *
     * @param listOfRoads
     */
    public Circle(List<Road> listOfRoads) {
        // test if one road isn't multipled in the list
        Set<Road> sr = new HashSet<>(listOfRoads);
        if (sr.size() != listOfRoads.size()) {
            ExperimentSetup.LOGGER.log(Level.WARNING, "Road can't be added to the circle because the circle has contain the road yet.");
        } else {
            
            this.listOfRoads = listOfRoads;
        }
    }

    /**
     * Add road to the circle. The road can be at most one times in the circle.
     *
     * @param r road to add
     * @return false if the road has been yet in the circle, true otherwise
     */
    public boolean addRoad(Road r) {
        if (this.listOfRoads.contains(r)) {
            ExperimentSetup.LOGGER.log(Level.WARNING, "Road can't be added to the circle because the circle has contain the road yet.");
            return false;
        } else {
            this.listOfRoads.add(r);
            return true;
        }
    }
    
    public int getLength() {
        return this.listOfRoads.size();
    }
    
    public List<Road> getRoads() {
        return Collections.unmodifiableList(this.listOfRoads);
    }

    @Override
    public String toString() {
        return  "length=" + listOfRoads.size() + " " + listOfRoads;
    }
    
    @Override
    public int hashCode() {
        int hash = 3 * listOfRoads.size();
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

        final Circle other = (Circle) obj;

        Set<Road> setThis = new HashSet<>(this.listOfRoads);
        Set<Road> setOther = new HashSet<>(other.listOfRoads);

        if (!setThis.equals(setOther)) {
            return false;
        }
        
        return true;
    }
}
