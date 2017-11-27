package com.appspot.mccfall2017g12.photoorganizer;

/**
 * Created by Edgar on 27/11/2017.
 */

public class User {
    private static volatile String username;
    private static volatile String groupid;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        User.username = username;
    }

    public static String getGroupid() {
        return groupid;
    }

    public static void setGroupid(String groupid) {
        User.groupid = groupid;
    }
}
