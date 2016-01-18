package liardice;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;

public class Main {

  private static final int DEFAULT_PORT = 42857;

  public static void main(String[] args) {

    // Welcome message
    JLabel welcome = new JLabel("Liar's Dice!", JLabel.CENTER);
    welcome.setFont(new Font("Phosphate", Font.BOLD, 72));
    JLabel message = new JLabel("To lie or not to lie.", JLabel.CENTER);
    message.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
   
    // Input text
    final JTextField nameInput = new JTextField(10);
    final JTextField playerNumberInput = new JTextField(1);
    final JTextField listeningPortInput = new JTextField("" + DEFAULT_PORT, 5);
    final JTextField hostInput = new JTextField(30);
    final JTextField connectPortInput = new JTextField("" + DEFAULT_PORT, 5);
    final JTextField aiNumberInput = new JTextField(1);
    
    // Buttons for setting mode
    final JRadioButton selectServerMode = new JRadioButton("Start a new game.");
    final JRadioButton selectClientMode = new JRadioButton("Connect to existing game.");
    
    // Set mode button together
    ButtonGroup group = new ButtonGroup();
    group.add(selectServerMode);
    group.add(selectClientMode);
    ActionListener radioListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectServerMode) {
          listeningPortInput.setEnabled(true);
          playerNumberInput.setEnabled(true);
          aiNumberInput.setEnabled(true);
          hostInput.setEnabled(false);
          connectPortInput.setEnabled(false);
          listeningPortInput.setEditable(true);
          playerNumberInput.setEditable(true);
          aiNumberInput.setEditable(true);
          hostInput.setEditable(false);
          connectPortInput.setEditable(false);
        } else {
          listeningPortInput.setEnabled(false);
          playerNumberInput.setEnabled(false);
          hostInput.setEnabled(true);
          connectPortInput.setEnabled(true);
          listeningPortInput.setEditable(false);
          playerNumberInput.setEditable(false);
          hostInput.setEditable(true);
          connectPortInput.setEditable(true);
        }
      }
    };
    selectServerMode.addActionListener(radioListener);
    selectClientMode.addActionListener(radioListener);
    selectServerMode.setSelected(true);
    hostInput.setEnabled(false);
    connectPortInput.setEnabled(false);
    hostInput.setEditable(false);
    connectPortInput.setEditable(false);

    // Create main panel
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new GridLayout(0, 1, 5, 5));
    inputPanel.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(Color.BLACK, 2),
          BorderFactory.createEmptyBorder(6, 6, 6, 6)));

    // Some black magic
    JPanel row, column;
    
    // Add welcome and message to main panel
    row = new JPanel();
    row.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.ipady = 20;
    c.gridx = 0;
    c.gridy = 0;
    row.add(welcome, c);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.ipady = 10;
    c.gridx = 0;
    c.gridy = 1;
    row.add(message, c);

    inputPanel.add(row);

    // Add server mode and client mode
    row = new JPanel();
    row.setLayout(new GridLayout(0, 1));
    
    // Player name input
    column = new JPanel();
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(new JLabel("Please type your nickname (length<11):"));
    column.add(nameInput);
    row.add(column);
    
    // Server mode
    row.add(selectServerMode); // select button

    column = new JPanel(); // number of player input
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(Box.createHorizontalStrut(40)); // reserved space
    column.add(new JLabel("Number of player (1~6):"));
    column.add(playerNumberInput);
    row.add(column);
    
    column = new JPanel(); // number of AI
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(Box.createHorizontalStrut(40)); // reserved space
    column.add(new JLabel("Number of AI (0~5):"));
    column.add(aiNumberInput);
    column.add(Box.createHorizontalStrut(5)); // reserved space
    column.add(new JLabel("(Total players <= 10)"));
    row.add(column);
    
    column = new JPanel(); // port input
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(Box.createHorizontalStrut(40)); // reserved space
    column.add(new JLabel("Listen on port:"));
    column.add(listeningPortInput);
    row.add(column);

    // Client mode
    row.add(selectClientMode); // select button

    column = new JPanel(); // host input
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(Box.createHorizontalStrut(40)); // reserved space
    column.add(new JLabel("Host URL:"));
    column.add(hostInput);
    row.add(column);
    
    column = new JPanel(); // port input
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(Box.createHorizontalStrut(40)); // reserved space
    column.add(new JLabel("Port Number:"));
    column.add(connectPortInput);
    row.add(column);

    inputPanel.add(row);

    while (true) {

      int action = JOptionPane.showConfirmDialog(null, inputPanel, "Liar's Dice",
          JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

      if (action != JOptionPane.OK_OPTION)
        return;

      if (selectServerMode.isSelected()) {
        String nickname;
        int playerNumber, port, aiNumber;
        try {
          playerNumber = Integer.parseInt(playerNumberInput.getText().trim());
          if (playerNumber < 1 || playerNumber > 6)
            throw new IllegalPlayerNumberException("Illegal number of player");
        } catch (NumberFormatException e) {
          message.setText("You must enter number of player!");
          message.setForeground(Color.red);
          playerNumberInput.selectAll();
          playerNumberInput.requestFocus();
          continue;
        } catch (IllegalPlayerNumberException e) {
          message.setText(e.getMessage());
          message.setForeground(Color.red);
          playerNumberInput.selectAll();
          playerNumberInput.requestFocus();
          continue;
        }
        try {
          aiNumber = Integer.parseInt(aiNumberInput.getText().trim());
          if (aiNumber < 0 || aiNumber + playerNumber > 6 || aiNumber + playerNumber < 2)
            throw new IllegalAINumberException("Illegal number of AI");
          playerNumber += aiNumber;
        } catch (NumberFormatException e) {
          aiNumberInput.setText("0");
          aiNumber = 0;
          continue;
        } catch (IllegalAINumberException e) {
          message.setText(e.getMessage());
          message.setForeground(Color.red);
          aiNumberInput.selectAll();
          aiNumberInput.requestFocus();
          continue;
        }
        try {
          nickname = nameInput.getText().trim();
          if (nickname.length() == 0)
            throw new IllegalNameException("You must enter a nickname!");
          else if (nickname.length() > 10)
            throw new IllegalNameException("Nickname is too long.");
          port = Integer.parseInt(listeningPortInput.getText().trim());
          if (port <= 0 || port >= 65536)
            throw new IllegalPortException("Illegal port number!");
        } catch (IllegalPortException e) {
          message.setText(e.getMessage());
          message.setForeground(Color.red);
          listeningPortInput.selectAll();
          listeningPortInput.requestFocus();
          continue;
        } catch (NumberFormatException e) {
          message.setText("You must enter a port number!");
          message.setForeground(Color.red);
          listeningPortInput.selectAll();
          listeningPortInput.requestFocus();
          continue;
        } catch (IllegalNameException e) {
          message.setText(e.getMessage());
          message.setForeground(Color.red);
          nameInput.requestFocus();
          continue;
        }
        // new GameHub
        try {
          new GameHub(port, playerNumber);
        } catch (IOException e) {
          message.setText("Error: Can't listen on port " + port);
          message.setForeground(Color.red);
          listeningPortInput.selectAll();
          listeningPortInput.requestFocus();
          continue;
        }
        // new GameWindow
        new GameWindow("localhost", port, nickname);
        // new Robot
        for (int i = 1; i <= aiNumber; i++) {
          try {
            new Robot("localhost", port);
          } catch (IOException e) {}
        }
        break;
      } else {
        String nickname, host;
        int port;
        try {
          nickname = nameInput.getText().trim();
          if (nickname.length() == 0)
            throw new IllegalNameException("You must enter a nickname!");
          else if (nickname.length() > 10)
            throw new IllegalNameException("Nickname is too long.");
          host = hostInput.getText().trim();
          if (host.length() == 0)
            throw new IllegalURLException("You must enter a host URL!");
          else if (host.length() > 30)
            throw new IllegalURLException("URL is too long. Please use IP.");
          port = Integer.parseInt(connectPortInput.getText().trim());
          if (port <= 0 || port >= 65536)
            throw new IllegalPortException("Illegal port number!");
        } catch (IllegalURLException e) {
          message.setText(e.getMessage());
          message.setForeground(Color.red);
          hostInput.requestFocus();
          continue;
        } catch (IllegalPortException e) {
          message.setText(e.getMessage());
          message.setForeground(Color.red);
          connectPortInput.selectAll();
          connectPortInput.requestFocus();
          continue;
        } catch (NumberFormatException e) {
          message.setText("You must enter a port number!");
          message.setForeground(Color.red);
          listeningPortInput.selectAll();
          listeningPortInput.requestFocus();
          continue;
        } catch (IllegalNameException e) {
          message.setText(e.getMessage());
          message.setForeground(Color.red);
          nameInput.requestFocus();
          continue;
        }
        // new GameWindow
        new GameWindow(host, port, nickname);
        break;
      }
    }
  }

  private static class IllegalPortException extends Exception {

    public IllegalPortException() {
      super("Illegal port number!");
    }

    public IllegalPortException(String message) {
      super(message);
    }
  }

  private static class IllegalURLException extends Exception {

    public IllegalURLException() {
      super("Illegal URL!");
    }

    public IllegalURLException(String message) {
      super(message);
    }
  }
  
  private static class IllegalNameException extends Exception {

	  public IllegalNameException() {
	    super("Illegal name!");
	  }

	  public IllegalNameException(String message) {
	    super(message);
	  }
  }
  
  private static class IllegalPlayerNumberException extends Exception {
    
	  public IllegalPlayerNumberException() {
      super("Illegal number of player!");
    }
    
    public IllegalPlayerNumberException(String message) {
	    super(message);
	  }
  }
  
  private static class IllegalAINumberException extends Exception {
    
    public IllegalAINumberException() {
      super("Illegal number of AI!");
    }
    
    public IllegalAINumberException(String message) {
      super(message);
    }
  }
}

