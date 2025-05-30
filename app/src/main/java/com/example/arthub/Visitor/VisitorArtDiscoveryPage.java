package com.example.arthub.Visitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

    VisitorArtworkAdapter adapter;
    List<Artwork> artworkList;


      RecyclerView recyclerViewvisitorartworks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_visitor_art_discovery_page);


        menuIcon = findViewById(R.id.menuIcon);
        searchartist = findViewById(R.id.searchartist);
        recyclerViewvisitorartworks = findViewById(R.id.recyclerViewvisitorartworks);


        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(VisitorArtDiscoveryPage.this, VisitorAccountPage.class);
            startActivity(intent);
        });


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



        recyclerViewvisitorartworks.setLayoutManager(new LinearLayoutManager(this));
        artworkList = new ArrayList<>();
        adapter = new VisitorArtworkAdapter(this, artworkList);
        recyclerViewvisitorartworks.setAdapter(adapter);

        loadArtworksFromFirebase();
    }


    private void loadArtworksFromFirebase() {
        DatabaseReference artworksRef = FirebaseDatabase.getInstance().getReference("artworks");

        artworksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                artworkList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Artwork artwork = dataSnapshot.getValue(Artwork.class);
                    if (artwork != null) {
                        artworkList.add(artwork);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }



}