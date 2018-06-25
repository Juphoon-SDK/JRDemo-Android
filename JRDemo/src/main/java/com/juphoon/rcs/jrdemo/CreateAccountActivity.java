package com.juphoon.rcs.jrdemo;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.juphoon.cmcc.app.lemon.MtcCliDbConstants;
import com.juphoon.rcs.JRAccount;
import com.juphoon.rcs.JRAccountConstants;

import java.util.HashMap;

public class CreateAccountActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    private EditText mEdtAccount;
    private EditText mEdtPassword;
    private EditText mEdtRealm;
    private EditText mEdtPort;
    private EditText mEdtIP;
    private EditText mEdtAuth;
    private Button mSave;
    private RadioGroup mRadioGroupTpt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
        initViews();
    }

    private void initViews() {
        mEdtAccount = (EditText) findViewById(R.id.edt_account);
        mEdtPassword = (EditText) findViewById(R.id.edt_password);
        mEdtRealm = (EditText) findViewById(R.id.edt_realm);
        mEdtPort = (EditText) findViewById(R.id.edt_port);
        mEdtIP = (EditText) findViewById(R.id.edt_ip);
        mEdtAuth = (EditText) findViewById(R.id.edt_auth);
        mSave = (Button) findViewById(R.id.button_save);
        mSave.setOnClickListener(this);
        mRadioGroupTpt = (RadioGroup) findViewById(R.id.radio_group_tpt);
        mRadioGroupTpt.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int tptType = -1;
        if (id == R.id.button_save) {
            if (!TextUtils.isEmpty(mEdtAccount.getText().toString())) {
                if (JRAccount.getInstance().createAccount(mEdtAccount.getText().toString())) {
                    HashMap<JRAccountConstants, String> params = new HashMap<>();
                    params.put(JRAccountConstants.JRAccountConfigKeyAuthName, mEdtAuth.getText().toString());
                    params.put(JRAccountConstants.JRAccountConfigKeyPassword, mEdtPassword.getText().toString());
                    params.put(JRAccountConstants.JRAccountConfigKeyRegIp, mEdtIP.getText().toString());
                    params.put(JRAccountConstants.JRAccountConfigKeyRegRealm, mEdtRealm.getText().toString());
                    params.put(JRAccountConstants.JRAccountConfigKeySipPort, mEdtPort.getText().toString());
                    switch (mRadioGroupTpt.getCheckedRadioButtonId()) {
                        case R.id.radio_tpt_udp:
                            tptType = MtcCliDbConstants.EN_MTC_TPT_UDP;
                            break;
                        case R.id.radio_tpt_tcp:
                            tptType = MtcCliDbConstants.EN_MTC_TPT_TCP;
                            break;
                        case R.id.radio_tpt_tls:
                            tptType = MtcCliDbConstants.EN_MTC_TPT_TLS;
                            break;
                    }
                    params.put(JRAccountConstants.JRAccountConfigKeySipTransportType, String.valueOf(tptType));
                    JRAccount.getInstance().setAccountConfig(mEdtAccount.getText().toString(), params);
                    finish();
                }
            }
        }
    }
}
