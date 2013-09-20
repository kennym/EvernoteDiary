package com.kennymeyer.greendiary;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.app.ProgressDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.text.TextUtils;
import android.util.Log;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.thrift.transport.TTransportException;

import java.util.*;

import android.widget.SimpleAdapter;


public class MainActivity extends Activity {
	private static final String CONSUMER_KEY = "kennymeyer-7705";
	private static final String CONSUMER_SECRET = "4ef2aa3a5e4dc2eb";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    private ListView notesListView;
    private SimpleAdapter listAdapter;
    private List<Map<String, String>> notes;
    private ProgressDialog progress;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

	protected EvernoteSession mEvernoteSession;
    protected Notebook mDiaryNotebook;


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
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        try{
            showLoadingSpinner();
            listNotebooks();
        } catch (TTransportException exception) {
            Log.e("Error", "Error retrieving notebooks", exception);
            stopLoadingSpinner();
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
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

    public void showLoadingSpinner() {
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Loading diary entries...");
        progress.show();
    }

    public void stopLoadingSpinner() {
        progress.dismiss();
    }

    public void listNotebooks() throws TTransportException {
        if (mEvernoteSession.isLoggedIn()) {
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
                    for (NoteMetadata note : data.getNotes()) {
                        String title = note.getTitle();
                        Long created_at = note.getCreated();
                        Map<String, String> new_note = new HashMap<String, String>(2);
                        new_note.put("title", title);
                        new_note.put("created_at", created_at.toString());
                        notes.add(new_note);
                    }

                    // Sort the array items in descending order of created_at
                    Collections.sort(notes, new Comparator<Map<String, String>>() {
                        @Override
                        public int compare(Map<String, String> first, Map<String, String> second) {
                            return second.get("created_at").compareTo(first.get("created_at"));
                        }
                    });

                    renderNotesList();
                }

                @Override
                public void onException(Exception exception) {
                    stopLoadingSpinner();
                    Toast.makeText(getApplicationContext(), "Error retrieving notes.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void renderNotesList() {
        listAdapter = new SimpleAdapter(this, notes, R.layout.note_row, new String[] {
                "title"
            }, new int[] {
                R.id.rowTextView
            });

        notesListView.setAdapter( listAdapter );
        notesListView.setOnItemClickListener(new NotesListItemClickListener());

        stopLoadingSpinner();
    }

    private class NotesListItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectNote(position);
        }
    }

    private void selectNote(int position) {
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        notesListView.setItemChecked(position, true);
        setTitle(notes.get(position).get("title"));
        mDrawerLayout.closeDrawer(notesListView);
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
}
