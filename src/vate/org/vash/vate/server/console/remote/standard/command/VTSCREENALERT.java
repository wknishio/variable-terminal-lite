package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSCREENALERT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTSCREENALERT()
  {
    this.setFullName("*VTSCREENALERT");
    this.setAbbreviatedName("*VTSA");
    this.setFullSyntax("*VTSCREENALERT <[TITLE;]ALERT> [DISPLAY]");
    this.setAbbreviatedSyntax("*VTSA <[TI;]AL> [DP]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\nVT>Graphical alert not supported in server!\nVT>");
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
