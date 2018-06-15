package common.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.juphoon.rcs.JRCallConstants;
import com.juphoon.rcs.JRCallItem;
import com.juphoon.rcs.JRLog;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Upon on 2018/4/23.
 */

public class RealmCallLog extends RealmObject implements Parcelable {
    public static final int CALL_TYPE_ONE_AUDIO = 1;
    public static final int CALL_TYPE_ONE_VIDEO = 2;
    public static final int CALL_TYPE_CONF_AUDIO = 3;
    public static final int CALL_TYPE_CONF_VIDEO = 4;

    public static final int CALL_STATE_INCOMING = 1;
    public static final int CALL_STATE_OUTGOING = 2;


    public static final String TABLE_NAME = "RealmCallLog";
    public static final String FILED_PEER_NUMBER = "peerNumber";
    public static final String FILED_START_TIME = "startTime";
    public static final String FILED_TALKING_TIME = "talkingTime";
    public static final String FILED_END_TIME = "endTime";
    public static final String FILED_CALL_TYPE = "callType";
    public static final String FILED_CALL_STATUS = "callStatus";

    @PrimaryKey
    private long startTime;
    private String perrNumber;
    private long talkingTime;
    private long endTime;
    private int callType;
    private int callStatus;

    public String getPerrNumber() {
        return perrNumber;
    }

    public void setPerrNumber(String perrNumber) {
        this.perrNumber = perrNumber;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public int getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(int callStatus) {
        this.callStatus = callStatus;
    }

    public long getTalkingTime() {
        return talkingTime;
    }

    public void setTalkingTime(long talkingTime) {
        this.talkingTime = talkingTime;
    }

    public static RealmCallLog item2Realm(JRCallItem item, RealmCallLog callLog) {
        if (item == null) {
            return null;
        }
        long startTime = item.getBeginTime();
        long talkingTime = item.getTalkingBeginTime();
        long endTime = item.getEndTime();

        StringBuilder peerNumber = new StringBuilder();
        for (int i = 0; i < item.getMemberList().size(); i++) {
            if (item.getMemberList().size() > 0) {
                peerNumber.append(item.getMemberList().get(i).getNumber());
                if (i != (item.getMemberList().size() - 1)) {
                    peerNumber.append(";");
                }
            }
        }
        callLog.setPerrNumber(peerNumber.toString());
        if (item.isVideo()) {
            if (item.isConference()) {
                callLog.setCallType(CALL_TYPE_CONF_VIDEO);
            } else {
                callLog.setCallType(CALL_TYPE_ONE_VIDEO);
            }
        } else {
            if (item.isConference()) {
                callLog.setCallType(CALL_TYPE_CONF_AUDIO);
            } else {
                callLog.setCallType(CALL_TYPE_ONE_AUDIO);
            }
        }
        callLog.setCallStatus(item.getCallDirection() == JRCallConstants.Direction.INCOMING ? CALL_STATE_INCOMING : CALL_STATE_OUTGOING);
        callLog.setStartTime(startTime);
        if (talkingTime != 0) {
            callLog.setTalkingTime(talkingTime);
        }
        if (endTime != 0) {
            callLog.setEndTime(endTime);
        }
        return callLog;
    }

    public boolean isVideo() {
        return callType == CALL_TYPE_CONF_VIDEO || callType == CALL_TYPE_ONE_VIDEO;
    }

    public boolean isIncoming() {
        return callStatus == CALL_STATE_INCOMING;
    }

    public boolean isMissed() {
        return getTalkingTime() == 0 && isIncoming();
    }

    public static final Parcelable.Creator<RealmCallLog> CREATOR = new Parcelable.Creator<RealmCallLog>() {
        @Override
        public RealmCallLog createFromParcel(Parcel source) {
            RealmCallLog ret = new RealmCallLog();
            ret.startTime = source.readLong();
            ret.talkingTime = source.readLong();
            ret.endTime = source.readLong();
            ret.perrNumber = source.readString();
            ret.callType = source.readInt();
            ret.callStatus = source.readInt();
            return ret;
        }

        @Override
        public RealmCallLog[] newArray(int size) {
            return new RealmCallLog[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(startTime);
        dest.writeLong(talkingTime);
        dest.writeLong(endTime);
        dest.writeString(perrNumber);
        dest.writeInt(callType);
        dest.writeInt(callStatus);
    }
}
