package OXPaxos;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Node{
	
	// Node Data
	private Set<NodeLocationData> nodes;   //store the NodeLocation of all the nodes in the group
	private NodeLocationData locationData;  //unique locationData to identify itself

	// Proposer Variables
	private int NodeID;  //the NodeID
	private Map<Integer, Integer> numAcceptRequests;  // to keep track of number of promises received
	private Map<Integer, Proposal> proposals;
	private int currentCsn;  // to keep track of csn used sofar
	private int highestCsn;  //highest received csn 
	private String value;

	
	// Acceptor Variables
	private Map<Integer, Proposal> maxAcceptedProposals;
	
	// Learner Variables
	private Map<Integer, Integer> numAcceptNotifications;
	private Map<Integer, String> chosenValues;
	
	public Node(int NodeID)
	{
		this.nodes = new HashSet<NodeLocationData>();
		this.locationData = new NodeLocationData(NodeID);
		
		//proposer variables
		this.NodeID = NodeID; 
		this.currentCsn = 0;
		this.highestCsn = -1;  // -1 refers to no proposal has been received
		this.value = null; 
		this.numAcceptRequests = new HashMap<Integer, Integer>();
		this.proposals = new HashMap<Integer, Proposal>();
		//acceptor
		this.maxAcceptedProposals = new HashMap<Integer, Proposal>();
		
		//learner
		this.numAcceptNotifications = new HashMap<Integer, Integer>();
		this.chosenValues = new HashMap<Integer, String>();
	}
	//Node data
	

	public void propose(String value, HashSet<Node> nodeSet)
	{
		propose(value, currentCsn++, nodeSet);  //the leader proposes and broadcast message to all the nodes in the NodeLocation data HashSet
	}
	
	public void propose(String value, int csn, HashSet<Node> nodeSet)
	{
		numAcceptRequests.put(csn, 0);
		Proposal proposal = new Proposal(csn, value);  // DO I need to Change this part and add NodeID?, the m.sender is the information
		proposals.put(csn, proposal);
		broadcast(new PrepareRequestMessage(csn), nodeSet);
	}
	
	
	
	private void broadcast(Message m, HashSet<Node> nodeSet)
	{
		m.setSender(locationData);   
		for(NodeLocationData node : nodes)
		{
			// immediately deliver to self
			m.setReceiver(node);
			deliver(m,nodeSet);
 
		}
	}
	
	
	public String toString()
	{
		return locationData.toString();
	}
	
	private void writeDebug(String s)
	{
		System.out.print(toString());
		System.out.print(": ");
		System.out.println(s);
	}
	
	public boolean isLeader()
	{
		return locationData.isLeader();
	}
	
	
	private void unicast(NodeLocationData node, Message m)
	{
         //From my understanding, this code deals with electing new leader
	}
	
	
	public void becomeLeader()
	{
		writeDebug("I'm Leader");
		locationData.becomeLeader();
		for(NodeLocationData node : nodes)     //potentially need to add if(node == locationData)  continue;
			node.becomeNonLeader();
	}
	
	public void setNodeList(Set<NodeLocationData> s)
	{
		this.nodes = s;
	}
	
	public NodeLocationData getLocationData()
	{
		return locationData;
	}
	
	//NodeLocationData is the receiver of this promise
	private void SendPromiseToLeader(PrepareResponseMessage m){
		NodeLocationData receiver = m.getReciever();
		
		
		
		
	}
	private void deliver(Message m, HashSet<Node> nodeSet)
	{
		if (m instanceof PrepareRequestMessage)   //Acceptor needs to deal with it
		{
			PrepareRequestMessage prepareRequest = (PrepareRequestMessage)m;
			int receivedCsn = prepareRequest.getCsn();
			//update currentCsn if necessary
			if(currentCsn <= receivedCsn)
				currentCsn = receivedCsn + 1;
			
			if (receivedCsn <= highestCsn ){
				return;
				//if the receivedCsn less than the highestCsn, ignore
			} else{
				writeDebug("Got Prepare Request from " + prepareRequest.getSender() + ": (" + receivedCsn + ")");
			
			
			// respond
			PrepareResponseMessage prepareResponse = new PrepareResponseMessage(receivedCsn, highestCsn, value);
			prepareResponse.setSender(locationData);
			prepareResponse.setReceiver(prepareRequest.getSender());
			SendPromiseToLeader(prepareResponse);
			
			//update storage
			highestCsn = receivedCsn;
			}
			
		}
		
		
		
		
		
	}
	

	
	

}
