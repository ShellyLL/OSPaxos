package OSPaxos;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class NodeTest
{

   @Test
   public void test()
   {
      HashSet<Node> nodes = new HashSet<Node>(); 
      Set<NodeLocationData> nodeLocationSet = new HashSet<NodeLocationData>(); 
      Map<NodeLocationData, Node> nodeLocationMap = new HashMap<NodeLocationData, Node>();
      
      for(int i = 0; i < 5; i++)
      {
         Node node = new Node1(i);
         
         if(i == 0) {// make 0 leader
            node.becomeLeader();
         }
         
         nodes.add(node);
         nodeLocationSet.add(node.getLocationData());
         nodeLocationMap.put(node.getLocationData(), node);
      }

      // give node list to all nodes (statically)
      for(Node node : nodes) {
         node.setNodeList(nodeLocationSet);
         node.setMessenger(nodeLocationMap);
      }
      
      
      int leaderID = 0;
      // remove the leader node
      for(Node node : nodes) {
         if(node.isLeader()) {
            leaderID = node.getLocationData().getNodeID();
            nodes.remove(node);
            nodeLocationSet.remove(node.getLocationData());
            nodeLocationMap.remove(node.getLocationData());            
            break;
         }
      }   
      // reset a leader
      for(NodeLocationData nodeLocation: nodeLocationSet) {
         if (nodeLocation.getNodeID() == leaderID + 1) {
            nodeLocationMap.get(nodeLocation).becomeLeader();
            break;
         }
      }
      
      // give node list to all nodes (statically)
      for(Node node : nodes) {
         node.setNodeList(nodeLocationSet);
         node.setMessenger(nodeLocationMap);
      } 
      
      System.out.println(System.currentTimeMillis());
      long timeMillis = System.currentTimeMillis();
      long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
      System.out.println(timeMillis);
      System.out.println(timeSeconds);
   }

}
