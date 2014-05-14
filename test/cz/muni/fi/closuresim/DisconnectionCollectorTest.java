package cz.muni.fi.closuresim;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tom
 */
public class DisconnectionCollectorTest {

    private Net net;
    private DisconnectionCollector disconnectionCollector;

    public DisconnectionCollectorTest() {
        net = new Net();
        disconnectionCollector = new DisconnectionCollector();
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

    @Test
    public void addDisconnectionTest() {

        Disconnection d1 = new Disconnection(net.getRoad(101));
        Disconnection d2 = new Disconnection(net.getRoad(102));
        Disconnection d3 = new Disconnection(net.getRoad(102), net.getRoad(103));

        disconnectionCollector.addDisconnection(d1);
        disconnectionCollector.addDisconnection(d2);
        disconnectionCollector.addDisconnection(d3);

        assertEquals(3, disconnectionCollector.getNumberOfDisconnections());
        assertEquals(2, disconnectionCollector.getNumberOfDisconnections(1));
        assertEquals(1, disconnectionCollector.getNumberOfDisconnections(2));

        disconnectionCollector.addDisconnection(d1);
        disconnectionCollector.addDisconnection(d2);
        disconnectionCollector.addDisconnection(d3);

        assertEquals(3, disconnectionCollector.getNumberOfDisconnections());
        assertEquals(2, disconnectionCollector.getNumberOfDisconnections(1));
        assertEquals(1, disconnectionCollector.getNumberOfDisconnections(2));

        Disconnection d11 = new Disconnection(net.getRoad(101));
        Disconnection d12 = new Disconnection(net.getRoad(102));
        Disconnection d13 = new Disconnection(net.getRoad(102), net.getRoad(103));

        disconnectionCollector.addDisconnection(d11);
        disconnectionCollector.addDisconnection(d12);
        disconnectionCollector.addDisconnection(d13);

        assertEquals(3, disconnectionCollector.getNumberOfDisconnections());
        assertEquals(2, disconnectionCollector.getNumberOfDisconnections(1));
        assertEquals(1, disconnectionCollector.getNumberOfDisconnections(2));
    }

    @Test
    public void otherTest() {
        List<Disconnection> ld = new LinkedList<>();
        ld.add(new Disconnection(net.getRoad(101)));
        ld.add(new Disconnection(net.getRoad(102)));
        ld.add(new Disconnection(net.getRoad(102), net.getRoad(103)));
        ld.add(new Disconnection(net.getRoad(102), net.getRoad(103)));

        disconnectionCollector.addDisconnections(ld);
        assertEquals(3, disconnectionCollector.getNumberOfDisconnections());
        assertEquals(2, disconnectionCollector.getMaxNumberOfClosedRoads());
        
        Set<Disconnection> sd;
        sd = disconnectionCollector.getDisconnections();
        
        assertEquals(3, sd.size());
    }
}
