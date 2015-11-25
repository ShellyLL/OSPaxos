package OSPaxos;

public class Message {
	private NodeLocationData sender;
	private NodeLocationData receiver;
	
	public Message(NodeLocationData sender, NodeLocationData receiver){
		this.sender = sender;
		this.receiver = receiver;
	}
	
	public NodeLocationData getSender() {
		return sender;
	}

	public void setSender(NodeLocationData sender) {
		this.sender = sender;
	}

	public NodeLocationData getReceiver() {
		return receiver;
	}

	public void setReceiver(NodeLocationData receiver) {
		this.receiver = receiver;
	}

}
