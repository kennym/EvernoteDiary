package com.kennymeyer.greendiary;

import android.provider.BaseColumns;

public class NoteContract {
    public NoteContract() {}

    public static abstract class NoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_GUID = "guid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_NULLABLE = "null";
    }

}
