package com.example.easytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.easytrack.R;
import com.example.easytrack.session.SessionManager;

/**
 * MainActivity serves as the "Splash Screen" for EasyTrack.
 * It's the first thing a user sees when they open the app.
 * Its main job is to show the logo for a few seconds and then decide
 * whether to send the user to the Login page or straight to their Tasks.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make the app content flow under the status bar for a "full screen" feel
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Ensure our content respects system bars like the notch and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Use our SessionManager to check if someone is already logged in
        SessionManager sessionManager = new SessionManager(this);

        /**
         * We use a Handler to create a 3-second delay (3000ms).
         * This gives the user time to see the splash screen branding.
         */
        new Handler().postDelayed(() -> {
            Intent intent;
            if (sessionManager.isLoggedIn()) {
                // If we remember them, take them to their tasks
                intent = new Intent(MainActivity.this, TasksActivity.class);
            } else {
                // If not, they need to sign in
                intent = new Intent(MainActivity.this, SignInActivity.class);
            }
            startActivity(intent);
            
            // finish() is crucial here - it removes the splash screen from the 
            // back stack so the user doesn't see it again if they press 'back'.
            finish();
        }, 3000);
    }
}
