package models;

import com.google.gson.Gson;

public class GameBoard {

  private Player p1;
  private Player p2;
  private boolean gameStarted;
  private int turn;
  private char[][] boardState;
  private int winner;
  private boolean isDraw;

  /**
   * Representation of the game board. This keeps track of the players, if the game has started, 
   * which player's turn it is, the board state, the winner, and if the game was a draw.
   * The constructor will create a new game board. 
   * @param p1Type Player 1 type: X or O
   */
  public GameBoard(char p1Type) {
    this.setP1(new Player(p1Type, 1));
    this.setP2(null);
    this.setGameStarted(false);
    this.setTurn(1);
    this.setBoardState(new char[3][3]);
    this.setWinner(0);
    this.setDraw(false);
  }

  //  Returns Player 1
  public Player getP1() {
    return p1;
  }

  //  Sets Player 1
  public void setP1(Player p1) {
    this.p1 = p1;
  }
  
  //  Returns Player 2
  public Player getP2() {
    return p2;
  }

  //  Sets Player 2
  public void setP2(Player p2) {
    this.p2 = p2;
  }

  //  Returns if the game has started. True if game started. False if game has not started.
  public boolean isGameStarted() {
    return gameStarted;
  }
  
  //  Sets if game has started or not.
  public void setGameStarted(boolean gameStarted) {
    this.gameStarted = gameStarted;
  }
  
  //  Returns the current turn 
  public int getTurn() {
    return turn;
  }
  
  //  Sets the turn (who is currently playing)
  public void setTurn(int turn) {
    this.turn = turn;
  }
  
  //  Returns board state
  public char[][] getBoardState() {
    return boardState;
  }
  
  //  Sets the board state
  public void setBoardState(char[][] boardState) {
    this.boardState = boardState;
  }
  
  //  Returns the winner
  public int getWinner() {
    return winner;
  }
  
  //  Sets the winner of the game
  public void setWinner(int winner) {
    this.winner = winner;
  }
  
  //  Returns if the game is a draw or not
  public boolean isDraw() {
    return isDraw;
  }
  
  //  Sets to indicate if a game is a draw or not
  public void setDraw(boolean isDraw) {
    this.isDraw = isDraw;
  }
  
  /**
   * Join a game in the game board. Instantiates a new player 2 with the appropriate type that is 
   * opposite from player 1. Then start the game.
   */
  public void joinGame() {
    this.setP2(new Player(this.determineP2Type(), 2));
    this.setGameStarted(true);
  }
  
  /**
   * Logic for determining player 2 type, which is opposite of that of player 1's.
   * @return player 2 type X or O
   */
  private char determineP2Type() {
    char p1Type = this.getP1().getType();
    if (p1Type == 'X') {
      return 'O';
    } else {
      return 'X';
    }
  }
  
  /**
   * Makes a move on the board for a player if the move is valid.
   * Also determines if there is a winner or a draw in the game. 
   * @param playerId signifies who is making the move
   * @param x x coordinate of the move
   * @param y y coordinate of the move
   * @return a Message to alert the UI if the move was successful or not. 
   *            If not, display a custom message to suggest some error. 
   */
  public Message move(int playerId, int x, int y) {
    // Check to see who the current player is by comparing the playerId and p1's id. 
    // If they are equal, then the current player is p1, if not p2
    Player currentPlayer = p1.getId() == playerId ? p1 : p2;
    Move move = new Move(currentPlayer, x, y);
    try {
      if (playerId != this.getTurn()) {
        // Player cannot make multiple moves in their turn
        throw new Exception("Please wait for your turn!");
      } else if (!this.isOpenSlot(move)) {
        // Player must choose an open slot 
        throw new Exception("Please select an open slot!");
      } else if (this.isDraw() || this.winner != 0) {
        throw new Exception("Game over!");
      }
      
      // The move is valid so assign X or O in the specified position
      char type = currentPlayer.getType();
      this.boardState[move.getMoveX()][move.getMoveY()] = type;
      
      // Check for win / draw
      if (this.checkWinner(currentPlayer)) {
        return new Message(true, 100, "");
      } else if (this.checkDraw()) {
        return new Message(true, 100, "");
      }
      
      // Continue playing
      int nextTurn = this.getTurn() == 1 ? 2 : 1;
      this.setTurn(nextTurn);
      return new Message(true, 100, "");
    } catch (Exception e) {
      return new Message(false, 200, e.getMessage());
    }
  }
  
  // Checks if specified position is open or not
  private boolean isOpenSlot(Move move) {
    return this.getBoardState()[move.getMoveX()][move.getMoveY()] == '\u0000';
  }
  
  /**
   * Checks for winner by checking the rows, columns, and diagonals.
   * If there is a winner, set the winner and set gameStarted to false. 
   * @param currentPlayer determine the win for this player
   * @return if the current player has won
   */
  private boolean checkWinner(Player currentPlayer) {
    int currentPlayerId = currentPlayer.getId();
    
    for (int i = 0; i < boardState.length; i++) {
      // Check row and if it is filled
      if (boardState[i][0] == boardState[i][1] && boardState[i][1] == boardState[i][2] 
          && boardState[i][0] != '\u0000' && boardState[i][1] != '\u0000' 
          && boardState[i][2] != '\u0000') {
        this.setWinner(currentPlayerId);
        this.setGameStarted(false);
        return true;
      }
      
      // Check column and if it is filled
      if (boardState[0][i] == boardState[1][i] && boardState[1][i] == boardState[2][i]
          && boardState[0][i] != '\u0000' && boardState[1][i] != '\u0000' 
          && boardState[2][i] != '\u0000') {
        this.setWinner(currentPlayerId);
        this.setGameStarted(false);
        return true;
      }
    }
    
    // Check left diagonal and if it is filled
    if (boardState[0][0] == boardState[1][1] && boardState[1][1] == boardState[2][2]
        && boardState[0][0] != '\u0000' && boardState[1][1] != '\u0000' 
        && boardState[2][2] != '\u0000') {
      this.setWinner(currentPlayerId);
      this.setGameStarted(false);
      return true;
    }
    
    // Check right diagonal and if it is filled
    if (boardState[0][2] == boardState[1][1] && boardState[1][1] == boardState[2][0]
        && boardState[0][2] != '\u0000' && boardState[1][1] != '\u0000' 
        && boardState[2][0] != '\u0000') {
      this.setWinner(currentPlayerId);
      this.setGameStarted(false);
      return true;
    }
    
    return false;
  }
  
  /**
   * Check for draw. If everything has been filled, it is a draw
   * @return true if draw, otherwise false
   */
  private boolean checkDraw() {
    for (int i = 0; i < boardState.length; i++) {
      for (int j = 0; j < boardState[0].length; j++) {
        if (boardState[i][j] == '\u0000') {
          return false;
        }
      }
    }
    
    this.setDraw(true);
    this.setGameStarted(false);
    return true;
  }
  
  // Convert the game board to JSON format
  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
