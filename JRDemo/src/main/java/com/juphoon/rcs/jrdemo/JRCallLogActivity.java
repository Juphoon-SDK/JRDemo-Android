package com.juphoon.rcs.jrdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cmcc.sso.sdk.auth.AuthnConstants;
import com.cmcc.sso.sdk.auth.TokenListener;
import com.cmcc.sso.sdk.util.SsoSdkConstants;
import com.juphoon.rcs.JRLog;

import org.json.JSONObject;

import java.util.HashMap;

import common.CommonValue;
import common.utils.CmccTokenManager;

/**
 * Created by Upon on 2018/4/23.
 */

public class JRCallLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replace);
        JRCallLogFragment fragment = new JRCallLogFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.replace, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "新建通话");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 1) {
            showCallDialog();
        }
        return true;
    }

    private void showCallDialog() {
        View parentView = LayoutInflater.from(this).inflate(R.layout.layout_choice, null, true);
        final TextView tv = parentView.findViewById(R.id.number);
        final CheckBox isVideo = parentView.findViewById(R.id.is_video);
        final CheckBox isMulti = parentView.findViewById(R.id.is_multi);
        new AlertDialog.Builder(this)
                .setTitle("请输入")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(parentView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!TextUtils.isEmpty(tv.getText())) {
                            String peernumber = tv.getText().toString();
                            final Intent intent = new Intent(JRCallLogActivity.this, JRCallActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra(CommonValue.JRCALL_EXTRA_PHONE_NUMBER, peernumber);
                            intent.putExtra(CommonValue.JRCALL_EXTRA_IS_MULTI, isMulti.isChecked());
                            intent.putExtra(CommonValue.JRCALL_EXTRA_IS_VIDEO, isVideo.isChecked());
                            if(isMulti.isChecked() && isVideo.isChecked()){
                                CmccTokenManager.getInstance(JRCallLogActivity.this).getAppPassword(null, new TokenListener() {
                                    @Override
                                    public void onGetTokenComplete(JSONObject jsonObject) {
                                        if (jsonObject == null) {
                                            return;
                                        }
                                        JRLog.log("token", jsonObject.toString());
                                        int resultCode = jsonObject.optInt(SsoSdkConstants.VALUES_KEY_RESULT_CODE, -1);
                                        if (resultCode == AuthnConstants.CLIENT_CODE_SUCCESS) {
                                            HashMap<String, String> param = new HashMap<>();
                                            String token = jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN);
                                            intent.putExtra(CommonValue.JRCALL_EXTRA_TOKEN,token);
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
