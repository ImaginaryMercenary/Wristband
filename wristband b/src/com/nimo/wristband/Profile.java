package com.nimo.wristband;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	MediaController mController;
	ProgressDialog mProgress;
	
	String bname;
	String arturl;
	
	public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.profile);
        Bundle extras = this.getIntent().getExtras();
        
        bandTitle = (TextView)findViewById(R.id.bandTitle);
        coverArt = (ImageView)findViewById(R.id.coverArt);
        albumsList = (ExpandableListView)findViewById(R.id.albumsList);
        videoView1 = (VideoView)findViewById(R.id.videoView1);
        videoView1.setMediaController(mController);
        mController = new MediaController(this);
        videoView1.setMediaController(mController);
        albumsList.setBackgroundColor(Color.TRANSPARENT);
        albumsList.setCacheColorHint(Color.TRANSPARENT);
        
        
        ProgressDialog mProgress = new ProgressDialog(this,ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setTitle("Wristband");
        mProgress.setMessage("Retrieving discography from Bandcamp");
        bname = extras.getString("band_name");
        arturl = extras.getString("album_art");
        mBandId = extras.getLong("band_id");
        Parcelable pAlbums[] = extras.getParcelableArray("theAlbums");
        mAlbums = new Album[pAlbums.length];
        for(int i=0;i<pAlbums.length;++i){
        	mAlbums[i] = (Album) pAlbums[i];
        }
        //mProgress.show();
        //new PopulateListTask().execute(extras.getLong("band_id"));
        //mProgress.dismiss();
        //populateList(extras.getLong("band_id"));
        continuePopulate();
		

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.profilemenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case R.id.bandcampItem:
			class GetUrlTask extends AsyncTask<Long, Void, String>{
				@Override
				protected String doInBackground(Long... id) {
					JSONObject bandInfo = UtilityBelt.retrieveJSON("http://api.bandcamp.com/api/band/3/info?key="+BCKEY+"&band_id="+id[0]);

					try {
						return bandInfo.getString("url");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return "http://www.bandcamp.com";
					}
				}
				@Override
				protected void onPostExecute(String result) {
					super.onPostExecute(result);
					Log.d("url",result);
					Intent browserIntent;
					browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result));
					startActivity(browserIntent);
				}
			}
			new GetUrlTask().execute(mBandId);
			break;
		}
		return true;
	}
	
	private void continuePopulate(){
		albumsList.setAdapter(new MyExpandableListAdapter());
        bandTitle.setText(bname);
        //Log.d("loaded","now about to get image");
        //coverArt.setImageURI(Uri.parse(extras.getString("album_art")));
        class BMT extends AsyncTask<String,Void,Bitmap>{

			@Override
			protected Bitmap doInBackground(String... url) {
				return UtilityBelt.bitmapFromNet(url[0]);
			}
        	protected void onPostExecute(Bitmap b){
        		coverArt.setImageBitmap(b);
        	}
        }
        new BMT().execute(arturl);
        //coverArt.setImageBitmap();
        //populateList(extras.getLong("band_id"));
        //Make the list
        albumsList.setOnGroupClickListener(new OnGroupClickListener(){
        	  public boolean onGroupClick(ExpandableListView arg0, View arg1,
                  int groupPosition, long arg3) {
        		  
        		  class BMTClick extends AsyncTask<String,Void,Bitmap>{

        				@Override
        				protected Bitmap doInBackground(String... url) {
        					return UtilityBelt.bitmapFromNet(url[0]);
        				}
        	        	protected void onPostExecute(Bitmap b){
        	        		coverArt.setImageBitmap(b);
        	        	}
        	        }
        		  //coverArt.setImageBitmap();
        		  new BMTClick().execute(mAlbums[groupPosition].getArtUrl());
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
            textView.setTextColor(Color.YELLOW);
            //textView.setBackgroundColor(0xcecc00);
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
			//textView.setTextColor(Color.BLACK);
			textView.setText(mAlbums[groupPosition].getTrack(childPosition).getTitle());
			textView.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					//Log.d("clicked child", String.valueOf(arg2)+" "+String.valueOf(arg3));
					Log.i(mAlbums[groupPosition].getTrack(childPosition).getTitle(), "clicked");
					Log.i("Stream",Uri.parse(mAlbums[groupPosition].getTrack(childPosition).getStreamingUrl()).toString());
					class BMTChild extends AsyncTask<String,Void,Bitmap>{

        				@Override
        				protected Bitmap doInBackground(String... url) {
        					return UtilityBelt.bitmapFromNet(url[0]);
        				}
        	        	protected void onPostExecute(Bitmap b){
        	        		coverArt.setImageBitmap(b);
        	        	}
        	        }
					new BMTChild().execute(mAlbums[groupPosition].getArtUrl());
					//coverArt.setImageBitmap(UtilityBelt.bitmapFromNet();
					//VideoView mp = new VideoView(Profile.this);
					
					videoView1.setVideoURI(Uri.parse(mAlbums[groupPosition].getTrack(childPosition).getStreamingUrl()));
					videoView1.requestFocus();
					mController.show(0);
					videoView1.start();
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
			//Log.i("list","generating list");
						
			TextView textView = getGenericView();
			//Log.i("album "+groupPosition, mAlbums[groupPosition].getAlbumTitle());
            textView.setText(mAlbums[groupPosition].getAlbumTitle());
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
