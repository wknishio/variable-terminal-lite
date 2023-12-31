package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTPRINTERS extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTPRINTERS()
  {
    this.setFullName("*VTPRINTERS");
    this.setAbbreviatedName("*VTPR");
    this.setFullSyntax("*VTPRINTERS [NUMBER]");
    this.setAbbreviatedSyntax("*VTPR [NU]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\nVT>No print services found on server!\nVT>");
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