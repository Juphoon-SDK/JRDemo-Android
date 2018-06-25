package com.juphoon.rcs.jrdemo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cmcc.sso.sdk.auth.AuthnConstants;
import com.cmcc.sso.sdk.auth.TokenListener;
import com.cmcc.sso.sdk.util.SsoSdkConstants;
import com.juphoon.rcs.JRAccount;
import com.juphoon.rcs.JRAccountConstants;
import com.juphoon.rcs.JRClient;
import com.juphoon.rcs.JRClientCallback;
import com.juphoon.rcs.JRClientConstants;
import com.juphoon.rcs.JRGroup;
import com.juphoon.rcs.JRGroupCallback;
import com.juphoon.rcs.JRGroupContants;
import com.juphoon.rcs.JRGroupItem;
import com.juphoon.rcs.JRLog;
import com.juphoon.rcs.JRMessage;
import com.juphoon.rcs.JRMessageCallback;
import com.juphoon.rcs.JRMessageItem;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.CommonValue;
import common.RealmDataHelper;
import common.RealmHelper;
import common.utils.CmccTokenManager;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, JRClientCallback, JRMessageCallback, JRGroupCallback {
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 100;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Realm mRealm;
    private PermissionListener listener = new PermissionListener() {

        @Override
        public void onSucceed(int requestCode, List<String> grantPermissions) {
            if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
                initSDK();
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
                .requestCode(REQUEST_WRITE_EXTERNAL_STORAGE)
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
    protected void onDestroy() {
        JRGroup.getInstance().removeCallback(this);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                startActivity(new Intent(MainActivity.this, AccountListActivity.class));
                break;
            case 1:
                if (JRClient.getInstance().getState() != JRClientConstants.State.REGED) {
                    Toast.makeText(this, "当前无账号登录", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(MainActivity.this, JRMessageListActivity.class));
                break;
            case 2:
                if (JRClient.getInstance().getState() != JRClientConstants.State.REGED) {
                    Toast.makeText(this, "当前无账号登录", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(MainActivity.this, JRCallLogActivity.class));
                break;
            case 3:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
        }
    }

    @Override
    public void onClientInitResult(boolean result, JRClientConstants.Reason reason) {
        if (result) {
            MainApplication.sSdkInitSucceed = true;
            Toast.makeText(this, "sdk初始化成功", Toast.LENGTH_SHORT).show();
            RealmDataHelper.init(this);
            JRMessage.getInstance().addCallback(this);
            JRGroup.getInstance().addCallback(this);
            initViews();
        } else {
            JRLog.log(TAG, "onClientInitResult: " + reason);
        }
    }

    @Override
    public void onClientLoginResult(boolean result, JRClientConstants.RegErrorCode reason) {
        if (result) {
            mRealm = RealmHelper.getInstance();
                CmccTokenManager.getInstance(MainActivity.this).getAppPassword(null, new TokenListener() {
                    @Override
                    public void onGetTokenComplete(JSONObject jsonObject) {
                        if (jsonObject == null) {
                            return;
                        }
                        JRLog.log("token", jsonObject.toString());
                        int resultCode = jsonObject.optInt(SsoSdkConstants.VALUES_KEY_RESULT_CODE, -1);
                        if (resultCode == AuthnConstants.CLIENT_CODE_SUCCESS) {
                            String token = jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN);
                            JRAccount.getInstance().setAccountConfig(JRClient.getInstance().getCurAccount(), JRAccountConstants.JRAccountConfigHttpToken, token);
                        }
                    }
                });
        }
    }

    @Override
    public void onClientLogoutResult(JRClientConstants.RegErrorCode reason) {

    }

    @Override
    public void onClientStateChange(JRClientConstants.State state) {

    }

    private void initSDK() {
        JRClient.getInstance().addCallback(this);
        JRClient.getInstance().startInitWithAppkey(this);
    }

    private void initViews() {
        ListView mListView = (ListView) findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_expandable_list_item_1);
        adapter.add("账号管理");
        adapter.add("消息模块");
        adapter.add("通话模块");
        adapter.add("关于");
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onTextMessageUpdate(JRMessageItem message) {
        if (message != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, message);
        }
    }

    @Override
    public void onFileMessageUpdate(JRMessageItem message) {
        if (message != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, message);
        }
    }

    @Override
    public void onGeoMessageUpdate(JRMessageItem message) {
        if (message != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, message);
        }
    }

    @Override
    public void onTextMessageReceive(JRMessageItem message) {
        if (message != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, message);
        }
    }

    @Override
    public void onFileMessageReceive(JRMessageItem message) {
        if (message != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, message);
        }
    }

    @Override
    public void onGeoMessageReceive(JRMessageItem message) {
        if (message != null) {
            RealmDataHelper.insertOrUpdateMessage(mRealm, message);
        }
    }

    @Override
    public void onOfflineMessageReceive(ArrayList<JRMessageItem> items) {
        if (items.size() > 0) {
            RealmDataHelper.insertOrUpdateMessages(mRealm, items);
        }
    }

    @Override
    public void onGroupAdd(JRGroupItem jrGroupItem) {
        if (jrGroupItem != null) {
            if (jrGroupItem.groupState == JRGroupContants.GroupStatus.INVITED) {
                RealmDataHelper.dealGroupInvite(mRealm, jrGroupItem);
            }
            RealmDataHelper.insertOrUpdateGroupAsync(mRealm, jrGroupItem, true);
        }
    }

    @Override
    public void onGroupRemove(JRGroupItem jrGroupItem) {
        if (jrGroupItem != null) {
            RealmDataHelper.deleteGroupAsync(mRealm, jrGroupItem);
        }
    }

    @Override
    public void onGroupUpdate(JRGroupItem jrGroupItem, boolean b) {
        if (jrGroupItem != null) {
            if (jrGroupItem.groupState != JRGroupContants.GroupStatus.INVITED) {
                RealmDataHelper.updateIsInvite(mRealm, jrGroupItem.sessIdentity, false);
            }
            RealmDataHelper.insertOrUpdateGroupAsync(mRealm, jrGroupItem, b);
        }
    }

    @Override
    public void onGroupOperationResult(int i, boolean b, int i1, String s) {

    }

    @Override
    public void onGroupListSubResult(boolean succ, ArrayList<JRGroupItem> arrayList) {
        if (succ) {
            RealmDataHelper.updateGroups(mRealm, arrayList);
        }
    }
}
