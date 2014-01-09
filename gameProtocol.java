import java.io.*;

public class gameProtocol {
  static worldClass theWorld = new worldClass();
  private int playerIndex = -1;
  private int lastBagIndex = -1;
  
  public gameProtocol() {
  }

  // returns a LOGN string with the results of trying to log the user in
  // inStr is in the form userName~passWord
  public String checkLogin(String inStr, boolean createUser) {
    String retVal = "NONE";
    String userName = "";
    String passWord = "";
    int splitter = inStr.indexOf('~');
    if ((splitter > 0) && (inStr.length() > splitter+1)){
      userName = inStr.substring(0,splitter);
      passWord = inStr.substring(splitter+1);
      playerIndex = theWorld.getPlayerIndex(userName,passWord);
      // Check for creating user if necessary
      if ((createUser == true) && (playerIndex == -101)) { // if user unknown
        playerIndex = theWorld.createUser(userName,passWord);
        System.out.println("Created user "+userName+".");
      }
      if (playerIndex >= 0) {
        System.out.println(userName+" has joined the game.");
        retVal = "LOGNSUCCESS";
      } else {
        if      (playerIndex == -100) retVal = "LOGNInvalid Password!";
        else if (playerIndex == -101) retVal = "LOGNINVALIDUSER"+userName+"~"+passWord+"";
        else if (playerIndex == -102) retVal = "LOGNThe server is full and cannot accept any more connections. Try again later.";
        else if (playerIndex == -103) retVal = "LOGNThe user is already logged in.";
        System.out.println("Player "+userName+" failed to login with message="+retVal.substring(4));
      }
    } else {
      retVal = "LOGNBadly formatted String.  You may have entered a ~ or be missing a username or password";
    }
    return retVal; 
  }

  // ------------------------ Image Initialization -----------------
  private String getImages() {   
    String retVal = "";
    String[] imageList;
    imageList = (new File(Constants.imageDir)).list(); // List the contents of the directory
    for (int i=0; i< imageList.length;i++) {
      retVal = retVal+" "+imageList[i];
    }
   return retVal;
  }

  // Takes the input from the client or Server Thread and processes it 
  public String processInput(String inString) {
    String outString = "NONE";
    if (inString.length() >= 4) {
    String command = inString.substring(0,4); // Take the first four chars for the command
    String details = inString.substring(4);
    if (playerIndex < 0) { 
     // ----------  If player is not logged in --------------------
     if (command.equals("INIT")) { // Establishing Connection
       outString = "SCRNINIT"+"IMGS"+getImages();
     } else if (command.equals("QUIT")) {
       outString = "QUIT";
     } else if (command.equals("USER")) { // Login Attempt
       outString = checkLogin(details,false); // don't create user
     } else if (command.equals("NEWU")) { // Create New User Attempt
       outString = checkLogin(details,true); // do create user
     } else {
      // Do nothing (All other commands are ignored before logging in)
      // System.out.println("INVALID NOT LOGGED IN COMMAND:"+inString); // Not a recognized command
     }
    } else { 
      // --------------- Player is logged in (playerIndex >= 0) ------------------
      if      (command.equals("QUIT")) outString = "QUIT";
      else if (command.equals("MOVE")) {
        theWorld.movePlayer(playerIndex, details); 
      } else if (command.equals("DRAW")) {
        outString = theWorld.getDisplay(playerIndex);
        outString = outString+"[~--OBJS--~]"+theWorld.getObj(playerIndex);
      } else if (command.equals("GPOS")) {
        outString = "TEXT"+theWorld.getPlayerXY(playerIndex);
      } else if (command.equals("TELL")) {
        outString = "TEXT"+theWorld.tellPlayer(playerIndex, details);
        if (outString.equals("TEXT")) outString = "NONE"; // no feedback if no errors found
      } else if (command.equals("DESC")) {
        outString = "INFO"+getDesc(details);
      } else if (command.equals("BAGI")) {
        outString = getBagDesc(details); // returns INFO and BAGI
      } else if (command.equals("TARG")) { // using a spell that requires a target
        outString = theWorld.parseTarget(playerIndex, details); 
      } else if (command.equals("STAT")) {
        outString = "STAT"+theWorld.getPlayerStats(playerIndex);
      } else if (command.equals("INVE")) {
        outString = "INVE"+theWorld.getPlayerInventory(playerIndex);
      } else if (command.equals("GETO")) { 
        theWorld.pickUpObj(playerIndex,details);
        outString = "INVE"+theWorld.getPlayerInventory(playerIndex); // display the inventory after attempted pick up
      } else if (command.equals("DRAG")) { 
        outString = doDrag(details);
      } else if (command.equals("EQIP")) { 
        outString = "EQIP"+theWorld.getEqp(playerIndex);
      } else if (command.equals("ATTR")) { 
        outString = "ATTR"+theWorld.getAttr(playerIndex);
      } else if (command.equals("ATTK")) { 
        outString = "TEXT"+theWorld.parseAttk(playerIndex,details);
      } else if (command.equals("FREE")) { 
        outString = "TEXT"+theWorld.addFreeAttributes(playerIndex,details);
      } else {        
        System.out.println("INVALID LOGGED IN COMMAND:"+inString); // Not a recognized command
      }
    }
    } else System.out.println("Invalid command :"+inString); // too short of a command (less than 4 chars)
    return outString; 
  }

  // Returns a string describing and object, creature, spell or map element
  private String getDesc(String inStr) {
    String retVal = "NONE";
    if (inStr.length() < 3) return "NONE"; // error checking (Must be 3 char command type)
    String type = inStr.substring(0,3);
    String location = inStr.substring(3);
    if      (type.equals("INV")) retVal = theWorld.getInvDesc(playerIndex, location); // from obj in inventory
    else if (type.equals("BAG")) retVal = theWorld.getBagDesc(playerIndex, location); // from contains[] in obj
    else if (type.equals("MAP")) retVal = theWorld.getMapDesc(playerIndex, location); // from position on map (creature/obj/terrain)
    else if (type.equals("EQP")) retVal = theWorld.getEqpDesc(playerIndex, location); // from eqp
    return retVal;  
  }

  private String getBagDesc(String inStr) { // returns INFO and (optional)BAGI
    String retVal = "NONE";
    if (inStr.length() < 3) return "NONE"; // error checking (Must be 3 char command type)
    String type = inStr.substring(0,3);
    String location = inStr.substring(3);
    if         (type.equals("INV")) { // the location is the Inventory index
      retVal = "INFO"+theWorld.getInvDesc(playerIndex, location); 
      String ret = theWorld.getInvBagDesc(playerIndex, location); // get the use or bag contents from in inventory
      if (ret.length() > 0) retVal += Constants.commandSeperator + ret;
    } else if (type.equals("BAG")) {
      retVal = "INFO"+theWorld.getBagDesc(playerIndex, location); // from contains[] in obj
      String ret = theWorld.getBagBagDesc(playerIndex, location); // get the use or bag contents from inside a bag
      if (ret.length() > 0) retVal += Constants.commandSeperator + ret;
    }
    if (retVal.indexOf("BAGI") >= 0) lastBagIndex = theWorld.getBagObjIndex(playerIndex, inStr);
    return retVal;
  }


 // Calls the drag procedure in worldClass.  It also handles clearing the client bag view if the user
 // dropped a bag that he is currently viewing. (known with of lastBagIndex)
 private String doDrag(String details) {
   String retVal = theWorld.drag(playerIndex,details);
   String clearBag = checkLastBagOwner();
   if (clearBag.length() > 0) {
     if (retVal.equals("NONE")) retVal = clearBag;
     else retVal += Constants.commandSeperator+clearBag; // other commands as well as clearing the bag
   }
   return retVal;
 }


 // Returns a new message waiting to be sent to the user
 public String getMessages() {
   if (playerIndex >= 0)
     return theWorld.getPlayerMessage(playerIndex);
   else return null;
 }
 
 // Terminate the Game Connection.  (Saving the character, and exiting him from the world)
 public void endGame(){
   if (playerIndex >=0) {
     System.out.println(theWorld.getPlayerName(playerIndex)+" has left the game.");
     theWorld.removePlayer(playerIndex);
   }
 }

 public String getLastBag() {
   String retVal = "NONE";
     if (lastBagIndex >= 0) {
       retVal = "BAGI"+theWorld.getBagString(lastBagIndex);
     }
   return retVal;
 }   
  
 public boolean inventoryChanged() {
   return theWorld.inventoryChanged(playerIndex);
 }
 
 // Returns a clearing string to the client if the lastBag is no longer owned
 // by the player
 public String checkLastBagOwner() {
   String retVal = "";
   if (lastBagIndex >= 0) {
     if (playerIndex != theWorld.findPlayerHoldingObj(lastBagIndex)) {
      retVal = "BAGIERASE";
      lastBagIndex = -1;
     }
   }
   return retVal;
 }
  
}