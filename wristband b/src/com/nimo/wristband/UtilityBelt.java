package com.nimo.wristband;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	
	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * @param ctx
	 * @return True if device has internet
	 *
	 * Code from: http://www.androidsnippets.org/snippets/131/
	 */
	public static boolean haveInternet(Context ctx) {

	    NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
	            .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

	    if (info == null || !info.isConnected()) {
	        return false;
	    }
	    if (info.isRoaming()) {
	        // here is the roaming option you can change it if you want to
	        // disable internet while roaming, just return false
	        return false;
	    }
	    return true;
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
	
	public static JSONObject retrieveJSON(String query){
		String theQuery = query;
		Log.d("bc query", theQuery);
		HttpClient client = new DefaultHttpClient();
		
		HttpGet get = new HttpGet(theQuery);
		HttpResponse responseGet;
		JSONObject response = null;
		try {
			//Retrieve the JSON 
			responseGet = client.execute(get);
			HttpEntity resEntityGet = responseGet.getEntity();
			String res = EntityUtils.toString(resEntityGet);
			Log.d("JSON response",res);
			response = new JSONObject(res);
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// check if the data is enabled
			e.printStackTrace();
		} catch (JSONException e) {
			// JSON parsing error
			e.printStackTrace();
		}
		
		return response;
	}
	
	

}
