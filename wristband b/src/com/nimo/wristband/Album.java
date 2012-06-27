package com.nimo.wristband;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable{
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
	private String BCKEY = "baugihungrmordaafstanda";
	private JSONObject jsonAlbum;
	private JSONObject jsonAlbumTracks;

	public Album(JSONObject album){
		jsonAlbum = album;
		init(album);
	}

	public Album(Parcel in){
		try {
			init(new JSONObject(in.readString()), new JSONObject(in.readString()));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void init(JSONObject album, JSONObject albumTracks){
		//parse the JSON
		releaseDate = album.optLong("release_date");
		artUrl = album.optString("small_art_url");
		albumTitle = album.optString("title");
		albumId = album.optLong("album_id");
		albumUrl = album.optString("url");
		JSONObject jsonalbum = albumTracks;
		//jsonAlbumTracks = jsonalbum;
		try {
			JSONArray trackArray = jsonalbum.getJSONArray("tracks");
			//Log.d("Tracks", "length "+String.valueOf(trackArray.length()));
			tracks = new Track[trackArray.length()];
			for(int i=0;i<trackArray.length();++i){
				tracks[i] = new Track(trackArray.getJSONObject(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void init(JSONObject album){
		//parse the JSON
		releaseDate = album.optLong("release_date");
		artUrl = album.optString("small_art_url");
		albumTitle = album.optString("title");
		albumId = album.optLong("album_id");
		albumUrl = album.optString("url");
		JSONObject jsonalbum = UtilityBelt.retrieveJSON("http://api.bandcamp.com/api/album/2/info?key="+BCKEY+"&album_id="+albumId);
		jsonAlbumTracks = jsonalbum;
		try {
			JSONArray trackArray = jsonalbum.getJSONArray("tracks");
			//Log.d("Tracks", "length "+String.valueOf(trackArray.length()));
			tracks = new Track[trackArray.length()];
			for(int i=0;i<trackArray.length();++i){
				tracks[i] = new Track(trackArray.getJSONObject(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Track getTrack(int i){
		return tracks[i];
	}

	public int getTrackCount(){
		if(tracks != null)
			return tracks.length;
		else
			return 0;
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

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(jsonAlbum.toString());
		dest.writeString(jsonAlbumTracks.toString());
	}

	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR =
			new Parcelable.Creator() {
		public Album createFromParcel(Parcel in) {
			return new Album(in);
		}

		public Album[] newArray(int size) {
			return new Album[size];
		}
	};

}
