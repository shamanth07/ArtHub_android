package com.example.arthub.Admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arthub.R;

import java.util.List;

public class AdminReportAdapter extends RecyclerView.Adapter<AdminReportAdapter.VH> {
    private Context ctx;
    private List<Report> list;

    public AdminReportAdapter(Context ctx, List<Report> list) {
        this.ctx = ctx;
        this.list = list;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title, interested, rsvp, liked, totalLikes, visitors;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.ivBanner);
            title = v.findViewById(R.id.tvTitle);
            interested = v.findViewById(R.id.tvInterested);
            rsvp = v.findViewById(R.id.tvRSVP);
            liked = v.findViewById(R.id.tvLikedArtist);
            totalLikes = v.findViewById(R.id.tvTotalLikes);
            visitors = v.findViewById(R.id.tvVisitors);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_report_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Report report = list.get(position);

        holder.title.setText(report.getTitle());

        Glide.with(ctx)
                .load(report.getBannerImageUrl())
                .into(holder.img);

        holder.interested.setText("Interested: " + report.getInterestedCount());
        holder.rsvp.setText("RSVP: " + report.getRsvpCount());

        if (report.getMostLikedArtist() != null && !report.getMostLikedArtist().isEmpty()) {
            holder.liked.setText("Top Artist: " + report.getMostLikedArtist());
            holder.totalLikes.setVisibility(View.VISIBLE);
            holder.totalLikes.setText("Total Likes (Top Artist): " + report.getTotalLikes());
        } else {
            holder.liked.setText("Top Artist: None");
            holder.totalLikes.setVisibility(View.GONE);
        }

        List<String> visitors = report.getConfirmedVisitors();
        if (visitors != null && !visitors.isEmpty()) {
            StringBuilder visitorNames = new StringBuilder();
            for (String visitor : visitors) {
                visitorNames.append(visitor).append(", ");
            }
            visitorNames.setLength(visitorNames.length() - 2);
            holder.visitors.setText("Visitors: " + visitorNames.toString());
        } else {
            holder.visitors.setText("Visitors: None");
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }
}
