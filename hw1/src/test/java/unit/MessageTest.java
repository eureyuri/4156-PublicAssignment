package unit;


import static org.junit.Assert.assertEquals;

import models.Message;
import org.junit.jupiter.api.Test;

public class MessageTest {
  
  @Test
  public void testMessageContructor() {
    boolean moveValidity = true;
    int code = 100;
    String message = "Test";
    Message msg = new Message(moveValidity, code, message);
    
    assertEquals(msg.isMoveValidity(), moveValidity);
    assertEquals(msg.getCode(), code);
    assertEquals(msg.getMessage(), message);
  }
  
  @Test
  public void testSetMoveValidity() {
    Message msg = new Message(true, 100, "Test");
    msg.setMoveValidity(false);
    
    assertEquals(msg.isMoveValidity(), false);
  }
  
  @Test
  public void testSetCode() {
    Message msg = new Message(true, 100, "Test");
    msg.setCode(200);
    
    assertEquals(msg.getCode(), 200);
  }
  
  @Test
  public void testSetMessage() {
    Message msg = new Message(true, 100, "Test");
    msg.setMessage("New message");
    
    assertEquals(msg.getMessage(), "New message");
  }
  
  @Test
  public void testToJson() {
    Message msg = new Message(true, 100, "Test");
    assertEquals(msg.toJson(), "{\"moveValidity\":true,\"code\":100,\"message\":\"Test\"}");
  }

}
