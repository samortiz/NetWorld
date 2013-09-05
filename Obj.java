public class Obj {
  
  // Constant types
  public final static String NULLOBJ = "NULLOBJ";
  public final static String CONTAINER = "container";
  public final static String HELMET = "helmet";
  public final static String CROWN = "crown";
  public final static String MASK = "mask";
  public final static String BREASTPLATE = "breastplate";
  public final static String SHIRT = "shirt";
  public final static String SLEEVE = "sleeve";
  public final static String WEAPON = "weapon";
  public final static String SHIELD = "shield";
  public final static String RING = "ring";
  public final static String BELT = "belt";
  public final static String GIRDLE = "girdle";
  public final static String LEGGING = "legging";
  public final static String SHOE = "shoe";
  public final static String BOOT = "boot";

  
  // System Attributes
  public String  type;
  public boolean exists;
  public int     x; // must be > 0  if onMap = true
  public int     y; // must be > 0  if onMap = true
  public boolean onMap; // true if it is a root level object (on the world map) 
  
  // General Attributes
  public String name;
  public String imageName;
  public double hitPoints;
  public double hitPointsMax;
  public double baseGoldValue;
  public double weight;
  public double range; // Distance this weapon can attack
  public int    attackDelay;

  // Usage and LifeSpan
  public Spell  use;
  public int    lifetime;
  public int    charges;
  public double durability;

  // Sub-Objects
  public int     maxContains;
  public int[]   contains = new int[Constants.objGlobalMaxContains];
 
  // Armor
  public double armorBludgeon;
  public double armorPierce;
  public double armorSlash;
  public double armorFire;
  public double armorIce;
  public double armorMagic;

  // Damage
  public double damageBludgeon;
  public double damagePierce;
  public double damageSlash;
  public double damageFire;
  public double damageIce;
  public double damageMagic;

 // -------------------------- Constructor ---------------------
 Obj(){
  clearAttributes(); // set all attributes to their defaults
 }


// ---------------------- METHODS -----------------------------
 
  // Set all Attributes to their defaults 
  public void clearAttributes () {
    type = NULLOBJ;
    exists = false;
    x = -1; // must be > 0  if onMap = true
    y = -1; // must be > 0  if onMap = true
    onMap = false; // true if it is a root level object (on the world map) 
    name = "";
    imageName = "noImage";
    hitPoints = 0;
    hitPointsMax =0;
    baseGoldValue =0;
    weight = 0;
    range = 0;
    attackDelay = 0;
    
    use = new Spell();
    lifetime = -1; // lasts forever
    charges = -1;   // infinite usages left
    durability = 1; // infinitely strong (0 breaks every time, 1 never breaks)
   
    maxContains = 0;
    for (int i=0; i< Constants.objGlobalMaxContains; i++) {
      contains[i] = -1;
    }
    
    armorBludgeon = 0;
    armorPierce = 0;
    armorSlash = 0;
    armorFire = 0;
    armorIce = 0;
    armorMagic = 0;
    damageBludgeon = 0;
    damagePierce = 0;
    damageSlash = 0;
    damageFire = 0;
    damageIce = 0;
    damageMagic = 0;
 }


 // Remove an object from the Game 
 public void kill() {
   clearAttributes();
 }

 // ------------------------------- Setting ATTRIBUTES ----------------------------------------------

  // Sets the object attribute specified with key to the value specified
  // with value.  NOTE: value may be of any type and will be cast depending on the key
  private void setVal(String key, String val){
    try {
    String thisKey = key.toLowerCase().trim();
    String thisVal = val.trim();
    if      (thisKey.equals("x"))              x              = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("y"))              y              = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("onmap")) {
      if (thisVal.toLowerCase().equals("true")) onMap = true;
      else                                      onMap = false;
    }
    else if (thisKey.equals("use"))            use            = new Spell(thisVal); 
    else if (thisKey.equals("name"))           name           = thisVal;
    else if (thisKey.equals("imagename"))      imageName      = thisVal;
    else if (thisKey.equals("image"))          imageName      = thisVal;
    else if (thisKey.equals("img"))            imageName      = thisVal;
    else if (thisKey.equals("type")) {         type           = thisVal; exists = true; } // object begins existing!
    else if (thisKey.equals("hitpoints"))      hitPoints      = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("hitpointsmax"))   hitPointsMax   = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("basegoldvalue"))  baseGoldValue  = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("weight"))         weight         = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("range"))          range          = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("attackdelay"))    attackDelay    = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("maxcontains"))    maxContains    = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("lifetime"))       lifetime       = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("charges"))        charges        = Integer.valueOf(thisVal).intValue();
    else if (thisKey.equals("durability"))     durability     = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorbludgeon"))  armorBludgeon  = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorpierce"))    armorPierce    = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorslash"))     armorSlash     = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorfire"))      armorFire      = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armorice"))       armorIce       = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("armormagic"))     armorMagic     = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damagebludgeon")) damageBludgeon = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damagepierce"))   damagePierce   = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damageslash"))    damageSlash    = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damagefire"))     damageFire     = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damageice"))      damageIce      = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("damagemagic"))    damageMagic    = Double.valueOf(thisVal).doubleValue();
    else if (thisKey.equals("contains"))       {} // do nothing the contains should already be set
    else System.out.println("Unable to set object attribute "+thisKey+" to "+thisVal);
  } catch (NumberFormatException e) { System.out.println("Could not set key="+key+" to val="+val);}
  }
  
  
  // Parses a string and sets all the attributes for an object in 
  // Obj[] specified by objIndex. 
  // This will clear all attributes before setting them. 
  // The String format is (attr=val,attr=val,....)
  // Contains MUST have the format contains=(index,index..) (already be converted from objects!)
  // the use=() must have () and will be removed before searching for a contains string
  public void setAttributes(String inString) {
    String inStr = inString.toLowerCase(); 

    // search for use = () for use spell.  (when found remove it, so the other searches work
    int useStart = 0;
    int useEnd = -1;
    int brackStart = -1;
    int brackEnd = -1;
    while (useStart >= 0) {
      useStart = inStr.indexOf("use",useStart+1);
      useEnd = inStr.indexOf("=",useStart);
      brackStart = inStr.indexOf('(',useEnd); // first ( after use=
      brackEnd = worldClass.closeBracket(inStr,brackStart); // close )
      if ((useStart >= 0) && (useEnd >= 0)) {
        String useStr = inStr.substring(useStart,useEnd);
        if (useStr.toLowerCase().trim().equals("use")) {// ensure that it is just use = , and not "misused" or "abuse, x="...
          if ((brackStart >= 0) && (brackEnd > 0)) {
            String spellStr = inStr.substring(brackStart,brackEnd+1); // get closing )
            setVal("use",spellStr); 
            // Remove String from inStr;
            int prevComma = inStr.lastIndexOf(',',useStart);
            if (prevComma < 0) prevComma = useStart;
            inStr = inStr.substring(0,prevComma)+inStr.substring(brackEnd+1,inStr.length());
            break; // exit while loop (only change first use, multiple use statements will cause object to fail to load)
          }
        }
      }
    }

    // search for () in the string (for contains)
    // (there should be only one set of (), and it must contain only integers)
    // format: contains=(index,index...) (object indexes, not object definitions)
    int containStart = inStr.indexOf('(',1);
    int containEnd = worldClass.closeBracket(inStr,containStart); // find the matching closing bracket
    if (containEnd == inStr.length()-1) containEnd = -1;
    if ((containStart >= 0) && (containEnd >= 0) && (containEnd > containStart)) {
      String containsStr = inStr.substring(containStart,containEnd+1);

      // set the contains indexes
      int start = 0;
      int end = containsStr.indexOf(',',start+1);
      int loopCounter = 0;
      while (end >= 0) {
        String intVal = containsStr.substring(start+1,end);
        int objIndex = -1;
        try {objIndex = Integer.valueOf(intVal).intValue();} catch (NumberFormatException e) {}
        if (objIndex >= 0) {
          contains[loopCounter] = objIndex; // set the Contains
        }
        loopCounter += 1;
        start = end;
        end = containsStr.indexOf(',', start+1);
      }
      // Delete the contains string (so the ,'s won't cause problems with the next loop)
      StringBuffer tempStr = new StringBuffer(inStr);
      inStr = tempStr.delete(containStart,containEnd+1).insert(containStart,"DONE").toString();
    }
    
    int start = 1;
    int end = inStr.indexOf(',');
    while (end >= 0) {
      String keyVal = inStr.substring(start,end);
      // Seperate the Keys and Values. String has the form "Key=Val"
      int splitter = keyVal.indexOf('=');
      if (splitter >= 0) {
        setVal(keyVal.substring(0,splitter), keyVal.substring(splitter+1,keyVal.length()));
      }
      start = end+1;
      end = inStr.indexOf(',', start+1);
      if ((end < 0) && (start < inStr.length()-1)) end = inStr.length()-1;
    }
  }


 // ---------------------------------- DISPLAY DATA ----------------------------------
 
 // Get a string that is used to display the object on screen
 public String toDisplay(int minX, int minY){
  String retVal = "";
  int relativeX = (x - minX);
  int relativeY = (y - minY);
  if ((!type.equals(NULLOBJ)) &&
      (relativeX >= 0) && (relativeX < Constants.screenSizeX) &&
      (relativeY >= 0) && (relativeY < Constants.screenSizeY)) {
    retVal = "("+relativeX+","+relativeY+","+imageName+")";
  }
  return retVal;
 }
 

  // Get a string representation of the object (all the possible data)
  public String toString() {
    String  retVal;
    retVal = "("+
             "x="+x+","+
             "y="+y+","+
             "onMap="+onMap+","+
             "name="+name+","+
             "imageName="+imageName+","+
             "type="+type+","+
             "hitPoints="+hitPoints+","+
             "hitPointsMax="+hitPointsMax+","+
             "baseGoldValue="+baseGoldValue+","+
             "weight="+weight+","+
             "range="+range+","+
             "attackdelay="+attackDelay+","+
             "use="+use.toString()+","+
             "lifetime="+lifetime+","+
             "charges="+charges+","+
             "durability="+durability+","+
             "maxContains="+maxContains+","+
             "contains=(";
    for (int i=0; i<maxContains;i++) { // will not run at all when maxContains = 0;
      if  (contains[i] >= 0) retVal += contains[i];
      retVal += ",";
    }         
    retVal += "),"+
              "armorBludgeon="+armorBludgeon+","+
              "armorPierce="+armorPierce+","+
              "armorSlash="+armorSlash+","+
              "armorFire="+armorFire+","+
              "armorIce="+armorIce+","+
              "armorMagic="+armorMagic+","+
              "damageBludgeon="+damageBludgeon+","+
              "damagePierce="+damagePierce+","+
              "damageSlash="+damageSlash+","+
              "damageFire="+damageFire+","+
              "damageIce="+damageIce+","+
              "damageMagic="+damageMagic+
              ")";
    return retVal;          
  }
 
 public String describe() {
   String retVal = name+"~";
   retVal += imageName+"~";
   if ((hitPoints > 0) && (hitPointsMax > 0)) retVal += "HP : "+hitPoints+" / "+hitPointsMax+"~";
   if (baseGoldValue >0) retVal += "Value (in Gold) : "+baseGoldValue+"~";
   if (maxContains > 0) retVal += "Can contain "+maxContains+" objects. ~";
   if (range > 0) retVal += "Has an attack range of "+range+"~";
   if (type.equals("helmet")||type.equals("crown")) retVal += "Can be equipped on your head.~";
   if (type.equals("mask")) retVal += "Can be equipped on your face.~";
   if (type.equals("weapon") || type.equals("shield")) retVal += "Can be equipped in a hand.~";
   if (type.equals("collar") || type.equals("necklace")) retVal += "Can be equipped on your neck.~";
   if (type.equals("breastplate") || type.equals("shirt")) retVal += "Can be equipped on your body.~";
   if (type.equals("sleeve")) retVal += "Can be equipped on your arms.~";
   if (type.equals("ring")) retVal += "Can be equipped on a finger.~";
   if (type.equals("belt")||type.equals("girdle")) retVal += "Can be equipped around your waist.~";
   if (type.equals("legging")) retVal += "Can be equipped on your legs.~";
   if (type.equals("shoe")||type.equals("boot")) retVal += "Can be equipped on your feet.~";
   if ((damageBludgeon > 0)||(damagePierce> 0)||(damageSlash> 0)||(damageFire> 0)||(damageIce> 0)||(damageMagic> 0)) {
     retVal += "Dmg: ";
     if (damageBludgeon > 0) retVal += (int)damageBludgeon+"B ";
     if (damagePierce> 0) retVal += (int)damagePierce+"P ";
     if (damageSlash > 0) retVal += (int)damageSlash+"S ";
     if (damageFire > 0) retVal += (int)damageFire+"F ";
     if (damageIce > 0) retVal += (int)damageIce+"I ";
     if (damageMagic > 0) retVal += (int)damageMagic+"M ";
     retVal += "~";
   }
   if ((armorBludgeon>0)||(armorPierce>0)||(armorSlash>0)||(armorFire>0)||(armorIce>0)||(armorMagic>0)) {
     retVal += "Def: ";
     if (armorBludgeon > 0) retVal += (int)armorBludgeon+"B ";
     if (armorPierce> 0) retVal += (int)armorPierce+"P ";
     if (armorSlash > 0) retVal += (int)armorSlash+"S ";
     if (armorFire > 0) retVal += (int)armorFire+"F ";
     if (armorIce > 0) retVal += (int)armorIce+"I ";
     if (armorMagic > 0) retVal += (int)armorMagic+"M ";
     retVal += "~";
   }
   if (use.exists) {
    retVal += use.description+"~";
   }
   if (attackDelay >0) retVal += "Attack Delay: "+attackDelay+"~";
   if (lifetime > 0) retVal += "time remaining: "+(lifetime/10)+" sec~";
   if (charges > 0) retVal += "charges remaining: "+charges+"~";
   if (durability < 1) retVal += "chance of breaking: "+(100-(int)(durability*100))+"%~";
   if (weight > 0) retVal +="weight: "+weight+"~";
   return retVal;
 }

  // Removes all references to a particular index from the object contains. 
  public void removeObjIndex(int index) {
    for (int i=0; i<maxContains; i++) {
      if (contains[i] == index) {
        contains[i] = -1;
      } 
    }
  }

}