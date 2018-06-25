package common.utils;

import android.content.Context;

import com.cmcc.sso.sdk.auth.AuthnHelper;
import com.cmcc.sso.sdk.auth.TokenListener;
import com.cmcc.sso.sdk.util.SsoSdkConstants;

/**
 * Created by Kevin on 2018/3/30.
 */

public class CmccTokenManager {

    private static final String TAG = "CmccTokenManager";
    private static final String APP_ID = "01000184";
    private static final String APP_KEY = "B5DC537F35FBFBFD";

    private static CmccTokenManager mCmccTokenManager;
    private static AuthnHelper mAuthnHelper;

    public static CmccTokenManager getInstance(Context ctx) {
        if (mCmccTokenManager == null) {
            mCmccTokenManager = new CmccTokenManager();
        }
        if (mAuthnHelper == null) {
            mAuthnHelper = new AuthnHelper(ctx.getApplicationContext());
        }
        return mCmccTokenManager;
    }

    public void getAppPassword(String userName, TokenListener tokenListener) {
        if (mAuthnHelper == null) {
            return;
        }
        mAuthnHelper.getAppPassword(APP_ID, APP_KEY, userName, SsoSdkConstants.LOGIN_TYPE_DEFAULT, tokenListener);
    }

}
