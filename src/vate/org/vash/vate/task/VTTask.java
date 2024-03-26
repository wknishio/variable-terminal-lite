package org.vash.vate.task;

import java.io.Closeable;
import java.io.IOException;

public abstract class VTTask implements Runnable, Closeable
{
  protected boolean stopped;
  private Thread taskThread;
  private Runnable next;
  
  public boolean isStopped()
  {
    return stopped;
  }
  
  public void setStopped(boolean stopped)
  {
    this.stopped = stopped;
  }
  
  public void interruptThread()
  {
    if (taskThread != null)
    {
      taskThread.interrupt();
    }
  }
  
  @SuppressWarnings("deprecation")
  public void stopThread()
  {
    if (taskThread != null)
    {
      try
      {
        // taskThread.stop();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public void joinThread()
  {
    if (taskThread != null)
    {
      try
      {
        taskThread.join();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  public boolean aliveThread()
  {
    if (taskThread != null)
    {
      return taskThread.isAlive();
    }
    return false;
  }
  
  public void startThread()
  {
    // setStopped(false);
    stopped = false;
    taskThread = new Thread(null, this, this.getClass().getSimpleName());
    taskThread.setDaemon(true);
    taskThread.start();
  }
  
  public void close() throws IOException
  {
    this.setStopped(true);
    if (taskThread != null)
    {
      try
      {
        taskThread.interrupt();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public void setNext(Runnable next)
  {
    this.next = next;
  }
  
  public VTTask addNext(VTTask task)
  {
    this.next = task;
    return task;
  }
  
  private final void next()
  {
    if (next != null)
    {
      try
      {
        next.run();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public final void run()
  {
    try
    {
      task();
    }
    catch (Throwable t)
    {
      
    }
    finally
    {
      next();
    }
  }
  
  public abstract void task();
}