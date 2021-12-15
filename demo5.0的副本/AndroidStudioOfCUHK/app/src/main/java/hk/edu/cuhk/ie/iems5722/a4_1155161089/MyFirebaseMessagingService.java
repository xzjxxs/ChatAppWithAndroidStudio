package hk.edu.cuhk.ie.iems5722.a4_1155161089;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingService";
    private static int count = 0;
    private final String user_id = "1155161089";

    // This ID can be the value you want.
    private static final String CHANNEL_ID_STRING = "Message Notifications";

    @SuppressLint("LongLogTag")

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // insert the token to database
        sendRegistrationToServer(token);
    }

    // submit token to server
    private void sendRegistrationToServer(String token) {
        try {
            URL url = new URL("http://18.116.63.13/api/a4/submit_push_token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            Uri.Builder builder = new Uri.Builder();

            // build the parameters using ArrayList objects para_names and para_values
            builder.appendQueryParameter("user_id", user_id);
            builder.appendQueryParameter("token", token);

            String query = builder.build().getEncodedQuery();
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = conn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                Log.d("Push token: ", "Success");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    This callback function will be called when an FCM message is received in foreground
    //    (except for a notification message is received when app is in background)
    @SuppressLint("LongLogTag")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            // send a notification to client
            clickNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("tag"),
                    remoteMessage.getData().get("body"));
            count = count + 1; // for multiple notifications
        }
        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message notification payload: " + remoteMessage.getNotification().getBody());
        }
    }

    // build a notification and create an intent for jumping to corresponding page
    private void clickNotification(String chatroom_name, String chatroom_id, String messageBody) {
        // create an intent where notification is clicked to jump to
        Intent intent = new Intent(this, ChatActivity.class);
        Log.d("notify chatroom_id", chatroom_id);
        Log.d("notify chatroom_name", chatroom_name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("chatroomId", chatroom_id);
        intent.putExtra("chatroomName", chatroom_name);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, count, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // create a channel when android api > 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_STRING, "Message Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("My notification channel description");
            notificationManager.createNotificationChannel(channel);
        }

        // create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_STRING)
                .setSmallIcon(R.drawable.send)
                .setContentTitle(chatroom_name)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // set the intent that will fire when the user taps the notification
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(count, builder.build());
    }
}
