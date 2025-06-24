package com.example.arthub.Admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arthub.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventTitle.setText(event.getTitle());

        String dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(new Date(event.getEventDate()));
        holder.eventDate.setText(dateStr);
        holder.eventTime.setText(event.getTime());

        Glide.with(context)
                .load(event.getBannerImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.eventImage);

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditEvent.class);
            intent.putExtra("event", event);
            context.startActivity(intent);
        });

        holder.btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (context instanceof AdminDashboard) {
                            ((AdminDashboard) context).deleteEvent(event.getEventId());
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        holder.admincomments.setOnClickListener(v -> showEventCommentsBottomSheet(event.getEventId()));
    }

    private void showEventCommentsBottomSheet(String eventId) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_comment, null);
        dialog.setContentView(view);

        ViewGroup commentsContainer = view.findViewById(R.id.commentsContainer);

        View commentInput = view.findViewById(R.id.commentInput);
        View sendCommentBtn = view.findViewById(R.id.sendCommentBtn);
        if (commentInput != null) commentInput.setVisibility(View.GONE);
        if (sendCommentBtn != null) sendCommentBtn.setVisibility(View.GONE);

        DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                .getReference("eventComments")
                .child(eventId);

        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsContainer.removeAllViews();
                for (DataSnapshot commentSnap : snapshot.getChildren()) {
                    String commentId = commentSnap.getKey();
                    String commentText = commentSnap.child("comment").getValue(String.class);
                    String userEmail = commentSnap.child("userEmail").getValue(String.class);

                    View commentView = LayoutInflater.from(context)
                            .inflate(R.layout.item_comment, commentsContainer, false);

                    TextView commentTextView = commentView.findViewById(R.id.commentText);
                    TextView commentEmail = commentView.findViewById(R.id.commentEmail);
                    TextView replyButton = commentView.findViewById(R.id.replyButton);
                    LinearLayout replyContainer = commentView.findViewById(R.id.replyContainer);

                    commentTextView.setText(commentText);
                    commentEmail.setText(userEmail);

                    loadReplies(eventId, commentId, replyContainer);

                    replyButton.setOnClickListener(v -> showReplyDialog(eventId, commentId, replyContainer));

                    commentsContainer.addView(commentView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        dialog.show();
    }

    private void loadReplies(String eventId, String commentId, LinearLayout container) {
        DatabaseReference replyRef = FirebaseDatabase.getInstance()
                .getReference("eventComments")
                .child(eventId)
                .child(commentId)
                .child("replies");

        replyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();
                for (DataSnapshot replySnap : snapshot.getChildren()) {
                    String replyText = replySnap.child("comment").getValue(String.class);
                    String userEmail = replySnap.child("userEmail").getValue(String.class);

                    View replyView = LayoutInflater.from(context)
                            .inflate(R.layout.item_reply, container, false);

                    TextView replyTextView = replyView.findViewById(R.id.replyText);
                    TextView replyUser = replyView.findViewById(R.id.replyUser);

                    replyTextView.setText(replyText);
                    replyUser.setText(userEmail);

                    container.addView(replyView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showReplyDialog(String eventId, String commentId, LinearLayout container) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_item_reply, null);
        dialog.setContentView(view);

        EditText input = view.findViewById(R.id.replyInput);
        ImageView sendBtn = view.findViewById(R.id.sendReplyBtn);

        sendBtn.setOnClickListener(v -> {
            String replyText = input.getText().toString().trim();
            if (replyText.isEmpty()) {
                Toast.makeText(context, "Reply can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String userEmail = "admin@example.com"; // Static admin email
            long timestamp = System.currentTimeMillis();

            DatabaseReference replyRef = FirebaseDatabase.getInstance()
                    .getReference("eventComments")
                    .child(eventId)
                    .child(commentId)
                    .child("replies")
                    .push();

            replyRef.child("comment").setValue(replyText);
            replyRef.child("userEmail").setValue(userEmail);
            replyRef.child("timestamp").setValue(timestamp);

            dialog.dismiss();
            loadReplies(eventId, commentId, container);
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventDate, eventTime, admincomments;
        ImageButton btnEdit, btnCancel;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            admincomments = itemView.findViewById(R.id.admincomments);
        }
    }
}
