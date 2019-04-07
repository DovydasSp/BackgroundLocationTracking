package speckauskas.dovydas.backgroundlocationtracking;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

public class BackgroundDetectedActivitiesService extends Service {

    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;

    IBinder mBinder = new BackgroundDetectedActivitiesService.LocalBinder();

    public class LocalBinder extends Binder {
        public BackgroundDetectedActivitiesService getServerInstance() {
            return BackgroundDetectedActivitiesService.this;
        }
    }

    public BackgroundDetectedActivitiesService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mIntentService = new Intent(this, DetectedActivitiesIntentService.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
        requestActivityUpdatesHandler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    //Request activity updates
    public void requestActivityUpdatesHandler() {
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                this.getResources().getInteger(R.integer.userActivitiesPullRateInMS),
                mPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                makeToast("Successfully requested activity updates");
                Log.i("BackgDetectedActivities", "Successfully requested activity updates");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast("Requesting activity updates failed to start");
                Log.i("BackgDetectedActivities", "Requesting activity updates failed to start");
            }
        });
    }

    //Unsubscribe from activity updates
    public void removeActivityUpdatesHandler() {
        Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                mPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                makeToast("Removed activity updates successfully!");
                Log.i("BackgDetectedActivities", "Removed activity updates successfully!");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast("Failed to remove activity updates!");
                Log.i("BackgDetectedActivities", "Failed to remove activity updates!");
            }
        });
    }

    private void makeToast(String text){
        if(this.getResources().getInteger(R.integer.notificationIntrusivenessLevel) == 2) {
            Toast.makeText(getApplicationContext(),
                    text,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActivityUpdatesHandler();
    }
}