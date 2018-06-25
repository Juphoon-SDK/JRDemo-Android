package com.juphoon.rcs.jrdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import common.CommonValue;
import common.model.CallLogs;
import common.model.RealmCallLog;
import common.utils.JRDateUtils;

/**
 * Created by Upon on 2018/4/24.
 */

public class JRCallLogAdapter extends RecyclerView.Adapter<JRCallLogAdapter.CallLogHolder> {
    private Context mContext;
    private ArrayList<CallLogs> mCallLogs;

    public JRCallLogAdapter(Context context, ArrayList<CallLogs> callLogs) {
        mContext = context;
        mCallLogs = callLogs;
    }

    @Override
    public CallLogHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.calllog_item, parent, false);
        return new CallLogHolder(v);
    }

    @Override
    public void onBindViewHolder(CallLogHolder holder, int position) {
        final CallLogs callLog = mCallLogs.get(position);
        RealmCallLog realmCallLog = callLog.callLogs.get(0);
        holder.date.setText(JRDateUtils.getTimeStringX(mContext, realmCallLog.getStartTime(), false));
        if (callLog.callLogs.size() > 1) {
            holder.name.setText(realmCallLog.getPerrNumber() + "(" + callLog.callLogs.size() + ")");
        } else {
            holder.name.setText(realmCallLog.getPerrNumber());
        }
        if (realmCallLog.getTalkingTime() == 0) {
            if (realmCallLog.getCallStatus() == RealmCallLog.CALL_STATE_INCOMING) {
                holder.time.setText("未接听");
            } else {
                holder.time.setText("已取消");
            }
        } else {
            holder.time.setText(JRDateUtils.getSecondTimestamp(realmCallLog.getEndTime() - realmCallLog.getTalkingTime()) + "秒");

        }

        if (realmCallLog.isVideo()) {
            if (realmCallLog.isIncoming()) {
                if (realmCallLog.isMissed()) {
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.recents_videoin_missed);
                    drawable.setColorFilter(mContext.getResources().getColor(R.color.missed), PorterDuff.Mode.SRC_ATOP);
                    holder.callType.setImageDrawable(drawable);

                } else {
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.recents_videoin);
                    drawable.setColorFilter(mContext.getResources().getColor(R.color.base), PorterDuff.Mode.SRC_ATOP);
                    holder.callType.setImageDrawable(drawable);
                }
            } else {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.recents_videoout);
                drawable.setColorFilter(mContext.getResources().getColor(R.color.base), PorterDuff.Mode.SRC_ATOP);
                holder.callType.setImageDrawable(drawable);
            }
        } else {
            if (realmCallLog.isIncoming()) {
                if (realmCallLog.isMissed()) {
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.recents_voicein_missed);
                    drawable.setColorFilter(mContext.getResources().getColor(R.color.missed), PorterDuff.Mode.SRC_ATOP);
                    holder.callType.setImageDrawable(drawable);
                } else {
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.recents_voicein);
                    drawable.setColorFilter(mContext.getResources().getColor(R.color.base), PorterDuff.Mode.SRC_ATOP);
                    holder.callType.setImageDrawable(drawable);
                }
            } else {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.recents_voiceout);
                drawable.setColorFilter(mContext.getResources().getColor(R.color.base), PorterDuff.Mode.SRC_ATOP);
                holder.callType.setImageDrawable(drawable);
            }
        }

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, JRInfoActivity.class);
                intent.putParcelableArrayListExtra(CommonValue.EXTRA_LOGS, callLog.callLogs);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCallLogs.size();
    }

    class CallLogHolder extends RecyclerView.ViewHolder {
        TextView name, time, date;
        ImageView callType;
        RelativeLayout itemLayout;
        String mPhone;

        public CallLogHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.recent_item_name);
            time = (TextView) itemView.findViewById(R.id.recent_item_time);
            date = (TextView) itemView.findViewById(R.id.recent_item_date);
            callType = (ImageView) itemView.findViewById(R.id.recent_item_type);
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.item_layout);
        }
    }
}
