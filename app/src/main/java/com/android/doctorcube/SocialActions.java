package com.android.doctorcube;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SocialActions {

    public void openWhatsApp(Context context) {
        String phoneNumber = context.getString(R.string.whatsapp_number);
        String defaultMessage = "Hello, I would like to know more about your consultancy services for MBBS admissions abroad. Please provide the necessary details.";
        String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(defaultMessage);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }


    public void openWhatsApp(Context context ,String message) {
        String phoneNumber = context.getString(R.string.whatsapp_number);
        String defaultMessage = "Hello, I would like to know more about your consultancy services for MBBS admissions abroad. Please provide the necessary details. regarding " +message;
        String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(defaultMessage);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public void makeDirectCall(Context context) {
        String phoneNumber = context.getString(R.string.whatsapp_number);
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            context.startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(context, "Call permission not granted", Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Dialer not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void openInstagram(Context context) {
        String url = context.getString(R.string.instagram_link);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Instagram not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public void openTwitter(Context context) {
        String url = context.getString(R.string.twitter_link);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Twitter not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public void openYouTube(Context context) {
        String url = context.getString(R.string.youtube_link);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "YouTube not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public void openEmailApp(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Get the current user ID dynamically
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        String email = context.getString(R.string.email_address);
        String subject = "Consultancy for Taking Admission in MBBS Abroad";

        // Use a StringBuilder to make the message effectively final
        StringBuilder message = new StringBuilder("Hello,\n\nI would like to know more about the consultancy services for MBBS admissions abroad. "
                + "Please provide the necessary details.\nThank you.");

        if (userId != null) {
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String phone = documentSnapshot.getString("phone");
                            String city = documentSnapshot.getString("city");
                            String state = documentSnapshot.getString("state");
                            String country = documentSnapshot.getString("country");

                            message.setLength(0); // Clear the existing message
                            message.append("Hello,\n\nI would like to know more about the consultancy services for MBBS admissions abroad. ")
                                    .append("Here are my details:\n")
                                    .append("Name: ").append(fullName).append("\n")
                                    .append("Phone: ").append(phone).append("\n")
                                    .append("City: ").append(city).append("\n")
                                    .append("State: ").append(state).append("\n")
                                    .append("Country: ").append(country).append("\n\n")
                                    .append("Please provide the necessary details.\nThank you.");
                        }

                        // Create and start the email intent
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:" + email + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(message.toString())));
                        context.startActivity(Intent.createChooser(intent, "Send Email"));
                    })
                    .addOnFailureListener(e -> {
                        // Start email intent even if fetching data fails
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:" + email + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(message.toString())));
                        context.startActivity(Intent.createChooser(intent, "Send Email"));
                    });
        } else {
            // Start email intent even if user is not logged in
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(message.toString())));
            context.startActivity(Intent.createChooser(intent, "Send Email"));
        }
    }
}
