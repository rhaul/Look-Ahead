package aaremm.com.projectci.config;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.AudioManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BootstrapApplication extends Application implements AudioManager.OnAudioFocusChangeListener {
    // app
    private static BootstrapApplication instance;
    private static int DISK_IMAGECACHE_SIZE = 1024 * 1024 * 20;
    private static Bitmap.CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static int DISK_IMAGECACHE_QUALITY = 80;  //PNG is lossless so quality is ignored but must be provided
    public static SharedPreferences sp;
    // Debugging tag for the application
    public static final String APPTAG = "look";
    public static final String SWITCH = "switch";



    public AudioManager manager;
    /**
     * Create main application
     */
    public BootstrapApplication() {

    }

    /**
     * Create main application
     *
     * @param context
     */
    public BootstrapApplication(final Context context) {
        this();
        attachBaseContext(context);
    }


    public static void setSP(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }


    public static void setSPBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    public static void setSPInteger(String key, int value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static Integer getSPInteger(String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        return sharedPreferences.getInt(key, 0); // 0 - professor 1 - student
    }

    public static String getSharedPreferencesString(String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        return sharedPreferences.getString(key, "");
    }

    public static Boolean getSharedPreferencesBoolean(String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        return sharedPreferences.getBoolean(key, true);
    }

/*
    public ArrayList<Typeface> getTypeface(int index) {
        String fontPath;
        Typeface tf;
        ArrayList<Typeface> tfs = new ArrayList<Typeface>();
        switch (index) {
            case 3:
                fontPath = "fonts/Lato-Reg.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/Lato-Bol.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/Lato-Lig.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/Lato-Bla.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                return tfs;
            case 4:
                fontPath = "fonts/OpenSans-Regular.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/OpenSans-Bold.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/OpenSans-Semibold.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/OpenSans-Light.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                return tfs;
        }
        return null;
    }*/


    public String getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;

                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.d("HashKey", something);
                return something;
            }
        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    }
/*


    private void setupFragments() {

        // texts
        texts.add("happy");
        texts.add("sad");
        texts.add("romantic");
        texts.add("crying");
        texts.add("naughty");
        texts.add("angry");
        texts.add("silent");
        texts.add("sleepy");
        texts.add("kissing");
        texts.add("whatever you're doing\n...\nI always want to be with you.");
        texts.add("so how's life? where's your mom and dad?\nwhat's going on...\nwait a minute!");
        texts.add("am I forgetting something?");
        texts.add("ok\nI remember...\nyou know what");
        Calendar temp = Calendar.getInstance();
        String date = ""+temp.get(Calendar.MONTH)+""+temp.get(Calendar.DAY_OF_MONTH);
        if(date.equalsIgnoreCase("710")) {
            texts.add("The BEST thing that has ever happened in this entire UNIVERSE on this day is...");
        }else{
            texts.add("The BEST thing that has ever happened in this entire UNIVERSE on the 10th August is...");
        }

        //images
        moods.add(R.drawable.happy);
        moods.add(R.drawable.sad);
        moods.add(R.drawable.inlove);
        moods.add(R.drawable.crying);
        moods.add(R.drawable.naughty);
        moods.add(R.drawable.angry);
        moods.add(R.drawable.silent);
        moods.add(R.drawable.sleepy);
        moods.add(R.drawable.kissing);

        //Colors
        colors.add(R.color.turqoise);
        colors.add(R.color.peter);
        colors.add(R.color.tall);
        colors.add(R.color.emarld);
        colors.add(R.color.amethyst);
        colors.add(R.color.wet);
        colors.add(R.color.sun);
        colors.add(R.color.concrete);
        colors.add(R.color.alizarin);
        colors.add(R.color.turqoise);
        colors.add(R.color.peter);
        colors.add(R.color.tall);
        colors.add(R.color.emarld);
        colors.add(R.color.amethyst);
        colors.add(R.color.wet);
    }
*/

    /**
     * Create main application
     *
     * @param instrumentation
     */
    public BootstrapApplication(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    public static BootstrapApplication getInstance() {

        if (instance == null) {
            instance = new BootstrapApplication();
            return instance;

        } else {
            return instance;
        }
    }

    public boolean isActivityVisible() {
        return activityVisible;
    }
    public boolean isActivityDestroyed() {
        return activityDestroyed;
    }

    public void activityResumed() {
        activityVisible = true;
        activityDestroyed = false;
    }

    public void activityPaused() {
        activityVisible = false;
        activityDestroyed = false;
    }

    public void activityDestroyed() {
        activityDestroyed = true;
    }

    public void mediaMusicMuted(){
        mediaMusicPaused = true;
    }
    public void mediaMusicUnMuted(){
        mediaMusicPaused = false;
    }
    public boolean isMediaMusicPaused(){return mediaMusicPaused;}

    public boolean activityVisible = false, activityDestroyed = true,mediaMusicPaused=false;

    /**
     * Calculates the end-point from a given source at a given range (meters)
     * and bearing (degrees). This methods uses simple geometry equations to
     * calculate the end-point.
     *
     * @param point   Point of origin
     * @param range   Range in meters
     * @param bearing Bearing in degrees
     * @return End-point from the source given the desired range and bearing.
     */
    public static LatLng calculateDerivedPosition(Location point,
                                                  double range, double bearing) {
        double EarthRadius = 6371000; // m

        double latA = Math.toRadians(point.getLatitude());
        double lonA = Math.toRadians(point.getLongitude());
        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance)
                                * Math.cos(trueCourse)
        );

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance)
                        * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat)
        );

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        LatLng newLatLng = new LatLng(lat, lon);

        return newLatLng;

    }



    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
        // Pause playback
    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
        // Resume playback
    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
        manager.abandonAudioFocus(this);
        // Stop playback
    }

    }

    public void abandonAudioFocus(){
        manager.abandonAudioFocus(this);
    }

    public boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    public boolean isMusicOn() {
        if (manager.isMusicActive()) {
            int result = manager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.d(APPTAG, "Music is on");
                return true;
            }
        }
        return false;
    }

    public boolean isOnCall() {
        if (manager.getMode() == AudioManager.MODE_IN_CALL) {
            Log.d(APPTAG, "Ongoing Call");
            return true;
        }
        return false;
    }

    public List<LatLng> geofencesLL = new ArrayList<LatLng>();

    public void setGeofencesLL(List<LatLng> sortedgeo){
        if(sortedgeo == null){
            geofencesLL.clear();
        }else {/*
          //  List<LatLng> temp = sortedgeo;
            for (int i = 0; i <= sortedgeo.size() - 1; i++) {
                int k = i + 1;
                while (k < sortedgeo.size()) {
                    float results[] = new float[3];
                    Location.distanceBetween(sortedgeo.get(i).latitude, sortedgeo.get(i).longitude, sortedgeo.get(k).latitude, sortedgeo.get(k).longitude, results);
                    if (results[0] < 30) {
                        sortedgeo.add(i, new LatLng(((sortedgeo.get(i).latitude + sortedgeo.get(k).latitude) / (double)2), ((sortedgeo.get(i).longitude + sortedgeo.get(k).longitude) / (double)2)));
                        sortedgeo.remove(k);
                    }
                }
            }
*/
            geofencesLL = sortedgeo;
        }
    }
    public List<LatLng> getGeofencesLL(){
        return geofencesLL;
    }

    public LatLng currentLocation;

    public void setCurrentLocation(LatLng loc){
        currentLocation = loc;
    }
    public LatLng getCurrentLocation(){
        return currentLocation;
    }
}


