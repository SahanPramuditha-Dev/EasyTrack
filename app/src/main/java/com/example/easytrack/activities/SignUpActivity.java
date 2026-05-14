package com.example.easytrack.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.easytrack.R;
import com.example.easytrack.database.DatabaseHelper;
import com.example.easytrack.utils.ToastHelper;
import com.google.android.material.button.MaterialButton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SignUpActivity is the registration page for new users.
 * It gathers user details and saves them to our local SQLite database.
 */
public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private DatabaseHelper dbHelper;
    
    // We use a background thread for database operations to keep the UI smooth
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        
        dbHelper = new DatabaseHelper(this);

        // Ensure the layout fits within the system bars (status bar, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize all our input fields and buttons
        EditText etUsername = findViewById(R.id.et_username);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etConfirmPassword = findViewById(R.id.et_confirm_password);
        MaterialButton btnCreateAccount = findViewById(R.id.btn_create_account);

        // Logic for when the user clicks the "Create Account" button
        btnCreateAccount.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            // 1. Basic validation: Are all fields filled?
            if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                ToastHelper.showCustomToast(this, "Please fill all fields");
            } 
            // 2. Validation: Do the passwords match?
            else if (!pass.equals(confirmPass)) {
                ToastHelper.showCustomToast(this, "Passwords do not match");
            } 
            // 3. All looks good, let's try to register the user
            else {
                executorService.execute(() -> {
                    boolean success = dbHelper.registerUser(user, email, pass);
                    runOnUiThread(() -> {
                        if (success) {
                            ToastHelper.showCustomToast(this, "Registration Successful!");
                            // Go back to the Sign In screen after success
                            finish(); 
                        } else {
                            // This usually happens if the username is already taken
                            ToastHelper.showCustomToast(this, "Registration Failed or Username exists");
                        }
                    });
                });
            }
        });

        // Helper buttons to navigate back to the previous screen
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_sign_in_link).setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up our background worker thread
        executorService.shutdown();
    }
}
