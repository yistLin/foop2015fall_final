package liardice;

import java.io.IOException;
import netgame.common.ForwardedMessage;
import netgame.common.Hub;

public class GameHub extends Hub{

	private static int PORT = 42857;

	public static void main(String[] args) {

		if (args.length > 0) {
			PORT = Integer.parseInt(args[0]);
		}

		try {
			new GameHub(PORT);
		}
		catch (IOException e) {
			System.out.println("Can't create listening socket.  Shutting down.");
		}
	}

	public GameHub(int port) throws IOException {
		super(port);
	}

	protected void playerConnected(int playerID) {
		System.out.println("Someone " + Integer.toString(playerID) + " connected!");
	}

	protected void messageReceived(int playerID, Object message) {
		if (message.equals("query")) {
			// send a dice number to the player
			System.out.println("receive a DICE query from client");
			sendToOne(playerID, new ForwardedMessage(playerID,new Dice()));
		}
		else {
			sendToAll(new ForwardedMessage(playerID,message));
		}
	}
}