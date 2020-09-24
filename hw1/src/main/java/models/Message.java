package models;

import com.google.gson.Gson;

public class Message {

  private boolean moveValidity;
  private int code;
  private String message;

  /**
   * Message that will be displayed when a Player makes a move.
   * @param moveValidity if the move is a valid move
   * @param code also indicates validity of a move. 100 for valid, 200 for invalid.
   * @param message message to be displayed
   */
  public Message(boolean moveValidity, int code, String message) {
    setMoveValidity(moveValidity);
    setCode(code);
    setMessage(message);
  }


  // Returns moveValidity
  public boolean isMoveValidity() {
    return moveValidity;
  }

  // Sets moveValidity
  public void setMoveValidity(boolean moveValidity) {
    this.moveValidity = moveValidity;
  }

  // Returns the code
  public int getCode() {
    return code;
  }

  // Sets the code
  public void setCode(int code) {
    this.code = code;
  }

  // Returns the message
  public String getMessage() {
    return message;
  }

  // Sets the message
  public void setMessage(String message) {
    this.message = message;
  }
  
  // Converts the Message into JSON format
  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
