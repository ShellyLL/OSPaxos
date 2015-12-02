package OSPaxos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Node implements Proposer, Acceptor, Learner {

	// by Hanzi, I changed the variable name nodes to nodeLocationSet in order
	// to
	// keep consistent with Main
	// Node Data
	private Set<NodeLocationData> nodeLocationSet; // store the NodeLocation of
													// all the
	// nodes in the group
	private Map<NodeLocationData, Node> nodeLocationMap;
	private NodeLocationData locationData; // unique locationData to identify
											// itself
	private Messenger messenger;
	private int currentSn; // to keep track of sn used so far

	// Proposer Variables
	private PriorityQueue<Proposal> promises;
	private boolean done;
	private String value; // for write only
	
	// Acceptor Variables
	private Proposal acceptedProposal;

	// Learner Variables
	private Map<Proposal, Integer> learnedProposals;// key:proposal, value:
													// times learned

	// by Hanzi: added String value to deal with write
	public Node(int NodeID) {
		this.nodeLocationSet = new HashSet<NodeLocationData>();
		this.nodeLocationMap = new HashMap<NodeLocationData, Node>();
		this.locationData = new NodeLocationData(NodeID);
		this.messenger = new Messenger(nodeLocationMap);
		this.currentSn = -1;
		
		// proposer
		this.promises = null;
		this.done = false;

		// acceptor
		this.acceptedProposal = new Proposal(-1, null);// -1 refers to no proposal accepted
		
		// learner
		learnedProposals = new HashMap<Proposal, Integer>();

		// by Hanzi: I put the following initialization in sendPrepareRequest()
		// when debugging
		// because when we do not know the nodes.size() until we finish creating
		// all nodes. Otherwise, there will be illegal argument exception
		// this.promises = new PriorityQueue<Proposal>(nodes.size());

	}

	public boolean isLeader() {
		return locationData.isLeader();
	}

	public void becomeLeader() {
		writeDebug("I'm Leader");
		locationData.becomeLeader();

		for (NodeLocationData node : nodeLocationSet) {
			if (node == locationData)
				continue; // added by Hanzi
			node.becomeNonLeader();
		}
	}

	public void setNodeList(Set<NodeLocationData> s) {
		this.nodeLocationSet = s;
	}

	public void setMessenger(Map<NodeLocationData, Node> map) {
		this.messenger = new Messenger(map);
	}

	public void setAcceptedProposal(String s) {
		this.acceptedProposal.setValue(s);
	}

	public NodeLocationData getLocationData() {
		return locationData;
	}

	
	// message dispatcher
	public void receive(Message m) {
		if (m instanceof PrepareRequestMessage) {
			PrepareRequestMessage prepareRequest = (PrepareRequestMessage) m;
			receivePrepareRequest(prepareRequest);
		} else if (m instanceof PromiseMessage) {
			PromiseMessage promise = (PromiseMessage) m;
			receivePromise(promise);
		} else if (m instanceof AcceptRequestMessage) {
			AcceptRequestMessage acceptRequest = (AcceptRequestMessage) m;
			receiveAcceptRequest(acceptRequest);
		} else if (m instanceof AcceptedMessage) {
			AcceptedMessage accepted = (AcceptedMessage) m;
			receiveAccepted(accepted);
		} else if (m instanceof DecisionMessage) {
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
		done = false;
		currentSn++;
		for (NodeLocationData node : nodeLocationSet) {
			writeDebug("Send Prepare Request to " + node + ": (" + currentSn + ")");
			Message prepareRequest = new PrepareRequestMessage(locationData,
					node, currentSn);
			messenger.send(prepareRequest);
		}
	}

	@Override
	public void receivePromise(PromiseMessage m) {
		writeDebug("Received Promise from " + m.getSender() + ": (" + m.getSn() + " ," + m.getPrevProposal() + ")");
		int sn = m.getSn();
		if (sn == currentSn) {
			promises.add(m.getPrevProposal());
		}
		if (promises.size() + 1 > nodeLocationSet.size() / 2) {// plus proposer
																// itself
			Proposal cur;// Proposal for this round
			if (value != null) {
				cur = new Proposal(sn, value);
			} else {
				promises.add(acceptedProposal);
				Proposal pre = promises.poll();// get the previously accepted
												// proposal with the highest sn
				cur = new Proposal(sn, pre.getValue());
			}
			for (NodeLocationData node : nodeLocationSet) {
				writeDebug("Send Accept! Request to " + node + ": " + cur);
				AcceptRequestMessage acceptRequest = new AcceptRequestMessage(
						locationData, node, cur);
				messenger.send(acceptRequest);
			}
		}
	}

	@Override
	public void receiveDecision(DecisionMessage m) {
		if (m.getProposal().getSn() == currentSn && done == false) {
			writeDebug("Value " + m.getProposal().getValue()
					+ " is returned to client");
			done = true;
		}
	}

	// Acceptor methods
	@Override
	public void receivePrepareRequest(PrepareRequestMessage m) {
		writeDebug("Received Prepare Request from " + m.getSender() + ": (" + m.getSn() + ")");
		int sn = m.getSn();
		if (sn > currentSn) {
			currentSn = sn;
			Message promise = new PromiseMessage(locationData, m.getSender(),
					sn, acceptedProposal);
			messenger.send(promise);
		}
	}

	@Override
	public void receiveAcceptRequest(AcceptRequestMessage m) {
		writeDebug("Received Accept Request from " + m.getSender() + ": " + m.getProposal());
		Proposal p = m.getProposal();
		int sn = p.getSn();
		if (sn >= currentSn) {
			this.currentSn = sn;
			this.acceptedProposal = p;
			for (NodeLocationData node : nodeLocationSet) {
				// writeDebug("Send Accepted to" + node);
				AcceptedMessage accepted = new AcceptedMessage(locationData,
						node, acceptedProposal);
				messenger.send(accepted);
			}
		}
	}

	// Learner methods
	@Override
	public void receiveAccepted(AcceptedMessage m) {
		writeDebug("Accepted from " + m.getSender() + ": " + m.getProposal());
		Proposal p = m.getProposal();
		Integer times = learnedProposals.get(p);
		learnedProposals.put(p, times == null ? 1 : times + 1);
		NodeLocationData leader = null;
		for (NodeLocationData node : nodeLocationSet) {
			if (node.isLeader()) {
				leader = node;
				break;
			}
		}
		// send the decided value to leader
		if (learnedProposals.get(p) > nodeLocationSet.size() / 2) {
			//writeDebug("Send the decided value to leader " + leader);
			Message decision = new DecisionMessage(locationData, leader, p);
			messenger.send(decision);
		}
	}

	private void writeDebug(String s) {
		System.out.println(locationData + ": " + s);
	}

}
