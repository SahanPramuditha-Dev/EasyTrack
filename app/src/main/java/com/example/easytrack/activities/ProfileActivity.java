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

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId;
    private ShapeableImageView ivUserAvatar;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    executorService.execute(() -> {
                        try {
                            Bitmap bitmap = uriToBitmap(uri);
                            if (bitmap != null) {
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                                byte[] imageBytes = bitmapToByteArray(scaledBitmap);
                                
                                boolean success = dbHelper.updateUserImage(currentUserId, imageBytes);
                                runOnUiThread(() -> {
                                    if (success) {
                                        ivUserAvatar.setImageBitmap(scaledBitmap);
                                        ivUserAvatar.setPadding(0, 0, 0, 0);
                                        ToastHelper.showCustomToast(this, "Profile image updated");
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> ToastHelper.showCustomToast(this, "Failed to load image"));
                        }
                    });
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        String username = sessionManager.getUsername();

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

        TextView tvProfileName = findViewById(R.id.tv_profile_name);
        TextView tvInfoUsername = findViewById(R.id.tv_info_username);
        TextView tvInfoEmail = findViewById(R.id.tv_info_email);
        ivUserAvatar = findViewById(R.id.iv_user_avatar);

        tvProfileName.setText(username);
        tvInfoUsername.setText(username);
        
        executorService.execute(() -> {
            String email = dbHelper.getUserEmail(currentUserId);
            runOnUiThread(() -> tvInfoEmail.setText(email));
        });

        loadProfileImage();

        ivUserAvatar.setOnClickListener(v -> showAvatarOptionsDialog());

        findViewById(R.id.btn_edit_info).setOnClickListener(v -> showEditInfoDialog());
        findViewById(R.id.btn_sign_out).setOnClickListener(v -> showSignOutDialog());

        findViewById(R.id.nav_tasks).setOnClickListener(v -> {
            startActivity(new Intent(this, TasksActivity.class));
            finish();
        });

        findViewById(R.id.nav_about).setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
            finish();
        });

        findViewById(R.id.pill_profile).setVisibility(android.view.View.VISIBLE);
        ((android.widget.TextView)findViewById(R.id.tv_profile)).setTextColor(getResources().getColor(R.color.button_green));
        ((android.widget.ImageView)findViewById(R.id.iv_profile)).setColorFilter(getResources().getColor(R.color.button_green));
    }

    private void loadProfileImage() {
        executorService.execute(() -> {
            byte[] imageBytes = dbHelper.getUserImage(currentUserId);
            runOnUiThread(() -> {
                if (imageBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    ivUserAvatar.setImageBitmap(bitmap);
                    ivUserAvatar.setPadding(0, 0, 0, 0);
                } else {
                    ivUserAvatar.setImageResource(R.drawable.ic_profile);
                    ivUserAvatar.setPadding(20, 20, 20, 20);
                }
            });
        });
    }

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

    private void showEditInfoDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

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
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
