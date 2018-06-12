package com.example.kiran.be_you;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Kiran on 7/16/2017.
 */

public class FirebaseMessagingservice extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String notification_title=remoteMessage.getNotification().getTitle();
        String notification_message=remoteMessage.getNotification().getBody();
        String click_action=remoteMessage.getNotification().getClickAction();
        String from_user_id=remoteMessage.getData().get("from_user_id");
        String userName=remoteMessage.getData().get("userName");
        String userprofile=remoteMessage.getData().get("userprofile");
        String userstatus=remoteMessage.getData().get("userstatus");

        // Sets an ID for the notification
        int mNotificationId = (int) System.currentTimeMillis();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.notification)
                        .setContentTitle(notification_title)
                        .setContentText(notification_message)
                        .setAutoCancel(true);




// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
       // mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        Notification notification = mBuilder.build();

// Builds the notification and issues it.
       // mNotifyMgr.notify(mNotificationId, mBuilder.build());
        mNotifyMgr.notify(mNotificationId, notification);


        final Intent resultIntent = new Intent(click_action);
       resultIntent.putExtra("user_name",userName);
        resultIntent.putExtra("user_profileimage",userprofile);
        resultIntent.putExtra("user_id",from_user_id);
        resultIntent.putExtra("user_status",userstatus);


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);







    }
}
