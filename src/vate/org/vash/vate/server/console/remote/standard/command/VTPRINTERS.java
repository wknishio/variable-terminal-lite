package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTPRINTERS extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTPRINTERS()
  {
    this.setFullName("*VTPRINTERS");
    this.setAbbreviatedName("*VTPRS");
    this.setFullSyntax("*VTPRINTERS [NUMBER]");
    this.setAbbreviatedSyntax("*VTPRS [NU]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\nVT>No print services found on server!\nVT>");
    connection.getResultWriter().flush();
  }
  
  public void close()
  {
    
  }
}