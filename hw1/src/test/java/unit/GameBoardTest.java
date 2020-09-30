package unit;

import models.GameBoard;
import models.Message;
import models.Player;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
  public void testJoinGameY() {
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
  public void testMoveWinColumn2() {
    char p1Type = 'X';
    GameBoard gameBoard = new GameBoard(p1Type);
    gameBoard.joinGame();
    Message msg = gameBoard.move(1, 0, 0);
    
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
  
}
