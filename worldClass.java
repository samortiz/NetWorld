import java.io.*;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

public class worldClass {
  public static int maxCreatureViews = 100;
  
  private mapElement[][] map       = new mapElement[Constants.mapMaxX][Constants.mapMaxY];
  private Obj[]          obj       = new Obj[Constants.maxObj];
  private Creature[]     creatures = new Creature[Constants.maxCreatures];
  private Creature[]     players   = new Creature[Constants.maxPlayers];

  private Random random = new Random();

 // Timer Stuff (Server timer that runs the AI)
  Timer timer;
  TimerTask AITimerTask = new TimerTask() {
     public void run() {

      // Run Creature AI
      for (int i=0; i< Constants.maxCreatures; i++) {
        if ((creatures[i].exists == true) && (creatures[i].alive == true)) {
          if (creatures[i].hitPoints <= 0) kill("creature",i); // if the creature is dead but doesn't know it yet...
          else if (creatures[i].ai.numRules > 0) {  // if there is an AI
            CreatureView[] views = new CreatureView[maxCreatureViews];
            int loopCounter = 0;
            // find all the creatures that this creature can see
            for (int j=0; j< Constants.maxCreatures; j++) {
              if ((creatures[j].exists == true) && (creatures[j].alive == true) && (i != j) ) {
                double distance = getDistance("creature",i,"creature",j);
                if ((distance >= 0) && (creatures[i].sightRange >= distance ) && (loopCounter < maxCreatureViews)) {
                  views[loopCounter] = getView("creature",j);
                  views[loopCounter].distance = distance;
                  loopCounter += 1;
                }
              }
            }
            // find all the players this creature can see
            for (int j=0; j< Constants.maxPlayers; j++) {
              if ((players[j].exists == true) && (players[j].alive == true)) {
                double distance = getDistance("creature",i,"player",j);
                if ((distance >= 0) && (creatures[i].sightRange >= distance ) && (loopCounter < maxCreatureViews)) {
                  views[loopCounter] = getView("player",j);
                  views[loopCounter].distance = distance;
                  loopCounter += 1;
                }
              }
            }
            // Calculate inRange and allInRange from Equipped objects
            double range = getInRange("max","creature",i);
            double allInRange = getInRange("all","creature",i);
            // Feed the AI it's world View
            creatures[i].ai.observe(views,loopCounter, creatures[i],range,allInRange);
            // Get an action from the AI
            AIAction action = creatures[i].ai.getAction();
            // Parse any server actions that may need to be done
            if (action.type.length() > 0) {
              if (action.type.equals("attack")) {
                String defWho = "player";
                int defIndex = getPlayerIndex(action.x,action.y);
                if (defIndex < 0) { // player not found there
                  defIndex = getCreatureIndex(action.x,action.y);
                  defWho = "creature";
                }
                if (defIndex >= 0) {
                  String attackResult = doAttack("creature",i,defWho,defIndex);
                  if (attackResult.indexOf("damage") >= 0) { // if damage was done
                    boolean result = leaveMessage("creature",i,defWho,defIndex,creatures[i].userName+" "+attackResult);
                  }
                }
              } else if (action.type.equals("move")){
                doMove(creatures[i],action.details);
              } else if (action.type.equals("tell")){
                String tellWho = "player";
                int tellIndex = getPlayerIndex(action.userName);
                if (tellIndex <0) tellIndex = getCreatureIndex(action.userName);
                if (tellIndex >=0) {
                  boolean success = leaveMessage("creature",i,tellWho,tellIndex,creatures[i].userName+": "+action.details);
                }
              } else System.out.println("Error doing action:"+action.type+" ("+action.x+","+action.y+") userName="+action.userName+" details="+action.details);
            }
            if (creatures[i].moveDelay   > 0) creatures[i].moveDelay   -=1;
            if (creatures[i].attackDelay > 0) creatures[i].attackDelay -=1;
            if (creatures[i].spellDelay  > 0) creatures[i].spellDelay  -=1;
          }
        } else if ((creatures[i].exists == true) && (creatures[i].alive == false)) {
          if (creatures[i].corpseTime > 0) creatures[i].corpseTime -= 1;
          else if (creatures[i].corpseTime == 0) {
            creatures[i].exists = false;
          }
        }
      }
    
      // Adjust player movement factors. (when the delay is 0 the player can move again)
      for (int i=0; i < Constants.maxPlayers;i++){
        if ((players[i].exists == true) && (players[i].alive == true)) {
          if (players[i].moveDelay   > 0) players[i].moveDelay   -=1;
          if (players[i].attackDelay > 0) players[i].attackDelay -=1;
          if (players[i].spellDelay  > 0) players[i].spellDelay  -=1;
          if (players[i].hitPoints <= 0) kill("player",i); 
          if (players[i].hitPoints < players[i].hitPointsMax) players[i].hitPoints += players[i].constitution*0.005; // regenerate hitpoints
          if (players[i].spellPoints < players[i].spellPointsMax) players[i].spellPoints += players[i].intelligence*0.005; // regenerate spellPoints
        }
      }//for

      // Modify all objects reducing lifetime. Remove objects that have lifetime = 0
      //   any objects with spells running should be run appropriately cont or dispelled
      for (int i=0; i<Constants.maxObj; i++) {
        if (obj[i].exists && (!obj[i].type.equals(Obj.NULLOBJ))) {
         if (obj[i].lifetime > 0) {
          obj[i].lifetime -= 1;
          if (obj[i].lifetime == 0) killObj(i); 
         }
         if (obj[i].use.exists && obj[i].use.isRunning) {
          // reduce spell lifetime, do continue spell effects, terminate spell if necessary
          if ((obj[i].use.lifetime > 0) || (obj[i].use.lifetime == -1)) { // if there is a continued effect
            if (obj[i].use.lifetime > 0) obj[i].use.lifetime -= 1; // remove one lifetime from the object's use
            Creature caster = getCreature(obj[i].use.cType, obj[i].use.cIndex); // get the caster
            Creature target = getCreature(obj[i].use.tType, obj[i].use.tIndex); // get the target
            if (obj[i].use.lifetime == 0) obj[i].use.cast(caster,target,"dispell"); // dispell if lifetime is up
            else obj[i].use.cast(caster,target,"cont"); // continue otherwise
          }
         }
        }
      }
     } 
  };
  
  // --------------------------------- KILL METHODS -----------------------------------------------------
  
  // Kills a player or creature
  private void kill(String who, int whoIndex) {
    if (who.equals("player")) {
      players[whoIndex].alive = false;
    } else if (who.equals("creature")) {
      creatures[whoIndex].alive = false;
      // Drop all equipped objects
      for (int i=0; i<Creature.maxEquip; i++) {
        if (creatures[whoIndex].equip[i] > 0) {
          addObjToMap(creatures[whoIndex].equip[i], creatures[whoIndex].x, creatures[whoIndex].y);
        }
      }
      // Drop all inventory objects
      for (int i=0; i<Creature.maxObj; i++) {
        if (creatures[whoIndex].inventory[i] > 0) {
          addObjToMap(creatures[whoIndex].inventory[i], creatures[whoIndex].x, creatures[whoIndex].y);
        }
      }
    }
  }


  // returns the root object index.  That is, the outermost bag (that is not contained in any other object)
  // returns index if index is the root object. (not contained in anything)
  private int getRootObj(int index) {
    int retVal = index;
      for (int i=0; i<Constants.maxObj; i++) {
        if (obj[i].exists && obj[i].maxContains > 0) {
          for (int c=0; c<obj[i].maxContains; c++) {
            if (obj[i].contains[c] == index) {
              return getRootObj(i);
            }
          }
        }
      }
    return retVal;
  }

  // returns the index of the player holding this object.
  // if no player is holding this object then it returns -1
  // if the object is in a bag it will return the player holding the bag (recursing as necessary)
  public int findPlayerHoldingObj(int objIndex) {
    int retVal = -1;
    int rootIndex = getRootObj(objIndex);
    // Find out if a player is holding this object in inventory or equip
    for (int i=0; i<Constants.maxPlayers; i++) {
      if (players[i].exists) {
        for (int v=0; v<Creature.maxObj; v++) { // players' inventory
          if (players[i].inventory[v] == rootIndex) return i;
        }
        for (int e=0; e<Creature.maxEquip; e++) {
          if (players[i].equip[e] == rootIndex) return i;
        }
      }
    }
    return retVal;
  }  
  

  // Removes an object from the game.
  // Removes all subObjects then removes all references to the object by creatures, players or other objects
  // dispells any spell that may be running
  private void killObj(int index) {
    // remove all SubObjects of this object (contains) (a bag being destroyed, destroys everything in the bag)
    for (int i=0; i<obj[index].maxContains; i++) {
      if (obj[index].contains[i] >= 0) killObj(obj[index].contains[i]);
    }
    // find a player that may be effected by removing this object 
    int playerIndex = findPlayerHoldingObj(index);
    // Remove References to this Object
    for (int i=0; i<Constants.maxPlayers; i++) { // players
      players[i].removeObjIndex(index);
    }
    for (int i=0; i<Constants.maxCreatures; i++) { // creatures
      creatures[i].removeObjIndex(index);
    }
    for (int i=0; i<Constants.maxObj; i++) { // other objects
      obj[i].removeObjIndex(index); 
    }
    // Alert the player holding this object (if there is one) that the object is gone
    if (playerIndex >= 0) players[playerIndex].inventoryChanged = true;
    // if there is a running spell, then dispell it
    if (obj[index].use.exists && obj[index].use.isRunning) {
      Creature caster = getCreature(obj[index].use.cType, obj[index].use.cIndex);
      Creature target = getCreature(obj[index].use.tType, obj[index].use.tIndex);
      obj[index].use.cast(caster,target,"dispell");
    }
    // Destroy the object (freeing it to be re-used)
    obj[index].clearAttributes();
  }  
  
  
  // ---------------------- GET INDEXES ----------------------------------
  
  // Returns the index of the first player at this location (lowest index)
  // returns -1 if no players are at that location
  private int getPlayerIndex (int x, int y) {
    int retVal = -1;
    for (int i=0; i<Constants.maxPlayers; i++) {
      if ((players[i].exists) && (players[i].alive) &&
          (players[i].x == x) && (players[i].y == y)) {
        retVal = i;
        break;
      }
    }
    return retVal;
  }
  // Same as getPlayerIndex(int,int), but it checks for creatures at the xy location
  private int getCreatureIndex (int x, int y) {
    int retVal = -1;
    for (int i=0; i<Constants.maxCreatures; i++) {
      if ((creatures[i].exists) && (creatures[i].alive) &&
          (creatures[i].x == x) && (creatures[i].y == y)) {
        retVal = i;
        break;
      }
    }
    return retVal;
  }
  // Gets the playerIndex from a String userName
  private int getPlayerIndex (String inStr) {
    String userName = inStr.toLowerCase().trim();
    int retVal = -1;
    for (int i=0; i<Constants.maxPlayers; i++) {
      if ((players[i].exists) && (players[i].alive) &&
          (players[i].userName.toLowerCase().trim().equals(userName))) {
        retVal = i;
        break;
      }
    }
    return retVal;
  }
  // gets a creature index from creatures if userName matches
  // userName-index  (used to distinctly remember creatures in ai)
  private int getCreatureIndex (String inStr) {
    String userName = inStr.toLowerCase().trim();
    int retVal = -1;
    for (int i=0; i<Constants.maxCreatures; i++) {
      if ((creatures[i].exists) && (creatures[i].alive) &&
          ((creatures[i].userName+"-"+i).toLowerCase().trim().equals(userName))) {
        retVal = i;
        break;
      }
    }
    return retVal;
  }

  // ------------------------------- Get Creature from Who, Index ---------------------------
  // Returns the creature at the index of players or creatures, with whoStr=player/creature
  // if it can't find the creature it will return a null creature
  private Creature getCreature(String whoStr, int whoIndex) {
    if (whoIndex >= 0) {
      if (whoStr.equals("player"))   return players[whoIndex];
      if (whoStr.equals("creature")) return creatures[whoIndex];
    } 
    return new Creature();
  }

  // -------------------------- GET RANGE -------------------------------------------------------
  
  // Returns the range of the currently equipped weapons for this creature
  // whoType = creature or player
  // type = max : returns the range of the longest ranged weapon equipped
  // type = all : returns the range of the shortest ranged weapon equipped
  private double getInRange(String type, String whoType, int creatureIndex) {
    double retVal = -1;
    double baseAttackRange = 0;
    if (whoType.equals("creature")) baseAttackRange = creatures[creatureIndex].baseAttackRange;
    else if (whoType.equals("player")) baseAttackRange = players[creatureIndex].baseAttackRange;
    if (creatureIndex >= 0) {
      if (type.equals("max")) {
        double currentMax = -1;
        for (int i=0; i<Creature.maxEquip; i++) {
          int objIndex = -1;
          if (whoType.equals("creature")) objIndex = creatures[creatureIndex].equip[i];
          else if (whoType.equals("player")) objIndex = players[creatureIndex].equip[i];
          if ((objIndex >= 0) && (obj[objIndex].range > currentMax) &&  // if it has a longer range
              ((obj[objIndex].damageBludgeon>0)||(obj[objIndex].damagePierce>0)||(obj[objIndex].damageSlash>0)|| // if it does damage
               (obj[objIndex].damageFire>0)||(obj[objIndex].damageIce>0)||(obj[objIndex].damageMagic>0))  ) {
            currentMax = obj[objIndex].range;
          }
        }
        if ((baseAttackRange > 0) && (baseAttackRange > currentMax)) {
          currentMax = baseAttackRange;
        }
        retVal = currentMax;
      } else if (type.equals("all")) {
        double currentMin = 1000; // some large number greater than the maximum range of any weapon
        for (int i=0; i<Creature.maxEquip; i++) {
          int objIndex = -1;
          if (whoType.equals("creature")) objIndex = creatures[creatureIndex].equip[i];
          else if (whoType.equals("player")) objIndex = players[creatureIndex].equip[i];
          if ((objIndex >= 0)&&(obj[objIndex].range < currentMin) &&  // if it has a shorter range
              ((obj[objIndex].damageBludgeon>0)||(obj[objIndex].damagePierce>0)||(obj[objIndex].damageSlash>0)|| // if it does damage
               (obj[objIndex].damageFire>0)||(obj[objIndex].damageIce>0)||(obj[objIndex].damageMagic>0))  ) {
            currentMin = obj[objIndex].range;
          }
        }
        // If base attack is shortest attacking distance then return base attack range
        if ((baseAttackRange > 0) && (baseAttackRange < currentMin)) {
          currentMin = baseAttackRange;
        }
        if (currentMin == 1000) currentMin = -1; // Do not return 1000, instead you have no weapons equipped
        retVal = currentMin;
      }
    }
    return retVal;
  }

  // returns true if x,y and X,Y are less than or equal to range distance apart
  private boolean inRange(int x, int y, int X, int Y, double range) {
    int xDif = Math.abs(x - X);
    int yDif = Math.abs(y - Y);
    double distance = Math.sqrt((xDif*xDif)+(yDif*yDif));
    if (distance <= range) 
      return true;
    else return false;
  }


 // ------------------- GET CREATUREVIEW ------------------------

  // returns a view of a creature or player.
  // who should be "creature" or "player", and index the index in creatures[] or players[]
  private CreatureView getView(String who, int index) {
    CreatureView retVal =  new CreatureView();
    if (who.equals("creature")) {
      retVal.userName = creatures[index].userName+'-'+index;
      retVal.x = creatures[index].x;
      retVal.y = creatures[index].y;
      retVal.HPPct = (creatures[index].hitPoints / creatures[index].hitPointsMax)*100;
      retVal.SPPct = (creatures[index].spellPoints / creatures[index].spellPointsMax)*100;
      if (creatures[index].clan.length() == 0) { // no clan set
       retVal.clan = creatures[index].userName+"-"+index; // username+index (something unique)
      } else retVal.clan = creatures[index].clan;
    } else if (who.equals("player")) {
      retVal.userName = players[index].userName;
      retVal.x = players[index].x;
      retVal.y = players[index].y;
      retVal.HPPct = (players[index].hitPoints / players[index].hitPointsMax)*100;
      retVal.SPPct = (players[index].spellPoints / players[index].spellPointsMax)*100;
      if (players[index].clan.length() == 0) { // no clan set
       retVal.clan = players[index].userName+"-"+index; // username+index (something unique)
      } else retVal.clan = players[index].clan;
    }
    return retVal;
  }


  // Returns the location of the matching close bracket given the index
  // of the starting bracket.  It will return -1 if it cannot find a close
  // bracket
  public static int closeBracket(String inStr, int start) {
    int openCount = 0;
    int retVal = -1;
    if ((start >= 0) && (inStr.charAt(start) == '(')) { 
      for (int i=start; i< inStr.length(); i++) {
        if (inStr.charAt(i)=='(') openCount += 1;
        else if (inStr.charAt(i)==')') openCount -=1;
        if (openCount == 0) {
          retVal = i;
          break;
        } // if
      } // for
    } // if
    return retVal;
  }
  
  
 // --------------------------- OBJ Helper Functions  -----------------------------------

  // Returns the index of the next undefined object (reserves it, so if you use this function
  // and don't use the index, free it by estting exists=false)
  private int getFreeObj() {
    int retVal = -1;
    for (int i=0; i<Constants.maxObj; i++) {
      if ((obj[i].type == Obj.NULLOBJ) && (obj[i].exists == false)) {
        retVal = i;
        obj[i].exists = true;
        break;
      }
    }
    return retVal;
  }

  // same as above, but returns the next highest index starting with maxIndex
  private int getFreeObj(int maxIndex) {
    int retVal = -1;
    for (int i=maxIndex; i<Constants.maxObj; i++) {
      if ((obj[i].type == Obj.NULLOBJ) && (obj[i].exists == false)) {
        retVal = i;
        obj[i].exists = true;
        break;
      }
    }
    return retVal;
  }
  
  

  // parse out contains=() from a single object
  // eg.
  // (x = 1, y=2, name=bag, contains=((x=2),(x=3)), weight=23)  turns into 
  // (x = 1, y=2, name=bag, contains=(2,3), weight=23)
  private String createSingleSubObject(String inStr) {
    StringBuffer retVal = new StringBuffer(inStr);
    String newObjStr;
    int startBracket = -1;
    int endBracket = -1;
    startBracket = retVal.toString().indexOf("contains");
    if (startBracket >= 0) startBracket = retVal.toString().indexOf("=",startBracket);
    if (startBracket >= 0) startBracket = retVal.toString().indexOf('(',startBracket);
    if (startBracket >= 0) endBracket = closeBracket(retVal.toString(), startBracket);
    if ((startBracket > 0) && (endBracket > 0)) {
       newObjStr = retVal.substring(startBracket,endBracket+1); // +1 to include the final )
       newObjStr = createSubObjects(newObjStr);
       retVal = retVal.delete(startBracket,endBracket+1);
       retVal = retVal.insert(startBracket,newObjStr);
    }
    return retVal.toString();    
  }
  
  // parse out the () from a list of objects (),,,(), and replace them with object 
  // indexes for newly defined objects  
  // eg. (x=1,y=2,rightHand=(name=long sword,img=sword)) is replaced with 
  //     (x=1,y=2,rightHand=3) where 3 is the obj index of the newly defined long sword
  // eg2. ((x=1,y=2),(x=2,y=4),,)
  //      (4,5,,)
  private String createSubObjects(String inStr){
    StringBuffer retVal = new StringBuffer(inStr);
    String newObjStr;
    int startBracket;
    int endBracket;
    int objIndex;
    startBracket = retVal.toString().indexOf('(',1);
    endBracket = closeBracket(retVal.toString(), startBracket);
    if ((startBracket > 0) && (endBracket > 0)) {
      newObjStr = retVal.substring(startBracket,endBracket+1); // +1 to include the final )
      if (newObjStr.length() > 2) {
        // Found a sub-sub-Object
        newObjStr = createSingleSubObject(newObjStr); // collapse any contains (must be done before setAttributes)
        objIndex = getFreeObj();
        if (objIndex >= 0) {
          obj[objIndex].setAttributes(newObjStr);
          retVal = retVal.delete(startBracket,endBracket+1);
          retVal = retVal.insert(startBracket,objIndex);
          retVal = new StringBuffer(createSubObjects(retVal.toString())); // collapse any other () in the string
        }
      }
    }
    return retVal.toString();
  }

  private void addObjToMap(int objIndex, int x, int y) {
    boolean goAhead = true;
    int maxIndex = -1; // max object with same x,y
    int newIndex = objIndex; // object moved to this index

    if ((objIndex  < 0) || (x < 0) || (y < 0)) goAhead = false; // error Checking
    for (int i=0; i<obj.length; i++) {
      if ((obj[i].x == x) && (obj[i].y == y)) {
        maxIndex = i;
      }
    }
    // object with same x,y exists higher than current objIndex
    // move the current object to a new Index that is higher than the other
    if ((maxIndex >= 0) && (maxIndex > objIndex)) {
      newIndex = getFreeObj(maxIndex);
      if (newIndex < 0) newIndex = objIndex; // out of space in obj array (so don't move it)
      else { // Move the object
        // copy object attributes to new index
        String objString = obj[objIndex].toString();
        obj[newIndex].setAttributes(objString);
       // Search for old references to this object and change them to the new Index
       // searching obj
        for (int o=0; o<obj.length; o++) {
          if (obj[o].maxContains > 0) {
            for (int c=0; c<obj[o].maxContains; c++) {
              if (obj[o].contains[c] == objIndex) {
                obj[o].contains[c] = newIndex;
              }
            }
          }
        }
        // searching creatures
        for (int c=0; c<creatures.length; c++) {
          for (int i=0; i<Creature.maxObj; i++) {
            if (creatures[c].inventory[i] == objIndex) {
              creatures[c].inventory[i] = newIndex;
            }
          }
        }
        // searching players
        for (int p=0; p<players.length; p++) {
          for (int i=0; i<Creature.maxObj; i++) {
            if (players[p].inventory[i] == objIndex) {
              players[p].inventory[i] = newIndex;
            }
          }
        }
        // this is done after the searching, so that nobody else can use
        // the object while I'm changing the references
        obj[objIndex].kill(); // remove the old Object
      }
    }
    if (goAhead) {
      obj[newIndex].x = x;
      obj[newIndex].y = y;
      obj[newIndex].onMap = true;
    }
  }

  
  
  // ===========================  Constructor ====================================
  
  public worldClass() {
   System.out.println("Starting to Create the World!");
   
   // -------------- Read in the Map Defs from a file ------------------------
   Hashtable mapTypes = new Hashtable(); // Temporarily hold all the map Definitions
   System.out.println("Reading Map Definitions from "+Constants.mapDefsFile+"...");
   try {
     FileReader mapDefs = new FileReader(new File(Constants.mapDefsFile));
     BufferedReader in = new BufferedReader(mapDefs);
     String thisLine = "INIT"; // just to make sure it enters the loop
     while (thisLine != null) {
       thisLine = in.readLine();
       // Parse the string of the form (fileChar,Img,name,move)
       if (thisLine != null) {
         int firstComma = thisLine.indexOf(',');
         int secondComma = thisLine.indexOf(',',(firstComma+1));
         int thirdComma = thisLine.indexOf(',',(secondComma+1));
         if ((firstComma > 0) && (secondComma > 0) && (thirdComma > 0)) {
           char fileChar = thisLine.charAt(1);                           // from ( to , 
           String img = thisLine.substring(firstComma+1,secondComma);    // from , to ,
           String name = thisLine.substring(secondComma+1,thirdComma);    // from , to ,
           int move = Integer.valueOf(thisLine.substring(thirdComma+1,(thisLine.length()-1))).intValue(); // from second , to )
           mapTypes.put(String.valueOf(fileChar), new mapElement(img, name, fileChar, move));
         } else System.out.println("Error reading Map Defs File error in line :"+thisLine);
       }
     }//while
     in.close();
     mapDefs.close();
   } catch (IOException e) {
      System.err.println("Could not open or read file "+Constants.mapDefsFile);
      System.exit(-1);
   }

  // --------- Read and define the World Map using the Definitions set up earlier ---------
   System.out.println("Reading map from "+Constants.worldMapFile+"...");
   try {
     FileReader theMap = new FileReader(new File(Constants.worldMapFile));
     BufferedReader in = new BufferedReader(theMap);
     String mapLine = "NOTHING"; // just to make sure it enters the loop
     int y = 0;
     char thisChar;
     while (mapLine != null) {
       mapLine = in.readLine();
       if (mapLine != null) {
         // run through the string and create each map Element according to the mapType
         for (int x=0; x< mapLine.length(); x++){
           thisChar = mapLine.charAt(x);
           if ((mapTypes.containsKey(String.valueOf(thisChar))) && // If the map Element is found
               (x < Constants.mapMaxX) && (y<Constants.mapMaxY)){ // If the map does not exceed max Map sizes
             map[x][y] = (mapElement)mapTypes.get(String.valueOf(thisChar));
           }
         } // for each x
         y += 1;
       } // if
     }//while
     in.close();
     theMap.close();
   } catch (IOException e) {
      System.err.println("Could not open and read file "+Constants.worldMapFile);
      System.exit(-1);
   }

   
   // ------------- Load the World Objects -------------------
   for (int i=0; i < Constants.maxObj; i++) {
     obj[i] = new Obj(); // create all the objects as NULLOBJ
   }
   System.out.println("Loading the world Objects from the file "+Constants.worldObjFile+"..");
   try {
     FileReader theObj = new FileReader(new File(Constants.worldObjFile));
     BufferedReader in = new BufferedReader(theObj);
     String theLine = "NOTHING"; // just to make sure it enters the loop
     while (theLine != null) {
       theLine = in.readLine();
       if (theLine != null) {
         // Sets all the attributes according to the info in the string
         String objStr = createSubObjects("("+theLine+")"); // the () are because createSubObjects expecst a list of objects ((obj),(obj))
       } // if
     }//while
     in.close();
     theObj.close();
   } catch (IOException e) {
      System.err.println("Could not open and read file "+Constants.worldObjFile);
      System.exit(-1);
   }

    // Create all the players to generic players
    for (int i=0; i<Constants.maxPlayers; i++){
      players[i] = new Creature();
    }  


    // ----------- Load the world creatures ------------------
    // Create all the creatures as non-existing, dead things
    for (int i=0; i<Constants.maxCreatures; i++){
      creatures[i] = new Creature();
    }  
   System.out.println("Loading the world Creatures from the file "+Constants.worldCreaturesFile+"..");
   try {
     FileReader theFile = new FileReader(new File(Constants.worldCreaturesFile));
     BufferedReader in = new BufferedReader(theFile);
     String fileLine = "NOTHING"; // just to make sure it enters the loop
     while (fileLine != null) {
       fileLine = in.readLine();
       if (fileLine != null) {
        createCreature(fileLine);
       } // if
     }//while
     in.close();
     theFile.close();
   } catch (IOException e) {
      System.err.println("Could not open and read file "+Constants.worldCreaturesFile);
      System.exit(-1);
   }
    
    // Start the AI Timer (should be the last thing done in initialization!)
    timer = new Timer(true);
    timer.scheduleAtFixedRate(AITimerTask,0,100);
    
   System.out.println("Finished Creating World!\n");
 } // constructor worldClass
 
 
 // ============================= INTERFACE METHODS ============================================
 
 // --------------- PLAYER Joining the world. --------------------------------
 
  // helper for players joining the world
  private void loadPlayerObjectsFromFile(int playerIndex, String fileName) {
    // open File and read in Equip and Inventory Objects
    try {
      BufferedReader in = new BufferedReader(new FileReader(fileName));
      String theLine = "";
      String indexStr = "";
      
      // The first lines are the player attributes (processed in the Creature class)
      for (int i=0; i< Creature.numAttributes; i++) { 
        theLine = in.readLine();
      }  
      
      // erase the equipped Objects for this player (previous player info)
      for (int i=0; i<Creature.maxEquip; i++) players[playerIndex].equip[i] = -1;
      
      // read in the equipped objects
      for (int i=0; i<Creature.maxEquip; i++) {
        theLine = in.readLine().toLowerCase().trim();
        indexStr = createSubObjects("("+theLine+")"); // the () are so that the equip are in the form ((obj..))
        indexStr = indexStr.substring(1,indexStr.length()-1); // remove the ()
        int thisIndex = -1;
        try { thisIndex = Integer.valueOf(indexStr).intValue(); }
        catch (NumberFormatException e) { }; // do nothing with errors
        players[playerIndex].equip[i] = thisIndex;
      }


      // Erase the old Inventory (Previous player)
      for (int i=0; i<Creature.maxObj; i++) players[playerIndex].inventory[i] = -1;
      
      // -----  Read in the Inventory from the file --------------
      theLine = in.readLine().toLowerCase().trim();
      indexStr = createSubObjects("("+theLine+")");
      indexStr = indexStr.substring(1,indexStr.length()-1); // remove the ()
      int start = 0;
      int end = indexStr.indexOf(',');
      String intStr = "";
      int loopCounter = 0;
      while ((end >= 0) && (loopCounter < Creature.maxObj)) {
        intStr = indexStr.substring(start,end);
        int objIndex = -1;
        try { objIndex = Integer.valueOf(intStr).intValue();} 
        catch (NumberFormatException e) {}
        players[playerIndex].inventory[loopCounter] = objIndex;
        loopCounter += 1;
        start = end+1;
        end = indexStr.indexOf(',',start);
      }
      
      in.close();
    } catch (IOException e) {
      System.err.println("Error reading file "+fileName);
    }
  }

 // -100 = password doesn't match
 // -101 = user does not exist
 // -102 = server full, cannot accept connections
 // -103 = Player already in game
 public int getPlayerIndex(String playerName, String playerPassWord){
   int retVal = -1;
   String inUserName = playerName.toLowerCase().trim(); // case insensitive username
   String inPassWord = playerPassWord.trim();           // case sensitive password
   int playerIndex = -1;

   for (int i=0; i<players.length; i++) {
     if (players[i].exists == false) {
       if (playerIndex == -1) { // only if it's the first one found!
         playerIndex = i;
         players[i].exists = true; // reserve this player while looking up username/password
       }
     } else if (players[i].userName.equals(inUserName)) { // player is already in game (and exists)
       retVal = -103;
     }
   }
   if (playerIndex == -1) { // no free player found in loop
     retVal = -102; // server is full
   }
   
   if ((retVal == -1) && (playerIndex >= 0))  { // No errors detected yet
    // find the file that matches this username
    String fileName = Constants.playerInfoDir+inUserName+".dat";
    File playerFile = new File(fileName);
    if (!playerFile.canRead()) {
      retVal = -101; // User does not exist (INVALIDUSERNAME)
    } else {
      // Check the Password in this file
      String inLine = "";
      try {
        BufferedReader in = new BufferedReader(new FileReader(playerFile));
        for (int i=0; i<3;i++) {// password is the 3rd line
          inLine = in.readLine().trim();
        }
        in.close();
      } catch (IOException e) {
        System.err.println("Error reading file "+fileName);
      }
      if (!inLine.equals(inPassWord)) {
        retVal = -100; // Doesn't match password
      } else { // Password Matches, so load the player's info
        retVal = playerIndex; 
        players[playerIndex].loadPlayerAttributesFromFile(fileName);
        loadPlayerObjectsFromFile(playerIndex, fileName);
      }
    }
   }
   // Free the reserved player on failed login
   if ((retVal < 0) && (playerIndex >=0)) {
     players[playerIndex].exists = false;
   } else { // Player is alive and ready to go!
     players[playerIndex].alive = true;
   }
   return retVal;
 }
 
  // Create a new user 
  public int createUser(String playerName, String playerPassWord) {
    int retVal = -1;
    String inUserName = playerName.toLowerCase().trim(); // case insensitive username
    String inPassWord = playerPassWord.trim();           // case sensitive password
    int playerIndex = -1;

    for (int i=0; i< players.length; i++) {
      if (players[i].exists == false) {
        if (playerIndex == -1) {
          playerIndex = i;
          players[i].exists = true; // reserve this player now
        }
      } else if (players[i].userName.equals(inUserName)) { // player is already in game
        retVal = -103;
      }
    }
    if (playerIndex == -1) {
     retVal = -102; // server full 
    }
    
   if ((retVal == -1) && (playerIndex >=0)) { // no errors found so far
      // Create new default User
      retVal = playerIndex;
      players[playerIndex].loadPlayerAttributesFromFile(Constants.playerInfoDir+"newplayer.dat");
      loadPlayerObjectsFromFile(playerIndex, Constants.playerInfoDir+"newplayer.dat");
      players[playerIndex].userName = playerName;
      players[playerIndex].passWord = playerPassWord;
      players[playerIndex].clan = playerName;
      players[playerIndex].alive = true;
    }
    
    if ((retVal < 0) && (playerIndex >= 0)) {
      players[playerIndex].exists = false; // free the reserved Player
    } else { // Player is alive and ready to go!
     players[playerIndex].alive = true;
   }
    return retVal;
  }
 

  // Expands a single Object's contains field (recurses as necessary)
  // finds object indexes in the format of sub brackets contains=(*) and replaces them with object strings
  // eg (x=10,y=20,contains=(6,7,)) is replaced with (x=10,y=20,contains=((name=sword,img=sword),(name=gem,img=diamond),)
  private String expandContains(String inStr) {
    StringBuffer retVal = new StringBuffer(inStr);
    String indexList;
    String objList = "";
    int startBracket;
    int endBracket;
    int objIndex = -1;
    startBracket = inStr.indexOf("contains");
    startBracket = inStr.indexOf("=",startBracket);
    startBracket = inStr.indexOf('(',startBracket); // first ( after contains...=
    endBracket = closeBracket(inStr, startBracket);
    if ((startBracket > 0) && (endBracket > 0)) {
       indexList = retVal.substring(startBracket,endBracket+1); // +1 to include the final )
       int start = 1;
       int end = indexList.indexOf(',');
       while (end >=0) {
         String objIndexStr = indexList.substring(start,end);
         try { objIndex = Integer.valueOf(objIndexStr).intValue();} catch (NumberFormatException e) {objIndex = -1;}
         String thisObjStr = "";
         if (objIndex >= 0) thisObjStr = obj[objIndex].toString();
         objList += expandContains(thisObjStr)+",";
         start = end+1;
         end = indexList.indexOf(',',start);
       }
    retVal = retVal.delete(startBracket+1,endBracket); // remove the indexes (leave the brackets)
    retVal = retVal.insert(startBracket+1,objList);  // insert the objects
    }
    return retVal.toString();
  }


// --------------------------------- CREATE CREATURE ----------------------------------------------------
 // Returns and reserves the next free index in creatures
 // If you do not use the index, please release it (set exists=false) manually...
  public int getFreeCreatureIndex() {
    int retVal = -1;
    for (int i=0; i<Constants.maxCreatures; i++) {
     if (creatures[i].exists == false) {
       creatures[i].exists = true; // reserved for the calling function to use
       retVal = i;
       break;
     }
    }
    return retVal;
  }
 
  public void createCreature(String inStr) {
    String creatureString = inStr;
    int creatureIndex = getFreeCreatureIndex();
    if (creatureIndex >= 0) {
      // creates the objects and replaces them with index numbers in the string
      creatureString = createSubObjects(creatureString);
      // Sets all the attributes according to the info in the string
      creatures[creatureIndex].setAttributes(creatureString);
    }
  }


 // ----------------------------------- INVENTORY / OBJ ---------------------------------------------

 public void createObj (String inStr) {
   int objIndex = getFreeObj();
   if (objIndex >= 0) {
     String objString = createSingleSubObject(inStr);
     obj[objIndex].setAttributes(objString);
   }
 }

 // Gets a string with all the objects that are currently equipped.
 // the order here is important, as they will be imported in the same order
 // format is (obj1)\n\n(obj3)\n... where obj2 is an empy equip slot
  private String getPlayerObjEquipped(int playerIndex) {
    String retVal = "";
    int thisIndex; 
    for (int i=0; i< Creature.maxEquip; i++) {
      thisIndex = players[playerIndex].equip[i];
      if (thisIndex >= 0) {
        retVal += expandContains(obj[thisIndex].toString());
        obj[thisIndex].kill();
      }
      retVal +="\n";
    }
    return retVal;
  }

 // returns a string with all the inventory objects for the player
 // the format is (obj1),(obj2),...(objn),\n  
  private String getPlayerObjInventory(int playerIndex) {
    String retVal = "";
    for (int i=0; i< Creature.maxObj; i++) {
      if (players[playerIndex].inventory[i] >= 0) {
        retVal += expandContains(obj[players[playerIndex].inventory[i]].toString());
      }
      retVal += ",";
    }
    retVal += "\n";
    return retVal;
  }


 // ------------------------- PLAYER LEAVING WORLD ------------------------------------

 // A player leaving the World
 public void removePlayer(int playerIndex) {
   players[playerIndex].exists = false;

   // Update the player's character info file
   String fileName = Constants.playerInfoDir+"/"+players[playerIndex].userName+".dat";
   try {
     File playerFile = new File(fileName);
     playerFile.delete(); // Delete the file (if it exists)
     playerFile.createNewFile(); // create a new file
     if (playerFile.canWrite()) {
       FileWriter out = new FileWriter(playerFile);
       out.write(players[playerIndex].toString());
       out.write(getPlayerObjEquipped(playerIndex));
       out.write(getPlayerObjInventory(playerIndex));
       out.close();
     }
   } catch (IOException e) {
      System.err.println("File IO error writing to file "+fileName);
      System.exit(-1);
   }
 }
 
 // ------------------------- DISPLAY MAP --------------------------------------

 // Get the Map display String
 public String getDisplay(int playerIndex) {
   StringBuffer retString = new StringBuffer("SCRN");
   // player Position
   int startX = players[playerIndex].x - (int)(Constants.screenSizeX/2); 
   int startY = players[playerIndex].y - (int)(Constants.screenSizeY/2);
   if (startX < 0) startX = 0;
   if (startY < 0) startY = 0;
   if (startX > (Constants.mapMaxX-Constants.screenSizeX)) startX = Constants.mapMaxX - Constants.screenSizeX;
   if (startY > (Constants.mapMaxY-Constants.screenSizeY)) startY = Constants.mapMaxY - Constants.screenSizeY;
   
    for (int y = startY; ((y<(startY+Constants.screenSizeY))&&(y<map[players[playerIndex].x].length)); y++) {
      for (int x = startX; ((x<(startX+Constants.screenSizeX))&&(x<map.length)); x++) {
        retString.append(map[x][y].img+",");
      } //x
      retString.append('~'); // end of screen line terminator
    }// y
   return retString.toString();
 }
  
 // --------------------------- DISPLAY OBJ and CREATURES -----------------------------------------
 
 // Display the Object display String (includes creatures and players)
 public String getObj(int playerIndex) {
  String retVal = "";
  int minX = players[playerIndex].x - (Constants.screenSizeX/2);
  int minY = players[playerIndex].y - (Constants.screenSizeY/2);
  if (minX < 0) minX = 0;
  if (minY < 0) minY = 0;
  if (minX > (Constants.mapMaxX - Constants.screenSizeX)) minX = Constants.mapMaxX - Constants.screenSizeX;
  if (minY > (Constants.mapMaxY - Constants.screenSizeY)) minY = Constants.mapMaxY - Constants.screenSizeY;
  for (int i=0; i < Constants.maxObj; i++){
    if ((obj[i].onMap == true) && (obj[i].x >= 0) && (obj[i].y >= 0)) {
      retVal = retVal+obj[i].toDisplay(minX,minY);
    }
  }
  // add the Creatures to the object List
  for (int i=0; i<Constants.maxCreatures; i++) {
    if ((creatures[i].exists == true) && 
        (creatures[i].x >= minX) && (creatures[i].y >= minY) &&
        (creatures[i].x < minX+Constants.screenSizeX) && (creatures[i].y < (minY+Constants.screenSizeY))) {
      String imageName = creatures[i].img;
      if (!creatures[i].alive) imageName = creatures[i].imgDead;
      retVal = retVal+"("+(creatures[i].x-minX)+","+(creatures[i].y-minY)+","+imageName+")";
    } //if 
  } // for i
  // add the players to the object List
  for (int i=0; i<Constants.maxPlayers; i++) {
    if ((players[i].exists == true) && 
        (players[i].x >= minX) && (players[i].y >= minY) &&
        (players[i].x < minX+Constants.screenSizeX) && (players[i].y < (minY+Constants.screenSizeY))) {
      String imageName = players[i].img;
      if (!players[i].alive) imageName = players[i].imgDead;
      retVal = retVal+"("+(players[i].x-minX)+","+(players[i].y-minY)+","+imageName+")";
    } //if 
  } // for i
  return retVal;
 }  
 
 // ------------------------------- MOVE --------------------------------------
  
 // public function called by protocol (interface for doMove)
 public void movePlayer(int playerIndex, String inAction) {
  if (playerIndex >= 0) doMove(players[playerIndex], inAction);
 }
  
 // if Action MOVE was encountered
 public void doMove(Creature who, String inAction) {
   String action = inAction.toLowerCase().trim();
   int x = -1;
   int y = -1;
   boolean canMove = true;

   if ((who.moveDelay > 0) || (who.alive == false)) canMove = false;
   x = who.x;
   y = who.y;

   if      (action.equals("up"))    y -= 1; 
   else if (action.equals("down"))  y += 1; 
   else if (action.equals("left"))  x -= 1; 
   else if (action.equals("right")) x += 1; 

         
   // Check boundaries of the World Map
   if (y < 0) y = 0;
   if (y > Constants.mapMaxY-1) y = Constants.mapMaxY-1;
   if (x < 0) x = 0;
   if (x > Constants.mapMaxX-1) x = Constants.mapMaxX-1;
          
   // Check if the map terrain disallows movement there
   if (map[x][y].move == -1) canMove = false;


   if (canMove) {
     who.x = x;
     who.y = y;
     who.moveDelay += map[x][y].move; // terrain slowing down
     who.moveDelay += getEncumberance(who); // encumbered by weight restrictions
     who.moveDelay += who.moveDelayMax;
   } 
   
 }// method move
 

 
 // ------------------------------- Player Messaging -------------------

 // Leaves a message for a player or creature.  Returns true if the message
 // was successfully delivered
 public boolean leaveMessage (String senderType, int senderIndex, String toType, int toIndex, String message) {
  boolean retVal = false;
  if ((senderIndex < 0)||(toIndex<0)) return false; // error checking
  if (toType.equals("player")) {
    if (players[toIndex].addMessage(message)) {
      return true;
    } else return false;
  } else if (toType.equals("creature")) {
    if (senderType.equals("player")) {
      creatures[toIndex].ai.tellMessage(players[senderIndex].userName,message);
    } else if (senderType.equals("creature")) {
      creatures[toIndex].ai.tellMessage(creatures[senderIndex].userName+"-"+senderIndex,message);
    }
    return true;
  }
  return retVal;
 }
 
 // a player sends a message to a player or creature
 // the first two characters of whoMessage should be UN (userName) or 
 // XY(x,y position) or up, down, here etc... where strings
 // if UN the system will look for a player with that userName
 // if otherwise the system will look for a player or creature at the specified
 // position to send the message to
 public String tellPlayer(int playerIndex, String whoMessage) {
  String retVal = "Failed to Deliver Message: ";
  int tildaIndex = whoMessage.indexOf('~');
  if ((tildaIndex > 0) && (players[playerIndex].alive)) {
    String whoName = whoMessage.substring(2,tildaIndex).toLowerCase().trim();
    String where = whoMessage.substring(0,tildaIndex).trim();
    String message = whoMessage.substring(tildaIndex+1);
    boolean foundRecipient = false;

    if (whoMessage.substring(0,2).equals("UN")){ //Match UserName (only for players)
      // Find the player with a matching name
      for (int i=0; i<Constants.maxPlayers;i++) {
        if ((players[i].userName.toLowerCase().equals(whoName)) && (players[i].exists) && (players[i].alive)) {
          if (leaveMessage("player",playerIndex,"player",i,players[playerIndex].userName+" tells you : "+message)) retVal = "";
          else retVal = retVal + "Player did not accept message. Please try again later.";
          foundRecipient = true;
        }
      }
      if (!foundRecipient) retVal = retVal + "Could not find player "+whoName;
    } else  { // not a UN (userName) so parse it in the Where style
              //  UP, DOWN, LEFT, RIGHT, HERE, XY 
      int x = getWhereX(playerIndex, where);
      int y = getWhereY(playerIndex, where);
      for (int i=0; i<Constants.maxCreatures; i++) {
        if ((creatures[i].exists) && (creatures[i].alive) &&
            (creatures[i].x == x) && (creatures[i].y==y)) {
           leaveMessage("player",playerIndex, "creature",i,message);
           foundRecipient = true;
           retVal = "";
           break; // only deliver message to one creature
        }
      }
      if (foundRecipient == false) {
        // Search for players at this location
        for (int i=0; i<Constants.maxPlayers;i++) {
          if ((players[i].exists) && (players[i].alive) &&
              (players[i].x == x) && (players[i].y == y)) {
            if (leaveMessage("player",playerIndex,"player",i,players[playerIndex].userName+" tells you : "+message)) retVal = "";
            else retVal = retVal + "Player did not accept message. Please try again later.";
            foundRecipient = true;
            break;
          }
        }
      }
      if (!foundRecipient) retVal = retVal + "Could not find creature or player at ("+x+","+y+")";
    }
  } else { // no ~ found in whoMessage or player is dead
    if (players[playerIndex].alive == false) retVal += "You are not alive";
    else retVal += "Badly formatted message or user.  Please try again.";
  }
  return retVal;
 } // tellPlayer

 public String getPlayerMessage(int playerIndex) {
   return players[playerIndex].getMessage();
 }  

 public String getPlayerName(int playerIndex) {
   return players[playerIndex].userName;
 } 
 
 // Gets the players X,Y position
 public String getPlayerXY(int playerIndex) {
  return "You are at ("+players[playerIndex].x+","+players[playerIndex].y+")";
 }
 
 
 // ------------------------ XY Conversion Methods --------------------------------------
 
 // Local XY into Global XY
  private int getGlobalX(int playerIndex, int localX) {
       // global position of top left corner of screen
       int globalX = (players[playerIndex].x - (Constants.screenSizeX/2)); 
       if (globalX < 0) globalX = 0;
       int maxX = (Constants.mapMaxX) - Constants.screenSizeX;
       if (globalX > maxX) globalX = maxX;
       return (globalX + localX);
  }

  private int getGlobalY(int playerIndex, int localY) {
       int globalY = (players[playerIndex].y - (Constants.screenSizeY/2));
       if (globalY < 0) globalY = 0;
       int maxY = (Constants.mapMaxY) - Constants.screenSizeY;
       if (globalY > maxY) globalY = maxY;
       return (globalY + localY);
  }

  // where String should be (UP,DOWN,LEFT,RIGHT,HERE,XYnum,num) into global XY
  private int getWhereX(int playerIndex, String inStr) {
    String location = inStr.toLowerCase().trim();
    int x = -1;
    if (location.equals("here")) {
      x = players[playerIndex].x;
    } else if (location.equals("up")) {
      x = players[playerIndex].x;
    } else if (location.equals("down")) {
      x = players[playerIndex].x;
    } else if (location.equals("left")) {
      x = players[playerIndex].x-1;
    } else if (location.equals("right")) {
      x = players[playerIndex].x+1;
    } else if (location.substring(0,2).equals("xy")) {
     int commaIndex = location.indexOf(',');
     if (commaIndex > 0) {
       int localX = -1;
       try {localX = Integer.valueOf(location.substring(2,commaIndex)).intValue(); } catch (NumberFormatException e) {}
       x = getGlobalX(playerIndex, localX);
     }
    }
    return x;
  }

  private int getWhereY(int playerIndex, String inStr) {
    String location = inStr.toLowerCase().trim();
    int y = -1;
    if (location.equals("here")) {
      y = players[playerIndex].y;
    } else if (location.equals("up")) {
      y = players[playerIndex].y-1;
    } else if (location.equals("down")) {
      y = players[playerIndex].y+1;
    } else if (location.equals("left")) {
      y = players[playerIndex].y;
    } else if (location.equals("right")) {
      y = players[playerIndex].y;
    } else if (location.substring(0,2).equals("xy")) {
     int commaIndex = location.indexOf(',');
     if (commaIndex > 0) {
       int localY =  -1;
       try {localY = Integer.valueOf(location.substring(commaIndex+1,location.length())).intValue();} catch (NumberFormatException e) {}
       y = getGlobalY(playerIndex, localY);
     }
    }
    return y;
  }
 
 
  
 // ---------------------------------- Use Obj ---------------------------------------------------------------

 private void doSystemEffects(EffectModifier[] effects,Creature caster, Creature target) {
   for (int i=0; i<effects.length; i++) {
     if      (effects[i].var.equals("createcreature")) createCreature(effects[i].val);
     else if (effects[i].var.equals("createobj")) createObj(effects[i].val);
       
    // TODO: apply the damage effects bludgeon, pierce...
   }
 }


 private String useObj(String casterType, int casterIndex, int objIndex) {
   if ((objIndex >=0) && obj[objIndex].exists && obj[objIndex].use.exists && 
       (casterIndex >= 0)) {
     if (obj[objIndex].use.targetType.equals("self")) {
       useObj(casterType, casterIndex, casterType, casterIndex, objIndex); // cast spell on self
       return "";
     } else return "TARG"+objIndex;
   } else return "";
 }

 // Uses an object with target Type = creature (either a player or a creature)
 private void useObj(String casterType, int casterIndex, String targetType, int targetIndex, int objIndex) {
   if ((objIndex >=0) && obj[objIndex].exists && obj[objIndex].use.exists && 
       (casterIndex >= 0) && (targetIndex >= 0) &&
       (casterType.equals("creature")||casterType.equals("player")) &&
       (targetType.equals("creature")||targetType.equals("player")||targetType.equals("obj")) &&
       ( (obj[objIndex].use.targetType.equals("self") && casterType.equals(targetType) && (casterIndex==targetIndex)) ||
         ((targetType.equals("creature") || targetType.equals("player")) && obj[objIndex].use.targetType.equals("creature")) ||
         (targetType.equals("obj") && obj[objIndex].use.targetType.equals("obj")) ||
         (obj[objIndex].use.targetType.equals("xy") && (targetType.equals("creature") || targetType.equals("player"))) )) { 
     Creature caster = new Creature();
     Creature target = new Creature();        
     if (casterType.equals("player")) caster = players[casterIndex];
     if (casterType.equals("creature")) caster = creatures[casterIndex];
     if (targetType.equals("player")) target = players[targetIndex];
     if (targetType.equals("creature")) target = creatures[targetIndex];
     obj[objIndex].use.cType = casterType;
     obj[objIndex].use.tType = targetType;
     obj[objIndex].use.cIndex = casterIndex;
     obj[objIndex].use.tIndex = targetIndex;
     EffectModifier[] effects = obj[objIndex].use.cast(caster,target,"cast"); // first time casting... (system will continue and dispell the spell)
     doSystemEffects(effects,caster,target);
   }
 }
 
 private String useObjXY(String casterType, int casterIndex, int x, int y, int objIndex) {
   boolean foundTarget = false;
   if ((objIndex >=0) && obj[objIndex].exists && obj[objIndex].use.exists && 
       (casterIndex >= 0) && (x >= 0) && (y >= 0) &&
       (casterType.equals("creature")||casterType.equals("player")) ) {
     Creature caster = new Creature();
     Creature target = new Creature();        
     if (casterType.equals("player")) caster = players[casterIndex];
     if (casterType.equals("creature")) caster = creatures[casterIndex];

     // Check that the spell target is within range of the caster
     if (!inRange(caster.x,caster.y,x,y, obj[objIndex].use.range)) return "TEXTOut of Range!";

     // Check the spell targetType
     if (obj[objIndex].use.targetType.equals("creature")) {
       int targetIndex = getPlayerIndex(x,y); // Check to see if there are any players at this location
       String targetType = "player"; 
       if (targetIndex < 0) {
         targetIndex = getCreatureIndex(x,y); // or creatures here
         targetType = "creature";
       }
       if (targetIndex >= 0) { // if a target was found
         useObj(casterType,casterIndex,targetType,targetIndex,objIndex);
         foundTarget = true;
       }
     } else if (obj[objIndex].use.targetType.equals("xy")) {
       // Look up all creatures and players at this location (and cast the spell on them)
       for (int i=0; i< Constants.maxCreatures; i++) {
         if (creatures[i].exists && creatures[i].alive &&  // if there is a player 
             inRange(creatures[i].x, creatures[i].y, x, y, obj[objIndex].use.areaEffect) ) {// in range of areaEffect of spell
           useObj(casterType,casterIndex,"creature",i,objIndex);
           foundTarget = true;
           if (obj[objIndex].use.areaEffect < 1) break; // if not a mass spell (only on the first target at XY)
         }
       }
       for (int i=0; i< Constants.maxPlayers; i++) {
         if (players[i].exists && players[i].alive &&  // if there is a player 
             inRange(players[i].x, players[i].y, x, y, obj[objIndex].use.areaEffect) ) {// in range of areaEffect of spell
           useObj(casterType,casterIndex,"player",i,objIndex);
           foundTarget = true;
           if (obj[objIndex].use.areaEffect < 1) break; // if not a mass spell (only on the first target at XY)
         }
       }

     } else if (obj[objIndex].use.targetType.equals("obj")) {
       // TODO: look up the obj at this location
     }
   }
   if (!foundTarget) return ("TEXTNo target found!");
   else return "NONE";
 }
 


 // PLAYER Function for using an object that requires a target.
 // inStr is of the form ObjIndex~Location
 public String parseTarget(int playerIndex, String inStr) {
  String retVal = "";
  int tildaIndex = inStr.indexOf('~');
  if (tildaIndex > 0) {
    int objIndex = -1;
    try {objIndex = Integer.valueOf(inStr.substring(0,tildaIndex)).intValue();} catch (NumberFormatException e) {}
    if (objIndex >= 0) {
       String location = inStr.substring(tildaIndex+1,inStr.length());
       if ((location.length() > 3) && (location.substring(0,3).equals("MAP"))) location = location.substring(3); // cut off the word MAP (may be other types later)
       int x = getWhereX(playerIndex, location);
       int y = getWhereY(playerIndex, location);
       if ((x >= 0) && (y >=0)) retVal = useObjXY("player",playerIndex,x,y,objIndex);
    }
  }
  return retVal;
 }


 // --------------------------- GET BAGI (from BAG or INV) imagelist of bag contents -----------------  
  
  // returns a string of the form
  // BAGIobjName~maxContains~index~image,image,...
  public String getBagString(int objIndex) {
    String retVal = "";
    retVal = obj[objIndex].name+"~"+
             obj[objIndex].maxContains+"~"+
             objIndex+"~";
    for (int i=0; i< obj[objIndex].maxContains; i++) {
      int objI = obj[objIndex].contains[i];
      if (objI >= 0) {
        retVal += obj[objI].imageName+",";
      } else retVal += "noimage,";
    }             
    return retVal;                    
  }

  // Returns the object index of the bag, inStr is of the form
  // INVindex
  // BAGobjindex,localIndex
  public int getBagObjIndex(int playerIndex, String inStr) {
    int retVal = -1;
    if (inStr.length() < 3) return -1; // error checking (Must be 3 char command type)
    String type = inStr.substring(0,3);
    String location = inStr.substring(3);
    if         (type.equals("INV")) { // the location is the Inventory index
      int locIndex = -1;
      int objIndex = -1;
      try {locIndex = Integer.valueOf(location).intValue();} catch (NumberFormatException e) {}
      if ((playerIndex >= 0) && (locIndex >= 0)) {
        objIndex = players[playerIndex].inventory[locIndex];
      }
      return objIndex;
    } else if (type.equals("BAG")) {
      int bagIndex = -1;
      int localIndex = -1;
      int comma = location.indexOf(',');
      int objIndex = -1;
      if ((comma > 0) && (location.length() > comma+1)) {
        try {
          bagIndex = Integer.valueOf(location.substring(0,comma)).intValue();
          localIndex = Integer.valueOf(location.substring(comma+1)).intValue();
        } catch (NumberFormatException e) {}
      }
      if ((playerIndex >= 0) && (bagIndex >= 0) && (localIndex >= 0) &&
          (bagIndex < Constants.maxObj) && (obj[bagIndex].maxContains > localIndex)) {
        objIndex = obj[bagIndex].contains[localIndex];
      }
      return objIndex;
    }
    return retVal;
  }

  // gets the BAGI String details from a bag index (location is a bagIndex,localIndex)
  public String getBagBagDesc(int playerIndex, String location) {
    String retVal = "";
    int bagIndex = -1;
    int localIndex = -1;
    int comma = location.indexOf(',');
    int objIndex = -1;
    if ((comma > 0) && (location.length() > comma+1)) {
      try {
        bagIndex = Integer.valueOf(location.substring(0,comma)).intValue();
        localIndex = Integer.valueOf(location.substring(comma+1)).intValue();
      } catch (NumberFormatException e) {}
    }
    if ((playerIndex >= 0) && (bagIndex >= 0) && (localIndex >= 0) &&
        (bagIndex < Constants.maxObj) && (obj[bagIndex].maxContains > localIndex)) {
      objIndex = obj[bagIndex].contains[localIndex];
    }
    if ((objIndex >= 0) && (objIndex < Constants.maxObj)) {
      if (obj[objIndex].use.exists) {
        retVal = useObj("player",playerIndex,objIndex);
      }
      if (obj[objIndex].type.equals(Obj.CONTAINER)) {
        if (retVal.length() == 0) retVal = "BAGI"+getBagString(objIndex);
        else retVal += Constants.commandSeperator+"BAGI"+getBagString(objIndex);
      }
    }
    return retVal;
  }

  // gets the BAGI String from Inventory (location is inv index)
  public String getInvBagDesc(int playerIndex, String location) {
    int locIndex = -1;
    int objIndex = -1;
    try {locIndex = Integer.valueOf(location).intValue();} catch (NumberFormatException e) {}
    String retVal = "";
    if ((playerIndex >= 0) && (locIndex >= 0)) {
      objIndex = players[playerIndex].inventory[locIndex];
    }
    if (objIndex >= 0) {
      if (obj[objIndex].use.exists) {
        retVal = useObj("player",playerIndex,objIndex);
      }
      if (obj[objIndex].type.equals(Obj.CONTAINER)) {
        if (retVal.length() == 0) retVal = "BAGI"+getBagString(objIndex);
        else retVal += Constants.commandSeperator+"BAGI"+getBagString(objIndex);
      }
    }
    return retVal;
  }


 // ------------------------ Get BAG and INV INFO (Desc) Methods ----------------------------------------------

 // describes an item in inventory (location is inv index)
  public String getInvDesc(int playerIndex, String location) {
    int locIndex = -1;
    int objIndex = -1;
    String retVal = "";
    try { locIndex = Integer.valueOf(location).intValue();} catch (NumberFormatException e) {}
    if ((playerIndex >= 0) && (locIndex >= 0)) {
      objIndex = players[playerIndex].inventory[locIndex];
    }
    if (objIndex >= 0) {
      retVal = obj[objIndex].describe();
    }
    return retVal;
  }

 // Describes an item in an equip slot (location is equip[] index)
  public String getEqpDesc(int playerIndex, String location) {
    int locIndex = -1; 
    int objIndex = -1;
    String retVal = "";
    try { locIndex = Integer.valueOf(location).intValue();} catch (NumberFormatException e) {}
    if ((playerIndex >= 0) && (locIndex >= 0)) {
      objIndex = players[playerIndex].equip[locIndex];
    }
    if (objIndex >= 0) {
      retVal = obj[objIndex].describe();
    }
    return retVal;
  }


 // describes an item in a bag (location is bagIndex,insideBagIndex) (what position in the bag)
  public String getBagDesc(int playerIndex, String location) {
    String retVal = "";
    int finalIndex = -1;
    int comma = location.indexOf(',');
    int bagIndex = -1;
    int insideBagIndex = -1;
    if (comma > 0) {
      try { 
        bagIndex = Integer.valueOf(location.substring(0,comma)).intValue();
        insideBagIndex = Integer.valueOf(location.substring(comma+1)).intValue();
      } catch (NumberFormatException e) {}
      if ((playerIndex >= 0) &&  (playerIndex < Constants.maxPlayers) && // the player is valid
          (bagIndex >= 0) && (bagIndex < Constants.maxObj) && // the bagIndex is valid
          (insideBagIndex >= 0) && (insideBagIndex < obj[bagIndex].maxContains)) { // the inside bagIndex is valid
        finalIndex = obj[bagIndex].contains[insideBagIndex];
      }
      if ((finalIndex >= 0) && (finalIndex < Constants.maxObj)) { // finalIndex is valid
        retVal = obj[finalIndex].describe();
      }
    }
    return retVal;
  }

 
  public String getMapDesc(int playerIndex, String location) {
    String retVal = null;
    int x = -1;
    int y = -1;
    boolean matched = false;
    
     x = getWhereX(playerIndex,location);
     y = getWhereY(playerIndex,location);
        
    // check to see if there is a player here 
    for (int i=0; i< players.length ; i++) {
      if ((players[i].x == x) && (players[i].y == y) &&
          (players[i].exists == true)) {
        matched = true;
        retVal = players[i].describe();
      }
    }
    // check to see if there is a creature here
    if (!matched) {
      for (int i=0; i< creatures.length ; i++) {
        if ((creatures[i].x == x) && (creatures[i].y == y) &&
            (creatures[i].exists == true)) {
          matched = true;
          retVal = creatures[i].describe();
        }
      }
    }
    if (!matched) {
      // check to see if there is an object here
      for (int i=0; i< obj.length ; i++) {
        if ((obj[i].x == x) && (obj[i].y == y) &&
            (!obj[i].type.equals(Obj.NULLOBJ))) {
          matched = true;
          retVal = obj[i].describe();
        }
      }
    }
    // get the map description if nothing else exists here
    if (!matched) {
     retVal = map[x][y].describe();
    }
    
    if (retVal == null) retVal = "Void~noImage~This is the ethereal void, ~there is absolutely nothing here~";
    return retVal;
  }


  // ---------------------- GET STATS / WEIGHT Calcualations -----------------------------------------------

  private double getObjWeight(int objIndex) {
    double retVal = 0;
    if (objIndex >= 0) {
      retVal = obj[objIndex].weight; // base weight of object
      for (int i=0; i<obj[objIndex].maxContains; i++) {
        if (obj[objIndex].contains[i] >= 0) retVal += getObjWeight(obj[objIndex].contains[i]);
      }
    }
    return retVal;
  }

  private double getBaseCreatureWeight(Creature who) {
    double retVal = 0;
    retVal += who.strength*4;
    retVal += who.constitution;
    return retVal; 
  }

  private double getWeight(Creature who) {
    double retVal = 0;
    retVal = getBaseCreatureWeight(who);
    // Equipped objects
    for (int i=0; i<Creature.maxEquip; i++) {
      if (who.equip[i] >= 0)  retVal += getObjWeight(who.equip[i]);
    }
    // Inventory objects 
    for (int i=0;  i<Creature.maxObj; i++) {
      if (who.inventory[i] >= 0) retVal += getObjWeight(who.inventory[i]);
    }
    return retVal;
  }
  
  private double getWeightMax(Creature who) {
   return (who.strength*10)+(who.constitution*2);
  }
  
  private int getEncumberance(Creature who) {
    double weight = getWeight(who);
    double weightMax = getWeightMax(who);
    if (weight > weightMax) {
      double diff = weight - weightMax;
      diff = (diff / 5);
      return (int)Math.floor(diff);
    } else return 0;
  }
  
  // Gets the Stats for a player
  public String getPlayerStats(int playerIndex) {
    String retVal = "";
    if (playerIndex > -1) {
      retVal = players[playerIndex].getStats(); 
      double weight = getWeight(players[playerIndex]);
      double weightMax = getWeightMax(players[playerIndex]);
      int    encumberance = getEncumberance(players[playerIndex]);
      retVal += ","+weight+","+weightMax+","+encumberance;
    }
    return retVal;
  }
  
  // ------------------------- GET INVENTORY ------------------------------------------
  
  public String getPlayerInventory(int playerIndex) {
    String retVal = "";
    if (playerIndex > -1) {
      for (int i=0; i < Creature.maxObj; i++) {
        int objIndex = players[playerIndex].inventory[i];
        if (objIndex > -1) retVal += obj[objIndex].imageName;
        else retVal += "noimage";
        retVal += ",";
      }
    }
    return retVal;
  }


 // Called by the protocol to redisplay the Player inventory/equip/bag if necessary
  public boolean inventoryChanged(int playerIndex) {
    if (playerIndex >= 0)  {
      if (players[playerIndex].inventoryChanged) {
        players[playerIndex].inventoryChanged = false;
        return true;
      } else return false;
    } else return false;
  }


  
   // --------------- HELPER FUNCTIONS FOR DRAG AND DROP -----------------------
  // -------------- Is Contained -------------------

    // Returns true if objIndex is contained in bagIndex (at any level)
  public boolean isContained(int objIndex, int bagIndex ) {
    if (objIndex == bagIndex) return true; // every object contains itself
    if (objIndex < 0) return true; // everything contains a nothing
    if (bagIndex < 0) return false; // nothing is contained in nothing    
    if (obj[bagIndex].maxContains <= 0) return false; // if bag cannot contain items, the obj isn't in there!
    for (int i=0; i< obj[bagIndex].maxContains; i++) { // for every contained item
      if (obj[bagIndex].contains[i] >= 0) { // if the slot has an item in it
        if (obj[bagIndex].contains[i] == objIndex) return true;
        else if (isContained(objIndex, obj[bagIndex].contains[i])) return true;
      }
    }
    return false;
  }

 // ------------ Can Equip
 // returns true if the object at objIndex can be equiped at eqpIndex
 public boolean canEquip(int eqpIndex, int objIndex) {
  String type = obj[objIndex].type.toLowerCase();
  if ((eqpIndex == Creature.head) && (type.equals("helmet"))) return true;
  if ((eqpIndex == Creature.face) && (type.equals("mask"))) return true;
  if ((eqpIndex == Creature.neck) && (type.equals("collar")||type.equals("necklace"))) return true;
  if ((eqpIndex == Creature.body) && (type.equals("breastplate")||type.equals("shirt"))) return true;
  if ((eqpIndex == Creature.arms) && (type.equals("sleeve"))) return true;
  if ((eqpIndex == Creature.rightHand) && (type.equals("weapon") || type.equals("shield"))) return true;
  if ((eqpIndex == Creature.leftHand)  && (type.equals("weapon") || type.equals("shield"))) return true;
  if ((eqpIndex == Creature.rightFinger) && (type.equals("ring"))) return true;
  if ((eqpIndex == Creature.leftFinger)  && (type.equals("ring"))) return true;
  if ((eqpIndex == Creature.waist)  && (type.equals("belt")||type.equals("girdle"))) return true;
  if ((eqpIndex == Creature.legs) && (type.equals("legging"))) return true;
  if ((eqpIndex == Creature.feet) && (type.equals("shoe")||type.equals("boot"))) return true;
  return false;
 }
  
  // -------------------------------- DRAG AND DROP  ------------------------------------------

  public String drag(int playerIndex, String inStr) {
    String retVal = "";
    int splitter = inStr.indexOf("(~TO~)");
    if (splitter >0) { 
      String dragFrom = inStr.substring(0,splitter);
      String dragTo = inStr.substring(splitter+6,inStr.length());
      if ((dragFrom.length() > 3) && (dragTo.length() > 3)) { // If they are possibly valid drag Commands
        String from = dragFrom.substring(0,3);
        String fromLoc = dragFrom.substring(3);
        String to = dragTo.substring(0,3);
        String toLoc = dragTo.substring(3);
        int fromIndex = -1;
        int fromBagIndex = -1;
        int toIndex = -1;
        int toBagIndex = -1;
        int toX = -1;
        int toY = -1;
        boolean validFrom = false;
        boolean validTo = false;
        // Parse the from and To indexes
        try {
          // FROM
          if (from.equals("INV")) {
            fromIndex = Integer.valueOf(fromLoc).intValue();
          } else if (from.equals("BAG")) {
            int comma = fromLoc.indexOf(',');
            if (comma > 0) {
              fromBagIndex = Integer.valueOf(fromLoc.substring(0,comma)).intValue();
              fromIndex    = Integer.valueOf(fromLoc.substring(comma+1)).intValue();
            }
          } else if (from.equals("EQP")) {
            fromIndex = Integer.valueOf(fromLoc).intValue();
          }
          // TO
          if (to.equals("INV")) {
            toIndex   = Integer.valueOf(toLoc).intValue();
          } else if (to.equals("BAG")) {
            int comma = toLoc.indexOf(',');
            if (comma > 0) {
              toBagIndex = Integer.valueOf(toLoc.substring(0,comma)).intValue();
              toIndex    = Integer.valueOf(toLoc.substring(comma+1)).intValue();
            }
          } else if (to.equals("MAP")) {
            int comma = toLoc.indexOf(',');
            if (comma > 0) {
              int localX = Integer.valueOf(toLoc.substring(0,comma)).intValue();
              int localY = Integer.valueOf(toLoc.substring(comma+1)).intValue();
              toX = getGlobalX(playerIndex, localX);
              toY = getGlobalY(playerIndex, localY);
            }
          } else if (to.equals("EQP")) {
            toIndex   = Integer.valueOf(toLoc).intValue();
          }
        } catch (NumberFormatException e) {System.err.println("Drag Error: fromLoc="+fromLoc+" toLoc="+toLoc);}
        
        // Verify the casting validity
        if ((from.equals("INV")) && (fromIndex >= 0) && (fromIndex < Constants.maxObj) &&
             (players[playerIndex].inventory[fromIndex] >= 0)) { 
          validFrom = true;
        } else if ((from.equals("BAG")) && (fromBagIndex >=0) && (fromIndex >=0) &&
                   (fromBagIndex < Constants.maxObj) && (fromIndex < obj[fromBagIndex].maxContains) &&
                   (obj[fromBagIndex].contains[fromIndex] >= 0)) {
          if (findPlayerHoldingObj(fromBagIndex) != playerIndex) return "TEXTYou do not own the source.";
          validFrom = true;
        } else if ((from.equals("EQP")) && (fromIndex >= 0) && (fromIndex < Constants.maxObj) &&
                   (players[playerIndex].equip[fromIndex] >= 0)) {
          validFrom = true;
        }
        if (to.equals("INV") && (toIndex >= 0) && (toIndex < Constants.maxObj) &&
             (players[playerIndex].inventory[toIndex] < 0)) {
          validTo = true;
        } else if ((to.equals("BAG")) && (toBagIndex >=0) && (toIndex >=0) &&
                   (toBagIndex < Constants.maxObj) && (toIndex < obj[toBagIndex].maxContains) &&
                   (obj[toBagIndex].contains[toIndex] < 0)) {
          if (findPlayerHoldingObj(toBagIndex) != playerIndex) return "TEXTYou do not own the destination";
          validTo = true;
        } else if ((to.equals("MAP")) && (toX >= 0) && (toY >= 0) &&
                    (toX < Constants.mapMaxX) && (toY < Constants.mapMaxY)) {
          validTo = true;
        } else if (to.equals("EQP") && (toIndex >= 0) && (toIndex < Constants.maxObj) &&
                  (players[playerIndex].equip[toIndex] < 0)) {
          validTo = true;
        }
        
        if (players[playerIndex].alive == false) { // dead people can't move things
          validFrom = false;
          validTo = false;
        }
        
        // BEGIN Object moving (moving object index array pointers)
        if (validFrom && validTo) {
          if (from.equals("INV")) {
            if (to.equals("INV")) {
              players[playerIndex].inventory[toIndex] = players[playerIndex].inventory[fromIndex];
              players[playerIndex].inventory[fromIndex] = -1; // remove the item from the player's inventory
            } else if (to.equals("MAP")) {
              int objIndex = players[playerIndex].inventory[fromIndex];
              addObjToMap(objIndex, toX, toY);  // Add the object to the world Map
              players[playerIndex].inventory[fromIndex] = -1; // remove Object from Inventory
            } else if (to.equals("BAG")) { 
              if (!isContained(toBagIndex,players[playerIndex].inventory[fromIndex])) { // item is not being moved into itself!
                obj[toBagIndex].contains[toIndex] = players[playerIndex].inventory[fromIndex]; // put the item in the bag
                players[playerIndex].inventory[fromIndex] = -1; // remove the item from the player's inventory
              } else {retVal = "TEXTItem cannot contain itself!";}
            } else if (to.equals("EQP")) {
              if (canEquip(toIndex, players[playerIndex].inventory[fromIndex])) { 
                players[playerIndex].equip[toIndex] = players[playerIndex].inventory[fromIndex];
                players[playerIndex].inventory[fromIndex] = -1;
              }
            }//INV
          } else if (from.equals("BAG")) {
            if (to.equals("BAG")) { 
              obj[toBagIndex].contains[toIndex] = obj[fromBagIndex].contains[fromIndex]; // move the object to the new bag
              obj[fromBagIndex].contains[fromIndex] = -1;  // remove the object from the old bag
            } else if (to.equals("INV")) {
              players[playerIndex].inventory[toIndex] = obj[fromBagIndex].contains[fromIndex];
              obj[fromBagIndex].contains[fromIndex] = -1;
            } else if (to.equals("MAP")) {
              addObjToMap(obj[fromBagIndex].contains[fromIndex], toX, toY); // Add to the Map
              obj[fromBagIndex].contains[fromIndex] = -1; // remove from the bag
            } else if (to.equals("EQP")) {
              if (canEquip(toIndex,obj[fromBagIndex].contains[fromIndex])) {
                players[playerIndex].equip[toIndex] = obj[fromBagIndex].contains[fromIndex];
                obj[fromBagIndex].contains[fromIndex] = -1; // remove from the bag
              }
            } // bag
          } else if (from.equals("EQP")) {
            if (to.equals("EQP")) {
              if (canEquip(toIndex,players[playerIndex].equip[fromIndex])) {
                players[playerIndex].equip[toIndex] = players[playerIndex].equip[fromIndex];
                players[playerIndex].equip[fromIndex] = -1; // remove the item from the equip slot
              }
            } else if (to.equals("INV")) {
              players[playerIndex].inventory[toIndex]  = players[playerIndex].equip[fromIndex];
              players[playerIndex].equip[fromIndex] = -1; // remove the item from the equip slot
            } else if (to.equals("MAP")) {
              int objIndex = players[playerIndex].equip[fromIndex];
              addObjToMap(objIndex, toX, toY);  // Add the object to the world Map
              players[playerIndex].equip[fromIndex] = -1; // remove the item from the equip slot
            } else if (to.equals("BAG")) { 
              if (!isContained(toBagIndex,players[playerIndex].equip[fromIndex])) { // item is not being moved into itself!
                obj[toBagIndex].contains[toIndex] = players[playerIndex].equip[fromIndex]; // put the item in the bag
                players[playerIndex].equip[fromIndex] = -1; // remove the item from the equip slot
              } else {retVal = "TEXTItem cannot contain itself!";}
            }//Eqp
          }
          
          
          // refresh the Inventory if necessary (something moved from or to the inventory)
          if (from.equals("INV") || to.equals("INV")) { 
            if (retVal.length() > 0) retVal += Constants.commandSeperator;
            retVal += "INVE"+getPlayerInventory(playerIndex);
          }
          // refresh the bag if necessary (something moved from or to the bag)
          if (from.equals("BAG") || to.equals("BAG")) { 
            int bagIndex = -1;
            if (from.equals("BAG")) bagIndex = fromBagIndex;
            else bagIndex = toBagIndex;
            if (retVal.length() > 0) retVal += Constants.commandSeperator; // if something else is being sent
            retVal += "BAGI"+getBagString(bagIndex); // refresh the bag display
          }
          // refresh the equip if necessary 
          if (from.equals("EQP") || to.equals("EQP")) {
            // something else being sent (bag refresh, or TEXT etc..)
            if (retVal.length() > 0) retVal += Constants.commandSeperator;
            retVal += "EQIP"+getEqp(playerIndex);
          }
        } // validFrom and To
      }
    } else {
      System.out.println("Badly formed DRAG command "+inStr);
    }
    if (retVal.length() == 0) return "NONE";
    else return retVal;
  }  
  
  
 // ----------------------- GETO Command: Picking Up Objects --------------------------------------
 
  // returns the index of the first available inventory slot (on the top level only)
  private int getFreeInvIndex(int playerIndex) {
    int retVal =  -1;
    for (int i=0; i< Creature.maxObj; i++) {
      if (players[playerIndex].inventory[i] < 0) {
        retVal = i;
        break; // return the first free slot
      }
    }
    return retVal;
  }
  
  public void pickUpObj(int playerIndex, String where) {
    int x = getWhereX(playerIndex,where);
    int y = getWhereY(playerIndex,where);
    int objIndex = -1;
    if (players[playerIndex].alive) {
      int freeInvIndex = getFreeInvIndex(playerIndex); // next available inventory slot
      boolean inRange = inRange(x,y,players[playerIndex].x,players[playerIndex].y,1.9); // 1 space away (including diagonals)
      // Get the "top" object at this location (highest ObjIndex)
      for (int i=obj.length-1; i >= 0; i--) {
        if ((obj[i].x == x) && (obj[i].y == y) &&
            (!obj[i].type.equals(Obj.NULLOBJ))) {
          objIndex = i;
          break;
        }
      }
      if ((objIndex >= 0) && (freeInvIndex >= 0) && (inRange==true)) {
        obj[objIndex].x = -1;
        obj[objIndex].y = -1;
        obj[objIndex].onMap = false;
        players[playerIndex].inventory[freeInvIndex] = objIndex;
      }
    }
  }
 
 // Called by protocol
 public String getEqp(int playerIndex) {
   String retVal = "";
   if (playerIndex >= 0) {
     for (int i=0; i<Creature.maxEquip; i++) {
       if (players[playerIndex].equip[i] >= 0) {
         retVal += obj[players[playerIndex].equip[i]].imageName;
       }
       retVal += ",";
     }
   }
   return retVal;
 }
 
 
 // ----------------------------------------------- ATTR String -----------------------------------------------
 
  // Called by protocol
  public String getAttr(int playerIndex) {
    String retVal = "";
    if (playerIndex >= 0) {
      retVal += players[playerIndex].freeAttributes +",";
      retVal += players[playerIndex].strength +",";
      retVal += players[playerIndex].dexterity +",";
      retVal += players[playerIndex].constitution +",";
      retVal += players[playerIndex].intelligence +",";
      retVal += players[playerIndex].charisma +",";
      // Without attr bonus
      retVal += getAttr("armorBludgeon",players[playerIndex],0,false,false)+",";
      retVal += getAttr("armorPierce",players[playerIndex],0,false,false) +",";
      retVal += getAttr("armorSlash",players[playerIndex],0,false,false) +",";
      retVal += getAttr("armorFire",players[playerIndex],0,false,false) +",";
      retVal += getAttr("armorIce",players[playerIndex],0,false,false) +",";
      retVal += getAttr("armorMagic",players[playerIndex],0,false,false) +",";
      retVal += getAttr("damageBludgeon",players[playerIndex],0,false,false)+",";
      retVal += getAttr("damagePierce",players[playerIndex],0,false,false) +",";
      retVal += getAttr("damageSlash",players[playerIndex],0,false,false) +",";
      retVal += getAttr("damageFire",players[playerIndex],0,false,false) +",";
      retVal += getAttr("damageIce",players[playerIndex],0,false,false) +",";
      retVal += getAttr("damageMagic",players[playerIndex],0,false,false) +",";
      // with attr bonus
      retVal += getAttr("armorBludgeon",players[playerIndex],0,false,true)+",";
      retVal += getAttr("armorPierce",players[playerIndex],0,false,true) +",";
      retVal += getAttr("armorSlash",players[playerIndex],0,false,true) +",";
      retVal += getAttr("armorFire",players[playerIndex],0,false,true) +",";
      retVal += getAttr("armorIce",players[playerIndex],0,false,true) +",";
      retVal += getAttr("armorMagic",players[playerIndex],0,false,true) +",";
      retVal += getAttr("damageBludgeon",players[playerIndex],0,false,true)+",";
      retVal += getAttr("damagePierce",players[playerIndex],0,false,true) +",";
      retVal += getAttr("damageSlash",players[playerIndex],0,false,true) +",";
      retVal += getAttr("damageFire",players[playerIndex],0,false,true) +",";
      retVal += getAttr("damageIce",players[playerIndex],0,false,true) +",";
      retVal += getAttr("damageMagic",players[playerIndex],0,false,true) +",";
      
      retVal += getInRange("all","player",playerIndex)+",";
      retVal += getInRange("max","player",playerIndex)+",";
    }
    return retVal;  
  }
  
 // ----------------------------- ATTACK --------------------------------------
 // Returns the sum of the attribute from all the equipped objects
 // and modified by strength or dexterity bonuses and a random factor
 private double getAttr(String attr, Creature who, double distance, boolean doRandom, boolean doAttr) {
   double retVal = 0.0;
   double sumDamage = 0.0;
   double damageModifier = 0.0;
   double attrModifier = 0.0;
   double randomModifier = 0.0;
   for (int i=0; i< Creature.maxEquip; i++) {
     if (who.equip[i] >= 0) {
       if (obj[who.equip[i]].range >= distance) { // range is only used for damage
         if (attr.equals("damageBludgeon")) sumDamage += obj[who.equip[i]].damageBludgeon;
         if (attr.equals("damagePierce"))   sumDamage += obj[who.equip[i]].damagePierce;
         if (attr.equals("damageSlash"))    sumDamage += obj[who.equip[i]].damageSlash;
         if (attr.equals("damageFire"))     sumDamage += obj[who.equip[i]].damageFire;
         if (attr.equals("damageIce"))      sumDamage += obj[who.equip[i]].damageIce;
         if (attr.equals("damageMagic"))    sumDamage += obj[who.equip[i]].damageMagic; 
       }
       if (attr.equals("armorBludgeon")) sumDamage += obj[who.equip[i]].armorBludgeon;
       if (attr.equals("armorPierce"))   sumDamage += obj[who.equip[i]].armorPierce;
       if (attr.equals("armorSlash"))    sumDamage += obj[who.equip[i]].armorSlash;
       if (attr.equals("armorFire"))     sumDamage += obj[who.equip[i]].armorFire;
       if (attr.equals("armorIce"))      sumDamage += obj[who.equip[i]].armorIce;
       if (attr.equals("armorMagic"))    sumDamage += obj[who.equip[i]].armorMagic;
     }
   }     
   if (attr.substring(0,6).equals("damage")) attrModifier = who.strength;
   if (attr.substring(0,5).equals("armor")) attrModifier = who.dexterity;
   // Base player damage and armor
   if (attr.equals("damageBludgeon")) sumDamage += who.damageBludgeon;
   if (attr.equals("damagePierce"))   sumDamage += who.damagePierce;
   if (attr.equals("damageSlash"))    sumDamage += who.damageSlash;
   if (attr.equals("damageFire"))     sumDamage += who.damageFire;
   if (attr.equals("damageIce"))      sumDamage += who.damageIce;
   if (attr.equals("damageMagic"))    sumDamage += who.damageMagic;
   if (attr.equals("armorBludgeon"))  sumDamage += who.armorBludgeon;
   if (attr.equals("armorPierce"))    sumDamage += who.armorPierce;
   if (attr.equals("armorSlash"))     sumDamage += who.armorSlash;
   if (attr.equals("armorFire"))      sumDamage += who.armorFire;
   if (attr.equals("armorIce"))       sumDamage += who.armorIce;
   if (attr.equals("armorMagic"))     sumDamage += who.armorMagic;
  
  // Modifier for weapon damage = third root * 10. So total dmg:27 gets 30, dmg:125 gets 50. 
  // So as weapon damage gets higher the strength bonus effect grows less effective. 
  damageModifier = Math.pow(sumDamage,0.3333333333)*10.0;
  // Attribute Modifier (25 attr = 50% bonus to damageModifier)
  if (doAttr) attrModifier = (attrModifier*(2.0/100.0))*damageModifier;
  else attrModifier = 0; // otherwise it's set to attribute value
  // Modify the damage by a random factor (standard deviation 20% of sumDamage)
  if (doRandom) randomModifier = (random.nextGaussian()*(sumDamage/5.0));
  // Final value (attrModifier is a percent of damageModifier)
  retVal = sumDamage + attrModifier + randomModifier;
  return retVal;
 }
 
 // Returns the distance between two players or creatures (or mixes thereof)
 // who = "player"/"creature"  indexes are in players[] or creatures[]
 private double getDistance(String who1, int index1, String who2, int index2) {
  double retVal = -1;
  int x1 = -1;
  int y1 = -1;
  int x2 = -1;
  int y2 = -1;
  if ((index1 <0)||(index2 < 0)) return -1; // error
  if (who1.equals("player")) {
    x1 = players[index1].x;
    y1 = players[index1].y;
  } else if (who1.equals("creature")) {
    x1 = creatures[index1].x;
    y1 = creatures[index1].y;
  }
  if (who2.equals("player")) {
    x2 = players[index2].x;
    y2 = players[index2].y;
  } else if (who2.equals("creature")) {
    x2 = creatures[index2].x;
    y2 = creatures[index2].y;
  }
  if ((x1<0)||(x2<0)||(y1<0)||(y2<0)) return -1; // error
  
  // Pythagoras
  retVal =  Math.sqrt(Math.pow(Math.abs(y1-y2),2)+Math.pow(Math.abs(x1-x2),2));
  return retVal;
 }

 private void applyDamage(double damage, String whoType, int whoIndex) {
  if (whoIndex >= 0) {
    if (whoType.equals("player")) {
      players[whoIndex].hitPoints -= damage;
    } else if (whoType.equals("creature")) {
      creatures[whoIndex].hitPoints -= damage;
    }
  }
 }
 
  private void updateAI(String whoAttacked, int attkIndex, int defIndex, double damage) {
    String clan = creatures[defIndex].clan;
    String userName = "";
    if (whoAttacked.equals("player")) userName = players[attkIndex].userName;
    else if (whoAttacked.equals("creature")) userName = creatures[attkIndex].userName+"-"+attkIndex;
    creatures[defIndex].ai.attackedBy("personal",userName,damage);
    for (int i=0; i<Constants.maxCreatures; i++) {
      if (creatures[i].exists && creatures[i].alive && 
          creatures[i].clan.equals(clan)) { // if the creature is the same clan as the defender
        creatures[i].ai.attackedBy("clan",userName,damage);
      }
    }
  }
 
  // Does a durability test on the object (and kills it if it fails)
  private void testObj(int index) {
    if ((index >= 0) && obj[index].exists && // basic checks
        (obj[index].durability < 1)){ // can fail the durability check
      if (Math.random() > obj[index].durability) {
        killObj(index);
      }
    }
  }
 
  // Destroys equipped items that fail the durability check (weapons for attacker, armor for defender)
  private void durabilityCheck(String whoAttacked, int attackIndex, String whoDef, int defIndex, double distance) {
    for (int i=0; i<Creature.maxEquip; i++) {
      int attkObj = -1;
      int defObj = -1;
      if (whoAttacked.equals("player")) attkObj = players[attackIndex].equip[i];
      else if (whoAttacked.equals("creature")) attkObj = creatures[attackIndex].equip[i];
      if (whoDef.equals("player")) defObj = players[defIndex].equip[i];
      else if (whoDef.equals("creature")) defObj = creatures[defIndex].equip[i];
      if ( (attkObj >= 0) && obj[attkObj].exists  &&  // Attacking Obj exists
           (obj[attkObj].range >= distance) && // attacking within range of this weapon
          ((obj[attkObj].damageBludgeon > 0)||(obj[attkObj].damagePierce > 0)||(obj[attkObj].damageSlash > 0)|| // obj is a weapon
           (obj[attkObj].damageFire > 0)||(obj[attkObj].damageIce > 0)||(obj[attkObj].damageMagic > 0)  )){ // of some type
        testObj(attkObj);
      }
      if ( (defObj > 0) && obj[defObj].exists  &&  // Defending Obj exists
          ((obj[defObj].armorBludgeon > 0)||(obj[defObj].armorPierce > 0)||(obj[defObj].armorSlash > 0)|| // obj is armor (of some type)
           (obj[defObj].armorFire > 0)||(obj[defObj].armorIce > 0)||(obj[defObj].armorMagic > 0)  )){
        testObj(defObj);
      }
    }
  }
 
  private int getAttackDelay (Creature who, double distance) {
    int retVal = who.attackDelayMax; // base attack Delay
    for (int i=0; i<Creature.maxEquip; i++) {
      if ((who.equip[i] >= 0) &&  (obj[who.equip[i]].exists) && (obj[who.equip[i]].range >= distance) &&  // object exists and is in range
          ((obj[who.equip[i]].damageBludgeon > 0)||(obj[who.equip[i]].damagePierce > 0)||(obj[who.equip[i]].damageSlash > 0)|| // and  obj is a weapon
           (obj[who.equip[i]].damageFire > 0)||(obj[who.equip[i]].damageIce > 0)||(obj[who.equip[i]].damageMagic > 0)) ) {
        retVal += obj[who.equip[i]].attackDelay;
      }
    }
    return retVal;
  }

 private String doAttack(String whoAttk, int attkIndex, String whoDef, int defIndex) {
  double dmg = 0.0;
  double arm = 0.0;
  double dif = 0.0;
  double totalDamage = 0.0;
  String retVal = "";
  Creature whoA = getCreature(whoAttk, attkIndex);
  Creature whoD = getCreature(whoDef, defIndex);
  double distance = getDistance(whoAttk, attkIndex, whoDef, defIndex);
  if ((distance<0)||(attkIndex<0)||(defIndex<0)) return "Attack failed";

  if (whoA.attackDelay>0)  return "To soon to attack";
  if (whoA.alive == false) return "You are dead and cannot attack";
  whoA.attackDelay += getAttackDelay(whoA, distance); // gets the sum of the delay of all the attacking weapons.
  
  dmg = getAttr("damageBludgeon",whoA,distance,true,true);
  arm = getAttr("armorBludgeon", whoD,distance,true,true);
  dif = dmg-arm;
  if (dif > 0) totalDamage += dif;
  dmg = getAttr("damagePierce",whoA,distance,true,true);
  arm = getAttr("armorPierce", whoD,distance,true,true);
  dif = dmg-arm;
  if (dif > 0) totalDamage += dif;
  dmg = getAttr("damageSlash",whoA,distance,true,true);
  arm = getAttr("armorSlash", whoD,distance,true,true);
  dif = dmg-arm;
  if (dif > 0) totalDamage += dif;
  dmg = getAttr("damageFire",whoA,distance,true,true);
  arm = getAttr("armorFire", whoD,distance,true,true);
  dif = dmg-arm;
  if (dif > 0) totalDamage += dif;
  dmg = getAttr("damageIce",whoA,distance,true,true);
  arm = getAttr("armorIce", whoD,distance,true,true);
  dif = dmg-arm;
  if (dif > 0) totalDamage += dif;
  dmg = getAttr("damageMagic",whoA,distance,true,true);
  arm = getAttr("armorMagic", whoD,distance,true,true);
  dif = dmg-arm;
  if (dif > 0) totalDamage += dif;
  
  // Object Durability Check (fragile items attacking or defending break)
  durabilityCheck(whoAttk,attkIndex, whoDef,defIndex, distance);
  // Applies the damage to the creature or player
  applyDamage(totalDamage, whoDef, defIndex);
  if (whoDef.equals("creature")) {
    // Notifies the AI of the defending creature, and all the defender's clan that he was attacked
    // and by who he was attacked
    updateAI(whoAttk,attkIndex, defIndex, totalDamage);
  }
  retVal += "did "+(int)totalDamage+" damage.";
  return retVal;
 }

 // Interface method parses an ATTK string, and gets the creature or player to attack 
 public String parseAttk(int playerIndex, String location) {
   String retVal = "";
   int x = getWhereX(playerIndex, location);
   int y = getWhereY(playerIndex, location);
   String who = "";
   int attkIndex = -1;
   
   // Get the player that is there
   for (int i=0; i< Constants.maxPlayers; i++) {
     if ((players[i].x == x) && (players[i].y == y) &&
         (players[i].exists) && (players[i].alive) &&
         (i != playerIndex)) {
       who = "player";
       attkIndex = i;
     }
   }
   // Get the creature that is there
   if (attkIndex < 0) {
     for (int i=0; i< Constants.maxCreatures; i++) {
       if ((creatures[i].x == x) && (creatures[i].y == y) &&
           (creatures[i].exists) && (creatures[i].alive)) {
         who = "creature";
         attkIndex = i;
       }
     }
   }
   
   if (attkIndex >= 0) {
     double range = getInRange("max","player",playerIndex);
     if (inRange(x,y,players[playerIndex].x,players[playerIndex].y,range)) {
       retVal = doAttack("player", playerIndex, who, attkIndex);
       if (retVal.indexOf("damage") >= 0) {
         retVal = "You "+retVal; // add "You " to "did x damage."
       }
     } else retVal = "Out of range!";
   } else retVal = "Could not find anything to attack.";
   
   return retVal;
 }  

  // --------------------------------------- ADDING FREE ATTRIBUTES ------------------------------------------
  
  // Adds the attribute to a creature, reducing freeAttributes as necessary
  // returns an error or success message
  private String addAttribute(Creature who, String attrName, double attrVal) {
    if (attrVal < 0) return "Attribute Value must be positive";
    if        (attrName.equals("strength") || attrName.equals("str")) {
      if (who.freeAttributes < attrVal) return "Not enough free attributes to raise strength by "+attrVal+" .";
      who.strength += attrVal; 
      who.freeAttributes -= attrVal;
      return "Added "+attrVal+" to your strength!";
    } else if (attrName.equals("constitution") || attrName.equals("con")) {
      if (who.freeAttributes < attrVal) return "Not enough free attributes to raise constitution by "+attrVal+".";
      who.constitution += attrVal; 
      who.freeAttributes -= attrVal;
      return "Added "+attrVal+" to your constitution!";
    } else if (attrName.equals("dexterity") || attrName.equals("dex")) {
      if (who.freeAttributes < attrVal) return "Not enough free attributes to raise dexterity by "+attrVal+".";
      who.dexterity += attrVal; 
      who.freeAttributes -= attrVal;
      return "Added "+attrVal+" to your dexterity!";
    } else if (attrName.equals("intelligence") || attrName.equals("int")) {
      if (who.freeAttributes < attrVal) return "Not enough free attributes to raise intelligence by "+attrVal+".";
      who.intelligence += attrVal; 
      who.freeAttributes -= attrVal;
      return "Added "+attrVal+" to your intelligence!";
    } else if (attrName.equals("charisma") || attrName.equals("cha")) {
      if (who.freeAttributes < attrVal) return "Not enough free attributes to raise charisma by "+attrVal+".";
      who.charisma += attrVal; 
      who.freeAttributes -= attrVal;
      return "Added "+attrVal+" to your charisma!";
    }
    return "Unable to add to attribute "+attrName+".";
  }
  
  public String addFreeAttributes(int playerIndex, String inStr) {
    if (playerIndex >= 0) {
      int tildaIndex = inStr.indexOf('~');
      if (tildaIndex >= 0) {
        String attrName = inStr.substring(0,tildaIndex).toLowerCase().trim();
        double attrVal = 0;
        try { attrVal = Double.valueOf(inStr.substring(tildaIndex+1,inStr.length())).doubleValue(); }
        catch (NumberFormatException e) {return "Attribute Value is not a valid Number.";}
        // Successfully passed error checking
        return addAttribute(players[playerIndex], attrName, attrVal);
      } else return "Invalid Attribute Command.";
    } else return "Not a valid player.";
  }
  
  
}
