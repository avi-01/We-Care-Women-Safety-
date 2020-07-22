package com.avnish.wecare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class googleSignIn<satic> extends AppCompatActivity {

    String TAG ="DEBUG";
    int RC_SIGN_IN;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        Button signOutButton = findViewById(R.id.sign_out);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    public static void updateUI(GoogleSignInAccount account) {
        if(account!=null) {
            Log.e("Update","update");

            MainActivity.user.setEmail(account.getEmail());
            MainActivity.user.setId(account.getId());
            MainActivity.user.setName(account.getDisplayName());

            Log.e("Main",MainActivity.user.getEmail()+"");
            Log.e("Main",MainActivity.user.getName()+"");
            Log.e("Main",MainActivity.user.getId()+"");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

//            Intent phoneVerify = new Intent(this,PhoneVerify.class);
//            startActivity(phoneVerify);
            Log.e("Task",account.getEmail());
            Log.e("Task",account.getDisplayName());
            Log.e("Task",account.getId());

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"name\":\""+account.getDisplayName()+"\",\"google_id\":\""+account.getId()+"\",\"email\":\""+account.getEmail()+"\"}");
            Request request = new Request.Builder()
                    .url(MainActivity.url+"/users")
                    .post(body)
                    .build();

            Log.e(TAG, "handleSignInResult: body  "+body.contentType() );

            Callback responseCall = new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                    e.printStackTrace();
                    Log.e("Error", String.valueOf(e));

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    final String yourResponse = response.body().string();
                    if (response.isSuccessful()) {

                        googleSignIn.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(googleSignIn.this, "Ok: " + yourResponse, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "run: "+yourResponse);
                            }
                        });
                    } else {
                        googleSignIn.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(googleSignIn.this, "Ok: " + yourResponse, Toast.LENGTH_SHORT).show();

                                Log.e(TAG, "run: "+yourResponse);
                            }
                        });
                    }

//                    response.body().close();
                }

            };

            try {
                Call call = client.newCall(request);
                call.enqueue(responseCall);


                        Log.e(TAG, "handleSignInResult: check");
            }catch(Exception e){

                Log.e(TAG, "handleSignInResult: error "+e);
            }



            updateUI(account);


            SQLiteDatabase wc=openOrCreateDatabase("am",MODE_PRIVATE,null);
            wc.execSQL("create table if not exists user(name varchar,email varchar,id varchar)");

            wc.execSQL("insert into user values('"+account.getDisplayName()+"','"+account.getEmail()+"','"+account.getId()+"')");

            Intent intent2 = new Intent(googleSignIn.this, MyBroadcastReceiver.class);
            intent2.putExtra("no",1);

            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(googleSignIn.this, MainActivity.NotificationID.getID(),intent2, 0);
            AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager1.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1*1000 ,pendingIntent2);
            Intent main = new Intent(this,MainActivity.class);
            startActivity(main);
            finish();

        } catch (ApiException e) {


            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }



}
