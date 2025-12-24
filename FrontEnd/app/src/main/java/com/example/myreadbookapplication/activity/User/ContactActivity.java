package com.example.myreadbookapplication.activity.User;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // Views will be initialized in setupClickListeners
    }

    private void setupClickListeners() {
        // Back button
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        // Contact methods
        LinearLayout layoutPhone = findViewById(R.id.layout_phone);
        LinearLayout layoutEmail = findViewById(R.id.layout_email);
        LinearLayout layoutWebsite = findViewById(R.id.layout_website);
        LinearLayout layoutAddress = findViewById(R.id.layout_address);

        layoutPhone.setOnClickListener(v -> {
            String phoneNumber = "+1 234 567 8900";
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });

        layoutEmail.setOnClickListener(v -> {
            String email = "support@readbook.com";
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Contact from ReadBook App");
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        layoutWebsite.setOnClickListener(v -> {
            String website = "https://www.readbook.com";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(website));
            startActivity(intent);
        });

        layoutAddress.setOnClickListener(v -> {
            String address = "123 Book Street, Reading City, RC 12345";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:0,0?q=" + Uri.encode(address)));
            startActivity(intent);
        });

        // Social media buttons
        Button btnFacebook = findViewById(R.id.btn_facebook);
        Button btnTwitter = findViewById(R.id.btn_twitter);
        Button btnInstagram = findViewById(R.id.btn_instagram);

        btnFacebook.setOnClickListener(v -> {
            // TODO: Open Facebook page
            Toast.makeText(this, "Opening Facebook page...", Toast.LENGTH_SHORT).show();
        });

        btnTwitter.setOnClickListener(v -> {
            // TODO: Open Twitter page
            Toast.makeText(this, "Opening Twitter page...", Toast.LENGTH_SHORT).show();
        });

        btnInstagram.setOnClickListener(v -> {
            // TODO: Open Instagram page
            Toast.makeText(this, "Opening Instagram page...", Toast.LENGTH_SHORT).show();
        });

        // Feedback button
        Button btnFeedback = findViewById(R.id.btn_feedback);
        btnFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(this, FeedbackActivity.class);
            startActivity(intent);
        });
    }
}
