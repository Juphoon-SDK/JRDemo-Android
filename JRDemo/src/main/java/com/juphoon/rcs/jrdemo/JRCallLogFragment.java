package com.juphoon.rcs.jrdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juphoon.rcs.JRLog;

import java.util.ArrayList;

import common.RealmHelper;
import common.model.CallLogs;
import common.model.RealmCallLog;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Upon on 2018/4/23.
 */

public class JRCallLogFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private Realm mRealm;
    private RealmResults<RealmCallLog> mRealmCallLogs;
    private ArrayList<CallLogs> mCallLogs = new ArrayList<>();
    private JRCallLogAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_recycler_list, null);
        mRecyclerView = view.findViewById(R.id.recycler_list);
        mRealm = RealmHelper.getInstance();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRealmCallLogs = mRealm.where(RealmCallLog.class).findAllSorted(RealmCallLog.FILED_START_TIME, Sort.DESCENDING);
        dealWithCallLogs();
        mAdapter = new JRCallLogAdapter(getActivity(), mCallLogs);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRealmCallLogs.addChangeListener(new RealmChangeListener<RealmResults<RealmCallLog>>() {
            @Override
            public void onChange(RealmResults<RealmCallLog> callLogs) {
                dealWithCallLogs();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void dealWithCallLogs() {
        mCallLogs.clear();
        for (int i = 0; i < mRealmCallLogs.size(); i++) {
            CallLogs logs = new CallLogs();
            RealmCallLog curLog = mRealmCallLogs.get(i);
            RealmCallLog lastLog = null;
            if (i > 0) {
                lastLog = mRealmCallLogs.get(i - 1);
            }
            if (lastLog == null) {
                logs.callLogs.add(curLog);
                mCallLogs.add(logs);
            } else {
                if (TextUtils.equals(curLog.getPerrNumber(), lastLog.getPerrNumber())) {
                    logs = mCallLogs.get(mCallLogs.size() - 1);
                    logs.callLogs.add(curLog);
                } else {
                    logs.callLogs.add(curLog);
                    mCallLogs.add(logs);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        mRealm.close();
        mRealmCallLogs.removeAllChangeListeners();
        super.onDestroy();
    }
}
