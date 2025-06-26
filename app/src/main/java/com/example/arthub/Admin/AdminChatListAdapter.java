package com.example.arthub.Admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.arthub.Chat.ChatUser;
import com.example.arthub.R;

import java.util.List;

public class AdminChatListAdapter extends ArrayAdapter<ChatUser> {

    public AdminChatListAdapter(@NonNull Context context, @NonNull List<ChatUser> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ChatUser user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_admin_chat_user, parent, false);
        }

        TextView emailText = convertView.findViewById(R.id.emailTextView);
        TextView roleText = convertView.findViewById(R.id.roleTextView);
        ImageView dot = convertView.findViewById(R.id.redDotView);

        if (user != null) {
            emailText.setText(user.getEmail());
            roleText.setText("Role: " + user.getRole());
            dot.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
