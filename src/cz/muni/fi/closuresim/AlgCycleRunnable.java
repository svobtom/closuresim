package cz.muni.fi.closuresim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Tom
 */
public class AlgCycleRunnable implements Runnable {

    private Net net;
    private DisconnectionCollector disconnectionCollector;
    private Set<Road> rRoads;

    public AlgCycleRunnable(Net net, DisconnectionCollector disconnectionCollector) {
        this.net = net.clone();
        this.disconnectionCollector = disconnectionCollector;
        rRoads = new HashSet<>();
    }

    @Override
    public void run() {

        // 1. Zvolim hranu e
        findShortestCycle(net.getRoads().iterator().next());

        /*
         //for (Iterator<Road> it = net.getRoads().iterator(); it.hasNext();) {
         //Road road = it.next();
         Iterator<Node> ni = net.getNodes().iterator();
         //search(ni.next(), ni.next(), new HashSet<Road>());
         //findShortestCycle(net.getRoads().iterator().next());
         Node temp = ni.next();
         getPath(net, temp, ni.next());
         getPath(net, ni.next(), ni.next());
         getPath(net, ni.next(), ni.next());
         getPath(net, ni.next(), ni.next());
         getPath(net, ni.next(), temp);
         getPath(net, ni.next(), ni.next());
         // }
         * */

    }

    private void findShortestCycle(Road road) {

        rRoads.add(road);
        for (Road hrana : rRoads) {
            List<Road> lr = getPath(hrana.getFirst_node(), hrana.getSecond_node());

            if (lr.isEmpty()) {
                Disconnection dis = new Disconnection(rRoads);
                disconnectionCollector.addDisconnection(dis);
            }

            //rRoads.addAll(lr.iterator().next());

        }

    }
    /*
     private List<Road> findPath(Node first, Node second, List<Road> currentPath) {
     System.out.println("Path: " + currentPath); //"First: " + first + "; Second: " + second + ",
     if (first.equals(second)) {
     return currentPath;
     }

     Set<Road> useableRoads;
     useableRoads = first.getRoads();
     useableRoads.removeAll(bannedRoads);

     List<Road> path = new LinkedList<>();

     for (Road road : useableRoads) {

     currentPath.add(road);
     bannedRoads.add(road);
     Node firstOpposite = road.getOppositeNode(first);

     return findPath(firstOpposite, second, currentPath);
     }

     return null;


     }

     public void search(Node n1, Node n2, Set<Road> path) {

     if (n1.equals(n2)) {
     System.out.println("We are there. ");
     return;
     }

     System.out.println(n1.getName() + " - " + n2.getName() + ", ");
     for (Road road : path) {
     System.out.println(road.getName() + " ");
     }
     System.out.println("");

     for (Iterator<Road> it = n1.getRoads().iterator(); it.hasNext();) {
     Road road = it.next();

     if (path.contains(road)) {
     continue;
     }
     path.add(road);
     Node opposite = road.getOppositeNode(n1);

     search(opposite, n2, path);

     }

     }
     */

    private Map<Node, NodeAndRoad> dijkstra(Node source, Node target) {
        Map<Node, Integer> distance = new HashMap<>();
        Map<Node, Boolean> visited = new HashMap<>();
        //Map<Node, Node> previous = new HashMap<>(); // previous node in the best path
        Map<Node, NodeAndRoad> previousRoad = new HashMap<>(); // previous node and road in the best path

        // init nodes
        for (Node node : net.getNodes()) {
            distance.put(node, Integer.MAX_VALUE);
            visited.put(node, Boolean.FALSE);
        }

        // start with the source node
        distance.put(source, 0);

        Set<Node> queue = new HashSet<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            Node u = null; // node with smallest distance, not visited
            int smallestDistance = Integer.MAX_VALUE;

            // find smallest distance
            for (Iterator<Node> it = queue.iterator(); it.hasNext();) {
                Node node = it.next();

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

            for (Road r : u.getRoads()) {

                if (r.isClosed() || rRoads.contains(r)) {
                    continue;
                }

                Node v = r.getOppositeNode(u);
                final int alt = distance.get(u) + 1; // accumulate shortest distance, dist[u] + dist_between(u, v)
                if (alt < distance.get(v) && !visited.get(v)) {
                    distance.put(v, alt);
                    //previous.put(v, u);
                    previousRoad.put(v, new NodeAndRoad(u, r));
                    queue.add(v);
                }
            } // end for
        } // end while
        return previousRoad;
    }

    private List<Road> getPath(Node source, Node target) {

        System.out.println(source + " => " + target);
        Map<Node, NodeAndRoad> map = dijkstra(source, target);

        /*
         System.out.println(map.size());
         for (Map.Entry<Node, NodeAndRoad> entry : map.entrySet()) {
         Node node = entry.getKey();
         NodeAndRoad nodeAndRoad = entry.getValue();

         System.out.print(nodeAndRoad.getNode() + "->" + node + "; ");
         }
         System.out.println();
         System.out.println();
         */

        List<Road> lr = new LinkedList<>();

        Node recent = target;

        while (!recent.equals(source)) {
            NodeAndRoad pre = map.get(recent);
            lr.add(pre.getRoad());
            recent = pre.getNode();
        }

        /*
         for (Iterator<Road> it = lr.iterator(); it.hasNext();) {
         Road road = it.next();
         System.out.print(road + " ");
         }
         System.out.println();
         */

        return lr;
    }
}
