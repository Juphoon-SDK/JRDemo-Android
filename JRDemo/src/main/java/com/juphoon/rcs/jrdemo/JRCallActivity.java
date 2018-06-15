package com.juphoon.rcs.jrdemo;

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

import com.juphoon.cmcc.app.lemon.MtcCall;
import com.juphoon.cmcc.app.lemon.MtcNumber;
import com.juphoon.cmcc.app.lemon.ST_MTC_RECT;
import com.juphoon.rcs.JRAccount;
import com.juphoon.rcs.JRAccountConstants;
import com.juphoon.rcs.JRCall;
import com.juphoon.rcs.JRCallCallback;
import com.juphoon.rcs.JRCallConstants;
import com.juphoon.rcs.JRCallItem;
import com.juphoon.rcs.JRCallMember;
import com.juphoon.rcs.JRCallStorage;
import com.juphoon.rcs.JRClient;
import com.juphoon.rcs.JRLog;
import com.juphoon.rcs.JRMediaDevice;
import com.juphoon.rcs.JRMediaDeviceCallback;
import com.juphoon.rcs.JRMediaDeviceCanvas;
import com.juphoon.rcs.JRMediaDeviceContancts;
import com.juphoon.rcs.utils.JRCommonUtils;
import com.juphoon.rcs.utils.JRRingUtils;
import com.juphoon.rcs.utils.JRVideoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.CommonValue;
import common.RealmDataHelper;
import common.RealmHelper;
import common.utils.JRNumberUtils;
import io.realm.Realm;
import ui.Statistics;
import ui.call.CallCell;
import ui.call.OperationLayer;

/**
 * Created by Upon on 2018/2/6.
 */

public class JRCallActivity extends AppCompatActivity implements JRCallCallback, View.OnClickListener, CallCell.Callback, JRMediaDeviceCallback {
    private static final int UPDATE_END = 0;
    private static final int UPDATE_TO_CS = 1;
    private static final int REJECT_VIDEO_REQ = 2;
    private JRCallItem mCurItem;
    private JRCallItem mAnotherItem;
    private boolean isAnotherItem;
    private JRMediaDeviceCanvas mLocalCanvas;
    private JRMediaDeviceCanvas mRemoteCanvas;
    private Statistics mStatistics;
    private FrameLayout mVideoLayout;
    //    private LinearLayout mMultiVideoLayout;
    private RecyclerView mMemberListView;
    private JRMultiMemberAdapter mAdapter;
    private OperationLayer mOperationLayer = new OperationLayer(this);
    private Realm mRealm;

    private ViewGroup mViewMain;

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

                                JRLog.log("yidao", phone + "");
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
                    JRCall.getInstance().answerUpdate(mCurItem, false);
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
        int sessId = intent.getIntExtra(CommonValue.JRCALL_EXTRA_SESSION_ID, -1);
        String phoneNumber = intent.getStringExtra(CommonValue.JRCALL_EXTRA_PHONE_NUMBER);
        boolean isVideo = intent.getBooleanExtra(CommonValue.JRCALL_EXTRA_IS_VIDEO, false);
        boolean isMulti = intent.getBooleanExtra(CommonValue.JRCALL_EXTRA_IS_MULTI, false);
        String token = intent.getStringExtra(CommonValue.JRCALL_EXTRA_TOKEN);
        if (sessId != -1) {
            mCurItem = JRCallStorage.getInstance().getCallItem(sessId);
            setInComing(mCurItem);
        } else {
            if (isMulti) {
                JRRingUtils.playAudio(this, R.raw.ring_back, false);
                if (!TextUtils.isEmpty(phoneNumber)) {
                    String[] phones = phoneNumber.split(";");
                    final ArrayList<String> phoneList = new ArrayList<>();
                    phoneList.addAll(Arrays.asList(phones));
                    if (isVideo) {
                        JRAccount.getInstance().setAccountConfig(JRClient.getInstance().getCurAccount(), JRAccountConstants.JRAccountConfigHttpToken, token);
//                        MtcConfDb.Mtc_ConfDbSetHttpConfToken(token);
//                        MtcCliDb.Mtc_CliDbApplyAll();
                        boolean s = JRCall.getInstance().createMultiCall(phoneList, true, null);
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
                JRRingUtils.playAudio(this, R.raw.ring_back, false);
                boolean s = JRCall.getInstance().call(JRCommonUtils.formatPhoneWithCountryCode(phoneNumber), isVideo, null);
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
//        mMultiVideoLayout = new LinearLayout(this);
//        mMultiVideoLayout.setOrientation(LinearLayout.VERTICAL);
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
                        mStatistics = new Statistics(getApplicationContext(), JRCallStorage.getInstance().getCurItem().getCallId());
                        mViewMain.addView(mStatistics);
                    }
                    if (mStatistics.isShow()) {
                        mStatistics.hideStat();
                    } else {
                        mStatistics.showStat();
                    }
                } else if (OperationLayer.EVENT_ANSWER_DEFAULT.equals(event)) {
                    if (JRCall.getInstance().answer(mCurItem, mCurItem.isVideo())) {
                        mOperationLayer.setStateText("接听中", true, false);
                        mOperationLayer.setStatusIncoming(false, false);
                    }
                    JRRingUtils.stop();
                } else if (OperationLayer.EVENT_ANSWER_CAMERA_OFF.equals(event)) {
                    if (JRCall.getInstance().answer(mCurItem, false)) {
                        mOperationLayer.setStateText("接听中", true, false);
                        mOperationLayer.setStatusIncoming(false, false);
                    }
                    JRRingUtils.stop();
                } else if (OperationLayer.EVENT_SWITCH_FRONT_REAR.equals(event)) {
                    JRCall.getInstance().switchCamera(JRCallStorage.getInstance().getCurItem());
                } else if (OperationLayer.EVENT_ANSWER_DECLINE.equals(event)) {
                    if (JRCall.getInstance().endCall(mCurItem, JRCallConstants.TremReason.DECLINE)) {
                        mOperationLayer.setStateText(getString(R.string.ending), true, false);
                    }
                    mHandler.sendEmptyMessageDelayed(UPDATE_END, 1500);
                } else if (OperationLayer.EVENT_END.equals(event)) {
                    JRCall.getInstance().endCall(JRCallStorage.getInstance().getCurItem(), JRCallConstants.TremReason.NOMAL);
                    mHandler.sendEmptyMessageDelayed(UPDATE_END, 1500);
                } else if (OperationLayer.EVENT_MERGE_CALL.equals(event)) {
                    if (mCurItem != null && mAnotherItem != null) {
                        String curNumber = mCurItem.getMemberList().get(0).getNumber();
                        String otherNumber = mAnotherItem.getMemberList().get(0).getNumber();
                        ArrayList<String> list = new ArrayList<String>();
                        list.add(curNumber);
                        list.add(otherNumber);
                        JRCall.getInstance().createMultiCall(list, false, null);
                    }
                } else if (OperationLayer.EVENT_MUTE.equals(event)) {
                    JRCall.getInstance().mute(JRCallStorage.getInstance().getCurItem());
                } else if (OperationLayer.EVENT_SPEAKER.equals(event)) {
                    JRMediaDevice.getInstance().enableSpeaker(null, !mOperationLayer.isSpeakerOn());
                    mOperationLayer.setSpeakerOn(!mOperationLayer.isSpeakerOn());
                } else if (OperationLayer.EVENT_AUDIO_TO_VIDEO.equals(event)) {
                    JRCall.getInstance().updateCall(JRCallStorage.getInstance().getCurItem(), true);
                } else if (OperationLayer.EVENT_VIDEO_TO_AUDIO.equals(event)) {
                    if (JRCall.getInstance().updateCall(JRCallStorage.getInstance().getCurItem(), false)) {
                        JRMediaDevice.getInstance().stopCamera();
                        mLocalCanvas = null;
                        if (mRemoteCanvas != null) {
                            JRMediaDevice.getInstance().stopVideo(mRemoteCanvas);
                        }
                        mRemoteCanvas = null;
                        mVideoLayout.removeAllViews();
                        mVideoLayout.setVisibility(View.GONE);
                        mOperationLayer.setSpeakerOn(false);
                        JRMediaDevice.getInstance().enableSpeaker(null, false);
                    }
                } else if (OperationLayer.EVENT_ADD_CALL.equals(event)) {
                    if (JRCallStorage.getInstance().getCurItem().isConference()) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, CommonValue.REQUEST_ADD_MEMBER);
                    } else if (JRCallStorage.getInstance().getCurItem().isHold()) {
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
                                    JRCall.getInstance().hold(JRCallStorage.getInstance().getCurItem());
                                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                                    startActivityForResult(intent, CommonValue.REQUEST_ADD_CALL);
                                }
                            }
                        });
                        builder.create().show();
                    }
                } else if (OperationLayer.EVENT_HOLD_CALL.equals(event)) {
                    if (JRCall.getInstance().hold(JRCallStorage.getInstance().getCurItem())) {
                        if (JRCallStorage.getInstance().getCurItem().isHold()) {
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
    public void onCallItemUpdated(JRCallItem item, JRCallConstants.Error error, int updateType) {
        updateCall(item, updateType, JRCallConstants.TremReason.NONE);
        if (updateType != JRCallConstants.UPDATE_TYPE_CALL_NET_STA_CHANGED) {
            RealmDataHelper.insertOrUpdateCallLog(mRealm, item);
        }
        updateCallByType(item, updateType);
    }

    @Override
    public void onCallItemRemove(JRCallItem item, JRCallConstants.TremReason error) {
        if (mCurItem != null) {
            if (item.getCallId() != mCurItem.getCallId()) {
                updateCall(item, -1, error);
            } else {
                updateCall(mCurItem, -1, error);
            }
        }
        RealmDataHelper.insertOrUpdateCallLog(mRealm, item);
    }

    @Override
    public void onCallItemAdd(JRCallItem item) {
        RealmDataHelper.insertOrUpdateCallLog(mRealm, item);
        updateCall(item, -1, JRCallConstants.TremReason.NONE);
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
        JRRingUtils.stop();
        if (mRemoteCanvas != null) {
            JRMediaDevice.getInstance().stopVideo(mRemoteCanvas);
        }
        JRMediaDevice.getInstance().stopAudio();
        JRCall.getInstance().removeCallback(this);
        JRCallStorage.getInstance().setCurItem(null);
        JRMediaDevice.getInstance().stopCamera();
        JRMediaDevice.getInstance().enableSpeaker(null, false);
        JRCallStorage.getInstance().clearCallIdList();
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
                JRCall.getInstance().call(phone, false, null);
                break;
            case CommonValue.REQUEST_ADD_MEMBER:
                if (!TextUtils.isEmpty(phone)) {
                    boolean succeed = JRCall.getInstance().addMultiCallMember(phone);
                    if (succeed) {
                        CallCell.addCall(1, phone, phone, this, JRCommonValue.EN_MTC_CONF_PARTP_STATE_PENDING);
                    }
                    if (mCurItem.isVideo() && mCurItem.isConference() && succeed) {
                        doWithMultiVideoCall(mCurItem, JRNumberUtils.formatPhoneByCountryCode(phone));
                    }
                }
                break;
            case CommonValue.REQUEST_TO_MULTI_CALL:
                if (!TextUtils.isEmpty(phone)) {
                    if (mCurItem.getMemberList().size() > 0) {
                        String oldNumber = mCurItem.getMemberList().get(0).getNumber();
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
        JRCallStorage.getInstance().setCurItem(item);
        if (item.isConference()) {
            List<JRCallMember> list = item.getMemberList();
            for (JRCallMember member : list) {
                CallCell.addCall(1, member.getNumber(), member.getNumber(), this, getMtcState(member.getState()));
            }
        }
        mOperationLayer.setOneCallViewsGone();
        mOperationLayer.setConfInComingViewsGone();
        if (item.getMemberList().size() > 0) {
            mOperationLayer.onUserInfoChanged(item.getMemberList().get(0).getDisplayName(), item.getMemberList().get(0).getNumber());
        }
        mOperationLayer.resetStateText();
        mOperationLayer.setStateText("", false, false);
        mOperationLayer.setStatusIncoming(true, item.isVideo());
        JRRingUtils.startRing(getBaseContext(), R.raw.ringtone_music_box);
        if (item.isVideo()) {
            initSurfaceView(item.isConference());
        }
    }

    private void updateCall(final JRCallItem item, int updateType, JRCallConstants.TremReason reason) {
        if (item == null) {
            return;
        }
        if (mCurItem == null) {
            isAnotherItem = false;
            mCurItem = item;
            JRLog.log("yidao", mCurItem.getMemberList().get(0).getNumber());
            mAnotherItem = null;
        } else if (item.getCallId() == mCurItem.getCallId()) {
            isAnotherItem = false;
            mCurItem = item;
        } else if (item.getCallId() != mCurItem.getCallId()) {
            isAnotherItem = true;
            mAnotherItem = item;
        }
        JRCallStorage.getInstance().setCurItem(item);
        if (item.isConference()) {
            List<JRCallMember> list = item.getMemberList();
            for (JRCallMember member : list) {
                CallCell.addCall(1, member.getNumber(), member.getNumber(), this, getMtcState(member.getState()));
            }
//            if (item.isVideo()) {
//                doWithMultiVideoCall(item);
//            }
        }
        mOperationLayer.setaddcallShow(item.isConference());

        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_OUTGOING) {
            if (item.isVideo()) {
                initSurfaceView(item.isConference());
            }
        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_TALKING) {
            if (item.isVideo() && !item.isConference()) {
                if (mRemoteCanvas == null) {
                    mRemoteCanvas = JRMediaDevice.getInstance().startVideo(JRCallStorage.getInstance().getCurItem().getMemberList().get(0).getRenderId(), JRMediaDeviceContancts.VIDEO_CAMERA_FRONT);
                    mVideoLayout.addView(mRemoteCanvas.getVideoView());
                }
                mVideoLayout.setVisibility(View.VISIBLE);
                mOperationLayer.setSpeakerOn(true);
                JRMediaDevice.getInstance().enableSpeaker(null, true);
            } else if (item.isVideo() && item.isConference()) {
                mOperationLayer.setSpeakerOn(true);
                JRMediaDevice.getInstance().enableSpeaker(null, true);
            }
            mOperationLayer.setStatusIncoming(false, item.isVideo());
            if (isAnotherItem) {
                mOperationLayer.resetStateText();
                mOperationLayer.setHold(false, true);
                mOperationLayer.setBaseTime(SystemClock.elapsedRealtime());
                mOperationLayer.startTimer();
            } else if (!isAnotherItem) {
                mOperationLayer.resetStateText();
                mOperationLayer.setHold(false, true);
                mOperationLayer.setBaseTime(SystemClock.elapsedRealtime());
                mOperationLayer.startTimer();
            }
        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_TO_AUDIO) {
            JRMediaDevice.getInstance().stopCamera();
            mLocalCanvas = null;
            if (mRemoteCanvas != null) {
                JRMediaDevice.getInstance().stopVideo(mRemoteCanvas);
            }
            mRemoteCanvas = null;
            mVideoLayout.removeAllViews();
            mVideoLayout.setVisibility(View.GONE);
        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_TO_VIDEO) {
            initSurfaceView(false);
            if (mRemoteCanvas == null) {
                mRemoteCanvas = JRMediaDevice.getInstance().startVideo(JRCallStorage.getInstance().getCurItem().getMemberList().get(0).getRenderId(), JRMediaDeviceContancts.VIDEO_CAMERA_FRONT);
                mVideoLayout.addView(mRemoteCanvas.getVideoView());
            }
        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_REQ_VIDEO) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(JRCallActivity.this);
            builder.setTitle("通知");
            builder.setMessage("对方邀请你视频通话，是否接受?");
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    JRCall.getInstance().answerUpdate(item, false);
                }
            });
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    JRCall.getInstance().answerUpdate(item, true);
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
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_VIDEO_STRM_UPDATE) {
            doWithMultiVideoCall(item, null);
        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_TERMED) {

        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_ALERT) {
            mOperationLayer.setStateText(getString(R.string.alerting), false, false);
            mOperationLayer.setStatusIncoming(false, item.isVideo());
        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_HELD) {
            mOperationLayer.setHeld(item.isHeld(), !item.isHeld());
            mOperationLayer.setStateText("被挂起", false, false);
        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_UNHELD) {
            mOperationLayer.setHeld(item.isHeld(), !item.isHeld());
            if (item.getTalkingBeginTime() != 0 && item.getCallState() == JRCallConstants.State.TALKING) {
                long duration = System.currentTimeMillis() - item.getTalkingBeginTime();
                mOperationLayer.setBaseTime(SystemClock.elapsedRealtime() - duration);
                mOperationLayer.startTimer();
            }
        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_HOLD) {
            mOperationLayer.setHold(true, true);
            mOperationLayer.setStateText("挂起", false, false);
            if (mAnotherItem != null) {
                JRCall.getInstance().answer(mAnotherItem, false);
            }
        }
        if (updateType == JRCallConstants.UPDATE_TYPE_CALL_UNHOLD) {
            mOperationLayer.setHold(false, true);
            if (item.getTalkingBeginTime() != 0 && item.getCallState() == JRCallConstants.State.TALKING) {
                long duration = System.currentTimeMillis() - item.getTalkingBeginTime();
                mOperationLayer.setBaseTime(SystemClock.elapsedRealtime() - duration);
                mOperationLayer.startTimer();
            }
        }

        updateCallState(item, isAnotherItem, reason);
        mOperationLayer.setLayoutState(item);

    }

    public void shrinkPreviewSurfaceView(SurfaceView surfaceView, int callId) {
        if (surfaceView == null)
            return;
        MtcNumber localWidth = new MtcNumber();
        MtcNumber localHeight = new MtcNumber();
        MtcCall.Mtc_SessGetVideoLocalSize(callId, localHeight, localWidth);
        if (surfaceView.getWidth() == localWidth.getValue() && surfaceView.getHeight() == localHeight.getValue()) {
            return;
        }
        int screenWidth = JRCommonUtils.getScreenWidth(JRClient.getInstance().getContext());
        int screenHeight = JRCommonUtils.getScreenWidth(JRClient.getInstance().getContext());
        ST_MTC_RECT localRect = JRVideoUtils.calcLocalRect(localWidth.getValue(),
                localHeight.getValue(), screenWidth, screenHeight);
        JRVideoUtils.setViewRect(surfaceView, localRect);
        surfaceView.setZOrderOnTop(true);
        surfaceView.setOnTouchListener(new JRVideoUtils.OnTouchMoveListener());
    }

    public void shrinkMultiSurfaceView(SurfaceView surfaceView, int i) {
        if (surfaceView == null)
            return;
        int screenWidth = JRCommonUtils.getScreenWidth(JRClient.getInstance().getContext());
        int screenHeight = JRCommonUtils.getScreenWidth(JRClient.getInstance().getContext());
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
            JRCall.getInstance().sendDtmf(mCurItem, getJRDtmf(dtmfStr.substring(0, 1)));
        }
    }

    public void onConferenceCellClick(View view) {
        final CallCell callCell = (CallCell) view.getTag();
        if (callCell != null) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage(String.format(getString(R.string.kick_member_message), callCell.mName));
            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String[] num = {callCell.mPhone};
                    JRCall.getInstance().removeMultiCallMember(num[0]);
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

    private int getMtcState(JRCallConstants.MemberState state) {
        if (state == JRCallConstants.MemberState.ALERTING) {
            return JRCommonValue.EN_MTC_CONF_PARTP_STATE_ALERTING;
        } else if (state == JRCallConstants.MemberState.CONNECTED) {
            return JRCommonValue.EN_MTC_CONF_PARTP_STATE_CONNED;
        } else if (state == JRCallConstants.MemberState.CONNECTING) {
            return JRCommonValue.EN_MTC_CONF_PARTP_STATE_PENDING;
        } else if (state == JRCallConstants.MemberState.DIALINGIN) {
            return JRCommonValue.EN_MTC_CONF_PARTP_STATE_DIALINGIN;
        } else if (state == JRCallConstants.MemberState.DIALINGOUT) {
            return JRCommonValue.EN_MTC_CONF_PARTP_STATE_DIALINGOUT;
        } else if (state == JRCallConstants.MemberState.LEAVED) {
            return JRCommonValue.EN_MTC_CONF_PARTP_STATE_DISCED;
        } else if (state == JRCallConstants.MemberState.ENDING) {
            return JRCommonValue.EN_MTC_CONF_PARTP_STATE_DISCING;
        } else if (state == JRCallConstants.MemberState.ONHOLD) {
            return JRCommonValue.EN_MTC_CONF_PARTP_STATE_ONHOLD;
        } else {
            return JRCommonValue.EN_MTC_CONF_PARTP_STATE_PENDING;
        }
    }

    private JRCallConstants.Dtmf getJRDtmf(String dtmf) {
        switch (dtmf) {
            case "0":
                return JRCallConstants.Dtmf.DTMF_0;
            case "1":
                return JRCallConstants.Dtmf.DTMF_1;
            case "2":
                return JRCallConstants.Dtmf.DTMF_2;
            case "3":
                return JRCallConstants.Dtmf.DTMF_3;
            case "4":
                return JRCallConstants.Dtmf.DTMF_4;
            case "5":
                return JRCallConstants.Dtmf.DTMF_5;
            case "6":
                return JRCallConstants.Dtmf.DTMF_6;
            case "7":
                return JRCallConstants.Dtmf.DTMF_7;
            case "8":
                return JRCallConstants.Dtmf.DTMF_8;
            case "9":
                return JRCallConstants.Dtmf.DTMF_9;
            case "*":
                return JRCallConstants.Dtmf.DTMF_STAR;
            case "#":
                return JRCallConstants.Dtmf.DTMF_POUND;
            case "A":
                return JRCallConstants.Dtmf.DTMF_A;
            case "B":
                return JRCallConstants.Dtmf.DTMF_B;
            case "C":
                return JRCallConstants.Dtmf.DTMF_C;
            case "D":
                return JRCallConstants.Dtmf.DTMF_D;
            default:
                return JRCallConstants.Dtmf.DTMF_0;
        }
    }

    private void initSurfaceView(boolean isConf) {
        if (mLocalCanvas == null) {
            JRLog.log("", "initSurView");
            mLocalCanvas = JRMediaDevice.getInstance().startCamera(JRMediaDeviceContancts.VIDEO_CAMERA_FRONT);
//            if (!isConf) {
            mVideoLayout.addView(mLocalCanvas.getVideoView());
//            }
        }
//        if (!isConf) {
        mVideoLayout.setVisibility(View.VISIBLE);
//        }
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

    @Override
    public void onCameraUpdate(boolean b, boolean b1) {
        if (b) {

        }
    }

    @Override
    public void onAudioOutputTypeChange(boolean b) {

    }

    @Override
    public void onRenderStart(SurfaceView surfaceView) {
        if (mRemoteCanvas != null) {
            if (mRemoteCanvas.getVideoView() == surfaceView) {
                shrinkPreviewSurfaceView(mLocalCanvas.getVideoView(), JRCallStorage.getInstance().getCurItem().getCallId());
            }
        }
    }

    private void updateCallByType(JRCallItem item, int updateType) {
        JRLog.log("", "updateType: %d", updateType);
    }

    private void updateCallState(final JRCallItem item, boolean isAnother, JRCallConstants.TremReason reason) {
        switch (item.getCallState()) {
            case INCOMING:
                if (isAnother && mCurItem != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("提示");
                    builder.setMessage("有一路通话接入，是否接听");
                    builder.setCancelable(false);
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            JRCall.getInstance().endCall(item, JRCallConstants.TremReason.BUSY);
                        }
                    });
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mCurItem.isHold()) {
                                JRCall.getInstance().answer(item, false);
                            } else {
                                JRCall.getInstance().hold(mCurItem);
                            }
                        }
                    });
                    builder.create().show();
                }
                break;
            case OUTGOING:
                if (item.isConference()) {
                    if (!item.isVideo()) {
                        mOperationLayer.enterConference();
                    }
                    List<JRCallMember> list = item.getMemberList();
                    for (JRCallMember member : list) {
                        CallCell.addCall(1, member.getNumber(), member.getNumber(), this, getMtcState(member.getState()));
                    }
                }
                if (item.getMemberList().size() > 0) {
                    mOperationLayer.onUserInfoChanged(item.getMemberList().get(0).getDisplayName(), item.getMemberList().get(0).getNumber());
                }
                mOperationLayer.setStateText(getString(R.string.calling), false, false);
                mOperationLayer.setStatusIncoming(false, item.isVideo());
                break;
            case ALERTED:

                break;
            case CONNECTING:

                break;
            case DIDEND:

                break;
            case ENDED:
                if (isAnother) {
                    if (mCurItem == null) {
                        mOperationLayer.setStateText(getString(R.string.ending), false, false);
                        switch (reason) {
                            case FORBIDDEN:
                            case NOT_FOUND:
                            case INTERNAL_ERR:
                            case TEMP_UNAVAIL:
                            case SRV_UNAVAIL:
                            case OTHER:
                                Message msg = mHandler.obtainMessage();
                                msg.what = UPDATE_TO_CS;
                                msg.obj = item.getMemberList().get(0).getNumber();
                                mHandler.sendMessageDelayed(msg, 500);
                                break;
                            default:
                                mHandler.sendEmptyMessageDelayed(UPDATE_END, 1500);
                        }
                        return;
                    } else {
                        mAnotherItem = null;
                        JRCall.getInstance().hold(mCurItem);
                    }
                } else {
                    if (mAnotherItem == null) {
                        mOperationLayer.setStateText(getString(R.string.ending), false, false);
                        switch (reason) {
                            case FORBIDDEN:
                            case NOT_FOUND:
                            case REQ_TERMED:
                            case INTERNAL_ERR:
                            case TEMP_UNAVAIL:
                            case SRV_UNAVAIL:
                                Message msg = mHandler.obtainMessage();
                                msg.what = UPDATE_TO_CS;
                                msg.obj = item.getMemberList().get(0).getNumber();
                                mHandler.sendMessageDelayed(msg, 500);
                                break;
                            default:
                                mHandler.sendEmptyMessageDelayed(UPDATE_END, 1500);
                        }
                        return;
                    } else {
                        mCurItem = null;
                        JRCall.getInstance().hold(mAnotherItem);
                    }
                }
                break;
            case RECEIVED:

                break;
            case TALKING:

                break;
            case IDLE:

                break;
        }
    }

    private void doWithMultiVideoCall(JRCallItem item, String addNumber) {
        mOperationLayer.hideMultiVideoItems();
        mVideoLayout.removeView(mMemberListView);
//        for (int i = 0; i < mMultiVideoLayout.getChildCount(); i++) {
//            View view = mMultiVideoLayout.getChildAt(i);
//            LinearLayout linearLayout = (LinearLayout) view;
//            linearLayout.removeAllViews();
//        }
//        mVideoLayout.removeView(mMultiVideoLayout);
//        mMultiVideoLayout.removeAllViews();
//        JRLog.log("yidao", "member size  " + item.getMemberList().size());
//        for (int i = 0; i < item.getMemberList().size(); i++) {
//            JRCallMember member = item.getMemberList().get(i);
//            if (TextUtils.equals(member.getNumber(), JRClient.getInstance().getCurLoginNumber())) {
//                return;
//            }
//            if (TextUtils.isEmpty(member.getNumber())) {
//                return;
//            }
//            JRMediaDeviceCanvas canvas = JRMediaDevice.getInstance().startVideo(member.getRenderId(), JRMediaDeviceContancts.VIDEO_CAMERA_FRONT);
//            LinearLayout partpLine = (LinearLayout) getLayoutInflater().inflate(R.layout.item_multi_video, null, false);
//            FrameLayout video = partpLine.findViewById(R.id.video_replace);
//            TextView partpTv = partpLine.findViewById(R.id.phone);
//            TextView partpStatus = partpLine.findViewById(R.id.status);
//
//            partpTv.setText(member.getNumber());
//            if (TextUtils.isEmpty(member.getRenderId())) {
//                partpStatus.setText("未连接");
//            } else {
//                partpStatus.setText("已连接");
//            }
//            if (canvas != null) {
//                video.addView(canvas.getVideoView());
//                canvas.getVideoView().setZOrderOnTop(true);
////                canvas.getVideoView().setZOrderMediaOverlay(true);
//            }
//            mMultiVideoLayout.addView(partpLine);
//        }
//        if (!TextUtils.isEmpty(addNumber)) {
//            JRLog.log("yidao","sdad");
//            LinearLayout addLine = (LinearLayout) getLayoutInflater().inflate(R.layout.item_multi_video, null, false);
//            TextView addName = addLine.findViewById(R.id.phone);
//            TextView addStatus = addLine.findViewById(R.id.status);
//            addStatus.setText("未连接");
//            addName.setText(addNumber);
//            mMultiVideoLayout.addView(addLine);
//        }
        if (mAdapter == null) {
            mAdapter = new JRMultiMemberAdapter(this, item.getMemberList(), addNumber);
            mMemberListView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(item.getMemberList(), addNumber);
        }
        mVideoLayout.addView(mMemberListView);
        mMemberListView.bringToFront();
    }
}
