package com.example.easytrack.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.easytrack.R;

/**
 * ToastHelper is a handy utility to show consistent, styled messages to the user.
 * Instead of the standard grey Android toast, we use a custom design defined in XML.
 */
public class ToastHelper {

    /**
     * Shows a custom-styled popup message (Toast) at the bottom of the screen.
     * @param activity The activity context where the toast should appear.
     * @param message The text you want to show the user.
     */
    public static void showCustomToast(Activity activity, String message) {
        // Load the custom toast layout from our resources
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_custom_toast, activity.findViewById(R.id.custom_toast_container));

        // Set the message text
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        // Create and show the toast
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT); // Show for a short time
        toast.setView(layout);
        toast.show();
    }
}
