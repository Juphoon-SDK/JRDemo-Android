package com.juphoon.jrsdk;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.juphoon.jrsdk.adapter.JRGroupMemberListAdapter;
import com.juphoon.jrsdk.listener.RecyclerViewClickListener;
import com.juphoon.jrsdk.model.RealmGroup;
import com.juphoon.jrsdk.model.RealmGroupMember;
import com.juphoon.jrsdk.utils.CommonValue;
import com.juphoon.jrsdk.utils.VCardUtils;
import com.juphoon.rcs.jrsdk.JRClient;
import com.juphoon.rcs.jrsdk.JRGroup;
import com.juphoon.rcs.jrsdk.JRMessage;
import com.juphoon.rcs.jrsdk.JRMessageConstants;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class JRGroupMemberListActivity extends AppCompatActivity {
    private RecyclerView mGroupMemberRecyclerView;
    private Realm mRealm;
    private JRGroupMemberListAdapter mAdapter;
    private RealmResults<RealmGroupMember> mRealmGroupMembers;
    private RealmGroup mRealmGroup;

    private String mSessIdentity;
    private boolean mIsChairman;
    private boolean mIsAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jrgroup_member_list);
        mRealm = RealmHelper.getInstance();
        HandleIntent();
        initViews();
        initData();
        initListener();
    }

    @Override
    protected void onDestroy() {
        mRealmGroup.removeAllChangeListeners();
        mRealmGroupMembers.removeAllChangeListeners();
        mRealm.close();
        super.onDestroy();
    }

    private void HandleIntent() {
        mSessIdentity = getIntent().getStringExtra(RealmGroup.FIELD_SESSIDENTITY);
        mIsAt = getIntent().getBooleanExtra(CommonValue.JRMESSAGE_EXTRA_IS_AT, false);
    }

    private void initData() {
        mRealmGroup = mRealm.where(RealmGroup.class).equalTo(RealmGroup.FIELD_SESSIDENTITY, mSessIdentity).findFirst();
        mRealmGroupMembers = mRealm.where(RealmGroupMember.class).equalTo(RealmGroupMember.FIELD_SESSIDENTITY, mSessIdentity).findAll();
        mAdapter = new JRGroupMemberListAdapter(this, mIsAt);
        mAdapter.setData(mRealmGroupMembers, mRealmGroup);
        mGroupMemberRecyclerView.setAdapter(mAdapter);
        mGroupMemberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mGroupMemberRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this, mGroupMemberRecyclerView,
                mOnItemClickListener));


        mIsChairman = TextUtils.equals(mRealmGroup.getChairmanNumber(), JRClient.getInstance().getCurrentNumber());
    }

    private void updateData() {
        if (!mRealmGroup.isValid() || !mRealmGroupMembers.isValid()) {
            finish();
            return;
        }
        mAdapter.setData(mRealmGroupMembers, mRealmGroup);
        mIsChairman = TextUtils.equals(mRealmGroup.getChairmanNumber(), JRClient.getInstance().getCurrentNumber());
    }

    private void initListener() {
        mRealmGroupMembers.addChangeListener(new RealmChangeListener<RealmResults<RealmGroupMember>>() {
            @Override
            public void onChange(RealmResults<RealmGroupMember> realmGroupMembers) {
                mRealmGroupMembers = realmGroupMembers;
                updateData();
            }
        });
        mRealmGroup.addChangeListener(new RealmChangeListener<RealmGroup>() {
            @Override
            public void onChange(RealmGroup group) {
                mRealmGroup = group;
                updateData();
            }
        });
    }

    private void initViews() {
        mGroupMemberRecyclerView = (RecyclerView) findViewById(R.id.group_member_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mIsAt) {
            menu.add(1, 1, 1, "@所有人");
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 1) {
            Intent intent = new Intent();
            intent.putExtra(CommonValue.JRMESSAGE_EXTRA_IS_AT_ALL, true);
            intent.putExtra(CommonValue.JRMESSAGE_EXTRA_DISPLAY_NAME, "所有人");
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private RecyclerViewClickListener.SimpleOnItemClickListener mOnItemClickListener = new RecyclerViewClickListener.SimpleOnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (mIsAt && !TextUtils.equals(mRealmGroupMembers.get(position).getNumber(), JRClient.getInstance().getCurrentNumber())) {
                Intent intent = new Intent();
                intent.putExtra(CommonValue.JRMESSAGE_EXTRA_DISPLAY_NAME, mRealmGroupMembers.get(position).getDisplayName() + "");
                intent.putExtra(CommonValue.JRMESSAGE_EXTRA_PEERMUMBER, mRealmGroupMembers.get(position).getNumber() + "");
                setResult(RESULT_OK, intent);
                finish();
            }
            if (TextUtils.equals(mRealmGroupMembers.get(position).getNumber(), JRClient.getInstance().getCurrentNumber())) {
                return;
            }
            if (TextUtils.equals(mRealmGroup.getChairmanNumber(), JRClient.getInstance().getCurrentNumber())) {
                final CharSequence[] items = {"踢出群聊", "转让群主", "交换名片"};
                AlertDialog.Builder builder = new AlertDialog.Builder(JRGroupMemberListActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            ArrayList<String> phoneBmber = new ArrayList<>();
                            phoneBmber.add(mRealmGroupMembers.get(position).getNumber());
                            JRGroup.getInstance().kick(RealmGroup.realm2Item(mRealmGroup), phoneBmber);
                        }
                        if (item == 1) {
                            JRGroup.getInstance().modifyChairman(RealmGroup.realm2Item(mRealmGroup), mRealmGroupMembers.get(position).getNumber());
                        }
                        if (item == 2) {
                            String displayName = "未知";
                            for (RealmGroupMember member : mRealmGroupMembers) {
                                if(TextUtils.equals(member.getNumber(),JRClient.getInstance().getCurrentNumber())){
                                    displayName = member.getDisplayName();
                                }
                            }
                            String selfContent = VCardUtils.createSelfVCardContent(mRealmGroup.getSubject(), displayName);
                            HashMap<String, Object> params = new HashMap<>();
                            params.put(JRMessageConstants.ExtraKey.TEXT_CONTENT_TYPE, JRMessageConstants.MessageContentType.EXCHANGE_VCARD);
                            params.put(JRMessageConstants.ExtraKey.PEER_NUMBERS, mRealmGroupMembers.get(position).getNumber());
                            JRMessage.getInstance().sendTextMessage(selfContent, JRMessageConstants.ChannelType.TYPE_ONE, params);
                        }
                    }
                });
                builder.show();
            } else {
                final CharSequence[] items = {"交换名片"};
                AlertDialog.Builder builder = new AlertDialog.Builder(JRGroupMemberListActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            String displayName = mRealmGroup.getSubject();
                            for (RealmGroupMember member : mRealmGroupMembers) {
                                if(TextUtils.equals(member.getNumber(),JRClient.getInstance().getCurrentNumber())){
                                    displayName = member.getDisplayName();
                                }
                            }
                            String selfContent = VCardUtils.createSelfVCardContent(mRealmGroup.getSubject(),displayName);
                            HashMap<String, Object> params = new HashMap<>();
                            params.put(JRMessageConstants.ExtraKey.PEER_NUMBERS, mRealmGroupMembers.get(position).getNumber());
                            params.put(JRMessageConstants.ExtraKey.TEXT_CONTENT_TYPE, JRMessageConstants.MessageContentType.EXCHANGE_VCARD);
                            JRMessage.getInstance().sendTextMessage(selfContent, JRMessageConstants.ChannelType.TYPE_ONE, params);
                        }
                    }
                });
                builder.show();
            }
        }
    };
}
