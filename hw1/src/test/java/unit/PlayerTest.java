package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import models.Player;
import org.junit.jupiter.api.Test;

public class PlayerTest {

  @Test
  public void testMoveContructor() {
    char type = 'X';
    int id = 1;
    Player player = new Player(type, id);
    
    assertEquals(player.getType(), type);
    assertEquals(player.getId(), id);
  }
  
  @Test
  public void testSetType() {
    Player player = new Player('X', 1);
    player.setType('O');
    
    assertEquals(player.getType(), 'O');
  }
  
  @Test
  public void testSetId() {
    Player player = new Player('X', 1);
    player.setId(2);
    
    assertEquals(player.getId(), 2);
  }
}
