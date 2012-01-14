package com.nimo.wristband;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.MediaController;
import android.widget.Toast;

public class SongClick implements OnItemLongClickListener{
	
	//MediaPlayer mPlayer;
	String[] streams;
	String[] images;
	int playing = 0;
	
	public SongClick(String[] strm){
		streams = strm;
	}
	
	public void playMusic(){
		int i = BCPlayer.coverFlow.getSelectedItemPosition();
		Log.d("item"+String.valueOf(i),streams[i]);
		try {
			playing = i;
			Uri link = Uri.parse(streams[i]);
			BCPlayer.mPlayer.setVideoURI(link);
			
			String bn = BCPlayer.nameList[i];
			String sn = BCPlayer.titleList[i];
			String vn = BCPlayer.venueList[i];
			String tm = BCPlayer.timeList[i];
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
			//BCPlayer.mPlayer.prepareAsync();
			//BCPlayer.titleText.setVisibility(View.VISIBLE);
			Toast play = Toast.makeText(BCPlayer.bcp, "Playing", Toast.LENGTH_SHORT);
			play.show();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		//This causes the selected music to play
		Log.v("click", "starting");
		if(BCPlayer.mPlayer.isPlaying() && (BCPlayer.coverFlow.getSelectedItemPosition() == playing)){
			Log.d("mPlayer", "Reseting");
			BCPlayer.mPlayer.stopPlayback();
			//BCPlayer.mPlayer.reset();
			//BCPlayer.titleText.setText("");
			Toast toast = Toast.makeText(BCPlayer.bcp, "Stopping...", Toast.LENGTH_SHORT);
			toast.show();
		}
		else{
			if(BCPlayer.mPlayer.isPlaying()){
				BCPlayer.mPlayer.stopPlayback();
				//BCPlayer.mPlayer.reset();
				Toast toast = Toast.makeText(BCPlayer.bcp, "Stopping...", Toast.LENGTH_SHORT);
				toast.show();
			}
			playMusic();
		}
		
		
	}
	
	
	public class Prep implements OnPreparedListener, MediaController.MediaPlayerControl{
		
		private Handler handler;
		
		public Prep()
		{
			handler = new Handler();
		}

		public void onPrepared(MediaPlayer mp) {
			Log.d("mPlayer", "ready");
			//BCPlayer.mediaController1.setMediaPlayer(this);
			//BCPlayer.mediaController1 = new MediaController(BCPlayer.bcp);
			BCPlayer.mediaController1.setAnchorView(BCPlayer.coverFlow);
			BCPlayer.mPlayer.start();
			BCPlayer.dateText.setText("Listening to: "+BCPlayer.nameList[BCPlayer.coverFlow.getSelectedItemPosition()]);
			handler.post(new Runnable() {
			      public void run() {
			        BCPlayer.mediaController1.setEnabled(true);
			        BCPlayer.mediaController1.show();
			      }});
			
			Log.d("mPlayer", "should be playing now");
			
		}
		
		public boolean canPause() {
			return BCPlayer.mPlayer.canPause();
		}

		public boolean canSeekBackward() {
			return BCPlayer.mPlayer.canSeekBackward();
		}

		public boolean canSeekForward() {
			return BCPlayer.mPlayer.canSeekForward();
		}

		public int getBufferPercentage() {
			return BCPlayer.mPlayer.getBufferPercentage();
		}

		public int getCurrentPosition() {
			return BCPlayer.mPlayer.getCurrentPosition();
		}

		public int getDuration() {
			return BCPlayer.mPlayer.getDuration();
		}

		public boolean isPlaying() {
			return BCPlayer.mPlayer.isPlaying();
		}

		public void pause() {
			BCPlayer.mPlayer.pause();
			
		}

		public void seekTo(int arg0) {
			BCPlayer.mPlayer.seekTo(arg0);
		}

		public void start() {
			BCPlayer.mPlayer.start();
		}
		
	}
	
	public class Next implements OnCompletionListener{

		public void onCompletion(MediaPlayer mp) {
			
			int i = BCPlayer.coverFlow.getSelectedItemPosition();
			BCPlayer.dateText.setText(MainActivity.date);
			mp.reset();
			if(i < BCPlayer.coverFlow.getCount()-1){
				BCPlayer.coverFlow.setSelection(i+1);
				playMusic();
			}
			else{
				mp.reset();
				//BCPlayer.dateText.setText(MainActivity.date);
			}
		}
		
	}
	
	public class ItemSelectListen implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			//update the info
			int i = BCPlayer.coverFlow.getSelectedItemPosition();
			String bn = BCPlayer.nameList[i];
			String sn = BCPlayer.titleList[i];
			String vn = BCPlayer.venueList[i];
			String tm = BCPlayer.timeList[i];
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
			if(BCPlayer.mPlayer.isPlaying())
				BCPlayer.mediaController1.show();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			//do nothing
			
		}
		
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		//This causes the selected music to play
		Log.v("click", "starting");
		if(BCPlayer.mPlayer.isPlaying() && (BCPlayer.coverFlow.getSelectedItemPosition() == playing)){
			Log.d("mPlayer", "Reseting");
			BCPlayer.dateText.setText(MainActivity.date);
			BCPlayer.mPlayer.stopPlayback();
			BCPlayer.mediaController1.hide();
			//BCPlayer.mPlayer.reset();
			//BCPlayer.titleText.setText("");
			Toast toast = Toast.makeText(BCPlayer.bcp, "Stopping...", Toast.LENGTH_SHORT);
			toast.show();
		}
		else{
			if(BCPlayer.mPlayer.isPlaying()){
				BCPlayer.mPlayer.stopPlayback();
				BCPlayer.dateText.setText(MainActivity.date);
				BCPlayer.mediaController1.hide();
				//BCPlayer.mPlayer.reset();
				Toast toast = Toast.makeText(BCPlayer.bcp, "Stopping...", Toast.LENGTH_SHORT);
				toast.show();
			}
			playMusic();
		}
		return false;
	}

	

}
