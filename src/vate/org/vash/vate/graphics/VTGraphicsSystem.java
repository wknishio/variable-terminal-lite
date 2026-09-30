package org.vash.vate.graphics;

public class VTGraphicsSystem
{
  private static boolean initialized = false;
  
  static
  {
    if (!initialized)
    {
      initialize();
    }
  }
  
  public static final void initialize()
  {
    initialized = true;
  }
}