package com.juphoon.rcs.jrdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.juphoon.rcs.JRCallMember;
import com.juphoon.rcs.JRClient;
import com.juphoon.rcs.JRMediaDevice;
import com.juphoon.rcs.JRMediaDeviceCanvas;
import com.juphoon.rcs.JRMediaDeviceContancts;
import com.juphoon.rcs.MtcUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Upon on 2018/5/30.
 */

public class JRMultiMemberAdapter extends RecyclerView.Adapter<JRMultiMemberAdapter.MemberHolder> {
    private Context mContext;
    private List<JRCallMember> mMembers = new ArrayList<>();
    private ArrayList<JRMediaDeviceCanvas> mCanvas = new ArrayList<>();
    private boolean mReload = false;

    public JRMultiMemberAdapter(Context context, List<JRCallMember> members, String addMember) {
        mContext = context;
        mMembers.clear();
        mMembers.addAll(members);
        if (!TextUtils.isEmpty(addMember)) {
            JRCallMember member = new JRCallMember();
            member.number = addMember;
            mMembers.add(member);
        }
        mCanvas.clear();
        for (int i = 0; i < mMembers.size(); i++) {
            JRMediaDeviceCanvas canvas = JRMediaDevice.getInstance().startVideo(mMembers.get(i).getRenderId(), JRMediaDeviceContancts.VIDEO_CAMERA_FRONT);
            mCanvas.add(canvas);
        }
        mReload = true;
    }

    public void setData(List<JRCallMember> members, String addMember) {
        mMembers.clear();
        boolean add = true;
        mMembers.addAll(members);
        for (JRCallMember member : members) {
            if (addMember != null && TextUtils.equals(MtcUtils.formatPhoneByCountryCode(addMember), member.number)) {
                add = false;
            }
            if (TextUtils.equals(member.getNumber(), JRClient.getInstance().getCurLoginNumber()) || TextUtils.isEmpty(member.getNumber())) {
                mMembers.remove(member);
            }
        }
        if (!TextUtils.isEmpty(addMember) && add) {
            JRCallMember member = new JRCallMember();
            member.number = addMember;
            mMembers.add(member);
        }
        for(JRMediaDeviceCanvas canvas:mCanvas){
            JRMediaDevice.getInstance().stopVideo(canvas);
        }
        mCanvas.clear();
        for (int i = 0; i < mMembers.size(); i++) {
            JRMediaDeviceCanvas canvas = JRMediaDevice.getInstance().startVideo(mMembers.get(i).getRenderId(), JRMediaDeviceContancts.VIDEO_CAMERA_FRONT);
            mCanvas.add(canvas);
        }
        mReload = true;
        notifyDataSetChanged();
    }

    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_multi_video, parent, false);
        JRMultiMemberAdapter.MemberHolder holder = new JRMultiMemberAdapter.MemberHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MemberHolder holder, int position) {
        JRCallMember member = mMembers.get(position);
        JRMediaDeviceCanvas canvas = mCanvas.get(position);
        holder.partpTv.setText(member.getNumber());
        if (mReload) {
            holder.video.removeAllViews();
        }
        if (TextUtils.isEmpty(member.getRenderId())) {
            holder.partpStatus.setText("未连接");
//            holder.video.addView(new View(mContext));
        } else {
            holder.partpStatus.setText("已连接");
            if (mReload) {
                holder.video.addView(canvas.getVideoView());
            }
        }
        if (position == mMembers.size()) {
            mReload = false;
        }
//        if (canvas.getVideoView() == null) {
//            JRLog.log("yidao", "removeVIEW");
//            holder.video.removeAllViews();
//        } else {
//            holder.video.addView(canvas.getVideoView());
//        }
        canvas.getVideoView().setZOrderOnTop(true);
    }

    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    class MemberHolder extends RecyclerView.ViewHolder {
        FrameLayout video;
        TextView partpTv;
        TextView partpStatus;

        public MemberHolder(View itemView) {
            super(itemView);
            video = itemView.findViewById(R.id.video_replace);
            partpTv = itemView.findViewById(R.id.phone);
            partpStatus = itemView.findViewById(R.id.status);
        }
    }
}
