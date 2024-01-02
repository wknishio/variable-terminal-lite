package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTPRINTERS extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTPRINTERS()
  {
    this.setFullName("*VTPRINTERS");
    this.setAbbreviatedName("*VTPR");
    this.setFullSyntax("*VTPRINTERS");
    this.setAbbreviatedSyntax("*VTPR");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    VTConsole.print("\rVT>No print services found on server!\nVT>");
  }
  
  public void close()
  {
    
  }
}
