import java.io.*;

public class Creature {
  public final static int maxObj = 24; // maximum number of objects in inventory
  public final static int maxEquip = 12;
  public final static int maxMessages = 10;
  public final static int numAttributes = 37; // total number of attributes in loadPlayerAttributesFromFile
  public final static int head = 0;
  public final static int face = 1;
  public final static int neck = 2;
  public final static int body = 3;
  public final static int arms = 4;
  public final static int rightHand = 5;
  public final static int leftHand = 6;
  public final static int rightFinger = 7;
  public final static int leftFinger = 8;
  public final static int waist = 9;
  public final static int legs = 10;
  public final static int feet = 11;

  // System
  public boolean exists = false;
  public boolean alive = false;
  public int moveDelay   = 0;
  public int attackDelay = 0;
  public int spellDelay  = 0;
  private int numMessages = 0; // Use accessor methods to see these
  private String[] waitingMessages = new String[maxMessages];
  public boolean inventoryChanged = false;

  // User System
  public long corpseTime = 600;
  public String userName = "";
  public String passWord = "";
  public int x = -1;
  public int y = -1;
  public String img = "noimage";
  public String imgDead = "corpse";

  // General
  public double hitPoints = 0;
  public double hitPointsMax = 0;
  public double spellPoints = 0;
  public double spellPointsMax = 0;
  public double gold = 0;

  // Timing                 Speed 10 = 1 move / second
  public int moveDelayMax   = 10;
  public int attackDelayMax = 10;
  public int spellDelayMax  = 10;
  
  // Leveling
  public int level = 0;
  public long experience = 0;
  public double freeAttributes = 0;

  // Attributes
  public double strength     = 0; // used for HP
  public double dexterity    = 0; // used for Defense
  public double constitution = 0; // used for hitpoints
  public double intelligence = 0; // used for casting spells
  public double charisma     = 0; // used for buying/selling

  // Base Armor
  public double armorBludgeon = 0;
  public double armorPierce = 0;
  public double armorSlash = 0;
  public double armorFire = 0;
  public double armorIce = 0;
  public double armorMagic = 0;

  // Base Damage
  public double baseAttackRange = 0;
  public double damageBludgeon = 0;
  public double damagePierce = 0;
  public double damageSlash = 0;
  public double damageFire = 0;
  public double damageIce = 0;
  public double damageMagic = 0;

  // Object indexes
  public int[] equip = new int[maxEquip];
  public int[] inventory = new int[maxObj];

  // AI
  public AIClass ai;
  public double sightRange = 5;
  public String clan = "";

  // Constructor
  Creature() {
    clearAttributes();
  }
  
  private void clearAttributes() {
    exists = false;
    alive = false;
    moveDelay   = 0;
    attackDelay = 0;
    spellDelay  = 0;
    numMessages = 0; // Use accessor methods to see these
    waitingMessages = new String[maxMessages];
    inventoryChanged = false;
    // User System
    corpseTime = 600;
    userName = "";
    passWord = "";
    clan = "";
    x = -1;
    y = -1;
    img = "noimage";
    imgDead = "corpse";
    // General
    hitPoints = 0;
    hitPointsMax = 0;
    spellPoints = 0;
    spellPointsMax = 0;
    gold = 0;
    // Timing                 Speed 10 = 1 move / second
    moveDelayMax   = 10;
    attackDelayMax = 30;
    spellDelayMax  = 50;
    // Leveling
    level = 0;
    experience = 0;
    freeAttributes = 0;
    // Attributes
    strength     = 0; // used for HP
    dexterity    = 0; // used for Defense
    constitution = 0; // used for hitpoints
    intelligence = 0; // used for casting spells
    charisma     = 0; // used for buying/selling
    // Base Armor/Damage
    armorBludgeon = 0;
    armorPierce = 0;
    armorSlash = 0;
    armorFire = 0;
    armorIce = 0;
    armorMagic = 0;
    baseAttackRange = 0;
    damageBludgeon = 0;
    damagePierce = 0;
    damageSlash = 0;
    damageFire = 0;
    damageIce = 0;
    damageMagic = 0;
    // Clear the Equipped objects
    equip = new int[maxEquip];
    for (int i=0 ; i<maxEquip ; i++) {
      equip[i] = -1;
    }
    // Clear the Inventory
    inventory = new int[maxObj];
    for (int i=0 ; i<maxObj ; i++) {
     inventory[i] = -1;
    }
    // Clear the AI
    ai = new AIClass(); // Brainless
    sightRange = 5;
  }
 
  // ----------------------------- SETTING ATTRIBUTES ----------------------------------------
 
  public void setVal(String key, String val) {
    String thisKey = key.toLowerCase().trim();
    String thisVal = val.trim();
    if      (thisKey.equals("corpsetime"))     corpseTime      = Integer.valueOf(thisVal).intValue(); 
    else if (thisKey.equals("x"))              x               = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("y"))              y               = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("img"))            img             = thisVal;
    else if (thisKey.equals("image"))          img             = thisVal;
    else if (thisKey.equals("imgdead"))        imgDead         = thisVal;
    else if (thisKey.equals("username"))       userName        = thisVal;
    else if (thisKey.equals("clan"))           clan            = thisVal;
    else if (thisKey.equals("sightrange"))     sightRange      = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("hitpoints"))      hitPoints       = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("hitpointsmax"))   hitPointsMax    = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("spellpoints"))    spellPoints     = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("spellpointsmax")) spellPointsMax  = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("gold"))           gold            = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("movedelaymax"))   moveDelayMax    = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("attackdelaymax")) attackDelayMax  = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("spelldelaymax"))  spellDelayMax   = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("level"))          level           = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("experience"))     experience      = Long.valueOf(thisVal).longValue();
    else if (thisKey.equals("freeattributes")) freeAttributes  = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("intelligence"))   intelligence    = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("strength"))       strength        = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("dexterity"))      dexterity       = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("constitution"))   constitution    = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("charisma"))       charisma        = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorbludgeon"))  armorBludgeon   = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorpierce"))    armorPierce     = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorslash"))     armorSlash      = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorfire"))      armorFire       = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorice"))       armorIce        = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armormagic"))     armorMagic      = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("baseattackrange"))baseAttackRange = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damagebludgeon")) damageBludgeon  = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damagepierce"))   damagePierce    = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damageslash"))    damageSlash     = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damagefire"))     damageFire      = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damageice"))      damageIce       = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damagemagic"))    damageMagic     = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("head"))           equip[head]     = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("face"))           equip[face]     = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("neck"))           equip[neck]     = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("body"))           equip[body]     = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("arms"))           equip[arms]     = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("righthand"))      equip[rightHand]= Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("lefthand"))       equip[leftHand] = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("rightfinger"))    equip[rightFinger] = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("leftfinger"))     equip[leftFinger]  = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("waist"))          equip[waist]    = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("legs"))           equip[legs]     = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("feet"))           equip[feet]     = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("ai"))             ai = new AIClass(thisVal);
    else System.out.println("Unable to set creature attribute "+thisKey+" to "+thisVal);
  }

  public void setAttributes(String inStr) {
    int start = 0;
    int end = inStr.indexOf(',');
    if (end < 0) end = inStr.length();
    clearAttributes();
    while (end >= 0) {
      String keyVal = inStr.substring(start,end);
      // Seperate the Keys and Values. String has the form "Key=Val"
       int splitter = keyVal.indexOf('='); // first = (ai's may have more than one =)
       if (splitter >0) setVal(keyVal.substring(0,splitter), keyVal.substring(splitter+1,keyVal.length()));
      start = end + 1;
      end = inStr.indexOf(',',start);
      if ((end<0) && (start < inStr.length())) end = inStr.length();
    }

    // Some basic attributes must be defined or the creature won't exist
    if ((x < 0) || (y < 0) || img.equals("noimage")) {
      exists = false;
      alive = false;
    } else {
      exists = true;
      alive = true;
    }
  }


  // Opens a file and reads in the lines that specify the player attributes
  // NOTE: Does NOT read in equip[] or inventory[]
  public void loadPlayerAttributesFromFile(String fileName) {
    try {
      BufferedReader in = new BufferedReader(new FileReader(fileName));
      alive = true; // players always join as alive (may be changed to dead immediately though...)
      exists = true;
      corpseTime = Integer.valueOf(in.readLine().trim()).intValue();
      userName   = in.readLine().trim();
      passWord   = in.readLine().trim();
      clan       = in.readLine().trim();
      x = Integer.valueOf(in.readLine().trim()).intValue();
      y = Integer.valueOf(in.readLine().trim()).intValue();
      img = in.readLine().trim();
      imgDead = in.readLine().trim();
      hitPoints = Double.valueOf(in.readLine().trim()).doubleValue();
      hitPointsMax = Double.valueOf(in.readLine().trim()).doubleValue();
      spellPoints = Double.valueOf(in.readLine().trim()).doubleValue();
      spellPointsMax = Double.valueOf(in.readLine().trim()).doubleValue();
      gold = Double.valueOf(in.readLine().trim()).doubleValue();
      moveDelayMax = Integer.valueOf(in.readLine().trim()).intValue();
      attackDelayMax = Integer.valueOf(in.readLine().trim()).intValue();
      spellDelayMax = Integer.valueOf(in.readLine().trim()).intValue();
      level = Integer.valueOf(in.readLine().trim()).intValue();
      experience = Long.valueOf(in.readLine().trim()).longValue();
      freeAttributes = Double.valueOf(in.readLine().trim()).doubleValue();
      strength = Double.valueOf(in.readLine().trim()).doubleValue();
      dexterity = Double.valueOf(in.readLine().trim()).doubleValue();
      constitution = Double.valueOf(in.readLine().trim()).doubleValue();
      intelligence = Double.valueOf(in.readLine().trim()).doubleValue();
      charisma = Double.valueOf(in.readLine().trim()).doubleValue();
      armorBludgeon = Double.valueOf(in.readLine().trim()).doubleValue();
      armorPierce = Double.valueOf(in.readLine().trim()).doubleValue();
      armorSlash = Double.valueOf(in.readLine().trim()).doubleValue();
      armorFire = Double.valueOf(in.readLine().trim()).doubleValue();
      armorIce = Double.valueOf(in.readLine().trim()).doubleValue();
      armorMagic = Double.valueOf(in.readLine().trim()).doubleValue();
      baseAttackRange = Double.valueOf(in.readLine().trim()).doubleValue();
      damageBludgeon = Double.valueOf(in.readLine().trim()).doubleValue();
      damagePierce = Double.valueOf(in.readLine().trim()).doubleValue();
      damageSlash = Double.valueOf(in.readLine().trim()).doubleValue();
      damageFire = Double.valueOf(in.readLine().trim()).doubleValue();
      damageIce = Double.valueOf(in.readLine().trim()).doubleValue();
      damageMagic = Double.valueOf(in.readLine().trim()).doubleValue();
      in.close();
    } catch (IOException e) {
      System.err.println("Error reading file "+fileName);
    }
  }



 // ----------------------------------- DISPLAY ------------------------------------------

  public String toString() {
   return corpseTime+"\n"+
          userName+"\n"+
          passWord+"\n"+
          clan+"\n"+
          x+"\n"+
          y+"\n"+
          img+"\n"+
          imgDead+"\n"+
          hitPoints+"\n"+
          hitPointsMax+"\n"+
          spellPoints+"\n"+
          spellPointsMax+"\n"+
          gold+"\n"+
          moveDelayMax+"\n"+
          attackDelayMax+"\n"+
          spellDelayMax+"\n"+
          level+"\n"+
          experience+"\n"+
          freeAttributes+"\n"+
          strength+"\n"+
          dexterity+"\n"+
          constitution+"\n"+
          intelligence+"\n"+          
          charisma+"\n"+
          armorBludgeon+"\n"+
          armorPierce+"\n"+
          armorSlash+"\n"+
          armorFire+"\n"+
          armorIce+"\n"+
          armorMagic+"\n"+
          baseAttackRange+"\n"+
          damageBludgeon+"\n"+
          damagePierce+"\n"+
          damageSlash+"\n"+
          damageFire+"\n"+
          damageIce+"\n"+
          damageMagic+"\n";
          // NOTE: does NOT display the equip[] or inventory[]
  }

  public String getStats() {
    String retVal = "";
    retVal += hitPoints+",";
    retVal += hitPointsMax+",";
    retVal += spellPoints+",";
    retVal += spellPointsMax+",";
    retVal += moveDelay+",";
    retVal += attackDelay+",";
    retVal += spellDelay+",";
    retVal += moveDelayMax+",";
    retVal += attackDelayMax+",";
    retVal += spellDelayMax+",";
    retVal += level+",";
    retVal += experience+",";
    retVal += gold;
    return retVal;
  }

 public String describe() {
   String retVal;
   retVal = userName+"~";
   if (alive == true) retVal += img+"~";
   else retVal += imgDead+"~";
   retVal += "HP : "+ (int)hitPoints +" / "+(int)hitPointsMax+"~";
   retVal += "SP : "+ (int)spellPoints+" / "+(int)spellPointsMax+"~";
   retVal += "Level : "+ level + "~";
   retVal += "Clan : "+clan+"~";
   return retVal;
 }

 // ----------------------------- MESSAGING -------------------------------
 public boolean addMessage (String message) {
  if (numMessages < maxMessages-1) {
    waitingMessages[numMessages] = new String(message);
    numMessages += 1;
    return true;
  } else return false; // out of space
 }

 // Queue style, FIFO messaging.  First in, first out. 
 public String getMessage() {
  String retVal = null;
  if (numMessages > 0) {
    retVal = waitingMessages[0]; // the first message
    for (int i=0; i<numMessages-1; i++) {
      waitingMessages[i] = waitingMessages[i+1]; // move all the messages down in the index;
    }
    waitingMessages[numMessages] = null;
    numMessages -=1;
  }
  return retVal;
 }


 // --------------------------------- INV / EQUIP  ----------------------
 
  // Removing objects from inventory or equip that have the same value as index
  public void removeObjIndex(int index) {
    for (int i=0; i< maxObj; i++) {
      if (inventory[i] == index) {
        inventory[i] = -1;
        inventoryChanged = true;
      }
    }
    for (int i=0; i< maxEquip; i++) {
      if (equip[i] == index) {
        equip[i] = -1;
        inventoryChanged = true;
      }
    }
  }


}