package com.juphoon.jrsdk.call;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Chronometer;
import android.widget.TextView;


import com.juphoon.cmcc.app.lemon.MtcConfConstants;
import com.juphoon.jrsdk.R;

import java.util.ArrayList;
import java.util.List;

public class CallConferenceAdapter extends BaseAdapter {
    private ArrayList<Integer> isConList = new ArrayList<>();

    public CallConferenceAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<CallCell> list) {
        mCallCellList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCallCellList == null ? 0 : mCallCellList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCallCellList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CallCell callCell = (CallCell) getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.call_conference_item, null);
        }
        callCell.mChrState = (Chronometer) convertView.findViewById(R.id.call_state);
        callCell.mTxtName = (TextView) convertView.findViewById(R.id.call_name);
        convertView.setTag(callCell);

        callCell.mTxtName.setText(callCell.mName);
        if (callCell.mSessState == MtcConfConstants.EN_MTC_CONF_PARTP_STATE_PENDING || callCell.mSessState == MtcConfConstants.EN_MTC_CONF_PARTP_STATE_DIALINGOUT) {
            callCell.mChrState.stop();
            callCell.mChrState.setText(mContext.getResources().getString(R.string.connecting));
        }
//        if (callCell.mSessState == JRCommonValue.EN_MTC_CONF_PARTP_STATE_CONNED) {
//            if (callCell.mBase == 0) {
//                callCell.mBase = SystemClock.elapsedRealtime();
//            }
//            callCell.mChrState.setBase(callCell.mBase);
//            callCell.mChrState.start();
//        }  else if (callCell.mSessState == JRCommonValue.EN_MTC_CONF_PARTP_STATE_DISCED || callCell.mSessState == JRCommonValue.EN_MTC_CONF_PARTP_STATE_DISCING) {
//            callCell.mChrState.stop();
//            callCell.removeDisconnected(callCell.mPhone);
//        } else {
//        }
        return convertView;
    }

    private Context mContext;
    private LayoutInflater mInflater;
    private List<CallCell> mCallCellList;


}
