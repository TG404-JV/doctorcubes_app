package com.android.doctorcube.study.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private List<NoteItem> originalNotesList;
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
        notesList = new ArrayList<>(originalNotesList);

        // Set up adapter
        adapter = new NotesAdapter(requireContext(), notesList);
        recyclerView.setAdapter(adapter);

        // Update empty state initially
        updateEmptyState();

        return view;
    }

    private List<NoteItem> getNotesList() {
        List<NoteItem> notes = new ArrayList<>();
        notes.add(new NoteItem("Mathematics", "Calculus notes", "Dr. Smith", "15 MB",
                "https://drive.google.com/uc?export=download&id=1TQwa6iLSPJyyvmimugZ4Nizc7qjc0psw",
                "Math", "March 10, 2025", 50));
        notes.add(new NoteItem("Physics", "Mechanics fundamentals", "Prof. Johnson", "12 MB",
                "https://drive.google.com/file/d/1aitdpqtldgIGTxmk72LI13J_HhruV5qN/view?usp=sharing",
                "Physics", "March 12, 2025", 45));
        notes.add(new NoteItem("Chemistry", "Organic Chemistry Basics", "Dr. Anderson", "8 MB",
                "https://drive.google.com/uc?export=download&id=1QWERTYUIOPLKJHGFDSA",
                "Chemistry", "March 14, 2025", 30));
        notes.add(new NoteItem("Biology", "Human Anatomy Overview", "Prof. Green", "20 MB",
                "https://drive.google.com/uc?export=download&id=1BIOLOGYPDF123XYZ",
                "Biology", "March 15, 2025", 60));
        notes.add(new NoteItem("Computer Science", "Data Structures & Algorithms", "Dr. Lee", "25 MB",
                "https://drive.google.com/uc?export=download&id=1CSALGO987654321",
                "CS", "March 16, 2025", 80));
        notes.add(new NoteItem("History", "World War II Summary", "Dr. Carter", "10 MB",
                "https://drive.google.com/uc?export=download&id=1HISTORYWWII9876",
                "History", "March 17, 2025", 35));
        return notes;
    }

    @Override
    public void performSearch(String query) {
        List<NoteItem> filteredList = new ArrayList<>();
        query = query.toLowerCase();

        for (NoteItem note : originalNotesList) {
            if (note.getTitle().toLowerCase().contains(query) ||
                    note.getDescription().toLowerCase().contains(query) ||
                    note.getAuthor().toLowerCase().contains(query) ||
                    note.getCategory().toLowerCase().contains(query)) {
                filteredList.add(note);
            }
        }

        adapter.updateData(filteredList);
        updateEmptyState(query, filteredList.isEmpty());
    }

    @Override
    public void resetSearch() {
        adapter.updateData(originalNotesList);
        updateEmptyState("", false);
    }

    private void updateEmptyState() {
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
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}