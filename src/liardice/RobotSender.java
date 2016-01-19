package liardice;

import java.io.IOException;

public class RobotSender {
  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println("Illegal argument.");
      System.exit(0);
    }

    int number = 0;
    try {
      number = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      System.out.println("Illegal number of Robots.");
      System.exit(0);
    }

    if (number > 5) {
      System.out.println("Illegal number of Robots.");
    }

    int port = 0;
    try {
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      System.out.println("Illegal port");
      System.exit(0);
    }

    String host = args[0];
    ConfigShuffler cs = new ConfigShuffler();
    for (int i = 0; i != number; i++) {
      try {
        new Robot(host, port, cs.getNext());
      } catch (IOException e) {
        System.out.println("Error: Can't connect to " + host);
        System.exit(0);
      }
    }
  }
}

