package com.juphoon.jrsdk.model;

import android.text.TextUtils;

import com.juphoon.rcs.jrsdk.JRGroupItem;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Upon on 2018/4/9.
 */

public class RealmGroup extends RealmObject {

    public static final String TABLE_NAME = "RealmGroup";
    public static final String FIELD_SESSIDENTITY = "sessIdentity";
    public static final String FIELD_GROUP_CHAT_ID = "groupChatId";
    public static final String FIELD_SUBJECT = "subject";
    public static final String FIELD_CHAIRMAN_NUMBER = "chairmanNumber";
    public static final String FIELD_SELF_NUMBER = "selfNumber";
    public static final String FIELD_GROUP_VERSION = "groupVersion";
    public static final String FIELD_GROUP_TYPE = "groupType";
    public static final String FIELD_GROUP_STATE = "groupState";

    @PrimaryKey
    private String sessIdentity;
    private String groupChatId;
    private String subject;
    private String chairmanNumber;
    private int groupVersion;
    private int groupType;
    private int groupState;

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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getChairmanNumber() {
        return chairmanNumber;
    }

    public void setChairmanNumber(String chairmanNumber) {
        this.chairmanNumber = chairmanNumber;
    }

    public int getGroupVersion() {
        return groupVersion;
    }

    public void setGroupVersion(int groupVersion) {
        this.groupVersion = groupVersion;
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public int getGroupState() {
        return groupState;
    }

    public void setGroupState(int groupState) {
        this.groupState = groupState;
    }

    public static JRGroupItem realm2Item(RealmGroup group) {
        if (group == null) {
            return null;
        }
        JRGroupItem item = new JRGroupItem();
        item.groupChatId = group.getGroupChatId();
        item.groupVersion = group.getGroupVersion();
        item.sessIdentity = group.getSessIdentity();
        item.subject = group.getSubject();
        item.chairMan = group.getChairmanNumber();
        item.groupState = group.getGroupState();
        return item;
    }

    public static RealmGroup item2Realm(JRGroupItem item, RealmGroup realmGroup,String oldSubject) {
        if (item == null) {
            return null;
        }
        if(TextUtils.isEmpty(realmGroup.getSessIdentity())) {
            realmGroup.setSessIdentity(item.sessIdentity);
        }
        if(!TextUtils.isEmpty(oldSubject)) {
            realmGroup.setSubject(oldSubject);
        }
        if (!TextUtils.isEmpty(item.subject)) {
            realmGroup.setSubject(item.subject);
        }
        if (!TextUtils.isEmpty(item.groupChatId)) {
            realmGroup.setGroupChatId(item.groupChatId);
        }
        if (!TextUtils.isEmpty(item.chairMan)) {
            realmGroup.setChairmanNumber(item.chairMan);
        }
        realmGroup.setGroupVersion(item.groupVersion);
        realmGroup.setGroupState(item.groupState);
        return realmGroup;
    }
}
