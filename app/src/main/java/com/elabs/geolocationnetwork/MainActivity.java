package com.elabs.geolocationnetwork;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.elabs.geolocationnetwork.utils.Constants;

public class MainActivity extends AppCompatActivity {
    TextView start, stop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = (TextView)findViewById(R.id.start);
        stop = (TextView)findViewById(R.id.stop);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i  = new Intent(MainActivity.this, LocationDetectingService.class);
                i.setAction(Constants.FOREGROUND_SERVICE_KEY);
                startService(i);

            }
        });
        stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i  = new Intent(MainActivity.this, LocationDetectingService.class);
                i.setAction(Constants.STOP_FOREGROUND_SERVICE_KEY);
              //  i.setClass(MainActivity.this, LocationDetectingService.class);
                stopService(i);
            }
        });

    }

    private void Display(String s){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Display("Destroyed");

    }
}
