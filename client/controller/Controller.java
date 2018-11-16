package client.controller;

import java.math.*;
import java.lang.*;
import java.util.regex.*;
import client.view.*;
import client.net.*;
import common.*;

/**
 * The Controller class is responsible for handling input commands passed from
 * <code>client.view.Interpreter</code>. Then it calls <code>client.net.ServerConnection</code>
 * to send the processed texts to the seerver. It compares input with types of valid commands which are
 * available in <code>client.controller.CmdType</code>. If its not matched, an error message will be shown.
 * for the commands that needs communication with the server we use <code>common.MsgType</code>
 * We have <code>client.view.SafeOutput</code> reference for printing error and exceptions.
 * defiend message types.
 * @see client.net.ServerConnection
 * @see client.controller.CmdType
 * @see common.MsgType
 * @see client.view.SafeOutput
 */
public class Controller
{
  private SafeOutput safeOut;
  private final ServerConnection serverCon = new ServerConnection();

  /**
   * The constructor saves <code>client.view.SafeOutput</code> reference.
   * @param so is the <code>client.view.SafeOutput</code> reference.
   * @see client.view.SafeOutput
   */
  public Controller(SafeOutput so)
  {
    safeOut = so;
  }

  /**
   * Validates the input IP by a regular expression.
   * @param inp It is the input String containing the IP.
   * @return returns a boolean.
   */
  private Boolean validateIP(String inp)
  {
    Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    Matcher m = p.matcher(inp);
    return m.matches();
  }

  /**
   * Validates the input Port by boundings from 1024 to 65535
   * @param inp It is the input String containing the Port.
   * @return returns a boolean.
   */
  private Boolean validatePORT(String inp)
  {
    try
    {
      int prt = Integer.parseInt(inp);
      if( (prt < 1024) || (prt > 65535) )
        throw new Exception();
    }
    catch (Exception e)
    {
      return false;
    }
    return true;
  }

  /**
   * Splites the input string and compares first part with <code>client.controller.CmdType</code>
   * types. and communicates with the <code>client.net.ServerConnection</code> by <code>common.MsgType</code>.
   * @param cmd It is incomming command
   * @see client.net.ServerConnection
   * @see common.MsgType
   */
  public void handleCMD(String cmd)
  {
    String[] splited = cmd.split(" ");
    if(splited[0].equals(CmdType.CONNECT.toString()))
    {
      if(serverCon.getConnected())
      {
        safeOut.printResult("You are already connected to a game server, Disconnect first.");
        return;
      }
      if(splited.length<3)
      {
        safeOut.printResult("Not enough arguments in: " + splited[0]);
        return;
      }
      if(splited.length>3)
      {
        safeOut.printResult("Too much arguments in: " + splited[0]);
        return;
      }
      if(!validateIP(splited[1]))
      {
        safeOut.printResult("IPv4 format is not correct in: " + splited[0]);
        return;
      }
      if(!validatePORT(splited[2]))
      {
        safeOut.printResult("PORT is not correct in: " + splited[0]);
        return;
      }
      //connect code
      serverCon.connect(splited[1],Integer.parseInt(splited[2]),safeOut);

    }
    else if(splited[0].equals(CmdType.START.toString()))
    {
      if(!serverCon.getConnected())
      {
        safeOut.printResult("First you should connect to the server: " + splited[0]);
        return;
      }
      if(splited.length>1)
      {
        safeOut.printResult("Too much arguments in: " + splited[0]);
        return;
      }
      //start code
      serverCon.sendMessage(MsgType.START.toString());
    }
    else if(splited[0].equals(CmdType.FINISH.toString()))
    {
      if(!serverCon.getConnected())
      {
        safeOut.printResult("First you should connect to the server: " + splited[0]);
        return;
      }
      if(splited.length>1)
      {
        safeOut.printResult("Too much arguments in: " + splited[0]);
        return;
      }
      //finish code
      serverCon.sendMessage(MsgType.FINISH.toString());
    }
    else if(splited[0].equals(CmdType.DISCONNECT.toString()))
    {
      if(!serverCon.getConnected())
      {
        safeOut.printResult("You are not connected to any server: " + splited[0]);
        return;
      }
      if(splited.length>1)
      {
        safeOut.printResult("Too much arguments in: " + splited[0]);
        return;
      }
      //finish code
      serverCon.disconnect();
    }
    else if(splited[0].equals(CmdType.GUESS.toString()))
    {
      if(!serverCon.getConnected())
      {
        safeOut.printResult("First you should connect to the server: " + splited[0]);
        return;
      }
      if(splited.length<2)
      {
        safeOut.printResult("Not enough arguments in: " + splited[0]);
        return;
      }
      if(splited.length>2)
      {
        safeOut.printResult("Too much arguments in: " + splited[0]);
        return;
      }
      //guess code
      serverCon.sendMessage(MsgType.GUESS.toString()+Constants.MSG_DELIMETER+splited[1]);
    }
    else
    {
      safeOut.printResult("This command is unknown: " + splited[0]);
    }
  }

}
