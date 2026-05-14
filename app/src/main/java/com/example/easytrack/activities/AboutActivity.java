package com.example.easytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.easytrack.R;

/**
 * AboutActivity displays information about the app and the developers.
 * It's a simple informational page that also handles app-wide exit and navigation.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display for a modern look
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);
        
        // Handle window insets to ensure content isn't hidden behind system bars (status/nav bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Toolbar Navigation (The back arrow)
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish()); // Just goes back to the previous screen
        }

        // The "Exit Application" button logic
        View btnExit = findViewById(R.id.btn_exit);
        if (btnExit != null) {
            btnExit.setOnClickListener(v -> {
                finishAffinity(); // Closes all activities in the stack
                System.exit(0);   // Forcefully terminates the app process
            });
        }

        // --- Bottom Navigation Handlers ---

        View navTasks = findViewById(R.id.nav_tasks);
        if (navTasks != null) {
            navTasks.setOnClickListener(v -> {
                startActivity(new Intent(this, TasksActivity.class));
                finish(); // Close this so we don't build up a huge stack of activities
            });
        }

        View navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            });
        }

        // Highlight this tab as the "active" one in the bottom navigation
        View pillAbout = findViewById(R.id.pill_about);
        if (pillAbout != null) {
            pillAbout.setVisibility(View.VISIBLE);
        }
        
        TextView tvAbout = findViewById(R.id.tv_about);
        if (tvAbout != null) {
            tvAbout.setTextColor(getResources().getColor(R.color.button_green));
        }
        
        ImageView ivAbout = findViewById(R.id.iv_about);
        if (ivAbout != null) {
            ivAbout.setColorFilter(getResources().getColor(R.color.button_green));
        }
    }
}
