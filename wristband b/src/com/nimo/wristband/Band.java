package com.nimo.wristband;

import java.net.URLEncoder;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Band {
	/* This is one band, complete with show information and one song to stream from
	 * bandcamp
	 */
	public final String BC_KEY = "baugihungrmordaafstanda";
	private String bandNameString;
	private String bandNameUF;
	private long bandId;
	private long albumId;
	private String streamUrl;
	private String imageUrl;
	private boolean hasBandCamp = false;
	private boolean onlyHasTrack = false;
	private JSONObject anAlbum;
	private String songName;
	private double[] latlong;
	
	public String getBandName(){return bandNameUF;}
	public String getRandomSongUrl(){return streamUrl;}
	public String getImageUrl(){return imageUrl;}
	public boolean isAvailable(){return hasBandCamp;}
	public String getSongName(){return songName;}
	public long getBandId(){return bandId;}
	public double[] getCoordinates(){return latlong;}
	
	public Band(String bandName,double lat, double lng){
		Log.d("latlongpassed",lat+","+lng);
		latlong = new double[2];
		latlong[0] = lat;
		latlong[1] = lng;
		//format the band name for the web then search for the band id
		bandNameUF = bandName;
		//Log.v("Unformatted name",bandNameUF);
		bandNameString = formatForUrl(bandNameUF);
		//Log.v("formatted name",bandNameString);
		bandId = getBandId(bandName);
		Log.d("Band ID","done, now changing progress title");
		//MainActivity.progress.setTitle("Populating Playlist: "+bandName);
		MainActivity.progress.incrementProgressBy(1);
		if(hasBandCamp){
			albumId = getAlbumId(bandId);
			MainActivity.progress.incrementProgressBy(1);
			if(!onlyHasTrack && hasBandCamp){
				anAlbum = getAlbum(albumId);
				MainActivity.progress.incrementProgressBy(1);
				try {
					if(!anAlbum.isNull("small_art_url")){
						imageUrl = anAlbum.getString("small_art_url");
						MainActivity.progress.incrementProgressBy(1);
					}
					else{
						imageUrl = anAlbum.getString("large_art_url");
						MainActivity.progress.incrementProgressBy(1);
					}
					streamUrl = getRandomTrackUrl(anAlbum);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		else
			Log.e(bandNameUF,"no bandcamp");
		//If we got this far, the Band object succeeded
		Log.i(bandNameUF, "SUCCESS");
	}
	
		
	private String getRandomTrackUrl(JSONObject album){
		String theUrl = "";
		String thename = "";
		try {
			JSONArray tracks = album.getJSONArray("tracks");
			Random blah = new Random();
			if(tracks.isNull(0))
				hasBandCamp = false;
			else{
				JSONObject track = tracks.getJSONObject(blah.nextInt(tracks.length()));
				theUrl = track.getString("streaming_url");
				thename = track.getString("title");
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		songName = thename;
		return theUrl;
	}
	
	private JSONObject getAlbum(long albumId){
		String query = "http://api.bandcamp.com/api/album/2/info?key="+BC_KEY+"&album_id="+albumId;
		return apiGet(query);
	}
	
	private JSONObject apiGet(String theUrl){
		JSONObject theResponse = null;
		String resp = "";
		try {
	        HttpClient client = new DefaultHttpClient();  
	        
	        HttpGet get = new HttpGet(theUrl);
	        HttpResponse responseGet = client.execute(get);  
	        HttpEntity resEntityGet = responseGet.getEntity();
	        
	        if (resEntityGet != null) { 
	        			resp = EntityUtils.toString(resEntityGet);
	                    theResponse = new JSONObject(resp);
	                    Log.i("BandCamp Response",resp);
	                    
	                }
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
		return theResponse;
		
	
	}

	private long getBandId(String bandName){
		//get bandcamp band id from specific artist
		
		String query = "http://api.bandcamp.com/api/band/3/search?key="+BC_KEY+"&name="+bandNameString;
		long id = 0;
		Log.d("bandidurl", query);
		JSONObject result = apiGet(query);
		
		try {
			JSONArray results = result.getJSONArray("results");
			JSONObject first = results.optJSONObject(0);
			if(first == null){
				Log.d("BC","no bandcamp");
				hasBandCamp = false;
				id = 0;
				}
			else{
				id = first.getLong("band_id");
				hasBandCamp = true;
			}
			
			
		} catch (JSONException e) {
			
			e.printStackTrace();
			hasBandCamp = false;
		}
		return id;
	}

	private long getAlbumId(long bandId){
		//return random album id
		long id = 0;
		if(hasBandCamp){
			String query = "http://api.bandcamp.com/api/band/3/discography?key="+BC_KEY+"&band_id="+bandId;
			JSONObject result = apiGet(query);
			
			try {
				JSONArray discography = result.getJSONArray("discography");
				if(!discography.isNull(0))
				{
					Random blah = new Random();
					//int len = discography.length() - 1;
					int randIndex = blah.nextInt(discography.length());
					//--randIndex;
					Log.d("# of albums",String.valueOf(discography.length()));
					JSONObject album = discography.optJSONObject(randIndex);
					if(album.has("album_id"))
						id = album.getLong("album_id");
					else{
						//TODO need to get the track by track id
						onlyHasTrack = true;
						imageUrl = album.getString("small_art_url");
						streamUrl = getTrack(album.getLong("track_id"));
					}
				}
				else{
					hasBandCamp = false;
					Log.e(bandNameUF, "no bandcamp");
				}
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
			
		}
		return id;
	}
	
	private String getTrack(long trackId){
		String query = "http://api.bandcamp.com/api/track/1/info?key="+BC_KEY+"&track_id="+trackId;
		JSONObject track = apiGet(query);
		String url = "";
		try {
			url = track.getString("streaming_url");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}
	
	private String formatForUrl(String theString){
		
		return URLEncoder.encode(theString);
		
	}

}
