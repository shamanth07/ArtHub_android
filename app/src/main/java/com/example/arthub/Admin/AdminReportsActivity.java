package com.example.arthub.Admin;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.arthub.R;
import com.google.firebase.database.*;

import java.util.*;

public class AdminReportsActivity extends AppCompatActivity {

    RecyclerView rv;
    AdminReportAdapter adapter;
    List<Report> reportList = new ArrayList<>();
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_reports);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        rv = findViewById(R.id.recyclerViewReports);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminReportAdapter(this, reportList);
        rv.setAdapter(adapter);


        SwipeRefreshHelper.setupSwipeRefresh(swipeRefresh, this, this::loadReports);


        loadReports();
    }

    private void loadReports() {
        AdminReportGenerator.generateReports(this);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("adminreports");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                reportList.clear();
                for (DataSnapshot r : snap.getChildren()) {
                    Report rep = r.getValue(Report.class);
                    if (rep != null) reportList.add(rep);
                }
                adapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }
}
