package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTDISPLAY extends VTServerStandardRemoteConsoleCommandProcessor
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
    connection.getResultWriter().write("\rVT>No graphical display devices found on server!\nVT>");
    connection.getResultWriter().flush();
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}