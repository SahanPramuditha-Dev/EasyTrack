package com.example.easytrack.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.easytrack.R;
import com.example.easytrack.database.DatabaseHelper;
import com.example.easytrack.session.SessionManager;
import com.example.easytrack.utils.ToastHelper;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ProfileActivity allows users to view and edit their personal information.
 * Users can update their username, email, and even upload a profile picture.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId;
    private ShapeableImageView ivUserAvatar;
    private TextView tvProfileName, tvInfoUsername, tvInfoEmail;
    
    // Using an ExecutorService for background tasks (like database/image operations)
    // to keep the UI smooth and responsive.
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * This launcher handles the image picking process.
     * When a user selects an image from their gallery, this code runs.
     */
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    executorService.execute(() -> {
                        try {
                            // Convert the URI to a Bitmap and scale it down to save memory
                            Bitmap bitmap = uriToBitmap(uri);
                            if (bitmap != null) {
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                                byte[] imageBytes = bitmapToByteArray(scaledBitmap);
                                
                                // Save the image bytes to the database
                                boolean success = dbHelper.updateUserImage(currentUserId, imageBytes);
                                runOnUiThread(() -> {
                                    if (success) {
                                        ivUserAvatar.setImageBitmap(scaledBitmap);
                                        ivUserAvatar.setPadding(0, 0, 0, 0);
                                        ivUserAvatar.clearColorFilter();
                                        ToastHelper.showCustomToast(this, "Profile image updated");
                                    }
                                });
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error processing image", e);
                            runOnUiThread(() -> ToastHelper.showCustomToast(this, "Failed to load image"));
                        }
                    });
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        // Safety check: if no one is logged in, kick them back to the sign-in screen
        if (currentUserId == -1) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvProfileName = findViewById(R.id.tv_profile_name);
        tvInfoUsername = findViewById(R.id.tv_info_username);
        tvInfoEmail = findViewById(R.id.tv_info_email);
        ivUserAvatar = findViewById(R.id.iv_user_avatar);

        // Fill the screen with the user's data
        updateUI();
        loadProfileImage();

        // Set up click listeners for all interactive elements
        ivUserAvatar.setOnClickListener(v -> showAvatarOptionsDialog());
        findViewById(R.id.btn_edit_info).setOnClickListener(v -> showEditInfoDialog());
        findViewById(R.id.btn_sign_out).setOnClickListener(v -> showSignOutDialog());

        // Navigation
        findViewById(R.id.nav_tasks).setOnClickListener(v -> {
            startActivity(new Intent(this, TasksActivity.class));
            finish();
        });

        findViewById(R.id.nav_about).setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
            finish();
        });

        // Set the active state for the bottom navigation
        findViewById(R.id.pill_profile).setVisibility(android.view.View.VISIBLE);
        ((android.widget.TextView)findViewById(R.id.tv_profile)).setTextColor(getResources().getColor(R.color.button_green));
        ((android.widget.ImageView)findViewById(R.id.iv_profile)).setColorFilter(getResources().getColor(R.color.button_green));
    }

    /**
     * Refreshes the text fields on the screen with the latest data from the session and database.
     */
    private void updateUI() {
        String username = sessionManager.getUsername();
        tvProfileName.setText(username);
        tvInfoUsername.setText(username);
        
        executorService.execute(() -> {
            String email = dbHelper.getUserEmail(currentUserId);
            runOnUiThread(() -> tvInfoEmail.setText(email));
        });
    }

    /**
     * Fetches the profile picture from the database and displays it.
     * Shows a default icon if no image is found.
     */
    private void loadProfileImage() {
        executorService.execute(() -> {
            byte[] imageBytes = dbHelper.getUserImage(currentUserId);
            runOnUiThread(() -> {
                if (imageBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    ivUserAvatar.setImageBitmap(bitmap);
                    ivUserAvatar.setPadding(0, 0, 0, 0);
                    ivUserAvatar.clearColorFilter();
                } else {
                    ivUserAvatar.setImageResource(R.drawable.ic_profile);
                    ivUserAvatar.setPadding(20, 20, 20, 20);
                    ivUserAvatar.setColorFilter(getResources().getColor(R.color.button_green));
                }
            });
        });
    }

    /**
     * Shows a popup asking the user if they want to change or delete their photo.
     */
    private void showAvatarOptionsDialog() {
        String[] options = {"Change Image", "Remove Image"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                pickImageLauncher.launch("image/*");
            } else if (which == 1) {
                executorService.execute(() -> {
                    boolean success = dbHelper.updateUserImage(currentUserId, null);
                    runOnUiThread(() -> {
                        if (success) {
                            loadProfileImage();
                            ToastHelper.showCustomToast(this, "Profile image removed");
                        }
                    });
                });
            }
        });
        builder.show();
    }

    // Helper methods for image conversion
    private Bitmap uriToBitmap(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) inputStream.close();
        return bitmap;
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    /**
     * Opens a custom dialog to let the user edit their username and email.
     */
    private void showEditInfoDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        EditText etUsername = dialog.findViewById(R.id.et_username);
        EditText etEmail = dialog.findViewById(R.id.et_email);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        // Pre-fill fields with current info
        etUsername.setText(sessionManager.getUsername());
        executorService.execute(() -> {
            String email = dbHelper.getUserEmail(currentUserId);
            runOnUiThread(() -> etEmail.setText(email));
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                ToastHelper.showCustomToast(this, "Please fill all fields");
                return;
            }

            executorService.execute(() -> {
                boolean success = dbHelper.updateUserInfo(currentUserId, newUsername, newEmail);
                runOnUiThread(() -> {
                    if (success) {
                        sessionManager.updateUsername(newUsername);
                        updateUI();
                        dialog.dismiss();
                        ToastHelper.showCustomToast(this, "Profile updated successfully");
                    } else {
                        ToastHelper.showCustomToast(this, "Update failed. Username might be taken.");
                    }
                });
            });
        });

        dialog.show();
    }

    /**
     * Shows a confirmation dialog before logging the user out.
     */
    private void showSignOutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sign_out);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSignOutConfirm = dialog.findViewById(R.id.btn_sign_out_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSignOutConfirm.setOnClickListener(v -> {
            sessionManager.logoutUser();
            dialog.dismiss();
            // Go back to the splash screen/login and clear the activity history
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up our background thread when the activity is destroyed
        executorService.shutdown();
    }
}
