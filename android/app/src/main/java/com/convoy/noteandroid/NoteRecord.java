package com.convoy.noteandroid;

public class NoteRecord {
    public final long id;
    public final String title;
    public final String body;
    public final long createdAt;
    public final long updatedAt;

    public NoteRecord(long id, String title, String body, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
