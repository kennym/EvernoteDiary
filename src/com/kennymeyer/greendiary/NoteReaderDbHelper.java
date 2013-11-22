package com.kennymeyer.greendiary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.kennymeyer.greendiary.NoteContract.NoteEntry;

public class NoteReaderDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "NoteReader.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String UNIQUE = " NOT NULL UNIQUE";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + NoteEntry.TABLE_NAME + " (" +
                    NoteEntry._ID + " INTEGER PRIMARY KEY," +
                    NoteEntry.COLUMN_GUID + TEXT_TYPE + UNIQUE + COMMA_SEP +
                    NoteEntry.COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                    NoteEntry.COLUMN_CONTENT + TEXT_TYPE + COMMA_SEP +
                    NoteEntry.COLUMN_CREATED_AT + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_NOTES_TABLE =
            "DROP TABLE IF EXISTS " + NoteEntry.TABLE_NAME;

    public NoteReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_NOTES_TABLE);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
