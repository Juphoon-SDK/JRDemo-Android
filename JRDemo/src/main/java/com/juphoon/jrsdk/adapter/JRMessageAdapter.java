package com.juphoon.jrsdk.adapter;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntryHandler;
import com.bumptech.glide.Glide;
import com.juphoon.jrsdk.LocationBaiduActivity;
import com.juphoon.jrsdk.R;
import com.juphoon.jrsdk.model.RealmMessage;
import com.juphoon.jrsdk.utils.CommonValue;
import com.juphoon.jrsdk.utils.DateUtils;
import com.juphoon.jrsdk.utils.NumberUtils;
import com.juphoon.jrsdk.utils.VCardUtils;
import com.juphoon.rcs.jrsdk.JRClient;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.File;

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
    public static final int ITEM_TYPE_SYSTEM = 13;

    private RealmResults<RealmMessage> mResults;
    private Context mContext;
    private String mSelfNumber;

    public JRMessageAdapter(Context context, RealmResults<RealmMessage> messageResults) {
        mContext = context;
        mResults = messageResults;
        mSelfNumber = NumberUtils.formatPhoneByCountryCode(JRClient.getInstance().getCurrentNumber());

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
        if (message.isRevoked()) {
            return ITEM_TYPE_SYSTEM;
        }
        switch (message.getType()) {
            case CommonValue.MESSAGE_TYPE_TEXT:
                if (isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_TEXT_SEND;
                } else {
                    return ITEM_TYPE_TEXT_RECV;
                }
            case CommonValue.MESSAGE_TYPE_IMAGE:
            case CommonValue.MESSAGE_TYPE_VIDEO:
                if (isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_IMAGE_SEND;
                } else {
                    return ITEM_TYPE_IMAGE_RECV;
                }
            case CommonValue.MESSAGE_TYPE_GEO:
                if (isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_GEO_SEND;
                } else {
                    return ITEM_TYPE_GEO_RECV;
                }
            case CommonValue.MESSAGE_TYPE_AUDIO:
                if (isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_AUDIO_SEND;
                } else {
                    return ITEM_TYPE_AUDIO_RECV;
                }
            case CommonValue.MESSAGE_TYPE_OTHER_FILE:
                if (isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_FILE_SEND;
                } else {
                    return ITEM_TYPE_FILE_RECV;
                }
            case CommonValue.MESSAGE_TYPE_VCARD:
                if (isSelf(message.getSenderPhone())) {
                    return ITEM_TYPE_VCARD_SEND;
                } else {
                    return ITEM_TYPE_VCARD_RECV;
                }
            case CommonValue.MESSAGE_TYPE_SYSTEM:
                return ITEM_TYPE_SYSTEM;

        }
        return super.getItemViewType(position);
    }

    private boolean isSelf(String senderPhone) {
        return TextUtils.equals(senderPhone, mSelfNumber);
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
            case ITEM_TYPE_SYSTEM:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_system, parent, false);
                holder = new SystemHolder(view);
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
            ((TextHolder) holder).date.setText(DateUtils.getTimeStringX(mContext, message.getTimeStamp(), false));
            if (message.isBurnAfterReading()) {
                ((TextHolder) holder).content.setVisibility(View.INVISIBLE);
                ((TextHolder) holder).burnView.setVisibility(View.VISIBLE);
            } else {
                ((TextHolder) holder).content.setVisibility(View.VISIBLE);
                ((TextHolder) holder).burnView.setVisibility(View.GONE);
            }
        } else if (holder instanceof ImageHolder) {
            Glide.with(mContext)
                    .load(message.getFileThumbPath())
                    .centerCrop()
                    .into(((ImageHolder) holder).imageView);
            if (message.getType() == CommonValue.MESSAGE_TYPE_VIDEO) {
                ((ImageHolder) holder).videoPlay.setVisibility(View.VISIBLE);
                ((ImageHolder) holder).duration.setVisibility(View.VISIBLE);
                ((ImageHolder) holder).duration.setText(message.getFileDuration() + "''");
            } else {
                ((ImageHolder) holder).videoPlay.setVisibility(View.GONE);
                ((ImageHolder) holder).duration.setVisibility(View.GONE);
            }
            ((ImageHolder) holder).name.setText(message.getSenderPhone() + "");
            ((ImageHolder) holder).date.setText(DateUtils.getTimeStringX(mContext, message.getTimeStamp(), false));
            if (message.getState() == CommonValue.MESSAGE_STATUS_SENDING || message.getState() == CommonValue.MESSAGE_STATUS_RECVING) {
                ((ImageHolder) holder).progress.setVisibility(View.VISIBLE);
                if (message.getFileSize() != 0) {
                    ((ImageHolder) holder).progress.setText(message.getFileTransSize() * 100 / message.getFileSize() + "%");
                }
            } else {
                ((ImageHolder) holder).progress.setVisibility(View.GONE);
            }

            if (message.isBurnAfterReading()) {
                ((ImageHolder) holder).videoLayout.setVisibility(View.INVISIBLE);
                ((ImageHolder) holder).burnView.setVisibility(View.VISIBLE);
            } else {
                ((ImageHolder) holder).videoLayout.setVisibility(View.VISIBLE);
                ((ImageHolder) holder).burnView.setVisibility(View.GONE);
            }
        } else if (holder instanceof GeoHolder) {
            ((GeoHolder) holder).name.setText(message.getSenderPhone() + "");
            ((GeoHolder) holder).date.setText(DateUtils.getTimeStringX(mContext, message.getTimeStamp(), false));
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
            ((AudioHolder) holder).duration.setText(message.getFileDuration() + "''");
            ((AudioHolder) holder).name.setText(message.getSenderPhone() + "");
            ((AudioHolder) holder).date.setText(DateUtils.getTimeStringX(mContext, message.getTimeStamp(), false));
            if (message.isBurnAfterReading()) {
                ((AudioHolder) holder).voiceLayout.setVisibility(View.INVISIBLE);
                ((AudioHolder) holder).burnView.setVisibility(View.VISIBLE);
            } else {
                ((AudioHolder) holder).voiceLayout.setVisibility(View.VISIBLE);
                ((AudioHolder) holder).burnView.setVisibility(View.GONE);
            }
        } else if (holder instanceof FileHolder) {
            ((FileHolder) holder).date.setText(DateUtils.getTimeStringX(mContext, message.getTimeStamp(), false));
            ((FileHolder) holder).name.setText(message.getSenderPhone() + "");
            String[] s = message.getFilePath().split("/");
            String fileName = s[s.length - 1];
            ((FileHolder) holder).label.setText(fileName);
        } else if (holder instanceof VCardHolder) {
            ((VCardHolder) holder).name.setText(message.getSenderPhone() + "");
            ((VCardHolder) holder).date.setText(DateUtils.getTimeStringX(mContext, message.getTimeStamp(), false));
            Uri uri = Uri.fromFile(new File(message.getFilePath()));
            VCardUtils.parseVCard(mContext, uri, new VCardEntryHandler() {
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
        } else if (holder instanceof SystemHolder) {
            ((SystemHolder) holder).content.setText(message.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    private class TextHolder extends RecyclerView.ViewHolder {
        TextView content, name, date;
        ImageView icon, burnView;

        public TextHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
            name = itemView.findViewById(R.id.display_name);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            burnView = itemView.findViewById(R.id.burn_view);
        }
    }

    private class ImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView, videoPlay, icon, burnView;
        TextView name, date, progress, duration;
        RelativeLayout videoLayout;

        public ImageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            videoPlay = itemView.findViewById(R.id.video_play);
            name = itemView.findViewById(R.id.display_name);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            progress = itemView.findViewById(R.id.progress);
            duration = itemView.findViewById(R.id.duration);
            burnView = itemView.findViewById(R.id.burn_view);
            videoLayout = itemView.findViewById(R.id.video_layout);
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
        ImageView icon, burnView;

        public VCardHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.display_name);
            cardName = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            label = itemView.findViewById(R.id.label);
            burnView = itemView.findViewById(R.id.burn_view);
        }
    }

    private class AudioHolder extends RecyclerView.ViewHolder {
        TextView name, date, duration;
        ImageView icon, play, burnView;
        LinearLayout voiceLayout;

        public AudioHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.display_name);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            play = itemView.findViewById(R.id.paly_voice);
            duration = itemView.findViewById(R.id.duration);
            voiceLayout = itemView.findViewById(R.id.voice_layout);
            burnView = itemView.findViewById(R.id.burn_view);
        }
    }

    private class FileHolder extends RecyclerView.ViewHolder {
        TextView name, date, label;
        ImageView icon, burnView;
        LinearLayout fileLayout;

        public FileHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.display_name);
            date = itemView.findViewById(R.id.date);
            icon = itemView.findViewById(R.id.user_icon);
            label = itemView.findViewById(R.id.file_name);
            fileLayout = itemView.findViewById(R.id.file_layout);
            burnView = itemView.findViewById(R.id.burn_view);
        }
    }

    private class SystemHolder extends RecyclerView.ViewHolder {
        TextView content;

        public SystemHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.text);
        }
    }
}
