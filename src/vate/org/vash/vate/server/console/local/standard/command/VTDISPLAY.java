package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTDISPLAY extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTDISPLAY()
  {
    this.setFullName("*VTDISPLAY");
    this.setAbbreviatedName("*VTDP");
    this.setFullSyntax("*VTDISPLAY");
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
