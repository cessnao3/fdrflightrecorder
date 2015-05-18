package com.ianorourke.fdrflightrecorder.Sound;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class SoundStart {
    public interface SoundStartInterface {
        void onSoundStartSuccess();
        void onSoundStopSuccess();
    }

    private SoundMeter soundMeter = new SoundMeter();
    private SoundStartInterface soundStartInterface;

    private long startTime = 0;

    private final int MIN_AMPLITUDE;
    private final int MIN_DURATION_SECONDS;

    private Timer soundTimer;
    private TimerTask soundTimerTask;

    private Timer stopTimer;
    private TimerTask stopTimerTask;

    private boolean isSoundStopping = false;

    public SoundStart(int min_amplitude, int duration_seconds, @NonNull SoundStartInterface startInterface) {
        MIN_AMPLITUDE = min_amplitude;
        MIN_DURATION_SECONDS = duration_seconds;
        soundStartInterface = startInterface;
    }

    public void triggerSoundStart() {
        triggerSoundStart(false);
    }

    public void triggerSoundStart(final boolean soundStop) {
        final int msRepeatDelay = 500;

        soundMeter.start();
        startTime = 0;

        soundTimer = new Timer(true);

        if (soundStop) {
            stopTimer = new Timer(true);
            stopTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (startTime == 0)
                        startTime = System.currentTimeMillis();

                    if (soundMeter.getAmplitude() > MIN_AMPLITUDE) startTime = System.currentTimeMillis();

                    float seconds = (System.currentTimeMillis() - startTime) / 1000.0f;

                    Log.v("FDR", "SoundStop: " + soundMeter.getAmplitude());

                    if (seconds > MIN_DURATION_SECONDS) {
                        startTime = 0;

                        stopTimer.cancel();
                        soundStartInterface.onSoundStopSuccess();
                        soundMeter.stop();
                    }
                }
            };
        }

        soundTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (startTime == 0)
                    startTime = System.currentTimeMillis();

                if (soundMeter.getAmplitude() < MIN_AMPLITUDE) startTime = System.currentTimeMillis();

                float seconds = (System.currentTimeMillis() - startTime) / 1000.0f;

                Log.v("FDR", "SoundStart: " + soundMeter.getAmplitude());

                if (seconds > MIN_DURATION_SECONDS) {
                    startTime = 0;

                    soundTimer.cancel();
                    soundStartInterface.onSoundStartSuccess();

                    if (soundStop) {
                        stopTimer.schedule(stopTimerTask, 0, msRepeatDelay);
                    } else {
                        soundMeter.stop();
                    }
                }
            }
        };

        soundTimer.schedule(soundTimerTask, 0, msRepeatDelay);
    }

    public void cancelAll() {
        if (soundTimer != null) {
            soundTimer.cancel();
            soundTimer = null;
        }

        if (stopTimer != null) {
            stopTimer.cancel();
            stopTimer = null;
        }

        soundMeter.stop();
    }
}
