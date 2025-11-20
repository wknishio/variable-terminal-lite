package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSCREENSHOT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTSCREENSHOT()
  {
    this.setFullName("*VTSCREENSHOT");
    this.setAbbreviatedName("*VTSS");
    this.setFullSyntax("*VTSCREENSHOT [MODE] [DISPLAY]");
    this.setAbbreviatedSyntax("*VTSS [MD] [DP]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\rVT>Screen capture not supported in server!\nVT>");
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
