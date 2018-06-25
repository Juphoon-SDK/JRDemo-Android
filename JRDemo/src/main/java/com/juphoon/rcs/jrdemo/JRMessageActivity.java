package com.juphoon.rcs.jrdemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntryHandler;
import com.juphoon.rcs.JRMessage;
import com.juphoon.rcs.JRMessageCallback;
import com.juphoon.rcs.JRMessageContants;
import com.juphoon.rcs.JRMessageItem;
import com.juphoon.rcs.JRThumbnailUtils;
import com.juphoon.rcs.MtcUtils;
import com.juphoon.rcs.utils.JRFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import common.CommonValue;
import common.RealmDataHelper;
import common.RealmHelper;
import common.model.RealmConversation;
import common.model.RealmGroup;
import common.model.RealmMessage;
import common.utils.JRAudioUtils;
import common.utils.JRNumberUtils;
import common.utils.JRVCardUtils;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import ui.RecyclerViewClickListener;

/**
 * Created by Upon on 2018/2/27.
 */

public class JRMessageActivity extends AppCompatActivity implements JRMessageCallback, View.OnClickListener {
    private RealmResults<RealmMessage> mMessageResults;
    private RealmGroup mRealmGroup;
    private ImageView mCamera, mPicture, mLocation, mOtherFile, mShowRecord, mShowInput, mVCard;
    private Button mSend;
    private EditText mEditInput;
    private RecyclerView mMessageList;
    private String peerNumber, mSessionIdentity;
    private JRMessageAdapter mAdapter;
    private Realm mRealm;
    private RelativeLayout mInputLayout, mVoiceLayout;

    //Voice
    private TextView mRecord;
    private View mViewRecord;
    private ImageView mRecordBackgroundImageView;
    private ImageView mRecordStatusImageView;
    private ImageView mRecordLeftImageView;
    private ImageView mRecordRightImageView;
    private TextView mRecordNoticeTextView;
    private TextView mRecordTimeCountTextView;

    private String mTakenFilePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        HandleIntent();
        initViews();
        initListeners();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!TextUtils.isEmpty(mSessionIdentity)) {
            menu.add(1, 1, 1, "群详情");
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void HandleIntent() {
        String account_phone = getIntent().getStringExtra("account_phone");
        if (!TextUtils.isEmpty(account_phone)) {
            if (account_phone.contains(";")) {
                peerNumber = account_phone;
            } else {
                peerNumber = JRNumberUtils.formatPhoneByCountryCode(account_phone);
            }
        }
        mSessionIdentity = getIntent().getStringExtra(RealmConversation.FIELD_SESSIONIDENTITY);
        getSupportActionBar().setTitle(peerNumber);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        JRMessage.getInstance().addCallback(this);
    }

    private void initViews() {
        mSend = (Button) findViewById(R.id.text_send);
        mCamera = (ImageView) findViewById(R.id.btn_camera);
        mPicture = (ImageView) findViewById(R.id.btn_picture);
        mLocation = (ImageView) findViewById(R.id.btn_location);
        mOtherFile = (ImageView) findViewById(R.id.btn_otherfile);
        mVCard = (ImageView) findViewById(R.id.btn_vcard);
        mEditInput = (EditText) findViewById(R.id.edit_text);
        mMessageList = (RecyclerView) findViewById(R.id.recycler_list);
        mInputLayout = (RelativeLayout) findViewById(R.id.layout_input_text);
        mVoiceLayout = (RelativeLayout) findViewById(R.id.layout_input_voice);
        mShowRecord = (ImageView) findViewById(R.id.iv_chat_recording);
        mShowInput = (ImageView) findViewById(R.id.iv_chat_content);

        mRecord = (TextView) findViewById(R.id.tv_chat_record);
        mViewRecord = findViewById(R.id.record_view);
        mRecordBackgroundImageView = (ImageView) findViewById(R.id.record_view_background_view);
        mRecordStatusImageView = (ImageView) findViewById(R.id.voice_record_status);
        mRecordLeftImageView = (ImageView) findViewById(R.id.voice_record_left);
        mRecordRightImageView = (ImageView) findViewById(R.id.voice_record_right);
        mRecordNoticeTextView = (TextView) findViewById(R.id.record_notice);
        mRecordTimeCountTextView = (TextView) mViewRecord.findViewById(R.id.record_time_count);

    }

    private void initListeners() {
        mRecord.setOnTouchListener(mRecordAudioRmsListener);
        mSend.setOnClickListener(this);
        mLocation.setOnClickListener(this);
        mPicture.setOnClickListener(this);
        mCamera.setOnClickListener(this);
        mShowRecord.setOnClickListener(this);
        mShowInput.setOnClickListener(this);
        mOtherFile.setOnClickListener(this);
        mVCard.setOnClickListener(this);
    }

    private void initData() {
        mRealm = RealmHelper.getInstance();
        if (!TextUtils.isEmpty(peerNumber)) {
            mMessageResults = mRealm.where(RealmMessage.class)
                    .equalTo(RealmMessage.FIELD_PEER_PHONE, peerNumber)
                    .findAll();
        }
        if (!TextUtils.isEmpty(mSessionIdentity)) {
            mMessageResults = mRealm.where(RealmMessage.class)
                    .equalTo(RealmMessage.FIELD_SESSIONIDENTITY, mSessionIdentity).findAll();
            mRealmGroup = mRealm.where(RealmGroup.class).equalTo(RealmGroup.FIELD_SESSIDENTITY,
                    mSessionIdentity).findFirst();
            getSupportActionBar().setTitle(mRealmGroup.getSubject() + "");
        }
        mAdapter = new JRMessageAdapter(getApplicationContext(), mMessageResults);
        mMessageList.setAdapter(mAdapter);
        mMessageList.addOnItemTouchListener(new RecyclerViewClickListener(this, mMessageList,
                mOnItemClickListener));
        LinearLayoutManager manager = new LinearLayoutManager(JRMessageActivity.this);
        manager.setStackFromEnd(true);
        mMessageList.setLayoutManager(manager);
        if (mMessageResults != null && mMessageResults.size() > 0) {
            mMessageList.smoothScrollToPosition(mMessageResults.size());
        }
        mMessageResults.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<RealmMessage>>() {
            @Override
            public void onChange(RealmResults<RealmMessage> realmMessages, @javax.annotation.Nullable OrderedCollectionChangeSet changeSet) {
                mAdapter.setData(realmMessages);
                if (changeSet.getInsertions().length > 0) {
                    OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
                    for (OrderedCollectionChangeSet.Range range : insertions) {
                        mAdapter.notifyItemRangeInserted(range.startIndex, range.length);
                    }
                    OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
                    for (OrderedCollectionChangeSet.Range range : modifications) {
                        mAdapter.notifyItemRangeChanged(range.startIndex, range.length);
                    }
                    if (insertions.length > 0) {
                        mMessageList.smoothScrollToPosition(realmMessages.size());
                    }
                }
            }
        });
        if (mRealmGroup != null) {
            mRealmGroup.addChangeListener(new RealmChangeListener<RealmGroup>() {
                @Override
                public void onChange(RealmGroup realmModel) {
                    if (!realmModel.isValid()) {
                        finish();
                    }
                }
            });
        }
    }

    @Override
    public void onTextMessageUpdate(JRMessageItem message) {

    }

    @Override
    public void onFileMessageUpdate(JRMessageItem message) {

    }

    @Override
    public void onGeoMessageUpdate(JRMessageItem message) {

    }

    @Override
    public void onTextMessageReceive(JRMessageItem message) {

    }

    @Override
    public void onFileMessageReceive(JRMessageItem message) {

    }

    @Override
    public void onGeoMessageReceive(JRMessageItem message) {

    }

    @Override
    public void onOfflineMessageReceive(ArrayList<JRMessageItem> arrayList) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.text_send) {
            if (TextUtils.isEmpty(mEditInput.getText().toString())) {
                return;
            }
            JRMessageItem item = null;
            if (mRealmGroup != null) {
                HashMap params = new HashMap<>();
                params.put(CommonValue.GROUP_CHAT_ID, mRealmGroup.getGroupChatId());
                params.put(CommonValue.SESSION_IDENTITY, mRealmGroup.getSessIdentity());
                params.put(CommonValue.SUBJECT, mRealmGroup.getSubject());
                params.put(CommonValue.GROUP_VERSION, mRealmGroup.getGroupVersion() + "");
                item = JRMessage.getInstance().sendTextMessage(JRMessageContants.ChatType.TYPE_GROUP, null, mEditInput.getText().toString(), params);
            } else if (peerNumber.contains(";")) {
                item = JRMessage.getInstance().sendTextMessage(JRMessageContants.ChatType.TYPE_LIST, peerNumber, mEditInput.getText().toString(), null);
            } else {
                item = JRMessage.getInstance().sendTextMessage(JRMessageContants.ChatType.TYPE_ONE, peerNumber, mEditInput.getText().toString(), null);
            }
            mEditInput.setText("");
            if (item != null) {
                RealmDataHelper.insertOrUpdateMessage(mRealm, item);
            }
        } else if (id == R.id.btn_location) {
            startActivityForResult(new Intent(this,
                    LocationBaiduActivity.class), CommonValue.REQUEST_LOCATION);
        } else if (id == R.id.btn_picture) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String[] cameraItems = {"选择视频", "选择照片"};
            builder.setItems(cameraItems, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        chooseVideo();
                    } else {
                        choosePicture();
                    }
                }
            });
            builder.create().show();
        } else if (id == R.id.btn_camera) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String[] cameraItems = {"拍摄视频", "拍摄照片"};
            builder.setItems(cameraItems, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        captureVideo();
                    } else {
                        capturePicture();
                    }
                }
            });
            builder.create().show();
        } else if (id == R.id.btn_otherfile) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setItems()
            Intent intentOther = MtcUtils.getMediaByTypeIntent(
                    "*/*", true);
            if (intentOther != null) {
                startActivityForResult(intentOther,
                        CommonValue.REQUEST_OTHER_FILE);
            }
        } else if (id == R.id.iv_chat_content) {
            mInputLayout.setVisibility(View.VISIBLE);
            mVoiceLayout.setVisibility(View.GONE);
        } else if (id == R.id.iv_chat_recording) {
            mInputLayout.setVisibility(View.GONE);
            mVoiceLayout.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_vcard) {
            selectContact();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == 1) {
            Intent intent = new Intent(JRMessageActivity.this, JRGroupInfoActivity.class);
            intent.putExtra(RealmGroup.FIELD_SESSIDENTITY, mRealmGroup.getSessIdentity());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (mRealm != null) {
            mRealm.close();
        }
        JRAudioUtils.getInstance().stopPlay();
        JRMessage.getInstance().removeCallback(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case CommonValue.REQUEST_LOCATION:
                String locationName = data.getStringExtra(LocationBaiduActivity.LOCATION_NAME);
                double latitude = data.getDoubleExtra(LocationBaiduActivity.LOCATION_LATITUDE, 0.0);
                double longitude = data.getDoubleExtra(LocationBaiduActivity.LOCATION_LONGITUDE, 0.0);
                float radius = data.getFloatExtra(LocationBaiduActivity.LOCATION_RADIUS, 0.0f);
                JRMessageItem item;
                if (mRealmGroup != null) {
                    HashMap params = new HashMap<>();
                    params.put(CommonValue.GROUP_CHAT_ID, mRealmGroup.getGroupChatId());
                    params.put(CommonValue.SESSION_IDENTITY, mRealmGroup.getSessIdentity());
                    params.put(CommonValue.SUBJECT, mRealmGroup.getSubject());
                    params.put(CommonValue.GROUP_VERSION, mRealmGroup.getGroupVersion() + "");
                    item = JRMessage.getInstance().sendGeoMessage(JRMessageContants.ChatType.TYPE_GROUP, null, latitude, longitude, radius, locationName, params);
                } else if (peerNumber.contains(";")) {
                    item = JRMessage.getInstance().sendGeoMessage(JRMessageContants.ChatType.TYPE_LIST, peerNumber, latitude, longitude, radius, locationName, null);
                } else {
                    item = JRMessage.getInstance().sendGeoMessage(JRMessageContants.ChatType.TYPE_ONE, peerNumber, latitude, longitude, radius, locationName, null);
                }

                if (item != null) {
                    RealmDataHelper.insertOrUpdateMessage(mRealm, item);
                }
                break;
            case CommonValue.REQUEST_CHOOSE_PICTURE:
                String srcPath = MtcUtils.getPath(this, data.getData());
                JRMessageItem picItem;
                if (mRealmGroup != null) {
                    HashMap params = new HashMap<>();
                    params.put(CommonValue.GROUP_CHAT_ID, mRealmGroup.getGroupChatId());
                    params.put(CommonValue.SESSION_IDENTITY, mRealmGroup.getSessIdentity());
                    params.put(CommonValue.SUBJECT, mRealmGroup.getSubject());
                    params.put(CommonValue.GROUP_VERSION, mRealmGroup.getGroupVersion() + "");
                    picItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_GROUP, null, JRMessageContants.Type.IMAGE, srcPath, JRThumbnailUtils.getOrCreateThumbnailOfImage(new File(srcPath)), params);
                } else if (peerNumber.contains(";")) {
                    picItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_LIST, peerNumber, JRMessageContants.Type.IMAGE, srcPath, JRThumbnailUtils.getOrCreateThumbnailOfImage(new File(srcPath)), null);
                } else {
                    picItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_ONE, peerNumber, JRMessageContants.Type.IMAGE, srcPath, JRThumbnailUtils.getOrCreateThumbnailOfImage(new File(srcPath)), null);
                }
                if (picItem != null) {
                    RealmDataHelper.insertOrUpdateMessage(mRealm, picItem);
                }
                break;
            case CommonValue.REQUEST_CAMERA_PICTURE:
                String takeThumbPath = JRThumbnailUtils.getOrCreateThumbnailOfImage(new File(mTakenFilePath));
                if (takeThumbPath != null && mTakenFilePath != null) {
                    JRMessageItem cameraItem;
                    if (mRealmGroup != null) {
                        HashMap params = new HashMap<>();
                        params.put(CommonValue.GROUP_CHAT_ID, mRealmGroup.getGroupChatId());
                        params.put(CommonValue.SESSION_IDENTITY, mRealmGroup.getSessIdentity());
                        params.put(CommonValue.SUBJECT, mRealmGroup.getSubject());
                        params.put(CommonValue.GROUP_VERSION, mRealmGroup.getGroupVersion() + "");
                        cameraItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_GROUP, null, JRMessageContants.Type.IMAGE, mTakenFilePath, takeThumbPath, params);
                    } else if (peerNumber.contains(";")) {
                        cameraItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_LIST, peerNumber, JRMessageContants.Type.IMAGE, mTakenFilePath, takeThumbPath, null);
                    } else {
                        cameraItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_ONE, peerNumber, JRMessageContants.Type.IMAGE, mTakenFilePath, takeThumbPath, null);
                    }
                    if (cameraItem != null) {
                        RealmDataHelper.insertOrUpdateMessage(mRealm, cameraItem);
                    }
                }
                break;
            case CommonValue.REQUEST_CAMERA_VIDEO:
                String videoThumbPath = JRThumbnailUtils.getOrCreateThumbnailOfVideo(new File(mTakenFilePath));
                if (videoThumbPath != null && mTakenFilePath != null) {
                    JRMessageItem cameraItem;
                    if (mRealmGroup != null) {
                        HashMap params = new HashMap<>();
                        params.put(CommonValue.GROUP_CHAT_ID, mRealmGroup.getGroupChatId());
                        params.put(CommonValue.SESSION_IDENTITY, mRealmGroup.getSessIdentity());
                        params.put(CommonValue.SUBJECT, mRealmGroup.getSubject());
                        params.put(CommonValue.GROUP_VERSION, mRealmGroup.getGroupVersion() + "");
                        cameraItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_GROUP, null, JRMessageContants.Type.VIDEO, mTakenFilePath, videoThumbPath, params);
                    } else if (peerNumber.contains(";")) {
                        cameraItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_LIST, peerNumber, JRMessageContants.Type.IMAGE, mTakenFilePath, videoThumbPath, null);
                    } else {
                        cameraItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_ONE, peerNumber, JRMessageContants.Type.IMAGE, mTakenFilePath, videoThumbPath, null);
                    }
                    if (cameraItem != null) {
                        RealmDataHelper.insertOrUpdateMessage(mRealm, cameraItem);
                    }
                }
                break;
            case CommonValue.REQUEST_CHOOSE_VIDEO:
                String videoPath = MtcUtils.getPath(this, data.getData());
                JRMessageItem videoItem;
                if (mRealmGroup != null) {
                    HashMap params = new HashMap<>();
                    params.put(CommonValue.GROUP_CHAT_ID, mRealmGroup.getGroupChatId());
                    params.put(CommonValue.SESSION_IDENTITY, mRealmGroup.getSessIdentity());
                    params.put(CommonValue.SUBJECT, mRealmGroup.getSubject());
                    params.put(CommonValue.GROUP_VERSION, mRealmGroup.getGroupVersion() + "");
                    videoItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_GROUP, null, JRMessageContants.Type.VIDEO, videoPath, JRThumbnailUtils.getOrCreateThumbnailOfVideo(new File(videoPath)), params);
                } else if (peerNumber.contains(";")) {
                    videoItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_LIST, peerNumber, JRMessageContants.Type.VIDEO, videoPath, JRThumbnailUtils.getOrCreateThumbnailOfVideo(new File(videoPath)), null);
                } else {
                    videoItem = JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_ONE, peerNumber, JRMessageContants.Type.VIDEO, videoPath, JRThumbnailUtils.getOrCreateThumbnailOfVideo(new File(videoPath)), null);
                }
                if (videoItem != null) {
                    RealmDataHelper.insertOrUpdateMessage(mRealm, videoItem);
                }
                break;
            case CommonValue.REQUEST_OTHER_FILE:
                String path = MtcUtils.getPath(this, data.getData());
                if (mRealmGroup != null) {
                    HashMap params = new HashMap<>();
                    params.put(CommonValue.GROUP_CHAT_ID, mRealmGroup.getGroupChatId());
                    params.put(CommonValue.SESSION_IDENTITY, mRealmGroup.getSessIdentity());
                    params.put(CommonValue.SUBJECT, mRealmGroup.getSubject());
                    params.put(CommonValue.GROUP_VERSION, mRealmGroup.getGroupVersion() + "");
                    JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_GROUP, null, JRMessageContants.Type.OTHER_FILE, path, null, params);
                } else if (peerNumber.contains(";")) {
                    JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_LIST, peerNumber, JRMessageContants.Type.OTHER_FILE, path, null, null);
                } else {
                    JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_ONE, peerNumber, JRMessageContants.Type.OTHER_FILE, path, null, null);
                }
                break;
            case CommonValue.REQUEST_CHOOSE_VCARD:
                Cursor c1 = getContentResolver().query(data.getData(),
                        new String[]{ContactsContract.Contacts._ID}, null, null, null);
                if (c1 != null) {
                    if (c1.moveToFirst()) {
                        sharevCardInfo(c1.getInt(c1.getColumnIndex(ContactsContract.Contacts._ID)));
                    }
                    c1.close();
                }
                break;
        }
    }

    private RecyclerViewClickListener.SimpleOnItemClickListener mOnItemClickListener = new RecyclerViewClickListener.SimpleOnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            int id = view.getId();
            RealmMessage message = mMessageResults.get(position);
            if (id == R.id.image || id == R.id.video_play) {
                if (message.getState() == CommonValue.MESSAGE_STATUS_INVITE) {
                    JRMessage.getInstance().transferFileMessage(message.messageToItem(message));
                } else if (message.isSender() || message.isSuccess()) {
                    if (message.isVideo()) {
                        VideoActivity.startWithPath(JRMessageActivity.this, message.getFilePath());
                    } else if (message.isImage()) {
                        ImageActivity.start(message.getFilePath(), JRMessageActivity.this);
                    }
                }
            } else if (id == R.id.voice_layout) {
                mMediaPlayerCallBack.setData(message, view);
                JRAudioUtils.getInstance().startPlay(message.getFilePath(), mMediaPlayerCallBack);
            } else if (id == R.id.file_layout || id == R.id.file_name) {
                if (message.getState() == CommonValue.MESSAGE_STATUS_INVITE) {
                    JRMessage.getInstance().transferFileMessage(message.messageToItem(message));
                } else {
                    File file = new File(message.getFilePath());
                    if (!file.exists()) {
                        Toast.makeText(JRMessageActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_VIEW);
                    String type = JRFileUtils.getMimeType(file.getName());
                    intent.setDataAndType(Uri.fromFile(file), type);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider.getUriForFile(JRMessageActivity.this, "com.juphoon.rcs.juphoonrcsandroid.fileProvider", file);
                        intent.setDataAndType(contentUri, type);
                    }
                    startActivity(intent);
                }
            } else if (id == R.id.vcard_layout || id == R.id.name || id == R.id.number) {
                if (message.isSuccess() || message.isSender()) {
                    final Uri uri = Uri.fromFile(new File(message.getFilePath()));
                    JRVCardUtils.parseVCard(JRMessageActivity.this, uri, new VCardEntryHandler() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onEntryCreated(VCardEntry vCardEntry) {
                            long rawContactId = JRVCardUtils.importToContact(JRMessageActivity.this, uri);
                            if (rawContactId != -1) {
                                long contactId = JRVCardUtils.getContactIdWithRawContactId(JRMessageActivity.this, rawContactId);
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
                                startActivity(i);
                            } else {
                                Toast.makeText(JRMessageActivity.this, "联系人读取失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onEnd() {
                        }
                    });
                }
            }
        }
    };


    /**** 录音相关 ****/
    private final View.OnTouchListener mRecordAudioRmsListener = new View.OnTouchListener() {

        private final JRAudioUtils mAudioRecorder = JRAudioUtils.getInstance();
        private String mFileName;
        private long startTimeStamp = -1;
        private AnimationDrawable mRecordLeft;
        private AnimationDrawable mRecordRight;
        private boolean isRecording = false;
        private boolean isMessageNeedSend = false;
        private boolean isCountdown = false;
        private int[] mHoldButtonLocation = new int[2];

        private int mCurrentTimeCount;
        private Timer mTimer;
        private TimerTask mTimerTask;

        private final static int MAX_AUDIO_MESSAGE_LENGTH = 180;


        private void vibrate() {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(50);
        }

        @Override
        public boolean onTouch(View arg0, MotionEvent ev) {
            JRAudioUtils.getInstance().stopPlay();
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                mRecord.setText(getString(R.string.un_touch_to_send));
                isMessageNeedSend = true;
                isCountdown = false;
                mRecordLeftImageView.setVisibility(View.VISIBLE);
                mRecordRightImageView.setVisibility(View.VISIBLE);
                mRecordTimeCountTextView.setVisibility(View.VISIBLE);
                mRecordStatusImageView.setVisibility(View.VISIBLE);
                mRecordBackgroundImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_background));
                mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_voice));
                mRecordTimeCountTextView.setText("00:00");
                mRecordNoticeTextView.setText(R.string.swipe_up_will_cancel_send_audio_message);
                arg0.getLocationInWindow(mHoldButtonLocation);
                startTimerTask();
                mCurrentTimeCount = 0;

                if (System.currentTimeMillis() - startTimeStamp >= 1000) {
                    mViewRecord.setVisibility(View.VISIBLE);
                    vibrate();

                    ImageView leftView = (ImageView) mViewRecord.findViewById(R.id.voice_record_left);
                    leftView.setImageResource(R.drawable.animation_voice_record_left);
                    mRecordLeft = (AnimationDrawable) leftView.getDrawable();
                    mRecordLeft.start();

                    ImageView RightView = (ImageView) mViewRecord.findViewById(R.id.voice_record_right);
                    RightView.setImageResource(R.drawable.animation_voice_record_right);
                    mRecordRight = (AnimationDrawable) RightView.getDrawable();
                    mRecordRight.start();

                    mFileName = MtcUtils.getSdcardPath(JRMessageActivity.this) + "/" + MtcUtils.getAppName(JRMessageActivity.this) + "/" + "temp/" + System.currentTimeMillis() + ".amr";
                    mAudioRecorder.startRecord(mFileName);
                    startTimeStamp = System.currentTimeMillis();
                    isRecording = true;
                }
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                mRecord.setText(getString(R.string.touch_to_record));
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                if (isRecording) {
                    if (mRecordLeft != null) {
                        mRecordLeft.stop();
                    }
                    if (mRecordRight != null) {
                        mRecordRight.stop();
                    }
                    isRecording = false;
                    isCountdown = false;
                    mCurrentTimeCount = 0;
                    // 向上滑动后取消发送
                    if (!isMessageNeedSend) {
                        mViewRecord.setVisibility(View.INVISIBLE);
                        mAudioRecorder.stopRecord();
                    } else if (System.currentTimeMillis() - startTimeStamp >= 1000) {
                        mAudioRecorder.stopRecord();
                        mViewRecord.setVisibility(View.INVISIBLE);
                        if (mRealmGroup != null) {
                            HashMap params = new HashMap<>();
                            params.put(CommonValue.GROUP_CHAT_ID, mRealmGroup.getGroupChatId());
                            params.put(CommonValue.SESSION_IDENTITY, mRealmGroup.getSessIdentity());
                            params.put(CommonValue.SUBJECT, mRealmGroup.getSubject());
                            params.put(CommonValue.GROUP_VERSION, mRealmGroup.getGroupVersion() + "");
                            JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_GROUP, null, JRMessageContants.Type.AUDIO, mFileName, null, params);
                        } else if (peerNumber.contains(";")) {
                            JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_LIST, peerNumber, JRMessageContants.Type.AUDIO, mFileName, null, null);
                        } else {
                            JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_ONE, peerNumber, JRMessageContants.Type.AUDIO, mFileName, null, null);
                        }
                        vibrate();
                    } else {
                        // 录音时间太短
                        mRecordBackgroundImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_background));
                        mRecordLeftImageView.setVisibility(View.INVISIBLE);
                        mRecordRightImageView.setVisibility(View.INVISIBLE);
                        mRecordTimeCountTextView.setVisibility(View.INVISIBLE);
                        mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_too_short));
                        mRecordTimeCountTextView.setVisibility(View.INVISIBLE);
                        mRecordNoticeTextView.setText(R.string.audio_length_too_short);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mViewRecord.setVisibility(View.INVISIBLE);
                                mAudioRecorder.stopRecord();
                            }
                        }, 1000);
                    }
                }
            } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                float positionY = Math.abs(ev.getRawY());
                if (positionY < mHoldButtonLocation[1]) {
                    if (!isCountdown) {
                        mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel_record_status));
                    }
                    mRecordNoticeTextView.setText(R.string.touch_up_will_cancel_send_audio_message);
                    mRecord.setText(getString(R.string.un_touch_to_cancel));
                    mRecordLeftImageView.setVisibility(View.INVISIBLE);
                    mRecordRightImageView.setVisibility(View.INVISIBLE);
                    mRecordTimeCountTextView.setVisibility(View.INVISIBLE);
                    mRecordBackgroundImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel_record_background));
                    isMessageNeedSend = false;
                } else {
                    if (!isCountdown) {
                        mRecordLeftImageView.setVisibility(View.VISIBLE);
                        mRecordRightImageView.setVisibility(View.VISIBLE);
                        mRecordTimeCountTextView.setVisibility(View.VISIBLE);
                        mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_voice));
                    }
                    mRecordBackgroundImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_background));
                    mRecordNoticeTextView.setText(R.string.swipe_up_will_cancel_send_audio_message);
                    mRecord.setText(getString(R.string.swipe_up_will_cancel_send_audio_message));
                    isMessageNeedSend = true;
                }
            }
            return true;
        }

        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int timeCount = msg.what;
                if (timeCount <= MAX_AUDIO_MESSAGE_LENGTH - 10) {
                    String minutes = String.valueOf(timeCount / 60);
                    String seconds = String.valueOf(timeCount % 60);
                    if (seconds.length() < 2) {
                        seconds = "0" + seconds;
                    }
                    mRecordTimeCountTextView.setText("0" + minutes + ":" + seconds);
                } else if (timeCount <= MAX_AUDIO_MESSAGE_LENGTH) {
                    isCountdown = true;
                    mRecordTimeCountTextView.setVisibility(View.INVISIBLE);
                    mRecordLeftImageView.setVisibility(View.INVISIBLE);
                    mRecordRightImageView.setVisibility(View.INVISIBLE);
                    switch (MAX_AUDIO_MESSAGE_LENGTH - timeCount) {
                        case 0:
                            mRecordStatusImageView.setVisibility(View.INVISIBLE);
                        case 1:
                            mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_countdown_1));
                            break;
                        case 2:
                            mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_countdown_2));
                            break;
                        case 3:
                            mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_countdown_3));
                            break;
                        case 4:
                            mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_countdown_4));
                            break;
                        case 5:
                            mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_countdown_5));
                            break;
                        case 6:
                            mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_countdown_6));
                            break;
                        case 7:
                            mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_countdown_7));
                            break;
                        case 8:
                            mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_countdown_8));
                            break;
                        case 9:
                            mRecordStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_countdown_9));
                            break;
                    }
                } else {
                    mAudioRecorder.stopRecord();
                }
                startTimerTask();
            }
        };

        private void startTimerTask() {
            if (mTimer == null) {
                mTimer = new Timer();
            }
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(++mCurrentTimeCount);
                }
            };
            mTimer.schedule(mTimerTask, 1000);
        }

    };


    private final JRAudioUtils.AudioPlayerListener mMediaPlayerCallBack = new JRAudioUtils.AudioPlayerListener() {

        AnimationDrawable animationDrawable;
        RealmMessage mMessage;
        View mAudioView;

        @Override
        public void setData(RealmMessage message, View audioView) {
            mMessage = message;
            mAudioView = audioView;
        }

        @SuppressLint("ResourceType")
        @Override
        public void onStartPlay() {
            ImageView audioPlayView = (ImageView) mAudioView.findViewById(R.id.paly_voice);
            if (!mMessage.isSender()) {
                audioPlayView.setImageResource(R.drawable.animation_voice_play_left);
            } else {
                audioPlayView.setImageResource(R.drawable.animation_voice_play_right);
            }
            animationDrawable = (AnimationDrawable) audioPlayView.getDrawable();
            animationDrawable.start();
        }

        @Override
        public void onStopPlay() {
            if (animationDrawable != null) {
                animationDrawable.stop();
            }
            ImageView audioPlayView = (ImageView) mAudioView.findViewById(R.id.paly_voice);
            if (!mMessage.isSender()) {
                audioPlayView.setImageResource(R.drawable.voice_three_3);
            } else {
                audioPlayView.setImageResource(R.drawable.voice_2_three_3);
            }
        }

    };


    /***** 拍摄选择图片文件 *****/
    private static final int[] sVideoDuration = new int[]{0, 5, 10, 15, 20,
            30, 40, 50, 60, 90, 120};

    private static int getVideoCaptureDurationLimit(long bytesAvailable) {
        CamcorderProfile camcorder = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        if (camcorder == null) {
            return 0;
        }
        bytesAvailable *= 8;        // convert to bits
        long seconds = bytesAvailable / (camcorder.audioBitRate + camcorder.videoBitRate);

        // Find the best match for one of the fixed durations
        for (int i = sVideoDuration.length - 1; i >= 0; i--) {
            if (seconds >= sVideoDuration[i]) {
                return sVideoDuration[i];
            }
        }
        return 0;
    }

    private void capturePicture() {
        mTakenFilePath = MtcUtils.getSdcardPath(this) + "/" + MtcUtils.getAppName(this) + "/temp/" + System.currentTimeMillis() + ".png";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(mTakenFilePath);
        if (file.exists()) {
            file.delete();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.juphoon.rcs.juphoonrcsandroid.fileProvider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }
        startActivityForResult(intent, CommonValue.REQUEST_CAMERA_PICTURE);
    }

    private void captureVideo() {
        mTakenFilePath = MtcUtils.getSdcardPath(this) + "/" + MtcUtils.getAppName(this) + "/temp/" + System.currentTimeMillis() + ".mp4";
        long sizeLimit = 10 * 1024 * 1024;
        sizeLimit *= .85F;
        int durationLimit = getVideoCaptureDurationLimit(sizeLimit);
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, sizeLimit);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationLimit);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.juphoon.rcs.juphoonrcsandroid.fileProvider", new File(mTakenFilePath));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mTakenFilePath)));
        }
        if (intent != null) {
            startActivityForResult(intent,
                    CommonValue.REQUEST_CAMERA_VIDEO);
        }
    }

    private void choosePicture() {
        Intent chooseImageIntent = MtcUtils
                .getMediaByTypeIntent("image/*", false);
        if (chooseImageIntent != null) {
            startActivityForResult(chooseImageIntent,
                    CommonValue.REQUEST_CHOOSE_PICTURE);
        }
    }

    private void chooseVideo() {
        Intent chooseVideoIntent = MtcUtils
                .getMediaByTypeIntent("video/*", true);
        if (chooseVideoIntent != null) {
            startActivityForResult(chooseVideoIntent,
                    CommonValue.REQUEST_CHOOSE_VIDEO);
        }
    }

    private void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CommonValue.REQUEST_CHOOSE_VCARD);
    }

    private void sharevCardInfo(int contactId) {
        final String filePath = MtcUtils.getSdcardPath(this) + "/" + MtcUtils.getAppName(this) + "/temp/" + contactId + ".vcf";
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        final Uri vcfUri = Uri.fromFile(file);
        ArrayList<Integer> listId = new ArrayList<Integer>();
        listId.add(contactId);
        JRVCardUtils.exportVCard(this, vcfUri, listId);
        if (mRealmGroup != null) {
            HashMap params = new HashMap<>();
            params.put(CommonValue.GROUP_CHAT_ID, mRealmGroup.getGroupChatId());
            params.put(CommonValue.SESSION_IDENTITY, mRealmGroup.getSessIdentity());
            params.put(CommonValue.SUBJECT, mRealmGroup.getSubject());
            params.put(CommonValue.GROUP_VERSION, mRealmGroup.getGroupVersion() + "");
            JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_GROUP, null, JRMessageContants.Type.VCARD, filePath, null, params);
        } else if (peerNumber.contains(";")) {
            JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_LIST, peerNumber, JRMessageContants.Type.VCARD, filePath, null, null);
        } else {
            JRMessage.getInstance().sendFileMessage(JRMessageContants.ChatType.TYPE_ONE, peerNumber, JRMessageContants.Type.VCARD, filePath, null, null);
        }
    }
}
