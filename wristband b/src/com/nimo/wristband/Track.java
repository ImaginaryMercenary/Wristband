package com.nimo.wristband;

import org.json.JSONObject;

public class Track {
	
	private String mTitle;
	private String mStreamingUrl;
	
	public Track(JSONObject track){
		mTitle = track.optString("title");
		mStreamingUrl = track.optString("streaming_url");
	}
	
	public String getTitle(){
		return mTitle;
	}
	
	public String getStreamingUrl(){
		return mStreamingUrl;
	}

}
