package cz.muni.fi.closuresim;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Tom
 */
public class Node {

    private int id;
    private int type;
    private Set<Road> roads;
    private String name;
    private int marking;
    private int numOfInhabitants;
    private double lat;
    private double lng;

    public Node() {
        roads = new HashSet<>();
    }

    /**
     * Copy node without information about roads.
     *
     * @param n Node to copy
     */
    public Node(final Node n) {
        this.id = n.id;
        this.type = n.type;
        this.name = n.name;
        this.marking = n.marking;
        this.numOfInhabitants = n.numOfInhabitants;
        roads = new HashSet<>();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.id;
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
        final Node other = (Node) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public int getType() {
        return type;
    }

    /**
     *
     * @return String connected cities separated by space
     */
    public String getConnectedCities() {
        String s = "";
        for (Iterator<Road> it = roads.iterator(); it.hasNext();) {
            Road r = it.next();

            if (r.getFirst_node().getName().equals(this.name)) {
                s = s.concat(r.getSecond_node().getName() + " ");
            } else {
                s = s.concat(r.getFirst_node().getName() + " ");
            }
        }
        return s;
    }

    @Override
    public String toString() {
        String n_type;
        if (this.type == 2) {
            n_type = "city";
        } else {
            n_type = "crossroad";
        }

        String n_name;
        if (this.name.equals("")) {
            n_name = "";

        } else {
            n_name = this.name + ", ";
        }
        return this.name + " (" + this.id + ", " + n_type + ")";
        //return "\n" + this.id + ":" + " " + n_name + n_type + ", connected=" + getConnectedCities();
    }

    public void setType(String type) {
        if (type.equals("city")) {
            this.type = 2;
        } else {
            this.type = 1;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Road> getRoads() {
        return this.roads;
    }

    public void setRoads(Set roads) {
        this.roads = roads;
    }

    public boolean setRoad(Road r) {
        return this.roads.add(r);
    }

    /**
     * Clear list of connected roads.
     */
    public void clearRoads() {
        this.roads.clear();
    }

    /**
     * Connect one road to the city.
     *
     * @param road
     */
    public void addRoad(Road road) {
        this.roads.add(road);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumOfInhabitant() {
        return numOfInhabitants;
    }

    public void setNumOfInhabitants(int numOfInhabitants) {
        this.numOfInhabitants = numOfInhabitants;
    }

    /**
     * Increase number of inhabitants by given value.
     * 
     * @param increase 
     */
    public void addInhabitants(int increase) {
        this.numOfInhabitants += increase;
    }

    public int getMarking() {
        return marking;
    }

    public void setMarking(int marking) {
        this.marking = marking;
    }

    /**
     * Return set of connected neighbours (roads must be open)
     *
     * @return Set<Node> Set of nodes
     */
    public Set<Node> getNeighbours() {
        Set<Node> result = new HashSet();
        for (Iterator<Road> it = roads.iterator(); it.hasNext();) {
            Road r = it.next();
            if (!r.isClosed()) {
                if (r.getFirst_node() == this) {
                    result.add(r.getSecond_node());
                } else {
                    result.add(r.getFirst_node());
                }
            }
        }
        return result;
    }

    void setLat(double d) {
        this.lat = d;
    }

    void setLng(double d) {
        this.lng = d;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

}
