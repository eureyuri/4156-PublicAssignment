package integration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import controllers.PlayGame;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import models.GameBoard;
import models.Message;
import models.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class) 
public class PlayGameTest {
  
  /**
   * Runs only once before the testing starts.
   * Starts the server.
   */
  @BeforeAll
  public static void init() {
    // Start Server
    PlayGame.main(new String[0]);
    System.out.println("Starting the server.");
  }

  /**
   * Starts a new game before every test.
   */
  @BeforeEach
  public void startNewGame() throws Exception {
    Unirest.get("http://localhost:8080/newgame").asString();
  }

  /**
   * Evaluate the /newgame endpoint.
   */
  @Test
  @Order(1)
  public void newGameTest() {
    HttpResponse<?> response = Unirest.get("http://localhost:8080/newgame").asString();
    int restStatus = response.getStatus();

    assertEquals(restStatus, 200);
  }
  
  /**
   * Testing /joingame endpoint before P1 creates the game (before /startgame).
   */
  @Test
  @Order(2)
  public void joinBeforeP1() {
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBody = (String) responseBoard.getBody();

    assertEquals(responseBody, "Game has not been created yet");
  }
  
  /**
   * Testing move before player 1 or player 2 joins the game.
   */
  @Test
  @Order(3)
  public void moveBeforeP1JoinsTest() {
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBody = (String) responseBoard.getBody();

    assertEquals(responseBody, "Game has not been created yet");
  }

  /**
   * Testing startgame endpoint.
   */
  @Test
  @Order(4)
  public void startGameTest() {
    HttpResponse<?> response = Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    String responseBody = (String) response.getBody();
    JSONObject jsonObject = new JSONObject(responseBody);

    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    Player player1 = gameBoard.getP1();

    // Check if game started after player 1 joins: Game should not start at this point
    assertEquals('X', player1.getType());
    assertEquals(false, gameBoard.isGameStarted());
    assertEquals(1, gameBoard.getTurn());
    assertEquals(0, gameBoard.getWinner());
    assertEquals(false, gameBoard.isDraw());
    assertArrayEquals(new char[3][3], gameBoard.getBoardState());
  }
  
  /**
   * Test for starting game with a type that is not either X or O.
   */
  @Test
  @Order(5)
  public void startInvalidTypeTet() {
    HttpResponse<?> response = Unirest.post("http://localhost:8080/startgame").body("type=A").asString();
    
    String responseBody = (String) response.getBody();
    assertEquals(responseBody, "Please use either X or O to play.");
  }
  
  /**
   * Testing move after player 1 joins and before player 2 joins.
   */
  @Test
  @Order(6)
  public void moveBeforeP2JoinsTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsonMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsonMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Game has not started yet!");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonObject = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    
    assertEquals(gameBoard.isGameStarted(), false);
  }
  
  /**
   * Testing move after both players have joined.
   */
  @Test
  @Order(7)
  public void moveAfterBothJoinedTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.isGameStarted(), true);
  }
  
  /**
   * Test P2 making the first move before P1 after the game starts. This is not a valid move.
   */
  @Test
  @Order(8)
  public void moveP2BeforeP1Test() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Please wait for your turn!");
  }
  
  /**
   * Test same player moving twice in a row.
   */
  @Test
  @Order(9)
  public void moveTwiceTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsonMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsonMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Please wait for your turn!");
  }
  
  /**
   * Test for P1 winning the game horizontally. 
   */
  @Test
  @Order(10)
  public void winRowP1Test() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getWinner(), 1);
  }
  
  /**
   * Test for P2 winning the game vertically. 
   */
  @Test
  @Order(11)
  public void winColP2Test() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=2").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getWinner(), 2);
  }
  
  /**
   * Test for P1 winning the game on right diagonal. 
   */
  @Test
  @Order(12)
  public void winRightDiagonalP1Test() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=0&y=2").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getWinner(), 1);
  }
  
  /**
   * Test for draw with checking if right diagonal win case is not satisfied.
   */
  @Test
  @Order(13)
  public void drawRightDiagonalTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=2").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.isDraw(), true);
    assertEquals(gameBoard.getWinner(), 0);
  }
  
  /**
   * Test for P1 winning the game on left diagonal. 
   */
  @Test
  @Order(14)
  public void winLeftDiagonalP1Test() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=1").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=2&y=2").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getWinner(), 1);
  }
  
  /**
   * Test for draw. 
   */
  @Test
  @Order(15)
  public void drawTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=2").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), true);
    assertEquals(msg.getCode(), 100);
    assertEquals(msg.getMessage(), "");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.isDraw(), true);
    assertEquals(gameBoard.getWinner(), 0);
  }
  
  /**
   * Test for placing a move in the same slot. 
   */
  @Test
  @Order(16)
  public void moveSameSlotTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Please select an open slot!");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.getTurn(), 2);
    assertEquals(gameBoard.getBoardState()[0][0], 'X');
  }
  
  /**
   * Test for moving after game over (win). 
   */
  @Test
  @Order(17)
  public void moveAfterWinTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=O").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/2").body("x=2&y=2").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsonMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsonMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Game over!");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getWinner(), 1);
  }
  
  /**
   * Test for moving after game over (draw). 
   */
  @Test
  @Order(18)
  public void moveAfterDrawTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=O").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/2").body("x=1&y=2").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsonMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsonMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Game over!");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.isDraw(), true);
    assertEquals(gameBoard.getWinner(), 0);
  }
  
  /**
   * Test for joining an ongoing game. 
   */
  @Test
  @Order(19)
  public void joinOngoingGameTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    HttpResponse<?> responseMove = Unirest.get("http://localhost:8080/joingame").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    assertEquals(responseBodyMove, "Another user is playing the game.");
  }
  
  /**
   * Test for move with an invalid player id.
   */
  @Test
  @Order(20)
  public void invalidPlayerIdTest() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/3").body("x=0&y=0").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid player id!");
  }
  
  /**
   * Test for move with an invalid x coordinate (-1).
   */
  @Test
  @Order(21)
  public void invalidCoordinateTest1() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=-1&y=0").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid move!");
  }
  
  /**
   * Test for move with an invalid x coordinate (3).
   */
  @Test
  @Order(22)
  public void invalidCoordinateTest2() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=3&y=0").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid move!");
  }
  
  /**
   * Test for move with an invalid y coordinate (-1).
   */
  @Test
  @Order(23)
  public void invalidCoordinateTest3() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=0&y=-1").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid move!");
  }
  
  /**
   * Test for move with an invalid y coordinate (3).
   */
  @Test
  @Order(24)
  public void invalidCoordinateTest4() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=0&y=3").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Not a valid move!");
  }
  
  
  
  
  
  
  
  // Robustness Tests
  
  /**
   * Check if database table is clean after a new game starts.
   */
  @Test
  @Order(25)
  public void checkCleanDB() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    
    PlayGame.stop();
    PlayGame.main(new String[0]);
    
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    
    Gson gson = new Gson();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonObject = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    
    assertArrayEquals(new char[3][3], gameBoard.getBoardState());
  }
  
  /**
   * Check if game board is preserved after application crash after a move.
   */
  @Test
  @Order(26)
  public void dataAfterCrashMove() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    
    PlayGame.stop();
    PlayGame.main(new String[0]);
    
    Gson gson = new Gson();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonObject = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    
    assertEquals('X', gameBoard.getBoardState()[0][0]);
  }
  
  /**
   * Application crash after draw. Check if game is still a draw.
   */
  @Test
  @Order(27)
  public void dataAfterCrashDraw() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    
    Gson gson = new Gson();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonObject = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    
    assertEquals(true, gameBoard.isDraw());
    
    PlayGame.stop();
    PlayGame.main(new String[0]);
    
    responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    responseBodyBoard = (String) responseBoard.getBody();
    jsonObject = new JSONObject(responseBodyBoard);
    gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);

    assertEquals(true, gameBoard.isDraw());
  }
  
  /**
   * Application crash after win and not new game started. Check if game state is preserved.
   */
  @Test
  @Order(28)
  public void dataAfterCrashWin() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    
    Gson gson = new Gson();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getWinner(), 1);
    
    PlayGame.stop();
    PlayGame.main(new String[0]);
    
    responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    responseBodyBoard = (String) responseBoard.getBody();
    jsonBoard = new JSONObject(responseBodyBoard);
    gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class);

    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getWinner(), 1);
  }
  
  /**
   * Check if P1 is preserved if P1 starts the game then crashes.
   */
  @Test
  @Order(29)
  public void dataAfterCrashStartP1() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    
    PlayGame.stop();
    PlayGame.main(new String[0]);
    
    Gson gson = new Gson();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getP1().getId(), 1);
    assertEquals(gameBoard.getP1().getType(), 'X');
  }
  
  /**
   * Check if P2 is preserved after P2 joins and then game crashes.
   */
  @Test
  @Order(30)
  public void dataAfterCrashP2Join() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    
    PlayGame.stop();
    PlayGame.main(new String[0]);
    
    Gson gson = new Gson();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    
    assertEquals(gameBoard.isGameStarted(), true);
    assertEquals(gameBoard.getP2().getId(), 2);
    assertEquals(gameBoard.getP2().getType(), 'O');
  }
  
  /**
   * If new game created then crashes, then a new game should be rebooted. 
   */
  @Test
  @Order(31)
  public void dataAfterCrashNewGame() {
    PlayGame.stop();
    PlayGame.main(new String[0]);
    
    Gson gson = new Gson();
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    
    assertEquals(gameBoard.isGameStarted(), false);
    assertEquals(gameBoard.getP1().getId(), 0);
    assertEquals(gameBoard.getP1().getType(), 'N');
    assertEquals(gameBoard.getP2().getId(), 0);
    assertEquals(gameBoard.getP2().getType(), 'N');
    assertEquals(gameBoard.getTurn(), -1);
    assertEquals(gameBoard.getWinner(), 0);
    assertEquals(gameBoard.isDraw(), false);
    assertArrayEquals(gameBoard.getBoardState(), new char[3][3]);
  }
  
  /**
   * If crash after invalid move, reboot to last valid move.
   */
  @Test
  @Order(32)
  public void dataAfterCrashInvalidMove() {
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    HttpResponse<?> responseMove = Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    
    String responseBodyMove = (String) responseMove.getBody();
    JSONObject jsonMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsonMove.toString(), Message.class);
    assertEquals(msg.isMoveValidity(), false);
    assertEquals(msg.getCode(), 200);
    assertEquals(msg.getMessage(), "Please wait for your turn!");
    
    HttpResponse<?> responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    String responseBodyBoard = (String) responseBoard.getBody();
    JSONObject jsonBoard = new JSONObject(responseBodyBoard);
    GameBoard gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class); 
    
    assertEquals(gameBoard.getTurn(), 2);
    assertEquals(gameBoard.getBoardState()[0][0], 'X');
    assertEquals(gameBoard.getBoardState()[0][1], '\u0000');
    
    PlayGame.stop();
    PlayGame.main(new String[0]);
    
    responseBoard = Unirest.get("http://localhost:8080/gameBoard").asString();
    responseBodyBoard = (String) responseBoard.getBody();
    jsonBoard = new JSONObject(responseBodyBoard);
    gameBoard = gson.fromJson(jsonBoard.toString(), GameBoard.class);

    assertEquals(gameBoard.getTurn(), 2);
    assertEquals(gameBoard.getBoardState()[0][0], 'X');
    assertEquals(gameBoard.getBoardState()[0][1], '\u0000');
  }

  /**
   * This method runs at the end to stop the server.
   */
  @AfterAll
  public static void close() {
    // Stop Server
    PlayGame.stop();
    System.out.println("Stopping the server.");
  }
  
}
