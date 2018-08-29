package com.juphoon.jrsdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.juphoon.rcs.jrsdk.JRCall;
import com.juphoon.rcs.jrsdk.JRCallCallback;
import com.juphoon.rcs.jrsdk.JRCallConstants;
import com.juphoon.rcs.jrsdk.JRCallItem;
import com.juphoon.rcs.jrsdk.JRClient;
import com.juphoon.rcs.jrsdk.JRClientCallback;
import com.juphoon.rcs.jrsdk.JRClientConstants;
import com.juphoon.rcs.jrsdk.MtcAudioUtils;

import java.io.File;

import io.realm.Realm;

/**
 * Created by Upon on 2017/12/17.
 */

public class MainApplication extends Application implements JRCallCallback,JRClientCallback {
    public static Context sApp;
    public static boolean sSdkInitSucceed = false;
    private int mCount = 0;

    @Override
    public void onCreate() {
        sApp = this;
        SDKInitializer.initialize(this);
        Realm.init(this);
        RealmDataHelper.init(this);
        MtcAudioUtils.init(this);
        JRCall.getInstance().addCallback(this);
        initSDK();
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (mCount == 0) {
                    Log.e("yidao", ">>>>>>>>>>>>>>>切到前台");
                    if (sSdkInitSucceed) {
                        JRClient.getInstance().refresh();
                    }
                }
                mCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mCount--;
                if (mCount == 0) {
                    Log.e("yidao", ">>>>>>>>>>>>>>>切到后台");
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    private void initSDK() {
        JRClient.getInstance().addCallback(this);
        JRClient.getInstance().startInitSDK(this, JRClientConstants.DeviceType.APP, "");
    }

    @Override
    public void onCallItemAdd(JRCallItem item) {
        if(item.direction == JRCallConstants.CALL_DIRECTION_IN) {
            Intent intent = new Intent(this, JRCallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onCallItemRemove(JRCallItem item, int reason) {

    }

    @Override
    public void onCallItemUpdate(JRCallItem item, int type) {

    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onClientInitResult(boolean b, int i) {
        Log.e("yidao", "onClientInitResult: "+b );
    }

    @Override
    public void onClientLoginResult(boolean b, int i) {

    }

    @Override
    public void onClientLogoutResult(int i) {

    }

    @Override
    public void onClientStateChange(int i) {

    }
}
