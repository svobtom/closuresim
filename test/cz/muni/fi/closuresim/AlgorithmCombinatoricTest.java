package cz.muni.fi.closuresim;

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
public class AlgorithmCombinatoricTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCombinatoricNumber() {
        System.out.println("combinatoricNumber");
        assertEquals(861, AlgorithmCombinatoric.combinatoricNumber(42, 2));
        assertEquals(1929501, AlgorithmCombinatoric.combinatoricNumber(84, 4));
        assertEquals(50, AlgorithmCombinatoric.combinatoricNumber(50, 1));
        assertEquals(18564, AlgorithmCombinatoric.combinatoricNumber(18, 6));
        assertEquals(1, AlgorithmCombinatoric.combinatoricNumber(1, 1));
    }
}