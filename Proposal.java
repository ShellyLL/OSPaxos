package OXPaxos;


//ToDo:
//Do I need to add NodeID to the class of Proposal?
public class Proposal
{
	private int csn;  //the serial number
	private String value;
	private int NodeID;
	
	public Proposal(int csn, String value)
	{
		this.csn = csn;
		this.value = value;
	}

	public int getPsn()
	{
		return csn;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public int getNodeID(){
		return NodeID;
	}
	public String toString()
	{
		return "{" + csn + ", " + value + "}";
	}
}