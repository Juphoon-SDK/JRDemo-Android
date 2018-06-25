package com.juphoon.rcs.jrdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.juphoon.rcs.JRMessageContants;

import common.model.RealmConversation;
import common.utils.JRDateUtils;
import io.realm.RealmResults;

/**
 * Created by Upon on 2018/3/13.
 */

public class JRMessageListAdapter extends RecyclerView.Adapter<JRMessageListAdapter.ItemHolder> {
    private Context mContext;
    private RealmResults<RealmConversation> mConversations;

    public JRMessageListAdapter(Context context, RealmResults<RealmConversation> conversations) {
        mContext = context;
        mConversations = conversations;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_message_list, parent, false);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        RealmConversation conversation = mConversations.get(position);
        if (conversation.getChatType() == JRMessageContants.ChatType.TYPE_GROUP) {
            holder.name.setText(conversation.getConversationName());
        } else {
            holder.name.setText(TextUtils.isEmpty(conversation.getConversationName()) ?
                    conversation.getPeerPhone() : conversation.getConversationName());
        }
        holder.message.setText(TextUtils.isEmpty(conversation.getLastMessage()) ? "" : conversation.getLastMessage());
        holder.date.setText(JRDateUtils.getTimeStringX(mContext, conversation.getUpdateTime(), false) + "");
        holder.okBtn.setVisibility(View.GONE);
        holder.ignoreBtn.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(conversation.getSessionIdentity()) && conversation.isInvite()) {
            holder.okBtn.setVisibility(View.VISIBLE);
            holder.ignoreBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mConversations.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        TextView name, date, message;
        Button okBtn, ignoreBtn;

        public ItemHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            okBtn = itemView.findViewById(R.id.group_invite_ok);
            ignoreBtn = itemView.findViewById(R.id.group_invite_ignore);
        }
    }
}
