package common;

import android.content.Context;
import android.text.TextUtils;

import com.juphoon.rcs.JRClient;

import java.util.HashMap;
import java.util.Locale;

import common.model.RealmCallLog;
import common.model.RealmConversation;
import common.model.RealmGroup;
//import common.model.RealmMessage;
import common.model.RealmMessage;
import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Upon on 2018/2/27.
 */

public class RealmHelper {
    private volatile static RealmConfiguration sRealmConfiguration;
    private static HashMap<String, RealmConfiguration> sConfigurationMap = new HashMap<>();
    private static final int CURRENT_VERSION = 1;
    private static Context sContext;

    public static void injectContext(Context context) {
        sContext = context;
    }

    private static class Migration implements RealmMigration {

        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();
            if (oldVersion == 0) {
                schema.create(RealmConversation.TABLE_NAME)
                        .addField(RealmConversation.FIELD_UID, String.class, FieldAttribute.PRIMARY_KEY)
                        .addField(RealmConversation.FIELD_PEER_PHONE, String.class)
                        .addField(RealmConversation.FIELD_UPDATE_TIME, long.class)
                        .addField(RealmConversation.FIELD_LAST_MESSAGE, String.class)
                        .addField(RealmConversation.FIELD_IS_NOTIFY, boolean.class)
                        .addField(RealmConversation.FIELD_IS_STICK, boolean.class)
                        .addField(RealmConversation.FIELD_UNREAD_COUNT, int.class)
                        .addField(RealmConversation.FIELD_IS_SHOW_NAME, boolean.class);
                schema.create(RealmMessage.TABLE_NAME)
                        .addField(RealmMessage.FIELD_IMDN_ID, String.class, FieldAttribute.PRIMARY_KEY)
                        .addField(RealmMessage.FIELD_PEER_PHONE, String.class)
                        .addField(RealmMessage.FIELD_TYPE, int.class)
                        .addField(RealmMessage.FIELD_SENDER_PHONE, String.class)
                        .addField(RealmMessage.FIELD_CONTENT, String.class)
                        .addField(RealmMessage.FIELD_STATE, int.class)
                        .addField(RealmMessage.FIELD_TIME_STAMP, long.class)
                        .addField(RealmMessage.FIELD_DISPLAY_NAME, String.class)
                        .addField(RealmMessage.FIELD_IS_READ, boolean.class)
                        .addField(RealmMessage.FIELD_FILE_TRANS_ID, String.class)
                        .addField(RealmMessage.FIELD_FILE_NAME, String.class)
                        .addField(RealmMessage.FIELD_FILE_PATH, String.class)
                        .addField(RealmMessage.FIELD_FILE_THUMB_PATH, String.class)
                        .addField(RealmMessage.FIELD_FILE_SIZE, int.class)
                        .addField(RealmMessage.FIELD_FILE_TRANS_SIZE, int.class)
                        .addField(RealmMessage.FIELD_FILE_DURATION, int.class)
                        .addField(RealmMessage.FIELD_FILE_PROGRESS, int.class)
                        .addField(RealmMessage.FIELD_IS_BURN_AFTER_READING, boolean.class)
                        .addField(RealmMessage.FIELD_LABEL, String.class)
                        .addField(RealmMessage.FIELD_LONGITUDE, double.class)
                        .addField(RealmMessage.FIELD_LATITUDE, double.class)
                        .addField(RealmMessage.FIELD_RADIUS, float.class);
                schema.create(RealmGroup.TABLE_NAME)
                        .addField(RealmGroup.FIELD_SESSIDENTITY, String.class, FieldAttribute.PRIMARY_KEY)
                        .addField(RealmGroup.FIELD_GROUP_CHAT_ID, String.class)
                        .addField(RealmGroup.FIELD_GROUP_TYPE, int.class)
                        .addField(RealmGroup.FIELD_SUBJECT, String.class)
                        .addField(RealmGroup.FIELD_CHAIRMAN_NUMBER, String.class)
                        .addField(RealmGroup.FIELD_GROUP_VERSION, int.class)
                        .addField(RealmGroup.FIELD_GROUP_STATE, int.class);
                schema.create(RealmCallLog.TABLE_NAME)
                        .addField(RealmCallLog.FILED_START_TIME, long.class, FieldAttribute.PRIMARY_KEY)
                        .addField(RealmCallLog.FILED_CALL_STATUS, int.class)
                        .addField(RealmCallLog.FILED_PEER_NUMBER, String.class)
                        .addField(RealmCallLog.FILED_TALKING_TIME, long.class)
                        .addField(RealmCallLog.FILED_END_TIME, long.class)
                        .addField(RealmCallLog.FILED_CALL_TYPE, int.class);
                oldVersion++;
            }
            if (oldVersion < newVersion) {
                throw new IllegalStateException(String.format(Locale.ENGLISH, "Migration missing from v%d to v%d", oldVersion, newVersion));
            }
        }
    }

    public static Realm getInstance() {
        String realmFileName = JRClient.getInstance().getCurLoginNumber();
        if (TextUtils.isEmpty(realmFileName)) {
            realmFileName = "default";
        }
        return getInstance(realmFileName);
    }

    public static Realm getInstance(String realmFilePrefix) {
        String realmFileName = realmFilePrefix + ".realm";
        sRealmConfiguration = sConfigurationMap.get(realmFileName);
        if (sRealmConfiguration == null) {
            synchronized (RealmHelper.class) {
                if (sRealmConfiguration == null) {
                    sRealmConfiguration = new RealmConfiguration.Builder().
                            name(realmFileName).
                            schemaVersion(CURRENT_VERSION).
                            migration(new Migration()).
                            build();
                    sConfigurationMap.put(realmFileName, sRealmConfiguration);
                }
            }
        }
        return Realm.getInstance(sRealmConfiguration);
    }
}
