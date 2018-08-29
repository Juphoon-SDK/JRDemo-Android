package com.juphoon.jrsdk.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmConversation extends RealmObject {

    public static final String TABLE_NAME = "RealmConversation";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_CONVERSATION_NAME = "conversationName";
    public static final String FIELD_PEER_PHONE = "peerPhone";
    public static final String FIELD_SESSIONIDENTITY = "sessionIdentity";
    public static final String FIELD_UPDATE_TIME = "updateTime";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_IS_NOTIFY = "isNotify";
    public static final String FIELD_IS_STICK = "isStick";
    public static final String FIELD_IS_SHOW_NAME = "isShowName";
    public static final String FIELD_UNREAD_COUNT = "unReadCount";
    public static final String FIELD_CHAT_TYPE = "chatType";
    public static final String FIELD_ISINVITE = "isInvite";

    @PrimaryKey
    private String uid;
    private String conversationName;
    private String peerPhone;
    private String sessionIdentity;
    private long updateTime;
    private String lastMessage;
    private boolean isNotify;
    private boolean isStick;
    private boolean isShowName;
    private int unReadCount;
    private int chatType;
    private boolean isInvite = false; // 群邀请

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public String getPeerPhone() {
        return peerPhone;
    }

    public void setPeerPhone(String peerPhone) {
        this.peerPhone = peerPhone;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isNotify() {
        return isNotify;
    }

    public void setNotify(boolean notify) {
        isNotify = notify;
    }

    public boolean isStick() {
        return isStick;
    }

    public void setStick(boolean stick) {
        isStick = stick;
    }

    public boolean isShowName() {
        return isShowName;
    }

    public void setShowName(boolean showName) {
        isShowName = showName;
    }

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public String getSessionIdentity() {
        return sessionIdentity;
    }

    public void setSessionIdentity(String sessionIdentity) {
        this.sessionIdentity = sessionIdentity;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public boolean isInvite() {
        return isInvite;
    }

    public void setInvite(boolean invite) {
        isInvite = invite;
    }
}
