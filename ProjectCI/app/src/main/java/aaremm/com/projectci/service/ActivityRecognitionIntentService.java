package aaremm.com.projectci.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import aaremm.com.projectci.activity.LookActivity;
import aaremm.com.projectci.config.BootstrapApplication;

public class ActivityRecognitionIntentService extends IntentService {

    public boolean IS_LOOK_ACTIVITY_VISIBLE = false;
    public String USER_ACTIVITY = "";
    public ActivityRecognitionIntentService() {
        super("ActivityRecognitionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity
            DetectedActivity mostProbableActivity =
                    result.getMostProbableActivity();
            /*
             * Get the probability that this activity is the
             * the user's actual activity
             */
            int confidence = mostProbableActivity.getConfidence();
            /*
             * Get an integer describing the type of activity
             */
            int activityType = mostProbableActivity.getType();
            doSomethingFromType(activityType);

            /*
             * At this point, you have retrieved all the information
             * for the current update. You can display this
             * information to the user in a notification, or
             * send it to an Activity or Service in a broadcast
             * Intent.
             */
        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */
        }
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private void doSomethingFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                if (anyDistractingAct()) {
                    USER_ACTIVITY = "in Vehicle "+USER_ACTIVITY;
                    showLookMessage();
                }
                Log.d("Activity", "Vehicle");
                break;
            case DetectedActivity.ON_BICYCLE:
                if (anyDistractingAct()) {
                    USER_ACTIVITY = "on Bicycle "+USER_ACTIVITY;
                    showLookMessage();
                }
                Log.d("Activity", "Bic");
                break;
            case DetectedActivity.ON_FOOT:
                if (anyDistractingAct()) {
                    USER_ACTIVITY = "Walking "+USER_ACTIVITY;
                    showLookMessage();
                }
                Log.d("Activity", "Foot");
                break;
            case DetectedActivity.STILL:
                Log.d("Activity", "Still");
                //do nothing
                break;
            case DetectedActivity.UNKNOWN:
                // do nothing
                break;
            case DetectedActivity.TILTING:
                //do nothing
                break;
        }
    }

    private boolean anyDistractingAct() {
        if (BootstrapApplication.getInstance().isMusicOn()) {
            USER_ACTIVITY = "and Music is on.";
            return true;
        }
        if (BootstrapApplication.getInstance().isScreenOn()) {
            USER_ACTIVITY = "and Screen is on.";
            return true;
        }
        if (BootstrapApplication.getInstance().isOnCall()) {
            USER_ACTIVITY = "and is on Telephone Call.";
            return true;
        }
        return false;
    }

    private void showLookMessage() {
        if (!BootstrapApplication.getInstance().isActivityVisible()) {
            Log.d("Look", "In");
            startActivity(new Intent(this, LookActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("USER_ACTIVITY",USER_ACTIVITY));
        }else{
            Log.d("Look", "Out");
        }
    }
}
