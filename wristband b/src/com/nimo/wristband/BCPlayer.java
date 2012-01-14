package com.nimo.wristband;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.nimo.wristband.SongClick.Prep;

public class BCPlayer extends Activity{
	
	private String BCKEY = "baugihungrmordaafstanda";
	
	private String[] artList;
	private String[] streamList;
	public static String[] nameList;
	public static String[] venueList;
	public static String[] titleList;
	public static double[][] coordList;
	public static String[] timeList;
	public static long[] idList;
	public static VideoView mPlayer;
	public static CoverFlow coverFlow;
	
	public static TextView nameText;
	
	public static Context bcp;
	public static TextView dateText;
	public static MediaController mediaController1;
	public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bcplayer);
        
                
        artList = setArtList();
        streamList = setStreamList();
        nameList = setNameList();
        venueList = setVenueList();
        titleList = setTitleList();
        coordList = setCoordList();
        timeList = setTimeList();
        idList = setIdList();
        
                
        //coverFlow = new CoverFlow(this);
        coverFlow = (CoverFlow) findViewById(R.id.coverFlow);
        nameText = (TextView) findViewById(R.id.nameText);
        
        dateText = (TextView) findViewById(R.id.showDateText);
        
        mPlayer = (VideoView)findViewById(R.id.mPlayer);
        
        ImageAdapter coverImageAdapter =  new ImageAdapter(this);
        SongClick itemListener = new SongClick(streamList);
        Prep prepListener = itemListener.new Prep();
        coverFlow.setAdapter(new ImageAdapter(this));
        //coverFlow.setOnItemClickListener(itemListener);
        coverFlow.setOnItemLongClickListener(itemListener);
        coverFlow.setOnItemSelectedListener(itemListener.new ItemSelectListen());
        mPlayer.setOnCompletionListener(itemListener.new Next());
        mPlayer.setOnPreparedListener(prepListener);
        
        //Log.v("DATE", MainActivity.date);
        dateText.setText(MainActivity.date);
        coverFlow.setAdapter(coverImageAdapter);
        coverFlow.setSpacing(-25);
        coverFlow.setSelection(0, true);
        coverFlow.setAnimationDuration(1000);
        coverFlow.setMaxZoom(-350);
        coverFlow.setMinimumHeight(40);
        coverFlow.setMinimumWidth(40);
        
        
        mediaController1 = new MediaController(this);
        mPlayer.setMediaController(mediaController1);
        
        int i = BCPlayer.coverFlow.getSelectedItemPosition();
		String bn = BCPlayer.nameList[i];
		String sn = BCPlayer.titleList[i];
		String vn = BCPlayer.venueList[i];
		String tm = timeList[i];
		String desc;
		if(sn == null){
			if(!(tm == null))
				desc = bn + " at " + vn + " at " + tm;
			else
				desc = bn + " at " + vn;
		}

		else{
			if(!(tm == null))
				desc = bn + " - '" + sn + "' at " + vn + " at " + tm;
			else
				desc = bn + " - '" + sn + "' at " + vn;
		}
		BCPlayer.nameText.setText(desc);
        bcp = this.getApplicationContext();
        Toast instruction = Toast.makeText(bcp, "Long-press an image to play/stop", Toast.LENGTH_LONG);
        instruction.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.playermenu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = BCPlayer.coverFlow.getSelectedItemPosition();
	    switch (item.getItemId()) {
	        case R.id.mapItem:
	        	//Toast.makeText(this, "will be implemented on next version", Toast.LENGTH_LONG).show();
	        	//Show Map;
	        	
	        	//Intent m = new Intent(BCPlayer.this,Directions.class);
	        	//m.putExtra("lat", coordList[i][0]);
	        	//m.putExtra("lng", coordList[i][1]);
	        	//m.putExtra("position", i);
	        	//startActivity(m);
	        	
	        	//Launch Maps for directions
	        	double lat = coordList[i][0];
	        	double lng = coordList[i][0];
	        	if(!(lat == 0.0 && lng == 0.0)){
	        		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
	        		Uri.parse("http://maps.google.com/maps?saddr="+MainActivity.GPS[0]+","+MainActivity.GPS[1]+"&daddr="+lat+","+lng));
	        		startActivity(intent);
	        	}
	        	else{
	        		//No address
	        		Toast.makeText(this,"Sorry, there was no address provided for this show.",Toast.LENGTH_SHORT);
	        	}
	                            break;	        
	        case R.id.website: 
	        	//Toast.makeText(this, "This will take you to a website" +
	        	//		"", Toast.LENGTH_LONG).show();
	        	//int i = BCPlayer.coverFlow.getSelectedItemPosition();
	        	Log.i("bandcamp","opening intent");
	        	//Intent p = new Intent(BCPlayer.this,Profile.class);
	        	//p.putExtra("band_name", nameList[i]);
	        	//p.putExtra("band_id", idList[i]);
	        	//p.putExtra("album_art", artList[i]);
	        	//Log.d(nameList[i],String.valueOf(idList[i]));
	        	//startActivity(p);
	        	JSONObject bandInfo = UtilityBelt.retrieveJSON("http://api.bandcamp.com/api/band/3/info?key="+BCKEY+"&band_id="+idList[i]);
	        	
			Intent browserIntent;
			try {
				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bandInfo.getString("url")));
				startActivity(browserIntent);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        	
	                            break;
	        case R.id.about:
	        	Intent a = new Intent(BCPlayer.this,AboutActivity.class);
	    		startActivity(a);
	            break;
	    }
	    return true;
	}
	
	@Override
	public void onBackPressed(){
		mPlayer.stopPlayback();
		trimCache(this.getApplicationContext());
		//mPlayer.release();
		this.finish();
	}
	
	public static void trimCache(Context context) {
        try {
        	Log.d("cache trimmer", "deleting");
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);

            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public static boolean deleteDir(File dir) {
        if (dir!=null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
	
	
	private String[] setArtList(){
		//load up the huge art list
		int grandTotal = 0;
		for(int i = 0; i < MainActivity.allTheEvents.length;++i){
			grandTotal += MainActivity.allTheEvents[i].getBands().size();
		}
		String[] theList = new String[grandTotal];
		int i = 0;
		for(int e = 0; e < MainActivity.allTheEvents.length;++e){
			for(int b = 0; b < MainActivity.allTheEvents[e].getBands().size();++b){
				theList[i] = MainActivity.allTheEvents[e].getBands().elementAt(b).getImageUrl();
				++i;
			}
		}
		return theList;
	}
	
	private String[] setNameList(){
		//load up the huge names
		int grandTotal = 0;
		for(int i = 0; i < MainActivity.allTheEvents.length;++i){
			grandTotal += MainActivity.allTheEvents[i].getBands().size();
		}
		String[] theList = new String[grandTotal];
		int i = 0;
		for(int e = 0; e < MainActivity.allTheEvents.length;++e){
			for(int b = 0; b < MainActivity.allTheEvents[e].getBands().size();++b){
				theList[i] = MainActivity.allTheEvents[e].getBands().elementAt(b).getBandName();
				++i;
			}
		}
		return theList;
	}
	
	private String[] setTitleList(){
		//load up the huge names
		int grandTotal = 0;
		for(int i = 0; i < MainActivity.allTheEvents.length;++i){
			grandTotal += MainActivity.allTheEvents[i].getBands().size();
		}
		String[] theList = new String[grandTotal];
		int i = 0;
		for(int e = 0; e < MainActivity.allTheEvents.length;++e){
			for(int b = 0; b < MainActivity.allTheEvents[e].getBands().size();++b){
				theList[i] = MainActivity.allTheEvents[e].getBands().elementAt(b).getSongName();
				++i;
			}
		}
		return theList;
	}
	
	private String[] setVenueList(){
		//load up the huge art list
		int grandTotal = 0;
		for(int i = 0; i < MainActivity.allTheEvents.length;++i){
			grandTotal += MainActivity.allTheEvents[i].getBands().size();
		}
		String[] theList = new String[grandTotal];
		int i = 0;
		for(int e = 0; e < MainActivity.allTheEvents.length;++e){
			for(int b = 0; b < MainActivity.allTheEvents[e].getBands().size();++b){
				theList[i] = MainActivity.allTheEvents[e].getVenueName();
				++i;
			}
		}
		return theList;
	}
	
	private double[][] setCoordList(){
		//load up the array of coords
		int grandTotal = 0;
		for(int i = 0; i < MainActivity.allTheEvents.length;++i){
			grandTotal += MainActivity.allTheEvents[i].getBands().size();
		}
		double[][] theList = new double[grandTotal][2];
		int i = 0;
		double[] temp = new double[2];
		for(int e = 0; e < MainActivity.allTheEvents.length;++e){
			for(int b = 0; b < MainActivity.allTheEvents[e].getBands().size();++b){
				
				temp = MainActivity.allTheEvents[e].getBands().get(b).getCoordinates();
				
				theList[i] = temp;
				++i;
			}
		}
		return theList;
	}
	
	private String[] setStreamList(){
		//load up the huge stream list
		int grandTotal = 0;
		for(int i = 0; i < MainActivity.allTheEvents.length;++i){
			grandTotal += MainActivity.allTheEvents[i].getBands().size();
		}
		String[] theList = new String[grandTotal];
		int i = 0;
		for(int e = 0; e < MainActivity.allTheEvents.length;++e){
			for(int b = 0; b < MainActivity.allTheEvents[e].getBands().size();++b){
				theList[i] = MainActivity.allTheEvents[e].getBands().elementAt(b).getRandomSongUrl();
				++i;
			}
		}
		return theList;
	}
	
	private long[] setIdList(){
		//load up the huge band id list
		int grandTotal = 0;
		for(int i = 0; i < MainActivity.allTheEvents.length;++i){
			grandTotal += MainActivity.allTheEvents[i].getBands().size();
		}
		long[] theList = new long[grandTotal];
		int i = 0;
		for(int e = 0; e < MainActivity.allTheEvents.length;++e){
			for(int b = 0; b < MainActivity.allTheEvents[e].getBands().size();++b){
				theList[i] = MainActivity.allTheEvents[e].getBands().get(b).getBandId();
				++i;
			}
		}
		return theList;
	}
	
	private String[] setTimeList(){
		//load up the huge time list
		int grandTotal = 0;
		for(int i = 0; i < MainActivity.allTheEvents.length;++i){
			grandTotal += MainActivity.allTheEvents[i].getBands().size();
		}
		String[] theList = new String[grandTotal];
		int i = 0;
		for(int e = 0; e < MainActivity.allTheEvents.length;++e){
			for(int b = 0; b < MainActivity.allTheEvents[e].getBands().size();++b){
				theList[i] = MainActivity.allTheEvents[e].getStartTime();
				++i;
			}
		}
		return theList;
	}
	

	
	 public class ImageAdapter extends BaseAdapter {
	     int mGalleryItemBackground;
	     private Context mContext;

	     @SuppressWarnings("unused")
		private FileInputStream fis;
	        
	     //This has the album images from the web
	     private String[] albumArt = artList;

	     @SuppressWarnings("unused")
		private ImageView[] mImages;
	     
	     public ImageAdapter(Context c) {
	      mContext = c;
	      mImages = new ImageView[albumArt.length];
	     }

	     public int getCount() {
	         return albumArt.length;
	     }

	     public Object getItem(int position) {
	         return position;
	     }

	     public long getItemId(int position) {
	         return position;
	     }

	     public View getView(int position, View convertView, ViewGroup parent) {

	      //Use this code if you want to load from resources
	         ImageView i = new ImageView(mContext);
	         
	         try {
                 /* Open a new URL and get the InputStream to load data from it. */
                 URL aURL = new URL(albumArt[position]);
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
                 i.setImageBitmap(bm);
         } catch (IOException e) {
                 i.setImageResource(R.drawable.wbicon);
                 Log.e("DEBUGTAG", "Remtoe Image Exception", e);
         }
	         
	         
	         i.setLayoutParams(new CoverFlow.LayoutParams(130, 130));
	         i.setScaleType(ImageView.ScaleType.CENTER_INSIDE); 
	         
	         //Make sure we set anti-aliasing otherwise we get jaggies
	         BitmapDrawable drawable = (BitmapDrawable) i.getDrawable();
	         drawable.setAntiAlias(true);
	         return i;
	      
	      //return mImages[position];
	     }
	   /** Returns the size (0.0f to 1.0f) of the views 
	      * depending on the 'offset' to the center. */ 
	      public float getScale(boolean focused, int offset) { 
	        /* Formula: 1 / (2 ^ offset) */ 
	          return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset))); 
	      } 

	 }


}
