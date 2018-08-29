package com.juphoon.jrsdk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.juphoon.jrsdk.R;
import com.juphoon.jrsdk.model.RealmGroup;

import io.realm.RealmResults;


/**
 * Created by Clive on 2018/4/11.
 */

public class JRGroupListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private RealmResults<RealmGroup> mRealmGroups;

    public JRGroupListAdapter(Context mContext, RealmResults<RealmGroup> mRealmGroups) {
        this.mContext = mContext;
        this.mRealmGroups = mRealmGroups;
    }

    public void setData(RealmResults<RealmGroup> mRealmGroups) {
        this.mRealmGroups = mRealmGroups;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_group, parent, false);
        RecyclerView.ViewHolder holder = new GroupHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RealmGroup realmGroup = mRealmGroups.get(position);
        GroupHolder groupHolder = (GroupHolder) holder;
        groupHolder.name.setText(realmGroup.getSubject());
    }

    @Override
    public int getItemCount() {
        return mRealmGroups.size();
    }


    private class GroupHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;

        public GroupHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.group_name);
            icon = itemView.findViewById(R.id.group_icon);
        }
    }
}
