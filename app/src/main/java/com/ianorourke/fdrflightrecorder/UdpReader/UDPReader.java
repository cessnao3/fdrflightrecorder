package com.ianorourke.fdrflightrecorder.UdpReader;

import android.util.Log;

/**
 * UDPReader - Received data from UDP socket via C# application from FSX for debugging purposes... Should be removed/disabled before release
 *
 * Created by cessn on 5/26/2016.
 */
public class UDPReader implements AsyncUDPReader.AsyncUDPReaderInterface {
    private AsyncUDPReader asyncReader = null;

    public interface UDPReaderInterface {
        void receivedUDPValues(UDPVals vals);
    }


    public UDPReader() {
        // Do Nothing
    }
    private UDPReaderInterface udpReaderInterface = null;

    public void start() {
        if (asyncReader == null) {
            asyncReader = new AsyncUDPReader(this);
            asyncReader.execute();
        }
    }

    public void stop() {
        if (asyncReader != null) {
            asyncReader.cancel(false);
            asyncReader = null;
        }
    }

    @Override
    public void receviedDatagram(String data) {
        try {
            UDPVals udpVals = new UDPVals(data);
            if (udpReaderInterface != null) udpReaderInterface.receivedUDPValues(udpVals);
        } catch (Exception e) {
            Log.v("FDR", "Error making UDP Data ", e);
        }
    }

    public void setInterface(UDPReaderInterface i) {
        udpReaderInterface = i;
    }
}
