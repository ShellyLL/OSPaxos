
package OXPaxos;

public class PrepareResponseMessage extends Message
{
	private int receivedCsn;
	private int highestCsn;
	private String value;    //proposal accepted before
	
	
	//如何查找收到的promise response 中最大的 highestCsn 对应的 value
	public PrepareResponseMessage( int csn, int highestCsn, String value)
	{
		this.value = value;    //get the psn and the value from proposal
		this.highestCsn = highestCsn;
		this.receivedCsn= csn;
	}
	
	public String getValue()
	{
		return value;
	}
	public int getHighestCsn(){
		return highestCsn;
	}
	
	public int getCsn()
	{
		return receivedCsn;
	}
}