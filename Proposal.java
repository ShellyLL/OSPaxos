package OXPaxos;


//ToDo:
//Do I need to add NodeID to the class of Proposal?
public class Proposal
{
	private int csn;  //the serial number
	private String value;
	
	public Proposal(int csn, String value)
	{
		this.csn = csn;
		this.value = value;
	}

	public int getCsn()
	{
		return csn;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public String toString()
	{
		return "{" + csn + ", " + value + "}";
	}
}
