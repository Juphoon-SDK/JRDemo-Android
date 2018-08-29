package com.juphoon.jrsdk;

import android.content.Context;
import android.text.TextUtils;

import com.juphoon.jrsdk.model.RealmCallLog;
import com.juphoon.jrsdk.model.RealmConversation;
import com.juphoon.jrsdk.model.RealmGroup;
import com.juphoon.jrsdk.model.RealmGroupMember;
import com.juphoon.jrsdk.model.RealmMessage;
import com.juphoon.jrsdk.utils.CommonValue;
import com.juphoon.rcs.jrsdk.JRCallItem;
import com.juphoon.rcs.jrsdk.JRGroupConstants;
import com.juphoon.rcs.jrsdk.JRGroupItem;
import com.juphoon.rcs.jrsdk.JRGroupMember;
import com.juphoon.rcs.jrsdk.JRMessageConstants;
import com.juphoon.rcs.jrsdk.JRMessageItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Upon on 2018/2/27.
 */

public class RealmDataHelper {
    private static Context sContext;
    private static final int SYS_TYPE_JOIN = 0;
    private static final int SYS_TYPE_LEAVE = 1;
    private static final int SYS_TYPE_SUBJECT = 2;
    private static final int SYS_TYPE_CHAIR_MAN = 3;

    public static void init(Context context) {
        sContext = context;
    }

    public static void insertOrUpdateMessage(Realm realm, final JRMessageItem messageItem) {
        if (realm == null) {
            return;
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmMessage message = RealmMessage.itemToMessage(messageItem);
                realm.insertOrUpdate(message);
                insertOrUpdateConversation(realm, message);
            }
        });
    }

    public static void deleteMessage(Realm realm, String imdnId, String peerNumber) {
        if (realm == null) {
            return;
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmMessage message = realm.where(RealmMessage.class)
                        .equalTo(RealmMessage.FIELD_IMDN_ID, imdnId)
                        .findFirst();
                if (message != null) {
                    message.deleteFromRealm();
                }
            }
        });
    }

    public static void setMessageToRevoed(Realm realm, String imdnId, String peerNumber) {
        if (realm == null) {
            return;
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmMessage message = realm.where(RealmMessage.class)
                        .equalTo(RealmMessage.FIELD_IMDN_ID, imdnId)
                        .findFirst();
                if (message != null) {
                    RealmMessage realmMessage = new RealmMessage();
                    if (message.getSessionIdentity() != null) {
                        realmMessage.setSessionIdentity(message.getSessionIdentity());
                        realmMessage.setTimeStamp(System.currentTimeMillis());
                        realmMessage.setSenderPhone(peerNumber);
                        realmMessage.setContent(peerNumber + "撤回一条消息");
                        realmMessage.setSessionIdentity(message.getSessionIdentity());
                        insertOrUpdateConversation(realm, realmMessage);
                    }
                    message.setRevoked(true);
                    message.setContent(peerNumber + "撤回一条消息");
                }
            }
        });
    }

    public static void insertOrUpdateMessages(Realm realm, final ArrayList<JRMessageItem> messageItems) {
        if (realm == null) {
            return;
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ArrayList<RealmMessage> messages = new ArrayList<>();
                for (JRMessageItem messageItem : messageItems) {
                    RealmMessage message = RealmMessage.itemToMessage(messageItem);
                    messages.add(message);
                    insertOrUpdateConversation(realm, message);
                }
                realm.insertOrUpdate(messages);
            }
        });
    }

    public static void insertOrUpdateConversation(Realm realm, final RealmMessage message) {
        RealmConversation conversation = null;
        RealmGroup realmGroup = null;
        if (!TextUtils.isEmpty(message.getSessionIdentity())) {
            conversation = realm.where(RealmConversation.class)
                    .equalTo(RealmConversation.FIELD_SESSIONIDENTITY, message.getSessionIdentity()).findFirst();
            realmGroup = realm.where(RealmGroup.class).equalTo(RealmGroup.FIELD_SESSIDENTITY, message.getSessionIdentity()).findFirst();
        } else if (!TextUtils.isEmpty(message.getPeerPhone())) {
            conversation = realm.where(RealmConversation.class)
                    .equalTo(RealmConversation.FIELD_PEER_PHONE, message.getPeerPhone()).findFirst();
        }
        if (conversation != null) {
            conversation.setUpdateTime(System.currentTimeMillis());
            conversation.setLastMessage(message.getContent());
            if (realmGroup != null && realmGroup.isValid() && !TextUtils.isEmpty(realmGroup.getSubject())) {
                conversation.setConversationName(realmGroup.getSubject());
            }
        } else {
            conversation = new RealmConversation();
            if (!TextUtils.isEmpty(message.getPeerPhone())) {
                conversation.setPeerPhone(message.getPeerPhone());
            }
            if (!TextUtils.isEmpty(message.getSessionIdentity())) {
                conversation.setSessionIdentity(message.getSessionIdentity());
            }
            if (realmGroup != null && realmGroup.isValid() && !TextUtils.isEmpty(realmGroup.getSubject())) {
                conversation.setConversationName(realmGroup.getSubject());
            }
            conversation.setUid(System.currentTimeMillis() + "");
            conversation.setLastMessage(message.getContent());
            conversation.setUpdateTime(System.currentTimeMillis());
        }
        realm.insertOrUpdate(conversation);
    }

    private static RealmConversation tectonicConversation(RealmMessage message) {
        RealmConversation conversation = new RealmConversation();
        conversation.setPeerPhone(message.getPeerPhone());
        conversation.setUid(message.getPeerPhone());
        if (!TextUtils.isEmpty(message.getContent())) {
            conversation.setLastMessage(message.getContent() + "");
        }
        conversation.setUpdateTime(message.getTimeStamp());
        return conversation;
    }

    public static void updateGroups(Realm realm, final ArrayList<JRGroupItem> groups) {
        if (realm == null) {
            return;
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<RealmGroup> realmGroups = realm.where(RealmGroup.class).findAll();
                HashMap<String, JRGroupItem> oldGroups = new HashMap<>(); // 记录本地群
                HashMap<String, JRGroupItem> newGroups = new HashMap<>(); // 记录所有群
                HashMap<String, JRGroupItem> delGroups = new HashMap<>(); // 记录删除群

                for (RealmGroup group : realmGroups) {
                    JRGroupItem jrGroupItem = RealmGroup.realm2Item(group);
                    oldGroups.put(jrGroupItem.groupChatId, jrGroupItem);
                }
                for (JRGroupItem groupItem : groups) {
                    newGroups.put(groupItem.groupChatId, groupItem);
                }
                for (String key : oldGroups.keySet()) {
                    if (!newGroups.containsKey(key)) {
                        delGroups.put(key, oldGroups.get(key));
                    }
                }
                for (String key : newGroups.keySet()) {
                    RealmGroup group = realm.where(RealmGroup.class).equalTo(RealmGroup.FIELD_SESSIDENTITY,
                            newGroups.get(key).sessIdentity).findFirst();
                    if (group == null || !group.isValid()) {
                        group = new RealmGroup();
                    }
                    RealmGroup realmGroup = RealmGroup.item2Realm(newGroups.get(key), group, "");
                    realm.insertOrUpdate(realmGroup);
                }
                for (String key : delGroups.keySet()) {
                    deleteGroup(realm, delGroups.get(key));

                }
            }
        });
    }

    public static void insertOrUpdateGroupAsync(Realm realm, final JRGroupItem groupItem, final boolean isFully) {
        if (realm == null) {
            return;
        }
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmGroup group = realm.where(RealmGroup.class).equalTo(RealmGroup.FIELD_SESSIDENTITY, groupItem.sessIdentity).findFirst();
                if (group == null || !group.isValid()) {
                    group = new RealmGroup();
                }
                String name = group.getSubject();
                if (!TextUtils.isEmpty(group.getSubject()) && !TextUtils.equals(group.getSubject(), groupItem.subject)) {
                    insertSystemMsg(null, groupItem.sessIdentity, groupItem, SYS_TYPE_SUBJECT, realm);
                }

                if (!TextUtils.isEmpty(group.getChairmanNumber()) && !TextUtils.equals(group.getChairmanNumber(), groupItem.chairMan)) {
                    insertSystemMsg(null, groupItem.sessIdentity, groupItem, SYS_TYPE_CHAIR_MAN, realm);
                }
                RealmGroup realmGroup = RealmGroup.item2Realm(groupItem, group, name);
                realm.insertOrUpdate(realmGroup);
                if (groupItem.members == null || groupItem.members.size() == 0) {
                    return;
                }
                HashMap<String, JRGroupMember> oldMembers = new HashMap<>(); // 记录本地群成员(活跃状态)
                HashMap<String, JRGroupMember> newMembers = new HashMap<>(); // 记录在群内的成员
                HashMap<String, JRGroupMember> delMembers = new HashMap<>(); // 记录删除成员

                RealmResults<RealmGroupMember> realmOldMembers = realm.where(RealmGroupMember.class)
                        .equalTo(RealmGroupMember.FIELD_SESSIDENTITY, groupItem.sessIdentity).findAll();
                for (RealmGroupMember realmOldMember : realmOldMembers) {
                    JRGroupMember jrGroupMember = RealmGroupMember.realm2Item(realmOldMember);
                    oldMembers.put(jrGroupMember.number, jrGroupMember);
                }
                for (JRGroupMember member : groupItem.members) {
                    if (!oldMembers.containsKey(member.number)) {
                        insertSystemMsg(member, member.sessIdentity, groupItem, SYS_TYPE_JOIN, realm);
                    }
                }
                for (JRGroupMember member : groupItem.members) {
                    if (member.state == JRGroupConstants.GROUP_PARTP_STATUS_EXIST) {
                        newMembers.put(member.number, member);
                    }
                }
                if (isFully) {
                    for (String key : oldMembers.keySet()) {
                        if (!newMembers.containsKey(key)) {
                            delMembers.put(key, oldMembers.get(key));
                        }
                    }
                } else {
                    for (JRGroupMember member : groupItem.members) {
                        if (member.state == JRGroupConstants.GROUP_PARTP_STATUS_NOT_EXIST) {
                            delMembers.put(member.number, member);
                        }
                    }
                }
                for (String key : newMembers.keySet()) {
                    insertOrUpdateGroupMember(realm, newMembers.get(key));
                }
                for (String key : delMembers.keySet()) {
                    insertSystemMsg(delMembers.get(key), delMembers.get(key).sessIdentity, groupItem, SYS_TYPE_LEAVE, realm);
                    deleteGroupMember(realm, delMembers.get(key));

                }
            }
        });
    }

    public static void deleteGroupAsync(Realm realm, final JRGroupItem groupItem) {
        if (realm == null) {
            return;
        }
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmGroup realmGroup = realm.where(RealmGroup.class).
                        equalTo(RealmGroup.FIELD_SESSIDENTITY, groupItem.sessIdentity).findFirst();
                if (realmGroup != null) {
                    realmGroup.deleteFromRealm();
                }
                RealmConversation conversation = realm.where(RealmConversation.class).
                        equalTo(RealmConversation.FIELD_SESSIONIDENTITY, groupItem.sessIdentity).findFirst();
                if (conversation != null) {
                    conversation.deleteFromRealm();
                }
                RealmResults<RealmMessage> messages = realm.where(RealmMessage.class).
                        equalTo(RealmMessage.FIELD_SESSIONIDENTITY, groupItem.sessIdentity).findAll();
                messages.deleteAllFromRealm();
            }
        });
    }

    public static void deleteGroup(Realm realm, final JRGroupItem groupItem) {
        if (realm == null) {
            return;
        }
        RealmGroup realmGroup = realm.where(RealmGroup.class).
                equalTo(RealmGroup.FIELD_SESSIDENTITY, groupItem.sessIdentity).findFirst();
        if (realmGroup != null) {
            realmGroup.deleteFromRealm();
        }
        RealmConversation conversation = realm.where(RealmConversation.class).
                equalTo(RealmConversation.FIELD_SESSIONIDENTITY, groupItem.sessIdentity).findFirst();
        if (conversation != null) {
            conversation.deleteFromRealm();
        }
        RealmResults<RealmMessage> messages = realm.where(RealmMessage.class).
                equalTo(RealmMessage.FIELD_SESSIONIDENTITY, groupItem.sessIdentity).findAll();
        messages.deleteAllFromRealm();
    }

    public static void insertOrUpdateGroupMember(Realm realm, final JRGroupMember memberItem) {
        if (realm == null) {
            return;
        }
        RealmGroupMember realmGroupMember = realm.where(RealmGroupMember.class)
                .equalTo(RealmGroupMember.FIELD_SESSIDENTITY, memberItem.sessIdentity)
                .equalTo(RealmGroupMember.FIELD_NUMBER, memberItem.number).findFirst();
        if (realmGroupMember == null || !realmGroupMember.isValid()) {
            realmGroupMember = new RealmGroupMember();
            realmGroupMember = RealmGroupMember.item2Realm(memberItem, realmGroupMember);
            realmGroupMember.setUid(UUID.randomUUID().toString());
        } else {
            realmGroupMember.setDisplayName(memberItem.displayName);
        }
        realm.insertOrUpdate(realmGroupMember);

    }

    public static void deleteGroupMember(Realm realm, final JRGroupMember memberItem) {
        if (realm == null) {
            return;
        }
        RealmGroupMember realmGroupMember = realm.where(RealmGroupMember.class)
                .equalTo(RealmGroupMember.FIELD_SESSIDENTITY, memberItem.sessIdentity)
                .equalTo(RealmGroupMember.FIELD_NUMBER, memberItem.number).findFirst();
        if (realmGroupMember != null) {
            realmGroupMember.deleteFromRealm();
        }
    }

    public static void dealGroupInvite(Realm realm, final JRGroupItem groupItem) {
        if (realm == null) {
            return;
        }
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmConversation conversation = realm.where(RealmConversation.class)
                        .equalTo(RealmConversation.FIELD_SESSIONIDENTITY, groupItem.sessIdentity).findFirst();
                if (conversation != null) {
                    conversation.setInvite(false);
                } else {
                    conversation = new RealmConversation();
                    conversation.setSessionIdentity(groupItem.sessIdentity);
                    conversation.setUid(System.currentTimeMillis() + "");
                    conversation.setInvite(true);
                }
                conversation.setUpdateTime(System.currentTimeMillis());
                if (!TextUtils.isEmpty(groupItem.subject)) {
                    conversation.setConversationName(groupItem.subject);
                }
                conversation.setChatType(JRMessageConstants.ChannelType.TYPE_GROUP);
                realm.insertOrUpdate(conversation);
            }
        });
    }

    public static void updateIsInvite(Realm realm, final String sessionIdentity, final boolean isInvite) {
        if (realm == null) {
            return;
        }
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmConversation conversation = realm.where(RealmConversation.class).equalTo(RealmConversation.FIELD_SESSIONIDENTITY,
                        sessionIdentity).findFirst();
                if (conversation != null) {
                    conversation.setInvite(isInvite);
                    realm.insertOrUpdate(conversation);
                }
            }
        });
    }

    public static void insertOrUpdateCallLog(Realm realm, final JRCallItem item) {
        if (realm == null) {
            return;
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmCallLog callLog = realm.where(RealmCallLog.class).equalTo(RealmCallLog.FILED_START_TIME, item.beginTime).findFirst();
                if (callLog != null) {
                    long talkingTime = item.talkingBeginTime;
                    long endTime = item.endTime;
                    if (talkingTime != 0) {
                        callLog.setTalkingTime(talkingTime);
                    }
                    if (endTime != 0) {
                        callLog.setEndTime(endTime);
                    }
//                    if(item.isConference()) {
//                        if(item.isVideo()) {
//                            callLog.setCallType(RealmCallLog.CALL_TYPE_CONF_VIDEO);
//                        } else {
//                            callLog.setCallType(RealmCallLog.CALL_TYPE_CONF_AUDIO);
//                        }
//                    } else {
//                        if(item.isVideo()) {
//                            callLog.setCallType(RealmCallLog.CALL_TYPE_ONE_VIDEO);
//                        } else {
//                            callLog.setCallType(RealmCallLog.CALL_TYPE_ONE_AUDIO);
//                        }
//                    }
                    realm.insertOrUpdate(callLog);
                } else {
                    RealmCallLog realmCallLog = new RealmCallLog();
                    RealmCallLog.item2Realm(item, realmCallLog);
                    realm.insertOrUpdate(realmCallLog);
                }
            }
        });
    }

    private static void insertSystemMsg(final JRGroupMember memberItem, String sessIdentity, JRGroupItem item, int type, Realm realm) {
        RealmMessage message = new RealmMessage();
        message.setImdnId(UUID.randomUUID().toString());
        message.setSessionIdentity(sessIdentity);
        message.setTimeStamp(System.currentTimeMillis());
        message.setType(CommonValue.MESSAGE_TYPE_SYSTEM);
        switch (type) {
            case SYS_TYPE_JOIN:
                message.setContent(String.format(sContext.getString(R.string.someone_join), TextUtils.isEmpty(memberItem.displayName) ? memberItem.number : memberItem.displayName));
                break;
            case SYS_TYPE_LEAVE:
                message.setContent(String.format(sContext.getString(R.string.someone_leave), TextUtils.isEmpty(memberItem.displayName) ? memberItem.number : memberItem.displayName));
                break;
            case SYS_TYPE_SUBJECT:
                if(TextUtils.isEmpty(item.subject)) {
                    return;
                }
                message.setContent(String.format(sContext.getString(R.string.subject_change), item.subject));
                break;
            case SYS_TYPE_CHAIR_MAN:
                if(TextUtils.isEmpty(item.chairMan)) {
                    return;
                }
                message.setContent(String.format(sContext.getString(R.string.chairman_change), item.chairMan));
                break;
        }
        realm.insertOrUpdate(message);
    }

}
