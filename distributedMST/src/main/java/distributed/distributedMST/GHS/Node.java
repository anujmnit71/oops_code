package distributed.distributedMST.GHS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Represents edges of MST between <code>Nodes</code> . <br/>
 *
 * An <code>Edge</code> can be of following types :
 * <ol>
 *     <li> BASIC.	</li>
 *     <li> BRANCH.	</li>
 *     <li> REJECT.	</li>
 * </ol>
 *
 */
class Edge implements Comparable<Edge>{
	public static final int BASIC  = 0;
	public static final int BRANCH = 1; 
	public static final int REJECT = 2;
	
	public static final String[] edgeTypes = {
												"Basic",
												"Branch",
												"Rejected"
											};
	int src;
	int dest;
	int weight;
	int status;

	public Edge(int d , int w) {
		this.weight = w;
		this.dest = d;
		this.status = Edge.BASIC;
	}
	
	@Override
	public String toString() {
		return src + "-->" + dest + ": " + " weight:" + weight + " status:" + edgeTypes[status];
	}

	public int compareTo(Edge that) {
			return this.weight - that.weight;
	}

}



/**
 * Represent <code>Node</code> of Graph . <br/><br/>
 *
 * Each <code>Node</code> runs in its own thread of execution .Each <code>Node</code> has a <code>BlockingQueue<Message> 
 * queue</code> , all messages which need to be sent to <code>Node</code> are put into this queue.This queue is 
 * thread safe so that nodes can concurrently read and write messages into this queue .
*/

public class Node extends Thread{	
	public static final int SLEEP = 0;
	public static final int SEARCH = 1; 
	public static final int FOUND = 2;

	public static final String[] nodeStatus = {
		"Sleep",
		"Search",
		"Found"
	};
	
	int nodeID;
	BlockingQueue<Message> msgqueue;
	int level;
	int core;
	ArrayList<Edge> edgeList;
	int status;
	int findcount;
	int parentId;
	Edge testEdge;
	Edge bestEdge;
	int bestWt;
	int infinity = 1000000;
	int waitTime = 10;
	//Iterator<Edge> itr;
	
	public Node(int n,BlockingQueue<Message> queue, int[] adjacencyList){
		this.nodeID = n;
		this.level = 0;
		this.core = -1;
		this.status = Node.SLEEP;
		this.findcount = 0;
		this.parentId = -1;
		this.msgqueue = queue;
		this.bestEdge = null;
		this.bestWt = infinity;
		this.testEdge = null;
		
		edgeList = new ArrayList<Edge>();
		//itr = edgeList.iterator(); 
		for(int i=0;i< adjacencyList.length ;i++){
			if(adjacencyList[i] != 0){
				Edge e = new Edge(i, adjacencyList[i]);
				e.src = nodeID;
				this.edgeList.add(e);
			}
				
		}
		
		Collections.sort(edgeList);
		
	}
	
	public void printNode(){
		//for(int i=0 ; i<MSTmain.noOfNodes; i++){
			//System.out.println(i);
			System.out.println("Node id : " + nodeID +"List " + edgeList);
		//}
	}
	
	@Override
	public void run(){
		
		if(MSTmain.debug == true){
			System.out.println("Thread started " + nodeID);
		}
		
		
		try {
			while(true){
				Message message = null;
				message = msgqueue.poll(waitTime,TimeUnit.SECONDS);
				
				if(MSTmain.debug == true){
					System.out.println("Node == "+ this + "Message rcvd" + message);
				}
				if(message == null){
					break;
				}
				
				Edge edgeRecv = findEdgeByDest(message.source); 
				
				//BEGIN WAKEUP MESSAGE
				if(message.messageType == Message.WAKEUP && this.status == Node.SLEEP){
					wakeup();
				}
				//END WAKEUP MESSAGE
				
				//BEGIN CONNECT MESSAGE
				else if(message.messageType == Message.CONNECT){
					if(this.status == Node.SLEEP)
						wakeup();
					if(message.level < this.level){
						int dest = message.source;
						edgeRecv.status = Edge.BRANCH;
						Message msgsend = new Message(Message.INITIATE, nodeID, dest, level ,core, status);
						MSTmain.queueList.get(dest).add(msgsend);
						if(status == Node.SEARCH)
							findcount = findcount + 1;
					}
					else{ 
						if(edgeRecv.status == Edge.BASIC){
						this.msgqueue.add(message);
						Thread.sleep(10);
					}
					else{
						int dest = message.source;
						Message msgsend = new Message(Message.INITIATE, nodeID, dest,  level +1 ,edgeRecv.weight, Node.SEARCH);
						MSTmain.queueList.get(dest).add(msgsend);
					}
					}	
					
				}
				//END CONNECT MESSAGE
				
				//BEGIN INITIATE MESSAGE
				else if(message.messageType == Message.INITIATE){
					this.level = message.level;
					this.core = message.core;
					this.status = message.status;
					this.parentId = message.source;
					this.bestEdge = null;
					this.bestWt = infinity;
					Iterator<Edge> itr = edgeList.iterator();
					Edge e = null;
					while(itr.hasNext()){
					    e = itr.next();
					    if(e.weight != edgeRecv.weight && e.status == Edge.BRANCH){
					    	Message msgsend = new Message(Message.INITIATE, nodeID, e.dest,this.level ,this.core, this.status);
							MSTmain.queueList.get(e.dest).add(msgsend);
							if(this.status == Node.SEARCH)
								this.findcount = this.findcount + 1;
					    }
					}
					if(this.status == Node.SEARCH){
						test();
					}
						
				}
				
				//END INITIATE MESSAGE
				
				//BEGIN TEST MESSAGE
				else if(message.messageType == Message.TEST){
					if(this.status == Node.SLEEP)
						wakeup();
					if(message.level > this.level){
						this.msgqueue.add(message);
						Thread.sleep(10);
					}
					else if(this.core != message.core){
						Message msgsend = new Message(Message.ACCEPT, nodeID, message.source);
						MSTmain.queueList.get(message.source).add(msgsend);
					}
					else{
						if(edgeRecv.status == Edge.BASIC)
							edgeRecv.status = Edge.REJECT;
					
						if(this.testEdge == null || (this.testEdge.weight != edgeRecv.weight)){
							Message msgsend = new Message(Message.REJECT, nodeID, message.source);
							MSTmain.queueList.get(message.source).add(msgsend);
						}
						else{
							test();
						}
					}
				}
				//END TEST MESSAGE
				
						
				//BEGIN ACCEPT MESSAGE
				else if(message.messageType == Message.ACCEPT){
					this.testEdge = null;
					if(edgeRecv.weight < this.bestWt){
						this.bestEdge = edgeRecv;
						this.bestWt = edgeRecv.weight;
					}
					
					report();
				}
				//END ACCEPT MESSAGE
				
				//BEGIN REJECT MESSAGE
				else if(message.messageType == Message.REJECT){
					if(edgeRecv.status == Edge.BASIC){
						edgeRecv.status = Edge.REJECT;
					}
					test();
				}
				//END REJECT MESSAGE
				
				//BEGIN REPORT MESSAGE
				else if(message.messageType == Message.REPORT){
					if(edgeRecv.dest != this.parentId){
						this.findcount = this.findcount - 1;
						if(message.cost < this.bestWt){
							this.bestWt = message.cost;
							this.bestEdge = edgeRecv;
						}
						report();
					}
					else if(this.status == Node.SEARCH){
							msgqueue.add(message);
							Thread.sleep(10);
						}
						else if(message.cost > this.bestWt){
							changeCore();
						}
						else if(message.cost == this.bestWt && this.bestWt == infinity){
							break;
						}
				}
				//END REPORT MESSAGE
		
				//BEGIN CHANGE_ROOT MESSAGE
				else if(message.messageType == Message.CHANGE_ROOT){
					changeCore();
				}
				//END CHANGE_ROOT MESSAGE
				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	public Edge findEdgeByDest(int destination) {
		Iterator<Edge> itr = edgeList.iterator();
		Edge e = null;
		while(itr.hasNext()){
		    e = itr.next();
		    if(e.dest == destination)
		    	break;
		}
		if(e == null)
			System.out.println("ERROR EDGE NOT FOUND");
		return e;
	
	}

	public void wakeup() {
		if(MSTmain.debug == true){
			System.out.println("Waking up " + this.nodeID);
		}
		Edge minwtEdge = edgeList.get(0);
		int dest = minwtEdge.dest;
		minwtEdge.status = Edge.BRANCH;
		this.level = 0;
		this.status = Node.FOUND;
		this.findcount = 0;
		
		Message msgSend = new Message(Message.CONNECT, this.nodeID, minwtEdge.dest,level,core,status);
		MSTmain.queueList.get(dest).add(msgSend);
	}
	
	synchronized private void  test() {
		boolean flag = false;
		Edge e = null;
		Iterator<Edge> itr = edgeList.iterator();
		while(itr.hasNext()){
		    e = itr.next();
		    if(e.status == Edge.BASIC){
		    	flag = true;
		    	this.testEdge = e;
		    	Message msgSend = new Message(Message.TEST, this.nodeID, e.dest,this.level,this.core,this.status);
		    	MSTmain.queueList.get(e.dest).add(msgSend);
		    	break;
		    }		    
		}
		if(flag == false){
			this.testEdge = null;
			report();
		}
    
	}

	
	private void report() {
		if(this.findcount <= 0 && this.testEdge == null){
			this.status = Node.FOUND;
			Message msgSend = new Message(Message.REPORT, this.nodeID, this.parentId,this.bestWt);
			MSTmain.queueList.get(this.parentId).add(msgSend);
		}
			
	}
	
	private void changeCore() {
		//if(this.bestEdge != null){
			if(this.bestEdge.status == Edge.BRANCH){
				Message msgSend = new Message(Message.CHANGE_ROOT, this.nodeID, this.bestEdge.dest);
				MSTmain.queueList.get(this.bestEdge.dest).add(msgSend);
			}
			else{
				Message msgSend = new Message(Message.CONNECT, this.nodeID, this.bestEdge.dest,level,core,status);
				MSTmain.queueList.get(this.bestEdge.dest).add(msgSend);
				this.bestEdge.status = Edge.BRANCH;
			}
		//}
			
	}

	@Override
	public String toString() {
	    return nodeID + "," + level + "," + core + "," + nodeStatus[status] + "," + parentId +"," +bestWt + ","+ bestEdge + ","
	        + msgqueue.size() + "," + findcount + "," + "testedge " + testEdge + "," + msgqueue.toString();
	  }

	
}





