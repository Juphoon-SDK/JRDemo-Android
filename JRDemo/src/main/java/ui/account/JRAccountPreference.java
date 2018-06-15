package ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import com.juphoon.rcs.JRAccount;
import com.juphoon.rcs.JRAccountConstants;
import com.juphoon.rcs.jrdemo.AccountAdvancedActivity;
import com.juphoon.rcs.jrdemo.R;

public class JRAccountPreference extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String ACCOUNT_USERNAME = "account_username";
    private static final String ACCOUNT_PASSWORD = "account_password";
    private static final String ACCOUNT_AUTHNAME = "account_authname";
    private static final String ACCOUNT_REGIP = "account_regip";
    private static final String ACCOUNT_REGREALM = "account_regrealm";
    private static final String ACCOUNT_SIP_TRANSPORT_TYPE = "advanced_sip_transport_type";
    private static final String ACCOUNT_SIP_PORT = "advanced_sip_port";
    private static final String ACCOUNT_SIP_INSTANCE_ENABLE = "account_sip_instance_enable";

    private static final String ACCOUNT_ADVANCED_SIP = "account_advanced_sip";
    private static final String ACCOUNT_ADVANCED_AUDIO = "account_advanced_audio";
    private static final String ACCOUNT_ADVANCED_VIDEO = "account_advanced_video";
    private static final String ACCOUNT_ADVANCED_TRANSPORT = "account_advanced_transport";

    public static final String ADVANCED_ACCOUNT = "advanced_account";

    public static JRAccountPreference newInstance(String account) {
        JRAccountPreference fragment = new JRAccountPreference();
        Bundle args = new Bundle();
        args.putString(ADVANCED_ACCOUNT, account);
        fragment.setArguments(args);
        return fragment;
    }

    private String mAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccount = getArguments().getString(ADVANCED_ACCOUNT);
            addPreferencesFromResource(R.xml.jraccount);
            initPreference();
            loadData();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(getActivity(), AccountAdvancedActivity.class);
        if (preference.getKey().equals(ACCOUNT_ADVANCED_SIP)) {
            intent.putExtra(AccountAdvancedActivity.ACCOUNT_ADVANCED_KEY, JRAccountAdvancedPreference.ADVANCED_TYPE_SIP);
            intent.putExtra(ADVANCED_ACCOUNT, mAccount);
            startActivity(intent);
            return true;
        } else if (preference.getKey().equals(ACCOUNT_ADVANCED_AUDIO)) {
            intent.putExtra(AccountAdvancedActivity.ACCOUNT_ADVANCED_KEY, JRAccountAdvancedPreference.ADVANCED_TYPE_AUDIO);
            intent.putExtra(ADVANCED_ACCOUNT, mAccount);
            startActivity(intent);
            return true;
        } else if (preference.getKey().equals(ACCOUNT_ADVANCED_VIDEO)) {
            intent.putExtra(AccountAdvancedActivity.ACCOUNT_ADVANCED_KEY, JRAccountAdvancedPreference.ADVANCED_TYPE_VIDEO);
            intent.putExtra(ADVANCED_ACCOUNT, mAccount);
            startActivity(intent);
            return true;
        } else if (preference.getKey().equals(ACCOUNT_ADVANCED_TRANSPORT)) {
            intent.putExtra(AccountAdvancedActivity.ACCOUNT_ADVANCED_KEY, JRAccountAdvancedPreference.ADVANCED_TYPE_TRANSPORT);
            intent.putExtra(ADVANCED_ACCOUNT, mAccount);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof EditTextPreference || preference instanceof ListPreference) {
            setStringValue(preference, (String) newValue);
        } else if (preference instanceof CheckBoxPreference) {
            setBooleanValue(preference, (boolean) newValue);
        }
        return false;
    }

    private void initPreference() {
        PreferenceScreen ps = getPreferenceScreen();
        final int screenCount = ps.getPreferenceCount();
        for (int i = 0; i < screenCount; ++i) {
            PreferenceCategory pc = (PreferenceCategory) ps.getPreference(i);
            final int categoryCount = pc.getPreferenceCount();
            for (int j = 0; j < categoryCount; ++j) {
                final Preference p = pc.getPreference(j);
                setListenerForPreference(p);
            }
        }
    }

    private void loadData() {
        JRAccount account = JRAccount.getInstance();
        String name = account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyUserName);
        setPreString(ACCOUNT_USERNAME, name);
        setPreString(ACCOUNT_PASSWORD, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyPassword));
        setPreString(ACCOUNT_AUTHNAME, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAuthName));
        setPreString(ACCOUNT_REGIP, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegIp));
        setPreString(ACCOUNT_REGREALM, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegRealm));
        int intTmp = Integer.parseInt(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySipTransportType));
        ListPreference list = (ListPreference) findPreference(ACCOUNT_SIP_TRANSPORT_TYPE);
        list.setValueIndex(intTmp);
        setPreString(ACCOUNT_SIP_TRANSPORT_TYPE, list.getValue());
        setPreString(ACCOUNT_SIP_PORT, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySipPort));
        setPreBoolean(ACCOUNT_SIP_INSTANCE_ENABLE, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySipInstanceEnable)));
    }

    private final void setPreString(String key, String s) {
        Preference p = findPreference(key);
        if (p == null) {
            return;
        }
        if (p instanceof EditTextPreference) {
            ((EditTextPreference) p).setText(s);
            if (key.equals(ACCOUNT_PASSWORD)) {
                String summary = "";
                for (int i = 0; i < s.length(); i++) {
                    summary += "•";
                }
                p.setSummary(summary);
                return;
            }
            p.setSummary(s);
        } else if (p instanceof ListPreference) {
            ((ListPreference) p).setValue(s);
            p.setSummary(s);
        }
    }

    private final void setPreBoolean(String key, boolean value) {
        Preference p = findPreference(key);
        if (p == null) {
            return;
        }

        if (p instanceof CheckBoxPreference) {
            ((CheckBoxPreference) p).setChecked(value);
        }
    }

    private void setListenerForPreference(Preference p) {
        if (p instanceof EditTextPreference || p instanceof ListPreference || p instanceof CheckBoxPreference) {
            p.setOnPreferenceChangeListener(this);
        } else if (p instanceof Preference) {
            p.setOnPreferenceClickListener(this);
        }
    }

    private void setStringValue(Preference preference, String value) {
        if (TextUtils.isEmpty(value))
            return;
        String key = preference.getKey();
        JRAccount account = JRAccount.getInstance();
        setPreString(key, value);
        if (key.equals(ACCOUNT_USERNAME)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyUserName, value);
        } else if (key.equals(ACCOUNT_PASSWORD)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyPassword, value);
        } else if (key.equals(ACCOUNT_AUTHNAME)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAuthName, value);
        } else if (key.equals(ACCOUNT_REGIP)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegIp, value);
        } else if (key.equals(ACCOUNT_REGREALM)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegRealm, value);
        } else if (key.equals(ACCOUNT_SIP_PORT)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySipPort, value);
        } else if (key.equals(ACCOUNT_SIP_TRANSPORT_TYPE)) {
            ListPreference p = (ListPreference) findPreference(key);
            p.setValue(value);
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegType, String.valueOf(p.findIndexOfValue((String) value)));
        }
    }

    private void setBooleanValue(Preference preference, boolean value) {
        String key = preference.getKey();
        JRAccount account = JRAccount.getInstance();
        setPreBoolean(key, value);
        if (key.equals(ACCOUNT_SIP_INSTANCE_ENABLE)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySipInstanceEnable, String.valueOf(value));
        }
    }

}