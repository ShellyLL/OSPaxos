package OSPaxos;

public class DecisionMessage extends Message {
	private final Proposal proposal;

	public DecisionMessage(NodeLocationData sender, NodeLocationData receiver, Proposal p) {
		super(sender, receiver);
		this.proposal = p;
	}

	public Proposal getProposal() {
		return proposal;
	}
}
