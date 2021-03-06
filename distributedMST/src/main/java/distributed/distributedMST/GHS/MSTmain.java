package distributed.distributedMST.GHS;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Represent <code>Main</code> class which does all the initialization job. <br/><br/>
*/

public class MSTmain {
	
	public static int noOfNodes;
	public static int[][] adjacencyMatrix;
	
	public static boolean debug = false;
	public static ArrayList<BlockingQueue<Message>> queueList = new ArrayList<BlockingQueue<Message>>();
	public static Node[] nodeList;

	public static void main(String[] args){
		BlockingQueue<Message> msgqueue;
		 
		FileReader fr = null;
		try {
			fr = new FileReader("input.txt");
			BufferedReader br = new BufferedReader(fr); 
			String str; 
			str = br.readLine();
			noOfNodes = Integer.parseInt(str);
			adjacencyMatrix = new int[noOfNodes][noOfNodes];
			
			for(int i=0 ; i<noOfNodes ; i++){
				str = br.readLine();
				String[] temp = str.split(" ");
				for(int j=0 ; j<noOfNodes ; j++){
					 adjacencyMatrix[i][j] = Integer.parseInt(temp[j]);
				}
			}
			fr.close(); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Thread[] t = new Thread[noOfNodes];
		
//		for(int i=0 ; i<noOfNodes ; i++){
//			for(int j=0 ; j<noOfNodes ; j++){
//				 System.out.print(adjacencyMatrix[i][j] + " ");
//			}
//			System.out.println();
//		}
		
		nodeList = new Node[noOfNodes];
		
		
		for(int i=0; i < noOfNodes;i++){
			msgqueue = new LinkedBlockingQueue<Message>();
			queueList.add(i, msgqueue);
			nodeList[i] = new Node(i, msgqueue,adjacencyMatrix[i]);
			
			t[i] = new Thread(nodeList[i]);
			t[i].start();
			//printing initial state of nodes
			nodeList[i].printNode();
		}
		
		for(int i=0; i < noOfNodes;i++){
			Message msgsend = new Message(Message.WAKEUP, i , i);
			queueList.get(i).add(msgsend);			
		}
		
		for(int i=0; i < noOfNodes;i++){
			try {
				t[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Final state is :");
		for(int i=0; i < noOfNodes;i++){
			if(nodeList[i].nodeID  <= nodeList[i].parentId)
				System.out.println((nodeList[i].nodeID + 1) + "\t" + (nodeList[i].parentId+1));
			else
				System.out.println((nodeList[i].parentId + 1) + "\t" + (nodeList[i].nodeID+1));
		}
		
	}
	
}
