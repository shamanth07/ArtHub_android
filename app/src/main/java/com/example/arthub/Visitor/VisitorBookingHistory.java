package com.example.arthub.Visitor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class VisitorBookingHistory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private VisitorBookingHistoryAdapter adapter;
    private List<VisitorBooking> bookingList;
    private DatabaseReference bookingsRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_booking_history);

        recyclerView = findViewById(R.id.bookingRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings").child(userId);

        bookingList = new ArrayList<>();
        adapter = new VisitorBookingHistoryAdapter(this, bookingList);
        recyclerView.setAdapter(adapter);

        loadBookings();

        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);
    }

    private void loadBookings() {
        swipeRefreshLayout.setRefreshing(true);
        bookingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    VisitorBooking booking = snap.getValue(VisitorBooking.class);
                    if (booking != null) {
                        booking.setBookingId(snap.getKey());
                        bookingList.add(booking);
                    }
                }
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
