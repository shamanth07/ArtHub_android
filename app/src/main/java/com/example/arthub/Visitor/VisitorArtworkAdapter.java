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

import java.util.ArrayList;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_visitor_artwork, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artwork artwork = artworkList.get(position);

        holder.artworkTitle.setText(artwork.getTitle());


        String artistEmail = artwork.getArtistId();
        String displayName = artistEmail.split("@")[0];
        holder.artistName.setText(displayName);
        holder.likeCount.setText(String.valueOf(artwork.getLikes()));
        holder.commentCount.setText(String.valueOf(artwork.getComments()));

        Glide.with(context)
                .load(artwork.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.artworkImage);


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArtworkDetailForVisitor.class);
            intent.putExtra("artworkId", artwork.getId());
            context.startActivity(intent);
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
