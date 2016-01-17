package liardice;

import netgame.common.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameWindow extends JFrame {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");
  
  private String myName;
  private String[] playerList;
  private Dice[] dice;
  private int lastNumber = 0, lastValue = 0;

  private JTextArea console;
  private JTextField fieldInput;
  private JButton buttonSend;

  private Display display;
  private Board board;
  private Chatroom chatroom;

  private DiceSet diceSet;
  private Image diceImages;
  
  private GameClient connection;

  public GameWindow(final String hubHostName, final int hubPort, final String myName) {
    super("Liar's Dice");

    this.myName = myName;

    display = new Display();
    setContentPane(display);
    pack();

    setResizable(false);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    setVisible(true);

    // Test Area
    ClassLoader cl = getClass().getClassLoader();
    URL imageURL = cl.getResource("liardice/dice.png");
    diceImages = Toolkit.getDefaultToolkit().createImage(imageURL);

    // End of Test

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        doQuit();
      }
    });

    /*
    display.addMouseListener(new MouseAdapter() {
      public ovid mousePressed(MouseEvent evt) {
        doClick(evt.getX(), evt.getY());
      }
    });
    */
  
    try {
      addMessage("Your IP is " + InetAddress.getLocalHost().getHostAddress() + "\n");
    } catch (UnknownHostException e) {
    }

    new Thread() {
      public void run() {
        try {
          final GameClient c = new GameClient(hubHostName, hubPort);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              connection = c;
            }
          });
        } catch (final IOException e) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              dispose();
              JOptionPane.showMessageDialog(null, "Could not connect to " + hubHostName + ".\nError: " + e);
              System.exit(0);
            }
          });
        }
      }
    }.start();
  }

  private class Display extends JPanel {

    Display() {
      setLayout(new FlowLayout(FlowLayout.LEFT));
      setBackground(new Color(173, 86, 31));

      board = new Board();
      chatroom = new Chatroom();
      add(board);
      add(chatroom);
    }
  }

  private class Board extends JPanel {

    final Color brown = new Color(130, 70, 0);

    Board() {
      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(675, 585));
      setBackground(new Color(172, 252, 252));
      setBorder(BorderFactory.createLineBorder(brown, 8));

      add(new JPanel(), BorderLayout.CENTER);

      diceSet = new DiceSet();
      add(diceSet, BorderLayout.SOUTH);
    }
  }

  private class DiceSet extends JPanel {

    DiceSet() {
      setPreferredSize(new Dimension(675, 150));
      setBorder(BorderFactory.createLineBorder(new Color(30, 70, 50), 3));
    }
    
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if(dice != null) {
        for (int i = 0; i < 5; i ++) {
          drawDice(g, dice[i].value, 45 + 120 * i, 30);
        }
      }
    }
    
    public void drawDice(Graphics g, int diceValue, int x,int y) {
      int column = (diceValue+2) % 3;
      int row = (diceValue+2) / 3 - 1;
      int cx = 229 * column + 5;
      int cy = 229 * row + 5;
      if (diceValue == 2) cx --;
      if (diceValue == 3) cx -=5;
      if (diceValue == 6) cx -=5;
      g.drawImage(diceImages, x, y, x+75, y+75, cx, cy, cx+225, cy+225, this);
    }
  }

  private class Chatroom extends JPanel {

    Chatroom() {
      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(300, 585));
      setBackground(new Color(190, 235, 237));
      setBorder(BorderFactory.createLineBorder(new Color(130, 70, 0), 8));

      console = new JTextArea();
      console.setEditable(false);
      ((DefaultCaret) console.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      console.setLineWrap(true);
      console.setWrapStyleWord(true);
      JScrollPane consoleSP = new JScrollPane(console);
      consoleSP.setBorder(BorderFactory.createTitledBorder("Console"));
      add(consoleSP, BorderLayout.CENTER);

      JPanel panelInput = new JPanel(new BorderLayout());

      fieldInput = new JTextField();
      fieldInput.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            sendChatMessage(fieldInput.getText().trim());
            fieldInput.setText("");
          }
        }
      });
      panelInput.add(fieldInput, BorderLayout.CENTER);
      buttonSend = new JButton("Send");
      buttonSend.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (e.getSource() == buttonSend) {
            sendChatMessage(fieldInput.getText().trim());
            fieldInput.setText("");
          }
        }
      });
      panelInput.add(buttonSend, BorderLayout.EAST);

      add(panelInput, BorderLayout.SOUTH);
    }
  }

  private class GameClient extends Client {

    public GameClient(String hubHostName, int hubPort) throws IOException {
      super(hubHostName, hubPort);
      send(myName);
    }

    protected void messageReceived(final Object forwardedMessage) {
      if (forwardedMessage instanceof ForwardedMessage) {
        ForwardedMessage fm = (ForwardedMessage)forwardedMessage;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (fm.message instanceof ChatMessage) {
              ChatMessage cm = (ChatMessage)fm.message;
              addMessage(cm.id, cm.message);
            } else if (fm.message instanceof String[]) {
              playerList = (String[])fm.message;
              addMessage("Every player is here.\n");
              for (int i = 0; i != playerList.length; i++) {
                addMessage("Player" + (i + 1) + ": " + playerList[i] + "\n");
              }
              addMessage("Game started!!!\n");
            } else if (fm.message instanceof String) {
              String name = (String)fm.message;
              addMessage(name + " has connected.\n");
            } else if (fm.message instanceof Dice[]) {
              dice = (Dice[])fm.message;
              diceSet.repaint();
              addMessage("I got ");
              for (Dice d: dice) {
                addMessage(d.value + " ");
              }
              addMessage("\n");
              connection.send(new ReadyMessage());
            } else if (fm.message instanceof GameStatus) {
              GameStatus gs = (GameStatus)fm.message;
              handleGameStatus(gs);
            }
          }
        });
      }
    }

    protected void serverShutdown(String message) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(GameWindow.this, "Your opponent has quit.\nThe game is over.");
          System.exit(0);
        }
      });
    }
  }

  private void addMessage(String message) {
    console.append(message);
  }

  private void addMessage(String nickname, String message) {
    console.append(DATE_FORMAT.format(new Date()) + " " + nickname + ": " + message + "\n");
  }

  private void sendChatMessage(String message) {
    if (message.length() == 0)
      return ;
    connection.send(new ChatMessage(myName, message));
  }

  private void doQuit() {
    dispose();
    if (connection != null) {
      connection.disconnect();
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }
    }
    System.exit(0);
  }

  private void handleGameStatus(GameStatus gs) {
    if (gs.status == GameStatus.ROUND_START) {
      addMessage("\nRound " + gs.round + " start.\n");
      lastNumber = 0;
      lastValue = 0;
    } else if (gs.status == GameStatus.DO_CATCH) {
      addMessage(playerList[gs.currentPlayer - 1] + " number: " + gs.numberOfDice + " value: " + gs.valueOfDice + "\n");
      lastNumber = gs.numberOfDice;
      lastValue = gs.valueOfDice;
      if (gs.currentPlayer != connection.getID())
        askCatch(playerList[gs.currentPlayer - 1], gs.numberOfDice, gs.valueOfDice);
    } else if (gs.status == GameStatus.DO_BID) {
      addMessage(playerList[gs.currentPlayer - 1] + " is bidding...\n");
      if (gs.currentPlayer == connection.getID())
        askBid();
    } else if (gs.status == GameStatus.NO_CATCH) {
      addMessage(playerList[gs.currentPlayer - 1] + " didn't catch.\n");
    } else if (gs.status == GameStatus.YES_CATCH) {
      addMessage(playerList[gs.currentPlayer - 1] + " catched.\n");
    } else if (gs.status == GameStatus.ROUND_END) {
      addMessage(playerList[gs.currentPlayer - 1] + " losed.\n");
      addMessage("Total dice: ");
      for (int i = 1; i != 7; i++)
        addMessage(gs.diceTable[i] + " ");
      addMessage("\n");
      Frame question = JOptionPane.getRootFrame();
      if (question != null)
        question.dispose();

      askContinue(playerList[gs.currentPlayer - 1], gs.diceTable);
    }
  }

  private void askBid() {
    JPanel askPanel = new JPanel();
    askPanel.setLayout(new GridLayout(0, 1));

    JLabel question = new JLabel("What's your bid?", JLabel.CENTER);
    question.setFont(new Font("Phosphate", Font.BOLD, 48));

    JLabel message = new JLabel("Enter number and value", JLabel.CENTER);
    message.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));

    JTextField numberInput = new JTextField(2);
    JTextField valueInput = new JTextField(1);

    askPanel.add(question);
    askPanel.add(message);

    JPanel row, column;

    row = new JPanel();
    row.setLayout(new GridLayout(0, 1));

    column = new JPanel();
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(Box.createHorizontalStrut(40)); // reserved space
    column.add(new JLabel("Number of dice:"));
    column.add(numberInput);
    if (lastNumber != 0)
      column.add(new JLabel("last number: " + lastNumber));
    row.add(column);

    column = new JPanel();
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(Box.createHorizontalStrut(40)); // reserved space
    column.add(new JLabel("Value of dice:"));
    column.add(valueInput);
    if (lastValue != 0)
      column.add(new JLabel("last value: " + lastValue));
    row.add(column);
    
    askPanel.add(row);

    String[] options = {"OK"};

    while (true) {
      int action = JOptionPane.showOptionDialog(null, askPanel, "title", JOptionPane.NO_OPTION,
          JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

      if (action != 0)
        continue;

      int number, value;
      try {
        number = Integer.parseInt(numberInput.getText().trim());
        if (number < 0)
          throw new IllegalNumberException("Illegal number of dice");
        if (number < lastNumber)
          throw new IllegalNumberException("Number can't less than last");
      } catch (NumberFormatException e) {
        message.setText("You must enter number of dice!");
        message.setForeground(Color.red);
        numberInput.selectAll();
        numberInput.requestFocus();
        continue;
      } catch (IllegalNumberException e) {
        message.setText(e.getMessage());
        message.setForeground(Color.red);
        numberInput.selectAll();
        numberInput.requestFocus();
        continue;
      }
      try {
        value = Integer.parseInt(valueInput.getText().trim());
        if (value < 0 || value > 6)
          throw new IllegalNumberException("Illegal value of dice");
        if (number == lastNumber && value <= lastValue)
          throw new IllegalNumberException("Value must greater than last");
      } catch (NumberFormatException e) {
        message.setText("You must enter value of dice!");
        message.setForeground(Color.red);
        valueInput.selectAll();
        valueInput.requestFocus();
        continue;
      } catch (IllegalNumberException e) {
        message.setText(e.getMessage());
        message.setForeground(Color.red);
        valueInput.selectAll();
        valueInput.requestFocus();
        continue;
      }

      connection.send(new BidMessage(number, value));
      break;
    }
  }

  private void askCatch(String player, int number, int value) {
    JPanel askPanel = new JPanel();
    askPanel.setLayout(new GridLayout(0, 1));

    JLabel question = new JLabel("To catch or not to catch?", JLabel.CENTER);
    question.setFont(new Font("Phosphate", Font.BOLD, 48));
    askPanel.add(question);

    JLabel discription = new JLabel(player + " bid number: " + number + " value: " + value + "\n", JLabel.CENTER);
    discription.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
    askPanel.add(discription);

    int action = JOptionPane.showConfirmDialog(null, askPanel, "To catch or not to catch?",
        JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (action == 0) { // yes
      connection.send(new CatchMessage(true));
    } else { // no
      connection.send(new CatchMessage(false));
    }
  }

  private void askContinue(String losedPlayer, int[] diceTable) {
    JPanel askPanel = new JPanel();
    askPanel.setLayout(new GridLayout(0, 1));

    JLabel question = new JLabel("Continue?", JLabel.CENTER);
    question.setFont(new Font("Phosphate", Font.BOLD, 48));
    askPanel.add(question);

    JLabel discription = new JLabel(losedPlayer + " losed. QAQ~", JLabel.CENTER);
    discription.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
    askPanel.add(discription);

    int action = JOptionPane.showConfirmDialog(null, askPanel, "Continue?",
        JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (action == 0) { // yes
      connection.send(new ContinueMessage(true));
    } else { // no
      connection.send(new ContinueMessage(false));
    }
  }

  private static class IllegalNumberException extends Exception {
    
	  public IllegalNumberException() {
      super("Illegal number!");
    }
    
    public IllegalNumberException(String message) {
	    super(message);
	  }
  }
}

