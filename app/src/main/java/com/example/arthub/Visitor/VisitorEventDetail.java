package com.example.arthub.Visitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arthub.Admin.Event;
import com.example.arthub.Artist.AcceptedArtist;
import com.example.arthub.Artist.AcceptedArtistAdapter;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class VisitorEventDetail extends AppCompatActivity {

    private TextView title, location, date, description, subtotal, tax, total, ticketCount;
    private ImageView bannerImage, btnMinus, btnPlus;
    private Button bookbtn, attending;
    private RecyclerView recyclerAcceptedArtists;

    private double totalVal;
    private int ticketQuantity = 1;
    private double price = 0.0;

    private Event event;
    private String eventId, eventTitle, visitorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_event_detail);

        initViews();

        event = (Event) getIntent().getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Event data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventId = event.getEventId();
        eventTitle = event.getTitle();
        visitorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupUIWithEvent(event);
        setupInterestToggle();
        setupQuantityButtons();
        setupBookButton();
        loadAcceptedArtists();
    }

    private void initViews() {
        title = findViewById(R.id.textTitle);
        location = findViewById(R.id.textLocation);
        date = findViewById(R.id.textDate);
        description = findViewById(R.id.textDescription);
        subtotal = findViewById(R.id.textSubtotal);
        tax = findViewById(R.id.textTax);
        total = findViewById(R.id.textTotal);
        ticketCount = findViewById(R.id.textTicketCount);
        bannerImage = findViewById(R.id.imageBanner);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        bookbtn = findViewById(R.id.bookbtn);
        attending = findViewById(R.id.attending);
        recyclerAcceptedArtists = findViewById(R.id.recyclerAcceptedArtists);
    }

    private void setupUIWithEvent(Event event) {
        title.setText(event.getTitle());
        location.setText("Location: " + event.getLocation());

        String formattedDate = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm", Locale.getDefault())
                .format(new Date(event.getEventDate()));
        date.setText("Date: " + formattedDate);

        description.setText(event.getDescription());
        Glide.with(this).load(event.getBannerImageUrl()).into(bannerImage);

        price = event.getticketPrice();
        updatePrice(price);
    }

    private void loadAcceptedArtists() {
        DatabaseReference invitationsRef = FirebaseDatabase.getInstance()
                .getReference("invitations")
                .child(eventId);

        invitationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AcceptedArtist> acceptedArtists = new ArrayList<>();
                for (DataSnapshot artistSnap : snapshot.getChildren()) {
                    String status = artistSnap.child("status").getValue(String.class);
                    if ("accepted".equalsIgnoreCase(status)) {
                        String artistId = artistSnap.getKey();
                        String artistName = artistSnap.child("artistName").getValue(String.class);
                        acceptedArtists.add(new AcceptedArtist(artistId, artistName, 0));
                    }
                }
                AcceptedArtistAdapter adapter = new AcceptedArtistAdapter(VisitorEventDetail.this, acceptedArtists, eventId);
                recyclerAcceptedArtists.setAdapter(adapter);
                recyclerAcceptedArtists.setLayoutManager(new LinearLayoutManager(VisitorEventDetail.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VisitorEventDetail.this, "Failed to load artists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupInterestToggle() {
        DatabaseReference eventInterestRef = FirebaseDatabase.getInstance()
                .getReference("eventinterest")
                .child(eventTitle)
                .child(visitorId)
                .child("interested");

        eventInterestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean interested = snapshot.getValue(Boolean.class) != null && snapshot.getValue(Boolean.class);
                updateInterestButton(interested);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VisitorEventDetail.this, "Failed to check interest", Toast.LENGTH_SHORT).show();
            }
        });

        attending.setOnClickListener(v -> {
            eventInterestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DatabaseReference countRef = FirebaseDatabase.getInstance()
                            .getReference("interestcount")
                            .child(eventTitle)
                            .child("interested");

                    if (snapshot.exists()) {
                        // Remove interest
                        eventInterestRef.removeValue();
                        updateInterestButton(false);

                        // Decrement count
                        countRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                Integer currentValue = currentData.getValue(Integer.class);
                                if (currentValue == null || currentValue <= 0) {
                                    currentData.setValue(0);
                                } else {
                                    currentData.setValue(currentValue - 1);
                                }
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {}
                        });

                        Toast.makeText(VisitorEventDetail.this, "Interest removed", Toast.LENGTH_SHORT).show();
                    } else {
                        // Add interest
                        eventInterestRef.setValue(true);
                        updateInterestButton(true);

                        // Increment count
                        countRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                Integer currentValue = currentData.getValue(Integer.class);
                                if (currentValue == null) {
                                    currentData.setValue(1);
                                } else {
                                    currentData.setValue(currentValue + 1);
                                }
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {}
                        });

                        Toast.makeText(VisitorEventDetail.this, "Interest marked", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(VisitorEventDetail.this, "Failed to update interest", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateInterestButton(boolean interested) {
        attending.setText(interested ? "✔ Marked as Interested" : "Interested");
    }

    private void setupQuantityButtons() {
        btnPlus.setOnClickListener(v -> {
            ticketQuantity++;
            updatePrice(price);
        });

        btnMinus.setOnClickListener(v -> {
            if (ticketQuantity > 1) {
                ticketQuantity--;
                updatePrice(price);
            }
        });
    }

    private void setupBookButton() {
        bookbtn.setEnabled(true);
        bookbtn.setOnClickListener(view -> {
            Intent intent = new Intent(VisitorEventDetail.this, StripePaymentPage.class);
            intent.putExtra("price", totalVal);
            intent.putExtra("ticketsBooked", ticketQuantity);
            intent.putExtra("event", event);
            startActivity(intent);
        });
    }

    private void updatePrice(double price) {
        ticketCount.setText(String.valueOf(ticketQuantity));
        double subtotalVal = ticketQuantity * price;
        double taxVal = subtotalVal * 0.18;
        totalVal = subtotalVal + taxVal;

        subtotal.setText(String.format(Locale.getDefault(), "SubTotal: $%.2f", subtotalVal));
        tax.setText(String.format(Locale.getDefault(), "Tax (18%%): $%.2f", taxVal));
        total.setText(String.format(Locale.getDefault(), "Total: $%.2f", totalVal));
    }
}
