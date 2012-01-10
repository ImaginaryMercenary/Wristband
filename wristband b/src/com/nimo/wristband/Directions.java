package com.nimo.wristband;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Directions extends MapActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.directions);
	    
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    List<Overlay> mapOverlays = mapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.wbicon);
	    TheOverlay itemizedoverlay = new TheOverlay(drawable, this);
	    Bundle extras = this.getIntent().getExtras();
	    int i = extras.getInt("position");
	    Log.d("extra", "position " + i);
	    double lat = BCPlayer.coordList[i][0];
		double lng = BCPlayer.coordList[i][1];
		Log.d("lat long extra", lat+","+lng);
	    
	    
	    int microlat = (int)(BCPlayer.coordList[i][0] * 1e6);
	    int microlng = (int)(BCPlayer.coordList[i][1] * 1e6);
	    Geocoder myLocation = new Geocoder(this, Locale.getDefault());
	    List<Address> theAddress;
		try {
			
			Log.d("location", String.valueOf(lat) + "," + String.valueOf(lng));
			theAddress = myLocation.getFromLocation(lat, lng, 1);
			//theAddress = myLocation.getFromLocationName(BCPlayer.venueList[i] + " venue", 1, lat - 1, lng - 1, lat + 1, lng + 1);
			
			GeoPoint point = new GeoPoint(microlat,microlng);
			
		    OverlayItem overlayitem = new OverlayItem(point, BCPlayer.venueList[i] + " - " + theAddress.get(0).getAddressLine(0), BCPlayer.nameList[i] + " at " + BCPlayer.timeList[i]);
		    itemizedoverlay.addOverlay(overlayitem);
		    mapOverlays.add(itemizedoverlay);
		    
		    MapController myMC = mapView.getController();
		    myMC.setZoom(15);
		    myMC.animateTo(point);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IndexOutOfBoundsException e){
			GeoPoint point = new GeoPoint(microlat,microlng);
			OverlayItem overlayitem = new OverlayItem(point, BCPlayer.venueList[i] + " - " + "Address not specified", BCPlayer.nameList[i] + " at " + BCPlayer.timeList[i]);
		    itemizedoverlay.addOverlay(overlayitem);
		    mapOverlays.add(itemizedoverlay);
		}
	    
	   
	    
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
