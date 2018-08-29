package com.juphoon.jrsdk.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.android.vcard.VCardComposer;
import com.android.vcard.VCardConfig;
import com.android.vcard.VCardEntryCommitter;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardEntryHandler;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardNestedException;
import com.android.vcard.exception.VCardNotSupportedException;
import com.android.vcard.exception.VCardVersionException;
import com.juphoon.cmcc.app.lemon.MtcCommonConstants;
import com.juphoon.rcs.jrsdk.JRClient;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Upon on 2018/3/19.
 */

public class VCardUtils {
    public static boolean exportVCard(Context context, Uri uri,
                                      List<Integer> listId) {

        if (uri == null)
            return false;
        final OutputStream outputStream;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
        } catch (FileNotFoundException e) {
            return false;
        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        VCardComposer composer = new VCardComposer(context, VCardConfig.VCARD_TYPE_V30_GENERIC, true);
        String args = "";
        if (listId != null) {
            args += "(";
            for (Integer id : listId) {
                if (args.length() > 1)
                    args += ",";
                args += "\"";
                args += id;
                args += "\"";
            }
            args += ")";
            composer.init(ContactsContract.Contacts._ID + " in " + args, null);
        } else {
            composer.init(ContactsContract.Contacts._ID, null);
        }

        final int total = composer.getCount();
        if (total == 0) {
            return false;
        }

        while (!composer.isAfterLast()) {
            try {
                writer.write(composer.createOneEntry());
            } catch (IOException e) {
                return false;
            }
        }
        if (composer != null) {
            composer.terminate();
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public static void parseVCard(InputStream inputStream, VCardEntryHandler handler) {
        if (inputStream != null) {
            try {
                VCardEntryConstructor constructor = new VCardEntryConstructor(
                        VCardConfig.VCARD_TYPE_V30_GENERIC);
                VCardParser parse = new VCardParser_V30(
                        VCardConfig.VCARD_TYPE_V30_GENERIC);
                constructor.addEntryHandler(handler);
                parse.addInterpreter(constructor);
                parse.parse(inputStream);
            } catch (Exception e) {

            } finally {

            }
        }
    }

    public static void parseVCard(String vcardContent, VCardEntryHandler handler) {
        InputStream is = new ByteArrayInputStream(vcardContent.getBytes());
        parseVCard(is, handler);
    }

    public static void parseVCard(Context context, Uri uri, VCardEntryHandler handler) {
        InputStream is = null;
        try {
            if (uri != null) {
                is = context.getContentResolver().openInputStream(uri);
                parseVCard(is, handler);
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static int importToContact(Context context, Uri uri) {
        InputStream is = null;
        try {
            if (uri != null) {
                is = context.getContentResolver().openInputStream(uri);
            }
            return importToContact(context, is);
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        return -1;
    }

    public static int importToContact(Context context, InputStream inputStream) {
        if (inputStream != null) {
            try {
                VCardEntryConstructor constructor = new VCardEntryConstructor(
                        VCardConfig.VCARD_TYPE_V30_GENERIC);
                VCardEntryCommitter mCardEntryCommitter = new VCardEntryCommitter(context
                        .getContentResolver());
                VCardParser parse = new VCardParser_V30(
                        VCardConfig.VCARD_TYPE_V30_GENERIC);
                constructor.addEntryHandler(mCardEntryCommitter);
                parse.addInterpreter(constructor);
                parse.parse(inputStream);
                List<Uri> createdUris = mCardEntryCommitter.getCreatedUris();

                if (createdUris == null) {
                    return -1;
                }
                Iterator<Uri> iterator = createdUris.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next() == null) {
                        iterator.remove();
                    }
                }
                if (createdUris.size() == 0) {
                    return -1;
                }
                return (int) ContentUris.parseId(createdUris.get(0));
            } catch (SecurityException e) {
            } catch (IOException e) {
            } catch (VCardNestedException e) {
            } catch (VCardNotSupportedException e) {
            } catch (VCardVersionException e) {
            } catch (VCardException e) {
            } finally {
            }
        }
        return -1;
    }

    public static long getContactIdWithRawContactId(Context context,
                                                    long rawContactId) {
        ContentResolver resolver = context.getContentResolver();
        long contactsId = MtcCommonConstants.INVALIDID;

        Cursor c = resolver.query(ContactsContract.RawContacts.CONTENT_URI, null,
                ContactsContract.RawContacts._ID + " = " + rawContactId, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                contactsId = c.getLong(c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
            }
            c.close();
        }
        return contactsId;
    }

    public static String createSelfVCardContent(String groupSubject,String displayName) {
        String company = "", email="";

//        RcsProfileDbHelper helper = RcsProfileDbHelper.getInstance();
//        RcsProfileBean profileBean = helper.getProfile();
//        List<RcsProfileName> names = profileBean.getProfileNames();
//        if (names != null && names.size() > 0) {
//            RcsProfileName name = names.get(0);
//            firtName = name.getFirstName();
//            familyName = name.getFamilyName();
//        }
//        String phone = JRClient.getInstance().getCurrentNumber();
//        RcsProfileCareer career = profileBean.getProfileCareer();
//        if (career != null) {
//            company = career.getCompany();
//        }
//        List<RcsProfileUri> uris = profileBean.getProfileUris();
//        for (RcsProfileUri uri:uris) {
//            if (uri.getLabel().equals("Email")) {
//                email = uri.getValue();
//                break;
//            }
//        }
            return createVCardContent(displayName, JRClient.getInstance().getCurrentNumber(), company, email, groupSubject);
    }

    public static String createVCardContent(String displayName, String phoneNumber,
                                            String company, String email, String groupSubject) {
        StringBuffer sb = new StringBuffer();
        sb.append("BEGIN:VCARD\n");
        sb.append("VERSION:3.0\n");
        sb.append(String.format("N:%s\n", displayName));
        sb.append(String.format("FN:%s\n",displayName));
        sb.append(String.format("TEL;TYPE=CELL:%s\n", phoneNumber));
        if (!TextUtils.isEmpty(company)) {
            sb.append(String.format("ORG:%s\n", company));
        }
        if (!TextUtils.isEmpty(email)) {
            sb.append(String.format("EMAIL;type=INTERNET;type=pref:%s\n", email));
        }
        if (!TextUtils.isEmpty(groupSubject)) {
            sb.append(String.format("NOTE:Hi,我是%s，来自咱们共同的群”%s”，希望与您交换名片。\n", displayName, groupSubject));
        }
        sb.append("END:VCARD\n");
        return sb.toString();
    }
}
