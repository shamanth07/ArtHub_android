package com.example.arthub.Visitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arthub.Artist.Artwork;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class VisitorArtworkAdapter extends RecyclerView.Adapter<VisitorArtworkAdapter.ViewHolder> {

    private Context context;
    private List<Artwork> artworkList;

    public VisitorArtworkAdapter(Context context, List<Artwork> artworkList) {
        this.context = context;
        this.artworkList = artworkList;
    }

    @NonNull
    @Override
    public VisitorArtworkAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_visitor_artwork, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitorArtworkAdapter.ViewHolder holder, int position) {
        Artwork artwork = artworkList.get(position);

        holder.artworkTitle.setText(artwork.getTitle());
        holder.likeCount.setText(String.valueOf(artwork.getLikes()));
        holder.commentCount.setText(String.valueOf(artwork.getComments()));


        Glide.with(context)
                .load(artwork.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.artworkImage);

        // Fetch artist name using artistId
        String artistId = artwork.getArtistId();
        DatabaseReference artistRef = FirebaseDatabase.getInstance().getReference("users").child(artistId);

        artistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    if (role != null && role.equalsIgnoreCase("artist")) {
                        String email = snapshot.child("email").getValue(String.class);
                        if (email != null && email.contains("@")) {
                            String extractedName = email.split("@")[0];
                            holder.artistName.setText(extractedName);
                        } else {
                            holder.artistName.setText("Unknown Artist");
                        }
                    } else {
                        holder.artistName.setText("Unknown Artist");
                    }
                } else {
                    holder.artistName.setText("Unknown Artist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.artistName.setText("Unknown Artist");
            }
        });



    }
    public void updateList(List<Artwork> filteredList) {
        this.artworkList = filteredList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return artworkList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView artworkImage, likeIcon, commentIcon;
        TextView artworkTitle, artistName, likeCount, commentCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artworkImage = itemView.findViewById(R.id.artworkImage);
            artworkTitle = itemView.findViewById(R.id.artworktitle);
            artistName = itemView.findViewById(R.id.artistName);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            commentIcon = itemView.findViewById(R.id.commentIcon);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
        }
    }
}
