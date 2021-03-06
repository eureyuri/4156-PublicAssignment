package controllers;

import io.javalin.Javalin;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Queue;
import models.GameBoard;
import models.Message;
import org.eclipse.jetty.websocket.api.Session;
import utils.SqliteDatabase;

public class PlayGame {

  private static final int PORT_NUMBER = 8080;
  private static Javalin app;
  private static GameBoard gameBoard;
  private static SqliteDatabase db;

  /** Main method of the application.
   * @param args Command line arguments
   * @throws SQLException if problem with connection or statement
   */
  public static void main(final String[] args) throws SQLException {
    
    try {
      db = new SqliteDatabase();
      gameBoard = db.getGameBoardData();
    } catch (Exception e) {
      gameBoard = null;
    }
    
    
    app = Javalin.create(config -> {
      config.addStaticFiles("/public");
    }).start(PORT_NUMBER);
    
    // Redirect player to the View component and clean the database table before starting.
    app.get("/newgame", ctx -> {
      db.cleanTable();
      gameBoard = new GameBoard(null, null, false, -1, new char[3][3], 0, false);
      db.addGameBoardData(gameBoard);
      ctx.redirect("tictactoe.html");
    });
    
    // Start the game by initializing a game board with player specified type (X or O) 
    // from the request body
    app.post("/startgame", ctx -> {
      char type = ctx.formParam("type").charAt(0);
      
      if (type != 'X' && type != 'O') {
        ctx.result("Please use either X or O to play.");
        return;
      }
      
      db.cleanTable();
      gameBoard = new GameBoard(type);
      db.addGameBoardData(gameBoard);
      ctx.result(gameBoard.toJson());
    });
    
    // End point for player 2 to join. Redirects to the View after joining the game. If player 2 
    // tries to join the game before player 1 creates the game, simply return.
    app.get("/joingame", ctx -> {
      if (gameBoard.getP1() == null) {
        return;
      } else if (gameBoard.isGameStarted()) {
        ctx.result("Another user is playing the game.");
        return;
      }
      
      gameBoard.joinGame();
      db.addGameBoardData(gameBoard);
      sendGameBoardToAllPlayers(gameBoard.toJson());  
      ctx.redirect("/tictactoe.html?p=2");
    });
    
    // End point to return the current state of game board.
    app.get("/gameBoard", ctx -> {
      if (gameBoard.getP1() == null) {
        ctx.result("Game has not been created yet");
        return;
      }
      ctx.result(gameBoard.toJson());
    });
    
    // End point to handle player moves. 
    // Returns the validity response for the move as well as updates the board
    app.post("/move/:playerId", ctx -> {
      // If game board has not been created yet or the game has not been started yet, then return
      if (gameBoard.getP1() == null) {
        ctx.result("Game has not started yet");
        return;
      }
      
      // Get playerId and x, y coordinates from the path parameter and request body
      int playerId = Integer.parseInt(ctx.pathParam("playerId"));
      int x = Integer.parseInt(ctx.formParam("x"));
      int y = Integer.parseInt(ctx.formParam("y"));
      
      Message validityResponse = gameBoard.move(playerId, x, y);
      ctx.result(validityResponse.toJson());
      
      sendGameBoardToAllPlayers(gameBoard.toJson());  
      
      // Only persist valid data after response in database
      if (validityResponse.isMoveValidity()) {
        db.addGameBoardData(gameBoard);
      }
    });

    // Web sockets - DO NOT DELETE or CHANGE
    app.ws("/gameboard", new UiWebSocket());
  }

  /** Send message to all players.
   * @param gameBoardJson Gameboard JSON
   * @throws IOException Websocket message send IO Exception
   */
  private static void sendGameBoardToAllPlayers(final String gameBoardJson) {
    Queue<Session> sessions = UiWebSocket.getSessions();
    for (Session sessionPlayer : sessions) {
      try {
        sessionPlayer.getRemote().sendString(gameBoardJson);
      } catch (IOException e) {
        // Add logger here
      }
    }
  }

  public static void stop() {
    app.stop();
  }
}
