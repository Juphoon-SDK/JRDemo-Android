package com.juphoon.jrsdk.model;

import android.text.TextUtils;

import com.juphoon.rcs.jrsdk.JRGroupMember;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Upon on 2018/4/9.
 */

public class RealmGroupMember extends RealmObject {
    public static final String TABLE_NAME = "RealmGroupMember";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_SESSIDENTITY = "sessIdentity";
    public static final String FIELD_GROUP_CHAT_ID = "groupChatId";
    public static final String FIELD_NUMBER = "number";
    public static final String FIELD_DISPLAY_NAME = "displayName";

    @PrimaryKey
    private String uid;
    private String sessIdentity;
    private String groupChatId;
    private String number;
    private String displayName;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSessIdentity() {
        return sessIdentity;
    }

    public void setSessIdentity(String sessIdentity) {
        this.sessIdentity = sessIdentity;
    }

    public String getGroupChatId() {
        return groupChatId;
    }

    public void setGroupChatId(String groupChatId) {
        this.groupChatId = groupChatId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public static JRGroupMember realm2Item(RealmGroupMember realmMember) {
        JRGroupMember member = new JRGroupMember();
        member.groupChatId = realmMember.getGroupChatId();
        member.sessIdentity = realmMember.getSessIdentity();
        member.number = realmMember.getNumber();
        member.displayName = realmMember.getDisplayName();
        return member;
    }

    public static RealmGroupMember item2Realm(JRGroupMember member, RealmGroupMember realmMember) {
        realmMember.setSessIdentity(member.sessIdentity);
        realmMember.setNumber(member.number);
        if (!TextUtils.isEmpty(member.groupChatId)) {
            realmMember.setGroupChatId(member.groupChatId);
        }
        if (!TextUtils.isEmpty(member.displayName)) {
            realmMember.setDisplayName(member.displayName);
        }
        return realmMember;
    }
}
