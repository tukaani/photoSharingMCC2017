package com.appspot.mccfall2017g12.photoorganizer;

/**
 * Created by Edgar on 27/11/2017.
 */

public class User {
    private static volatile String username;
    private static volatile String groupId;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        User.username = username;
    }

    public static String getGroupId() {
        return groupId;
    }

    public static void setGroupId(String groupId) {
        User.groupId = groupId;
    }
}
