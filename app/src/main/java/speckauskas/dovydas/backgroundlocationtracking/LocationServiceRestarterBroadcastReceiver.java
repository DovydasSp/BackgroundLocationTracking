package speckauskas.dovydas.backgroundlocationtracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationServiceRestarterBroadcastReceiver extends BroadcastReceiver {

    //Broadcast Receiver to restart LocationService
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LocationServiceRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops!");
        context.startService(new Intent(context, LocationService.class));;
    }
}
