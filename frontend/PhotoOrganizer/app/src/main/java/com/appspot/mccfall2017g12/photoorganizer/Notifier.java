package com.appspot.mccfall2017g12.photoorganizer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Notifier {

    private static Notifier instance;
    private NotificationManager notificationManager;

    private Map<String, NotificationItem> notificationItems = new HashMap<>();
    private int nextNotificationId = 1;

    public static synchronized Notifier getInstance() {
        if (instance == null) {
            instance = new Notifier();
        }
        return instance;
    }

    private synchronized NotificationManager getNotificationManager(Context context) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private synchronized NotificationItem getItem(String albumId) {
        NotificationItem item;
        if (notificationItems.containsKey(albumId)) {
            item = notificationItems.get(albumId);
        }
        else {
            item = new NotificationItem(albumId, nextNotificationId++);
            notificationItems.put(albumId, item);
        }
        return item;
    }

    public void notifyAddPhoto(final String albumId, final Context context) {
        ThreadTools.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                notifyAddPhotoInternal(albumId, context);
            }
        });
    }

    public void cancel(final String albumId, final Context context) {
        ThreadTools.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                cancelInternal(albumId, context);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private synchronized void notifyAddPhotoInternal(String albumId, Context context) {
        NotificationItem item = getItem(albumId);
        item.count++;

        String name = LocalDatabase.getInstance(context).galleryDao().getAlbumName(albumId);
        if (name == null)
            name = context.getString(R.string.unknown_album);

        Intent resultIntent = new Intent(context, AlbumActivity.class);
        resultIntent.putExtra(AlbumActivity.EXTRA_ALBUM, albumId);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(AlbumActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.new_photos))
                .setContentText(String.format(Locale.getDefault(),
                        context.getString(R.string.new_photos_text), item.count, name))
                .setSmallIcon(R.drawable.ic_cloud_download_black_24dp)
                .setContentIntent(resultPendingIntent);

        getNotificationManager(context).notify(item.notificationId, builder.build());
    }

    private synchronized void cancelInternal(String groupId, Context context) {
        getNotificationManager(context).cancel(getItem(groupId).notificationId);
        notificationItems.remove(groupId);
    }

    private static class NotificationItem {
        public final String albumId;
        public final int notificationId;
        public int count = 0;

        public NotificationItem(String albumId, int notificationId) {
            this.albumId = albumId;
            this.notificationId = notificationId;
        }
    }
}
