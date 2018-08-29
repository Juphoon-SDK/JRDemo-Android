package com.juphoon.jrsdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.juphoon.jrsdk.utils.CommonUtils;


public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        String versionCode = CommonUtils.getVersionCode(this);
//        String version = CommonUtils.getVersionName(this) + "(" + versionCode.substring(4, versionCode.length()) + ")";
        String version = CommonUtils.getVersionName(this) + "(" + versionCode + ")";
        ((TextView) findViewById(R.id.textView_version)).setText(String.format(getString(R.string.about_version_num), version));
    }

    public void onClickAbout(View v) {
        onBackPressed();
    }

}
