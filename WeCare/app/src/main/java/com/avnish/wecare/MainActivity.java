package com.avnish.wecare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.security.auth.Subject;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    String TAG = "DEBUG";
    TextView g_id;
    LocationListener locationListenerGPS;
    public static String RemoteUrl ="https://avi-01-we-care.herokuapp.com";
    public static String LocalUrl ="http://192.168.43.228:3000";
    public static String url =RemoteUrl;
    public static User user = new User(null, null, null, null,null,null,null);
    Button helpButton;
    public  static Socket mSocket;

    {
        try {
            mSocket = IO.socket(url);
        } catch (URISyntaxException e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //TODO: remove Intent
//        Intent i = new Intent(MainActivity.this,AddFriends.class);
//        startActivity(i);

        locationListenerGPS = newLocationListener();

//        Button signOutButton = findViewById(R.id.sign_out);
//
//        signOutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signOut();
//            }
//        });

        checkPer();

        mSocket.connect();

        mSocket.emit("Location","help");

        g_id = findViewById(R.id.g_id);

        helpButton = findViewById(R.id.helpButton);

        ImageButton add = findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,AddFriends.class);
                startActivity(i);
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(TAG, "onClick: Location to be start");

                sendLocation();

                Log.e(TAG, "onClick: Location Stat");

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"name\":\""+user.getName()+"\",\"google_id\":\""+user.getId()+"\"}");
                sendData(body,"post","/help");
            }
        });

         Emitter.Listener onNewMessage = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    JSONObject data = (JSONObject) args[0];
//                    String username;
//                    String message;
//                    try {
//                        username = data.getString("username");
//                        message = data.getString("message");
//                    } catch (JSONException e) {
//                        return;
//                    }

                // add the message to view
//                    addMessage(username, message);

                Log.e(TAG, "call: Receviced");
//                attemptSend("new message","Receviced");
                }
            });
            }
        };

        mSocket.on("sendLocation", onNewMessage);

    }


    private void sendLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            buildAlertMessageNoGps();
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkPer();
            }

            Log.e(TAG, "sendLocation: Updating location" );
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location==null)
            {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            }
            if(location!=null)
            {
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"latitude\":\""+location.getLatitude()+"\",\"longitude\":\""+location.getLongitude()+"\"}");
                sendData(body,"patch","/users/me?google_id=" + user.getId());
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,0 , locationListenerGPS);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0 , locationListenerGPS);
        }


    }


    private LocationListener newLocationListener() {
        LocationListener locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {


//                String msg1="location New Latitude: "+location.getLatitude() + "   New Longitude: "+location.getLongitude();
//                Log.e("lo",msg1+"");

//                if(user.getLatitude()!=null)
//                {
//
//                    user.setLatitude(Double.toString(location.getLatitude()));
//                    user.setLongitude(Double.toString(location.getLongitude()));
//                    return;
//                }

                user.setLatitude(Double.toString(location.getLatitude()));
                user.setLongitude(Double.toString(location.getLongitude()));


                Log.e("update", "handleSignInResult: body  "+" "+user.getLatitude()+ " "+ user.getLongitude() );

                String latitudeCurr = user.getLatitude();
                String longitudeCurr = user.getLongitude();
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"latitude\":\""+latitudeCurr+"\",\"longitude\":\""+longitudeCurr+"\"}");
                Request request = new Request.Builder()
                        .url(MainActivity.url+"/users/me?google_id="+user.getId())
                        .patch(body)
                        .build();


                Callback responseCall = new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                        e.printStackTrace();

                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        final String yourResponse = response.body().string();
                        if (response.isSuccessful()) {

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "runResponse: "+yourResponse);
                                }
                            });
                        } else {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Log.e(TAG, "runResponse: "+yourResponse);
                                }
                            });
                        }
                        response.body().close();
                    }

                };

                try {
                    Call call = client.newCall(request);
                    call.enqueue(responseCall);


                    Log.e(TAG, "handleSignInResult: check");
                }catch(Exception e){

                    Log.e(TAG, "handleSignInResult: error "+e);
                }


                Log.e("Location",latitudeCurr+" "+longitudeCurr);


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




    private void checkPer() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG,"Permission is provided");
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION, },
                    1);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.e(TAG,"permissions granted");
            }
            else
            {
                finish();
            }
        }
    }

    public void buildAlertMessageNoGps() {
        Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    public static void attemptSend(String name,String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        mSocket.emit(name, message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {

            googleSignIn.updateUI(acct);
        }
        else
        {
            Intent SignIn = new Intent(this,googleSignIn.class);
            startActivity(SignIn);
            finish();
        }

        g_id.setText("Google id: "+user.getId());

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        MediaType mediaType = MediaType.parse("application/json");
                        RequestBody body = RequestBody.create(mediaType, "{\"token\":\"" + token + "\"}");
                        sendData(body,"patch","/users/me?google_id=" + user.getId());
                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("Token", msg);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendData(RequestBody body,String type, String location)
    {

        Log.e(TAG, "sendData: "+body);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(MainActivity.url + location)
                .post(body)
                .build();

        if(type=="patch")
        {

            request = new Request.Builder()
                    .url(MainActivity.url + location)
                    .patch(body)
                    .build();
        }

        Log.e(TAG, "handleSignInResult: body  " + body.contentType()+" "+request.method());

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

    private void signOut() {
        Intent SignIn = new Intent(this,googleSignIn.class);
        startActivity(SignIn);
        finish();
    }

    public static class NotificationID {
        public static int getID() {
            Date now = new Date();
            int id = Integer.parseInt(new SimpleDateFormat("ddHHSS",  Locale.US).format(now));
            id +=(int) (Calendar.getInstance().getTimeInMillis()%10000000);
            id%=1000000007;
            return id;
        }
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
