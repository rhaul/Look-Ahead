package aaremm.com.projectci.service;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import aaremm.com.projectci.activity.LookActivity;
import aaremm.com.projectci.config.BootstrapApplication;

/**
 * Created by rahul on 01-09-2014.
 */
public class UserMode extends BroadcastReceiver {

    DevicePolicyManager mDPM;
    ComponentName mAdminName;
    NotificationManager nm;
    LocationManager manager;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(BootstrapApplication.getInstance().isActivityDestroyed()) {
            manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            mAdminName = new ComponentName(context, DeviceAdmin.class);
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String action = intent.getAction();

            if (LookActivity.IDO_ACTION.equalsIgnoreCase(action)) {
                nm.cancel(LookActivity.notifyID);
                mDPM.lockNow();
            } else if (LookActivity.IDONT_ACTION.equalsIgnoreCase(action)) {
                nm.cancel(LookActivity.notifyID);
                if(BootstrapApplication.getInstance().isMediaMusicPaused()) {
                    BootstrapApplication.getInstance().abandonAudioFocus();
                }
            } else if (LookActivity.CANCEL_ACTION.equalsIgnoreCase(action)) {
                checkGPS(context);
            }
        }else{
            Intent temp = new Intent(LookActivity.BROADCAST);
            temp.putExtra(LookActivity.INTENT_ACTION,intent.getAction());
            context.sendBroadcast(temp);
        }
    }

    private void checkGPS(Context context) {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (BootstrapApplication.getInstance().getSharedPreferencesBoolean(BootstrapApplication.SWITCH)) {
                Intent pushIntent = new Intent(context, UserLocationService.class);
                context.startService(pushIntent);
            } else {
                Intent pushIntent2 = new Intent(context, UserLocationService.class);
                context.stopService(pushIntent2);
            }
        }
    }

}
