package com.example.arthub.Visitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.Admin.Event;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

public class VisitorBookingHistoryAdapter extends RecyclerView.Adapter<VisitorBookingHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<VisitorBooking> bookingList;
    private final String userId;

    public VisitorBookingHistoryAdapter(Context context, List<VisitorBooking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_visitor_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VisitorBooking booking = bookingList.get(position);
        Event event = booking.getEvent();

        // Set event title and location
        holder.titleText.setText(event.getTitle());
        holder.locationText.setText("📍 " + event.getLocation());

        // Set event date and time
        holder.dateTimeText.setText("📅 " + event.getEventDate() + " at " + event.getTime());

        // Set ticket and price info
        holder.ticketsText.setText("Tickets: " + booking.getTicketsBooked());
        holder.priceText.setText(String.format(Locale.getDefault(),
                "Subtotal: ₹%.2f\nTax: ₹%.2f\nTotal: ₹%.2f",
                booking.getSubtotal(),
                booking.getTax(),
                booking.getTotal()));

        // Set booking timestamp
        holder.bookingDateText.setText("Booked on: " + booking.getBookingTimestamp());

        // Cancel booking handler
        holder.cancelButton.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("bookings")
                    .child(userId)
                    .child(booking.getBookingId())
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Booking canceled", Toast.LENGTH_SHORT).show();
                        bookingList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, locationText, dateTimeText, ticketsText, priceText, bookingDateText;
        Button cancelButton;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            locationText = itemView.findViewById(R.id.locationText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            ticketsText = itemView.findViewById(R.id.ticketsText);
            priceText = itemView.findViewById(R.id.priceText);
            bookingDateText = itemView.findViewById(R.id.bookingDateText);
            cancelButton = itemView.findViewById(R.id.cancelbooking);
        }
    }
}
