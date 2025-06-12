package com.example.arthub.Visitor;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.arthub.R;

public class Settings extends AppCompatActivity {

    TextView darkmode,changelanguage,livesupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);



        darkmode = findViewById(R.id.darkmode);
        changelanguage = findViewById(R.id.changelanguage);
        livesupport = findViewById(R.id.livesupport);
    }
}