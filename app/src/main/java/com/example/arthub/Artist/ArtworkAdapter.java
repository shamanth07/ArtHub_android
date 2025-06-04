package com.example.arthub.Artist;

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
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        assert user != null;
        String useremail = user.getEmail();
        String artistName = useremail != null ? useremail.split("@")[0] : "Unknown";
        holder.artistName.setText(artistName);


        holder.likeIcon.setOnClickListener(v -> listener.onLikeClick(artwork));


        holder.deleteIcon.setOnClickListener(v -> listener.onDeleteClick(artwork));


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
        ImageView artworkImage, likeIcon, deleteIcon;
        TextView artworkTitle, artistName;

        public ArtworkViewHolder(@NonNull View itemView) {
            super(itemView);
            artworkImage = itemView.findViewById(R.id.artworkImage);
            artworkTitle = itemView.findViewById(R.id.artworktitle);
            artistName = itemView.findViewById(R.id.artistName);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }

    public interface OnArtworkActionListener {
        void onLikeClick(Artwork artwork);
        void onEditClick(Artwork artwork);
        void onDeleteClick(Artwork artwork);
    }
}
