package com.appspot.mccfall2017g12.photoorganizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;


/**
 * Created by Ilkka on 25.11.2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    int status = CheckNetworkConnection.getConnectivityStatusString(context);
                    if (status == CheckNetworkConnection.NETWORK_STATUS_WIFI){
                        //use wifi preferences
                        Toast.makeText(context, "Wifi", Toast.LENGTH_SHORT).show();
                    }
                    else if (status == CheckNetworkConnection.NETWORK_STATUS_MOBILE){
                        //use mobile preferences
                        Toast.makeText(context, "Mobile", Toast.LENGTH_SHORT).show();
                    }
                    else if (status == CheckNetworkConnection.NETWORK_STATUS_NOT_CONNECTED){
                        //do stuff
                        Toast.makeText(context, "No connection", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 2000);

        }
    }
}