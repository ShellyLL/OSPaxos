package OSPaxos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Node2 implements Proposer, Acceptor, Learner2 {

	// by Hanzi, I changed the variable name nodes to nodeLocationSet in order
	// to
	// keep consistent with Main
	// Node Data
	private Set<NodeLocationData> nodeLocationSet; // store the NodeLocation of
	// all the
	// nodes in the group
	private Map<NodeLocationData, Node2> nodeLocationMap;
	private NodeLocationData locationData; // unique locationData to identify
	// itself
	private Messenger messenger;
	private int currentSn; // to keep track of sn used so far

	// Proposer Variables
	private PriorityQueue<Proposal> promises; // sorted by sn in descending order
	private int phase; // indicate which phase of current round [0,1,2]
	private String value; // for write only
	private int acceptedNum;

	// Acceptor Variables
	private Proposal acceptedProposal;

	// Learner Variables
	//private Map<Proposal, Integer> learnedProposals;// key:proposal, value:
	// times learned

	// state failure variable
	private ArrayList<Boolean> isRunning;

	public Node2(int NodeID) {
		this.nodeLocationSet = new HashSet<NodeLocationData>();
		this.nodeLocationMap = new HashMap<NodeLocationData, Node2>();
		this.locationData = new NodeLocationData(NodeID);
		this.messenger = new Messenger(nodeLocationMap);
		this.currentSn = -1;

		// proposer
		this.promises = null;

		// acceptor
		this.acceptedProposal = new Proposal(-1, null);// -1 refers to no
														// proposal accepted

		// learner
		//learnedProposals = new HashMap<Proposal, Integer>();

		// state failure
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

	public void setMessenger(Map<NodeLocationData, Node2> map) {
		this.messenger = new Messenger(map);
	}

	public void setAcceptedProposal(String s) {
		this.acceptedProposal.setValue(s);
	}

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
	public void receive(Message m) {
		if (m instanceof PrepareRequestMessage && this.getIsRunning().get(0)) {
			PrepareRequestMessage prepareRequest = (PrepareRequestMessage) m;
			receivePrepareRequest(prepareRequest);
		} else if (m instanceof PromiseMessage && this.getIsRunning().get(1)) {
			PromiseMessage promise = (PromiseMessage) m;
			receivePromise(promise);
		} else if (m instanceof AcceptRequestMessage && this.getIsRunning().get(2)) {
			AcceptRequestMessage acceptRequest = (AcceptRequestMessage) m;
			receiveAcceptRequest(acceptRequest);
		} else if (m instanceof AcceptedMessage && this.getIsRunning().get(3)) {
			AcceptedMessage accepted = (AcceptedMessage) m;
			receiveAccepted(accepted);
		} else if (m instanceof DecisionMessage && this.getIsRunning().get(4)) {
			DecisionMessage decision = (DecisionMessage) m;
			receiveDecision(decision);
		}
	}

	// Proposer methods
	@Override
	public void sendPrepareRequest(String v) {
		// The following two lines are changed by Hanzi when debugging
		this.promises = new PriorityQueue<Proposal>(nodeLocationSet.size());
		this.value = v;
		this.phase = 0;
		this.acceptedNum = 0;
		this.currentSn++;
		for (NodeLocationData node : nodeLocationSet) {
			if (node == this.locationData)
				continue;
			writeDebug("Send Prepare Request to " + node + ": (" + currentSn + ")");
			Message prepareRequest = new PrepareRequestMessage(locationData,
					node, currentSn);
			this.messenger.send(prepareRequest);
		}
	}

	@Override
	public void receivePromise(PromiseMessage m) {
		writeDebug("Received Promise from " + m.getSender() + ": (" + m.getSn()
				+ ", " + m.getPrevProposal() + ")");
		int sn = m.getSn();
		if (sn == this.currentSn) {
			this.promises.add(m.getPrevProposal());
		}
		if (this.phase == 0 && promises.size() + 1 > nodeLocationSet.size() / 2) {// plus itself
			this.promises.add(this.acceptedProposal);
			this.phase = 1;// prepare phase completed
			Proposal cur;// Proposal for this round
			if (this.value != null) {
				cur = new Proposal(sn, this.value);
			} else {
				Proposal pre = promises.poll();// get the previously accepted
				// proposal with the highest sn
				cur = new Proposal(sn, pre.getValue());
			}
			for (NodeLocationData node : nodeLocationSet) {
				//writeDebug("Send Accept! Request to " + node + ": " + cur);
				AcceptRequestMessage acceptRequest = new AcceptRequestMessage(
						locationData, node, cur);
				if (node == this.locationData){
					this.receiveAcceptRequest(acceptRequest);
				} else {
					this.messenger.send(acceptRequest);
				}
			}
		}
	}
	
	public void receiveAccepted(AcceptedMessage m) {
		Proposal p = m.getProposal();
		writeDebug("Accepted from " + m.getSender() + ": " + p);
		if (p.getSn() == this.currentSn){
			this.acceptedNum++;
			if (this.acceptedNum == this.nodeLocationSet.size() / 2 + 1){
				if (this.value == null){
					writeDebug("** Value " + m.getProposal().getValue()
						+ " is returned to client **");
				} else {
					writeDebug("** Value " + m.getProposal().getValue() + " is written to database **");
				}
				for (NodeLocationData node : this.nodeLocationSet){
					if (node == locationData) continue;
					DecisionMessage decision = new DecisionMessage(locationData, node, p);
					this.messenger.send(decision);
				}
			}
		}
	}
	
	// Acceptor methods
	@Override
	public void receivePrepareRequest(PrepareRequestMessage m) {
		writeDebug("Received Prepare Request from " + m.getSender() + ": ("
				+ m.getSn() + ")");
		int sn = m.getSn();
		if (sn > this.currentSn) {
			this.currentSn = sn;
			Message promise = new PromiseMessage(locationData, m.getSender(),
					sn, acceptedProposal);
			this.messenger.send(promise);
		}
	}

	@Override
	public void receiveAcceptRequest(AcceptRequestMessage m) {
		writeDebug("Received Accept Request from " + m.getSender() + ": " + m.getProposal());
		Proposal p = m.getProposal();
		if (p.getSn() >= this.currentSn) {
			this.currentSn = p.getSn();
			this.acceptedProposal = p;
			// send accepted only to distinguished learner (leader)
			AcceptedMessage accepted = new AcceptedMessage(locationData, m.getSender(), acceptedProposal);
			if (m.getSender() == this.locationData){
				this.receiveAccepted(accepted);
			} else {
				this.messenger.send(accepted);
			}
		}
	}

	// Learner methods
	@Override
	public void receiveDecision(DecisionMessage m) {
		Proposal p = m.getProposal();
		writeDebug("Received Decision from " + m.getSender() + ": " + p);
		if (p.getSn() >= this.currentSn){
			this.currentSn = p.getSn();
			this.acceptedProposal = p;
		}
	}

	private void writeDebug(String s) {
		System.out.println(locationData + ": " + s);
	}

}
