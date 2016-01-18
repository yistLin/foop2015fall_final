package liardice;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import java.util.*;
import netgame.common.*;

public class Robot extends Client {
    private String myName;
    private int numOfPlayers;
    private Dice[] dice;
    private int maxDice;
    private int[] diceTable;
    private int lastNumber;
    private int lastValue;

    Robot(String hubHostName, int hubPort, String nickName) throws IOException {
        super(hubHostName, hubPort);
        this.myName = nickName;
        send(myName);
        diceTable = new int[7];
    }

    protected void messageReceived(final Object forwardedMessage) {
        if (forwardedMessage instanceof ForwardedMessage) {
        ForwardedMessage fm = (ForwardedMessage)forwardedMessage;
            if (fm.message instanceof ChatMessage) {
                //do nothing
                //ChatMessage cm = (ChatMessage)fm.message;
            } else if (fm.message instanceof String[]) {
                numOfPlayers = ((String[])(fm.message)).length;
            } else if (fm.message instanceof String) {
                //do nothing
            } else if (fm.message instanceof Dice[]) {
                for (int i = 0; i < diceTable.length; i++)
                    diceTable[i] = 0;
                maxDice = 0;
                dice = (Dice[])fm.message;
                for (Dice d : dice)
                    diceTable[d.value]++;
                for (int i = 1; i < diceTable.length; i++) {
                    if(diceTable[i] > diceTable[0]) {
                        diceTable[0] = diceTable[i];
                        maxDice = i;
                    }
                }
                send(new ReadyMessage());
            } else if (fm.message instanceof GameStatus) {
                GameStatus gs = (GameStatus)fm.message;
                handleGameStatus(gs);
            }
        }
    }

    private void handleGameStatus(GameStatus gs) {
        if (gs.status == GameStatus.ROUND_START) {
            lastNumber = 0;
            lastValue = 0;
        } else if (gs.status == GameStatus.DO_CATCH) {
            lastNumber = gs.numberOfDice;
            lastValue = gs.valueOfDice;
            if (gs.currentPlayer != getID()) {
                int randomValue = (int)(Math.floor(Math.random() * 3) - 1);
                if (diceTable[lastValue] + numOfPlayers + randomValue < lastNumber)
                    send(new CatchMessage(true));
                else
                    send(new CatchMessage(false));
            }
        } else if (gs.status == GameStatus.DO_BID) {
            if (gs.currentPlayer == getID()) {
                if(maxDice > lastValue)
                    send(new BidMessage(lastNumber, maxDice));
                else
                    send(new BidMessage(lastNumber+1, maxDice));
            }
        } else if (gs.status == GameStatus.NO_CATCH) {
            //do nothing
        } else if (gs.status == GameStatus.YES_CATCH) {
            //do nothing
        } else if (gs.status == GameStatus.ROUND_END) {
            send(new ContinueMessage(true));
        }
    }
    
	protected void serverShutDown(String message) {
        System.exit(0);
	}
}
