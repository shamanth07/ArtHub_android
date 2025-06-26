package com.example.arthub.Visitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.arthub.Admin.Event;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StripePaymentPage extends AppCompatActivity {

    private final String PublishableKey = "pk_test_51QfwnlFTdqp1BVfOlcBaG0SJftyxo4zULXt6Tb2FrKwvTvgUaTAAdxRhH9qh5mmqXySE3LeDeVg7SeKOCt6TqeqC00sH1k2SD4";
    private final String SecretKey = "sk_test_51QfwnlFTdqp1BVfOCnwqUZnmOr1YBOTR6Huz4x2g6UUE80LP3NBBDCI6HtydFQV5iadBWKJvFoVmgCqE5dx1jd4s00a7bnJiKA";
    private final String CustomersURL = "https://api.stripe.com/v1/customers";
    private final String EphemeralKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
    private final String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    private String CustomerId;
    private String EphemeralKey;
    private String ClientSecret;
    private final String Currency = "usd";

    private PaymentSheet paymentSheet;
    private double totalPrice;
    private int ticketsBooked;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stripe_payment_page);

        totalPrice = getIntent().getDoubleExtra("price", 0.0);
        ticketsBooked = getIntent().getIntExtra("ticketsBooked", 1);
        event = (Event) getIntent().getSerializableExtra("event");

        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        createCustomer();
    }

    private void createCustomer() {
        StringRequest request = new StringRequest(Request.Method.POST, CustomersURL, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                CustomerId = jsonResponse.getString("id");
                getEphemeralKey();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            String errorMessage = error.networkResponse != null ?
                    "Code: " + error.networkResponse.statusCode + " - " + new String(error.networkResponse.data)
                    : "Unknown error: " + error.toString();
            Toast.makeText(this, "Customer error: " + errorMessage, Toast.LENGTH_LONG).show();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void getEphemeralKey() {
        StringRequest request = new StringRequest(Request.Method.POST, EphemeralKeyURL, response -> {
            try {
                EphemeralKey = new JSONObject(response).getString("secret");
                getClientSecret(CustomerId);
            } catch (JSONException e) {
                Toast.makeText(this, "Ephemeral key error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(this, "Ephemeral key error", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                headers.put("Stripe-Version", "2022-11-15");
                return headers;
            }

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void getClientSecret(String customerId) {
        StringRequest request = new StringRequest(Request.Method.POST, ClientSecretURL, response -> {
            try {
                ClientSecret = new JSONObject(response).getString("client_secret");
                paymentFlow();
            } catch (JSONException e) {
                Toast.makeText(this, "Client secret error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(this, "Client secret error", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }

            @Override
            public Map<String, String> getParams() {
                int amountInCents = (int) (totalPrice * 100);
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                params.put("amount", String.valueOf(amountInCents));
                params.put("currency", Currency);
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void paymentFlow() {
        paymentSheet.presentWithPaymentIntent(ClientSecret, new PaymentSheet.Configuration(
                "Stripe",
                new PaymentSheet.CustomerConfiguration(CustomerId, EphemeralKey)
        ));
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Booking Confirmed", Toast.LENGTH_SHORT).show();

            DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference("bookings");
            String bookingId = bookingRef.push().getKey();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());

            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("title", event.getTitle());
            eventMap.put("description", event.getDescription());
            eventMap.put("bannerImageUrl", event.getBannerImageUrl());
            eventMap.put("location", event.getLocation());
            eventMap.put("ticketPrice", event.getticketPrice());
            eventMap.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(event.getEventDate())));
            eventMap.put("time", event.getTime());

            double subtotal = event.getticketPrice() * ticketsBooked;
            double tax = subtotal * 0.18;
            double total = subtotal + tax;

            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("userId", userId);
            bookingData.put("event", eventMap);
            bookingData.put("ticketsBooked", ticketsBooked);
            bookingData.put("subtotal", subtotal);
            bookingData.put("tax", tax);
            bookingData.put("total", total);
            bookingData.put("bookingTimestamp", timestamp);
            bookingData.put("paymentStatus", "Success");

            bookingRef.child(bookingId).setValue(bookingData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DatabaseReference rsvpRef = FirebaseDatabase.getInstance().getReference("rsvp");
                    String eventName = event.getTitle();

                    // Save tickets booked under rsvp/{eventName}/{userId}/tickets
                    rsvpRef.child(eventName).child(userId).child("tickets").setValue(ticketsBooked)
                            .addOnSuccessListener(aVoid -> {

                                // Increment rsvp_counts/{eventName}/attending by ticketsBooked
                                DatabaseReference rsvpCountRef = FirebaseDatabase.getInstance()
                                        .getReference("rsvpcount")
                                        .child(eventName)
                                        .child("attending");

                                rsvpCountRef.runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                        Integer currentCount = currentData.getValue(Integer.class);
                                        if (currentCount == null) {
                                            currentData.setValue(ticketsBooked);
                                        } else {
                                            currentData.setValue(currentCount + ticketsBooked);
                                        }
                                        return Transaction.success(currentData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                        startActivity(new Intent(StripePaymentPage.this, VisitorDashboard.class));
                                        finish();
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update RSVP tickets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "Failed to save booking", Toast.LENGTH_SHORT).show();
                }
            });

        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, "Payment Failed: " + ((PaymentSheetResult.Failed) paymentSheetResult).getError().getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
