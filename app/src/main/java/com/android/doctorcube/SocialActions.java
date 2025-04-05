package com.android.doctorcube;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log; // Import Log for error handling

import com.android.doctorcube.authentication.datamanager.EncryptedSharedPreferencesManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SocialActions {

    private static final String TAG = "SocialActions"; // Add a TAG for logging


    public void openWhatsApp(Context context, String message) {
        String phoneNumber = context.getString(R.string.whatsapp_number); // Use string resource
        String defaultMessage = "Hello, I would like to know more about your consultancy services for MBBS admissions abroad. Please provide the necessary details.";
        if (message != null && !message.isEmpty()) {
            defaultMessage = "Hello, I would like to know more about your consultancy services for MBBS admissions abroad. Please provide the necessary details regarding " + message;
        }

        String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(defaultMessage);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        // Use resolveActivity to avoid crashes.
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            CustomToast.showToast((Activity) context, "WhatsApp not installed");
            Log.e(TAG, "WhatsApp not installed on this device."); // Log the error
        }
    }

    public void openWhatsApp(Context context) {
        openWhatsApp(context, null);
    }

    public void makeDirectCall(Context context) {
        String phoneNumber = context.getString(R.string.whatsapp_number); // Use string resource
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        // Use try-catch for SecurityException, and check for Activity
        try {
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                CustomToast.showToast((Activity) context, "Cannot make call");
                Log.e(TAG, "No app can handle calls on this device.");
            }

        } catch (SecurityException e) {
            CustomToast.showToast((Activity) context, "Permission denied.  Make sure you have CALL_PHONE permission.");
            Log.e(TAG, "SecurityException: ", e); // Log the exception
        }
    }

    public void openInstagram(Context context) {
        openSocialMediaApp(context, context.getString(R.string.instagram_link), "Instagram not installed");
    }

    public void openTwitter(Context context) {
        openSocialMediaApp(context, context.getString(R.string.twitter_link), "Twitter not installed");
    }

    public void openYouTube(Context context) {
        openSocialMediaApp(context, context.getString(R.string.youtube_link), "YouTube not installed");
    }

    // Refactor common social media opening logic
    private void openSocialMediaApp(Context context, String urlString, String errorMessage) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(urlString));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            CustomToast.showToast((Activity) context, errorMessage);
            Log.e(TAG, errorMessage);
        }
    }

    public void openEmailApp(Context context) {
        String email = context.getString(R.string.email_address);
        String subject = "Consultancy for Taking Admission in MBBS Abroad";
        StringBuilder message = new StringBuilder(); // Use StringBuilder for efficiency

        // Get user data *before* starting the async operation.
        EncryptedSharedPreferencesManager encryptedSharedPreferencesManager = new EncryptedSharedPreferencesManager(context);
        String fullName = encryptedSharedPreferencesManager.getString("name", "");
        String phone = encryptedSharedPreferencesManager.getString("phone", "");
        String city = encryptedSharedPreferencesManager.getString("city", "");
        String state = encryptedSharedPreferencesManager.getString("state", "");
        String country = encryptedSharedPreferencesManager.getString("country", "");

        message.append("Hello,\n\nI would like to know more about the consultancy services for MBBS admissions abroad. ")
                .append("Here are my details:\n")
                .append("Name: ").append(fullName).append("\n")
                .append("Phone: ").append(phone).append("\n")
                .append("City: ").append(city).append("\n")
                .append("State: ").append(state).append("\n")
                .append("Country Where I Want To Study: ").append(country).append("\n\n")
                .append("Please provide the necessary details.\nThank you.");

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(message.toString())));
        context.startActivity(Intent.createChooser(intent, "Send Email"));

    }
}

