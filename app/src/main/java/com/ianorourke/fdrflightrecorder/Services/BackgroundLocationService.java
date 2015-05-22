package com.ianorourke.fdrflightrecorder.Services;

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
import com.ianorourke.fdrflightrecorder.FlightData.FlightDataEvent;
import com.ianorourke.fdrflightrecorder.FlightData.FlightDataLog;
import com.ianorourke.fdrflightrecorder.MapActivity;
import com.ianorourke.fdrflightrecorder.FlightFormatters.FDRFormatter;
import com.ianorourke.fdrflightrecorder.R;
import com.ianorourke.fdrflightrecorder.Sensors.GyroscopeReader;
import com.ianorourke.fdrflightrecorder.Sound.SoundStart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GyroscopeReader.GyroscopeReaderInterface, SoundStart.SoundStartInterface {
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

    public interface BackgroundLocationServiceInterface {
        void backgroundServiceChanged();
    }

    public static BackgroundLocationServiceInterface serviceInterface;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;

    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;

    private LatLng currentLoc;

    private GyroscopeReader gyroscopeReader;

    private boolean isStarted = false;

    private SoundStart soundStart;
    private int MIN_AMPLITUDE = 15000;
    private int NUM_HOLD_SECONDS = 5;

    private FlightDataLog flightLog;
    private FlightDataEvent flightEvent;

    private Timer updateTimer;
    private int timeInterval = 500;

    private long startTime = 0;

    private final int NOTIFICATION_ID = 101;
    private final float METERS_TO_FEET = 3.28f;

    private boolean soundStartEnabled;
    private boolean soundStopEnabled;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        soundStartEnabled = intent.getBooleanExtra(getString(R.string.service_soundstart), false);
        soundStopEnabled = intent.getBooleanExtra(getString(R.string.service_soundstop), false);

        soundStart = new SoundStart(MIN_AMPLITUDE, NUM_HOLD_SECONDS, this);

        // Checking if Null
        if (mGoogleApiClient == null) Log.v("FDR", "Google API Client Null");
        if (locationRequest == null) Log.v("FDR", "Location Request Null");

        if (mGoogleApiClient == null || locationRequest == null) {
            stopSelf();
            return START_NOT_STICKY;
        } else {
            mGoogleApiClient.connect();
        }

        // Wakelock
        PowerManager mgr = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.service_wakelock));
        wakeLock.acquire();

        // Notification
        Intent viewIntent = new Intent(this, MapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);

        notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder.setContentTitle(getText(R.string.app_name));
        notificationBuilder.setContentText("Location Service Connecting...");
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        // Creating File and Log
        Calendar zuluTime = GregorianCalendar.getInstance();

        flightLog = new FlightDataLog(null, null, null, null, null, zuluTime);
        flightEvent = new FlightDataEvent();

        flightEvent.setSeconds(0);

        updateTimer = new Timer(false);

        Log.v("FDR", "Service Started");
        running = true;

        if (soundStartEnabled)
            soundStart.triggerSoundStart(soundStopEnabled);
        else
            startLog();

        if (serviceInterface != null) serviceInterface.backgroundServiceChanged();

        return START_STICKY;
    }

    public void onSoundStartSuccess() {
        startLog();
    }

    public void onSoundStopSuccess() {
        stopSelf();
    }

    private void startLog() {
        Log.v("FDR", "Log Started");

        isStarted = true;

        startTime = 0;

        gyroscopeReader.resetCalibration();
        gyroscopeReader.setEnabled(true, true);

        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentLoc == null) return;
                if (startTime == 0) startTime = System.currentTimeMillis();

                double seconds = (System.currentTimeMillis() - startTime) / 1000.0;

                flightEvent.setSeconds(seconds);
                flightLog.addFlightDataEvent(flightEvent);

                Intent intent = new Intent();
                intent.setAction(getString(R.string.map_intent));
                intent.putExtra(getString(R.string.map_lat), currentLoc.latitude);
                intent.putExtra(getString(R.string.map_lon), currentLoc.longitude);
                sendBroadcast(intent);
            }
        }, 0, timeInterval);
    }

    @Override
    public void receivedGyroValues(float x, float y) {
        flightEvent.setRoll(x);
        flightEvent.setPitch(y);

        //Log.v("FDR", "Roll: " + x + ", Pitch: " + y);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.v("FDR", "Null Location");
            return;
        }
        currentLoc = new LatLng(location.getLatitude(), location.getLongitude());

        flightEvent.setLat(currentLoc.latitude);
        flightEvent.setLon(currentLoc.longitude);
        flightEvent.setAltitude((int) (location.getAltitude() * METERS_TO_FEET));
        flightEvent.setHeading((int) location.getBearing());

        float accuracy = location.getAccuracy() * METERS_TO_FEET;

        if (soundStartEnabled && !isStarted) {
            notificationBuilder.setContentText("Waiting for Sound Start...");
        } else {
            notificationBuilder.setContentText("Accuracy: " + ((int) accuracy) + " Feet");
        }
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
            Toast.makeText(this, "Connection Suspended: " + i, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed: " + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }

    private File getFile(String filename) {
        File folderFile = new File(Environment.getExternalStorageDirectory(), getString(R.string.save_folder));
        if (!folderFile.exists()) folderFile.mkdir();

        File file = new File(folderFile, filename);

        boolean success = false;

        try {
            if (!file.exists()) success = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (success) ? file : null;
    }

    private void saveLog() {
        File saveFile = getFile(flightLog.getFilename() + FDRFormatter.FILE_EXT);

        try {
            FileWriter fileWriter = new FileWriter(saveFile);

            fileWriter.write(FDRFormatter.formatLog(flightLog));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        wakeLock.release();
        wakeLock = null;

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
        mGoogleApiClient = null;

        if (updateTimer != null) updateTimer.cancel();
        updateTimer = null;

        notificationManager.cancelAll();

        gyroscopeReader.setEnabled(false);

        Log.v("FDR", "Service Destroyed");

        running = false;
        isStarted = false;

        soundStart.cancelAll();

        if (serviceInterface != null) serviceInterface.backgroundServiceChanged();

        saveLog();
        Log.v("FDR", "File Saved");

        super.onDestroy();

        stopSelf();
    }
}