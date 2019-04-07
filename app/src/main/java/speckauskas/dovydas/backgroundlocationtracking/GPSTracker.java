package speckauskas.dovydas.backgroundlocationtracking;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class GPSTracker extends Service implements LocationListener {

    private Context mContext;// = null;

    // flag for GPS status
    boolean isGPSEnabled = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // Declaring a Location Manager
    protected LocationManager locationManager;

    //Initialize GPSTracker
    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    //Checks gps module and returns Location
    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGPSEnabled) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    //Variables used in timerTask
    private int counter=0;
    private Timer timer;
    private TimerTask timerTask;
    private Location lastLocation = null;

    //Start gps tracking timer
    public void startTracking(int thisTripID){
        timer = new Timer();

        initializeTrackerTask(thisTripID);

        timer.schedule(timerTask, 10, this.getResources().getInteger(R.integer.gpsUpdatingRateInMS));
    }

    //TimerTast to pull gps coordinates and put them to DB
    private void initializeTrackerTask(final int thisTripID) {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("GPSTracker", "Location timer ticks passed "+ (counter++));

                Location thisLocation = getLocation();
                if(thisLocation != null) {
                    if (lastLocation == null) { lastLocation = thisLocation; }
                    float distance = thisLocation.distanceTo(lastLocation);
                    if(distance > 10.0)
                    {
                        DBHandler dbHandler = new DBHandler(mContext, null, null, 1);
                        DBClass locationClass = new DBClass(0, thisTripID, thisLocation.getLatitude(), thisLocation.getLongitude());
                        dbHandler.addHandler(locationClass);
                        lastLocation = thisLocation;
                    }

                    Log.i("GPSTracker", "GPS Latitude  " + (thisLocation.getLatitude()) + " Longitude  " + (thisLocation.getLongitude()));
                }
            }
        };
    }

    //Stop tracking by stopping timer
    public void stopTracking(){
        timer.cancel();
        timer.purge();
        timer = null;
    }

    //Function to get latitude
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    //Function to get longitude
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}