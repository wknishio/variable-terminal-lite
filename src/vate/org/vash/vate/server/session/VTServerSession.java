package org.vash.vate.server.session;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VTSystem;
import org.vash.vate.ping.VTNanoPingListener;
import org.vash.vate.ping.VTNanoPingService;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.console.remote.VTServerRemoteConsoleReader;
//import org.vash.vate.server.console.shell.VTServerShellErrorWriter;
import org.vash.vate.server.console.shell.VTServerShellExitListener;
import org.vash.vate.server.console.shell.VTServerShellOutputWriter;
import org.vash.vate.server.filesystem.VTServerFileModifyOperation;
import org.vash.vate.server.filesystem.VTServerFileScanOperation;
import org.vash.vate.server.filesystem.VTServerFileSystemRootsResolver;
import org.vash.vate.server.filetransfer.VTFileTransferServer;
import org.vash.vate.server.network.VTServerHostResolver;
import org.vash.vate.server.network.VTServerNetworkInterfaceResolver;
import org.vash.vate.server.opticaldrive.VTServerOpticalDriveOperation;
import org.vash.vate.server.runtime.VTServerRuntimeExecutor;
import org.vash.vate.shell.adapter.VTShellAdapter;
import org.vash.vate.shell.adapter.VTShellProcessor;
import org.vash.vate.socket.remote.VTRemotePipedSocketFactory;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;
import org.vash.vate.tunnel.connection.VTTunnelConnection;
import org.vash.vate.tunnel.connection.VTTunnelConnectionHandler;

public class VTServerSession
{
  private boolean stoppingShell;
  private boolean restartingShell;
  private boolean echoCommands;
  private int echoState;
  private long sessionLocalNanoDelay;
  private long sessionRemoteNanoDelay;
  private VTShellAdapter shellAdapter;
  private String user;
  private VTServer server;
  private VTServerConnection connection;
  private VTServerRemoteConsoleReader clientReader;
  private VTServerShellOutputWriter shellOutputWriter;
  private VTServerShellExitListener shellExitListener;
  private VTFileTransferServer fileTransferServer;
  private VTServerRuntimeExecutor runtimeExecutor;
  private VTServerFileScanOperation fileScanOperation;
  private VTServerFileModifyOperation fileModifyOperation;
  private VTServerHostResolver hostResolver;
  private VTServerNetworkInterfaceResolver networkInterfaceResolver;
  private VTServerOpticalDriveOperation opticalDriveOperation;
  private VTServerSessionListViewer connectionListViewer;
  private VTServerFileSystemRootsResolver fileSystemRootsResolver;
  private VTTunnelConnectionHandler tunnelsHandler;
  private VTNanoPingService pingServiceClient;
  private VTNanoPingService pingServiceServer;
  private Collection<Closeable> sessionCloseables;
  private ExecutorService executorService;
  
  public VTServerSession(VTServer server, VTServerConnection connection)
  {
    this.server = server;
    this.connection = connection;
    this.executorService = server.getExecutorService();
    this.sessionCloseables = new ConcurrentLinkedQueue<Closeable>();
    this.shellAdapter = new VTShellAdapter(executorService);
  }
  
  public void initialize()
  {
    this.setEchoState(0);
    this.setEchoCommands(false);
    this.shellAdapter.setShellEncoding(null);
    this.stoppingShell = false;
    this.restartingShell = false;
    this.echoCommands = false;
    this.clientReader = new VTServerRemoteConsoleReader(this);
    this.shellOutputWriter = new VTServerShellOutputWriter(this);
    this.shellExitListener = new VTServerShellExitListener(this);
    this.fileTransferServer = new VTFileTransferServer(this);
    this.runtimeExecutor = new VTServerRuntimeExecutor(this);
    this.fileScanOperation = new VTServerFileScanOperation(this);
    this.fileModifyOperation = new VTServerFileModifyOperation(this);
    this.opticalDriveOperation = new VTServerOpticalDriveOperation(this);
    this.hostResolver = new VTServerHostResolver(this);
    this.networkInterfaceResolver = new VTServerNetworkInterfaceResolver(this);
    this.connectionListViewer = new VTServerSessionListViewer(this);
    this.fileSystemRootsResolver = new VTServerFileSystemRootsResolver(this);
    this.tunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(executorService, sessionCloseables, connection.getSecureRandom()));
    this.pingServiceClient = new VTNanoPingService(server.getPingIntervalMilliseconds(), server.getPingIntervalMilliseconds() / 2, false, executorService);
    this.pingServiceClient.addListener(new VTNanoPingListener()
    {
      public void pingObtained(long nanoDelay)
      {
        sessionLocalNanoDelay = nanoDelay;
      }
    });
    this.pingServiceServer = new VTNanoPingService(server.getPingIntervalMilliseconds(), 0, true, executorService);
    this.pingServiceServer.addListener(new VTNanoPingListener()
    {
      public void pingObtained(long nanoDelay)
      {
        sessionRemoteNanoDelay = nanoDelay;
      }
    });
    setShellType(VTShellProcessor.SHELL_TYPE_PROCESS);
    setShellBuilder(null, null, null);
    clientReader.setStopped(false);
    shellExitListener.setStopped(false);
    tunnelsHandler.getConnection().setControlInputStream(connection.getTunnelControlDataInputStream());
    tunnelsHandler.getConnection().setControlOutputStream(connection.getTunnelControlDataOutputStream());
    tunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
    tunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
    pingServiceClient.setInputStream(connection.getPingClientInputStream());
    pingServiceClient.setOutputStream(connection.getPingClientOutputStream());
    pingServiceServer.setInputStream(connection.getPingServerInputStream());
    pingServiceServer.setOutputStream(connection.getPingServerOutputStream());
  }
  
  public ExecutorService getExecutorService()
  {
    return executorService;
  }
  
  public void addSessionCloseable(Closeable value)
  {
    sessionCloseables.add(value);
  }
  
  public boolean removeSessionCloseable(Closeable value)
  {
    return sessionCloseables.remove(value);
  }
  
  public void clearSessionCloseables()
  {
    sessionCloseables.clear();
  }
  
  public boolean setShellEncoding(String shellEncoding)
  {
    shellOutputWriter.setShellEncoding(shellEncoding);
    return shellAdapter.setShellEncoding(shellEncoding);
  }
  
  public void setShellBuilder(String command, String[] names, String[] values)
  {
    shellAdapter.setShellBuilder(command, names, values);
  }
  
  public long getLocalNanoDelay()
  {
    return sessionLocalNanoDelay;
  }
  
  public long getRemoteNanoDelay()
  {
    return sessionRemoteNanoDelay;
  }
  
  public String getUser()
  {
    return user;
  }
  
  public void setUser(String user)
  {
    this.user = user;
  }
  
  public boolean isStoppingShell()
  {
    return stoppingShell;
  }
  
  public void setStoppingShell(boolean stoppingShell)
  {
    this.stoppingShell = stoppingShell;
  }
  
  public boolean isRestartingShell()
  {
    return restartingShell;
  }
  
  public void setRestartingShell(boolean restartingShell)
  {
    this.restartingShell = restartingShell;
  }
  
  public VTServerRemoteConsoleReader getClientReader()
  {
    return clientReader;
  }
  
  public void setRuntimeBuilderWorkingDirectory(File runtimeDirectory)
  {
    this.runtimeExecutor.setRuntimeBuilderWorkingDirectory(runtimeDirectory);
  }
  
  public VTShellProcessor getShellProcessor()
  {
    return shellAdapter.getShellProcessor();
  }
  
  public Map<String, String> getShellEnvironment()
  {
    return shellAdapter.getShellEnvironment();
  }
  
  public File getShellDirectory()
  {
    return shellAdapter.getShellDirectory();
  }
  
  public boolean setShellDirectory(File shellDirectory)
  {
    return shellAdapter.setShellDirectory(shellDirectory);
  }
  
  public InputStream getShellInputStream()
  {
    return shellAdapter.getShellInputStream();
  }
  
  public OutputStream getShellOutputStream()
  {
    return shellAdapter.getShellOutputStream();
  }
  
  public VTServerConnection getConnection()
  {
    return connection;
  }
  
  public VTServerShellExitListener getShellExitListener()
  {
    return shellExitListener;
  }
  
  public VTServerHostResolver getHostResolver()
  {
    return hostResolver;
  }
  
  public VTServerShellOutputWriter getOutputWriter()
  {
    return shellOutputWriter;
  }
  
  public VTServerRuntimeExecutor getRuntimeExecutor()
  {
    return runtimeExecutor;
  }
  
  public VTFileTransferServer getFileTransferServer()
  {
    return fileTransferServer;
  }
  
  public VTServer getServer()
  {
    return server;
  }
  
  public VTServerFileScanOperation getFileScanOperation()
  {
    return fileScanOperation;
  }
  
  public VTServerFileModifyOperation getFileModifyOperation()
  {
    return fileModifyOperation;
  }
  
  public VTServerNetworkInterfaceResolver getNetworkInterfaceResolver()
  {
    return networkInterfaceResolver;
  }
  
  public VTServerOpticalDriveOperation getOpticalDriveOperation()
  {
    return opticalDriveOperation;
  }
  
  public VTServerSessionListViewer getConnectionListViewer()
  {
    return connectionListViewer;
  }
  
  public VTServerFileSystemRootsResolver getFileSystemRootsResolver()
  {
    return fileSystemRootsResolver;
  }
  
  public void ping()
  {
    pingServiceClient.ping();
  }
  
  public void addPingListener(VTNanoPingListener listener)
  {
    pingServiceClient.addListener(listener);
  }
  
  public void removePingListener(VTNanoPingListener listener)
  {
    pingServiceClient.removeListener(listener);
  }
  
  public VTTunnelConnectionHandler getTunnelsHandler()
  {
    return tunnelsHandler;
  }
  
  public VTRemotePipedSocketFactory createRemotePipedSocketFactory(int type)
  {
    return tunnelsHandler.getConnection().createRemotePipedSocketFactory(type);
  }
  
  public VTRemoteSocketFactory createRemoteSocketFactory(int type)
  {
    type |= VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT;
    return tunnelsHandler.getConnection().createRemoteSocketFactory(tunnelsHandler.getConnection().getResponseChannel(type));
  }
  
  public boolean isStopped()
  {
    return clientReader.isStopped() || !connection.isConnected();
  }
  
  public void stopTasks()
  {
    connection.closeSockets();
    clientReader.setStopped(true);
    shellOutputWriter.setStopped(true);
    shellExitListener.setStopped(true);
    fileTransferServer.getHandler().getSession().getTransaction().setStopped(true);
    pingServiceClient.setStopped(true);
    pingServiceServer.setStopped(true);
    pingServiceClient.ping();
    pingServiceServer.ping();
  }
  
  public void restartShell()
  {
    setRestartingShell(true);
    stopShell();
    waitShell();
    waitShellThreads();
    startShell();
    restartShellThreads();
  }
  
  public void startShell()
  {
    try
    {
      shellAdapter.startShell();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
  
  public void startSession() throws IOException
  {
    connection.getShellWriter().writeLong(connection.getSecureRandom().nextLong());
    connection.getShellWriter().flush();
    connection.getCommandReader().readLong();
  }
  
  public void startSessionThreads()
  {
    pingServiceServer.startThread();
    pingServiceClient.startThread();
    clientReader.startThread();
    shellOutputWriter.startThread();
    shellExitListener.startThread();
    tunnelsHandler.startThread();
  }
  
  public void restartShellThreads()
  {
    shellOutputWriter.setStopped(false);
    shellExitListener.setStopped(false);
    shellOutputWriter.startThread();
    shellExitListener.startThread();
  }
  
  public void waitSession()
  {
    synchronized (this)
    {
      while (!isStopped())
      {
        try
        {
          wait();
        }
        catch (Throwable e)
        {
          return;
        }
      }
    }
  }
  
  public void waitShell()
  {
    shellAdapter.waitShell();
  }
  
  public void tryStopShellThreads()
  {
    shellOutputWriter.setStopped(true);
    shellExitListener.setStopped(true);
  }
  
  public void tryStopSessionThreads()
  {
    stopTasks();
    try
    {
      for (Closeable closeable : sessionCloseables)
      {
        try
        {
          closeable.close();
        }
        catch (Throwable t)
        {
          
        }
      }
    }
    catch (Throwable t)
    {
      
    }
    runtimeExecutor.clear();
    if (fileScanOperation.aliveThread())
    {
      fileScanOperation.interruptThread();
      fileScanOperation.stopThread();
    }
    if (fileModifyOperation.aliveThread())
    {
      fileModifyOperation.interruptThread();
      fileModifyOperation.stopThread();
    }
    if (runtimeExecutor.aliveThread())
    {
      runtimeExecutor.interruptThread();
      runtimeExecutor.stopThread();
    }
    if (hostResolver.aliveThread())
    {
      hostResolver.interruptThread();
      hostResolver.stopThread();
    }
    if (networkInterfaceResolver.aliveThread())
    {
      networkInterfaceResolver.interruptThread();
      networkInterfaceResolver.stopThread();
    }
    if (connectionListViewer.aliveThread())
    {
      connectionListViewer.interruptThread();
      connectionListViewer.stopThread();
    }
    if (fileSystemRootsResolver.aliveThread())
    {
      fileSystemRootsResolver.interruptThread();
      fileSystemRootsResolver.stopThread();
    }
    if (opticalDriveOperation.aliveThread())
    {
      opticalDriveOperation.interruptThread();
      opticalDriveOperation.stopThread();
    }
    tunnelsHandler.getConnection().close();
  }
  
  public String getShellEncoding()
  {
    return shellAdapter.getShellEncoding();
  }
  
  public void stopShell()
  {
    tryStopShellThreads();
    shellAdapter.stopShell();
  }
  
  public void waitThreads()
  {
    try
    {
      clientReader.joinThread();
      shellOutputWriter.joinThread();
      shellExitListener.joinThread();
      fileTransferServer.joinThread();
      runtimeExecutor.joinThread();
      fileScanOperation.joinThread();
      fileModifyOperation.joinThread();
      hostResolver.joinThread();
      networkInterfaceResolver.joinThread();
      connectionListViewer.joinThread();
      fileSystemRootsResolver.joinThread();
      tunnelsHandler.joinThread();
      pingServiceClient.joinThread();
      pingServiceServer.joinThread();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      // return;
    }
  }
  
  public void waitShellThreads()
  {
    try
    {
      shellOutputWriter.joinThread();
      shellExitListener.joinThread();
    }
    catch (Throwable e)
    {
      // return;
    }
  }
  
  public boolean isEchoCommands()
  {
    return echoCommands;
  }
  
  public void setEchoCommands(boolean echoCommands)
  {
    this.echoCommands = echoCommands;
  }
  
  public int getEchoState()
  {
    return echoState;
  }
  
  public void setEchoState(int echoState)
  {
    this.echoState = echoState;
  }
  
  public void setShellType(int shellType)
  {
    shellAdapter.setShellType(shellType);
  }
  
  public boolean isAgent()
  {
    return shellAdapter.isAgent();
  }
  
  public void negotiateShell() throws IOException
  {
    String clientShell = connection.getCommandReader().readUTF();
    connection.setQuiet(connection.getCommandReader().readBoolean());
    shellAdapter.setAgent(connection.getCommandReader().readBoolean());
//    boolean requestPTY = connection.getCommandReader().readBoolean();
//    boolean hasPTY = requestPTY && !connection.isManaged() && (!VTReflectionUtils.detectWindows()
//    || (VTMainNativeUtils.checkShAvailable() && (VTMainNativeUtils.checkWinptyAvailable() || VTMainNativeUtils.checkScriptAvailable()))
//    || (VTMainNativeUtils.checkWSLShAvailable() && VTMainNativeUtils.checkWSLScriptAvailable()));
//    connection.getShellWriter().writeBoolean(hasPTY);
//    shellAdapter.setAttachPTY(requestPTY && hasPTY);
    String serverShell = server.getServerConnector().getSessionShell();
    if (clientShell != null && clientShell.length() > 0)
    {
      if (clientShell.trim().equalsIgnoreCase("B"))
      {
        setShellType(VTShellProcessor.SHELL_TYPE_BEANSHELL);
      }
      else if (clientShell.trim().equalsIgnoreCase("N"))
      {
        setShellBuilder("", null, null);
      }
      else
      {
        setShellBuilder(clientShell, null, null);
      }
    }
    else if (serverShell != null && serverShell.length() > 0)
    {
      if (serverShell.trim().equalsIgnoreCase("B"))
      {
        setShellType(VTShellProcessor.SHELL_TYPE_BEANSHELL);
      }
      else if (serverShell.trim().equalsIgnoreCase("N"))
      {
        setShellBuilder("", null, null);
      }
      else
      {
        setShellBuilder(serverShell, null, null);
      }
    }
    else
    {
      setShellBuilder(null, null, null);
    }
  }
}