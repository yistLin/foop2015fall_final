package liardice;

import java.io.IOException;
import netgame.common.ForwardedMessage;
import netgame.common.Hub;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class GameHub extends Hub{

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
            System.out.println("usage: java ... liardice.GameHub [port] [numberOfPlayers]");
            return;
        }

        try {
            new GameHub(PORT, NUM_OF_PLAYERS);
        }
        catch (IOException e) {
            System.out.println("Can't create listening socket.  Shutting down.");
        }
    }

    private int topOfNicknames = 0;
    private int topOfReadyPlayers = 0;
    private int topOfCatchPlyaers = 0;
    private String[] nicknames;
    private int[] diceTable;
    private int rounds = 1;
    private int currentPlayer;
    private int currentStatus;
    private int lastValueOfDice, lastNumberOfDice, lastPlayerID;

    public GameHub(int port, int numberOfPlayers) throws IOException {
        super(port);
        this.NUM_OF_PLAYERS = numberOfPlayers;
        nicknames = new String[numberOfPlayers];
        diceTable = new int[7];

        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal signo) {
                System.out.println("GameHub is shutting down.");
                shutDownHub();
            }
        });
    }

    protected void playerConnected(int playerID) {
        System.out.println("Player " + Integer.toString(playerID) + " connected.");
    }

    protected void playerDisconnected(int playerID) {
        System.out.println("Player " + Integer.toString(playerID) + " disconnected.");
        System.out.println("GameHub is shutting down.");
        shutDownHub();
    }

    //deal dices to all players
    private void dealDice() {
        Dice[] playerDice;
        Dice tmpDice;
        for (int element : diceTable)
            element = 0;
        for (int playerID = 1; playerID <= NUM_OF_PLAYERS; playerID++) {
            playerDice = new Dice[NUM_OF_DICE];
            for (int i = 0; i < NUM_OF_DICE; i++) {
                tmpDice = new Dice();
                playerDice[i] = tmpDice;
                diceTable[tmpDice.value]++;
            }
            sendToOne(playerID, new ForwardedMessage(0, playerDice));
        }
    }

    private void doSleep(double sec) {
        int msec = (int)(sec * 1000);
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {}
    }

    protected void messageReceived(int playerID, Object message) {

        // players send their nicknames
        if (message instanceof String) {
            String nickname = (String)message;
            nicknames[ playerID - 1 ] = nickname;
            topOfNicknames++;
            System.out.println("Player #" + Integer.toString(playerID) + " says his nickname is " + nickname);
            sendToAll(new ForwardedMessage(0, nickname));

            // It's time to send nickname and deal dices to all players
            if (topOfNicknames == NUM_OF_PLAYERS) {
                doSleep(0.5);

                sendToAll(new ForwardedMessage(0, nicknames));
                System.out.println("[Status] Send nicknames to all players");
                doSleep(0.5);

                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.ROUND_START, rounds)));
                System.out.println("[Status] Send GameStatus.ROUND_START, round = " + Integer.toString(rounds) + ", to all players");
                doSleep(0.5);

                dealDice();
                System.out.println("[Status] Deal dices to all players");
                doSleep(0.5);
            }
        }

        // get ReadyMessage from all players
        else if (message instanceof ReadyMessage) {
            topOfReadyPlayers++;

            if (topOfReadyPlayers == NUM_OF_PLAYERS) {
                currentPlayer = 1;
                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.DO_BID, currentPlayer)));
                currentStatus = BID_STATUS;
                topOfReadyPlayers = 0;
            }
        }

        // receive BidMessage from currentPlayer
        else if (message instanceof BidMessage && playerID == currentPlayer && currentStatus == BID_STATUS) {
            int n = ((BidMessage)message).numberOfDice;
            int v = ((BidMessage)message).valueOfDice;

            // TODO: check the correctness of valueOfDice and numberOfDice

            sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.DO_CATCH, n, v, currentPlayer)));
            currentPlayer++;
            currentStatus = CATCH_STATUS;
            topOfCatchPlyaers = 0;
            lastNumberOfDice = n;
            lastValueOfDice = v;
            lastPlayerID = playerID;
            System.out.println("[Status] Player #" + Integer.toString(playerID) +
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
                if (diceTable[lastValueOfDice] < lastNumberOfDice)
                    sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.ROUND_END, lastPlayerID, diceTable.clone())));
                else
                    sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.ROUND_END, playerID, diceTable.clone())));

                doSleep(1.5);
                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.DO_CONTINUE)));
                rounds++;
                doSleep(0.5);

                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.ROUND_START, rounds)));
                System.out.println("[Status] Send GameStatus.ROUND_START, round = " + Integer.toString(rounds) + ", to all players");
                doSleep(0.5);

                dealDice();
                System.out.println("[Status] Deal dices to all players");
                doSleep(0.5);
            }
            else {
                topOfCatchPlyaers++;
                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.NO_CATCH, playerID)));
                System.out.println("[Status] Player #" + Integer.toString(playerID) + " don't catch");
                if (topOfCatchPlyaers == (NUM_OF_PLAYERS-1)) {
                    currentStatus = BID_STATUS;
                    sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.DO_BID, currentPlayer)));
                }
            }
        }

        // redirect ChatMessage to all players
        else if (message instanceof ChatMessage) {
            sendToAll(new ForwardedMessage(playerID, (ChatMessage)message));
        }
    }
}