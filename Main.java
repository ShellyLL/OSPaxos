package OSPaxos;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

   private static Set<Node1> nodes;
   private static Set<NodeLocationData> nodeLocationSet;
   private static Map<NodeLocationData, Node> nodeLocationMap;
   private static Queue<Node1> failedLeaders;
   private static Queue<Node1> failedVoters;
   private static final long failTime = 5;

   public static void main(String[] args) throws IOException {
      //nodes = new HashSet<Node1>();
      failedLeaders = new LinkedList<Node1>();
      failedVoters = new LinkedList<Node1>();
      //nodeLocationSet = new HashSet<NodeLocationData>();
      //nodeLocationMap = new HashMap<NodeLocationData, Node>();
      System.out.println("Type 'help' for a list of commands");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      while (true) {
         try {
            String[] s = in.readLine().split(" ", 2);
            String cmd = s[0];
            String arg = s.length > 1 ? s[1] : null;

            if (cmd.equalsIgnoreCase("help"))
               helpCommands();
            else if (cmd.equalsIgnoreCase("init"))
               createNodes(Integer.parseInt(arg));
            else if (cmd.equalsIgnoreCase("read"))
               read(nodes);
            else if (cmd.equalsIgnoreCase("write"))
               write(arg, nodes);
            else if (cmd.equalsIgnoreCase("leaderFail"))
               leaderFail();
            else if (cmd.equalsIgnoreCase("voterFail"))
               voterFail(-1); // -1 means random voter
            else if (cmd.equalsIgnoreCase("randomFail"))
               randomFail();
            else if (cmd.equalsIgnoreCase("leaderRecover"))
               leaderRecover();
            else if (cmd.equalsIgnoreCase("voterRecover"))
               voterRecover();
            // random pick a failed node. after some time, recover it
            else if (cmd.equalsIgnoreCase("failRecoverDemo"))
               failAndRecoverDemo(); 
            // failure can happen during any state in the voting process
            else if (cmd.equalsIgnoreCase("proposorStateFail"))
               proposorStateFail();
            else if (cmd.equalsIgnoreCase("acceptorStateFail"))
               acceptorStateFail();
            else if (cmd.equalsIgnoreCase("learnerStateFail"))
               learnerStateFail();
            else if (cmd.equalsIgnoreCase("stateRecover"))
               stateRecover();
            else
               writeDebug("Unrecognized command");

         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private static void createNodes(int n) {
      nodes = new HashSet<Node1>();
      nodeLocationSet = new HashSet<NodeLocationData>();
      nodeLocationMap = new HashMap<NodeLocationData, Node>();

      for (int i = 0; i < n; i++) {
         Node1 node = new Node1(i);

         if (i == 0) {// make 0 leader
            node.becomeLeader();
         }

         addToSetAndMap(node);
      }
      resetSetAndMap();
      writeDebug(n + " nodes created");
   }


   private static void read(Set<Node1> nodes) {
      writeDebug("Proposing for reading: ");
      for (Node1 node : nodes) {
         if (node.isLeader()) {
            node.sendPrepareRequest(null);//if read, just pass null for value
            break;
         }
      }
   }

   private static void write(String v, Set<Node1> nodes) {
      writeDebug("Proposing for writing: " + v);
      for (Node1 node : nodes) {
         if (node.isLeader()) {
            node.sendPrepareRequest(v);
            break;
         }
      }
   }

   private static void leaderFail() {
      writeDebug("The leader is failed ");
      int leaderID = 0;
      // remove the leader node
      for (Node1 node : nodes) {
         if (node.isLeader()) {
            leaderID = node.getLocationData().getNodeID();
            failedLeaders.add(node);
            removeFromSetAndMap(node);
            break;
         }
      }
      // reset a leader
      for (NodeLocationData nodeLocation : nodeLocationSet) {
         if (nodeLocation.getNodeID() == leaderID + 1) {
            nodeLocationMap.get(nodeLocation).becomeLeader();
            break;
         }
      }
      resetSetAndMap();
   }

   private static void voterFail(int voterID) {
      writeDebug("A voter is failed ");
      if (voterID == -1) {
         for (Node1 node : nodes) {
            if (!node.isLeader()) {
               failedVoters.add(node);
               removeFromSetAndMap(node);
               break;
            }
         }
      } else {
         for (Node1 node : nodes) {
            if (node.getLocationData().getNodeID() == voterID) {
               failedVoters.add(node);
               removeFromSetAndMap(node);
               break;
            }
         }
      }

      resetSetAndMap();
   }

   private static void randomFail() {
      writeDebug("Random set a node failed ");
      int randomID = randInt(0, nodes.size() - 1);
      writeDebug("Random pick a node " + randomID + " failed");
      if (randomID == 0)
         leaderFail();
      else
         voterFail(randomID);
   }

   private static void leaderRecover() {
      if (failedLeaders.isEmpty()) {
         writeDebug("No leader failed.");
         return;
      }

      Node1 node1 = failedLeaders.poll();
      writeDebug("Leader " + node1.getLocationData().getNodeID()
            + " is Recovered");
      node1.becomeLeader();
      addToSetAndMap(node1);
      while (!failedLeaders.isEmpty()) {
         Node1 node2 = failedLeaders.poll();
         writeDebug("Node " + node2.getLocationData().getNodeID()
               + " is Recovered");
         addToSetAndMap(node2);
      }
      resetSetAndMap();
   }

   private static void voterRecover() {
      if (failedVoters.isEmpty()) {
         writeDebug("No voter failed.");
         return;
      }

      while (!failedVoters.isEmpty()) {
         Node1 node = failedVoters.poll();
         writeDebug("Node " + node.getLocationData().getNodeID()
               + " is Recovered");
         addToSetAndMap(node);
      }
      resetSetAndMap();
   }

   private static void failAndRecoverDemo() {
      randomFail();
      read(nodes);
      write("s", nodes);
      writeDebug("Now wait some time for the node to recover");
      // after some time, recover the nodes
      long timeMillis = System.currentTimeMillis();
      long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
      long recoverTime = timeSeconds + failTime;
      while (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) < recoverTime) {
         // System.out.print("-");
      }
      leaderRecover();
      voterRecover();
      read(nodes);
   }

   private static void proposorStateFail() {
      int randomProposorState = randInt(0, 1);
      for (Node1 n : nodes) {
         if (n.isLeader()) {
            n.isRunning.set(1, false);
         }
      }
   }
   
   private static void acceptorStateFail() {
      int random = randInt(0, 1);
      for (Node1 n : nodes) {
         if (!n.isLeader()) {
            //if (random == 0)
               n.isRunning.set(0, false);
            //else 
            //   n.isRunning.set(2, false);
            break;
         }
      }
   }
   
   private static void learnerStateFail() {
      for (Node1 n : nodes) {
         if (n.isLeader()) {
            n.isRunning.set(3, false);
         }
      }
   }

   private static void stateFail() {
      writeDebug("Random set a node failed ");
      int randomID = randInt(0, nodes.size() - 1);
      writeDebug("Random pick a node " + randomID + " failed");
      Node1 node = null;
      for (Node1 n : nodes) {
         if (n.getLocationData().getNodeID() == randomID) {
            node = n;
            break;
         }
      }

      int randomState = randInt(0, 4);
      writeDebug("Random pick a state " + randomState + " failed");
      ArrayList<Boolean> isRunning = node.getIsRunning();
      for (int i = randomState; i < 5; i++) {
         isRunning.set(i, false);
      }
      node.setIsRunning(isRunning);
   }

   private static void stateRecover() {

   }

   private static void addToSetAndMap(Node1 node) {
      nodes.add(node);
      nodeLocationSet.add(node.getLocationData());
      nodeLocationMap.put(node.getLocationData(), node);
   }

   private static void removeFromSetAndMap(Node1 node) {
      nodes.remove(node);
      nodeLocationSet.remove(node.getLocationData());
      nodeLocationMap.remove(node.getLocationData());
   }

   private static void resetSetAndMap() {
      // give node list to all nodes (statically)
      for (Node1 node : nodes) {
         node.setNodes(nodes);
         node.setNodeList(nodeLocationSet);
         node.setMessenger(nodeLocationMap);
      }
   }

   private static void helpCommands() {
      String m = "";
      m += "List of valid commands:";
      m += "\n\thelp - displays this list";
      m += "\n\tinit <num> - creates <num> nodes";
      m += "\n\tread - the current leader accept the client's read request and expect to return to the client";
      m += "\n\twrite <value> - the current leader accept the client's write request and expect to return to the client";
      m += "\n\tleaderFail - tests leaderFail";
      m += "\n\tvoterFail - tests voterFail";
      m += "\n\trandomFail - Random set a node fail";
      m += "\n\tleaderRecover - Recover leader node";
      m += "\n\tvoterRecover - Recover leader node";
      m += "\n\tfailRecoverDemo - automatically demo node fail and recover";

      writeDebug("\n" + m + "\n");
   }

   // this method is used to generate random number
   public static int randInt(int min, int max) {
      Random rand = new Random();
      int randomNum = rand.nextInt((max - min) + 1) + min;
      return randomNum;
   }

   private static void writeDebug(String s) {
      System.out.print("*** ");
      System.out.print(s);
      System.out.println(" ***");
   }

   /*
    * private static void write (String s, HashSet<Node> nodes) {
    * writeDebug("Proposing for writing: " + s); for(Node node : nodes)
    * if(node.isLeader()) { node.propose(s, nodes); break; } }
    * 
    * private static String read (HashSet<Node> nodes) {
    * writeDebug("Proposing for reading: "); for(Node node : nodes)
    * if(node.isLeader()) { node.propose(null, nodes); break; } return ""; }
    */
}
