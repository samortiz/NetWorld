public class mapElement {
  // Variables
  public String  img;
  public String  name;
  public char    fileChar;
  public int     move;
 
  // Constructor
  mapElement() {
    img = "NULLMAP";
    name = "void";
    fileChar = 'X';
    move = 1;
  }

  mapElement(String img, String name, char fileChar, int move) {
    this.img = img;
    this.name = name;
    this.fileChar = fileChar;
    this.move = move;
  }
 
  public String describe() {
    String retVal = name+"~";
    retVal += img+"~";
    if (move == -1) retVal += "You cannot walk here.~";
    else if (move == 0) retVal += "You can freely walk here.~";
    else retVal += "Your movement here is ~effected by "+move+".~";
    return retVal;
  }  
 
}