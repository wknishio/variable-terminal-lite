package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTPRINTDATA extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTPRINTDATA()
  {
    this.setFullName("*VTPRINTDATA");
    this.setAbbreviatedName("*VTPD");
    this.setFullSyntax("*VTPRINTDATA [MODE] [DATA] [PRINTER]");
    this.setAbbreviatedSyntax("*VTPD [MD] [DT] [PR]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\rVT>Data printing not supported in server!\nVT>");
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