package com.example.arthub.Visitor;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.arthub.Visitor.Comment;


import com.bumptech.glide.Glide;
import com.example.arthub.Artist.Artwork;
import com.example.arthub.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class VisitorArtworkAdapter extends RecyclerView.Adapter<VisitorArtworkAdapter.ViewHolder> {

    private Context context;
    private List<Artwork> artworkList;
    private HashMap<String, String> artistNameCache = new HashMap<>();

    public VisitorArtworkAdapter(Context context, List<Artwork> artworkList) {
        this.context = context;
        this.artworkList = artworkList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_visitor_artwork, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artwork artwork = artworkList.get(position);
        String artworkId = artwork.getId();
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        holder.artworkTitle.setText(artwork.getTitle());
        holder.likeCount.setText(String.valueOf(artwork.getLikes()));

        // Load comment count from Firebase
        loadCommentCount(artworkId, holder);

        Glide.with(context)
                .load(artwork.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.artworkImage);

        String artistId = artwork.getArtistId();
        if (artistNameCache.containsKey(artistId)) {
            holder.artistName.setText("By: " + artistNameCache.get(artistId));
        } else {
            holder.artistName.setText("By: unknown");
            fetchArtistName(artistId, holder);
        }


        holder.likeIcon.setOnClickListener(v -> toggleLike(artworkId, currentUserId, holder));


        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes").child(artworkId).child(currentUserId);
        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean liked = snapshot.exists();
                holder.likeIcon.setImageResource(liked ? R.drawable.likes : R.drawable.unliked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        holder.commentIcon.setOnClickListener(v -> showCommentBottomSheet(artworkId, holder));
    }

    private void loadCommentCount(String artworkId, ViewHolder holder) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child("comments");

        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long commentCount = snapshot.getChildrenCount();
                holder.commentCount.setText(String.valueOf(commentCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.commentCount.setText("0");
            }
        });
    }

    private void toggleLike(String artworkId, String userId, ViewHolder holder) {
        DatabaseReference interactionRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child(userId)
                .child("likes");

        DatabaseReference artworkRef = FirebaseDatabase.getInstance().getReference("artworks").child(artworkId);

        interactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean liked = snapshot.exists();
                if (liked) {
                    interactionRef.removeValue();
                    artworkRef.child("likes").runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer currentLikes = currentData.getValue(Integer.class);
                            if (currentLikes == null) currentLikes = 0;
                            currentData.setValue(currentLikes - 1);
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                            holder.likeIcon.setImageResource(R.drawable.unliked);
                            holder.likeCount.setText(String.valueOf(snapshot.getValue(Integer.class)));
                        }
                    });
                } else {
                    interactionRef.setValue(true);
                    artworkRef.child("likes").runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer currentLikes = currentData.getValue(Integer.class);
                            if (currentLikes == null) currentLikes = 0;
                            currentData.setValue(currentLikes + 1);
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                            holder.likeIcon.setImageResource(R.drawable.likes);
                            holder.likeCount.setText(String.valueOf(snapshot.getValue(Integer.class)));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void fetchArtistName(String artistId, ViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(artistId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.child("email").getValue(String.class);
                if (email != null) {
                    String name = email.split("@")[0];
                    artistNameCache.put(artistId, name);
                    holder.artistName.setText("By: " + name);
                } else {
                    holder.artistName.setText("By: Unknown");
                }
            }

            public void onCancelled(@NonNull DatabaseError error) {
                holder.artistName.setText("By: Unknown");
            }
        });
    }

    private void showCommentBottomSheet(String artworkId, ViewHolder holder) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_comment, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText commentInput = sheetView.findViewById(R.id.commentInput);
        ImageView sendBtn = sheetView.findViewById(R.id.sendCommentBtn);
        ViewGroup commentsContainer = sheetView.findViewById(R.id.commentsContainer);

        sendBtn.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            addCommentToFirebase(artworkId, commentText, bottomSheetDialog, holder);
        });

        // Fetch all comments
        DatabaseReference artworkRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId);

        artworkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsContainer.removeAllViews();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String userId = userSnap.getKey();
                    String email = userSnap.child("email").getValue(String.class);

                    for (DataSnapshot commentSnap : userSnap.child("comments").getChildren()) {
                        String comment = commentSnap.child("comment").getValue(String.class);
                        long timestamp = commentSnap.child("timestamp").getValue(Long.class);

                        View commentView = LayoutInflater.from(context).inflate(R.layout.item_comment, null);
                        TextView commentText = commentView.findViewById(R.id.commentText);
                        TextView commentEmail = commentView.findViewById(R.id.commentEmail);
                        TextView commentTime = commentView.findViewById(R.id.commentTime);

                        commentText.setText(comment);
                        commentEmail.setText(email != null ? email : "Unknown");

                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, h:mm a");
                        commentTime.setText(sdf.format(new java.util.Date(timestamp)));

                        commentsContainer.addView(commentView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        bottomSheetDialog.show();
    }


    private void addCommentToFirebase(String artworkId, String commentText, BottomSheetDialog dialog, ViewHolder holder) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        DatabaseReference commentRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child(userId)
                .child("comments")
                .push();

        Comment comment = new Comment(commentText, timestamp, userId);

        commentRef.setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Comment posted", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadCommentCount(artworkId, holder);
            } else {
                Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return artworkList.size();
    }


    public void updateList(List<Artwork> newArtworkList) {
        artworkList = newArtworkList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView artworkImage, likeIcon, commentIcon;
        TextView artworkTitle, artistName, likeCount, commentCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artworkImage = itemView.findViewById(R.id.artworkImage);
            artworkTitle = itemView.findViewById(R.id.artworkTitle);
            artistName = itemView.findViewById(R.id.artistName);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            commentIcon = itemView.findViewById(R.id.commentIcon);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
        }
    }



}
