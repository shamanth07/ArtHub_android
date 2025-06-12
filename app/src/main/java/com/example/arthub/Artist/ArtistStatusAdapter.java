package com.example.arthub.Artist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arthub.Admin.Event;
import com.example.arthub.Admin.Invitation;
import com.example.arthub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArtistStatusAdapter extends RecyclerView.Adapter<ArtistStatusAdapter.StatusViewHolder> {

    private Context context;
    private List<ArtistInvitationItem> itemList;

    public ArtistStatusAdapter(Context context, List<ArtistInvitationItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        ArtistInvitationItem item = itemList.get(position);
        Event event = item.getEvent();
        Invitation invitation = item.getInvitation();

        holder.title.setText(event.getTitle());
        holder.description.setText(event.getDescription());
        holder.location.setText(event.getLocation());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(event.getEventDate()));
        holder.date.setText(dateStr);
        holder.time.setText(event.getTime());

        holder.status.setText("Status: " + invitation.getStatus().toUpperCase());
        if (invitation.getStatus().equalsIgnoreCase("accepted")) {
            holder.status.setTextColor(Color.parseColor("#4CAF50"));
        } else if (invitation.getStatus().equalsIgnoreCase("rejected")) {
            holder.status.setTextColor(Color.parseColor("#F44336"));
        } else {
            holder.status.setTextColor(Color.GRAY);
        }

        Glide.with(context)
                .load(event.getBannerImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.bannerImage);

        fetchRSVPCount(event.getTitle(), holder.rsvpcount);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArtistEventDetailActivity.class);
            intent.putExtra("event", event);
            intent.putExtra("invitation", invitation);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    private void fetchRSVPCount(String eventTitle, TextView rsvpTextView) {
        DatabaseReference rsvpCountRef = FirebaseDatabase.getInstance()
                .getReference("rsvp_counts")
                .child(eventTitle)
                .child("attending");

        rsvpCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.exists() ? snapshot.getValue(Long.class) : 0;
                rsvpTextView.setText("RSVP Count: " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                rsvpTextView.setText("RSVP Count: N/A");
                Toast.makeText(context, "Failed to load RSVP count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;
        TextView title, description, date, time, location, status, rsvpcount;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImageView);
            title = itemView.findViewById(R.id.eventTitleTextView);
            description = itemView.findViewById(R.id.eventDescriptionTextView);
            date = itemView.findViewById(R.id.eventDateTextView);
            time = itemView.findViewById(R.id.eventTimeTextView);
            location = itemView.findViewById(R.id.locationTextView);
            status = itemView.findViewById(R.id.statusTextView);
            rsvpcount = itemView.findViewById(R.id.rsvpcount);
        }
    }
}