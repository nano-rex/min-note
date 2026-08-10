package com.convoy.noteandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private NoteRepository repository;
    private NoteListAdapter adapter;
    private EditText etSearch;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new NoteRepository(this);
        adapter = new NoteListAdapter(this, note -> openEditor(note.id), this::confirmDelete);
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
        ListView lvNotes = findViewById(R.id.lvNotes);
        Button btnNewNote = findViewById(R.id.btnNewNote);

        lvNotes.setAdapter(adapter);
        btnNewNote.setOnClickListener(v -> openEditor(0));
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        adapter.setNotes(repository.listNotes(etSearch.getText().toString()));
        tvEmpty.setText(adapter.isEmpty() ? "No notes yet." : "");
    }

    private void openEditor(long noteId) {
        Intent intent = new Intent(this, NoteEditorActivity.class);
        intent.putExtra("note_id", noteId);
        startActivity(intent);
    }

    private void confirmDelete(NoteRecord note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete note")
                .setMessage("Remove this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteNote(note.id);
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
