package com.avnish.wecare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String id;


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob();
            } else {
                // Handle message within 10 seconds
//                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            Intent showDetails = new Intent(this,SHOW_DEATILS.class);
//            showDetails.putExtra("field1",remoteMessage.getNotification().getBody());
        }

        sendNotification(remoteMessage.getNotification().getBody(),remoteMessage.getData());
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }


    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        SQLiteDatabase wc=openOrCreateDatabase("am",MODE_PRIVATE,null);
        wc.execSQL("create table if not exists user(name varchar,email varchar,id varchar)");
        Cursor cursor = wc.rawQuery("select * from user",null);
        if (cursor.moveToFirst())
        {
            id = cursor.getString(2);
        }
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"token\":\"" + token + "\"}");
        sendData(body);
    }

    private void sendData(RequestBody body)
    {

        Log.e(TAG, "sendData: "+body);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(MainActivity.url + "/users/me?google_id=" + id)
                .patch(body)
                .build();

        Log.e(TAG, "handleSignInResult: body  " + body.contentType());

        Callback responseCall = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

                e.printStackTrace();

            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String yourResponse = response.body().string();
                if (response.isSuccessful()) {
                    Log.e(TAG, "run: " + yourResponse);
                } else {
                    Log.e(TAG, "run: " + yourResponse);
                }

                response.body().close();
            }

        };

        try {
            Call call = client.newCall(request);
            call.enqueue(responseCall);


            Log.e(TAG, "handleSignInResult: check");
        } catch (Exception e) {

            Log.e(TAG, "handleSignInResult: error " + e);
        }
    }

    private void sendNotification(String messageBody, Map<String,String> content) {
        Log.e(TAG, "sendNotification: ");
        Intent intent = new Intent(this, SHOW_DEATILS.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle b1= new Bundle();
        b1.putString("latitude",content.get("latitude"));
        b1.putString("longitude",content.get("longitude"));
        b1.putString("name",content.get("name"));
        b1.putString("distance",content.get("distance"));
        intent.putExtras(b1);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "1";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(content.get("name")+" needs your help")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}