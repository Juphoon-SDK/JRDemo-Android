package com.juphoon.jrsdk.ui.account;

import android.text.TextUtils;

import com.juphoon.cmcc.app.lemon.MtcCallDb;
import com.juphoon.cmcc.app.lemon.MtcCli;
import com.juphoon.cmcc.app.lemon.MtcCliCfg;
import com.juphoon.cmcc.app.lemon.MtcCliDb;
import com.juphoon.cmcc.app.lemon.MtcCliDbConstants;
import com.juphoon.cmcc.app.lemon.MtcCommonConstants;
import com.juphoon.cmcc.app.lemon.MtcConfDb;
import com.juphoon.cmcc.app.lemon.MtcCpDb;
import com.juphoon.cmcc.app.lemon.MtcImDb;
import com.juphoon.cmcc.app.lemon.MtcNumber;
import com.juphoon.cmcc.app.lemon.MtcProf;
import com.juphoon.cmcc.app.lemon.MtcProfDb;
import com.juphoon.cmcc.app.lemon.MtcString;

import java.util.ArrayList;

public class RcsAccount {

    public static final int ACCOUNT_TYPE_NORMAL = 0;
    public static final int ACCOUNT_TYPE_CMCC = ACCOUNT_TYPE_NORMAL + 1;

    private static final String ACCOUNT_TYPE = "account_type";
    private static final String ACCOUNT_ORIG_PASS = "account_orig_pass";

    private int mBrHi;
    private int mBrLo;
    private int mFrHi;
    private int mFrLo;
    private int mVideoResolutionW;
    private int mVideoResolutionH;
    private int mSesstmrInfoType;
    private int mSesstmrInfoLen;
    private int mSesstmrInfoMinLen;
    private int mSesstmrInfoRefreshTpe;
    private static RcsAccount sInstance;

    public static RcsAccount getInstance() {
        if (sInstance == null) {
            sInstance = new RcsAccount();
        }
        return sInstance;
    }

    public static boolean hasOpenedAccount() {
        return !TextUtils.isEmpty("");
    }

    public static String getMtcCurrentAccount() {
        return "";
    }

    public void open(String account) {
        MtcCli.Mtc_CliOpen(account);
        MtcNumber piBrHi = new MtcNumber();
        MtcNumber piBrLo = new MtcNumber();
        MtcNumber piFrHi = new MtcNumber();
        MtcNumber piFrLo = new MtcNumber();
        MtcCallDb.Mtc_CallDbGetVideoArsParm(piBrHi, piBrLo, piFrHi, piFrLo);
        mBrHi = piBrHi.getValue();
        mBrLo = piBrLo.getValue();
        mFrHi = piFrHi.getValue();
        mFrLo = piFrLo.getValue();
        MtcNumber piVideoresolutionW = new MtcNumber();
        MtcNumber piVideoresolutionH = new MtcNumber();
        MtcCallDb.Mtc_CallDbGetVideoResolution(piVideoresolutionW, piVideoresolutionH);
        mVideoResolutionW = piVideoresolutionW.getValue();
        mVideoResolutionH = piVideoresolutionH.getValue();
        MtcNumber piSesstmrInfoType = new MtcNumber();
        MtcNumber piSesstmrInfoLen = new MtcNumber();
        MtcNumber piSesstmrInfoMinLen = new MtcNumber();
        MtcNumber piSesstmrRefreshType = new MtcNumber();
        MtcCallDb.Mtc_CallDbGetSessTmrInfo(piSesstmrInfoType, piSesstmrRefreshType, piSesstmrInfoLen, piSesstmrInfoMinLen);
        mSesstmrInfoType = piSesstmrInfoType.getValue();
        mSesstmrInfoLen = piSesstmrInfoLen.getValue();
        mSesstmrInfoMinLen = piSesstmrInfoMinLen.getValue();
        mSesstmrInfoRefreshTpe = piSesstmrRefreshType.getValue();
    }

    public int getAccountType() {
        MtcNumber number = new MtcNumber();
        if (MtcCommonConstants.ZFAILED == MtcProfDb.Mtc_ProfDbGetExtnParmInt(ACCOUNT_TYPE, number)) {
            return ACCOUNT_TYPE_NORMAL;
        }
        return number.getValue();
    }

    public void setAccountType(int type) {
        MtcProfDb.Mtc_ProfDbSetExtnParmInt(ACCOUNT_TYPE, type);
    }

    public String getAccountOrigPass() {
        MtcString string = new MtcString();
        if (MtcCommonConstants.ZFAILED == MtcProfDb.Mtc_ProfDbGetExtnParm(ACCOUNT_ORIG_PASS, string)) {
            return "";
        }
        return string.getValue();
    }

    public void setAccountOrigPass(String pass) {
        MtcProfDb.Mtc_ProfDbSetExtnParm(ACCOUNT_ORIG_PASS, pass);
    }

    public String getMtcUserName() {
        return MtcCliDb.Mtc_CliDbGetUserName();
    }

    public void setMtcUserName(String userName) {
        MtcCliDb.Mtc_CliDbSetUserName(userName);
        MtcCliDb.Mtc_CliDbSetImpu("sip:" + userName + "@" + MtcCliDb.Mtc_CliDbGetSipRegRealm());
    }

    public String getMtcAuthName() {
        return MtcCliDb.Mtc_CliDbGetAuthName();
    }

    public void setMtcAuthName(String authName) {
        MtcCliDb.Mtc_CliDbSetAuthName(authName);
    }

    public String getMtcAuthPass() {
        return MtcCliDb.Mtc_CliDbGetAuthPass();
    }

    public void setMtcAuthPass(String authPass) {
        MtcCliDb.Mtc_CliDbSetAuthPass(authPass);
    }

    public String getMtcNickName() {
        return MtcCliDb.Mtc_CliDbGetNickName();
    }

    public void setMtcNickName(String nickName) {
        MtcCliDb.Mtc_CliDbSetNickName(nickName);
    }

    public String getMtcAccNetworkInfo() {
        return MtcCliDb.Mtc_CliDbGetAccNetInfo();
    }

    public void setMtcAccNetworkInfo(String pcInfo) {
        MtcCliDb.Mtc_CliDbSetAccNetInfo(pcInfo);
    }

    public int getMtcAccNetworkType() {
        return MtcCliDb.Mtc_CliDbGetAccNetType();
    }

    public void setMtcAccNetworkType(int netType) {
        MtcCliDb.Mtc_CliDbSetAccNetType(netType);
    }

    public String getMtcSipRegIp() {
        return MtcCliDb.Mtc_CliDbGetSipRegIp();
    }

    public void setMtcSipRegIp(String sipRegIp) {
        MtcCliDb.Mtc_CliDbSetSipRegIp(sipRegIp);
    }

    public String getMtcSipRegRealm() {
        return MtcCliDb.Mtc_CliDbGetSipRegRealm();
    }

    public void setMtcSipRegRealm(String sipRegRealm) {
        MtcCliDb.Mtc_CliDbSetSipRegRealm(sipRegRealm);
        MtcCliDb.Mtc_CliDbSetImpu("sip:" + MtcCliDb.Mtc_CliDbGetUserName() + "@" + sipRegRealm);
    }

    public int getMtcRegType() {
        return MtcCliCfg.Mtc_CliCfgGetRegSrvType();
    }

    public void setMtcRegType(int type) {
        MtcCliCfg.Mtc_CliCfgSetRegSrvType(type);
    }

    public short getMtcSipRegTpt() {
        return MtcCliDb.Mtc_CliDbGetSipRegTpt();
    }

    public void setMtcAreaCode(String value) {
        MtcProfDb.Mtc_ProfDbSetAreaCode(value);
    }

    public String getMtcAreaCode() {
        return MtcProfDb.Mtc_ProfDbGetAreaCode();
    }

    public void setMtcSipRegTpt(short sipRegTpt) {
        MtcCliDb.Mtc_CliDbSetSipRegTpt(sipRegTpt);
    }

    public int getMtcSipRegUdpPort() {
        return MtcCliDb.Mtc_CliDbGetSipRegUdpPort();
    }

    public void setMtcSipRegUdpPort(int SipRegUdpPort) {
        MtcCliDb.Mtc_CliDbSetSipRegUdpPort(SipRegUdpPort);
    }

    public int getMtcSipRegTcpPort() {
        return MtcCliDb.Mtc_CliDbGetSipRegTcpPort();
    }

    public void setMtcSipRegTcpPort(int SipRegTcpPort) {
        MtcCliDb.Mtc_CliDbSetSipRegTcpPort(SipRegTcpPort);
    }

    public int getMtcSipRegTlsPort() {
        return MtcCliDb.Mtc_CliDbGetSipRegTlsPort();
    }

    public void setMtcSipRegTlsPort(int SipRegTlsPort) {
        MtcCliDb.Mtc_CliDbSetSipRegTlsPort(SipRegTlsPort);
    }

    public int getMtcExpireTime() {
        return MtcCliDb.Mtc_CliDbGetRegExpire();
    }

    public void setMtcExpireTime(int expire) {
        MtcCliDb.Mtc_CliDbSetRegExpire(expire);
    }

    public int getMtcRegSubExpireTime() {
        return MtcCliDb.Mtc_CliDbGetSubsRegExpire();
    }

    public void setMtcRegSubExpireTime(int expire) {
        MtcCliDb.Mtc_CliDbSetSubsRegExpire(expire);
    }

    public int getMtcTlsVeryType() {
        return MtcCliDb.Mtc_CliDbGetTlsCliVeryType();
    }

    public void setMtcTlsVeryType(int value) {
        MtcCliDb.Mtc_CliDbSetTlsCliVeryType(value);
    }

    public ArrayList<String> getMtcAudioCodecArray() {
        int size = MtcCallDb.Mtc_CallDbGetAudioCodecCount();
        ArrayList<String> audioCodecArray = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            audioCodecArray.add(MtcCallDb.Mtc_CallDbGetAudioCodecByPriority((short) i));
        }
        return audioCodecArray;
    }

    public void setMtcAudioCodecArray(ArrayList<String> audioCodecArray) {
        int size = MtcCallDb.Mtc_CallDbGetSuptAudioCodecCount();
        for (int i = 0; i < size; i++) {
            String audioCodec = MtcCallDb.Mtc_CallDbGetSuptAudioCodec(i);
            if (audioCodecArray.contains(audioCodec)) {
                MtcCallDb.Mtc_CallDbSetAudioCodecEnable(audioCodec, true);
            } else {
                MtcCallDb.Mtc_CallDbSetAudioCodecEnable(audioCodec, false);
            }
        }
        for (int i = 0; i < audioCodecArray.size(); i++) {
            String audioCodec = audioCodecArray.get(i);
            MtcCallDb.Mtc_CallDbSetAudioCodecByPriority(audioCodec, (short) i);
        }
    }

    public ArrayList<String> getMtcVideoCodecArray() {
        int size = MtcCallDb.Mtc_CallDbGetVideoCodecCount();
        ArrayList<String> videoCodecArray = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            videoCodecArray.add(MtcCallDb.Mtc_CallDbGetVideoCodecByPriority((short) i));
        }
        return videoCodecArray;
    }

    public void setMtcVideoCodecArray(ArrayList<String> videoCodecArray) {
        int size = MtcCallDb.Mtc_CallDbGetSuptVideoCodecCount();
        for (int i = 0; i < size; i++) {
            String videoCodec = MtcCallDb.Mtc_CallDbGetSuptVideoCodec(i);
            if (videoCodecArray.contains(videoCodec)) {
                MtcCallDb.Mtc_CallDbSetVideoCodecEnable(videoCodec, true);
            } else {
                MtcCallDb.Mtc_CallDbSetVideoCodecEnable(videoCodec, false);
            }
        }
        for (int i = 0; i < videoCodecArray.size(); i++) {
            String videoCodec = videoCodecArray.get(i);
            MtcCallDb.Mtc_CallDbSetVideoCodecByPriority(videoCodec, (short) i);
        }
    }

    public boolean getMtcRegNoDigest() {
        return MtcCliDb.Mtc_CliDbGetRegNoDigest();
    }

    public void setMtcRegNoDigest(boolean value) {
        MtcCliDb.Mtc_CliDbSetRegNoDigest(value);
    }

    public boolean getMtcUseIpv4() {
        return MtcCliDb.Mtc_CliDbGetUseIpv4();
    }

    public void setMtcUseIpv4(boolean value) {
        MtcCliDb.Mtc_CliDbSetUseIpv4(value);
    }

    public boolean getMtcMDMEnable() {
        return MtcCallDb.Mtc_CallDbGetMdmEnable();
    }

    public void setMtcMDMEnable(boolean MDMEnable) {
        MtcCallDb.Mtc_CallDbSetMdmEnable(MDMEnable);
    }

    public void setMtcLogLevel(int level) {
        MtcCliCfg.Mtc_CliCfgSetLogLevel(level);
    }

    public boolean getMtcAgcEnable() {
        return MtcCallDb.Mtc_CallDbGetAgcEnable();
    }

    public void setMtcAgcEnable(boolean agcEnable) {
        MtcCallDb.Mtc_CallDbSetAgcEnable(agcEnable);
    }

    public boolean getMtcRxAgcEnable() {
        return MtcCallDb.Mtc_CallDbGetRxAgcEnable();
    }

    public void setMtcRxAgcEnable(boolean rxAgcEnable) {
        MtcCallDb.Mtc_CallDbSetRxAgcEnable(rxAgcEnable);
    }

    public boolean getMtcAnrEnable() {
        return MtcCallDb.Mtc_CallDbGetAnrEnable();
    }

    public void setMtcAnrEnable(boolean anrEnable) {
        MtcCallDb.Mtc_CallDbSetAnrEnable(anrEnable);
    }

    public boolean getMtcRxAnrEnable() {
        return MtcCallDb.Mtc_CallDbGetRxAnrEnable();
    }

    public void setMtcRxAnrEnable(boolean rxAnrEnable) {
        MtcCallDb.Mtc_CallDbSetRxAnrEnable(rxAnrEnable);
    }

    public boolean getMtcVadEnable() {
        return MtcCallDb.Mtc_CallDbGetVadEnable();
    }

    public void setMtcVadEnable(boolean vadEnabel) {
        MtcCallDb.Mtc_CallDbSetVadEnable(vadEnabel);
    }

    public int getMtcPtime() {
        return MtcCallDb.Mtc_CallDbGetPtime();
    }

    public void setMtcPtime(int ptime) {
        MtcCallDb.Mtc_CallDbSetPtime(ptime);
    }

    public int getMtcBrHi() {
        return mBrHi;
    }

    public void setMtcBrHi(int brHi) {
        mBrHi = brHi;
        MtcCallDb.Mtc_CallDbSetVideoArsParm(mBrHi, mBrLo, mFrHi, mFrLo);
    }

    public int getMtcBrLo() {
        return mBrLo;
    }

    public void setMtcBrLo(int brLo) {
        mBrLo = brLo;
        MtcCallDb.Mtc_CallDbSetVideoArsParm(mBrHi, mBrLo, mFrHi, mFrLo);
    }

    public int getMtcFrHi() {
        return mFrHi;
    }

    public void setMtcFrHi(int frHi) {
        mFrHi = frHi;
        MtcCallDb.Mtc_CallDbSetVideoArsParm(mBrHi, mBrLo, mFrHi, mFrLo);
    }

    public int getMtcFrLo() {
        return mFrHi;
    }

    public void setMtcFrLo(int frLo) {
        mFrLo = frLo;
        MtcCallDb.Mtc_CallDbSetVideoArsParm(mBrHi, mBrLo, mFrHi, mFrLo);
    }

    public int getMtcVideoBitrate() {
        return MtcCallDb.Mtc_CallDbGetVideoBitrate();
    }

    public void setMtcVideoBitrate(int videoBitrate) {
        MtcCallDb.Mtc_CallDbSetVideoBitrate(videoBitrate);
    }

    public int getMtcVideoResolutionW() {
        return mVideoResolutionW;
    }

    public void setMtcVideoResolutionW(int videoResolutionW) {
        mVideoResolutionW = videoResolutionW;
        MtcCallDb.Mtc_CallDbSetVideoResolution(mVideoResolutionW, mVideoResolutionH);
    }

    public int getMtcVideoResolutionH() {
        return mVideoResolutionH;
    }

    public void setMtcVideoResolutionH(int videoResolutionH) {
        mVideoResolutionH = videoResolutionH;
        MtcCallDb.Mtc_CallDbSetVideoResolution(mVideoResolutionW, mVideoResolutionH);
    }

    public boolean getMtcAutoCodecPriority() {
        return MtcCallDb.Mtc_CallDbGetCodecPriorityAutoAdjust();
    }

    public void setMtcAutoCodecPriority(boolean value) {
        MtcCallDb.Mtc_CallDbSetCodecPriorityAutoAdjust(value);
    }

    public boolean getMtcVideoResolutionControl() {
        return MtcCallDb.Mtc_CallDbGetResolutionControl();
    }

    public void setMtcVideoResolutionControl(boolean resolutionControl) {
        MtcCallDb.Mtc_CallDbSetResolutionControl(resolutionControl);
    }

    public int getMtcVideoFramerate() {
        return MtcCallDb.Mtc_CallDbGetVideoFramerate();
    }

    public void setMtcVideoFramerate(int videoFramerate) {
        MtcCallDb.Mtc_CallDbSetVideoFramerate(videoFramerate);
    }

    public boolean getMtcVideoFramerateControl() {
        return MtcCallDb.Mtc_CallDbGetFramerateControl();
    }

    public void setMtcVideoFramerateControl(boolean framerateControl) {
        MtcCallDb.Mtc_CallDbSetFramerateControl(framerateControl);
    }

    public short getMtcArsMode() {
        return MtcCallDb.Mtc_CallDbGetArsMode();
    }

    public void setMtcArsMode(short arsMode) {
        MtcCallDb.Mtc_CallDbSetArsMode(arsMode);
    }

    public boolean getMtcFirByInfo() {
        return MtcCallDb.Mtc_CallDbGetFirByInfo();
    }

    public void setMtcFirByInfo(boolean firByInfo) {
        MtcCallDb.Mtc_CallDbSetFirByInfo(firByInfo);
    }

    public int getMtcKeyPeriod() {
        return MtcCallDb.Mtc_CallDbGetKeyPeriod();
    }

    public void setMtcKeyPeriod(int keyPeriod) {
        MtcCallDb.Mtc_CallDbSetKeyPeriod(keyPeriod);
    }

    public boolean getMtcSmallNaluEnable() {
        return MtcCallDb.Mtc_CallDbGetSmallNaluEnable();
    }

    public void setMtcSmallNaluEnable(boolean smallNaluEnable) {
        MtcCallDb.Mtc_CallDbSetSmallNaluEnable(smallNaluEnable);
    }

    public boolean getMtcVideoBem() {
        return MtcCallDb.Mtc_CallDbGetVideoBem();
    }

    public void setMtcVideoBem(boolean videoBem) {
        MtcCallDb.Mtc_CallDbSetVideoBem(videoBem);
    }

    // public boolean getMtcAudioArs() {
    // return mAudioArs;
    // }
    //
    // public void setMtcAudioArs(boolean audioArs) {
    // mAudioArs = audioArs;
    // }

    public boolean getMtcAudioRedFec() {
        return MtcCallDb.Mtc_CallDbGetAudioRed();
    }

    public void setMtcAudioRedFec(boolean audioRedFec) {
        MtcCallDb.Mtc_CallDbSetAudioRed(audioRedFec);
    }

    public boolean getMtcVideoArs() {
        return MtcCallDb.Mtc_CallDbGetVideoArs();
    }

    public void setMtcVideoArs(boolean videoArs) {
        MtcCallDb.Mtc_CallDbSetVideoArs(videoArs);
    }

    public boolean getMtcVideoRedFec() {
        return false;
    }

    public void setMtcVideoRedFec(boolean videoRedFec) {
//        MtcCallDb.Mtc_CallDbSetVideoRedFec(videoRedFec);
    }

    public boolean getMtcNackEnable() {
        return MtcCallDb.Mtc_CallDbGetNackEnable();
    }

    public void setMtcNackEnable(boolean nackEnable) {
        MtcCallDb.Mtc_CallDbSetNackEnable(nackEnable);
    }

    public boolean getMtcRpsiEnable() {
        return MtcCallDb.Mtc_CallDbGetRpsiEnable();
    }

    public void setMtcRpsiEnable(boolean rpsiEnable) {
        MtcCallDb.Mtc_CallDbSetRpsiEnable(rpsiEnable);
    }

    // public boolean getMtcAsymNego() {
    // return mAsymNego;
    // }

    // public void setMtcAsymNego(boolean asymNego) {
    // mAsymNego = asymNego;
    // }

    public int getMtcDtmfType() {
        return MtcCallDb.Mtc_CallDbGetDtmfType();
    }

    public void setMtcDtmfType(int dtmfType) {
        MtcCallDb.Mtc_CallDbSetDtmfType(dtmfType);
    }

    public int getMtcDtmPayload() {
        return MtcCallDb.Mtc_CallDbGetDtmfPayload();
    }

    public void setMtcDtmPayload(int dtmPayload) {
        MtcCallDb.Mtc_CallDbSetDtmfPayload(dtmPayload);
    }

    public boolean getMtcMediaRtcpMuxEnable() {
        return MtcCallDb.Mtc_CallDbGetAudioRtcpMuxEnable();
    }

    public void setMtcMediaRtcpMuxEnable(boolean mediaRtcpMuxEnable) {
        MtcCallDb.Mtc_CallDbSetAudioRed(mediaRtcpMuxEnable);
    }

    // public int getMtcNatType() {
    // return mNatType;
    // }
    //
    // public void setMtcNatType(int natType) {
    // mNatType = natType;
    // }

    // public boolean getMtcNatSupportTURN() {
    // return mNatSupportTURN;
    // }
    //
    // public void setMtcNatSupportTURN(Boolean supportTURN) {
    // mNatSupportTURN = supportTURN;
    // }

    public String getMtcNatServer() {
        return MtcCliDb.Mtc_CliDbGetStunServerName();
    }

    public void setMtcNatServer(String natServer) {
        MtcCliDb.Mtc_CliDbSetStunServerName(natServer);
    }

    public int getMtcNatPort() {
        return MtcCliDb.Mtc_CliDbGetStunServerPort();
    }

    public void setMtcNatPort(int natPort) {
        MtcCliDb.Mtc_CliDbSetStunServerPort(natPort);
    }

    public int getMtcSesstmrInfoType() {
        return mSesstmrInfoType;
    }

    public void setMtcSesstmrInfoType(int sesstmrInfoType) {
        mSesstmrInfoType = sesstmrInfoType;
        MtcCallDb.Mtc_CallDbSetSessTmrInfo(mSesstmrInfoType, mSesstmrInfoRefreshTpe, mSesstmrInfoLen, mSesstmrInfoMinLen);
    }

    public int getMtcSesstmrInfoLen() {
        return mSesstmrInfoLen;
    }

    public void setMtcSesstmrInfoLen(int sesstmrInfoLen) {
        mSesstmrInfoLen = sesstmrInfoLen;
        MtcCallDb.Mtc_CallDbSetSessTmrInfo(mSesstmrInfoType, mSesstmrInfoRefreshTpe, mSesstmrInfoLen, mSesstmrInfoMinLen);
    }

    public int getMtcSesstmrInfoMinLen() {
        return mSesstmrInfoMinLen;
    }

    public void setMtcSesstmrInfoMinLen(int sesstmrInfoMinLen) {
        mSesstmrInfoMinLen = sesstmrInfoMinLen;
        MtcCallDb.Mtc_CallDbSetSessTmrInfo(mSesstmrInfoType, mSesstmrInfoRefreshTpe, mSesstmrInfoLen, mSesstmrInfoMinLen);
    }

    public int getMtcKeepAliveType() {
        return MtcCliDb.Mtc_CliDbGetKeepAliveType();
    }

    public void setMtcKeepAliveType(int keepAliveType) {
        MtcCliDb.Mtc_CliDbSetKeepAliveType(keepAliveType);
    }

    public void setMtcAcvServAddr(String acvServAddr) {
        MtcCliDb.Mtc_CliDbSetAcvServAddr(acvServAddr);
    }

    public void setMtcAcvUrl(String acvUrl) {
        MtcCliDb.Mtc_CliDbSetAcvUrl(acvUrl);
    }

    public void setMtcAcvServProt(int acvServPort) {
        MtcCliDb.Mtc_CliDbSetAcvServPort(acvServPort);
    }

    public int getMtcSrtpType() {
        return MtcCallDb.Mtc_CallDbGetSrtpCryptoType();
    }

    public void setMtcSrtpType(int srtpType) {
        MtcCallDb.Mtc_CallDbSetSrtpCryptoType(srtpType);
    }

    public boolean getMtcSrtpAuthRtp() {
        return MtcCallDb.Mtc_CallDbGetSrtpAuthRtp();
    }

    public void setMtcmSrtpAuthRtp(boolean srtpAuthRtp) {
        MtcCallDb.Mtc_CallDbSetSrtpAuthRtp(srtpAuthRtp);
    }

    public boolean getMtcSrtpEncRtcp() {
        return MtcCallDb.Mtc_CallDbGetSrtpEncryptRtcp();
    }

    public void setMtcSrtpEncRtcp(boolean srtpEncRtcp) {
        MtcCallDb.Mtc_CallDbSetSrtpEncryptRtcp(srtpEncRtcp);
    }

    public boolean getMtcSrtpEncRtp() {
        return MtcCallDb.Mtc_CallDbGetSrtpEncryptRtp();
    }

    public void setMtcSrtpEncRtp(boolean srtpEncRtp) {
        MtcCallDb.Mtc_CallDbSetSrtpEncryptRtp(srtpEncRtp);
    }

    public int getMtcAecMode() {
        return MtcCallDb.Mtc_CallDbGetAecMode();
    }

    public void setMtcAecMode(int mode) {
        MtcCallDb.Mtc_CallDbSetAecMode((short) mode);
    }

    public boolean getMtcAutoAccept() {
        return MtcImDb.Mtc_ImDbGetFtAutAccept();
    }

    public void setMtcAutoAccept(boolean autoAccept) {
        MtcImDb.Mtc_ImDbSetFtAutAccept(autoAccept);
    }

    public void setMtcThumbnailEnabled(boolean thumbnailEnabled) {
        MtcImDb.Mtc_ImDbSetFtThumb(thumbnailEnabled);
    }

    public boolean getMtcThumbnailEnabled() {
        return MtcImDb.Mtc_ImDbGetFtThumb();
    }

    public void setMtcUseSessMode(boolean value) {
        MtcImDb.Mtc_ImDbSetUseSessModeMsg(value);
    }

    public boolean getMtcUseSessMode() {
        return MtcImDb.Mtc_ImDbGetUseSessModeMsg();
    }

    public void setMtcMsrpMsgPrint(boolean value) {
        MtcCliDb.Mtc_CliDbSetMsrpLogMsgPrint(value);
    }

    public boolean getMtcMsrpMsgPrint() {
        return MtcCliDb.Mtc_CliDbGetMsrpLogMsgPrint();
    }

    public boolean getMtcServerForward() {
        return MtcImDb.Mtc_ImDbGetFtStAndFwEnabled();
    }

    public void setMtcServerForward(boolean serverForward) {
        MtcImDb.Mtc_ImDbSetFtStAndFwEnabled(serverForward);
    }

    public boolean getMtcUseTelUri() {
        return MtcCliDb.Mtc_CliDbGetUseTelUri();
    }

    public void setMtcUseTelUri(boolean useTelUri) {

        MtcCliDb.Mtc_CliDbSetUseTelUri(useTelUri);
    }

    public boolean getMtcCpm() {
        return MtcImDb.Mtc_ImDbGetImMsgTech() && MtcImDb.Mtc_ImDbGetStandaloneMsgAuth();
    }

    public void setMtcCpm(boolean cpm) {
        MtcImDb.Mtc_ImDbSetImMsgTech(cpm);
        MtcImDb.Mtc_ImDbSetStandaloneMsgAuth(cpm);
    }

    public boolean getMtcDeliveryRequest() {
//        return MtcImDb.Mtc_ImDbGetImdnSendDeliReqEnable();
        return false;
    }

    public void setMtcDeliveryRequest(boolean deliveryReques) {
//        MtcImDb.Mtc_ImDbSetImdnSendDeliReqEnable(deliveryReques);
    }

    public boolean getMtcDeliveryResponse() {
        return MtcImDb.Mtc_ImDbGetImdnSendDeliRspEnable();
    }

    public void setMtcDeliveryResponse(boolean deliveryResponse) {
        MtcImDb.Mtc_ImDbSetImdnSendDeliRspEnable(deliveryResponse);
    }

    public boolean getMtcDispRequest() {
        return MtcImDb.Mtc_ImDbGetImdnSendDispReqEnable();
    }

    public void setMtcDispRequest(boolean dispReques) {
        MtcImDb.Mtc_ImDbSetImdnSendDispReqEnable(dispReques);
    }

    public boolean getMtcDispResponse() {
        return MtcImDb.Mtc_ImDbGetImdnSendDispRspEnable();
    }

    public void setMtcDispResponse(boolean dispResponse) {
        MtcImDb.Mtc_ImDbSetImdnSendDispRspEnable(dispResponse);
    }

    public boolean getMtcMsrpOverTls() {
        return (MtcImDb.Mtc_ImDbGetMediaProtoType() == MtcImDb.EN_MTC_IM_PROTO_MSRP_TLS);
    }

    public void setMtcMsrpOverTls(boolean value) {
        MtcImDb.Mtc_ImDbSetMediaProtoType(value ? MtcImDb.EN_MTC_IM_PROTO_MSRP_TLS : MtcImDb.EN_MTC_IM_PROTO_MSRP_TCP);
    }

    public int getMtcMaxGroupIMSize() {
        return MtcImDb.Mtc_ImDbGetMaxSize1ToM();
    }

    public void setMtcMaxGroupIMSize(int size) {
        if (size < 0)
            return;
        MtcImDb.Mtc_ImDbSetMaxSize1ToM(size);
    }

    public int getMtcMaxFileSize() {
        return MtcImDb.Mtc_ImDbGetMaxSizeFileTr();
    }

    public void setMtcMaxFileSize(int size) {
        if (size < 0)
            return;
        MtcImDb.Mtc_ImDbSetMaxSizeFileTr(size);
    }

    public int getMtcWarnFileSize() {
        int warnSize = MtcImDb.Mtc_ImDbGetFtWarnSize();
        if (warnSize < 0) {
            return Integer.MAX_VALUE;
        }
        return warnSize;
    }

    public void setMtcWarnFileSize(int size) {
        if (size < 0)
            return;
        MtcImDb.Mtc_ImDbSetFtWarnSize(size);
    }

    public int getMtcMaxGroupMemberSize() {
        return MtcImDb.Mtc_ImDbGetMaxAdhocGroupSize();
    }

    public void setMtcMaxGroupMemberSize(int size) {
        if (size < 3)
            return;
        MtcImDb.Mtc_ImDbSetMaxAdhocGroupSize(size);
    }

    public boolean getMtcSessModeWithMsg() {
        return MtcImDb.Mtc_ImDbGetFirstMsgInvite();
    }

    public void setMtcSessModeWithMsg(boolean value) {
        MtcImDb.Mtc_ImDbSetFirstMsgInvite(value);
    }

    public String getMtcConferenceUri() {
        return MtcImDb.Mtc_ImDbGetConfFctyUri();
    }

    public void setMtcConferenceUri(String uri) {
        MtcImDb.Mtc_ImDbSetConfFctyUri(uri);
    }

    public String getMtcMultiTextUri() {
        return MtcImDb.Mtc_ImDbGetMultiTextUri();
    }

    public void setMtcMultiTextUri(String uri) {
        MtcImDb.Mtc_ImDbSetMultiTextUri(uri);
    }

    public String getMtcCallConfUri() {
        return MtcConfDb.Mtc_ConfDbGetFactUri();
    }

    public void setMtcCallConfUri(String uri) {
        MtcConfDb.Mtc_ConfDbSetFactUri(uri);
    }

    public boolean getMtcAutoAcceptGroupChat() {
        return MtcImDb.Mtc_ImDbGetAutAcceptGroupChat();
    }

    public void setMtcAutoAcceptGroupChat(boolean isAutoAccept) {
        MtcImDb.Mtc_ImDbSetAutAcceptGroupChat(isAutoAccept);
    }

    public boolean getMtcUserRegDetect() {
        return MtcCliDb.Mtc_CliDbGetUserRegDetect();
    }

    public void setMtcUserRegDetect(boolean bEnable) {
        MtcCliDb.Mtc_CliDbSetUserRegDetect(bEnable);
    }

    public int getMtcStgUseType() {
        return MtcCliDb.Mtc_CliDbGetStgUseType();
    }

    public void setMtcStgUseType(int type) {
        MtcCliDb.Mtc_CliDbSetStgUseType(type);
    }

    public int getMtcStgTunnelType() {
        return MtcCliDb.Mtc_CliDbGetStgTunnelType();
    }

    public void setMtcStgTunnelType(int type) {
        MtcCliDb.Mtc_CliDbSetStgTunnelType(type);
    }

    public String getMtcStgTunnelIp() {
        return MtcCliDb.Mtc_CliDbGetStgTunnelIp();
    }

    public void setMtcStgTunnelIp(String ip) {
        MtcCliDb.Mtc_CliDbSetStgTunnelIp(ip);
    }

    public int getMtcStgTunnelPort() {
        return MtcCliDb.Mtc_CliDbGetStgTunnelPort();
    }

    public void setMtcStgTunnelPort(int type) {
        MtcCliDb.Mtc_CliDbSetStgTunnelPort(type);
    }

    public boolean getMtcCpimBase64EncodeEnable() {
        return MtcImDb.Mtc_ImDbGetCpimBase64EncodeEnable();
    }

    public void setMtcCpimBase64EncodeEnable(boolean enabled) {
        MtcImDb.Mtc_ImDbSetCpimBase64EncodeEnable(enabled);
    }

    public boolean getMtcThumbBase64EncodeEnable() {
        return MtcImDb.Mtc_ImDbGetThumbBase64EncodeEnable();
    }

    public void setMtcThumbBase64EncodeEnable(boolean enabled) {
        MtcImDb.Mtc_ImDbSetThumbBase64EncodeEnable(enabled);
    }

    public void save() {
        MtcCliDb.Mtc_CliDbSetRoamType(MtcCliDbConstants.EN_MTC_ROAM_NONE);
        MtcCliDb.Mtc_CliDbApplyAll();
        MtcProf.Mtc_ProfSaveProvision();
    }

    public String getDMServerAddress() {
        return MtcCpDb.Mtc_CpDbGetSrvAddr();
    }

    public void setDMServerAddress(String address) {
        if (!TextUtils.isEmpty(address)) {
            MtcCpDb.Mtc_CpDbSetSrvAddr(address);
        }
    }

    public String getQRServerAddress() {
        return MtcCliDb.Mtc_CliDbGetQrcardServAddr();
    }

    public void setQRServerAddress(String qrServer) {
        if (!TextUtils.isEmpty(qrServer)) {
            MtcCliDb.Mtc_CliDbSetQrcardServAddr(qrServer);
        }
    }

    public String getProfileServerAddress() {
        return MtcCliDb.Mtc_CliDbGetProfileServAddr();
    }

    public void setProfileServerAddress(String profileServer) {
        if (!TextUtils.isEmpty(profileServer)) {
            MtcCliDb.Mtc_CliDbSetProfileServAddr(profileServer);
        }
    }

    public String getPublicAccountServerAddress() {
        return MtcCliDb.Mtc_CliDbGetPaServAddr();
    }

    public void setPublicAccountServerAddress(String paServer) {
        if (!TextUtils.isEmpty(paServer)) {
            MtcCliDb.Mtc_CliDbSetPaServAddr(paServer);
        }
    }


    public void setImdnSendDeliNtfy(boolean enable) {
        MtcImDb.Mtc_ImDbSetImdnDeliSuccRptSupt(enable);
    }

    public void setImdnSendFailedDeliNtfy(boolean enable) {
        MtcImDb.Mtc_ImDbSetImdnDeliFailRptSupt(enable);
    }

    public boolean getImdnSendDeliNtfy() {
        return MtcImDb.Mtc_ImDbGetImdnDeliSuccRptSupt();
    }

    public void setImdnDeliForwardRptSupt(boolean enable) {
        MtcImDb.Mtc_ImDbSetImdnDeliForwardRptSupt(enable);
    }

    public boolean getImdnDeliForwardRptSupt() {
        return MtcImDb.Mtc_ImDbGetImdnDeliForwardRptSupt();
    }

    public int getSipDscpValue() {
        return MtcCliDb.Mtc_CliDbGetSipDscpValue();
    }

    public void setSipDscpValue(int value) {
        MtcCliDb.Mtc_CliDbSetSipDscpValue(value);
    }

    public int getAudioDscpValue() {
        return MtcCallDb.Mtc_CallDbGetVoiceDscpValue();
    }

    public void setAudioDscpValue(int value) {
        MtcCallDb.Mtc_CallDbSetVoiceDscpValue(value);
    }

    public int getVideoDscpValue() {
        return MtcCallDb.Mtc_CallDbGetVideoDscpValue();
    }

    public void setVideoDscpValue(int value) {
        MtcCallDb.Mtc_CallDbSetVideoDscpValue(value);
    }
}
