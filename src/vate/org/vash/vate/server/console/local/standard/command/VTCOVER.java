package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTCOVER extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTCOVER()
  {
    this.setFullName("*VTCOVER");
    this.setAbbreviatedName("*VTCV");
    this.setFullSyntax("*VTCOVER");
    this.setAbbreviatedSyntax("*VTCV");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (server.isDaemon())
    {
      
    }
    else
    {
      if (VTSystemConsole.isDaemon())
      {
        //server.enableTrayIcon();
        VTSystemConsole.print("\rVT>Server console interface enabled\nVT>");
        VTSystemConsole.setDaemon(false);
      }
      else
      {
        //server.disableTrayIcon();
        VTSystemConsole.print("\rVT>Server console interface disabled\nVT>");
        VTSystemConsole.setDaemon(true);
      }
    }
  }
  
  public void close()
  {
    
  }
}
