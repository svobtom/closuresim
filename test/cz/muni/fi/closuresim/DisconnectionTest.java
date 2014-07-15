package cz.muni.fi.closuresim;

import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tom
 */
public class DisconnectionTest {

    private final Net net;

    public DisconnectionTest() {
        net = new Net();
    }

    @Before
    public void setUp() {
        Road ra = new Road(101);
        net.addRoad(ra);
        Road rb = new Road(102);
        net.addRoad(rb);
        Road rc = new Road(103);
        net.addRoad(rc);
        Road rd = new Road(104);
        net.addRoad(rd);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of compareTo method, of class Disconnection.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        Set<Road> sr = new HashSet();
        Disconnection d0 = new Disconnection(sr);
        Disconnection d1a = new Disconnection(net.getRoad(101));
        Disconnection d1b = new Disconnection(net.getRoad(102));
        Disconnection d2a = new Disconnection(net.getRoad(101), net.getRoad(102));
        Disconnection d2b = new Disconnection(net.getRoad(101), net.getRoad(103));

        // different number of roads
        assertEquals(-1, d0.compareTo(d1a));
        assertEquals(1, d1a.compareTo(d0));

        assertEquals(-1, d1a.compareTo(d2a));
        assertEquals(1, d2a.compareTo(d1a));

        // same number of roads
        assertEquals(-1, d1a.compareTo(d1b));
        assertEquals(1, d1b.compareTo(d1a));

        assertEquals(-1, d2a.compareTo(d2b));
        assertEquals(1, d2b.compareTo(d2a));
    }

    /**
     * Test of compareTo method, of class Disconnection.
     */
    @Test
    public void testCompareToAndEquals() {
        Set<Road> sr = new HashSet();
        Disconnection d0 = new Disconnection(sr);
        Disconnection d1a = new Disconnection(net.getRoad(101));
        Disconnection d1b = new Disconnection(net.getRoad(102));
        Disconnection d2a = new Disconnection(net.getRoad(101), net.getRoad(102));
        Disconnection d2b = new Disconnection(net.getRoad(101), net.getRoad(103));

        assertEquals(d0.equals(d0), d0.compareTo(d0) == 0);
        assertEquals(d0.equals(d1b), d0.compareTo(d1b) == 0);

        assertEquals(d1a.equals(d1a), d1a.compareTo(d1a) == 0);
        assertEquals(d1a.equals(d1b), d1a.compareTo(d1b) == 0);

        assertEquals(d2a.equals(d2a), d2a.compareTo(d2a) == 0);
        assertEquals(d2a.equals(d1b), d2a.compareTo(d1b) == 0);
    }

}
