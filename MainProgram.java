package com.RajendraArora;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainProgram
  implements Runnable
{
  public static final int NULL = 0;
  public static final int DISCONNECTED = 1;
  public static final int DISCONNECTING = 2;
  public static final int BEGIN_CONNECT = 3;
  public static final int CONNECTED = 4;
  public static final String[] statusMessages = {
    " Error! Could not connect!", " Disconnected", 
    " Disconnecting...", " Connecting...", " Connected" };
  public static final MainProgram jchat = new MainProgram();
  public static final String END_CHAT_SESSION = new Character('\000').toString();
  public static String hostIP = "localhost";
  public static int port = 1234;
  public static int connectionStatus = 1;
  public static boolean isHost = true;
  public static String statusString = statusMessages[connectionStatus];
  public static StringBuffer toAppend = new StringBuffer("");
  public static StringBuffer toSend = new StringBuffer("");
  public static JFrame mainFrame = null;
  public static JTextArea chatText = null;
  public static JTextField chatLine = null;
  public static JPanel statusBar = null;
  public static JLabel statusField = null;
  public static JTextField statusColor = null;
  public static JTextField ipField = null;
  public static JTextField portField = null;
  public static JRadioButton hostOption = null;
  public static JRadioButton guestOption = null;
  public static JButton connectButton = null;
  public static JButton disconnectButton = null;
  public static ServerSocket hostServer = null;
  public static Socket socket = null;
  public static BufferedReader in = null;
  public static PrintWriter out = null;
  
  private static JPanel initOptionsPane()
  {
    JPanel pane = null;
    ActionAdapter buttonListener = null;
    

    JPanel optionsPane = new JPanel(new GridLayout(4, 1));
    

    pane = new JPanel(new FlowLayout(2));
    pane.add(new JLabel("Host IP:"));
    ipField = new JTextField(10);
    ipField.setText(hostIP);
    ipField.setEnabled(false);
    ipField.addFocusListener(new FocusAdapter()
    {
      public void focusLost(FocusEvent e)
      {
    	  MainProgram.ipField.selectAll();
        if (MainProgram.connectionStatus != 1) {
        	MainProgram.changeStatusNTS(0, true);
        } else {
        	MainProgram.hostIP = MainProgram.ipField.getText();
        }
      }
    });
    pane.add(ipField);
    optionsPane.add(pane);
    

    pane = new JPanel(new FlowLayout(2));
    pane.add(new JLabel("Port:"));
    portField = new JTextField(10);portField.setEditable(true);
    portField.setText(new Integer(port).toString());
    portField.addFocusListener(new FocusAdapter()
    {
      public void focusLost(FocusEvent e)
      {
        if (MainProgram.connectionStatus != 1) {
        	MainProgram.changeStatusNTS(0, true);
        } else {
          try
          {
            int temp = Integer.parseInt(MainProgram.portField.getText());
            MainProgram.port = temp;
          }
          catch (NumberFormatException nfe)
          {
        	  MainProgram.portField.setText(new Integer(MainProgram.port).toString());
        	  MainProgram.mainFrame.repaint();
          }
        }
      }
    });
    pane.add(portField);
    optionsPane.add(pane);
    

    buttonListener = new ActionAdapter()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (MainProgram.connectionStatus != 1)
        {
        	MainProgram.changeStatusNTS(0, true);
        }
        else
        {
          MainProgram.isHost = e.getActionCommand().equals("host");
          if (MainProgram.isHost)
          {
            MainProgram.ipField.setEnabled(false);
            MainProgram.ipField.setText("localhost");
            MainProgram.hostIP = "localhost";
          }
          else
          {
            MainProgram.ipField.setEnabled(true);
          }
        }
      }
    };
    ButtonGroup bg = new ButtonGroup();
    hostOption = new JRadioButton("Host", true);
    hostOption.setMnemonic(72);
    hostOption.setActionCommand("host");
    hostOption.addActionListener(buttonListener);
    guestOption = new JRadioButton("Guest", false);
    guestOption.setMnemonic(71);
    guestOption.setActionCommand("guest");
    guestOption.addActionListener(buttonListener);
    bg.add(hostOption);
    bg.add(guestOption);
    pane = new JPanel(new GridLayout(1, 2));
    pane.add(hostOption);
    pane.add(guestOption);
    optionsPane.add(pane);
    

    JPanel buttonPane = new JPanel(new GridLayout(1, 2));
    buttonListener = new ActionAdapter()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (e.getActionCommand().equals("connect")) {
          MainProgram.changeStatusNTS(3, true);
        } else {
          MainProgram.changeStatusNTS(2, true);
        }
      }
    };
    connectButton = new JButton("Connect");
    connectButton.setMnemonic(67);
    connectButton.setActionCommand("connect");
    connectButton.addActionListener(buttonListener);
    connectButton.setEnabled(true);
    disconnectButton = new JButton("Disconnect");
    disconnectButton.setMnemonic(68);
    disconnectButton.setActionCommand("disconnect");
    disconnectButton.addActionListener(buttonListener);
    disconnectButton.setEnabled(false);
    buttonPane.add(connectButton);
    buttonPane.add(disconnectButton);
    optionsPane.add(buttonPane);
    
    return optionsPane;
  }
  
  private static void initGUI()
  {
    statusField = new JLabel();
    statusField.setText(statusMessages[1]);
    statusColor = new JTextField(1);
    statusColor.setBackground(Color.red);
    statusColor.setEditable(false);
    statusBar = new JPanel(new BorderLayout());
    statusBar.add(statusColor, "West");
    statusBar.add(statusField, "Center");
    

    JPanel optionsPane = initOptionsPane();
    

    JPanel chatPane = new JPanel(new BorderLayout());
    chatText = new JTextArea(10, 20);
    chatText.setLineWrap(true);
    chatText.setEditable(false);
    chatText.setForeground(Color.blue);
    JScrollPane chatTextPane = new JScrollPane(chatText, 
      22, 
      31);
    chatLine = new JTextField();
    chatLine.setEnabled(false);
    chatLine.addActionListener(new ActionAdapter()
    {
      public void actionPerformed(ActionEvent e)
      {
        String s = MainProgram.chatLine.getText();
        if (!s.equals(""))
        {
          MainProgram.appendToChatBox("OUTGOING: " + s + "\n");
          MainProgram.chatLine.selectAll();
          

          MainProgram.sendString(s);
        }
      }
    });
    chatPane.add(chatLine, "South");
    chatPane.add(chatTextPane, "Center");
    chatPane.setPreferredSize(new Dimension(200, 200));
    

    JPanel mainPane = new JPanel(new BorderLayout());
    mainPane.add(statusBar, "South");
    mainPane.add(optionsPane, "West");
    mainPane.add(chatPane, "Center");
    

    mainFrame = new JFrame("Chat box made by: Rajendra Arora");
    mainFrame.setDefaultCloseOperation(3);
    mainFrame.setContentPane(mainPane);
    
    mainFrame.setIconImage(new ImageIcon("Jface.png").getImage());
    mainFrame.setResizable(false);
    mainFrame.setSize(mainFrame.getPreferredSize());
    mainFrame.setLocation(200, 200);
    mainFrame.pack();
    mainFrame.setVisible(true);
  }
  
  private static void changeStatusTS(int newConnectStatus, boolean noError)
  {
    if (newConnectStatus != 0) {
      connectionStatus = newConnectStatus;
    }
    if (noError) {
      statusString = statusMessages[connectionStatus];
    } else {
      statusString = statusMessages[0];
    }
    SwingUtilities.invokeLater(jchat);
  }
  
  private static void changeStatusNTS(int newConnectStatus, boolean noError)
  {
    if (newConnectStatus != 0) {
      connectionStatus = newConnectStatus;
    }
    if (noError) {
      statusString = statusMessages[connectionStatus];
    } else {
      statusString = statusMessages[0];
    }
    jchat.run();
  }
  
  private static void appendToChatBox(String s)
  {
    synchronized (toAppend)
    {
      toAppend.append(s);
    }
  }
  
  private static void sendString(String s)
  {
    synchronized (toSend)
    {
      toSend.append(s + "\n");
    }
  }
  
  private static void cleanUp()
  {
    try
    {
      if (hostServer != null)
      {
        hostServer.close();
        hostServer = null;
      }
    }
    catch (IOException e)
    {
      hostServer = null;
    }
    try
    {
      if (socket != null)
      {
        socket.close();
        socket = null;
      }
    }
    catch (IOException e)
    {
      socket = null;
    }
    try
    {
      if (in != null)
      {
        in.close();
        in = null;
      }
    }
    catch (IOException e)
    {
      in = null;
    }
    if (out != null)
    {
      out.close();
      out = null;
    }
  }
  
  public void run()
  {
    switch (connectionStatus)
    {
    case 1: 
      connectButton.setEnabled(true);
      disconnectButton.setEnabled(false);
      ipField.setEnabled(true);
      portField.setEnabled(true);
      hostOption.setEnabled(true);
      guestOption.setEnabled(true);
      chatLine.setText("");chatLine.setEnabled(false);
      statusColor.setBackground(Color.red);
      break;
    case 2: 
      connectButton.setEnabled(false);
      disconnectButton.setEnabled(false);
      ipField.setEnabled(false);
      portField.setEnabled(false);
      hostOption.setEnabled(false);
      guestOption.setEnabled(false);
      chatLine.setEnabled(false);
      statusColor.setBackground(Color.orange);
      break;
    case 4: 
      connectButton.setEnabled(false);
      disconnectButton.setEnabled(true);
      ipField.setEnabled(false);
      portField.setEnabled(false);
      hostOption.setEnabled(false);
      guestOption.setEnabled(false);
      chatLine.setEnabled(true);
      statusColor.setBackground(Color.green);
      break;
    case 3: 
      connectButton.setEnabled(false);
      disconnectButton.setEnabled(false);
      ipField.setEnabled(false);
      portField.setEnabled(false);
      hostOption.setEnabled(false);
      guestOption.setEnabled(false);
      chatLine.setEnabled(false);
      chatLine.grabFocus();
      statusColor.setBackground(Color.orange);
    }
    ipField.setText(hostIP);
    portField.setText(new Integer(port).toString());
    hostOption.setSelected(isHost);
    guestOption.setSelected(!isHost);
    statusField.setText(statusString);
    chatText.append(toAppend.toString());
    toAppend.setLength(0);
    
    mainFrame.repaint();
  }
  
  public static void main(String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception localException) {}
    initGUI();
    for (;;)
    {
      try
      {
        Thread.sleep(10L);
      }
      catch (InterruptedException localInterruptedException) {}
      switch (connectionStatus)
      {
      case 3: 
        try
        {
          if (isHost)
          {
            hostServer = new ServerSocket(port);
            socket = hostServer.accept();
          }
          else
          {
            socket = new Socket(hostIP, port);
          }
          in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
          out = new PrintWriter(socket.getOutputStream(), true);
          changeStatusTS(4, true);
        }
        catch (IOException e)
        {
          cleanUp();
          changeStatusTS(1, false);
        }
        break;
      case 4: 
        try
        {
          if (toSend.length() != 0)
          {
            out.print(toSend);out.flush();
            toSend.setLength(0);
            changeStatusTS(0, true);
          }
          if (in.ready())
          {
            String s = in.readLine();
            if ((s != null) && (s.length() != 0)) {
              if (s.equals(END_CHAT_SESSION))
              {
                changeStatusTS(2, true);
              }
              else
              {
                appendToChatBox("INCOMING: " + s + "\n");
                changeStatusTS(0, true);
              }
            }
          }
        }
        catch (IOException e)
        {
          cleanUp();
          changeStatusTS(1, false);
        }
        break;
      case 2: 
        out.print(END_CHAT_SESSION);out.flush();
        

        cleanUp();
        changeStatusTS(1, true);
      }
    }
  }
}
