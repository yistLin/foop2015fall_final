package liardice;

import java.io.IOException;
import java.util.*;
import netgame.common.*;
import liardice.message.*;

public class Robot extends Client {
    private ChatRobot chatRobot;
    private String robotTalk;
    private String myName;
    private String[] playerList;
    private int numOfPlayers = 9;
    private Dice[] dice;
    private int maxDice;
    private int[] diceTable;
    private int lastNumber;
    private int lastValue;
    private boolean hasBidOne;

    public Robot(String hubHostName, int hubPort, String filename) throws IOException {
        super(hubHostName, hubPort);
        chatRobot = new ChatRobot(filename);
        this.myName = chatRobot.NICKNAME;
        send(myName);
        diceTable = new int[7];
        new Thread() {
            public void run() {
                while (true) {
                    doSleep(Math.random() * ((double)numOfPlayers * 4.0 + 3.0));
                    String randomTalk = chatRobot.talk(ChatRobot.RANDOM_TALK);
                    if(randomTalk != null) {
                        send(new ChatMessage(myName, randomTalk));
                        doSleep((double)numOfPlayers * 2);
                    }
                }
            }
        }.start();
    }

    protected void messageReceived(final Object forwardedMessage) {
        if (forwardedMessage instanceof ForwardedMessage) {
            ForwardedMessage fm = (ForwardedMessage)forwardedMessage;
            if (fm.message instanceof ChatMessage) {
                if (fm.senderID != getID()) {
                    robotTalk = chatRobot.talk(ChatRobot.OTHER_TALK, ((ChatMessage)fm.message).message);
                    if(robotTalk != null)
                        send(new ChatMessage(myName, robotTalk));
                }
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
                robotTalk = chatRobot.talk(ChatRobot.GET_DICE);
                if(robotTalk!= null)
                    send(new ChatMessage(myName, robotTalk));
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
            robotTalk = chatRobot.talk(ChatRobot.ROUND_START);
            if(robotTalk != null)
                send(new ChatMessage(myName, robotTalk));
        } else if (gs.status == GameStatus.DO_CATCH) {
            doSleep(Math.random() * 1.0 + 0.5);
            lastNumber = gs.numberOfDice;
            lastValue = gs.valueOfDice;
            if (lastValue == 1)
                hasBidOne = true;
            if (gs.currentPlayer != getID()) {
                int random = (int)(Math.floor(Math.random() * 3) - 1);
                int myNum = diceTable[lastValue] + numOfPlayers - 1 +
                            ((hasBidOne) ? 0 : diceTable[1] + numOfPlayers - 1);
                if (myNum + 2 < lastNumber) {
                	doSleep(Math.random() * 1.0 + 0.5);
                	robotTalk = chatRobot.talk(ChatRobot.DO_CATCH, "yes");
                    if(robotTalk != null)
                        send(new ChatMessage(myName, robotTalk));
                    send(new CatchMessage(true));
                }
                else if (myNum + random < lastNumber) {
                	doSleep(Math.random() * 2.5 + 1.5);
                    robotTalk = chatRobot.talk(ChatRobot.DO_CATCH, "yes");
                    if(robotTalk != null)
                        send(new ChatMessage(myName, robotTalk));
                    send(new CatchMessage(true));
                }
                else if (lastNumber < myNum - 2) {
                	doSleep(Math.random() * 1.0 + 0.5);
                    robotTalk = chatRobot.talk(ChatRobot.DO_CATCH, "no");
                    if(robotTalk != null)
                        send(new ChatMessage(myName, robotTalk));
                    send(new CatchMessage(false));
                }
                else {
                	doSleep(Math.random() * 2.0 + 1.0);
                	robotTalk = chatRobot.talk(ChatRobot.DO_CATCH, "no");
                    if(robotTalk != null)
                        send(new ChatMessage(myName, robotTalk));
                    send(new CatchMessage(false));
                }
            }
        } else if (gs.status == GameStatus.DO_BID) {
            doSleep(Math.random() * 1.5 + 1.5);
            if (gs.currentPlayer == getID()) {
                robotTalk = chatRobot.talk(ChatRobot.DO_BID, "me");
                if(robotTalk != null)
                    send(new ChatMessage(myName, robotTalk));
                
                int myNum = diceTable[maxDice] + numOfPlayers - 1 +
                            ((hasBidOne || maxDice == 1) ? 0 : (diceTable[1] + numOfPlayers - 1));
                int limit = (lastNumber < (myNum-1)) ? 3 : 0;
                int range = (int)(Math.random() * limit - 1);

                if (lastValue == 0)
                    send(new BidMessage(myNum + range, maxDice));
                else if (maxDice > lastValue)
                    send(new BidMessage(((lastNumber>myNum) ? lastNumber:myNum) + range, maxDice));
                else
                    send(new BidMessage(((lastNumber>myNum) ? lastNumber:myNum) + range + 2, maxDice));
            } else {
                robotTalk = chatRobot.talk(ChatRobot.DO_BID, "other");
                if(robotTalk != null)
                    send(new ChatMessage(myName, robotTalk));
            }
        } else if (gs.status == GameStatus.NO_CATCH) {
            if(gs.currentPlayer != getID()) {
                robotTalk = chatRobot.talk(ChatRobot.NO_CATCH);
                if(robotTalk != null)
                    send(new ChatMessage(myName, robotTalk));
            }
        } else if (gs.status == GameStatus.YES_CATCH) {
            if(gs.currentPlayer != getID()) {
                robotTalk = chatRobot.talk(ChatRobot.YES_CATCH);
                if(robotTalk != null)
                    send(new ChatMessage(myName, robotTalk));
            }
        } else if (gs.status == GameStatus.ROUND_END) {
            if (gs.currentPlayer == getID()) {
                robotTalk = chatRobot.talk(ChatRobot.ROUND_END, true);
                if(robotTalk != null)
                    send(new ChatMessage(myName, robotTalk));
            } else {
                robotTalk = chatRobot.talk(ChatRobot.ROUND_END, false);
                if(robotTalk != null)
                    send(new ChatMessage(myName, robotTalk));
            }
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
