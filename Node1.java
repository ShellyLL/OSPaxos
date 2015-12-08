package OSPaxos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Node1 extends Node {
   protected Set<Node1> nodes;

   // Proposer Variables
   protected PriorityQueue<Proposal> promises; // sorted by sn in descending order
   protected String value = null; // for write only

   // Acceptor Variables
   protected Proposal acceptedProposal;

   // Learner Variables
   protected Map<Proposal, Integer> learnedProposals;// key:proposal, value: accepted received

   public Node1(int NodeID) {
      super(NodeID);
      this.nodes = new HashSet<Node1>();
      // proposer
      this.promises = null;

      // acceptor
      this.acceptedProposal = new Proposal(-1, null);// -1 refers to no
      // proposal accepted
      // learner
      learnedProposals = new HashMap<Proposal, Integer>();
   }

   public void setNodes(Set<Node1> s) {
      this.nodes = s;
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
      } else if (m instanceof PrepareRequestMessage && !this.getIsRunning().get(0)) {
         writeDebug("Acceptor Fail: Fail to Receive the Prepare Request and Send Promise");
         acceptorFail();
      } else if (m instanceof AcceptRequestMessage && !this.isRunning.get(2)) {
         writeDebug("Acceptor Fail: Fail to Receive the Accept! Request");
         acceptorFail();
      } else if (m instanceof PromiseMessage && !this.isRunning.get(1)) {
         writeDebug("Proposor Fail: Fail to Evaluate Acceptor's Promise and Send Accept!");
         proposorFail();
      }
   }

   protected void acceptorFail() {
      for (Node1 n : nodes) {
         //n.nodeLocationMap.remove(this);
         n.nodeLocationSet.remove(this);
      }
   }
   
   protected void proposorFail() {
      for (Node1 n : nodes) {
         //n.nodeLocationMap.remove(this);
         n.nodeLocationSet.remove(this);
      }
   }


   /*
	// message dispatcher
	public synchronized void receive(Message m) {
		if (m instanceof PrepareRequestMessage && this.getIsRunning().get(0)) {
			PrepareRequestMessage prepareRequest = (PrepareRequestMessage) m;
			receivePrepareRequest(prepareRequest); // I think this is
													// leader/proposer fail
													// because it is leader's
													// responsibility to send
													// prepare request
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
    */
   // Proposer methods
   @Override
   public void sendPrepareRequest(String v) {
      // The following two lines are changed by Hanzi when debugging
      this.promises = new PriorityQueue<Proposal>(nodeLocationSet.size());
      this.value = v;
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
		if (promises.size() == nodeLocationSet.size() / 2) {// plus itself
			this.promises.add(this.acceptedProposal);
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
      int sn = p.getSn();
      if (sn >= this.currentSn) {
         this.currentSn = sn;
         this.acceptedProposal = p;
         for (NodeLocationData node : nodeLocationSet) {
            // writeDebug("Send Accepted to" + node);
            AcceptedMessage accepted = new AcceptedMessage(locationData,
                  node, acceptedProposal);
            if (node == this.locationData) {
               this.receiveAccepted(accepted);
            } else {
               this.messenger.send(accepted);
            }
         }
      }
   }

   // Learner methods
   @Override
   public void receiveAccepted(AcceptedMessage m) {
      Proposal p = m.getProposal();
      writeDebug("Accepted from " + m.getSender() + ": " + p);
      Integer times = learnedProposals.get(p);
      learnedProposals.put(p, times == null ? 1 : times + 1);
      if (learnedProposals.get(p) == nodeLocationSet.size() / 2 + 1) {
         if (p.getSn() > this.currentSn) {
            this.currentSn = p.getSn();
            this.acceptedProposal = p;
         }
         // leader return value to client
         if (this.locationData.isLeader() && this.currentSn == p.getSn()) {
            returnResult(this.value, p);
         }
      }
   }

}
