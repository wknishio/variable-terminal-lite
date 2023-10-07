package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTDISPLAYS extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTDISPLAYS()
  {
    this.setFullName("*VTDISPLAYS");
    this.setAbbreviatedName("*VTDPS");
    this.setFullSyntax("*VTDISPLAYS");
    this.setAbbreviatedSyntax("*VTDPS");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\nVT>No graphical display devices found on server!\nVT>");
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