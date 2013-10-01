package cz.muni.fi.closuresim;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Tom
 */
public class Net {

    private String name;
    private Set<Node> nodes;
    private Set<Road> roads;

    public Net() {
        nodes = new HashSet<Node>();
        roads = new HashSet<Road>();
    }

    @Override
    public String toString() {
        return name + "\n==========\nNodes: \n" + nodes + "\n\nRoads: \n" + roads + "\n";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set getNodes() {
        return nodes;
    }

    public void setNodes(Set nodes) {
        this.nodes = nodes;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void addRoad(Road road) {
        this.roads.add(road);
    }

    /**
     * Find node in the net
     *
     * @param node_id
     * @return found node if exists, else null
     */
    public Node getNode(int node_id) {
        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            Node n = it.next();
            if (n.getId() == node_id) {
                return n;
            }
        }
        return null;
    }

    /**
     * Find node in the net
     *
     * @param node_id
     * @return found node if exists, else null
     */
    public Road getRoad(int roadID) {
        for (Iterator<Road> it = roads.iterator(); it.hasNext();) {
            Road r = it.next();
            if (r.getId() == roadID) {
                return r;
            }
        }
        return null;
    }

    public Set<Road> getRoads() {
        return roads;
    }

    public void setRoads(Set<Road> roads) {
        this.roads = roads;
    }

    /**
     * Count the components. If the net is connected it return 1.
     *
     * @return int - number of components in the net
     */
    public int getNumOfComponents() {
        // clearing of marking
        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            Node n = it.next();
            n.setMarking(0);
        }

        // marking of the nodes, find the first node and the others (important if the net is not connected)
        int nextMarking = 1;
        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            Node node = it.next();
            if (getNumOfComponentsRec(node, nextMarking)) {
                nextMarking++;
            }
        }

        // count different markings
        Set<Integer> dm = new HashSet();
        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            Node n = it.next();
            dm.add(n.getMarking());
        }

        return dm.size();
    }

    private boolean getNumOfComponentsRec(Node n, int mark) {
        // test if the node is marked
        if (n.getMarking() != 0) {
            // node has been marked yet
            return false;
        }

        n.setMarking(mark);
        // get all neighbours of the node and recursion on them
        for (Iterator<Node> it = n.getNeighbours().iterator(); it.hasNext();) {
            Node neighbour = it.next();
            getNumOfComponentsRec(neighbour, mark);
        }

        return true;
    }

    /**
     * Test if the net is one connected compoment
     *
     * @return true if the net is connected
     */
    public boolean isInOneComponent() {
        // short style of this method, using the other method
        //return getNumOfComponents() == 1;

        // clearing of marking
        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            Node n = it.next();
            n.setMarking(0);
        }

        // marking of the nodes, choose first node
        Iterator<Node> it = nodes.iterator();
        if (it.hasNext()) {
            Node n = it.next();
            isInOneComponentRec(n);
        }

        // check if all nodes are marked
        for (Iterator<Node> it2 = nodes.iterator(); it2.hasNext();) {
            Node n = it2.next();
            if (n.getMarking() == 0) {
                return false;
            }
        }
        return true;

    }
    
    public boolean isInOneComponentFaster() {
        // short style of this method, using the other method
        //return getNumOfComponents() == 1;

        // marking of the nodes, choose first node
        Iterator<Node> it = nodes.iterator();
        if (it.hasNext()) {
            Node n = it.next();
            isInOneComponentRec(n);
        }

        // check if all nodes are marked
        boolean result = true;
        for (Iterator<Node> it2 = nodes.iterator(); it2.hasNext();) {
            Node n = it2.next();
            if (n.getMarking() != 0) {
                n.setMarking(0);
            } else {
                result = false;
            }
        }
        return result;

    }

    private void isInOneComponentRec(Node n) {
        // test if the node is marked
        if (n.getMarking() == 1) {
            return;
        }

        n.setMarking(1);
        Set neighbours = n.getNeighbours();
        for (Iterator<Node> it = neighbours.iterator(); it.hasNext();) {
            Node neighbour = it.next();
            isInOneComponentRec(neighbour);
        }
    }

    /**
     * Return variance of the net. Method getNumOfComponents() must be run
     * before running this method (because marking of nodes).
     *
     * @param numOfTheComponents - number of components in the net
     * @return variance
     */
    public double getValueOfBadness(final int numOfTheComponents) {
        int sumOfInhabitants = 0;
        int[] inhabitantsInComponent;
        inhabitantsInComponent = new int[numOfTheComponents];

        // iterate over all nodes and sum inhabitants
        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            Node n = it.next();
            sumOfInhabitants += n.getNumOfInhabitant();
            if (n.getMarking() == 0) {
                System.err.println("Method Net.getValueOfBadness found marking 0 in the net.");
                System.exit(1);
            }

            // sum inhabitants in each component
            for (int i = 0; i < numOfTheComponents; i++) {
                if (n.getMarking() == i + 1) {
                    inhabitantsInComponent[i] += n.getNumOfInhabitant();
                }
            }
        }

        // count average value
        double expectedValue = (1 / (double) numOfTheComponents) * (sumOfInhabitants);

        // count variance
        double varRight = 0;
        for (int i = 0; i < numOfTheComponents; i++) {
            varRight += Math.pow(inhabitantsInComponent[i] - expectedValue, 2);
        }
        double variance = (1 / (double) numOfTheComponents) * varRight;

        return Math.sqrt(variance);

    }

    @Override
    public Net clone() {

        Net clonedNet = new Net();
        clonedNet.setName(name);

        Set<Node> clonedNodes = new HashSet(nodes.size());
        Set<Road> clonedRoads = new HashSet(nodes.size());

        // create new nodes
        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            Node oldNode = it.next();
            Node newNode = new Node(oldNode);
            clonedNodes.add(newNode);
        }
        clonedNet.nodes = clonedNodes;

        // create new roads
        for (Iterator<Road> it = roads.iterator(); it.hasNext();) {
            Road oldRoad = it.next();
            Road newRoad = new Road(oldRoad);
            clonedRoads.add(newRoad);

            // find nodes in the cloned net
            Node newFirstNode = clonedNet.getNode(oldRoad.getFirst_node().getId());
            Node newSecondNode = clonedNet.getNode(oldRoad.getSecond_node().getId());

            // add info about road to node
            newFirstNode.addRoad(newRoad);
            newSecondNode.addRoad(newRoad);

            // store info about first and second node to road
            newRoad.setFirst_node(newFirstNode);
            newRoad.setSecond_node(newSecondNode);
        }

        clonedNet.roads = clonedRoads;

        return clonedNet;
    }

    public boolean distanceBetweenRoadsIsAtLeast(final int distance, Collection<Road> roads) {
        if (distance <= 0 || roads.size() == 1) {
            return true;
        }

        for (Road road1 : roads) {
            for (Road road2 : roads) {
                if (!road1.equals(road2) && getRoadDistance(road1, road2) < distance) {
                    return false;
                }
            }
        }

        return true;
    }

    private int getRoadDistance(Road r1, Road r2) {
        if (r1.equals(r2)) {
            return 0;
        }

        if (r1.getFirst_node().equals(r2.getFirst_node())
                || r1.getSecond_node().equals(r2.getSecond_node())
                || r1.getFirst_node().equals(r2.getSecond_node())
                || r1.getSecond_node().equals(r2.getFirst_node())) {
            return 1;
        } else {
            return 2;
        }

        /*
         Set<Node> tempSet1 = new HashSet<>(r1.getNodes());
         Set<Node> tempSet2 = new HashSet<>(r2.getNodes());
        
         int size1 = tempSet1.size();
         int size2 = tempSet2.size();
        
         tempSet1.addAll(tempSet2);
        
         // pokud silnice nesdilely uzly vzdalenost je 2, jinak 1 // todo dodelat, aby to vracelo realnou vzdalenost
         if (tempSet1.size() == (size1 + size2)) {
         return 2;
         } else {
         return 1;
         }
         */ 
        
    }
}
