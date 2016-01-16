package liardice;

import netgame.common.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameWindow extends JFrame {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");
  
  private String myName;

  private JTextArea console;
  private JTextField fieldInput;
  private JButton buttonSend;

  private Display display;
  private Board board;
  private Chatroom chatroom;

  // private Image diceImages;
  
  private GameClient connection;

  public GameWindow(final String hubHostName, final int hubPort, final String myName) {
    super("Liar's Dice");

    /*
    ClassLoader cl = getClass().getClassLoader();
    URL imageURL = cl.getResource("liardice/dice.png");
    diceImages = Toolkit.getDefaultTookit().createImage(imageURL);
    */

    this.myName = myName;

    display = new Display();
    setContentPane(display);
    pack();

    setResizable(false);
    setLocation(200, 100);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    setVisible(true);

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
    final Color green = new Color(0, 100, 0);

    Board() {
      setLayout(null);
      setPreferredSize(new Dimension(675, 585));
      setBackground(new Color(172, 252, 252));
      setBorder(BorderFactory.createLineBorder(brown, 8));

      // messageFromServer = makeLabel(30, 205, 500, 25, 16, brown);
      
    }

    JLabel makeLabel(int x, int y, int width, int height, int fontSize, Color color) {
      JLabel label = new JLabel();
      label.setBounds(x, y, width, height);
      label.setOpaque(false);
      label.setForeground(color);
      label.setFont(new Font("Serif", Font.BOLD, fontSize));
      add(label);
      return label;
    }

    JButton makeButton(String text, int x, int y, ActionListener listener) {
      JButton button = new JButton(text);
      button.setEnabled(false);
      button.setBounds(x, y, 80, 35);
      button.setFont(new Font("SansSerif", Font.BOLD, 24));
      button.addActionListener(listener);
      add(button);
      return button;
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
      panelInput.add(fieldInput, BorderLayout.CENTER);
      buttonSend = new JButton("Send");
      buttonSend.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (e.getSource() == buttonSend) {
            sendChatMessage(fieldInput.getText());
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
            }
            /*
            else if (fm.message instanceof String[]) {
              // TODO
            } else if (fm.message instanceof Dice[]) {
              // TODO
            }
            */
          }
        });
      }
    }
  }

  private void addMessage(String nickname, String message) {
    console.append(DATE_FORMAT.format(new Date()) + " " + nickname + ": " + message + "\n");
  }

  private void sendChatMessage(String message) {
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
}

