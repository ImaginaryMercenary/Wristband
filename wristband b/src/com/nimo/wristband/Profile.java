package com.nimo.wristband;

import java.io.IOException;

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
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class Profile extends Activity {

	public String mBandName;
	public long mBandId;
	private String BCKEY = "baugihungrmordaafstanda";
	private Album[] mAlbums;
	
	TextView bandTitle;
	ImageView coverArt;
	ExpandableListView albumsList;
	
	VideoView videoView1;
	
	public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.profile);
        Bundle extras = this.getIntent().getExtras();
        populateList(extras.getLong("band_id"));
        
        bandTitle = (TextView)findViewById(R.id.bandTitle);
        coverArt = (ImageView)findViewById(R.id.coverArt);
        albumsList = (ExpandableListView)findViewById(R.id.albumsList);
        videoView1 = (VideoView)findViewById(R.id.videoView1);
        
        videoView1.setMediaController(new MediaController(this));
        
        albumsList.setAdapter(new MyExpandableListAdapter());
        bandTitle.setText(extras.getString("band_name"));
        Log.d("loaded","now about to get image");
        //coverArt.setImageURI(Uri.parse(extras.getString("album_art")));
        coverArt.setImageBitmap(UtilityBelt.bitmapFromNet(extras.getString("album_art")));
        //populateList(extras.getLong("band_id"));
        //Make the list
        albumsList.setOnGroupClickListener(new OnGroupClickListener(){
        	  public boolean onGroupClick(ExpandableListView arg0, View arg1,
                  int groupPosition, long arg3) {
        		  coverArt.setImageBitmap(UtilityBelt.bitmapFromNet(mAlbums[groupPosition].getArtUrl()));
              return false;
              }
        });
        albumsList.setOnChildClickListener(new OnChildClickListener(){

			public boolean onChildClick(ExpandableListView arg0, View arg1,
					int arg2, int arg3, long arg4) {
				
				return false;
			}
        	
        });
		

	}
	
	private void populateList(long bandId){
		
		String theQuery = "http://api.bandcamp.com/api/band/3/discography?key="+BCKEY+"&band_id="+bandId;
		Log.d("bc query", theQuery);
		HttpClient client = new DefaultHttpClient();
		
		HttpGet get = new HttpGet(theQuery);
		HttpResponse responseGet;
		try {
			//Retrieve the JSON discography
			responseGet = client.execute(get);
			HttpEntity resEntityGet = responseGet.getEntity();
			String res = EntityUtils.toString(resEntityGet);
			Log.d("JSON response",res);
			JSONObject discography = new JSONObject(res);
			JSONArray discArray = discography.getJSONArray("discography");
			mAlbums = new Album[discArray.length()];
			
			for(int i = 0; i < discArray.length(); ++i){
				//Fill the Album array
				mAlbums[i] = new Album(discArray.getJSONObject(i));
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
        
	}

	public class MyExpandableListAdapter extends BaseExpandableListAdapter{
		
		//private Album[] groups = mAlbums;

		public Object getChild(int groupPosition, int childPosition) {
			return mAlbums[groupPosition].getTrack(childPosition);
		}
		
		public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 64);

            TextView textView = new TextView(Profile.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public View getChildView(final int groupPosition, final int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			Log.i("list","expanded");
			//coverArt.setImageBitmap(UtilityBelt.bitmapFromNet((mAlbums[groupPosition].getArtUrl())));
			TextView textView = getGenericView();
			textView.setText(mAlbums[groupPosition].getTrack(childPosition).getTitle());
			textView.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					//Log.d("clicked child", String.valueOf(arg2)+" "+String.valueOf(arg3));
					Log.i(mAlbums[groupPosition].getTrack(childPosition).getTitle(), "clicked");
					coverArt.setImageBitmap(UtilityBelt.bitmapFromNet(mAlbums[groupPosition].getArtUrl()));
					//VideoView mp = new VideoView(Profile.this);
					videoView1.setVideoURI(Uri.parse(mAlbums[groupPosition].getTrack(childPosition).getTitle()));
					videoView1.requestFocus();
				}
				
			});
			// Need to poll bandcamp again for songs
			return textView;
		}

		public int getChildrenCount(int groupPosition) {
			return mAlbums[groupPosition].getTrackCount();
		}

		public Object getGroup(int groupPosition) {
			return mAlbums[groupPosition];
		}

		public int getGroupCount() {
			return mAlbums.length;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			Log.i("list","generating list");
						
			TextView textView = getGenericView();
			Log.i("album "+groupPosition, mAlbums[groupPosition].getAlbumTitle() + " " + mAlbums[groupPosition].getReleaseDate());
            textView.setText(mAlbums[groupPosition].getAlbumTitle() + " " + mAlbums[groupPosition].getReleaseDate());
            return textView;
			
		}

		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}

		public int getCount() {
			return mAlbums.length;
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getItemViewType(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		public void registerDataSetObserver(DataSetObserver arg0) {
			// TODO Auto-generated method stub
			
		}

		public void unregisterDataSetObserver(DataSetObserver arg0) {
			// TODO Auto-generated method stub
			
		}

		public boolean areAllItemsEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			return false;
		}

		public long getCombinedChildId(long groupId, long childId) {
			// TODO Auto-generated method stub
			return 0;
		}

		public long getCombinedGroupId(long groupId) {
			// TODO Auto-generated method stub
			return 0;
		}

		public void onGroupCollapsed(int groupPosition) {
			// TODO Auto-generated method stub
			
		}

		public void onGroupExpanded(int groupPosition) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
