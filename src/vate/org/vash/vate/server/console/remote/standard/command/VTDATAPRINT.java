package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTDATAPRINT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTDATAPRINT()
  {
    this.setFullName("*VTDATAPRINT");
    this.setAbbreviatedName("*VTDPR");
    this.setFullSyntax("*VTDATAPRINT [MODE] [DATA] [PRINTER]");
    this.setAbbreviatedSyntax("*VTDPR [MD] [DT] [PR]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\nVT>Data printing not supported in server!\nVT>");
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