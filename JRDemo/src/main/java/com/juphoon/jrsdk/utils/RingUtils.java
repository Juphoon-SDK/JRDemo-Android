package com.juphoon.jrsdk.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import java.io.File;
import java.io.IOException;

public class RingUtils {

    public static boolean startRing(Context c, String filePath) {

        AudioManager audioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_RING, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        int mode = audioManager.getRingerMode();
        switch (mode) {
            case AudioManager.RINGER_MODE_SILENT:
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                if (sVibrator == null)
                    sVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                sVibrator.vibrate(VIBRATE_PATTERN, 0);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                if (sVibrator == null)
                    sVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                try {
                    int value = Settings.System.getInt(c.getContentResolver(), "vibrate_when_ringing");
                    if (value == 1) {
                        sVibrator.vibrate(VIBRATE_PATTERN, 0);
                    }
                } catch (SettingNotFoundException e) {
                    sVibrator.vibrate(VIBRATE_PATTERN, 0);
                }

                if (sMediaPlayer == null) {
                    sMediaPlayer = new MediaPlayer();
                } else if (sMediaPlayer.isPlaying()) {
                    sMediaPlayer.stop();
                    sMediaPlayer.reset();
                }
                sMediaPlayer.setLooping(true);
                sMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);

                try {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        return false;
                    }
                    sMediaPlayer.setDataSource(filePath);
                    sMediaPlayer.prepare();
                    sMediaPlayer.start();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    public static void startRingBack(Context c, String fileName) {
        if (sMediaPlayer == null) {
            sMediaPlayer = new MediaPlayer();
        } else if (sMediaPlayer.isPlaying()) {
            sMediaPlayer.stop();
            sMediaPlayer.reset();
        }
        sMediaPlayer.setLooping(true);
        sMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        AudioManager audioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        try {
            AssetFileDescriptor afd = c.getAssets().openFd(fileName);
            sMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            sMediaPlayer.prepare();
            sMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean startRing(Context c, int resid) {
        AudioManager audioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_RING, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        int mode = audioManager.getRingerMode();
        switch (mode) {
            case AudioManager.RINGER_MODE_SILENT:
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                if (sVibrator == null)
                    sVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                sVibrator.vibrate(VIBRATE_PATTERN, 0);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                if (sVibrator == null)
                    sVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                try {
                    int value = Settings.System.getInt(c.getContentResolver(), "vibrate_when_ringing");
                    if (value == 1) {
                        sVibrator.vibrate(VIBRATE_PATTERN, 0);
                    }
                } catch (SettingNotFoundException e) {
                    sVibrator.vibrate(VIBRATE_PATTERN, 0);
                }

                if (sMediaPlayer == null) {
                    sMediaPlayer = new MediaPlayer();
                } else if (sMediaPlayer.isPlaying()) {
                    sMediaPlayer.stop();
                    sMediaPlayer.reset();
                }
                sMediaPlayer.setLooping(true);
                sMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);

                try {
                    AssetFileDescriptor afd = c.getResources().openRawResourceFd(resid);
                    if (afd == null) {
                        return false;
                    }
                    sMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                    sMediaPlayer.prepare();
                    sMediaPlayer.start();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    return false;
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                break;
        }
        return true;
    }

    public static void playAudio(Context c, int resid, boolean isLoop) {
        if (sMediaPlayer == null) {
            sMediaPlayer = new MediaPlayer();
        } else if (sMediaPlayer.isPlaying()) {
            sMediaPlayer.stop();
        }
        sMediaPlayer.reset();
        sMediaPlayer.setLooping(isLoop);
        sMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        AudioManager audioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        try {
            AssetFileDescriptor afd = c.getResources().openRawResourceFd(resid);
            sMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            sMediaPlayer.prepare();
            sMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (sMediaPlayer != null) {
            sMediaPlayer.stop();
            sMediaPlayer.reset();
            sMediaPlayer.release();
            sMediaPlayer = null;
        }
        if (sVibrator != null) {
            sVibrator.cancel();
            sVibrator = null;
        }
    }

    static MediaPlayer sMediaPlayer;
    static Vibrator sVibrator;
    private static final long VIBRATE_PATTERN[] = new long[]{1000, 1000};
}
