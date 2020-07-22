package com.avnish.wecare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class SHOW_DEATILS extends AppCompatActivity {

    String latitude;
    String longitude;
    String name;
    String distance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show__deatils);
        Bundle bundle = getIntent().getExtras();
        latitude = bundle.getString("latitude");
        longitude = bundle.getString("longitude");
        distance = bundle.getString("distance");
        name = bundle.getString("name");

        Log.e("SHOW_DETAILS", "onCreate: "+latitude+" "+longitude+" "+name);

        TextView nameView = findViewById(R.id.name);
        nameView.setText(name);


        TextView distanceView = findViewById(R.id.distance);
        distanceView.setText(distance+"km");

        Button map = findViewById(R.id.button2);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String geoUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + name + ")";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                startActivity(intent);
            }
        });
    }
}
