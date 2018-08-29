package com.juphoon.jrsdk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.juphoon.rcs.jrsdk.JRCapCallback;
import com.juphoon.rcs.jrsdk.JRCapacity;

/**
 * Created by Upon on 2018/8/1.
 */

public class CapActivity extends AppCompatActivity implements JRCapCallback {
    private EditText mEditText;
    private TextView mResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cap);
        JRCapacity.getInstance().addCallback(this);
        initViews();
    }

    private void initViews() {
        mEditText = findViewById(R.id.edit_input);
        mResult = findViewById(R.id.result);
    }

    public void onCap(View V){
        if(TextUtils.isEmpty(mEditText.getText().toString())){
            Toast.makeText(this, "号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        JRCapacity.getInstance().queryRcsCapacity(mEditText.getText().toString());
    }

    @Override
    public void onQueryRcsCapacityResult(boolean b, boolean b1, boolean b2, String s, int i) {
        mResult.setText(" 查询结果  " + b + "\n 是否是RCS用户  " + b1 + "\n 用户是否在线  " + b2 + "\n 用户号码 " + s);
    }

    @Override
    protected void onDestroy() {
        JRCapacity.getInstance().removeCallback(this);
        super.onDestroy();
    }
}
