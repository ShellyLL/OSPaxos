package OSPaxos;

public class PromiseMessage extends Message {
	private int currentSn;
	private Proposal acceptedProposal; // proposal accepted before

	public PromiseMessage(NodeLocationData sender, NodeLocationData receiver, int csn, Proposal accepted) {
		super(sender, receiver);
		this.currentSn = csn;
		this.acceptedProposal = accepted;
	}
	
	public int getSn() {
		return currentSn;
	}
	
	public Proposal getPrevProposal() {
		return acceptedProposal;
	}

	
}