package org.scratch.microwebserver.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.scratch.microwebserver.MicrowebserverActivity;
import org.scratch.microwebserver.data.Cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AndroidImageResolver
{

	public static final String CACHEID="/@ANDROID-DRAWABLE:";

	// will return a total path (cache !)
	public static String resolveCachedAndroidImage(int resid) throws IOException
	{
		if(Cache.getInstance().has(CACHEID+resid))
		{
			return CACHEID+resid;
		}
		else
		{
			Bitmap b=BitmapFactory.decodeResource(MicrowebserverActivity.getAppContext().getResources(),resid);

			ByteArrayOutputStream out=new ByteArrayOutputStream();
			b.compress(Bitmap.CompressFormat.PNG,100,out);
			Cache.getInstance().cache(CACHEID+resid,out.toByteArray());

			return CACHEID+resid;
		}
	}

	// will return a total path (cache !)
	public static String resolveCachedAndroidImagePath(int resid) throws IOException
	{
		if(Cache.getInstance().has(CACHEID+resid))
		{
			return Cache.getInstance().getCached(CACHEID+resid).getAbsolutePath();
		}
		else
		{
			Bitmap b=BitmapFactory.decodeResource(MicrowebserverActivity.getAppContext().getResources(),resid);

			ByteArrayOutputStream out=new ByteArrayOutputStream();
			b.compress(Bitmap.CompressFormat.PNG,100,out);
			Cache.getInstance().cache(CACHEID+resid,out.toByteArray());

			return Cache.getInstance().getCached(CACHEID+resid).getPath();
		}
	}
}
