package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTDISPLAYS extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTDISPLAYS()
  {
    this.setFullName("*VTDISPLAYS");
    this.setAbbreviatedName("*VTDP");
    this.setFullSyntax("*VTDISPLAYS");
    this.setAbbreviatedSyntax("*VTDP");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    message.setLength(0);
    message.append("\rVT>No graphical display devices found on server!\nVT>");
    VTConsole.print(message.toString());
  }
  
  public void close()
  {
    
  }
}
