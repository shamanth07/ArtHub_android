package com.example.arthub.Artist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

public class AcceptedArtistAdapter extends RecyclerView.Adapter<AcceptedArtistAdapter.ArtistViewHolder> {

    private Context context;
    private List<AcceptedArtist> artistList;
    private String eventId;

    public AcceptedArtistAdapter(Context context, List<AcceptedArtist> artistList, String eventId) {
        this.context = context;
        this.artistList = artistList;
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_artist_card, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        AcceptedArtist artist = artistList.get(position);
        holder.artistName.setText(artist.getArtistName());

        String visitorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                .getReference("artistLikesInEvents")
                .child(eventId)
                .child(artist.getArtistId())
                .child(visitorId);

        // 🔁 Fetch and set initial like button state
        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.likeButton.setImageResource(R.drawable.liked);
                } else {
                    holder.likeButton.setImageResource(R.drawable.unliked);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // ✅ Fetch and display initial like count
        updateLikeCount(artist, holder);

        // Toggle like on click
        holder.likeButton.setOnClickListener(v -> {
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        likeRef.removeValue().addOnSuccessListener(aVoid -> {
                            holder.likeButton.setImageResource(R.drawable.unliked);
                            updateLikeCount(artist, holder);
                        });
                    } else {
                        likeRef.setValue(true).addOnSuccessListener(aVoid -> {
                            holder.likeButton.setImageResource(R.drawable.liked);
                            updateLikeCount(artist, holder);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateLikeCount(AcceptedArtist artist, ArtistViewHolder holder) {
        DatabaseReference artistLikesRef = FirebaseDatabase.getInstance()
                .getReference("artistLikesInEvents")
                .child(eventId)
                .child(artist.getArtistId());

        artistLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int likes = (int) snapshot.getChildrenCount();
                artist.setLikeCount(likes);
                holder.likeCount.setText("Likes: " + likes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        ImageView artistImage, likeButton;
        TextView artistName, likeCount;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            artistImage = itemView.findViewById(R.id.artistImage);
            likeButton = itemView.findViewById(R.id.likeButton);
            artistName = itemView.findViewById(R.id.artistName);
            likeCount = itemView.findViewById(R.id.likeCount);
        }
    }
}
