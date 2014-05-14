package cz.muni.fi.closuresim;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test nontrivial funcionality of the Net class.
 *
 * @author Tom
 */
public class NetTest {

    Net net;

    public NetTest() {
        net = new Net();
        net.setName("Pokusna sit");
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        Random r = new Random();

        // create test network
        Node n1 = new Node(1);
        n1.setNumOfInhabitants(r.nextInt(1000) + 1);
        net.addNode(n1);
        Node n2 = new Node(2);
        n2.setNumOfInhabitants(r.nextInt(1000) + 1);
        net.addNode(n2);
        Node n3 = new Node(3);
        n3.setNumOfInhabitants(r.nextInt(1000) + 1);
        net.addNode(n3);
        Node n4 = new Node(4);
        n4.setNumOfInhabitants(r.nextInt(1000) + 1);
        net.addNode(n4);
        Road ra = new Road(101);
        net.addRoad(ra);
        Road rb = new Road(102);
        net.addRoad(rb);
        Road rc = new Road(103);
        net.addRoad(rc);
        Road rd = new Road(104);
        net.addRoad(rd);

        net.connectRoadToNode(ra, n1);
        net.connectRoadToNode(ra, n2);
        net.connectRoadToNode(rb, n2);
        net.connectRoadToNode(rb, n3);
        net.connectRoadToNode(rc, n1);
        net.connectRoadToNode(rc, n3);
        net.connectRoadToNode(rd, n2);
        net.connectRoadToNode(rd, n4);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsInOneComponentFaster() {
        assertEquals(true, net.isInOneComponentFaster());

        Node n5 = new Node(5);
        net.addNode(n5);
        Node n6 = new Node(6);
        net.addNode(n6);
        Road re = new Road(105);
        net.addRoad(re);
        net.connectRoadToNode(re, n5);
        net.connectRoadToNode(re, n6);

        assertEquals(false, net.isInOneComponentFaster());
    }

    @Test
    public void testIsInOneComponent() {
        assertEquals(true, net.isInOneComponent());

        Node n5 = new Node(5);
        net.addNode(n5);
        Node n6 = new Node(6);
        net.addNode(n6);
        Road re = new Road(105);
        net.addRoad(re);
        net.connectRoadToNode(re, n5);
        net.connectRoadToNode(re, n6);

        assertEquals(false, net.isInOneComponent());
    }

    @Test
    public void testGetNumOfComponents() {
        assertEquals(1, net.getNumOfComponents());

        Node n5 = new Node(5);
        net.addNode(n5);
        Node n6 = new Node(6);
        net.addNode(n6);
        Road re = new Road(105);
        net.addRoad(re);
        net.connectRoadToNode(re, n5);
        net.connectRoadToNode(re, n6);

        assertEquals(2, net.getNumOfComponents());
    }

    @Test
    public void testDistanceBetweenRoadsIsAtLeast() {
        List<Road> lr1 = new LinkedList<>();
        lr1.add(net.getRoad(101));
        lr1.add(net.getRoad(102));
        assertEquals(true, net.distanceBetweenRoadsIsAtLeast(1, lr1));
        assertEquals(false, net.distanceBetweenRoadsIsAtLeast(2, lr1));

        List<Road> lr2 = new LinkedList<>();
        lr2.add(net.getRoad(103));
        lr2.add(net.getRoad(104));
        assertEquals(true, net.distanceBetweenRoadsIsAtLeast(2, lr2));
        assertEquals(false, net.distanceBetweenRoadsIsAtLeast(3, lr2));
    }

    @Test
    public void testGetRemotenessComponentsIndex() {
        net.getNumOfComponents();
        assertEquals(0, net.getRemotenessComponentsIndex());

        net.getRoad(104).close();
        net.getNumOfComponents();
        assertEquals(1, net.getRemotenessComponentsIndex());

        net.getRoad(101).close();
        net.getRoad(102).close();
        net.getNumOfComponents();
        assertEquals(2, net.getRemotenessComponentsIndex());

        net.getRoad(104).open();
        net.getNumOfComponents();
        assertEquals(1, net.getRemotenessComponentsIndex());
    }
    
    @Test
    public void testFindShortestPath() {
        
        // test length of the path
        assertEquals(2, net.findShortestPath(net.getNode(1), net.getNode(4)).size());
        assertEquals(0, net.findShortestPath(net.getNode(1), net.getNode(1)).size());
        assertEquals(1, net.findShortestPath(net.getNode(1), net.getNode(2)).size());
        
    }
}
