package OSPaxos;

import java.util.*;

public abstract class Node implements Proposer, Acceptor, Learner {

   
	protected Set<NodeLocationData> nodeLocationSet; // store the NodeLocation
														// of
	// all the nodes in the group
	protected Map<NodeLocationData, Node> nodeLocationMap;
	protected NodeLocationData locationData; // unique locationData to identify
	// itself
	protected Messenger messenger;
	protected int currentSn; // to keep track of sn used so far

	// state failure variable
	protected ArrayList<Boolean> isRunning;

	public Node(int NodeID) {

		this.nodeLocationSet = new HashSet<NodeLocationData>();
		this.nodeLocationMap = new HashMap<NodeLocationData, Node>();
		this.locationData = new NodeLocationData(NodeID);
		this.messenger = new Messenger(nodeLocationMap);
		this.currentSn = -1;

		// state failure, why it is 1 to 5
		this.isRunning = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			this.isRunning.add(true);
		}
	}

	public boolean isLeader() {
		return locationData.isLeader();
	}

	public void becomeLeader() {
		writeDebug("I'm Leader");
		this.locationData.becomeLeader();
		for (NodeLocationData node : nodeLocationSet) {
			if (node == locationData) {
				continue;
			}
			node.becomeNonLeader();
		}
	}

	public void setNodeList(Set<NodeLocationData> s) {
		this.nodeLocationSet = s;
	}

	public void setMessenger(Map<NodeLocationData, Node> map) {
		this.messenger = new Messenger(map);
	}

	/*
	 * public void setAcceptedProposal(String s) {
	 * this.acceptedProposal.setValue(s); }
	 */

	public NodeLocationData getLocationData() {
		return locationData;
	}

	public ArrayList<Boolean> getIsRunning() {
		return isRunning;
	}

	public void setIsRunning(ArrayList<Boolean> isRunning) {
		this.isRunning = isRunning;
		if (!this.isRunning.get(0)) {
			writeDebug("Acceptor Fail: Fail to Receive the Prepare Request and Send Promise");
		} else if (!this.isRunning.get(1)) {
			writeDebug("Proposor Fail: Fail to Evaluate Acceptor's Promise and Send Accept!");
		} else if (!this.isRunning.get(2)) {
			writeDebug("Acceptor Fail: Fail to Receive the Accept! Request");
		} else if (!this.isRunning.get(3)) {
			writeDebug("Learner Fail: Can not Learn the Result");
		} else if (!this.isRunning.get(4)) {
			writeDebug("Proposor Fail: Can not Response to Client");
		}
	}

	public void reSetIsRunning() {
		for (int i = 0; i < 5; i++) {
			getIsRunning().set(i, true);
		}
	}

	// message dispatcher
	public synchronized void receive(Message m) {
		if (m instanceof PrepareRequestMessage && this.getIsRunning().get(0)) {
			PrepareRequestMessage prepareRequest = (PrepareRequestMessage) m;
			receivePrepareRequest(prepareRequest);
		} else if (m instanceof PromiseMessage && this.getIsRunning().get(1)) {
			PromiseMessage promise = (PromiseMessage) m;
			receivePromise(promise);
		} else if (m instanceof AcceptRequestMessage
				&& this.getIsRunning().get(2)) {
			AcceptRequestMessage acceptRequest = (AcceptRequestMessage) m;
			receiveAcceptRequest(acceptRequest);
		} else if (m instanceof AcceptedMessage && this.getIsRunning().get(3)) {
			AcceptedMessage accepted = (AcceptedMessage) m;
			receiveAccepted(accepted);
		} 
	}
	
	protected void writeDebug(String s) {
		System.out.println(this.locationData + ": " + s);
	}
	
	protected void writeEmp(String s){
		System.err.println(this.locationData + ": " + s);
	}
	
	protected void returnResult(String value, Proposal p) {
		if (value == null) {
			writeEmp("** Value " + p.getValue() + " is returned to client **");
		} else {
			writeEmp("** Value " + p.getValue()
					+ " is written to database **");
		}
	}
}
