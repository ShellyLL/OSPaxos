package OSPaxos;

import java.util.*;

public abstract class Node implements Proposer, Acceptor, Learner {
   protected String value = null; // for write only
   
   protected Set<Node> nodes;
   
   protected Set<NodeLocationData> nodeLocationSet; // store the NodeLocation
														// of
	// all the nodes in the group
	//protected Map<NodeLocationData, Node> nodeLocationMap;
	protected NodeLocationData locationData; // unique locationData to identify
	// itself
	protected Messenger messenger;
	protected int currentSn; // to keep track of sn used so far
	protected long startTime; //for timer

	// state failure variable
	protected ArrayList<Boolean> isRunning;
	
   protected int count;

	public Node(int NodeID) {
		this.nodeLocationSet = new HashSet<NodeLocationData>();
		//this.nodeLocationMap = new HashMap<NodeLocationData, Node>();
		this.locationData = new NodeLocationData(NodeID);
		//this.messenger = new Messenger(nodeLocationMap);
		this.currentSn = -1;

		this.count = 0;
		
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
	
   public void setCount(int n) {
      this.count = n;
   }
   
   public void setNodes(Set<Node> s) {
      this.nodes = s;
   }
   
   public String getValue() {
      return this.value;
   }
   
   public void setValue(String v) {
      this.value = v;
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

   public synchronized void receive(Message m) {
      if (m instanceof PrepareRequestMessage && this.getIsRunning().get(0)) {
         PrepareRequestMessage prepareRequest = (PrepareRequestMessage) m;
         receivePrepareRequest(prepareRequest);
      } else if (m instanceof PromiseMessage && this.getIsRunning().get(0) 
            && this.getIsRunning().get(1)) {
         PromiseMessage promise = (PromiseMessage) m;
         receivePromise(promise);
      } else if (m instanceof AcceptRequestMessage
            && this.getIsRunning().get(0) 
            && this.getIsRunning().get(1)
            && this.getIsRunning().get(2)) {
         AcceptRequestMessage acceptRequest = (AcceptRequestMessage) m;
         receiveAcceptRequest(acceptRequest);
      } else if (m instanceof AcceptedMessage 
            && this.getIsRunning().get(0) 
            && this.getIsRunning().get(1)
            && this.getIsRunning().get(2) 
            && this.getIsRunning().get(3)) {
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
	
   public void acceptorFail() {
      this.nodeLocationSet.remove(this.locationData);
      for (Node n : nodes) {
         n.setNodeList(nodeLocationSet);
      }
   }
   
   public void proposorFail() {
      System.out.println("ProposorStateFail in Node.java");
      
      if (count != 0)
        return;
      
      for (Node n : nodes) {
         if (n.getLocationData().getNodeID() == this.getLocationData().getNodeID() + 1) {
            n.becomeLeader();
            n.setValue(this.getValue()); 
            break;
         }
      }
            
      this.nodeLocationSet.remove(this.locationData);
      for (Node n : nodes) {
         n.setNodeList(nodeLocationSet);
         n.setCount(1);
      }
      
      for (Node n : nodes) {
         if (n.isLeader()) {
            n.sendPrepareRequest(n.getValue(), System.currentTimeMillis());
            break;
         }
      }
      
   }
   
	/*
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
	*/
   
	protected synchronized void writeDebug(String s) {
		System.out.println(this.locationData + ": " + s);
	}
	
	protected void returnResult(String value, Proposal p) {
		String v = p.getValue();
		if (value == null) {
			System.err.println("*** Value " + v + " is returned to client ***");
		} else {
			System.err.println("*** Value " + v + " is written to datumbase ***");
		}
		System.err.println("*** Time used: " + (System.currentTimeMillis() - startTime) + " msec ***");
	}
}
