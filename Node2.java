package OSPaxos;

import java.util.*;

public class Node2 extends Node {
	// Proposer Variables
	private PriorityQueue<Proposal> promises; // sorted by sn in descending order
	private String value; // for write only
	private int acceptedNum = 0;// for distinguished learner

	// Acceptor Variables
	private Proposal acceptedProposal;

	public Node2(int NodeID) {
		super(NodeID);
		// proposer
		this.promises = null;
		// acceptor
		this.acceptedProposal = new Proposal(-1, null);// -1 refers to no
																// proposal accepted
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
		} else if (m instanceof DecisionMessage && this.getIsRunning().get(4)) {
			DecisionMessage decision = (DecisionMessage) m;
			receiveDecision(decision);
		}
	}

	// Proposer methods
	@Override
	public void sendPrepareRequest(String v, long startTime) {
		// The following two lines are changed by Hanzi when debugging
		this.promises = new PriorityQueue<Proposal>(nodeLocationSet.size());
		this.value = v;
		this.startTime = startTime;
		this.acceptedNum = 0;
		this.currentSn++;
		for (NodeLocationData node : nodeLocationSet) {
			if (node == this.locationData)
				continue;
			writeDebug("Send Prepare Request to " + node + ": (" + currentSn
					+ ")");
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
		if (this.promises.size() == nodeLocationSet.size() / 2) {// plus itself
			this.promises.add(this.acceptedProposal);
			//writeDebug(promises.toString());
			Proposal cur;// Proposal for this round
			if (this.value != null) {
				cur = new Proposal(sn, this.value);
			} else {
				Proposal pre = promises.poll();// get the previously accepted
				// proposal with the highest sn
				cur = new Proposal(sn, pre.getValue());
			}
			for (NodeLocationData node : nodeLocationSet) {
				// writeDebug("Send Accept! Request to " + node + ": " + cur);
				AcceptRequestMessage acceptRequest = new AcceptRequestMessage(
						locationData, node, cur);
				if (node == this.locationData) {
					this.receiveAcceptRequest(acceptRequest);
				} else {
					this.messenger.send(acceptRequest);
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
		Proposal p = m.getProposal();
		writeDebug("Received Accept Request from " + m.getSender() + ": " + p);
		if (p.getSn() >= this.currentSn && p.getSn() > this.acceptedProposal.getSn()) {
			this.currentSn = p.getSn();
			this.acceptedProposal = p;
			// send accepted only to distinguished learner (leader)
			AcceptedMessage accepted = new AcceptedMessage(locationData,
					m.getSender(), acceptedProposal);
			if (m.getSender() == this.locationData) {
				this.receiveAccepted(accepted);
			} else {
				this.messenger.send(accepted);
			}
		}
	}

	// Learner methods
	// distinguished learner (leader)
	public void receiveAccepted(AcceptedMessage m) {
		Proposal p = m.getProposal();
		writeDebug("Accepted from " + m.getSender() + ": " + p);
		if (p.getSn() == this.currentSn) {
			this.acceptedNum++;
			if (this.acceptedNum == this.nodeLocationSet.size() / 2 + 1) {
				returnResult(this.value, p);
				for (NodeLocationData node : this.nodeLocationSet) {
					if (node == locationData)
						continue;
					DecisionMessage decision = new DecisionMessage(
							locationData, node, p);
					this.messenger.send(decision);
				}
			}
		}
	}
	
	//other learners
	public void receiveDecision(DecisionMessage m) {
		Proposal p = m.getProposal();
		writeDebug("Received Decision from " + m.getSender() + ": " + p);
		if (p.getSn() >= this.currentSn) {
			this.currentSn = p.getSn();
			this.acceptedProposal = p;
		}
	}
	
}
