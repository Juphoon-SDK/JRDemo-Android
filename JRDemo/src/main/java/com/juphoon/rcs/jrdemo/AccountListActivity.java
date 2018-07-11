package com.juphoon.rcs.jrdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmcc.sso.sdk.auth.AuthnConstants;
import com.cmcc.sso.sdk.auth.TokenListener;
import com.cmcc.sso.sdk.util.SsoSdkConstants;
import com.juphoon.cmcc.app.lemon.MtcProf;
import com.juphoon.rcs.JRAccount;
import com.juphoon.rcs.JRAccountConstants;
import com.juphoon.rcs.JRAutoConfig;
import com.juphoon.rcs.JRAutoConfigCallback;
import com.juphoon.rcs.JRAutoConfigConstants;
import com.juphoon.rcs.JRClient;
import com.juphoon.rcs.JRClientCallback;
import com.juphoon.rcs.JRClientConstants;
import com.juphoon.rcs.JRLog;
import com.juphoon.rcs.MtcUtils;

import org.json.JSONObject;

import java.util.HashMap;

import common.utils.CmccTokenManager;
import common.utils.JRNumberUtils;

public class AccountListActivity extends AppCompatActivity implements JRAutoConfigCallback, JRClientCallback, OnClickListener {
    private ListView mUserListView;
    private AccountAdapter mAdapter;
    private ProgressDialog mProgress;
    private RadioGroup mSerType;
    private String mCurLoginedUser;
    private String mLoginTag;
    private HashMap<String, String> mCachedParam;
    private boolean mIsBusiness = true;
//    private String mCachedPwd;
//    private String mCachedAcc;

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
                if (JRClient.getInstance().getState() == JRClientConstants.State.REGED) {
                    if (i == 0) {

                    } else if (i == 1) {
                        mProgress.setMessage("注销中");
                        mProgress.show();
                        JRClient.getInstance().logout();
                    }
                } else {
                    if (i == JRAccount.getInstance().getAccountList().size()) {
                        // CP之前取统一认证相关参数，需要参数为账号，密码，token
                        Toast.makeText(AccountListActivity.this, "统一认证获取", Toast.LENGTH_SHORT).show();
                        CmccTokenManager.getInstance(AccountListActivity.this).getAppPassword(null, new TokenListener() {
                            @Override
                            public void onGetTokenComplete(JSONObject jsonObject) {
                                if (jsonObject == null) {
                                    return;
                                }
                                JRLog.log("token", jsonObject.toString());
                                int resultCode = jsonObject.optInt(SsoSdkConstants.VALUES_KEY_RESULT_CODE, -1);
                                if (resultCode == AuthnConstants.CLIENT_CODE_SUCCESS) {
                                    mCachedParam = new HashMap<>();
                                    mCachedParam.put(JRAutoConfig.KEY_ACCOUNT, jsonObject.optString(SsoSdkConstants.VALUES_KEY_USERNAME));
                                    mCachedParam.put(JRAutoConfig.KEY_PWD, jsonObject.optString(SsoSdkConstants.VALUES_KEY_PASSWORD));
//                                    mCachedPwd = jsonObject.optString(SsoSdkConstants.VALUES_KEY_PASSWORD);
//                                    mCachedAcc = jsonObject.optString(SsoSdkConstants.VALUES_KEY_USERNAME);
                                    mCachedParam.put(JRAutoConfig.KEY_TOKEN, jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN));
                                    mCachedParam.put(JRAutoConfig.KEY_IS_BUS,String.valueOf(mIsBusiness));
                                    JRAutoConfig.getInstance().startAutoConfig(mCachedParam);
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
                        JRClient.getInstance().login(JRAccount.getInstance().getAccountList().get(i));
                        mLoginTag = JRAccount.getInstance().getAccountList().get(i);
                    }

                }
            }
        });
        mUserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (JRClient.getInstance().getState() != JRClientConstants.State.REGED && i < JRAccount.getInstance().getAccountList().size()) {
                    AccountActivity.openAccount(AccountListActivity.this, JRAccount.getInstance().getAccountList().get(i));
                } else if (JRClient.getInstance().getState() == JRClientConstants.State.REGED && i == 0) {
                    AccountActivity.openAccount(AccountListActivity.this, MtcProf.Mtc_ProfGetCurUser());
                }
                return true;
            }
        });
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
    }

    @Override
    public void onClientInitResult(boolean result, JRClientConstants.Reason reason) {

    }

    @Override
    public void onClientLoginResult(boolean result, JRClientConstants.RegErrorCode reason) {
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
    public void onClientLogoutResult(JRClientConstants.RegErrorCode reason) {
        String errorString = JRNumberUtils.getStatMsg(reason, this);
        if (!TextUtils.isEmpty(errorString)) {
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
        }
        mProgress.dismiss();
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onClientStateChange(JRClientConstants.State state) {
        mProgress.dismiss();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAutoConfigResult(boolean result, JRAutoConfigConstants.Error error) {
        mAdapter.notifyDataSetChanged();
        if (result) {
            Toast.makeText(MainApplication.sApp, "自动获取配置成功！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainApplication.sApp, "自动获取配置失败！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAutoConfigReceivedAgreement(String agreement) {

    }

    @Override
    public void onAutoConfigStateChanged(JRAutoConfigConstants.State state, JRAutoConfigConstants.State oldState) {
        if (state == JRAutoConfigConstants.State.EXPIRE) {
            //重新cp
            if (mCachedParam != null) {
                JRAutoConfig.getInstance().startAutoConfig(mCachedParam);
            }
        } else {
            //将统一认证参数清空
            mCachedParam = null;
        }
    }

    @Override
    public void onClick(View view) {

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
            if (JRClient.getInstance().getState() == JRClientConstants.State.REGED) {
                return 2;
            } else {
                return JRAccount.getInstance().getAccountList().size() + 1;
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

            if (JRClient.getInstance().getState() == JRClientConstants.State.REGED) {
                String curUser = MtcProf.Mtc_ProfGetCurUser();
                holder.delete.setVisibility(View.GONE);
                if (position == 0) {
                    holder.name.setText(curUser);
                } else if (position == 1) {
                    holder.name.setText("注销");
                }
            } else {
                if (position == JRAccount.getInstance().getAccountList().size()) {
                    holder.name.setText("添加");
                    holder.delete.setVisibility(View.GONE);
                } else {
                    holder.name.setText(JRAccount.getInstance().getAccountList().get(position));
                    holder.delete.setVisibility(View.VISIBLE);
                }
                holder.delete.setTag(position);
                holder.delete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (int) view.getTag();
                        if (JRAccount.getInstance().deleteAccount(JRAccount.getInstance().getAccountList().get(pos))) {
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
