package speckauskas.dovydas.backgroundlocationtracking;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationService extends Service {
    BroadcastReceiver broadcastReceiver;

    private int thisTripID;
    private int lastTripID;
    private boolean timerRunning = false;
    GPSTracker gps;

    public LocationService(Context applicationContext) {
        super();
    }

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("LocationService", "STARTS");

        createNotificationChannel();

        //Check if last trip was completed if not, then continue it
        SharedPreferences sharedPreferences = getSharedPreferences("locationTrackPrefs", Context.MODE_PRIVATE);
        thisTripID = sharedPreferences.getInt("thisTripID", 0);
        lastTripID = sharedPreferences.getInt("lastTripID", 0);
        if(thisTripID != lastTripID) { thisTripID = lastTripID +1; }
        Log.i("LocationService", "ThisTripID: "+thisTripID);

        //Create BroadcastReceiver to receive updates about changes in user activity
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("activity_intent")) {
                    handleUserActivity(intent.getIntExtra("type", -1),
                            intent.getIntExtra("confidence", 0));
                }
            }
        };
        //Register receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("activity_intent"));

        //Start activity tracking service
        startTracking();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("LocationService", "onDestroy()");

        //If gps tracking was enabled, turn it off
        if(timerRunning) {
            gps.stopTracking();
            gps.stopSelf();
            gps = null;
        }

        SharedPreferences sharedPreferenceKillService = getSharedPreferences("locationServiceKillPref", Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = getSharedPreferences("locationTrackPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //If "STOP" button was pressed, calculate distance driven and mark trip as completed
        if(sharedPreferenceKillService.getBoolean("killProcess", false))
        {
            calculateDistanceDrivenOnTrip(thisTripID);
            editor.putInt("lastTripID", thisTripID);
            editor.putInt("thisTripID", -1);
        }
        //If "STOP" button was not pressed, mark trip as not completed and restart LocationService
        else {
            editor.putInt("thisTripID", thisTripID);
            editor.putInt("lastTripID", thisTripID);
            Intent broadcastIntent = new Intent(this, LocationServiceRestarterBroadcastReceiver.class);
            sendBroadcast(broadcastIntent);
        }
        editor.commit();

        //Stop service
        stopTracking();
    }

    private Map<Integer, String> activityNameMap = new HashMap<Integer, String>() {{
        put(0, "IN_VEHICLE");
        put(1, "ON_BICYCLE");
        put(2, "ON_FOOT");
        put(3, "STILL");
        put(4, "UNKNOWN");
        put(5, "TILTING");
        put(7, "WALKING");
        put(8, "RUNNING");
    }};

    //When change in user activity is detected
    private void handleUserActivity(int type, int confidence){
        Log.i("LocationService", "ActivityDetection type "+type+" "+ activityNameMap.get(type)+" confidence "+confidence);
        //Check if confidence level of activity is reached
        if(confidence > this.getResources().getInteger(R.integer.confideceToReachToAcceptActivity)) {
            SharedPreferences sharedPreferences = getSharedPreferences("locationTrackPrefs", Context.MODE_PRIVATE);
            switch (type) {
                //If user started driving, start tracking
                case 0: {
                    if(this.getResources().getInteger(R.integer.notificationIntrusivenessLevel) == 2) {
                        Toast.makeText(getApplicationContext(),
                                "Driving " + confidence,
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                    //If tracking is already started, do nothing
                    if(!timerRunning){
                        //Notify user about detected trip start and location tracking
                        if(this.getResources().getInteger(R.integer.notificationIntrusivenessLevel) > 0) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "locationTrackingChannel")
                                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                                    .setContentTitle("Started tracking")
                                    .setContentText("App detected the beginning of the trip. Starting location tracking.")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                            notificationManager.notify(this.getResources().getInteger(R.integer.notificationId), builder.build());
                        }

                        Log.i("LocationService", "START TIMER");
                        thisTripID = sharedPreferences.getInt("thisTripID", 0);
                        lastTripID = sharedPreferences.getInt("lastTripID", 0);
                        if(thisTripID != lastTripID) { thisTripID = lastTripID +1; }
                        gps = new GPSTracker(getApplicationContext());
                        gps.startTracking(thisTripID);
                        timerRunning = true;
                    }
                    break;
                }
                //If user is on foot stop tracking and calculate distance driven
                case 2: {
                    if(this.getResources().getInteger(R.integer.notificationIntrusivenessLevel) == 2) {
                        Toast.makeText(getApplicationContext(),
                                "OnFoot " + confidence,
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                    //If timer is not running do nothing
                    if(timerRunning) {
                        calculateDistanceDrivenOnTrip(thisTripID);
                        Log.i("LocationService", "STOOOOOP TIMER");
                        gps.stopTracking();
                        gps.stopSelf();
                        gps = null;

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("lastTripID", thisTripID);
                        editor.putInt("thisTripID", -1);
                        editor.commit();
                        timerRunning = false;
                    }
                    break;
                }
            }
        }
    }

    //Get trip locations from DB and calculate total distance driven in the trip
    public double calculateDistanceDrivenOnTrip(int tripID) {
        DBHandler dbHandler = new DBHandler(getBaseContext(), null, null, 1);
        List<DBClass> dbList = dbHandler.findHandler(tripID);
        double lastLat = 0;
        double lastLon = 0;
        double totalDist = 0;
        Log.i("LocationService", "ListSize: "+dbList.size());
        for (DBClass dbClass : dbList) {
            Log.i("LocationService", "COORDS: "+dbClass.getID()+" "+dbClass.getTripID()+" "+dbClass.getLatitude()+" "+dbClass.getLongitude());
            double lat = dbClass.getLatitude();
            double lon = dbClass.getLongitude();
            if(lastLat == 0 && lastLon == 0)
            {
                lastLat = lat;
                lastLon = lon;
            }
            totalDist += getDistanceBetween(lastLat, lastLon, lat, lon);
            lastLat = lat;
            lastLon = lon;
        }
        Log.i("LocationService", "TotalDistance: "+totalDist);


        //Notify user about trip end and show total distance driven
        if(this.getResources().getInteger(R.integer.notificationIntrusivenessLevel) > 0) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "locationTrackingChannel")
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setContentTitle("Stopped tracking")
                    .setContentText("End of trip. Traveled distance: " + totalDist)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(this.getResources().getInteger(R.integer.notificationId), builder.build());
        }

        return totalDist;
    }

    //Calculate distance between two coordinates
    private float getDistanceBetween(double lat1, double lon1, double lat2, double lon2) {
        float[] distance = new float[2];
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);
        return distance[0];
    }

    //Start BackgroundDetectedActivitiesService
    private void startTracking() {
        Intent intent1 = new Intent(this, BackgroundDetectedActivitiesService.class);
        startService(intent1);
    }

    //Stop BackgroundDetectedActivitiesService
    private void stopTracking() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        Intent intent = new Intent(this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "locationTrackingChannel.name";
            String description = "locationTrackingChannel.desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("locationTrackingChannel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}