package aaremm.com.projectci.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

import aaremm.com.projectci.R;
import aaremm.com.projectci.activity.LookActivity;
import aaremm.com.projectci.config.BootstrapApplication;

public class ReceiveTransitionsIntentService extends IntentService {
    /**
     * Sets an identifier for the service
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    /**
     * Handles incoming intents
     *
     * @param intent The Intent sent by Location Services. This
     *               Intent is provided
     *               to Location Services (inside a PendingIntent) when you call
     *               addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                            Integer.toString(errorCode)
            );
            /*
             * You can also send the error code to an Activity or
             * Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {
            // Get the type of transition (entry or exit)
            int transitionType =
                    LocationClient.getGeofenceTransition(intent);
            List<Geofence> mList =
                    LocationClient.getTriggeringGeofences(intent);
            // Test that a valid transition was reported
            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d("Enter ", +mList.size() + "");
                Intent pushIntent = new Intent(this, UserActivityService.class);
                getBaseContext().startService(pushIntent);
            } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d("EXIT ", +mList.size() + "");
                Intent pushIntent = new Intent(this, UserActivityService.class);
                getBaseContext().stopService(pushIntent);
                // remove notification
                if (!BootstrapApplication.getInstance().isActivityDestroyed()) {
                    Intent temp = new Intent(LookActivity.BROADCAST);
                    temp.putExtra(LookActivity.INTENT_ACTION, LookActivity.OUTOFAPA_ACTION);
                    sendBroadcast(temp);
                } else {
                    displayOutOfAPANotification();
                    if (BootstrapApplication.getInstance().isMediaMusicPaused()) {
                        BootstrapApplication.getInstance().abandonAudioFocus();
                    }
                }
            }
        }
    }

    private void displayOutOfAPANotification() {
        NotificationManager nf;
        NotificationCompat.Builder mBuilder;
        nf = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic)
                .setTicker("Out of APA")
                .setContentTitle("Be Safe")
                .setContentText("You're now out of Accident Prone Area.")
                .setVibrate(new long[]{0, 500, 500, 500, 500, 500});
        nf.notify(15000, mBuilder.build());
    }
}