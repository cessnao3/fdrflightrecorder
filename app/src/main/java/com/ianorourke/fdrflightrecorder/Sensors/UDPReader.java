package com.ianorourke.fdrflightrecorder.Sensors;

import android.content.Context;

/**
 * UDPReader - Received data from UDP socket via C# application from FSX for debugging purposes... Should be removed/disabled before release
 *
 * Created by cessn on 5/26/2016.
 */
public class UDPReader {

    public interface UDPReaderInterface {
        void receivedUDPValues(UDPVals vals);
    }

    private UDPReaderInterface udpReaderInterface = null;

    public UDPReader(Context c) {

    }

    public void start() {

    }

    public void stop() {

    }

    public void setInterface(UDPReaderInterface i) {
        udpReaderInterface = i;
    }
}
