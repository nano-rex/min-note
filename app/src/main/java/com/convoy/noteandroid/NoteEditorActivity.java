package com.convoy.noteandroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NoteEditorActivity extends AppCompatActivity {
    private NoteRepository repository;
    private long noteId;
    private EditText etTitle;
    private EditText etBody;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        repository = new NoteRepository(this);
        noteId = getIntent().getLongExtra("note_id", 0);
        etTitle = findViewById(R.id.etTitle);
        etBody = findViewById(R.id.etBody);
        tvStatus = findViewById(R.id.tvStatus);

        findViewById(R.id.btnSave).setOnClickListener(v -> saveNote());
        findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());

        loadNote();
    }

    private void loadNote() {
        if (noteId == 0) {
            tvStatus.setText("New note");
            return;
        }
        NoteRecord note = repository.getNote(noteId);
        if (note == null) {
            tvStatus.setText("Note not found");
            return;
        }
        etTitle.setText(note.title);
        etBody.setText(note.body == null ? "" : note.body);
        tvStatus.setText("Editing note");
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            tvStatus.setText("Title is required");
            return;
        }
        String body = etBody.getText().toString();
        if (noteId == 0) {
            noteId = repository.createNote(title, body);
            tvStatus.setText("Note created");
        } else {
            repository.updateNote(noteId, title, body);
            tvStatus.setText("Note updated");
        }
    }

    private void confirmDelete() {
        if (noteId == 0) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete note")
                .setMessage("Remove this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteNote(noteId);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
