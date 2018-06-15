package common.model;

import android.text.TextUtils;

import com.juphoon.cmcc.app.lemon.MtcImFileConstants;
import com.juphoon.rcs.JRClient;
//import com.juphoon.rcs.JRMessageContants;
//import com.juphoon.rcs.JRMessageItem;

import common.CommonValue;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class RealmMessage extends RealmObject {
    public static final String TABLE_NAME = "RealmMessage";
    // basic
    public static final String FIELD_IMDN_ID = "imdnId";
    public static final String FIELD_PEER_PHONE = "peerPhone";
    public static final String FIELD_SESSIONIDENTITY = "sessionIdentity";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_SENDER_PHONE = "senderPhone";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_STATE = "state";
    public static final String FIELD_TIME_STAMP = "timeStamp";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_IS_READ = "isRead";
    public static final String FIELD_IS_BURN_AFTER_READING = "isBurnAfterReading";
    // file
    public static final String FIELD_FILE_TRANS_ID = "fileTransId";
    public static final String FIELD_FILE_NAME = "fileName";
    public static final String FIELD_FILE_PATH = "filePath";
    public static final String FIELD_FILE_THUMB_PATH = "fileThumbPath";
    public static final String FIELD_FILE_SIZE = "fileSize";
    public static final String FIELD_FILE_TRANS_SIZE = "fileTransSize";
    public static final String FIELD_FILE_DURATION = "fileDuration";
    public static final String FIELD_FILE_PROGRESS = "fileProgress";
    public static final String FIELD_LATITUDE = "latitude";
    public static final String FIELD_LONGITUDE = "cardContent";
    public static final String FIELD_RADIUS = "radius";
    public static final String FIELD_LABEL = "label";


    @PrimaryKey
    private String imdnId;
    /**
     * peerPhone 不为空代表是一对一消息
     */
    private String peerPhone;
    /**
     * sessionIdentity 不为空代表是群消息
     */
    private String sessionIdentity;
    private int type;
    private String senderPhone;
    private String content;
    private int state;
    private long timeStamp;
    private String displayName;
    private boolean isRead;
    private boolean isBurnAfterReading;

    private String fileTransId;
    private String fileName;
    private String filePath;
    private String fileThumbPath;
    private int fileSize;
    private int fileTransSize;
    private int fileDuration;
    private int progress;

    //地理位置
    private double latitude;
    private double longitude;
    private float radius;
    private String label;

    public String getImdnId() {
        return imdnId;
    }

    public void setImdnId(String imdnId) {
        this.imdnId = imdnId;
    }

    public String getPeerPhone() {
        return peerPhone;
    }

    public void setPeerPhone(String peerPhone) {
        this.peerPhone = peerPhone;
    }

    public String getSessionIdentity() {
        return sessionIdentity;
    }

    public void setSessionIdentity(String sessionIdentity) {
        this.sessionIdentity = sessionIdentity;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isBurnAfterReading() {
        return isBurnAfterReading;
    }

    public void setBurnAfterReading(boolean burnAfterReading) {
        isBurnAfterReading = burnAfterReading;
    }

    public String getFileTransId() {
        return fileTransId;
    }

    public void setFileTransId(String fileTransId) {
        this.fileTransId = fileTransId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileThumbPath() {
        return fileThumbPath;
    }

    public void setFileThumbPath(String fileThumbPath) {
        this.fileThumbPath = fileThumbPath;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileTransSize() {
        return fileTransSize;
    }

    public void setFileTransSize(int fileTransSize) {
        this.fileTransSize = fileTransSize;
    }

    public int getFileDuration() {
        return fileDuration;
    }

    public void setFileDuration(int fileDuration) {
        this.fileDuration = fileDuration;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

//    public static RealmMessage itemToMessage(JRMessageItem item) {
//        if (item == null) {
//            return null;
//        }
//        RealmMessage message = new RealmMessage();
//        message.imdnId = item.imdnId;
//        message.content = item.text;
//        message.timeStamp = item.timeStamp;
//        message.sessionIdentity = item.sessionIdentity;
//        if (TextUtils.isEmpty(message.sessionIdentity)) {
//            message.peerPhone = item.direction == JRMessageContants.Direction.RECV ? item.senderNumber : item.recvAddress;
//        }
//        message.senderPhone = item.senderNumber;
//        message.type = getMessageType(item);
//        message.filePath = item.filePath;
//        message.fileTransId = item.transId;
//        message.fileThumbPath = item.thumbPath;
//        message.fileSize = item.fileSize;
//        message.fileTransSize = item.tranferSize;
//        message.label = item.label;
//        message.latitude = item.latitude;
//        message.longitude = item.longitude;
//        message.radius = item.radius;
//        message.state = getMessageState(item);
//        message.fileDuration = item.duration;
//        return message;
//    }

//    public JRMessageItem messageToItem(RealmMessage message) {
//        if (message == null) {
//            return null;
//        }
//        JRMessageItem item = new JRMessageItem();
//        item.imdnId = message.getImdnId();
//        item.text = message.content;
//        item.timeStamp = message.timeStamp;
//        item.senderNumber = message.senderPhone;
//        item.sessionIdentity = message.sessionIdentity;
//        item.recvAddress = TextUtils.equals(message.senderPhone, message.peerPhone) ? JRClient.getInstance().getCurLoginNumber() : message.senderPhone;
//        item.direction = TextUtils.equals(message.senderPhone, JRClient.getInstance().getCurLoginNumber()) ? JRMessageContants.Direction.SEND : JRMessageContants.Direction.RECV;
//        item.filePath = message.filePath;
//        item.messageType = getMessageType(message);
//        item.transId = message.fileTransId;
//        item.thumbPath = message.fileThumbPath;
//        item.fileSize = message.fileSize;
//        item.tranferSize = message.fileTransSize;
//        item.fileType = getFileType(message);
//        item.label = message.label;
//        item.latitude = message.latitude;
//        item.longitude = message.longitude;
//        item.radius = message.radius;
//        item.state = getMessageState(message);
//        item.duration = message.fileDuration;
//        return item;
//    }

//    private static int getMessageType(JRMessageItem item) {
//        switch (item.messageType) {
//            case AUDIO:
//                return CommonValue.MESSAGE_TYPE_AUDIO;
//            case GEO:
//                return CommonValue.MESSAGE_TYPE_GEO;
//            case IMAGE:
//                return CommonValue.MESSAGE_TYPE_IMAGE;
//            case LMSG:
//                return CommonValue.MESSAGE_TYPE_LMSG;
//            case OTHER_FILE:
//                return CommonValue.MESSAGE_TYPE_OTHER_FILE;
//            case VCARD:
//                return CommonValue.MESSAGE_TYPE_VCARD;
//            case PMSG:
//                return CommonValue.MESSAGE_TYPE_PMSG;
//            case VIDEO:
//                return CommonValue.MESSAGE_TYPE_VIDEO;
//            case SYSTEM:
//                return CommonValue.MESSAGE_TYPE_SYSTEM;
//        }
//        return CommonValue.MESSAGE_TYPE_UNKNOWN;
//    }

//    private static JRMessageContants.Type getMessageType(RealmMessage message) {
//        switch (message.type) {
//            case CommonValue.MESSAGE_TYPE_AUDIO:
//                return JRMessageContants.Type.AUDIO;
//            case CommonValue.MESSAGE_TYPE_GEO:
//                return JRMessageContants.Type.GEO;
//            case CommonValue.MESSAGE_TYPE_IMAGE:
//                return JRMessageContants.Type.IMAGE;
//            case CommonValue.MESSAGE_TYPE_LMSG:
//                return JRMessageContants.Type.LMSG;
//            case CommonValue.MESSAGE_TYPE_OTHER_FILE:
//                return JRMessageContants.Type.OTHER_FILE;
//            case CommonValue.MESSAGE_TYPE_VCARD:
//                return JRMessageContants.Type.VCARD;
//            case CommonValue.MESSAGE_TYPE_PMSG:
//                return JRMessageContants.Type.PMSG;
//            case CommonValue.MESSAGE_TYPE_VIDEO:
//                return JRMessageContants.Type.VIDEO;
//            case CommonValue.MESSAGE_TYPE_SYSTEM:
//                return JRMessageContants.Type.SYSTEM;
//        }
//        return JRMessageContants.Type.UNKNOWN;
//    }

    private String getFileType(RealmMessage message) {
        switch (message.type) {
            case CommonValue.MESSAGE_TYPE_AUDIO:
                return MtcImFileConstants.MTC_IM_FILE_CONT_AUDIO_AMR;
            case CommonValue.MESSAGE_TYPE_IMAGE:
                return MtcImFileConstants.MTC_IM_FILE_CONT_IMG_JPEG;
            case CommonValue.MESSAGE_TYPE_OTHER_FILE:
                return MtcImFileConstants.MTC_IM_FILE_CONT_APP_OSTRM;
            case CommonValue.MESSAGE_TYPE_VIDEO:
                return MtcImFileConstants.MTC_IM_FILE_CONT_VIDEO_MP4;
        }
        return "";
    }

//    private static int getMessageState(JRMessageItem item) {
//        switch (item.state) {
//            case RECEIVE_FAILED:
//                return CommonValue.MESSAGE_STATUS_RECV_FAILED;
//            case RECEIVE_OK:
//                return CommonValue.MESSAGE_STATUS_RECV_OK;
//            case RECEIVEING_PAUSE:
//                return CommonValue.MESSAGE_STATUS_RECV_PAUSED;
//            case RECEIVEING:
//                return CommonValue.MESSAGE_STATUS_RECVING;
//            case RECEIVE_INVITE:
//                return CommonValue.MESSAGE_STATUS_INVITE;
//            case SEND_FAILED:
//                return CommonValue.MESSAGE_STATUS_SEND_FAILED;
//            case SEND_OK:
//                return CommonValue.MESSAGE_STATUS_SEND_OK;
//            case SENDING:
//                return CommonValue.MESSAGE_STATUS_SENDING;
//            case SENDING_PAUSE:
//                return CommonValue.MESSAGE_STATUS_SEND_PAUSED;
//        }
//        return CommonValue.MESSAGE_STATUS_UNKNOWN;
//    }

//    private static JRMessageContants.State getMessageState(RealmMessage message) {
//        switch (message.state) {
//            case CommonValue.MESSAGE_STATUS_RECV_FAILED:
//                return JRMessageContants.State.RECEIVE_FAILED;
//            case CommonValue.MESSAGE_STATUS_RECV_OK:
//                return JRMessageContants.State.RECEIVE_OK;
//            case CommonValue.MESSAGE_STATUS_RECV_PAUSED:
//                return JRMessageContants.State.RECEIVEING_PAUSE;
//            case CommonValue.MESSAGE_STATUS_RECVING:
//                return JRMessageContants.State.RECEIVEING;
//            case CommonValue.MESSAGE_STATUS_INVITE:
//                return JRMessageContants.State.RECEIVE_INVITE;
//            case CommonValue.MESSAGE_STATUS_SEND_FAILED:
//                return JRMessageContants.State.SEND_FAILED;
//            case CommonValue.MESSAGE_STATUS_SEND_OK:
//                return JRMessageContants.State.SEND_OK;
//            case CommonValue.MESSAGE_STATUS_SENDING:
//                return JRMessageContants.State.SENDING;
//            case CommonValue.MESSAGE_STATUS_SEND_PAUSED:
//                return JRMessageContants.State.SENDING_PAUSE;
//        }
//        return JRMessageContants.State.INIT;
//    }

    public boolean isSender() {
        return state == CommonValue.MESSAGE_STATUS_SEND_FAILED || state == CommonValue.MESSAGE_STATUS_SEND_OK || state == CommonValue.MESSAGE_STATUS_SEND_PAUSED || state == CommonValue.MESSAGE_STATUS_SENDING;
    }

    public boolean isVideo() {
        return type == CommonValue.MESSAGE_TYPE_VIDEO;
    }

    public boolean isImage() {
        return type == CommonValue.MESSAGE_TYPE_IMAGE;
    }

    public boolean isSuccess() {
        return state == CommonValue.MESSAGE_STATUS_RECV_OK;
    }
}
