package aaremm.com.projectci.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Random;

import aaremm.com.projectci.R;
import aaremm.com.projectci.config.BootstrapApplication;
import aaremm.com.projectci.service.DeviceAdmin;
import aaremm.com.projectci.service.UserLocationService;
import aaremm.com.projectci.service.UserMode;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class LookActivity extends Activity implements View.OnClickListener {

    @InjectView(R.id.b_look_iDo)
    Button b_iDo;
    @InjectView(R.id.b_look_iDont)
    Button b_iDont;
    @InjectView(R.id.b_look_passenger)
    Button b_passenger;
    @InjectView(R.id.b_look_indoor)
    Button b_indoor;
    @InjectView(R.id.b_look_maps)
    Button b_maps;

    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    private LocationManager manager;
    public static final int REQUEST_CODE_ENABLE_ADMIN = 10;
    private boolean dontPressed = false;
    private String alerts[] = {"Look Ahead!", "Stay Alert!", "Watch Out!"};
    private String modes[] = {"Passenger", "Indoor", "Using maps"};
    //notification
    NotificationManager nf;
    NotificationCompat.Builder mBuilder;
    public static final int notifyID = 15000; //if you know what i mean
    public static final int notifyModID = 5000; //if you know what i mean

    Intent clickI, cancelI, iDoI, iDontI;
    PendingIntent clickPI, cancelPI, iDoPI, iDontPI;

    Random r = new Random();

    public static final String BROADCAST = "aaremm.com.projectci.android.nfaction.broadcast";
    public static final String INTENT_ACTION = "action";
    public static final String CANCEL_ACTION = "cancel";
    public static final String IDO_ACTION = "ido";
    public static final String IDONT_ACTION = "idont";
    public static final String OUTOFAPA_ACTION = "outofAPA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look);
        ButterKnife.inject(this);

        String userActivity = getIntent().getStringExtra("USER_ACTIVITY");
        if(userActivity!= null){
            Toast.makeText(this, "User is "+userActivity, Toast.LENGTH_LONG).show();
        }

        b_iDo.setOnClickListener(this);
        b_iDont.setOnClickListener(this);
        b_passenger.setOnClickListener(this);
        b_indoor.setOnClickListener(this);
        b_maps.setOnClickListener(this);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminName = new ComponentName(this, DeviceAdmin.class);
        nf = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!mDPM.isAdminActive(mAdminName)) {
            // Launch the activity to have the user enable our admin.
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This permission is required to lock the phone screen automatically.");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);

        }
        clickI = new Intent(this, LookActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        clickPI = PendingIntent.getBroadcast(this, 0, clickI, 0);

        cancelI = new Intent(this, UserMode.class);
        cancelI.setAction(CANCEL_ACTION);
        cancelPI = PendingIntent.getBroadcast(this, 0, cancelI, 0);

        iDoI = new Intent(this, UserMode.class);
        iDoI.setAction(IDO_ACTION);
        iDoPI = PendingIntent.getBroadcast(this, 12345, iDoI, PendingIntent.FLAG_UPDATE_CURRENT);

        iDontI = new Intent(this, UserMode.class);
        iDontI.setAction(IDONT_ACTION);
        iDontPI = PendingIntent.getBroadcast(this, 12345, iDontI, PendingIntent.FLAG_UPDATE_CURRENT);

        displayAlertNotification();
    }

    private void displayAlertNotification() {
        int i1 = r.nextInt(3);
        mBuilder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_ic)
                .setTicker(alerts[i1])
                .setContentTitle("Accident Prone Area")
                .setContentText("Do you care about your LIFE?")
                .setContentIntent(clickPI)
                .setVibrate(new long[]{0, 1000, 1000})
                .addAction(R.drawable.ic_stat_right, "I do", iDoPI)
                .addAction(R.drawable.ic_stat_wrong_128, "I don't", iDontPI);
        nf.notify(notifyID, mBuilder.build());
        if (BootstrapApplication.getInstance().isOnCall()) {
            new playNotificationMediaFileAsyncTask().execute();
        }

    }

    private void displayModeNotification(int m) {
        nf.cancel(notifyID);
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic)
                .setTicker(modes[m])
                .setContentTitle(modes[m])
                .setContentText("Swipe to disable the mode.")
                .setDeleteIntent(cancelPI);
        nf.notify(notifyModID, mBuilder.build());

    }

    private void displayOutOfAPANotification() {
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic)
                .setTicker("Out of APA")
                .setContentTitle("Be Safe")
                .setContentText("You're now out of Accident Prone Area.")
                .setVibrate(new long[]{0, 500, 500,500,500,500});
        nf.notify(notifyID, mBuilder.build());
    }

    private static class playNotificationMediaFileAsyncTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {
            MediaPlayer mediaPlayer = null;
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setLooping(false);
                mediaPlayer.setDataSource(BootstrapApplication.getInstance(), Settings.System.DEFAULT_NOTIFICATION_URI);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.release();
                    }
                });
                return null;
            } catch (Exception ex) {
                Log.e("ERROR: ", ex.toString());
                mediaPlayer.release();
                return null;
            }
        }

        protected void onPostExecute(Void result) {
            //Do Nothing
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ENABLE_ADMIN:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(BootstrapApplication.APPTAG, "Administration enabled!");
                } else {
                    Log.i(BootstrapApplication.APPTAG, "Administration enable FAILED!");
                }
                return;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.b_look_iDo: {
                nf.cancel(notifyID);
                mDPM.lockNow();
                finish();
            }
            break;
            case R.id.b_look_iDont: {
                if (dontPressed) {
                    nf.cancel(notifyID);
                    finish();
                } else {
                    Toast.makeText(this, "Ok Tough Guy, Press it again!", Toast.LENGTH_LONG).show();
                    dontPressed = true;
                }
            }
            break;
            case R.id.b_look_passenger: {
                nevermind(0);
            }
            break;
            case R.id.b_look_indoor: {
                nevermind(1);
            }
            break;
            case R.id.b_look_maps: {
                nevermind(2);
            }
            break;

        }

    }

    private void nevermind(int m) {
        Intent locIntent = new Intent(this, UserLocationService.class);
        stopService(locIntent);
        Toast.makeText(this, "Nevermind!", Toast.LENGTH_LONG).show();
        displayModeNotification(m);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.look, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        BootstrapApplication.getInstance().activityResumed();
        IntentFilter intentFilter = new IntentFilter(BROADCAST);
        registerReceiver(nfDoDontAction, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BootstrapApplication.getInstance().activityPaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BootstrapApplication.getInstance().activityDestroyed();
        unregisterReceiver(nfDoDontAction);
    }

    private BroadcastReceiver nfDoDontAction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(INTENT_ACTION);

            if (LookActivity.IDO_ACTION.equalsIgnoreCase(action)) {
                nf.cancel(notifyID);
                mDPM.lockNow();
                finish();
            } else if (LookActivity.IDONT_ACTION.equalsIgnoreCase(action)) {
                nf.cancel(LookActivity.notifyID);
                if (BootstrapApplication.getInstance().isMediaMusicPaused()) {
                    BootstrapApplication.getInstance().abandonAudioFocus();
                }
                finish();
            } else if (LookActivity.OUTOFAPA_ACTION.equalsIgnoreCase(action)) {
                displayOutOfAPANotification();
                if (BootstrapApplication.getInstance().isMediaMusicPaused()) {
                    BootstrapApplication.getInstance().abandonAudioFocus();
                }
                finish();
            }
        }
    };

}
