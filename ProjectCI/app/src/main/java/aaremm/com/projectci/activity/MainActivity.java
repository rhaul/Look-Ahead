package aaremm.com.projectci.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import aaremm.com.projectci.R;
import aaremm.com.projectci.config.BootstrapApplication;
import aaremm.com.projectci.service.DeviceAdmin;
import aaremm.com.projectci.service.UserLocationService;
import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity{

    @InjectView(R.id.iv_main_switch)
    ImageView mainSwitch;
    @InjectView(R.id.iv_guide)
    ImageButton guide;
    LocationManager manager;
    AlertDialog dialog;
    private GoogleMap mMap;
    private Marker now;

    public static final String BROADCAST = "aaremm.com.projectci.android.locaction.broadcast";
    public static final String INTENT_ACTION = "action";
    public static final String LC_ACTION = "locchanged";
    public static final String RESET_GEOFENCES_ACTION = "reset_geofence";

    public boolean flag_loc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkGPS();
        checkIfAdminActive();

        try {
            // Loading map
            initilizeMap();
            if (mMap != null) {
                displayCLandGeofences();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mainSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean value = BootstrapApplication.getInstance().getSharedPreferencesBoolean(BootstrapApplication.SWITCH);
                if (value) {
                    Intent pushIntent = new Intent(MainActivity.this, UserLocationService.class);
                    stopService(pushIntent);
                    mainSwitch.setImageResource(R.drawable.on);
                    value = false;
                    BootstrapApplication.getInstance().setSPBoolean(BootstrapApplication.SWITCH, value);
                }else{
                    checkGPS();
                }
            }
        });

        guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,DemoActivity.class));
            }
        });
    }


    /**
     * function to load map. If map is not created it will create it for you
     */
    private void initilizeMap() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            mMap.setMyLocationEnabled(false); // false to disable
            mMap.getUiSettings().setZoomControlsEnabled(false);

            // check if map is created successfully or not
            if (mMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void displayCLandGeofences() {
        if (BootstrapApplication.getInstance().getCurrentLocation() != null && BootstrapApplication.getInstance().getGeofencesLL() != null && BootstrapApplication.getInstance().getGeofencesLL().size() > 0) {
            mMap.clear();
            updateLocMarker();
            List<LatLng> geofencesLL = BootstrapApplication.getInstance().getGeofencesLL();

            for (int i = 0; i < geofencesLL.size(); i++) {
                LatLng fence = geofencesLL.get(i);
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(fence.latitude, fence.longitude))
                        .title("APA " + i)
                        .snippet("Radius: " + 30 + "m")).showInfoWindow();

                //Instantiates a new CircleOptions object +  center/radius
                CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(fence.latitude, fence.longitude))
                        .radius(30)
                        .fillColor(0x20ff0000)
                        .strokeColor(Color.TRANSPARENT)
                        .strokeWidth(2);

                Circle circle = mMap.addCircle(circleOptions);

            }
            mMap.animateCamera(CameraUpdateFactory.zoomTo(19));
        }
    }

    private void checkIfAdminActive() {
        DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mAdminName = new ComponentName(this, DeviceAdmin.class);

        if (!mDPM.isAdminActive(mAdminName)) {
            // Launch the activity to have the user enable our admin.
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This permission is required to lock the phone screen automatically.");
            startActivityForResult(intent, LookActivity.REQUEST_CODE_ENABLE_ADMIN);

        }
    }

    private void checkGPS() {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            /*Toast.makeText(this, "GPS enabled -start",
                    Toast.LENGTH_SHORT).show();*/
            Intent pushIntent = new Intent(this, UserLocationService.class);
            startService(pushIntent);
            mainSwitch.setImageResource(R.drawable.off);
            BootstrapApplication.getInstance().setSPBoolean(BootstrapApplication.SWITCH, true);
            if(mMap!=null) {
                mMap.clear();
            }

        } else {
            /*Toast.makeText(this, "GPS disabled",
                    Toast.LENGTH_SHORT).show();*/
            showDialogEnableGPS();
        }
    }

    private void showDialogEnableGPS() {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Enable GPS to use the App. Click OK to go to"
                + " location services settings.";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                Intent gpsOptionsIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                                d.dismiss();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        }
                );

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
        unregisterReceiver(onLocationChanged);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mainSwitch.setImageResource(R.drawable.on);
            Intent pushIntent = new Intent(MainActivity.this, UserLocationService.class);
            stopService(pushIntent);
            BootstrapApplication.getInstance().setSPBoolean(BootstrapApplication.SWITCH, false);
        }else{
            mainSwitch.setImageResource(R.drawable.off);
            Intent pushIntent = new Intent(this, UserLocationService.class);
            startService(pushIntent);
            BootstrapApplication.getInstance().setSPBoolean(BootstrapApplication.SWITCH, true);
        }
        initilizeMap();
        IntentFilter intentFilter = new IntentFilter(BROADCAST);
        registerReceiver(onLocationChanged, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver onLocationChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(INTENT_ACTION);

            if (LC_ACTION.equalsIgnoreCase(action)) {
                updateLocMarker();
            } else if (RESET_GEOFENCES_ACTION.equalsIgnoreCase(action)) {
                displayCLandGeofences();
            }
        }
    };

    private void updateLocMarker() {
        if (now != null) {
            now.remove();
        }
        // Creating a LatLng object for the current location
        LatLng latLng = BootstrapApplication.getInstance().getCurrentLocation();
        now = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.iamhere)).title("I am here!"));
        now.showInfoWindow();
       // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        if(mMap.getCameraPosition().zoom < 17){
            mMap.animateCamera(CameraUpdateFactory.zoomTo(19));
        }
    }

}
