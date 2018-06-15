package com.juphoon.rcs.jrdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.juphoon.rcs.JRGroup;

import common.RealmHelper;
import common.model.RealmConversation;
import common.model.RealmGroup;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import ui.RecyclerViewClickListener;

public class JRGroupListActivity extends AppCompatActivity {
    private RecyclerView mGroupRecyclerView;
    private Realm mRealm;
    private JRGroupListAdapter mAdapter;
    private RealmResults<RealmGroup> mRealmGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jrgroup_list);
        mRealm = RealmHelper.getInstance();
        initViews();
        initData();
        initListener();
        JRGroup.getInstance().subscribeGroupList();
    }

    @Override
    protected void onDestroy() {
        mRealmGroups.removeAllChangeListeners();
        mRealm.close();
        super.onDestroy();
    }

    private void initViews() {
        mGroupRecyclerView = (RecyclerView) findViewById(R.id.group_list);
    }

    private void initListener() {
        mGroupRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this, mGroupRecyclerView, mOnItemClickListener));
        mRealmGroups.addChangeListener(new RealmChangeListener<RealmResults<RealmGroup>>() {
            @Override
            public void onChange(RealmResults<RealmGroup> realmGroups) {
                mRealmGroups = realmGroups;
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initData() {
        mRealmGroups = mRealm.where(RealmGroup.class).findAll();
        mAdapter = new JRGroupListAdapter(this, mRealmGroups);
        mGroupRecyclerView.setAdapter(mAdapter);
        mGroupRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private RecyclerViewClickListener.SimpleOnItemClickListener mOnItemClickListener = new RecyclerViewClickListener.SimpleOnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            RealmGroup realmGroup = mRealmGroups.get(position);
//            Intent intent = new Intent(JRGroupListActivity.this, JRMessageActivity.class);
//            intent.putExtra(RealmConversation.FIELD_SESSIONIDENTITY, realmGroup.getSessIdentity());
//            startActivity(intent);
        }
    };

}
