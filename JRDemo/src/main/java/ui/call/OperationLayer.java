package ui.call;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;

import com.juphoon.rcs.JRCallConstants;
import com.juphoon.rcs.JRCallItem;
import com.juphoon.rcs.JRLog;
import com.juphoon.rcs.jrdemo.R;
import com.juphoon.rcs.utils.JRCommonUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ui.view.CallIncomingSlideView;
import ui.view.CircleButton;
import ui.view.CirclePageIndicator;
import ui.view.Rotatable;
import ui.view.TwoStateScrollViewPager;

public class OperationLayer extends Layer implements View.OnClickListener, CallIncomingSlideView.Callback, CallCell.Callback {
    public static final String EVENT_END = "end";
    public static final String EVENT_MUTE = "mute";
    public static final String EVENT_SPEAKER = "speaker";
    public static final String EVENT_AUDIO_TO_VIDEO = "audio_to_video";
    public static final String EVENT_VIDEO_TO_AUDIO = "video_to_audio";
    public static final String EVENT_ADD_CALL = "add_call";
    public static final String EVENT_HOLD_CALL = "hold_call";
    public static final String EVENT_MERGE_CALL = "merge_call";
    public static final String EVENT_SWITCH_FRONT_REAR = "switch_front_rear";
    public static final String EVENT_REDIAL = "redial";
    public static final String EVENT_PAGE_CLICKED = "page_clicked";
    public static final String EVENT_SHOWN = "shown";
    public static final String EVENT_HIDDEN = "hidden";
    public static final String EVENT_ANSWER_DEFAULT = "answer_default";
    public static final String EVENT_ANSWER_CAMERA_OFF = "answer_camera_off";
    public static final String EVENT_ANSWER_DECLINE = "answer_decline";
    public static final String EVENT_STATISTIC = "statistic";
    public static final String EVENT_DOODLE = "doodle";
    public static final String EVENT_SEND_FILE = "send_file";

    @Override
    public void callCellDidEndConference(CallCell callCell) {

    }

    @Override
    public void callCellNeedsReloadData(CallCell callCell) {

    }

    @Override
    public void callCellStateChanged(CallCell callCell) {

    }

    public interface EventReceiver {
        void onEvent(String event, String... args);
    }

    private EventReceiver mEventReceiver;

    private final Context mContext;
    private ViewGroup mParentView;
    private View mRootView;

    private UIElementsContainer mUIContainer;

    private MyHandler mHandler;

    public OperationLayer(Context context) {
        mContext = context;
        mUIContainer = new UIElementsContainer();
        mHandler = new MyHandler(this);
    }

    public void setParentView(ViewGroup parentView, boolean shouldPageClick) {
        mParentView = parentView;
        initViews(shouldPageClick);
    }

    public void setEventReceiver(EventReceiver listener) {
        mEventReceiver = listener;
    }

    private void initViews(final boolean shouldPageClick) {
        mRootView = findViewById(R.id.operation_layer);
        if (shouldPageClick) {
            mUIContainer.initViews(mContext, mParentView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendEvent(EVENT_PAGE_CLICKED);
                }
            });
        } else {
            mUIContainer.initViews(mContext, mParentView, null);
        }
        mUIContainer.setPageEnabledInternal(false);
        colorCircleButtons();
        initClickListener();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            int navigationBarHeight = MtcUtils.getNavigationBarHeight((Activity) mContext);
            mRootView.setPadding(0, 0, 0, 0);
        }
    }

    private void initClickListener() {
        mUIContainer.mCircleButtonEnd.setOnClickListener(this);
        mUIContainer.mCircleButtonCancel.setOnClickListener(this);
        mUIContainer.mCircleButtonMute.setOnClickListener(this);
        mUIContainer.mCircleButtonSpeaker.setOnClickListener(this);
        mUIContainer.mCircleButtonCameraOff.setOnClickListener(this);
        mUIContainer.mCircleButtonMultiAdd.setOnClickListener(this);
        mUIContainer.mCircleButtonSwitchFrontRear.setOnClickListener(this);
        mUIContainer.mCircleButtonCameraOn.setOnClickListener(this);
        mUIContainer.mCircleButtonRedial.setOnClickListener(this);
        mUIContainer.mCircleButtonHoldCall.setOnClickListener(this);
        mUIContainer.mCircleButtonAddCall.setOnClickListener(this);
        mUIContainer.mCircleButtonMergeCall.setOnClickListener(this);

        mUIContainer.mViewStatistic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.call_button_end) {
            sendEvent(EVENT_END);
        } else if (id == R.id.call_button_cancel) {
            sendEvent(EVENT_END);
        } else if (id == R.id.call_button_mute) {
            mUIContainer.mCircleButtonMute.setSelected(!mUIContainer.mCircleButtonMute.isSelected());
            sendEvent(EVENT_MUTE);
        } else if (id == R.id.call_button_speaker) {
            sendEvent(EVENT_SPEAKER);
        } else if (id == R.id.call_button_camera_off) {
            JRCommonUtils.setViewEnabled(mUIContainer.mComboButtonCameraOn, true);
            sendEvent(EVENT_VIDEO_TO_AUDIO);
        } else if (id == R.id.call_button_camera_on) {
            sendEvent(EVENT_AUDIO_TO_VIDEO);
        } else if (id == R.id.call_button_add_call) {
            delayShowFirstPage();
            sendEvent(EVENT_ADD_CALL);
        } else if (id == R.id.call_button_hold_call) {
            delayShowFirstPage();
            sendEvent(EVENT_HOLD_CALL);
        } else if (id == R.id.call_button_merge_call) {
            delayShowFirstPage();
            sendEvent(EVENT_MERGE_CALL);
        } else if (id == R.id.call_button_switch_front_rear) {
            sendEvent(EVENT_SWITCH_FRONT_REAR);
        } else if (id == R.id.call_button_redial) {
            sendEvent(EVENT_REDIAL);
        } else if (id == R.id.call_statistic) {
            sendEvent(EVENT_STATISTIC);
        } else if (id == R.id.call_button_add_multi_member) {
            sendEvent(EVENT_ADD_CALL);
        }
    }

    private void delayShowFirstPage() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_FIRST_PAGE, 500);
    }

    private void colorCircleButtons() {
        colorNormalCircleButton(mUIContainer.mCircleButtonMute, R.drawable.call_mute_state);
        colorNormalCircleButton(mUIContainer.mCircleButtonSpeaker, R.drawable.call_speaker_state);
        colorNormalCircleButton(mUIContainer.mCircleButtonCameraOff, R.drawable.call_camera_off_state);
        colorNormalCircleButton(mUIContainer.mCircleButtonSwitchFrontRear, R.drawable.call_switch_state);
        colorNormalCircleButton(mUIContainer.mCircleButtonCancel, R.drawable.call_cancel_state);
        colorEndCircleButton(mUIContainer.mCircleButtonEnd, R.drawable.call_end_state);
        colorRedialCircleButton(mUIContainer.mCircleButtonRedial, R.drawable.call_redial_voice_state);
        colorNormalCircleButton(mUIContainer.mCircleButtonCameraOn, R.drawable.call_camera_on_state);
        colorNormalCircleButton(mUIContainer.mCircleButtonHoldCall, R.drawable.call_hold_state);
        colorNormalCircleButton(mUIContainer.mCircleButtonAddCall, R.drawable.call_add_state);
        colorNormalCircleButton(mUIContainer.mCircleButtonMultiAdd, R.drawable.call_add_state);
        colorNormalCircleButton(mUIContainer.mCircleButtonMergeCall, R.drawable.call_merge_state);
    }

    public boolean isVisible() {
        return mRootView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void show() {
        mRootView.setVisibility(View.VISIBLE);
        sendEvent(EVENT_SHOWN);
    }

    @Override
    public void hide() {
        mRootView.setVisibility(View.INVISIBLE);
        setPage(0);
        sendEvent(EVENT_HIDDEN);
    }

    @Override
    public boolean visible() {
        return mRootView.getVisibility() == View.VISIBLE;
    }

    public void onActivityResume() {
        mHandler.removeMessages(MESSAGE_SHOW_FIRST_PAGE);
    }

    public void setVideo(boolean isVideo) {
        mUIContainer.mComboButtonMute.setVisibility(View.VISIBLE);
        mUIContainer.mComboButtonSpeaker.setVisibility(View.VISIBLE);
        mUIContainer.mCircleButtonEnd.setVisibility(View.VISIBLE);
        mUIContainer.mComboButtonCancel.setVisibility(View.GONE);
        mUIContainer.mComboButtonRedial.setVisibility(View.GONE);
        if (isVideo) {
            mUIContainer.setPageEnabledInternal(false);
            mUIContainer.mComboButtonCameraOn.setVisibility(View.GONE);
            mUIContainer.mComboButtonHoldCall.setVisibility(View.GONE);
//            mUIContainer.mComboButtonAddCall.setVisibility(View.GONE);
            mUIContainer.mComboButtonMergeCall.setVisibility(View.GONE);

            mUIContainer.mComboButtonCameraOff.setVisibility(View.VISIBLE);
            mUIContainer.mComboButtonSwitchFrontRear.setVisibility(View.VISIBLE);
        } else {
            mUIContainer.setPageEnabledInternal(true);
            mUIContainer.mComboButtonCameraOn.setVisibility(View.VISIBLE);
            mUIContainer.mComboButtonHoldCall.setVisibility(View.VISIBLE);
//            mUIContainer.mComboButtonAddCall.setVisibility(View.VISIBLE);
            mUIContainer.mComboButtonMergeCall.setVisibility(View.GONE);

            mUIContainer.mComboButtonCameraOff.setVisibility(View.GONE);
            mUIContainer.mComboButtonSwitchFrontRear.setVisibility(View.GONE);
        }
    }

    public void setLayoutState(JRCallItem item) {
        JRCallConstants.State state = item.getCallState();
        boolean isVideo = item.isVideo();
        setVideo(isVideo);

        if (item.isConference()) {
            setOneCallViewsGone();
            if (item.isVideo()) {
                if (item.getCallDirection() == JRCallConstants.Direction.OUTGOING) {
                    mUIContainer.mComboButtonMultiAdd.setVisibility(View.VISIBLE);
                } else {
                    mUIContainer.mComboButtonMultiAdd.setVisibility(View.GONE);
                }
                mUIContainer.mComboButtonAddCall.setVisibility(View.GONE);
            } else {
                mUIContainer.mComboButtonMultiAdd.setVisibility(View.GONE);
                mUIContainer.mComboButtonAddCall.setVisibility(View.VISIBLE);
            }
        } else {
            mUIContainer.mComboButtonAddCall.setVisibility(View.GONE);
        }
        if (state == JRCallConstants.State.ALERTED) {

        } else if (state == JRCallConstants.State.CONNECTING) {

        } else if (state == JRCallConstants.State.INCOMING) {
            if (!isVideo) {
                mUIContainer.mCircleButtonCameraOn.setEnabled(false);
                mUIContainer.mCircleButtonHoldCall.setEnabled(false);
                mUIContainer.mCircleButtonAddCall.setEnabled(false);
                mUIContainer.mCircleButtonMergeCall.setEnabled(false);
            } else {
                mUIContainer.mCircleButtonCameraOff.setEnabled(false);
                mUIContainer.mCircleButtonSwitchFrontRear.setEnabled(false);
            }
        } else if (state == JRCallConstants.State.OUTGOING) {
            mUIContainer.mCircleButtonHoldCall.setEnabled(false);
            mUIContainer.mCircleButtonAddCall.setEnabled(false);
            mUIContainer.mCircleButtonMergeCall.setEnabled(false);
            if (!isVideo) {
                mUIContainer.mCircleButtonCameraOn.setEnabled(false);
            } else {
                mUIContainer.mCircleButtonCameraOff.setEnabled(false);
                mUIContainer.mCircleButtonSwitchFrontRear.setEnabled(false);
            }
        } else if (state == JRCallConstants.State.ENDED) {

        } else if (state == JRCallConstants.State.TALKING) {
            if (!isVideo) {
                JRLog.log("yidao", "ssss");
                mUIContainer.mCircleButtonCameraOn.setEnabled(true);
                mUIContainer.mCircleButtonHoldCall.setEnabled(true);
                mUIContainer.mCircleButtonAddCall.setEnabled(true);
                mUIContainer.mCircleButtonMergeCall.setEnabled(true);
            } else {
                mUIContainer.mCircleButtonCameraOff.setEnabled(true);
                mUIContainer.mCircleButtonSwitchFrontRear.setEnabled(true);
            }
        }
        if (item.isHeld()) {
            if (!isVideo) {
                mUIContainer.mCircleButtonAddCall.setEnabled(false);
                mUIContainer.mCircleButtonCameraOn.setEnabled(false);
            }
        }
        if (item.isHold()) {
            if (!isVideo) {
                mUIContainer.mCircleButtonCameraOn.setEnabled(false);
            }
        }
    }

//    public void setConfLayoutState(int state, boolean isIncoming) {
//        setVideo(false);
//        setOneCallViewsGone();
//        switch (state) {
//            case RcsCallSession.State.INITIATED:
//            case RcsCallSession.State.CONNECTING:
//                if (isIncoming) {
//                    setConfInComingViewsGone();
//                    mUIContainer.mCircleButtonHoldCall.setEnabled(false);
//                    mUIContainer.mCircleButtonAddCall.setEnabled(false);
//                    mUIContainer.mCircleButtonMergeCall.setEnabled(false);
//                } else {
//                    mUIContainer.mCircleButtonHoldCall.setEnabled(false);
//                    mUIContainer.mCircleButtonAddCall.setEnabled(false);
//                    mUIContainer.mCircleButtonMergeCall.setEnabled(false);
//                }
//                break;
//            case RcsCallSession.State.TALKING:
//                if (isIncoming) {
//                    setConfInComingViewsGone();
//                    mUIContainer.mCircleButtonHoldCall.setEnabled(true);
//                    mUIContainer.mCircleButtonAddCall.setEnabled(true);
//                    mUIContainer.mCircleButtonMergeCall.setEnabled(true);
//                } else {
//                    mUIContainer.mCircleButtonHoldCall.setEnabled(true);
//                    mUIContainer.mCircleButtonAddCall.setEnabled(true);
//                    mUIContainer.mCircleButtonMergeCall.setEnabled(true);
//                }
//                break;
//            case RcsCallSession.State.TERMINATING:
//            case RcsCallSession.State.TERMINATED:
//                mUIContainer.mComboButtonMute.setVisibility(View.GONE);
//                mUIContainer.mComboButtonSpeaker.setVisibility(View.GONE);
//                mUIContainer.mCircleButtonEnd.setVisibility(View.GONE);
//                mUIContainer.mComboButtonCameraOn.setVisibility(View.GONE);
//                mUIContainer.mComboButtonCameraOff.setVisibility(View.GONE);
//                mUIContainer.mComboButtonSwitchFrontRear.setVisibility(View.GONE);
//                mUIContainer.mComboButtonHoldCall.setVisibility(View.GONE);
//                mUIContainer.mComboButtonAddCall.setVisibility(View.GONE);
//                mUIContainer.mComboButtonMergeCall.setVisibility(View.GONE);
//                mUIContainer.mComboButtonCancel.setVisibility(View.VISIBLE);
//                mUIContainer.setPageEnabledInternal(false);
//                break;
//        }
//    }

    public void setOneCallViewsGone() {
        mUIContainer.mComboButtonCameraOn.setVisibility(View.GONE);
        mUIContainer.mComboButtonCameraOff.setVisibility(View.GONE);
        mUIContainer.mComboButtonHoldCall.setVisibility(View.GONE);
    }

    public void setConfInComingViewsGone() {
        mUIContainer.mComboButtonAddCall.setVisibility(View.GONE);
    }

    public void setaddcallShow(boolean show) {
        mUIContainer.mComboButtonAddCall.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setButtonSetEnabledCamera(boolean enabled) {
        JRCommonUtils.setMultipleViewsEnabled(enabled, mUIContainer.mComboButtonSwitchFrontRear,
                mUIContainer.mComboButtonCameraOff, mUIContainer.mComboButtonCameraOn);
    }

    public void setButtonSetEnabledNormal(boolean enabled) {
        JRCommonUtils.setMultipleViewsEnabled(enabled, mUIContainer.mCircleButtonEnd, mUIContainer.mComboButtonMute, mUIContainer.mComboButtonSpeaker,
                mUIContainer.mComboButtonAddCall, mUIContainer.mComboButtonMergeCall, mUIContainer.mComboButtonHoldCall);
    }

    public void setButtonSetEnabledAll(boolean enabled) {
        setButtonSetEnabledCamera(enabled);
        setButtonSetEnabledNormal(enabled);
    }

    public void setStateText(CharSequence text, boolean animated, boolean warn) {
        int textColor = Color.WHITE;
        if (TextUtils.isEmpty(text)) {
            if (mUIContainer.mChronometerState.getBase() != 0) {
                mUIContainer.mChronometerState.start();
            } else {
                mUIContainer.mChronometerState.setText("");
            }
        } else {
            mUIContainer.mChronometerState.stop();
            if (animated) {
                text = text.toString().concat("...");
            }
            if (warn) {
                textColor = mContext.getResources().getColor(R.color.call_poor_network_bg);
            }
            mUIContainer.mChronometerState.setText(text);
        }
        mUIContainer.mChronometerState.setTextColor(textColor);
    }

    public void resetStateText() {
        if (mUIContainer.mChronometerState != null) {
            mUIContainer.mChronometerState.stop();
            mUIContainer.mChronometerState.setBase(0);
            mUIContainer.mChronometerState.setText("");
            mUIContainer.mChronometerState.setTextColor(Color.WHITE);
        }
    }

    public String getStateText() {
        return mUIContainer.mChronometerState.getText().toString();
    }

    public void setPage(int index) {
        mUIContainer.setPage(index);
    }

    public void setPageEnabled(boolean enabled) {
        mUIContainer.setPageEnabledInternalNoChangeIndex(enabled);
    }

    public void setConferenceList(List<CallCell> callCells) {
        mUIContainer.mConferenceAdapter.setData(CallCell.copyList());
    }

    public String getUserName() {
        return mUIContainer.mTextViewName.getText().toString();
    }

    public boolean isMuted() {
        return mUIContainer.mCircleButtonMute.isSelected();
    }

    public boolean isSpeakerOn() {
        return mUIContainer.mCircleButtonSpeaker.isSelected();
    }

    public void setSpeakerOn(boolean on) {
        mUIContainer.mCircleButtonSpeaker.setSelected(on);
    }

    public void setSpeakerImage(int resId) {
        mUIContainer.mCircleButtonSpeaker.setImageResource(resId);
    }

    public boolean isHold() {
        return mUIContainer.mCircleButtonHoldCall.isSelected();
    }

    public void setHold(boolean isSelected, boolean enable) {
        mUIContainer.mCircleButtonHoldCall.setSelected(isSelected);
        mUIContainer.mCircleButtonHoldCall.setClickable(enable);
    }

    public void setHeld(boolean isSelected, boolean enable) {
        mUIContainer.mCircleButtonHoldCall.setSelected(isSelected);
        mUIContainer.mCircleButtonHoldCall.setClickable(enable);
        mUIContainer.mCircleButtonAddCall.setEnabled(enable);
        mUIContainer.mCircleButtonCameraOn.setEnabled(enable);
    }

    public void showMergeCall() {
        mUIContainer.mComboButtonMergeCall.setVisibility(View.VISIBLE);
        mUIContainer.mComboButtonAddCall.setVisibility(View.GONE);
    }

    public void hideMergeCall(boolean isVideo) {
        mUIContainer.mComboButtonMergeCall.setVisibility(View.GONE);
        mUIContainer.mComboButtonAddCall.setVisibility(isVideo ? View.GONE : View.VISIBLE);
    }

    public void onSessionDestroyed() {
        if (mUIContainer.mViewIncoming != null) {
            mUIContainer.mViewIncoming.destroy();
        }
    }

    public void onOrientationChanged(int orientation) {
        if (orientation == Surface.ROTATION_180) {
            return;
        }

        mUIContainer.mViewUser.setVisibility(orientation == Surface.ROTATION_0 ? View.VISIBLE : View.GONE);

        Rotatable[] indicators = {mUIContainer.mCircleButtonEnd, mUIContainer.mCircleButtonMute, mUIContainer.mCircleButtonSpeaker,
                mUIContainer.mCircleButtonCameraOn, mUIContainer.mCircleButtonCameraOff, mUIContainer.mCircleButtonSwitchFrontRear,
                mUIContainer.mCircleButtonHoldCall, mUIContainer.mCircleButtonRedial, mUIContainer.mCircleButtonCancel,
                mUIContainer.mCircleButtonAddCall, mUIContainer.mCircleButtonMergeCall
        };
        final int degree = orientation * 90;
        for (Rotatable indicator : indicators) {
            if (indicator != null) {
                indicator.setOrientation(degree, true);
            }
        }

        View[] texts = {
                mUIContainer.mTextViewCameraOn, mUIContainer.mTextViewCameraOff, mUIContainer.mTextViewSwitchFrontRear,
                mUIContainer.mTextViewRedial, mUIContainer.mTextViewHoldCall,
                mUIContainer.mTextViewAddCall, mUIContainer.mTextViewMergeCall
        };
        int visibility = (orientation & 1) == 0 ? View.VISIBLE : View.INVISIBLE;
        for (View view : texts) {
            view.setVisibility(visibility);
        }
    }

    public void onUserInfoChanged(String name, String number) {
        mUIContainer.mTextViewName.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(name)) {
            if (isUndialableAnonymous(number)) {
                mUIContainer.mTextViewName.setText("匿名号码");
            } else {
                mUIContainer.mTextViewName.setText(number);
            }
        } else {
            mUIContainer.mTextViewName.setText(isUndialableAnonymous(name) ? "匿名号码" : name);
        }
    }

    public void hideMultiVideoItems() {
        mUIContainer.mTextViewName.setVisibility(View.GONE);
        mUIContainer.mChronometerState.setVisibility(View.GONE);
    }

    public void appendDtmf(String text) {
        mUIContainer.mTextViewDtmf.append(text);
    }

    public boolean isUndialableAnonymous(String number) {
        return "Anonymous".equalsIgnoreCase(number);
    }

    public void setDirectionIncoming(boolean incoming) {
        mUIContainer.mTextViewRedial.setText(incoming ? R.string.call_back : R.string.redial);
    }

    public void setStatusIncoming(boolean incoming, boolean isVideo) {
        if (incoming) {
            if (mUIContainer.mViewIncoming == null) {
                mUIContainer.inflateIncomingView();
            } else {
                mUIContainer.mViewIncoming.reset();
                mUIContainer.mViewIncoming.setVisibility(View.VISIBLE);
            }
            mUIContainer.mViewIncoming.setCallback(this);
            mUIContainer.mViewIncoming.setVideo(isVideo);

            mUIContainer.mButtonsContainerFirstLine.setVisibility(View.INVISIBLE);
            mUIContainer.mButtonsContainerSecondLine.setVisibility(View.INVISIBLE);
            mUIContainer.setPageEnabledInternal(false);
        } else {
            if (mUIContainer.mViewIncoming != null) {
                mUIContainer.mViewIncoming.destroy();
                mUIContainer.mViewIncoming.setVisibility(View.GONE);
                mUIContainer.mViewIncoming.setCallback(null);
            }

            mUIContainer.mButtonsContainerFirstLine.setVisibility(View.VISIBLE);
            mUIContainer.mButtonsContainerSecondLine.setVisibility(View.VISIBLE);
        }
    }

    private View findViewById(int id) {
        return mParentView.findViewById(id);
    }

    public void setBaseTime(long baseTime) {
        mUIContainer.mChronometerState.setBase(baseTime);
    }

    public long getBaseTime() {
        return mUIContainer.mChronometerState.getBase();
    }

    public void startTimer() {
        mUIContainer.mChronometerState.start();
    }

    public void enterConference() {
        mUIContainer.mListViewConference.setVisibility(View.VISIBLE);
        mUIContainer.mViewUser.setVisibility(View.INVISIBLE);
        mUIContainer.setPageEnabledInternal(false);

    }

    public void enterIncomingConference() {
        mUIContainer.mListViewConference.setVisibility(View.INVISIBLE);
        mUIContainer.mViewUser.setVisibility(View.VISIBLE);
    }

    public void exitConference() {
        mUIContainer.mListViewConference.setVisibility(View.INVISIBLE);
        mUIContainer.mViewUser.setVisibility(View.VISIBLE);
        mUIContainer.setPageEnabledInternal(true);
    }

    private void colorNormalCircleButton(CircleButton button, int resId) {
//        Resources res = mContext.getResources();
//        button.setStroke(2, res.getColor(R.color.call_menu_default_stroke_color));
//        button.setDisabledStroke(2, res.getColor(R.color.call_menu_default_disabled_stroke_color));
//        button.setBackgroundNormalColor(res.getColor(R.color.call_menu_default_bg_normal_color));
//        button.setBackgroundPressedColor(res.getColor(R.color.call_menu_default_bg_pressed_color));
//        button.setBackgroundSelectedColor(res.getColor(R.color.call_menu_default_bg_selected_color));
//        button.setBackgroundDisabledColor(res.getColor(R.color.call_menu_default_bg_disabled_color));
        button.setImageResource(resId);
    }

    private void colorEndCircleButton(CircleButton button, int resId) {
        Resources res = mContext.getResources();
        button.setStroke(0, 0);
        button.setDisabledStroke(0, 0);
        button.setBackgroundNormalColor(res.getColor(R.color.call_menu_end_bg_normal_color));
        button.setBackgroundPressedColor(res.getColor(R.color.call_menu_end_bg_pressed_color));
        button.setBackgroundDisabledColor(res.getColor(R.color.call_menu_end_bg_disabled_color));
        button.setImageResource(resId);
    }

    private void colorRedialCircleButton(CircleButton button, int resId) {
        Resources res = mContext.getResources();
        button.setStroke(0, 0);
        button.setDisabledStroke(0, 0);
        button.setBackgroundNormalColor(res.getColor(R.color.call_menu_redial_bg_normal_color));
        button.setBackgroundPressedColor(res.getColor(R.color.call_menu_redial_bg_pressed_color));
        button.setBackgroundDisabledColor(res.getColor(R.color.call_menu_redial_bg_disabled_color));
        button.setImageResource(resId);
    }

    @Override
    public void callHandleViewAnswerVoice(CallIncomingSlideView v) {
        sendEvent(EVENT_ANSWER_DEFAULT);
    }

    @Override
    public void callHandleViewAnswerVideo(CallIncomingSlideView v) {
        sendEvent(EVENT_ANSWER_DEFAULT);
    }

    @Override
    public void callHandleViewAnswerCameraOff(CallIncomingSlideView v) {
        sendEvent(EVENT_ANSWER_CAMERA_OFF);
    }

    @Override
    public void callHandleViewDecline(CallIncomingSlideView v) {
        sendEvent(EVENT_ANSWER_DECLINE);
    }

    private void sendEvent(String event, String... args) {
        if (mEventReceiver != null) {
            log("send event: " + event);
            mEventReceiver.onEvent(event, args);
        }
    }

    private static void log(String msg) {

    }

    private static final int MESSAGE_SHOW_FIRST_PAGE = 1000;

    private static class MyHandler extends Handler {
        private WeakReference<OperationLayer> mOperationLayerWeakReference;

        public MyHandler(OperationLayer operationLayer) {
            mOperationLayerWeakReference = new WeakReference<>(operationLayer);
        }

        @Override
        public void handleMessage(Message msg) {
            OperationLayer operationLayer = mOperationLayerWeakReference.get();
            if (operationLayer == null) {
                return;
            }
            switch (msg.what) {
                case MESSAGE_SHOW_FIRST_PAGE:
                    operationLayer.setPage(0);
                    break;
            }
        }
    }

    private static class CallPagerAdapter extends PagerAdapter {

        public void setViews(List<View> viewList) {
            mViewList = viewList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mViewList == null ? 0 : mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup view, int position, Object object) {
            view.removeView(mViewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            view.addView(mViewList.get(position));
            return mViewList.get(position);
        }

        private List<View> mViewList;
    }

    private static class UIElementsContainer implements ViewPager.OnPageChangeListener {
        public View mRoot;

        public ListView mListViewConference;
        public CallConferenceAdapter mConferenceAdapter;
        public View mButtonsContainerFirstLine;
        public View mButtonsContainerSecondLine;

        private View mViewUser;
        private TextView mTextViewName;
        private Chronometer mChronometerState;
        private TextView mTextViewDtmf;

        private View mComboButtonCancel;
        private View mComboButtonMute;
        private View mComboButtonSpeaker;
        private CircleButton mCircleButtonEnd;
        private CircleButton mCircleButtonCancel;
        private CircleButton mCircleButtonMute;
        private CircleButton mCircleButtonSpeaker;
        private View mViewStatistic;
        private CallIncomingSlideView mViewIncoming;

        public View mComboButtonCameraOff;
        public View mComboButtonMultiAdd;
        public View mComboButtonAddCall;
        public View mComboButtonSwitchFrontRear;
        public View mComboButtonCameraOn;
        public View mComboButtonHoldCall;
        public View mComboButtonMergeCall;
        public View mComboButtonRedial;
        public CircleButton mCircleButtonCameraOff;
        public CircleButton mCircleButtonAddCall;
        public CircleButton mCircleButtonMultiAdd;
        public CircleButton mCircleButtonSwitchFrontRear;
        public CircleButton mCircleButtonCameraOn;
        public CircleButton mCircleButtonHoldCall;
        public CircleButton mCircleButtonMergeCall;
        public CircleButton mCircleButtonRedial;
        public TextView mTextViewCameraOff;
        public TextView mTextViewAddCall;
        public TextView mTextViewMultiAdd;
        public TextView mTextViewSwitchFrontRear;
        public TextView mTextViewCameraOn;
        public TextView mTextViewHoldCall;
        public TextView mTextViewMergeCall;
        public TextView mTextViewRedial;

        public void initViews(Context context, View view, View.OnClickListener listener) {
            mRoot = view;

            initPages(context, listener);

            mViewUser = view.findViewById(R.id.call_user);
            mTextViewName = (TextView) view.findViewById(R.id.call_name);
            mChronometerState = (Chronometer) view.findViewById(R.id.call_state);

            mComboButtonCancel = view.findViewById(R.id.call_combo_button_cancel);
            mComboButtonMute = view.findViewById(R.id.call_combo_button_mute);
            mComboButtonSpeaker = view.findViewById(R.id.call_combo_button_speaker);
            mCircleButtonEnd = (CircleButton) view.findViewById(R.id.call_button_end);
            mCircleButtonCancel = (CircleButton) view.findViewById(R.id.call_button_cancel);
            mCircleButtonMute = (CircleButton) view.findViewById(R.id.call_button_mute);
            mCircleButtonSpeaker = (CircleButton) view.findViewById(R.id.call_button_speaker);
            mViewStatistic = view.findViewById(R.id.call_statistic);

            mButtonsContainerFirstLine = view.findViewById(R.id.call_buttons_first_line);
            mButtonsContainerSecondLine = view.findViewById(R.id.call_buttons_second_line);

            mComboButtonRedial = view.findViewById(R.id.call_combo_button_redial);
            mCircleButtonRedial = (CircleButton) view.findViewById(R.id.call_button_redial);
            mTextViewRedial = (TextView) view.findViewById(R.id.call_button_text_redial);

        }

        public void inflateIncomingView() {
            mViewIncoming = (CallIncomingSlideView) ((ViewStub) mRoot.findViewById(R.id.call_incoming_import)).inflate();
        }

        private TwoStateScrollViewPager mViewPager;
        private CirclePageIndicator mPageIndicator;

        private void initPages(Context context, View.OnClickListener listener) {
            View page1 = LayoutInflater.from(context).inflate(R.layout.call_buttons_page1, null);
            View page2 = LayoutInflater.from(context).inflate(R.layout.call_buttons_page2, null);
            page1.setOnClickListener(listener);
            page2.setOnClickListener(listener);
            List<View> viewList = new ArrayList<>();
            viewList.add(page1);
            viewList.add(page2);
            CallPagerAdapter pagerAdapter = new CallPagerAdapter();
            pagerAdapter.setViews(viewList);
            mViewPager = (TwoStateScrollViewPager) mRoot.findViewById(R.id.button_pager);
            mViewPager.setAdapter(pagerAdapter);
            mViewPager.addOnPageChangeListener(this);
            mPageIndicator = (CirclePageIndicator) mRoot.findViewById(R.id.button_pager_indicator);
            mPageIndicator.setViewPager(mViewPager);

            mComboButtonCameraOff = page1.findViewById(R.id.call_combo_button_camera_off);
            mCircleButtonCameraOff = (CircleButton) page1.findViewById(R.id.call_button_camera_off);
            mTextViewCameraOff = (TextView) page1.findViewById(R.id.call_button_text_camera_off);

            mComboButtonMultiAdd = page1.findViewById(R.id.call_combo_button_add_multi_member);
            mCircleButtonMultiAdd = (CircleButton) page1.findViewById(R.id.call_button_add_multi_member);
            mTextViewMultiAdd = (TextView) page1.findViewById(R.id.call_button_text_add_multi_call);

            mComboButtonAddCall = page1.findViewById(R.id.call_combo_button_add_call);
            mCircleButtonAddCall = (CircleButton) page1.findViewById(R.id.call_button_add_call);
            mTextViewAddCall = (TextView) page1.findViewById(R.id.call_button_text_add_call);

            mComboButtonSwitchFrontRear = page1.findViewById(R.id.call_combo_button_switch_front_rear);
            mCircleButtonSwitchFrontRear = (CircleButton) page1.findViewById(R.id.call_button_switch_front_rear);
            mTextViewSwitchFrontRear = (TextView) page1.findViewById(R.id.call_button_text_switch_front_rear);

            mComboButtonCameraOn = page1.findViewById(R.id.call_combo_button_camera_on);
            mCircleButtonCameraOn = (CircleButton) page1.findViewById(R.id.call_button_camera_on);
            mTextViewCameraOn = (TextView) page1.findViewById(R.id.call_button_text_camera_on);

            mComboButtonHoldCall = page1.findViewById(R.id.call_combo_button_hold_call);
            mCircleButtonHoldCall = (CircleButton) page1.findViewById(R.id.call_button_hold_call);
            mTextViewHoldCall = (TextView) page1.findViewById(R.id.call_button_text_hold_call);

            mComboButtonMergeCall = page1.findViewById(R.id.call_combo_button_merge_call);
            mCircleButtonMergeCall = (CircleButton) page1.findViewById(R.id.call_button_merge_call);
            mTextViewMergeCall = (TextView) page1.findViewById(R.id.call_button_text_merge_call);

            mListViewConference = (ListView) page1.findViewById(R.id.call_conference);
            mConferenceAdapter = new CallConferenceAdapter(context);
            mListViewConference.setDivider(null);
            mListViewConference.setDividerHeight(0);
            mListViewConference.setAdapter(mConferenceAdapter);
            mTextViewDtmf = (TextView) page2.findViewById(R.id.dialpad_text_view);
        }

        private void setPageEnabledInternal(boolean enabled) {
            if (enabled) {
                mPageIndicator.setVisibility(View.VISIBLE);
                mViewPager.setScrollable(true);
            } else {
                mPageIndicator.setVisibility(View.INVISIBLE);
                mViewPager.setCurrentItem(0);
                mViewPager.setScrollable(false);
            }
        }

        private void setPageEnabledInternalNoChangeIndex(boolean enabled) {
            if (enabled) {
                mPageIndicator.setVisibility(View.VISIBLE);
                mViewPager.setScrollable(true);
            } else {
                mPageIndicator.setVisibility(View.INVISIBLE);
                mViewPager.setScrollable(false);
            }
        }

        public void setPage(int index) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(index);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (position == 0) {
                mViewUser.setAlpha(255 - 255 * positionOffset);
            } else if (position == 1) {
                mViewUser.setAlpha(255 * positionOffset);
            }
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
