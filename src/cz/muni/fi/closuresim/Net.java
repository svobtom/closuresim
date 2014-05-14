package cz.muni.fi.closuresim;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of the network. 
 *
 * @author Tom
 */
public class Net {

    private String name;
    private Set<Node> nodes;
    private Set<Road> roads;

    public Net() {
        nodes = new HashSet<>();
        roads = new HashSet<>();
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

    public Set<Node> getNodes() {
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
     * Find road in the net
     *
     * @param roadID
     * @return found road if exists, else null
     */
    public Road getRoad(final int roadID) {
        for (Iterator<Road> it = roads.iterator(); it.hasNext();) {
            Road r = it.next();
            if (r.getId() == roadID) {
                return r;
            }
        }
        return null;
    }

    /**
     * Find road in the net by its name.
     *
     * @param roadName name of the road
     * @return road if exists, else null
     */
    public Road getRoad(final String roadName) {

        for (Road r : this.roads) {
            if (r.getName().equals(roadName)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Get set of roads.
     *
     * @return set of roads
     */
    public Set<Road> getRoads() {
        return roads;
    }

    /**
     * Get set of closed roads.
     *
     * @return set of roads
     */
    public Set<Road> getClosedRoads() {
        Set<Road> result = new HashSet<>();
        for (Road r : this.roads) {
            if (r.isClosed()) {
                result.add(r);
            }
        }
        return result;
    }

    public void setRoads(Set<Road> roads) {
        this.roads = roads;
    }

    /**
     * Open all roads in the net.
     */
    public void openAllRoads() {
        for (Road r : this.roads) {
            r.open();
        }
    }

    /**
     * Count the components. If the net is connected it return 1. Let nodes in
     * the net marked by integers (from 1 to number_of_component).
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
            markTheNode(n);
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

    /**
     * Check if the net is connected. It is faster because it doesn't set
     * marking to zero at beginnig, but during checking cycle.
     *
     * @return true if the net is connected, false if isn't
     */
    public boolean isInOneComponentFaster() {

        // marking of the nodes, choose first node
        Iterator<Node> it = nodes.iterator();
        if (it.hasNext()) {
            Node n = it.next();
            markTheNode(n);
        }

        // check if all nodes are marked and set to zero marking of nodes
        boolean result = true;
        for (Node n : this.nodes) {
            if (n.getMarking() != 0) {
                n.setMarking(0);
            } else {
                result = false;
            }
        }
        return result;
    }

    /**
     * Mark node (by number 1) and run itself on neighbours.
     *
     * @param n node to mark
     */
    private void markTheNode(final Node n) {
        // test if the node is marked
        if (n.getMarking() != 1) {
            n.setMarking(1);

            for (Iterator<Node> it = n.getNeighbours().iterator(); it.hasNext();) {
                markTheNode(it.next());
            }
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

    /**
     * Check if the distance between all two roads in the collection is at least
     * a desired number.
     *
     * @param distance
     * @param roads
     * @return
     */
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

    /**
     * Check road distance. Road distance is a number which says how many nodes
     * is on the way from first roud to the second road.
     *
     * @param r1 first road
     * @param r2 second road
     * @return
     */
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
            return 2; // TODO - zatim to jen pozna, jestli uzly spolu naprimo sousedi nebo ne
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

    /**
     * Remove all roads from net.
     */
    void clearRoads() {

        // clear lists of roads in nodes
        for (Node node : this.nodes) {
            node.clearRoads();
        }

        // remove roads set
        this.roads.clear();
    }
    /*
     public boolean isAllRoadsConnected() {

     for (Road r : roads) {
     r.setMarking(0);
     }

     Road first = roads.iterator().next();
     isAllRoadsConnectedRec(first);

     for (Road r : roads) {
     if (r.getMarking() == 0) {
     return false;
     }
     }
     return true;
        
     }

     private void isAllRoadsConnectedRec(Road first) {
     if (first.getMarking() == 0) {
     first.setMarking(1);
     for (Node node : first.) {
                
     }
     }
        
        
     }
     */

    /**
     * Get node by its name.
     *
     * @param name
     * @return node if found, el
     */
    public Node getNode(String name) {
        for (Node node : this.nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Check if the net contains node with the given name.
     *
     * @param name of the node
     * @return true if the net contains the node, false if not
     */
    public boolean containsNode(String name) {

        for (Node n : this.nodes) {
            if (n.getName().equals(name)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Check if the net contains road with the given name.
     *
     * @param name name of the road
     * @return true if the net contains the road, false if not
     */
    public boolean containsRoad(String name) {

        for (Road r : this.roads) {
            if (r.getName().equals(name)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Count inhabitants in all components of the net. Method
     * getNumOfComponents() must be run before running this method (because
     * marking of nodes).
     *
     * @return list of sums of inhabitants by components, null if nodes weren't
     * marked
     */
    public List<Integer> getInhabitantsByComponents() {

        // check marking
        for (Node node : this.nodes) {
            if (node.getMarking() == 0) {
                ExperimentSetup.LOGGER.warning("A node wasn't marked");
                return null;
            }
        }

        Map<Integer, Integer> inhabitantsByComponent = new HashMap<>();

        // sum nodes in the component
        for (Node node : this.nodes) {
            final int marking = node.getMarking();
            if (inhabitantsByComponent.containsKey(marking)) {
                int recentNumOfInhabitants = inhabitantsByComponent.get(marking);
                inhabitantsByComponent.put(marking, recentNumOfInhabitants + node.getNumOfInhabitant());
            } else {
                inhabitantsByComponent.put(node.getMarking(), node.getNumOfInhabitant());
            }
        }

        // return list
        List<Integer> result = new LinkedList<>();
        result.addAll(inhabitantsByComponent.values());
        return result;
    }

    /**
     * Check how much roads must be repared IN ROW to have connected network.
     * Method getNumOfComponents() must be run before running this method
     * (because marking of nodes).
     *
     * @return number of roads, -1 if nodes weren't marked
     */
    public int getRemotenessComponentsIndex() {

        Net reducedNetwork = getReductedNetwork();

        reducedNetwork.openAllRoads();

        // choose largest component/node (according to number of inhabitants)
        Node largestNode = null;
        for (Node node : reducedNetwork.getNodes()) {
            if (largestNode == null || largestNode.getNumOfInhabitant() < node.getNumOfInhabitant()) {
                largestNode = node;
            }
        }

        // path from other components/nodes to the largest node
        int longestPathLength = 0;
        for (Node otherNode : reducedNetwork.getNodes()) {
            List<Road> path = findShortestPath(largestNode, otherNode);
            if (path.size() > longestPathLength) {
                longestPathLength = path.size();
            }
        }

        return longestPathLength;

    }

    /**
     * Create and return reducted network. It means that every component is
     * represented by one node. Every road in reducted network represent closed
     * road. There doesn't have to be all closed roads from origin network.
     *
     * @return reducted network
     */
    private Net getReductedNetwork() {

        // check marking
        for (Node node : this.nodes) {
            if (node.getMarking() == 0) {
                ExperimentSetup.LOGGER.warning("A node wasn't marked");
                return null;
            }
        }

        // create reducted net
        Net result = new Net();

        int j = 1;
        for (Node node : this.nodes) {
            int marking = node.getMarking();
            if (!result.containsNode("m" + marking)) {
                // node hasn't exist yest
                Node n = new Node();
                n.setId(j++);
                n.setName("m" + marking);
                n.setNumOfInhabitants(node.getNumOfInhabitant());
                result.addNode(n);
            } else {
                // node exist in reducted net, add inhabitants
                Node n = result.getNode("m" + marking);
                n.addInhabitants(node.getNumOfInhabitant());
            }
        }

        int i = 1;
        for (Road closedRoad : getClosedRoads()) {
            int marking1 = closedRoad.getFirst_node().getMarking();
            int marking2 = closedRoad.getSecond_node().getMarking();

            if (!result.containsRoad(marking1 + "-" + marking2) && !result.containsRoad(marking2 + "-" + marking1)) {
                Road r = new Road();
                r.setId(i++);
                r.setName(marking1 + "-" + marking2);
                r.close();

                // add road
                r.setFirst_node(result.getNode("m" + marking1));
                r.setSecond_node(result.getNode("m" + marking2));
                result.getNode("m" + marking1).addRoad(r);
                result.getNode("m" + marking2).addRoad(r);
                result.addRoad(r);
            }

        }

        return result;
    }

    /**
     * Find shortest path between specified nodes avoiding closed roads.
     *
     * @param source source node
     * @param target target node
     * @return List<Road> list of roads on the shortest path, if the path
     * doesn't exist the empty list is returned
     */
    protected List<Road> findShortestPath(final Node source, final Node target) {
        // path to return, empty yet
        final List<Road> listOfRoadsOnThePath = new LinkedList<>();

        // get map of ancestors for reconstruction path from source to target 
        final Map<Node, NodeAndRoad> mapOfAncestors = dijkstra(source, target);

        // road doesn't exist
        if (!mapOfAncestors.containsKey(target)) {
            return listOfRoadsOnThePath;
        }

        // reconstruction path from source to target from map
        Node recent = target;
        NodeAndRoad previous;
        while (!recent.equals(source)) {
            previous = mapOfAncestors.get(recent);

            listOfRoadsOnThePath.add(previous.getRoad());
            recent = previous.getNode();
        }

        return listOfRoadsOnThePath;
    }

    /**
     * Dijkstra algorithm modified to avoid closed roads.
     *
     * @param source start node
     * @param target target node
     * @return map of nodes and its ancestors on the optimal path
     */
    private Map<Node, NodeAndRoad> dijkstra(final Node source, final Node target) {
        final Map<Node, Integer> distance = new HashMap<>();
        final Map<Node, Boolean> visited = new HashMap<>();
        final Map<Node, NodeAndRoad> previousRoad = new HashMap<>(); // previous node and road in the best path

        // init nodes
        for (Node node : getNodes()) {
            distance.put(node, Integer.MAX_VALUE);
            visited.put(node, Boolean.FALSE);
        }

        // start with the source node
        distance.put(source, 0);

        final Set<Node> queue = new HashSet<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            Node u = null; // node with smallest distance, not visited
            int smallestDistance = Integer.MAX_VALUE;

            // find smallest distance
            for (Node node : queue) {
                if (!visited.get(node) && distance.get(node) < smallestDistance) {
                    smallestDistance = distance.get(node);
                    u = node;
                }
            }

            // if we closed target node
            if (target.equals(u)) {
                return previousRoad;
            }

            queue.remove(u);
            visited.put(u, Boolean.TRUE);

            for (final Road r : u.getRoads()) {

                // skip closed road
                if (!r.isClosed()) {

                    final Node v = r.getOppositeNode(u);
                    final int alt = distance.get(u) + 1; // accumulate shortest distance, dist[u] + dist_between(u, v)
                    if (alt < distance.get(v) && !visited.get(v)) {
                        distance.put(v, alt);
                        //previous.put(v, u);
                        previousRoad.put(v, new NodeAndRoad(u, r));
                        queue.add(v);
                    }

                }

            } // end for
        } // end while
        return previousRoad;
    }

    /**
     * Connects the road to the node. More roads can be connected to one node.
     * However at most two nodes can be assign to the road.
     *
     * @param r road to connect
     * @param n node to connect
     * @return true if the connection was successful, false otherwise
     */
    public boolean connectRoadToNode(Road r, Node n) {
        // test existing in the net
        if (!this.nodes.contains(n) || !this.roads.contains(r)) {
            throw new IllegalArgumentException("Node " + n + " or road " + r + " doesn't belong to the net " + this);
        }

        if (r.getFirst_node() == null) {
            n.addRoad(r);
            r.setFirst_node(n);
            return true;
        }
        if (r.getSecond_node() == null) {
            n.addRoad(r);
            r.setSecond_node(n);
            return true;
        }
        // road has been assigned to two nodes yet
        return false;
    }

    /**
     * Check if the set of closed roads make MINIMAL cut-set of the net. 
     * 
     * @return true if the roads make cut-set, false otherwise
     */
    public boolean isClosedRoadsMinimalCutSet() {

        // check marking
        for (Node node : this.nodes) {
            if (node.getMarking() == 0) {
                ExperimentSetup.LOGGER.warning("A node wasn't marked");
                throw new IllegalArgumentException("A node wasn't marked");
            }
        }

        for (Road road : this.roads) {
            if (road.isClosed() && road.getFirst_node().getMarking() == road.getSecond_node().getMarking()) {
                return false;
            }
        }

        return true;
    }

}
