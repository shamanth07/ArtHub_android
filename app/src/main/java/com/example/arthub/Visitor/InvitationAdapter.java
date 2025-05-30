package com.example.arthub.Visitor;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.Admin.Invitation;
import com.example.arthub.R;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.InviteViewHolder> {

    private List<String> keys;
    private List<Invitation> invitations;
    private DatabaseReference dbRef;

    private DatabaseReference eventDbRef;

    public InvitationAdapter(List<String> keys, List<Invitation> invitations, DatabaseReference dbRef,DatabaseReference eventDbRef) {
        this.keys = keys;
        this.invitations = invitations;
        this.dbRef = dbRef;
        this.eventDbRef = eventDbRef;
    }

    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invitation, parent, false);
        return new InviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteViewHolder holder, int position) {
        String key = keys.get(position);
        Invitation invite = invitations.get(position);

        String status = invite.status != null ? invite.status : "pending";
        String artistname = invite.artistName != null ? invite.artistName : "Unknown Artist";
        String Email = invite.email != null ? invite.email : "No email provided";

        holder.artistName.setText("Artist: " + artistname);
        holder.email.setText("Email: " + Email);
        holder.status.setText("Status: " + status.toUpperCase());

        // 🔹 Fetch Event Name using event ID (key)
        eventDbRef.child(key).child("title").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String eventName = snapshot.getValue(String.class);
                holder.eventName.setText("Event: " + eventName);
            } else {
                holder.eventName.setText("Event: Unknown");
            }
        }).addOnFailureListener(e -> holder.eventName.setText("Event: Error"));


        if ("accepted".equalsIgnoreCase(status)) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnConfirmed.setVisibility(View.VISIBLE);
            holder.btnAccept.setEnabled(false);
            holder.btnReject.setEnabled(false);
        } else if ("rejected".equalsIgnoreCase(status)) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnConfirmed.setVisibility(View.GONE);
            holder.btnAccept.setEnabled(true);
            holder.btnReject.setEnabled(false);
        } else {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnConfirmed.setVisibility(View.GONE);
            holder.btnAccept.setEnabled(true);
            holder.btnReject.setEnabled(true);
        }

        holder.btnAccept.setOnClickListener(v -> {
            dbRef.child(key).child("status").setValue("accepted");
            invite.status = "accepted";
            notifyItemChanged(position);
        });

        holder.btnReject.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Confirm Rejection")
                    .setMessage("Are you sure you want to reject this invitation?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbRef.child(key).child("status").setValue("rejected");
                        invite.status = "rejected";
                        notifyItemChanged(position);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }




    @Override
    public int getItemCount() {
        return invitations.size();
    }

    public static class InviteViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, artistName, email, status;
        Button btnAccept, btnReject, btnConfirmed;

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            artistName = itemView.findViewById(R.id.artistName);
            email = itemView.findViewById(R.id.email);
            status = itemView.findViewById(R.id.status);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnConfirmed = itemView.findViewById(R.id.btnConfirmed);
        }
    }
}
