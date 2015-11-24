package OXPaxos;

//send only the csn to the acceptors, if the csn is greater than the csn they promised, then the acceptor sends response
public class PrepareRequestMessage extends Message
{
	private int csn;
	
	public PrepareRequestMessage(int csn)
	{
		this.csn = csn;
	}
	
	public int getCsn()
	{
		return csn;
	}
	
}