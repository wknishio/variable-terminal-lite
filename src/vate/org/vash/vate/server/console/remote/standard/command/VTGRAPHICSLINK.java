package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.VT;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTGRAPHICSLINK extends VTServerStandardRemoteConsoleCommandProcessor
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
    session.getConnection().getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_LINK_SESSION_UNSTARTED);
    session.getConnection().getGraphicsControlDataOutputStream().flush();
    session.getConnection().getGraphicsControlDataInputStream().read();
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}
