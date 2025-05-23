package org.vash.vate.startup;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;

public final class VTGraphicalStart
{
  private static String option;
  
  public static final void main(String[] args)
  {
    VTConsole.setLanterna(true);
    VTConsole.setGraphical(true);
    VTConsole.setRemoteIcon(true);
    
    VTClient client = new VTClient();
    VTServer server = new VTServer();
    
    if (args.length == 0)
    {
      VTConsole.initialize();
      VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
      VTConsole.print("VT>Variable-Terminal " + VT.VT_VERSION + " - Module - (c) " + VT.VT_YEAR + " wknishio@gmail.com\n" + 
      "VT>This software is under MIT license with no warranty, use at your own risk!\n");
      VTConsole.print("VT>Enter module(client as C or server as S, default:C):");
      try
      {
        option = VTConsole.readLine(true);
        if (option.toUpperCase().startsWith("S"))
        {
          server.setDaemon(false);
          // server.initialize();
          // server.configure();
          server.start();
        }
        else if (option != null)
        {
          // client.initialize();
          // client.configure();
          client.start();
        }
        else
        {
          VTRuntimeExit.exit(0);
        }
      }
      catch (Throwable e)
      {
        VTRuntimeExit.exit(0);
      }
    }
    else
    {
      int type = 0;
      for (int i = 0; i < args.length; i++)
      {
        if ("-C".equalsIgnoreCase(args[i]))
        {
          type = 1;
        }
        else if ("-S".equalsIgnoreCase(args[i]))
        {
          type = 2;
        }
        else if ("-D".equalsIgnoreCase(args[i]))
        {
          type = 3;
        }
        else if ("-H".equalsIgnoreCase(args[i]))
        {
          type = 4;
        }
        else if ("-A".equalsIgnoreCase(args[i]))
        {
          type = 5;
        }
        else
        {
          if (client.loadClientSettingsFile(args[i]))
          {
            client.start();
            return;
          }
          if (server.loadServerSettingsFile(args[i]))
          {
            server.start();
            return;
          }
        }
      }
      if (type == 1)
      {
        try
        {
          client.parseParameters(args);
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(-1);
        }
        // client.initialize();
        client.start();
      }
      else if (type == 2)
      {
        try
        {
          server.parseParameters(args);
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(-1);
        }
        // server.initialize();
        server.setDaemon(false);
        server.start();
      }
      else if (type == 3)
      {
        VTConsole.setDaemon(true);
        try
        {
          server.parseParameters(args);
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(-1);
        }
        // server.initialize();
        server.setDaemon(true);
        server.start();
      }
      else if (type == 4)
      {
        VTConsole.initialize();
        VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
        // VTConsole.print(VTHelpManager.printApplicationParametersHelp());
        VTConsole.print(VTHelpManager.printGeneralModeParameterHelp());
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
      else if (type == 5)
      {
        try
        {
          client.parseParameters(args);
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(-1);
        }
        client.setDaemon(true);
        // client.initialize();
        client.start();
      }
      else
      {
        VTConsole.initialize();
        VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
        VTConsole.print("VT>Variable-Terminal " + VT.VT_VERSION + " - Module - (c) " + VT.VT_YEAR + " wknishio@gmail.com\n" + 
        "VT>This software is under MIT license with no warranty, use at your own risk!\n");
        VTConsole.print("VT>Enter module(client as C or server as S, default:C):");
        try
        {
          option = VTConsole.readLine(true);
          if (option.toUpperCase().startsWith("S"))
          {
            server.setDaemon(false);
            try
            {
              server.parseParameters(args);
            }
            catch (Throwable e)
            {
              
            }
            // server.initialize();
            // server.configure();
            server.start();
          }
          else
          {
            try
            {
              client.parseParameters(args);
            }
            catch (Throwable e)
            {
              
            }
            // client.initialize();
            // client.configure();
            client.start();
          }
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(0);
        }
      }
    }
  }
}