package com.juphoon.rcs.jrdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.juphoon.rcs.JRGroup;

import java.util.ArrayList;
import java.util.Arrays;

import common.RealmDataHelper;
import common.RealmHelper;
import common.model.RealmConversation;
import common.model.RealmGroup;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import ui.RecyclerViewClickListener;

/**
 * Created by Upon on 2018/3/13.
 */

public class JRMessageListActivity extends AppCompatActivity {
    private RecyclerView mMessageListView;
    private RealmResults<RealmConversation> mRealmConversations;
    private Realm mRealm;
    private JRMessageListAdapter mAdapter;
    private EditText mEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_list);
        mRealm = RealmHelper.getInstance();
        initViews();
        initListener();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "新建消息");
        menu.add(1, 2, 2, "新建群聊");
        menu.add(1, 3, 3, "群列表");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 1) {
            showEditDialog("请输入号码", null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String peernumber = mEditText.getText().toString();
                    Intent intent = new Intent(JRMessageListActivity.this, JRMessageActivity.class);
                    intent.putExtra("account_phone", peernumber);
                    startActivity(intent);
                }
            });
        } else if (id == 2) {
            showEditDialog("请输入号码(以“;”分割)", null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String[] numberArray = mEditText.getText().toString().split(";");
                    final ArrayList numbers = new ArrayList(Arrays.asList(numberArray));
                    showEditDialog("请输入群名称", null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String groupName = mEditText.getText().toString();
                            JRGroup.getInstance().create(groupName, numbers);
                        }
                    });
                }
            });
        } else if (id == 3) {
            startActivity(new Intent(JRMessageListActivity.this, JRGroupListActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        mMessageListView = (RecyclerView) findViewById(R.id.recycler_list);
    }

    private void initListener() {

    }

    private void initData() {
        mRealmConversations = mRealm.where(RealmConversation.class)
                .findAllSorted(RealmConversation.FIELD_UPDATE_TIME, Sort.DESCENDING);
        mRealmConversations.addChangeListener(new RealmChangeListener<RealmResults<RealmConversation>>() {
            @Override
            public void onChange(RealmResults<RealmConversation> conversations) {
                mAdapter.notifyDataSetChanged();
            }
        });
        mAdapter = new JRMessageListAdapter(getApplicationContext(), mRealmConversations);
        mMessageListView.setAdapter(mAdapter);
        mMessageListView.setLayoutManager(new LinearLayoutManager(this));
        mMessageListView.addOnItemTouchListener(new RecyclerViewClickListener(this, mMessageListView,
                mOnItemClickListener));
    }

    private RecyclerViewClickListener.SimpleOnItemClickListener mOnItemClickListener = new RecyclerViewClickListener.SimpleOnItemClickListener() {
        @Override
        public void onItemClick(View view, final int position) {
            final int id = view.getId();
            if (id == R.id.user_name || id == R.id.item_view || id == R.id.icon || id == R.id.message) {
                String peernumber = mRealmConversations.get(position).getPeerPhone();
                String sessionIdentity = mRealmConversations.get(position).getSessionIdentity();
                Intent intent = new Intent(JRMessageListActivity.this, JRMessageActivity.class);
                if (!TextUtils.isEmpty(peernumber) && TextUtils.isEmpty(sessionIdentity)) {
                    intent.putExtra("account_phone", peernumber);
                } else if (!TextUtils.isEmpty(sessionIdentity)) {
                    intent.putExtra(RealmConversation.FIELD_SESSIONIDENTITY, mRealmConversations.get(position).getSessionIdentity());
                }
                startActivity(intent);
            } else if (id == R.id.group_invite_ignore) {
                RealmDataHelper.updateIsInvite(mRealm, mRealmConversations.get(position).getSessionIdentity(), false);
            } else if (id == R.id.group_invite_ok) {
                RealmGroup group = mRealm.where(RealmGroup.class).equalTo(RealmGroup.FIELD_SESSIDENTITY,
                        mRealmConversations.get(position).getSessionIdentity()).findFirst();
                JRGroup.getInstance().acceptInvite(RealmGroup.realm2Item(group));
            }
        }
    };

    private void showEditDialog(String title, String editStr, DialogInterface.OnClickListener listener) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(50, 0, 50, 0);
        EditText editText = new EditText(this);
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText.setText(editStr);
        linearLayout.addView(editText);
        editText.setSelection(TextUtils.isEmpty(editStr) ? 0 : editStr.length());
        mEditText = editText;
        new AlertDialog.Builder(this).setTitle(title).setView(linearLayout).setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
