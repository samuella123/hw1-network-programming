package server.net;

import java.io.*;
import java.util.*;
import java.net.*;


/**
 * The GameServer class is responsible for communicating the clients. Given the port
 * to the main function argument, it listens to all incomming connections and creates
 * a <code>server.net.Player</code> object for each of them and runs it on a seperate thread.
 * @see server.net.Player
 */
public class GameServer
{
  private static final int LINGER_TIME = 5000; //socket linger time
  private static final int TIMEOUT_TIME = 1500000; //socket timeout time
  public static final String WORDS_FILE_PATH = "words.txt";
  private int port = 8080; //socket port number (default value)
  private ArrayList<Player> players = new ArrayList<>(); // We put all player objects in an array just in case.

  /**
   * @param args uses one cmd argument, the port number. by default it is 8080
   */
  public static void main(String[] args)
  {
      GameServer server = new GameServer();

      if(server.validatePort(args))
        server.port = Integer.parseInt(args[0]);

      server.run();
  }

  /**
  * Listens to all connections on the specified port and creates a player object for each of them and runs it
  * on a seperate thread.
  */
  private void run()
  {
      try
      {
          ServerSocket listeningSocket = new ServerSocket(this.port);
          System.out.println("Server is listening on port: "+ port + " successfully.");
          while(true)
          {
              Socket playerSocket = listeningSocket.accept();
              playerSocket.setSoLinger(true, LINGER_TIME);
              playerSocket.setSoTimeout(TIMEOUT_TIME);

              Player player = new Player(this, playerSocket);
              synchronized(players)
              {
                  players.add(player);
              }

              Thread playerThread = new Thread(player);
              playerThread.setPriority(Thread.MAX_PRIORITY);
              playerThread.start();
          }
      }
      catch (IOException e)
      {
          System.err.println("Server failure.");
      }
  }

  /**
   * The player which is disconnected should be removed from our players array.
   * @param player is the object of a player
   * @return no return
   */
  void removePlayer(Player player)
  {
      synchronized (players)
      {
          players.remove(player);
      }
  }

  /**
  * Validates the input port number. It should be a positive integer more than 1024 and less than 65535.
  * @param arg the port number which is going to be validated
  * @return a boolean result
  */
  private Boolean validatePort(String[] arg)
  {
    if(arg.length==0)
      return false;

    try
    {
      int prt = Integer.parseInt(arg[0]);
      if( (prt < 1024) || (prt > 65535) )
        throw new Exception();
    }
    catch (Exception e)
    {
      System.err.println("The entered port number is corrupt. Going on with the default.");
      return false;
    }

    return true;
  }

}
