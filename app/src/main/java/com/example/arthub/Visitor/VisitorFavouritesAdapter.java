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
import com.example.arthub.R;

import java.util.List;
import java.util.Map;

public class VisitorFavouritesAdapter extends RecyclerView.Adapter<VisitorFavouritesAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, String>> list;

    public VisitorFavouritesAdapter(Context context, List<Map<String, String>> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VisitorFavouritesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favourite_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitorFavouritesAdapter.ViewHolder holder, int position) {
        Map<String, String> data = list.get(position);

        String name = data.get("name");
        String bio = data.get("bio");
        String Email = data.get("email");
        String profileImage = data.get("imageUrl");

        holder.textName.setText(name != null ? name : "Unknown");
        holder.textBio.setText(bio != null ? bio : "");
        holder.emailid.setText(Email != null ? Email : "N/A");

        Glide.with(context)
                .load(profileImage)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imageProfile);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textBio, emailid;
        ImageView imageProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textBio = itemView.findViewById(R.id.textBio);
            emailid = itemView.findViewById(R.id.emailid);
            imageProfile = itemView.findViewById(R.id.imageArtwork);
        }
    }
}
