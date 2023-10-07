package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.reflection.VTReflectionUtils;

public class VTGRAPHICSLINK extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTGRAPHICSLINK()
  {
    this.setFullName("*VTGRAPHICSLINK");
    this.setAbbreviatedName("*VTGL");
    this.setFullSyntax("*VTGRAPHICSLINK [MODE]");
    this.setAbbreviatedSyntax("*VTGL [MD]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      VTConsole.print("\nVT>Remote graphics link start on client failed!\nVT>");
      return;
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}
