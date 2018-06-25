package com.juphoon.rcs.jrdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntryHandler;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.File;

import common.CommonValue;
import common.model.RealmMessage;
import common.utils.JRDateUtils;
import common.utils.JRNumberUtils;
import common.utils.JRVCardUtils;
import io.realm.RealmResults;

/**
 * Created by Upon on 2018/2/27.
 */

public class JRMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_TYPE_UNKNOWN = 0;
    public static final int ITEM_TYPE_TEXT_SEND = 1;
    public static final int ITEM_TYPE_TEXT_RECV = 2;
    public static final int ITEM_TYPE_IMAGE_SEND = 3;
    public static final int ITEM_TYPE_IMAGE_RECV = 4;
    public static final int ITEM_TYPE_GEO_SEND = 5;
    public static final int ITEM_TYPE_GEO_RECV = 6;
    public static final int ITEM_TYPE_FILE_SEND = 7;
    public static final int ITEM_TYPE_FILE_RECV = 8;
    public static final int ITEM_TYPE_AUDIO_SEND = 9;
    public static final int ITEM_TYPE_AUDIO_RECV = 10;
    public static final int ITEM_TYPE_VCARD_SEND = 11;
    public static final int ITEM_TYPE_VCARD_RECV = 12;

    private RealmResults<RealmMessage> mResults;
    private Context mContext;

    public JRMessageAdapter(Context context, RealmResults<RealmMessage> messageResults) {
        mContext = context;
        mResults = messageResults;
    }

    public void setData(RealmResults<RealmMessage> element) {
        mResults = element;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        RealmMessage message = mResults.get(position);
        if (message == null) {
            return ITEM_TYPE_UNKNOWN;
        }
        switch (message.getType()) {
            case CommonValue.MESSAGE_TYPE_LMSG:
            case CommonValue.MESSAGE_TYPE_PMSG:
                if (JRNumberUtils.isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_TEXT_SEND;
                } else {
                    return ITEM_TYPE_TEXT_RECV;
                }
            case CommonValue.MESSAGE_TYPE_IMAGE:
            case CommonValue.MESSAGE_TYPE_VIDEO:
                if (JRNumberUtils.isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_IMAGE_SEND;
                } else {
                    return ITEM_TYPE_IMAGE_RECV;
                }
            case CommonValue.MESSAGE_TYPE_GEO:
                if (JRNumberUtils.isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_GEO_SEND;
                } else {
                    return ITEM_TYPE_GEO_RECV;
                }
            case CommonValue.MESSAGE_TYPE_AUDIO:
                if (JRNumberUtils.isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_AUDIO_SEND;
                } else {
                    return ITEM_TYPE_AUDIO_RECV;
                }
            case CommonValue.MESSAGE_TYPE_OTHER_FILE:
                if (JRNumberUtils.isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_FILE_SEND;
                } else {
                    return ITEM_TYPE_FILE_RECV;
                }
            case CommonValue.MESSAGE_TYPE_VCARD:
                if (JRNumberUtils.isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_VCARD_SEND;
                } else {
                    return ITEM_TYPE_VCARD_RECV;
                }

        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view = null;
        switch (viewType) {
            case ITEM_TYPE_TEXT_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_text_msg_recv, parent, false);
                holder = new TextHolder(view);
                break;
            case ITEM_TYPE_TEXT_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_text_msg_send, parent, false);
                holder = new TextHolder(view);
                break;
            case ITEM_TYPE_IMAGE_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_image_message_recv, parent, false);
                holder = new ImageHolder(view);
                break;
            case ITEM_TYPE_IMAGE_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_image_message_send, parent, false);
                holder = new ImageHolder(view);
                break;
            case ITEM_TYPE_GEO_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_geo_message_recv, parent, false);
                holder = new GeoHolder(view);
                break;
            case ITEM_TYPE_GEO_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_geo_message_send, parent, false);
                holder = new GeoHolder(view);
                break;
            case ITEM_TYPE_AUDIO_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_audio_message_recv, parent, false);
                holder = new AudioHolder(view);
                break;
            case ITEM_TYPE_AUDIO_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_audio_message_send, parent, false);
                holder = new AudioHolder(view);
                break;
            case ITEM_TYPE_FILE_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_file_message_recv, parent, false);
                holder = new FileHolder(view);
                break;
            case ITEM_TYPE_FILE_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_file_message_send, parent, false);
                holder = new FileHolder(view);
                break;
            case ITEM_TYPE_VCARD_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_vcard_message_recv, parent, false);
                holder = new VCardHolder(view);
                break;
            case ITEM_TYPE_VCARD_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_vcard_message_send, parent, false);
                holder = new VCardHolder(view);
                break;
            default:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_text_msg_send, parent, false);
                holder = new TextHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final RealmMessage message = mResults.get(position);
        if (holder instanceof TextHolder) {
            ((TextHolder) holder).content.setText(message.getContent() + "");
            ((TextHolder) holder).name.setText(message.getSenderPhone() + "");
            ((TextHolder) holder).date.setText(JRDateUtils.getTimeStringX(mContext, message.getTimeStamp() * 1000, false));
        } else if (holder instanceof ImageHolder) {
            Glide.with(mContext)
                    .load(message.getFileThumbPath())
                    .centerCrop()
                    .into(((ImageHolder) holder).imageView);
            if (message.getType() == CommonValue.MESSAGE_TYPE_VIDEO) {
                ((ImageHolder) holder).videoPlay.setVisibility(View.VISIBLE);
                ((ImageHolder) holder).duration.setVisibility(View.VISIBLE);
                ((ImageHolder) holder).duration.setText(message.getFileDuration() / 1000 + "''");
            } else {
                ((ImageHolder) holder).videoPlay.setVisibility(View.GONE);
                ((ImageHolder) holder).duration.setVisibility(View.GONE);
            }
            ((ImageHolder) holder).name.setText(message.getSenderPhone() + "");
            ((ImageHolder) holder).date.setText(JRDateUtils.getTimeStringX(mContext, message.getTimeStamp() * 1000, false));
            if (message.getState() == CommonValue.MESSAGE_STATUS_SENDING || message.getState() == CommonValue.MESSAGE_STATUS_RECVING) {
                ((ImageHolder) holder).progress.setVisibility(View.VISIBLE);
                if (message.getFileSize() != 0) {
                    ((ImageHolder) holder).progress.setText(message.getFileTransSize() * 100 / message.getFileSize() + "%");
                }
            } else {
                ((ImageHolder) holder).progress.setVisibility(View.GONE);
            }
        } else if (holder instanceof GeoHolder) {
            ((GeoHolder) holder).name.setText(message.getSenderPhone() + "");
            ((GeoHolder) holder).date.setText(JRDateUtils.getTimeStringX(mContext, message.getTimeStamp() * 1000, false));
            ((GeoHolder) holder).label.setText(message.getLabel() + "");
            ((GeoHolder) holder).map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String lacation = new JSONStringer().object().key(LocationBaiduActivity.LOCATION_LATITUDE)
                                .value(message.getLatitude()).key(LocationBaiduActivity.LOCATION_LONGITUDE).value(message.getLongitude())
                                .key(LocationBaiduActivity.LOCATION_RADIUS).value(message.getRadius()).endObject().toString();
                        Intent intent = new Intent(mContext, LocationBaiduActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(LocationBaiduActivity.LOCATION_JSONSTRING, lacation);
                        intent.putExtra(LocationBaiduActivity.LOCATION_NAME, message.getLabel());
                        mContext.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (holder instanceof AudioHolder) {
            ((AudioHolder) holder).duration.setText(message.getFileDuration() / 1000 + "''");
            ((AudioHolder) holder).name.setText(message.getSenderPhone() + "");
            ((AudioHolder) holder).date.setText(JRDateUtils.getTimeStringX(mContext, message.getTimeStamp() * 1000, false));
        } else if (holder instanceof FileHolder) {
            ((FileHolder) holder).date.setText(JRDateUtils.getTimeStringX(mContext, message.getTimeStamp() * 1000, false));
            ((FileHolder) holder).name.setText(message.getSenderPhone() + "");
            String[] s = message.getFilePath().split("/");
            String fileName = s[s.length - 1];
            ((FileHolder) holder).label.setText(fileName);
        } else if (holder instanceof VCardHolder) {
            ((VCardHolder) holder).name.setText(message.getSenderPhone() + "");
            ((VCardHolder) holder).date.setText(JRDateUtils.getTimeStringX(mContext, message.getTimeStamp() * 1000, false));
            Uri uri = Uri.fromFile(new File(message.getFilePath()));
            JRVCardUtils.parseVCard(mContext, uri, new VCardEntryHandler() {
                @Override
                public void onStart() {

                }

                @Override
                public void onEntryCreated(VCardEntry vCardEntry) {
                    String name = vCardEntry.getNameData() != null ? vCardEntry.getNameData().displayName : "";
                    String phone = vCardEntry.getPhoneList() != null
                            && vCardEntry.getPhoneList().size() > 0 ? vCardEntry.getPhoneList().get(0).getNumber() : "";
                    ((VCardHolder) holder).cardName.setText(name);
                    ((VCardHolder) holder).cardName.setVisibility(TextUtils.isEmpty(name) ? View.GONE : View.VISIBLE);
                    ((VCardHolder) holder).number.setText(phone);
                    ((VCardHolder) holder).number.setVisibility(TextUtils.isEmpty(phone) ? View.GONE : View.VISIBLE);
                }

                @Override
                public void onEnd() {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    private class TextHolder extends RecyclerView.ViewHolder {
        TextView content, name, date;
        ImageView icon;

        public TextHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
            name = itemView.findViewById(R.id.display_name);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
        }
    }

    private class ImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView, videoPlay, icon;
        TextView name, date, progress, duration;

        public ImageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            videoPlay = itemView.findViewById(R.id.video_play);
            name = itemView.findViewById(R.id.display_name);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            progress = itemView.findViewById(R.id.progress);
            duration = itemView.findViewById(R.id.duration);
        }
    }

    private class GeoHolder extends RecyclerView.ViewHolder {
        TextView name, date, label;
        ImageView icon, map;

        public GeoHolder(View itemView) {
            super(itemView);
            map = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.display_name);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            label = itemView.findViewById(R.id.label);
        }
    }

    private class VCardHolder extends RecyclerView.ViewHolder {
        TextView name, date, label, cardName, number;
        ImageView icon;

        public VCardHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.display_name);
            cardName = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            label = itemView.findViewById(R.id.label);
        }
    }

    private class AudioHolder extends RecyclerView.ViewHolder {
        TextView name, date, duration;
        ImageView icon, play;
        LinearLayout voiceLayout;

        public AudioHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.display_name);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            play = itemView.findViewById(R.id.paly_voice);
            duration = itemView.findViewById(R.id.duration);
            voiceLayout = itemView.findViewById(R.id.voice_layout);
        }
    }

    private class FileHolder extends RecyclerView.ViewHolder {
        TextView name, date, label;
        ImageView icon;
        LinearLayout fileLayout;

        public FileHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.display_name);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            label = itemView.findViewById(R.id.file_name);
            fileLayout = itemView.findViewById(R.id.file_layout);
        }
    }
}
