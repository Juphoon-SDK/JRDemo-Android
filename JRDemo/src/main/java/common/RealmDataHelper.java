package common;

import android.content.Context;
import android.text.TextUtils;

import com.juphoon.rcs.JRCallItem;
import com.juphoon.rcs.JRGroupContants;
import com.juphoon.rcs.JRGroupItem;
import com.juphoon.rcs.JRLog;
import com.juphoon.rcs.JRMessageContants;
import com.juphoon.rcs.JRMessageItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import common.model.RealmCallLog;
import common.model.RealmConversation;
import common.model.RealmGroup;
import common.model.RealmGroupMember;
import common.model.RealmMessage;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Upon on 2018/2/27.
 */

public class RealmDataHelper {
    private static Context sContext;

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
            if (realmGroup != null && realmGroup.isValid()) {
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
            if (realmGroup != null && realmGroup.isValid()) {
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
                    RealmGroup realmGroup = RealmGroup.item2Realm(newGroups.get(key), group);
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
                RealmGroup realmGroup = RealmGroup.item2Realm(groupItem, group);
                realm.insertOrUpdate(realmGroup);

                if (groupItem.members == null || groupItem.members.size() == 0) {
                    return;
                }
                HashMap<String, JRGroupItem.Member> oldMembers = new HashMap<>(); // 记录本地群成员(活跃状态)
                HashMap<String, JRGroupItem.Member> newMembers = new HashMap<>(); // 记录在群内的成员
                HashMap<String, JRGroupItem.Member> delMembers = new HashMap<>(); // 记录删除成员

                RealmResults<RealmGroupMember> realmOldMembers = realm.where(RealmGroupMember.class)
                        .equalTo(RealmGroupMember.FIELD_SESSIDENTITY, groupItem.sessIdentity).findAll();
                for (RealmGroupMember realmOldMember : realmOldMembers) {
                    JRGroupItem.Member jrGroupMember = RealmGroupMember.realm2Item(realmOldMember);
                    oldMembers.put(jrGroupMember.number, jrGroupMember);
                }
                for (JRGroupItem.Member member : groupItem.members) {
                    if (member.state == JRGroupContants.PartpStatus.EXIST) {
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
                    for (JRGroupItem.Member member : groupItem.members) {
                        if (member.state == JRGroupContants.PartpStatus.NOT_EXIST) {
                            delMembers.put(member.number, member);
                        }
                    }
                }
                for (String key : newMembers.keySet()) {
                    insertOrUpdateGroupMember(realm, newMembers.get(key));
                }
                for (String key : delMembers.keySet()) {
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
    }

    public static void insertOrUpdateGroupMember(Realm realm, final JRGroupItem.Member memberItem) {
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
        JRLog.log("", realmGroupMember.getNumber());
        realm.insertOrUpdate(realmGroupMember);

    }

    public static void deleteGroupMember(Realm realm, final JRGroupItem.Member memberItem) {
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
                conversation.setConversationName(groupItem.subject);
                conversation.setChatType(JRMessageContants.ChatType.TYPE_GROUP);
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
                RealmCallLog callLog = realm.where(RealmCallLog.class).equalTo(RealmCallLog.FILED_START_TIME, item.getBeginTime()).findFirst();
                if (callLog != null) {
                    long talkingTime = item.getTalkingBeginTime();
                    long endTime = item.getEndTime();
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
}
