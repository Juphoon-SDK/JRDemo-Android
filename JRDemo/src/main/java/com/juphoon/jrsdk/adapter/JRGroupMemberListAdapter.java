package com.juphoon.jrsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.juphoon.jrsdk.R;
import com.juphoon.jrsdk.model.RealmGroup;
import com.juphoon.jrsdk.model.RealmGroupMember;
import com.juphoon.jrsdk.utils.CommonValue;
import com.juphoon.rcs.jrsdk.JRClient;

import java.util.ArrayList;

import io.realm.RealmResults;

import static android.app.Activity.RESULT_OK;


/**
 * Created by Clive on 2018/4/9.
 */

public class JRGroupMemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private RealmResults<RealmGroupMember> mRealmGroupMembers;
    private String mChairman;
    private RealmGroup mRealmGroup;
    private boolean mIsChairman;
    private boolean mIsAt;

    public JRGroupMemberListAdapter(Context context,boolean isAt) {
        this.mContext = context;
        this.mIsAt = isAt;
    }

    public void setData(RealmResults<RealmGroupMember> mRealmGroupMembers, RealmGroup realmGroup) {
        this.mRealmGroupMembers = mRealmGroupMembers;
        this.mChairman = realmGroup.getChairmanNumber();
        this.mRealmGroup = realmGroup;
        mIsChairman = TextUtils.equals(mRealmGroup.getChairmanNumber(), JRClient.getInstance().getCurrentNumber());
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_group_member, parent, false);
        RecyclerView.ViewHolder holder = new MemberHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final RealmGroupMember realmGroupMember = mRealmGroupMembers.get(position);
        MemberHolder memberHolder = (MemberHolder) holder;
        String member = TextUtils.isEmpty(realmGroupMember.getDisplayName()) ? realmGroupMember.getNumber() : realmGroupMember.getDisplayName();
        if (TextUtils.equals(realmGroupMember.getNumber(), JRClient.getInstance().getCurrentNumber())) {
            member += "-我";
        }
        if (TextUtils.equals(realmGroupMember.getNumber(), mChairman)) {
            member += "-群主";
        }
        memberHolder.name.setText(member);
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mIsChairman) return;
//                if (mIsAt) {
//                    return;
//                }
//                final CharSequence[] items = {"踢出群聊", "转让群主","交换名片"};
//                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                builder.setItems(items, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int item) {
//                        if (item == 0) {
//                            ArrayList<String> phoneBmber = new ArrayList<>();
//                            phoneBmber.add(realmGroupMember.getNumber());
//                            JRGroup.getInstance().kick(RealmGroup.realm2Item(mRealmGroup), phoneBmber);
//                        }
//                        if (item == 1) {
//                            JRGroup.getInstance().modifyChairman(RealmGroup.realm2Item(mRealmGroup), realmGroupMember.getNumber());
//                        }
//                        if (item == 2) {
////                            JRMessage.getInstance().sendTextMessage()
//                        }
//                    }
//                });
//                builder.show();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mRealmGroupMembers.size();
    }

    private class MemberHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;

        public MemberHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.display_name);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
