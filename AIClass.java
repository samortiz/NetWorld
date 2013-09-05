// ---------------- CLASS AI Command ----------------------
// Can be an Action or a Condition depending on the type
class AICommand {
  public static final int maxWords = 100;
  public String[] word = new String[maxWords];
  public int numWords = 0;
  
  // Constructors
  AICommand() {}
  // Sets all the variables from a string (comma seperated)
  AICommand (String inStr) {
    for (int i=0; i<maxWords; i++) word[i] = "";// Clear all the words
    int start = 0;
    int end = inStr.indexOf(' ');
    if (end <0) end = inStr.length();
    int loopCounter = 0;
    while ((end>0) && (loopCounter < maxWords)) {
      String thisStr = inStr.substring(start,end);
      word[loopCounter] = thisStr;
      start = end+1;
      if (start >0) end = inStr.indexOf(' ',start);
      if (end<0) if (start < inStr.length()) end = inStr.length(); // get the last value (no space at the end)
      loopCounter += 1;
    }
    numWords = loopCounter;
  }

  // Returns a string with all the variables
  public String toString() {
    String retVal = "";
    for (int i=0; i<maxWords; i++) {
      retVal += " "+word[i];
    }
    retVal = retVal.trim(); // remove preceeding or trailing spaces
    return retVal;
  }
}


// ------------------------- CLASS AIRule -----------------------------
// This contains a series of conditions and an action to do if they are all true
class AIRule {
  public static final int maxAICommands = 100;
  public AICommand[] commands = new AICommand[maxAICommands];
  public int numCommands =0;

  AIRule() {}
  AIRule(String inStr) {
    int start = 0;
    int end = inStr.indexOf('~');
    if (end<0) end = inStr.length();    
    int loopCounter = 0;
    while ((end > 0) && (loopCounter < maxAICommands)) {
      if (end>0) {
        commands[loopCounter] = new AICommand(inStr.substring(start,end));
        loopCounter += 1;
      }
      start = end+1;
      end = inStr.indexOf('~',start);
      if ((start<inStr.length()) && (end<0)) end = inStr.length();
    }
    numCommands = loopCounter;
  }
  
  public String toString() {
    String retVal = "";
    for (int i=0; i<numCommands; i++) {
      if (i>0) retVal += "~"; // don't put a ~ in front of the first one
      retVal += commands[i].toString();
    }
    return retVal;
  }
  
}

// ------------------------- CLASS CreatureView --------------------------
class CreatureView {
  public String userName = "";
  public int x = -1;
  public int y = -1;
  public double HPPct = 0;
  public double SPPct = 0;
  public double distance = 0; // distance from Me!
  public String clan = "";
  
  CreatureView() {}
}

// ----------------------- CLASS KnownCreature ---------------------------

// Contains internal and external information about a creature. 
class KnownCreature {
  public static final int maxVars = 100;

  // External (set by View)
  public String userName;
  public int x;
  public int y;
  public double HPPct;
  public double SPPct;
  public double distance; // distance from Me!
  public String clan;
  
  // Internal (set by AI)
  public boolean visible; 
  public double[] vars;
  public int totalAttackedCount;
  public double totalAttackedDmg;
  public int attackedCount;
  public double attackedDmg;
  public int clanAttackedCount;
  public double clanAttackedDmg;
  public String tellMessage;
  
  KnownCreature() {
    userName = "";
    x = -1;
    y = -1;
    HPPct = 0;
    SPPct = 0;
    distance = 0;
    clan = "";
    visible = false; 
    vars = new double[maxVars];
    totalAttackedCount = 0;
    totalAttackedDmg = 0;
    attackedCount = 0;
    attackedDmg = 0;
    clanAttackedCount = 0;
    clanAttackedDmg = 0;
    tellMessage = "";
  }
  
  // Methods
  public void setView(CreatureView view) {
    userName = view.userName.toLowerCase().trim();
    x = view.x;
    y = view.y;
    HPPct = view.HPPct;
    SPPct = view.SPPct;
    distance = view.distance;
    clan = view.clan;
    visible = true; // from a view
  }

}

// ------------------------- CLASS AIAction -----------------------------
class AIAction {
  public String type;
  public int x;
  public int y;
  public String userName;
  public String details;
  
  AIAction() {
    type = "";
    x = -1;
    y = -1;
    userName = "";
    details = "";
  }
}

// ------------------------- CLASS AI --------------------------------

public class AIClass {
  public static final int maxAIRules = 100;
  public static final int maxStates = 100;
  public static final int maxKnownCreatures = 1000;
  
  // Rules (which control behavior)
  int numRules = 0;
  AIRule[] rules = new AIRule[maxAIRules];

  // Sight
  int numKnownCreatures = 0;
  KnownCreature[] knownCreatures = new KnownCreature[maxKnownCreatures];

  // Knowledge Base (what the AI "knows")
  double[] state = new double[maxStates];
  int x = -1;
  int y = -1;
  double inRange = 0;
  double allInRange = 0;
  double HP = 0;
  double HPMax = 0;
  double HPPct = 0;
  double SP = 0;
  double SPMax = 0;
  double SPPct = 0;
  int moveDelay = 0;
  int attackDelay = 0;
  int spellDelay = 0;
  String clan = "";
  String userName = "";
  
  AIClass() {for (int i=0; i<maxStates; i++) state[i] = 0;}
  AIClass (String stringIn) {
    // clear all the states (starting new AI)
    for (int i=0; i<maxStates; i++) state[i] = 0;
    // inStr should be of the form : ConditionStr~ConditionStr~ActionStr()ConditionStr~ActionStr
    String inStr = stringIn.trim();
    int start = 0;
    int end = inStr.indexOf("()");
    if (end<0) end = inStr.length();
    int loopCounter = 0;
    while ((end>0) && (loopCounter < maxAIRules)) {
      rules[loopCounter] = new AIRule(inStr.substring(start,end));
      loopCounter += 1;
      start = end+2;
      end = inStr.indexOf("()",start);
      if ((start<inStr.length()) && (end<0)) end = inStr.length();
    }
    numRules = loopCounter;
  }
  
  public String toString() {
    String retVal = "";
    for (int i=0; i<numRules; i++) {
      if (i>0) retVal += "()"; // no () in front of first one
      retVal += rules[i].toString();
    }
    return retVal;
  }

  // returns the index of a creature looked up by it's userName 
  // returns -1 if it can't find it
  private int getKnownCreatureIndex(String inStr) {
    int retVal = -1;
    String creatureName = inStr.toLowerCase().trim();
    for (int i=0; i<numKnownCreatures; i++) {
      if (knownCreatures[i].userName.toLowerCase().trim().equals(creatureName)) {
        retVal = i;
        break;
      }
    }
    return retVal;
  }


  public void observe(CreatureView[] views, int viewCount, Creature self, double range, double allInRange) {
    // Nothing is visible
    for (int i=0; i< numKnownCreatures; i++) knownCreatures[i].visible = false;
    // set the views (and make the visible creatures visible)
    for (int i=0; i< viewCount; i++ ) {
      int index = getKnownCreatureIndex(views[i].userName);
      if (index < 0) {
        index = numKnownCreatures;
        knownCreatures[index] = new KnownCreature(); // create a new creature
        numKnownCreatures += 1;
      }
      knownCreatures[index].setView(views[i]);
    }
    this.inRange = range;
    this.allInRange = allInRange;
    HP = self.hitPoints;
    HPMax = self.hitPointsMax;
    HPPct = (HP/HPMax)*100;
    SP = self.spellPoints;
    SPMax = self.spellPointsMax;
    SPPct = (SP/SPMax)*100;
    moveDelay = self.moveDelay;
    attackDelay = self.attackDelay;
    spellDelay = self.spellDelay;
    x = self.x;
    y = self.y;
    clan = self.clan;
    userName = self.userName;
  }

  // This method is called when this creature is attacked, or when this creature's clan is attacked
  // attackType = personal, or clan
  // userName = if player: userName   if creature userName+'-'+index
  public void attackedBy(String inAttackType, String userName, double damage) {
    String attackType = inAttackType.toLowerCase().trim();
    int whoIndex = -1;
    whoIndex = getKnownCreatureIndex(userName);
    if (whoIndex >= 0) {
      if (attackType.equals("personal")) {
        knownCreatures[whoIndex].attackedCount += 1;
        knownCreatures[whoIndex].attackedDmg += damage;
      } else if (attackType.equals("clan")) {
        knownCreatures[whoIndex].clanAttackedCount += 1;
        knownCreatures[whoIndex].clanAttackedDmg += damage;
      }
      knownCreatures[whoIndex].totalAttackedCount += 1;
      knownCreatures[whoIndex].totalAttackedDmg += damage;
    }
  }

  public void tellMessage(String userName, String message) {
    int whoIndex = getKnownCreatureIndex(userName);
    if (whoIndex >=0) {
      knownCreatures[whoIndex].tellMessage = message;
    }
  }

  // returns a double representation of the string,
  // substitutes any variables strings that it might find.
  // will return 0 if it encounters any errors.
  private double getValue(String inString, KnownCreature c) {
    String inStr = inString.toLowerCase().trim();
    double retVal = 0;
    if      (inStr.equals("random"))   retVal = Math.random(); // number in range [0,1)
    else if (inStr.equals("distance")) retVal = c.distance;
    else if (inStr.equals("clan"))     retVal = (double)c.clan.hashCode(); // converted to int then double for comparison
    else if (inStr.equals("attackedcount"))      retVal = c.attackedCount;
    else if (inStr.equals("totalattackedcount")) retVal = c.totalAttackedCount;
    else if (inStr.equals("clanattackedcount"))  retVal = c.clanAttackedCount;
    else if (inStr.equals("attackeddmg"))      retVal = c.attackedDmg;
    else if (inStr.equals("totalattackeddmg")) retVal = c.totalAttackedDmg;
    else if (inStr.equals("clanattackeddmg"))  retVal = c.clanAttackedDmg;
    else if (inStr.equals("x"))      retVal = (double)c.x;
    else if (inStr.equals("y"))        retVal = (double)c.y;
    else if (inStr.equals("hppct"))    retVal = c.HPPct;
    else if (inStr.equals("sppct"))    retVal = c.SPPct;
    else if (inStr.equals("username")) retVal = (double)c.userName.hashCode();
    else if ((inStr.length() > 3) && (inStr.substring(0,3).equals("var"))) {
      int whichVar = -1;
      try {whichVar = Integer.valueOf(inStr.substring(3)).intValue();} catch (NumberFormatException e) {System.out.println("Invalid int for var: "+inStr);}
      if (whichVar >= 0) retVal = c.vars[whichVar];
    } else if (inStr.equals("myx"))          retVal = x;
    else if (inStr.equals("myy"))          retVal = y;
    else if (inStr.equals("myinrange"))    retVal = inRange;
    else if (inStr.equals("myallinrange")) retVal = allInRange;
    else if (inStr.equals("myhp"))    retVal = HP;
    else if (inStr.equals("myhpmax")) retVal = HPMax;
    else if (inStr.equals("myhppct")) retVal = HPPct;
    else if (inStr.equals("mysp"))    retVal = SP;
    else if (inStr.equals("myspmax")) retVal = SPMax;
    else if (inStr.equals("mysppct")) retVal = SPPct;
    else if (inStr.equals("mymovedelay")) retVal = moveDelay;
    else if (inStr.equals("myattackdelay")) retVal = attackDelay;
    else if (inStr.equals("myspelldelay")) retVal = spellDelay;
    else if (inStr.equals("myclan")) retVal = (double)clan.hashCode();
    else if (inStr.equals("myusername")) retVal = (double)userName.hashCode();
    else if ((inStr.length() > 7) && (inStr.substring(0,7).equals("mystate"))) {
      int which = -1;
      try {which = Integer.valueOf(inStr.substring(7)).intValue();} catch (NumberFormatException e) {System.out.println("Invalid int for state: "+inStr);}
      if (which >= 0) retVal = state[which];
    // As a last resort try to cast to a double, and if that failes hashCode the string to a double. (so the user can do string equality tests in cases like userName and clan)
    } else try {retVal = Double.valueOf(inStr).doubleValue();} catch (NumberFormatException e) {retVal = (double)inStr.hashCode();}
   
   
   return retVal;
  }

  // Applies the string op to the two double parameters
  // returns false if the operator is unknown or if the value is not true
  private boolean applyOp(double var, String op, double val) {
    if (((op.equals("<"))  && (var < val))  ||
        ((op.equals(">"))  && (var > val))  ||
        ((op.equals("="))  && (var == val)) || 
        ((op.equals(">=")) && (var >= val)) ||
        ((op.equals("<=")) && (var <= val)) ||
        ((op.equals("!=")) && (var != val)))  {
      return true;
    } else return false;
  }

  // gets an array of indexes (in knownCreatures) to creatures that match the 
  // parameters specified
  private int[] getTargets(int[] targets, String var, String inOp, String val) {
    String op = inOp.toLowerCase().trim();
    int[] retVal;
    int[] tempVal = new int[targets.length];
    int numFound = 0;
   
    for (int i=0; i<targets.length; i++) {
      if ((targets[i] >= 0) && (knownCreatures[targets[i]].visible)) {
        // Setting var
        if (op.equals("contains") || op.equals("notcontains")) {
          String thisVar = var.toLowerCase().trim();
          String thisVal = val.toLowerCase().trim();
          if (thisVar.equals("tell")) thisVar = knownCreatures[targets[i]].tellMessage;
          thisVar = thisVar.toLowerCase().trim();
          if ((thisVar.length() > 0) &&
             ((op.equals("contains") && ((thisVar.indexOf(thisVal) >= 0) || thisVar.equals(thisVal))) ||
              (op.equals("notcontains") && (thisVar.indexOf(thisVal) < 0)))) {
            tempVal[numFound] = targets[i];
            numFound += 1;
          }
        } else { // a regular operator
          double thisVar = getValue(var,knownCreatures[targets[i]]);
          double thisVal = getValue(val,knownCreatures[targets[i]]);
          if (applyOp(thisVar, op, thisVal)) {
            tempVal[numFound] = targets[i];
            numFound += 1;
          }
        }
      }
    }
    // copy tempVal into retVal (an appropriately sized array)
    retVal = new int[numFound];
    for (int i=0; i< numFound; i++) {
      retVal[i] = tempVal[i];
    }
    return retVal;
  }

  // Searches through the target array and returns a single target
  // that has the min or max variable value for the variable named var
  private int getSingleTarget(int[] targets, String inMinMax, String var) {
    String minMax = inMinMax.toLowerCase().trim();
    int retTarget = -1;
    double val = 0;
    double extremeVal = 0;
    if (minMax.equals("min")) extremeVal = 99999999;
    else if (minMax.equals("max")) extremeVal = -99999999;
    for (int i=0; i<targets.length; i++) {
      if (targets[i] >= 0) val = getValue(var,knownCreatures[targets[i]]);
      if ((minMax.equals("min") && (val < extremeVal)) ||
          (minMax.equals("max") && (val > extremeVal)))  {
        extremeVal = val;
        retTarget = i;
      } 
    }
    if (retTarget >= 0) return targets[retTarget];
    else if (targets.length > 0) return targets[0];
    else return -1;
  }

  // Searches the command for a sortby clause, and if one is found
  // it will return a single int from the targets array sorted by the
  // sortby clause. 
  private int getSingleTarget(int[] targets, AICommand command) {
    int retVal = -1;
    String sortDir = "";
    String sortVar = "";
    String type = command.word[0].toLowerCase().trim();
    if (type.equals("iftarget")) { // the only type that can be sorted
      if (targets.length == 1) return targets[0];
      int wordIndex = 1;
      while (wordIndex < command.numWords) {
        if (command.numWords < wordIndex+2) return -100; // invalid command (wordgroups of 3)
        if (command.word[wordIndex].toLowerCase().trim().equals("sortby")) {
         sortDir = command.word[wordIndex+1];
         sortVar = command.word[wordIndex+2];
         break;
        };
        wordIndex += 3; // groups of 3
      }
      if ((sortDir.length() > 0) && (sortVar.length() >0)) {
        return getSingleTarget(targets,sortDir, sortVar);
      } else if (targets.length > 0) return targets[0];
    }
    return retVal;
  }

  // checks to see if the condition is true (var op val).
  private boolean validCondition(String var, String op, String val) {
    KnownCreature nobody = new KnownCreature(); // just for getValue
    double thisVar = getValue(var,nobody);
    double thisVal = getValue(val,nobody);
    return applyOp(thisVar, op, thisVal);
  }


  // Checks to see if a command is true 
  // returns -100 if the command is false or is a target command that does not resolve any targets
  // returns -1 if the command is true and does not resolve to a target
  // returns the target if the command resolves a target
  // returns -1 if empty, and -100 if invalid
  private int isTrue(AICommand command) {
    int retVal = -1;
    String type = "";
    int[] targets = new int[numKnownCreatures];
    int wordIndex = 0;
    if (command.numWords == 0) return -1; // nothing here!
    type = command.word[0].toLowerCase().trim();
    for (int i=0; i<numKnownCreatures; i++) targets[i] = i; // set up the targets array to default to all known creatures
    if (!type.substring(0,2).equals("if")) return -1; // it's a command not a condition (therefore always true)
    if (type.equals("iftarget")) { // if it needs to resolve to a target
      wordIndex = 1;
      while (wordIndex < command.numWords) {
        if (command.numWords < wordIndex+2) return -100; // invalid command (wordgroups of 3)
        targets = getTargets(targets,command.word[wordIndex],command.word[wordIndex+1],command.word[wordIndex+2]);
        wordIndex += 3; // groups of 3
      }
      if (targets.length == 0) retVal = -100; // No targets found!
      else if (targets.length > 0) retVal = getSingleTarget(targets,command);
    } else if (type.equals("ifmy")) {
      wordIndex = 1;
      boolean allTrue = true;
      while (allTrue && (wordIndex < command.numWords)) {
        if (command.numWords < wordIndex+2) return -100; // invalid command (wordgroups of 3)
        allTrue = validCondition(command.word[wordIndex],command.word[wordIndex+1],command.word[wordIndex+2]);
        wordIndex += 3; // groups of 3
      }
      if (allTrue == false) retVal = -100; // No targets found!
      // otherwise retVal will be -1 (all True with no target returned)
    }
    
    return retVal;
  }

  // Checks to see if all the commands in a rule are true and returns the target.
  // It will return the target if a target is found. 
  // returns -1 if a target is not found, but all conditions are true
  // will return -100 if any condition is found to be false, or a target expression
  // does not return a target.  
  // - actions are always true.
  private int getTarget(AIRule rule) {
    int retVal = -1;
    for (int i=0; i<rule.numCommands; i++) {
      int target = isTrue(rule.commands[i]); // checks if the command is true (actions are true)
      if (target == -100) return -100; // return an error
      if ((retVal == -1) && (target >= 0)) { // if target found and no previous targets found (only return the first target)
        retVal = target;
      } 
    }
    return retVal;
  }
  

   // Will return the string up,down,left or right as appropriate to move fromx,y tox,y
  private String getDir(int fromX, int fromY, int toX, int toY) {
    int theX = toX - fromX;
    int theY = toY - fromY;
    if (Math.abs(theX) > Math.abs(theY)) { // Move on X axis
      if (theX >= 0) return "right";
      else return "left";
    } else { // Move on Y axis
      if (theY >= 0) return "down";
      else return "up";
    }
  }

  // Will return an action that is generated by finding the first action command in the rule
  // and applying the target to it (as necessary)
  private AIAction getAction(AIRule rule, int target) {
    AIAction retVal = new AIAction();
    int commandIndex = -1;
    // for each command in the rule check to see if it's an action
    for (int i=0; i< rule.numCommands; i++) {
      if ((rule.commands[i].word[0].length() > 2) && (!rule.commands[i].word[0].substring(0,2).toLowerCase().equals("if"))) { // this command is an action
        // Check for commands that do not return anything to the server
        if ((rule.commands[i].word[0].length() > 2)&&(rule.commands[i].word[0].substring(0,3).toLowerCase().equals("var"))) { // VAR command (set target variable)
          int varIndex = -1;
          try { varIndex = Integer.valueOf(rule.commands[i].word[0].substring(3)).intValue();} catch (NumberFormatException e) {}
          if ((varIndex >= 0) && (varIndex < KnownCreature.maxVars) && // if a valid index was found
              (rule.commands[i].numWords == 3)) { // and this is a valid command
            String op = rule.commands[i].word[1].toLowerCase().trim();
            double val = 0;
            try {val = Double.valueOf(rule.commands[i].word[2]).doubleValue();} catch (NumberFormatException e) {}
            if      (op.equals("set")) knownCreatures[target].vars[varIndex] = val;
            else if (op.equals("add")) knownCreatures[target].vars[varIndex] += val;
            else if (op.equals("sub")) knownCreatures[target].vars[varIndex] -= val;
          }
        } else if ((rule.commands[i].word[0].length() > 7) && (rule.commands[i].word[0].substring(0,7).toLowerCase().equals("mystate"))) {
          // Setting my state variable
          int stateIndex = -1;
          try { stateIndex = Integer.valueOf(rule.commands[i].word[0].substring(7)).intValue();} catch (NumberFormatException e) {}
          if ((stateIndex >= 0) && (stateIndex < maxStates) && // if a valid index was found
              (rule.commands[i].numWords == 3)) { // and this is a good command, eg: stateX set 3 
            String op = rule.commands[i].word[1].toLowerCase().trim();
            double val = 0;
            try {val = Double.valueOf(rule.commands[i].word[2]).doubleValue();} catch (NumberFormatException e) {}
            if      (op.equals("set")) state[stateIndex] = val;
            else if (op.equals("add")) state[stateIndex] += val;
            else if (op.equals("sub")) state[stateIndex] -= val;
          }
        } else { // a returnable command
          commandIndex = i; // only get the last returnable command
        }
      }
    }
    
    if (commandIndex >= 0) {
      retVal.type = rule.commands[commandIndex].word[0].toLowerCase().trim(); // Command name
      if (target >=0) { // Basic target information set for all Commands  (this is all Attack needs)
        retVal.x = knownCreatures[target].x;
        retVal.y = knownCreatures[target].y;
        retVal.userName = knownCreatures[target].userName;
      }
      if (retVal.type.equals("move")) {
        if (rule.commands[commandIndex].numWords >= 2) { // valid Move
          if ((rule.commands[commandIndex].numWords == 3) && (target >= 0)) { 
            if (rule.commands[commandIndex].word[1].toLowerCase().trim().equals("towards")) retVal.details = getDir(x,y,retVal.x,retVal.y);// Move towards target
            else if (rule.commands[commandIndex].word[1].toLowerCase().trim().equals("away")) retVal.details = getDir(retVal.x,retVal.y,x,y);// Move away from  target
          } else if (rule.commands[commandIndex].word[1].toLowerCase().trim().equals("random")) {
            int r = (int)Math.floor(Math.random()*4); // get random 0-3;
            if      (r == 0) retVal.details = "up";
            else if (r == 1) retVal.details = "down";
            else if (r == 2) retVal.details = "left";
            else if (r == 3) retVal.details = "right";
          } else retVal.details = rule.commands[commandIndex].word[1]; // Up, down, left or right
        }
      } else if (retVal.type.equals("tell")) { // word 1..n = message
        if ((rule.commands[commandIndex].numWords >= 2) && (target >= 0)) { // there is a target, and a message
         retVal.details += rule.commands[commandIndex].word[1];
         for (int i=2; i< rule.commands[commandIndex].numWords; i++) {
          retVal.details += " "+rule.commands[commandIndex].word[i]; // details = message
         }
          knownCreatures[target].tellMessage = ""; // erase the message from the user
        }
      } else if (retVal.type.equals("spell")) { // word 1 = spellname
        if ((rule.commands[commandIndex].numWords == 3) && (target >= 0)) { // there is a target, and a spellname
          retVal.details = rule.commands[commandIndex].word[1]; // details = spellname
        }
      } else if (retVal.type.equals("drop")) {
        // TODO: Dropping inventory/equip/bag objects...
      } 
    }
    return retVal;
  }
  
  // Returns the action to perform if the rule is true. 
  // If the rule is not to be run for any reason (a condition is false, or something is invalid)
  // a null action will be returned (default values)
  private AIAction applyRule(AIRule rule) {
    AIAction retVal = new AIAction();
    int target = getTarget(rule);
    if (target > -100) { // all the conditions were true 
      retVal = getAction(rule,target);
    }
    return retVal;
  }
  
  public AIAction getAction() {
    AIAction retVal = new AIAction();
    // For each rule
    for (int i=0; i<numRules; i++) {
      AIAction tempAction = applyRule(rules[i]);
      if (tempAction.type.length() > 0) {
        retVal = tempAction;
        break; // out of the for loop (only do the action for the first rule that is true)
      }
    }
    return retVal;
  }
 
}