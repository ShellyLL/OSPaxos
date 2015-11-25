package OSPaxos;

public interface Learner {
	public void receiveAccepted(AcceptedMessage m);
}
