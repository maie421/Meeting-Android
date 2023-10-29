package com.example.meeting_android.activity.chatting;

import static com.example.meeting_android.activity.chatting.MemberData.getRandomColor;
import static com.example.meeting_android.common.Common.getNowTime;
import static com.example.meeting_android.webrtc.PeerConnectionClient.peerDataChannelnMap;
import static com.example.meeting_android.webrtc.WebSocketClientManager.name;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.meeting_android.R;

import org.webrtc.DataChannel;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ChattingMainActivity extends AppCompatActivity {
    public ListView chatListView;
    public ImageButton sendButton;
    public static MessageAdapter messageAdapter;
    private String CHATTING_TAG = "채팅";
    public EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_main);

        chatListView = findViewById(R.id.messages_view);
        sendButton = findViewById(R.id.sendButton);
        editText = findViewById(R.id.editText);

        messageAdapter = new MessageAdapter(this, this);
        chatListView.setAdapter(messageAdapter);
        chatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL); // 스크롤 모드 설정

        // 리스트뷰에 새로운 항목을 추가할 때 자동으로 스크롤
        chatListView.post(new Runnable() {
            @Override
            public void run() {
                chatListView.setSelection(chatListView.getCount() - 1);
            }
        });

        sendButton.setOnClickListener(v->{
            sendMessage(editText.getText().toString());
        });
    }
    public void sendMessage(String messageString) {
        if (messageString != null && !messageString.isEmpty()) {
            MemberData memberData = new MemberData(name, getRandomColor());
            StringBuffer sb1 = new StringBuffer();
            sb1.append(messageString);

            Message message = new Message(messageString, memberData, true);
            messageAdapter.add(message);

            peerDataChannelnMap.forEach((key, value)->{
                ByteBuffer data = stringToByteBuffer("-s" +name+"::"+messageString, Charset.defaultCharset());
                Log.d(CHATTING_TAG, "send data : "+ data + " key : " + key);
                peerDataChannelnMap.get(key).send(new DataChannel.Buffer(data, false));
            });

            editText.getText().clear();
        }
    }

    private ByteBuffer stringToByteBuffer(String msg, Charset charset) {
        return ByteBuffer.wrap(msg.getBytes(charset));
    }

}