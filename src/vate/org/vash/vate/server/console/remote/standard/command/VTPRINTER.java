package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTPRINTER extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTPRINTER()
  {
    this.setFullName("*VTPRINTER");
    this.setAbbreviatedName("*VTPR");
    this.setFullSyntax("*VTPRINTER [PRINTER]");
    this.setAbbreviatedSyntax("*VTPR [PR]");
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