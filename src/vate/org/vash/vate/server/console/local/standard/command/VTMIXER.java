package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTMIXER extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTMIXER()
  {
    this.setFullName("*VTMIXER");
    this.setAbbreviatedName("*VTMX");
    this.setFullSyntax("*VTMIXER");
    this.setAbbreviatedSyntax("*VTMX");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    message.setLength(0);
    message.append("\nVT>List of server audio mixers:\nVT>");
    message.append("\nVT>End of server audio mixers list\nVT>");
    VTSystemConsole.print(message.toString());
  }
  
  public void close()
  {
    
  }
}
