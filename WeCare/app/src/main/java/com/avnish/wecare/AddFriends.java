package com.avnish.wecare;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class AddFriends extends AppCompatActivity {

    private static final String TAG = "AddFriends";
    ListView list;
    ArrayList<User> listItem;
    ArrayAdapter adapter;
    Dialog md;
    TextView error_mess;
    String go_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        listItem = new ArrayList<User>();
        list = findViewById(R.id.listView);

        ImageButton add = findViewById(R.id.add);

        md = new Dialog(this);


        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{}");
        sendData(body,"get","/users/friend?google_id="+MainActivity.user.getId());

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPopup(v);
            }
        });
    }

    private void sendData(RequestBody body,final String type, String location)
    {

        Log.e(TAG, "sendData: "+body);
        OkHttpClient client = new OkHttpClient();

        Request request;

        if(type=="post")
        {

            request = new Request.Builder()
                    .url(MainActivity.url + location)
                    .post(body)
                    .build();
        }
        else
        {
            request = new Request.Builder()
                .url(MainActivity.url + location)
                .get()
                .build();
        }

        Log.e(TAG, "handleSignInResult: body  " + body.contentType()+" "+request.method());

        Callback responseCall = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

                Toast.makeText(AddFriends.this, "Network Failure", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: error");
                e.printStackTrace();

            }

            @Override
            public void onResponse(final Response response) throws IOException {
//                final String yourResponse = response.body().string();
//
                Log.e(TAG, "onResponse: "+response );
//
//                if (response.isSuccessful()) {
//                    Log.e(TAG, "run: " + yourResponse);
//
//                } else {
//                    Log.e(TAG, "run: " + yourResponse);
//                }

                Handler refresh = new Handler(Looper.getMainLooper());
                refresh.post(new Runnable() {
                    public void run()
                    {
                        callback(type,response);
                    }
                });

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

    private void callback(String type,Response response)
    {
        Log.e(TAG, "callback: "+type+" "+response);
        if(type=="post")
        {
            if(response.code() == 200)
            {
                md.dismiss();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{}");
                sendData(body,"get","/users/friend?google_id="+go_id);
            }
            else
            {
                Toast.makeText(AddFriends.this, "Wrong Email Address", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            String data="hello";
            try {
                data = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            viewData(data);
        }
    }

    public void ShowPopup(View v) {
        md.setContentView(R.layout.addfriend);
        md.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageButton close;
        final EditText f_email;
        Button add;
        close=(ImageButton)md.findViewById(R.id.imageButton);
        add=(Button)md.findViewById(R.id.button2);
        f_email=(EditText)md.findViewById(R.id.editText);
        error_mess = md.findViewById(R.id.error);
        error_mess.setText("");
        md.show();
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String regex = "^[a-z0-9](\\.?[a-z0-9]){5,}@g(oogle)?mail\\.com$";
                String s=f_email.getText().toString();
                if(s.equals(""))
                {
                    f_email.setText("");
                    Toast.makeText(AddFriends.this, "Please Enter a Google email", Toast.LENGTH_SHORT).show();
                }
                else if(!s.matches(regex))
                {
                    f_email.setText("");
                    Toast.makeText(AddFriends.this, "Please Enter a Google email", Toast.LENGTH_SHORT).show();
                }
                else
                {


                    SQLiteDatabase wc=openOrCreateDatabase("am",MODE_PRIVATE,null);
                    wc.execSQL("create table if not exists user(name varchar,email varchar,id varchar)");

                    Cursor cursor = wc.rawQuery("select * from user",null);
                    go_id="";

                    if(cursor.moveToFirst())
                    {
                        go_id= cursor.getString(2);
                    }

                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, "{\"friend_email\":\"" + s + "\"}");
                    sendData(body,"post","/users/friend?google_id="+go_id);
//                    Toast.makeText(AddFriends.this, "Google id", Toast.LENGTH_SHORT).show();

                }
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                md.dismiss();
            }
        });

    }


    public void viewData(String data) {

        Log.e(TAG, "viewData: "+data);

        User user;

        JSONArray Jarray=null;
        try{
            JSONObject Jobject = new JSONObject(data);
            Log.e(TAG, "viewData: Jobject "+Jobject);
            Jarray = Jobject.getJSONArray("user_friends");


            Log.e(TAG, "viewData: "+Jarray);
            for (int i = 0; i < Jarray.length(); i++) {
                JSONObject object     = Jarray.getJSONObject(i);
                user = new User(object.getString("name"),object.getString("email"),"","","","","");
                listItem.add(user);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

//        user = new User("Rakesh","rk@gmail.com","","","","","");
//        listItem.add(user);
//        user = new User("Ssaf","Ssaf@gmail.com","","","","","");
//        listItem.add(user);
//        user = new User("Simea","Simea@gmail.com","","","","","");
//        listItem.add(user);
//        user = new User("Simea","Simea@gmail.com","","","","","");
//        listItem.add(user);
//        user = new User("Simea","Simea@gmail.com","","","","","");
//        listItem.add(user);
//        user = new User("Simea","Simea@gmail.com","","","","","");
//        listItem.add(user);


        adapter = new FriendsAdpater(this, R.layout.friend_layout, listItem);
        list.setAdapter(adapter);

    }

}