package OXPaxos;


public class Message {
	protected NodeLocationData sender;
	protected NodeLocationData receiver;

	public NodeLocationData getSender() {
		return sender;
	}

	public void setSender(NodeLocationData sender) {
		this.sender = sender;
	}

	public NodeLocationData getReciever() {
		return receiver;
	}

	public void setReceiver(NodeLocationData receiver) {
		this.receiver = receiver;
	}

}
