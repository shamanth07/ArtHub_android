package com.example.arthub.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class ChatUserListActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private ChatUserAdapter adapter;
    private List<ChatUserItem> users = new ArrayList<>();
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user_list);

        rvUsers = findViewById(R.id.rvUsers);
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new ChatUserAdapter(users, new ChatUserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(ChatUserItem user) {
                Intent intent = new Intent(ChatUserListActivity.this, ChatActivity.class);
                intent.putExtra("userId", user.userId);
                intent.putExtra("role", user.role);
                startActivity(intent);
            }
        });

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        fetchChatUsers();
    }

    private void fetchChatUsers() {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot chatSnap : snapshot.getChildren()) {
                    String chatId = chatSnap.getKey();
                    if (chatId == null || !chatId.contains(currentUid)) continue;

                    String[] parts = chatId.split("_");
                    if (parts.length != 2) continue;

                    String otherId = parts[0].equals(currentUid) ? parts[1] : parts[0];

                    int unread = countUnread(chatSnap.child("messages"), otherId);

                    fetchUserInfo(otherId, unread);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private int countUnread(DataSnapshot msgsSnap, String otherId) {
        int count = 0;
        for (DataSnapshot msgSnap : msgsSnap.getChildren()) {
            Boolean isRead = msgSnap.child("isRead").getValue(Boolean.class);
            String sender = msgSnap.child("sender").getValue(String.class);
            if (otherId.equals(sender) && (isRead == null || !isRead)) count++;
        }
        return count;
    }

    private void fetchUserInfo(String uid, final int unreadCount) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                String email = snap.child("email").getValue(String.class);
                String role = snap.child("role").getValue(String.class);
                users.add(new ChatUserItem(uid, email, role, unreadCount));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    static class ChatUserItem {
        final String userId, email, role;
        final int unreadCount;
        ChatUserItem(String u, String e, String r, int c) {
            userId = u; email = e; role = r; unreadCount = c;
        }
    }
}
