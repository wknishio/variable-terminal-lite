package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.vash.vate.proxy.client.VTProxy.VTProxyType;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.session.VTTunnelCloseableSocket;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannelBindSocketListener implements Runnable
{
  private final VTTunnelChannel channel;
  private ServerSocket serverSocket;
  private volatile boolean closed = false;
  private static final String SESSION_SEPARATOR = "\f";
  private static final char SESSION_MARK = '\b';
  
  public VTTunnelChannelBindSocketListener(VTTunnelChannel channel)
  {
    this.channel = channel;
    this.closed = false;
  }
  
  public String toString()
  {
    return channel.toString();
  }
  
  public boolean equals(Object other)
  {
    return this.toString().equals(other.toString());
  }
  
  public VTTunnelChannel getChannel()
  {
    return channel;
  }
  
  public void close() throws IOException
  {
    if (closed)
    {
      return;
    }
    try
    {
      if (serverSocket != null)
      {
        serverSocket.close();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    try
    {
      channel.close();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    //channel.getConnection().removeChannel(this);
    closed = true;
  }
  
  public boolean bind()
  {
    try
    {
      if (serverSocket == null || serverSocket.isClosed())
      {
        serverSocket = new ServerSocket();
        // serverSocket.setReuseAddress(true);
      }
      // serverSocket.setReuseAddress(true);
      serverSocket.bind(channel.getBindAddress());
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  public void remove()
  {
    channel.getConnection().removeBindListener(this);
  }
  
  public void run()
  {
    Thread.currentThread().setName(getClass().getSimpleName());
    try
    {
      while (!closed && !serverSocket.isClosed() && serverSocket.isBound())
      {
        VTTunnelSession session = null;
        VTTunnelSessionHandler handler = null;
        Socket acceptedSocket = null;
        InputStream socketInputStream = null;
        OutputStream socketOutputStream = null;
        
        try
        {
          acceptedSocket = accept();
          socketInputStream = acceptedSocket.getInputStream();
          socketOutputStream = acceptedSocket.getOutputStream();
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
        }
        
        if (socketInputStream != null && socketOutputStream != null)
        {
          char tunnelType = channel.getTunnelType();
          int channelType = channel.getChannelType();
          int connectTimeout = channel.getConnectTimeout();
          int dataTimeout = channel.getDataTimeout();
          String bind = channel.getNetwork();
          
          if (dataTimeout > 0)
          {
            acceptedSocket.setSoTimeout(dataTimeout);
          }
          
          session = new VTTunnelSession(channel.getConnection(), true);
          session.setSocket(acceptedSocket);
          session.setSocketInputStream(socketInputStream);
          session.setSocketOutputStream(socketOutputStream);
          
          VTProxyType proxyType = channel.getProxy().getProxyType();
          String proxyHost = channel.getProxy().getProxyHost();
          int proxyPort = channel.getProxy().getProxyPort();
          String proxyUser = channel.getProxy().getProxyUser();
          String proxyPassword = channel.getProxy().getProxyPassword();
          
          String proxyTypeLetter = "G";
          
          if (proxyType == VTProxyType.GLOBAL)
          {
            proxyTypeLetter = "G";
          }
          else if (proxyType == VTProxyType.DIRECT)
          {
            proxyTypeLetter = "D";
          }
          else if (proxyType == VTProxyType.HTTP)
          {
            proxyTypeLetter = "H";
          }
          else if (proxyType == VTProxyType.SOCKS)
          {
            proxyTypeLetter = "S";
          }
          else if (proxyType == VTProxyType.PLUS)
          {
            proxyTypeLetter = "P";
          }
          
          if (proxyUser == null || proxyPassword == null || proxyUser.length() == 0 || proxyPassword.length() == 0)
          {
            proxyUser = "*";
            proxyPassword = "*" + SESSION_SEPARATOR + "*";
          }
          
//          if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
//          {
//            handler = new VTTunnelSocksSessionHandler(session, channel, channel.getTunnelUsername(), channel.getTunnelPassword(), bind, 0, channel.getConnection().createRemoteSocketFactory(channel), channel.getProxy());
//            channel.getConnection().getExecutorService().execute(handler);
//            continue;
//          }
          
          handler = new VTTunnelSessionHandler(session, channel);
          
          VTLinkableDynamicMultiplexedInputStream input = channel.getConnection().getInputStream(channelType, handler);
          VTLinkableDynamicMultiplexedOutputStream output = channel.getConnection().getOutputStream(channelType, handler);
          
          if (output != null && input != null)
          {
            final int inputNumber = input.number();
            final int outputNumber = output.number();
            
            input.setOutputStream(session.getSocketOutputStream(), new VTTunnelCloseableSocket(acceptedSocket));
            output.open();
            
            session.setTunnelInputStream(input);
            session.setTunnelOutputStream(output);
            
            if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_TCP)
            {
              String host = channel.getRedirectHost();
              int port = channel.getRedirectPort();
              // request message sent
              channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + connectTimeout + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + bind + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
              channel.getConnection().getControlOutputStream().flush();
            }
            else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
            {
              String username = channel.getTunnelUsername();
              String password = channel.getTunnelPassword();
              if (username == null || password == null || username.length() == 0 || password.length() == 0)
              {
                username = "";
                password = "";
              }
              // request message sent
              channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + connectTimeout + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + bind + SESSION_SEPARATOR + username + SESSION_SEPARATOR + password + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
              channel.getConnection().getControlOutputStream().flush();
            }
            else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_FTP)
            {
              String username = channel.getTunnelUsername();
              String password = channel.getTunnelPassword();
              if (username == null || password == null || username.length() == 0 || password.length() == 0)
              {
                username = "";
                password = "";
              }
              // request message sent
              channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "F" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + connectTimeout + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + bind + SESSION_SEPARATOR + username + SESSION_SEPARATOR + password + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
              channel.getConnection().getControlOutputStream().flush();
            }
          }
          else
          {
            // cannot handle more sessions
            if (handler != null)
            {
              handler.close();
            }
          }
        }
      }
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    //closed = true;
  }
  
  private Socket accept()
  {
    Socket socket = null;
    try
    {
      socket = serverSocket.accept();
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
      //socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
      return socket;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
}