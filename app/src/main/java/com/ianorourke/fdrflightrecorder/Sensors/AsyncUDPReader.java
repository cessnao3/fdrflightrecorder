package com.ianorourke.fdrflightrecorder.Sensors;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class AsyncUDPReader extends AsyncTask<Void, String, String> {
    private final int PORT = 17271;
    private boolean should_run = true;

    interface AsyncUDPReaderInterface {
        void receviedDatagram(String data);
    }

    AsyncUDPReaderInterface asyncInterface = null;

    public AsyncUDPReader(AsyncUDPReaderInterface i) {
        this.asyncInterface = i;
    }

    @Override
    protected String doInBackground(Void... params) {
        Log.v("FDR", "Started Task");

        DatagramSocket clientSocket = null;

        try {
            clientSocket = new DatagramSocket(PORT);
            clientSocket.setSoTimeout(10000);
        } catch (SocketException e) {
            Log.v("FDR", "UDP Socket Exception", e);
            return null;
        }

        byte[] receivedata = new byte[1024];

        while (!isCancelled()) {
            DatagramPacket recv_packet = new DatagramPacket(receivedata, receivedata.length);

            try {
                clientSocket.receive(recv_packet);
            } catch (SocketTimeoutException e) {
                Log.v("FDR", "Socket Timed Out", e);
            } catch (IOException e) {
                Log.v("FDR", "IO Exec UDP", e);
                continue;
            }

            String rec_str = new String(recv_packet.getData());
            rec_str = rec_str.trim();
            publishProgress(rec_str);
        }

        clientSocket.close();
        clientSocket = null;

        return null;
    }

    protected void onProgressUpdate(String... progress) {
        if (asyncInterface != null) asyncInterface.receviedDatagram(progress[0]);
    }

    protected void onPostExecute(String response) {
        Log.v("FDR", "Ended Task");
    }
}
