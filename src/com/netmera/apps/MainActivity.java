package com.netmera.apps;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.*;
import com.netmera.boxandcirclesearchex.R;
import com.netmera.extra.Constants;
import com.netmera.mobile.*;
import com.netmera.overlays.BalloonItemizedOverlay;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends MapActivity {
	private MapView mapView;
	private Button boxSearchButton;
	private Button circleSearchButton;
	private Drawable marker;
	private Drawable markercenter;
	private MarkerOverlay pointsOverlay;
	private MarkerOverlay centerofIstanbulOverlay;
	private GeoPoint geoLocationofIstanbul;
	private NetmeraGeoLocation netmeraGeoLocationofIstanbul;
	private ProgressDialog mDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		NetmeraClient.init(getApplicationContext(), Constants.SECURITY_KEY);
		init();
		setMap();

		boxSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Clear pointsOverlay that includes current overlays on the map
				pointsOverlay.clear();
				// Calling doBoxSearch
				doBoxSearch();
			}
		});

		circleSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Clear pointsOverlay that includes current overlays on the map
				pointsOverlay.clear();
				// Calling doCircleSearch
				doCircleSearch();
			}
		});

	}

	/**
	 * Setting up map and overlays
	 */
	public void setMap() {
		centerofIstanbulOverlay = new MarkerOverlay(markercenter, mapView);
		OverlayItem overlayItem = new OverlayItem(geoLocationofIstanbul, "Center", "");

		// center point added map
		centerofIstanbulOverlay.addOverlay(overlayItem);
		mapView.getOverlays().add(centerofIstanbulOverlay);

		// New points that comes from search will be added this overlay.
		pointsOverlay = new MarkerOverlay(marker, mapView);
		mapView.getOverlays().add(pointsOverlay);

		mapView.getController().setCenter(geoLocationofIstanbul);
		mapView.getController().setZoom(14);
	}

	public void doBoxSearch() {
		// The loading screen begins show
		mDialog.show();
		pointsOverlay.hideAllBalloons();
		centerofIstanbulOverlay.hideAllBalloons();
		double latitude = 0;
		double longitude = 0;
		GeoPoint geopoint = new GeoPoint((int) (latitude * 1e6), (int) (longitude * 1e6));
		// Create new NetmeraService to access "Points" table of our app

		NetmeraGeoLocation mapcoordinates[] = new NetmeraGeoLocation[2];
		// Getting current map coordinates
		mapcoordinates = getmapcoordinates();
		NetmeraGeoLocation topleft = mapcoordinates[0];
		NetmeraGeoLocation bottomright = mapcoordinates[1];
		
		
		
		NetmeraService service = new NetmeraService("Points");
		service.boxSearchInBackground(topleft, bottomright, "location", new NetmeraCallback< List< NetmeraContent>>() {
		    
		    public void callback(List< NetmeraContent> contentList, NetmeraException exception) {
		        if (contentList != null && exception == null) {
		            // Success
		            for (NetmeraContent content : contentList) {
		                try {
		                    NetmeraGeoLocation geo = content.getNetmeraGeoLocation("location");
		                    double latitude = geo.getLatitude();
		                    double longitude = geo.getLongitude();
		                    String title=content.getString("title");
		                    GeoPoint geopoint = new GeoPoint((int) (latitude * 1e6), (int) (longitude * 1e6));
		                    draw(geopoint, title);
		                    
		                    
		                    
		                } catch (NetmeraException e) {
		                    // Handle exception
		                }
		            }
		        } else {
		            // Error occurred.
		        }   
		        mDialog.dismiss();
		        mapView.postInvalidate();
		    }
		});
		
		
		
	}

	public void doCircleSearch() {
		// The loading screen begins show
		mDialog.show();
		pointsOverlay.hideAllBalloons();
		centerofIstanbulOverlay.hideAllBalloons();
		NetmeraGeoLocation netmeraGeoLocationofIstanbul = new NetmeraGeoLocation(Constants.latitudeofIstanbul, Constants.longitudeofIstanbul);

		
		NetmeraService service = new NetmeraService("Points");
		int distance=3;
		service.circleSearchInBackground(netmeraGeoLocationofIstanbul, distance, "location", new NetmeraCallback< List< NetmeraContent>>() {
		   
		    public void callback(List< NetmeraContent> contentList, NetmeraException exception) {
		        if (contentList != null && exception == null) {
		            // Success
		            for (NetmeraContent content : contentList) {
		                try {
		                    NetmeraGeoLocation geo = content.getNetmeraGeoLocation("location");
		                    double latitude = geo.getLatitude();
		                    double longitude = geo.getLongitude();
		                    
		                    String title=content.getString("title");
		                    GeoPoint geopoint = new GeoPoint((int) (latitude * 1e6), (int) (longitude * 1e6));
		                    draw(geopoint, title);
		                    
		                    
		                } catch (NetmeraException e) {
		                    // Handle exception
		                }
		            }
		        } else {
		            // Error occurred.
		        }        
		        mDialog.dismiss();
		        mapView.postInvalidate();
		    }
		});
	

	}

	/**
	 * This method add a marker to the map
	 * 
	 * @param geo
	 *            -Geopint that marker added on it.
	 * @param title
	 *            -When the tap marker this title will be shown.
	 */
	public void draw(GeoPoint geo, String title) {

		OverlayItem overlayItem = new OverlayItem(geo, title, "");
		int markerWidth = marker.getIntrinsicWidth();
		int markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(-markerWidth / 2, -markerHeight, markerWidth / 2, 0);
		pointsOverlay.addOverlay(overlayItem);

	}

	/**
	 * Return a NetmeraGeoLocation array size of 2, that includes topleft and
	 * bottomright coordinates of current map
	 */

	public NetmeraGeoLocation[] getmapcoordinates() {
		NetmeraGeoLocation[] coordinates = new NetmeraGeoLocation[2];
		NetmeraGeoLocation maptopleft, mapbottomright;
		Projection proj = mapView.getProjection();
		GeoPoint topLeft = proj.fromPixels(0, 0);
		GeoPoint bottomRight = proj.fromPixels(mapView.getWidth() - 10, mapView.getHeight() - 10);
		double topLat = topLeft.getLatitudeE6() / 1E6;
		double topLon = topLeft.getLongitudeE6() / 1E6;
		double bottomLat = bottomRight.getLatitudeE6() / 1E6;
		double bottomLon = bottomRight.getLongitudeE6() / 1E6;
		maptopleft = new NetmeraGeoLocation(topLat, topLon);
		mapbottomright = new NetmeraGeoLocation(bottomLat, bottomLon);

		coordinates[0] = maptopleft;
		coordinates[1] = mapbottomright;

		return coordinates;

	}

	// initialize variables

	public void init() {

		mapView = (MapView) findViewById(R.id.mapview);
		boxSearchButton = (Button) findViewById(R.id.boxSearchButton);
		circleSearchButton = (Button) findViewById(R.id.circleSearchButton);
		marker = getResources().getDrawable(R.drawable.mapmarker);
		markercenter = getResources().getDrawable(R.drawable.redmarker);
		geoLocationofIstanbul = new GeoPoint((int) (Constants.latitudeofIstanbul * 1E6), (int) (Constants.longitudeofIstanbul * 1E6));
		netmeraGeoLocationofIstanbul = new NetmeraGeoLocation(Constants.latitudeofIstanbul, Constants.longitudeofIstanbul);
		mDialog = new ProgressDialog(MainActivity.this);
		mDialog.setMessage("Loading...");
		mDialog.setCancelable(false);
		mDialog.setProgress(0);

	}

	public class MarkerOverlay extends BalloonItemizedOverlay<OverlayItem> {

		private ArrayList<OverlayItem> m_overlays = new ArrayList<OverlayItem>();

		public synchronized void clear() {
			m_overlays.clear();
			setLastFocusedIndex(-1);
			populate();
			mapView.invalidate();
		}

		public MarkerOverlay(Drawable defaultMarker, MapView mapView) {
			super(boundCenter(defaultMarker), mapView);
			populate();
		}

		public synchronized void addOverlay(OverlayItem overlay) {
			synchronized (m_overlays) {
				m_overlays.add(overlay);
				setLastFocusedIndex(-1);
				populate();
			}

		}

		@Override
		protected synchronized OverlayItem createItem(int i) {
			return m_overlays.get(i);
		}

		@Override
		public synchronized int size() {
			return m_overlays.size();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapview) {

			int actionId = event.getAction();

			switch (actionId) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				break;
			}
			return false;
		}

		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
			pointsOverlay.hideAllBalloons();
			centerofIstanbulOverlay.hideAllBalloons();
			return super.onTap(p, mapView);
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
