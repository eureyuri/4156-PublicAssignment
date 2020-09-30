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
    HttpResponse<?> response = Unirest.get("http://localhost:8080/newgame").asString();
    
    // Check if server is running.
    int restStatus = response.getStatus();
    assertEquals(restStatus, 200);
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
   * Test P2 moving before P1 after the game starts. This is not a valid move.
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
    JSONObject jsontMove = new JSONObject(responseBodyMove);
    Gson gson = new Gson();
    Message msg = gson.fromJson(jsontMove.toString(), Message.class);
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

  /**
   * This method runs only once after all the test cases have been executed.
   */
  @AfterAll
  public static void close() {
    // Stop Server
    PlayGame.stop();
    System.out.println("Stopping the server.");
  }
  
}
