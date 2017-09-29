package com.elabs.geolocationnetwork;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.elabs.geolocationnetwork.utils.Constants;
import com.elabs.geolocationnetwork.utils.LocationManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.stats.WakeLockEvent;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tanmay on 28-09-2017.
 */
//Handler h;

public class LocationDetectingService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Handler h;
    int i;
    GoogleApiClient googleApiClient;
    PowerManager.WakeLock wakeLock;
    PowerManager powerManager;
    LocationRequest locationServices;
    DatabaseReference firebase;
    Location lastLocation = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        h = new Handler();
        createApiClient();
       if(intent!=null){
           if (intent.getAction().equals(Constants.FOREGROUND_SERVICE_KEY)) {
               Display("called");
               startForeground(Constants.NOTIFICATION_ID_START_GEOLOCATION, getCompatNotification(Constants.NOTIFICATION_ID_START_GEOLOCATION));
               startForegroundService(Constants.NOTIFICATION_ID_START_GEOLOCATION, getCompatNotification(Constants.NOTIFICATION_ID_START_GEOLOCATION));
           } else if (intent.getAction().equals(Constants.STOP_FOREGROUND_SERVICE_KEY)) {
               stopSelf();
               googleApiClient.disconnect();
               if(powerManager!=null){
                   if(wakeLock.isHeld()){
                       wakeLock.release();
                   }
               }
               stopForeground(true);

           }
       }



        return START_STICKY;
    }



    private void createApiClient() {
        if (googleApiClient == null)
            googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    private void startForegroundService(int notificationIdStartGeolocation, Notification compatNotification) {
        compatNotification.flags = Notification.FLAG_NO_CLEAR;
        //NotificationManager notificationManagere = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManagere.notify(notificationIdStartGeolocation, compatNotification);
        i = 1;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if(lastLocation!=null)
                        Log.i("23235616", "I ran @ " + i +" @ location "+lastLocation.getLatitude()+","+lastLocation.getLongitude());
                    else
                        Log.i("23235616", "I ran @ " + i );
                    i++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Display(e.toString());
                    }
                }
            }
        });

        t.start();

    }

    private void Display(final String s) {
        Log.i("23235616", s);
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LocationDetectingService.this, s, Toast.LENGTH_LONG).show();
            }
        });

    }

    private Notification getCompatNotification(int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent i = new Intent(this, MainActivity.class);

        Intent nextIntent = new Intent(this, LocationDetectingService.class);
        nextIntent.setAction(Constants.STOP_FOREGROUND_SERVICE_KEY);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);
        builder.setContentTitle("Foreground Service");
        builder.setSmallIcon(R.mipmap.ic_launcher).setTicker("Detecting Location").setAutoCancel(false)
                .setWhen(System.currentTimeMillis());
        Intent startIntent = new Intent(getApplicationContext(), LocationDetectingService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, startIntent,PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true).addAction(android.R.drawable.star_big_off,"stop",pendingIntent);
        Notification n =  builder.build();
        n.flags = Notification.FLAG_NO_CLEAR|Notification.FLAG_FOREGROUND_SERVICE;
        return n;

    }

    @Override
    public void onConnected(Bundle bundle) {
        Initialise();
        Display("Api Client Connected");
        locationServices = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(6000).setFastestInterval(4000);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
            Display("Please provide the permission");
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationServices, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

        Display("Api Client Disconnected");

    }

    private void Initialise(){
        firebase = FirebaseDatabase.getInstance().getReference();
        powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"My Wake Lock");
        if(!wakeLock.isHeld())
            wakeLock.acquire();
    }
@Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Display("Api Client Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {

        if(lastLocation==null){
            lastLocation = location;
        }else if(location.getLatitude()==lastLocation.getLatitude()&&location.getLongitude()==lastLocation.getLongitude()){
          //  return ;
        }
       // Display("He is at "+location.getLongitude()+", "+location.getLatitude()+" when "+getTime(System.currentTimeMillis()));
        LocationManager locationManager = new LocationManager(location.getLatitude(), location.getLongitude(),"asd","asd","asd",getTime(System.currentTimeMillis()));
        firebase.setValue(locationManager);

    }

    private String getTime(long millis){
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();


        int currentHour = cal.get(Calendar.HOUR);
        int currentMinutes = cal.get(Calendar.MINUTE);
        int currentSeconds = cal.get(Calendar.SECOND);

        String t = currentHour+":"+currentMinutes+":"+currentSeconds+":"+currentLocalTime.getDate()+currentLocalTime.getMonth();
        return t;
    }
}
