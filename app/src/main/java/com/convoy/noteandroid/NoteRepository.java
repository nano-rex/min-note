package com.convoy.noteandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class NoteRepository {
    private final NoteDbOpenHelper helper;

    public NoteRepository(Context context) {
        this.helper = new NoteDbOpenHelper(context);
    }

    public List<NoteRecord> listNotes(String query) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String trimmed = query == null ? "" : query.trim();
        String sql = "SELECT id, title, body, created_at, updated_at FROM notes";
        String[] args = null;
        if (!trimmed.isEmpty()) {
            sql += " WHERE title LIKE ?";
            args = new String[]{"%" + trimmed + "%"};
        }
        sql += " ORDER BY updated_at DESC, id DESC";
        Cursor cursor = db.rawQuery(sql, args);
        ArrayList<NoteRecord> notes = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                notes.add(new NoteRecord(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getLong(4)
                ));
            }
        } finally {
            cursor.close();
        }
        return notes;
    }

    public NoteRecord getNote(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, title, body, created_at, updated_at FROM notes WHERE id = ?",
                new String[]{String.valueOf(id)}
        );
        try {
            if (!cursor.moveToFirst()) return null;
            return new NoteRecord(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getLong(3),
                    cursor.getLong(4)
            );
        } finally {
            cursor.close();
        }
    }

    public long createNote(String title, String body) {
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("body", body == null ? "" : body);
        values.put("created_at", now);
        values.put("updated_at", now);
        return helper.getWritableDatabase().insertOrThrow("notes", null, values);
    }

    public void updateNote(long id, String title, String body) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("body", body == null ? "" : body);
        values.put("updated_at", System.currentTimeMillis());
        helper.getWritableDatabase().update("notes", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteNote(long id) {
        helper.getWritableDatabase().delete("notes", "id = ?", new String[]{String.valueOf(id)});
    }
}
