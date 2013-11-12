package cz.muni.fi.closuresim;

/**
 *
 * @author Tom
 */
public class NodeAndRoad {

    private Node node;
    private Road road;

    public NodeAndRoad(Node node, Road road) {
        this.node = node;
        this.road = road;
    }

    public NodeAndRoad() {
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }
    
    
    
}
