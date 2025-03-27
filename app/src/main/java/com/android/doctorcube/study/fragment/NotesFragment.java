package com.android.doctorcube.study.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.R;
import com.android.doctorcube.StudyMaterialFragment;
import com.android.doctorcube.study.fragment.adapter.NotesAdapter;
import com.android.doctorcube.study.fragment.models.NoteItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment implements StudyMaterialFragment.SearchableFragment {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<NoteItem> notesList;
    private List<NoteItem> originalNotesList;
    private TextView emptyView;
    private FirebaseFirestore db;
    private boolean isDataLoaded = false;
    private String pendingSearchQuery = null;
    private static final String TAG = "NotesFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);

        Log.d(TAG, "onCreateView: RecyclerView found: " + (recyclerView != null));
        Log.d(TAG, "onCreateView: EmptyView found: " + (emptyView != null));

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        notesList = new ArrayList<>();
        originalNotesList = new ArrayList<>();
        adapter = new NotesAdapter(requireContext(), notesList);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "onCreateView: Adapter set with initial size: " + adapter.getItemCount());

        db = FirebaseFirestore.getInstance();

        fetchNotesFromFirestore();

        return view;
    }

    private void fetchNotesFromFirestore() {
        if (getContext() == null) {
            Log.w(TAG, "fetchNotesFromFirestore: Context is null, aborting");
            return;
        }

        emptyView.setText("Loading notes...");
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        Log.d(TAG, "fetchNotesFromFirestore: Initial visibility - RecyclerView: GONE, EmptyView: VISIBLE");

        db.collection("notes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) {
                        Log.w(TAG, "fetchNotesFromFirestore: Fragment not attached, aborting");
                        return;
                    }

                    if (task.isSuccessful()) {
                        List<NoteItem> fetchedNotes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String notesUrl = document.getString("notesUrl");
                            Log.d(TAG, "fetchNotesFromFirestore: Fetched - Title: " + title + ", URL: " + notesUrl);
                            if (title != null && notesUrl != null) {
                                fetchedNotes.add(new NoteItem(title, notesUrl));
                            }
                        }
                        Log.d(TAG, "fetchNotesFromFirestore: Total fetched notes: " + fetchedNotes.size());
                        updateAdapterAndFinishLoading(fetchedNotes);
                    } else {
                        Log.e(TAG, "fetchNotesFromFirestore: Error fetching notes: ", task.getException());
                        Toast.makeText(getContext(), "Error fetching notes: " + task.getException(), Toast.LENGTH_LONG).show();
                        emptyView.setText("Failed to load notes");
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        isDataLoaded = true;
                    }
                });
    }

    private void updateAdapterAndFinishLoading(List<NoteItem> fetchedNotes) {
        if (!isAdded() || getContext() == null) {
            Log.w(TAG, "updateAdapterAndFinishLoading: Fragment not attached, aborting");
            return;
        }

        notesList.clear();
        notesList.addAll(fetchedNotes);
        originalNotesList.clear();
        originalNotesList.addAll(fetchedNotes);

        Log.d(TAG, "updateAdapterAndFinishLoading: Notes list size before adapter update: " + notesList.size());
        adapter.updateData(notesList);
        Log.d(TAG, "updateAdapterAndFinishLoading: Adapter item count after updateData: " + adapter.getItemCount());

        isDataLoaded = true;

        if (!fetchedNotes.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            Log.d(TAG, "updateAdapterAndFinishLoading: RecyclerView visible with " + adapter.getItemCount() + " items");
        } else {
            updateEmptyState("", true);
        }

        if (pendingSearchQuery != null && !pendingSearchQuery.isEmpty()) {
            Log.d(TAG, "updateAdapterAndFinishLoading: Applying pending search: " + pendingSearchQuery);
            performSearch(pendingSearchQuery);
            pendingSearchQuery = null;
        }
    }

    @Override
    public void performSearch(String query) {
        if (!isAdded() || getContext() == null) return;

        if (!isDataLoaded) {
            pendingSearchQuery = query;
            return;
        }

        List<NoteItem> filteredList = new ArrayList<>();
        query = query.toLowerCase();

        for (NoteItem note : originalNotesList) {
            if (note.getTitle().toLowerCase().contains(query) ||
                    note.getUrl().toLowerCase().contains(query)) {
                filteredList.add(note);
            }
        }

        adapter.updateData(filteredList);
        updateEmptyState(query, filteredList.isEmpty());
    }

    @Override
    public void resetSearch() {
        if (!isAdded() || getContext() == null) return;

        pendingSearchQuery = null;
        adapter.updateData(originalNotesList);
        updateEmptyState("", adapter.getItemCount() == 0);
    }

    private void updateEmptyState(String query, boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            if (!query.isEmpty()) {
                emptyView.setText("No notes found for: " + query);
            } else {
                emptyView.setText("No notes available");
            }
            Log.d(TAG, "updateEmptyState: Showing empty view - Text: " + emptyView.getText());
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            Log.d(TAG, "updateEmptyState: Showing RecyclerView - Item count: " + adapter.getItemCount());
        }
    }
}