package liardice;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import java.util.*;
import netgame.common.*;

public class TestClient {
    
    private static String HOST = "localhost";
    private static int PORT = 42857;
    private static String PLAYER = "NoName";

    public static void main(String[] args) {

    	if (args.length > 2) {
    		PLAYER = args[0];
    		HOST = args[1];
    		PORT = Integer.parseInt(args[2]);
    	}
        else {
        	System.out.println("usage: java ... liardice.TestClient [your_name] [host] [port]");
        	return;
        }

        new TestClient(HOST);
    }

    private class ChatClient extends Client {

        ChatClient(String host) throws IOException {
            super(host, PORT);
        }

        protected void messageReceived(Object message) {
            if (message instanceof ForwardedMessage) {  // (no other message types are expected)
                ForwardedMessage fm = (ForwardedMessage)message;

                if (fm.message instanceof Dice) {
                	Dice dice = (Dice)fm.message;
                	pasteToConsole("#FromServer DICE = " + dice);
                }
                else {
                	ChatMessage cm = (ChatMessage)fm.message;
                	pasteToConsole("[" + cm.id + "] SAYS:  " + cm.message);
                }
            }
            else {
            	pasteToConsole("Received unexpected type");
            }
        }

        protected void connectionClosedByError(String message) {
            pasteToConsole("Sorry, communication has shut down due to an error:\n     " + message);
            connected = false;
            connection = null;
        }

        protected void playerConnected(int newPlayerID) {
            pasteToConsole("Someone new has joined the chat room, with ID number " + newPlayerID);
        }

        protected void playerDisconnected(int departingPlayerID) {
            pasteToConsole("The person with ID number " + departingPlayerID + " has left the chat room");
        }
	}

    private ChatClient connection;
    private volatile boolean connected;
    private Scanner scanner;

    private TestClient(final String host) {
        
    	scanner = new Scanner(System.in);

        new Thread() {
            public void run() {
                try {
                    pasteToConsole("Connecting to " + host + " ...");
                    connection = new ChatClient(host);
                    connected = true;
                }
                catch (IOException e) {
                    pasteToConsole("Connection attempt failed.");
                    pasteToConsole("Error: " + e);
                }
            }
        }.start();

        String textMessage;
        while (true) {
        	textMessage = scanner.nextLine();
        	if (textMessage.equals("quit"))
        		doQuit();
        	else if (textMessage.equals("query"))
        		connection.send("query");
        	else {
        		connection.send(new ChatMessage(PLAYER, textMessage));
        	}
        }
    }

    private void pasteToConsole(String message) {
        // for testing
        System.out.println(message + "\n");
    }
    
    
    /**
     * Called when the user clicks the Quit button or closes
     * the window by clicking its close box.
     */
    private void doQuit() {
        if (connected)
            connection.disconnect();  // Sends a DisconnectMessage to the server.
        try {
            Thread.sleep(1000); // Time for DisconnectMessage to actually be sent.
        }
        catch (InterruptedException e) {
        }
        System.exit(0);
    }
}
