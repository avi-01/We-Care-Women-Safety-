package com.avnish.wecare;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;
import static com.avnish.wecare.MainActivity.attemptSend;
import static com.avnish.wecare.MainActivity.mSocket;
import static com.avnish.wecare.MainActivity.user;

public class backgroundTask extends Service {
    private final LocalBinder mBinder = new LocalBinder();
    protected Handler handler;
    protected Toast mToast;

    public class LocalBinder extends Binder {
        public backgroundTask getService() {
            return backgroundTask.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: destroy the service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent,flags,startId);
        Log.d(TAG, "run: running service");

            for(int i=0;i<10;i++)
        {
            Log.e(TAG, "run: loop "+i);

            if(i==2)
            {
                Log.e(TAG, "onStartCommand: "+i );
                attemptSend("Location","Help");
            }
            else
            {
                attemptSend("new message","Hello "+user.getName());
            }

            try{
                Thread.sleep(2*1000);
            }catch (Exception e)
            {
                Log.e(TAG, "run: Error");
            }
        }

//            mSocket.on("sendLocation", onNewMessage);
        return START_STICKY;
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
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
                    attemptSend("new message","Receviced");
//                }
//            });
        }
    };

}