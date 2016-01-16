package liardice;

import java.io.IOException;
import netgame.common.ForwardedMessage;
import netgame.common.Hub;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class GameHub extends Hub{

	private static int PORT = 42857;
	private static int NUM_OF_PLAYERS = 2;

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
	private String[] nicknames;

	public GameHub(int port, int numberOfPlayers) throws IOException {
		super(port);
		this.NUM_OF_PLAYERS = numberOfPlayers;
		nicknames = new String[numberOfPlayers];

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

	protected void messageReceived(int playerID, Object message) {

		// players send their nicknames
		if (message instanceof String) {
			nicknames[ playerID - 1 ] = (String)message;
			topOfNicknames++;
			System.out.println("Player #" + Integer.toString(playerID) + " says his nickname is " + (String)message);

			// It's time to send nickname to all players
			if (topOfNicknames == NUM_OF_PLAYERS) {
				sendToAll(new ForwardedMessage(0, nicknames));
				System.out.println("Send nicknames to all players");
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