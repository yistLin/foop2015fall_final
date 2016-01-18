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

  private Display display;
  private Board board;
  private Chatroom chatroom;

  private JLabel statusMessage;

  private JTextField bidNumberInput;
  private JTextField bidValueInput;
  private JLabel bidLastNumber;
  private JLabel bidLastValue;
  private JButton bidButton;

  private JLabel catchDiscription;
  private JButton catchNoButton;
  private JButton catchYesButton;

  private JTextArea console;
  private JTextField fieldInput;
  private JButton buttonSend;

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
    URL imageURL = cl.getResource("./src/liardice/dice.png");
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
      setBorder(BorderFactory.createLineBorder(brown, 8));

      JPanel statusPanel = new JPanel();
      statusPanel.setBorder(BorderFactory.createLineBorder(new Color(30, 70, 50), 3));
      statusMessage = new JLabel("Waiting for other players.", JLabel.CENTER);
      statusMessage.setFont(new Font("Phosphate", Font.BOLD, 48));
      statusPanel.add(statusMessage);
      add(statusPanel, BorderLayout.NORTH);

      JPanel gamePanel = new JPanel();
      gamePanel.setLayout(new GridLayout(0, 1));

      JPanel bidPanel = new BidPanel();
      JPanel catchPanel = new CatchPanel();
      gamePanel.add(bidPanel);
      gamePanel.add(catchPanel);
      add(gamePanel, BorderLayout.CENTER);

      diceSet = new DiceSet();
      add(diceSet, BorderLayout.SOUTH);
    }
  }

  private class BidPanel extends JPanel {

    private JLabel message;

    BidPanel() {
      setBorder(BorderFactory.createLineBorder(new Color(30, 70, 50), 3));
      setLayout(new GridLayout(0, 1));

      JLabel panelName = new JLabel("Bid Panel", JLabel.CENTER);
      panelName.setFont(new Font("Phosphate", Font.BOLD, 48));
      add(panelName);

      message = new JLabel("Enter number and value", JLabel.CENTER);
      message.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
      add(message);

      bidNumberInput = new JTextField(2);
      bidValueInput = new JTextField(1);
      bidNumberInput.setEnabled(false);
      bidValueInput.setEnabled(false);

      bidNumberInput.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            bidValueInput.selectAll();
            bidValueInput.requestFocus();
          }
        }
      });

      bidValueInput.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            bidIt();
          }
        }
      });

      bidLastNumber = new JLabel("", JLabel.CENTER);
      bidLastValue = new JLabel("", JLabel.CENTER);

      JPanel row, column;

      row = new JPanel();
      row.setLayout(new FlowLayout(FlowLayout.LEFT));

      column = new JPanel();
      column.setLayout(new FlowLayout(FlowLayout.LEFT));
      //column.setBorder(BorderFactory.createLineBorder(new Color(30, 70, 50), 3));
      column.setPreferredSize(new Dimension(270, 50));
      column.add(Box.createHorizontalStrut(15)); // reserved space
      column.add(new JLabel("Number of dice:"));
      column.add(bidNumberInput);
      column.add(bidLastNumber);
      row.add(column);

      column = new JPanel();
      column.setLayout(new FlowLayout(FlowLayout.LEFT));
      //column.setBorder(BorderFactory.createLineBorder(new Color(30, 70, 50), 3));
      column.setPreferredSize(new Dimension(270, 50));
      column.add(Box.createHorizontalStrut(15)); // reserved space
      column.add(new JLabel("Value of dice:"));
      column.add(bidValueInput);
      column.add(bidLastValue);
      row.add(column);

      column = new JPanel();
      column.setLayout(new BorderLayout());
      //column.setBorder(BorderFactory.createLineBorder(new Color(30, 70, 50), 3));
      column.setPreferredSize(new Dimension(90, 50));
      bidButton = new JButton("Bid!");
      bidButton.setEnabled(false);
      column.add(bidButton, BorderLayout.CENTER);
      row.add(column);

      add(row);

      ActionListener buttonListener = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          if (ae.getSource() == bidButton) {
            bidIt();
          }
        }
      };

      bidButton.addActionListener(buttonListener);
    }

    private void bidIt() {
      boolean success = true;
      int number = 0, value = 0;
      try {
        number = Integer.parseInt(bidNumberInput.getText().trim());
        if (number < 0)
          throw new IllegalNumberException("Illegal number of dice");
        if (number < lastNumber)
          throw new IllegalNumberException("Number can't less than last");
      } catch (NumberFormatException e) {
        message.setText("You must enter number of dice!");
        message.setForeground(Color.red);
        bidNumberInput.selectAll();
        bidNumberInput.requestFocus();
        success = false;
      } catch (IllegalNumberException e) {
        message.setText(e.getMessage());
        message.setForeground(Color.red);
        bidNumberInput.selectAll();
        bidNumberInput.requestFocus();
        success = false;
      }
      try {
        value = Integer.parseInt(bidValueInput.getText().trim());
        if (value < 1 || value > 6)
          throw new IllegalNumberException("Illegal value of dice");
        if (number == lastNumber && value <= lastValue)
          throw new IllegalNumberException("Value must greater than last");
      } catch (NumberFormatException e) {
        message.setText("You must enter value of dice!");
        message.setForeground(Color.red);
        bidValueInput.selectAll();
        bidValueInput.requestFocus();
        success = false;
      } catch (IllegalNumberException e) {
        message.setText(e.getMessage());
        message.setForeground(Color.red);
        bidValueInput.selectAll();
        bidValueInput.requestFocus();
        success = false;
      }

      if (success) {
        connection.send(new BidMessage(number, value));

        bidNumberInput.setText("");
        bidValueInput.setText("");
        bidLastNumber.setText("");
        bidLastValue.setText("");

        bidNumberInput.setEnabled(false);
        bidValueInput.setEnabled(false);
        bidButton.setEnabled(false);
      }

    }
  }

  private class CatchPanel extends JPanel {

    CatchPanel() {
      setBorder(BorderFactory.createLineBorder(new Color(30, 70, 50), 3));
      setLayout(new GridLayout(0, 1));

      JLabel panelName = new JLabel("Catch Panel", JLabel.CENTER);
      panelName.setFont(new Font("Phosphate", Font.BOLD, 48));
      add(panelName);

      catchDiscription = new JLabel("", JLabel.CENTER);
      catchDiscription.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
      add(catchDiscription);

      JPanel row = new JPanel();
      row.setLayout(new GridLayout(1, 2));

      catchNoButton = new JButton("No");
      catchNoButton.setEnabled(false);
      row.add(catchNoButton);

      catchYesButton = new JButton("Yes");
      catchYesButton.setEnabled(false);
      row.add(catchYesButton);

      add(row);

      ActionListener buttonListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (e.getSource() == catchNoButton) {
            connection.send(new CatchMessage(false));
          } else {
            connection.send(new CatchMessage(true));
          }
          catchNoButton.setEnabled(false);
          catchYesButton.setEnabled(false);
        }
      };

      catchNoButton.addActionListener(buttonListener);
      catchYesButton.addActionListener(buttonListener);
    }
  }

  private class DiceSet extends JPanel {

    private DicePanel[] dicePanel = new DicePanel[5];
    
    DiceSet() {
      setLayout(new GridLayout(1, 5));
      setPreferredSize(new Dimension(675, 135));
      setBorder(BorderFactory.createLineBorder(new Color(30, 70, 50), 3));
      
      for (int i = 0; i < 5; i++) {
        dicePanel[i] = new DicePanel(i);
        add(dicePanel[i]);
      }
    }
    
    public void reDraw() {
      for (int i = 0; i < 5; i++) {
        dicePanel[i].repaint();
      }
    }
  }

  private class DicePanel extends JPanel {
    
    private int num;
    
    DicePanel(final int num) {
      this.num = num;
    }
    
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if(dice != null) {
        drawDice(g, dice[num].value, 30, 30);
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
              statusMessage.setText("Game started!!!\n");
            } else if (fm.message instanceof String) {
              String name = (String)fm.message;
              addMessage(name + " has connected.\n");
            } else if (fm.message instanceof Dice[]) {
              dice = (Dice[])fm.message;
              diceSet.reDraw();
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
      else
        statusMessage.setText("Wait for other's catching");
    } else if (gs.status == GameStatus.DO_BID) {
      addMessage(playerList[gs.currentPlayer - 1] + " is bidding...\n");
      if (gs.currentPlayer == connection.getID())
        askBid();
      else
        statusMessage.setText("Wait for other's bidding");
    } else if (gs.status == GameStatus.NO_CATCH) {
      addMessage(playerList[gs.currentPlayer - 1] + " didn't catch.\n");
    } else if (gs.status == GameStatus.YES_CATCH) {
      catchNoButton.setEnabled(false);
      catchYesButton.setEnabled(false);
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

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }
      askContinue(playerList[gs.currentPlayer - 1], gs.diceTable);
    }
  }

  private void askBid() {
    statusMessage.setText("What's your bid?");

    bidNumberInput.setEnabled(true);
    bidValueInput.setEnabled(true);
    bidButton.setEnabled(true);

    bidNumberInput.selectAll();
    bidNumberInput.requestFocus();

    if (lastNumber != 0) {
      bidLastNumber.setText("last number:" + lastNumber);
      bidLastValue.setText("last value:" + lastValue);
    }
  }

  private void askCatch(String player, int number, int value) {
    statusMessage.setText("Do you want to catch?");

    catchNoButton.setEnabled(true);
    catchYesButton.setEnabled(true);

    catchDiscription.setText(player + " bid number: " + number + " value: " + value);
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

