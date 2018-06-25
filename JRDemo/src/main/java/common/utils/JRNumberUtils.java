package common.utils;

import android.content.Context;
import android.text.TextUtils;

import com.juphoon.rcs.JRClient;
import com.juphoon.rcs.JRClientConstants;
import com.juphoon.rcs.jrdemo.JRCommonValue;
import com.juphoon.rcs.jrdemo.R;

/**
 * Created by Upon on 2018/2/27.
 */

public class JRNumberUtils {
    public static boolean isSelf(String number) {
        return TextUtils.equals(number, JRClient.getInstance().getCurLoginNumber());
    }

    private static boolean contactsIsDialable(char c) {
        return c == '+' || c == '*' || c == '#' || (c >= '0' && c <= '9');
    }

    public static String formatPhoneByCountryCode(String phone) {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        if (phone.startsWith("00")) {
            sb.append("+");
            i = 2;
        } else if (!phone.startsWith("+")) {
            sb.append("+86");
        }
        for (; i < phone.length(); ++i) {
            char c = phone.charAt(i);
            if (contactsIsDialable(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String getStatMsg(JRClientConstants.RegErrorCode statCode, Context context) {
        String loginState = "";
        if (statCode == JRClientConstants.RegErrorCode.SEND_MSG) {
            loginState = context.getString(R.string.login_error_send_msg);
        } else if (statCode == JRClientConstants.RegErrorCode.AUTH_FAILED) {
            loginState = context.getString(R.string.login_error_auth_fail);
        } else if (statCode == JRClientConstants.RegErrorCode.INVALID_USER) {
            loginState = context.getString(R.string.login_error_invalid_user);
        } else if (statCode == JRClientConstants.RegErrorCode.ERR_TIMEOUT) {
            loginState = context.getString(R.string.login_error_timeout);
        } else if (statCode == JRClientConstants.RegErrorCode.SERV_BUSY) {
            loginState = context.getString(R.string.login_error_srv_busy);
        } else if (statCode == JRClientConstants.RegErrorCode.SERV_NOT_REACH) {
            loginState = context.getString(R.string.login_error_not_reach);
        } else if (statCode == JRClientConstants.RegErrorCode.SRV_FORBIDDEN) {
            loginState = context.getString(R.string.login_error_srv_forbidden);
        } else if (statCode == JRClientConstants.RegErrorCode.SRV_UNAVAIL) {
            loginState = context.getString(R.string.login_error_srv_unavailable);
        } else if (statCode == JRClientConstants.RegErrorCode.DNS_QRY) {
            loginState = context.getString(R.string.login_error_dns_qry);
        } else if (statCode == JRClientConstants.RegErrorCode.NETWORK) {
            loginState = context.getString(R.string.login_error_network);
        } else if (statCode == JRClientConstants.RegErrorCode.INTERNAL) {
            loginState = context.getString(R.string.login_error_internal);
        } else if (statCode == JRClientConstants.RegErrorCode.REJECTED) {
            loginState = context.getString(R.string.login_error_rejected);
        } else if (statCode == JRClientConstants.RegErrorCode.OTHER) {
            loginState = context.getString(R.string.login_error_other);
        } else if (statCode == JRClientConstants.RegErrorCode.SIP_SESS) {
            loginState = context.getString(R.string.login_error_sip_sess);
        } else if (statCode == JRClientConstants.RegErrorCode.UNREG) {
            loginState = context.getString(R.string.login_error_unreg);
        } else if (statCode == JRClientConstants.RegErrorCode.INVALID_ADDR) {
            loginState = context.getString(R.string.login_error_invalid_addr);
        } else if (statCode == JRClientConstants.RegErrorCode.NOT_FOUND) {
            loginState = context.getString(R.string.login_error_not_found);
        } else if (statCode == JRClientConstants.RegErrorCode.AUTH_REJECT) {
            loginState = context.getString(R.string.login_error_auth_reject);
        } else if (statCode == JRClientConstants.RegErrorCode.ID_NOT_MATCH) {
            loginState = context.getString(R.string.login_error_not_match);
        } else if (statCode == JRClientConstants.RegErrorCode.USER_NOT_EXIST) {
            loginState = context.getString(R.string.login_error_user_not_exist);
        }
//        else {
//            loginState = context.getString(R.string.logouted);
//        }
        return loginState;
    }
}
