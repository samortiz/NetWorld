// ------------------------ CLASS EffectModifier ----------------------------
// This storage class contains one attribute and a value to modify that attribute
class EffectModifier {
  String var; // HP, Strength, weight etc... (any creature or obj attribute)
  String val; // how much to alter target attr by (can also be a variable)

  // ------ Constructors  and I/O -------
  EffectModifier() {
    var = "";
    val = "";
  } 
  EffectModifier(String var, String val) {
    this.var = var;
    this.val = val;
  }
  // String with form "var,val"
  EffectModifier(String inStr) {
    int index = inStr.indexOf(' ');
    if (index > 0) {
      var = inStr.substring(0,index).toLowerCase().trim();
      val = inStr.substring(index+1,inStr.length()).toLowerCase().trim();
    }
  }
  public String toString() {
    return var+" "+val;
  }
  
 // ---------   EffectModifier GET/SET  VAR/VAL -----------
 // Does the actual compare of a variable Name with a creature
  public static double calcVar(Creature creature, String varName) {
    double retVal = 0;
    try {retVal = Double.valueOf(varName).doubleValue();} 
    catch (NumberFormatException e) { // Only if it is not already a number
      if      (varName.equals("x"))              retVal = creature.x;
      else if (varName.equals("y"))              retVal = creature.y;
      else if (varName.equals("hitpoints"))      retVal = creature.hitPoints;
      else if (varName.equals("hitpointsmax"))   retVal = creature.hitPointsMax;
      else if (varName.equals("spellpoints"))    retVal = creature.spellPoints;
      else if (varName.equals("spellpointsmax")) retVal = creature.spellPointsMax;
      else if (varName.equals("gold"))           retVal = creature.gold;
      else if (varName.equals("movedelay"))      retVal = creature.moveDelay;
      else if (varName.equals("attackdelay"))    retVal = creature.attackDelay;
      else if (varName.equals("spelldelay"))     retVal = creature.spellDelay;
      else if (varName.equals("movedelaymax"))   retVal = creature.moveDelayMax;
      else if (varName.equals("attackdelaymax")) retVal = creature.attackDelayMax;
      else if (varName.equals("spelldelaymax"))  retVal = creature.spellDelayMax;
      else if (varName.equals("level"))          retVal = creature.level;
      else if (varName.equals("experience"))     retVal = creature.experience;
      else if (varName.equals("freeattributes")) retVal = creature.freeAttributes;
      else if (varName.equals("strength"))       retVal = creature.strength;
      else if (varName.equals("dexterity"))      retVal = creature.dexterity;
      else if (varName.equals("constitution"))   retVal = creature.constitution;
      else if (varName.equals("intelligence"))   retVal = creature.intelligence;
      else if (varName.equals("charisma"))       retVal = creature.charisma;
      else if (varName.equals("armorbludgeon"))  retVal = creature.armorBludgeon;
      else if (varName.equals("armorpierce"))    retVal = creature.armorPierce;
      else if (varName.equals("armorslash"))     retVal = creature.armorSlash;
      else if (varName.equals("armorfire"))      retVal = creature.armorFire;
      else if (varName.equals("armorice"))       retVal = creature.armorIce;
      else if (varName.equals("armormagic"))     retVal = creature.armorMagic;
      else if (varName.equals("baseattackrange"))retVal = creature.baseAttackRange;
      else if (varName.equals("damagebludgeon")) retVal = creature.damageBludgeon;
      else if (varName.equals("damagepierce"))   retVal = creature.damagePierce;
      else if (varName.equals("damageslash"))    retVal = creature.damageSlash;
      else if (varName.equals("damagefire"))     retVal = creature.damageFire;
      else if (varName.equals("damageice"))      retVal = creature.damageIce;
      else if (varName.equals("damagemagic"))    retVal = creature.damageMagic;
      else if (varName.equals("sightrange"))     retVal = creature.sightRange;
      else if (varName.equals("rand"))           retVal = Math.random();
    }
    return retVal; 
  }
  // Sets this creature's var to val according to varName
  public static void setVar(String varName, double val, Creature creature) {
      if      (varName.equals("x"))              creature.x = (int)val;
      else if (varName.equals("y"))              creature.y = (int)val;
      else if (varName.equals("hitpoints"))      creature.hitPoints = val;
      else if (varName.equals("hitpointsmax"))   creature.hitPointsMax = val;
      else if (varName.equals("spellpoints"))    creature.spellPoints = val;
      else if (varName.equals("spellpointsmax")) creature.spellPointsMax = val;
      else if (varName.equals("gold"))           creature.gold = val;
      else if (varName.equals("movedelay"))      creature.moveDelay = (int)val;
      else if (varName.equals("attackdelay"))    creature.attackDelay = (int)val;
      else if (varName.equals("spelldelay"))     creature.spellDelay = (int)val;
      else if (varName.equals("movedelaymax"))   creature.moveDelayMax = (int)val;
      else if (varName.equals("attackdelaymax")) creature.attackDelayMax = (int)val;
      else if (varName.equals("spelldelaymax"))  creature.spellDelayMax = (int)val;
      else if (varName.equals("level"))          creature.level = (int)val;
      else if (varName.equals("experience"))     creature.experience = (long)val;
      else if (varName.equals("freeattributes")) creature.freeAttributes = val;
      else if (varName.equals("strength"))       creature.strength = val;
      else if (varName.equals("dexterity"))      creature.dexterity = val;
      else if (varName.equals("constitution"))   creature.constitution = val;
      else if (varName.equals("intelligence"))   creature.intelligence = val;
      else if (varName.equals("charisma"))       creature.charisma = val;
      else if (varName.equals("armorbludgeon"))  creature.armorBludgeon = val;
      else if (varName.equals("armorpierce"))    creature.armorPierce = val;
      else if (varName.equals("armorslash"))     creature.armorSlash = val;
      else if (varName.equals("armorfire"))      creature.armorFire = val;
      else if (varName.equals("armorice"))       creature.armorIce = val;
      else if (varName.equals("armormagic"))     creature.armorMagic = val;
      else if (varName.equals("baseattackrange"))creature.baseAttackRange = val;
      else if (varName.equals("damagebludgeon")) creature.damageBludgeon = val;
      else if (varName.equals("damagepierce"))   creature.damagePierce = val;
      else if (varName.equals("damageslash"))    creature.damageSlash = val;
      else if (varName.equals("damagefire"))     creature.damageFire = val;
      else if (varName.equals("damageice"))      creature.damageIce = val;
      else if (varName.equals("damagemagic"))    creature.damageMagic = val;
      else if (varName.equals("sightrange"))     creature.sightRange = val;
  }

  // Sets a creature's var to val can operate on caster or target depending on varName (c=caster t=target)
  public static void setVar(String varName, double val, Creature caster, Creature target) {
    if (varName.length() > 1) {
      String prefix = varName.substring(0,1);
      if (prefix.equals("c")) setVar(varName.substring(1),val,caster);
      else if (prefix.equals("t")) setVar(varName.substring(1),val,target);
    }
  }
 // Returns the value of a variable (with only one creature it ignores the caster/target switch)
 public double getVar(Creature creature) {
  if (var.length() > 1) return calcVar(creature,var.substring(1));
  else return 0;
 }
 // Returns the val (with only one creature it ignores the caster/target switch)
 public double getVal(Creature creature) {
  try {return Double.valueOf(val).doubleValue();} 
  catch (NumberFormatException e) {
    if (val.length() > 1) return calcVar(creature,val.substring(1));
    else return 0;
  }
 }
 // Returns the value of var from the appropriate creature (c=caster t=target)
 public double getVar(Creature caster, Creature target) {
   if (var.length() > 1) {
     String prefix = var.substring(0,1);
     if      (var.equals("rand")) return calcVar(caster,var);
     else if (prefix.equals("c")) return calcVar(caster,var.substring(1));
     else if (prefix.equals("t")) return calcVar(target,var.substring(1));
     else return 0;
   } else return 0;
 }
 // Returns the val (usually just a number, but it could  be a variable of a caster or target
 public double getVal(Creature caster, Creature target) {
  try {return Double.valueOf(val).doubleValue();} 
  catch (NumberFormatException e) {
    if (val.length() > 1) {
      String prefix = val.substring(0,1);
      if (prefix.equals("c")) return calcVar(caster,val.substring(1));
      else if (prefix.equals("t")) return calcVar(target,val.substring(1));
      else return 0;
    } else return 0;
  }
 }

 
 
}

// ---------------------- CLASS EFFECT --------------------------------------

// This contains a specific action for a spell to do.  It modifies one attribute
// of a Creature or Object, or it can create one Creature or Object
class Effect {
  public static final int maxMods = 10;
  
  // If Modifying target 
  String type;
  int numMods; 
  EffectModifier[] mods = new EffectModifier[maxMods];
  double modVal; // how much the target was actually altered


  // ------------- Effect Constructors and IO --------------------------
  Effect() {
    clearValues();
  }
  Effect(String inStr) {
    clearValues();
    String word = "";
    int index = inStr.indexOf(' ');
    if (index > 0) word = inStr.substring(0,index).toLowerCase().trim();
    type = word;
    int endIndex = inStr.indexOf(' ',index+1);
    if (endIndex > 0) word = inStr.substring(index+1,endIndex).toLowerCase().trim();
    if (word.equals("createobj") || word.equals("createcreature")) {
      mods[0] = new EffectModifier(word,inStr.substring(endIndex+1, inStr.length()));
      numMods = 1;
    } else { // Not a create statement (a variable modification)
      int start = inStr.indexOf(' ')+1; // ignore the first word
      int end = inStr.indexOf(' ',start);
      String var = "";
      String val = "";
      while (end > 0) {
        var = inStr.substring(start,end).toLowerCase().trim();
        start = end+1;
        end = inStr.indexOf(' ',start);
        if (end < 0) end = inStr.length(); // no trailing space required
        if (end > 0) {
          val = inStr.substring(start,end);
          mods[numMods] = new EffectModifier(var,val);
          numMods += 1;
          start = end+1;
          end = inStr.indexOf(' ',start);
        }
      }
    }
    
  }

  // Clears all variables to their defaults  
  private void clearValues() {
    type = "";
    numMods = 0;
    modVal = 0;
  }

  public String toString() {
    String retVal = "("+type;
    for (int i=0; i< numMods; i++) {
      retVal += " "+mods[i].toString();
    }
    return retVal+")";
  }
  
  // ------------ Apply Effect --------------
  // when contains cast, temp, cont, dispell (only effect is if when = dispell, then temp
  // effects are reversed)
  public EffectModifier applyEffect(Creature caster, Creature target, String when) {
    EffectModifier retVal = new EffectModifier();
    if (numMods > 0) {
     String theVar = mods[0].var; // First Mod is the variable
     if (theVar.equals("createcreature")) {
       String thisVal = mods[0].val.substring(1,(mods[0].val.length()-1)); // remove ()
       thisVal = "x="+target.x+", y="+target.y+","+" clan="+target.clan+", "+thisVal; // add x,y and clan from target
       return new EffectModifier(theVar, thisVal); 
     }
     if (theVar.equals("createobj")) {
       String thisVal = mods[0].val.substring(1); // remove first (
       thisVal = "(x="+target.x+", y="+target.y+", onmap=true, "+thisVal;
       return new EffectModifier(theVar, thisVal); 
     }
     if (when.equals("dispell") && type.equals("temp")) { // Undo the initial effect
      double origVal = mods[0].getVar(caster,target); // gets the original value of the variable from the creature
      double newVal = origVal - modVal; // modVal set when casting
      EffectModifier.setVar(theVar, newVal, caster, target); // set the var
      return new EffectModifier(); // return nothing
     }
     double theVal = mods[0].getVal(caster,target); // base value added
     double origVal = mods[0].getVar(caster,target); // gets the original value of the variable from the creature
     double postMax =  99999999;
     double postMin = -99999999;
     double preMax  =  99999999;
     double preMin  = -99999999;
     for (int i=1; i<numMods; i++) {
       if      (mods[i].var.equals("postmax")) postMax = mods[i].getVal(caster,target); 
       else if (mods[i].var.equals("postmin")) postMin = mods[i].getVal(caster,target);
       else if (mods[i].var.equals("premax"))  preMax  = mods[i].getVal(caster,target);
       else if (mods[i].var.equals("premin"))  preMin  = mods[i].getVal(caster,target);
       else theVal += (mods[i].getVar(caster,target) * mods[i].getVal(caster,target));
     }
     if (theVal > preMax) theVal = preMax;
     if (theVal < preMin) theVal = preMin;
     if (theVar.equals("attackbludgeon")) return new EffectModifier(theVar, theVal+"");
     if (theVar.equals("attackpierce"))   return new EffectModifier(theVar, theVal+"");
     if (theVar.equals("attackslash"))    return new EffectModifier(theVar, theVal+"");
     if (theVar.equals("attackfire"))     return new EffectModifier(theVar, theVal+"");
     if (theVar.equals("attackice"))      return new EffectModifier(theVar, theVal+"");
     if (theVar.equals("attackmagic"))    return new EffectModifier(theVar, theVal+"");
     if (when.equals("cast")) modVal = theVal; // recovered when dispell
     double totalVal = origVal + theVal;
     if ((origVal >= postMax) && (theVal > 0)) totalVal = origVal; // if adding to a value already above the max leave it at it's original value
     if ((origVal < postMax) && (totalVal > postMax)) totalVal = postMax; // if adding to a value that is below the max (and trying to move it above the max) set attr to postMax
     if ((origVal <= postMin) && (theVal <0)) totalVal = origVal;
     if ((origVal > postMin) && (totalVal < postMin)) totalVal = postMin;
     EffectModifier.setVar(theVar, totalVal, caster, target);
    }
    return retVal;
  }
  
}



// ---------------------------- CLASS SPELL ---------------------------------

// This is used for spells, and also for object effects (using objects)
public class Spell {
  public static final int maxRequirements = 10;
  public static final int maxEffects = 20;
  
  // Variables set and used Internally
  boolean exists;
  boolean isRunning;
  boolean isCont;
  
  // Variables set and used Externally (by worldClass)
  public String cType; // creature, player
  public int cIndex;
  public String tType; // creature, player, obj
  public int tIndex;
  public int x;  // -1 or x position of spell
  public int y;  // -1 or y position of spell
  
  // Variables set by User
  String description; // some text describing the spell effects/costs etc... use ~ to seperate lines of text (do NOT use , or () in the description!)
  long lifetime; // how long the spell lasts -1 is forever, 0 is instant (vanishes immediately, used for instant spells)
  long lifetimeMax; // value used to reset the spell lifetime when cast
  String targetType; //(creature, obj, self, xy) // set by user to limit target Types
  double range; // allowed distance from caster
  double areaEffect; // if targetType=xy range of creatures effected (NO mass obj spells)
  int numRequirements;
  EffectModifier[] requirements = new EffectModifier[maxRequirements];  
  int numEffects;
  Effect[] effects = new Effect[maxEffects];

 // ---------------------Spell CONSTRUCTORS and IO -------------------------
  Spell() {
    clearValues();
  }
  // inStr is "(description,lifetime,casterType,targetType,range,areaEffect,((Requirement)(Requirement)),((Effect)(Effect)))"  
  Spell (String inStr) {
    clearValues();
    int start = 1;
    int end = inStr.indexOf(',');
    if (end > 0) {
      description = inStr.substring(start,end).trim();
      start = end+1;
      end = inStr.indexOf(',',start);
      if (end > 0) try{lifetime = Long.valueOf(inStr.substring(start,end)).longValue();
                       lifetimeMax = lifetime; } catch (NumberFormatException e) {}
      start = end+1;
      end = inStr.indexOf(',',start);
      if (end > 0) targetType = inStr.substring(start,end).toLowerCase().trim();
      start = end+1;
      end = inStr.indexOf(',',start);
      if (end > 0) try{range = Double.valueOf(inStr.substring(start,end)).doubleValue();} catch (NumberFormatException e) {}
      start = end+1;
      end = inStr.indexOf(',',start);
      if (end > 0) try{areaEffect = Double.valueOf(inStr.substring(start,end)).doubleValue();} catch (NumberFormatException e) {}
      // parse the Requirements array
      start = inStr.indexOf('(',end);
      end = worldClass.closeBracket(inStr, start);
      String req = "";
      if ((start>=0) && (end >=0)) req = inStr.substring(start+1,end);
      int inStart = req.indexOf('(');
      int inEnd = worldClass.closeBracket(req,inStart);
      while (inEnd > 0) {
        requirements[numRequirements] = new EffectModifier(req.substring(inStart+1,inEnd)); // don't send the ()
        numRequirements += 1;
        inStart = req.indexOf('(',inEnd);
        inEnd = worldClass.closeBracket(req,inStart);
      }
      // parse the effects
      start = inStr.indexOf(',',end); // find the next ,
      start = inStr.indexOf('(',start); // find the ( after the ,
      end = worldClass.closeBracket(inStr, start);
      String eff = "";
      if( (start>=0) && (end>0)) eff = inStr.substring(start+1,end);
      inStart = eff.indexOf('(');
      inEnd = worldClass.closeBracket(eff,inStart);
      while (inEnd > 0) {
        effects[numEffects] = new Effect(eff.substring(inStart+1,inEnd));
        if (effects[numEffects].type.equals("cont")) isCont = true;
        numEffects += 1;
        inStart = eff.indexOf('(',inEnd);
        inEnd = worldClass.closeBracket(eff,inStart);
      }
      if (numEffects > 0) exists = true;
    }
  }

 // Clears all the values to the defaults
  private void clearValues() {
    exists = false;
    isRunning = false;
    isCont = false;
    description = "";
    lifetime = -1; // forever
    lifetimeMax = -1; // forever
    range = 0;
    areaEffect=1;
    targetType = "";
    numRequirements = 0;
    numEffects = 0;
  }

  public String toString() {
    String retVal = "("+description+","+lifetimeMax+","+targetType+","+range+","+areaEffect;
    retVal += ",(";
    for (int i=0; i<numRequirements; i++) {
      retVal += "("+requirements[i].toString()+")";
    }
    retVal += "),(";
    for (int i=0; i<numEffects; i++ ) {
      retVal += effects[i].toString();
    }
    retVal += "))";
    if (numEffects >0) return retVal;
    else return "()";
  }
  
  
   // ----------------------- CAST SPELL ------------------------
  // when contains
  // cast : first time cast, set all necessary vars
  // cont : continuing spell effects
  // dispell : end spell, undo temp effects, and run delay effects
  public EffectModifier[] cast(Creature caster, Creature target, String when) {
    EffectModifier[] tempVal = new EffectModifier[maxEffects];
    EffectModifier[] retVal;
    int loopCounter = 0;
    
    if ( targetType.equals("creature") || targetType.equals("xy") || 
        (targetType.equals("self") && (caster == target))) {
      if (validateRequirements(caster)) {
        for (int i=0; i<numEffects; i++){
          if (effects[i].type.equals(when) ||
              (when.equals("cast") && effects[i].type.equals("temp")) ||
              (when.equals("dispell") && effects[i].type.equals("temp"))) {
            EffectModifier e = effects[i].applyEffect(caster,target,when);
            if (e.var.length() > 0) {
              tempVal[loopCounter] = e;
              loopCounter += 1;
            }
          }
        }
        if (when.equals("cast")) {
          lifetime = lifetimeMax;
          isRunning = true;
        } else  if (when.equals("dispell")) {
          lifetime = 0;
          isRunning = false;
        }
      }
    }
    
    if (loopCounter > 0) {
      retVal = new EffectModifier[loopCounter];
      for (int i=0; i<loopCounter; i++) {
        retVal[i] = tempVal[i];
      }
    } else retVal = new EffectModifier[0];
         
    return retVal;
  }
  
  // TODO: Cast spell on object
  public EffectModifier[] cast(Creature caster, Obj target) {
    return null;
  }

  private boolean validateRequirements(Creature caster) {
    boolean retVal = true;
    for (int i=0; i<numRequirements; i++){
      double var = requirements[i].getVar(caster);
      double val = requirements[i].getVal(caster);
      if (val < 0) { // val is negative, so do a less than compare (var must be less than val)
        if (var >= (val * -1)) {
          retVal = false;
          break;
        }
      } else { // val is positive, so do a greater than compare (var must be equal to or greater than val)
        if (var < val) {
          retVal = false;
          break;
        }
      }
    }
    return retVal;
  }
  
}