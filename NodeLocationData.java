package OXPaxos;


public class NodeLocationData 
{

	private int NodeID;
	private boolean isLeader;
	
	public NodeLocationData(int num)
	{
		this.NodeID = num;
		this.isLeader = false;
	}
	
	public void becomeLeader()
	{
		isLeader = true;
	}
	
	public void becomeNonLeader()
	{
		isLeader = false;
	}
	
	public boolean isLeader()
	{
		return isLeader;
	}
	
	public int getNodeID()
	{
		return NodeID;
	}
	
	public String toString()
	{
		return ((Integer)NodeID).toString();
	}
}
