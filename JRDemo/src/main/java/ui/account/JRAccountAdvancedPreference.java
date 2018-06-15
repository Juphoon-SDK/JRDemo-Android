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
import android.widget.Toast;

import com.juphoon.rcs.JRAccount;
import com.juphoon.rcs.JRAccountConstants;
import com.juphoon.rcs.jrdemo.CodecActivity;
import com.juphoon.rcs.jrdemo.R;


public class JRAccountAdvancedPreference extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    public static final String ADVANCED_TYPE_KEY = "advanced_type_key";
    public static final String ADVANCED_ACCOUNT_KEY = "advanced_account_key";

    public static final int ADVANCED_TYPE_SIP = 0;
    public static final int ADVANCED_TYPE_AUDIO = 1;
    public static final int ADVANCED_TYPE_VIDEO = 2;
    public static final int ADVANCED_TYPE_TRANSPORT = 3;

    private int mType;
    private String mAccount;

    public static JRAccountAdvancedPreference newInstance(int type, String account) {
        JRAccountAdvancedPreference fragment = new JRAccountAdvancedPreference();
        Bundle args = new Bundle();
        args.putInt(ADVANCED_TYPE_KEY, type);
        args.putString(ADVANCED_ACCOUNT_KEY, account);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(ADVANCED_TYPE_KEY);
            mAccount = getArguments().getString(ADVANCED_ACCOUNT_KEY);
            if (mType == ADVANCED_TYPE_SIP) {
                addPreferencesFromResource(R.xml.account_sip);
            } else if (mType == ADVANCED_TYPE_AUDIO) {
                addPreferencesFromResource(R.xml.account_audio);
            } else if (mType == ADVANCED_TYPE_VIDEO) {
                addPreferencesFromResource(R.xml.account_video);
            } else if (mType == ADVANCED_TYPE_TRANSPORT) {
                addPreferencesFromResource(R.xml.account_transport);
            }
            initPreference();
            loadData();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        boolean ret = false;
        String key = preference.getKey();
        if (key.equals(ADVANCED_AUDIO_CODE)) {
            Intent intent = new Intent(getActivity(), CodecActivity.class);
            intent.putExtra(CodecActivity.CODEC_TYPE, CodecActivity.AUDIO);
            startActivity(intent);
            ret = true;
        } else if (key.equals(ADVANCED_VIDEO_CODE)) {
            Intent intent = new Intent(getActivity(), CodecActivity.class);
            intent.putExtra(CodecActivity.CODEC_TYPE, CodecActivity.VIDEO);
            startActivity(intent);
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean ret = false;
        if (preference instanceof EditTextPreference) {
            ret = checkValue(preference, (String) newValue);
            if (ret) {
                setEditTextValue((EditTextPreference) preference, (String) newValue);
            }
        } else if (preference instanceof ListPreference) {
            setListValue((ListPreference) preference, (String) newValue);
            ret = true;
        } else if (preference instanceof CheckBoxPreference) {
            setSwitchValue((CheckBoxPreference) preference, (Boolean) newValue);
            ret = true;
        }
        return ret;
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
        if (mType == ADVANCED_TYPE_SIP) {
            //注册
            int intTmp;
            ListPreference list;
            setPreBoolean(ACCOUNT_REG_NO_DIGEST, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegNoDigest)));
            setPreString(ADVANCED_REG_EXPIRE_TIME, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegExpireTime));
            setPreBoolean(ADVANCED_OPEN_SUBS, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyOpenSubs)));
            setPreString(ADVANCED_SUBS_EXPIRE_TIME, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySubsExpireTime));
            // Heartneat
            intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyHeartbeat));
            list = (ListPreference) findPreference(ADVANCED_HEARTBEAT);
            list.setValueIndex(intTmp);
            setPreString(ADVANCED_HEARTBEAT, list.getValue());
            setPreString(ADVANCED_PS_HEARTBEAT, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyPsHeartBeat));
            setPreString(ADVANCED_WIFI_HEARTBEAT, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyWifiHeartBeat));
            //高级配置
            setPreBoolean(ADVANCED_USE_TEL_URI, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyUseTelUri)));
            intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySessType));
            list = (ListPreference) findPreference(ADVANCED_SESS_TYPE);
            list.setValueIndex(intTmp);
            setPreString(ADVANCED_SESS_TYPE, list.getValue());
            setPreString(ADVANCED_SESS_LEN, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySessLen));
            setPreString(ADVANCED_SESS_MIN_LEN, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySessMinLen));
        } else if (mType == ADVANCED_TYPE_AUDIO) {
            // 音频编解码
            setPreString(ADVANCED_AUDIO_BITRATE, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAudioBitrate));
            setPreString(ADVANCED_AUDIO_PTIME, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAudioPtime));
            // DTMF
            int intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyDtmfType));
            ListPreference list = (ListPreference) findPreference(ADVANCED_DTMF_TYPE);
            list.setValueIndex(intTmp);
            setPreString(ADVANCED_DTMF_TYPE, list.getValue());
            setPreString(ADVANCED_DTMF_PAYLOAD, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyDtmfPayload));

            //增益控制
            list = (ListPreference) findPreference(ADVANCED_SEND_AGC_MODE);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgcEnable))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgcMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_SEND_AGC_MODE, list.getValue());
            setPreString(ADVANCED_SEND_AGC, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgc));
            setPreString(ADVANCED_RECV_AGC, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgc));
            list = (ListPreference) findPreference(ADVANCED_RECV_AGC_MODE);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgcEnable))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgcMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_RECV_AGC_MODE, list.getValue());

            //噪音消除
            list = (ListPreference) findPreference(ADVANCED_SEND_ANR_MODE);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAnr))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAnrMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_SEND_ANR_MODE, list.getValue());
            list = (ListPreference) findPreference(ADVANCED_RECV_ANR_MODE);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAnr))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAnrMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_RECV_ANR_MODE, list.getValue());
            //抗抖动
            setPreString(ADVANCED_JITTER_BUFFER_MIN_DELAY, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBufferMinDelay));
            setPreString(ADVANCED_JITTER_BUFFER_MAX_PACKEY, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBufferMaxPacket));
            //音频Qos配置
            list = (ListPreference) findPreference(ADVANCED_VAD);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVad))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVadMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_VAD, list.getValue());
            list = (ListPreference) findPreference(ADVANCED_AEC);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAec))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAecMode));
                list.setValueIndex(intTmp);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_AEC, list.getValue());
            setPreBoolean(ADVANCED_FEC, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFec)));
        } else if (mType == ADVANCED_TYPE_VIDEO) {
            int intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyH264PacketMode));
            ListPreference list = (ListPreference) findPreference(ADVANCED_H264_PACKET_MODE);
            list.setValueIndex(intTmp);
            setPreString(ADVANCED_H264_PACKET_MODE, list.getValue());
            setPreString(ADVANCED_H264_PAYLOAD, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyH264Payload));
            setPreString(ADVANCED_VIDEO_BITRATE_VALUE, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBitrateValue));
            setPreBoolean(ADVANCED_VIDEO_BITRATE_CONTROL, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBitrateControl)));
            list = (ListPreference) findPreference(ADVANCED_RESOLUTION_MAX);
            String resolution = account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVideoWAndH);
            int index = list.findIndexOfValue(resolution);
            if (index > -1) {
                list.setValue(resolution);
            } else {
                list.setValue("480X320");
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVideoWAndH, "480X320");
            }
            setPreString(ADVANCED_RESOLUTION_MAX, list.getValue());

            setPreBoolean(ADVANCED_RESOLUTION_CONTROL, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyResolutionControl)));
            setPreString(ADVANCED_FRAMERATE_MAX, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFramerateMax));
            setPreBoolean(ADVANCED_FRAMERATE_CONTROL, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVideoFramerateControl)));
            setPreString(ADVANCED_KEYPEROID, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyKeyPeroid));

            list = (ListPreference) findPreference(ADVANCED_FIRBYINFO);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFir))) {
                if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFirByInfo))) {
                    list.setValue("INFO");
                } else {
                    list.setValue("RTCP");
                }
            } else {
                list.setValue("OFF");
            }
            setPreString(ADVANCED_FIRBYINFO, list.getValue());

            setPreBoolean(ADVANCED_RPSI, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRpsi)));
            setPreBoolean(ADVANCED_VIDEO_FEC, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVideoFec)));
            setPreBoolean(ADVANCED_NACK, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyNack)));
            setPreBoolean(ADVANCED_RTX, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRtx)));
            setPreBoolean(ADVANCED_BEM, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBem)));
        } else if (mType == ADVANCED_TYPE_TRANSPORT) {
            int intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySrtpType));
            ListPreference list = (ListPreference) findPreference(SRTP_TYPE);
            list.setValueIndex(intTmp);
            setPreString(SRTP_TYPE, list.getValue());
            setPreBoolean(ADVANCED_AUDIO_RTCPMUX, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAudioRtcpmux)));
            setPreBoolean(ADVANCED_VIDEO_RTCPMUX, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVideoRtcpmux)));
            intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyNatType));
            list = (ListPreference) findPreference(ADVANCED_NAT_TYPE);
            list.setValueIndex(intTmp);
            setPreString(ADVANCED_NAT_TYPE, list.getValue());
            setPreString(ADVANCED_STUN_SERVER, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyStunServer));
            setPreString(ADVANCED_STUN_SERVER_PORT, account.getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyStunServerPort));
        }
    }

    private void setListenerForPreference(Preference p) {
        String key = p.getKey();
        if (key.equals(ADVANCED_AUDIO_CODE) || key.equals(ADVANCED_VIDEO_CODE)) {
            p.setOnPreferenceClickListener(this);
        } else {
            p.setOnPreferenceChangeListener(this);
        }
    }

    private void setEditTextValue(EditTextPreference p, String value) {
        String key = p.getKey();
        JRAccount account = JRAccount.getInstance();
        p.setText(value);
        p.setSummary(value);
        if (key.equals(ADVANCED_REG_EXPIRE_TIME)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegExpireTime, value);
        } else if (key.equals(ADVANCED_SUBS_EXPIRE_TIME)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySubsExpireTime, value);
        } else if (key.equals(ADVANCED_PS_HEARTBEAT)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyPsHeartBeat, value);
        } else if (key.equals(ADVANCED_WIFI_HEARTBEAT)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyWifiHeartBeat, value);
        } else if (key.equals(ADVANCED_SESS_LEN)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySessLen, value);
        } else if (key.equals(ADVANCED_SESS_MIN_LEN)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySessMinLen, value);
        } else if (key.equals(ADVANCED_AUDIO_BITRATE)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAudioBitrate, value);
        } else if (key.equals(ADVANCED_AUDIO_PTIME)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAudioPtime, value);
        } else if (key.equals(ADVANCED_DTMF_PAYLOAD)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyDtmfPayload, value);
        } else if (key.equals(ADVANCED_SEND_AGC)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgc, value);
        } else if (key.equals(ADVANCED_RECV_AGC)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgc, value);
        } else if (key.equals(ADVANCED_JITTER_BUFFER_MIN_DELAY)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBufferMinDelay, value);
        } else if (key.equals(ADVANCED_JITTER_BUFFER_MAX_PACKEY)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBufferMaxPacket, value);
        } else if (key.equals(ADVANCED_H264_PAYLOAD)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyH264Payload, value);
        } else if (key.equals(ADVANCED_VIDEO_BITRATE_VALUE)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBitrateValue, value);
        } else if (key.equals(ADVANCED_FRAMERATE_MAX)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFramerateMax, value);
        } else if (key.equals(ADVANCED_KEYPEROID)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyKeyPeroid, value);
        } else if (key.equals(ADVANCED_STUN_SERVER)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyStunServer, value);
        } else if (key.equals(ADVANCED_STUN_SERVER_PORT)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyStunServerPort, value);
        }
    }

    private void setListValue(ListPreference p, String value) {
        String key = p.getKey();
        JRAccount account = JRAccount.getInstance();
        p.setValue(value);
        p.setSummary(value);
        if (key.equals(ACCOUNT_REGTYPE)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegType, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_HEARTBEAT)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyHeartbeat, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_SESS_TYPE)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySessType, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_AUDIO_PTIME)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAudioPtime, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_DTMF_TYPE)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyDtmfType, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_SEND_AGC_MODE)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgc, JRAccount.getInstance().getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgc));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgcMode, "-1");
            } else {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgc, JRAccount.getInstance().getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgc));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAgcMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_RECV_AGC_MODE)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgc, JRAccount.getInstance().getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgc));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgcMode, "-1");
            } else {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgc, JRAccount.getInstance().getAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgc));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAgcMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_SEND_ANR_MODE)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAnr, String.valueOf(false));
            } else {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAnr, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySendAnrMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_RECV_ANR_MODE)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAnr, String.valueOf(false));
            } else {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAnr, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRecvAnrMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_VAD)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVad, String.valueOf(false));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVadMode, "-1");
            } else {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVad, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVadMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_AEC)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAec, String.valueOf(false));
            } else {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAec, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAecMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_H264_PACKET_MODE)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyH264PacketMode, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_RESOLUTION_MAX)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVideoWAndH, value);
        } else if (key.equals(ADVANCED_FIRBYINFO)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFir, String.valueOf(false));
            } else if (p.findIndexOfValue(value) == 1) {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFir, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFirByInfo, String.valueOf(false));
            } else if (p.findIndexOfValue(value) == 2) {
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFir, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFirByInfo, String.valueOf(true));
            }
        } else if (key.equals(SRTP_TYPE)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeySrtpType, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_NAT_TYPE)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyNatType, String.valueOf(p.findIndexOfValue(value)));
        }
    }

    private void setSwitchValue(CheckBoxPreference p, boolean value) {
        String key = p.getKey();
        JRAccount account = JRAccount.getInstance();
        p.setChecked(value);
        if (key.equals(ACCOUNT_REG_NO_DIGEST)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRegNoDigest, String.valueOf(value));
        } else if (key.equals(ADVANCED_OPEN_SUBS)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyOpenSubs, String.valueOf(value));
        } else if (key.equals(ADVANCED_USE_TEL_URI)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyUseTelUri, String.valueOf(value));
        } else if (key.equals(ADVANCED_FEC)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyFec, String.valueOf(value));
        } else if (key.equals(ADVANCED_VIDEO_BITRATE_CONTROL)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBitrateControl, String.valueOf(value));
        } else if (key.equals(ADVANCED_RESOLUTION_CONTROL)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyResolutionControl, String.valueOf(value));
        } else if (key.equals(ADVANCED_FRAMERATE_CONTROL)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVideoFramerateControl, String.valueOf(value));
        } else if (key.equals(ADVANCED_RPSI)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRpsi, String.valueOf(value));
        } else if (key.equals(ADVANCED_VIDEO_FEC)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVideoFec, String.valueOf(value));
        } else if (key.equals(ADVANCED_NACK)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyNack, String.valueOf(value));
        } else if (key.equals(ADVANCED_RTX)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyRtx, String.valueOf(value));
        } else if (key.equals(ADVANCED_BEM)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyBem, String.valueOf(value));
        } else if (key.equals(ADVANCED_AUDIO_RTCPMUX)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyAudioRtcpmux, String.valueOf(value));
        } else if (key.equals(ADVANCED_VIDEO_RTCPMUX)) {
            account.setAccountConfig(mAccount, JRAccountConstants.JRAccountConfigKeyVideoRtcpmux, String.valueOf(value));
        }
    }

    private final void setPreString(String key, String s) {
        Preference p = findPreference(key);
        if (p == null) {
            return;
        }
        if (p instanceof EditTextPreference) {
            ((EditTextPreference) p).setText(s);
            p.setSummary(s);
        } else if (p instanceof ListPreference) {
            ((ListPreference) p).setValue(s);
            p.setSummary(s);
        } else if (p instanceof Preference) {
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

    private boolean checkValue(Preference preference, String value) {
        String key = preference.getKey();
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        if (key.equals(ADVANCED_SEND_AGC) || key.equals(ADVANCED_RECV_AGC)) {
            int intValue = Integer.valueOf(value);
            return checkRange(intValue, 0, 30);
        } else if (key.equals(ADVANCED_KEYPEROID)) {
            int intValue = Integer.valueOf(value);
            return checkRange(intValue, 0, 600);
        } else if (key.equals(ADVANCED_DTMF_PAYLOAD)) {
            int intValue = Integer.valueOf(value);
            return checkRange(intValue, 96, 127);
        } else if (key.equals(ADVANCED_FRAMERATE_MAX)) {
            int intValue = Integer.valueOf(value);
            return checkRange(intValue, 1, 30);
        } else if (key.equals(ADVANCED_H264_PAYLOAD)) {
            int intValue = Integer.valueOf(value);
            return checkRange(intValue, 118, 127);
        }
        return true;
    }

    private boolean checkRange(int value, int min, int max) {
        if (value < min || value > max) {
            String message = String.format(getResources().getString(R.string.advanced_popup_message), min, max);
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkValueMin(int value, int min) {
        if (value < min) {
            String message = String.format(getResources().getString(R.string.advanced_popup_message_min), min);
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * sip
     **/
    //注册
    private final static String ACCOUNT_REGTYPE = "account_regtype";//注册类型
    private final static String ACCOUNT_REG_NO_DIGEST = "account_reg_no_digest";//注册无鉴权头
    private final static String ADVANCED_REG_EXPIRE_TIME = "advanced_reg_expire_time";//注册刷新时间
    private final static String ADVANCED_OPEN_SUBS = "advanced_open_subs";//开启注册订阅
    private final static String ADVANCED_SUBS_EXPIRE_TIME = "advanced_subs_expire_time";//订阅刷新时间
    //heartbeat
    public final static String ADVANCED_PS_HEARTBEAT = "advanced_ps_heartbeat";//心跳类型
    public final static String ADVANCED_WIFI_HEARTBEAT = "advanced_wifi_heartbeat";//数据网络心跳间隔时长
    public final static String ADVANCED_HEARTBEAT = "advanced_heartbeat";//WiFi心跳间隔时长
    //高级配置
    public final static String ADVANCED_SESS_TYPE = "advanced_sess_type";//定时器类型
    public final static String ADVANCED_SESS_LEN = "advanced_sess_len";//定时器时长
    public final static String ADVANCED_SESS_MIN_LEN = "advanced_sess_min_len";//最小定时器时长(s)
    private final static String ADVANCED_USE_TEL_URI = "advanced_use_tel_uri";//使用 Tel URI
    /**
     * audio
     **/
    //音频编解码
    public final static String ADVANCED_AUDIO_CODE = "advanced_audio_code";
    public final static String ADVANCED_AUDIO_BITRATE = "advanced_audio_bitrate";
    public final static String ADVANCED_AUDIO_PTIME = "advanced_ptime";
    //DTMF
    public final static String ADVANCED_DTMF_TYPE = "advanced_dtmf_type";
    public final static String ADVANCED_DTMF_PAYLOAD = "advanced_dtmf_payload";
    //增益控制
    public final static String ADVANCED_SEND_AGC_MODE = "advanced_send_agc_mode";
    public final static String ADVANCED_SEND_AGC = "advanced_send_agc";
    public final static String ADVANCED_RECV_AGC = "advanced_recv_agc";
    public final static String ADVANCED_RECV_AGC_MODE = "advanced_recv_agc_mode";
    //噪音消除
    public final static String ADVANCED_SEND_ANR_MODE = "advanced_send_anr_mode";
    public final static String ADVANCED_RECV_ANR_MODE = "advanced_recv_anr_mode";
    //抗抖动
    public final static String ADVANCED_JITTER_BUFFER_MIN_DELAY = "advanced_jitter_buffer_min_delay";
    public final static String ADVANCED_JITTER_BUFFER_MAX_PACKEY = "advanced_jitter_buffer_max_packet";
    //音频Qos配置
    public final static String ADVANCED_VAD = "advanced_vad";
    public final static String ADVANCED_AEC = "advanced_aec";
    public final static String ADVANCED_FEC = "advanced_fec";

    /**
     * video
     */
    //视频编解码
    public final static String ADVANCED_VIDEO_CODE = "advanced_video_code";
    public final static String ADVANCED_H264_PACKET_MODE = "advanced_h264_packet_mode";
    public final static String ADVANCED_H264_PAYLOAD = "advanced_h264_payload";
    //码率配置
    public final static String ADVANCED_VIDEO_BITRATE_VALUE = "advanced_video_bitrate_value";
    public final static String ADVANCED_VIDEO_BITRATE_CONTROL = "advanced_video_bitrate_control";
    //分辨率配置
    public final static String ADVANCED_RESOLUTION_MAX = "advanced_resolution_max";
    public final static String ADVANCED_RESOLUTION_CONTROL = "advanced_resolution_control";
    //帧率配置
    public final static String ADVANCED_FRAMERATE_MAX = "advanced_framerate_max";
    public final static String ADVANCED_FRAMERATE_CONTROL = "advanced_framerate_control";
    //视频QoS配置
    public final static String ADVANCED_KEYPEROID = "advanced_keyperoid";
    public final static String ADVANCED_FIRBYINFO = "advanced_firbyinfo";
    public final static String ADVANCED_RPSI = "advanced_rpsi";
    public final static String ADVANCED_VIDEO_FEC = "advanced_video_fec";
    public final static String ADVANCED_NACK = "advanced_nack";
    public final static String ADVANCED_RTX = "advanced_rtx";
    public final static String ADVANCED_BEM = "advanced_bem";

    /**
     * tranport
     */
    //媒体传输配置
    public final static String SRTP_TYPE = "srtp_type";
    public final static String ADVANCED_AUDIO_RTCPMUX = "advanced_audio_rtcpmux";
    public final static String ADVANCED_VIDEO_RTCPMUX = "advanced_video_rtcpmux";
    //NAT穿越配置
    public final static String ADVANCED_NAT_TYPE = "advanced_nat_type";
    public final static String ADVANCED_STUN_SERVER = "advanced_stun_server";
    public final static String ADVANCED_STUN_SERVER_PORT = "advanced_stun_server_port";
//    public final static String ADVANCED_MDM_VERSION = "advanced_mdm_version";
//    public final static String ADVANCED_MDM_SWITCH = "advanced_use_mdm";
//    public final static String ADVANCED_AUDIO = "advanced_audio";
//
//    public final static String ADVANCED_AUDIO_SEND_AGC = "advanced_audio_send_agc";
//    public final static String ADVANCED_AUDIO_RECV_AGC = "advanced_audio_recv_agc";
//    public final static String ADVANCED_AUDIO_SEND_ANR = "advanced_audio_send_anr";
//    public final static String ADVANCED_AUDIO_SEND_ANR_MODE = "advanced_audio_send_anr_mode";
//    public final static String ADVANCED_AUDIO_RECV_ANR = "advanced_audio_recv_anr";
//    public final static String ADVANCED_AUDIO_RECV_ANR_MODE = "advanced_audio_recv_anr_mode";
//    public final static String ADVANCED_AUDIO_VAD = "advanced_audio_vad";
//    public final static String ADVANCED_AUDIO_AEC = "advanced_audio_aec";
//
//    public final static String ADVANCED_AUDIO_AMRWB_BITRATE = "advanced_audio_amrwb_bitrate";
//    public final static String ADVANCED_VIDEO = "advanced_video";
//    public final static String ADVANCED_VIDEO_CODEC = "advanced_video_codec";
//    public final static String ADVANCED_VIDEO_BITRATE = "advanced_video_bitrate";
//    public final static String ADVANCED_VIDEO_RESOLUTION = "advanced_video_resolution";
//    public final static String ADVANCED_VIDEO_RESOLUTION_CONTROL = "advanced_video_resolution_control";
//    public final static String ADVANCED_CODEC_AUTO_PRIORITY = "advanced_codec_auto_priority";
//    public final static String ADVANCED_VIDEO_FRAMERATE = "advanced_video_framerate";
//    public final static String ADVANCED_VIDEO_FRAMERATE_CONTROL = "advanced_video_framerate_control";
//    public final static String ADVANCED_VIDEO_ARS_MODE = "advanced_video_ars_mode";
//    public final static String ADVANCED_VIDEO_KEYPERIOD = "advanced_video_keyperiod";
//    public final static String ADVANCED_VIDEO_FIRBYINFO = "advanced_video_firbyinfo";
//    public final static String ADVANCED_VIDEO_BEM = "advanced_video_bem";
//    public final static String ADVANCED_H264_PACKET_MODE = "advanced_h264_packet_mode";
//    public final static String ADVANCED_H264_PAYLOAD = "advanced_h264_payload";
//    public final static String ADVANCED_VIDEO_CAPTURE = "advanced_video_capture";
//    public final static String ADVANCED_JUSQOS = "advanced_jusqos";
//    public final static String ADVANCED_JUSQOS_RPSI = "advanced_jusqos_rpsi";
//    public final static String ADVANCED_JUSQOS_NACK = "advanced_jusqos_nack";
//    public final static String ADVANCED_JUSQOS_TMMBR = "advanced_jusqos_tmmbr";
//    public final static String ADVANCED_JUSQOS_ASYMNEGO = "advanced_jusqos_asymnego";
//    public final static String ADVANCED_JUSQOS_VIDEO_ARS = "advanced_jusqos_video_ars";
//    public final static String ADVANCED_JUSQOS_VIDEO_FEC = "advanced_jusqos_video_fec";
//    public final static String ADVANCED_JUSQOS_AUDIO_ARS = "advanced_jusqos_audio_ars";
//    public final static String ADVANCED_JUSQOS_AUDIO_FEC = "advanced_jusqos_audio_fec";
//    public final static String ADVANCED_MEDIA = "advanced_media";
//    public final static String ADVANCED_MEDIA_DTMF = "advanced_media_dtmf";
//    public final static String ADVANCED_MEDIA_DTMF_PAYLOAD = "advanced_media_dtmf_payload";
//    public final static String ADVANCED_MEDIA_RTCPMUX = "advanced_media_rtcpmux";
//    public final static String ADVANCED_NAT_TRAVERSAL = "advanced_nat_traversal";
//    //    public final static String ADVANCED_NAT_TRAVERSAL_TYPE = "advanced_nat_traversal_type";
//    public final static String ADVANCED_PROTOCOL = "advanced_protocol";
//
//
//    public final static String ADVANCED_STUN = "advanced_stun";
//    public final static String ADVANCED_STUN_OPEN = "advanced_stun_open";
//    public final static String ADVANCED_STUN_SERVER = "advanced_stun_server";
//    public final static String ADVANCED_STUN_SERVER_PORT = "advanced_stun_server_port";
}