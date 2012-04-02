package com.nimo.wristband;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SKEvent {
	//Represent a single event returned by songkick //
	
	private double venueLat, venueLng;
	private String venueDisplayName;
	private String startDate;
	private String startTime;
	private JSONArray performanceArray;
	private JSONObject artistObject;
	private Vector<JSONObject> artists;
	private Vector<Band> bands;
	
	
	
	
	//constructor
	public SKEvent(JSONObject venue, JSONObject start, JSONArray performance){
		performanceArray = performance;
		artists = new Vector<JSONObject>();
		try {
			//Test if there are valid coordinates.  Else, return 0.0
			if( venue.get("lat") instanceof java.lang.Double){
				venueLat = venue.getDouble("lat");
				venueLng = venue.getDouble("lng");
			}
			else
				venueLat = venueLng = 0.0;
			
			venueDisplayName = venue.getString("displayName");
			startDate = start.getString("date");
			startTime = start.getString("time");
			for(int i = 0; i < performanceArray.length(); ++i){
				artistObject = performanceArray.getJSONObject(i);
				artists.add(artistObject);
			}
			
			//finally, populate bands vector
			bands = populateBands();
			//MainActivity.progress.incrementProgressBy(1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Vector<Band> populateBands(){
		//populate a vector of bands if they have bandcamp
		//Log.d("pop","Populating Bands");
		Vector<Band> temp = new Vector<Band>();
		//Log.v("total artists",String.valueOf(getArtistCount()));
		
		for(int i=0;i<getArtistCount();++i){
			//Log.v("band "+i,getArtistName(i));
			//Log.i("Longlat",venueLat+","+venueLng);
			Band tempBand = new Band(getArtistName(i), venueLat, venueLng);
			if(tempBand.isAvailable()){
				temp.add(tempBand);
			}
		}
		return temp;
	}
	
	//Properties
	public double getVenueLat() {
		return venueLat;
	}
	public double getVenueLng() {
		return venueLng;
	}
	public String getStartDate() {
		return startDate;
	}
	public String getStartTime() {
		return startTime;
	}
	public String getVenueName(){
		return venueDisplayName;
	}
	public int getArtistCount(){
		return artists.size();
	}
	public String getArtistName(int index){
		try {
			String bandName = artists.elementAt(index).getString("displayName");
			return bandName;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}
	public Vector<Band> getBands(){
		return bands;
	}
	
	
}
