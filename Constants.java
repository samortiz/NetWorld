import java.awt.Color;

public class Constants {

  // Shared
  public static final String commandSeperator = "(~~~)";
  public static final int    screenSizeX = 21;
  public static final int    screenSizeY = 21;

  // Server Side
  public static final int    mapMaxX = 100;
  public static final int    mapMaxY = 100;
  public static final int    maxObj = 1000;
  public static final int    maxCreatures = 500;
  public static final int    maxPlayers = 100;
  public static final int    maxSpells = 1000;
  public static final int    maxMapTypes = 256;
  public static final String mapDefsFile = "config/mapDefs.txt";
  public static final String worldMapFile = "config/worldMap.txt";
  public static final String worldObjFile = "config/worldObj.txt";
  public static final String worldCreaturesFile = "config/worldCreatures.txt";
  public static final String playerInfoDir = "players/";
  public static final String imageDir = "images";
  public static final int    objGlobalMaxContains = 24;
  
  // Client Side
  public static final String hostName = "www.pongbee.com";
  public static final int    border = 2;
  public static final Color  mainBorderColor = Color.black;
  public static final int    scaleSize = 20;
  public static final int    imageSize = 20; // same thing as above with a different name
  public static final int    appletSizeX = 600;
  public static final int    appletSizeY = 500;

  // Text 
  public static final int    maxLinesOfText = 50;
  public static final int    displayLinesOfText = 5;
  public static final int    textIndent = 2;
  public static final int    textSpacing = 13;
  public static final int    textHeight = 10;
  
  // Map, Stats and Info
  public static final int    mapSizeX = (screenSizeX*scaleSize);
  public static final int    mapSizeY = (screenSizeY*scaleSize);
  public static final int    statsHeight = 85; //Just height, because X is calculated from remaining space
  public static final int    infoHeight = 150; // Just height, because X is calculated from remaining space
  
  // Inventory Constants
  public static final int   invHeight  = 85; // Just Y, because X is calculated from remaining space (invWidth)
  public static final int   invBorder = 1;
  public static final int   invGridX = 8; // 8 x 3 Inventory Spaces (Should match Creature.maxObj)
  public static final int   invGridY = 3;
  public static final int   maxCreatureInventory = invGridX * invGridY; 
  public static final int   invStartX = (border*2)+mapSizeX;
  public static final int   invStartY = (border*3)+statsHeight+infoHeight;
  public static final int   invWidth = (appletSizeX-((border*3)+mapSizeX));
  public static final int   invX = invStartX + 3;  // x start position of the grid
  public static final int   invY = invStartY + textHeight + 3 + 3; // Y start position of the grid (use -3px to put text)
  public static final Color invBackground = new Color(255,255,230);
  public static final Color invText = new Color(60,60,10);
  public static final Color invGridColor = new Color(100,100,30);
  
  // Bag Constants (Sub Items)
  public static final int   bagHeight  = 85; // Just Y, because X is calculated from remaining space (bagWidth)
  public static final int   bagBorder = 1;
  public static final int   bagGridX = 8; // 8 x 3 Spaces
  public static final int   bagGridY = 3;
  public static final int   maxBagInventory = bagGridX * bagGridY; 
  public static final int   bagStartX = (border*2)+mapSizeX;
  public static final int   bagStartY = (border*4)+statsHeight+infoHeight+invHeight;
  public static final int   bagWidth = (appletSizeX-((border*3)+mapSizeX));
  public static final int   bagX = bagStartX + 3;  // x start position of the grid
  public static final int   bagY = bagStartY + textHeight + 3 + 3; // Y start position of the grid (use -3px to put text)
  public static final Color bagBackground = new Color(255,255,230);
  public static final Color bagText = new Color(60,60,10);
  public static final Color bagGridColor = new Color(100,100,30);
  public static final Color bagEmptyColor = new Color(200,200,130);

  // Attr Constants (Attribute display)
  public static final int   attrHeight  = 100; 
  public static final int   attrWidth = mapSizeX;
  public static final int   attrStartX = border;
  public static final int   attrStartY = border;
  public static final Color attrBackground = new Color(230,230,230);
  public static final Color attrText = new Color(50,50,50);

  // Eqp Constants (Equipped Inventory)
  public static final int   eqpBorder = 3;
  public static final int   eqpHeight  = Creature.maxEquip * (imageSize+eqpBorder); 
  public static final int   eqpWidth = 80;
  public static final int   eqpStartX = border+mapSizeX - eqpWidth;
  public static final int   eqpStartY = attrHeight;
  public static final Color eqpBackground = new Color(230,230,230);
  public static final Color eqpText = new Color(60,60,60);
  public static final Color eqpBorderColor = new Color(100,100,100);
  public static final Color eqpEmptyColor = new Color(200,200,200);


}
