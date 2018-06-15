package com.juphoon.rcs.jrdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

public class VideoActivity extends Activity {

    public static final String VIDEO_PATH = "video_path";

    public static void startWithPath(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath))
            return;
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra(VIDEO_PATH, filePath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String filePath = getIntent().getStringExtra(VIDEO_PATH);
        VideoView videoView = new VideoView(this);
        videoView.setVideoURI(Uri.parse(filePath));
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.start();
        videoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        RelativeLayout rl = new RelativeLayout(this);
        rl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        rl.setGravity(Gravity.CENTER);
        rl.addView(videoView);
        setContentView(rl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
