package cz.muni.fi.closuresim;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

/**
 * Manage loading network from file/files.
 *
 * @author Tom
 */
public class NetLoader {

    final private Net net;

    public NetLoader() {
        net = new Net();
    }

    /**
     * Load the network using format of two files.
     *
     * @param fileCity path to the file with cities
     * @param fileRoad path to the file with roads
     * @return loaded network
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
     * @return loaded network
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
     * Get number of loaded nodes.
     *
     * @return number of nodes
     */
    public int getNumOfLoadedNodes() {
        return this.net.getNodes().size();
    }

    /**
     * Get number of loaded roads.
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
        // create structures for loaded data
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

            // first road id
            int newRoadID = 1;

            // get lines from file
            String line;
            while ((line = br.readLine()) != null) {
                String[] elements = line.split("  ", 2);

                // check if the line is not empty
                if (elements.length >= 2) {

                    String[] node_elements = elements[0].split(";");
                    String newNodeName = node_elements[0];
                    Node newNode = new Node();

                    // searching new node in recently loaded nodes
                    for (Node oldNode : nodes) {
                        if (oldNode.getName().equals(newNodeName)) {
                            newNode = oldNode;
                        }
                    }

                    String roadsString;
                    StringTokenizer st = new StringTokenizer(elements[1], " ");
                    while (st.hasMoreElements()) {
                        roadsString = (String) st.nextElement();

                        // test if there is enought semicolons
                        if (roadsString.split(";").length < 4) {
                            // if not append rest of the string
                            roadsString += "  " + (String) st.nextElement();
                        }

                        String[] oneRoad = roadsString.split(";");

                        for (Node n : nodes) {

                            if (n.getName().equals(oneRoad[0])) {
                                Road newRoad = new Road();

                                newRoad.setName(oneRoad[2].replace("\t", " "));
                                newRoad.setFirst_node(newNode);
                                newRoad.setSecond_node(n);
                                if (oneRoad.length > 3) {
                                    newRoad.setLength(Integer.parseInt(oneRoad[3]));
                                }
                                if (oneRoad.length > 4) {
                                    newRoad.setTime((int) Double.parseDouble(oneRoad[4]));
                                }

                                // test if the road has been loaded yet
                                boolean isThereYet = false;
                                for (Road oldRoad : roads) {
                                    if ((oldRoad.getFirst_node() == newRoad.getSecond_node() && oldRoad.getSecond_node() == newRoad.getFirst_node())) {
                                        isThereYet = isThereYet || true;
                                    }
                                }

                                if (!isThereYet) {
                                    newRoad.setId(newRoadID++);
                                    roads.add(newRoad);
                                }

                            }
                        }

                    }

                }
            }
        } catch (FileNotFoundException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "File with nodes and rodes not found.", ex);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "IO exception occur.", ex);
        }

    }

    /**
     * Create file in csv format from given list of nodes.
     *
     * @param fileName name of new file
     * @param nodes list of nodes
     */
    public void creatNodesFile(String fileName, List<Node> nodes) {
        try {
            FileWriter writer = new FileWriter(fileName);

            for (Node n : nodes) {

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
            ExperimentSetup.LOGGER.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Create file in csv format from given list of roads.
     *
     * @param fileName name of new file
     * @param roads list of roads
     */
    public void creatRoadsFile(String fileName, List<Road> roads) {
        try {
            FileWriter writer = new FileWriter(fileName);

            for (Road r : roads) {

                writer.append(Integer.toString(r.getId()));
                writer.append(";");
                writer.append(Integer.toString(r.getFirst_node().getId()));
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

    /**
     * Load geographical coordinates from special file.
     *
     * @param fileName name fo the file
     */
    public void loadCoordinates(String fileName) {
        try {
            InputStream fis = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            while ((line = br.readLine()) != null) {

                String[] elements = line.split(";");

                if (elements.length < 2) {
                    ExperimentSetup.LOGGER.log(Level.WARNING, "Loading coordinates - line is too short");
                }

                String name = elements[0];
                String lat = elements[1];
                String lng = elements[2];

                Node node = this.net.getNode(name);
                if (node != null) {
                    this.net.getNode(name).setLat(Double.parseDouble(lat));
                    this.net.getNode(name).setLng(Double.parseDouble(lng));
                } else {
                    ExperimentSetup.LOGGER.log(Level.WARNING, "Loading coordinates - non-existent node (" + name + ") " + line);
                }

            }
        } catch (FileNotFoundException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "File with coordinates not found.", ex);
        } catch (IOException ex) {
            ExperimentSetup.LOGGER.log(Level.SEVERE, "IO exception occur.", ex);
        }

    }

    /**
     * Load roads which should be skipped during algorithm.
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
