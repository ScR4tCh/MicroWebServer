package org.scratch.microwebserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import android.util.Log;

public class RootCommands
{
  public static boolean canRunRootCommands()
  {
    boolean retval = false;
    Process suProcess;
    
    try
    {
      suProcess = Runtime.getRuntime().exec("su");
      
      DataOutputStream os = 
          new DataOutputStream(suProcess.getOutputStream());
      BufferedReader osRes = 
          new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
      
      if (null != os && null != osRes)
      {
        // Getting the id of the current user to check if this is root
        os.writeBytes("id\n");
        os.flush();

        String currUid = osRes.readLine();
        boolean exitSu = false;
        if (null == currUid)
        {
          retval = false;
          exitSu = false;
          Log.d("ROOT", "Can't get root access or denied by user");
        }
        else if (true == currUid.contains("uid=0"))
        {
          retval = true;
          exitSu = true;
          Log.d("ROOT", "Root access granted");
        }
        else
        {
          retval = false;
          exitSu = true;
          Log.d("ROOT", "Root access rejected: " + currUid);
        }

        if (exitSu)
        {
          os.writeBytes("exit\n");
          os.flush();
        }
      }
    }
    catch (Exception e)
    {
      // Can't get root !
      // Probably broken pipe exception on trying to write to output
      // stream after su failed, meaning that the device is not rooted
      
      retval = false;
      Log.d("ROOT", "Root access rejected [" +
            e.getClass().getName() + "] : " + e.getMessage());
    }

    return retval;
  }
  
  public static final boolean execute(String command,final StringBuffer out,final StringBuffer err)
  {
	  Vector<String> c = new Vector<String>();
	  c.add(command);
	  return execute(c,out,err);
  }
  
  public static final boolean execute(Vector<String> commands,final StringBuffer out,final StringBuffer err)
  {
    boolean retval = false;
    
    try
    {
      if (null != commands && commands.size() > 0)
      {
        Process suProcess = Runtime.getRuntime().exec("su");
        
        DataOutputStream os = 
            new DataOutputStream(suProcess.getOutputStream());

        // Execute commands that require root access
        for (String currCommand : commands)
        {
          os.writeBytes(currCommand + "\n");
          os.flush();
        }

        os.writeBytes("exit\n");
        os.flush();

        try
        {
          int suProcessRetval = suProcess.waitFor();
          if (255 != suProcessRetval)
          {
            // Root access granted
            retval = true;
            
            if(out!=null)
            {
          	  BufferedReader bin = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
          	  int read;
          	  char[] buffer = new char[4096];
          	  while ((read = bin.read(buffer)) > 0)
          	  {
          	       out.append(buffer, 0, read);
          	  }
          	  bin.close();
            }
            
            if(err!=null)
            {
          	  BufferedReader bin = new BufferedReader(new InputStreamReader(suProcess.getErrorStream()));
          	  int read;
          	  char[] buffer = new char[4096];
          	  while ((read = bin.read(buffer)) > 0)
          	  {
          	       err.append(buffer, 0, read);
          	  }
          	  bin.close();
            }
          }
          else
          {
            // Root access denied
            retval = false;
          }
        }
        catch (Exception ex)
        {
          Log.e("Error executing root action", ex.getMessage());
        }
        
        suProcess.getErrorStream().close();
        suProcess.getInputStream().close();
      }
    }
    catch (IOException ex)
    {
      Log.w("ROOT", "Can't get root access", ex);
    }
    catch (SecurityException ex)
    {
      Log.w("ROOT", "Can't get root access", ex);
    }
    catch (Exception ex)
    {
      Log.w("ROOT", "Error executing internal operation", ex);
    }
    
    return retval;
  }
  
}