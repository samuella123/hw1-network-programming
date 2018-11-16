package client.net;

import java.io.*;
import java.net.*;
import client.view.*;
import common.*;

/**
 * The ServerConnection class is responsible for communicating the server. Given the ip and port
 * to the <code>connect</code> method, it trys to get connected and then prints the result with
 * <code>client.view.SafeOutput</code> given from the controller. After establishing a connection,
 * We create a <code>client.net.ServerConnection.Listener</code> class on a separete thread in order
 * to take care of the responses of the server and printing them using <code>client.view.SafeOutput</code>.
 * there are 2 staic parameters that should be initialised, <code>TIMEOUT_TIME_HOUR</code> and
 * <code>TIMEOUT_TIME_MIN</code>.
 * @see client.view.SafeOutput
 * @see client.net.ServerConnection.Listener
 */
public class ServerConnection
{

    private static int TIMEOUT_TIME_HOUR = 1500000; //the time that the connection can be open whithout any message being transferred.
    private static int TIMEOUT_TIME_MIN = 30000; //the time that we wait for the server to respond to our connection request.

    private Socket serverSocket;
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private volatile Boolean isConnected = false;

    private SafeOutput safeOut;

    /**
     * It is used by other classes to check whether we are connected to server
     * or not.
     * @return A Bloolean.
     */
    public Boolean getConnected()
    {
      return isConnected;
    }

    /**
     * Creates a new class and connects to the server. Also starts a listener thread for
     * receiving messages from server and printing them.
     *
     * @param host IP address of the server.
     * @param port Server's port number.
     * @param safeOut is the <code>client.view.SafeOutput</code> reference for printing results.
     */
    public void connect(String host, int port, SafeOutput safeOut)
    {
        try
        {
          this.serverSocket = new Socket();
          serverSocket.connect(new InetSocketAddress(host, port), TIMEOUT_TIME_MIN);
          serverSocket.setSoTimeout(TIMEOUT_TIME_HOUR);

          this.isConnected = true;
          Boolean flush = true;

          toServer = new PrintWriter(serverSocket.getOutputStream(), flush);
          fromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        }
        catch(IOException ex)
        {
          safeOut.printResult("Unable to connect to the server.");
        }

        safeOut.printResult("Successfully connected to the game server.");

        new Thread(new Listener(safeOut)).start();
    }

    /**
     * Sends the message to the server.
     *
     * @param inp The message wants to be sent.
     */
    public void sendMessage(String inp)
    {
        toServer.println(inp);
    }

    /**
     * It will closes the socket and our state changes to not connected.
     */
    public void disconnect()
    {
        try
        {
          toServer.println("DISCONNECT");
          serverSocket.close();
        }
        catch(IOException ex)
        {
          safeOut.printResult("Unable to close the socket.");
        }
        serverSocket = null;
        isConnected = false;
    }

    /**
    * We create This class on a separete thread in order to take care of the
    * responses of the server and printing them using <code>client.view.SafeOutput</code>.
    * @see client.view.SafeOutput
    */
    private class Listener implements Runnable
    {
        private final SafeOutput safeOut;

        private Listener(SafeOutput safeOut)
        {
            this.safeOut = safeOut;
        }

        /**
        * read line from the ongoing connection and observe the
        * responses of the server and printing them using <code>client.view.SafeOutput</code>.
        * @see client.view.SafeOutput
        */
        @Override
        public void run()
        {
            try
            {
                while(true)
                {
                    safeOut.printResult(reviseMessage(fromServer.readLine()));
                }
            }
            catch (Exception ex)
            {
                safeOut.printResult("Connection terminated.");
            }
        }

        /**
        * This method trims the message comming from the server and prints it.
        * it splits the message <code>Constants.MSG_DELIMETER</code> and shows it.
        */
        private String reviseMessage(String inp)
        {
          String[] msgParts = inp.split(Constants.MSG_DELIMETER);
          if(msgParts.length == 1)
            return inp;
          else if(!msgParts[0].equals(MsgType.RESULT.toString()))
          {
            return msgParts[0] + ": " + msgParts[1];
          }
          else
          {
            String[] msgParts2 = msgParts[1].split("\\s+");
            return msgParts[0] + ": " + msgParts2[0] + " attempts remaining: " + msgParts2[1] + " score: " + msgParts2[2];
          }
        }
    }
}
