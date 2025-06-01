package com.example.arthub.Visitor;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.arthub.Artist.Artwork;
import com.example.arthub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ArtworkDetailForVisitor extends AppCompatActivity {

    private ImageView artworkImage, likeIcon, commentIcon;
    private TextView title, artistName, likeCount, commentCount, description, instagramLink, websiteLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artwork_detail_for_visitor);

        String artworkId = getIntent().getStringExtra("artworkId");

        artworkImage = findViewById(R.id.artworkImage);
        title = findViewById(R.id.artworkTitle);
        artistName = findViewById(R.id.artistName);
        likeCount = findViewById(R.id.likeCount);
        commentCount = findViewById(R.id.commentCount);
        likeIcon = findViewById(R.id.likeIcon);
        commentIcon = findViewById(R.id.commentIcon);
        description = findViewById(R.id.artworkDescription);
        instagramLink = findViewById(R.id.instagramLink);
        websiteLink = findViewById(R.id.websiteLink);

        loadArtworkDetails(artworkId);
    }

    private void loadArtworkDetails(String artworkId) {
        FirebaseDatabase.getInstance().getReference("artworks").child(artworkId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Artwork artwork = snapshot.getValue(Artwork.class);
                        if (artwork != null) {
                            title.setText(artwork.getTitle());
                            description.setText(artwork.getDescription());
                            likeCount.setText(String.valueOf(artwork.getLikes()));
                            commentCount.setText(String.valueOf(artwork.getComments()));

                            Glide.with(ArtworkDetailForVisitor.this)
                                    .load(artwork.getImageUrl())
                                    .into(artworkImage);

                            fetchArtistDetails(artwork.getArtistId());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void fetchArtistDetails(String artistId) {
        FirebaseDatabase.getInstance().getReference("artists").child(artistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = snapshot.child("email").getValue(String.class);
                        String name = (email != null) ? email.split("@")[0] : "Unknown";
                        artistName.setText(name);

                        String instagram = snapshot.child("socialLinks").child("instagram").getValue(String.class);
                        String website = snapshot.child("socialLinks").child("website").getValue(String.class);

                        instagramLink.setText(instagram != null ? instagram : "N/A");
                        websiteLink.setText(website != null ? website : "N/A");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
