package com.juphoon.rcs.jrdemo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.NetworkUtil;
import com.baidu.mapapi.SDKInitializer;
import com.juphoon.rcs.JRCall;
import com.juphoon.rcs.JRCallCallback;
import com.juphoon.rcs.JRCallConstants;
import com.juphoon.rcs.JRCallItem;
import com.juphoon.rcs.JRClient;
import com.juphoon.rcs.JRLog;
import com.juphoon.rcs.utils.MtcAudioUtils;

import java.io.Serializable;

import common.CommonValue;
import io.realm.Realm;

/**
 * Created by Upon on 2017/12/17.
 */

public class MainApplication extends Application implements JRCallCallback {
    public static Context sApp;
    public static boolean sSdkInitSucceed = false;
    private int mCount = 0;

    @Override
    public void onCreate() {
        sApp = this;
        SDKInitializer.initialize(this);
        Realm.init(this);
        MtcAudioUtils.init(this);
        JRCall.getInstance().addCallback(this);
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

    @Override
    public void onCallItemUpdated(JRCallItem item, JRCallConstants.Error error, int updateType) {

    }

    @Override
    public void onCallItemRemove(JRCallItem item, JRCallConstants.TremReason error) {
    }

    @Override
    public void onCallItemAdd(JRCallItem item) {
        if(item.getCallDirection() == JRCallConstants.Direction.INCOMING) {
            Intent intent = new Intent(this, JRCallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }
}
