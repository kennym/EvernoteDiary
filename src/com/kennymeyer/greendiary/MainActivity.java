package com.kennymeyer.greendiary;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.Menu;
import android.app.ProgressDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.thrift.transport.TTransportException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import com.kennymeyer.greendiary.NoteContract.NoteEntry;

import android.widget.SimpleAdapter;


public class MainActivity extends Activity {
	private static final String CONSUMER_KEY = "kennymeyer-7705";
	private static final String CONSUMER_SECRET = "4ef2aa3a5e4dc2eb";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    private ListView notesListView;
    private SimpleAdapter listAdapter;
    private List<Map<String, String>> notes;
    private ProgressDialog progress;
    protected DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    protected CharSequence mDrawerTitle;
    protected CharSequence mTitle;

	protected EvernoteSession mEvernoteSession;
    protected Notebook mDiaryNotebook;
    protected NoteReaderDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEvernoteSession = EvernoteSession.getInstance(this, CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_SERVICE);
        if (!mEvernoteSession.isLoggedIn()) {
            mEvernoteSession.authenticate(this);
        }


        notes = new ArrayList<Map<String, String>>();
        notesListView = (ListView) findViewById( R.id.notesListView );

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_launcher,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getNotesFromDatabase();

        if (notes.isEmpty()) {
            try{
                listNotebooks();
                renderNotesList();
            } catch (TTransportException exception) {
                Log.e("Error", "Error retrieving notebooks", exception);
                stopLoadingSpinner();
            }
        } else {
            renderNotesList();
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        switch (item.getItemId()) {
            case R.id.action_sync:
                syncNotes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(notesListView);
        menu.findItem(R.id.action_sync).setVisible(true);
        menu.findItem(R.id.action_save).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case EvernoteSession.REQUEST_CODE_OAUTH:
                if (resultCode == Activity.RESULT_OK) {
                    setContentView(R.layout.activity_main);
                }
                break;
            default:
                break;
        }
    }

    public void getNotesFromDatabase() {
        try {
            mDbHelper = new NoteReaderDbHelper(getApplicationContext());
            SQLiteDatabase mDbReadable = mDbHelper.getReadableDatabase();

            Log.v("GreenDiary", "Loaded database...");

            String[] projection = {
                    NoteEntry.COLUMN_GUID,
                    NoteEntry.COLUMN_TITLE,
                    NoteEntry.COLUMN_CREATED_AT
            };
            String sortOrder = NoteEntry.COLUMN_CREATED_AT + " DESC";

            Cursor c = mDbReadable.query(
                    NoteEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder
            );


            if (c.moveToFirst()) {
                do {
                    Map<String, String> new_note = new HashMap<String, String>(3);

                    String guid = c.getString(0);
                    String title = c.getString(1);
                    String created_at = c.getString(2);

                    new_note.put("guid", guid);
                    new_note.put("title", title);
                    new_note.put("created_at", created_at);

                    notes.add(new_note);
                } while(c.moveToNext());

                sortNotes();
            }

            c.close();

        } catch (Exception e) {
            Log.e("GreenDiary", e.toString());
        }
    }

    /* Adds note to database */
    public void addNote(Note note) {
        SQLiteDatabase mDbWritable = mDbHelper.getWritableDatabase();
        String title = note.getTitle();
        String guid = note.getGuid();
        String created_at = String.valueOf(note.getCreated());

        Map<String, String> new_note = new HashMap<String, String>(3);

        new_note.put("guid", guid);
        new_note.put("title", title);
        new_note.put("created_at", created_at);

        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_GUID, guid);
        values.put(NoteContract.NoteEntry.COLUMN_TITLE, title);
        values.put(NoteContract.NoteEntry.COLUMN_CREATED_AT, created_at);

        mDbWritable.insert(
                NoteContract.NoteEntry.TABLE_NAME,
                NoteContract.NoteEntry.COLUMN_NAME_NULLABLE,
                values);

        notes.add(new_note);
        mDbWritable.close();

        sortNotes();
    }

    /* Override sync notes from Evernote */
    public void syncNotes() {
        showLoadingSpinner();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                stopLoadingSpinner();
            }
        }, 2000);
    }


    public void showLoadingSpinner() {
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Loading diary entries...");
        progress.show();
    }

    public void stopLoadingSpinner() {
        if (progress != null) progress.dismiss();
    }

    public void listNotebooks() throws TTransportException {
        if (mEvernoteSession.isLoggedIn()) {
            showLoadingSpinner();
            mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>() {
                @Override
                public void onSuccess(final List<Notebook> notebooks) {
                    for (Notebook notebook : notebooks) {
                        if (notebook.getName().equals("Diary")) {
                            mDiaryNotebook = notebook;
                        }
                    }
                    Toast.makeText(getApplicationContext(), "Diary has been retrieved", Toast.LENGTH_LONG).show();

                    try{
                        getNotes();
                    } catch (TTransportException exception) {
                        Toast.makeText(getApplicationContext(), "Error retrieving notes.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onException(Exception exception) {
                    Log.e("Test", "Error retrieving notebooks", exception);
                    stopLoadingSpinner();
                }
            });
        }
    }

    public void sortNotes() {
        // Sort the array items in descending order of created_at
        Collections.sort(notes, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> first, Map<String, String> second) {
                return second.get("created_at").compareTo(first.get("created_at"));
            }
        });
    }

    public void getNotes() throws TTransportException {
        if (mEvernoteSession.isLoggedIn()) {
            NoteFilter filter = new NoteFilter();
            filter.setNotebookGuid(mDiaryNotebook.getGuid());

            NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
            spec.setIncludeTitle(true);
            spec.setIncludeCreated(true);

            int offset = 0;
            int pageSize = 40;

            mEvernoteSession.getClientFactory().createNoteStoreClient().findNotesMetadata(filter, offset, pageSize, spec, new OnClientCallback<NotesMetadataList>() {
                @Override
                public void onSuccess(NotesMetadataList data) {
                    SQLiteDatabase mDbWritable = mDbHelper.getWritableDatabase();
                    for (NoteMetadata note : data.getNotes()) {
                        Note note_ = new Note();
                        note_.setTitle(note.getTitle());
                        note_.setGuid(note.getGuid());
                        note_.setCreated(note.getCreated());

                        addNote(note_);
                    }
                    mDbWritable.close();

                    sortNotes();

                    stopLoadingSpinner();
                }

                @Override
                public void onException(Exception exception) {
                    stopLoadingSpinner();
                    Toast.makeText(getApplicationContext(), "Error retrieving notes.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }

    private boolean noteCreatedToday(Date date) {
        Date startOfDay = getStartOfDay(new Date());
        // Check if
        if (date.after(startOfDay) && date.before(new Date())) {
            return true;
        } else {
            return false;
        }
    }

    /* Creates or uses existing note from today's date */
    public void addTodayNote() {
        Date created_at = new Date(Long.valueOf(notes.get(0).get("created_at")));

        if (noteCreatedToday(created_at)) {
            // Do nothing
        } else {
            Map<String, String> new_note = new HashMap<String, String>(3);

            DateFormat dateFormat = new SimpleDateFormat("E, MMM d");
            String title = dateFormat.format(new Date());
            new_note.put("title", title);

            notes.add(0, new_note);
        }
    }

    public void renderNotesList() {
        if (!notes.isEmpty()) {
            addTodayNote();
        }

        listAdapter = new SimpleAdapter(getBaseContext(), notes, R.layout.note_row, new String[] {
                "title"
            }, new int[] {
                R.id.rowTextView
            });

        notesListView.setAdapter( listAdapter );
        notesListView.setOnItemClickListener(new NotesListItemClickListener());
    }

    private class NotesListItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectNote(position);
        }
    }

    private void selectNote(int position) {
        if (position == 0 && notes.get(position).get("guid") == null) {  // Today note
            Fragment fragment = new NoteFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();

            setTitle(notes.get(0).get("title"));
        } else {
            try {
                showLoadingSpinner();
                String guid = notes.get(position).get("guid");
                mEvernoteSession.getClientFactory().createNoteStoreClient().getNote(guid, true, true, false, false, new OnClientCallback<Note>() {
                    @Override
                    public void onSuccess(Note note) {
                        Fragment fragment = new NoteFragment();
                        Bundle args = new Bundle();
                        args.putString("content", note.getContent().toString());
                        fragment.setArguments(args);

                        // Insert the fragment by replacing any existing fragment
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, fragment)
                                .commit();

                        setTitle(note.getTitle());
                        stopLoadingSpinner();
                    }

                    @Override
                    public void onException(Exception e) {
                        Log.d("GreenDiary", "Oops");
                    }
                });
                // Highlight the selected item, update the title, and close the drawer
            } catch (TTransportException e) {
                stopLoadingSpinner();
                Toast.makeText(getApplicationContext(), "Error fetching note", Toast.LENGTH_LONG).show();
            }
        }
        mDrawerLayout.closeDrawer(notesListView);
        notesListView.setItemChecked(position, true);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    public void syncNote() {

    }
}
