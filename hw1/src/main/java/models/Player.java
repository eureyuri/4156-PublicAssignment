package models;

public class Player {

  private char type;
  private int id;
  
  /**
   * Player describes the player who is playing the game.
   * There are some constraints on the parameter.
   * @param type  'X' or 'O'
   * @param id  '1' or '2'
   */
  public Player(char type, int id) {
    this.setType(type);
    this.setId(id);
  }

  //  Returns Player type
  public char getType() {
    return type;
  }
  
  //  Sets Player type
  public void setType(char type) {
    this.type = type;
  }

  //  Returns Player ID
  public int getId() {
    return id;
  }

  //  Sets Player ID
  public void setId(int id) {
    this.id = id;
  }

}
