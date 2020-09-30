package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import models.Move;
import models.Player;
import org.junit.jupiter.api.Test;

public class MoveTest {

  @Test
  public void testMoveContructor() {
    Player player = new Player('X', 1);
    int moveX = 0;
    int moveY = 0;
    Move move = new Move(player, moveX, moveY);
    
    assertEquals(move.getPlayer(), player);
    assertEquals(move.getMoveX(), moveX);
    assertEquals(move.getMoveY(), moveY);
  }
  
  @Test
  public void testSetPlayer() {
    Move move = new Move(new Player('X', 1), 0, 0);
    Player newPlayer = new Player('O', 2);
    move.setPlayer(newPlayer);
    
    assertEquals(move.getPlayer(), newPlayer);
  }
  
  @Test
  public void testSetMoveX() {
    Move move = new Move(new Player('X', 1), 0, 0);
    move.setMoveX(1);
    
    assertEquals(move.getMoveX(), 1);
  }
  
  @Test
  public void testSetMoveY() {
    Move move = new Move(new Player('X', 1), 0, 0);
    move.setMoveY(1);
    
    assertEquals(move.getMoveY(), 1);
  }
}
