package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTMIXERS extends VTServerStandardRemoteConsoleCommandProcessor
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
        message.setLength(0);
        message.append("\nVT>List of server audio mixers:\nVT>");
        message.append("\nVT>End of server audio mixers list\nVT>");
        connection.getResultWriter().write(message.toString());
        connection.getResultWriter().flush();
      }
      else if (parsed[1].toUpperCase().startsWith("L"))
      {
        
      }
    }
    else
    {
      message.setLength(0);
      message.append("\nVT>List of server audio mixers:\nVT>");
      message.append("\nVT>End of server audio mixers list\nVT>");
      connection.getResultWriter().write(message.toString());
      connection.getResultWriter().flush();
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
