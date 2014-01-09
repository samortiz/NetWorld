import java.applet.Applet;
import java.awt.*;
import java.awt.event.*; 
import java.io.*; 
import java.net.*;
import java.awt.image.*; 
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;


public class gameClientApplet extends Applet implements KeyListener, MouseListener, MouseMotionListener {

  // Random number, hopefully unique
  private static final long serialVersionUID = 722839075759364150L;
	
  // Connection Variables
  Socket gSocket = null;
  PrintWriter out = null;
  BufferedReader in = null;
  
  // General Variables
  String inputMode = "LOGINUSER";  // Defines how keypresses are handled
 
  // Drawing Variables
  String fromServerSCRN = "";
  String fromServerINFO = "Instructions~samplayer~Click - look~G - Get~Esc - Move mode~R Click - contents";
  String fromServerSTAT = "";
  String fromServerINV = "";
  String fromServerEQP = "";
  String fromServerATTR = "";
  String fromServerTARG = "";
  String bagName = "";
  int    bagMaxContains = 0;
  int    bagIndex = -1;
  boolean loggedIn = false;
  boolean drawText = false;
  boolean drawInfo = false;
  boolean drawBorders = true;
  boolean drawInv = false;
  boolean drawBag = false;
  boolean drawEqp = false;
  boolean drawAttr = false;

  // Text Variables
  String[] allText = new String[Constants.maxLinesOfText];
  int textIndex = 0; // last line of text
  StringBuffer tempStr = new StringBuffer(); // holds partial statements
  StringBuffer tellWhoStr= new StringBuffer(); // Holds Tell Who 
  int tellIndex = 0;
  boolean displayAllText = false;
  // Free Attribute variables
  String attributeName = "";
  double attributeAmountAdded = 0;


  // Graphics Variables
	BufferedImage bi;// main painting variable
	Graphics2D big; // the image that is the whole screen
	BufferedImage dragBi; // screen while dragging
	Graphics2D drag; // copy of the screen for erasing drag images
  String[] imageList;
  Hashtable images = new Hashtable();   // Game Images, loaded in init()
  String[] inventory    = new String[Constants.maxCreatureInventory]; // images in each inventory position
  String[] bagInventory = new String[Constants.objGlobalMaxContains]; // images in each bag position
  String[] eqpImages    = new String[Creature.maxEquip]; // images for each equip position
  int dragImageIndex = -1; // location in inventory for item being dragged
  String dragFrom = "";
  int dragX = -1;
  int dragY = -1;
  
  // Misc Variables
  Timer timer;  // Timer Variable
  


  // --------------------------- Paint the Screen ------------------------------------
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
		// Draws the buffered image to the screen.
		if (g2 != null) { 
		  g2.drawImage(bi, 0, 0, this);
		}
  }
  
  public void drawDragImage() {
    drag.drawImage(bi,0,0,this); // make a backup copy of the screen
  }
  
  // ---------------------------- Generate the Image for painting the screen -----------------
  public void updateScreen() {
    int startObjs = fromServerSCRN.lastIndexOf("[~--OBJS--~]");
    Image thisImg; // currently drawing image

		// Clears the map Screen Display
		big.setColor(Color.white);
    big.clearRect(Constants.border, Constants.border,(Constants.screenSizeX*Constants.scaleSize), (Constants.screenSizeY*Constants.scaleSize));
		big.setColor(Color.black); // for text
		
    if ((fromServerSCRN.length() > 0) && (displayAllText == false)) {
     if (fromServerSCRN.substring(4).equals("INIT")) {
       big.drawString("Welcome to Sam's game.",100,90);
       big.drawString("Please login below.",100,120);
       big.drawString("If you do not have a user yet, enter a username",100,140);
       big.drawString("and password and it will ask you if you want to create",100,160);
       big.drawString("a user.  Press the Y key and you should be playing.",100,180);
       big.drawString("If you have any questions you may contact Sam Ortiz",100,210);
     } else { // Not INIT 
        // Variables
        String mapStr = fromServerSCRN.substring(4,startObjs);
        String objStr = fromServerSCRN.substring(startObjs+12);

        int y = 0; 
        int x = 0; 
        int startIndex;  // for parsing strings
        int endIndex; 

        // ---------- Draw the Map ------------------
        big.setClip(Constants.border,Constants.border,Constants.border+Constants.mapSizeX,Constants.border+Constants.mapSizeY);
        endIndex = 1; // To enter the loop
        String imageName = "";
        startIndex = 0;
        while (endIndex > 0) {
          endIndex = mapStr.indexOf(',',startIndex);
          if ((startIndex >= 0) && (endIndex >= 0)) {
            imageName = mapStr.substring(startIndex,endIndex);
          } else {
            break;
          }
          thisImg = (Image)(images.get(imageName));
          if (thisImg != null) {
            big.drawImage(thisImg,(x*Constants.scaleSize)+Constants.border,(y*Constants.scaleSize)+Constants.border,this);
          } else {
            big.drawString("Missing Image "+imageName,(x*Constants.scaleSize)+Constants.border,(y*Constants.scaleSize)+Constants.border);
          }
          if (mapStr.charAt(endIndex+1) == '~') {
            x = 0;
            y += 1;
            startIndex = endIndex +2; // the , and ~
          } else {
            x += 1;
            startIndex = endIndex+1; // the ,
          }
        }
       // -------------- Draw the Objects sent in objStr -------------------
       String firstObjStr = "";
       String restObjStr = objStr;
       Integer objX;
       Integer objY;
       String objName;
       startIndex = 1;
       endIndex = 1;

       while (startIndex > -1) {
         startIndex = restObjStr.indexOf('(');
         endIndex   = restObjStr.indexOf(')')+1;
         if ((startIndex > -1) && (endIndex > -1)) {
           firstObjStr = restObjStr.substring(startIndex,endIndex);
           restObjStr  = restObjStr.substring(endIndex);

           // Parse the string firstObjStr of the form (x,y,displayChar)
           int firstComma = firstObjStr.indexOf(',');
           int secondComma = firstObjStr.indexOf(',',(firstComma+1)); 
           if ((firstComma >= 0) && (secondComma >= 0)) {
             objX = Integer.valueOf(firstObjStr.substring(1,firstComma));             // from ( to , 
             objY = Integer.valueOf(firstObjStr.substring(firstComma+1,secondComma)); // from , to ,
             objName = firstObjStr.substring(secondComma+1,(firstObjStr.length()-1));   // from second , to )
             // draw the Object
             thisImg = (Image)(images.get(objName));
             if (thisImg != null) {
               big.drawImage(thisImg,(objX.intValue()*Constants.scaleSize)+Constants.border,(objY.intValue()*Constants.scaleSize)+Constants.border,this);
             } else {
               big.drawString("Object Missing Image="+objName,((objX.intValue()*Constants.scaleSize)+Constants.border),((objY.intValue()*Constants.scaleSize)+Constants.border));
             }
           }
         } // if
       } 
     } // else.. not INIT
    } // if fromServerSCRN.length() > 0
    big.setClip(0,0,Constants.appletSizeX,Constants.appletSizeY);
    
    // ------------------------ Draw Text --------------------------------
    if ((drawText == true) || (displayAllText == true)) {
      // Clear the text area display
      big.setColor(new Color(200,240,240));
      int startY = ((Constants.border*2)+Constants.mapSizeY);
      int height = Constants.appletSizeY - ((Constants.border*3) + Constants.mapSizeY);
      int linesToDraw = Constants.displayLinesOfText;
      if (displayAllText) {
        startY = Constants.border; // clear whole map area
        height = Constants.appletSizeY - (Constants.border*2);
        linesToDraw = 40;
      }
      
      big.fillRect (Constants.border,  // x
                    startY, // y
                    Constants.mapSizeX, // width
                    height); // height
      big.setClip (Constants.border,  // x
                   startY, // y
                   Constants.mapSizeX, // width
                   height); 
      big.setColor(new Color(10,50,50));
      int loopCounter = 0;  
      for (int i=textIndex; loopCounter < linesToDraw; i--) {
        if (i < 0) i = (Constants.maxLinesOfText-1);
        if (allText[i].length() > 0) {
          big.drawString(allText[i],(Constants.border+Constants.textIndent),(490-(loopCounter*Constants.textSpacing)));
        }
        loopCounter += 1;
      }
      big.setClip(0,0,Constants.appletSizeX,Constants.appletSizeY);
      drawText = false;
    }
    
    // ------------------- Draw  the Stats (top right corner) -----------------------------
    if (fromServerSTAT.length() > 3) {
     Color background  = new Color(200,240,220);
     Color text        = new Color(0,80,0);
     Color fullBar     = new Color(50,250,50);
     Color emptyBar    = new Color(10,200,10);
     int startX = (Constants.mapSizeX + (Constants.border*2));
     int startY = Constants.border;
     int textHeight = Constants.textHeight;
     int yInc = Constants.textSpacing;
     int y = startY + textHeight + 3; // from top border
     int x = startX + 3; // from left border
     int width = Constants.appletSizeX - ((Constants.border*3) + Constants.mapSizeX);
     int height = Constants.statsHeight;
     
     // Erase the area (top right)
     big.setColor(background);
     big.setClip(startX, startY, width, height);
     big.fillRect(startX, startY, width, height); 
       double hitPoints      = 0;
       double hitPointsMax   = 0;
       double spellPoints    = 0;
       double spellPointsMax = 0;
       int    moveDelay      = 0;
       int    attackDelay    = 0;
       int    spellDelay     = 0;
       int    moveDelayMax   = 0;
       int    attackDelayMax = 0;
       int    spellDelayMax  = 0;
       int    level          = 0;
       long   exp            = 0;
       double gold           = 0;
       double weight         = 0;
       double weightMax      = 0;
       int    encumberance   = 0;

     try {
       hitPoints      = Double.valueOf( getVal(fromServerSTAT,0)).doubleValue();
       hitPointsMax   = Double.valueOf( getVal(fromServerSTAT,1)).doubleValue();
       spellPoints    = Double.valueOf( getVal(fromServerSTAT,2)).doubleValue();
       spellPointsMax = Double.valueOf( getVal(fromServerSTAT,3)).doubleValue();
       moveDelay      = Integer.valueOf(getVal(fromServerSTAT,4)).intValue();
       attackDelay    = Integer.valueOf(getVal(fromServerSTAT,5)).intValue();
       spellDelay     = Integer.valueOf(getVal(fromServerSTAT,6)).intValue();
       moveDelayMax   = Integer.valueOf(getVal(fromServerSTAT,7)).intValue();
       attackDelayMax = Integer.valueOf(getVal(fromServerSTAT,8)).intValue();
       spellDelayMax  = Integer.valueOf(getVal(fromServerSTAT,9)).intValue();
       level          = Integer.valueOf(getVal(fromServerSTAT,10)).intValue();
       exp            = Long.valueOf(   getVal(fromServerSTAT,11)).longValue();
       gold           = Double.valueOf( getVal(fromServerSTAT,12)).doubleValue();
       weight         = Double.valueOf( getVal(fromServerSTAT,13)).doubleValue();
       weightMax      = Double.valueOf( getVal(fromServerSTAT,14)).doubleValue();
       encumberance   = Integer.valueOf(getVal(fromServerSTAT,15)).intValue();
     } catch (NumberFormatException e) {}
     
     // Error checking
     if (hitPoints < 0) hitPoints = 0;
     if (hitPointsMax < 0) hitPointsMax = 0;
     if (spellPoints < 0) spellPoints = 0;
     if (spellPointsMax < 0) spellPointsMax = 0;
     if (moveDelay < 0) moveDelay = 0;
     if (attackDelay < 0) attackDelay = 0;
     if (spellDelay < 0) spellDelay = 0;
     // the move,attack,spell delayMax is not used (fixed scale because base will often be more than max)
     
     big.setColor(text);
     big.drawString("HP:",x,y);
     big.setColor(fullBar);
     big.fillRect(x+25,y-textHeight,(int)(50*(hitPoints/hitPointsMax)),10);
     big.setColor(emptyBar);
     big.fillRect(x+25+(int)(50*(hitPoints/hitPointsMax)),y-textHeight,50-(int)(50*(hitPoints/hitPointsMax)),10);
     big.setColor(text);
     big.drawString((int)hitPoints+" / "+(int)hitPointsMax,x+80,y);

     y += yInc;
     big.drawString("SP:",x,y);
     big.setColor(fullBar);
     big.fillRect(x+25,y-textHeight,(int)(50*(spellPoints/spellPointsMax)),10);
     big.setColor(emptyBar);
     big.fillRect(x+25+(int)(50*(spellPoints/spellPointsMax)),y-textHeight,50-(int)(50*(spellPoints/spellPointsMax)),10);
     big.setColor(text);
     big.drawString((int)spellPoints+" / "+(int)spellPointsMax,x+80,y);
     
     y += yInc;
     big.drawString("Level"   ,x+50,y);
     big.drawString(": "+level,x+80,y);
     y += yInc;
     big.drawString("Exp"   ,x+50,y);
     big.drawString(": "+exp,x+80,y);
     y += yInc;
     big.drawString("Gold"   ,x+50,y);
     big.drawString(": "+((double)((int)(gold*10))/10.0),x+80,y); // 1 digit
     
     // Draw the delay status bars
     big.drawString("M",x,y);
     big.drawString("A",x+15,y);
     big.drawString("S",x+30,y);
     int tempY = y- (3*yInc)+(textHeight/2); // go up three "lines" (the textHeight/2 is to center it)
     big.setColor(fullBar);
     int h;
     h = 20-moveDelay;
     if (h < 0) h = 0;
     big.fillRect(x,tempY+h,10,20-h);
     h = 20-((int)(attackDelay/2));
     if (h < 0) h = 0;
     big.fillRect(x+15,tempY+h,10,20-h);
     h = 20-((int)(spellDelay/3));
     if (h < 0) h = 0;
     big.fillRect(x+30,tempY+h,10,20-h);

     y += yInc;
     big.setColor(text);
     big.drawString("Weight: "+(int)weight+" / "+(int)weightMax+"  Move: "+encumberance,x,y);
     
     big.setClip(0,0,Constants.appletSizeX,Constants.appletSizeY);
    }

    // ------------------------------ Draw INFO ----------------------------------------
    if ((drawInfo == true) && (fromServerINFO.length() > 0) ) {
      int startX = (Constants.border*2)+Constants.mapSizeX;
      int startY = (Constants.border*2)+Constants.statsHeight;
      int width = (Constants.appletSizeX-((Constants.border*3)+Constants.mapSizeX));
      int height = Constants.infoHeight;
      int x = startX + 3; // left padding
      int y = startY + Constants.textHeight + 3; // top padding
      Color background = new Color(255,250,250);
      Color text = new Color(80,10,10);
      
      big.setColor(background);
      big.fillRect(startX,startY,width, height); // Info bottom border
      big.setClip(startX,startY,width,height);
      big.setColor(text);
      
      //  Prints out the info lines in the form of lines of text followed by ~
      // the format is Name~imageName~some text~more text....~last line of text here~
      int start = 0;
      int end = fromServerINFO.indexOf('~',start+1);
      int loopCounter = 0;
      while(end > -1) {
        if ((start > -1) && (end > -1)) {
          String theText = fromServerINFO.substring(start,end);
          if (loopCounter == 0) {
            big.drawString(theText,x,y+(Constants.textHeight/2)); // /2 is for centering
          } else if (loopCounter == 1) {
            drawImageStr(theText,(Constants.appletSizeX-(Constants.border+20+3)),(startY+3));
          } else {
            big.drawString(theText,x,y);
          }
        }
        start = end+1;
        end = fromServerINFO.indexOf('~',start+1);
        y += Constants.textSpacing;
        loopCounter += 1;
      }
      drawInfo = false;
      big.setClip(0,0,Constants.appletSizeX,Constants.appletSizeY);
    }

 // ------------------------------ Draw Inventory ----------------------------------------
    if (drawInv && (fromServerINV.length() > 0)) {
      int x = Constants.invX; // left side of grid
      int y = Constants.invY; // top of grid
      int b = Constants.invBorder;
      int size = Constants.imageSize;
      int gridX = Constants.invGridX;
      int gridY = Constants.invGridY;
      
      big.setColor(Constants.invBackground);
      big.fillRect(Constants.invStartX,Constants.invStartY,Constants.invWidth,Constants.invHeight); // Inventory Area
      big.setClip(Constants.invStartX,Constants.invStartY,Constants.invWidth,Constants.invHeight);
      big.setColor(Constants.invText);
      big.drawString("Inventory",x,y-3); // leave 3px between grid and text

      // display the empty inventory grid
      big.setColor(Constants.invGridColor);
      for (int X=0; X<gridX+1; X++) { // vertical lines
        big.fillRect(x+(X*(size+b)),y,b,(gridY*(size+b)));
      }
      for (int Y=0; Y<gridY+1; Y++) { // horizontal lines
        big.fillRect(x,y+(Y*(size+b)),(gridX*(size+b))+b,b);
      }
      
      int loopCounter = 0;
      int end = -1;
      for (int start=0; start > -1 ; start = fromServerINV.indexOf(',',start+1)) {
        if (start > 0) start +=1 ; // not the first time through the loop
        end = fromServerINV.indexOf(',',start);
        if ((start > -1) && (end > 0)) {
          String imageName = fromServerINV.substring(start,end);
          if (!imageName.equals("noimage")) {
            int Y = (int)Math.floor((double)loopCounter/(double)gridX); // grid Position
            int X = loopCounter - (Y * gridX);                          // grid Position
            int absX = x+(X*(size+b))+b;
            int absY = y+(Y*(size+b))+b;
            drawImageStr(imageName,absX,absY);
          }
          loopCounter +=1;
        }
      }
      big.setClip(0,0,Constants.appletSizeX,Constants.appletSizeY);
      drawInv = false;
    }
    
  // ---------------------------- Draw the Bag View ------------------------------------
  if (drawBag) {
      // Same size as Inventory
      int x = Constants.bagX; // left side of grid
      int y = Constants.bagY; // top of grid
      int b = Constants.bagBorder;
      int size = Constants.imageSize;
      int gridX = Constants.bagGridX;
      int gridY = Constants.bagGridY;
      
      big.setColor(Constants.bagBackground);
      big.fillRect(Constants.bagStartX,Constants.bagStartY,Constants.bagWidth,Constants.bagHeight); // Inventory Area
      big.setClip(Constants.bagStartX,Constants.bagStartY,Constants.bagWidth,Constants.bagHeight);
      big.setColor(Constants.bagText);
      big.drawString(bagName+" contains",x,y-3); // leave 3px between grid and text

      // display the empty grid
      big.setColor(Constants.bagGridColor);
      for (int X=0; X<gridX+1; X++) { // vertical lines
        big.fillRect(x+(X*(size+b)),y,b,(gridY*(size+b)));
      }
      for (int Y=0; Y<gridY+1; Y++) { // horizontal lines
        big.fillRect(x,y+(Y*(size+b)),(gridX*(size+b))+b,b);
      }

      for (int i=0; i<bagMaxContains; i++) {
        String imageName = bagInventory[i];
        int Y = (int)Math.floor((double)i/(double)gridX); // grid Position
        int X = i - (Y * gridX);                          // grid Position
        int absX = x+(X*(size+b))+b;
        int absY = y+(Y*(size+b))+b;
        drawImageStr(imageName,absX,absY);
      }
      
      // For every element that the bag cannot contain
      for (int i=bagMaxContains; i<Constants.maxBagInventory; i++) {
        int Y = (int)Math.floor((double)i/(double)gridX); // grid Position
        int X = i - (Y * gridX);                          // grid Position
        int absX = x+(X*(size+b))+b;
        int absY = y+(Y*(size+b))+b;
        big.setColor(Constants.bagEmptyColor);
        big.fillRect(absX,absY,Constants.imageSize,Constants.imageSize);
      }
      big.setClip(0,0,Constants.appletSizeX,Constants.appletSizeY);
      drawBag = false;
    }

  // -------------------- Draw the Attributes Screen -------------------------
  if (drawAttr) {
    int x = Constants.attrStartX;
    int y = Constants.attrStartY;
    big.setClip(x,y,Constants.attrWidth,Constants.attrHeight);
    big.setColor(Constants.attrBackground);
    big.fillRect(x,y,Constants.attrWidth,Constants.attrHeight);
    
    // Attr draws it's own borders
    big.setColor(Constants.mainBorderColor);
    big.fillRect(x,y+Constants.attrHeight-Constants.border,Constants.attrWidth,Constants.border); // bottom

    double freeAttributes = 0;
    double strength = 0;
    double dexterity = 0;
    double constitution = 0;
    double intelligence = 0;
    double charisma = 0;
    double armorBludgeon = 0;
    double armorPierce = 0;
    double armorSlash = 0;
    double armorFire = 0;
    double armorIce = 0;
    double armorMagic = 0;
    double damageBludgeon = 0;
    double damagePierce = 0;
    double damageSlash = 0;
    double damageFire = 0;
    double damageIce = 0;
    double damageMagic = 0;
    double armorBludgeonT = 0;
    double armorPierceT = 0;
    double armorSlashT = 0;
    double armorFireT = 0;
    double armorIceT = 0;
    double armorMagicT = 0;
    double damageBludgeonT = 0;
    double damagePierceT = 0;
    double damageSlashT = 0;
    double damageFireT = 0;
    double damageIceT = 0;
    double damageMagicT = 0;

    double allRange = 0;
    double maxRange = 0;
    
    try {
      freeAttributes  = ((double)(int)(Double.valueOf(getVal(fromServerATTR,0 )).doubleValue()*10))/10;
      strength        = ((double)(int)(Double.valueOf(getVal(fromServerATTR,1 )).doubleValue()*10))/10;
      dexterity       = ((double)(int)(Double.valueOf(getVal(fromServerATTR,2 )).doubleValue()*10))/10;
      constitution    = ((double)(int)(Double.valueOf(getVal(fromServerATTR,3 )).doubleValue()*10))/10;
      intelligence    = ((double)(int)(Double.valueOf(getVal(fromServerATTR,4 )).doubleValue()*10))/10;
      charisma        = ((double)(int)(Double.valueOf(getVal(fromServerATTR,5 )).doubleValue()*10))/10;
      armorBludgeon   = ((double)(int)(Double.valueOf(getVal(fromServerATTR,6 )).doubleValue()*10))/10;
      armorPierce     = ((double)(int)(Double.valueOf(getVal(fromServerATTR,7 )).doubleValue()*10))/10;
      armorSlash      = ((double)(int)(Double.valueOf(getVal(fromServerATTR,8 )).doubleValue()*10))/10;
      armorFire       = ((double)(int)(Double.valueOf(getVal(fromServerATTR,9 )).doubleValue()*10))/10;
      armorIce        = ((double)(int)(Double.valueOf(getVal(fromServerATTR,10)).doubleValue()*10))/10;
      armorMagic      = ((double)(int)(Double.valueOf(getVal(fromServerATTR,11)).doubleValue()*10))/10;
      damageBludgeon  = ((double)(int)(Double.valueOf(getVal(fromServerATTR,12)).doubleValue()*10))/10;
      damagePierce    = ((double)(int)(Double.valueOf(getVal(fromServerATTR,13)).doubleValue()*10))/10;
      damageSlash     = ((double)(int)(Double.valueOf(getVal(fromServerATTR,14)).doubleValue()*10))/10;
      damageFire      = ((double)(int)(Double.valueOf(getVal(fromServerATTR,15)).doubleValue()*10))/10;
      damageIce       = ((double)(int)(Double.valueOf(getVal(fromServerATTR,16)).doubleValue()*10))/10;
      damageMagic     = ((double)(int)(Double.valueOf(getVal(fromServerATTR,17)).doubleValue()*10))/10;
      armorBludgeonT  = ((double)(int)(Double.valueOf(getVal(fromServerATTR,18 )).doubleValue()*10))/10;
      armorPierceT    = ((double)(int)(Double.valueOf(getVal(fromServerATTR,19 )).doubleValue()*10))/10;
      armorSlashT     = ((double)(int)(Double.valueOf(getVal(fromServerATTR,20 )).doubleValue()*10))/10;
      armorFireT      = ((double)(int)(Double.valueOf(getVal(fromServerATTR,21 )).doubleValue()*10))/10;
      armorIceT       = ((double)(int)(Double.valueOf(getVal(fromServerATTR,22)).doubleValue()*10))/10;
      armorMagicT     = ((double)(int)(Double.valueOf(getVal(fromServerATTR,23)).doubleValue()*10))/10;
      damageBludgeonT = ((double)(int)(Double.valueOf(getVal(fromServerATTR,24)).doubleValue()*10))/10;
      damagePierceT   = ((double)(int)(Double.valueOf(getVal(fromServerATTR,25)).doubleValue()*10))/10;
      damageSlashT    = ((double)(int)(Double.valueOf(getVal(fromServerATTR,26)).doubleValue()*10))/10;
      damageFireT     = ((double)(int)(Double.valueOf(getVal(fromServerATTR,27)).doubleValue()*10))/10;
      damageIceT      = ((double)(int)(Double.valueOf(getVal(fromServerATTR,28)).doubleValue()*10))/10;
      damageMagicT    = ((double)(int)(Double.valueOf(getVal(fromServerATTR,29)).doubleValue()*10))/10;
      allRange        = ((double)(int)(Double.valueOf(getVal(fromServerATTR,30)).doubleValue()*10))/10;
      maxRange        = ((double)(int)(Double.valueOf(getVal(fromServerATTR,31)).doubleValue()*10))/10;
     } catch (NumberFormatException e) {}

    // First Column
    x = Constants.attrStartX + Constants.border+3;
    y = Constants.attrStartY + Constants.border;
    y += Constants.textSpacing;
    big.drawString("Attributes",x,y);                                                  y += Constants.textSpacing;
    big.drawString("Free Attr",x,y);       big.drawString(": "+freeAttributes,x+70,y); y += Constants.textSpacing;
    big.drawString("Strength",x,y);        big.drawString(": "+strength,x+70,y);       y += Constants.textSpacing;
    big.drawString("Dexterity",x,y);       big.drawString(": "+dexterity,x+70,y);      y += Constants.textSpacing;
    big.drawString("Constitution",x,y);    big.drawString(": "+constitution,x+70,y);   y += Constants.textSpacing;
    big.drawString("Intelligence",x,y);    big.drawString(": "+intelligence,x+70,y);   y += Constants.textSpacing;
    big.drawString("Charisma",x,y);        big.drawString(": "+charisma,x+70,y);

    // Second Column (armor)
    x = Constants.attrStartX + Constants.border+111;
    y = Constants.attrStartY + Constants.border;
    y += Constants.textSpacing;
    big.drawString("Armor",x,y);    big.drawString("  base",x+55,y);               big.drawString("total",x+92,y);           y += Constants.textSpacing;
    big.drawString("Bludgeon",x,y); big.drawString(": "+armorBludgeon,x+55,y);     big.drawString(""+armorBludgeonT,x+92,y); y += Constants.textSpacing;
    big.drawString("Pierce",x,y);   big.drawString(": "+armorPierce,x+55,y);       big.drawString(""+armorPierceT,x+92,y);   y += Constants.textSpacing;
    big.drawString("Slash",x,y);    big.drawString(": "+armorSlash,x+55,y);        big.drawString(""+armorSlashT,x+92,y);    y += Constants.textSpacing;
    big.drawString("Fire",x,y);     big.drawString(": "+armorFire,x+55,y);         big.drawString(""+armorFireT,x+92,y);     y += Constants.textSpacing;
    big.drawString("Ice",x,y);      big.drawString(": "+armorIce,x+55,y);          big.drawString(""+armorIceT,x+92,y);      y += Constants.textSpacing;
    big.drawString("Magic",x,y);    big.drawString(": "+armorMagic,x+55,y);        big.drawString(""+armorMagicT,x+92,y);    y += Constants.textSpacing;

    // Third Column (Damage)
    x = Constants.attrStartX + Constants.border+233;
    y = Constants.attrStartY + Constants.border;
    y += Constants.textSpacing;
    big.drawString("Damage",x,y);  big.drawString("  base",x+55,y);               big.drawString("total",x+92,y);            y += Constants.textSpacing;
    big.drawString("Bludgeon",x,y); big.drawString(": "+damageBludgeon,x+55,y); big.drawString(""+damageBludgeonT,x+92,y); y += Constants.textSpacing;
    big.drawString("Pierce",x,y);   big.drawString(": "+damagePierce,x+55,y);   big.drawString(""+damagePierceT,x+92,y);   y += Constants.textSpacing;
    big.drawString("Slash",x,y);    big.drawString(": "+damageSlash,x+55,y);    big.drawString(""+damageSlashT,x+92,y);    y += Constants.textSpacing;
    big.drawString("Fire",x,y);     big.drawString(": "+damageFire,x+55,y);     big.drawString(""+damageFireT,x+92,y);     y += Constants.textSpacing;
    big.drawString("Ice",x,y);      big.drawString(": "+damageIce,x+55,y);      big.drawString(""+damageIceT,x+92,y);      y += Constants.textSpacing;
    big.drawString("Magic",x,y);    big.drawString(": "+damageMagic,x+55,y);    big.drawString(""+damageMagicT,x+92,y);    y += Constants.textSpacing;

    // Fourth Column (Ranges)
    x = Constants.attrStartX + Constants.border+355;
    y = Constants.attrStartY + Constants.border;
    y += Constants.textSpacing;
    big.drawString("Range",x,y);                                           y += Constants.textSpacing;
    big.drawString("All",x,y); big.drawString(": "+allRange,x+30,y); y += Constants.textSpacing;
    big.drawString("Max",x,y); big.drawString(": "+maxRange,x+30,y); y += Constants.textSpacing;

    
    
    big.setClip(0,0,Constants.appletSizeX,Constants.appletSizeY);
  }

  // -------------------- Draw the Equipped Items -------------------------
  if (drawEqp) {
    int x = Constants.eqpStartX;
    int y = Constants.eqpStartY;
    big.setClip(x,y,Constants.eqpWidth,Constants.eqpHeight);
    big.setColor(Constants.eqpBackground);
    big.fillRect(x,y,Constants.eqpWidth,Constants.eqpHeight);
    
    // EQP draws it's own borders
    big.setColor(Constants.mainBorderColor);
    big.fillRect(x,y,Constants.eqpWidth,Constants.border); // top
    big.fillRect(x,y+Constants.eqpHeight-Constants.border,Constants.eqpWidth,Constants.border); // bottom
    big.fillRect(x,y,Constants.border,Constants.eqpHeight); // left

    big.setColor(Constants.eqpBorderColor);
    big.fillRect(x+Constants.border+Constants.imageSize,y+Constants.border,Constants.eqpBorder,Constants.eqpHeight-(Constants.border*2)); // vertical line between images and text

    x += Constants.border;// start of image location
    y += Constants.border; 
    int textX = x + Constants.imageSize + Constants.eqpBorder + 3; // start of text location (+3 spacer)
    int textYModifier = Constants.textHeight+((Constants.imageSize-Constants.textHeight)/2); // centering text
    int stepSize = Constants.imageSize+Constants.eqpBorder;

    for (int i=0; i<Creature.maxEquip; i++) {
      String imageName = eqpImages[i];
      if ((imageName.length() >0) && (!imageName.equals("noimage"))) {
        drawImageStr(imageName,x,y);
      }
      big.setColor(Constants.eqpText);
      if      (i == Creature.head) big.drawString("Head",textX,y+textYModifier);
      else if (i == Creature.face) big.drawString("Face",textX,y+textYModifier);
      else if (i == Creature.neck) big.drawString("Neck",textX,y+textYModifier);
      else if (i == Creature.body) big.drawString("Body",textX,y+textYModifier);
      else if (i == Creature.arms) big.drawString("Arms",textX,y+textYModifier);
      else if (i == Creature.rightHand) big.drawString("R Hand",textX,y+textYModifier);
      else if (i == Creature.leftHand) big.drawString("L Hand",textX,y+textYModifier);
      else if (i == Creature.rightFinger) big.drawString("R Finger",textX,y+textYModifier);
      else if (i == Creature.leftFinger) big.drawString("L Finger",textX,y+textYModifier);
      else if (i == Creature.waist) big.drawString("Waist",textX,y+textYModifier);
      else if (i == Creature.legs) big.drawString("Legs",textX,y+textYModifier);
      else if (i == Creature.feet) big.drawString("Feet",textX,y+textYModifier);
      y += stepSize;
      if (i != Creature.maxEquip -1) { // Don't draw on the last loop
        big.setColor(Constants.eqpBorderColor);
        big.fillRect(x,y-Constants.eqpBorder,Constants.eqpWidth,Constants.eqpBorder); // below text
      }
    }
          
    big.setClip(0,0,Constants.appletSizeX,Constants.appletSizeY);
  }

  
  // ----------------- Draw the borders ----------------------------------
  if (drawBorders == true) {
		// draw the applet borders
		big.setColor(Color.black);
		int b = Constants.border;
		int X = Constants.appletSizeX;
		int Y = Constants.appletSizeY;
		// Rectangles have the form (X,Y,width,height) (600 x 500)
		big.fillRect(0,0,X,b); // top side
		big.fillRect(0,Y-b,X,b); // bottom Side
		big.fillRect(0,0,b,Y); // left side
		big.fillRect(X-b,0,b,Y); // right side
		
    // Map borders
    big.setColor(Constants.mainBorderColor);
		big.fillRect(b,b+Constants.mapSizeY,Constants.mapSizeX+b,b); // bottom Side
		big.fillRect(b+Constants.mapSizeX,b,b,(Y-(b*2))); // right side
		big.fillRect((b*2)+Constants.mapSizeX,b+Constants.statsHeight,(X-((b*3)+Constants.mapSizeX)), b); // stats bottom border
		big.fillRect((b*2)+Constants.mapSizeX,(b*2)+Constants.statsHeight+Constants.infoHeight,(X-((b*3)+Constants.mapSizeX)), b); // Info bottom border
		big.fillRect((b*2)+Constants.mapSizeX,(b*3)+Constants.statsHeight+Constants.infoHeight+Constants.invHeight,(X-((b*3)+Constants.mapSizeX)), b); // Inv bottom border
		big.fillRect((b*2)+Constants.mapSizeX,(b*4)+Constants.statsHeight+Constants.infoHeight+Constants.invHeight+Constants.bagHeight,(X-((b*3)+Constants.mapSizeX)), b); // Bag bottom border
		// EQP draws it's own borders
		drawBorders = false;
  }
    
  } // updateScreen
  
  
  // ------------ Helper function for updateScreen (drawing Stats) ----------------
  // returns the itemNo'th number starting with 0 in a string with the format #,#,#,...#,#
  private String getVal(String inStr, int itemNo) {
    String retVal;
    int start = 0;
    int end = -1;
    for (int i=0; i< itemNo; i++) {
      start = inStr.indexOf(',',start)+1; // next number
      if (start < 0) break;
    }
    end = inStr.indexOf(',',start+1);
    if (end < 0) end = inStr.length(); // no comma at the end
    if ((start < 0) || (end < 0)) {
      retVal = "";
      System.out.println("Error parsing string for itemNo="+itemNo+" string="+inStr);
    } else {
      retVal = inStr.substring(start,end);
    }
    return retVal;
  }

  // -------------------------- Image Display ----------------------------
  // Puts an image on big (buffered image graphic), at the specified location
  private void drawImageStr(String imageName, int x, int y) {
    String imageStr = imageName.toLowerCase().trim();
    if ((imageStr.length() > 0) && (!imageStr.equals("noimage"))) {
      Image thisImg = (Image)(images.get(imageStr));
      if (thisImg != null) { // make sure the image exists
        big.drawImage(thisImg,x,y,this);
      } else { // image not found
        System.out.println("Image not found :"+imageName);
      }
    }
  }
  
  // ------------------------ Image Initialization -----------------
  private void loadImages(String imgStr) {
    int loopCounter = 0;
    // Count the number of Images
    for (int i=0; i>=0; i=imgStr.indexOf(' ',(i+1))) {
      loopCounter += 1;
    }
    imageList = new String[loopCounter];
    loopCounter = 0;
    // Parse out the string
    for (int i=0; i>=0; i=imgStr.indexOf(' ',(i+1))) {
      int start = i+1;
      int end = imgStr.indexOf(' ',start+1);
      if (end < 0) end = imgStr.length();
      imageList[loopCounter] = imgStr.substring(start,end);
      loopCounter += 1;
    }
    // Put the images into the hash by image name
    for (int i=0; i< imageList.length;i++) {
      int dotIndex = imageList[i].indexOf('.');
      if (dotIndex >= 0)  {
        String imageName = imageList[i].substring(0,dotIndex);
        images.put(imageName, getImage(getDocumentBase(),Constants.imageDir+"/"+imageList[i]));
      }
    }
  }

  // Prepares the images for displaying on screen.
  // This loads the images into memory for fast display
  // (the displaying of images is done in a seperate thread,
  //  and if it doesn't finish before the paint(g) is called then
  //  the image will fail to display)  This speeds up the 
  //  drawImage method quite a bit.
  public void prepareAllImages() {
    Image  thisImg;
    String imageName;
    for (int i=0 ; i< imageList.length ; i++) {
      imageName = imageList[i].substring(0,imageList[i].indexOf('.')); // crop off the file extension
      thisImg = (Image)(images.get(imageName));
      prepareImage(thisImg,this);
    }
  }



  // --------------------- Main Initialization --------------------------------------
  public void init() {
    String initStr = "";
    String fromServerIMGS = "";
    // Open Connection
    try {
      gSocket = new Socket(Constants.hostName, 4444);
      out = new PrintWriter(gSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(gSocket.getInputStream()));
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host:"+Constants.hostName);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to:"+Constants.hostName);
      System.exit(1);
    }
    try {
      // Initialize the game! (server will send an initialization string
      initStr = in.readLine();
    } catch (IOException exception) {
        System.err.println("Error reading from in stream");
        System.exit(1);
      } // try
    
    // Parse the init string from the server
    // Ensure the string is a valid length
    if (initStr.length() < 12) {
      System.err.println("Game initialization string from server is invalid (less than 12 chars INITSCRNIMGS)");
      System.exit(1);
    } 
    fromServerSCRN = initStr.substring(0,8);
    fromServerIMGS = initStr.substring(12);

   //Initialize the text to empty
   for (int i=0; i<Constants.maxLinesOfText; i++) {
    allText[i] = "";
   }

   // Initialize the equip Display to empty
   for (int i=0; i< Creature.maxEquip; i++) {
    eqpImages[i] = "";
   }
    
   // Initialize the inventory to "noimage"
   for (int i=0; i<Constants.maxCreatureInventory; i++) {
     inventory[i] = "noimage";
   }

   // Display the initial login text 
   addText("Please enter your User Name and Password to logon to the Server.");
   addText("User Name : ");
 
   // Setup Input devices
   this.addKeyListener(this);
   this.addMouseListener(this);
   this.addMouseMotionListener(this);

   // Set up imageList as an array of Strings and load all the images
   // into the hash images (by their base filename)
   loadImages(fromServerIMGS);

   // Set up the graphics for double buffering
   Dimension dim = getSize();
   bi = (BufferedImage)createImage(dim.width, dim.height);
   big = bi.createGraphics();
   dragBi = (BufferedImage)createImage(dim.width, dim.height);
   drag = dragBi.createGraphics();
   
   // prepare the images for loading onto the screen (this pre-loads them so they don't take as long to load)
   prepareAllImages();

   updateScreen(); // draws the graphics to big (the buffer)
 
   // Set up the timer to listen to the server.  
   // This should be the last thing run in init()
   timer = new Timer(true);
   timer.scheduleAtFixedRate(serverListen,0,100);
  }
 


  // --------------------------- Text Routines Modify Text Strings -------------------------------------
  
  // Adds a line to the user text display
  public void addText(String inStr) {
    if (textIndex < Constants.maxLinesOfText-1) textIndex += 1;
    else textIndex = 0;
    allText[textIndex] = inStr;
    drawText = true; // redraw text at next screen refresh
  }

  // replaces the last text line with inStr
  public void modifyText(String inStr) {
   allText[textIndex] = inStr;
   drawText = true;
  }

  // replaces the line of text at index with inStr
  public void modifyText(int index, String inStr) {
   allText[index] = inStr;
   drawText = true;
  }


  public void addKeyTotempStr(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
      if (tempStr.length() > 0) tempStr = tempStr.deleteCharAt(tempStr.length()-1);
    } else {
      if ((e.getKeyChar() != '~') && (e.getKeyChar() != ',') && (e.getKeyChar() != ')') && (e.getKeyChar() != '(')) {
        tempStr = tempStr.append(e.getKeyChar());
      }
   }
  }


 // -------------------- Parsing the LOGN String from the server ---------------------------
 private void parseLogin(String command, String details) {
   if (details.equals("SUCCESS")) {
     loggedIn = true;
     addText("Login Successful!");
     drawInfo = true;
     out.println("INVE"); // tell the server to send an initial inventory display
     drawBag = true;
     inputMode = "MOVE";
   } else if (details.substring(0,11).equals("INVALIDUSER")) {
     addText("Invalid User Name.");
     addText("Would you like to Create a new User with the following information?");
     addText("User Name = "+details.substring(11,details.indexOf('~')));
     addText(" Password = "+details.substring(details.indexOf('~')+1));
     addText("Please enter Y to create a new user, or any other key to try logging in again.");
     tempStr = new StringBuffer(details.substring(11)); // string of the form username~password
     inputMode = "LOGIN_NEW_USER_YN";
   } else {
     addText("Login Failed :"+details);
     addText("Please try logging in again.");
     addText("User Name : ");
     tempStr = new StringBuffer();
     inputMode = "LOGINUSER";
   }
 }

  // -------------------------- Parse an Inventory Display ------------------------------
  private void parseInventory(String inStr) {
    fromServerINV = inStr; // for drawing the image
    int loopCounter = 0;
    int end = -1;
    for (int start=0; start > -1 ; start = inStr.indexOf(',',start+1)) {
      if (start > 0) start +=1 ; // not the first time through the loop
      end = inStr.indexOf(',',start);
      if ((start > -1) && (end > 0)) {
        String imageName = inStr.substring(start,end);
        if (loopCounter < Constants.maxCreatureInventory) {
          inventory[loopCounter] = imageName; // for displaying drag images
        }
        loopCounter +=1;
      }
    }
    drawInv = true;
  }


  // -------------------------- Parse Equip Display ------------------------------
  private void parseEqp(String inStr) {
    fromServerEQP = inStr; // for drawing the image
    int loopCounter = 0;
    int start = 0;
    int end = inStr.indexOf(',');
    while (end >= 0) {
      if ((start >= 0) && (end >= 0)) {
        String imageName = inStr.substring(start,end);
        if (loopCounter < Creature.maxEquip) {
          eqpImages[loopCounter] = imageName; // for displaying drag images
        }
        loopCounter +=1;
      }
      start = end+1;
      end = inStr.indexOf(',',start);
    }
  }

 // ------------------------ Parse a BAGI string --------------------------------
 // sets the bagName, bagMaxContains, and bagInventory variables for drawing bag contents
 // inStr should be of the form : bagName~maxContains~bagIndex~image,image,image...
 // or "ERASE" - to erase the bag view (when the user drops a bag he is currently looking at)
 private void parseBag(String inStr) {
   if (inStr.equals("ERASE")) {
     bagName = "";
     bagMaxContains = 0;
     for (int i=0; i<bagMaxContains; i++) {
       bagInventory[i] = "noimage";
     }
     
    
   } else { // it is a normal BAGI String
     int startMaxContains = inStr.indexOf('~');
     int startIndex = -1;
     int startDetails = -1;
     if (startMaxContains > 0) {
       bagName = inStr.substring(0,startMaxContains);
       startIndex = inStr.indexOf('~',startMaxContains+1);
     } 
     if (startIndex > 0) {
       try {bagMaxContains = Integer.valueOf(inStr.substring(startMaxContains+1,startIndex)).intValue();} catch (NumberFormatException e) {}
       startDetails = inStr.indexOf('~',startIndex+1);
     }
     if (startDetails > 0) {
       try {bagIndex = Integer.valueOf(inStr.substring(startIndex+1,startDetails)).intValue();} catch (NumberFormatException e) {}
       String bagImages = inStr.substring(startDetails+1);
       int start = 0;
       int end = bagImages.indexOf(',');
       for (int i=0; i<bagMaxContains; i++) {
         if ((start >= 0) && (end >= 0)) {
           bagInventory[i] = bagImages.substring(start,end);
         }
         start = end+1;
         end = bagImages.indexOf(',',start);  
       }
     }
   }
   drawBag = true;
 }

  // ---------------------------------------- If the server requests a Target -------------------------------
  
  public void getTarget(String details) {
    fromServerTARG = details;
    addText("Choose Target");  
    inputMode = "TARGET";
  }

  // ------------------------------- Timer Method --------------------------------------
  
  TimerTask serverListen = new TimerTask() {
    public void run() {
      timer.cancel(); // Only run this timerTask once! (it's in a seperate thread)
      String fromServer = ""; // receiving server replies
      
      try {
      while(!fromServer.equals("QUIT")) {
        // get the data from the server
        fromServer = in.readLine();
        for (int i=0 // Start with i = 0
            ; ((i==0) || (i > Constants.commandSeperator.length())) // until i is negative (or 0)
            ; i = fromServer.indexOf(Constants.commandSeperator,i+1)+Constants.commandSeperator.length()) { // find the next command
         int start = i;
         int end = fromServer.indexOf(Constants.commandSeperator,i+1);
         if (end >= 0) {
           String thisAction = fromServer.substring(start,end);
           String command = thisAction.substring(0,4);
           String details = thisAction.substring(4);
           if      (command.equals("SCRN")) {
             fromServerSCRN = thisAction;
           } else if (command.equals("TEXT")) { 
             addText(details);
           } else if (command.equals("LOGN")) { 
              parseLogin(command, details);
           } else if (command.equals("INFO")) {
             fromServerINFO = details;
             drawInfo = true;
           } else if (command.equals("BAGI")) {
             parseBag(details);
           } else if (command.equals("STAT")) {
             fromServerSTAT = details;
           } else if (command.equals("INVE")) {
              parseInventory(details);
           } else if (command.equals("EQIP")) {
              parseEqp(details);
           } else if (command.equals("ATTR")) {
              fromServerATTR = details;
           } else if (command.equals("TARG")) {
              getTarget(details);
           } else if (!command.equals("NONE")) {
             addText("Unknown:"+command+" details="+details);
             System.out.println(fromServer);
           } // .. else if...
         } // if end
        } // for
        
        // ReDraw the Screen
        updateScreen();
        if (!inputMode.equals("DRAG")) paint(getGraphics());
        drawDragImage();
      } // while
    } catch (IOException exception) {
        //System.err.println("Error reading from stream.");
        //System.exit(1);
      } // try
    } // run
  };

  
  
  // ----------------------- Methods required for KeyListener --------------------------
  public void keyPressed(KeyEvent e) {
    String fromClient = "";
    int theCode = e.getKeyCode();
    fromClient = "INVALID"; // Clear Keypress
    if ((theCode == KeyEvent.VK_ESCAPE) && (loggedIn)) inputMode = "MOVE";
    
    if (inputMode.equals("MOVE")) {
      if (theCode == KeyEvent.VK_UP)    fromClient = "MOVEUP";
      else if (theCode == KeyEvent.VK_DOWN)  fromClient = "MOVEDOWN";
      else if (theCode == KeyEvent.VK_LEFT)  fromClient = "MOVELEFT";
      else if (theCode == KeyEvent.VK_RIGHT) fromClient = "MOVERIGHT";
      else if (theCode == KeyEvent.VK_Q)    {fromClient = "QUIT"; serverListen.cancel();}
      else if (theCode == KeyEvent.VK_P)     fromClient = "GPOS";
      else if (theCode == KeyEvent.VK_T) { // Enter TELL Mode
        inputMode = "TELLWHO";
        addText("Tell Who : ");
        tellIndex = textIndex;
        tempStr = new StringBuffer("");
      } else if (theCode == KeyEvent.VK_G) { // Enter GET Mode
        inputMode = "GET";
      } else if (theCode == KeyEvent.VK_E) { // Begin or End Displaying EQP (not an input mode)
        if (drawEqp == true) {
         fromClient = "EQUIPSTOP"; 
         drawEqp = false;
        } else {
         fromClient = "EQUIPSTART"; 
         drawEqp = true;
        }
      } else if (theCode == KeyEvent.VK_S) { // Begin or End Displaying Attributes (not an input mode)
        if (drawAttr== true) {
          fromClient = "ATTRSTOP";
          drawAttr = false;
        } else {
         fromClient = "ATTRSTART"; 
         drawAttr = true;
        }
      } else if (theCode == KeyEvent.VK_A) { // Enter ATTACK Mode
        inputMode = "ATTACK";
        //addText("Press a direction key, or click on the creature to attack.");
      } else if (theCode == KeyEvent.VK_D) {
        if (displayAllText) {
          displayAllText= false; 
          drawBorders = true; // a border was erased while displayAllText was drawing
          drawText = true;
        } else displayAllText = true;
      } else if (theCode == KeyEvent.VK_F) { // Enter assign free Attributes Mode
        inputMode = "GETFREEATTRIBUTE";
        tempStr = new StringBuffer();
        addText("Assigning Free Attributes");
        addText("Please enter the attribute name you would like to add to");
        addText("Attribute : ");
      }
      
      
    } else if (inputMode.equals("LOGINUSER")) {
      if (theCode == KeyEvent.VK_ENTER) {
        inputMode = "LOGINPASSWORD";
        addText("Password : ");
        tempStr = tempStr.append('~');
      } else if (Character.isDefined(e.getKeyChar())) {
        addKeyTotempStr(e); 
        modifyText("User Name : "+tempStr.toString());
      }
    } else if (inputMode.equals("LOGINPASSWORD")) {
      if (theCode == KeyEvent.VK_ENTER) {
        inputMode = "MOVE";
        fromClient = "USER"+tempStr.toString();
      } else if (Character.isDefined(e.getKeyChar())) {
        addKeyTotempStr(e); 
        // the below line displays passwords as they are typed
        //modifyText("Password : "+tempStr.toString().substring(tempStr.toString().indexOf('~')+1));
      }
    } else if (inputMode.equals("LOGIN_NEW_USER_YN")) {
      if (Character.isDefined(e.getKeyChar())) {
        if (theCode == KeyEvent.VK_Y) {
          inputMode = "NOINPUT";
          fromClient = "NEWU"+tempStr.toString();
        } else {
          tempStr = new StringBuffer();
          inputMode = "LOGINUSER";
          addText("User Name :");
        }
      }
    // TELL 
    } else if (inputMode.equals("TELLWHO")) {
      if (theCode == KeyEvent.VK_ENTER) {
        tellWhoStr = new StringBuffer("UN"+tempStr.toString()+"~"); // Start with UN if username typed in
        inputMode = "TELLWHAT"; 
        addText("Message : "); 
        tellIndex = textIndex; 
        tempStr = new StringBuffer(""); // clear the tempStr to hold the message
      } else if (theCode == KeyEvent.VK_UP) {tellWhoStr = new StringBuffer("UP~");   inputMode = "TELLWHAT";addText("Message : "); tellIndex = textIndex; tempStr = new StringBuffer("");}
      else if (theCode == KeyEvent.VK_DOWN) {tellWhoStr = new StringBuffer("DOWN~"); inputMode = "TELLWHAT";addText("Message : "); tellIndex = textIndex; tempStr = new StringBuffer("");}
      else if (theCode == KeyEvent.VK_LEFT) {tellWhoStr = new StringBuffer("LEFT~"); inputMode = "TELLWHAT";addText("Message : "); tellIndex = textIndex; tempStr = new StringBuffer("");}
      else if (theCode == KeyEvent.VK_RIGHT){tellWhoStr = new StringBuffer("RIGHT~");inputMode = "TELLWHAT";addText("Message : "); tellIndex = textIndex; tempStr = new StringBuffer("");}
      else if (Character.isDefined(e.getKeyChar())) {
        addKeyTotempStr(e); // local method
        modifyText(tellIndex, "Tell Who : "+tempStr.toString());
      }
    } else if (inputMode.equals("TELLWHAT")) {
      if (theCode == KeyEvent.VK_ENTER) {
        inputMode = "NEWTELLWHAT";
        fromClient = "TELL"+tellWhoStr.toString()+tempStr.toString(); 
        tempStr = new StringBuffer(""); // clear tempStr for the next message
      } else if (Character.isDefined(e.getKeyChar())) {
        addKeyTotempStr(e); // local method
        modifyText(tellIndex, "Message : "+tempStr.toString());
      }
    } else if (inputMode.equals("NEWTELLWHAT")) {
      if (Character.isLetterOrDigit(e.getKeyChar())) {
        inputMode = "TELLWHAT";
        addKeyTotempStr(e); // local method
        addText("Message : ");
        tellIndex = textIndex;
        modifyText(tellIndex, "Message : "+tempStr.toString());
      } else {
        inputMode = "MOVE";
      }
   } else if (inputMode.equals("GET")) {
      if (theCode == KeyEvent.VK_UP)    fromClient = "GETOUP";
      if (theCode == KeyEvent.VK_DOWN)  fromClient = "GETODOWN";
      if (theCode == KeyEvent.VK_LEFT)  fromClient = "GETOLEFT";
      if (theCode == KeyEvent.VK_RIGHT) fromClient = "GETORIGHT";
      if (theCode == KeyEvent.VK_G)     fromClient = "GETOHERE";
      inputMode = "MOVE";
    } else if (inputMode.equals("ATTACK")) {
      if (theCode == KeyEvent.VK_UP)    fromClient = "ATTKUP";
      if (theCode == KeyEvent.VK_DOWN)  fromClient = "ATTKDOWN";
      if (theCode == KeyEvent.VK_LEFT)  fromClient = "ATTKLEFT";
      if (theCode == KeyEvent.VK_RIGHT) fromClient = "ATTKRIGHT";
      if (theCode == KeyEvent.VK_A)     fromClient = "ATTKHERE";
      inputMode = "MOVE";
    } else if (inputMode.equals("TARGET")) {
      if (theCode == KeyEvent.VK_UP)    fromClient = "TARG"+fromServerTARG+"~UP";
      if (theCode == KeyEvent.VK_DOWN)  fromClient = "TARG"+fromServerTARG+"~DOWN";
      if (theCode == KeyEvent.VK_LEFT)  fromClient = "TARG"+fromServerTARG+"~LEFT";
      if (theCode == KeyEvent.VK_RIGHT) fromClient = "TARG"+fromServerTARG+"~RIGHT";
      inputMode = "MOVE";
    } else if (inputMode.equals("GETFREEATTRIBUTE")) {
      if (theCode == KeyEvent.VK_ENTER) {
        attributeName = tempStr.toString();
        tempStr = new StringBuffer();
        inputMode = "GETFREEATTRIBUTEVALUE";
        addText("How many free attribute points do you want to use raising your "+attributeName+"?");
        addText("Amount : ");
      } else if (Character.isDefined(e.getKeyChar())) {
        addKeyTotempStr(e); 
        modifyText("Attribute : "+tempStr.toString());
      }
    } else if (inputMode.equals("GETFREEATTRIBUTEVALUE")) {
      boolean validString = true;
      if (theCode == KeyEvent.VK_ENTER) {
        try {
          attributeAmountAdded = Double.valueOf(tempStr.toString()).doubleValue();
        } catch (NumberFormatException ex) { 
          addText("You must enter a number for the amount to be added");
          validString = false;
        }
        if (attributeAmountAdded < 0) {
          addText("The amount must be a positive number.");
          validString = false;
        }
        if (validString) {
          tempStr = new StringBuffer();
          inputMode = "MOVE";
          fromClient = "FREE"+attributeName+"~"+attributeAmountAdded;
        } else {  // not a valid Number (re-enter amount)
          tempStr = new StringBuffer();
          addText("Amount : ");
        }
      } else if (Character.isDefined(e.getKeyChar())) {
        addKeyTotempStr(e); 
        modifyText("Amount : "+tempStr.toString());
      }
    }// else if...
  
    if (fromClient != "INVALID") {
        out.println(fromClient);
    } // if
  } // method KeyPressed

  public void keyReleased(KeyEvent e) {
  }

  public void keyTyped(KeyEvent e) {
  }
  
  
  // -------------------- Methods inherited from MouseListener -----------------------------

  // Helper functions
  private boolean isMap(int x, int y) {
    if ((x > Constants.border) && (x < (Constants.border+(Constants.screenSizeX*Constants.scaleSize))) &&
        (y > Constants.border) && (y < (Constants.border+(Constants.screenSizeY*Constants.scaleSize)))) {
      return true;
    } else return false;
  }
  private boolean isInventory(int x, int y) {
    if ((x > (Constants.invX)) && (x < (Constants.invX+(Constants.invGridX*(Constants.imageSize+Constants.invBorder)))) &&
        (y > (Constants.invY)) && (y < (Constants.invY+(Constants.invGridY*(Constants.imageSize+Constants.invBorder))))) {
      return true;
    } else return false;
  }
  private boolean isBag(int x, int y) {
    if ((x > (Constants.bagX)) && (x < (Constants.bagX+(Constants.bagGridX*(Constants.imageSize+Constants.bagBorder)))) &&
        (y > (Constants.bagY)) && (y < (Constants.bagY+(Constants.bagGridY*(Constants.imageSize+Constants.bagBorder))))) {
      return true;
    } else return false;
  }
  private boolean isEqp(int x, int y) {
    if ((x > (Constants.eqpStartX)) && (x < (Constants.eqpStartX+Constants.eqpWidth)) &&
        (y > (Constants.eqpStartY)) && (y < (Constants.eqpStartY+Constants.eqpHeight)) &&
        (drawEqp == true)) { // and the eqp window is showing
      return true;
    } else return false;
  }

  public void mousePressed(MouseEvent event) {
    int x = event.getX();
    int y = event.getY();
    int localX = -1;
    int localY = -1;
    int localIndex = -1;
    String clickArea = "NONE";

    if (isMap(x,y)) { // get the localX,Y
      clickArea = "MAP";
      localX = ((x-Constants.border)/Constants.scaleSize);
      localY = ((y-Constants.border)/Constants.scaleSize);
    }
    if (isInventory(x,y)) { // get the inventory Index
        clickArea = "INV";
        localX = ((x - Constants.invX) / (Constants.imageSize+Constants.invBorder));
        localY = ((y - Constants.invY) / (Constants.imageSize+Constants.invBorder));
        localIndex = (localY*Constants.invGridX)+localX; // find the index in creature's inventory[]
    }
    if (isBag(x,y)) { // get the bag Index
        clickArea = "BAG";
        localX = ((x - Constants.bagX) / (Constants.imageSize+Constants.bagBorder));
        localY = ((y - Constants.bagY) / (Constants.imageSize+Constants.bagBorder));
        localIndex = (localY*Constants.bagGridX)+localX; // find the index in objects's contains[]
        if (localIndex >= bagMaxContains) localIndex = -1;
    }
    if (isEqp(x,y)) { // Set localIndex to the equip slot that is being altered
      clickArea="EQP";
      localIndex = ((y - Constants.eqpStartY)/(Constants.imageSize+Constants.eqpBorder));
    }

    if (inputMode.equals("GET") && (clickArea.equals("MAP"))) {
      out.println("GETOXY"+localX+","+localY);
      inputMode = "MOVE";
    } else if (inputMode.equals("ATTACK") && (clickArea.equals("MAP"))) {
      out.println("ATTKXY"+localX+","+localY);
      inputMode = "MOVE";
    } else if (inputMode.equals("TELLWHO") && (clickArea.equals("MAP"))) {
      out.println("DESCMAPXY"+localX+","+localY);
      tellWhoStr = new StringBuffer("XY"+localX+","+localY+"~");
      inputMode = "TELLWHAT";
      addText("Message : ");
      tellIndex = textIndex;
    } else if (inputMode.equals("TARGET") && (clickArea.equals("MAP"))) {
      out.println("TARG"+fromServerTARG+"~"+"MAPXY"+localX+","+localY);
      inputMode = "MOVE";
    } else if (inputMode.equals("MOVE")) {
      if (clickArea.equals("MAP")) {
        out.println("DESCMAPXY"+localX+","+localY);
        inputMode = "MOVE";
      } else if (clickArea.equals("INV") || clickArea.equals("BAG") || clickArea.equals("EQP")) { // Begin Drag 
        if ((event.getModifiers() == MouseEvent.BUTTON2_MASK) ||
            (event.getModifiers() == MouseEvent.BUTTON3_MASK)) {
         // Alternate mouse button clicked Display contents of the object, or use object
         if (clickArea.equals("INV"))      out.println("BAGIINV"+localIndex);
         else if (clickArea.equals("BAG")) out.println("BAGIBAG"+bagIndex+","+localIndex);
         else if (clickArea.equals("EQP")) out.println("BAGIEQP"+localIndex);
        } else { // Left mouse click (regular, view the object and begin dragging)
          if (clickArea.equals("INV")) {
            out.println("DESCINV"+localIndex);
            dragFrom = "INV"+localIndex;
          } else if (clickArea.equals("BAG")) {
            out.println("DESCBAG"+bagIndex+","+localIndex);
            dragFrom = "BAG"+bagIndex+","+localIndex;
          } else if (clickArea.equals("EQP")) {
            out.println("DESCEQP"+localIndex);
            dragFrom = "EQP"+localIndex;
          }
          // Get the inventory index in preparation for dragging it
          dragImageIndex = localIndex;
          inputMode = "DRAG";
          dragX = x;
          dragY = y;
        }
      }
    }
  }

  public void mouseReleased(MouseEvent event) {
    int x = event.getX();
    int y = event.getY();
    int localX = -1;
    int localY = -1;
    int localIndex = -1;
    String clickArea = "NONE";

    // Check to see if it's a map click (then convert to localX/Y)
    if (isMap(x,y)) {
      clickArea = "MAP";
      localX = ((x-Constants.border)/Constants.scaleSize);
      localY = ((y-Constants.border)/Constants.scaleSize);
    }
    
    // if It's an inventory Click then convert to inventory X,Y
    if (isInventory(x,y)) {
        clickArea = "INV";
        // Get Inventory position in X,Y co-ordinates
        localX = ((x - Constants.invX) / (Constants.imageSize+Constants.invBorder));
        localY = ((y - Constants.invY) / (Constants.imageSize+Constants.invBorder));
        localIndex = (localY*Constants.invGridX)+localX; // find the index in creature's inventory[]
    }
    // if It's a bag release then convert to bag X,Y
    if (isBag(x,y)) {
        clickArea = "BAG";
        // Get Inventory position in X,Y co-ordinates
        localX = ((x - Constants.bagX) / (Constants.imageSize+Constants.bagBorder));
        localY = ((y - Constants.bagY) / (Constants.imageSize+Constants.bagBorder));
        localIndex = (localY*Constants.bagGridX)+localX; // find the index in creature's inventory[]
        if (localIndex >= bagMaxContains) localIndex = -1;
    }
    if (isEqp(x,y)) {
       clickArea = "EQP";
       localIndex = ((y - Constants.eqpStartY)/(Constants.imageSize+Constants.eqpBorder));
    }
    
    if (inputMode.equals("DRAG")) {
      String dragTo = null;
      inputMode = "MOVE"; // end drag on mouseUp no matter where you are
      if (clickArea.equals("INV")) {
        dragTo = "INV"+localIndex;
      } else if (clickArea.equals("MAP")) {
        dragTo = "MAP"+localX+","+localY;
      } else if (clickArea.equals("BAG")) {
        dragTo = "BAG"+bagIndex+","+localIndex;
      } else if (clickArea.equals("EQP")) {
        dragTo = "EQP"+localIndex;
      }
      
      if ((dragTo != null) && (!dragTo.equals(dragFrom))){ // only if the item is being moved
        out.println("DRAG"+dragFrom+"(~TO~)"+dragTo);
      }
    }
    
    // Any mouse release will reset the drag variables
    dragX = -1;
    dragY = -1; 
  }

  public void mouseClicked(MouseEvent event) {}
  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}



  // ----------------- Methods from MouseMotionListener --------------------------------------

  public void mouseDragged(MouseEvent event) {
    int x = event.getX();
    int y = event.getY();
    if (inputMode.equals("DRAG")) {
      Graphics2D dragScreen = (Graphics2D) getGraphics();
      dragScreen.drawImage(dragBi,(BufferedImageOp)null,0,0);
      String imageName = "noimage";
      if ((dragFrom.length() > 3) && (dragImageIndex >=0)) {
        if      (dragFrom.substring(0,3).equals("INV")) imageName = inventory[dragImageIndex];
        else if (dragFrom.substring(0,3).equals("BAG")) imageName = bagInventory[dragImageIndex];
        else if (dragFrom.substring(0,3).equals("EQP")) imageName = eqpImages[dragImageIndex];
      }
      if ((!imageName.equals("noimage")) && (imageName.length() > 0)) {
        Image thisImg = (Image)(images.get(imageName.toLowerCase().trim()));
        if (thisImg != null) { // make sure the image exists
          dragScreen.drawImage(thisImg,x-(Constants.imageSize/2),y-(Constants.imageSize/2),this);
        } else { // image not found
          System.out.println("Image not found :"+imageName);
        }
      }
    }  
  }
  
  public void mouseMoved(MouseEvent event) {}

  
  // ----------------- Methods from Applet --------------------------------------------------

  public void destroy() {
    // Clean Up
    out.println("QUIT");
    serverListen.cancel(); 
    try {
      out.close();
      in.close();
      gSocket.close();
    } catch (IOException exception) {
      System.err.println("Error closing streams");
      System.exit(1);
    } // try
  } // method destroy

} //class

