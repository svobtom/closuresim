package cz.muni.fi.closuresim;

import org.paukov.combinatorics.ICombinatoricsVector;
import org.paukov.combinatorics.IFilter;

/**
 *
 * @author Tom
 */
public class IFilterImplDisconnection implements IFilter<ICombinatoricsVector<Road>> {

    @Override
    public boolean accepted(long index, ICombinatoricsVector<Road> value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
