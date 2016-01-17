package liardice;

import java.io.IOException;
import netgame.common.ForwardedMessage;
import netgame.common.Hub;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class GameHub extends Hub{

    private static int PORT = 42857;
    private static int NUM_OF_PLAYERS = 2;
    private static int NUM_OF_DICES = 5;
    private Dice[][] dicesTable;
    
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
    private String[] nicknames;
    private int rounds = 0;

    public GameHub(int port, int numberOfPlayers) throws IOException {
        super(port);
        this.NUM_OF_PLAYERS = numberOfPlayers;
        nicknames = new String[numberOfPlayers];
        dicesTable = new Dice[numberOfPlayers][NUM_OF_DICES];
        
        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal signo) {
                System.out.println("GameHub is shutting down.");
                shutDownHub();
            }
        });
    }

    protected void playerConnected(int playerID) {
        System.out.println("Player " + Integer.toString(playerID) + " connected!");
    }

    //deal dices to all players
    private void dealDice() {
        for(int playerID = 1; playerID <= NUM_OF_PLAYERS; playerID++) {
            for(int i = 0; i < NUM_OF_DICES; i++) {
                dicesTable[playerID - 1][i] = new Dice();
            }
            sendToOne(playerID, new ForwardedMessage(0, dicesTable[playerID - 1]));
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
                
                sendToAll(new ForwardedMessage(0, nicknames));
                System.out.println("[Status] Send nicknames to all players");
                doSleep(0.5);

                sendToAll(new ForwardedMessage(0, new GameStatus(GameStatus.ROUND_START, rounds)));
                System.out.println("[Status] Send GameStatus.ROUND_START to all players");
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
                // TODO:
            }
        }

        // redirect ChatMessage to all players
        else if (message instanceof ChatMessage) {
            sendToAll(new ForwardedMessage(playerID, (ChatMessage)message));
        }

        // something need to discuss
        else {
            // TODO:
        }
    }
}