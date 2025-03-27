package com.android.doctorcube.adminpannel;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks; // Import the Tasks class

/**
 * This class is responsible for loading student data from Firestore.  It retrieves
 * data from both the "registrations" and "xl data" collections and merges them
 * into a single list of Student objects.
 */
public class StudentDataLoader {

    private static final String TAG = "StudentDataLoader"; // Tag for logging
    private FirebaseFirestore firestoreDB; // Firestore instance
    private List<Student> studentList; // List to hold the loaded student data
    private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.getDefault()); // Date format for strings
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); // Date format for Firestore Timestamps

    /**
     * Interface definition for a callback to be invoked when the student data
     * has been loaded.
     */
    public interface DataLoadListener {
        /**
         * Called when the student data has been successfully loaded.
         *
         * @param students The list of Student objects.
         */
        void onDataLoaded(List<Student> students);

        /**
         * Called when there is an error loading the student data.
         *
         * @param error A message describing the error.
         */
        void onDataLoadFailed(String error);
    }

    /**
     * Constructor for StudentDataLoader.  Initializes the Firestore instance
     * and the student list.
     */
    public StudentDataLoader() {
        firestoreDB = FirebaseFirestore.getInstance(); // Get the Firestore instance
        studentList = new ArrayList<>(); // Initialize the student list
    }

    /**
     * Loads student data from Firestore.  This method retrieves data from both
     * the "registrations" and "xl data" collections and merges them into a single
     * list.  It then notifies the listener.
     *
     * @param listener The DataLoadListener to notify when the data is loaded.
     */
    public void loadStudents(final DataLoadListener listener) {
        studentList.clear(); // Clear the list before loading
        loadRegistrationsAndXlData(listener);
    }

    private void loadRegistrationsAndXlData(final DataLoadListener listener) {
        final Task<QuerySnapshot> registrationsTask = firestoreDB.collection("registrations").get();
        final Task<QuerySnapshot> xlDataTask = firestoreDB.collection("xl_data").get();

        Tasks.whenAllSuccess(registrationsTask, xlDataTask) // Use whenAllSuccess
                .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> results) {
                        List<Student> allStudents = new ArrayList<>();
                        //Process the result
                        for (Object result : results) {
                            if (result instanceof QuerySnapshot) {
                                QuerySnapshot snapshot = (QuerySnapshot) result;
                                for (DocumentSnapshot document : snapshot) {
                                    try {
                                        Student student;
                                        if (snapshot.getMetadata().isFromCache()) {
                                            if(document.getReference().getParent().getId().equals("registrations")){
                                                student = createStudentFromRegistration(document);
                                            }
                                            else{
                                                student = createStudentFromXlData(document);
                                            }

                                        }
                                        else{
                                            if(document.getReference().getParent().getId().equals("registrations")){
                                                student = createStudentFromRegistration(document);
                                            }
                                            else{
                                                student = createStudentFromXlData(document);
                                            }
                                        }

                                        if (student != null) {
                                            allStudents.add(student);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing data: " + e.getMessage(), e);
                                        listener.onDataLoadFailed("Error parsing data: " + e.getMessage());
                                        return;
                                    }
                                }
                            }
                        }
                        listener.onDataLoaded(allStudents);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading data: " + e.getMessage(), e);
                        listener.onDataLoadFailed("Failed to load data: " + e.getMessage());
                    }
                });
    }


    /**
     * Creates a Student object from a DocumentSnapshot from the "registrations"
     * collection.
     *
     * @param document The DocumentSnapshot.
     * @return A Student object, or null if there's an error.
     */
    private Student createStudentFromRegistration(DocumentSnapshot document) throws ParseException {
        Student student = new Student();
        student.setId(document.getId());  // Use document ID
        student.setName(FirestoreDataHelper.getString(document, "name"));
        student.setMobile(FirestoreDataHelper.getString(document, "mobile"));
        student.setEmail(FirestoreDataHelper.getString(document, "email"));
        student.setState(FirestoreDataHelper.getString(document, "state"));
        student.setCity(FirestoreDataHelper.getString(document, "city"));
        student.setInterestedCountry(FirestoreDataHelper.getString(document, "interestedCountry"));
        student.setHasNeetScore(FirestoreDataHelper.getString(document, "hasNeetScore"));
        student.setNeetScore(FirestoreDataHelper.getString(document, "neetScore"));
        student.setHasPassport(FirestoreDataHelper.getString(document, "hasPassport"));
        student.setCallStatus(FirestoreDataHelper.getString(document, "callStatus", "pending"));
        student.setIsInterested(FirestoreDataHelper.getBoolean(document, "isInterested", false));
        student.setAdmitted(FirestoreDataHelper.getBoolean(document, "isAdmitted", false));
        Date registrationDate = FirestoreDataHelper.getTimestamp(document, "registrationDate");
        if (registrationDate != null) {
            student.setSubmissionDate(dateFormat.format(registrationDate));
            student.setFirebasePushDate(dateFormat.format(registrationDate));
        } else {
            Log.w(TAG, "Registration Date is  null for document: " + document.getId());
        }

        return student;
    }

    /**
     * Creates a Student object from a DocumentSnapshot from the "xl data"
     * collection.
     *
     * @param document The DocumentSnapshot.
     * @return A Student object, or null if there's an error.
     */
    private Student createStudentFromXlData(DocumentSnapshot document) throws ParseException {
        Student student = new Student();
        student.setId(document.getId()); // Use document ID
        student.setName(FirestoreDataHelper.getString(document, "name"));
        student.setMobile(FirestoreDataHelper.getString(document, "mobile"));
        student.setEmail(FirestoreDataHelper.getString(document, "email"));
        student.setState(FirestoreDataHelper.getString(document, "state"));
        student.setCity(FirestoreDataHelper.getString(document, "city"));
        student.setInterestedCountry(FirestoreDataHelper.getString(document, "interestedCountry"));
        student.setHasNeetScore(FirestoreDataHelper.getString(document, "hasNeetScore"));
        student.setNeetScore(FirestoreDataHelper.getString(document, "neetScore"));
        student.setHasPassport(FirestoreDataHelper.getString(document, "hasPassport"));
        student.setCallStatus(FirestoreDataHelper.getString(document, "callStatus", "pending"));
        student.setIsInterested(FirestoreDataHelper.getBoolean(document, "isInterested", false));
        student.setAdmitted(FirestoreDataHelper.getBoolean(document, "isAdmitted", false));
        Date uploadDate = FirestoreDataHelper.getTimestamp(document, "uploadDate");
        if (uploadDate != null) {
            student.setSubmissionDate(dateFormat.format(uploadDate));
            student.setFirebasePushDate(dateFormat.format(uploadDate));
        } else {
            Log.w(TAG, "Upload Date is null for document: " + document.getId());
        }
        return student;
    }

    /**
     * Updates a student's data in Firestore.  This method allows you to update
     * a specific field for a student in either the "registrations" or "xl data"
     * collection.
     *
     * @param collection The Firestore collection ("registrations" or "xl data").
     * @param documentId The ID of the document to update.
     * @param field The field to update.
     * @param value The new value for the field.
     */
    public void updateStudent(String collection, String documentId, String field, Object value) {
        firestoreDB.collection(collection).document(documentId)
                .update(field, value)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Student data updated successfully for document: " + documentId + " in collection " + collection))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update student data: " + e.getMessage()));
    }

    /**
     * Helper class to safely extract data from Firestore DocumentSnapshot.  This
     * class provides methods to get data of various types from a DocumentSnapshot,
     * handling null values and potential exceptions.
     */
    private static class FirestoreDataHelper {

        /**
         * Gets a string value from a DocumentSnapshot.
         *
         * @param snapshot The DocumentSnapshot.
         * @param key The key of the field to retrieve.
         * @return The string value, or null if the field is not found or is null.
         */
        static String getString(DocumentSnapshot snapshot, String key) {
            return getString(snapshot, key, null);
        }

        /**
         * Gets a string value from a DocumentSnapshot with a default value.
         *
         * @param snapshot The DocumentSnapshot.
         * @param key The key of the field to retrieve.
         * @param defaultValue The default value to return if the field is not
         * found or is null.
         * @return The string value, or the default value if the field is not
         * found or is null.
         */
        static String getString(DocumentSnapshot snapshot, String key, String defaultValue) {
            if (snapshot.contains(key)) {
                try {
                    Object value = snapshot.get(key);
                    if (value != null) {
                        return value.toString().trim();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting string for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        /**
         * Gets a boolean value from a DocumentSnapshot with a default value.
         *
         * @param snapshot The DocumentSnapshot.
         * @param key The key of the field to retrieve.
         * @param defaultValue The default value to return if the field is not
         * found or is null.
         * @return The boolean value, or the default value if the field is not
         * found or is null.
         */
        static Boolean getBoolean(DocumentSnapshot snapshot, String key, boolean defaultValue) {
            if (snapshot.contains(key)) {
                try {
                    Boolean value = snapshot.getBoolean(key);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting boolean for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        /**
         * Gets a Long value from a DocumentSnapshot with a default value.
         *
         * @param snapshot The DocumentSnapshot.
         * @param key The key of the field to retrieve.
         * @param defaultValue The default value to return if the field is not
         * found or is null.
         * @return The Long value, or the default value if the field is not
         * found or is null.
         */
        static Long getLong(DocumentSnapshot snapshot, String key, Long defaultValue) {
            if (snapshot.contains(key)) {
                try {
                    Long value = snapshot.getLong(key);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting Long for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        /**
         * Gets a Double value from a DocumentSnapshot with a default value.
         *
         * @param snapshot The DocumentSnapshot.
         * @param key The key of the field to retrieve.
         * @param defaultValue The default value to return if the field is not
         * found or is null.
         * @return The Double value, or the default value if the field is not
         * found or is null.
         */
        static Double getDouble(DocumentSnapshot snapshot, String key, Double defaultValue) {
            if (snapshot.contains(key)) {
                try {
                    Double value = snapshot.getDouble(key);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting Double for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        /**
         * Gets a Date value from a DocumentSnapshot, specifically for Firestore
         * Timestamps.
         *
         * @param snapshot The DocumentSnapshot.
         * @param key The key of the field to retrieve.
         * @return The Date value, or null if the field is not found or is null.
         */
        static Date getTimestamp(DocumentSnapshot snapshot, String key) {
            if (snapshot.contains(key)) {
                try {
                    com.google.firebase.Timestamp timestamp = snapshot.getTimestamp(key);
                    if (timestamp != null) {
                        return timestamp.toDate();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting Timestamp for key " + key + ": " + e.getMessage());
                    return null;
                }
            }
            return null;
        }

        /**
         * Gets a Date value from a DocumentSnapshot, formatted as a string.
         *
         * @param snapshot The DocumentSnapshot.
         * @param key The key of the field to retrieve.
         * @param dateFormat The SimpleDateFormat to use for parsing the date
         * string.
         * @param defaultValue The default value to return if the field is not
         * found or is null.
         * @return The Date value, or the default value if the field is not
         * found or is null.
         */
        static Date getDate(DocumentSnapshot snapshot, String key, SimpleDateFormat dateFormat, Date defaultValue) {
            if (snapshot.contains(key)) {
                try {
                    String dateString = snapshot.getString(key);
                    if (dateString != null) {
                        return dateFormat.parse(dateString);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }
    }
}

