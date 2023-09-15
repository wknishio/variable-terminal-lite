package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTMIXERS extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTMIXERS()
  {
    this.setFullName("*VTMIXERS");
    this.setAbbreviatedName("*VTMX");
    this.setFullSyntax("*VTMIXERS");
    this.setAbbreviatedSyntax("*VTMX");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    message.setLength(0);
    message.append("\nVT>List of server audio mixers:\nVT>");
    message.append("\nVT>End of server audio mixers list\nVT>");
    VTConsole.print(message.toString());
  }
  
  public void close()
  {
    
  }
}
