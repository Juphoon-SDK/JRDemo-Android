package ui.call;

import android.app.Dialog;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;

import com.juphoon.cmcc.app.lemon.MtcCall;
import com.juphoon.cmcc.app.lemon.MtcCallConstants;
import com.juphoon.rcs.jrdemo.JRCommonValue;
import com.juphoon.rcs.jrdemo.MainApplication;
import com.juphoon.rcs.jrdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peterwang on 6/23/15.
 */
public class CallCell {

    public static final int PRIORITY_TALKING = 5;
    public static final int PRIORITY_RINGING = 4;
    public static final int PRIORITY_CONNECTING = 3;
    public static final int PRIORITY_CALLING = 2;
    public static final int PRIORITY_ANSWERING = 1;
    public static final int PRIORITY_END = 0;

    public interface Callback {
        void callCellDidEndConference(CallCell callCell);

        void callCellNeedsReloadData(CallCell callCell);

        void callCellStateChanged(CallCell callCell);
    }

    public static List<CallCell> sCallArray;

    private static final int MSG_CALL_DISCONNECTED = 100;

    private static final android.os.Handler sHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CALL_DISCONNECTED:
                    ((CallCell) msg.obj).callDisconnected();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private boolean mDisconnectedTiming;
    private long mConnectingDate;
    private boolean mRtpConnected;
    private boolean mReconnecting;
    private boolean mPaused;
    private boolean mPausedByCS;
    private Dialog mAlertDialog;

    public int mSessId;
    public int mContactId;
    public String mPhone;
    public int mSessState;
    public Chronometer mChrState;
    public String mBaseText;

    public Callback mCallback;

    public TextView mTxtName;
    public TextView mTxtError;

    public String mStateText = "";
    public boolean mAnimated = false;
    private String mErrorText = "";
    public long mBase = 0;
    public String mName = "";

    private int mAudioNetSta = MtcCallConstants.EN_MTC_NET_STATUS_NORMAL;

    private int mDirection = 0;


    public static boolean isConference() {
        return sCallArray != null && sCallArray.size() > 0;
    }

    public static List<CallCell> getCallArray() {
        return sCallArray;
    }

    public static List<CallCell> copyList() {
        if (sCallArray == null) {
            return null;
        }
        List<CallCell> list = new ArrayList<>();
        for (CallCell callCell : sCallArray) {
            list.add(callCell);
        }
        return list;
    }

    public static int callCount() {
        return sCallArray != null ? sCallArray.size() : 0;
    }

    public static CallCell callCellWithPhone(String mPhone) {
        if (sCallArray == null) {
            return null;
        }

        for (CallCell callCell : sCallArray) {
            if (TextUtils.equals(callCell.mPhone, mPhone)) {
                return callCell;
            }
        }

        return null;
    }

    public static CallCell noCallCellWithPhone(int pos) {
        if (sCallArray == null) {
            return null;
        }
        for (int i = 0; i < sCallArray.size(); i++) {
            if (i == pos) {
                return sCallArray.get(i);
            }
        }
        return null;
    }

    public void removeDisconnected(String mPhone) {
        if (sCallArray == null) {
            return;
        }
        int pos = -1;
        for (int i = 0; i < sCallArray.size(); i++) {
            if (TextUtils.equals(sCallArray.get(i).mPhone, mPhone)) {
                pos = i;
                Log.e("removedis", "www");
                break;
            }
        }
        if (pos != -1) {
            sCallArray.remove(this);

            mCallback.callCellNeedsReloadData(this);
        }

    }

    public static boolean addConnected(String mPhone) {
        if (sCallArray == null) {
            return true;
        }
        for (int i = 0; i < sCallArray.size(); i++) {
            if (TextUtils.equals(sCallArray.get(i).mPhone, mPhone)) {
                return false;
            }
        }
        return true;
    }

    public static CallCell callCellWithSess(int sessId) {
        if (sCallArray == null) {
            return null;
        }

        for (CallCell callCell : sCallArray) {
            if (callCell.mSessId == sessId) {
                return callCell;
            }
        }

        return null;
    }

    public static CallCell callCellWithIndex(int index) {
        if (sCallArray == null) {
            return null;
        }

        return sCallArray.get(index);
    }

    public static void add(int sessId, int contactId, String phone, String name, int sessState,
                           long base, boolean reconnecting, boolean paused, boolean pausedByCS,
                           boolean audioInterrupted, int audioNetSta, Callback delegate, int direction) {
        if (sCallArray == null) {
            sCallArray = new ArrayList<>();
        }

        CallCell callCell = new CallCell(sessId, contactId, phone, name, sessState, base,
                reconnecting, paused, pausedByCS, audioInterrupted, audioNetSta, delegate, direction);
        callCell.add();
    }

    public static void addCall(int contactId, String phone, String name, Callback delegate, int state) {
        if (sCallArray == null) {
            sCallArray = new ArrayList<>();
        }

        CallCell callCell = CallCell.callCellWithPhone(phone);
        if (callCell != null) {
            callCell.setSessState(state, callCell.mPhone);
        } else {
            callCell = new CallCell(-1, contactId, phone, name,
                    state, 0, false, false, false, false,
                    MtcCallConstants.EN_MTC_NET_STATUS_NORMAL, delegate, 0);
            callCell.add();
        }
    }

    public static void removeCall(int pos) {
        CallCell callCell = CallCell.noCallCellWithPhone(pos);
        if (callCell != null) {
            callCell.remove();
        }
    }

    public static void removeAll() {
        if (sCallArray == null) {
            return;
        }
        for (int i = 0; i < sCallArray.size(); i++) {
            CallCell callCell = CallCell.noCallCellWithPhone(i);
            if (callCell != null) {
                callCell.remove();
            }
        }
    }


    public static void connecting(int sessId) {
        CallCell callCell = CallCell.callCellWithSess(sessId);
        if (callCell != null) {
            callCell.connecting();
        }
    }

    public static void talking(String number) {

        CallCell callCell = CallCell.callCellWithPhone(number);
        if (callCell != null) {
            callCell.talking();
        }
    }

    public static void termed(int sessId, int statCode, String pcReason) {
        CallCell callCell = CallCell.callCellWithSess(sessId);
        if (callCell != null) {
            callCell.termed(statCode, pcReason);
        }
    }

    public static void end() {
        if (sCallArray == null) {
            return;
        }

        for (Object callCell : sCallArray.toArray()) {
            ((CallCell) callCell).ended();
        }
    }


    public static void netStaChanged(int sessId, int iStatus) {
        CallCell callCell = CallCell.callCellWithSess(sessId);
        if (callCell != null) {
            callCell.netStaChanged(iStatus);
        }
    }

    private void setSessState(int sessState, String phone) {
        mSessState = sessState;

        switch (mSessState) {
            case JRCommonValue.EN_MTC_CONF_PARTP_STATE_DIALINGOUT: {
                setStateText(MainApplication.sApp.getString(R.string.connecting), false);
                break;
            }
            case JRCommonValue.EN_MTC_CONF_PARTP_STATE_ALERTING: {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                }
                mReconnecting = false;
                mPaused = false;
                mPausedByCS = false;
                mAudioNetSta = MtcCallConstants.EN_MTC_NET_STATUS_NORMAL;
                if (mChrState != null) {
                    mChrState.stop();
                }
                setErrorText("");
                setStateText(MainApplication.sApp.getString(R.string.connecting), false);
                break;
            }

            case JRCommonValue.EN_MTC_CONF_PARTP_STATE_PENDING: {
                setStateText(MainApplication.sApp.getString(R.string.connecting), true);
                break;
            }
            case JRCommonValue.EN_MTC_CONF_PARTP_STATE_CONNED: {
                if (mBase == 0) {
                    mBase = SystemClock.elapsedRealtime();
                }
                setStateText("", false);
                break;
            }
            case JRCommonValue.EN_MTC_CONF_PARTP_STATE_DISCING: {
                setStateText(MainApplication.sApp.getString(R.string.leaved), false);
//                removeDisconnected(phone);
                break;
            }
            case JRCommonValue.EN_MTC_CONF_PARTP_STATE_DISCED: {
                setStateText(MainApplication.sApp.getString(R.string.leaved), false);
//                removeDisconnected(phone);
                break;
            }

            default:
                break;
        }

        if (mCallback != null) {
            mCallback.callCellNeedsReloadData(this);
        }
    }


    public static void setStateText(int sessId, String text, boolean animated) {
        if (sCallArray == null) {
            return;
        }
        CallCell callCell = CallCell.callCellWithSess(sessId);
        if (callCell != null) {
            callCell.setStateText(text, animated);
        }
    }

    private void setStateText(String text, boolean animated) {
        mStateText = text;
        mAnimated = animated;

        if (mStateText == null) {
            mStateText = "";
        }
        if (TextUtils.isEmpty(mStateText)) {
            if (mBase != 0) {
                if (mChrState != null) {
                    mChrState.setBase(mBase);
                    mChrState.start();
                }
            } else {
                if (mChrState != null) {
                    mChrState.setText("");
                }
            }
        } else {
            if (mChrState != null) {
                mChrState.stop();
                mChrState.setText(mAnimated ? mStateText.concat("...") : mStateText);
            } else {
            }
        }
    }

    private CallCell(int sessId, int contactId, String phone, String name, int sessState,
                     long base, boolean reconnecting, boolean paused, boolean pausedByCS,
                     boolean audioInterrupted, int audioNetSta, Callback delegate, int direction) {
        CallCell callCell = this;
        callCell.mSessId = sessId;
        callCell.mContactId = contactId;
        callCell.mPhone = phone;
        callCell.mName = name;
        callCell.mBase = base;
        callCell.mSessState = sessState;
        setSessState(sessState, "");
        callCell.mCallback = delegate;
        callCell.mReconnecting = reconnecting;
        callCell.mPaused = paused;
        callCell.mPausedByCS = pausedByCS;
        callCell.mAudioNetSta = audioNetSta;
        callCell.mDirection = direction;
    }

    private void add() {
        sCallArray.add(this);
        mCallback.callCellNeedsReloadData(this);
    }

    private void remove() {
        sCallArray.remove(this);
        mCallback.callCellNeedsReloadData(this);
    }

    public void onClick() {
        remove();
    }

    private void endConference() {
//        setSessState(RcsCallSession.State.IDLE,"");
//        setStateText(BaseApplication.sApp.getString(R.string.Call_ended), false);
//
//        if (sCallArray == null) {
//            return;
//        }
//        boolean isConferenceEnd = true;
//        for (CallCell callCell : sCallArray) {
//            if (callCell.mSessId != RcsCallSession.State.INVALID) {
//                isConferenceEnd = false;
//                break;
//            }
//        }
//
//        if (isConferenceEnd) {
//            sCallArray.clear();
//            sCallArray = null;
//            mCallback.callCellDidEndConference(null);
//        }
    }

    private void callSuccess(int sessId) {
        mSessId = sessId;
    }


    private void connecting() {
        mConnectingDate = SystemClock.elapsedRealtime();
        //setSessState(RcsCallSession.State.CONNECTING);
    }

    private void talking() {
        sHandler.removeMessages(MSG_CALL_DISCONNECTED);
//        mRtpConnected = true;
//
//        if (mSessState < RcsCallSession.State.CONNECTING) {
//            connecting();
//        }
//
//        mReconnecting = false;
//        mPausedByCS = false;
//        mPaused = false;
//        mAudioNetSta = MtcCallConstants.EN_MTC_NET_STATUS_NORMAL;
//        if (mSessState == RcsCallSession.State.CONNECTING) {
//            int connectingTime =  (int) (SystemClock.elapsedRealtime() - mConnectingDate);
//            mBase = SystemClock.elapsedRealtime();
//            if (mChrState != null) {
//                mChrState.setBase(mBase);
//                mChrState.start();
//            }
//            //setSessState(RcsCallSession.State.TALKING);
//            mDirection = 0;
//        }
    }


    private void termed(int dwStatCode, String pcReason) {
        boolean isConferenceEnd = true;
//        for (CallCell callCell : sCallArray) {
//            if (callCell != this && callCell.mSessId != RcsCallSession.State.INVALID) {
//                isConferenceEnd = false;
//                break;
//            }
//        }

        if (isConferenceEnd) {
            sCallArray.clear();
            sCallArray = null;
            mCallback.callCellDidEndConference(null);
        }

        int state = mSessState;
        //setSessState(RcsCallSession.State.IDLE);

        String stateText = null;
//        if (dwStatCode < 0) {
//            dwStatCode = MtcCallConstants.EN_MTC_CALL_TERM_STATUS_NORMAL;
//        }
//        switch (dwStatCode) {
//            case MtcCallConstants.EN_MTC_CALL_TERM_STATUS_NORMAL:
//            case MtcCallConstants.EN_MTC_CALL_TERM_STATUS_REPLACED: {
//                if (state == CALL_STATE_ENDING) {
//                    stateText = BaseApplication.sContext.getString(R.string.Call_ending);
//                } else if (state == CALL_STATE_INCOMING) {
//
//                } else {
//                    stateText = BaseApplication.sContext.getString(R.string.Call_ended);
//                    ringTerm();
//                }
//                break;
//            }
//
//            case MtcCallConstants.EN_MTC_CALL_TERM_STATUS_DECLINE: {
//                if (state == CALL_STATE_DECLINING) {
//                    break;
//                }
//            }
//            case MtcCallConstants.EN_MTC_CALL_TERM_STATUS_BUSY:
//                if (TextUtils.isEmpty(pcReason)) {
//                    stateText = BaseApplication.sContext.getString(R.string.Busy);
//                } else {
//                    stateText = pcReason;
//                }
//                break;
//            case MtcCallConstants.EN_MTC_CALL_TERM_STATUS_NOT_FOUND:
//                stateText = BaseApplication.sContext.getString(R.string.app_label_hasnot_been_installed, MtcDelegate.getApplicationLabel());
//                ringTerm();
//                break;
//            case MtcCallConstants.EN_MTC_CALL_TERM_STATUS_USER_OFFLINE:
//                stateText = BaseApplication.sContext.getString(R.string.Offline);
//                break;
//            case MtcCallConstants.EN_MTC_CALL_TERM_STATUS_TIMEOUT:
//                if (state >= CALL_STATE_CALLING && state < CALL_STATE_CONNECTING) {
//                    stateText = BaseApplication.sContext.getString(R.string.No_answer);
//                } else {
//                    stateText = BaseApplication.sContext.getString(R.string.Temporarily_unavailable);
//                    ringTerm();
//                }
//                break;
//            case MtcCallConstants.EN_MTC_CALL_TERM_STATUS_ERROR_TRANSACTION_FAIL:
//                if (state >= CALL_STATE_CALLING && state < CALL_STATE_CONNECTING) {
//                    stateText = BaseApplication.sContext.getString(R.string.No_internet_connection);
//                } else {
//                    stateText = BaseApplication.sContext.getString(R.string.Temporarily_unavailable);
//                }
//                ringTerm();
//                break;
//            default:
//                if (state >= CALL_STATE_CALLING && state < CALL_STATE_CONNECTING) {
//                } else {
//                    ringTerm();
//                }
//                stateText = BaseApplication.sApp.getString(R.string.Temporarily_unavailable);
//                break;
//        }
//        setStateText(BaseApplication.sApp.getResources().getString(R.string.Call_ended), false);
    }

    private void ended() {
        endConference();
    }

    private void netStaChanged(int iStatus) {
        mAudioNetSta = iStatus;
        if (iStatus > MtcCall.EN_MTC_NET_STATUS_DISCONNECTED) {
            stopDisconnectedTimer();
        }
    }

    public static boolean setErrorText(int sessId, String text) {
        if (sCallArray == null) {
            return false;
        }

        CallCell callCell = CallCell.callCellWithSess(sessId);
        return callCell != null && callCell.setErrorText(text);

    }

    private boolean setErrorText(String text) {
        if (TextUtils.equals(text, mErrorText)) {
            return false;
        }

        mErrorText = text;
        if (mTxtError != null) {
            mTxtError.setText(mErrorText);
        }
        return true;
    }

    private void startDisconnectedTimer() {
        if (!mDisconnectedTiming) {
            mDisconnectedTiming = true;
            Message message = sHandler.obtainMessage(MSG_CALL_DISCONNECTED, this);
            sHandler.sendMessageDelayed(message, 30000);
        }
    }

    private void stopDisconnectedTimer() {
        if (mDisconnectedTiming) {
            mDisconnectedTiming = false;
            sHandler.removeMessages(MSG_CALL_DISCONNECTED);
        }
    }

    private void callDisconnected() {

    }

}
