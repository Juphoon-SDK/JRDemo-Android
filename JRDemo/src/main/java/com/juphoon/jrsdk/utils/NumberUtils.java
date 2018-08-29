package com.juphoon.jrsdk.utils;

import android.content.Context;
import android.text.TextUtils;

import com.juphoon.jrsdk.R;
import com.juphoon.rcs.jrsdk.JRClientConstants;

/**
 * Created by Upon on 2018/2/27.
 */

public class NumberUtils {

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

    public static String getStatMsg(int statCode, Context context) {
        String loginState = "";
        if (statCode == JRClientConstants.REG_ERROR_SEND_MESSAGE_ERROR) {
            loginState = context.getString(R.string.login_error_send_msg);
        } else if (statCode == JRClientConstants.REG_ERROR_AUTHENTICATION_FAILED) {
            loginState = context.getString(R.string.login_error_auth_fail);
        } else if (statCode == JRClientConstants.REG_ERROR_INVALID_USER) {
            loginState = context.getString(R.string.login_error_invalid_user);
        } else if (statCode == JRClientConstants.REG_ERROR_TIMEOUT) {
            loginState = context.getString(R.string.login_error_timeout);
        } else if (statCode == JRClientConstants.REG_ERROR_SERVER_BUSY) {
            loginState = context.getString(R.string.login_error_srv_busy);
        } else if (statCode == JRClientConstants.REG_ERROR_SERVER_NOT_REACHED) {
            loginState = context.getString(R.string.login_error_not_reach);
        } else if (statCode == JRClientConstants.REG_ERROR_SERVER_FORBIDDEN) {
            loginState = context.getString(R.string.login_error_srv_forbidden);
        } else if (statCode == JRClientConstants.REG_ERROR_SERVER_UNAVAILABLE) {
            loginState = context.getString(R.string.login_error_srv_unavailable);
        } else if (statCode == JRClientConstants.REG_ERROR_DNS_QUERY_FAILED) {
            loginState = context.getString(R.string.login_error_dns_qry);
        } else if (statCode == JRClientConstants.REG_ERROR_NETWORK_ERROR) {
            loginState = context.getString(R.string.login_error_network);
        } else if (statCode == JRClientConstants.REG_ERROR_INTERNAL_ERROR) {
            loginState = context.getString(R.string.login_error_internal);
        } else if (statCode == JRClientConstants.REG_ERROR_REJECTED) {
            loginState = context.getString(R.string.login_error_rejected);
        } else if (statCode == JRClientConstants.REG_ERROR_OTHER_ERROR) {
            loginState = context.getString(R.string.login_error_other);
        } else if (statCode == JRClientConstants.REG_ERROR_SIP_SESSION_ERROR) {
            loginState = context.getString(R.string.login_error_sip_sess);
        } else if (statCode == JRClientConstants.REG_ERROR_UNREGISTER_ERROR) {
            loginState = context.getString(R.string.login_error_unreg);
        } else if (statCode == JRClientConstants.REG_ERROR_INVALID_IP_ADDR) {
            loginState = context.getString(R.string.login_error_invalid_addr);
        } else if (statCode == JRClientConstants.REG_ERROR_NOT_FOUND_USER) {
            loginState = context.getString(R.string.login_error_not_found);
        } else if (statCode == JRClientConstants.REG_ERROR_AUTHENTICATION_REJECTED) {
            loginState = context.getString(R.string.login_error_auth_reject);
        } else if (statCode == JRClientConstants.REG_ERROR_ID_NOT_MATCH) {
            loginState = context.getString(R.string.login_error_not_match);
        } else if (statCode == JRClientConstants.REG_ERROR_USER_NOT_EXIST) {
            loginState = context.getString(R.string.login_error_user_not_exist);
        }
//        else {
//            loginState = context.getString(R.string.logouted);
//        }
        return loginState;
    }
}
