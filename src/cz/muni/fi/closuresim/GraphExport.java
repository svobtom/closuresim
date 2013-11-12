package cz.muni.fi.closuresim;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Tom
 */
public class GraphExport {

    private String filename = "source-net.graphml";

    public void export(Net net) {

        // create new graph
        Graph graph = new TinkerGraph();

        // mapping new vertex to integer, useful for creating edges
        Map<Integer, Vertex> mapa = new HashMap<>(net.getNodes().size());

        // create vertexes
        for (Iterator<Node> it = net.getNodes().iterator(); it.hasNext();) {
            Node node = it.next();

            Vertex newVertex = graph.addVertex("n" + node.getId());
            newVertex.setProperty("label", node.getName());

            newVertex.setProperty("r", 240);
            newVertex.setProperty("g", 60);
            newVertex.setProperty("b", 240);

            mapa.put(node.getId(), newVertex);

        }

        // create edges
        for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
            Road road = it.next();

            Vertex a = mapa.get(road.getFirst_node().getId());
            Vertex b = mapa.get(road.getSecond_node().getId());

            Edge e = graph.addEdge("e" + road.getId(), a, b, road.getName());

            e.setProperty("weight", road.getId() % 2 == 0 ? 1 : 2);
            e.setProperty("r", 255);
            e.setProperty("g", 0);
            e.setProperty("b", 0);
        }

        try {
            GraphMLWriter.outputGraph(graph, this.filename);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, null, ex);
        }

    }

    public void exportDisconnections(Net net, DisconnectionCollector disconnectionCollector) {

        int number = 1;
        for (Disconnection disconnection : disconnectionCollector.getDisconnections()) {

            // create new graph
            Graph graph = new TinkerGraph();

            // mapping new vertex to integer, useful for creating edges
            Map<Integer, Vertex> mapa = new HashMap<>(net.getNodes().size());

            // close all roads in disconnection
            for (Iterator<Road> it = disconnection.getRoads().iterator(); it.hasNext();) {
                Road road = it.next();
                for (Road r2 : net.getRoads()) {
                    if (road.equals(r2)) {
                        r2.close();
                    }
                }
            }

            // mark vertices in components
            net.getNumOfComponents();

            // create vertices
            for (Iterator<Node> it = net.getNodes().iterator();
                    it.hasNext();) {
                Node node = it.next();

                Vertex newVertex = graph.addVertex("n" + node.getId());
                newVertex.setProperty("label", node.getName());

                colorVertex(newVertex, node.getMarking());

                mapa.put(node.getId(), newVertex);

            }
            // create edges
            for (Iterator<Road> it = net.getRoads().iterator();
                    it.hasNext();) {
                Road road = it.next();

                Vertex a = mapa.get(road.getFirst_node().getId());
                Vertex b = mapa.get(road.getSecond_node().getId());

                Edge e = graph.addEdge("e" + road.getId(), a, b, road.getName());
                
                if (road.isClosed()) {
                    //e.setProperty("weight", 3);
                    e.setProperty("r", 255);
                    e.setProperty("g", 0);
                    e.setProperty("b", 0);
                } else {
                    //e.setProperty("weight", 2);
                }
            }

            try {
                
                GraphMLWriter.outputGraph(graph, "disconnection-" + number++ + ".graphml");
            } catch (IOException ex) {
                ExperimentSetup.LOGGER.log(Level.SEVERE, null, ex);
            }
            // open all roads in disconnection
            for (Iterator<Road> it = disconnection.getRoads().iterator(); it.hasNext();) {
                Road road = it.next();
                for (Road r2 : net.getRoads()) {
                    if (road.equals(r2)) {
                        r2.open();

                    }
                }
            }
        }
    }

    private void colorVertex(Vertex v, final int marking) {
        v.setProperty("r", getColor("r", marking));
        v.setProperty("g", getColor("g", marking));
        v.setProperty("b", getColor("b", marking));
    }

    private int getColor(final String part, final int turn) {
        switch (turn) {
            case 1:
                switch (part) {
                    case "r":
                        return 0;
                    case "g":
                        return 175;
                    case "b":
                        return 100;
                }
            case 2:
                switch (part) {
                    case "r":
                        return 11;
                    case "g":
                        return 97;
                    case "b":
                        return 164;
                }
            case 3:
                switch (part) {
                    case "r":
                        return 255;
                    case "g":
                        return 146;
                    case "b":
                        return 0;
                }
            case 4:
                switch (part) {
                    case "r":
                        return 255;
                    case "g":
                        return 73;
                    case "b":
                        return 0;
                }
            case 5:
                switch (part) {
                    case "r":
                        return 84;
                    case "g":
                        return 14;
                    case "b":
                        return 173;
                }
        }

        throw new IllegalArgumentException("No such rgb part color");
    }
}
