package com.juphoon.jrsdk.model;

import android.text.TextUtils;

import com.juphoon.cmcc.app.lemon.MtcImFileConstants;
import com.juphoon.jrsdk.utils.CommonValue;
import com.juphoon.rcs.jrsdk.JRClient;
import com.juphoon.rcs.jrsdk.JRMessageConstants;
import com.juphoon.rcs.jrsdk.JRMessageItem;

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
    public static final String FIELD_IS_REVOKED = "isRevoked";
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
    public static final String FIELD_IMDN_DELI = "imdnDeli";
    public static final String FIELD_LABEL = "label";
    public static final String FIELD_IMDN_DIP = "imdnDip";


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
    private boolean isRevoked;
    private boolean imdnDip;
    private boolean imdnDeli;

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

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }

    public static RealmMessage itemToMessage(JRMessageItem item) {
        if (item == null) {
            return null;
        }
        RealmMessage message = new RealmMessage();
        if (item instanceof JRMessageItem.FileItem) {
            JRMessageItem.FileItem fileItem = (JRMessageItem.FileItem) item;
            message.filePath = fileItem.filePath;
            message.fileTransId = fileItem.fileTransId;
            message.fileThumbPath = fileItem.fileThumbPath;
            message.fileSize = fileItem.fileSize;
            message.fileTransSize = fileItem.fileTransSize;
            message.fileDuration = fileItem.fileMediaDuration;
            message.type = getMessageType(fileItem);
        }
        if (item instanceof JRMessageItem.TextItem) {
            JRMessageItem.TextItem textItem = (JRMessageItem.TextItem) item;
            message.content = textItem.content;
            message.type = CommonValue.MESSAGE_TYPE_TEXT;
        }
        if (item instanceof JRMessageItem.GeoItem) {
            JRMessageItem.GeoItem geoItem = (JRMessageItem.GeoItem) item;
            message.label = geoItem.geoFreeText;
            message.latitude = geoItem.geoLatitude;
            message.longitude = geoItem.geoLongitude;
            message.radius = geoItem.geoRadius;
            message.fileTransId = geoItem.geoTransId;
            message.type = CommonValue.MESSAGE_TYPE_GEO;
        }
        message.isBurnAfterReading = item.isBurnAfterReading;
        message.imdnId = item.messageImdnId;
        message.timeStamp = item.timestamp;
        message.sessionIdentity = item.sessIdentity;
//        message.imdnDeli = item.
        if (TextUtils.isEmpty(message.sessionIdentity)) {
            message.peerPhone = item.messageDirection == JRMessageConstants.Direction.RECV ? item.senderNumber : item.receiverNumber;
        }
        if(!TextUtils.isEmpty(item.senderNumber)) {
            message.senderPhone = item.senderNumber;
        }
        message.state = getMessageState(item);

        return message;
    }

    public JRMessageItem messageToItem(RealmMessage message) {
        if (message == null) {
            return null;
        }
        if (!TextUtils.isEmpty(message.filePath)) {
            JRMessageItem.FileItem fileItem = new JRMessageItem.FileItem();
            fileItem.messageImdnId = message.getImdnId();
            fileItem.timestamp = message.timeStamp;
            fileItem.senderNumber = message.senderPhone;
            fileItem.sessIdentity = message.sessionIdentity;
            fileItem.receiverNumber = TextUtils.equals(message.senderPhone, message.peerPhone) ? JRClient.getInstance().getCurrentNumber() : message.peerPhone;
            fileItem.messageDirection = TextUtils.equals(message.senderPhone, JRClient.getInstance().getCurrentNumber()) ? JRMessageConstants.Direction.SEND : JRMessageConstants.Direction.RECV;
            fileItem.filePath = message.filePath;
            fileItem.messageType = JRMessageConstants.Type.FILE;
            fileItem.fileTransId = message.fileTransId;
            fileItem.fileThumbPath = message.fileThumbPath;
            fileItem.fileSize = message.fileSize;
            fileItem.fileTransSize = message.fileTransSize;
            fileItem.fileType = getFileType(message);
            fileItem.messageState = getMessageState(message);
            fileItem.fileMediaDuration = message.fileDuration;
            fileItem.isBurnAfterReading = message.isBurnAfterReading;
            if(!TextUtils.isEmpty(message.sessionIdentity)){
                fileItem.messageChannelType = JRMessageConstants.ChannelType.TYPE_GROUP;
            } else if (!TextUtils.isEmpty(message.peerPhone) && message.peerPhone.contains(";")) {
                fileItem.messageChannelType = JRMessageConstants.ChannelType.TYPE_LIST;
            } else {
                fileItem.messageChannelType = JRMessageConstants.ChannelType.TYPE_ONE;
            }
            return fileItem;
        } else if (!TextUtils.isEmpty(message.label)) {
            JRMessageItem.GeoItem geoItem = new JRMessageItem.GeoItem();
            geoItem.messageImdnId = message.getImdnId();
            geoItem.timestamp = message.timeStamp;
            geoItem.senderNumber = message.senderPhone;
            geoItem.sessIdentity = message.sessionIdentity;
            geoItem.receiverNumber = TextUtils.equals(message.senderPhone, message.peerPhone) ? JRClient.getInstance().getCurrentNumber() : message.senderPhone;
            geoItem.messageDirection = TextUtils.equals(message.senderPhone, JRClient.getInstance().getCurrentNumber()) ? JRMessageConstants.Direction.SEND : JRMessageConstants.Direction.RECV;
            geoItem.messageType = JRMessageConstants.Type.GEO;
            geoItem.geoFreeText = message.label;
            geoItem.geoLatitude = message.latitude;
            geoItem.geoLongitude = message.longitude;
            geoItem.geoRadius = message.radius;
            geoItem.geoTransId = message.fileTransId;
            geoItem.messageState = getMessageState(message);
            if(!TextUtils.isEmpty(message.sessionIdentity)){
                geoItem.messageChannelType = JRMessageConstants.ChannelType.TYPE_GROUP;
            } else if (!TextUtils.isEmpty(message.peerPhone) && message.peerPhone.contains(";")) {
                geoItem.messageChannelType = JRMessageConstants.ChannelType.TYPE_LIST;
            } else {
                geoItem.messageChannelType = JRMessageConstants.ChannelType.TYPE_ONE;
            }
            return geoItem;
        } else {
            JRMessageItem.TextItem textItem = new JRMessageItem.TextItem();
            textItem.messageImdnId = message.getImdnId();
            textItem.content = message.content;
            textItem.timestamp = message.timeStamp;
            textItem.senderNumber = message.senderPhone;
            textItem.sessIdentity = message.sessionIdentity;
            textItem.receiverNumber = TextUtils.equals(message.senderPhone, message.peerPhone) ? JRClient.getInstance().getCurrentNumber() : message.peerPhone;
            textItem.messageDirection = TextUtils.equals(message.senderPhone, JRClient.getInstance().getCurrentNumber()) ? JRMessageConstants.Direction.SEND : JRMessageConstants.Direction.RECV;
            textItem.messageState = getMessageState(message);
            textItem.isBurnAfterReading = message.isBurnAfterReading;
            if(!TextUtils.isEmpty(message.sessionIdentity)){
                textItem.messageChannelType = JRMessageConstants.ChannelType.TYPE_GROUP;
            } else if (!TextUtils.isEmpty(message.peerPhone) && message.peerPhone.contains(";")) {
                textItem.messageChannelType = JRMessageConstants.ChannelType.TYPE_LIST;
            } else {
                textItem.messageChannelType = JRMessageConstants.ChannelType.TYPE_ONE;
            }
            return textItem;
        }
    }

    private static int getMessageType(JRMessageItem.FileItem item) {
        switch (item.fileType) {
            case MtcImFileConstants.MTC_IM_FILE_CONT_AUDIO_AMR:
            case MtcImFileConstants.MTC_IM_FILE_CONT_AUDIO_WAV:
                return CommonValue.MESSAGE_TYPE_AUDIO;
            case MtcImFileConstants.MTC_IM_FILE_CONT_IMG_JPEG:
            case MtcImFileConstants.MTC_IM_FILE_CONT_IMG_PNG:
            case "image/jpg":
                return CommonValue.MESSAGE_TYPE_IMAGE;
            case MtcImFileConstants.MTC_IM_FILE_CONT_APP_OSTRM:
                return CommonValue.MESSAGE_TYPE_OTHER_FILE;
            case MtcImFileConstants.MTC_IM_FILE_CONT_TXT_VCARD:
            case "text/vcard":
                return CommonValue.MESSAGE_TYPE_VCARD;
            case MtcImFileConstants.MTC_IM_FILE_CONT_VIDEO_MP4:
                return CommonValue.MESSAGE_TYPE_VIDEO;
        }
        return CommonValue.MESSAGE_TYPE_UNKNOWN;
    }

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
            case CommonValue.MESSAGE_TYPE_VCARD:
                return MtcImFileConstants.MTC_IM_FILE_CONT_TXT_VCARD;
        }
        return "";
    }

    private static int getMessageState(JRMessageItem item) {
        switch (item.messageState) {
            case JRMessageConstants.State.RECEIVE_FAILED:
                return CommonValue.MESSAGE_STATUS_RECV_FAILED;
            case JRMessageConstants.State.RECEIVE_OK:
                return CommonValue.MESSAGE_STATUS_RECV_OK;
            case JRMessageConstants.State.RECEIVEING_PAUSE:
                return CommonValue.MESSAGE_STATUS_RECV_PAUSED;
            case JRMessageConstants.State.RECEIVEING:
                return CommonValue.MESSAGE_STATUS_RECVING;
            case JRMessageConstants.State.RECEIVE_INVITE:
                return CommonValue.MESSAGE_STATUS_INVITE;
            case JRMessageConstants.State.SEND_FAILED:
                return CommonValue.MESSAGE_STATUS_SEND_FAILED;
            case JRMessageConstants.State.SEND_OK:
                return CommonValue.MESSAGE_STATUS_SEND_OK;
            case JRMessageConstants.State.SENDING:
                return CommonValue.MESSAGE_STATUS_SENDING;
            case JRMessageConstants.State.SENDING_PAUSE:
                return CommonValue.MESSAGE_STATUS_SEND_PAUSED;
        }
        return CommonValue.MESSAGE_STATUS_UNKNOWN;
    }

    private static int getMessageState(RealmMessage message) {
        switch (message.state) {
            case CommonValue.MESSAGE_STATUS_RECV_FAILED:
                return JRMessageConstants.State.RECEIVE_FAILED;
            case CommonValue.MESSAGE_STATUS_RECV_OK:
                return JRMessageConstants.State.RECEIVE_OK;
            case CommonValue.MESSAGE_STATUS_RECV_PAUSED:
                return JRMessageConstants.State.RECEIVEING_PAUSE;
            case CommonValue.MESSAGE_STATUS_RECVING:
                return JRMessageConstants.State.RECEIVEING;
            case CommonValue.MESSAGE_STATUS_INVITE:
                return JRMessageConstants.State.RECEIVE_INVITE;
            case CommonValue.MESSAGE_STATUS_SEND_FAILED:
                return JRMessageConstants.State.SEND_FAILED;
            case CommonValue.MESSAGE_STATUS_SEND_OK:
                return JRMessageConstants.State.SEND_OK;
            case CommonValue.MESSAGE_STATUS_SENDING:
                return JRMessageConstants.State.SENDING;
            case CommonValue.MESSAGE_STATUS_SEND_PAUSED:
                return JRMessageConstants.State.SENDING_PAUSE;
        }
        return JRMessageConstants.State.INIT;
    }

    public boolean isSender() {
        return state == CommonValue.MESSAGE_STATUS_SEND_FAILED || state == CommonValue.MESSAGE_STATUS_SEND_OK || state == CommonValue.MESSAGE_STATUS_SEND_PAUSED || state == CommonValue.MESSAGE_STATUS_SENDING;
    }

    public boolean isVideo() {
        return type == CommonValue.MESSAGE_TYPE_VIDEO;
    }

    public boolean isAudio() {
        return type == CommonValue.MESSAGE_TYPE_AUDIO;
    }

    public boolean isImage() {
        return type == CommonValue.MESSAGE_TYPE_IMAGE;
    }

    public boolean isSuccess() {
        return state == CommonValue.MESSAGE_STATUS_RECV_OK;
    }
}
