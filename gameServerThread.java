import java.net.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;


public class gameServerThread extends Thread {
  private Socket socket = null;
	private gameProtocol game = new gameProtocol();
	private PrintWriter out;
  private String screenRefresh = "";
  private String sendString = "";
  private boolean sendReady = false;
  private String messageString = null;
  private boolean sendAttr = false;
  private boolean sendEquip = false;
  
 // Timer Stuff (Timer that sends screen refreshes to the client)
  Timer timer;
  TimerTask refreshTimerTask = new TimerTask() {
     public void run() {
       String outStr = "";
       
       // Check if there is feedback from an input command
       if (sendReady == true) {
         outStr += sendString+Constants.commandSeperator;
         sendReady = false;
       }
       
       // Check for TELL messages waiting
       messageString = game.getMessages();
       if (messageString != null) { // there is content
         outStr+="TEXT"+messageString+Constants.commandSeperator;
         messageString = null;
       }
       
       // Get the current Stats
       outStr += game.processInput("STAT")+Constants.commandSeperator;
       
       // Get the attributes 
       if (sendAttr) outStr += game.processInput("ATTR")+Constants.commandSeperator;
       
       // Get the inventory 
       if (game.inventoryChanged()) {
         outStr += game.processInput("INVE")+Constants.commandSeperator;
         if (sendEquip) outStr += game.processInput("EQIP")+Constants.commandSeperator;
         outStr += game.getLastBag()+Constants.commandSeperator;
       }
       
       // Get the Screen map refresh
       outStr += game.processInput("DRAW")+Constants.commandSeperator;
       
       // Send everything to the client
       out.println(outStr);
     } 
  };

  // Constructor 
  public gameServerThread(Socket socket) {
	  super("gameServerThread");
	  this.socket = socket;
  }

  public void run() {
	  try {
	    // Open reading and writing
	    out = new PrintWriter(socket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(
				    new InputStreamReader(
				    socket.getInputStream()));

      // Initialize the Game
	    String inputLine, outputLine;
	    outputLine = game.processInput("INIT");
	    out.println(outputLine);

      // Start the screen Refresh Timer
      timer = new Timer(true);
      timer.scheduleAtFixedRate(refreshTimerTask,0,100); // 10 per second

      // Main Game loop
	    while ((inputLine = in.readLine()) != null) {
	      if      (inputLine.equals("ATTRSTART")) {sendAttr = true;  inputLine = "ATTR";}
	      else if (inputLine.equals("ATTRSTOP"))  {sendAttr = false; inputLine = "";}
	      else if (inputLine.equals("EQUIPSTART")) {sendEquip = true;  inputLine = "EQIP"; }
	      else if (inputLine.equals("EQUIPSTOP"))  {sendEquip = false; inputLine = "";}
        if (inputLine.length() > 0) outputLine = game.processInput(inputLine);
		    if ((outputLine != null) && (!outputLine.equals("NONE")) && (!outputLine.equals("QUIT")) && (outputLine.length() > 0)){
          sendString = outputLine;
		      sendReady = true;
		    }
		    if (outputLine.equals("QUIT")) break; // out of while loop
	    }

      // Cleaning Up
      refreshTimerTask.cancel();
      timer.cancel(); // stop sending things to the client
      out.println("QUIT"); // tell the client to stop listening
      game.endGame();
	    out.close();
	    in.close();
	    socket.close();
	  } catch (IOException e) {
	    System.err.println("IO Error in gameServerThread");
	    try { socket.close(); } catch (IOException E) {System.err.println("Could not close socket");}
	    refreshTimerTask.cancel();
	    timer.cancel();
	    game.endGame();
	    //e.printStackTrace();
	  }
  } // run

}
