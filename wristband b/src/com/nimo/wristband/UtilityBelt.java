package com.nimo.wristband;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class UtilityBelt {
	
	/* An Android class of useful shit I don't want to write
	 * over and over again
	 */
	
	public static boolean isDataConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {

            networkInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!networkInfo.isAvailable()) {
                networkInfo = connectivityManager
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            }
        }
        return networkInfo == null ? false : networkInfo.isConnected();
    }
	
	public static Bitmap bitmapFromNet(String url){
		//Load bitmap from the internet
		 try {
             /* Open a new URL and get the InputStream to load data from it. */
             URL aURL = new URL(url);
             URLConnection conn = aURL.openConnection();
             conn.connect();
             InputStream is = conn.getInputStream();
             /* Buffered is always good for a performance plus. */
             BufferedInputStream bis = new BufferedInputStream(is);
             /* Decode url-data to a bitmap. */
             Bitmap bm = BitmapFactory.decodeStream(bis);
             bis.close();
             is.close();
             /* Apply the Bitmap to the ImageView that will be returned. */
             return bm;
     } catch (IOException e) {
             
             Log.e("DEBUGTAG", "Remtoe Image Exception", e);
             return null;
     }
	}
	
	

}
