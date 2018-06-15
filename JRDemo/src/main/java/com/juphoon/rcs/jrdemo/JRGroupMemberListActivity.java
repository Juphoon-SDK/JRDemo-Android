package com.juphoon.rcs.jrdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.juphoon.rcs.JRClient;

import common.RealmHelper;
import common.model.RealmGroup;
import common.model.RealmGroupMember;
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
    }

    private void initData() {
        mRealmGroup = mRealm.where(RealmGroup.class).equalTo(RealmGroup.FIELD_SESSIDENTITY, mSessIdentity).findFirst();
        mRealmGroupMembers = mRealm.where(RealmGroupMember.class).equalTo(RealmGroupMember.FIELD_SESSIDENTITY, mSessIdentity).findAll();
        mAdapter = new JRGroupMemberListAdapter(this);
        mAdapter.setData(mRealmGroupMembers, mRealmGroup);
        mGroupMemberRecyclerView.setAdapter(mAdapter);
        mGroupMemberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mIsChairman = TextUtils.equals(mRealmGroup.getChairmanNumber(), JRClient.getInstance().getCurLoginNumber());
    }

    private void updateData() {
        if (!mRealmGroup.isValid() || !mRealmGroupMembers.isValid()) {
            finish();
            return;
        }
        mAdapter.setData(mRealmGroupMembers, mRealmGroup);
        mIsChairman = TextUtils.equals(mRealmGroup.getChairmanNumber(), JRClient.getInstance().getCurLoginNumber());
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
}
