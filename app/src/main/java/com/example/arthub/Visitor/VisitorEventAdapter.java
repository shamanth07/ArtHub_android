package com.example.arthub.Visitor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arthub.Admin.Event;
import com.example.arthub.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VisitorEventAdapter extends RecyclerView.Adapter<VisitorEventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;

    public VisitorEventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_visitor_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.titleTextView.setText(event.getTitle());

        // Format the date
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(event.getEventDate()));
        holder.dateTextView.setText(formattedDate);


        Glide.with(context).load(event.getBannerImageUrl()).into(holder.bannerImageView);


//        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, EventDetailActivity.class);
//            intent.putExtra("event", event);
//            context.startActivity(intent);
//        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImageView;
        TextView titleTextView, dateTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImageView = itemView.findViewById(R.id.imageBanner);
            titleTextView = itemView.findViewById(R.id.textTitle);
            dateTextView = itemView.findViewById(R.id.textDate);
        }
    }
}
