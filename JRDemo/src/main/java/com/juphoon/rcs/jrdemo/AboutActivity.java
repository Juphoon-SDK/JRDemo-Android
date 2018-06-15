package com.juphoon.rcs.jrdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.juphoon.rcs.utils.JRCommonUtils;


public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        String versionCode = JRCommonUtils.getVersionCode(this);
        String version = JRCommonUtils.getVersionName(this) + "(" + versionCode.substring(4, versionCode.length()) + ")";
        ((TextView) findViewById(R.id.textView_version)).setText(String.format(getString(R.string.about_version_num), version));
    }

    public void onClickAbout(View v) {
        onBackPressed();
    }

}
