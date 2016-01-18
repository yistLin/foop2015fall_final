package liardice;

import java.io.IOException;
import java.util.*;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import netgame.common.*;
import liardice.message.*;

public class Robot extends Client {
    private String myName;
    private int numOfPlayers;
    private Dice[] dice;
    private int maxDice;
    private int[] diceTable;
    private int lastNumber;
    private int lastValue;
    private boolean hasBidOne;
    
    public static void main(String[] args) {
        String hubHostName, nickName;
        int hubPort;
    	if (args.length > 2) {
    		hubHostName = args[0];
            hubPort = Integer.parseInt(args[1]);
    		nickName = args[2];
    	}
        else {
        	return;
        }
        try {
            new Robot(hubHostName, hubPort, nickName);
        } catch (IOException e) {
            System.out.println("Cannot new GameHub");
        }   
    }

    public Robot(String hubHostName, int hubPort, String nickName) throws IOException {
        super(hubHostName, hubPort);
        this.myName = nickName;
        send(myName);
        diceTable = new int[7];
        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal signo) {
                disconnect();
                doSleep(1);
                System.exit(0);
            }
        });
    }

    protected void messageReceived(final Object forwardedMessage) {
        if (forwardedMessage instanceof ForwardedMessage) {
        ForwardedMessage fm = (ForwardedMessage)forwardedMessage;
            if (fm.message instanceof ChatMessage) {
                ChatMessage cm = (ChatMessage)fm.message;
                if (cm.id.compareTo(myName) != 0 && Math.random() > 0.8)
                    send(new ChatMessage(myName, cm.message));
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
                    if (diceTable[i] > diceTable[0]) {
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
            doSleep(Math.random() * 2 + 1);
            lastNumber = gs.numberOfDice;
            lastValue = gs.valueOfDice;
            if (lastValue == 1)
                hasBidOne = true;
            if (gs.currentPlayer != getID()) {
                int random = (int)(Math.floor(Math.random() * 4) - 1);
                int myNum = diceTable[lastValue] + numOfPlayers - 1 +
                            ((hasBidOne) ? 0:diceTable[1]);
                if (myNum + random < lastNumber)
                    send(new CatchMessage(true));
                else
                    send(new CatchMessage(false));
            }
        } else if (gs.status == GameStatus.DO_BID) {
            doSleep(Math.random() * 2 + 3);
            if (gs.currentPlayer == getID()) {
                int random = (int)(Math.floor(Math.random() * 3) - 2);
                int myNum = diceTable[maxDice] + numOfPlayers - 1 +
                            ((hasBidOne || maxDice == 1) ? 0:diceTable[1]);
                if (lastValue == 0)
                    send(new BidMessage(myNum + random, maxDice));
                else if (maxDice > lastValue)
                    send(new BidMessage(lastNumber, maxDice));
                else
                    send(new BidMessage(lastNumber+1, maxDice));
            }
        } else if (gs.status == GameStatus.NO_CATCH) {
            if (Math.random() > 0.8)
                send(new ChatMessage(myName, "Why not catch~ ?"));
        } else if (gs.status == GameStatus.YES_CATCH) {
            if (Math.random() > 0.7)
                send(new ChatMessage(myName, "Let's laugh at"+ gs.currentPlayer));
        } else if (gs.status == GameStatus.ROUND_END) {
            if (gs.currentPlayer == getID() && Math.random() > 0.5)
                send(new ChatMessage(myName, "NOOOOOOOO~~~~~"));
            else if (gs.currentPlayer != getID() && Math.random() > 0.5)
                send(new ChatMessage(myName, "My grandma is smarter than you!!!"));
            lastNumber = 0;
            lastValue = 0;
            hasBidOne = false;
            send(new ContinueMessage(true));
        }
    }
    
	protected void serverShutDown(String message) {
        doSleep(1.5);
        System.exit(0);
	}
    
    private void doSleep(double sec) {
        int msec = (int)(sec * 1000);
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {}
    }
}
