package com.android.doctorcube.study.fragment;

// NotesFragment.java

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize data
        notesList = getNotesList();

        // Set up adapter
        adapter = new NotesAdapter(getContext(), notesList); // If inside a Fragment
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<NoteItem> getNotesList() {
        List<NoteItem> notes = new ArrayList<>();
        notes.add(new NoteItem("Mathematics", "Calculus notes", "Dr. Smith", "15 MB",
                "https://drive.google.com/viewerng/viewer?embedded=true&url=https://yourfileurl.com/sample.pdf"));

        notes.add(new NoteItem("Physics", "Mechanics fundamentals", "Prof. Johnson", "12 MB",
                "https://drive.google.com/viewerng/viewer?embedded=true&url=https://yourfileurl.com/physics.pdf"));

        return notes;
    }


    @Override
    public void performSearch(String query) {
        List<NoteItem> filteredList = new ArrayList<>();
        for (NoteItem note : notesList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    note.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(note);
            }
        }
        adapter.updateData(filteredList);

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "No notes found for: " + query, Toast.LENGTH_SHORT).show();
        }
    }
}
