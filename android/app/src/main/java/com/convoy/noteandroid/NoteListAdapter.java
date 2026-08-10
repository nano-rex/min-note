package com.convoy.noteandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteListAdapter extends ArrayAdapter<NoteRecord> {
    public interface OpenHandler { void open(NoteRecord note); }
    public interface DeleteHandler { void delete(NoteRecord note); }

    private final LayoutInflater inflater;
    private final OpenHandler openHandler;
    private final DeleteHandler deleteHandler;

    public NoteListAdapter(Context context, OpenHandler openHandler, DeleteHandler deleteHandler) {
        super(context, 0, new ArrayList<>());
        this.inflater = LayoutInflater.from(context);
        this.openHandler = openHandler;
        this.deleteHandler = deleteHandler;
    }

    public void setNotes(List<NoteRecord> notes) {
        clear();
        addAll(notes);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView == null ? inflater.inflate(R.layout.item_note, parent, false) : convertView;
        NoteRecord note = getItem(position);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvBodyPreview = view.findViewById(R.id.tvBodyPreview);
        TextView tvMeta = view.findViewById(R.id.tvMeta);
        Button btnOpen = view.findViewById(R.id.btnOpen);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        tvTitle.setText(note.title);
        String body = note.body == null ? "" : note.body.trim();
        tvBodyPreview.setText(body.isEmpty() ? "No content" : body);
        String updated = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(note.updatedAt));
        tvMeta.setText("Updated: " + updated);
        btnOpen.setOnClickListener(v -> openHandler.open(note));
        btnDelete.setOnClickListener(v -> deleteHandler.delete(note));
        return view;
    }
}
