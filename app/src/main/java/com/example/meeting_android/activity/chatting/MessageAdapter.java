package com.example.meeting_android.activity.chatting;

import static com.example.meeting_android.activity.chatting.ChattingMainActivity.messageAdapter;
import static com.example.meeting_android.activity.chatting.Message.GUIDE;
import static com.example.meeting_android.activity.chatting.Message.MESSAGE;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.meeting_android.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageAdapter extends BaseAdapter {

    public static List<Message> messages = new ArrayList<Message>();
    Context context;
    Activity activity;

    public MessageAdapter(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void add(Message message) {
        activity.runOnUiThread(() -> {
            this.messages.add(message);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * 채팅 참고 UI
     * https://www.scaledrone.com/blog/android-chat-tutorial/
     */
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (Objects.equals(message.getType(), MESSAGE)) {
            if (message.isBelongsToCurrentUser()) {
                convertView = messageInflater.inflate(R.layout.my_message, null);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                convertView.setTag(holder);
                holder.messageBody.setText(message.getText());
            } else {
                convertView = messageInflater.inflate(R.layout.their_message, null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.messageTime = (TextView) convertView.findViewById(R.id.message_time);
                convertView.setTag(holder);

                holder.name.setText(message.getMemberData().getName());
                holder.messageBody.setText(message.getText());
                holder.messageTime.setText(message.getTime());
            }
        }

        if (Objects.equals(message.getType(), GUIDE)){
            convertView = messageInflater.inflate(R.layout.guide_message, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            holder.messageBody.setText("["+message.getMemberData().getName()+"]"+ message.getText());
        }

        return convertView;
    }

}

class MessageViewHolder {
    public TextView name;
    public TextView messageBody;
    public TextView messageTime;
}