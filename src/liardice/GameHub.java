package liardice;

import java.io.IOException;
import java.io.PrintWriter;
import netgame.common.ForwardedMessage;
import netgame.common.Hub;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class GameHub extends Hub{

	private PrintWriter writer;
    private static int PORT = 42857;
    private static int NUM_OF_PLAYERS = 2;
    private final static int NUM_OF_DICE = 5;
    private final static int BID_STATUS = 1;
    private final static int CATCH_STATUS = 2;

    public static void main(String[] args) {

        if (args.length > 1) {
            PORT = Integer.parseInt(args[0]);
            NUM_OF_PLAYERS = Integer.parseInt(args[1]);
        }
        else {
            System.out.println("[Error] usage: java ... liardice.GameHub [port] [numberOfPlayers]");
            return;
        }

        try {
            new GameHub(PORT, NUM_OF_PLAYERS);
        }
        catch (IOException e) {
            System.out.println("[Error] Can't create listening socket.  Shutting down.");
        }
    }

    private int topOfNicknames, topOfReadyPlayers, topOfCatchPlyaers, topOfContinuePlayers;
    private String[] nicknames;
    private int[] diceTable;
    private int rounds = 1;
    private int currentPlayer, currentStatus;
    private int lastValueOfDice, lastNumberOfDice, lastPlayerID = 1;
    private boolean hasBidOne;

    public GameHub(int port, int numberOfPlayers) throws IOException {
        super(port);
        writer = new PrintWriter("liardice_gamehub.log", "UTF-8");
        this.NUM_OF_PLAYERS = numberOfPlayers;
        nicknames = new String[numberOfPlayers];
        diceTable = new int[7];

        write2log("[Status] GameHub start");

        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal signo) {
                write2log("[Status] GameHub is shutting down.");
                shutDownHub();
            }
        });
    }

    private void write2log(String str) {
    	writer.println(str);
    }

    protected void playerConnected(int playerID) {
        write2log("Player #" + Integer.toString(playerID) + " connected.");
    }

    protected void playerDisconnected(int playerID) {
        write2log("Player #" + Integer.toString(playerID) + " disconnected.");
        write2log("[Status] GameHub is shutting down.");
        shutDownHub();
    }

    public void shutDownHub() {
        super.shutDownHub();
        writer.println("[Status] shutdown");
        writer.close();
    }

    //deal dices to all players
    private void dealDice() {
        Dice[] playerDice;
        Dice tmpDice;
        for (int i = 0; i < diceTable.length; i++)
        	diceTable[i] = 0;

        for (int playerID = 1; playerID <= NUM_OF_PLAYERS; playerID++) {
            playerDice = new Dice[NUM_OF_DICE];
            for (int i = 0; i < NUM_OF_DICE; i++) {
                tmpDice = new Dice();
                playerDice[i] = tmpDice;
                diceTable[tmpDice.value]++;
            }
            sendToOne(playerID, new ForwardedMessage(0, playerDice));
        }
        write2log("[Status] deal dice to all players");
    }

    private void doSleep(double sec) {
        int msec = (int)(sec * 1000);
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {}
        write2log("[Status] sleep " + Double.toString(sec) + " sec");
    }

    protected void messageReceived(int playerID, Object message) {

        // players send their nicknames
        if (message instanceof String) {
            String nickname = (String)message;
            nicknames[ playerID - 1 ] = nickname;
            topOfNicknames++;
            write2log("Player #" + Integer.toString(playerID) + " says his nickname is " + nickname);
            sendToAll(new ForwardedMessage(0, nickname));

            // It's time to send nickname and deal dices to all players
            if (topOfNicknames == NUM_OF_PLAYERS) {
                doSleep(0.5);

                sendToAll(new ForwardedMessage(0, nicknames));
                write2log("[Status] Send nicknames to all players");
                doSleep(0.5);

                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.ROUND_START, rounds)));
                write2log("[Status] Round Start, round = " + Integer.toString(rounds));
                doSleep(0.5);

                dealDice();
                hasBidOne = false;
            }
        }

        // get ReadyMessage from all players
        else if (message instanceof ReadyMessage) {
            topOfReadyPlayers++;

            if (topOfReadyPlayers == NUM_OF_PLAYERS) {
                currentPlayer = lastPlayerID;
                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.DO_BID, currentPlayer)));
                currentStatus = BID_STATUS;
                topOfReadyPlayers = 0;
            }
        }

        // receive BidMessage from currentPlayer
        else if (message instanceof BidMessage && playerID == currentPlayer && currentStatus == BID_STATUS) {
            int n = ((BidMessage)message).numberOfDice;
            int v = ((BidMessage)message).valueOfDice;

            if (v == 1)
            	hasBidOne = true;

            sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.DO_CATCH, n, v, currentPlayer)));
            currentPlayer++;
            currentStatus = CATCH_STATUS;
            topOfCatchPlyaers = 0;
            lastNumberOfDice = n;
            lastValueOfDice = v;
            lastPlayerID = playerID;
            write2log("Player #" + Integer.toString(playerID) +
                " bid valueOfDice = " + Integer.toString(lastValueOfDice) +
                ", numberOfDice = " + Integer.toString(lastNumberOfDice));

            if (currentPlayer == (NUM_OF_PLAYERS+1) )
                currentPlayer = 1;
        }

        // receive CatchMessage from some players
        else if (message instanceof CatchMessage && currentStatus == CATCH_STATUS) {
            CatchMessage cm = (CatchMessage)message;
            
            if (cm.doCatch) {
                currentStatus = BID_STATUS;
                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.YES_CATCH, playerID)));
                write2log("Player #" + Integer.toString(playerID) + " catch");
                doSleep(0.3);

                int trueNumberOfDice = diceTable[lastValueOfDice];
                if (!hasBidOne)
                	trueNumberOfDice += diceTable[1];

                if (trueNumberOfDice < lastNumberOfDice) {
                    sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.ROUND_END, lastPlayerID, diceTable.clone())));
                	write2log("Player #" + Integer.toString(lastPlayerID) + " lose");
                }
                else {
                    sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.ROUND_END, playerID, diceTable.clone())));
                	write2log("Player #" + Integer.toString(playerID) + " lose");
                }

                lastPlayerID++;
                if (lastPlayerID > NUM_OF_PLAYERS)
                	lastPlayerID = 1;
                rounds++;
            }
            else {
                topOfCatchPlyaers++;
                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.NO_CATCH, playerID)));
                write2log("Player #" + Integer.toString(playerID) + " didn't catch");
                if (topOfCatchPlyaers == (NUM_OF_PLAYERS-1)) {
                    currentStatus = BID_STATUS;
                    sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.DO_BID, currentPlayer)));
                }
            }
        }

        else if (message instanceof ContinueMessage) {
        	if (!((ContinueMessage)message).doContinue) {
        		write2log("Player #" + Integer.toString(playerID) + " didn't want to continue");
        		shutDownHub();
        	}

        	topOfContinuePlayers++;
        	if (topOfContinuePlayers == NUM_OF_PLAYERS) {
                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.ROUND_START, rounds)));
                write2log("[Status] Round Start, round = " + Integer.toString(rounds));
                doSleep(0.5);
                dealDice();
                topOfContinuePlayers = 0;
                hasBidOne = false;
        	}
        }

        // redirect ChatMessage to all players
        else if (message instanceof ChatMessage) {
            sendToAll(new ForwardedMessage(playerID, (ChatMessage)message));
        }
    }
}