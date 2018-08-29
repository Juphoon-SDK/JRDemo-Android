package com.juphoon.jrsdk.utils;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.view.View;

import com.juphoon.jrsdk.model.RealmMessage;

import java.io.IOException;

public class AudioUtils {

    public static final int MIN_AUDIO_DURATION = 1000;

    public interface AudioPlayerListener {
        void onStartPlay();

        void onStopPlay();

        void setData(RealmMessage message, View view);
    }

    private AudioPlayerListener mLastListener;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;

    private static AudioUtils sInstance;

    private AudioUtils() {
    }

    public static AudioUtils getInstance() {
        if (sInstance == null) {
            sInstance = new AudioUtils();
        }
        return sInstance;
    }

    public void startRecord(String filename) {
        if (mRecorder != null) {
            stopRecord();
        }
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(filename);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (RuntimeException e) {
            mRecorder = null;
            e.printStackTrace();
        }
    }

    public void startPlay(String filePath, final AudioPlayerListener listener) {
        if (mPlayer != null) {
            stopPlay();
        }
        try {
            listener.onStartPlay();
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(filePath);
            mPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                    listener.onStopPlay();
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            stopPlay();
            e.printStackTrace();
        }

        mLastListener = listener;
    }

    public void startPlay(String filePath) {
        if (mPlayer != null) {
            stopPlay();
        }
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(filePath);
            mPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            stopPlay();
            e.printStackTrace();
        }
    }

    public void stopPlay() {
        if (mLastListener != null)
            mLastListener.onStopPlay();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public int getAudioDuration(String path) {
        mPlayer = new MediaPlayer();
        mPlayer.reset();
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mPlayer.getDuration();
    }

    public static String getAudioDuration(int millionSeconds) {
        int seconds = millionSeconds / 1000;
        if (seconds == 0)
            return "1\"";
        if (seconds > 60 * 60) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (seconds >= 60) {
            int minute = seconds / 60;
            sb.append(minute).append("' ");
        }
        sb.append(seconds % 60).append("\"");
        return sb.toString();
    }

    public int getAudioDuring(String path) {
        mPlayer = new MediaPlayer();
        mPlayer.reset();
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mPlayer.getDuration();
    }
}
