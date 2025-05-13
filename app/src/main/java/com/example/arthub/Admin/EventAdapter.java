package com.example.arthub.Admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arthub.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventTitle.setText(event.getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(event.getEventDate()));
        holder.eventDate.setText(dateStr);

        holder.eventTime.setText(event.getTime());

        Glide.with(context)
                .load(event.getBannerImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.eventImage);


        holder.btnEdit.setOnClickListener(v -> {
            if (context instanceof AdminDashboard) {
                ((AdminDashboard) context).editEvent(event);
            }
        });


        holder.btnCancel.setOnClickListener(v -> {
            if (context instanceof AdminDashboard) {
                ((AdminDashboard) context).deleteEvent(event.getEventId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        ImageView eventImage;
        TextView eventTitle, eventDate, eventTime;
        ImageButton btnEdit, btnCancel;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}

