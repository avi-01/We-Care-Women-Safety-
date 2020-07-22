package com.avnish.wecare;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class MyBroadcastReceiver extends BroadcastReceiver {
    LocationListener locationListenerGPS;
    Context mcontext;
    String id;
    LocationManager locationManager;
    @Override
    public void onReceive(Context context, Intent intent) {



        int i = intent.getIntExtra("no",2);

        Log.e("Recevier", "Recieved "+i);


        SQLiteDatabase wc=context.openOrCreateDatabase("am",MODE_PRIVATE,null);
        Cursor curosor = wc.rawQuery("select * from user",null);
        if (curosor.moveToFirst())
        {
            id = curosor.getString(2);
        }
        mcontext = context;

        locationListenerGPS = newLocationListener();
        sendLocation();

        Intent intent1 = new Intent(context, MyBroadcastReceiver.class);
        intent1.putExtra("no",i+1);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MainActivity.NotificationID.getID(), intent1, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        //TODO : open the comment
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30 * 60 * 1000, pendingIntent);

    }

    public void sendLocation() {
        locationManager = (LocationManager) mcontext.getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else {
            if (mcontext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && mcontext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerGPS);

        }

    }

    private void checkPer() {
        if (mcontext.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                mcontext.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "Permission is provided");
        } else {
            return;
        }
    }

    private void buildAlertMessageNoGps() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        mcontext.startActivity(intent);
    }


    private LocationListener newLocationListener() {
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {

//                String channelId = "2";
//                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                NotificationCompat.Builder notificationBuilder =
//                        new NotificationCompat.Builder(mcontext, channelId)
//                                .setSmallIcon(R.drawable.logo)
//                                .setContentTitle("needs your help")
//                                .setContentText("s")
//                                .setAutoCancel(true)
//                                .setSound(defaultSoundUri);
//
//                NotificationManager notificationManager =
//                        (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
//
//                // Since android Oreo notification channel is needed.
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    NotificationChannel channel = new NotificationChannel(channelId,
//                            "Channel human readable title",
//                            NotificationManager.IMPORTANCE_DEFAULT);
//                    notificationManager.createNotificationChannel(channel);
//                }
//
//                notificationManager.notify((int)System.currentTimeMillis() /* ID of notification */, notificationBuilder.build());
//

                String msg1="location New Latitude: "+location.getLatitude() + "   New Longitude: "+location.getLongitude();
                Log.e("lo",msg1+"");


                String latitudeCurr = Double.toString(location.getLatitude());
                String longitudeCurr = Double.toString(location.getLongitude());
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"latitude\":\"" + latitudeCurr + "\",\"longitude\":\"" + longitudeCurr + "\"}");
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


                Log.e("Location", latitudeCurr + " " + longitudeCurr);

                locationManager.removeUpdates(locationListenerGPS);


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        return locationListener;
    }
}



