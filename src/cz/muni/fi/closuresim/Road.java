package cz.muni.fi.closuresim;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents road. 
 * 
 * @author Tom
 */
public class Road {
    private int id;
    private Node first_node;
    private Node second_node;
    private String name;
    /** Length of the road in meters */
    private int length;
    /** Duration of the road in seconds */
    private int time;
    private boolean closed = false;

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
            fn = "cr" + first_node.getId();
        } else {
            fn = first_node.getName();
        }
        
        if (second_node.getName().equals("")) {
            sn = "cr" + second_node.getId();
        } else {
            sn = second_node.getName();
        }
        
        return id + name + (closed ? " closed" : "") + " (" + fn + " - " + sn + ")";
    }

    public boolean isClosed() {
        return closed;
    }

    public void open() {
        this.closed = false;
    }
    
    public void close() {
        this.closed = true;
    }
    
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    public Node getFirst_node() {
        return first_node;
    }

    public void setFirst_node(Node first_node) {
        this.first_node = first_node;
    }

    public Node getSecond_node() {
        return second_node;
    }

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
        Set nodes = new HashSet<Node>();
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
}

