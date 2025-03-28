package com.android.doctorcube.study.fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.R;
import com.android.doctorcube.PdfViewerActivity;
import com.android.doctorcube.study.fragment.models.NoteItem;

import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private Context context;
    private List<NoteItem> notesList;
    private static final String TAG = "NotesAdapter";
    private OnItemClickListener itemClickListener; // Add this listener

    // Add this interface
    public interface OnItemClickListener {
        void onItemClick(NoteItem item);
    }

    public NotesAdapter(Context context, List<NoteItem> notesList) { // Add listener to constructor
        this.context = context;
        this.notesList = new ArrayList<>(notesList);
        Log.d(TAG, "Constructor: Initial list size: " + this.notesList.size());
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteItem note = notesList.get(position);
        holder.title.setText(note.getTitle());
        holder.categoryTextView.setText(note.getCategory());
        holder.detailTextView.setText(note.getDetail());
        holder.descriptionTextView.setText(note.getDescription());
        holder.timestampTextView.setText(note.getTimestamp());

        Log.d(TAG, "onBindViewHolder: Binding position " + position + " - Title: " + note.getTitle());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PdfViewerActivity.class);
            intent.putExtra("pdfTitle", note.getTitle());
            intent.putExtra("pdfFilePath", note.getUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        int size = notesList != null ? notesList.size() : 0;
        Log.d(TAG, "getItemCount: Returning size: " + size);
        return size;
    }

    public void updateData(List<NoteItem> newNotesList) {
        Log.d(TAG, "updateData: Input list size: " + (newNotesList != null ? newNotesList.size() : 0));
        if (newNotesList == null) {
            this.notesList = new ArrayList<>();
        } else {
            this.notesList = new ArrayList<>(newNotesList); // Replace with a new copy
        }
        Log.d(TAG, "updateData: After update - List size: " + notesList.size());
        notifyDataSetChanged();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title,descriptionTextView,categoryTextView,detailTextView,timestampTextView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleTextView);
            descriptionTextView=itemView.findViewById(R.id.descriptionTextView);
            categoryTextView=itemView.findViewById(R.id.categoryTextView);
            detailTextView = itemView.findViewById(R.id.detailTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);


        }
    }
}
