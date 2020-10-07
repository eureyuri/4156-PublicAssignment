package unit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import models.GameBoard;
import models.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import utils.SqliteDatabase;

public class SqliteDatabaseTest {
  
  static SqliteDatabase db;
  
  @BeforeAll
  public static void init() throws SQLException {
    db = new SqliteDatabase();
  }
  
  /**
   * Before each test, clean the database table so tests are independent of each other.
   * @throws SQLException if there is a sql error
   */
  @BeforeEach
  public void clean() throws SQLException {
    // Clean DB
    db.cleanTable();
  }
  
  /**
   * Testing the constructor. Will only fail if there is a SQLException.
   */
  @Test
  @Order(1)
  public void testDbContructor() {
    try {
      db = new SqliteDatabase();
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }
  
  // Testing create connection private method using reflection. Check if db exists. 
  @Test
  @Order(2)
  public void testCreateConnection()
      throws NoSuchMethodException,
      SecurityException,
      IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException, 
      SQLException {
    Method method = SqliteDatabase.class.getDeclaredMethod("createConnection");
    method.setAccessible(true);
    
    method.invoke(db);
    
    boolean exists = new File("./tictactoe.db").exists();
    assertEquals(exists, true);
  }
  
  /**
   * Adding game board with P1.
   * @throws SQLException if sql error
   */
  @Test
  @Order(3)
  public void testAddGameBoardDataP1() throws SQLException {
    GameBoard gameBoard = new GameBoard('X');
    db.addGameBoardData(gameBoard);
    
    GameBoard g2 = db.getGameBoardData();
    
    assertEquals(gameBoard.getP1().getId(), g2.getP1().getId());
    assertEquals(gameBoard.getP1().getType(), g2.getP1().getType());
    assertEquals(0, g2.getP2().getId());
    assertEquals('N', g2.getP2().getType());
    assertEquals(gameBoard.isGameStarted(), g2.isGameStarted());
    assertEquals(gameBoard.getTurn(), g2.getTurn());
    assertArrayEquals(gameBoard.getBoardState(), g2.getBoardState());
    assertEquals(gameBoard.getWinner(), g2.getWinner());
    assertEquals(gameBoard.isDraw(), g2.isDraw());
  }
  
  /**
   * Adding game board with no players.
   * @throws SQLException if sql error
   */
  @Test
  @Order(4)
  public void testAddGameBoardDataNone() throws SQLException {
    GameBoard gameBoard = new GameBoard(null, null, false, 0, new char[3][3], 0, false);
    db.addGameBoardData(gameBoard);
    
    GameBoard g2 = db.getGameBoardData();
    
    assertEquals(0, g2.getP1().getId());
    assertEquals('N', g2.getP1().getType());
    assertEquals(0, g2.getP2().getId());
    assertEquals('N', g2.getP2().getType());
    assertEquals(gameBoard.isGameStarted(), g2.isGameStarted());
    assertEquals(gameBoard.getTurn(), g2.getTurn());
    assertArrayEquals(gameBoard.getBoardState(), g2.getBoardState());
    assertEquals(gameBoard.getWinner(), g2.getWinner());
    assertEquals(gameBoard.isDraw(), g2.isDraw());
  }
  
  /**
   * Adding game board with both players.
   * @throws SQLException if sql error
   */
  @Test
  @Order(5)
  public void testAddGameBoardDataP1P2() throws SQLException {
    GameBoard gameBoard = new GameBoard(new Player('X', 1), new Player('O', 2), 
        true, 0, new char[3][3], 0, false);
    db.addGameBoardData(gameBoard);
    
    GameBoard g2 = db.getGameBoardData();
    
    assertEquals(gameBoard.getP1().getId(), g2.getP1().getId());
    assertEquals(gameBoard.getP1().getType(), g2.getP1().getType());
    assertEquals(gameBoard.getP2().getId(), g2.getP2().getId());
    assertEquals(gameBoard.getP2().getType(), g2.getP2().getType());
    assertEquals(gameBoard.isGameStarted(), g2.isGameStarted());
    assertEquals(gameBoard.getTurn(), g2.getTurn());
    assertArrayEquals(gameBoard.getBoardState(), g2.getBoardState());
    assertEquals(gameBoard.getWinner(), g2.getWinner());
    assertEquals(gameBoard.isDraw(), g2.isDraw());
  }
  
  /**
   * Adding game board with draw state.
   * @throws SQLException if sql error
   */
  @Test
  @Order(6)
  public void testAddGameBoardDataDraw() throws SQLException {
    char[][] drawState = {{'X', 'O', 'X'}, {'O', 'O', 'X'}, {'O', 'X', 'X'}};
    GameBoard gameBoard = new GameBoard(new Player('X', 1), new Player('O', 2), 
        true, 0, drawState, 0, true);
    db.addGameBoardData(gameBoard);
    
    GameBoard g2 = db.getGameBoardData();
    
    assertEquals(gameBoard.getP1().getId(), g2.getP1().getId());
    assertEquals(gameBoard.getP1().getType(), g2.getP1().getType());
    assertEquals(gameBoard.getP2().getId(), g2.getP2().getId());
    assertEquals(gameBoard.getP2().getType(), g2.getP2().getType());
    assertEquals(gameBoard.isGameStarted(), g2.isGameStarted());
    assertEquals(gameBoard.getTurn(), g2.getTurn());
    assertArrayEquals(gameBoard.getBoardState(), g2.getBoardState());
    assertEquals(gameBoard.getWinner(), g2.getWinner());
    assertEquals(gameBoard.isDraw(), g2.isDraw());
  }
  
  /**
   * Getting game board state with win state.
   * @throws SQLException if sql error
   */
  @Test
  @Order(7)
  public void testGetGameBoardData() throws SQLException {
    char[][] winState = {{'X', 'X', 'X'}, {'O', 'O', '\u0000'}, {'\u0000', '\u0000', '\u0000'}};
    GameBoard gameBoard = new GameBoard(new Player('X', 1), new Player('O', 2), 
        false, 2, winState, 1, false);
    db.addGameBoardData(gameBoard);
    
    GameBoard g2 = db.getGameBoardData();
    
    assertEquals(gameBoard.getP1().getId(), g2.getP1().getId());
    assertEquals(gameBoard.getP1().getType(), g2.getP1().getType());
    assertEquals(gameBoard.getP2().getId(), g2.getP2().getId());
    assertEquals(gameBoard.getP2().getType(), g2.getP2().getType());
    assertEquals(gameBoard.isGameStarted(), g2.isGameStarted());
    assertEquals(gameBoard.getTurn(), g2.getTurn());
    assertArrayEquals(gameBoard.getBoardState(), g2.getBoardState());
    assertEquals(gameBoard.getWinner(), g2.getWinner());
    assertEquals(gameBoard.isDraw(), g2.isDraw());
  }

}
