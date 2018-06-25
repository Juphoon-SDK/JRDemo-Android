package com.juphoon.rcs.jrdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juphoon.rcs.JRCall;
import com.juphoon.rcs.JRCallMember;
import com.juphoon.rcs.JRClient;
import com.juphoon.rcs.JRLog;
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
    private ArrayList<JRMediaDeviceCanvas> mDelCanvas = new ArrayList<>();
    private boolean mReload = false;
    private boolean mSender = false;

    public JRMultiMemberAdapter(Context context, List<JRCallMember> members, String addMember, boolean sender) {
        mContext = context;
        mMembers.clear();
        mMembers.addAll(members);
        if (!TextUtils.isEmpty(addMember)) {
            JRCallMember member = new JRCallMember();
            member.number = addMember;
            mMembers.add(member);
        }
        mSender = sender;
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

        for (JRMediaDeviceCanvas canvas : mCanvas) {
            canvas.mActive = false;
        }
        for (int i = 0; i < mMembers.size(); i++) {
            boolean canAdd = true;
            String renderId = mMembers.get(i).getRenderId();
            for (int j = 0; j < mCanvas.size(); j++) {
                if (TextUtils.equals(mCanvas.get(j).mRenderId, renderId)) {
                    canAdd = false;
                    mCanvas.get(j).mActive = true;
                }
            }
            if (canAdd) {
                JRMediaDeviceCanvas canvas = JRMediaDevice.getInstance().startVideo(renderId, JRMediaDeviceContancts.VIDEO_CAMERA_FRONT);
                canvas.mActive = true;
                mCanvas.add(canvas);
            }
        }
        mDelCanvas.clear();
        for (JRMediaDeviceCanvas canvas : mCanvas) {
            if (!canvas.mActive) {
                mDelCanvas.add(canvas);
            }
        }
        for (JRMediaDeviceCanvas canvas : mDelCanvas) {
            JRMediaDevice.getInstance().stopVideo(canvas);
        }
        mCanvas.removeAll(mDelCanvas);
        mReload = true;
        notifyDataSetChanged();
    }

    public void clearData() {
        for (JRMediaDeviceCanvas canvas : mCanvas) {
            JRMediaDevice.getInstance().stopVideo(canvas);
        }
    }

    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_multi_video, parent, false);
        JRMultiMemberAdapter.MemberHolder holder = new JRMultiMemberAdapter.MemberHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MemberHolder holder, int position) {
        final JRCallMember member = mMembers.get(position);
        JRMediaDeviceCanvas deviceCanvas = null;
        for (JRMediaDeviceCanvas canvas : mCanvas) {
            if (TextUtils.equals(canvas.mRenderId, member.getRenderId())) {
                deviceCanvas = canvas;
            }
        }
        holder.partpTv.setText(member.getNumber());
        if (mReload) {
            holder.video.removeAllViews();
        }
        if (TextUtils.isEmpty(member.getRenderId())) {
            holder.partpStatus.setText("未连接");
//            holder.video.addView(new View(mContext));
        } else {
            holder.partpStatus.setText("已连接");
            if (mReload && deviceCanvas != null && deviceCanvas.getVideoView() != null) {
                holder.video.addView(deviceCanvas.getVideoView());
            }
        }
        if (position == mMembers.size()) {
            mReload = false;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JRLog.log("yidao", "click");
                if (!mSender) {
                    return;
                }
                if (TextUtils.isEmpty(member.getRenderId())) {
                    showDialog(true, member.getNumber());
                } else {
                    showDialog(false, member.getNumber());
                }
            }
        });
        if (deviceCanvas != null && deviceCanvas.getVideoView() != null) {
            deviceCanvas.getVideoView().setZOrderOnTop(true);
        }
    }

    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    class MemberHolder extends RecyclerView.ViewHolder {
        FrameLayout video;
        LinearLayout item;
        TextView partpTv;
        TextView partpStatus;

        public MemberHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item_view);
            video = itemView.findViewById(R.id.video_replace);
            partpTv = itemView.findViewById(R.id.phone);
            partpStatus = itemView.findViewById(R.id.status);
        }
    }

    private void showDialog(final boolean invite, final String phone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        if (invite) {
            builder.setTitle("是否邀请" + phone + "进入多方会议?");
        } else {
            builder.setTitle("是否将" + phone + "踢出多方会议?");
        }
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (invite) {
                    JRCall.getInstance().addMultiCallMember(phone);
                } else {
                    JRCall.getInstance().removeMultiCallMember(phone);
                }
            }
        });
        builder.create().show();
    }
}
