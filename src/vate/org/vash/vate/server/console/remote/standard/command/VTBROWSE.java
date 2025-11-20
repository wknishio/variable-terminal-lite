package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTBROWSE extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTBROWSE()
  {
    this.setFullName("*VTBROWSE");
    this.setAbbreviatedName("*VTBR");
    this.setFullSyntax("*VTBROWSE <URI>");
    this.setAbbreviatedSyntax("*VTBR <UR>");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\rVT>Browse operation not supported in server!\nVT>");
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
