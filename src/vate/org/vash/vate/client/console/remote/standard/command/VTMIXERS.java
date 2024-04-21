package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;

public class VTMIXERS extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTMIXERS()
  {
    this.setFullName("*VTMIXERS");
    this.setAbbreviatedName("*VTMX");
    this.setFullSyntax("*VTMIXERS [SIDE]");
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
        VTConsole.print(message.toString());
      }
      else
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      }
    }
    else
    {
      message.setLength(0);
      message.append("\nVT>List of client audio mixers:\nVT>");
      message.append("\nVT>End of client audio mixers list\nVT>");
      VTConsole.print(message.toString());
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
