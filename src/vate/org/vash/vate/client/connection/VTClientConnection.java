package org.vash.vate.client.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;
import org.vash.vate.VT;
import org.vash.vate.console.VTConsole;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3DigestRandom;
import org.vash.vate.security.VTBlake3MessageDigest;
import org.vash.vate.security.VTCryptographicEngine;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTClientConnection
{
  private static final String MAJOR_MINOR_VERSION = VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION;
  private static byte[] VT_SERVER_CHECK_STRING_NONE = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_NONE = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_RC4 = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_RC4 = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_AES = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_AES = new byte[16];
  //private static byte[] VT_SERVER_CHECK_STRING_BLOWFISH = new byte[16];
  //private static byte[] VT_CLIENT_CHECK_STRING_BLOWFISH = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_SALSA = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_SALSA = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_HC256 = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_HC256 = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_ISAAC = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_ISAAC = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_GRAIN = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_GRAIN = new byte[16];
  
  static
  {
    try
    {
      VT_SERVER_CHECK_STRING_NONE = (StringUtils.reverse("VT/SERVER/NONE/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/NONE/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_NONE = (StringUtils.reverse("VT/CLIENT/NONE/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/NONE/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_RC4 = (StringUtils.reverse("VT/SERVER/RC4/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/RC4/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_RC4 = (StringUtils.reverse("VT/CLIENT/RC4/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/RC4/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_AES = (StringUtils.reverse("VT/SERVER/AES/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/AES/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_AES = (StringUtils.reverse("VT/CLIENT/AES/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/AES/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_SALSA = (StringUtils.reverse("VT/SERVER/SALSA/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/SALSA/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_SALSA = (StringUtils.reverse("VT/CLIENT/SALSA/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/SALSA/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_HC256 = (StringUtils.reverse("VT/SERVER/HC256/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/HC256/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_HC256 = (StringUtils.reverse("VT/CLIENT/HC256/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/HC256/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_ISAAC = (StringUtils.reverse("VT/SERVER/ISAAC/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/ISAAC/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_ISAAC = (StringUtils.reverse("VT/CLIENT/ISAAC/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/ISAAC/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_GRAIN = (StringUtils.reverse("VT/SERVER/GRAIN/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/GRAIN/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_GRAIN = (StringUtils.reverse("VT/CLIENT/GRAIN/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/GRAIN/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      
    }
  }
  
  private volatile boolean connected = false;
  private volatile boolean closed = true;
  
  private int encryptionType;
  private byte[] encryptionKey;
  // private byte[] digestedClient;
  // private byte[] digestedServer;
  private byte[] localNonce = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  private byte[] remoteNonce = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  private byte[] randomData = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  // private byte[] paddingData = new byte[1024];
  // private MessageDigest sha256Digester;
  private VTBlake3MessageDigest blake3Digest;
  private VTBlake3DigestRandom secureRandom;
  private VTCryptographicEngine cryptoEngine;
  private Socket connectionSocket;
  private InputStream connectionSocketInputStream;
  private OutputStream connectionSocketOutputStream;
  private InputStream connectionInputStream;
  private OutputStream connectionOutputStream;
  private VTLittleEndianInputStream nonceReader;
  private VTLittleEndianOutputStream nonceWriter;
  private VTLinkableDynamicMultiplexingInputStream multiplexedConnectionInputStream;
  private VTLinkableDynamicMultiplexingOutputStream multiplexedConnectionOutputStream;
  
  // private InputStream authenticationInputStream;
  private VTLinkableDynamicMultiplexedInputStream shellInputStream;
  private VTLinkableDynamicMultiplexedInputStream fileTransferControlInputStream;
  private VTLinkableDynamicMultiplexedInputStream fileTransferDataInputStream;
  // private VTMultiplexedInputStream graphicsCheckInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsControlInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsDirectImageInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsDeflatedImageInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsSnappedImageInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsClipboardInputStream;
  private VTLinkableDynamicMultiplexedInputStream audioDataInputStream;
  private VTLinkableDynamicMultiplexedInputStream audioControlInputStream;
  private VTLinkableDynamicMultiplexedInputStream pingInputStream;
  private VTLinkableDynamicMultiplexedInputStream tunnelControlInputStream;
  // private VTLinkableDynamicMultiplexedInputStream socksControlInputStream;
  
  // private OutputStream authenticationOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream shellOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream fileTransferControlOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream fileTransferDataOutputStream;
  // private VTMultiplexedOutputStream graphicsCheckOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsControlOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsDirectImageOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsDeflatedImageOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsSnappedImageOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsClipboardOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream audioDataOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream audioControlOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream pingOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream tunnelControlOutputStream;
  // private VTLinkableDynamicMultiplexedOutputStream socksControlOutputStream;
  
  // private VTLittleEndianInputStream verificationReader;
  // private VTLittleEndianOutputStream verificationWriter;
  private VTLittleEndianInputStream authenticationReader;
  private VTLittleEndianOutputStream authenticationWriter;
  
  private Reader resultReader;
  private BufferedWriter commandWriter;
  private InputStream shellDataInputStream;
  private OutputStream shellDataOutputStream;
  private InputStream clipboardDataInputStream;
  private OutputStream clipboardDataOutputStream;
  
  private VTLittleEndianInputStream fileTransferControlDataInputStream;
  private VTLittleEndianOutputStream fileTransferControlDataOutputStream;
  // private VTLittleEndianInputStream graphicsCheckDataInputStream;
  // private VTLittleEndianOutputStream graphicsCheckDataOutputStream;
  private VTLittleEndianInputStream graphicsControlDataInputStream;
  private VTLittleEndianOutputStream graphicsControlDataOutputStream;
  
  private VTLittleEndianInputStream directImageDataInputStream;
  private VTLittleEndianOutputStream directImageDataOutputStream;
  private VTLittleEndianInputStream deflatedImageDataInputStream;
  private VTLittleEndianOutputStream deflatedImageDataOutputStream;
  private VTLittleEndianInputStream snappedImageDataInputStream;
  private VTLittleEndianOutputStream snappedImageDataOutputStream;
  
  // private boolean zstdAvailable;
  
  // private ZstdInputStream zstdImageInputStream;
  
  // private ZstdInputStream zstdClipboardInputStream;
  // private ZstdOutputStream zstdClipboardOutputStream;
  
  public VTClientConnection(VTBlake3DigestRandom secureRandom)
  {
    // try
    // {
    // this.sha256Digester = MessageDigest.getInstance("SHA-256");
    // }
    // catch (NoSuchAlgorithmException e)
    // {
    // e.printStackTrace();
    // }
    this.secureRandom = secureRandom;
    this.cryptoEngine = new VTCryptographicEngine();
    this.blake3Digest = new VTBlake3MessageDigest();
    this.authenticationReader = new VTLittleEndianInputStream(null);
    this.authenticationWriter = new VTLittleEndianOutputStream(null);
  }
  
  public VTBlake3DigestRandom getSecureRandom()
  {
    return secureRandom;
  }
  
  public VTLinkableDynamicMultiplexingInputStream getMultiplexedConnectionInputStream()
  {
    return multiplexedConnectionInputStream;
  }
  
  public VTLinkableDynamicMultiplexingOutputStream getMultiplexedConnectionOutputStream()
  {
    return multiplexedConnectionOutputStream;
  }
  
  public byte[] getLocalNonce()
  {
    return localNonce;
  }
  
  public byte[] getRemoteNonce()
  {
    return remoteNonce;
  }
  
  public void setEncryptionType(int encryptionType)
  {
    this.encryptionType = encryptionType;
  }
  
  public void setEncryptionKey(byte[] encryptionKey)
  {
    this.encryptionKey = encryptionKey;
  }
  
  public Socket getConnectionSocket()
  {
    return connectionSocket;
  }
  
  public void setConnectionSocket(Socket connectionSocket)
  {
    this.connectionSocket = connectionSocket;
    this.closed = false;
  }
  
  /*
   * public InputStream getAuthenticationInputStream() { return
   * authenticationInputStream; }
   */
  
  public InputStream getShellInputStream()
  {
    return shellInputStream;
  }
  
  public InputStream getFileTransferDataInputStream()
  {
    return fileTransferDataInputStream;
  }
  
  public InputStream getGraphicsControlInputStream()
  {
    return graphicsControlInputStream;
  }
  
  /*
   * public InputStream getGraphicsImageInputStream() { return
   * graphicsImageInputStream; }
   */
  
  public InputStream getAudioDataInputStream()
  {
    return audioDataInputStream;
  }
  
  public InputStream getAudioControlInputStream()
  {
    return audioControlInputStream;
  }
  
  public InputStream getPingInputStream()
  {
    return pingInputStream;
  }
  
  public InputStream getTunnelControlInputStream()
  {
    return tunnelControlInputStream;
  }
  
  // public InputStream getSocksControlInputStream()
  // {
  // return socksControlInputStream;
  // }
  
  /*
   * public OutputStream getAuthenticationOutputStream() { return
   * authenticationOutputStream; }
   */
  
  public OutputStream getShellOutputStream()
  {
    return shellOutputStream;
  }
  
  public OutputStream getFileTransferDataOutputStream()
  {
    return fileTransferDataOutputStream;
  }
  
  /*
   * public OutputStream getGraphicsImageOutputStream() { return
   * graphicsImageOutputStream; }
   */
  
  public OutputStream getAudioDataOutputStream()
  {
    return audioDataOutputStream;
  }
  
  public OutputStream getAudioControlOutputStream()
  {
    return audioControlOutputStream;
  }
  
  public OutputStream getPingOutputStream()
  {
    return pingOutputStream;
  }
  
  public OutputStream getTunnelControlOutputStream()
  {
    return tunnelControlOutputStream;
  }
  
  // public OutputStream getSocksControlOutputStream()
  // {
  // return socksControlOutputStream;
  // }
  
  public VTLittleEndianInputStream getAuthenticationReader()
  {
    return authenticationReader;
  }
  
  public Reader getResultReader()
  {
    return resultReader;
  }
  
  public VTLittleEndianOutputStream getAuthenticationWriter()
  {
    return authenticationWriter;
  }
  
  public BufferedWriter getCommandWriter()
  {
    return commandWriter;
  }
  
  public VTLittleEndianInputStream getFileTransferControlDataInputStream()
  {
    return fileTransferControlDataInputStream;
  }
  
  public VTLittleEndianOutputStream getFileTransferControlDataOutputStream()
  {
    return fileTransferControlDataOutputStream;
  }
  
  // public VTLittleEndianInputStream getGraphicsCheckDataInputStream()
  // {
  // return graphicsCheckDataInputStream;
  // }
  
  // public VTLittleEndianOutputStream getGraphicsCheckDataOutputStream()
  // {
  // return graphicsCheckDataOutputStream;
  // }
  
  public VTLittleEndianInputStream getGraphicsControlDataInputStream()
  {
    return graphicsControlDataInputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsControlDataOutputStream()
  {
    return graphicsControlDataOutputStream;
  }
  
  public VTLittleEndianInputStream getGraphicsDirectImageDataInputStream()
  {
    return directImageDataInputStream;
  }
  
  public VTLittleEndianInputStream getGraphicsDeflatedImageDataInputStream()
  {
    return deflatedImageDataInputStream;
  }
  
  public VTLittleEndianInputStream getGraphicsSnappedImageDataInputStream()
  {
    return snappedImageDataInputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsDirectImageDataOutputStream()
  {
    return directImageDataOutputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsDeflatedImageDataOutputStream()
  {
    return deflatedImageDataOutputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsSnappedImageDataOutputStream()
  {
    return snappedImageDataOutputStream;
  }
  
  public InputStream getGraphicsClipboardInputStream()
  {
    return graphicsClipboardInputStream;
  }
  
  public OutputStream getGraphicsClipboardOutputStream()
  {
    return graphicsClipboardOutputStream;
  }
  
  public InputStream getGraphicsClipboardDataInputStream()
  {
    return clipboardDataInputStream;
  }
  
  public OutputStream getGraphicsClipboardDataOutputStream()
  {
    return clipboardDataOutputStream;
  }
  
  public void closeSockets()
  {
    if (closed)
    {
      return;
    }
//    StringBuilder message = new StringBuilder();
//    StackTraceElement[] stackStrace = Thread.currentThread().getStackTrace();
//    for (int i = stackStrace.length - 1; i >= 0; i--)
//    {
//      message.append(stackStrace[i].toString() + "\n");
//    }
//    System.err.println(message.toString());
    VTConsole.setCommandEcho(true);
    if (connectionSocket != null)
    {
      try
      {
        connectionSocket.close();
      }
      catch (IOException e)
      {
        
      }
    }
    if (multiplexedConnectionOutputStream != null)
    {
      try
      {
        multiplexedConnectionOutputStream.close();
      }
      catch (IOException e)
      {
        
      }
    }
    if (multiplexedConnectionInputStream != null)
    {
      try
      {
        multiplexedConnectionInputStream.stopPacketReader();
      }
      catch (IOException e)
      {
        
      }
      catch (InterruptedException e)
      {
        
      }
    }
    if (authenticationReader != null)
    {
      try
      {
        authenticationReader.close();
      }
      catch (Throwable t)
      {
        
      }
    }
    if (authenticationWriter != null)
    {
      try
      {
        authenticationWriter.close();
      }
      catch (Throwable t)
      {
        
      }
    }
    closed = true;
  }
  
//	public void closeSocketsFromDialog()
//	{
//		dialog = true;
//		closeSockets();
//	}
  
  public void closeConnection()
  {
    // VTConsole.setLogReadLine(null);
    // VTConsole.setLogOutput(null);
    if (!closed || connected)
    {
      VTConsole.print("\nVT>Connection with server closed!");
    }
    // VTConsole.setCommandEcho(true);
    closeSockets();
    if (connected)
    {
      try
      {
        VTConsole.interruptReadLine();
      }
      catch (Throwable t)
      {
        
      }
      try
      {
        VTConsole.setLogOutput(null);
        VTConsole.setLogReadLine(null);
      }
      catch (Throwable t)
      {
        
      }
    }
    connected = false;
    synchronized (this)
    {
      notifyAll();
    }
  }
  
  public boolean isConnected()
  {
    return connectionSocket != null && connectionSocket.isConnected() && !connectionSocket.isClosed() && connected && !closed;
  }
  
  private void setNonceStreams() throws IOException
  {
    connectionSocketInputStream = connectionSocket.getInputStream();
    connectionSocketOutputStream = connectionSocket.getOutputStream();
    nonceReader = new VTLittleEndianInputStream(connectionSocketInputStream);
    nonceWriter = new VTLittleEndianOutputStream(connectionSocketOutputStream);
    // Arrays.fill(localNonce, (byte)0);
    // Arrays.fill(remoteNonce, (byte)0);
  }
  
  private void exchangeNonces(boolean update) throws IOException
  {
    secureRandom.nextBytes(randomData);
    nonceWriter.write(randomData);
    nonceWriter.flush();
    if (update)
    {
      for (int i = 0; i < randomData.length; i++)
      {
        localNonce[i] ^= randomData[i];
      }
    }
    else
    {
      for (int i = 0; i < randomData.length; i++)
      {
        localNonce[i] = randomData[i];
      }
    }
    nonceReader.readFully(randomData);
    if (update)
    {
      for (int i = 0; i < randomData.length; i++)
      {
        remoteNonce[i] ^= randomData[i];
      }
    }
    else
    {
      for (int i = 0; i < randomData.length; i++)
      {
        remoteNonce[i] = randomData[i];
      }
    }
    
    blake3Digest.reset();
    byte[] seed = new byte[VT.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(localNonce, 0, seed, 0, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(remoteNonce, 0, seed, VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    //secureRandom.setSeed(seed);
    blake3Digest.setSeed(seed);
    blake3Digest.reset();
  }
  
  private void setVerificationStreams(boolean encrypted) throws IOException
  {
    if (encrypted)
    {
      cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce);
      authenticationReader.setIntputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream));
      authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream));
    }
    else
    {
      authenticationReader.setIntputStream(connectionSocketInputStream);
      authenticationWriter.setOutputStream(connectionSocketOutputStream);
    }
  }
  
  public void setAuthenticationStreams() throws IOException
  {
    //exchangeNonces(true);
    cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce);
    authenticationReader.setIntputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream));
    authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream));
    nonceReader.setIntputStream(authenticationReader.getInputStream());
    nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
    //exchangeNonces(true);
  }
  
  public boolean setConnectionStreams(byte[] digestedCredentials, String user, String password) throws IOException
  {
    try
    {
      exchangeNonces(true);
    }
    catch (Throwable t)
    {
      return false;
    }
    cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce, digestedCredentials, user != null ? user.getBytes("UTF-8") : null, password != null ? password.getBytes("UTF-8") : null);
    connectionInputStream = cryptoEngine.getDecryptedInputStream(connectionSocketInputStream);
    connectionOutputStream = cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream);
    //authenticationReader.setIntputStream(connectionInputStream);
    //authenticationWriter.setOutputStream(connectionOutputStream);
    //nonceReader.setIntputStream(authenticationReader.getInputStream());
    //nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
    return true;
  }
  
  private void setMultiplexedStreams() throws IOException
  {
    multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(new BufferedInputStream(connectionInputStream, VT.VT_CONNECTION_PACKET_BUFFER_SIZE_BYTES), VT.VT_PACKET_DATA_SIZE_BYTES, VT.VT_CHANNEL_PACKET_BUFFER_SIZE_BYTES, false);
    multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, VT.VT_PACKET_DATA_SIZE_BYTES);
    
    pingInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED, 0);
    pingOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED, 0);
    
    shellInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 1);
    shellOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 1);
    
    fileTransferControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 2);
    fileTransferControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 2);
    fileTransferDataInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 3);
    fileTransferDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 3);
    fileTransferDataInputStream.addPropagated(fileTransferDataOutputStream);
    
    graphicsControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 4);
    graphicsControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 4);
    graphicsDirectImageInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 5);
    graphicsDirectImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 5);
    graphicsDeflatedImageInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 6);
    graphicsDeflatedImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 6);
    graphicsSnappedImageInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 7);
    graphicsSnappedImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 7);
    graphicsClipboardInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 8);
    graphicsClipboardOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 8);
    
    //graphicsControlInputStream.addPropagated(graphicsControlOutputStream);
    //graphicsControlInputStream.addPropagated(graphicsDirectImageInputStream);
    //graphicsControlInputStream.addPropagated(graphicsDeflatedImageInputStream);
    //graphicsControlInputStream.addPropagated(graphicsSnappedImageInputStream);
    // graphicsControlInputStream.addPropagated(graphicsClipboardInputStream);
    // graphicsControlInputStream.addPropagated(graphicsClipboardOutputStream);
    
    audioControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 9);
    audioControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 9);
    audioDataInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 10);
    audioDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 10);
    
    //audioDataOutputStream.addPropagated(audioDataInputStream);
    //audioDataInputStream.addPropagated(audioDataOutputStream);
    
    tunnelControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 11);
    tunnelControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 11);
    
    // socksControlInputStream =
    // multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED,
    // 12);
    // socksControlOutputStream =
    // multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED,
    // 12);
    
    shellDataOutputStream = VTCompressorSelector.createBufferedZlibOutputStreamDefault(shellOutputStream);
    // shellDataOutputStream =
    // VTCompressorSelector.createFlushBufferedSyncFlushDeflaterOutputStream(shellOutputStream);
    // shellDataOutputStream = shellOutputStream;
    
    shellDataInputStream = VTCompressorSelector.createBufferedZlibInputStream(shellInputStream);
    // shellDataInputStream =
    // VTCompressorSelector.createFlushBufferedSyncFlushInflaterInputStream(shellInputStream);
    // shellDataInputStream = shellInputStream;
    
    resultReader = new InputStreamReader(shellDataInputStream, "UTF-8");
    commandWriter = new BufferedWriter(new OutputStreamWriter(shellDataOutputStream, "UTF-8"));
    
    graphicsControlDataInputStream = new VTLittleEndianInputStream(new BufferedInputStream(graphicsControlInputStream));
    graphicsControlDataOutputStream = new VTLittleEndianOutputStream(new BufferedOutputStream(graphicsControlOutputStream));
    
    directImageDataInputStream = new VTLittleEndianInputStream(new BufferedInputStream(graphicsDirectImageInputStream, VT.VT_STANDARD_BUFFER_SIZE_BYTES));
    directImageDataOutputStream = new VTLittleEndianOutputStream(graphicsDirectImageOutputStream);
    
    // deflatedImageDataInputStream =
    // VTCompressorSelector.createCompatibleSyncFlushInflaterInputStream(graphicsDeflatedImageInputStream);
    //deflatedImageDataInputStream = VTCompressorSelector.createBufferedZlibInputStream(graphicsDeflatedImageInputStream);
    deflatedImageDataInputStream = new VTLittleEndianInputStream(VTCompressorSelector.createBufferedZstdInputStream(graphicsDeflatedImageInputStream));
    deflatedImageDataOutputStream = new VTLittleEndianOutputStream(graphicsDeflatedImageOutputStream);
    
    //snappedImageDataInputStream = VTCompressorSelector.createBufferedLz4InputStream(graphicsSnappedImageInputStream);
    snappedImageDataInputStream = new VTLittleEndianInputStream(VTCompressorSelector.createBufferedZlibInputStream(graphicsSnappedImageInputStream));
    snappedImageDataOutputStream = new VTLittleEndianOutputStream((graphicsSnappedImageOutputStream));
    
    clipboardDataOutputStream = VTCompressorSelector.createBufferedZstdOutputStream(graphicsClipboardOutputStream);
    clipboardDataInputStream = VTCompressorSelector.createBufferedZstdInputStream(graphicsClipboardInputStream);
    
    fileTransferControlDataInputStream = new VTLittleEndianInputStream(new BufferedInputStream(fileTransferControlInputStream));
    fileTransferControlDataOutputStream = new VTLittleEndianOutputStream(new BufferedOutputStream(fileTransferControlOutputStream));
    
    // graphicsControlInputStream.addPropagated(deflatedImageDataInputStream);
    // graphicsControlInputStream.addPropagated(snappedImageDataInputStream);
    
    // graphicsControlInputStream.addPropagated(clipboardDataOutputStream);
    // graphicsControlInputStream.addPropagated(clipboardDataInputStream);
    // closeAudioStreams();
    // audioDataInputStream.addPropagated(audioDataOutputStream);
  }
  
//  public boolean exchangeConnectionPadding() throws IOException
//  {
//    secureRandom.nextBytes(paddingData);
//    authenticationWriter.write(paddingData);
//    authenticationWriter.flush();
//    authenticationReader.readFully(paddingData);
//    return true;
//  }
//  
//  public boolean exchangeAuthenticationPadding() throws IOException
//  {
//    secureRandom.nextBytes(paddingData);
//    authenticationWriter.write(paddingData);
//    authenticationWriter.flush();
//    authenticationReader.readFully(paddingData);
//    return true;
//  }
  
  private byte[] exchangeCheckString(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, byte[] localCheckString, int encryptionType) throws IOException
  {
    blake3Digest.reset();
    blake3Digest.update(remoteNonce);
    blake3Digest.update(localNonce);
    if (encryptionKey != null && encryptionType != VT.VT_CONNECTION_ENCRYPT_NONE)
    {
      blake3Digest.update(encryptionKey);
    }
    byte[] data = blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, localCheckString);
    authenticationWriter.write(data);
    authenticationWriter.flush();
    authenticationReader.readFully(data);
    return data;
  }
  
//  private boolean matchRemoteEncryptionSettings(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, int encryptionType) throws IOException
//  {
//    byte[] localCheckString = null;
//    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_NONE;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_RC4;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_AES;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_SALSA;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_HC256;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_ISAAC;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_GRAIN;
//    }
//    
//    byte[] digestedServer = exchangeCheckString(localNonce, remoteNonce, encryptionKey, localCheckString, encryptionType);
//    
//    blake3Digester.reset();
//    blake3Digester.update(localNonce);
//    blake3Digester.update(remoteNonce);
//    if (encryptionKey != null)
//    {
//      blake3Digester.update(encryptionKey);
//    }
//    
//    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_NONE));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_RC4));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_AES));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_SALSA));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_HC256));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_ISAAC));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_GRAIN));
//    }
//    return false;
//  }
  
  private int discoverRemoteEncryptionType(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, byte[] localCheckString, int encryptionType) throws IOException
  {
    byte[] digestedServer = exchangeCheckString(localNonce, remoteNonce, encryptionKey, localCheckString, encryptionType);
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    // if (encryptionKey != null)
    // {
    // blake3Digester.update(encryptionKey);
    // }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_SERVER_CHECK_STRING_NONE)))
    {
      return VT.VT_CONNECTION_ENCRYPT_NONE;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_SERVER_CHECK_STRING_RC4)))
    {
      return VT.VT_CONNECTION_ENCRYPT_RC4;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_SERVER_CHECK_STRING_AES)))
    {
      return VT.VT_CONNECTION_ENCRYPT_AES;
    }
    
    // sha256Digester.reset();
    // sha256Digester.update(localNonce);
    // sha256Digester.update(remoteNonce);
    // if (encryptionKey != null)
    // {
    // sha256Digester.update(encryptionKey);
    // }
    // if (VTArrayComparator.arrayEquals(digestedServer,
    // sha256Digester.digest(VT_SERVER_CHECK_STRING_BLOWFISH)))
    // {
    // return VT.VT_CONNECTION_ENCRYPT_BLOWFISH;
    // }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_SERVER_CHECK_STRING_SALSA)))
    {
      return VT.VT_CONNECTION_ENCRYPT_SALSA;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_SERVER_CHECK_STRING_HC256)))
    {
      return VT.VT_CONNECTION_ENCRYPT_HC256;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_SERVER_CHECK_STRING_ISAAC)))
    {
      return VT.VT_CONNECTION_ENCRYPT_ISAAC;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_SERVER_CHECK_STRING_GRAIN)))
    {
      return VT.VT_CONNECTION_ENCRYPT_GRAIN;
    }
    
    return -1;
  }
  
  public boolean verifyConnection() throws IOException
  {
    connected = true;
    setNonceStreams();
    exchangeNonces(false);
    setVerificationStreams(false);
    //exchangeNonces(true);
    // if (matchRemoteEncryptionSettings(localNonce, remoteNonce,
    // encryptionKey))
    // {
    // return true;
    // }
    
    // exchangeNonces(true);
    // setVerificationStreams(true);
    
    int remoteEncryptionType = 0;
    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_NONE, encryptionType);
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_RC4);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_AES);
        return true;
      }
      // if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_BLOWFISH)
      // {
      // setEncryptionType(VT.VT_CONNECTION_ENCRYPT_BLOWFISH);
      // return true;
      // }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_SALSA);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_HC256);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_ISAAC);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_RC4, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_RC4);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_AES, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_AES);
        return true;
      }
    }
    // else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_BLOWFISH)
    // {
    // remoteEncryptionType = discoverRemoteEncryptionType(localNonce,
    // remoteNonce, null, VT_CLIENT_CHECK_STRING_BLOWFISH);
    // if (remoteEncryptionType != -1)
    // {
    // setEncryptionType(VT.VT_CONNECTION_ENCRYPT_BLOWFISH);
    // return true;
    // }
    // }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_SALSA, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_SALSA);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_HC256, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_HC256);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_ISAAC, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_ISAAC);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_GRAIN, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN);
        return true;
      }
    }
    return false;
  }
  
  /*
   * public void startConnection() {
   * multiplexedConnectionInputStream.startPacketReader(); }
   */
  
  public void startConnection() throws IOException
  {
    setMultiplexedStreams();
    //exchangeNonces(true);
    multiplexedConnectionInputStream.startPacketReader();
  }
  
  /*
   * public boolean startedConnection() { return
   * multiplexedConnectionInputStream != null &&
   * multiplexedConnectionInputStream.isPacketReaderStarted(); }
   */
  
  public void closeGraphicsModeStreams() throws IOException
  {
    try
    {
      graphicsDirectImageInputStream.close();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      deflatedImageDataInputStream.close();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      snappedImageDataInputStream.close();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      graphicsControlOutputStream.close();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      graphicsControlInputStream.close();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  // public void resetDirectGraphicsModeStreams() throws IOException
  // {
  // graphicsDirectImageOutputStream.open();
  // graphicsDirectImageInputStream.open();
  // }
  
  public void resetGraphicsModeStreams() throws IOException
  {
    graphicsControlInputStream.open();
    graphicsControlOutputStream.open();
    
    graphicsDirectImageOutputStream.open();
    graphicsDirectImageInputStream.open();
    graphicsDeflatedImageOutputStream.open();
    graphicsDeflatedImageInputStream.open();
    graphicsSnappedImageOutputStream.open();
    graphicsSnappedImageInputStream.open();
    
    graphicsControlDataInputStream.setIntputStream(new BufferedInputStream(graphicsControlInputStream));
    graphicsControlDataOutputStream.setOutputStream(new BufferedOutputStream(graphicsControlOutputStream));
    
    deflatedImageDataInputStream = new VTLittleEndianInputStream(VTCompressorSelector.createBufferedZstdInputStream(graphicsDeflatedImageInputStream));
    deflatedImageDataOutputStream = new VTLittleEndianOutputStream(graphicsDeflatedImageOutputStream);
    
    snappedImageDataInputStream = new VTLittleEndianInputStream(VTCompressorSelector.createBufferedZlibInputStream(graphicsSnappedImageInputStream));
    snappedImageDataOutputStream = new VTLittleEndianOutputStream(graphicsSnappedImageOutputStream);
    
    // graphicsControlInputStream.addPropagated(deflatedImageDataInputStream);
    // graphicsControlInputStream.addPropagated(snappedImageDataInputStream);
    
    resetClipboardStreams();
  }
  
  public void resetClipboardStreams() throws IOException
  {
    try
    {
      clipboardDataOutputStream.close();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      clipboardDataInputStream.close();
    }
    catch (Throwable t)
    {
      
    }
    // graphicsControlInputStream.removePropagated(clipboardDataOutputStream);
    // graphicsControlInputStream.removePropagated(clipboardDataInputStream);
    
    graphicsClipboardOutputStream.open();
    graphicsClipboardInputStream.open();
    
    clipboardDataOutputStream = VTCompressorSelector.createBufferedZstdOutputStream(graphicsClipboardOutputStream);
    clipboardDataInputStream = VTCompressorSelector.createBufferedZstdInputStream(graphicsClipboardInputStream);
    
    // graphicsControlInputStream.addPropagated(clipboardDataOutputStream);
    // graphicsControlInputStream.addPropagated(clipboardDataInputStream);
  }
  
  public void resetFileTransferStreams() throws IOException
  {
    fileTransferDataOutputStream.open();
    fileTransferDataInputStream.open();
  }
  
  public void closeFileTransferStreams() throws IOException
  {
    fileTransferDataOutputStream.close();
    fileTransferDataInputStream.close();
  }
  
  public void closeAudioStreams() throws IOException
  {
    audioDataOutputStream.close();
    audioDataInputStream.close();
  }
  
  public void resetAudioStreams() throws IOException
  {
    audioDataOutputStream.open();
    audioDataInputStream.open();
    // audioDataOutputStream = audioOutputStream;
    // audioDataInputStream = audioInputStream;
  }
  
  public void setRateInBytesPerSecond(long bytesPerSecond)
  {
    multiplexedConnectionOutputStream.setBytesPerSecond(bytesPerSecond);
  }
  
  public long getRateInBytesPerSecond()
  {
    return multiplexedConnectionOutputStream.getBytesPerSecond();
  }
  
  // public boolean isRunningAudio()
  // {
  // return !audioDataOutputStream.closed();
  // }
}