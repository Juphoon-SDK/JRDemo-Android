package com.juphoon.rcs.jrdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import common.CommonValue;
import common.model.RealmCallLog;
import de.hdodenhof.circleimageview.CircleImageView;
import ui.view.LogEntryCardView;

/**
 * Created by Upon on 2018/4/25.
 */

public class JRInfoActivity extends AppCompatActivity {
    private LogEntryCardView mLogEntryCardView;
    private ArrayList<RealmCallLog> mCallLogs;
    private CardView mVideoCall, mVoiceCall, mMultiCall;
    private CircleImageView mIcon;
    private TextView mName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        handleIntent();
        initViews();
        initData();
    }

    private void handleIntent() {
        mCallLogs = getIntent().getParcelableArrayListExtra(CommonValue.EXTRA_LOGS);

    }

    private void initViews() {
        mLogEntryCardView = (LogEntryCardView) findViewById(R.id.log_entry_card_view);
        mVideoCall = (CardView) findViewById(R.id.video_button);
        mVoiceCall = (CardView) findViewById(R.id.voice_button);
        mMultiCall = (CardView) findViewById(R.id.multi_button);
        mIcon = (CircleImageView) findViewById(R.id.photo);
        mName = (TextView) findViewById(R.id.name);
    }

    private void initData() {
        if (mCallLogs == null) {
            mLogEntryCardView.setVisibility(View.GONE);
        } else {
            mLogEntryCardView.initLogEntry(0, mCallLogs, mCallLogs.get(0).getPerrNumber(), this);
            mName.setText(mCallLogs.get(0).getPerrNumber());
        }
    }

    public void onCallBack(View view) {
        //TODO 回拨电话(列表点击事件)
        LogEntryCardView.LogEntry entry = (LogEntryCardView.LogEntry) view.getTag();
        if (entry == null) {
            return;
        }
        Toast.makeText(this, "ddd", Toast.LENGTH_SHORT).show();
    }

    public void onMultiCall(View view) {
        Intent intent = new Intent(JRInfoActivity.this, JRCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(CommonValue.JRCALL_EXTRA_PHONE_NUMBER, mCallLogs.get(0).getPerrNumber());
        intent.putExtra(CommonValue.JRCALL_EXTRA_IS_MULTI, true);
        intent.putExtra(CommonValue.JRCALL_EXTRA_IS_VIDEO, false);
        startActivity(intent);
    }

    public void onVoiceCall(View view) {
        Intent intent = new Intent(JRInfoActivity.this, JRCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(CommonValue.JRCALL_EXTRA_PHONE_NUMBER, mCallLogs.get(0).getPerrNumber());
        intent.putExtra(CommonValue.JRCALL_EXTRA_IS_MULTI, false);
        intent.putExtra(CommonValue.JRCALL_EXTRA_IS_VIDEO, false);
        startActivity(intent);
    }

    public void onVideoCall(View view) {
        Intent intent = new Intent(JRInfoActivity.this, JRCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(CommonValue.JRCALL_EXTRA_PHONE_NUMBER, mCallLogs.get(0).getPerrNumber());
        intent.putExtra(CommonValue.JRCALL_EXTRA_IS_MULTI, false);
        intent.putExtra(CommonValue.JRCALL_EXTRA_IS_VIDEO, true);
        startActivity(intent);
    }
}
