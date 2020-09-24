package models;

public class Move {

  private Player player;
  private int moveX;
  private int moveY;
  
  /**
   * Describe the move taken by a certain Player.
   * @param player 1 or 2
   * @param moveX x coordinate of the move
   * @param moveY y coordinate of the move
   */
  public Move(Player player, int moveX, int moveY) {
    this.setPlayer(player);
    this.setMoveX(moveX);
    this.setMoveY(moveY);
  }

  //  Returns the player that is associated with the move
  public Player getPlayer() {
    return player;
  }
  
  //  Sets the player associated to the move
  public void setPlayer(Player player) {
    this.player = player;
  }

  //  Returns the x coordinate
  public int getMoveX() {
    return moveX;
  }

  //  Sets the x coordinate
  public void setMoveX(int moveX) {
    this.moveX = moveX;
  }

  //  Returns the y coordinate
  public int getMoveY() {
    return moveY;
  }

  //  Sets the y coordinate
  public void setMoveY(int moveY) {
    this.moveY = moveY;
  }

}
