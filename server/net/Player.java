package server.net;

import java.io.*;
import java.net.*;
import java.util.*;

import server.controller.*;


/**
 * The Player class is responsible for running the whole game for the assinged
 * client and ask the game the inputs from client connection and send the asnwer
 * back to the client. It creates a controller and passes the string comming from
 * client to it.
 @see server.controller.Controller
 */
public class Player implements Runnable
{
  private final GameServer server;
  private final Socket playerSocket;
  private BufferedReader fromPlayer;
  private PrintWriter toPlayer;
  private Boolean isConnected;
  private int connectedPlayersNum = 0;
  private Controller controller;

  /**
   * By creating this player it will handle communication with a player connected to the socket.
   * @param playerSocket The socket that the player is connected through it to the server.
   * @param server The GameServer class reference
   */
  Player(GameServer server, Socket playerSocket)
  {
      this.server = server;
      this.playerSocket = playerSocket;
      isConnected = true;
  }

  @Override
  public void run()
  {
      try
      {
          Boolean flush = true;
          fromPlayer = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
          toPlayer = new PrintWriter(playerSocket.getOutputStream(), flush);
      }
      catch (Exception ex)
      {
          System.err.println("Player socket initialization failure.");
      }

      try
      {
          controller = new Controller(GameServer.WORDS_FILE_PATH);
      }
      catch (Exception ex)
      {
          System.err.println("Hangman words file initialization failure.");
      }

      toPlayer.println(controller.getResult());
      this.connectedPlayersNum++;
      System.out.println("A new player connected.");

      while(isConnected)
      {
        try
        {
          String inp = fromPlayer.readLine();
          if(inp.equals("DISCONNECT"))
          {
            disconnect();
            break;
          }
          controller.askTheGame(inp);
          toPlayer.println(controller.getResult());
        }
        catch (Exception ex)
        {
          System.err.println("A Player's socket is not working anymore.");
          break;
        }
      }

    }

    private void disconnect()
    {
        try
        {
            playerSocket.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        isConnected = false;
        server.removePlayer(this);
        this.connectedPlayersNum--;
        System.out.println("A player disconnected.");
    }
}
