package org.vash.vate.server.startup;

// import org.vash.vate.VT;

import org.vash.vate.VT;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;

public class VTServerGraphicalStart
{
  public static void main(String[] args)
  {
    boolean help = false;
    boolean daemon = false;
    
    VTConsole.setLanterna(true);
    VTConsole.setGraphical(true);
    VTConsole.setRemoteIcon(true);
    // VTConsole.setCommandEcho(false);
    
    if (args.length >= 1)
    {
      for (int i = 0; i < args.length; i++)
      {
        if ("-H".equalsIgnoreCase(args[i]))
        {
          help = true;
        }
        if ("-D".equalsIgnoreCase(args[i]))
        {
          daemon = true;
        }
      }
      if (help && !daemon)
      {
        VTConsole.initialize();
        VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Server - Console");
        VTConsole.print(VTHelpManager.printServerModeParametersHelp());
        VTConsole.print(VTHelpManager.printConnnectionParametersHelp());
        if (VTConsole.isGraphical())
        {
          try
          {
            VTConsole.readLine();
          }
          catch (Throwable e)
          {
            
          }
        }
        VTRuntimeExit.exit(0);
      }
      VTConsole.setDaemon(daemon);
      VTServer server = new VTServer();
      try
      {
        server.parseParameters(args);
      }
      catch (Throwable e)
      {
        VTRuntimeExit.exit(-1);
      }
      // server.initialize();
      server.setDaemon(daemon);
      server.start();
    }
    else
    {
      VTConsole.setDaemon(daemon);
      VTServer server = new VTServer();
      // server.initialize();
      // server.configure();
      server.setDaemon(daemon);
      server.start();
    }
  }
}