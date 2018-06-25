package com.juphoon.rcs.jrdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.juphoon.rcs.utils.JRFileUtils;

import ui.view.TouchScaleImageView;


/**
 * Created by Upon on 2017/8/28.
 */

public class ImageActivity extends AppCompatActivity {
    public static final String EXTRA_IMG_PATH = "img_path";
    private FrameLayout mReplace;

    public static void start(String path, Context context) {
        Intent intent = new Intent(context, ImageActivity.class);
        intent.putExtra(EXTRA_IMG_PATH, path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mReplace = (FrameLayout) findViewById(R.id.replace);
        String path = getIntent().getStringExtra(EXTRA_IMG_PATH);
        TouchScaleImageView img = new TouchScaleImageView(this);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        opts.inSampleSize = JRFileUtils.computeSampleSize(opts.outWidth, opts.outHeight, -1, 1024 * 1024);
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path, opts);
        } catch (OutOfMemoryError err) {
        }
        if (bitmap == null) {
            Toast.makeText(this, "图片已经损坏", Toast.LENGTH_LONG).show();
            return;
        }
        int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
        bitmap.recycle();
        img.setImageBitmap(scaled);
        img.setMaxZoom(4f);
        mReplace.addView(img);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
