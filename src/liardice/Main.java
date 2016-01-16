package liardice;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Main {

  private static final int DEFAULT_PORT = 42857;

  public static void main(String[] args) {

    // Welcome message
    JLabel welcome = new JLabel("Liar's Dice!", JLabel.CENTER);
    welcome.setFont(new Font("Phosphate", Font.BOLD, 72));
    JLabel message = new JLabel("To lie or not to lie.", JLabel.CENTER);
    message.setFont(new Font("Nanum Pen Script", Font.PLAIN, 36));

    // Connection port
    final JTextField listeningPortInput = new JTextField("" + DEFAULT_PORT, 5);
    final JTextField hostInput = new JTextField(30);
    final JTextField connectPortInput = new JTextField("" + DEFAULT_PORT, 5);

    // Buttons for setting mode
    final JRadioButton selectServerMode = new JRadioButton("Start a new game.");
    final JRadioButton selectClientMode = new JRadioButton("Connect to existing game.");
    
    // Put mode button together
    ButtonGroup group = new ButtonGroup();
    group.add(selectServerMode);
    group.add(selectClientMode);
    ActionListener radioListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectServerMode) {
          listeningPortInput.setEnabled(true);
          hostInput.setEnabled(false);
          connectPortInput.setEnabled(false);
          listeningPortInput.setEditable(true);
          hostInput.setEditable(false);
          connectPortInput.setEditable(false);
        } else {
          listeningPortInput.setEnabled(false);
          hostInput.setEnabled(true);
          connectPortInput.setEnabled(true);
          listeningPortInput.setEditable(false);
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
    row.setLayout(new GridLayout(0, 1));
    row.add(welcome);
    row.add(message);
    inputPanel.add(row);

    // Add server mode and client mode
    row = new JPanel();
    row.setLayout(new GridLayout(0, 1));
    
    // Server mode
    row.add(selectServerMode); // select button
    column = new JPanel();
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(Box.createHorizontalStrut(40)); // reserved space
    column.add(new JLabel("Listen on port:"));
    column.add(listeningPortInput);
    row.add(column);

    // Client mode
    row.add(selectClientMode); // select button
    column = new JPanel();
    column.setLayout(new FlowLayout(FlowLayout.LEFT));
    column.add(Box.createHorizontalStrut(40)); // reserved space
    column.add(new JLabel("Host URL:"));
    column.add(hostInput);
    row.add(column);
    
    column = new JPanel();
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
        int port;
        try {
          port = Integer.parseInt(listeningPortInput.getText().trim());
          if (port <= 0 || port >= 65536)
            throw new IllegalPortException("Illegal port number!");
        } catch (IllegalPortException e) {
          message.setText(e.getMessage());
          message.setForeground(Color.red);
          listeningPortInput.selectAll();
          listeningPortInput.requestFocus();
          continue;
        }
        /*
        try {
          new Hub();
        } catch (IOException e) {
          message.setText("Error: Can't listen on port " + port);
          message.setForeground(Color.red);
          listeningPortInput.selectAll();
          listeningPortInput.requestFocus();
          continue;
        }
        new Window();
        */
        break;
      } else {
        String host;
        int port;
        try {
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
        }
        /*
        new Window();
        */
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
}

