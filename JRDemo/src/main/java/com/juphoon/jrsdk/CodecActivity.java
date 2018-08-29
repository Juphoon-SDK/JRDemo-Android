package com.juphoon.jrsdk;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.juphoon.cmcc.app.lemon.MtcCallDb;
import com.juphoon.jrsdk.ui.account.RcsAccount;
import com.juphoon.jrsdk.view.DragListView;

import java.util.ArrayList;
import java.util.List;

public class CodecActivity extends AppCompatActivity implements DragListView.DragCallBack {

    public static final String CODEC_TYPE = "codec_type";
    public static final int AUDIO = 0;
    public static final int VIDEO = 1;

    private int mType;
    protected ArrayList<String> mListEnableAudio = new ArrayList<String>();
    protected ArrayList<String> mListDisableAudio = new ArrayList<String>();
    protected ArrayList<String> mListEnableVideo = new ArrayList<String>();
    protected ArrayList<String> mListDisableVideo = new ArrayList<String>();

    private DragListView mDragList;
    private DragListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codec);

        mType = getIntent().getIntExtra(CODEC_TYPE, AUDIO);

        initData();

        mDragList = (DragListView) findViewById(R.id.drag_list);
        mAdapter = new DragListAdapter(this, mType == AUDIO ? mListEnableAudio : mListEnableVideo);
        mDragList.setAdapter(mAdapter);
        mDragList.setDragCallBack(this);

//        getSupportActionBar().setTitle(RcsAccount.getInstance().getMtcAccount());

        registerForContextMenu(mDragList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//        if ((mType == AUDIO && mListDisableAudio.size() > 0) || (mType == VIDEO && mListDisableVideo.size() > 0)) {
//            getMenuInflater().inflate(R.menu.codec, menu);
//        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mType == AUDIO) {
            RcsAccount.getInstance().setMtcAudioCodecArray(mListEnableAudio);
        } else if (mType == VIDEO) {
            RcsAccount.getInstance().setMtcVideoCodecArray(mListEnableVideo);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//            return true;
//        } else if (item.getItemId() == R.id.action_add) {
//            if (mType == AUDIO) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle(R.string.add);
//                String items[] = new String[mListDisableAudio.size()];
//                for (int i = 0; i < mListDisableAudio.size(); i++)
//                    items[i] = mListDisableAudio.get(i);
//                builder.setItems(items, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int item) {
//                        String s = mListDisableAudio.get(item);
//                        mListDisableAudio.remove(item);
//                        mListEnableAudio.add(s);
//                        mAdapter.notifyDataSetChanged();
//                        invalidateOptionsMenu();
//                        RcsAccount.getInstance().setMtcAudioCodecArray(mListEnableAudio);
//                        dialog.dismiss();
//                    }
//                });
//                builder.show();
//            } else if (mType == VIDEO) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle(R.string.add);
//                String items[] = new String[mListDisableVideo.size()];
//                for (int i = 0; i < mListDisableVideo.size(); i++)
//                    items[i] = mListDisableVideo.get(i);
//                builder.setItems(items, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int item) {
//                        String s = mListDisableVideo.get(item);
//                        mListDisableVideo.remove(item);
//                        mListEnableVideo.add(s);
//                        mAdapter.notifyDataSetChanged();
//                        invalidateOptionsMenu();
//                        RcsAccount.getInstance().setMtcVideoCodecArray(mListEnableVideo);
//                        dialog.dismiss();
//                    }
//                });
//                builder.show();
//            }
//        }
        return super.onOptionsItemSelected(item);
    }

    public void initData() {
        mListEnableAudio = RcsAccount.getInstance().getMtcAudioCodecArray();

        for (int i = 0; i < MtcCallDb.Mtc_CallDbGetSuptAudioCodecCount(); i++) {
            String s = MtcCallDb.Mtc_CallDbGetSuptAudioCodec(i);
            if (!mListEnableAudio.contains(s))
                mListDisableAudio.add(s);
        }

        mListEnableVideo = RcsAccount.getInstance().getMtcVideoCodecArray();

        for (int i = 0; i < MtcCallDb.Mtc_CallDbGetSuptVideoCodecCount(); i++) {
            String s = MtcCallDb.Mtc_CallDbGetSuptVideoCodec(i);
            if (!mListEnableVideo.contains(s))
                mListDisableVideo.add(s);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        if (v == mDragList) {
            menu.setHeaderTitle(R.string.codec);
            menu.add(0, 0, 0, R.string.del);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == 0) {
            if (mType == AUDIO) {
                String codec = mListEnableAudio.get(info.position);
                mListEnableAudio.remove(info.position);
                mListDisableAudio.add(codec);
            } else if (mType == VIDEO) {
                String codec = mListEnableVideo.get(info.position);
                mListEnableVideo.remove(info.position);
                mListDisableVideo.add(codec);
            }
            mAdapter.notifyDataSetChanged();
            invalidateOptionsMenu();
        }
        return false;
    }

    public static class DragListAdapter extends ArrayAdapter<String> {

        public DragListAdapter(Context context, List<String> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_codec, null);
            }
            if (position == 0)
                convertView.findViewById(R.id.view_divid).setVisibility(View.GONE);
            else
                convertView.findViewById(R.id.view_divid).setVisibility(View.VISIBLE);
            TextView textView = (TextView) convertView.findViewById(R.id.drag_list_item_text);
            textView.setText(getItem(position));

            return convertView;
        }
    }

    @Override
    public void OnSwitch(int from, int to) {
        if (mType == AUDIO) {
            String codec = mListEnableAudio.get(from);
            mListEnableAudio.remove(from);
            mListEnableAudio.add(to, codec);
        } else if (mType == VIDEO) {
            String codec = mListEnableVideo.get(from);
            mListEnableVideo.remove(from);
            mListEnableVideo.add(to, codec);
        }
        mAdapter.notifyDataSetChanged();
    }
}
