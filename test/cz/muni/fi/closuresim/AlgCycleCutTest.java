/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.closuresim;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
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
public class AlgCycleCutTest {

    private final Random ran = new Random();

    public AlgCycleCutTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        ExperimentSetup.USE_CPUs = 4;
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

    @Test
    public void testRandomGraphs() {

        int maxNumberOfNodes = 30;
        int maxNumberOfCutSetRoads = 4;
        int numberOfCycles = 5;

        boolean complete = true;
        int numberOfNets = 3;
        for (int i = 0; i < numberOfCycles; i++) {
            Net net = testGraphThreeComponents(maxNumberOfNodes, Math.max(2, ran.nextInt(maxNumberOfCutSetRoads)), numberOfNets, complete);

            // store to file to ilustration
            File f = new File("test-result");
            f.mkdir();
            GraphExport ge = new GraphExport(f, "com3-" + i);
            ge.export(net);
        }
        complete = true;
        for (int i = 0; i < numberOfCycles; i++) {
            Net net = testGraphThreeComponents(maxNumberOfNodes, Math.max(2, ran.nextInt(maxNumberOfCutSetRoads)), numberOfNets, complete);

            // store to file to ilustration
            File f = new File("test-result");
            f.mkdir();
            GraphExport ge = new GraphExport(f, "com3c-" + i);
            ge.export(net);
        }
    }

    private Net testGraphThreeComponents(int maxNumberOfNodes, int numberOfCutSetRoads, int numberOfNets, boolean complete) {
        // create nets
        Map<Integer, Net> mapOfNets = new HashMap<>(numberOfNets);
        int count = 1;
        for (Net net : generateNets(numberOfNets, maxNumberOfNodes, complete)) {
            mapOfNets.put(count++, net);
        }

        for (int i = 1; i < numberOfNets; i++) {
            assertTrue("Net " + numberOfNets + " of network isn't in one component, should be", mapOfNets.get(i).isInOneComponent());
        }

        Queue<Node> chosenNodes1 = new LinkedList<>();
        Queue<Node> chosenNodes2 = new LinkedList<>();
        Queue<Node> chosenNodes3 = new LinkedList<>();
        Queue<Node> chosenNodes4 = new LinkedList<>();

        // choosing nodes to be incident to cut-set
        for (Node node : mapOfNets.get(1).getNodes()) {
            if (chosenNodes1.size() < numberOfCutSetRoads) {
                chosenNodes1.add(node);
            }
        }

        for (Node node : mapOfNets.get(2).getNodes()) {
            if (chosenNodes2.size() < numberOfCutSetRoads) {
                chosenNodes2.add(node);
            }
        }

        int secondNumberOfCutSetRoads = Math.max(1, ran.nextInt(numberOfCutSetRoads));
        int skip = 0;
        for (Node node : mapOfNets.get(2).getNodes()) {
            if (skip >= secondNumberOfCutSetRoads && chosenNodes3.size() < secondNumberOfCutSetRoads) {
                chosenNodes3.add(node);
            }
            skip++;
        }

        for (Node node : mapOfNets.get(3).getNodes()) {
            if (chosenNodes4.size() < secondNumberOfCutSetRoads) {
                chosenNodes4.add(node);
            }
        }

        Set<Road> cutSetRoads = new HashSet<>();
        Net result = new Net();

        // copy nodes and roads to one net
        for (Map.Entry<Integer, Net> entry : mapOfNets.entrySet()) {
            Net net = entry.getValue();

            for (Node node : net.getNodes()) {
                result.addNode(node);
            }
            for (Road road : net.getRoads()) {
                result.addRoad(road);
            }
        }

        for (int i = 0; i < numberOfCutSetRoads; i++) {
            Node n1 = chosenNodes1.poll();
            Node n2 = chosenNodes2.poll();

            Road r = new Road();
            r.setName("closed " + i + 10000);
            r.setId(i + 10000);
            r.setNodes(n1, n2);
            n1.setRoad(r);
            n2.setRoad(r);
            cutSetRoads.add(r);
            result.addRoad(r);
        }

        for (int i = 0; i < secondNumberOfCutSetRoads; i++) {
            Node n1 = chosenNodes3.poll();
            Node n2 = chosenNodes4.poll();

            Road r = new Road();
            r.setName("closed " + i + 20000);
            r.setId(i + 20000);
            r.setNodes(n1, n2);
            n1.setRoad(r);
            n2.setRoad(r);
            cutSetRoads.add(r);
            result.addRoad(r);
        }

        assertTrue("Union of networks isn't in one component, should be", result.isInOneComponent());

        DisconnectionCollector dc = new DisconnectionCollector();
        Algorithm alg = new AlgorithmCycleCut(result, dc, 3, false, true);

        alg.start(2 * numberOfCutSetRoads);
        System.out.println(dc.getNumberOfDisconnections());

        boolean cutSetFound = false;
        for (Disconnection d : dc.getDisconnections()) {
            if (d.getRoads().equals(cutSetRoads)) {
                cutSetFound = true;
            }
        }

        assertTrue("The cut-set wasn't found and should be found", cutSetFound);
        return result;
    }

    /**
     * Generate set of random networks.
     *
     * @param count - count networks in the returned set
     * @param maxNumberOfNodes - max count of nodes in a network
     * @param complete - if true all networks will be complete Kn
     * @return
     */
    private Set<Net> generateNets(int count, int maxNumberOfNodes, boolean complete) {
        Set<Net> resultSet = new HashSet<>(count);

        int nodesCounter = 0;
        int roadsCounter = 0;

        //create nets
        for (int i = 0; i < count; i++) {

            Net net = new Net();
            int number = ran.nextInt(maxNumberOfNodes);
            if (number < 10) {
                number = 10;
            }

            //create nodes
            for (int j = 0; j < number; j++) {
                Node n = new Node();
                n.setId(nodesCounter);
                n.setName("Node" + nodesCounter);
                nodesCounter++;
                net.addNode(n);
            }

            // create all roads
            for (Node n1 : net.getNodes()) {
                for (Node n2 : net.getNodes()) {
                    if (!n1.equals(n2) && (complete || ran.nextInt(100) < 10)) {
                        Road r = new Road();
                        r.setId(roadsCounter);
                        r.setName("road" + roadsCounter);
                        roadsCounter++;
                        r.setNodes(n1, n2);
                        n1.setRoad(r);
                        n2.setRoad(r);
                        net.addRoad(r);
                    }
                }
            }

            // make the graph connected
            if (!complete) {
                Node previous = null;
                for (Node node : net.getNodes()) {
                    if (previous != null && !node.getNeighbours().contains(previous)) {
                        Road r = new Road();
                        r.setId(roadsCounter);
                        r.setName("road" + roadsCounter);
                        roadsCounter++;
                        r.setNodes(node, previous);
                        node.setRoad(r);
                        previous.setRoad(r);
                        net.addRoad(r);
                    }
                    previous = node;
                }
            }

            resultSet.add(net);
        }
        return resultSet;
    }
}
