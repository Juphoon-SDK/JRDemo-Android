package com.juphoon.jrsdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.juphoon.jrsdk.ui.account.JRAccountAdvancedPreference;
import com.juphoon.jrsdk.ui.account.JRAccountPreference;

public class AccountAdvancedActivity extends Activity {
    public static final String ACCOUNT_ADVANCED_KEY = "account_advanced_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra(ACCOUNT_ADVANCED_KEY, JRAccountAdvancedPreference.ADVANCED_TYPE_SIP);
        String account = getIntent().getStringExtra(JRAccountPreference.ADVANCED_ACCOUNT);
        JRAccountAdvancedPreference advancedPreference = JRAccountAdvancedPreference.newInstance(type, account);
        getFragmentManager().beginTransaction().replace(android.R.id.content, advancedPreference).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
