package com.nimo.wristband;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Album {
	/* Takes a JSONObject from the bandcamp
	 * discography API and turns it into a nice
	 * Java object
	 */
	private long releaseDate;
	private String artUrl;
	private String albumTitle;
	private long albumId;
	private String albumUrl;
	private Track[] tracks;
	
	public Album(JSONObject album){
		//parse the JSON
		releaseDate = album.optLong("release_date");
		artUrl = album.optString("small_art_url");
		albumTitle = album.optString("title");
		albumId = album.optLong("album_id");
		albumUrl = album.optString("url");
		try {
			tracks = new Track[album.getJSONArray("tracks").length()];
			for(int i = 0; i < tracks.length; ++i){
				tracks[i] = new Track(album.getJSONArray("tracks").optJSONObject(i));
			}
		} catch (JSONException e) {
			tracks = null;
		}
	}
	
	public Track getTrack(int i){
		return tracks[i];
	}
	
	public String getReleaseDate(){
		Date date = new Date(releaseDate);
		return String.valueOf(date.getMonth()+1) + "/" + String.valueOf(date.getDate()) + "/" + String.valueOf(date.getYear());
	}
	
	public String getArtUrl(){
		return artUrl;
	}
	
	public String getAlbumTitle(){
		return albumTitle;
	}
	
	public long getAlbumId(){
		return albumId;
	}
	
	public String getAlbumUrl(){
		return albumUrl;
	}

}
