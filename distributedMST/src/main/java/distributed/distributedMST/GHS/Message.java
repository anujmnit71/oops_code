package distributed.distributedMST.GHS;

/**
 * Represents messages used by <code>Node</code> to communicate with each other. <br/><br/>
 *
 * A <code>Message</code> is of following types :
 *
 * <ol>
 *     <li> WAKEUP.		</li>
 *     <li> CONNECT.	</li>
 *     <li> INITIATE.	</li>
 *     <li> TEST.		</li>
 *     <li> ACCEPT.		</li>
 *     <li> REJECT.		</li>
 *     <li> REPORT.		</li>
 *     <li> CHANGE_ROOT.</li>
 * </ol>
 *
 */
public final class Message {
  public static final int WAKEUP = 0; 
  public static final int CONNECT = 1;
  public static final int INITIATE = 2;
  public static final int TEST = 3;
  public static final int ACCEPT = 4;
  public static final int REJECT = 5;
  public static final int REPORT = 6;
  public static final int CHANGE_ROOT = 7;

  public static final String[] messageNames = {
					       "wakeup",
					       "connect",
					       "initiate",
					       "test",
					       "accept",
					       "reject",
					       "report",
					       "changeRoot",
					       };


  public int messageType; 
  public int source;      
  public int destination; 
  public int core;        
  public int level;       
  public int cost;        
  public int status;	
 

  public Message(int messageType, int sender, int destination,
		 int level ,int core,  int status) {
    this(messageType, sender, destination,level, core, status , 0);
  }
  
  public Message(int messageType, int sender, int destination, int cost) {
    this(messageType, sender, destination, 0, 0, 0 , cost);
  }

  public Message(int messageType, int sender, int destination) {
    this(messageType, sender, destination, 0, 0,0,0);
  }

  public Message(int messageType,
		  int sender,
		  int destination,
		  int level,
		  int core,
		  int status,
		  int cost) {
    if (messageType < 0 || messageType > 7)
      throw new IllegalArgumentException("Bad message type: " + messageType);
    this.messageType = messageType;
    this.source = sender;
    this.destination = destination;
    this.level = level;
    this.core = core;
    this.status = status;
    this.cost = cost;
  }

  @Override
  public String toString() {
    return source + "-->" + destination + ": " + shortString();
  }

  public String shortString() {
    String result = messageNames[messageType] + "(";
    switch (messageType) {
	    case REPORT:
	    			result += cost; break;
	    case INITIATE:
	    case TEST:
	    case CONNECT:
	    			result += level + "," + core + "," + status; break;
    }
    result += ")";
    return result;
  }
    
} 
