package controllers;

import io.javalin.Javalin;
import java.io.IOException;
import java.util.Queue;
import models.GameBoard;
import models.Message;
import org.eclipse.jetty.websocket.api.Session;

class PlayGame {

  private static final int PORT_NUMBER = 8080;
  private static Javalin app;
  private static GameBoard gameBoard;

  /** Main method of the application.
   * @param args Command line arguments
   */
  public static void main(final String[] args) {

    app = Javalin.create(config -> {
      config.addStaticFiles("/public");
    }).start(PORT_NUMBER);

    // Test Echo Server
    app.post("/echo", ctx -> {
      ctx.result(ctx.body());
    });
    
    // Redirect player to the View component
    app.get("/newgame", ctx -> {
      ctx.redirect("tictactoe.html");
    });
    
    // Start the game by initializing a game board with player specified type (X or O) 
    // from the request body
    app.post("/startgame", ctx -> {
      char type = ctx.formParam("type").charAt(0);
      gameBoard = new GameBoard(type);
      ctx.result(gameBoard.toJson());
    });
    
    // End point for player 2 to join. Redirects to the View after joining the game. If player 2 
    // tries to join the game before player 1 creates the game, simply return.
    app.get("/joingame", ctx -> {
      if (gameBoard == null) {
        return;
      }
      
      gameBoard.joinGame();
      sendGameBoardToAllPlayers(gameBoard.toJson());  
      ctx.redirect("/tictactoe.html?p=2");
    });
    
    // End point to handle player moves. 
    // Returns the validity response for the move as well as updates the board
    app.post("/move/:playerId", ctx -> {
      // If game board has not been created yet or the game has not been started yet, then return
      if (gameBoard == null || !gameBoard.isGameStarted()) {
        return;
      }
      
      // Get playerId and x, y coordinates from the path parameter and request body
      int playerId = Integer.parseInt(ctx.pathParam("playerId"));
      int x = Integer.parseInt(ctx.formParam("x"));
      int y = Integer.parseInt(ctx.formParam("y"));
      
      Message validityResponse = gameBoard.move(playerId, x, y);
      ctx.result(validityResponse.toJson());
      sendGameBoardToAllPlayers(gameBoard.toJson());  
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
