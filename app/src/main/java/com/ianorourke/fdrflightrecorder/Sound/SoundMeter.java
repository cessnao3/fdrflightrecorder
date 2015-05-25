package com.ianorourke.fdrflightrecorder.Sound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

//http://stackoverflow.com/questions/4777060/android-sample-microphone-without-recording-to-get-live-amplitude-level

public class SoundMeter {

    private AudioRecord ar = null;
    private int minSize;

    //TODO: REMOVE
    File file;
    FileWriter fileWriter;

    Timer timer;

    public void start() {
        minSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,minSize);
        ar.startRecording();

        file = new File(Environment.getExternalStorageDirectory(), "sound_log.log");

        try {
            if (!file.exists()) file.createNewFile();
            fileWriter = new FileWriter(file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (fileWriter == null) return;

                double amplitude = getAmplitude();

                try {
                    fileWriter.write(amplitude + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
    }

    public void stop() {
        if (ar != null) {
            ar.stop();
        }

        if (fileWriter == null) return;
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer.cancel();
    }

    public double getAmplitude() {
        short[] buffer = new short[minSize];
        ar.read(buffer, 0, minSize);
        int max = 0;
        for (short s : buffer)
        {
            if (Math.abs(s) > max)
            {
                max = Math.abs(s);
            }
        }
        return max;
    }

}