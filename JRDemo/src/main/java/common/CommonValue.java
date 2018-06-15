package common;

/**
 * Created by Upon on 2018/2/27.
 */

public class CommonValue {
    public static final String JRCALL_EXTRA_SESSION_ID = "call_session_id";
    public static final String JRCALL_EXTRA_PHONE_NUMBER = "call_phone_number";
    public static final String JRCALL_EXTRA_IS_VIDEO = "call_is_video";
    public static final String JRCALL_EXTRA_IS_MULTI = "call_is_multi";
    public static final String JRCALL_EXTRA_TOKEN = "call_token";

    public static final String GROUP_CHAT_ID = "groupChatId";
    public static final String SESSION_IDENTITY = "sessionIdentity";
    public static final String SUBJECT = "subject";
    public static final String GROUP_VERSION = "groupVersion";

    //messageType
    public static final int MESSAGE_TYPE_UNKNOWN = 0; //未知文件类型
    public static final int MESSAGE_TYPE_PMSG = 1; //page消息
    public static final int MESSAGE_TYPE_LMSG = 2; //large消息
    public static final int MESSAGE_TYPE_IMAGE = 3; //图片消息
    public static final int MESSAGE_TYPE_VIDEO = 4; //视频消息
    public static final int MESSAGE_TYPE_AUDIO = 5; //音频消息
    public static final int MESSAGE_TYPE_VCARD = 6; //名片消息
    public static final int MESSAGE_TYPE_GEO = 7; //地理位置消息
    public static final int MESSAGE_TYPE_OTHER_FILE = 8; //替他类型文件消息
    public static final int MESSAGE_TYPE_SYSTEM = 9; //系统消息

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


    public static final int REQUEST_ADD_CALL = 7;
    public static final int REQUEST_ADD_MEMBER = 8;
    public static final int REQUEST_TO_MULTI_CALL = 9;
}
