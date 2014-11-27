package aaremm.com.projectci.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

/**
 * Created by rahul on 26-08-2014.
 */
public class LocationOnOff  extends BroadcastReceiver{

    // context
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        final LocationManager manager = (LocationManager) mContext.getSystemService( Context.LOCATION_SERVICE );
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            /*Toast.makeText(mContext, "GPS enabled",
                    Toast.LENGTH_SHORT).show();*/
            Intent pushIntent = new Intent(mContext, UserLocationService.class);
            context.startService(pushIntent);
        }else{
            /*Toast.makeText(mContext, "GPS disabled",
                    Toast.LENGTH_SHORT).show();*/
            Intent pushIntent = new Intent(mContext, UserLocationService.class);
            context.stopService(pushIntent);
            Intent pushIntent2 = new Intent(mContext, UserActivityService.class);
            context.stopService(pushIntent2);
        }
    }


}