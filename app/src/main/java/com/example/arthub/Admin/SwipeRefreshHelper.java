package com.example.arthub.Admin;

import android.content.Context;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class SwipeRefreshHelper {

    public interface OnRefreshCallback {
        void onRefresh();
    }

    public static void setupSwipeRefresh(SwipeRefreshLayout swipeRefreshLayout, Context context, OnRefreshCallback callback) {
        if (swipeRefreshLayout == null) return;

        swipeRefreshLayout.setOnRefreshListener(() -> {

            Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show();

            // Execute user-defined callback logic
            if (callback != null) {
                callback.onRefresh();
            }

            // Stop the animation (you can control this manually too)

        });
    }
}
