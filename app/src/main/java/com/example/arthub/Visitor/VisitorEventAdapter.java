package com.example.arthub.Visitor;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arthub.Admin.Event;
import com.example.arthub.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class VisitorEventAdapter extends RecyclerView.Adapter<VisitorEventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;

    public VisitorEventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_visitor_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.titleTextView.setText(event.getTitle());

        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(event.getEventDate()));
        holder.dateTextView.setText(formattedDate);

        Glide.with(context).load(event.getBannerImageUrl()).into(holder.bannerImageView);

        holder.visitorcomments.setOnClickListener(v -> showCommentBottomSheet(event.getEventId()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VisitorEventDetail.class);
            intent.putExtra("event", event);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateList(List<Event> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImageView, visitorcomments;
        TextView titleTextView, dateTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImageView = itemView.findViewById(R.id.imageBanner);
            titleTextView = itemView.findViewById(R.id.textTitle);
            dateTextView = itemView.findViewById(R.id.textDate);
            visitorcomments = itemView.findViewById(R.id.visitorcomments);
        }
    }

    private void showCommentBottomSheet(String eventId) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_comment, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText commentInput = sheetView.findViewById(R.id.commentInput);
        ImageView sendBtn = sheetView.findViewById(R.id.sendCommentBtn);
        ViewGroup commentsContainer = sheetView.findViewById(R.id.commentsContainer);

        loadAllEventComments(eventId, commentsContainer);

        sendBtn.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            addEventCommentToFirebase(eventId, commentText, commentsContainer, commentInput);
        });

        bottomSheetDialog.show();
    }

    private void loadAllEventComments(String eventId, ViewGroup commentsContainer) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                .getReference("eventComments")
                .child(eventId);

        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsContainer.removeAllViews();

                for (DataSnapshot commentSnap : snapshot.getChildren()) {
                    String comment = commentSnap.child("comment").getValue(String.class);
                    Long timestamp = commentSnap.child("timestamp").getValue(Long.class);
                    String userEmail = commentSnap.child("userEmail").getValue(String.class);

                    View commentView = LayoutInflater.from(context).inflate(R.layout.item_comment, commentsContainer, false);
                    TextView commentText = commentView.findViewById(R.id.commentText);
                    TextView commentTime = commentView.findViewById(R.id.commentTime);
                    TextView commentEmail = commentView.findViewById(R.id.commentEmail);
                    LinearLayout replyContainer = commentView.findViewById(R.id.replyContainer);

                    View replyButton = commentView.findViewById(R.id.replyButton);
                    if (replyButton != null) {
                        replyButton.setVisibility(View.GONE);
                    }

                    commentText.setText(comment != null ? comment : "");
                    commentTime.setText(timestamp != null
                            ? new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(new Date(timestamp))
                            : "");
                    commentEmail.setText(userEmail != null ? userEmail : "Unknown");

                    // Load replies under this comment
                    loadEventReplies(eventId, commentSnap.getKey(), replyContainer);

                    commentsContainer.addView(commentView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addEventCommentToFirebase(String eventId, String commentText, ViewGroup commentsContainer, EditText input) {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("eventComments")
                .child(eventId).push();

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("userEmail", userEmail);
        commentData.put("comment", commentText);
        commentData.put("timestamp", System.currentTimeMillis());

        commentRef.setValue(commentData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                input.setText("");
                loadAllEventComments(eventId, commentsContainer);
            } else {
                Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEventReplies(String eventId, String commentId, LinearLayout replyContainer) {
        DatabaseReference repliesRef = FirebaseDatabase.getInstance()
                .getReference("eventComments")
                .child(eventId)
                .child(commentId)
                .child("replies");

        repliesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                replyContainer.removeAllViews();

                for (DataSnapshot replySnap : snapshot.getChildren()) {
                    String replyText = replySnap.child("comment").getValue(String.class);
                    Long timestamp = replySnap.child("timestamp").getValue(Long.class);
                    String userEmail = replySnap.child("userEmail").getValue(String.class);

                    View replyView = LayoutInflater.from(context).inflate(R.layout.item_reply, replyContainer, false);
                    TextView replyContent = replyView.findViewById(R.id.replyText);
                    TextView replyTime = replyView.findViewById(R.id.replyTime);
                    TextView replyEmail = replyView.findViewById(R.id.replyUser);

                    replyContent.setText(replyText != null ? replyText : "");
                    replyTime.setText(timestamp != null
                            ? new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(new Date(timestamp))
                            : "");
                    replyEmail.setText(userEmail != null ? userEmail : "Unknown");

                    replyContainer.addView(replyView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load replies", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
