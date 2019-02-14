package com.example.notekeeper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteKeeperOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NoteKeeper.db";
    public static final int DATABASE_VERSION = 2;

    public NoteKeeperOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(NoteKeeperDatabaseContract.CourseInfoEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(NoteKeeperDatabaseContract.NoteInfoEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(NoteKeeperDatabaseContract.CourseInfoEntry.SQL_CREATE_INDEX1);
        sqLiteDatabase.execSQL(NoteKeeperDatabaseContract.NoteInfoEntry.SQL_CREATE_INDEX1);

        DatabaseDataWorker worker = new DatabaseDataWorker(sqLiteDatabase);
        worker.insertCourses();
        worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if(i < 2){
            sqLiteDatabase.execSQL(NoteKeeperDatabaseContract.CourseInfoEntry.SQL_CREATE_INDEX1);
            sqLiteDatabase.execSQL(NoteKeeperDatabaseContract.NoteInfoEntry.SQL_CREATE_INDEX1);
        }
    }
}
