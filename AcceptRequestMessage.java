package OSPaxos;

public class AcceptRequestMessage extends Message {
	private Proposal proposal;

	public AcceptRequestMessage(NodeLocationData sender, NodeLocationData receiver, Proposal proposal) {
		super(sender, receiver);
		this.proposal = proposal;
	}

	public Proposal getProposal() {
		return proposal;
	}
	
}
