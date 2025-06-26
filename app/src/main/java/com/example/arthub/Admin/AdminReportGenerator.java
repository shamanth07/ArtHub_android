package com.example.arthub.Admin;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.*;

public class AdminReportGenerator extends AppCompatActivity {

    public static void generateReports(Context ctx) {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        DatabaseReference rsvpCountRef = FirebaseDatabase.getInstance().getReference("rsvpcount");
        DatabaseReference interestCountRef = FirebaseDatabase.getInstance().getReference("interestcount");
        DatabaseReference artistLikesRef = FirebaseDatabase.getInstance().getReference("artistLikesInEvents");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("adminreports");
        DatabaseReference mostLikedArtistCountRef = FirebaseDatabase.getInstance().getReference("mostlikedartistcount");
        DatabaseReference artistLikesCountRef = FirebaseDatabase.getInstance().getReference("artistlikescount");
        DatabaseReference artistsRef = FirebaseDatabase.getInstance().getReference("artists");
        DatabaseReference artworksRef = FirebaseDatabase.getInstance().getReference("artworks");

        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot eventsSnap) {
                for (DataSnapshot eventDS : eventsSnap.getChildren()) {
                    final String eventId = eventDS.getKey();
                    final String title = eventDS.child("title").getValue(String.class);
                    final String banner = eventDS.child("bannerImageUrl").getValue(String.class);

                    if (title == null || eventId == null) continue;

                    final int[] interestCount = {0};
                    final int[] rsvpCount = {0};

                    interestCountRef.child(title).child("interested").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot interestSnap) {
                            if (interestSnap.exists()) {
                                Integer val = interestSnap.getValue(Integer.class);
                                if (val != null) interestCount[0] = val;
                            }

                            rsvpCountRef.child(title).child("attending").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot rsvpSnap) {
                                    if (rsvpSnap.exists()) {
                                        Integer val = rsvpSnap.getValue(Integer.class);
                                        if (val != null) rsvpCount[0] = val;
                                    }

                                    bookingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot bookingsSnap) {
                                            Set<String> visitorIds = new HashSet<>();

                                            for (DataSnapshot booking : bookingsSnap.getChildren()) {
                                                String bookedTitle = booking.child("event").child("title").getValue(String.class);
                                                String userId = booking.child("userId").getValue(String.class);

                                                if (title.equals(bookedTitle) && userId != null) {
                                                    visitorIds.add(userId);
                                                }
                                            }

                                            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot usersSnap) {
                                                    List<String> confirmedVisitors = new ArrayList<>();
                                                    for (String uid : visitorIds) {
                                                        DataSnapshot userSnap = usersSnap.child(uid);
                                                        String email = userSnap.child("email").getValue(String.class);
                                                        String role = userSnap.child("role").getValue(String.class);
                                                        if ("Visitor".equals(role) && email != null && email.contains("@")) {
                                                            String name = email.split("@")[0];
                                                            confirmedVisitors.add(name);
                                                        }
                                                    }

                                                    artistLikesRef.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot likesSnap) {
                                                            Map<String, Integer> artistLikesMap = new HashMap<>();
                                                            for (DataSnapshot artistDS : likesSnap.getChildren()) {
                                                                int likes = (int) artistDS.getChildrenCount();
                                                                String artistId = artistDS.getKey();
                                                                artistLikesMap.put(artistId, likes);
                                                                artistLikesCountRef.child(eventId).child(artistId).setValue(likes);
                                                            }

                                                            String mostLikedArtistId = "";
                                                            int maxLikes = 0;
                                                            for (Map.Entry<String, Integer> entry : artistLikesMap.entrySet()) {
                                                                if (entry.getValue() > maxLikes) {
                                                                    maxLikes = entry.getValue();
                                                                    mostLikedArtistId = entry.getKey();
                                                                }
                                                            }

                                                            if (!mostLikedArtistId.isEmpty()) {
                                                                final String finalMostLikedArtistId = mostLikedArtistId;
                                                                final int finalMaxLikes = maxLikes;
                                                                artistsRef.child(finalMostLikedArtistId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot nameSnap) {
                                                                        final String[] artistName = { nameSnap.getValue(String.class) };
                                                                        if (artistName[0] == null) artistName[0] = finalMostLikedArtistId;

                                                                        mostLikedArtistCountRef.child(eventId).setValue(artistName[0] + " : " + finalMaxLikes);

                                                                        artworksRef.orderByChild("artistId").equalTo(finalMostLikedArtistId)
                                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot artSnap) {
                                                                                        int totalLikes = 0;
                                                                                        for (DataSnapshot artDS : artSnap.getChildren()) {
                                                                                            Long likes = artDS.child("likes").getValue(Long.class);
                                                                                            if (likes != null) totalLikes += likes;
                                                                                        }

                                                                                        Report report = new Report(title, banner, interestCount[0], rsvpCount[0], artistName[0], confirmedVisitors);
                                                                                        report.setTotalLikes(totalLikes);
                                                                                        reportsRef.child(eventId).setValue(report);
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                                                        Report report = new Report(title, banner, interestCount[0], rsvpCount[0], artistName[0], confirmedVisitors);
                                                                                        report.setTotalLikes(0);
                                                                                        reportsRef.child(eventId).setValue(report);
                                                                                    }
                                                                                });
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                                        Report report = new Report(title, banner, interestCount[0], rsvpCount[0], finalMostLikedArtistId, confirmedVisitors);
                                                                        report.setTotalLikes(0);
                                                                        reportsRef.child(eventId).setValue(report);
                                                                    }
                                                                });

                                                            } else {
                                                                Report report = new Report(title, banner, interestCount[0], rsvpCount[0], "", confirmedVisitors);
                                                                report.setTotalLikes(0);
                                                                reportsRef.child(eventId).setValue(report);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) { }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
