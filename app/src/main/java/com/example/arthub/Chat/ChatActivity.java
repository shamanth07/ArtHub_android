package com.example.arthub.Chat;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.arthub.R;
import com.google.firebase.database.*;

public class ChatActivity extends AppCompatActivity {

    private LinearLayout messageContainer;
    private EditText etMessage;
    private ScrollView scrollView;


    private  TextView chatwithwho;

    private DatabaseReference chatRef;
    private String senderRole;
    private String userId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageContainer = findViewById(R.id.messageContainer);
        etMessage = findViewById(R.id.etMessage);
        scrollView = findViewById(R.id.scrollView);
        ImageButton btnSend = findViewById(R.id.btnSend);
        chatwithwho = findViewById(R.id.chatwithwho);




        senderRole = getIntent().getStringExtra("role");
        userId = getIntent().getStringExtra("userId");

        if (senderRole == null) senderRole = "visitor";
        if (userId == null) userId = "unknown";

        chatId = userId;


        switch (senderRole.toLowerCase()) {
            case "artist":
                chatwithwho.setText("Chat with Admin (You are Artist)");
                break;
            case "visitor":
                chatwithwho.setText("Chat with Admin (You are Visitor)");
                break;
            case "admin":
                chatwithwho.setText("Chat with User: " + userId);
                break;
            default:
                chatwithwho.setText("Live Chat");
                break;
        }

        chatRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(chatId)
                .child("messages");

        btnSend.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        String messageId = chatRef.push().getKey();
        if (messageId == null) return;

        ChatMessage chatMessage = new ChatMessage(
                messageText,
                senderRole,
                System.currentTimeMillis()
        );

        chatRef.child(messageId).setValue(chatMessage);
        etMessage.setText("");
    }

    private void listenForMessages() {
        chatRef.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String prevKey) {
                ChatMessage msg = snapshot.getValue(ChatMessage.class);
                if (msg != null) showMessage(msg);
            }

            public void onChildChanged(@NonNull DataSnapshot snapshot, String prevKey) {}
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            public void onChildMoved(@NonNull DataSnapshot snapshot, String prevKey) {}
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showMessage(ChatMessage message) {
        TextView tv = new TextView(this);

        tv.setText(message.getMessage());
        tv.setTextSize(16);
        tv.setPadding(24, 16, 24, 16);
        tv.setMaxWidth(800);
        tv.setTextColor(getResources().getColor(android.R.color.black));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );


        if (message.getSender().equalsIgnoreCase(senderRole)) {
            params.gravity = Gravity.END;
            params.setMargins(100, 8, 0, 8);
            tv.setBackgroundResource(R.drawable.bg_bubble_sent);
        } else {
            params.gravity = Gravity.START;
            params.setMargins(0, 8, 100, 8);
            tv.setBackgroundResource(R.drawable.bg_bubble_received);
        }

        tv.setLayoutParams(params);
        messageContainer.addView(tv);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
