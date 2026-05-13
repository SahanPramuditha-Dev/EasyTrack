package com.example.easytrack;

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

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Toolbar Navigation (Back Button)
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Exit Button
        View btnExit = findViewById(R.id.btn_exit);
        if (btnExit != null) {
            btnExit.setOnClickListener(v -> {
                finishAffinity();
                System.exit(0);
            });
        }

        // Setup Bottom Navigation
        View navTasks = findViewById(R.id.nav_tasks);
        if (navTasks != null) {
            navTasks.setOnClickListener(v -> {
                startActivity(new Intent(this, TasksActivity.class));
                finish();
            });
        }

        View navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            });
        }

        // Active state for About tab
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
