package com.ianorourke.fdrflightrecorder.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import com.ianorourke.fdrflightrecorder.Database.FlightDatabaseHelper;
import com.ianorourke.fdrflightrecorder.MainActivity;
import com.ianorourke.fdrflightrecorder.R;
import com.ianorourke.fdrflightrecorder.Sensors.GyroscopeReader;
import com.ianorourke.fdrflightrecorder.Sensors.UDPReader;
import com.ianorourke.fdrflightrecorder.Sensors.UDPVals;
import com.ianorourke.fdrflightrecorder.Sound.SoundStart;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GyroscopeReader.GyroscopeReaderInterface, UDPReader.UDPReaderInterface, SoundStart.SoundStartInterface {
    private static boolean DEBUG = false;

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
    private UDPReader udpReader;

    private boolean isStarted = false;

    private SoundStart soundStart;
    private int MIN_AMPLITUDE = 15000;
    private int NUM_HOLD_SECONDS = 5;

    private FlightDataLog flightLog;
    private FlightDataEvent flightEvent;

    private Timer updateTimer;
    private int timeInterval = 1000;

    private long startTime = 0;

    private final int NOTIFICATION_ID = 101;
    private final float METERS_TO_FEET = 3.28f;
    private final float METERS_SECONDS_TO_KNOTS = 1.94384f;

    private boolean soundStartEnabled;
    private boolean soundStopEnabled;

    private FlightDatabaseHelper databaseHelper;

    private boolean hasNewValue = true;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!DEBUG) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(timeInterval / 2);

            gyroscopeReader = new GyroscopeReader(this);
            gyroscopeReader.setInterface(this);
        } else {
            udpReader = new UDPReader(this);
            udpReader.setInterface(this);
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        soundStartEnabled = intent.getBooleanExtra(getString(R.string.service_soundstart), false);
        soundStopEnabled = intent.getBooleanExtra(getString(R.string.service_soundstop), false);

        String pilot = intent.getStringExtra(getString(R.string.service_pilot_name));
        if (pilot == null) pilot = "";

        String plane_type = intent.getStringExtra(getString(R.string.service_plane_type));
        String plane_tail = intent.getStringExtra(getString(R.string.service_plane_tail));

        soundStart = new SoundStart(MIN_AMPLITUDE, NUM_HOLD_SECONDS, this);

        if (!DEBUG) {
            // Checking if Null
            if (mGoogleApiClient == null) Log.v("FDR", "Google API Client Null");
            if (locationRequest == null) Log.v("FDR", "Location Request Null");

            if (mGoogleApiClient == null || locationRequest == null) {
                stopSelf();
                return START_NOT_STICKY;
            } else {
                mGoogleApiClient.connect();
            }
        }

        // Wakelock
        PowerManager mgr = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.service_wakelock));
        wakeLock.acquire();

        // Notification
        Intent viewIntent = new Intent(this, MainActivity.class);
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

        databaseHelper = FlightDatabaseHelper.getInstance(getApplicationContext());
        databaseHelper.markAllFlightsCompleted();

        flightLog = new FlightDataLog(pilot, plane_type, plane_tail, "29.92", "14", zuluTime);

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

        databaseHelper.addFlight(flightLog, true);

        isStarted = true;

        startTime = 0;

        if (!DEBUG) {
            gyroscopeReader.resetCalibration();
            gyroscopeReader.setEnabled(true, true);
        } else {
            udpReader.start();
        }

        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentLoc == null) return;
                if (startTime == 0) startTime = System.currentTimeMillis();

                double seconds = (System.currentTimeMillis() - startTime) / 1000.0;

                flightEvent.setSeconds(seconds);

                if (hasNewValue) {
                    flightLog.addFlightDataEvent(flightEvent);
                    databaseHelper.addEventToFlight(flightLog.getName(), flightEvent);

                    Intent intent = new Intent();
                    intent.setAction(getString(R.string.map_intent));
                    intent.putExtra(getString(R.string.map_lat), currentLoc.latitude);
                    intent.putExtra(getString(R.string.map_lon), currentLoc.longitude);
                    sendBroadcast(intent);

                    hasNewValue = false;
                }
            }
        }, 0, timeInterval);
    }

    @Override
    public void receivedUDPValues(UDPVals vals) {

        flightEvent.setRoll(vals.bank);
        flightEvent.setPitch(vals.pitch);

        flightEvent.setLat(vals.lat);
        flightEvent.setLon(vals.lon);
        flightEvent.setAltitude((int) (vals.alt * METERS_TO_FEET));
        flightEvent.setHeading((int) vals.heading);
        flightEvent.setGroundSpeed((int) (vals.airspeed * METERS_SECONDS_TO_KNOTS));

        notificationBuilder.setContentText("Connected to " + vals.title);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        hasNewValue = true;
    }

    @Override
    public void receivedGyroValues(float x, float y) {
        flightEvent.setRoll(x);
        flightEvent.setPitch(y);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.v("FDR", "Null Location");
            return;
        }

        // TODO: Why creating currentLoc and then just calling it?
        currentLoc = new LatLng(location.getLatitude(), location.getLongitude());

        flightEvent.setLat(currentLoc.latitude);
        flightEvent.setLon(currentLoc.longitude);
        flightEvent.setAltitude((int) (location.getAltitude() * METERS_TO_FEET));
        flightEvent.setHeading((int) location.getBearing());
        flightEvent.setGroundSpeed((int) (location.getSpeed() * METERS_SECONDS_TO_KNOTS));

        float accuracy = location.getAccuracy() * METERS_TO_FEET;

        if (soundStartEnabled && !isStarted) {
            notificationBuilder.setContentText("Waiting for Sound Start...");
        } else {
            notificationBuilder.setContentText("Accuracy: " + ((int) accuracy) + " Feet");
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        hasNewValue = true;
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (locationRequest != null && !DEBUG)
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed: " + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        wakeLock.release();
        wakeLock = null;

        if (!DEBUG) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();
            mGoogleApiClient = null;

            gyroscopeReader.setEnabled(false);
        } else {
            udpReader.stop();
        }

        if (updateTimer != null) updateTimer.cancel();
        updateTimer = null;

        Log.v("FDR", "Service Destroyed");

        running = false;
        isStarted = false;

        soundStart.cancelAll();
        soundStart = null;

        databaseHelper.markAllFlightsCompleted();

        //Final Closeout
        notificationManager.cancelAll();
        if (serviceInterface != null) serviceInterface.backgroundServiceChanged();
        super.onDestroy();
    }
}