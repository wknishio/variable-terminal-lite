package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSCREENSHOT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTSCREENSHOT()
  {
    this.setFullName("*VTSCREENSHOT");
    this.setAbbreviatedName("*VTSCS");
    this.setFullSyntax("*VTSCREENSHOT [MODE] [DISPLAY]");
    this.setAbbreviatedSyntax("*VTSCS [MD] [DP]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\nVT>Screen capture not supported in server!\nVT>");
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
