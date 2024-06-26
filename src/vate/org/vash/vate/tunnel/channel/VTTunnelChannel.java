package org.vash.vate.tunnel.channel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.vash.vate.VT;
import org.vash.vate.socket.proxy.VTProxy;
import org.vash.vate.tunnel.connection.VTTunnelConnection;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannel
{
  public static final int TUNNEL_TYPE_TCP = 0;
  public static final int TUNNEL_TYPE_SOCKS = 1;
  public static final int TUNNEL_TYPE_ANY = 2;
  
  private final int tunnelType;
  private int channelType = VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT;
  private final VTTunnelConnection connection;
  private final List<VTTunnelSessionHandler> sessions;
  
  private int connectTimeout;
  private int dataTimeout;
  private InetSocketAddress bindAddress;
  private String socksUsername;
  private String socksPassword;
  private String bindHost;
  private int bindPort;
  private String redirectHost;
  private int redirectPort;
  private VTProxy proxy;
  
  public int getTunnelType()
  {
    return tunnelType;
  }
  
  public int getChannelType()
  {
    return channelType;
  }
  
  public void setChannelType(int channelType)
  {
    this.channelType = channelType;
  }
  
  // SOCKS bind tunnel without authentication
  public VTTunnelChannel(int channelType, VTTunnelConnection connection, int connectTimeout, int dataTimeout, String bindHost, int bindPort, VTProxy proxy)
  {
    this.tunnelType = TUNNEL_TYPE_SOCKS;
    this.channelType = channelType;
    this.connection = connection;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.bindHost = bindHost;
    this.bindPort = bindPort;
    this.proxy = proxy;
    if (bindHost != null && bindHost.length() > 0)
    {
      this.bindAddress = new InetSocketAddress(bindHost, bindPort);
    }
    else
    {
      this.bindAddress = new InetSocketAddress(bindPort);
    }
    this.sessions = new ArrayList<VTTunnelSessionHandler>();
  }
  
  // SOCKS bind tunnel with authentication
  public VTTunnelChannel(int channelType, VTTunnelConnection connection, int connectTimeout, int dataTimeout, String bindHost, int bindPort, String socksUsername, String socksPassword, VTProxy proxy)
  {
    this.tunnelType = TUNNEL_TYPE_SOCKS;
    this.channelType = channelType;
    this.connection = connection;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.bindHost = bindHost;
    this.bindPort = bindPort;
    this.proxy = proxy;
    if (bindHost != null && bindHost.length() > 0)
    {
      this.bindAddress = new InetSocketAddress(bindHost, bindPort);
    }
    else
    {
      this.bindAddress = new InetSocketAddress(bindPort);
    }
    this.socksUsername = socksUsername;
    this.socksPassword = socksPassword;
    this.sessions = new ArrayList<VTTunnelSessionHandler>();
  }
  
  // TCP bind redirect tunnel
  public VTTunnelChannel(int channelType, VTTunnelConnection connection, int connectTimeout, int dataTimeout, String bindHost, int bindPort, String redirectHost, int redirectPort, VTProxy proxy)
  {
    this.tunnelType = TUNNEL_TYPE_TCP;
    this.channelType = channelType;
    this.connection = connection;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.bindHost = bindHost;
    this.bindPort = bindPort;
    this.redirectHost = redirectHost;
    this.redirectPort = redirectPort;
    this.proxy = proxy;
    if (bindHost != null && bindHost.length() > 0)
    {
      this.bindAddress = new InetSocketAddress(bindHost, bindPort);
    }
    else
    {
      this.bindAddress = new InetSocketAddress(bindPort);
    }
    this.sessions = new ArrayList<VTTunnelSessionHandler>();
  }
  
  //Generic response channel
  public VTTunnelChannel(int channelType, VTTunnelConnection connection)
  {
    this.tunnelType = TUNNEL_TYPE_ANY;
    this.channelType = channelType;
    this.connection = connection;
    this.sessions = new ArrayList<VTTunnelSessionHandler>();
  }
  
  public String toString()
  {
    return String.valueOf(this.getBindHost() + " " + bindPort);
  }
  
  public boolean equals(Object other)
  {
    return this.toString().equals(other.toString());
  }
  
  public void close()
  {
    // closed = true;
    //synchronized (sessions)
    //{
      for (VTTunnelSessionHandler handler : sessions.toArray(new VTTunnelSessionHandler[] {}))
      {
        try
        {
          handler.getSession().close();
        }
        catch (Throwable e)
        {
          // e.printStackTrace();
        }
      }
    //}
    sessions.clear();
  }
  
  public VTTunnelConnection getConnection()
  {
    return connection;
  }
  
  public InetSocketAddress getBindAddress()
  {
    return bindAddress;
  }
  
  public String getSocksUsername()
  {
    return socksUsername;
  }
  
  public String getSocksPassword()
  {
    return socksPassword;
  }
  
  public void setSocksUsername(String socksUsername)
  {
    this.socksUsername = socksUsername;
  }
  
  public void setSocksPassword(String socksPassword)
  {
    this.socksPassword = socksPassword;
  }
  
  public void addSession(VTTunnelSessionHandler handler)
  {
    sessions.add(handler);
  }
  
  public boolean removeSession(VTTunnelSessionHandler handler)
  {
    return sessions.remove(handler);
  }
  
  public int getConnectTimeout()
  {
    return connectTimeout;
  }
  
  public int getDataTimeout()
  {
    return dataTimeout;
  }
  
  public String getBindHost()
  {
    if (bindHost == null || bindHost.length() == 0)
    {
      return "*";
    }
    return bindHost;
  }
  
  public int getBindPort()
  {
    return bindPort;
  }
  
  public String getRedirectHost()
  {
    if (redirectHost == null || redirectHost.length() == 0)
    {
      return "*";
    }
    return redirectHost;
  }
  
  public int getRedirectPort()
  {
    return redirectPort;
  }
  
  public void setRedirectAddress(String redirectHost, int redirectPort)
  {
    this.redirectHost = redirectHost;
    this.redirectPort = redirectPort;
  }
  
  public void setConnectTimeout(int connectTimeout)
  {
    this.connectTimeout = connectTimeout;
  }
  
  public void setDataTimeout(int dataTimeout)
  {
    this.dataTimeout = dataTimeout;
  }
  
  public void setProxy(VTProxy proxy)
  {
    this.proxy = proxy;
  }
  
  public VTProxy getProxy()
  {
    return proxy;
  }  
}