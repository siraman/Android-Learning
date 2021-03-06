package com.example.notekeeper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.example.notekeeper.NoteKeeperDatabaseContract.*;

public class NoteKeeperProvider extends ContentProvider {
    NoteKeeperOpenHelper dbOpenHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int COURSES = 0;
    private static final int NOTES = 1;
    private static final int NOTES_EXPANDED = 2;

    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH_EXPANDED, NOTES_EXPANDED);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        dbOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch){
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
        }

        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {
        String[] columns = new String[projection.length];
        for(int idx = 0 ; idx < projection.length; idx++){
            columns[idx] = projection[idx].equals(BaseColumns._ID) ||
                    projection[idx].equals(NoteKeeperProviderContract.CoursesIdColumns.COLUMN_COURSE_ID) ?
                    NoteInfoEntry.getQName(projection[idx]):projection[idx];
        }
        String tableWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
                CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

        return db.query(tableWithJoin,columns,selection,selectionArgs,null,null,sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
