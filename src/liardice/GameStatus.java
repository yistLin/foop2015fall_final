package liardice;

import java.io.Serializable;

public class GameStatus implements Serializable {
    static final int ROUND_START = 0;
    static final int DO_BID = 1;
    static final int DO_CATCH = 2;
    static final int DO_CONTINUE = 3;
    int status;
    int round;
    int numberOfDice;
    int valueOfDice;
    int currentPlayer;
    GameStatus(int status) {
        this.status = status;
    };
    //GameStatus for ROUND_START and DO_BID
    GameStatus(int status, int msg) {
        this.status = status;
        if(status == ROUND_START)
            this.round = msg;
        else if(status == DO_BID)
            this.currentPlayer = msg;
    };
    //GameStatus for DO_CATCH
    GameStatus(int status, int numberOfDice, int valueOfDice, int currentPlayer) {
        this.status = status;
        this.numberOfDice = numberOfDice;
        this.valueOfDice = valueOfDice;
        this.currentPlayer = currentPlayer;
    }
}