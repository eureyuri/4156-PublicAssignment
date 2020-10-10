package unit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import models.GameBoard;
import models.Message;
import models.Move;
import models.Player;
import org.junit.jupiter.api.Test;

public class GameBoardTest {

  @Test
  public void testGameBoardContructor() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    
    assertEquals(gameBoard.getP1().getType(), 'X');
    assertEquals(gameBoard.getP1().getId(), 1);
    assertEquals(gameBoard.getP2(), null);
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getTurn(), 1);
    assertArrayEquals(gameBoard.getBoardState(), new char[3][3]);
    assertEquals(gameBoard.getWinner(), 0);
    assertEquals(gameBoard.isDraw(), false);
  }
  
  @Test
  public void testGameBoardContructor2() {
    Player p1 = new Player('X', 1);
    Player p2 = new Player('O', 2);
    GameBoard gameBoard = new GameBoard(p1, p2, false, 1, new char[3][3], 0, false);
    
    assertEquals(gameBoard.getP1().getType(), p1.getType());
    assertEquals(gameBoard.getP1().getId(), p1.getId());
    assertEquals(gameBoard.getP2().getType(), p2.getType());
    assertEquals(gameBoard.getP2().getId(), p2.getId());
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getTurn(), 1);
    assertArrayEquals(gameBoard.getBoardState(), new char[3][3]);
    assertEquals(gameBoard.getWinner(), 0);
    assertEquals(gameBoard.isDraw(), false);
  }
  
  @Test
  public void testSetP1() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    Player newPlayer = new Player('O', 1);
    gameBoard.setP1(newPlayer);
    
    assertEquals(gameBoard.getP1(), newPlayer);
  }
  
  @Test
  public void testSetP2() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    Player newPlayer = new Player('O', 2);
    gameBoard.setP2(newPlayer);
    
    assertEquals(gameBoard.getP2(), newPlayer);
  }
  
  @Test
  public void testSetGameStarted() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.setGameStarted(true);
    
    assertEquals(gameBoard.isGameStarted(), true);
  }
  
  @Test
  public void testSetTurn() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.setTurn(2);
    
    assertEquals(gameBoard.getTurn(), 2);
  }
  
  @Test
  public void testSetBoardState() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    char[][] newState = {{'O', 'O', 'O'}, {'O', 'O', 'O'}, {'O', 'O', 'O'}};
    gameBoard.setBoardState(newState);
    
    assertArrayEquals(gameBoard.getBoardState(), newState);
  }
  
  @Test
  public void testSetWinner() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.setWinner(1);
    
    assertEquals(gameBoard.getWinner(), 1);
  }
  
  @Test
  public void testSetDraw() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.setDraw(true);
    
    assertEquals(gameBoard.isDraw(), true);
  }
  
  @Test
  public void testJoinGameX() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    
    assertEquals(gameBoard.getP2().getType(), 'O');
    assertEquals(gameBoard.getP2().getId(), 2);
    assertEquals(gameBoard.isGameStarted(), true);
  }
  
  @Test
  public void testJoinGameO() {
    char p1Type = 'O';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    
    assertEquals(gameBoard.getP2().getType(), 'X');
    assertEquals(gameBoard.getP2().getId(), 2);
    assertEquals(gameBoard.isGameStarted(), true);
  }
  
  @Test
  public void testMoveValid() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    Message msg = gameBoard.move(1, 0, 0);
    
    assertEquals(gameBoard.getBoardState()[0][0], 'X');
    assertEquals(gameBoard.getTurn(), 2);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
  }
  
  @Test
  public void testMoveValidP2() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    Message msg2 = gameBoard.move(2, 0, 1);
    
    assertEquals(gameBoard.getBoardState()[0][1], 'O');
    assertEquals(gameBoard.getTurn(), 1);
    assertEquals(msg2.isMoveValidity(), true);
    assertEquals(msg2.getCode(), 100);
    assertEquals(msg2.getMessage(), "");
  }
  
  /**
   * Testing with invalid player id.
   */
  @Test
  public void testMoveInvalidId() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    Message msg = gameBoard.move(3, 0, 0);
    
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid player id!");
  }
  
  /**
   * Move before game has been started. 
   */
  @Test
  public void testMoveGameNotStarted() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.setGameStarted(false);
    Message msg = gameBoard.move(1, 0, 0);
    
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Game has not started yet!");
  }
  
  /**
   * Test invalid move: negative x.
   */
  @Test
  public void testMoveInvalidMove1() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    Message msg = gameBoard.move(1, -1, 0);
    
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid move!");
  }
  
  /**
   * Test invalid move: > 2 x.
   */
  @Test
  public void testMoveInvalidMove2() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    Message msg = gameBoard.move(1, 3, 0);
    
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid move!");
  }
  
  /**
   * Test invalid move: negative y.
   */
  @Test
  public void testMoveInvalidMove3() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    Message msg = gameBoard.move(1, 0, -1);
    
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid move!");
  }
  
  /**
   * Test invalid move: > 2 y.
   */
  @Test
  public void testMoveInvalidMove4() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    Message msg = gameBoard.move(1, 0, 3);
    
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid move!");
  }
  
  @Test
  public void testMoveNotCorrectTurn() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    Message msg = gameBoard.move(2, 0, 0);
    
    assertEquals(gameBoard.getBoardState()[0][0], '\0');
    assertEquals(gameBoard.getTurn(), 1);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Please wait for your turn!");
  }
  
  @Test
  public void testMoveNotOpen() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    Message msg2 = gameBoard.move(2, 0, 0);
    
    assertEquals(gameBoard.getBoardState()[0][0], 'X');
    assertEquals(gameBoard.getTurn(), 2);
    assertEquals(msg2.isMoveValidity(), false);
    assertEquals(msg2.getCode(), 200);
    assertEquals(msg2.getMessage(), "Please select an open slot!");
  }
  
  @Test
  public void testMoveDraw() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 0, 1);
    gameBoard.move(1, 0, 2);
    gameBoard.move(2, 1, 1);
    gameBoard.move(1, 1, 0);
    gameBoard.move(2, 2, 0);
    gameBoard.move(1, 1, 2);
    gameBoard.move(2, 2, 2);
    Message msg = gameBoard.move(1, 2, 1);
    
    assertEquals(gameBoard.isDraw(), true);
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
  }
  
  @Test
  public void testMoveWinColumn() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 0, 1);
    gameBoard.move(1, 1, 0);
    gameBoard.move(2, 1, 1);
    Message msg = gameBoard.move(1, 2, 0);
    
    assertEquals(gameBoard.getWinner(), 1);
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
  }
  
  @Test
  public void testMoveWinRow() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 1, 0);
    gameBoard.move(1, 0, 1);
    gameBoard.move(2, 1, 1);
    Message msg = gameBoard.move(1, 0, 2);
    
    assertEquals(gameBoard.getWinner(), 1);
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
  }
  
  @Test
  public void testMoveWinLeftDiagonal() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 1, 0);
    gameBoard.move(1, 1, 1);
    gameBoard.move(2, 1, 2);
    Message msg = gameBoard.move(1, 2, 2);
    
    assertEquals(gameBoard.getWinner(), 1);
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
  }
  
  @Test
  public void testMoveWinRightDiagonal() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 2);
    gameBoard.move(2, 1, 0);
    gameBoard.move(1, 1, 1);
    gameBoard.move(2, 1, 2);
    Message msg = gameBoard.move(1, 2, 0);
    
    assertEquals(gameBoard.getWinner(), 1);
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
  }
  
  @Test
  public void testMoveAfterDraw() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.setDraw(true);
    Message msg = gameBoard.move(1, 0, 0);
    
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Game over!");
  }
  
  @Test
  public void testMoveAfterWinner() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.setWinner(1);
    Message msg = gameBoard.move(1, 0, 0);
    
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Game over!");
  }
  
  @Test
  public void testToJson() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    
    assertEquals(gameBoard.toJson(), "{\"p1\":{\"type\":\"X\",\"id\":1},"
        + "\"gameStarted\":false,\"turn\":1,\"boardState\":"
        + "[[\"\\u0000\",\"\\u0000\",\"\\u0000\"],[\"\\u0000\",\"\\u0000\",\"\\u0000\"],"
        + "[\"\\u0000\",\"\\u0000\",\"\\u0000\"]],\"winner\":0,\"isDraw\":false}");
  }
  
  
  // Testing private methods using reflection
  
  @Test
  public void testDetermineP2Type()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    
    Method method = GameBoard.class.getDeclaredMethod("determineP2Type");
    method.setAccessible(true);
    
    char p2Type = (char) method.invoke(gameBoard);
    assertEquals(p2Type, 'O');
  }
  
  @Test
  public void testIsOpenSlot()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    
    Method method = GameBoard.class.getDeclaredMethod("isOpenSlot", Move.class);
    method.setAccessible(true);
    
    boolean isOpen = (boolean) method.invoke(gameBoard, new Move(gameBoard.getP2(), 0, 0));
    assertEquals(isOpen, false);
  }
  
  @Test
  public void testWinnerCol()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 0, 1);
    gameBoard.move(1, 1, 0);
    gameBoard.move(2, 1, 1);
    gameBoard.move(1, 2, 0);
    
    Method method = GameBoard.class.getDeclaredMethod("checkWinner", Player.class);
    method.setAccessible(true);
    
    boolean isWin = (boolean) method.invoke(gameBoard, gameBoard.getP1());
    assertEquals(isWin, true);
  }
  
  // Covering cases where filled column is not a win
  @Test
  public void testCol()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 0, 2);
    gameBoard.move(1, 0, 1);
    
    Method method = GameBoard.class.getDeclaredMethod("checkWinner", Player.class);
    method.setAccessible(true);
    
    boolean isWin = (boolean) method.invoke(gameBoard, gameBoard.getP1());
    assertEquals(isWin, false);
  }
  
  @Test
  public void testWinnerRow()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 1, 0);
    gameBoard.move(1, 0, 1);
    gameBoard.move(2, 1, 1);
    gameBoard.move(1, 0, 2);
    
    Method method = GameBoard.class.getDeclaredMethod("checkWinner", Player.class);
    method.setAccessible(true);
    
    boolean isWin = (boolean) method.invoke(gameBoard, gameBoard.getP1());
    assertEquals(isWin, true);
  }
  
  // Covering cases where filled row is not a win
  @Test
  public void testRow()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 1, 0);
    gameBoard.move(1, 2, 0);
    
    Method method = GameBoard.class.getDeclaredMethod("checkWinner", Player.class);
    method.setAccessible(true);
    
    boolean isWin = (boolean) method.invoke(gameBoard, gameBoard.getP1());
    assertEquals(isWin, false);
  }
  
  @Test
  public void testWinnerLeftDiagonal()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 1, 0);
    gameBoard.move(1, 1, 1);
    gameBoard.move(2, 1, 2);
    gameBoard.move(1, 2, 2);
    
    Method method = GameBoard.class.getDeclaredMethod("checkWinner", Player.class);
    method.setAccessible(true);
    
    boolean isWin = (boolean) method.invoke(gameBoard, gameBoard.getP1());
    assertEquals(isWin, true);
  }
  
  // Covering cases where filled left diagonal is not a win
  @Test
  public void testLeftDiagonal()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 1, 0);
    gameBoard.move(1, 1, 1);
    gameBoard.move(2, 2, 1);
    gameBoard.move(1, 1, 2);
    gameBoard.move(2, 2, 2);
    
    Method method = GameBoard.class.getDeclaredMethod("checkWinner", Player.class);
    method.setAccessible(true);
    
    boolean isWin = (boolean) method.invoke(gameBoard, gameBoard.getP1());
    assertEquals(isWin, false);
  }
  
  @Test
  public void testWinnerRightDiagonal()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 2);
    gameBoard.move(2, 1, 0);
    gameBoard.move(1, 1, 1);
    gameBoard.move(2, 1, 2);
    gameBoard.move(1, 2, 0);
    
    Method method = GameBoard.class.getDeclaredMethod("checkWinner", Player.class);
    method.setAccessible(true);
    
    boolean isWin = (boolean) method.invoke(gameBoard, gameBoard.getP1());
    assertEquals(isWin, true);
  }
  
  // Covering cases where filled right diagonal is not a win
  @Test
  public void testRightDiagonal()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 1, 0);
    gameBoard.move(2, 2, 0);
    gameBoard.move(1, 1, 1);
    gameBoard.move(2, 2, 1);
    gameBoard.move(1, 0, 2);
    gameBoard.move(2, 1, 2);
    
    Method method = GameBoard.class.getDeclaredMethod("checkWinner", Player.class);
    method.setAccessible(true);
    
    boolean isWin = (boolean) method.invoke(gameBoard, gameBoard.getP1());
    assertEquals(isWin, false);
  }
  
  @Test
  public void testWinnerNone()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    
    Method method = GameBoard.class.getDeclaredMethod("checkWinner", Player.class);
    method.setAccessible(true);
    
    boolean isWin = (boolean) method.invoke(gameBoard, gameBoard.getP1());
    assertEquals(isWin, false);
  }
  
  @Test
  public void testDraw()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    gameBoard.move(1, 0, 0);
    gameBoard.move(2, 0, 1);
    gameBoard.move(1, 0, 2);
    gameBoard.move(2, 1, 1);
    gameBoard.move(1, 1, 0);
    gameBoard.move(2, 2, 0);
    gameBoard.move(1, 1, 2);
    gameBoard.move(2, 2, 2);
    gameBoard.move(1, 2, 1);
    
    Method method = GameBoard.class.getDeclaredMethod("checkDraw");
    method.setAccessible(true);
    
    boolean isDraw = (boolean) method.invoke(gameBoard);
    assertEquals(isDraw, true);
  }
  
}
