package com.example.arthub.Visitor;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        holder.artworkTitle.setText(artwork.getTitle());
        holder.likeCount.setText(String.valueOf(artwork.getLikes()));
        holder.commentCount.setText(String.valueOf(artwork.getComments()));


        Glide.with(context)
                .load(artwork.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.artworkImage);


        String artistId = artwork.getArtistId();
        if (artistNameCache.containsKey(artistId)) {
            holder.artistName.setText(artistNameCache.get(artistId));
        } else {
            holder.artistName.setText("unkonwn artist");
            fetchArtistName(artistId, holder);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArtworkDetailForVisitor.class);
            intent.putExtra("artworkId", artwork.getId());
            context.startActivity(intent);
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

    @Override
    public int getItemCount() {
        return artworkList.size();
    }

    public void updateList(List<Artwork> newList) {
        this.artworkList = new ArrayList<>(newList);
        notifyDataSetChanged();
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
