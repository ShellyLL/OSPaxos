package OSPaxos;

public interface Acceptor {
	public void receivePrepareRequest(PrepareRequestMessage m);

	//public void sendPromise(int to, int sn, int prevSn, String prevValue);

	public void receiveAcceptRequest(AcceptRequestMessage m);

	//public void sendAccepted(int sn, String value);
}
