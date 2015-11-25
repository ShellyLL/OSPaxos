package OSPaxos;

public class AcceptedMessage extends Message {
	private Proposal proposal;

	public AcceptedMessage(NodeLocationData sender, NodeLocationData receiver, Proposal proposal) {
		super(sender, receiver);
		this.proposal = proposal;
	}

	public Proposal getProposal() {
		return proposal;
	}
}
