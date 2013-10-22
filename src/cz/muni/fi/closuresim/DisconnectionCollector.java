package cz.muni.fi.closuresim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * Add one disconnections to the list.
     *
     * @param dis
     * @return
     */
    public boolean addDisconnection(Disconnection dis) {
        return disconnections.add(dis);
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
     * Get all collected disconnection with specified number of closed roads.
     *
     * @return set of disconnection
     */
    public Set<Disconnection> getDisconnections(int numOfRoads) {
        Set<Disconnection> result = new HashSet<>();

        for (Iterator<Disconnection> it = this.disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            if (disconnection.getNumClosedRoads() == numOfRoads) {
                result.add(disconnection);
            }
        }

        return result;
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
    public int getNumberOfDisconnections(int numberOfClosedRoads) {
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

    /**
     * Store results to files. Result was stored to files results-n.csv, where n
     * is number of closed roads.
     */
    public void storeResultsToFile() {
        // check maximum num of closed roads
        int maxClosedRoads = 0;
        for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
            Disconnection disconnection = it.next();
            maxClosedRoads = Math.max(maxClosedRoads, disconnection.getNumClosedRoads());
        }

        // create File Writers
        try {
            FileWriter fileWriterAll = new FileWriter("results-all.csv");
            BufferedWriter outAll = new BufferedWriter(fileWriterAll);
            FileWriter[] fileWriter;
            fileWriter = new FileWriter[maxClosedRoads];
            BufferedWriter[] out;
            out = new BufferedWriter[maxClosedRoads];

            for (int i = 0; i < maxClosedRoads; i++) {
                fileWriter[i] = new FileWriter("results-" + (i + 1) + ".csv");
                out[i] = new BufferedWriter(fileWriter[i]);
            }

            // interate over all results
            for (Iterator<Disconnection> it = disconnections.iterator(); it.hasNext();) {
                Disconnection disconnection = it.next();

                outAll.write("");
                // iterate over all closed roads in the disconnection
                for (Iterator<Road> it1 = disconnection.getRoads().iterator(); it1.hasNext();) {
                    Road r = it1.next();
                    // write to right result-n.csv file
                    out[disconnection.getNumClosedRoads() - 1].write(r.getName() + ";");
                    // write to all result file
                    outAll.write(r.getName() + ";");
                }
                outAll.write("VAL;");

                out[disconnection.getNumClosedRoads() - 1].write(Integer.toString((Integer) disconnection.getEvaluation(0)) + ";");
                out[disconnection.getNumClosedRoads() - 1].write(Double.toString((Double) disconnection.getEvaluation(1))); // todo
                out[disconnection.getNumClosedRoads() - 1].newLine();

                outAll.write(Integer.toString((Integer) disconnection.getEvaluation(0)) + ";"); // todo
                outAll.write(Double.toString((Double) disconnection.getEvaluation(1)));
                outAll.newLine();
            }

            // closing files
            for (int i = 0; i < maxClosedRoads; i++) {
                out[i].close();
            }
            outAll.close();

        } catch (IOException ex) {
            Logger.getLogger(ResultCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sort(int type) {
        
        switch (type) {
            case 1:
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
