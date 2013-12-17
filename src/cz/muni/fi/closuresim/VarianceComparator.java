package cz.muni.fi.closuresim;

import java.util.Comparator;

/**
 *
 * @author Tom
 */
public class VarianceComparator implements Comparator<Disconnection> {

    @Override
    public int compare(Disconnection d1, Disconnection d2) {
        double d1_variance = (Double) d1.getEvaluation(Valuation.VARIANCE);
        double d2_variance = (Double) d2.getEvaluation(Valuation.VARIANCE);
        if (d1_variance < d2_variance) {
            return -1;
        } else if (d1_variance == d2_variance) {
            // when variance is equal, compare roads name
            return d1.getRoadsNames().compareTo(d2.getRoadsNames()) ;
                
        }
        return 1;
    }
    
}
