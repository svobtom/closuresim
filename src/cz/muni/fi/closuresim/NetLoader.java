package cz.muni.fi.closuresim;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Manage loading network from files.
 *
 * @author Tom
 */
public class NetLoader {

    /**
     * New loaded net
     */
    final private Net net;

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
    public Net load(final String fileCity, final String fileRoad) {
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
    public Net load(final String oneFile) {
        convertFromFormatCDV(oneFile);
        return net;
    }

    /**
     * Load nodes from a file.
     *
     * @param fileCity
     */
    private void loadNodes(final String fileCity) {
        try {
            //START loading nodes
            InputStream fis = new FileInputStream(fileCity);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            while ((line = br.readLine()) != null) {

                String[] elements = line.split(";");

                if (elements.length < 2) {
                    ExperimentSetup.LOGGER.log(Level.SEVERE, "loading nodes - line is too short");
                }

                String sid = elements[0];
                String type = elements[1];
                String name;
                String inhab;
                if (elements[1].equals("city")) {
                    name = elements[2];
                    inhab = elements[3];
                } else {
                    name = elements[2];
                    inhab = "0";
                }

                final int id = Integer.parseInt(sid);
                final int inhabitions = Integer.parseInt(inhab);

                Node n = new Node();
                n.setId(id);
                n.setType(type);
                n.setName(name);
                n.setNumOfInhabitants(inhabitions);

                this.net.addNode(n);

            }
        } catch (FileNotFoundException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "File with nodes not found.", ex);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "IO exception occur.", ex);
        }
    }

    /**
     * Load roads from the file.
     *
     * @param fileRoad
     */
    private void loadRoads(final String fileRoad) {
        try {
            InputStream fis = new FileInputStream(fileRoad);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            while ((line = br.readLine()) != null) {

                String[] elements = line.split(";");

                if (elements.length < 3) {
                    ExperimentSetup.LOGGER.log(Level.SEVERE, "loading roads - line is too short");
                }

                String sid = elements[0];
                String sstart = elements[1];
                String send = elements[2];
                String name = elements[3];
                String slength = elements[4];
                String stime = elements[5];

                final int id = Integer.parseInt(sid);
                final int start = Integer.parseInt(sstart);
                final int end = Integer.parseInt(send);
                final int length = Integer.parseInt(slength);
                final int time = Integer.parseInt(stime);

                Road r = new Road();
                r.setId(id);
                r.setName(name);
                r.setLength(length);
                r.setTime(time);

                Node start_node = this.net.getNode(start);
                Node end_node = this.net.getNode(end);

                if (start_node == null || end_node == null) {
                    ExperimentSetup.LOGGER.log(Level.SEVERE, "Node wasn't found while loading roads.");
                } else {

                    r.setNodes(start_node, end_node);

                    if (this.net.containsRoad(name)) {
                        System.out.println("POOOOOOOOOOOOOOOOZOOOOR");

                        Road tempRoad = this.net.getRoad(name);
                        System.out.println("N: " + tempRoad);
                        System.out.println("S: " + r);
                        System.out.println();
                    }

                    start_node.addRoad(r);
                    end_node.addRoad(r);

                    net.addRoad(r);
                }
            }
        } catch (FileNotFoundException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "File with roads not found.", ex);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "IO exception occur.", ex);
        }
    }

    /**
     *
     * @return number of nodes
     */
    public int getNumOfLoadedNodes() {
        return this.net.getNodes().size();
    }

    /**
     *
     * @return number of roads
     */
    public int getNumOfLoadedRoads() {
        return this.net.getRoads().size();
    }

    /**
     * Convert the file in CDV format to two files in csv format and load them.
     *
     * @param oneFile path to the file in CDV format
     */
    private void convertFromFormatCDV(final String oneFile) {
        // create structure where will be load the data from source file
        List<Node> nodes = new LinkedList();
        List<Road> roads = new LinkedList();

        // read source file to get nodes
        readNodes(oneFile, nodes);

        // read source file to get roads
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

    /**
     * Read CDV format file to get nodes.
     *
     * @param fileName name of the file
     * @param nodes
     */
    private void readNodes(final String fileName, List<Node> nodes) {
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
                    node.setNumOfInhabitants(num);
                    nodes.add(node);
                    i++;

                }
            }
        } catch (FileNotFoundException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "File with nodes and rodes not found.", ex);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "IO exception occur.", ex);
        }
    }

    /**
     * Read CDV file to get information about roads.
     *
     * @param fileName
     * @param nodes
     * @param roads
     */
    private void readRoads(final String fileName, final List<Node> nodes, List<Road> roads) {
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
                    //int poc = 0;
                    while (st.hasMoreElements()) {
                        roadsString = (String) st.nextElement();

                        if (roadsString.split(";").length >= 4) {

                            //String string = roadsString[poc];
                            //System.out.println(" (" + roadsString + ") --- ");
                            //poc++;
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
                                    r.setTime((int) Double.parseDouble(oneRoad[4]));
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
        } catch (FileNotFoundException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "File with nodes and rodes not found.", ex);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "IO exception occur.", ex);
        }

        // System.out.println(roads.toString());
    }

    public void creatNodesFile(String fileName, List<Node> nodes) {
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

    public void creatRoadsFile(String fileName, List<Road> roads) {
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

    public void loadCoordinates(String fileName) {
        try {
            InputStream fis = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            while ((line = br.readLine()) != null) {

                String[] elements = line.split(";");

                if (elements.length < 2) {
                    ExperimentSetup.LOGGER.log(Level.SEVERE, "loading coordinates - line is too short");
                }

                String name = elements[0];
                String lat = elements[1];
                String lng = elements[2];

                this.net.getNode(name).setLat(Double.parseDouble(lat));
                this.net.getNode(name).setLng(Double.parseDouble(lng));

            }
        } catch (FileNotFoundException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "File with coordinates not found.", ex);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "IO exception occur.", ex);
        }

    }

    /**
     * Load roads which should be skipped.
     *
     * @param path path to directory with processed roads files
     * @return set of roads which should be skipped
     */
    public Set<Road> loadRoadsToSkip(String path) {

        // load directory
        File directory = new File(path);
        Collection<File> fileList = FileUtils.listFiles(directory, FileFilterUtils.suffixFileFilter(".csv"), null);

        Set<Road> result = new HashSet<>();

        // for each file in directory
        for (File file : fileList) {

            String roadName = file.getName().replace(".csv", "");

            /*
             // Do the road exist in this net?
             if (!this.net.containsRoad(roadName)) {
             ExperimentSetup.LOGGER.log(Level.WARNING, "The net doesn't contain road with name " + roadName + " which should be skiped");
             continue;
             }
             */
            Road newRoad = this.net.getRoad(roadName);

            // Do the road exist in this net?
            if (newRoad == null) {
                ExperimentSetup.LOGGER.log(Level.WARNING, "The net doesn't contain road with name " + roadName + " which should be skiped");
            }
            result.add(newRoad);
        }

        return result;
    }
}
