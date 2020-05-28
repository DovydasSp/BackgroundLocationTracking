package speckauskas.dovydas.backgroundlocationtracking;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import speckauskas.dovydas.backgroundlocationtracking.map.MapsActivity;

public class MainActivity extends AppCompatActivity {

    Intent locationIntent;
    private LocationService locationService;
    Context context;
    public Context getContext() { return context; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);

        //START BUTTON LISTENER
        Button startServiceButton = findViewById(R.id.startServiceButton);
        startServiceButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferenceKillService = getSharedPreferences("locationServiceKillPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferenceKillService.edit();
                editor.putBoolean("killProcess", false); //Process does not need to be killed
                editor.commit();
                startLocationService(); //Start activity tracking
            }
        });

        //STOP BUTTON LISTENER
        Button stopServiceButton = findViewById(R.id.stopServiceButton);
        stopServiceButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferenceKillService = getSharedPreferences("locationServiceKillPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferenceKillService.edit();
                editor.putBoolean("killProcess", true); //Process needs to be killed
                editor.commit();
                if(locationService != null) {
                    locationService.stopSelf(); //Stop all processes
                    stopService(locationIntent);
                }
            }
        });

        Button mapsButton = findViewById(R.id.openMapButton);
        mapsButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapsActivity.class);
                startActivity(intent);
            }
        });

        //CHECK AND REQUEST PERMISSIONS
        checkLocationPermission();
        requestChangeBatteryOptimizations();
    }

    //Check if battery optimization stopping is granted, if not request permission and stop DOZE for app
    private void requestChangeBatteryOptimizations ()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            String packageName = context.getApplicationContext().getPackageName();
            boolean ignoringOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName);

            if (ignoringOptimizations) {
                return;
            }

            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            context.startActivity(intent);
        }
    }

    //Check if location tracking permission is granted, if not request permission
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_location_permission)
                    .setMessage(R.string.text_location_permission)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION}, 99);
                        }
                    })
                    .create()
                    .show();
        }
    }

    //Start main service
    private void startLocationService(){
        locationService = new LocationService(getContext());
        locationIntent = new Intent(getContext(), locationService.getClass());
        if (!isMyServiceRunning(locationService.getClass())) {
            startService(locationIntent);
        }
    }

    //Check if main service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("MainActivity", "isMyServiceRunning? "+true+"");
                return true;
            }
        }
        Log.i ("MainActivity", "isMyServiceRunning? "+false+"");
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(locationIntent);
        Log.i("MainActivity", "onDestroy()");
        super.onDestroy();

    }
}
