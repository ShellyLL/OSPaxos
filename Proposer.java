package OXPaxos;

public interface Proposer {

	//public void setProposal(String value);
	public void sendPrepareRerquest(int psn, String newValue);
	//public void receivePromise(int fromNodeID, int curPsn, int prevPsn, String prevAcceptedValue);
	public void sendAcceptRequest(int psn, String decidedValue);  // to everyone

}