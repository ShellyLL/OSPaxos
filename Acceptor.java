package OSPaxos;

public interface Acceptor {
	
	public void receivePrepareRequest(PrepareRequestMessage m);

	public void receiveAcceptRequest(AcceptRequestMessage m);
	
}
