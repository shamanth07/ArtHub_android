package com.example.arthub.Artist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.arthub.Admin.Event;
import com.example.arthub.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArtistEventAdapter extends RecyclerView.Adapter<ArtistEventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private OnApplyClickListener listener;

    public interface OnApplyClickListener {
        void onApplyClick(Event event);
    }

    public ArtistEventAdapter(Context context, List<Event> eventList, OnApplyClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_apply_for_event_for_artist, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText(formatDate(event.getEventDate()) + " - " + event.getTime());

        holder.applyButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApplyClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTextView;
        Button applyButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.EventTitle);
            dateTextView = itemView.findViewById(R.id.EventDateTime);
            applyButton = itemView.findViewById(R.id.applyButton);
        }
    }

    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
    }
}
