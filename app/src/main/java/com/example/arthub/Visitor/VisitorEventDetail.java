package com.example.arthub.Visitor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.arthub.Admin.Event;
import com.example.arthub.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VisitorEventDetail extends AppCompatActivity {
    private TextView title, location, date, description, subtotal, tax, total, ticketCount;
    private ImageView bannerImage, btnMinus, btnPlus;
    private Button bookbtn;

    private int ticketQuantity = 1;
    private double price = 0.0; // Now fetched directly as double

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_event_detail);

        // Initialize UI components
        title = findViewById(R.id.textTitle);
        location = findViewById(R.id.textLocation);
        date = findViewById(R.id.textDate);
        description = findViewById(R.id.textDescription);
        subtotal = findViewById(R.id.textSubtotal);
        tax = findViewById(R.id.textTax);
        total = findViewById(R.id.textTotal);
        ticketCount = findViewById(R.id.textTicketCount);
        bannerImage = findViewById(R.id.imageBanner);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        bookbtn = findViewById(R.id.bookbtn);

        // Get Event object passed from intent
        Event event = (Event) getIntent().getSerializableExtra("event");

        if (event != null) {
            title.setText(event.getTitle());
            location.setText("Location: " + (event.getLocation() != null ? event.getLocation() : "Not available"));
            date.setText("Date: " + new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm", Locale.getDefault()).format(new Date(event.getEventDate())));
            description.setText(event.getDescription());
            Glide.with(this).load(event.getBannerImageUrl()).into(bannerImage);

            price = event.getticketPrice(); // Directly use double
            updatePrice(price);
        }

        btnPlus.setOnClickListener(v -> {
            ticketQuantity++;
            updatePrice(price);
        });

        btnMinus.setOnClickListener(v -> {
            if (ticketQuantity > 1) {
                ticketQuantity--;
                updatePrice(price);
            }
        });
    }

    private void updatePrice(double price) {
        ticketCount.setText(String.valueOf(ticketQuantity));
        double subtotalVal = ticketQuantity * price;
        double taxVal = subtotalVal * 0.18;
        double totalVal = subtotalVal + taxVal;

        subtotal.setText("SubTotal: $" + String.format(Locale.getDefault(), "%.2f", subtotalVal));
        tax.setText("Tax (18%): $" + String.format(Locale.getDefault(), "%.2f", taxVal));
        total.setText("Total: $" + String.format(Locale.getDefault(), "%.2f", totalVal));
    }
}
