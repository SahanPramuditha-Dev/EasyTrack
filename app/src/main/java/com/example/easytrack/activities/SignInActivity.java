package com.example.easytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.easytrack.R;
import com.example.easytrack.database.DatabaseHelper;
import com.example.easytrack.session.SessionManager;
import com.example.easytrack.utils.ToastHelper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnSignIn = findViewById(R.id.btn_sign_in);
        Button btnSignUpLink = findViewById(R.id.btn_sign_up_link);

        btnSignIn.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                ToastHelper.showCustomToast(this, "Please enter all fields");
            } else {
                executorService.execute(() -> {
                    boolean isValid = dbHelper.checkUser(username, password);
                    runOnUiThread(() -> {
                        if (isValid) {
                            executorService.execute(() -> {
                                int userId = dbHelper.getUserId(username);
                                runOnUiThread(() -> {
                                    sessionManager.createLoginSession(userId, username);
                                    ToastHelper.showCustomToast(this, "Login Successful!");
                                    Intent intent = new Intent(SignInActivity.this, TasksActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                            });
                        } else {
                            ToastHelper.showCustomToast(this, "Invalid Username or Password");
                        }
                    });
                });
            }
        });

        btnSignUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
