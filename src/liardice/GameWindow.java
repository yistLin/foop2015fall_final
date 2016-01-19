package liardice;

import netgame.common.*;
import liardice.message.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameWindow extends JFrame {

  // console date format
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");
  
  // game data
  private String myName;
  private String[] playerList;
  private Dice[] dice;
  private int lastNumber = 0, lastValue = 0;
  private boolean shutdown = false;
  private boolean bidLose = false;
  private boolean catchWin = false;
  private boolean[] playerListMuted;

  // status color
  private final Color normalPanelColor = new Color(190, 198, 216);
  private final Color normalColor = new Color(209, 209, 224);
  private final Color bidColor = new Color(234, 100, 100);
  private final Color catchColor = new Color(255, 194, 102);
  private final Color yesCatchColor = new Color(92, 174, 100);
  private final Color losedColor = new Color(111, 117, 132);

  // main panel
  private Display display;
  private Board board;
  private Chatroom chatroom;

  // status panel
  private JLabel statusMessage;
  private JPanel playerPanel;
  private JButton[] playerListButton;
  private Image diceLogo;
  private Image beerImage;

  // bid panel
  private JPanel bidPanel;
  private JTextField bidNumberInput;
  private JTextField bidValueInput;
  private JLabel bidMessage;
  private JLabel bidDiscription;
  private JLabel bidLastNumber;
  private JLabel bidLastValue;
  private JButton bidButton;

  // catch panel
  private JPanel catchPanel;
  private JLabel catchMessage;
  private JLabel catchDiscription;
  private JButton catchNoButton;
  private JButton catchYesButton;

  // chatroom panel
  private JTextArea console;
  private JTextField fieldInput;
  private JButton buttonSend;

  // dice set panel
  private DiceSet diceSet;
  private Image diceImages;
  
  // client for connection
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

    ClassLoader cl = getClass().getClassLoader();
    URL imageURL = cl.getResource("./src/liardice/dice.png");
    diceImages = Toolkit.getDefaultToolkit().createImage(imageURL);
    imageURL = cl.getResource("./src/liardice/dice_logo.png");
    diceLogo = Toolkit.getDefaultToolkit().createImage(imageURL);
    imageURL = cl.getResource("./src/liardice/beer.png");
    beerImage = Toolkit.getDefaultToolkit().createImage(imageURL);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        doQuit();
      }
    });

    // show IP
    try {
      addMessage("Your IP is " + InetAddress.getLocalHost().getHostAddress() + "\n");
    } catch (UnknownHostException e) {
    }

    // set connection to server
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
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      add(new Status());

      JPanel row = new JPanel();
      row.setLayout(new FlowLayout(FlowLayout.LEFT));
      row.setBackground(new Color(110, 58, 0));
      board = new Board();
      chatroom = new Chatroom();
      row.add(board);
      row.add(chatroom);
      add(row);

    }
  }

  private class Status extends JPanel {

    Status() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBackground(new Color(110, 58, 0));
      setPreferredSize(new Dimension(675, 150));

      JPanel statusPanel = new StatusPanel();
      add(statusPanel);

      playerPanel = new JPanel();
      playerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
      playerPanel.setPreferredSize(new Dimension(675, 70));
      playerPanel.setBackground(new Color(191, 116, 105));
      playerPanel.setBorder(BorderFactory.createLineBorder(new Color(110, 58, 0), 5));
      add(playerPanel);
    }
  }

  private class StatusPanel extends JPanel {

    StatusPanel() {
      setLayout(new BorderLayout());
      setBackground(new Color(143, 206, 216));
      setPreferredSize(new Dimension(675, 90));
      setBorder(BorderFactory.createLineBorder(new Color(110, 58, 0), 5));
      statusMessage = new JLabel("Waiting for other players.", JLabel.CENTER);
      statusMessage.setForeground(new Color(100, 100, 100));
      statusMessage.setFont(new Font("Phosphate", Font.BOLD, 48));
      add(statusMessage, BorderLayout.CENTER);
    }

    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.drawImage(diceLogo, 15, 10, 70, 70, this);
      g.drawImage(beerImage, 920, 16, 84, 60, this);
    }
  }

  private class Board extends JPanel {

    Board() {
      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(675, 585));
      setBorder(BorderFactory.createLineBorder(new Color(130, 70, 0), 8));

      JPanel gamePanel = new JPanel();
      gamePanel.setLayout(new GridLayout(0, 1));

      bidPanel = new BidPanel();
      catchPanel = new CatchPanel();
      gamePanel.add(bidPanel);
      gamePanel.add(catchPanel);
      add(gamePanel, BorderLayout.CENTER);

      diceSet = new DiceSet();
      add(diceSet, BorderLayout.SOUTH);
    }
  }

  private class BidPanel extends JPanel {

    BidPanel() {
      setBorder(BorderFactory.createLineBorder(new Color(110, 58, 0), 3));
      setLayout(new GridLayout(0, 1));
      setBackground(normalPanelColor);

      // some black magic
      JPanel row, column;

      row = new JPanel();
      row.setLayout(new GridLayout(1, 1));
      row.setOpaque(false);
      JLabel panelName = new JLabel("Bid Panel", JLabel.CENTER);
      panelName.setFont(new Font("Phosphate", Font.BOLD, 48));
      panelName.setForeground(new Color(100, 100, 100));
      row.add(panelName);
      bidMessage = new JLabel("", JLabel.CENTER);
      bidMessage.setFont(new Font("Nanum Pen Script", Font.BOLD, 28));
      row.add(bidMessage);
      add(row);

      bidDiscription = new JLabel("", JLabel.CENTER);
      bidDiscription.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
      add(bidDiscription);

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


      row = new JPanel();
      row.setLayout(new FlowLayout(FlowLayout.LEFT));
      row.setOpaque(false);

      column = new JPanel();
      column.setLayout(new FlowLayout(FlowLayout.LEFT));
      column.setOpaque(false);
      column.setPreferredSize(new Dimension(270, 50));
      column.add(Box.createHorizontalStrut(15)); // reserved space
      column.add(new JLabel("Number of dice:"));
      column.add(bidNumberInput);
      column.add(bidLastNumber);
      row.add(column);

      column = new JPanel();
      column.setLayout(new FlowLayout(FlowLayout.LEFT));
      column.setOpaque(false);
      column.setPreferredSize(new Dimension(270, 50));
      column.add(Box.createHorizontalStrut(15)); // reserved space
      column.add(new JLabel("Value of dice:"));
      column.add(bidValueInput);
      column.add(bidLastValue);
      row.add(column);

      column = new JPanel();
      column.setLayout(new BorderLayout());
      column.setOpaque(false);
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
        if (number <= 0)
          throw new IllegalNumberException("Illegal number of dice");
        if (number < lastNumber)
          throw new IllegalNumberException("Number can't less than last");
      } catch (NumberFormatException e) {
        bidDiscription.setText("You must enter number of dice!");
        bidDiscription.setForeground(Color.white);
        bidNumberInput.selectAll();
        bidNumberInput.requestFocus();
        success = false;
      } catch (IllegalNumberException e) {
        bidDiscription.setText(e.getMessage());
        bidDiscription.setForeground(Color.white);
        bidNumberInput.selectAll();
        bidNumberInput.requestFocus();
        success = false;
      }
      try {
        value = Integer.parseInt(bidValueInput.getText().trim());
        if (value <= 0 || value > 6)
          throw new IllegalNumberException("Illegal value of dice");
        if (number == lastNumber && value <= lastValue)
          throw new IllegalNumberException("Value must greater than last");
      } catch (NumberFormatException e) {
        bidDiscription.setText("You must enter value of dice!");
        bidDiscription.setForeground(Color.white);
        bidValueInput.selectAll();
        bidValueInput.requestFocus();
        success = false;
      } catch (IllegalNumberException e) {
        bidDiscription.setText(e.getMessage());
        bidDiscription.setForeground(Color.white);
        bidValueInput.selectAll();
        bidValueInput.requestFocus();
        success = false;
      }

      if (success) {
        connection.send(new BidMessage(number, value));
        disBid();
      }

    }
  }

  private class CatchPanel extends JPanel {

    CatchPanel() {
      setBorder(BorderFactory.createLineBorder(new Color(110, 58, 0), 3));
      setLayout(new GridLayout(0, 1));
      setBackground(normalPanelColor);

      JPanel row = new JPanel();
      row.setLayout(new GridLayout(1, 1));
      row.setOpaque(false);
      JLabel panelName = new JLabel("Catch Panel", JLabel.CENTER);
      panelName.setFont(new Font("Phosphate", Font.BOLD, 48));
      panelName.setForeground(new Color(100, 100, 100));
      row.add(panelName);
      catchMessage = new JLabel("", JLabel.CENTER);
      catchMessage.setFont(new Font("Nanum Pen Script", Font.BOLD, 28));
      row.add(catchMessage);
      add(row);

      catchDiscription = new JLabel("", JLabel.CENTER);
      catchDiscription.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
      add(catchDiscription);

      row = new JPanel();
      row.setLayout(new GridLayout(1, 2));
      row.setOpaque(false);

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
          disableCatch();
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
      setBackground(new Color(191, 116, 105));
      setPreferredSize(new Dimension(675, 135));
      setBorder(BorderFactory.createLineBorder(new Color(110, 58, 0), 3));
      
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
      setOpaque(false);

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

  private class DiceShowPanel extends JPanel {

    private int num;

    DiceShowPanel(final int num) {
      setPreferredSize(new Dimension(50, 50));

      this.num = num;
    }

    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      drawDice(g, num, 5, 5);
    }

    public void drawDice(Graphics g, int diceValue, int x,int y) {
      int column = (diceValue+2) % 3;
      int row = (diceValue+2) / 3 - 1;
      int cx = 229 * column + 5;
      int cy = 229 * row + 5;
      if (diceValue == 2) cx --;
      if (diceValue == 3) cx -=5;
      if (diceValue == 6) cx -=5;
      g.drawImage(diceImages, x, y, x+39, y+39, cx, cy, cx+225, cy+225, this);
    }
  }
  
  private class Chatroom extends JPanel {

    Chatroom() {
      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(300, 585));
      setBorder(BorderFactory.createLineBorder(new Color(130, 70, 0), 8));

      console = new JTextArea();
      console.setBackground(new Color(240, 255, 255));
      console.setEditable(false);
      ((DefaultCaret) console.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      console.setLineWrap(true);
      console.setWrapStyleWord(true);
      JScrollPane consoleSP = new JScrollPane(console);
      consoleSP.setBackground(new Color(240, 255, 255));
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
        final ForwardedMessage fm = (ForwardedMessage)forwardedMessage;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (fm.message instanceof ChatMessage) {
              ChatMessage cm = (ChatMessage)fm.message;
              int id = (int)fm.senderID;
              if (!playerListMuted[id - 1])
                addMessage(cm.id, cm.message);
            } else if (fm.message instanceof String[]) {
              playerList = (String[])fm.message;
              createPlayerListButton();
              statusMessage.setText("Game started!!!\n");
            } else if (fm.message instanceof String) {
              String name = (String)fm.message;
              addMessage(name + " has connected.\n");
            } else if (fm.message instanceof Dice[]) {
              dice = (Dice[])fm.message;
              randomDice();
            } else if (fm.message instanceof GameStatus) {
              GameStatus gs = (GameStatus)fm.message;
              handleGameStatus(gs);
            } else if (fm.message instanceof RejectedMessage) {
              serverReject();
            }
          }
        });
      }
    }

    protected void serverShutdown(String message) {
      if (shutdown) {
        System.exit(0);
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(GameWindow.this, "Your opponent has quit.\nThe game is over.");
            System.exit(0);
         }
       });
      }
    }

    private void serverReject() {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(GameWindow.this, "Server is busy.");
          System.exit(0);
        }
      });
    }
  }

  private void createPlayerListButton() {
    playerListButton = new JButton[playerList.length];
    playerListMuted = new boolean[playerList.length];
    for (int i = 0; i != playerList.length; i++) {
      playerListButton[i] = new JButton(playerList[i]);
      playerListButton[i].setFont(new Font("Nanum Pen Script", Font.PLAIN, 20));
      playerListButton[i].setContentAreaFilled(true);
      playerListButton[i].setOpaque(true);
      playerListButton[i].setBorderPainted(false);
      playerListButton[i].setBackground(new Color(209, 209, 224));

      playerListMuted[i] = false;

      JPanel buf = new JPanel();
      buf.setLayout(new GridLayout(0, 1));
      buf.setPreferredSize(new Dimension(100, 45));
      buf.add(playerListButton[i]);
      playerPanel.add(buf);

      final int id = i;
      playerListButton[i].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          flipPlayerMuted(id);
        }
      });
    }
  }

  private void flipPlayerMuted(int id) {
    if (playerListMuted[id]) {
      playerListMuted[id] = false;
      playerListButton[id].setForeground(Color.black);
      addMessage(playerList[id] + " is recovered.\n");
    } else {
      playerListMuted[id] = true;
      playerListButton[id].setForeground(Color.white);
      addMessage(playerList[id] + " is muted.\n");
    }
  }

  private void setPlayerListButton(Color now) {
    for (int i = 0; i != playerList.length; i++)
      playerListButton[i].setBackground(now);
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
      shutdown = true;
      connection.send(new ContinueMessage(false));
    } else {
      System.exit(0);
    }
  }

  private void handleGameStatus(GameStatus gs) {
    if (gs.status == GameStatus.ROUND_START) {
      setPlayerListButton(normalColor);
      bidPanel.setBackground(normalPanelColor);
      catchPanel.setBackground(normalPanelColor);
      statusMessage.setText("Round " + gs.round);
      lastNumber = 0;
      lastValue = 0;
      addMessage("\nRound " + gs.round + " start\n\n");
    } else if (gs.status == GameStatus.DO_CATCH) {
      setPlayerListButton(catchColor);
      playerListButton[gs.currentPlayer - 1].setBackground(normalColor);
      lastNumber = gs.numberOfDice;
      lastValue = gs.valueOfDice;
      if (gs.currentPlayer != connection.getID())
        askCatch(playerList[gs.currentPlayer - 1], gs.numberOfDice, gs.valueOfDice);
      else
        catchMessage.setText("Wait for other's catching");
    } else if (gs.status == GameStatus.DO_BID) {
      playerListButton[gs.currentPlayer - 1].setBackground(bidColor);
      bidLose = false;
      if (gs.currentPlayer == connection.getID())
        askBid();
      else {
        bidMessage.setText(playerList[gs.currentPlayer - 1] + " is bidding...\n");
      }
    } else if (gs.status == GameStatus.NO_CATCH) {
      playerListButton[gs.currentPlayer - 1].setBackground(normalColor);
    } else if (gs.status == GameStatus.YES_CATCH) {
      playerListButton[gs.currentPlayer - 1].setBackground(yesCatchColor);
      if (gs.currentPlayer == connection.getID())
        catchWin = true;
      else
        catchWin = false;
      disableCatch();
    } else if (gs.status == GameStatus.ROUND_END) {
      playerListButton[gs.currentPlayer - 1].setBackground(losedColor);
      if (gs.currentPlayer == connection.getID()) {
        if (bidLose)
          bidPanel.setBackground(losedColor);
        else
          catchPanel.setBackground(losedColor);
      } else {
        if (catchWin)
          catchPanel.setBackground(yesCatchColor);
      }

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {}

      if (gs.currentPlayer == connection.getID())
        askContinue("You", gs.diceTable);
      else
        askContinue(playerList[gs.currentPlayer - 1], gs.diceTable);
    }
  }

  private void askBid() {
    bidPanel.setBackground(bidColor);

    bidMessage.setText("What's your bid?");

    bidNumberInput.setEnabled(true);
    bidValueInput.setEnabled(true);
    bidButton.setEnabled(true);

    bidDiscription.setText("Enter number and value");
    bidDiscription.setForeground(Color.black);

    bidNumberInput.selectAll();
    bidNumberInput.requestFocus();

    if (lastNumber != 0) {
      bidLastNumber.setText("last number:" + lastNumber);
      bidLastValue.setText("last value:" + lastValue);
    }

    bidLose = true;
  }

  private void disBid() {
    bidPanel.setBackground(normalPanelColor);

    bidMessage.setText("");
    bidNumberInput.setText("");
    bidValueInput.setText("");
    bidLastNumber.setText("");
    bidLastValue.setText("");
    bidDiscription.setText("");

    bidNumberInput.setEnabled(false);
    bidValueInput.setEnabled(false);
    bidButton.setEnabled(false);
  }

  private void askCatch(String player, int number, int value) {
    catchPanel.setBackground(catchColor);

    bidMessage.setText("");
    catchMessage.setText("Do you want to catch?");

    catchNoButton.setEnabled(true);
    catchYesButton.setEnabled(true);

    catchDiscription.setText(player + " bid number: " + number + " value: " + value);
  }

  private void disableCatch() {
    catchPanel.setBackground(normalPanelColor);

    catchMessage.setText("");
    catchDiscription.setText("");

    catchNoButton.setEnabled(false);
    catchYesButton.setEnabled(false);
  }

  private void askContinue(String losedPlayer, int[] diceTable) {
    JPanel askPanel = new JPanel();
    askPanel.setLayout(new GridLayout(0, 1));

    JPanel row = new JPanel(new GridLayout(0, 1));
    row.setOpaque(false);
    JLabel question = new JLabel("Continue?", JLabel.CENTER);
    question.setFont(new Font("Phosphate", Font.BOLD, 48));
    row.add(question);

    JLabel discription = new JLabel(losedPlayer + " losed. QAQ~", JLabel.CENTER);
    discription.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
    row.add(discription);

    askPanel.add(row);

    JPanel diceShow = new JPanel();
    diceShow.setOpaque(false);
    diceShow.setLayout(new GridLayout(2, 3));
    diceShow.setPreferredSize(new Dimension(380, 100));

    DiceShowPanel[] dicePanel = new DiceShowPanel[7];
    for (int i = 1; i != 7; i++) {
      row = new JPanel();
      row.setLayout(new FlowLayout(FlowLayout.CENTER));
      row.setOpaque(false);

      JPanel column = new JPanel();
      column.setPreferredSize(new Dimension(30, 50));
      column.setOpaque(false);
      JLabel num = new JLabel("" + diceTable[i], JLabel.CENTER);
      num.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
      column.add(num);
      row.add(column);

      column = new JPanel();
      column.setPreferredSize(new Dimension(30, 50));
      column.setOpaque(false);
      JLabel x = new JLabel("X", JLabel.CENTER);
      x.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));
      column.add(x);
      row.add(column);

      dicePanel[i] = new DiceShowPanel(i);
      row.add(dicePanel[i]);
      
      diceShow.add(row);
    }

    askPanel.add(diceShow);

    int action = JOptionPane.showConfirmDialog(null, askPanel, "Continue?",
        JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (action == 0) { // yes
      connection.send(new ContinueMessage(true));
    } else { // no
      connection.send(new ContinueMessage(false));
      shutdown = true;
    }
  }

  private void sortDice() {
    for (int i = 0; i != 5; i++) {
      for (int j = i + 1; j != 5; j++) {
        if (dice[j].value < dice[i].value) {
          Dice temp = dice[i];
          dice[i] = dice[j];
          dice[j] = temp;
        }
      }
    }

    diceSet.reDraw();
  }

  private void randomDice() {
    java.util.Random random = new java.util.Random();

    new Thread() {
      public void run() {
        Dice[] savedDice = new Dice[5];
        for (int i = 0; i != 5; i++)
          savedDice[i] = new Dice(dice[i]);

        for (int times = 0; times != 50; times++) {
          for (int i = 4; i >= 0; i--) {
            if (times > i * 10) {
              dice[i] = new Dice(savedDice[i]);
              break;
            }
            dice[i] = new Dice();
          }
          diceSet.reDraw();
          try {
            Thread.sleep(70);
          } catch (InterruptedException e) {}
        }

        connection.send(new ReadyMessage());
      }
    }.start();
  }

  private void drunkDice() {
    java.util.Random random = new java.util.Random();
    new Thread() {
      public void run() {
        for (int times = 0; times != 20; times++) {
          for (int i = 4; i >= 0; i--) {
            int j = random.nextInt(i + 1);
            Dice temp = dice[i];
            dice[i] = dice[j];
            dice[j] = temp;
          }
          diceSet.reDraw();
          try {
            Thread.sleep(300);
          } catch (InterruptedException e) {}
        }
      }
    }.start();
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

