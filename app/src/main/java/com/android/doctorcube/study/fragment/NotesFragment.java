package com.android.doctorcube.study.fragment;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment implements StudyMaterialFragment.SearchableFragment {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<NoteItem> notesList;
    private List<NoteItem> originalNotesList; // Store the original list
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize data
        originalNotesList = getNotesList();
        notesList = new ArrayList<>(originalNotesList); // Create a working copy

        // Set up adapter
        adapter = new NotesAdapter(getContext(), notesList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<NoteItem> getNotesList() {
        List<NoteItem> notes = new ArrayList<>();

        notes.add(new NoteItem("Mathematics", "Calculus notes", "Dr. Smith", "15 MB",
                "https://drive.google.com/uc?export=download&id=1TQwa6iLSPJyyvmimugZ4Nizc7qjc0psw"));

        notes.add(new NoteItem("Physics", "Mechanics fundamentals", "Prof. Johnson", "12 MB",
                "https://drive.google.com/uc?export=download&id=1XYZabcDEF456"));

        notes.add(new NoteItem("Chemistry", "Organic Chemistry Basics", "Dr. Anderson", "8 MB",
                "https://drive.google.com/uc?export=download&id=1QWERTYUIOPLKJHGFDSA"));

        notes.add(new NoteItem("Biology", "Human Anatomy Overview", "Prof. Green", "20 MB",
                "https://drive.google.com/uc?export=download&id=1BIOLOGYPDF123XYZ"));

        notes.add(new NoteItem("Computer Science", "Data Structures & Algorithms", "Dr. Lee", "25 MB",
                "https://drive.google.com/uc?export=download&id=1CSALGO987654321"));

        notes.add(new NoteItem("History", "World War II Summary", "Dr. Carter", "10 MB",
                "https://drive.google.com/uc?export=download&id=1HISTORYWWII9876"));

        return notes;
    }

    @Override
    public void performSearch(String query) {
        List<NoteItem> filteredList = new ArrayList<>();

        for (NoteItem note : originalNotesList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    note.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                    note.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(note);
            }
        }

        updateSearchResults(filteredList, query);
    }

    @Override
    public void resetSearch() {
        // Reset to original list
        notesList.clear();
        notesList.addAll(originalNotesList);
        adapter.notifyDataSetChanged();

        // Hide empty view if it was visible
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void updateSearchResults(List<NoteItem> filteredList, String query) {
        notesList.clear();
        notesList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            if (emptyView != null) {
                emptyView.setText("No notes found for: " + query);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(requireContext(), "No notes found for: " + query, Toast.LENGTH_SHORT).show();
            }
        } else if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }
}