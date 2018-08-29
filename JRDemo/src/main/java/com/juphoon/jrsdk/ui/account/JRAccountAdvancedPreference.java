package com.juphoon.jrsdk.ui.account;

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

import com.juphoon.jrsdk.CodecActivity;
import com.juphoon.jrsdk.R;
import com.juphoon.rcs.jrsdk.JRAccount;
import com.juphoon.rcs.jrsdk.JRAccountDefine;

public class JRAccountAdvancedPreference extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    public static final String ADVANCED_TYPE_KEY = "advanced_type_key";
    public static final String ADVANCED_ACCOUNT_KEY = "advanced_account_key";

    public static final int ADVANCED_TYPE_SIP = 0;
    public static final int ADVANCED_TYPE_AUDIO = 1;
    public static final int ADVANCED_TYPE_VIDEO = 2;
    public static final int ADVANCED_TYPE_TRANSPORT = 3;
    public static final int ADVANCED_TYPE_MESSAGE = 4;

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
            } else if (mType == ADVANCED_TYPE_MESSAGE) {
                addPreferencesFromResource(R.xml.account_message);
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
            setPreBoolean(ACCOUNT_REG_NO_DIGEST, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.RegNoDigest)));
            setPreString(ADVANCED_REG_EXPIRE_TIME, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.RegExpireTime));
            setPreBoolean(ADVANCED_OPEN_SUBS, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.OpenSubs)));
            setPreString(ADVANCED_SUBS_EXPIRE_TIME, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SubsExpireTime));
            // Heartneat
            intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.Heartbeat));
            list = (ListPreference) findPreference(ADVANCED_HEARTBEAT);
            list.setValueIndex(intTmp);
            setPreString(ADVANCED_HEARTBEAT, list.getValue());
            setPreString(ADVANCED_PS_HEARTBEAT, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.PsHeartBeat));
            setPreString(ADVANCED_WIFI_HEARTBEAT, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.WifiHeartBeat));
            //高级配置
            setPreBoolean(ADVANCED_USE_TEL_URI, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.UseTelUri)));
            intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SessType));
            list = (ListPreference) findPreference(ADVANCED_SESS_TYPE);
            list.setValueIndex(intTmp);
            setPreString(ADVANCED_SESS_TYPE, list.getValue());
            setPreString(ADVANCED_SESS_LEN, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SessLen));
            setPreString(ADVANCED_CONF_ID, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.HttpConfId));
            setPreString(ADVANCED_SESS_MIN_LEN, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SessMinLen));
        } else if (mType == ADVANCED_TYPE_AUDIO) {
            // 音频编解码
            setPreString(ADVANCED_AUDIO_BITRATE, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.AudioBitrate));
            setPreString(ADVANCED_AUDIO_PTIME, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.AudioPtime));
            // DTMF
            int intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.DtmfType));
            ListPreference list = (ListPreference) findPreference(ADVANCED_DTMF_TYPE);
            list.setValueIndex(intTmp);
            setPreString(ADVANCED_DTMF_TYPE, list.getValue());
            setPreString(ADVANCED_DTMF_PAYLOAD, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.DtmfPayload));

            //增益控制
            list = (ListPreference) findPreference(ADVANCED_SEND_AGC_MODE);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgcEnable))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgcMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_SEND_AGC_MODE, list.getValue());
            setPreString(ADVANCED_SEND_AGC, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgc));
            setPreString(ADVANCED_RECV_AGC, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgc));
            list = (ListPreference) findPreference(ADVANCED_RECV_AGC_MODE);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgcEnable))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgcMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_RECV_AGC_MODE, list.getValue());

            //噪音消除
            list = (ListPreference) findPreference(ADVANCED_SEND_ANR_MODE);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAnr))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAnrMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_SEND_ANR_MODE, list.getValue());
            list = (ListPreference) findPreference(ADVANCED_RECV_ANR_MODE);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAnr))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAnrMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_RECV_ANR_MODE, list.getValue());
            //抗抖动
            setPreString(ADVANCED_JITTER_BUFFER_MIN_DELAY, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.BufferMinDelay));
            setPreString(ADVANCED_JITTER_BUFFER_MAX_PACKEY, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.BufferMaxPacket));
            //音频Qos配置
            list = (ListPreference) findPreference(ADVANCED_VAD);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.Vad))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.VadMode));
                list.setValueIndex(intTmp + 1);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_VAD, list.getValue());
            list = (ListPreference) findPreference(ADVANCED_AEC);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.Aec))) {
                intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.AecMode));
                list.setValueIndex(intTmp);
            } else {
                list.setValueIndex(0);
            }
            setPreString(ADVANCED_AEC, list.getValue());
            setPreBoolean(ADVANCED_FEC, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.Fec)));
        } else if (mType == ADVANCED_TYPE_VIDEO) {
            int intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.H264PacketMode));
            ListPreference list = (ListPreference) findPreference(ADVANCED_H264_PACKET_MODE);
            list.setValueIndex(intTmp);
            int fecType = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoFecType));
            ListPreference fecList = (ListPreference) findPreference(ADVANCED_VIDEO_FEC_TYPE);
            fecList.setValueIndex(fecType);
            setPreString(ADVANCED_H264_PACKET_MODE, list.getValue());
            setPreString(ADVANCED_H264_PAYLOAD, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.H264Payload));
            setPreString(ADVANCED_VIDEO_BITRATE_VALUE, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.BitrateValue));
            setPreBoolean(ADVANCED_VIDEO_BITRATE_CONTROL, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.BitrateControl)));
            list = (ListPreference) findPreference(ADVANCED_RESOLUTION_MAX);
            String resolution = account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoWAndH);
            int index = list.findIndexOfValue(resolution);
            if (index > -1) {
                list.setValue(resolution);
            } else {
                list.setValue("480X320");
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoWAndH, "480X320");
            }
            setPreString(ADVANCED_RESOLUTION_MAX, list.getValue());

            setPreBoolean(ADVANCED_RESOLUTION_CONTROL, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.ResolutionControl)));
            setPreString(ADVANCED_FRAMERATE_MAX, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.FramerateMax));
            setPreBoolean(ADVANCED_FRAMERATE_CONTROL, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoFramerateControl)));
            setPreString(ADVANCED_KEYPEROID, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.KeyPeroid));

            list = (ListPreference) findPreference(ADVANCED_FIRBYINFO);
            if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.Fir))) {
                if (Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.FirByInfo))) {
                    list.setValue("INFO");
                } else {
                    list.setValue("RTCP");
                }
            } else {
                list.setValue("OFF");
            }
            setPreString(ADVANCED_FIRBYINFO, list.getValue());

            setPreBoolean(ADVANCED_RPSI, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.Rpsi)));
            setPreBoolean(ADVANCED_VIDEO_FEC, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoFec)));
            setPreBoolean(ADVANCED_NACK, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.Nack)));
            setPreBoolean(ADVANCED_RTX, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.Rtx)));
            setPreBoolean(ADVANCED_BEM, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.Bem)));
        } else if (mType == ADVANCED_TYPE_TRANSPORT) {
            int intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SrtpType));
            ListPreference list = (ListPreference) findPreference(SRTP_TYPE);
            list.setValueIndex(intTmp);
            setPreString(SRTP_TYPE, list.getValue());
            setPreBoolean(ADVANCED_AUDIO_RTCPMUX, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.AudioRtcpmux)));
            setPreBoolean(ADVANCED_VIDEO_RTCPMUX, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoRtcpmux)));
            intTmp = Integer.valueOf(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.NatType));
            list = (ListPreference) findPreference(ADVANCED_NAT_TYPE);
            list.setValueIndex(intTmp);
            setPreString(ADVANCED_NAT_TYPE, list.getValue());
            setPreString(ADVANCED_STUN_SERVER, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.StunServer));
            setPreString(ADVANCED_STUN_SERVER_PORT, account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.StunServerPort));
        } else if (mType == ADVANCED_TYPE_MESSAGE) {
            setPreBoolean(ADVANCED_MESSAGE_DELI_FAIL, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendDeliFail)));
            setPreBoolean(ADVANCED_MESSAGE_DELI_SUCC, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendDeliSucc)));
            setPreBoolean(ADVANCED_MESSAGE_DELI_FORWARD, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendDeliForward)));
            setPreBoolean(ADVANCED_MESSAGE_SEND_DISP_REQ, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendDispReq)));
            setPreBoolean(ADVANCED_GROUP_MESSAGE_DELI_FAIL, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.GroupSendDeliFail)));
            setPreBoolean(ADVANCED_GROUP_MESSAGE_DELI_SUCC, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.GroupSendDeliSucc)));
            setPreBoolean(ADVANCED_GROUP_MESSAGE_SEND_DISP_REQ, Boolean.parseBoolean(account.getAccountConfig(mAccount, JRAccountDefine.ConfigKey.GroupSendDispReq)));
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
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RegExpireTime, value);
        } else if (key.equals(ADVANCED_SUBS_EXPIRE_TIME)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SubsExpireTime, value);
        } else if (key.equals(ADVANCED_PS_HEARTBEAT)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.PsHeartBeat, value);
        } else if (key.equals(ADVANCED_WIFI_HEARTBEAT)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.WifiHeartBeat, value);
        } else if (key.equals(ADVANCED_SESS_LEN)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SessLen, value);
        } else if (key.equals(ADVANCED_CONF_ID)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.HttpConfId, value);
        } else if (key.equals(ADVANCED_SESS_MIN_LEN)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SessMinLen, value);
        } else if (key.equals(ADVANCED_AUDIO_BITRATE)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.AudioBitrate, value);
        } else if (key.equals(ADVANCED_AUDIO_PTIME)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.AudioPtime, value);
        } else if (key.equals(ADVANCED_DTMF_PAYLOAD)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.DtmfPayload, value);
        } else if (key.equals(ADVANCED_SEND_AGC)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgc, value);
        } else if (key.equals(ADVANCED_RECV_AGC)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgc, value);
        } else if (key.equals(ADVANCED_JITTER_BUFFER_MIN_DELAY)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.BufferMinDelay, value);
        } else if (key.equals(ADVANCED_JITTER_BUFFER_MAX_PACKEY)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.BufferMaxPacket, value);
        } else if (key.equals(ADVANCED_H264_PAYLOAD)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.H264Payload, value);
        } else if (key.equals(ADVANCED_VIDEO_BITRATE_VALUE)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.BitrateValue, value);
        } else if (key.equals(ADVANCED_FRAMERATE_MAX)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.FramerateMax, value);
        } else if (key.equals(ADVANCED_KEYPEROID)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.KeyPeroid, value);
        } else if (key.equals(ADVANCED_STUN_SERVER)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.StunServer, value);
        } else if (key.equals(ADVANCED_STUN_SERVER_PORT)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.StunServerPort, value);
        }
    }

    private void setListValue(ListPreference p, String value) {
        String key = p.getKey();
        JRAccount account = JRAccount.getInstance();
        p.setValue(value);
        p.setSummary(value);
        if (key.equals(ACCOUNT_REGTYPE)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RegType, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_HEARTBEAT)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Heartbeat, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_SESS_TYPE)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SessType, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_AUDIO_PTIME)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.AudioPtime, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_DTMF_TYPE)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.DtmfType, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_VIDEO_FEC_TYPE)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoFecType, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_SEND_AGC_MODE)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgc, JRAccount.getInstance().getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgc));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgcMode, "-1");
            } else {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgc, JRAccount.getInstance().getAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgc));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAgcMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_RECV_AGC_MODE)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgc, JRAccount.getInstance().getAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgc));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgcMode, "-1");
            } else {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgc, JRAccount.getInstance().getAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgc));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAgcMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_SEND_ANR_MODE)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAnr, String.valueOf(false));
            } else {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAnr, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendAnrMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_RECV_ANR_MODE)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAnr, String.valueOf(false));
            } else {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAnr, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RecvAnrMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_VAD)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Vad, String.valueOf(false));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.VadMode, "-1");
            } else {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Vad, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.VadMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_AEC)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Aec, String.valueOf(false));
            } else {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Aec, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.AecMode, String.valueOf(p.findIndexOfValue(value) - 1));
            }
        } else if (key.equals(ADVANCED_H264_PACKET_MODE)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.H264PacketMode, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_RESOLUTION_MAX)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoWAndH, value);
        } else if (key.equals(ADVANCED_FIRBYINFO)) {
            if (p.findIndexOfValue(value) == 0) {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Fir, String.valueOf(false));
            } else if (p.findIndexOfValue(value) == 1) {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Fir, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.FirByInfo, String.valueOf(false));
            } else if (p.findIndexOfValue(value) == 2) {
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Fir, String.valueOf(true));
                account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.FirByInfo, String.valueOf(true));
            }
        } else if (key.equals(SRTP_TYPE)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SrtpType, String.valueOf(p.findIndexOfValue(value)));
        } else if (key.equals(ADVANCED_NAT_TYPE)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.NatType, String.valueOf(p.findIndexOfValue(value)));
        }
    }

    private void setSwitchValue(CheckBoxPreference p, boolean value) {
        String key = p.getKey();
        JRAccount account = JRAccount.getInstance();
        p.setChecked(value);
        if (key.equals(ACCOUNT_REG_NO_DIGEST)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.RegNoDigest, String.valueOf(value));
        } else if (key.equals(ADVANCED_OPEN_SUBS)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.OpenSubs, String.valueOf(value));
        } else if (key.equals(ADVANCED_USE_TEL_URI)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.UseTelUri, String.valueOf(value));
        } else if (key.equals(ADVANCED_FEC)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Fec, String.valueOf(value));
        } else if (key.equals(ADVANCED_VIDEO_BITRATE_CONTROL)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.BitrateControl, String.valueOf(value));
        } else if (key.equals(ADVANCED_RESOLUTION_CONTROL)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.ResolutionControl, String.valueOf(value));
        } else if (key.equals(ADVANCED_FRAMERATE_CONTROL)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoFramerateControl, String.valueOf(value));
        } else if (key.equals(ADVANCED_RPSI)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Rpsi, String.valueOf(value));
        } else if (key.equals(ADVANCED_VIDEO_FEC)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoFec, String.valueOf(value));
        } else if (key.equals(ADVANCED_NACK)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Nack, String.valueOf(value));
        } else if (key.equals(ADVANCED_RTX)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Rtx, String.valueOf(value));
        } else if (key.equals(ADVANCED_BEM)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.Bem, String.valueOf(value));
        } else if (key.equals(ADVANCED_AUDIO_RTCPMUX)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.AudioRtcpmux, String.valueOf(value));
        } else if (key.equals(ADVANCED_VIDEO_RTCPMUX)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.VideoRtcpmux, String.valueOf(value));
        } else if (key.equals(ADVANCED_MESSAGE_DELI_FAIL)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendDeliFail, String.valueOf(value));
        } else if (key.equals(ADVANCED_MESSAGE_DELI_SUCC)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendDeliSucc, String.valueOf(value));
        } else if (key.equals(ADVANCED_MESSAGE_DELI_FORWARD)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendDeliForward, String.valueOf(value));
        } else if (key.equals(ADVANCED_MESSAGE_SEND_DISP_REQ)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.SendDispReq, String.valueOf(value));
        } else if (key.equals(ADVANCED_GROUP_MESSAGE_DELI_FAIL)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.GroupSendDeliFail, String.valueOf(value));
        } else if (key.equals(ADVANCED_GROUP_MESSAGE_DELI_SUCC)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.GroupSendDeliSucc, String.valueOf(value));
        } else if (key.equals(ADVANCED_GROUP_MESSAGE_SEND_DISP_REQ)) {
            account.setAccountConfig(mAccount, JRAccountDefine.ConfigKey.GroupSendDispReq, String.valueOf(value));
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
    private final static String ADVANCED_CONF_ID = "advanced_conf_id";//多方通话appid
    //回执配置
    public final static String ADVANCED_GROUP_MESSAGE_SEND_DISP_REQ = "advanced_group_message_send_disp_req";
    public final static String ADVANCED_GROUP_MESSAGE_DELI_FAIL = "advanced_group_message_deli_fail";
    public final static String ADVANCED_GROUP_MESSAGE_DELI_SUCC = "advanced_group_message_deli_succ";
    public final static String ADVANCED_MESSAGE_DELI_FORWARD = "advanced_message_deli_forward";
    public final static String ADVANCED_MESSAGE_SEND_DISP_REQ = "advanced_message_send_disp_req";
    public final static String ADVANCED_MESSAGE_DELI_SUCC = "advanced_message_deli_succ";
    public final static String ADVANCED_MESSAGE_DELI_FAIL = "advanced_message_deli_fail";
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
    public final static String ADVANCED_VIDEO_FEC_TYPE = "advanced_video_fec_type";
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