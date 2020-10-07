package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import models.GameBoard;
import models.Player;

public class SqliteDatabase {
  
  private Connection connection;
  private int key;
  private static final String DB_NAME = "tictactoe.db";
  private static final String TABLE_NAME = "GameBoard";

  public SqliteDatabase() {
    connection = null;
    key = 1;
    
    this.createConnection();
    this.createTable();
  }
  
  private void createConnection() {
    try {
      Class.forName("org.sqlite.JDBC");
      connection = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);      
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    
    System.out.println("Opened database successfully");
  }
  
  private void createTable() {
    Statement statement = null;
    
    try {
      statement = connection.createStatement();
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
      statement.close();
    } catch (Exception e) { 
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    
    System.out.println("Table created successfully");     
  }
  
  public void addGameBoardData(GameBoard gameBoard) {
    try {
      connection.setAutoCommit(false);
      System.out.println("Inserting game board data.");
      
      String p1Id;
      String p1Type;
      if (gameBoard.getP1() == null) {
        p1Id = "NULL";
        p1Type = "NULL";
      } else {
        p1Id = Integer.toString(gameBoard.getP1().getId());
        p1Type = String.valueOf(gameBoard.getP1().getType());
      }
      
      String p2Id;
      String p2Type;
      if (gameBoard.getP2() == null) {
        p2Id = "NULL";
        p2Type = "NULL";
      } else {
        p2Id = Integer.toString(gameBoard.getP2().getId());
        p2Type = String.valueOf(gameBoard.getP2().getType());
      }
      
      String gameStarted = gameBoard.isGameStarted() ? "1" : "0";
      String turn = Integer.toString(gameBoard.getTurn());
      String winner = Integer.toString(gameBoard.getWinner());
      String isDraw = gameBoard.isDraw() ? "1" : "0";
      
      String boardState = "";
      for (int i = 0; i < gameBoard.getBoardState().length; i++) {
        for (int j = 0; j < gameBoard.getBoardState()[i].length; j++) {
          char type = gameBoard.getBoardState()[i][j] == '\u0000' ? 'N' 
              : gameBoard.getBoardState()[i][j];
          boardState += type;
        }
      }
      
      String sql = "INSERT INTO " + TABLE_NAME 
          + " (ID, P1_ID, P1_TYPE, P2_ID, P2_TYPE, GAMESTARTED, TURN, BOARDSTATE, WINNER, ISDRAW)"
          + "VALUES ("
          + Integer.toString(key) + ", "
          + p1Id + ", "
          + "'" + p1Type + "', "
          + p2Id + ", "
          + "'" + p2Type + "', "
          + gameStarted + ", "
          + turn + ", "
          + "'" + boardState + "', "
          + winner + ", "
          + isDraw + ");";
      
      Statement statement = connection.createStatement();
      statement.execute(sql);
      statement.close();
      connection.commit();
      
      key++;
      
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }
  
  public GameBoard getGameBoardData() {
    try {
      System.out.println("Getting game board data from database.");
      
      String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY ID DESC LIMIT 1";
      Statement statement = connection.createStatement();      
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
          
      char[][] boardStateFormat = new char [3][3];
      for (int i = 0; i < boardStateFormat.length; i++) {
        for (int j = 0; j < boardStateFormat.length; j++) {
          char type = boardState.charAt(i + j) == 'N' ? '\u0000' : boardState.charAt(i + j);
          boardStateFormat[i][j] = type;
        }
      }
      
      GameBoard gameBoard = new GameBoard(new Player(p1Type, p1Id), new Player(p2Type, p2Id), 
          gameStarted, turn, boardStateFormat, winner, isDraw);
      
      statement.close();
      
      return gameBoard;
      
    } catch (Exception e) {
//      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.out.println("Empty databse");
      return null;
//      System.exit(0);
    }
  }
  
  public void cleanTable() {
    Statement statement = null;
    
    try {
      connection.setAutoCommit(false);
      statement = connection.createStatement();
      String sql = "DELETE FROM " + TABLE_NAME;
      statement.executeUpdate(sql);
      statement.close();
      connection.commit();
    } catch (Exception e) { 
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    
    System.out.println("Table cleaned successfully");  
  }
  
}
