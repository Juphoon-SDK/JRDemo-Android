package ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juphoon.rcs.jrdemo.R;

import java.util.ArrayList;
import java.util.List;

import common.model.RealmCallLog;
import common.utils.JRDateUtils;

/**
 * Created by Cathy on 15/9/10.
 */
public class LogEntryCardView extends CardView {

    public static final class LogEntry {

        private final String mPhoneNumber;
        private final boolean mIsVideo;
        private String mTime;
        private String mDuration;
        private Drawable mTypeDrawable;
        private int mCallType;
        private boolean mIncoming;
        private boolean mMissed;
        private Context mContext;

        public LogEntry(String phone, boolean isVideo, String time, String duration, Drawable drawable) {
            mPhoneNumber = phone;
            mIsVideo = isVideo;
            mTime = time;
            mDuration = duration;
            mTypeDrawable = drawable;
        }

        public LogEntry(String phone, boolean isVideo, String time, String duration, int type, boolean incoming,boolean missed) {
            mPhoneNumber = phone;
            mIsVideo = isVideo;
            mTime = time;
            mDuration = duration;
            mCallType = type;
            mIncoming = incoming;
            mMissed = missed;
        }

        public void refresh(String time, String duration, Drawable drawable) {
            mTime = time;
            mDuration = duration;
            mTypeDrawable = drawable;
        }

        public String getPhoneNumber() {
            return mPhoneNumber;
        }

        public boolean getCallIsVideo() {
            return mIsVideo;
        }

        String getTime() {
            return mTime;
        }

        String getDuration() {
            return mDuration;
        }

        Drawable getTypeDrawable() {
            return mTypeDrawable;
        }
    }

    private int mContactId;
    private List<LogEntry> mEntries = new ArrayList<LogEntry>();
    private LinearLayout mEntriesViewGroup;
    private ArrayList<RealmCallLog> mArrayListLogId;

    public LogEntryCardView(Context context) {
        this(context, null);
    }

    public LogEntryCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        View logEntryCardView = inflater.inflate(R.layout.log_entry_card_view, this);

        mEntriesViewGroup = ((LinearLayout) logEntryCardView.findViewById(R.id.content_area_linear_layout));
    }

    public void initLogEntry(int contactId, ArrayList<RealmCallLog> arrayListLogId, String phone, Context context) {
        mContactId = contactId;
        mArrayListLogId = arrayListLogId;
        if (mArrayListLogId == null) {
            mArrayListLogId = new ArrayList<>();
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        loadLogEntry(arrayListLogId, phone);

        int entrySize = mEntries.size();
        for (int i = 0; i < entrySize; i++) {
            LogEntry entry = mEntries.get(i);
            mEntriesViewGroup.addView(createEntryView(entry, layoutInflater));
            if (i < entrySize - 1) {
                mEntriesViewGroup.addView(createSeparator());
            }
        }
    }

    private View createSeparator() {
        View separator = new View(getContext());
        Resources res = getResources();

        separator.setBackgroundColor(res.getColor(R.color.login_divider_color));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                res.getDimensionPixelSize(R.dimen.expanding_entry_card_item_separator_height));

        // The separator is aligned with the text in the entry. This is offset by a default
        // margin. If there is an icon present, the icon's width and margin are added
        layoutParams.leftMargin = res.getDimensionPixelSize(R.dimen.expanding_entry_card_item_margin_left);
        separator.setLayoutParams(layoutParams);
        return separator;
    }

    private void loadLogEntry(ArrayList<RealmCallLog> arrayListLogId, String phone) {
        int nLogSize = arrayListLogId.size();

        for (int i = 0; i < nLogSize; i++) {
            RealmCallLog data = arrayListLogId.get(i);
            int callType = data.getCallType();
            String duration;

            if (data.getTalkingTime() == 0) {
                if (data.getCallStatus() == RealmCallLog.CALL_STATE_INCOMING) {
                    duration = "未接听";
                } else {
                    duration = "已取消";
                }
            } else {
                duration = JRDateUtils.getSecondTimestamp(data.getEndTime() - data.getTalkingTime()) + "秒";

            }

            String date = JRDateUtils.formatTimeToYMD(data.getStartTime() / 1000);
            LogEntry logEntry = new LogEntry(phone, data.isVideo(), date, duration, callType, data.isIncoming(),data.isMissed());
            mEntries.add(logEntry);
        }
    }

    private View createEntryView(LogEntry entry, LayoutInflater layoutInflater) {
        final View view = layoutInflater.inflate(R.layout.log_entry_card_item, this, false);

        final TextView time = (TextView) view.findViewById(R.id.log_time);
        time.setText(entry.getTime());

        final TextView duration = (TextView) view.findViewById(R.id.log_duration);
        duration.setText(entry.getDuration());

        final ImageView type = (ImageView) view.findViewById(R.id.log_type);
        boolean incoming = entry.mIncoming;
        int callType = entry.mCallType;
        if (entry.mIsVideo) {
            if (incoming) {
                if (entry.mMissed) {
                    Drawable drawable = getResources().getDrawable(R.drawable.recents_videoin_missed);
                    drawable.setColorFilter(getResources().getColor(R.color.missed), PorterDuff.Mode.SRC_ATOP);
                    type.setImageDrawable(drawable);

                } else {
                    Drawable drawable = getResources().getDrawable(R.drawable.recents_videoin);
                    drawable.setColorFilter(getResources().getColor(R.color.base), PorterDuff.Mode.SRC_ATOP);
                    type.setImageDrawable(drawable);
                }
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.recents_videoout);
                drawable.setColorFilter(getResources().getColor(R.color.base), PorterDuff.Mode.SRC_ATOP);
                type.setImageDrawable(drawable);
            }
        } else {
            if (incoming) {
                if (entry.mMissed) {
                    Drawable drawable = getResources().getDrawable(R.drawable.recents_voicein_missed);
                    drawable.setColorFilter(getResources().getColor(R.color.missed), PorterDuff.Mode.SRC_ATOP);
                    type.setImageDrawable(drawable);
                } else {
                    Drawable drawable = getResources().getDrawable(R.drawable.recents_voicein);
                    drawable.setColorFilter(getResources().getColor(R.color.base), PorterDuff.Mode.SRC_ATOP);
                    type.setImageDrawable(drawable);
                }
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.recents_voiceout);
                drawable.setColorFilter(getResources().getColor(R.color.base), PorterDuff.Mode.SRC_ATOP);
                type.setImageDrawable(drawable);
            }
        }
        view.setTag(entry);
        return view;
    }
}
