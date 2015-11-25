package OSPaxos;

public interface Proposer {

	public void sendPrepareRequest();

	public void receivePromise(PromiseMessage m);
	
	public void receiveDecision(DecisionMessage m);
}