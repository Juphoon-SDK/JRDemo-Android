package com.juphoon.jrsdk.utils;

/**
 * Created by Upon on 2018/2/27.
 */

public class CommonValue {
    public static final String JRCALL_EXTRA_SESSION_ID = "call_session_id";
    public static final String JRCALL_EXTRA_PHONE_NUMBER = "call_phone_number";
    public static final String JRCALL_EXTRA_IS_VIDEO = "call_is_video";
    public static final String JRCALL_EXTRA_IS_MULTI = "call_is_multi";
    public static final String JRCALL_EXTRA_IS_MCU = "call_is_mcu";
    public static final String JRCALL_EXTRA_TOKEN = "call_token";

    public static final String JRMESSAGE_EXTRA_IS_AT = "messageIsAt";
    public static final String JRMESSAGE_EXTRA_IS_AT_ALL = "messageIsAtAll";
    public static final String JRMESSAGE_EXTRA_DISPLAY_NAME = "messageDisplayName";
    public static final String JRMESSAGE_EXTRA_PEERMUMBER = "messagePeernumber";

    //messageType
    public static final int MESSAGE_TYPE_UNKNOWN = 0; //未知文件类型
    public static final int MESSAGE_TYPE_TEXT = 1; //page消息
    public static final int MESSAGE_TYPE_IMAGE = 2; //图片消息
    public static final int MESSAGE_TYPE_VIDEO = 3; //视频消息
    public static final int MESSAGE_TYPE_AUDIO = 4; //音频消息
    public static final int MESSAGE_TYPE_VCARD = 5; //名片消息
    public static final int MESSAGE_TYPE_GEO = 6; //地理位置消息
    public static final int MESSAGE_TYPE_OTHER_FILE = 7; //替他类型文件消息
    public static final int MESSAGE_TYPE_SYSTEM = 8; //系统消息

    //message status
    public static final int MESSAGE_STATUS_UNKNOWN = -1;
    public static final int MESSAGE_STATUS_INVITE = 0;
    public static final int MESSAGE_STATUS_SEND_OK = 1;
    public static final int MESSAGE_STATUS_SENDING = 2;
    public static final int MESSAGE_STATUS_SEND_FAILED = 3;
    public static final int MESSAGE_STATUS_RECV_OK = 4;
    public static final int MESSAGE_STATUS_RECVING = 5;
    public static final int MESSAGE_STATUS_RECV_FAILED = 6;
    public static final int MESSAGE_STATUS_RECV_PAUSED = 7;
    public static final int MESSAGE_STATUS_SEND_PAUSED = 8;


    public static final String EXTRA_NUMBER = "extra_number";
    public static final String EXTRA_LOGS = "extra_logs";


    //各个requestCode
    public static final int REQUEST_LOCATION = 0;
    public static final int REQUEST_CAMERA_VIDEO = 1;
    public static final int REQUEST_CAMERA_PICTURE = 2;
    public static final int REQUEST_CHOOSE_PICTURE = 3;
    public static final int REQUEST_CHOOSE_VIDEO = 4;
    public static final int REQUEST_OTHER_FILE = 5;
    public static final int REQUEST_CHOOSE_VCARD = 6;
    public static final int REQUEST_CHOOSE_AT = 7;


    public static final int REQUEST_ADD_CALL = 7;
    public static final int REQUEST_ADD_MEMBER = 8;
    public static final int REQUEST_TO_MULTI_CALL = 9;
}
