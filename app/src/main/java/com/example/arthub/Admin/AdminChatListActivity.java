package com.example.arthub.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arthub.Chat.ChatUser;
import com.example.arthub.R;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class AdminChatListActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseReference chatsRef, usersRef;
    private ArrayList<ChatUser> userList;
    private com.example.arthub.Admin.AdminChatListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        listView = findViewById(R.id.listView);
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        userList = new ArrayList<>();
        adapter = new com.example.arthub.Admin.AdminChatListAdapter(this, userList);
        listView.setAdapter(adapter);


        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String userId = chatSnapshot.getKey();
                    if (userId == null) continue;

                    usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String email = userSnapshot.child("email").getValue(String.class);
                            String role = userSnapshot.child("role").getValue(String.class);
                            if (email != null && role != null) {
                                userList.add(new ChatUser(userId, email, role));
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });


        listView.setOnItemClickListener((parent, view, position, id) -> {
            ChatUser selectedUser = userList.get(position);
            Intent intent = new Intent(AdminChatListActivity.this, AdminChatActivity.class);
            intent.putExtra("userId", selectedUser.getUserId());
            startActivity(intent);
        });
    }
}
