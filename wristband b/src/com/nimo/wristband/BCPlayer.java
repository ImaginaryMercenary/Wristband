package com.nimo.wristband;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;

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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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
	
	private ProgressDialog pdiag;
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
	
	private String databaseName;
	DatabaseHandler db;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bcplayer);
		
		databaseName = this.getIntent().getExtras().getString("dbName");
		db = new DatabaseHandler(this,databaseName);

		artList = new String[db.getNumberEntries()];
		streamList = new String[db.getNumberEntries()];
		nameList = new String[db.getNumberEntries()];
		venueList = new String[db.getNumberEntries()];
		titleList = new String[db.getNumberEntries()];
		coordList = new double[db.getNumberEntries()][2];
		idList = new long[db.getNumberEntries()];
		
		db.getAllData(nameList, idList, venueList, coordList, titleList, streamList, artList);
		//debug:
		for(int i = 0; i < nameList.length; ++i){
			Log.d("Band: ",nameList[i]);
		}
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
		//coverFlow.setSelection(0, true);
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
		String desc;
		if(sn == null){
			desc = bn + " at " + vn;
		}

		else{
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
			double lng = coordList[i][1];
			//List<Address> addresses = null; 
			//List<Address> locations = null;
			if(!(lat == 0.0 && lng == 0.0)){
				//	Geocoder geocoder = new Geocoder(this, Locale.getDefault());

				//	try {
				//		addresses = geocoder.getFromLocation(lat, lng, 1);
				//		locations = geocoder.getFromLocation(MainActivity.GPS[0], MainActivity.GPS[1], 1);
				//	} catch (IOException e) {
				//		// TODO Auto-generated catch block
				//		e.printStackTrace();
				//	}
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
						Uri.parse("http://maps.google.com/maps?saddr="+MainActivity.GPS[0]+","+MainActivity.GPS[1]+"&daddr="+lat+","+lng));
				startActivity(intent);
			}
			else{
				//No address
				Toast.makeText(BCPlayer.bcp,"Sorry, there was no address provided for this show.",Toast.LENGTH_SHORT).show();
			}
			break;	        
		case R.id.website: 
			//Toast.makeText(this, "This will take you to a website" +
			//		"", Toast.LENGTH_LONG).show();
			//int i = BCPlayer.coverFlow.getSelectedItemPosition();
			/* populate an array of Album objects and
			 * pass them to the profile
			 */
			pdiag = new ProgressDialog(this);
			
			class GetAlbumTask extends AsyncTask<Long,Integer,Album[]>{
				int i = BCPlayer.coverFlow.getSelectedItemPosition();
				Album[] albums = null;
				
				protected void onPreExecute(){
					pdiag.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					pdiag.setMessage("Loading discography from Bandcamp");
					pdiag.show();
				}
				@Override
				protected Album[] doInBackground(Long... bandId) {
					
					String theQuery = "http://api.bandcamp.com/api/band/3/discography?key="+BCKEY+"&band_id="+bandId[0];
					//Log.d("bc query", theQuery);
					HttpClient client = new DefaultHttpClient();
					
					HttpGet get = new HttpGet(theQuery);
					HttpResponse responseGet;
					try {
						//Retrieve the JSON discography
						responseGet = client.execute(get);
						HttpEntity resEntityGet = responseGet.getEntity();
						String res = EntityUtils.toString(resEntityGet);
						//Log.d("JSON response",res);
						JSONObject discography = new JSONObject(res);
						JSONArray discArray = discography.getJSONArray("discography");
						albums = new Album[discArray.length()];
						
						for(int i = 0; i < discArray.length(); ++i){
							//Fill the Album array
							albums[i] = new Album(discArray.getJSONObject(i));
						}
						
						
						
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
					
					return albums;
				}
				protected void onProgressUpdate(Integer... progress){
					pdiag.incrementProgressBy(progress[0]);
				}
				protected void onPostExecute(Album[] theAlbums){
					Log.i("bandcamp","opening intent");
					Intent p = new Intent(BCPlayer.this,Profile.class);
					p.putExtra("band_name", nameList[i]);
					p.putExtra("band_id", idList[i]);
					p.putExtra("album_art", artList[i]);
					p.putExtra("theAlbums", albums);
					//Bundle b = new Bundle();
					
					Log.d(nameList[i],String.valueOf(idList[i]));
					startActivity(p);
				}
			}
			GetAlbumTask gt = new GetAlbumTask();
			gt.execute(idList[i]);
			//------------------------

			/*JSONObject bandInfo = UtilityBelt.retrieveJSON("http://api.bandcamp.com/api/band/3/info?key="+BCKEY+"&band_id="+idList[i]);

			Intent browserIntent;
			try {
				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bandInfo.getString("url")));
				startActivity(browserIntent);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

			break;
		case R.id.about:
			Intent a = new Intent(BCPlayer.this,AboutActivity.class);
			startActivity(a);
			break;
		case R.id.shareItem:
			
			class GetUrlTask extends AsyncTask<Long, Void, String>{
				String bandUrl;
				int i;
				public GetUrlTask(int index){
					i = index;
				}
				@Override
				protected String doInBackground(Long... id) {
					JSONObject bandInfo = UtilityBelt.retrieveJSON("http://api.bandcamp.com/api/band/3/info?key="+BCKEY+"&band_id="+id[0]);

					try {
						return bandInfo.getString("url");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return "";
					}
				}
				@Override
				protected void onPostExecute(String result) {
					super.onPostExecute(result);
					bandUrl = result;

					String message = "Check out " + nameList[i] + " playing " +
							compareDate(MainActivity.date) + " at " + venueList[i] + ". " +
							bandUrl + "  (Wristband for Android)";
					Intent share = new Intent(Intent.ACTION_SEND);
					share.setType("text/plain");
					share.putExtra(Intent.EXTRA_TEXT, message);

					startActivity(Intent.createChooser(share, "Share " + nameList[i] + "..."));
				}
			}
			GetUrlTask gut = new GetUrlTask(i);
			gut.execute(idList[i]);
			break;
		}
		return true;
	}
	
	private String compareDate(String date){
		Calendar today = Calendar.getInstance();
		int[] todayParts = {today.get(Calendar.MONTH)+1,today.get(Calendar.DATE),today.get(Calendar.YEAR)};
		String[] dateParts = date.split("/");
		String response = null;
		if(Integer.parseInt(dateParts[0]) == todayParts[0] &&
			Integer.parseInt(dateParts[1]) == todayParts[1] &&
			Integer.parseInt(dateParts[2]) == todayParts[2]){
			//today
			response = "today";
		}else{
			response = date;
		}
		return response;
	}

	@Override
	public void onBackPressed(){
		mPlayer.stopPlayback();
		trimCache(this.getApplicationContext());
		//mPlayer.release();
		db.close();
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


	/*private String[] setArtList(){
		
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
	}*/



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
			final ImageView i = new ImageView(mContext);

			/*try {
                  Open a new URL and get the InputStream to load data from it. 
                 URL aURL = new URL(albumArt[position]);
                 URLConnection conn = aURL.openConnection();
                 conn.connect();
                 InputStream is = conn.getInputStream();
                  Buffered is always good for a performance plus. 
                 BufferedInputStream bis = new BufferedInputStream(is);
                  Decode url-data to a bitmap. 
                 Bitmap bm = BitmapFactory.decodeStream(bis);
                 bis.close();
                 is.close();
                  Apply the Bitmap to the ImageView that will be returned. 
                 i.setImageBitmap(bm);
         } catch (IOException e) {
                 i.setImageResource(R.drawable.wbicon);
                 Log.e("DEBUGTAG", "Remtoe Image Exception", e);
         }*/
			class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
				protected Bitmap doInBackground(String... urls) {
					return UtilityBelt.bitmapFromNet(urls[0]);
				}

				protected void onPostExecute(Bitmap result) {
					if(result != null)
						i.setImageBitmap(result);
					else
						i.setImageResource(R.drawable.wbicon);
				}
			}
			new DownloadImageTask().execute(albumArt[position]);

			i.setLayoutParams(new CoverFlow.LayoutParams(130, 130));
			i.setScaleType(ImageView.ScaleType.CENTER_INSIDE); 

			//Make sure we set anti-aliasing otherwise we get jaggies
			//BitmapDrawable drawable = (BitmapDrawable) i.getDrawable();
			//drawable.setAntiAlias(true);
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
