package com.example.arthub.Visitor;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class VisitorFavouritesArtworks extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VisitorFavouritesAdapter adapter;
    private final List<Map<String, String>> artworkList = new ArrayList<>();
    private DatabaseReference dbRef;
    private String visitorId;
    private TextView textNoFavourites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_favourites_artworks);

        recyclerView = findViewById(R.id.recyclerViewFavourites);
        textNoFavourites = findViewById(R.id.textNoFavourites);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new VisitorFavouritesAdapter(this, artworkList);
        recyclerView.setAdapter(adapter);

        visitorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        fetchFavouriteArtworks();
    }

    private void fetchFavouriteArtworks() {
        dbRef.child("favourites").child(visitorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                if (!favSnapshot.exists()) {
                    // No favourites, show text, hide list
                    textNoFavourites.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    return;
                }

                textNoFavourites.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                artworkList.clear();

                for (DataSnapshot favItem : favSnapshot.getChildren()) {
                    String artistId = favItem.getKey();
                    if (artistId == null) continue;

                    // Fetch each artist's details as they come
                    dbRef.child("artists").child(artistId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot artistSnap) {
                            String name = artistSnap.child("name").getValue(String.class);
                            String email = artistSnap.child("email").getValue(String.class);
                            String bio = artistSnap.child("bio").getValue(String.class);
                            String profileImageUrl = artistSnap.child("profileImageUrl").getValue(String.class);

                            Map<String, String> map = new HashMap<>();
                            map.put("name", name != null ? name : "Unknown");
                            map.put("email", email != null ? email : "N/A");
                            map.put("bio", bio != null ? bio : "");
                            map.put("imageUrl", profileImageUrl != null ? profileImageUrl : "");

                            artworkList.add(map);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error if needed
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }
}
