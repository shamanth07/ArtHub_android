package com.example.arthub.Artist;

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
import com.example.arthub.R;
import com.example.arthub.Visitor.Comment;
import com.example.arthub.Visitor.Reply;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.List;

public class ArtworkAdapter extends RecyclerView.Adapter<ArtworkAdapter.ArtworkViewHolder> {

    private Context context;
    private List<Artwork> artworkList;
    private OnArtworkActionListener listener;

    public ArtworkAdapter(Context context, List<Artwork> artworkList, OnArtworkActionListener listener) {
        this.context = context;
        this.artworkList = artworkList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArtworkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_artwork, parent, false);
        return new ArtworkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtworkViewHolder holder, int position) {
        Artwork artwork = artworkList.get(position);

        Glide.with(context)
                .load(artwork.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.artworkImage);

        holder.artworkTitle.setText(artwork.getTitle());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artwork.getId())
                .child("comments");

        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long commentCount = snapshot.getChildrenCount();
                holder.CommentCountText.setText(String.valueOf(commentCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.CommentCountText.setText("0");
            }
        });

        assert user != null;
        String useremail = user.getEmail();
        String artistName = useremail != null ? useremail.split("@")[0] : "Unknown";
        holder.artistName.setText(artistName);
        holder.CommentIcon.setOnClickListener(v -> showCommentBottomSheet(artwork.getId(), holder));


        DatabaseReference likesRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artwork.getId())
                .child("likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long likeCount = snapshot.getChildrenCount();
                holder.likeCountText.setText(String.valueOf(likeCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.likeCountText.setText("0");
            }
        });

        holder.likeIcon.setOnClickListener(v -> listener.onLikeClick(artwork));
        holder.deleteIcon.setOnClickListener(v -> listener.onDeleteClick(artwork));
        holder.CommentIcon.setOnClickListener(v -> showCommentBottomSheet(artwork.getId(), holder));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditArtworkPage.class);
            intent.putExtra("artworkId", artwork.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return artworkList.size();
    }

    public static class ArtworkViewHolder extends RecyclerView.ViewHolder {
        ImageView artworkImage, likeIcon, deleteIcon, CommentIcon;
        TextView artworkTitle, artistName, likeCountText, CommentCountText;

        public ArtworkViewHolder(@NonNull View itemView) {
            super(itemView);
            artworkImage = itemView.findViewById(R.id.artworkImage);
            artworkTitle = itemView.findViewById(R.id.artworktitle);
            artistName = itemView.findViewById(R.id.artistName);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
            likeCountText = itemView.findViewById(R.id.likeCountText);
            CommentIcon = itemView.findViewById(R.id.CommentIcon);
            CommentCountText = itemView.findViewById(R.id.CommentCountText);
        }
    }

    private void showCommentBottomSheet(String artworkId, ArtworkViewHolder holder) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_comment, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText commentInput = sheetView.findViewById(R.id.commentInput);
        ImageView sendBtn = sheetView.findViewById(R.id.sendCommentBtn);
        ViewGroup commentsContainer = sheetView.findViewById(R.id.commentsContainer);

        loadAllComments(artworkId, commentsContainer);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.child("role").getValue(String.class);
                    if ("Artist".equals(role)) {

                        commentInput.setEnabled(false);
                        commentInput.setHint("Artists cannot comment");
                        sendBtn.setVisibility(View.GONE);
                    } else {
                        sendBtn.setOnClickListener(v -> {
                            String commentText = commentInput.getText().toString().trim();
                            if (TextUtils.isEmpty(commentText)) {
                                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            addCommentToFirebase(artworkId, commentText, commentsContainer, commentInput);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    sendBtn.setOnClickListener(v -> {
                        String commentText = commentInput.getText().toString().trim();
                        if (TextUtils.isEmpty(commentText)) {
                            Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        addCommentToFirebase(artworkId, commentText, commentsContainer, commentInput);
                    });
                }
            });
        }

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
                    String commentId = commentSnap.getKey();
                    String commentTextVal = commentSnap.child("comment").getValue(String.class);
                    Long timestamp = commentSnap.child("timestamp").getValue(Long.class);
                    String userId = commentSnap.child("userId").getValue(String.class);

                    // 🔍 Check role of user who commented
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String role = userSnapshot.child("role").getValue(String.class);
                            if ("Visitor".equals(role)) {
                                View commentView = LayoutInflater.from(context).inflate(R.layout.item_comment, commentsContainer, false);
                                TextView commentText = commentView.findViewById(R.id.commentText);
                                TextView commentEmail = commentView.findViewById(R.id.commentEmail);
                                TextView replyBtn = commentView.findViewById(R.id.replyButton);
                                LinearLayout replyContainer = commentView.findViewById(R.id.replyContainer);

                                commentText.setText(commentTextVal != null ? commentTextVal : "");
                                fetchUserEmail(userId, commentEmail);


                                replyBtn.setOnClickListener(v -> showReplyDialog(artworkId, commentId, replyContainer));


                                loadReplies(artworkId, commentId, replyContainer);

                                commentsContainer.addView(commentView);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: handle error
            }
        });
    }




    private void addCommentToFirebase(String artworkId, String commentText, ViewGroup container, EditText inputField) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        long timestamp = System.currentTimeMillis();

        DatabaseReference commentRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child("comments")
                .push();

        Comment commentObj = new Comment(commentText, timestamp, userId);
        commentRef.setValue(commentObj).addOnSuccessListener(unused -> {
            inputField.setText("");
            loadAllComments(artworkId, container);
        });
    }

    private void fetchUserEmail(String userId, TextView emailTextView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("email");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.getValue(String.class);
                emailTextView.setText(email != null ? email : "Unknown");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                emailTextView.setText("Unknown");
            }
        });
    }
    private void loadReplies(String artworkId, String commentId, LinearLayout repliesContainer) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentArtistUid = currentUser.getUid();

        DatabaseReference repliesRef = FirebaseDatabase.getInstance()
                .getReference("artworkInteractions")
                .child(artworkId)
                .child("comments")
                .child(commentId)
                .child("replies");

        repliesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                repliesContainer.removeAllViews();

                for (DataSnapshot replySnap : snapshot.getChildren()) {
                    String text = replySnap.child("comment").getValue(String.class);

                    String replyUserId = replySnap.child("userId").getValue(String.class);


                    if (currentArtistUid.equals(replyUserId)) {
                        View replyView = LayoutInflater.from(context).inflate(R.layout.item_reply, repliesContainer, false);
                        TextView replyText = replyView.findViewById(R.id.replyText);
                        TextView replyUser = replyView.findViewById(R.id.replyUser);

                        replyText.setText(text != null ? text : "");
                        fetchUserEmail(replyUserId, replyUser);
                        repliesContainer.addView(replyView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void showReplyDialog(String artworkId, String commentId, LinearLayout container) {
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

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

            String replyUserId = user.getUid();
            long timestamp = System.currentTimeMillis();

            DatabaseReference replyRef = FirebaseDatabase.getInstance()
                    .getReference("artworkInteractions")
                    .child(artworkId)
                    .child("comments")
                    .child(commentId)
                    .child("replies")
                    .push();

            Reply reply = new Reply(replyText, timestamp, replyUserId);
            replyRef.setValue(reply).addOnSuccessListener(unused -> {
                dialog.dismiss();
                loadReplies(artworkId, commentId, container);
            });
        });

        dialog.show();
    }


    public interface OnArtworkActionListener {
        void onLikeClick(Artwork artwork);
        void onEditClick(Artwork artwork);
        void onDeleteClick(Artwork artwork);
    }
}
