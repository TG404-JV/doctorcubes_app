package com.android.doctorcube.study.fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.PdfViewerActivity;
import com.android.doctorcube.R;
import com.android.doctorcube.study.fragment.models.NoteItem;

import java.util.List;

// NotesAdapter.java
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<NoteItem> notesList;
    private Context context;

    public NotesAdapter(Context context, List<NoteItem> notesList) {
        this.context = context;
        this.notesList = notesList;
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
        holder.titleTextView.setText(note.getTitle());
        holder.descriptionTextView.setText(note.getDescription());
        holder.authorTextView.setText("by " + note.getAuthor());
        holder.sizeTextView.setText(note.getSize());

        // Open PDF when item is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PdfViewerActivity.class);
            intent.putExtra("pdfTitle", note.getTitle());
            intent.putExtra("pdfDescription", note.getDescription());
            intent.putExtra("pdfSize", note.getSize());
            intent.putExtra("pdfFilePath", note.getPdfUrl()); // PDF file path (assets or URL)
            context.startActivity(intent);
        });

        holder.downloadButton.setOnClickListener(v -> {
            // Implement download functionality (if needed)
        });
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public void updateData(List<NoteItem> newData) {
        this.notesList = newData;
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView authorTextView;
        TextView sizeTextView;
        ImageView downloadButton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            sizeTextView = itemView.findViewById(R.id.sizeTextView);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }
    }
}
