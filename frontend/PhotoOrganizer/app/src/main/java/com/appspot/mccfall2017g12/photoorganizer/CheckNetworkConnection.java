package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckNetworkConnection {

    //fetch information of the current active network
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }
    //Check if the active network has connectivity
    public static boolean isConnected(Context context){
        NetworkInfo info = CheckNetworkConnection.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

   //Check if wifi connection exists
    public static boolean isConnectedWifi(Context context){
        NetworkInfo info = CheckNetworkConnection.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    //Check if mobile network connection exists
    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = CheckNetworkConnection.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }
}
