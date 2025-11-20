package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTMAIL extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTMAIL()
  {
    this.setFullName("*VTMAIL");
    this.setAbbreviatedName("*VTML");
    this.setFullSyntax("*VTMAIL [URI]");
    this.setAbbreviatedSyntax("*VTML [UR]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\rVT>Mail operation not supported in server!\nVT>");
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
