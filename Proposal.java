package OSPaxos;

//ToDo:
//Do I need to add NodeID to the class of Proposal?
//Proposal is a pair of (sn, value)
public class Proposal implements Comparable<Proposal>{
	private int sn; // the serial number
	private String value;

	public Proposal(int sn, String value) {
		this.sn = sn;
		this.value = value;
	}

	public int getSn() {
		return sn;
	}
	
	public void setSn(int sn){
		this.sn = sn;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public String toString() {
		return "{" + sn + ", " + value + "}";
	}

	@Override
	public int compareTo(Proposal o) {
		// TODO Auto-generated method stub
		if (this.sn < o.sn){
			return 1;
		} else if (this.sn > o.sn){
			return -1;
		}
		return 0;
	}
}
