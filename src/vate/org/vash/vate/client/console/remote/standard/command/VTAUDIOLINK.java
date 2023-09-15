package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

public class VTAUDIOLINK extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTAUDIOLINK()
  {
    this.setFullName("*VTAUDIOLINK");
    this.setAbbreviatedName("*VTAL");
    this.setFullSyntax("*VTAUDIOLINK [MODE] [SIDE/TYPE/MIXER] [.]");
    this.setAbbreviatedSyntax("*VTAL [MD] [SD/TP/MX] [.]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    try
    {
      VTConsole.print("\nVT>Remote audio link start on client failed!\nVT>");
      return;
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
      throw new Exception(t);
    }
  }
  
  public void close()
  {
    
  }
}
