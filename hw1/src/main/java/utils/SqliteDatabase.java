package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import models.GameBoard;
import models.Player;

public class SqliteDatabase {
  
  private Connection connection;
  private int key;
  private static final String DB_NAME = "tictactoe.db";
  private static final String TABLE_NAME = "GameBoard";

  /**
   * Constructor for the sqlite database.
   * Creates a connection and a table if it doesn't exist in the database.
   * @throws SQLException if problem with connection or statement 
   */
  public SqliteDatabase() throws SQLException {
    connection = null;
    key = 1;
    
    this.createConnection();
    this.createTable();
  }
  
  /**
   * Creates a connection with the database.
   */
  private void createConnection() {
    try {
      Class.forName("org.sqlite.JDBC");
      connection = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);      
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return;
    }
    
    System.out.println("Opened database successfully");
  }
  
  /**
   * Creating the table for game board if it does not exist.
   * @throws SQLException if problem with connection or statement
   */
  private void createTable() throws SQLException {
    Statement statement = connection.createStatement();
    
    try {
      String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " "
                  + "(ID INT PRIMARY KEY          NOT NULL," 
                  + " P1_ID         INT                   , "
                  + " P1_TYPE       CHAR(1)               , "
                  + " P2_ID         INT                   , "
                  + " P2_TYPE       CHAR(1)               , "
                  + " GAMESTARTED   BOOLEAN       NOT NULL, "
                  + " TURN          INT           NOT NULL, "
                  + " BOARDSTATE    CHAR(9)       NOT NULL, "
                  + " WINNER        INT           NOT NULL, "
                  + " ISDRAW        BOOLEAN       NOT NULL)";
      statement.executeUpdate(sql);
    } finally { 
      statement.close();
    }
    
    System.out.println("Table created successfully");     
  }
  
  /**
   * Stores the given GameBoard to the database.
   * Need to make a string representation for all data to store in the database.
   * @param gameBoard to be stored in the database
   * @throws SQLException if problem with connection or statement
   */
  public void addGameBoardData(GameBoard gameBoard) throws SQLException {
    String sql = "INSERT INTO " + TABLE_NAME 
        + " (ID, P1_ID, P1_TYPE, P2_ID, P2_TYPE, GAMESTARTED, TURN, BOARDSTATE, WINNER, ISDRAW)"
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement pstmt = connection.prepareStatement(sql);
    
    try {
      connection.setAutoCommit(false);
      System.out.println("Inserting game board data.");
      pstmt.setInt(1, key);
      
      String p1Id;
      String p1Type;
      if (gameBoard.getP1() == null) {
        p1Id = "NULL";
        p1Type = "NULL";
      } else {
        p1Id = Integer.toString(gameBoard.getP1().getId());
        p1Type = String.valueOf(gameBoard.getP1().getType());
      }
      pstmt.setString(2, p1Id);
      pstmt.setString(3, p1Type);
      
      String p2Id;
      String p2Type;
      if (gameBoard.getP2() == null) {
        p2Id = "NULL";
        p2Type = "NULL";
      } else {
        p2Id = Integer.toString(gameBoard.getP2().getId());
        p2Type = String.valueOf(gameBoard.getP2().getType());
      }
      pstmt.setString(4, p2Id);
      pstmt.setString(5, p2Type);
      
      String gameStarted = gameBoard.isGameStarted() ? "1" : "0";
      pstmt.setString(6, gameStarted);
      
      String turn = Integer.toString(gameBoard.getTurn());
      pstmt.setString(7, turn);
      
      // Creating a string representation of the board state
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < gameBoard.getBoardState().length; ++i) {
        for (int j = 0; j < gameBoard.getBoardState()[i].length; j++) {
          char type = gameBoard.getBoardState()[i][j] == '\u0000' ? 'N' 
              : gameBoard.getBoardState()[i][j];
          buf.append(type);
        }
      }
      String boardState = buf.toString();
      pstmt.setString(8, boardState);
      
      String winner = Integer.toString(gameBoard.getWinner());
      pstmt.setString(9, winner);
      
      String isDraw = gameBoard.isDraw() ? "1" : "0";
      pstmt.setString(10, isDraw);
      
      pstmt.executeUpdate();
      connection.commit();
      
      key++;
    } finally {
      pstmt.close();
    }
    
  }
  
  /**
   * Retrieving data from the database. 
   * @return GameBoard that was stored in the database
   * @throws SQLException if problem with connection or statement 
   */
  public GameBoard getGameBoardData() throws SQLException {
    Statement statement = connection.createStatement();
    
    try {
      System.out.println("Getting game board data from database.");
      
      String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY ID DESC LIMIT 1";
      ResultSet result = statement.executeQuery(sql);
      
      int p1Id = result.getInt("P1_ID");
      char p1Type = result.getString("P1_TYPE").charAt(0);
      int p2Id = result.getInt("P2_ID");
      char p2Type = result.getString("P2_TYPE").charAt(0);
      boolean gameStarted = result.getBoolean("GAMESTARTED");
      int turn = result.getInt("TURN");
      String boardState = result.getString("BOARDSTATE");
      int winner = result.getInt("WINNER");
      boolean isDraw = result.getBoolean("ISDRAW");
          
      // Reconstructing the board state
      int offset = 0;
      char[][] boardStateFormat = new char [3][3];
      for (int i = 0; i < boardStateFormat.length; i++) {
        for (int j = 0; j < boardStateFormat.length; j++) {
          char type = boardState.charAt(i + j + offset) == 'N' 
              ? '\u0000' : boardState.charAt(i + j + offset);
          boardStateFormat[i][j] = type;
        }
        
        offset += 2;
      }
      
      GameBoard gameBoard = new GameBoard(new Player(p1Type, p1Id), new Player(p2Type, p2Id), 
          gameStarted, turn, boardStateFormat, winner, isDraw);
      
      return gameBoard;
    } finally {
      statement.close();
    }
  }
  
  /**
   * Cleaning the table so that no records are present. 
   * @throws SQLException if problem with connection or statement 
   */
  public void cleanTable() throws SQLException {
    Statement statement = connection.createStatement();
    
    try {
      connection.setAutoCommit(false);
      String sql = "DELETE FROM " + TABLE_NAME;
      statement.executeUpdate(sql);
      connection.commit();
    } finally {
      statement.close();
    }
    
    System.out.println("Table cleaned successfully");  
  }
  
  /**
   * Closing connections to the database.
   * @throws SQLException if sqlite error
   */
  public void closeConenction() throws SQLException {
    connection.close();
  }
  
}
