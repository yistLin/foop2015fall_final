package liardice;

import java.io.IOException;
import java.util.*;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import netgame.common.*;
import liardice.message.*;

public class Robot extends Client {
    private String myName;
    private String[] playerList;
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
    	if (args.length > 1) {
    		hubHostName = args[0];
            hubPort = Integer.parseInt(args[1]);
    	} else
            return;
        try {
            new Robot(hubHostName, hubPort);
        } catch (IOException e) {
            System.out.println("Cannot new GameHub");
        }   
    }

    public Robot(String hubHostName, int hubPort) throws IOException {
        super(hubHostName, hubPort);
        ChatRobot chatrobot = new ChatRobot(getID());
        this.myName = chatrobot.NICKNAME;
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
                if (fm.id != getID() && Math.random() > 0.85)
                    send(new ChatMessage(myName, cm.message));
            } else if (fm.message instanceof String[]) {
                playerList = (String[])fm.message;
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
            hasBidOne = false;
            //TODO with chatrobot
        } else if (gs.status == GameStatus.DO_CATCH) {
            //TODO with chatrobot
            doSleep(Math.random() * 2);
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
            //TODO with chatrobot
            doSleep(Math.random() * 2 + 1);
            if (gs.currentPlayer == getID()) {
                int random = (int)(Math.floor(Math.random() * 3) - 1);
                int myNum = diceTable[maxDice] + numOfPlayers - 1 +
                            ((hasBidOne || maxDice == 1) ? 0:diceTable[1]);
                if (lastValue == 0)
                    send(new BidMessage(myNum+random, maxDice));
                else if (maxDice > lastValue)
                    send(new BidMessage(lastNumber, maxDice));
                else
                    send(new BidMessage(lastNumber+1, maxDice));
            }
        } else if (gs.status == GameStatus.NO_CATCH) {
            if(gs.currentPlayer != getID())
                //TODO with chatrobot
        } else if (gs.status == GameStatus.YES_CATCH) {
            if(gs.currentPlayer != getID())
                //TODO with chatrobot
        } else if (gs.status == GameStatus.ROUND_END) {
            if (gs.currentPlayer == getID())
                //TODO with chatrobot
            else
                //TODO with chatrobot
            send(new ContinueMessage(true));
        }
    }
    
	protected void serverShutDown(String message) {
        doSleep(1);
        System.exit(0);
	}
    
    private void doSleep(double sec) {
        int msec = (int)(sec * 1000);
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {}
    }
}
