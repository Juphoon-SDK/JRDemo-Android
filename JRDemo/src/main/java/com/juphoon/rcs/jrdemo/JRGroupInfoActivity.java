package com.juphoon.rcs.jrdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.juphoon.rcs.JRClient;
import com.juphoon.rcs.JRGroup;
import com.juphoon.rcs.JRGroupCallback;
import com.juphoon.rcs.JRGroupItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import common.RealmHelper;
import common.model.RealmGroup;
import common.model.RealmGroupMember;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class JRGroupInfoActivity extends AppCompatActivity implements View.OnClickListener, JRGroupCallback {

    private Realm mRealm;
    private RealmGroup mRealmGroup;
    private RealmResults<RealmGroupMember> mRealmGroupMembers;
    private String mSessIdentity;
    private List<LinearLayout> mListMemberContainer = new ArrayList<>();
    private String mGroupNickName;   // 本人在群中的昵称
    private boolean mIsChairman = false; // 本人是否是群主

    private TextView mGroupMemberText;
    private LinearLayout mMemberLayout1, mMemberLayout2, mMemberLayout3, mMemberLayout4, mMemberAddLayout,
            mGroupNameLayout, mNickNameLayout, mGroupLeaveLayout, mGroupDissolveLayot;
    private ConstraintLayout mConstraintLayout;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jrgroup_info);
        mRealm = RealmHelper.getInstance();
        HandleIntent();
        initViews();
        initData();
        initListener();
        JRGroup.getInstance().subscribeGroupInfo(mRealmGroup.getSessIdentity());
    }

    @Override
    protected void onDestroy() {
        JRGroup.getInstance().removeCallback(this);
        mRealmGroup.removeAllChangeListeners();
        mRealmGroupMembers.removeAllChangeListeners();
        mRealm.close();
        super.onDestroy();
    }

    private void HandleIntent() {
        mSessIdentity = getIntent().getStringExtra(RealmGroup.FIELD_SESSIDENTITY);
    }

    private void initViews() {
        mConstraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
        mGroupMemberText = (TextView) findViewById(R.id.group_member_text);
        mMemberLayout1 = (LinearLayout) findViewById(R.id.member1);
        mMemberLayout2 = (LinearLayout) findViewById(R.id.member2);
        mMemberLayout3 = (LinearLayout) findViewById(R.id.member3);
        mMemberLayout4 = (LinearLayout) findViewById(R.id.member4);
        mMemberAddLayout = (LinearLayout) findViewById(R.id.member_add);
        mGroupNameLayout = (LinearLayout) findViewById(R.id.group_name);
        mNickNameLayout = (LinearLayout) findViewById(R.id.nick_name);
        mGroupLeaveLayout = (LinearLayout) findViewById(R.id.group_leave);
        mGroupDissolveLayot = (LinearLayout) findViewById(R.id.group_dissolve);
        mListMemberContainer.add(mMemberLayout1);
        mListMemberContainer.add(mMemberLayout2);
        mListMemberContainer.add(mMemberLayout3);
        mListMemberContainer.add(mMemberLayout4);
    }

    private void initListener() {
        mConstraintLayout.setOnClickListener(this);
        mMemberAddLayout.setOnClickListener(this);
        mGroupNameLayout.setOnClickListener(this);
        mNickNameLayout.setOnClickListener(this);
        mGroupLeaveLayout.setOnClickListener(this);
        mGroupDissolveLayot.setOnClickListener(this);
        JRGroup.getInstance().addCallback(this);
        mRealmGroup.addChangeListener(new RealmChangeListener<RealmGroup>() {
            @Override
            public void onChange(RealmGroup realmGroup) {
                mRealmGroup = realmGroup;
                updateView();
            }
        });
        mRealmGroupMembers.addChangeListener(new RealmChangeListener<RealmResults<RealmGroupMember>>() {
            @Override
            public void onChange(RealmResults<RealmGroupMember> realmGroupMembers) {
                mRealmGroupMembers = realmGroupMembers;
                updateView();
            }
        });
    }

    private void initData() {
        mRealmGroup = mRealm.where(RealmGroup.class).equalTo(RealmGroup.FIELD_SESSIDENTITY, mSessIdentity).findFirst();
        mRealmGroupMembers = mRealm.where(RealmGroupMember.class).equalTo(RealmGroupMember.FIELD_SESSIDENTITY, mSessIdentity).findAll();
        updateView();
    }

    private void updateView() {
        if (!mRealmGroup.isValid()) {
            finish();
            return;
        }
        mGroupMemberText.setText("群成员" + String.format("（%1$d）", mRealmGroupMembers.size()));
        ArrayList<RealmGroupMember> memberList = new ArrayList<>();
        for (RealmGroupMember member : mRealmGroupMembers) {
            memberList.add(member);
        }
        for (int i = 0; i < memberList.size(); i++) {
            if (TextUtils.equals(mRealmGroup.getChairmanNumber(), memberList.get(i).getNumber())) {
                Collections.swap(memberList, 0, i);
                break;
            }
        }
        for (int i = 0; i < memberList.size() && i < 4; i++) {
            LinearLayout layout = mListMemberContainer.get(i);
            RealmGroupMember member = memberList.get(i);
            boolean isChairman = TextUtils.equals(mRealmGroup.getChairmanNumber(), member.getNumber());
            if (TextUtils.equals(member.getNumber(), JRClient.getInstance().getCurLoginNumber())) {
                mGroupNickName = member.getDisplayName();
            }
            layout.setVisibility(View.VISIBLE);
            setImageAndText(layout, member, isChairman);
        }
        for (int i = 3; i >= memberList.size(); i--) {
            mListMemberContainer.get(i).setVisibility(View.GONE);
        }
        mMemberAddLayout.findViewById(R.id.tag_chairman).setVisibility(View.GONE);
        ((ImageView) mMemberAddLayout.findViewById(R.id.member_icon)).setImageResource(R.drawable.icon_add_member);

        ((TextView) mGroupNameLayout.findViewById(R.id.item_title)).setText("群名称");
        ((TextView) mGroupNameLayout.findViewById(R.id.item_content)).setText(mRealmGroup.getSubject());

        ((TextView) mNickNameLayout.findViewById(R.id.item_title)).setText("群名片");
        ((TextView) mNickNameLayout.findViewById(R.id.item_content)).setText(mGroupNickName);

        ((TextView) mGroupLeaveLayout.findViewById(R.id.item_title)).setText("离开群");

        mIsChairman = TextUtils.equals(mRealmGroup.getChairmanNumber(), JRClient.getInstance().getCurLoginNumber());
        if (mIsChairman) {
            mGroupDissolveLayot.setVisibility(View.VISIBLE);
            ((TextView) mGroupDissolveLayot.findViewById(R.id.item_title)).setText("解散群");
        } else {
            mGroupDissolveLayot.setVisibility(View.GONE);
        }
    }

    private void setImageAndText(LinearLayout layout, RealmGroupMember realmGroupMember, boolean isChairman) {
        layout.findViewById(R.id.tag_chairman).setVisibility(isChairman ? View.VISIBLE : View.GONE);
        boolean me = TextUtils.equals(realmGroupMember.getNumber(), JRClient.getInstance().getCurLoginNumber());
        String nickName;
        if (me) {
            nickName = "我";
        } else if (!TextUtils.isEmpty(realmGroupMember.getDisplayName())) {
            nickName = realmGroupMember.getDisplayName();
        } else {
            nickName = realmGroupMember.getNumber();
        }
        ((TextView) layout.findViewById(R.id.member_nick_name)).setText(nickName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.constraintLayout:
                Intent intent = new Intent(JRGroupInfoActivity.this, JRGroupMemberListActivity.class);
                intent.putExtra(RealmGroup.FIELD_SESSIDENTITY, mSessIdentity);
                startActivity(intent);
                break;
            case R.id.member_add:
                showEditDialog("添加群成员", null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] numberArray = mEditText.getText().toString().split(";");
                        ArrayList<String> numberList = new ArrayList<>(Arrays.asList(numberArray));
                        if (JRGroup.getInstance().invite(RealmGroup.realm2Item(mRealmGroup), numberList)) {
                            dialog.dismiss();
                        }
                    }
                });
                break;
            case R.id.group_name:
                if (!mIsChairman) {
                    Toast.makeText(JRGroupInfoActivity.this, "您不是群主！", Toast.LENGTH_SHORT).show();
                    return;
                }
                showEditDialog("修改群名称", mRealmGroup.getSubject(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newGroupName = mEditText.getText().toString();
                        if (JRGroup.getInstance().modifyGroupName(RealmGroup.realm2Item(mRealmGroup), newGroupName)) {
                            dialog.dismiss();
                        }
                    }
                });
                break;
            case R.id.nick_name:
                showEditDialog("修改群名片", mGroupNickName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newNickName = mEditText.getText().toString();
                        if (JRGroup.getInstance().modifyNickName(RealmGroup.realm2Item(mRealmGroup), newNickName)) {
                            dialog.dismiss();
                        }
                    }
                });
                break;
            case R.id.group_leave:
                if (mIsChairman) {
                    Toast.makeText(JRGroupInfoActivity.this, "请先移交群主", Toast.LENGTH_SHORT).show();
                } else {
                    JRGroup.getInstance().leave(RealmGroup.realm2Item(mRealmGroup));
                }
                break;
            case R.id.group_dissolve:
                if (mIsChairman) {
                    JRGroup.getInstance().dissolve(RealmGroup.realm2Item(mRealmGroup));
                } else {
                    Toast.makeText(JRGroupInfoActivity.this, "您不是群主！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onGroupAdd(JRGroupItem jrGroupItem) {

    }

    @Override
    public void onGroupRemove(JRGroupItem jrGroupItem) {
        finish();
    }

    @Override
    public void onGroupUpdate(JRGroupItem jrGroupItem, boolean isFully) {

    }

    @Override
    public void onGroupOperationResult(int i, boolean b, int i1, String s) {

    }

    @Override
    public void onGroupListSubResult(boolean succ, ArrayList<JRGroupItem> arrayList) {

    }

    private void showEditDialog(String title, String editStr, DialogInterface.OnClickListener listener) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(50, 0, 50, 0);
        EditText editText = new EditText(this);
        editText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        editText.setText(editStr);
        linearLayout.addView(editText);
        editText.setSelection(TextUtils.isEmpty(editStr) ? 0 : editStr.length());
        mEditText = editText;
        new AlertDialog.Builder(this).setTitle(title).setView(linearLayout).setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
