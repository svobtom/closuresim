package cz.muni.fi.closuresim.tools;

import cz.muni.fi.closuresim.Net;
import cz.muni.fi.closuresim.NetLoader;
import cz.muni.fi.closuresim.Node;
import cz.muni.fi.closuresim.Road;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tom
 */
public class NetReducer {

    private Net net;
    private Net newNet;

    public NetReducer(Net net) {
        this.net = net;
        this.newNet = new Net();
    }

    public void reduce(int numberOfNodes) {
        Node startNode = getStartNode();
        chooseNodes(startNode, numberOfNodes);
        chooseRoads();
        storeToFiles();
    }

    private Node getStartNode() {
        Iterator<Node> it = net.getNodes().iterator();
        if (it.hasNext()) {
            Node node = it.next();
            return node;
        }
        return null;
    }

    private void chooseNodes(Node startNode, int numberOfNodes) {
        if (numberOfNodes == 0) {
            return;
        }

        newNet.addNode(startNode);
        Set<Node> sn = startNode.getNeighbours();
        for (Node node : sn) {
            chooseNodes(node, numberOfNodes - 1);
        }
    }

    private void chooseRoads() {
        for (Road road : net.getRoads()) {
            if (newNet.getNodes().contains(road.getFirst_node()) && newNet.getNodes().contains(road.getSecond_node())) {
                newNet.addRoad(road);
            }
        }
    }

    private void storeToFiles() {
        NetLoader nl = new NetLoader();
        List nodeList = new LinkedList(this.newNet.getNodes());
        nl.creatNodesFile("nodes-reduced.csv", nodeList);
        List roadList = new LinkedList(this.newNet.getRoads());
        nl.creatRoadsFile("roads-reduced.csv", roadList);
    }
}
