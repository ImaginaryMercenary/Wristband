package com.nimo.wristband;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

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
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
	
	public final String SK_KEY = "plPE4X1rCdADL3A3";
	
	//TextView mainTextView;
	EditText cityText;
	EditText dateText;
	ImageView buttonImage;
	Calendar calendar;
	public static SKEvent[] allTheEvents;
	public static ProgressDialog progress;
	public static boolean isReady = false;
	private Intent i;
	public static String date;
	public static double[] GPS;
	DatePickerDialog dateDialog;
	Geocoder geocoder;
	private DatabaseHandler db;
	private String databaseName;
				
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.newmain);
                        
        cityText = (EditText)findViewById(R.id.cityText);
        cityText.setEnabled(false);
        cityText.setFocusable(false);
        dateText = (EditText)findViewById(R.id.dateText);
        dateText.setOnClickListener(new DateListener());
        buttonImage = (ImageView)findViewById(R.id.buttonImage);
        
        progress = new ProgressDialog(this);
        BCPlayer.trimCache(getApplicationContext());
        
        calendar = Calendar.getInstance();
        
        //Get the date and the city name to fill the
        //edit text views
        
        
        buttonImage.setOnClickListener(this);
        
        if(UtilityBelt.haveInternet(getApplicationContext()))
        	setEverythingUp();
        else{
        	Toast.makeText(this, "Wristband requires a data connection. Check your settings and try again.", Toast.LENGTH_LONG).show();
        }
        	
    }
    
    public class CityListener implements OnClickListener{

		public void onClick(View arg0) {
			//try GPS again
			getGPS();
			//Log.i("gps", GPS[0]+","+GPS[1]);
	        List<Address> addresses = null;
	        if(GPS != null){
	        	geocoder = new Geocoder(arg0.getContext());
	        	try {
	        		addresses = geocoder.getFromLocation(GPS[0], GPS[1], 1);
	        	} catch (IOException e) {
	        		//Error, start again
	        		Toast.makeText(getApplicationContext(), "There was an error retrieving your city, but I have your GPS coordinates. You can continue.",Toast.LENGTH_LONG).show();
	        		//onCreate(savedInstanceState);
	        		
	        	}
	        }
	        if(addresses != null)
	        {
	        	int ndx = addresses.get(0).getMaxAddressLineIndex();
	        	cityText.setText(addresses.get(0).getAddressLine(ndx-1));
	        }
		}
    	
    }
    
    public class DateListener implements OnClickListener, DatePickerDialog.OnDateSetListener{

		public void onClick(View v) {
			// open a date dialog and
			// return the date.
			dateDialog = new DatePickerDialog(v.getContext(), this, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE));
			dateDialog.show(); 
			
		}

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			//date = String.valueOf(year) + "-" + String.valueOf(monthOfYear+1) + "-" + String.valueOf(dayOfMonth);
			dateText.setText(formatDate(year,monthOfYear+1,dayOfMonth));
			
			
		}
		
		private String formatDate(int year, int month, int day){
			int D = day;
			//Log.i("D",String.valueOf(D));
			int M = month;
			//Log.i("M",String.valueOf(M));
			int Y = year;
			//Log.i("Y",String.valueOf(Y));
			String MM = "0" + M;
		    MM = MM.substring(MM.length()-2, MM.length()); 
		    String DD = "0" + D; 
		    DD = DD.substring(DD.length()-2, DD.length()); 
		    
			String theDate = String.valueOf(Y) + "-" + MM + "-" + DD;
			date = MM + "/" + DD + "/" + String.valueOf(Y);
			return theDate;
			
		}
    	
    }
    
    
    private void setEverythingUp(){
    	getGPS();
        List<Address> addresses = null;
        if(GPS != null){
        	geocoder = new Geocoder(this.getApplicationContext(), Locale.getDefault());
        	try {
        		addresses = geocoder.getFromLocation(GPS[0], GPS[1], 1);
        	} catch (IOException e) {
        		//Error, start again
        		Toast.makeText(getApplicationContext(), "There was an error retrieving your city, but I have your GPS coordinates. You can continue.",Toast.LENGTH_SHORT).show();
        		//onCreate(savedInstanceState);
        		
        	}
        }
        if(addresses != null)
        {
        	int ndx = addresses.get(0).getMaxAddressLineIndex();
        	cityText.setText(addresses.get(0).getAddressLine(ndx-1));
        }
        //date = getTodaysDate();
        dateText.setText(getTodaysDate());
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mainmenu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.mainAbout:
	        	Intent intent = new Intent(MainActivity.this,AboutActivity.class);
	    		startActivity(intent);
	                            break;
	        
	    }
	    return true;
	}

        
    private String makeQueryString(String date, String coords){
		
		String searchString = "http://api.songkick.com/api/3.0/events.json?apikey=" + SK_KEY + "&min_date=" 
		+ date + "&max_date=" + date + "&location=geo:" + coords;
		
		return searchString;
	}
    
    private String getCoordString(double[] loc){
		
		double lat = loc[0];
		double lon = loc[1];
		
		String theFix = String.valueOf(lat) + "," + String.valueOf(lon);
		return theFix;
	}

    @Override
    protected void onResume() {
      super.onResume();
      
    }

    @Override
    protected void onStop() {
      
      super.onStop();
    }

	public boolean onLongClick(View v) {
		// Search
		
		if(UtilityBelt.haveInternet(getApplicationContext())){
			//Data is enabled. Proceed.
			getGPS();
			String theQuery = makeQueryString(getTodaysDate(),getCoordString(GPS));
			new RetrieveData().execute(theQuery);
			return true;
		}
		else{
			//Data disabled. Warn user.
			String disco = "A connection to the internet is required to proceed. Please check " +
					"your wireless settings and try again.";
			
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;

			Toast.makeText(context, disco, duration).show();
			return false;
		}
	}
    
	
	private String getTodaysDate(){
		int D = calendar.get(Calendar.DATE);
		//Log.i("D",String.valueOf(D));
		int M = calendar.get(Calendar.MONTH) + 1;
		//Log.i("M",String.valueOf(M));
		int Y = calendar.get(Calendar.YEAR);
		//Log.i("Y",String.valueOf(Y));
		String MM = "0" + M;
	    MM = MM.substring(MM.length()-2, MM.length()); 
	    String DD = "0" + D; 
	    DD = DD.substring(DD.length()-2, DD.length()); 
	    
		String theDate = String.valueOf(Y) + "-" + MM + "-" + DD;
		date = MM + "/" + DD + "/" + String.valueOf(Y);
		return theDate;
		
	}
	
	public void getGPS() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
		List<String> providers = lm.getProviders(true);

		/* Loop over the array backwards, and if you get an accurate location, then break                 out the loop*/
		Location l = null;

		for (int i=providers.size()-1; i>=0; i--) {
		l = lm.getLastKnownLocation(providers.get(i));
		if (l != null) break;
		}

		double[] gps = new double[2];
		if (l != null) {
		gps[0] = l.getLatitude();
		////Log.i("lat",String.valueOf(gps[0]));
		gps[1] = l.getLongitude();
		////Log.i("long",String.valueOf(gps[1]));
		GPS = gps;
		}
		else
		{
			//Coarse location returned null
			//Log.d("location", "coarse null, looking for fine");
			getGPSFine();
		}
		
				
	}
	
	public void getGPSFine(){
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
				
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		      retCoords(location);
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {
		    	/* location provider is disabled. Warn the user
		    	 * and wait for it to be enabled.
		    	 */
		    	String disco = "Could not get a fix on your location. Please check your " +
		    			"settings and make sure either GPS or Wi-fi location is enabled.";
				
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;

				Toast.makeText(context, disco, duration).show();
				
		    }
		  };
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}
	public void retCoords(Location location){
    	double[] gps = new double[2];
    	gps[0] = location.getLatitude();
    	gps[1] = location.getLongitude();
    	GPS = gps;
    }
	
	private void SwitchNow(){
		i = new Intent(MainActivity.this,BCPlayer.class);
		i.putExtra("dbName", databaseName);
		startActivity(i);
	}
	
	//DATA RETRIEVAL CLASS ************
public class RetrieveData extends AsyncTask<String, Integer, SKEvent[]>{
		
		//ProgressDialog progress;
		int MAX;

		protected SKEvent[] doInBackground(String... args) {
			
			//Log.v("query",args[0]);
			SKEvent[] myShows = null;
			try {
				myShows = sendRequest(args[0]);
				//allTheEvents = myShows;		
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return myShows;
		}
		
		
		
		
		
		private SKEvent[] sendRequest(String theUrl) throws ClientProtocolException, IOException, JSONException{
			
				HttpClient client = new DefaultHttpClient();
				
				HttpGet get = new HttpGet(theUrl);
				HttpResponse responseGet = client.execute(get);
				HttpEntity resEntityGet = responseGet.getEntity();
				SKEvent[] allShows;
				
				if (resEntityGet != null) {
					// do something with the response
					String resp = EntityUtils.toString(resEntityGet);
					//Log.i("GET RESPONSE", resp);
					JSONObject skResults = new JSONObject(resp);
					JSONObject resultsObject = skResults.getJSONObject("resultsPage");
					//need to continue extracting pertinent attributes
					int totalEntries = resultsObject.getInt("totalEntries");
					/* HACK:
					 * For now must set totalEntries to 50 or will get an error
					 * until I can figure out how to move on the next page
					 * of shows
					 */
					if(totalEntries > 50)
						totalEntries = 50;
					/* Remember to change that
					 * *******************************
					 */
					
					//PopupWindow pw = new PopupWindow(this.getLayoutInflater().inflate(R.layout.showprogress, null), 100, 100, true);
					//pw.showAtLocation(this.findViewById(R.id.theRoot), Gravity.CENTER, 0, 0);
					
					MAX = totalEntries * 6;
					MainActivity.progress.setMax(MAX);
					JSONObject results = resultsObject.getJSONObject("results");
					JSONArray eventArray = results.getJSONArray("event");
					
					//Create Vector of eventObject objects to hold all the shows
					Vector<JSONObject> allEvents = new Vector<JSONObject>();
					for(int i = 0; i < totalEntries; ++i){
						//Log.i("index",String.valueOf(i));
						JSONObject eventObject = eventArray.getJSONObject(i);
						allEvents.add(eventObject);
						MainActivity.progress.incrementProgressBy(1);
					}
					
					//Use the vector to create an array of SKEvents which will represent all the
					//show data we need for this day
					allShows = new SKEvent[totalEntries];
					for (int i = 0; i < totalEntries; ++i){
						////Log.i("index",String.valueOf(i));
						allShows[i] = new SKEvent(allEvents.elementAt(i).getJSONObject("venue"),allEvents.elementAt(i).getJSONObject("start"),allEvents.elementAt(i).getJSONArray("performance"));
						MainActivity.progress.incrementProgressBy(1);
					}
					//pw.dismiss();
					return allShows;
				}
				else
					return null;
				//return allShows;
	    
		}
		
		@Override
		protected void onPreExecute() {
			//progress = MainActivity.progress;
			MainActivity.progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			MainActivity.progress.setTitle("Searching for shows. This may take a moment...");
			MainActivity.progress.setMax(50);
			MainActivity.progress.show();
		}
		
		protected void onPostExecute(SKEvent[] result){
			if(result != null){
				MainActivity.allTheEvents = result;
				//add all the data to the database
				for(int e = 0; e < MainActivity.allTheEvents.length;++e){
					for(int b = 0; b < MainActivity.allTheEvents[e].getBands().size();++b){
						db.addEntry(allTheEvents[e].getBands().elementAt(b).getBandName(),
								allTheEvents[e].getBands().elementAt(b).getBandId(),
								allTheEvents[e].getVenueName(),
								allTheEvents[e].getVenueLat(), 
								allTheEvents[e].getVenueLng(),
								allTheEvents[e].getBands().elementAt(b).getSongName(),
								allTheEvents[e].getBands().elementAt(b).getRandomSongUrl(),
								allTheEvents[e].getBands().elementAt(b).getImageUrl());
					}
				}
				MainActivity.progress.dismiss();
				MainActivity.isReady = true;
				SwitchNow();
			}
			else
				Toast.makeText(getBaseContext(), "Sorry, I could not find any shows for this particular area.", Toast.LENGTH_LONG).show();
		}
		
		protected void onProgressUpdate(int integers) {
			MainActivity.progress.incrementProgressBy(integers);
		 }


	}

public void onClick(View arg0) {
	//The button was clicked, execute
	
	databaseName = dateText.getText().toString();

	if (!DatabaseHandler.checkDataBase(databaseName)) {
	    // Database does not exist so copy it from assets here
	    Log.i("Database", "Not Found");
	    db = new DatabaseHandler(this,databaseName);
		
		if(UtilityBelt.haveInternet(getApplicationContext()) && GPS != null){
			//Data is enabled. Proceed.
			
			String theQuery = makeQueryString(dateText.getText().toString(),getCoordString(GPS));
			new RetrieveData().execute(theQuery);
			//return true;
		}
		else{
			//Data disabled. Warn user.
			String disco = "A connection to the internet is required to proceed. Please check " +
					"your wireless settings and try again.";
			
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;

			Toast.makeText(context, disco, duration).show();
			//return false;
		}
	} else {
	    Log.i("Database", "Found");
	    //go directly to BCPlayer
	    db = new DatabaseHandler(this,databaseName);
	    isReady = true;
	    SwitchNow();
	}
	
	
	
}
}