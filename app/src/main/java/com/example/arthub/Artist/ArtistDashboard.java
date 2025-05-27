package com.example.arthub.Artist;




import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class ArtistDashboard extends AppCompatActivity {

    Button upldartwork;
    ImageView menuIcon;
    RecyclerView recyclerViewArtWorks;
    ArtworkAdapter adapter;
    List<Artwork> artworkList;

    String artworkId;

    DatabaseReference dbRef;
    FirebaseUser currentUser;


    private DatabaseReference artworksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_artist_dashboard);

        upldartwork = findViewById(R.id.upldartwork);
        menuIcon = findViewById(R.id.menuIcon);
        recyclerViewArtWorks = findViewById(R.id.recyclerViewArtWorks);


        artworksRef = FirebaseDatabase.getInstance().getReference("artworks");

        artworkList = new ArrayList<>();
        adapter = new ArtworkAdapter(this, artworkList, new ArtworkAdapter.OnArtworkActionListener() {
            @Override
            public void onLikeClick(Artwork artwork) {



                Toast.makeText(ArtistDashboard.this, "Liked: " + artwork.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(Artwork artwork) {
//                Intent intent = new Intent(ArtistDashboard.this, EditArtworkPage.class);
//                intent.putExtra("artworkId", artworkId);
//                startActivity(intent);
                Toast.makeText(ArtistDashboard.this, "Edit: " + artwork.getTitle(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onDeleteClick(Artwork artwork) {
                artworksRef.child(artwork.getId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ArtistDashboard.this, "Deleted: " + artwork.getTitle(), Toast.LENGTH_SHORT).show();
                        artworkList.remove(artwork);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ArtistDashboard.this, "Delete failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        recyclerViewArtWorks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewArtWorks.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference("artworks");

        if (currentUser != null) {
            fetchUserArtworks(currentUser.getUid());
        }

        upldartwork.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistDashboard.this, UploadArtwork.class);
            startActivity(intent);
        });

        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistDashboard.this, ArtistAccountPage.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchUserArtworks(String artistId) {
        Query query = dbRef.orderByChild("artistId").equalTo(artistId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                artworkList.clear();
                for (DataSnapshot artworkSnapshot : snapshot.getChildren()) {
                    Artwork artwork = artworkSnapshot.getValue(Artwork.class);
                    if (artwork != null) {
                        artworkList.add(artwork);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArtistDashboard.this, "Failed to load artworks: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

