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

    private List<String> compositeKeys; // format: "eventId/artistId"
    private List<Invitation> invitations;
    private DatabaseReference dbRef;      // reference to "invitations" node
    private DatabaseReference eventDbRef; // reference to "events" node

    public InvitationAdapter(List<String> compositeKeys, List<Invitation> invitations,
                             DatabaseReference dbRef, DatabaseReference eventDbRef) {
        this.compositeKeys = compositeKeys;
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
        String compositeKey = compositeKeys.get(position);
        Invitation invite = invitations.get(position);

        if (invite == null) {
            holder.artistName.setText("Artist: Unknown");
            holder.email.setText("Email: Unknown");
            holder.status.setText("Status: Unknown");
            holder.eventName.setText("Event: Unknown");
            return;
        }

        holder.artistName.setText("Artist: " + (invite.getArtistName() != null ? invite.getArtistName() : "Unknown"));
        holder.email.setText("Email: " + (invite.getEmail() != null ? invite.getEmail() : "Unknown"));
        String status = invite.getStatus() != null ? invite.getStatus() : "pending";
        holder.status.setText("Status: " + status.toUpperCase());


        if (invite.getEventId() != null && !invite.getEventId().isEmpty()) {
            eventDbRef.child(invite.getEventId()).child("title").get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String eventName = snapshot.getValue(String.class);
                            holder.eventName.setText("Event: " + eventName);
                        } else {
                            holder.eventName.setText("Event: Unknown");
                        }
                    })
                    .addOnFailureListener(e -> holder.eventName.setText("Event: Error"));
        } else {
            holder.eventName.setText("Event: Unknown");
        }


        if ("accepted".equalsIgnoreCase(status)) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnConfirmed.setVisibility(View.VISIBLE);
            holder.btnConfirmed.setText("Confirmed");
            holder.btnConfirmed.setEnabled(false);
        } else if ("rejected".equalsIgnoreCase(status)) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnReject.setText("Rejected");
            holder.btnReject.setEnabled(false);
            holder.btnConfirmed.setVisibility(View.GONE);
        } else {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnReject.setText("Reject");
            holder.btnReject.setEnabled(true);
            holder.btnConfirmed.setVisibility(View.GONE);
        }



        holder.btnAccept.setOnClickListener(v -> {
            // Update status in Firebase using composite key split
            String[] parts = compositeKey.split("/");
            if (parts.length == 2) {
                String eventId = parts[0];
                String artistId = parts[1];
                dbRef.child(eventId).child(artistId).child("status").setValue("accepted");
                invite.setStatus("accepted");
                notifyItemChanged(position);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Confirm Rejection")
                    .setMessage("Are you sure you want to reject this invitation?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String[] parts = compositeKey.split("/");
                        if (parts.length == 2) {
                            String eventId = parts[0];
                            String artistId = parts[1];
                            dbRef.child(eventId).child(artistId).child("status").setValue("rejected");
                            invite.setStatus("rejected");
                            notifyItemChanged(position);
                        }
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
