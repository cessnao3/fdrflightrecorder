package com.ianorourke.fdrflightrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.ianorourke.fdrflightrecorder.FDR.FDRFormatter;
import com.ianorourke.fdrflightrecorder.FDR.FDRLog;
import com.ianorourke.fdrflightrecorder.Sensors.GyroscopeReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ian on 5/8/15.
 */
public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GyroscopeReader.GyroscopeReaderInterface {
    private static boolean running = false;

    public static boolean isRunning() {
        return running;
    }

    IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    PowerManager.WakeLock wakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;

    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;

    private LatLng currentLoc;

    private GyroscopeReader gyroscopeReader;

    private FDRLog fdrLog;
    private FDRFormatter fdrFormatter;

    private Timer updateTimer;
    private int timeInterval = 1000;

    private double altitude = 0.0;
    private float accuracy = 0.0f;
    private int heading = 0;
    private float speed = 0.0f;

    private float roll = 0.0f;
    private float pitch = 0.0f;

    private long startTime;

    private final int NOTIFICATION_ID = 101;
    private final float METERS_TO_FEET = 3.28f;
    private final float MS_TO_KNOTS = 1.94384f;

    private String filename;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(timeInterval / 2);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        gyroscopeReader = new GyroscopeReader(this);
        gyroscopeReader.setInterface(this);

        Calendar c = GregorianCalendar.getInstance();
        filename = (new SimpleDateFormat("MM-dd-yyyy HH-mm-ss")).format(c.getTime()) + getString(R.string.file_ext);

        fdrLog = new FDRLog(c, null, null, null, null);

        fdrFormatter = new FDRFormatter();
        fdrFormatter.setSeconds(0);

        updateTimer = new Timer(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        PowerManager mgr = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.service_wakelock));
        wakeLock.acquire();

        if (mGoogleApiClient == null) Log.v("FDR", "Google API Client Null");
        if (locationRequest == null) Log.v("FDR", "Location Request Null");

        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);

        notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder.setContentTitle(getText(R.string.app_name));
        notificationBuilder.setContentText("Location Service Connecting...");
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        mGoogleApiClient.connect();

        gyroscopeReader.setEnabled(true);

        startTime = System.currentTimeMillis();

        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentLoc == null) return;

                fdrFormatter.setLat(currentLoc.latitude);
                fdrFormatter.setLon(currentLoc.longitude);
                fdrFormatter.setAltitude((int) altitude);
                fdrFormatter.setHeading(heading);
                fdrFormatter.setAirspeed(speed);

                fdrFormatter.setRoll(roll);
                fdrFormatter.setPitch(pitch);

                fdrFormatter.setSeconds((int) (System.currentTimeMillis() - startTime) / 1000);

                fdrLog.appendData(fdrFormatter);

                Intent intent = new Intent();
                intent.setAction(getString(R.string.map_intent));
                intent.putExtra(getString(R.string.map_lat), currentLoc.latitude);
                intent.putExtra(getString(R.string.map_lon), currentLoc.longitude);
                sendBroadcast(intent);

                //Log.v("FDR", fdrFormatter.getData());
            }
        }, 0, timeInterval);

        Log.v("FDR", "Service Started");
        running = true;

        return START_STICKY;
    }

    @Override
    public void receivedValues(float x, float y) {
        roll = x;
        pitch = y;

        //Log.v("FDR", "Roll: " + x + ", Pitch: " + y);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.v("FDR", "Null Location");
            return;
        }
        currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
        altitude = location.getAltitude() * METERS_TO_FEET;
        accuracy = location.getAccuracy() * METERS_TO_FEET;
        heading = (int) location.getBearing();
        speed = location.getSpeed() * MS_TO_KNOTS;

        notificationBuilder.setContentText("Accuracy: " + ((int) accuracy) + " Feet");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        if (locationRequest != null)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Toast.makeText(this, "Connection Suspended: " + i, Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed: " + connectionResult.getErrorCode(), Toast.LENGTH_LONG);
    }

    @Override
    public void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        wakeLock.release();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
        mGoogleApiClient = null;

        if (updateTimer != null) updateTimer.cancel();
        updateTimer = null;

        notificationManager.cancelAll();

        Log.v("FDR", "Service Destroyed");

        super.onDestroy();

        running = false;

        try {
            File file = new File(Environment.getExternalStorageDirectory(), filename);
            if (!file.exists()) file.createNewFile();

            FileWriter fileWriter = new FileWriter(file, false);

            fileWriter.write(fdrLog.getLog());

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Log.v("FDR", e.toString());
            e.printStackTrace();
        }

        stopSelf();
    }
}
