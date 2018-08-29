package com.juphoon.jrsdk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmcc.sso.sdk.auth.AuthnConstants;
import com.cmcc.sso.sdk.auth.TokenListener;
import com.cmcc.sso.sdk.util.SsoSdkConstants;
import com.juphoon.jrsdk.manager.CmccTokenManager;
import com.juphoon.jrsdk.utils.CommonValue;
import com.juphoon.jrsdk.utils.VCardUtils;
import com.juphoon.rcs.jrsdk.JRClient;
import com.juphoon.rcs.jrsdk.JRClientCallback;
import com.juphoon.rcs.jrsdk.JRClientConstants;
import com.juphoon.rcs.jrsdk.JRGroup;
import com.juphoon.rcs.jrsdk.JRGroupCallback;
import com.juphoon.rcs.jrsdk.JRGroupConstants;
import com.juphoon.rcs.jrsdk.JRGroupItem;
import com.juphoon.rcs.jrsdk.JRMessage;
import com.juphoon.rcs.jrsdk.JRMessageCallback;
import com.juphoon.rcs.jrsdk.JRMessageConstants;
import com.juphoon.rcs.jrsdk.JRMessageItem;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements JRClientCallback, JRMessageCallback, JRGroupCallback {
    private Realm mRealm;

    private PermissionListener listener = new PermissionListener() {

        @Override
        public void onSucceed(int requestCode, List<String> grantPermissions) {
            if (requestCode == 100) {
                initViews();
                JRMessage.getInstance().addCallback(MainActivity.this);
                JRGroup.getInstance().addCallback(MainActivity.this);
                JRClient.getInstance().addCallback(MainActivity.this);
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndPermission.with(this)
                .requestCode(100)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CHANGE_NETWORK_STATE)
                .send();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        JRMessage.getInstance().removeCallback(MainActivity.this);
        JRGroup.getInstance().removeCallback(MainActivity.this);
        JRClient.getInstance().removeCallback(MainActivity.this);
        super.onDestroy();
    }

    @Override
    public void onClientInitResult(boolean result, int reason) {
        Toast.makeText(this, result + "", Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        ListView mListView = findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_expandable_list_item_1);
        adapter.add("账号管理");
        adapter.add("消息模块");
        adapter.add("通话模块");
        adapter.add("头像模块");
        adapter.add("能力模块");
        adapter.add("关于");
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, AccountListActivity.class));
                        break;
                    case 1:
                        if (JRClient.getInstance().getState() != JRClientConstants.CLIENT_STATE_LOGINED) {
                            Toast.makeText(MainActivity.this, "当前无账号登录", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //示例代码
                        startActivity(new Intent(MainActivity.this, JRMessageListActivity.class));
                        break;
                    case 2:
                        if (JRClient.getInstance().getState() != JRClientConstants.CLIENT_STATE_LOGINED) {
                            Toast.makeText(MainActivity.this, "当前无账号登录", Toast.LENGTH_SHORT).show();
                            return;
                        }
//                        startActivity(new Intent(MainActivity.this, JRCallLogActivity.class));
                        showCallDialog();
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        break;
                    case 4:
                        startActivity(new Intent(MainActivity.this, CapActivity.class));
                        break;
                    case 5:
                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        break;
                }
            }
        });
    }

    @Override
    public void onClientLoginResult(boolean result, int reason) {
        mRealm = RealmHelper.getInstance();
    }

    @Override
    public void onClientLogoutResult(int reason) {

    }

    @Override
    public void onClientStateChange(int state) {

    }

    @Override
    public void onTextMessageReceived(JRMessageItem item) {
        if (item != null) {
            if (item instanceof JRMessageItem.TextItem) {
                JRMessageItem.TextItem textItem = (JRMessageItem.TextItem) item;
                if (textItem.isAtMsg) {
                    String before = textItem.content;
                    textItem.content = "[有人@我] " + before;
                    RealmDataHelper.insertOrUpdateMessage(mRealm, textItem);
                    return;
                }
                if (textItem.contentType == JRMessageConstants.MessageContentType.AGREE_EXCHANGE_VCARD) {
                    RealmDataHelper.insertOrUpdateMessage(mRealm, textItem);
                    return;
                } else if (textItem.contentType == JRMessageConstants.MessageContentType.EXCHANGE_VCARD) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("名片交换");
                    builder.setMessage("有来自群名片交换的信息,是否接受");
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String selfContent = VCardUtils.createSelfVCardContent("未知", JRClient.getInstance().getCurrentNumber());
                            HashMap<String, Object> params = new HashMap<>();
                            params.put(JRMessageConstants.ExtraKey.TEXT_CONTENT_TYPE, JRMessageConstants.MessageContentType.AGREE_EXCHANGE_VCARD);
                            params.put(JRMessageConstants.ExtraKey.PEER_NUMBERS, textItem.senderNumber);
                            params.put(JRMessageConstants.ExtraKey.CONVERSATION_ID, textItem.convId);
                            JRMessage.getInstance().sendTextMessage(selfContent, JRMessageConstants.ChannelType.TYPE_ONE, params);
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.create().show();
                    return;
                }
            }
            RealmDataHelper.insertOrUpdateMessage(mRealm, item);
        }
    }

    @Override
    public void onTextMessageUpdate(JRMessageItem item) {
        if (item != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, item);
        }
    }

    @Override
    public void onGeoMessageReceived(JRMessageItem item) {
        if (item != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, item);
        }
    }

    @Override
    public void onGeoMessageUpdate(JRMessageItem item) {
        if (item != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, item);
        }
    }

    @Override
    public void onFileMessageReceived(JRMessageItem item) {
        if (item != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, item);
        }
    }

    @Override
    public void onFileMessageUpdate(JRMessageItem item) {
        if (item != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, item);
        }
    }

    @Override
    public void onOfflineMessageReceive(ArrayList<JRMessageItem> items) {
        RealmDataHelper.insertOrUpdateMessages(mRealm, items);
    }

    @Override
    public void onCommandReceive(String imdnId, String peerNumber, int command) {
        if (imdnId != null) {
            if (command == JRMessageConstants.CommandType.TYPE_REVOKE) {
                RealmDataHelper.setMessageToRevoed(mRealm, imdnId, peerNumber);
            }
        }
        if (command == JRMessageConstants.CommandType.TYPE_COMPLAIN) {
            Toast.makeText(this, "举报结果 " + peerNumber, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGroupAdd(JRGroupItem jrGroupItem) {
        if (jrGroupItem != null) {
            if (jrGroupItem.groupState == JRGroupConstants.GROUP_STATUS_INVITED) {
                RealmDataHelper.dealGroupInvite(mRealm, jrGroupItem);
            }
            RealmDataHelper.insertOrUpdateGroupAsync(mRealm, jrGroupItem, true);
        }
    }

    @Override
    public void onGroupRemove(JRGroupItem item) {
        if (item != null) {
            RealmDataHelper.deleteGroupAsync(mRealm, item);
        }
    }

    @Override
    public void onGroupUpdate(JRGroupItem jrGroupItem, boolean isFully) {
        if (jrGroupItem != null) {
            if (jrGroupItem.groupState != JRGroupConstants.GROUP_STATUS_INVITED) {
                RealmDataHelper.updateIsInvite(mRealm, jrGroupItem.sessIdentity, false);
            }
            RealmDataHelper.insertOrUpdateGroupAsync(mRealm, jrGroupItem, isFully);
        }
    }

    @Override
    public void onGroupOperationResult(int operationType, boolean succeed, int reason, String sessionIdentity) {
        if (operationType == JRGroupConstants.GROUP_OPERATION_TYPE_INVITE) {
            if (succeed) {
                Toast.makeText(this, "邀请成员成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "邀请成员失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onGroupListSubResult(boolean succeed, ArrayList<JRGroupItem> groupList) {
        if (succeed) {
            RealmDataHelper.updateGroups(mRealm, groupList);
        }
    }

    private void showCallDialog() {
        View parentView = LayoutInflater.from(this).inflate(R.layout.layout_choice, null, true);
        final TextView tv = parentView.findViewById(R.id.number);
        final CheckBox isVideo = parentView.findViewById(R.id.is_video);
        final CheckBox isMulti = parentView.findViewById(R.id.is_multi);
        final CheckBox isMcu = parentView.findViewById(R.id.is_mcu);
        isMcu.setVisibility(View.GONE);
        new AlertDialog.Builder(this)
                .setTitle("请输入")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(parentView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!TextUtils.isEmpty(tv.getText())) {
                            String peernumber = tv.getText().toString();
                            final Intent intent = new Intent(MainActivity.this, JRCallActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra(CommonValue.JRCALL_EXTRA_PHONE_NUMBER, peernumber);
                            intent.putExtra(CommonValue.JRCALL_EXTRA_IS_MULTI, isMulti.isChecked());
                            intent.putExtra(CommonValue.JRCALL_EXTRA_IS_VIDEO, isVideo.isChecked());
                            intent.putExtra(CommonValue.JRCALL_EXTRA_IS_MCU, isMcu.isChecked());
                            if (isMulti.isChecked() && isVideo.isChecked()) {
                                CmccTokenManager.getInstance(MainActivity.this).getAppToken(null, new TokenListener() {
                                    @Override
                                    public void onGetTokenComplete(JSONObject jsonObject) {
                                        if (jsonObject == null) {
                                            return;
                                        }
                                        int resultCode = jsonObject.optInt(SsoSdkConstants.VALUES_KEY_RESULT_CODE, -1);
                                        if (resultCode == AuthnConstants.CLIENT_CODE_SUCCESS) {
                                            HashMap<String, String> param = new HashMap<>();
                                            String token = jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN);
                                            intent.putExtra(CommonValue.JRCALL_EXTRA_TOKEN, token);
                                            startActivity(intent);
                                        } else {
                                            // 统一认证失败
                                        }
                                    }
                                });
                            } else {
                                startActivity(intent);
                            }
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
