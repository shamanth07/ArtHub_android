package com.example.arthub.Visitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arthub.Admin.AdminChatListActivity;
import com.example.arthub.Chat.ChatActivity;
import com.example.arthub.R;

public class Settings extends AppCompatActivity {

    private TextView darkmode, changelanguage, livesupport;

    private String userRole = "visitor";
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        darkmode = findViewById(R.id.darkmode);
        changelanguage = findViewById(R.id.changelanguage);
        livesupport = findViewById(R.id.livesupport);

        if (getIntent() != null) {
            String roleExtra = getIntent().getStringExtra("role");
            if (roleExtra != null && !roleExtra.trim().isEmpty()) {
                userRole = roleExtra.trim().toLowerCase();
            }

            userId = getIntent().getStringExtra("userId");
        }

        livesupport.setOnClickListener(v -> {
            if ("admin".equals(userRole)) {
                // Direct admin chat list screen
                Intent intent = new Intent(Settings.this, AdminChatListActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else {
                if (userId == null || userId.isEmpty()) return;

                Intent intent = new Intent(Settings.this, ChatActivity.class);
                intent.putExtra("role", userRole);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }
}
