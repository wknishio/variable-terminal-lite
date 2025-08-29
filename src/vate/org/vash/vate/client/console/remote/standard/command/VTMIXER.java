package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.help.VTHelpManager;

public class VTMIXER extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTMIXER()
  {
    this.setFullName("*VTMIXER");
    this.setAbbreviatedName("*VTMX");
    this.setFullSyntax("*VTMIXER [SIDE]");
    this.setAbbreviatedSyntax("*VTMX [SD]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      if (parsed[1].toUpperCase().startsWith("R"))
      {
        connection.getCommandWriter().writeLine(command);
        connection.getCommandWriter().flush();
      }
      else if (parsed[1].toUpperCase().startsWith("L"))
      {
        message.setLength(0);
        message.append("\nVT>List of client audio mixers:\nVT>");
        message.append("\nVT>End of client audio mixers list\nVT>");
        VTMainConsole.print(message.toString());
      }
      else
      {
        VTMainConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      }
    }
    else
    {
      message.setLength(0);
      message.append("\nVT>List of client audio mixers:\nVT>");
      message.append("\nVT>End of client audio mixers list\nVT>");
      VTMainConsole.print(message.toString());
      connection.getCommandWriter().writeLine(command);
      connection.getCommandWriter().flush();
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}
