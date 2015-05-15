package com.ianorourke.fdrflightrecorder.Sound;

import android.support.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

public class SoundStart {
    public interface SoundStartInterface {
        void onSoundStartSuccess();
    }

    private SoundMeter soundMeter = new SoundMeter();
    private SoundStartInterface soundStartInterface;

    private long startTime = 0;

    private final int MIN_AMPLITUDE;
    private final int MIN_DURATION_SECONDS;

    public SoundStart(int min_amplitude, int duration_seconds, @NonNull SoundStartInterface startInterface) {
        MIN_AMPLITUDE = min_amplitude;
        MIN_DURATION_SECONDS = duration_seconds;
        soundStartInterface = startInterface;
    }

    public void triggerSoundStart() {
        int msRepeatDelay = 500;

        soundMeter.start();

        final Timer soundTimer = new Timer(false);
        soundTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (startTime == 0 || soundMeter.getAmplitude() < MIN_AMPLITUDE)
                    startTime = System.currentTimeMillis();

                float seconds = (System.currentTimeMillis() - startTime) / 1000.0f;

                if (seconds > MIN_DURATION_SECONDS) {
                    soundStartInterface.onSoundStartSuccess();
                    soundTimer.cancel();
                    soundMeter.stop();
                    startTime = 0;
                }
            }
        }, 0, msRepeatDelay);
    }
}
