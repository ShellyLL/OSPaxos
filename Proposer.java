package OSPaxos;

public interface Proposer {
	
	public void sendPrepareRequest(String v, long timeStamp);

	public void receivePromise(PromiseMessage m);
	
}