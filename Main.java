package OSPaxos;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

//test test
public class Main {
	private static HashSet<Node> nodes;
	private static HashSet<NodeLocationData> nodeLocations;
	private static ArrayList<String> proposebuffer;
	public static void main(String[] args) throws IOException{
		nodes = new HashSet<Node>();
		nodeLocations = new HashSet<NodeLocationData>();
		System.out.println("Type 'help' for a list of commands");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		{
			try{
				String[] s = in.readLine().split(" ", 2);
				String cmd = s[0];
				String arg = s.length > 1 ? s[1] : null;
				
				if(cmd.equalsIgnoreCase("init"))
					createNodes(Integer.parseInt(arg));
				
				else if(cmd.equalsIgnoreCase("read"))
						read(nodes);
				else if (cmd.equalsIgnoreCase("write"))
					    write(arg, nodes);
				else
					writeDebug("Unrecognized command");
		
	        } catch(IOException e ){
	            e.printStackTrace();
	        }
		}

	}
	
	
	private static void write (String s, HashSet<Node> nodes)
	{
		writeDebug("Proposing for writing: " + s);
		for(Node node : nodes)
			if(node.isLeader())
			{
				node.propose(s, nodes);
				break;
			}
	}
	private static String read (HashSet<Node> nodes)
	{
		writeDebug("Proposing for reading: ");
		for(Node node : nodes)
			if(node.isLeader())
			{
				node.propose(null, nodes);
				break;
			}
		return "";
	}
	
	private static void createNodes(int n)
	{		
		for(int i = 0; i < n; i++)
		{
			Node node = new Node(i);
			if(i == 0) // make 0 leader
				node.becomeLeader();
			nodes.add(node);
			nodeLocations.add(node.getLocationData());
		}
		
		// give node list to all nodes (statically)
		for(Node node : nodes)
			node.setNodeList(nodeLocations);
		
		writeDebug(n + " nodes created");
	}
	
	
	private static void writeDebug(String s)
	{
		System.out.print("*** ");
		System.out.print(s);
		System.out.println(" ***");
	}

}
