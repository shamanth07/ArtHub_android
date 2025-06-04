package com.example.arthub.Visitor;

import android.content.Context;
import android.content.Intent;
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


        loadCommentCount(artworkId, holder);

        Glide.with(context)
                .load(artwork.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.artworkImage);

        String artistId = artwork.getArtistId();
        if (artistNameCache.containsKey(artistId)) {
            holder.artistName.setText( artistNameCache.get(artistId));
        } else {
            holder.artistName.setText("unknown");
            fetchArtistName(artistId, holder);
        }


        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child("likes")
                .child(currentUserId);
        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean liked = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                holder.likeIcon.setImageResource(liked ? R.drawable.like : R.drawable.unliked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.likeIcon.setOnClickListener(v -> toggleLike(artworkId, currentUserId, holder));

        holder.commentIcon.setOnClickListener(v -> showCommentBottomSheet(artworkId, holder));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArtworkDetailForVisitor.class);
            intent.putExtra("artworkId", artwork.getId());
            context.startActivity(intent);
        });

    }

    private void toggleLike(String artworkId, String userId, ViewHolder holder) {
        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child("likes")
                .child(userId);

        DatabaseReference artworkRef = FirebaseDatabase.getInstance().getReference("artworks").child(artworkId);

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean liked = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                if (liked) {

                    likeRef.removeValue();
                    artworkRef.child("likes").runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer currentLikes = currentData.getValue(Integer.class);
                            if (currentLikes == null) currentLikes = 0;
                            currentData.setValue(Math.max(currentLikes - 1, 0));
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                            holder.likeIcon.setImageResource(R.drawable.unliked);
                            Integer newLikes = snapshot != null ? snapshot.getValue(Integer.class) : 0;
                            holder.likeCount.setText(String.valueOf(newLikes != null ? newLikes : 0));
                        }
                    });
                } else {

                    likeRef.setValue(true);
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
                            holder.likeIcon.setImageResource(R.drawable.like);
                            Integer newLikes = snapshot != null ? snapshot.getValue(Integer.class) : 0;
                            holder.likeCount.setText(String.valueOf(newLikes != null ? newLikes : 0));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void showCommentBottomSheet(String artworkId, ViewHolder holder) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_comment, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText commentInput = sheetView.findViewById(R.id.commentInput);
        ImageView sendBtn = sheetView.findViewById(R.id.sendCommentBtn);
        ViewGroup commentsContainer = sheetView.findViewById(R.id.commentsContainer);


        loadAllComments(artworkId, commentsContainer);

        sendBtn.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            addCommentToFirebase(artworkId, commentText, bottomSheetDialog, holder, commentsContainer,commentInput);
        });

        bottomSheetDialog.show();
    }

    private void loadAllComments(String artworkId, ViewGroup commentsContainer) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child("comments");

        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsContainer.removeAllViews();

                for (DataSnapshot commentSnap : snapshot.getChildren()) {
                    String comment = commentSnap.child("comment").getValue(String.class);
                    Long timestamp = commentSnap.child("timestamp").getValue(Long.class);
                    String userId = commentSnap.child("userId").getValue(String.class);

                    View commentView = LayoutInflater.from(context).inflate(R.layout.item_comment, commentsContainer, false);
                    TextView commentText = commentView.findViewById(R.id.commentText);
                    TextView commentTime = commentView.findViewById(R.id.commentTime);
                    TextView commentEmail = commentView.findViewById(R.id.commentEmail);

                    commentText.setText(comment != null ? comment : "");
                    if (timestamp != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, h:mm a");
                        commentTime.setText(sdf.format(new java.util.Date(timestamp)));
                    } else {
                        commentTime.setText("");
                    }


                    if (userId != null) {
                        fetchUserEmail(userId, commentEmail);
                    } else {
                        commentEmail.setText("Unknown");
                    }

                    commentsContainer.addView(commentView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchUserEmail(String userId, TextView commentEmail) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.child("email").getValue(String.class);
                commentEmail.setText(email != null ? email : "Unknown");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                commentEmail.setText("Unknown");
            }
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
    private void loadCommentCount(String artworkId, ViewHolder holder) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child("comments");

        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                holder.commentCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.commentCount.setText("0");
            }
        });
    }


    private void addCommentToFirebase(String artworkId, String commentText, BottomSheetDialog dialog, ViewHolder holder, ViewGroup commentsContainer,EditText commentInput) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        DatabaseReference commentRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child("comments")
                .push();

        Comment comment = new Comment(commentText, timestamp, userId);

        commentRef.setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Comment posted", Toast.LENGTH_SHORT).show();
                commentInput.setText("");
                // Refresh comments list
                loadAllComments(artworkId, commentsContainer);
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
