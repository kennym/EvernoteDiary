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
import java.util.List;
import java.util.ArrayList;


public class MainActivity extends Activity {
	private static final String CONSUMER_KEY = "kennymeyer-7705";
	private static final String CONSUMER_SECRET = "4ef2aa3a5e4dc2eb";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    private ListView notesListView;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> notesNames;
    private ProgressDialog progress;

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
        notesNames = new ArrayList();
        notesListView = (ListView) findViewById( R.id.notesListView );

        try{
            showLoadingSpinner();
            listNotebooks();
        } catch (TTransportException exception) {
            Log.e("Error", "Error retrieving notebooks", exception);
            stopLoadingSpinner();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
        progress.setMessage("Wait while loading...");
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

            int offset = 0;
            int pageSize = 40;

            mEvernoteSession.getClientFactory().createNoteStoreClient().findNotesMetadata(filter, offset, pageSize, spec, new OnClientCallback<NotesMetadataList>() {
                @Override
                public void onSuccess(NotesMetadataList data) {
                    for (NoteMetadata note : data.getNotes()) {
                        String title = note.getTitle();
                        notesNames.add(title);
                    }

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
        listAdapter = new ArrayAdapter<String>(this, R.layout.note_row, notesNames);

        notesListView.setAdapter( listAdapter );
        stopLoadingSpinner();
    }
}
