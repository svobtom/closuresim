package cz.muni.fi.closuresim;

import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tom
 */
public class RoadTest {
    
    public RoadTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of hashCode method, of class Road.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Road instance = new Road();
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class Road.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        Road instance = new Road();
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Road.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Road instance = new Road();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isClosed method, of class Road.
     */
    @Test
    public void testIsClosed() {
        System.out.println("isClosed");
        Road instance = new Road();
        assertFalse("road wasn't open", instance.isClosed());
       instance.open();
        assertFalse("road wasn't open", instance.isClosed());
        instance.close();
        assertTrue("road wasn't closed", instance.isClosed());
        

    }

    /**
     * Test of open method, of class Road.
     */
    @Test
    public void testOpen() {
        System.out.println("open");
        Road instance = new Road();
        instance.open();
        assertFalse("road wasn't open", instance.isClosed());
    }

    /**
     * Test of close method, of class Road.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        Road instance = new Road();
        instance.close();
        assertTrue("road wasn't closed", instance.isClosed());
    }

    /**
     * Test of setClosed method, of class Road.
     */
    @Test
    public void testSetClosed() {
        System.out.println("setClosed");
        Road instance = new Road();
        instance.setClosed(true);
        assertTrue("road wasn't closed", instance.isClosed());
        instance.setClosed(false);
        assertFalse("road wasn't open", instance.isClosed());
    }

    /**
     * Test of getFirst_node method, of class Road.
     */
    @Test
    public void testGetFirst_node() {
        System.out.println("getFirst_node");
        Road instance = new Road();
        Node expResult = null;
        Node result = instance.getFirst_node();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFirst_node method, of class Road.
     */
    @Test
    public void testSetFirst_node() {
        System.out.println("setFirst_node");
        Node first_node = null;
        Road instance = new Road();
        instance.setFirst_node(first_node);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSecond_node method, of class Road.
     */
    @Test
    public void testGetSecond_node() {
        System.out.println("getSecond_node");
        Road instance = new Road();
        Node expResult = null;
        Node result = instance.getSecond_node();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSecond_node method, of class Road.
     */
    @Test
    public void testSetSecond_node() {
        System.out.println("setSecond_node");
        Node second_node = null;
        Road instance = new Road();
        instance.setSecond_node(second_node);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getId method, of class Road.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        Road instance = new Road();
        int expResult = 0;
        int result = instance.getId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setId method, of class Road.
     */
    @Test
    public void testSetId() {
        System.out.println("setId");
        int id = 0;
        Road instance = new Road();
        instance.setId(id);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNodes method, of class Road.
     */
    @Test
    public void testGetNodes() {
        System.out.println("getNodes");
        Road instance = new Road();
        Set expResult = null;
        Set result = instance.getNodes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setNodes method, of class Road.
     */
    @Test
    public void testSetNodes() {
        System.out.println("setNodes");
        Node firstNode = null;
        Node secondNode = null;
        Road instance = new Road();
        instance.setNodes(firstNode, secondNode);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class Road.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        Road instance = new Road();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class Road.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        Road instance = new Road();
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLength method, of class Road.
     */
    @Test
    public void testGetLength() {
        System.out.println("getLength");
        Road instance = new Road();
        int expResult = 0;
        int result = instance.getLength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLength method, of class Road.
     */
    @Test
    public void testSetLength() {
        System.out.println("setLength");
        int length = 0;
        Road instance = new Road();
        instance.setLength(length);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTime method, of class Road.
     */
    @Test
    public void testGetTime() {
        System.out.println("getTime");
        Road instance = new Road();
        int expResult = 0;
        int result = instance.getTime();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTime method, of class Road.
     */
    @Test
    public void testSetTime() {
        System.out.println("setTime");
        int time = 0;
        Road instance = new Road();
        instance.setTime(time);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}