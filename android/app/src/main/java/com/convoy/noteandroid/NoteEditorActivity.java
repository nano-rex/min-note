package com.convoy.noteandroid;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class NoteEditorActivity extends AppCompatActivity {
    private NoteRepository repository;
    private long noteId;
    private EditText etTitle;
    private EditText etBody;
    private EditText etFind;
    private EditText etReplace;
    private TextView tvLineNumbers;
    private TextView tvStatus;
    private VerticalSeekBar sbBodyScroll;
    private boolean updatingScroll;
    private int foundCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        repository = new NoteRepository(this);
        noteId = getIntent().getLongExtra("note_id", 0);
        etTitle = findViewById(R.id.etTitle);
        etBody = findViewById(R.id.etBody);
        etFind = findViewById(R.id.etFind);
        etReplace = findViewById(R.id.etReplace);
        tvLineNumbers = findViewById(R.id.tvLineNumbers);
        tvStatus = findViewById(R.id.tvStatus);
        sbBodyScroll = findViewById(R.id.sbBodyScroll);

        findViewById(R.id.btnSave).setOnClickListener(v -> saveNote());
        findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());
        findViewById(R.id.btnReplace).setOnClickListener(v -> replaceFoundWords());

        etBody.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLineNumbers();
                highlightFoundWords();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        etFind.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                highlightFoundWords();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        etBody.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> syncEditorScroll());
        etBody.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            updateLineNumbers();
            syncEditorScroll();
        });
        sbBodyScroll.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser || updatingScroll) {
                    return;
                }
                int maxScroll = getMaxBodyScroll();
                etBody.scrollTo(0, Math.round(maxScroll * (progress / 100f)));
                tvLineNumbers.scrollTo(0, etBody.getScrollY());
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        loadNote();
        updateLineNumbers();
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

    private void updateLineNumbers() {
        int lineCount = Math.max(1, etBody.getLineCount());
        StringBuilder lines = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            if (i > 1) {
                lines.append('\n');
            }
            lines.append(i);
        }
        tvLineNumbers.setText(lines);
        tvLineNumbers.post(() -> tvLineNumbers.scrollTo(0, etBody.getScrollY()));
        etBody.post(this::syncEditorScroll);
    }

    private void syncEditorScroll() {
        int maxScroll = getMaxBodyScroll();
        updatingScroll = true;
        sbBodyScroll.setEnabled(maxScroll > 0);
        sbBodyScroll.setProgress(maxScroll == 0 ? 0 : Math.round(etBody.getScrollY() * 100f / maxScroll));
        updatingScroll = false;
        tvLineNumbers.scrollTo(0, etBody.getScrollY());
    }

    private int getMaxBodyScroll() {
        if (etBody.getLayout() == null) {
            return 0;
        }
        int contentHeight = etBody.getLayout().getHeight()
                + etBody.getCompoundPaddingTop()
                + etBody.getCompoundPaddingBottom();
        return Math.max(0, contentHeight - etBody.getHeight());
    }

    private void highlightFoundWords() {
        Editable editable = etBody.getText();
        BackgroundColorSpan[] spans = editable.getSpans(0, editable.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : spans) {
            editable.removeSpan(span);
        }

        String query = etFind.getText().toString();
        foundCount = 0;
        if (query.isEmpty()) {
            return;
        }

        String body = editable.toString();
        String haystack = body.toLowerCase(Locale.ROOT);
        String needle = query.toLowerCase(Locale.ROOT);
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            editable.setSpan(
                    new BackgroundColorSpan(Color.rgb(255, 235, 130)),
                    index,
                    index + query.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            foundCount++;
            index = haystack.indexOf(needle, index + query.length());
        }
        if (foundCount > 0) {
            tvStatus.setText(foundCount + " found");
        } else {
            tvStatus.setText("No matches");
        }
    }

    private void replaceFoundWords() {
        String query = etFind.getText().toString();
        if (query.isEmpty()) {
            tvStatus.setText("Enter a word to replace");
            return;
        }
        String replacement = etReplace.getText().toString();
        String body = etBody.getText().toString();
        String haystack = body.toLowerCase(Locale.ROOT);
        String needle = query.toLowerCase(Locale.ROOT);
        int index = haystack.indexOf(needle);
        if (index < 0) {
            tvStatus.setText("No matches");
            return;
        }

        SpannableStringBuilder replaced = new SpannableStringBuilder();
        int start = 0;
        int replacements = 0;
        while (index >= 0) {
            replaced.append(body, start, index);
            replaced.append(replacement);
            start = index + query.length();
            replacements++;
            index = haystack.indexOf(needle, start);
        }
        replaced.append(body, start, body.length());
        etBody.setText(replaced);
        etBody.setSelection(Math.min(replaced.length(), start));
        tvStatus.setText(replacements + " replaced");
    }
}
