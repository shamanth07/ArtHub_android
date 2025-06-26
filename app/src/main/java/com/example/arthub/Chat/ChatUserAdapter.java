package com.example.arthub.Chat;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.arthub.R;
import java.util.List;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.VH> {

    interface OnUserClickListener {
        void onUserClick(ChatUserListActivity.ChatUserItem user);
    }

    private final List<ChatUserListActivity.ChatUserItem> list;
    private final OnUserClickListener listener;

    public ChatUserAdapter(List<ChatUserListActivity.ChatUserItem> l, OnUserClickListener c) {
        list = l; listener = c;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int i) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_admin_chat_user, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        ChatUserListActivity.ChatUserItem u = list.get(i);
        h.txtEmail.setText(u.email);
        h.txtRole.setText("Role: " + u.role);
        h.redDot.setVisibility(u.unreadCount > 0 ? View.VISIBLE : View.INVISIBLE);
        h.itemView.setOnClickListener(v -> listener.onUserClick(u));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtEmail, txtRole;
        ImageView redDot;
        VH(@NonNull View v) {
            super(v);
            txtEmail = v.findViewById(R.id.emailTextView);
            txtRole = v.findViewById(R.id.roleTextView);
            redDot = v.findViewById(R.id.redDotView);
        }
    }
}
