package OSPaxos;

import java.util.Map;

public class Messenger {
	Map<NodeLocationData, Node> nodeLocation;
	public Messenger(Map<NodeLocationData, Node> map){
		nodeLocation = map;
	}
	public void send(Message m){
		NodeLocationData receiver = m.getReceiver();
		nodeLocation.get(receiver).receive(m);
	}
}
