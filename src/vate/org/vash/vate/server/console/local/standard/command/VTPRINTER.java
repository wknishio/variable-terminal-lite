package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTPRINTER extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTPRINTER()
  {
    this.setFullName("*VTPRINTER");
    this.setAbbreviatedName("*VTPR");
    this.setFullSyntax("*VTPRINTER");
    this.setAbbreviatedSyntax("*VTPR");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    VTMainConsole.print("\rVT>No print services found on server!\nVT>");
  }
  
  public void close()
  {
    
  }
}
