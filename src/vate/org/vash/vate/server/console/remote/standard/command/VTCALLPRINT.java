package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTCALLPRINT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTCALLPRINT()
  {
    this.setFullName("*VTCALLPRINT");
    this.setAbbreviatedName("*VTCPR");
    this.setFullSyntax("*VTCALLPRINT <FILE>");
    this.setAbbreviatedSyntax("*VTCPR <FL>");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\rVT>Print dialog operation not supported in server!\nVT>");
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
