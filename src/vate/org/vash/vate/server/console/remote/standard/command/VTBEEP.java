package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.nativeutils.VTMainNativeUtils;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTBEEP extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTBEEP()
  {
    this.setFullName("*VTBEEP");
    this.setAbbreviatedName("*VTBP");
    this.setFullSyntax("*VTBEEP [HERTZ TIME] [MIXER]");
    this.setAbbreviatedSyntax("*VTBP [HZ TM] [MX]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      VTMainConsole.bell();
      connection.getResultWriter().write("\rVT>Beep played on server!\nVT>");
      connection.getResultWriter().flush();
    }
    else if (parsed.length >= 3)
    {
      try
      {
        if (VTMainNativeUtils.beep(Integer.parseInt(parsed[1]), Integer.parseInt(parsed[2]), false, session.getExecutorService()))
        {
          connection.getResultWriter().write("\rVT>Beep is playing on server!\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Beep is not playing on server!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      catch (NumberFormatException e)
      {
        connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
      catch (Throwable e)
      {
        connection.getResultWriter().write("\rVT>Beep is not playing on server!\nVT>");
        connection.getResultWriter().flush();
      }
    }
    else
    {
      connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      connection.getResultWriter().flush();
    }
  }
  
  public void close()
  {
    
  }
  public boolean remote()
  {
    return false;
  }
}
