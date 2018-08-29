package com.juphoon.jrsdk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.juphoon.cmcc.app.lemon.MtcConf;
import com.juphoon.cmcc.app.lemon.MtcConfCmcc;
import com.juphoon.cmcc.app.lemon.MtcConfCmccConstants;
import com.juphoon.cmcc.app.lemon.MtcConfConstants;
import com.juphoon.cmcc.app.lemon.ST_MTC_RECT;
import com.juphoon.jrsdk.adapter.JRMultiMemberAdapter;
import com.juphoon.jrsdk.call.CallCell;
import com.juphoon.jrsdk.call.OperationLayer;
import com.juphoon.jrsdk.utils.CommonUtils;
import com.juphoon.jrsdk.utils.CommonValue;
import com.juphoon.jrsdk.utils.NumberUtils;
import com.juphoon.jrsdk.utils.RingUtils;
import com.juphoon.jrsdk.view.Statistics;
import com.juphoon.rcs.jrsdk.JRCall;
import com.juphoon.rcs.jrsdk.JRCallCallback;
import com.juphoon.rcs.jrsdk.JRCallConstants;
import com.juphoon.rcs.jrsdk.JRCallItem;
import com.juphoon.rcs.jrsdk.JRCallMember;
import com.juphoon.rcs.jrsdk.JRClient;
import com.juphoon.rcs.jrsdk.JRMediaDevice;
import com.juphoon.rcs.jrsdk.JRMediaDeviceCallback;
import com.juphoon.rcs.jrsdk.JRMediaDeviceConstants;
import com.juphoon.rcs.jrsdk.JRMediaDeviceVideoCanvas;
import com.juphoon.rcs.jrsdk.JRVideoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;

/**
 * Created by Upon on 2018/2/6.
 */

public class JRCallActivity extends AppCompatActivity implements JRCallCallback, View.OnClickListener, CallCell.Callback, JRMediaDeviceCallback {
    private static final int UPDATE_END = 0;
    private static final int UPDATE_TO_CS = 1;
    private static final int REJECT_VIDEO_REQ = 2;
    private JRCallItem mCurItem;
    private JRMediaDeviceVideoCanvas mLocalCanvas;
    private JRMediaDeviceVideoCanvas mRemoteCanvas;
    private Statistics mStatistics;
    private FrameLayout mVideoLayout;
    private RecyclerView mMemberListView;
    private JRMultiMemberAdapter mAdapter;
    private OperationLayer mOperationLayer = new OperationLayer(this);
    private Realm mRealm;

    private ViewGroup mViewMain;
    private boolean mIsSender = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case UPDATE_END:
                    finish();
                    break;
                case UPDATE_TO_CS:
                    final String phone = (String) msg.obj;
                    AlertDialog.Builder builder = new AlertDialog.Builder(JRCallActivity.this);
                    builder.setTitle("警告");
                    builder.setMessage("呼叫失败,是否转成CS通话");
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_CALL);
                                intent.setData(Uri.parse("tel:" + phone));
                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(JRCallActivity.this, "Call Not Supported", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.create().show();
                    break;
                case REJECT_VIDEO_REQ:
                    AlertDialog dialog = (AlertDialog) msg.obj;
                    dialog.dismiss();
                    JRCall.getInstance().answerUpdate(false);
                    Toast.makeText(JRCallActivity.this, "已拒绝对方视频邀请", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Window window = getWindow();
        WindowManager.LayoutParams attrs = window.getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

        window.setAttributes(attrs);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jr_call);
        initViews();
        initLayers();
        initListeners();
        handleIntent(getIntent());
    }

    @Override
    public void onBackPressed() {

        if (mCurItem != null) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void handleIntent(Intent intent) {
        String phoneNumber = intent.getStringExtra(CommonValue.JRCALL_EXTRA_PHONE_NUMBER);
        boolean isVideo = intent.getBooleanExtra(CommonValue.JRCALL_EXTRA_IS_VIDEO, false);
        boolean isMulti = intent.getBooleanExtra(CommonValue.JRCALL_EXTRA_IS_MULTI, false);
        boolean isMcu = intent.getBooleanExtra(CommonValue.JRCALL_EXTRA_IS_MCU, false);
        String token = intent.getStringExtra(CommonValue.JRCALL_EXTRA_TOKEN);
        mCurItem = JRCall.getInstance().getCurrentCallItem();
        if (mCurItem != null) {
            setInComing(mCurItem);
        } else {
            if (isMulti) {
                RingUtils.playAudio(this, R.raw.ring_back, false);
                if (!TextUtils.isEmpty(phoneNumber)) {
                    String[] phones = phoneNumber.split(";");
                    final ArrayList<String> phoneList = new ArrayList<>();
                    phoneList.addAll(Arrays.asList(phones));
                    if (isVideo) {
                        mIsSender = true;
                        boolean s = JRCall.getInstance().createMultiCall(phoneList, true, token);
                        if (!s) {
                            Toast.makeText(this, "接口调用失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        return;
                    }
                    boolean s = JRCall.getInstance().createMultiCall(phoneList, false, null);
                    if (!s) {
                        Toast.makeText(this, "接口调用失败", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                return;
            }
            if (!TextUtils.isEmpty(phoneNumber)) {
                RingUtils.playAudio(this, R.raw.ring_back, false);
                if (isMcu) {
//                    JRCall.getInstance().mcuCall(CommonUtils.formatPhoneByCountryCode(phoneNumber));
                    return;
                }
                boolean s = JRCall.getInstance().call(CommonUtils.formatPhoneByCountryCode(phoneNumber), isVideo);
                if (!s) {
                    Toast.makeText(this, "接口调用失败", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void initViews() {
        mRealm = RealmHelper.getInstance();
        mViewMain = (ViewGroup) findViewById(R.id.call_main);
        mVideoLayout = (FrameLayout) findViewById(R.id.video_view);
        mMemberListView = new RecyclerView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMemberListView.setLayoutManager(new GridLayoutManager(this, 3));
        mMemberListView.setLayoutParams(layoutParams);
        boolean isVideo = getIntent().getBooleanExtra(CommonValue.JRCALL_EXTRA_IS_VIDEO, false);
        boolean isMulti = getIntent().getBooleanExtra(CommonValue.JRCALL_EXTRA_IS_MULTI, false);
        if (isMulti && isVideo) {
            mOperationLayer.setParentView(mViewMain, false);
        } else {
            mOperationLayer.setParentView(mViewMain, true);
        }
    }


    private void initLayers() {
        mOperationLayer.setEventReceiver(new OperationLayer.EventReceiver() {
            @Override
            public void onEvent(String event, String... args) {
                if (OperationLayer.EVENT_SHOWN.equals(event)) {
                } else if (OperationLayer.EVENT_HIDDEN.equals(event)) {

                } else if (OperationLayer.EVENT_STATISTIC.equals(event)) {
                    if (mStatistics == null) {
//                        JRLog
                        mStatistics = new Statistics(getApplicationContext(), mCurItem.callMembers.get(0));
                        mViewMain.addView(mStatistics);
                    }
                    if (mStatistics.isShow()) {
                        mStatistics.hideStat();
                    } else {
                        mStatistics.showStat();
                    }
                } else if (OperationLayer.EVENT_ANSWER_DEFAULT.equals(event)) {
                    if (JRCall.getInstance().answer(mCurItem.isVideo(), "")) {
                        mOperationLayer.setStateText("接听中", true, false);
                        mOperationLayer.setStatusIncoming(false, false);
                    }
                    RingUtils.stop();
                } else if (OperationLayer.EVENT_ANSWER_CAMERA_OFF.equals(event)) {
                    if (JRCall.getInstance().answer(false, "")) {
                        mOperationLayer.setStateText("接听中", true, false);
                        mOperationLayer.setStatusIncoming(false, false);
                    }
                    RingUtils.stop();
                } else if (OperationLayer.EVENT_SWITCH_FRONT_REAR.equals(event)) {
                    JRCall.getInstance().switchCamera();
                } else if (OperationLayer.EVENT_ANSWER_DECLINE.equals(event)) {
                    if (JRCall.getInstance().end(JRCallConstants.CALL_TERM_REASON_DECLINE)) {
                        mOperationLayer.setStateText(getString(R.string.ending), true, false);
                    }
                } else if (OperationLayer.EVENT_END.equals(event)) {
                    JRCall.getInstance().end(JRCallConstants.CALL_TERM_REASON_NORMAL);
                } else if (OperationLayer.EVENT_MERGE_CALL.equals(event)) {
                } else if (OperationLayer.EVENT_MUTE.equals(event)) {
                    JRCall.getInstance().mute();
                } else if (OperationLayer.EVENT_SPEAKER.equals(event)) {
                    JRMediaDevice.getInstance().enableSpeaker(!mOperationLayer.isSpeakerOn());
                    mOperationLayer.setSpeakerOn(!mOperationLayer.isSpeakerOn());
                } else if (OperationLayer.EVENT_AUDIO_TO_VIDEO.equals(event)) {
                    JRCall.getInstance().updateCall(true);
                } else if (OperationLayer.EVENT_VIDEO_TO_AUDIO.equals(event)) {
                    if (JRCall.getInstance().updateCall(false)) {
                        mLocalCanvas = null;
                        if (mRemoteCanvas != null) {
                            JRMediaDevice.getInstance().stopVideo(mRemoteCanvas);
                        }
                        mRemoteCanvas = null;
                        mVideoLayout.removeAllViews();
                        mVideoLayout.setVisibility(View.GONE);
                        mOperationLayer.setSpeakerOn(false);
                        JRMediaDevice.getInstance().enableSpeaker(false);
                    }
                } else if (OperationLayer.EVENT_ADD_CALL.equals(event)) {
                    if (mCurItem.isConf()) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, CommonValue.REQUEST_ADD_MEMBER);
                    } else if (mCurItem.hold) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, CommonValue.REQUEST_ADD_CALL);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(JRCallActivity.this);
                        String[] items = {"多方通话", "添加通话"};
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                                    startActivityForResult(intent, CommonValue.REQUEST_TO_MULTI_CALL);
                                } else {
                                    JRCall.getInstance().hold();
                                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                                    startActivityForResult(intent, CommonValue.REQUEST_ADD_CALL);
                                }
                            }
                        });
                        builder.create().show();
                    }
                } else if (OperationLayer.EVENT_HOLD_CALL.equals(event)) {
                    if (JRCall.getInstance().hold()) {
                        if (mCurItem.hold) {
                            mOperationLayer.setHold(false, true);
                        } else {
                            mOperationLayer.setHold(true, true);
                        }
                    }
                }
            }

        });
    }

    private void initListeners() {
        JRCall.getInstance().addCallback(this);
        JRMediaDevice.getInstance().addCallback(this);
    }

    @Override
    public void onCallItemUpdate(JRCallItem item, int updateType) {
        updateCall(item, updateType, -1);
        if (updateType != JRCallConstants.CALL_UPDATE_TYPE_NET_STATUS_CHANGED) {
            RealmDataHelper.insertOrUpdateCallLog(mRealm, item);
        }
    }

    @Override
    public void onCallItemRemove(JRCallItem item, int reason) {
        updateCall(item, JRCallConstants.CALL_UPDATE_TYPE_TERMED, reason);
        RealmDataHelper.insertOrUpdateCallLog(mRealm, item);
    }

    @Override
    public void onCallItemAdd(JRCallItem item) {
        RealmDataHelper.insertOrUpdateCallLog(mRealm, item);
        updateCall(item, -1, JRCallConstants.CALL_TERM_REASON_NONE);
    }

    @Override
    protected void onDestroy() {
        if (CallCell.sCallArray != null) {
            CallCell.sCallArray.clear();
        }
        mRealm.close();
        mHandler.removeMessages(UPDATE_TO_CS);
        mHandler.removeMessages(UPDATE_END);
        mHandler.removeMessages(REJECT_VIDEO_REQ);
        RingUtils.stop();

        if (mRemoteCanvas != null) {
            JRMediaDevice.getInstance().stopVideo(mRemoteCanvas);
        }
        if (mAdapter != null) {
            mAdapter.clearData();
        }
        JRMediaDevice.getInstance().enableSpeaker(false);
        JRCall.getInstance().removeCallback(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
        String phone = getNumberWithCursor(cursor);
        switch (requestCode) {
            case CommonValue.REQUEST_ADD_CALL:
                JRCall.getInstance().call(phone, false);
                break;
            case CommonValue.REQUEST_ADD_MEMBER:
                if (!TextUtils.isEmpty(phone)) {
                    boolean succeed = JRCall.getInstance().addMultiCallMember(phone);
                    if (succeed) {
                        CallCell.addCall(1, NumberUtils.formatPhoneByCountryCode(phone), NumberUtils.formatPhoneByCountryCode(phone), this, MtcConfConstants.EN_MTC_CONF_PARTP_STATE_PENDING);
                    }
                    if (mCurItem.isVideo() && mCurItem.isConf() && succeed) {
                        doWithMultiVideoCall(mCurItem, NumberUtils.formatPhoneByCountryCode(phone));
                    }
                }
                break;
            case CommonValue.REQUEST_TO_MULTI_CALL:
                if (!TextUtils.isEmpty(phone)) {
                    if (mCurItem.callMembers.size() > 0) {
                        String oldNumber = mCurItem.callMembers.get(0).number;
                        ArrayList<String> newList = new ArrayList<>();
                        newList.add(oldNumber);
                        newList.add(phone);
                        JRCall.getInstance().createMultiCall(newList, false, null);
                    }
                }
                break;
        }
    }

    private String getNumberWithCursor(Cursor contactCursor) {
        if (contactCursor == null)
            return null;

        if (!contactCursor.moveToFirst())
            return null;

        int phoneColumn = contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        int nameColumn = contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int thumbColunm = contactCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
        int phoneNum = contactCursor.getInt(phoneColumn);
        String name = contactCursor.getString(nameColumn);
        String thumbUri = contactCursor.getString(thumbColunm);
        String phoneNumber = "";
        if (phoneNum > 0) {
            // 获得联系人的ID号
            int idColumn = contactCursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId = contactCursor.getString(idColumn);
            // 获得联系人电话的cursor
            Cursor phoneCursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
                            + contactId, null, null);
            if (phoneCursor.moveToFirst()) {
                for (; !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {
                    int index = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int typeIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    int phoneType = phoneCursor.getInt(typeIndex);
                    phoneNumber = phoneCursor.getString(index);
                    phoneNumber = phoneNumber.replace(" ", "").replace("-", "");
                }
                if (!phoneCursor.isClosed()) {
                    phoneCursor.close();
                }
            }
        }
        contactCursor.close();
        return phoneNumber;
    }

    private void setInComing(JRCallItem item) {
        if (item == null) {
            return;
        }
        if (item.isConf()) {
            List<JRCallMember> list = item.callMembers;
            for (JRCallMember member : list) {
                CallCell.addCall(1, member.number, member.number, this, getMtcState(member.status));
            }
        }
        mOperationLayer.setOneCallViewsGone();
        mOperationLayer.setConfInComingViewsGone();
        if (item.callMembers.size() > 0) {
            mOperationLayer.onUserInfoChanged(item.callMembers.get(0).displayName, item.callMembers.get(0).number);
        }
        mOperationLayer.resetStateText();
        mOperationLayer.setStateText("", false, false);
        mOperationLayer.setStatusIncoming(true, item.isVideo());
        RingUtils.startRing(getBaseContext(), R.raw.ringtone_music_box);
        if (item.isVideo()) {
            initSurfaceView(item.isConf());
        }
    }

    private void updateCall(final JRCallItem item, int updateType, int reason) {
        if (item == null) {
            return;
        }
        mCurItem = item;
        if (item.isConf()) {
            List<JRCallMember> list = item.callMembers;
            for (JRCallMember member : list) {
                CallCell.addCall(1, member.number, member.number, this, getMtcState(member.status));
            }
//            if (item.isVideo()) {
//                doWithMultiVideoCall(item);
//            }
        }
        mOperationLayer.setaddcallShow(item.isConf());

        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_OUTGOING) {
            if (item.isVideo()) {
                initSurfaceView(item.isConf());
            }
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_TALKING) {
            if (item.isVideo() && !item.isConf()) {
                if (mRemoteCanvas == null) {
                    mRemoteCanvas = JRMediaDevice.getInstance().startVideo(item.callMembers.get(0).videoSource, JRMediaDeviceConstants.RENDER_TYPE_AUTO);
                    mVideoLayout.addView(mRemoteCanvas.getVideoView());
                }
                mVideoLayout.setVisibility(View.VISIBLE);
                mOperationLayer.setSpeakerOn(true);
                JRMediaDevice.getInstance().enableSpeaker(true);
            } else if (item.isVideo() && item.isConf()) {
                mOperationLayer.setSpeakerOn(true);
                JRMediaDevice.getInstance().enableSpeaker(true);
            } else if (!item.isVideo()) {
                mLocalCanvas = null;
                if (mRemoteCanvas != null) {
                    JRMediaDevice.getInstance().stopVideo(mRemoteCanvas);
                }
                mRemoteCanvas = null;
                mVideoLayout.removeAllViews();
                mVideoLayout.setVisibility(View.GONE);
                mOperationLayer.setSpeakerOn(false);
                JRMediaDevice.getInstance().enableSpeaker(false);
            }
            mOperationLayer.setStatusIncoming(false, item.isVideo());
            mOperationLayer.resetStateText();
            mOperationLayer.setHold(false, true);
            mOperationLayer.setBaseTime(SystemClock.elapsedRealtime());
            mOperationLayer.startTimer();
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_TO_AUDIO_OK) {
            mLocalCanvas = null;
            if (mRemoteCanvas != null) {
                JRMediaDevice.getInstance().stopVideo(mRemoteCanvas);
            }
            mRemoteCanvas = null;
            mVideoLayout.removeAllViews();
            mVideoLayout.setVisibility(View.GONE);
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_TO_VIDEO_OK) {
            initSurfaceView(false);
            if (mRemoteCanvas == null) {
                mRemoteCanvas = JRMediaDevice.getInstance().startVideo(mCurItem.callMembers.get(0).videoSource, JRMediaDeviceConstants.RENDER_TYPE_AUTO);
                mVideoLayout.addView(mRemoteCanvas.getVideoView());
            }
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_REQ_VIDEO) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(JRCallActivity.this);
            builder.setTitle("通知");
            builder.setMessage("对方邀请你视频通话，是否接受?");
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    JRCall.getInstance().answerUpdate(false);
                }
            });
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    JRCall.getInstance().answerUpdate(true);
                    initSurfaceView(false);
                    mHandler.removeMessages(REJECT_VIDEO_REQ);
                }
            });
            final AlertDialog dia = builder.create();
            dia.show();
            Message msg = mHandler.obtainMessage();
            msg.what = REJECT_VIDEO_REQ;
            msg.obj = dia;
            mHandler.sendMessageDelayed(msg, 15000);
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_STRM_UPDATE) {
//            mOperationLayer.hide();
            mOperationLayer.setMargin();
            doWithMultiVideoCall(item, null);
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_TERMED) {

        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_ALERTING) {
            mOperationLayer.setStateText(getString(R.string.alerting), false, false);
            mOperationLayer.setStatusIncoming(false, item.isVideo());
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_HELD) {
            mOperationLayer.setHeld(item.held, !item.held);
            mOperationLayer.setStateText("被挂起", false, false);
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_UNHELD) {
            mOperationLayer.setHeld(item.held, !item.held);
            if (item.talkingBeginTime != 0 && item.state == JRCallConstants.CALL_STATE_TALKING) {
                long duration = System.currentTimeMillis() - item.talkingBeginTime;
                mOperationLayer.setBaseTime(SystemClock.elapsedRealtime() - duration);
                mOperationLayer.startTimer();
            }
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_UNHOLD) {
            mOperationLayer.setHold(false, true);
            if (item.talkingBeginTime != 0 && item.state == JRCallConstants.CALL_STATE_TALKING) {
                long duration = System.currentTimeMillis() - item.talkingBeginTime;
                mOperationLayer.setBaseTime(SystemClock.elapsedRealtime() - duration);
                mOperationLayer.startTimer();
            }
        }
        if (updateType == JRCallConstants.CALL_UPDATE_TYPE_CONF_MEMBER_UPDATE) {
            if (!item.isVideo()) {
                List<JRCallMember> list = item.callMembers;
                for (JRCallMember member : list) {
                    CallCell.addCall(1, member.number, member.number, this, getMtcState(member.status));
                }
            } else {
                doWithMultiVideoCall(item, null);
            }
        }
        updateCallState(item, reason);
        mOperationLayer.setLayoutState(item);

    }

    public void shrinkPreviewSurfaceView(SurfaceView surfaceView, int callId) {
        if (surfaceView == null)
            return;
        int screenWidth = CommonUtils.getScreenWidth(JRClient.getInstance().getContext());
        int screenHeight = CommonUtils.getScreenWidth(JRClient.getInstance().getContext());
        ST_MTC_RECT localRect = JRVideoUtils.calcLocalRect(130,
                220, screenWidth, screenHeight);
        JRVideoUtils.setViewRect(surfaceView, localRect);
        surfaceView.setZOrderOnTop(true);
    }

    public void shrinkMultiSurfaceView(SurfaceView surfaceView, int i) {
        if (surfaceView == null)
            return;
        int screenWidth = CommonUtils.getScreenWidth(JRClient.getInstance().getContext());
        int screenHeight = CommonUtils.getScreenWidth(JRClient.getInstance().getContext());
        ST_MTC_RECT rect = JRVideoUtils.calcLocalRect(250,
                320, screenWidth, screenHeight);
        FrameLayout.LayoutParams rlp = new FrameLayout.LayoutParams(rect.getIWidth(), rect.getIHeight());
        rlp.leftMargin = rect.getIX();
        rlp.topMargin = rect.getIY();
        rlp.gravity = Gravity.NO_GRAVITY;
        rlp.topMargin = 200 * i;
        surfaceView.setLayoutParams(rlp);
        surfaceView.setOnTouchListener(new JRVideoUtils.OnTouchMoveListener());
    }

    public void onClickDialpad(View v) {
        String dtmfStr = v.getTag().toString();
        mOperationLayer.appendDtmf(dtmfStr);
        if (dtmfStr.length() > 0) {
            JRCall.getInstance().sendDtmf(getJRDtmf(dtmfStr.substring(0, 1)));
        }
    }

    public void onConferenceCellClick(View view) {
        final CallCell callCell = (CallCell) view.getTag();
        if (callCell != null) {
            if (callCell.mSessState == MtcConfConstants.EN_MTC_CONF_PARTP_STATE_DISCING || callCell.mSessState == MtcConfConstants.EN_MTC_CONF_PARTP_STATE_DISCED) {
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMessage(String.format(getString(R.string.invite_member_message), callCell.mName));
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] num = {callCell.mPhone};
                        JRCallMember member = new JRCallMember();
                        member.number = callCell.mPhone;
                        member.displayName = callCell.mPhone;
                        JRCall.getInstance().addMultiCallMember(member.number);
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                b.show();
                return;
            }
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage(String.format(getString(R.string.kick_member_message), callCell.mName));
            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String[] num = {callCell.mPhone};
                    JRCallMember member = new JRCallMember();
                    member.number = callCell.mPhone;
                    member.displayName = callCell.mPhone;
                    JRCall.getInstance().removeMultiCallMember(member);
//                    callCell.onClick();
                }
            });
            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            b.show();

        }
    }

    @Override
    public void onClick(View view) {

    }

    private int getMtcState(int state) {
        if (state == JRCallConstants.CALL_MEMBER_STATUS_ALERTING) {
            return MtcConfConstants.EN_MTC_CONF_PARTP_STATE_ALERTING;
        } else if (state == JRCallConstants.CALL_MEMBER_STATUS_CONNED) {
            return MtcConfConstants.EN_MTC_CONF_PARTP_STATE_CONNED;
        } else if (state == JRCallConstants.CALL_MEMBER_STATUS_PENDING) {
            return MtcConfConstants.EN_MTC_CONF_PARTP_STATE_PENDING;
        } else if (state == JRCallConstants.CALL_MEMBER_STATUS_DIALINGIN) {
            return MtcConfConstants.EN_MTC_CONF_PARTP_STATE_DIALINGIN;
        } else if (state == JRCallConstants.CALL_MEMBER_STATUS_DIALINGOUT) {
            return MtcConfConstants.EN_MTC_CONF_PARTP_STATE_DIALINGOUT;
        } else if (state == JRCallConstants.CALL_MEMBER_STATUS_DISCED) {
            return MtcConfConstants.EN_MTC_CONF_PARTP_STATE_DISCED;
        } else if (state == JRCallConstants.CALL_MEMBER_STATUS_DISCING) {
            return MtcConfConstants.EN_MTC_CONF_PARTP_STATE_DISCING;
        } else if (state == JRCallConstants.CALL_MEMBER_STATUS_ONHOLD) {
            return MtcConfConstants.EN_MTC_CONF_PARTP_STATE_ONHOLD;
        } else {
            return MtcConfConstants.EN_MTC_CONF_PARTP_STATE_PENDING;
        }
    }

    private int getJRDtmf(String dtmf) {
        switch (dtmf) {
            case "0":
                return JRCallConstants.CALL_DTMF_TYPE_0;
            case "1":
                return JRCallConstants.CALL_DTMF_TYPE_1;
            case "2":
                return JRCallConstants.CALL_DTMF_TYPE_2;
            case "3":
                return JRCallConstants.CALL_DTMF_TYPE_3;
            case "4":
                return JRCallConstants.CALL_DTMF_TYPE_4;
            case "5":
                return JRCallConstants.CALL_DTMF_TYPE_5;
            case "6":
                return JRCallConstants.CALL_DTMF_TYPE_6;
            case "7":
                return JRCallConstants.CALL_DTMF_TYPE_7;
            case "8":
                return JRCallConstants.CALL_DTMF_TYPE_8;
            case "9":
                return JRCallConstants.CALL_DTMF_TYPE_9;
            case "*":
                return JRCallConstants.CALL_DTMF_TYPE_STAR;
            case "#":
                return JRCallConstants.CALL_DTMF_TYPE_POUND;
            case "A":
                return JRCallConstants.CALL_DTMF_TYPE_A;
            case "B":
                return JRCallConstants.CALL_DTMF_TYPE_B;
            case "C":
                return JRCallConstants.CALL_DTMF_TYPE_C;
            case "D":
                return JRCallConstants.CALL_DTMF_TYPE_D;
            default:
                return JRCallConstants.CALL_DTMF_TYPE_0;
        }
    }

    private void initSurfaceView(boolean isConf) {
        if (mLocalCanvas == null) {
            mLocalCanvas = JRMediaDevice.getInstance().startCameraVideo(JRMediaDeviceConstants.RENDER_TYPE_AUTO);
            mVideoLayout.addView(mLocalCanvas.getVideoView());
        }
        mVideoLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void callCellDidEndConference(CallCell callCell) {

    }

    @Override
    public void callCellNeedsReloadData(CallCell callCell) {
        mOperationLayer.setConferenceList(CallCell.sCallArray);
    }

    @Override
    public void callCellStateChanged(CallCell callCell) {

    }

    private void updateCallState(final JRCallItem item, int reason) {
        switch (item.state) {
            case JRCallConstants.CALL_STATE_PENDING:
            case JRCallConstants.CALL_STATE_INIT:
                if (!item.isConn && item.isVideo()) {
                    return;
                }
                if (item.isConf()) {
                    if (!item.isVideo()) {
                        mOperationLayer.enterConference();
                    }
                    List<JRCallMember> list = item.callMembers;
                    for (JRCallMember member : list) {
                        CallCell.addCall(1, member.number, member.number, this, getMtcState(member.status));
                    }
                }
                if (item.callMembers.size() > 0) {
                    mOperationLayer.onUserInfoChanged(item.callMembers.get(0).displayName, item.callMembers.get(0).number);
                }
                mOperationLayer.setStateText(getString(R.string.calling), false, false);
                mOperationLayer.setStatusIncoming(false, item.isVideo());
                break;
            case JRCallConstants.CALL_STATE_OK:
            case JRCallConstants.CALL_STATE_CANCELED:
                mOperationLayer.setStateText(getString(R.string.ending), false, false);
                switch (reason) {
                    case JRCallConstants.CALL_TERM_REASON_FORBIDDEN:
                    case JRCallConstants.CALL_TERM_REASON_NOT_FOUND:
                    case JRCallConstants.CALL_TERM_REASON_INTERNAL_ERR:
                    case JRCallConstants.CALL_TERM_REASON_TEMP_UNAVAIL:
                    case JRCallConstants.CALL_TERM_REASON_SRV_UNAVAIL:
                    case JRCallConstants.CALL_TERM_REASON_OTHER_ERROR:
                    case JRCallConstants.CALL_TERM_REASON_REQ_TERMED:
                        Message msg = mHandler.obtainMessage();
                        msg.what = UPDATE_TO_CS;
                        msg.obj = item.callMembers.get(0).number;
                        mHandler.sendMessageDelayed(msg, 500);
                        break;
                    default:
                        mHandler.sendEmptyMessageDelayed(UPDATE_END, 500);
                }
                break;
        }
    }

    private void doWithMultiVideoCall(JRCallItem item, String addNumber) {
        mOperationLayer.hideMultiVideoItems();
        mVideoLayout.removeView(mMemberListView);
        if (mAdapter == null) {
            mAdapter = new JRMultiMemberAdapter(this, item.callMembers, addNumber, mIsSender);
            mMemberListView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(item.callMembers, addNumber);
        }
        mVideoLayout.addView(mMemberListView);
        mMemberListView.bringToFront();
    }

    @Override
    public void onCameraTypeChange() {

    }

    @Override
    public void onAudioOutputTypeChange() {

    }

    @Override
    public void onRenderStart(SurfaceView surfaceView) {
        //这回调代表远端图像流收到了  需要缩小自己的本地窗口
        if (mRemoteCanvas != null) {
            if (mRemoteCanvas.getVideoView() == surfaceView) {
                //缩小本地窗口方法
                shrinkPreviewSurfaceView(mLocalCanvas.getVideoView(), mCurItem.callId);
            }
        }
    }
}
