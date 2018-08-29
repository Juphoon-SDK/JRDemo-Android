package com.juphoon.jrsdk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.juphoon.jrsdk.ui.account.JRAccountPreference;
import com.juphoon.rcs.jrsdk.JRClient;
import com.juphoon.rcs.jrsdk.JRClientCallback;
import com.juphoon.rcs.jrsdk.JRClientConstants;

public class AccountActivity extends AppCompatActivity implements JRClientCallback {

    public final static String ACCOUNT_NAME = "account_name";
    public final static String ACCOUNT_ACTION = "account_action";

    private String mAccount;
    private ProgressDialog mDialog;

    public static void openAccount(Context context, String name) {
        Intent intent = new Intent(context, AccountActivity.class);
        intent.putExtra(ACCOUNT_NAME, name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccount = getIntent().getStringExtra(ACCOUNT_NAME);
//        JRAccount.getInstance().getAccountConfig(mAccount,null);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("账号详情");
        JRAccountPreference accountPreference = JRAccountPreference.newInstance(mAccount);
        getFragmentManager().beginTransaction().replace(android.R.id.content, accountPreference).commit();
        JRClient.getInstance().addCallback(this);
    }


    @Override
    public void onBackPressed() {
//        RcsAccount.getInstance().save();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account, menu);
        menu.findItem(R.id.action_account_logout).setVisible(JRClient.getInstance().getState() == JRClientConstants.CLIENT_STATE_LOGINED);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            ret = true;
        } else if (id == R.id.action_account_logout) {
            showProgressDialog("", getString(R.string.logouting));
            JRClient.getInstance().logout();
            ret = true;
        }
        return ret;
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void showProgressDialog(String titleText, String msgText) {
        dismissProgressDialog();
        mDialog = ProgressDialog.show(this, titleText, msgText);
    }

    private void dismissProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void onClientInitResult(boolean result, int reason) {

    }

    @Override
    public void onClientLoginResult(boolean result, int reason) {

    }

    @Override
    public void onClientLogoutResult(int reason) {
        finish();
    }

    @Override
    public void onClientStateChange(int state) {

    }
}