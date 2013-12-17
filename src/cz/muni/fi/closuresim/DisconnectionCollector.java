package cz.muni.fi.closuresim;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Tom
 */
public class DisconnectionCollector {

    private Set<Disconnection> disconnections;
    private Comparator comparator;

    public DisconnectionCollector() {
        this.disconnections = Collections.synchronizedSet(new HashSet<Disconnection>());
    }

    /**
     * Add one disconnections to the set.
     *
     * @param dis
     * @return
     */
    public boolean addDisconnection(Disconnection dis) {
        return disconnections.add(dis);
    }

    public boolean addDisconnections(Collection<Disconnection> dis) {
        return this.disconnections.addAll(dis);
    }

    /**
     * Get all collected disconnection.
     *
     * @return all disconnection
     */
    public Set<Disconnection> getDisconnections() {
        return disconnections;
    }

    /**
     * Get all disconnection with specified number of closed roads.
     *
     * @return set of disconnection
     */
    public Set<Disconnection> getDisconnections(final int numOfRoads) {
        SortedSet<Disconnection> result = new TreeSet<>(this.comparator);

        for (Iterator<Disconnection> it = this.disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            if (disconnection.getNumClosedRoads() == numOfRoads) {
                result.add(disconnection);
            }
        }

        return result;
    }

    /**
     * Get maximum number of closed roads in disconnections.
     *
     * @return int maximum closed roads
     */
    public int getMaxNumberOfClosedRoads() {
        // check maximum num of closed roads
        int maxClosedRoads = 0;
        for (Iterator<Disconnection> it = this.disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            maxClosedRoads = Math.max(maxClosedRoads, disconnection.getNumClosedRoads());
        }
        return maxClosedRoads;
    }

    /**
     * Check if the disconnection is found and write down yet.
     *
     * @param dis
     * @return true if it already contain gicen disconnection
     */
    public boolean containDisconnection(Disconnection dis) {
        return disconnections.contains(dis);
    }

    /**
     * Check if some set of roads make disconenction.
     *
     * @param rs
     * @return true if the set is of road is already disconnection
     */
    public boolean containDisconnection(RoadsSelection rs) {
        Disconnection tempDis = new Disconnection(rs.getRoads());

        return disconnections.contains(tempDis);
    }

    public boolean make1RDisconnection(Collection<Road> roads) {
        for (Road road : roads) {
            Disconnection dis = new Disconnection(road);
            if (disconnections.contains(dis)) {
                return true;
            }
        }
        return false;
    }

    public boolean make2RDisconnection(Collection<Road> roads) {
        if (roads.size() == 1) {
            return false;
        }

        for (Road road1 : roads) {
            for (Road road2 : roads) {
                Disconnection dis = new Disconnection(road1, road2);
                if (!road1.equals(road2) && disconnections.contains(dis)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Display names of roads in every disconnections.
     */
    public void displayDisconnections() {
        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection dis = it.next();
            System.out.println(dis.getRoadsNames());
        }
    }

    /**
     * Count number of disconnections (doesn't matter on number of roads in
     * disconnections).
     *
     * @return - number of road
     */
    public int getNumberOfDisconnections() {
        return disconnections.size();
    }

    /**
     * Count number of disconnections with specific number of closed roads.
     *
     * @param numberOfClosedRoads - number of closed roads
     * @return
     */
    public int getNumberOfDisconnections(final int numberOfClosedRoads) {
        int result = 0;
        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            if (disconnection.getNumClosedRoads() == numberOfClosedRoads) {
                result++;
            }
        }
        return result;
    }

    /**
     * Display overall statistics
     */
    public void displayStatistics() {
        int maxClosedRoads = 0;
        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            maxClosedRoads = Math.max(maxClosedRoads, disconnection.getNumClosedRoads());
        }

        int[] numOfDisconnection;
        numOfDisconnection = new int[maxClosedRoads];

        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            numOfDisconnection[disconnection.getNumClosedRoads() - 1]++;
        }

        for (int i = 0; i < maxClosedRoads; i++) {
            int cr = i + 1;
            System.out.println("Disconnection closing " + cr + " roads: " + numOfDisconnection[i]);
        }

        System.out.println("Total number of disconnection " + getNumberOfDisconnections());
    }

    public void sort(Valuation v) {

        switch (v) {
            case VARIANCE:
                this.comparator = new VarianceComparator();
                break;

            default:
                this.comparator = new VarianceComparator();
        }

        SortedSet<Disconnection> sortedSet = new TreeSet<>(this.comparator);
        sortedSet.addAll(this.disconnections);
        this.disconnections = sortedSet;
    }

    /**
     * Let only specified number of disconnection.
     *
     * @param num
     */
    public void letOnlyFirst(final int num) {
        if (num == -1) {
            return;
        }
        int i = 0;
        SortedSet<Disconnection> newSet = new TreeSet<>(this.comparator);
        for (Iterator<Disconnection> it = this.disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            if (i++ < num) {
                newSet.add(disconnection);
            } else {
                break;
            }
        }
        this.disconnections = newSet;
    }
}
