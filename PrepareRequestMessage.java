package OSPaxos;

public class PrepareRequestMessage extends Message {
	private int sn;

	public PrepareRequestMessage(NodeLocationData sender,
			NodeLocationData receiver, int sn) {
		super(sender, receiver);
		this.sn = sn;
	}

	public int getSn() {
		return sn;
	}

}