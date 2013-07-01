package cz.muni.fi.closuresim;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage loading network from files.
 *
 * @author Tom
 */
public class NetLoader {
    /** New loaded net */
    private Net net;

    public NetLoader() {
        net = new Net();
    }

    /**
     * Load the network using format of two files.
     *
     * @param fileCity path to the file with cities
     * @param fileRoad path to the file with roads
     * @return Net - loaded network
     */
    public Net load(String fileCity, String fileRoad) {
        loadNodes(fileCity);
        loadRoads(fileRoad);
        return net;
    }

    /**
     * Load the network using the one file format.
     *
     * @param oneFile path to the file in format of CDV
     * @return Net - loaded network
     */
    public Net load(String oneFile) {
        convertFromFormatCDV(oneFile);
        return net;
    }

    private void loadNodes(String fileCity) {
        try {
            //START loading nodes
            InputStream fis = new FileInputStream(fileCity);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            while ((line = br.readLine()) != null) {

                String[] elements = line.split(";");

                if (elements.length < 2) {
                    throw new RuntimeException("line too short");
                }

                String sid = elements[0];
                String type = elements[1];
                String name;
                String inhab;
                if (elements[1].equals("city")) {
                    name = elements[2];
                    inhab = elements[3];
                } else {
                    name = "";
                    inhab = "0";
                }

                int id = Integer.parseInt(sid);
                int inhabitions = Integer.parseInt(inhab);

                Node n = new Node();
                n.setId(id);
                n.setType(type);
                n.setName(name);
                n.setNumOfInhabitants(inhabitions);

                this.net.addNode(n);

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRoads(String fileRoad) {
        try {
            InputStream fis = new FileInputStream(fileRoad);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            while ((line = br.readLine()) != null) {

                String[] elements = line.split(";");

                if (elements.length < 3) {
                    throw new RuntimeException("line too short");
                }

                String sid = elements[0];
                String sstart = elements[1];
                String send = elements[2];
                String name = elements[3];
                String slength = elements[4];
                String stime = elements[5];
                
                int id = Integer.parseInt(sid);
                int start = Integer.parseInt(sstart);
                int end = Integer.parseInt(send);
                int length = Integer.parseInt(slength);
                int time = Integer.parseInt(stime);

                Road r = new Road();
                r.setId(id);
                r.setName(name);
                r.setLength(length);
                r.setTime(time);

                Node start_node = this.net.getNode(start);
                Node end_node = this.net.getNode(end);

                r.setNodes(start_node, end_node);
                start_node.addRoad(r);
                end_node.addRoad(r);

                net.addRoad(r);
                //System.out.println(r.toString());

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNumOfLoadedNodes() {
        return this.net.getNodes().size();
    }

    public int getNumOfLoadedRoads() {
        return this.net.getRoads().size();
    }

    /**
     * Convert the file in CDV format to two files in csv format and load them.
     *
     * @param oneFile path to the file in CDV format
     */ 
    private void convertFromFormatCDV(String oneFile) {
        // create structure where will be load the data from source file
        List<Node> nodes = new LinkedList();
        List<Road> roads = new LinkedList();

        // read source file for nodes
        readNodes(oneFile, nodes);

        // read source file for roads
        readRoads(oneFile, nodes, roads);

        // create csv file with nodes
        final String nodesFile = "nodes-converted.csv";
        creatNodesFile(nodesFile, nodes);

        // create csv file with roads
        final String roadsFile = "roads-converted.csv";
        creatRoadsFile(roadsFile, roads);

        // load the new csv files
        load(nodesFile, roadsFile);
    }

    private void readNodes(String fileName, List<Node> nodes) {
        try {
            InputStream fis = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            int i = 1;

            // get lines from source file
            while ((line = br.readLine()) != null) {
                String[] elements = line.split("  ", 2);

                // check if the line is not empty
                if (elements.length >= 2) {

                    // get name of the node and num of inhabitions
                    String[] line_elements = elements[0].split(";");
                    //System.out.println(" (" + line_elements[0] + ", " + line_elements[1] + ") ");
                    //System.out.print(i + ": " + elements[0]);
                    Node node = new Node();
                    node.setId(i);
                    node.setName(line_elements[0]);
                    int num = Integer.parseInt(line_elements[1]);
                    node.setNumOfInhabitants(num); //setNumInhabitions(num);
                    nodes.add(node);
                    i++;

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        //System.out.println(nodes.toString());
    }

    private void readRoads(String fileName, List<Node> nodes, List<Road> roads) {
        try {
            InputStream fis = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            int newRoadID = 1;
            // get lines from file
            while ((line = br.readLine()) != null) {
                String[] elements = line.split("  ", 2);

                // check if the line is not empty
                if (elements.length >= 2) {

                    String[] node_elements = elements[0].split(";");
                    String startNodeName = node_elements[0];
                    Node startNode = new Node();
                    for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
                        Node n = it.next();
                        if (n.getName().equals(startNodeName)) {
                            startNode = n;
                        }
                    }

                    String roadsString;
                    StringTokenizer st = new StringTokenizer(elements[1], " ");
                    int poc = 0;
                    while (st.hasMoreElements()) {
                        roadsString = (String) st.nextElement();

                        if (roadsString.split(";").length >= 4) {

                            //String string = roadsString[poc];
                            //System.out.println(" (" + roadsString + ") --- ");
                            poc++;
                        } else {
                            roadsString += " " + (String) st.nextElement();
                            //System.out.println(" (" + roadsString + ") === ");
                        }


                        // for (int j = 0; j < roads_elements.length; j++) {
                        //String string = roads_elements[j];
                        //System.out.println(" (" + string + ") ");

                        //String[] oneRoad = string.split(";");
                        String[] oneRoad = roadsString.split(";");

                        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
                            Node n = it.next();
                            if (n.getName().equals(oneRoad[0])) {
                                Road r = new Road();

                                r.setName(oneRoad[2].replace("\t", " "));
                                r.setFirst_node(startNode); //r.setFirstNode(startNodeID);
                                r.setSecond_node(n);//r.setSecondNode(n.getId());
                                if (oneRoad.length > 3) {
                                    r.setLength(Integer.parseInt(oneRoad[3]));//r.setDistance(Integer.parseInt(oneRoad[3]));
                                }
                                if (oneRoad.length > 4) {
                                    r.setTime(Integer.parseInt(oneRoad[4]));
                                }

                                // test if the road has been loaded yet
                                boolean isThereYet = false;
                                for (Iterator<Road> it2 = roads.iterator(); it2.hasNext();) {
                                    Road r2 = it2.next();
                                    if ((r2.getFirst_node() == r.getSecond_node() && r2.getSecond_node() == r.getFirst_node())) {
                                        isThereYet = isThereYet || true;
                                    }
                                }

                                if (!isThereYet) {
                                    r.setId(newRoadID++);
                                    roads.add(r);
                                }

                            }
                        }

                        //}

                    }


                    //String[] roads_elements = elements[1].split(" ");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        // System.out.println(roads.toString());
    }

    private void creatNodesFile(String fileName, List<Node> nodes) {
        try {
            FileWriter writer = new FileWriter(fileName);

            for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
                Node n = it.next();

                writer.append(Integer.toString(n.getId()));
                writer.append(";");

                if (n.getNumOfInhabitant() == 0) {
                    writer.append("node");
                } else {
                    writer.append("city");
                }
                writer.append(";");

                writer.append(n.getName());
                writer.append(";");

                writer.append(Integer.toString(n.getNumOfInhabitant()));
                writer.append("\n");
            }

            writer.flush();
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void creatRoadsFile(String fileName, List<Road> roads) {
        try {
            FileWriter writer = new FileWriter(fileName);

            for (Iterator<Road> it = roads.iterator(); it.hasNext();) {
                Road r = it.next();

                writer.append(Integer.toString(r.getId()));
                writer.append(";");
                writer.append(Integer.toString(r.getFirst_node().getId())); //writer.append(Integer.toString(r.getFirstNode()));
                writer.append(";");
                writer.append(Integer.toString(r.getSecond_node().getId()));
                writer.append(";");
                writer.append(r.getName());
                writer.append(";");
                writer.append(Integer.toString(r.getLength()));
                writer.append(";");
                writer.append(Integer.toString(r.getTime()));
                writer.append("\n");
            }

            writer.flush();
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
