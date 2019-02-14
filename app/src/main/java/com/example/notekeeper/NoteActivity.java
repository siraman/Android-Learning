package com.example.notekeeper;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
//import android.support.v4.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.zip.CheckedOutputStream;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    //public static final String NOTE_POSITION ="com.example.notekeeper.NOTE_POSITION";
    public static final String NOTE_ID ="com.example.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_COURSE_TITLE = "com.example.notekeeper.ORIGINAL_NOTE_COURSE_TITLE";
    public static final String ORIGINAL_NOTE_COURSE_TEXT = "com.example.notekeeper.ORIGINAL_NOTE_COURSE_TEXT";
    public static final int ID_NOT_SET = -1;
    private static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private Spinner spinnerCourses;
    private EditText textNoteTitle;
    private EditText textNoteText;
//    private int mNotePosition;
    private boolean mIsCancelling;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteKeeperOpenHelper dbOpenHelper;
    private String selection;
    private Cursor noteCursor;
    private int courseIdPos;
    private int noteTitlePos;
    private int noteTextPos;
    private int mNoteId;
    private SimpleCursorAdapter adapterCourses;
    private boolean coursesQueryFinished;
    private boolean notesQueryFinished;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_COURSE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_COURSE_TEXT, mOriginalNoteText);
    }

    @Override
    protected void onDestroy() {
        dbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbOpenHelper = new NoteKeeperOpenHelper(this);

        spinnerCourses = findViewById(R.id.spinner_courses);
        //List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        ArrayAdapter<CourseInfo> adapterCourses =
//                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
//        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerCourses.setAdapter(adapterCourses);
        adapterCourses = new SimpleCursorAdapter(this,android.R.layout.simple_spinner_item,null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1},0);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapterCourses);
        
//        loadCourseData();
        getLoaderManager().initLoader(LOADER_COURSES,null,this);

        readDisplayStateValues();
        if(savedInstanceState == null)
            saveOriginalStateValues();
        else {
            restoreOriginalNoteValues(savedInstanceState);
        }
        textNoteTitle = findViewById(R.id.text_note_title);
        textNoteText = findViewById(R.id.text_note_text);

        if(!mIsNewNote) {
//            loadNoteData();
            getLoaderManager().initLoader(LOADER_NOTES,null, this);
        }
    }

    private void loadCourseData() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME,courseColumns,
                null,null,null,null,CourseInfoEntry.COLUMN_COURSE_TITLE);
        adapterCourses.changeCursor(cursor);
    }

    private void loadNoteData() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
//
//        String courseId = "android_intents";
//        String titleStart = "dynamic";

//        String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND " +
//                NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ?" ;
//        String[] selectionArgs = {courseId, titleStart + "%"};
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,selection,
                selectionArgs,null, null, null);

        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        noteCursor.moveToNext();

        displayNote();

    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_TEXT);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_TITLE);
    }

    private void saveOriginalStateValues() {
        if(mIsNewNote){
            return;
        }
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    private void displayNote() {
        String courseId = noteCursor.getString(courseIdPos);
        String noteTitle = noteCursor.getString(noteTitlePos);
        String noteText = noteCursor.getString(noteTextPos);
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        CourseInfo course = DataManager.getInstance().getCourse(courseId);
//        int courseIndex = courses.indexOf(course);
        int courseIndex = getIndexOfCourseId(courseId);
        spinnerCourses.setSelection(courseIndex);
        textNoteText.setText(noteText);
        textNoteTitle.setText(noteTitle);
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = adapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while(more){
            String cursorCourseId = cursor.getString(courseIdPos);
            if(courseId.equals(cursorCourseId))
                break;

            courseRowIndex ++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
//        mNote = intent.getParcelableExtra(NOTE_POSITION);
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
//        mIsNewNote = mNote == null;
        mIsNewNote = mNoteId == ID_NOT_SET;
//        if(!mIsNewNote)
//            mNote = DataManager.getInstance().getNotes().get(position);
        if(mIsNewNote){
            createNewNote();
        }
//        else {
//            mNote = DataManager.getInstance().getNotes().get(mNoteId);
//        }
    }

    private void createNewNote() {
//        DataManager dm = DataManager.getInstance();
//        mNoteId = dm.createNewNote();
//        mNote = dm.getNotes().get(mNoteId);

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID,"");
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE,"");
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT,"");

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME,null,values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling) {
            if(mIsNewNote) {
                //DataManager.getInstance().removeNote(mNoteId);
                deleteNoteFromDatabase();
            }
            else {
                storePreviousNoteValues();
            }
        }
        else {
            saveNote();
        }
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME,selection,selectionArgs);
                return null;
            }
        };
        task.execute();
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setText(mOriginalNoteText);
        mNote.setTitle(mOriginalNoteTitle);
    }

    private void saveNote() {
//        mNote.setCourse((CourseInfo)spinnerCourses.getSelectedItem());
//        mNote.setTitle(textNoteTitle.getText().toString());
//        mNote.setText(textNoteText.getText().toString());
        String courseId = selectedCourseId();
        String noteTitle = textNoteTitle.getText().toString();
        String noteText = textNoteText.getText().toString();

        saveNoteToDatabase(courseId,noteTitle,noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = spinnerCourses.getSelectedItemPosition();
        Cursor cursor = adapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);

        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText){
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID,courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE,noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT,noteText);

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.update(NoteInfoEntry.TABLE_NAME, values,selection,selectionArgs);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
        }
        else if (id == R.id.action_next){
            moveNext();
        }
        else if (id == R.id.menu_cancel){
            mIsCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNodeIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNoteId < lastNodeIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);

        saveOriginalStateValues();
        displayNote();

        invalidateOptionsMenu();
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) spinnerCourses.getSelectedItem();
        String subject = textNoteTitle.getText().toString();
        String text = "Checkout what I learnt in the PluralSight course " +
                course.getTitle() + "\n" + textNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);

        startActivity(intent);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        CursorLoader loader = null;
        if(i == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (i == LOADER_COURSES) {
            loader = createLoaderCourses();
        }
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        coursesQueryFinished = false;
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

                String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry._ID
                };
                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,null,
                        null,null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
            }
        };
    }

    private CursorLoader createLoaderNotes() {
        notesQueryFinished = false;
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
                String selection = NoteInfoEntry._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mNoteId)};

                String[] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT
                };
                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns,selection,
                        selectionArgs,null, null, null);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId() == LOADER_NOTES) {
            loadFinishedNotes(cursor);
        }
        else if(loader.getId() == LOADER_COURSES) {
            adapterCourses.changeCursor(cursor);
            coursesQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }
    }

    private void loadFinishedNotes(Cursor cursor) {
        noteCursor = cursor;
        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        noteCursor.moveToNext();
        notesQueryFinished = true;
        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
        if(notesQueryFinished && coursesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES){
            if(noteCursor != null)
                noteCursor.close();
            else if (loader.getId() == LOADER_COURSES){
                adapterCourses.changeCursor(null);
            }
        }
    }
}
