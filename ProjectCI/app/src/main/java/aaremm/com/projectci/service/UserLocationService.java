package aaremm.com.projectci.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import aaremm.com.projectci.activity.MainActivity;
import aaremm.com.projectci.config.BootstrapApplication;
import aaremm.com.projectci.object.APAGeofence;
import aaremm.com.projectci.object.JSONParser;

/**
 * Created by rahul on 26-08-2014.
 */
public class UserLocationService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
        LocationClient.OnAddGeofencesResultListener {
    // Global constants
    private boolean accidentProneArea = false;
    private boolean isUserActivityServiceRunning = false;
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    /*
* Use to set an expiration time for a geofence. After this amount
* of time Location Services will stop tracking the geofence.
*/
    private static final long SECONDS_PER_HOUR = 60;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS *
                    SECONDS_PER_HOUR *
                    MILLISECONDS_PER_SECOND;

    private static final int GEOFENCE_RADIUS = 30;

    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    private Context mContext = BootstrapApplication.getInstance();
    private Location mLocation = null;
    private int postalCode = -1;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    List<LatLng> cords = new ArrayList<LatLng>();
    List<Geofence> mGeofenceList = new ArrayList<Geofence>();
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;


    // Defines the allowable request types.
    public enum REQUEST_TYPE {
        ADD, REMOVE_INTENT, L_UP
    }

    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    PendingIntent mTransitionPendingIntent;

    private boolean RESET_GEOFENCES_IN_PROGRESS = false;
    private boolean SERVICE_IS_DESTROYED =false;

    @Override
    public void onCreate() {
        startTracking();
    }


    private void startTracking() {
        Log.d(BootstrapApplication.APPTAG, "startTracking");

        // TODO : Error if Google Play Services not available
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mLocationClient = new LocationClient(this, this, this);

            if (!mLocationClient.isConnected() || !mLocationClient.isConnecting()) {
                mLocationClient.connect();

                // Create the LocationRequest object
                mLocationRequest = LocationRequest.create();
                // Use high accuracy
                mLocationRequest.setPriority(
                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                // Set the update interval to 60 seconds
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                // Set the fastest update interval to 1 second
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
                mRequestType = REQUEST_TYPE.L_UP;
            }
        } else {
            Log.e(BootstrapApplication.APPTAG, "unable to connect to google play services.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLocation == null) {
            mLocation = location;
        }else if(mLocation.distanceTo(location) < 0.1){
            return;
        }
        // Report to the UI that the location was updated
        Log.d("Updated Location: ", Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude()));
        BootstrapApplication.getInstance().setCurrentLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        Intent temp = new Intent(MainActivity.BROADCAST);
        temp.putExtra(MainActivity.INTENT_ACTION, MainActivity.LC_ACTION);
        sendBroadcast(temp);

        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null) {
                int code = Integer.valueOf(addresses.get(0).getPostalCode());
                if (code != postalCode) {
                    postalCode = code;
                   // Toast.makeText(this, postalCode + " code", Toast.LENGTH_LONG).show();
                    Log.d("Code", postalCode + "");
                    if (mTransitionPendingIntent != null) {
                        removeGeofences(mTransitionPendingIntent);
                    }else {
                        new GetAPACords(this, postalCode).execute();
                    }
                }else {
                    if (mLocation.distanceTo(location) > 200 && !RESET_GEOFENCES_IN_PROGRESS) {
                        mLocation = location;
                        if (mTransitionPendingIntent != null) {
                            removeGeofences(mTransitionPendingIntent);
                        } else {
                            new SetAPACords(this, cords).execute();
                        }
                        RESET_GEOFENCES_IN_PROGRESS = true;
                    }
                }
            }
        }
    }

    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
    public void addGeofences() {
        // Start a request to add geofences
        mRequestType = REQUEST_TYPE.ADD;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    /**
     * Start a request to remove geofences by calling
     * LocationClient.connect()
     */
    public void removeGeofences(PendingIntent requestIntent) {
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_INTENT;
        // Store the PendingIntent
        mGeofenceRequestIntent = requestIntent;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
       // mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */

    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(this,
                ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public List<LatLng> compareLoc(List<LatLng> unsortedNBLatLng, final Location mLocation) {

        Comparator comp = new Comparator<LatLng>() {
            @Override
            public int compare(LatLng lhs, LatLng rhs) {
                float[] result1 = new float[3];
                android.location.Location.distanceBetween(mLocation.getLatitude(), mLocation.getLongitude(), lhs.latitude, lhs.longitude, result1);
                Float distance1 = result1[0];

                float[] result2 = new float[3];
                android.location.Location.distanceBetween(mLocation.getLatitude(), mLocation.getLongitude(), rhs.latitude, rhs.longitude, result2);

                Float distance2 = result2[0];

                return distance1.compareTo(distance2);
            }
        };

        Collections.sort(unsortedNBLatLng, comp);
        return unsortedNBLatLng;

    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {/*
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();*/
        /*if (mLocation != null) {
            (new GetAddressTask(this)).execute(mLocation);
        }*/

        switch (mRequestType) {

            case L_UP:

                // When the location client is connected, set mock mode
                mLocationClient.requestLocationUpdates(mLocationRequest, this);
                mLocation = mLocationClient.getLastLocation();
                // createLocation();
                break;
            case ADD:
                // Get the PendingIntent for the request
                mTransitionPendingIntent =
                        getTransitionPendingIntent();
                // Send a request to add the current geofences
                mLocationClient.addGeofences(
                        mGeofenceList, mTransitionPendingIntent, this);
                break;

            case REMOVE_INTENT:
                mLocationClient.removeGeofences(mGeofenceRequestIntent, new LocationClient.OnRemoveGeofencesResultListener() {
                    @Override
                    public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {

                    }

                    @Override
                    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
                        // If removing the geofences was successful
                        if (statusCode == LocationStatusCodes.SUCCESS) {
                            BootstrapApplication.getInstance().setGeofencesLL(null);
                            if(SERVICE_IS_DESTROYED) {
                                onStop();
                            }else if(RESET_GEOFENCES_IN_PROGRESS){
                                new SetAPACords(BootstrapApplication.getInstance(), cords).execute();
                            }else{
                                new GetAPACords(mContext, postalCode).execute();
                            }
            /*
             * Handle successful removal of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
                        } else {
                            // If adding the geocodes failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
                        }
        /*
         * Disconnect the location client regardless of the
         * request status, and indicate that a request is no
         * longer in progress
         */
                        mInProgress = false;

                    }
                });
                break;
        }

    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(mContext, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();

        // Turn off the request flag
        mInProgress = false;
    }


    protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            mLocationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        mLocationClient.disconnect();
    }

    @Override
    public void onDestroy() {
        SERVICE_IS_DESTROYED = true;
        if (mTransitionPendingIntent != null) {
            removeGeofences(mTransitionPendingIntent);
        }else{
            onStop();
        }
        stopActivityService();
    }

    private void stopActivityService() {
        Intent pushIntent = new Intent(this, UserActivityService.class);
        stopService(pushIntent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] strings) {

        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            Intent temp = new Intent(MainActivity.BROADCAST);
            temp.putExtra(MainActivity.INTENT_ACTION, MainActivity.RESET_GEOFENCES_ACTION);
            sendBroadcast(temp);
        } else {
            // Toast.makeText(this,"Geofences not added",Toast.LENGTH_LONG).show();
        }
        // Turn off the in progress flag and disconnect the client
        mInProgress = false;
        RESET_GEOFENCES_IN_PROGRESS = false;
    }

    /**
     * A subclass of AsyncTask that calls getFromLocation() in the
     * background. The class definition has these generic types:
     * Location - A Location object containing
     * the current location.
     * Void     - indicates that progress units are not used
     * String   - An address passed to onPostExecute()
     */
    private class GetAPACords extends
            AsyncTask<Void, Void, List<LatLng>> {
        Context mContext;
        int mPostalCode;

        public GetAPACords(Context context, int pc) {
            super();
            mContext = context;
            mPostalCode = pc;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         * @params params One or more Location objects
         */

        @Override
        protected List<LatLng> doInBackground(Void... params) {
            String url = "http://data.cityofnewyork.us/resource/h9gi-nx95.json?$select=longitude,latitude&zip_code=" + mPostalCode;
            JSONArray jsonArray = jParser.getJson(url);
            List<LatLng> mCords = new ArrayList<LatLng>();
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        double latitude = jsonArray.getJSONObject(i).getDouble("latitude");
                        double longitude = jsonArray.getJSONObject(i).getDouble("longitude");
                        if (!isDup(mCords, latitude, longitude)) {
                            mCords.add(new LatLng(latitude, longitude));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return mCords;
            }else{
                return null;
            }
        }

        /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
        @Override
        protected void onPostExecute(List<LatLng> latLngs) {
            if(latLngs!= null) {
                cords = latLngs;
                new SetAPACords(mContext, cords).execute();
            }else{
                Toast.makeText(mContext,"APA Locations are not available!",Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isDup(List<LatLng> temp, double lat, double lng) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).latitude == lat && temp.get(i).longitude == lng) {
                return true;
            }
        }
        return false;
    }

    private class SetAPACords extends
            AsyncTask<Void, Void, Integer> {
        Context mContext;
        List<LatLng> mCords;

        public SetAPACords(Context context, List<LatLng> mCords) {
            super();
            mContext = context;
            this.mCords = mCords;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         * @params params One or more Location objects
         */

        @Override
        protected Integer doInBackground(Void... params) {
            final double mult = 1; // mult = 1.1; is more reliable
            final int radius = 300;
            LatLng p1 = BootstrapApplication.calculateDerivedPosition(mLocation, mult * radius, 0);
            LatLng p2 = BootstrapApplication.calculateDerivedPosition(mLocation, mult * radius, 90);
            LatLng p3 = BootstrapApplication.calculateDerivedPosition(mLocation, mult * radius, 180);
            LatLng p4 = BootstrapApplication.calculateDerivedPosition(mLocation, mult * radius, 270);
            List<LatLng> unsortedNBLatLng = new ArrayList<LatLng>();

            for (int i = 0; i < mCords.size(); i++) {
                if (mCords.get(i).latitude > p3.latitude && mCords.get(i).latitude < p1.latitude && mCords.get(i).longitude > p4.longitude && mCords.get(i).longitude < p2.longitude) {
                    unsortedNBLatLng.add(mCords.get(i));
                }
            }
            List<LatLng> snbl = compareLoc(unsortedNBLatLng, mLocation);
            if (snbl.size() >= 50) {
                for (int i = 0; i < 50; i++) {
                    mGeofenceList.add(new APAGeofence(snbl.get(i).latitude + "" + snbl.get(i).longitude, snbl.get(i).latitude, snbl.get(i).longitude, GEOFENCE_RADIUS, GEOFENCE_EXPIRATION_TIME, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).toGeofence());

                    BootstrapApplication.getInstance().getGeofencesLL().add(snbl.get(i));
                }
                // BootstrapApplication.getInstance().setGeofencesLL(snbl.subList(0,49));
            } else {
                for (int i = 0; i < snbl.size(); i++) {
                    mGeofenceList.add(new APAGeofence(snbl.get(i).latitude + "" + snbl.get(i).longitude, snbl.get(i).latitude, snbl.get(i).longitude, GEOFENCE_RADIUS, GEOFENCE_EXPIRATION_TIME, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).toGeofence());

                    BootstrapApplication.getInstance().getGeofencesLL().add(snbl.get(i));
                }
                // BootstrapApplication.getInstance().setGeofencesLL(snbl.subList(0,snbl.size()));
            }
            if(mGeofenceList.size()>0){
                return 1;
            }else {
                return 0;
            }
        }


        /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
        @Override
        protected void onPostExecute(Integer value) {
            if (value == 1) {
                addGeofences();
            }else{
                Toast.makeText(mContext,"No AccidentProneAreas around!",Toast.LENGTH_LONG).show();
            }
        }
    }

}
