package com.juphoon.jrsdk;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmcc.sso.sdk.auth.AuthnConstants;
import com.cmcc.sso.sdk.auth.TokenListener;
import com.cmcc.sso.sdk.util.SsoSdkConstants;
import com.juphoon.cmcc.app.lemon.MtcProf;
import com.juphoon.jrsdk.manager.CmccTokenManager;
import com.juphoon.jrsdk.utils.NumberUtils;
import com.juphoon.rcs.jrsdk.JRAccount;
import com.juphoon.rcs.jrsdk.JRAutoConfig;
import com.juphoon.rcs.jrsdk.JRAutoConfigCallback;
import com.juphoon.rcs.jrsdk.JRClient;
import com.juphoon.rcs.jrsdk.JRClientCallback;
import com.juphoon.rcs.jrsdk.JRClientConstants;

import org.json.JSONObject;

public class AccountListActivity extends AppCompatActivity implements JRAutoConfigCallback, JRClientCallback, OnClickListener {
    private ListView mUserListView;
    private AccountAdapter mAdapter;
    private ProgressDialog mProgress;
    private RadioGroup mSerType;
    private String mCurLoginedUser;
    private String mLoginTag;
    private boolean mIsBusiness = true;
    private String mCachedPwd;
    private String mCachedAcc;
    private String mCachedToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        JRClient.getInstance().addCallback(this);
        JRAutoConfig.getInstance().addCallback(this);
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JRClient.getInstance().removeCallback(this);
        JRAutoConfig.getInstance().removeCallback(this);
    }

    @Override
    protected void onResume() {
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    private void initViews() {
        mSerType = (RadioGroup) findViewById(R.id.server_type);
        mSerType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_server_business) {
                    mIsBusiness = true;
                } else if (checkedId == R.id.radio_server_test) {
                    mIsBusiness = false;
                }
            }
        });
        mUserListView = (ListView) findViewById(R.id.list);
        mAdapter = new AccountAdapter(this);
        mUserListView.setAdapter(mAdapter);
        mUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (JRClient.getInstance().getState() == JRClientConstants.CLIENT_STATE_LOGINED) {
                    if (i == 0) {

                    } else if (i == 1) {
                        mProgress.setMessage("注销中");
                        mProgress.show();
                        JRClient.getInstance().logout();
                    }
                } else {
                    if (i == JRAccount.getInstance().getAccountsList().size()) {
                        // CP之前取统一认证相关参数，需要参数为账号，密码，token
                        Toast.makeText(AccountListActivity.this, "统一认证获取", Toast.LENGTH_SHORT).show();
                        CmccTokenManager.getInstance(AccountListActivity.this).getAppPassword(null, new TokenListener() {
                            @Override
                            public void onGetTokenComplete(JSONObject jsonObject) {
                                if (jsonObject == null) {
                                    return;
                                }
                                int resultCode = jsonObject.optInt(SsoSdkConstants.VALUES_KEY_RESULT_CODE, -1);
                                Log.e("yidao", "onGetTokenComplete: "+jsonObject );
                                if (resultCode == AuthnConstants.CLIENT_CODE_SUCCESS) {
                                    mCachedAcc = jsonObject.optString(SsoSdkConstants.VALUES_KEY_USERNAME);
                                    mCachedPwd = jsonObject.optString(SsoSdkConstants.VALUES_KEY_PASSWORD);
                                    mCachedToken = jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN);
                                    JRAutoConfig.getInstance().startAutoConfig(mCachedAcc, mCachedPwd, mCachedToken, mIsBusiness);
                                } else {
                                    // 统一认证失败
                                }
                            }
                        });
                    } else {
                        mProgress.setMessage("登录中");
                        mProgress.show();
//                        if(mCachedPwd != null) {
//                            JRAccount.getInstance().setAccountConfig(mCachedAcc, JRAccountConstants.JRAccountConfigKeyPassword, mCachedPwd);
//                            mCachedPwd = null;
//                        }
                        JRClient.getInstance().login(JRAccount.getInstance().getAccountsList().get(i));
                        mLoginTag = JRAccount.getInstance().getAccountsList().get(i);
                    }

                }
            }
        });
        mUserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (JRClient.getInstance().getState() != JRClientConstants.CLIENT_STATE_LOGINED && i < JRAccount.getInstance().getAccountsList().size()) {
                    AccountActivity.openAccount(AccountListActivity.this, JRAccount.getInstance().getAccountsList().get(i));
                } else if (JRClient.getInstance().getState() == JRClientConstants.CLIENT_STATE_LOGINED && i == 0) {
                    AccountActivity.openAccount(AccountListActivity.this, MtcProf.Mtc_ProfGetCurUser());
                }
                return true;
            }
        });
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onClientInitResult(boolean result, int reason) {

    }

    @Override
    public void onClientLoginResult(boolean result, int reason) {
        mProgress.dismiss();
        if (result) {
            Toast.makeText(this, "已登录", Toast.LENGTH_SHORT).show();
            mCurLoginedUser = mLoginTag;
            mAdapter.notifyDataSetChanged();
        } else {
            mCurLoginedUser = null;
        }
    }

    @Override
    public void onClientLogoutResult(int reason) {
        String errorString = NumberUtils.getStatMsg(reason, this);
        if (!TextUtils.isEmpty(errorString)) {
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
        }
        mProgress.dismiss();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClientStateChange(int state) {
        mProgress.dismiss();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAutoConfigResult(boolean result, int code) {
        mAdapter.notifyDataSetChanged();
        if (result) {
            Toast.makeText(AccountListActivity.this, "自动获取配置成功！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AccountListActivity.this, "自动获取配置失败！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAutoConfigExpire() {
        JRAutoConfig.getInstance().startAutoConfig(mCachedAcc, mCachedPwd, mCachedToken, mIsBusiness);
    }

    @Override
    public void onAutoConfigAuthInd() {

    }

    private static class AccountAdapter extends BaseAdapter {

        private Context mContext;
        AccountAdapter mAccountAdapter;

        public AccountAdapter(Context context) {
            mContext = context;
            mAccountAdapter = this;
        }

        @Override
        public int getCount() {
            //判断登录状态
            if (JRClient.getInstance().getState() == JRClientConstants.CLIENT_STATE_LOGINED) {
                return 2;
            } else {
                return JRAccount.getInstance().getAccountsList().size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_drawer, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.drawer_item_title);
                holder.delete = (Button) convertView.findViewById(R.id.delete);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (JRClient.getInstance().getState() == JRClientConstants.CLIENT_STATE_LOGINED) {
                String curUser = MtcProf.Mtc_ProfGetCurUser();
                holder.delete.setVisibility(View.GONE);
                if (position == 0) {
                    holder.name.setText(curUser);
                } else if (position == 1) {
                    holder.name.setText("注销");
                }
            } else {
                if (position == JRAccount.getInstance().getAccountsList().size()) {
                    holder.name.setText("添加");
                    holder.delete.setVisibility(View.GONE);
                } else {
                    holder.name.setText(JRAccount.getInstance().getAccountsList().get(position));
                    holder.delete.setVisibility(View.VISIBLE);
                }
                holder.delete.setTag(position);
                holder.delete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (int) view.getTag();
                        if (JRAccount.getInstance().deleteAccount(JRAccount.getInstance().getAccountsList().get(pos))) {
                            notifyDataSetChanged();
                        }
                    }
                });
            }

            return convertView;
        }

        class ViewHolder {
            TextView name;
            Button delete;

        }
    }
}
