package aaremm.com.projectci.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

/**
 * Created by rahul on 26-08-2014.
 */
public class UserActivityService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    // Constants that define the activity detection interval
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 5;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

    /*
     * Store the PendingIntent used to send activity recognition events
     * back to the app
     */
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;
    // Flag that indicates if a request is underway.
    private boolean mInProgress = false;
    public enum REQUEST_TYPE {START, STOP}
    private REQUEST_TYPE mRequestType;

    @Override
    public void onCreate() {
        /*
         * Instantiate a new activity recognition client. Since the
         * parent Activity implements the connection listener and
         * connection failure listener, the constructor uses "this"
         * to specify the values of those parameters.
         */
        mActivityRecognitionClient =
                new ActivityRecognitionClient(getBaseContext(), this, this);
        /*
         * Create the PendingIntent that Location Services uses
         * to send activity recognition updates back to this app.
         */
        Intent intent = new Intent(
                getBaseContext(), ActivityRecognitionIntentService.class);
        /*
         * Return a PendingIntent that starts the IntentService.
         */
        mActivityRecognitionPendingIntent =
                PendingIntent.getService(getBaseContext(), 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        startUpdates();
    }/**
     * Request activity recognition updates based on the current
     * detection interval.
     *
     */
    public void startUpdates() {
        // Set the request type to START
        mRequestType = REQUEST_TYPE.START;

        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            //
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (mRequestType) {
            case START:
                /*
                 * Request activity recognition updates using the
                 * preset detection interval and PendingIntent.
                 * This call is synchronous.
                 */
                mActivityRecognitionClient.requestActivityUpdates(
                        DETECTION_INTERVAL_MILLISECONDS,
                        mActivityRecognitionPendingIntent);
                mInProgress = false;
                mActivityRecognitionClient.disconnect();
                break;

            case STOP :
                mActivityRecognitionClient.removeActivityUpdates(
                        mActivityRecognitionPendingIntent);
                mInProgress = false;
                break;
        }
    }

    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Delete the client
        mActivityRecognitionClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }/**
     * Turn off activity recognition updates
     *
     */
    public void stopUpdates() {
        // Set the request type to STOP
        mRequestType = REQUEST_TYPE.STOP;

        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            //
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    @Override
    public void onDestroy() {
        stopUpdates();
    }
}
