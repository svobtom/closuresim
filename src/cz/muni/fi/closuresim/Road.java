package cz.muni.fi.closuresim;

import java.util.HashSet;
import java.util.Set;

/**
 * Class representing road. Every road is two-way.  
 * 
 * @author Tom
 */
public class Road {
    private int id;
    private Node first_node;
    private Node second_node;
    /** Name of the road */
    private String name;
    /** Length of the road in meters */
    private int length;
    /** Duration of the way by the road in seconds */
    private int time;
    /** Atribute represents rideable of the road  */
    private boolean closed = false;

    private int marking;
    
    /**
     * Create new road.
     */
    public Road() {
        
    }
    
    /**
     * Copy road except the information about first and second nodes.
     * @param r Road to copy
     */
    public Road(Road r) {
        this.id = r.id;
        this.name = r.name;
        this.closed = r.closed;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Road other = (Road) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        String fn;
        String sn;
        if (first_node.getName().equals("")) {
            fn = "id" + first_node.getId();
        } else {
            fn = first_node.getName();
        }
        
        if (second_node.getName().equals("")) {
            sn = "id" + second_node.getId();
        } else {
            sn = second_node.getName();
        }
        return this.name;
        //return id +";"+ name + (closed ? " (closed" : "open") + ", " + fn + " - " + sn + ")";
    }

    /**
     * Detect if the road is close. 
     * 
     * @return boolean, true if the road is closed, false otherwise
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Open the road.
     */
    public void open() {
        this.closed = false;
    }
    
    /**
     * Close the road. 
     */
    public void close() {
        this.closed = true;
    }
    
    /**
     * Set rideable of the road.
     * 
     * @param closed this boolean is new status of the road.  
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    /**
     * Get the node connected to road. 
     * 
     * @return first node connected to road
     */
    public Node getFirst_node() {
        return first_node;
    }
    
    /**
     * Set the node that will be connected to the road. 
     * 
     * @param first_node first node
     */
    public void setFirst_node(Node first_node) {
        this.first_node = first_node;
    }

    /**
     * Get the node connected to road. 
     * 
     * @return Node second node connected to road
     */
    public Node getSecond_node() {
        return second_node;
    }

    /**
     * Set the node that will be connected to the road. 
     * 
     * @param second_node second node
     */
    public void setSecond_node(Node second_node) {
        this.second_node = second_node;
    }
      
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Return two connected nodes as a set.
     * @return Set<Node> 
     */
    public Set getNodes() {
        Set nodes = new HashSet<>();
        nodes.add(this.first_node);
        nodes.add(this.second_node);
        return nodes;
    }

    public void setNodes(Node firstNode, Node secondNode) {        
        this.first_node = firstNode;
        // TODO pridat ochranu proti vlozeni dvou stejnych uzlu
        this.second_node = secondNode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    /**
     * Return the opposite node of the road. If given node is the first node the method return second node, 
     * otherwise it return first node. 
     *  
     * @param node given node
     * @return opposite node
     */
    public Node getOppositeNode(final Node node) {
        if (this.first_node.equals(node)) {
            return this.second_node;
        } else {
            return this.first_node;
        }
    }

    public int getMarking() {
        return marking;
    }

    public void setMarking(final int marking) {
        this.marking = marking;
    }
    
    
}

