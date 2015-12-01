package OSPaxos;

public interface Proposer {
	public void sendPrepareRequest(String v);

	public void receivePromise(PromiseMessage m);
	
	public void receiveDecision(DecisionMessage m);
}