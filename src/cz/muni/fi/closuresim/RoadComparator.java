package cz.muni.fi.closuresim;

import java.util.Comparator;

/**
 * Comparator roads according to natural ordering by its names.
 * 
 * @author Tom
 */
public class RoadComparator implements Comparator<Road> {

    @Override
    public int compare(Road r1, Road r2) {
        
        // compare names of the roads
        return r1.getName().compareTo(r2.getName());
        
    }
    
    
    
}
