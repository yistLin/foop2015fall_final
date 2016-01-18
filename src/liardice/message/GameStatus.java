package liardice.message;

import java.io.Serializable;

public class GameStatus implements Serializable {
    public static final int ROUND_START = 0;
    public static final int DO_BID = 1;
    public static final int DO_CATCH = 2;
    public static final int NO_CATCH = 3;
    public static final int YES_CATCH = 4;
    public static final int ROUND_END = 5;
    public int status;
    public int round;
    public int numberOfDice;
    public int valueOfDice;
    public int currentPlayer;
    public int[] diceTable;
    
    public GameStatus(int status) {
        this.status = status;
    };
    //GameStatus for ROUND_START & DO_BID & NO_CATCH & YES_CATCH
    public GameStatus(int status, int msg) {
        this.status = status;
        if(status == ROUND_START)
            this.round = msg;
        else
            this.currentPlayer = msg;
    }
    //GameStatus for DO_CATCH
    public GameStatus(int status, int numberOfDice, int valueOfDice, int currentPlayer) {
        this.status = status;
        this.numberOfDice = numberOfDice;
        this.valueOfDice = valueOfDice;
        this.currentPlayer = currentPlayer;
    }
    //GameStatus for ROUND_END
    public GameStatus(int status, int currentPlayer, int[] diceTable) {
        this.status = status;
        this.currentPlayer = currentPlayer;
        this.diceTable = diceTable;
    }
}