package com.example.arthub.Visitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.Artist.Artwork;
import com.example.arthub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VisitorArtDiscoveryPage extends AppCompatActivity {

    ImageView menuIcon;
    SearchView searchartist;
    RecyclerView recyclerViewvisitorartworks;

    VisitorArtworkAdapter adapter;
    List<Artwork> artworkList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_art_discovery_page);

        menuIcon = findViewById(R.id.menuIcon);
        searchartist = findViewById(R.id.searchartist);
        recyclerViewvisitorartworks = findViewById(R.id.recyclerViewvisitorartworks);

        adapter = new VisitorArtworkAdapter(this, artworkList);
        recyclerViewvisitorartworks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewvisitorartworks.setAdapter(adapter);

        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(VisitorArtDiscoveryPage.this, VisitorAccountPage.class);
            startActivity(intent);
        });

        loadArtworksFromFirebase();

        searchartist.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterArtworks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterArtworks(newText);
                return true;
            }

            private void filterArtworks(String query) {
                List<Artwork> filteredList = new ArrayList<>();
                for (Artwork artwork : artworkList) {
                    if (artwork.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                            artwork.getArtistId().toLowerCase().contains(query.toLowerCase())) {
                        filteredList.add(artwork);
                    }
                }
                adapter.updateList(filteredList);
            }
        });
    }

    private void loadArtworksFromFirebase() {
        DatabaseReference artworksRef = FirebaseDatabase.getInstance().getReference("artworks");
        artworksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                artworkList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Artwork artwork = dataSnapshot.getValue(Artwork.class);
                    if (artwork != null) {
                        Log.d("FirebaseArtwork", "Loaded: " + artwork.getTitle());
                        artworkList.add(artwork);
                    }
                }
                adapter.updateList(artworkList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load artworks: " + error.getMessage());
            }
        });
    }
}
